package org.apache.coheigea.santuario.benchmarks;
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.w3c.dom.Document;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)

/**
 * Some benchmarking tests for XML Encryption
 */
public class EncryptionBenchmark {
	
	@State(Scope.Thread)
    public static class EncryptionState {
		Key key;
		X509Certificate cert;
		Document doc;
		SecretKey secretKey;
		
		public EncryptionState() {
			try {
				KeyStore keyStore = KeyStore.getInstance("jks");
		        keyStore.load(
		            this.getClass().getClassLoader().getResource("clientstore.jks").openStream(),
		            "cspass".toCharArray()
		        );
		        key = keyStore.getKey("myclientkey", "ckpass".toCharArray());
		        cert = (X509Certificate)keyStore.getCertificate("myclientkey");
		        
		        // Set up the secret Key
		        KeyGenerator keygen = KeyGenerator.getInstance("AES");
		        keygen.init(256);
		        secretKey = keygen.generateKey();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	@Benchmark
    @Fork(1)
    @Warmup(iterations = 5)
	@Threads(10)
    public void testEncryption(EncryptionState state) throws Exception {
		 // Read in plaintext document
        InputStream sourceDocument =
                this.getClass().getClassLoader().getResourceAsStream("plaintext.xml");
        
        // Note this is very inefficient to to this every time - but I don't care, I just want to 
        // compare consistent performance across different Santuario versions
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document document = dbf.newDocumentBuilder().parse(sourceDocument);

        // Encrypt using DOM
        EncryptionUtils.encryptUsingDOM(
        		document, "http://www.w3.org/2001/04/xmlenc#aes256-cbc", state.secretKey,
        		"http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p", state.cert.getPublicKey(), true
        );

        // XMLUtils.outputDOM(document, Files.newOutputStream(Paths.get("encrypted.xml")));
    }
	
	@Benchmark
    @Fork(1)
    @Warmup(iterations = 5)
	@Threads(1)
    public void testDecryption(EncryptionState state) throws Exception {
		 // Read in signed document
        InputStream sourceDocument =
                this.getClass().getClassLoader().getResourceAsStream("encrypted.xml");

        // Note this is very inefficient to to this every time - but I don't care, I just want to 
        // compare consistent performance across different Santuario versions
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document document = dbf.newDocumentBuilder().parse(sourceDocument);

        // Verify using JSR-105
        EncryptionUtils.decryptUsingDOM(document, "http://www.w3.org/2001/04/xmlenc#aes256-cbc", state.key);
    }
	
}
