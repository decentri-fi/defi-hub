FROM openjdk:15.0.2

VOLUME /tmp
ADD target/defitrack-abi-0.0.1-SNAPSHOT.jar defitrack.jar
RUN sh -c 'touch /defitrack.jar' && \
    mkdir config

ENV JAVA_OPTS=""

EXPOSE 8080

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /defitrack.jar" ]