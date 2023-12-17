# Custom Search Engine
Built with an inverted file. Front end uses SpringBoot + Thymleaf to display and SpringBoot as the model in MVC.


### Link to page hosting the engine on Google Cloud Run: 
https://search-azndw25kaq-uc.a.run.app

I am not a web designer, I just wanted to add a basic front end to it and learn
Springboot. 
### To import your own crawled web pages
Use https://github.com/TrueFMartin/SearchEngineInvertedFile, to build the inverted file. 
The buildscript, build.sh, can be used along with an input directory and output directory.

`./build.sh infiles outfiles` : where infiles contain your collection of .html files

This will build the inverted file in the config/ folder. 

Using this project, transfer over the config files from SearchEngineInvertedFile
into config/ folder. Transfer your collection of html files, assuming you want
them displayed when a result is found, in src/main/resources/templates/cachedfiles

### To Build

Run `mvn package` to build the jar file which contains your cached html files and the source code

Run `docker build -t [What you want to name it] .` : the '.' at the end assumes you are in the project root

This will add you config files to the docker image as well as the jar file build using maven.

### To Run 

`docker run [What you named your docker image] -p 8080` to foward port 8080.
Open at localhost:8080. 
