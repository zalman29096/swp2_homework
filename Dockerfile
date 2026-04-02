FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests=false clean package

FROM eclipse-temurin:17-jre
ENV LANG=C.UTF-8
WORKDIR /app
COPY --from=builder /app/target/swp2_homework-1.0-SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
