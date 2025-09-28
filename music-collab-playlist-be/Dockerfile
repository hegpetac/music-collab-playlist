FROM eclipse-temurin:25
RUN mkdir /opt/app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} opt/app/app.jar
CMD ["java", "-jar", "/opt/app/app.jar"]