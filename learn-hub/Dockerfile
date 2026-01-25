FROM gradle:8.5-jdk17 AS build
WORKDIR /build
COPY . .
RUN ./gradlew build

FROM amazoncorretto:17-alpine
WORKDIR /app
COPY --from=build /build/build/libs/*.jar app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

