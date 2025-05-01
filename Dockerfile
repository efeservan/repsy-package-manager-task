# Dockerfile
FROM eclipse-temurin:17-jdk as build

WORKDIR /app

RUN apt-get update && apt-get install -y maven
RUN mkdir -p /root/.m2

COPY pom.xml .
RUN mvn dependency:go-offline -Dproject.build.sourceEncoding=UTF-8

COPY src ./src
# UTF-8 encoding ile derle
ENV MAVEN_OPTS="-Dfile.encoding=UTF-8"
RUN mvn package -DskipTests -Dproject.build.sourceEncoding=UTF-8

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p /data/packages

EXPOSE 8098

ENTRYPOINT ["java", "-jar", "app.jar"]