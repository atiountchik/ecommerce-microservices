FROM maven:3.8.3-openjdk-17-slim

RUN addgroup spring && useradd -m product-docker-user
USER product-docker-user:spring

WORKDIR /home/product-docker-user

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-Dspring.profiles.active=dev", "-jar","/home/product-docker-user/app.jar"]
