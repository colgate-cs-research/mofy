# Mofy
Mofy is a network configuration tool that performs random, legal modifications of configuration files of networks.

## Prerequisites
* ANTLR
* batfish
* Z3

## Compilation
1. Clone and install [batfish](https://github.com/batfish/batfish).
2. `cd projects/mofy`
3. Compile mofy:
`mvn install`

## Running
1. `cd projects/mofy/target`
2. `java -jar mofy-1.0-jar-with-dependencies.jar -configs <CONFIGS DIR> -outputDir <PATH_TO_OUTPUT_DIR>`

OR `java -cp "projects/mofy/target/mofy-1.0-jar-with-dependencies.jar -configs <CONFIGS DIR> -outputDir <PATH_TO_OUTPUT_DIR>"`

## New version running
`java -jar mofy-1.0-jar-with-dependencies.jar -configs <CONFIGS DIR> -outputDir <PATH_TO_OUTPUT_DIR> -Modification <TYPE_OF_MODIFICATION> -Percentage <CHANCE> -seed <RANDOMSEED>``
