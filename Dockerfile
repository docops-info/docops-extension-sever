FROM openjdk:18-jdk-slim
EXPOSE 8010:8010
COPY ./target/*-with-dependencies.jar /tmp/docops-extension-server.jar
ENTRYPOINT ["java","-jar","/tmp/docops-extension-server.jar"]