FROM  --platform=linux/amd64 openjdk:17-jdk-alpine
LABEL authors="truefmartin"
LABEL description="Spring and Sql project"
COPY target/query-api.jar /query-api.jar
ADD https://cockroachlabs.cloud/clusters/2a6d3958-661a-4c70-8a1e-22e342cb0cca/cert /root/.postgresql/root.crt
EXPOSE 8080
ENTRYPOINT ["java","-jar","/query-api.jar"]