FROM  --platform=linux/amd64 openjdk:17-jdk-alpine
LABEL authors="truef"
COPY target/query-api.jar /query-api.jar
COPY ./config/* /
EXPOSE 8080
ENTRYPOINT ["java","-jar","/query-api.jar"]
