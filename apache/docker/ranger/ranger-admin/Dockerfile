
FROM maven:3.5.3-jdk-8
#FROM openjdk:8u121-jdk

LABEL maintainer-twitter="@coheigea"
LABEL version=1.1.0

ARG RANGER_VERSION=2.0.0-SNAPSHOT

ADD https://jdbc.postgresql.org/download/postgresql-42.2.1.jar /opt

ENV RANGER_INSTALLATION_DIR=/usr/local
ENV RANGER_HOME=/usr/local/ranger-admin
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/

COPY ranger-${RANGER_VERSION}-admin.tar.gz ${RANGER_INSTALLATION_DIR}
WORKDIR ${RANGER_INSTALLATION_DIR}
RUN apt-get -q update && apt-get install -y -q python gcc bc \
 && tar zxvf ranger-${RANGER_VERSION}-admin.tar.gz \
 && ln -s ranger-${RANGER_VERSION}-admin ranger-admin

COPY ranger-entrypoint.sh ${RANGER_HOME}
RUN chmod +x ${RANGER_HOME}/ranger-entrypoint.sh

COPY install.properties ${RANGER_HOME}

EXPOSE 6080

ENTRYPOINT ["bash","-c","/${RANGER_HOME}/ranger-entrypoint.sh"]
