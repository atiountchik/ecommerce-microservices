FROM maven:3.8.3-openjdk-17-slim

RUN addgroup spring && useradd -m seller-docker-user
USER seller-docker-user:spring

WORKDIR /home/seller-docker-user

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-Dspring.profiles.active=dev", "-jar","/home/seller-docker-user/app.jar"]
