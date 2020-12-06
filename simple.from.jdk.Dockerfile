FROM openjdk:8-jdk-alpine
MAINTAINER AK-M
ADD /target/rest1-0.0.1-SNAPSHOT.jar /app/rest1.jar
ENTRYPOINT [ "java", "-jar", "/app/rest1.jar" ]