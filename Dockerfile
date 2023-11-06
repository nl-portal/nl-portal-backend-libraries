FROM amazoncorretto:17-alpine-jdk as build

WORKDIR /app

COPY . /app
RUN chmod +x gradlew
RUN ./gradlew app:clean app:bootJar
RUN cp app/build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
EXPOSE 8090
