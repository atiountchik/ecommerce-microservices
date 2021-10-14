FROM maven:3.8.3-openjdk-17-slim

RUN addgroup spring && useradd -m payment-docker-user
USER payment-docker-user:spring

WORKDIR /home/payment-docker-user

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-Dspring.profiles.active=dev", "-jar","/home/payment-docker-user/app.jar"]
