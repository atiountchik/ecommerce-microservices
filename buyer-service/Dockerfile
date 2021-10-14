FROM maven:3.8.3-openjdk-17-slim

RUN addgroup spring && useradd -m buyer-docker-user
USER buyer-docker-user:spring

WORKDIR /home/buyer-docker-user

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-Dspring.profiles.active=dev", "-jar","/home/buyer-docker-user/app.jar"]
