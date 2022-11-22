FROM openjdk:13.0.2

ADD /build/libs/*.jar /app.jar

ENTRYPOINT ["java","-Xms1024M","-Xmx1024M","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]