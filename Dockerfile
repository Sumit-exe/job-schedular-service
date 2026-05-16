FROM eclipse-temurin:17-jdk

RUN apt-get update && apt-get install -y maven

WORKDIR /app
COPY . .

RUN mvn clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/jobschedular-0.0.1-SNAPSHOT.jar"]