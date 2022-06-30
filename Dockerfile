FROM openjdk:11-jdk-slim
VOLUME /tmp
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ARG SPRING_ENV
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_ENV}", "-Duser.timezone=Asia/Seoul", "-jar", "/app.jar"]