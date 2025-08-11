FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/Taskflow-0.0.1-SNAPSHOT.jar taskflow.jar
ENTRYPOINT ["java", "-jar", "taskflow.jar"]
