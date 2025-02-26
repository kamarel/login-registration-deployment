FROM openjdk:17-alpine
ADD target/login-registration-0.0.1-SNAPSHOT.jar login-registration-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","login-registration-0.0.1-SNAPSHOT.jar"]