FROM openjdk:17-alpine

ENV ACTIVE_PROFILE=uat
ARG JAR_FILE=target/obc-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${ACTIVE_PROFILE}", "app.jar"]

EXPOSE 8080
