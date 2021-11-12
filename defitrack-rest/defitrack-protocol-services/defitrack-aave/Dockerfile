FROM openjdk:15.0.2-slim

ADD target/defitrack-aave-1.0.1-SNAPSHOT.jar defitrack.jar
EXPOSE 8080

ENTRYPOINT [ "sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -jar /defitrack.jar" ]