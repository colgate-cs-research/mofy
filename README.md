# Mofy
Mofy is a network configuration tool that performs random, legal modifications of configuration files of networks.

## Compile
* Dependencies
    * Maven
    * ANTLR (automatically installed via Maven)
    * [batfish](https://github.com/colgate-cs-research/batfish)
* Compile mofy using Maven: `mvn install`

## Running
`java -jar mofy-1.0-jar-with-dependencies.jar -configs <CONFIGS DIR> -outputDir <PATH_TO_OUTPUT_DIR> -Modification <TYPE_OF_MODIFICATION> -Percentage <CHANCE> -seed <RANDOMSEED>`

For details about parameters, run: 
`java -jar mofy-1.0-jar-with-dependencies.jar -h`

## Testing
A test suite resides in the `test` directory. 

To run the test suite, run: `test/test.sh`.

To add a new test case, create a new directory in `test/cases`. Inside the
new directory, create a BASH script called `run.sh` that contains the commands
needed to run the test case; see the `run.sh` script for existing tests for
examples. All files required for the test case (e.g., input configurations and
expected outputs) should be placed in the directory for the case.
