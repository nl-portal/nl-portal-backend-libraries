FROM openjdk:13.0.2

ADD /build/libs/*.jar /app.jar

RUN echo "yes" | keytool -import -keystore /usr/java/openjdk-13/lib/security/cacerts -alias Haal-Centraal -file /wsgateway.ot.denhaag.nl_2022.cer -storepass changeit

ENTRYPOINT ["java","-Xms1024M","-Xmx1024M","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]