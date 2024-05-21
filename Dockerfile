FROM eclipse-temurin:17-jdk-alpine
WORKDIR /project
ADD . .
RUN ./gradlew build
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/project/app.jar"]

