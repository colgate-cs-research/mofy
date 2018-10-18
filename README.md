### Mofy
Mofy is a network configuration tool that performs random, legal modifications of configuration files of networks.

##### Prerequisites
* ANTLR
* batfish
* ARC
* Z3

##### Compilation
1. Clone and install [batfish](https://github.com/batfish/batfish).
2. `cd projects/mofy`
2. Install the ARC JAR into your local maven repository:
`mvn install:install-file -Dfile=lib/arc.jar -DgroupId=edu.wisc.cs -DartifactId=arc -Dversion=1.0 -Dpackaging=jar`
3. Compile mofy:
`mvn install`
