###Mofy
Mofy is a network configuration tool that performs random, legal modifications of configuration files of networks.

#####Prerequisites
* ANTLR
* batfish
* ARC
* Z3

#####Compilation
1. Clone and install **batfish**.
2. Obtain `arc.jar` and run `mvn install:install-file-Dfile=src/main/resources/batfish-<version>.jar -DgroupId=org.batfish -DartifactId=batfish -Dversion=<version> -Dpackaging=jar`
3. Run `mvn install`
