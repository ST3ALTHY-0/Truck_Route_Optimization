FROM eclipse-temurin:17-jdk-jammy
LABEL maintainer="monroe36@purdue.edu"
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

