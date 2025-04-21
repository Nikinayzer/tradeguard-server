FROM --platform=linux/amd64 eclipse-temurin:21-jre-jammy
VOLUME /tmp

# Copy the pre-built JAR from the host machine
COPY ./target/TradeGuardServer-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"] 