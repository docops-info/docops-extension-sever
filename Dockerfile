FROM maven:3.8.5-openjdk-18-slim AS build
COPY . /home/maven/src
WORKDIR /home/maven/src
RUN mvn package

FROM openjdk:18.0.2.1-slim
EXPOSE 8010:8010
RUN mkdir /app
COPY --from=build /home/maven/src/target/*-with-dependencies.jar /app/docops-extension-server.jar
ENTRYPOINT ["java","-jar","/app/docops-extension-server.jar"]