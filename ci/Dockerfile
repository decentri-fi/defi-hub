FROM openjdk:20 as builder

WORKDIR application
ARG JAR_FILE=target/defitrack-*-SNAPSHOT.jar
COPY ${JAR_FILE} application.jar
ADD target/newrelic/newrelic.jar /opt/newrelic.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM openjdk:20
WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
COPY --from=builder /opt/newrelic.jar /opt/newrelic.jar
ENTRYPOINT ["java", "-javaagent:/opt/newrelic.jar",  "org.springframework.boot.loader.JarLauncher"]
