# Combining Spring Boot+Thymleaf with base Hibernate instead of Spring's JPA
This was developed with the goal of learning how to include a database with a SpringBoot application. 
Front end uses SpringBoot endpoints as a controller and Thymleaf to template the displayed HTML. 
The back end uses Hibernate ORM to interact with a Postgres database. Most queries use HQL instead of raw SQL. 
The application is a maven built Jar hosted in a Docker container running on Google Cloud Run. It takes in environment variables through 
Google Cloud Secrets to set the DB configuration. The current database is a CockroachDB serverless instance provided by Cockroach Labs.    


### Link to page hosting the engine on Google Cloud Run: 
https://spring-and-sql-azndw25kaq-uc.a.run.app/

I am not a web designer, I just wanted to add a basic front end to improve as a developer.

### To Build

Run `mvn package` to build the jar file which contains your cached html files and the source code

Run `docker build -t [What you want to name it] .` : the '.' at the end assumes you are in the project root

### To Run 

`docker run [What you named your docker image] -p 8080` to forward port 8080.

Open at localhost:8080. 
