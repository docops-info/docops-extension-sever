FROM openjdk:17-jdk
EXPOSE 8010:8010
COPY ./target/docops-extension-server-2023.07.jar /tmp/docops-extension-server.jar
ENTRYPOINT ["java","-jar","/tmp/docops-extension-server.jar"]