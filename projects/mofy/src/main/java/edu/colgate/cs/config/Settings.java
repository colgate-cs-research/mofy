package edu.colgate.cs.config;

import edu.colgate.cs.mofy.Mofy;
import org.apache.commons.cli.*;

/**
 * Stores and parses settings for Network Config Modifications.
 */

public class Settings {

    private static final String HELP ="help";

    private static final String CONFIGS_DIRECTORY = "configs";
    private static final String OUTPUT_DIRECTORY = "outputDir";

    /** Where are the configuration files stored? */
    private String configsDirectory;

    /** Where are the modified configuration files to be stored? */
    private String outputDirectory;

    public Settings(String[] args) throws ParseException{
        Options options = this.getOptions();

        for (String arg : args) {
            if (arg.equals("-"+HELP)) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.setLongOptPrefix("-");
                formatter.printHelp(Mofy.class.getName(), options, true);
                System.exit(0);
            }
        }

        CommandLineParser parser = new DefaultParser();
        CommandLine line =null;
        line = parser.parse(options, args);

        configsDirectory = line.getOptionValue(CONFIGS_DIRECTORY);
        outputDirectory = line.getOptionValue(OUTPUT_DIRECTORY);
    }


    /**
     * Set up the list of command line arguments the program accepts.
     * @return arguments the program accepts
     */
    private Options getOptions(){
        Options options = new Options();

        options.addOption(HELP,false,
                "Print Usage Information");

        Option option = new Option(CONFIGS_DIRECTORY, true,
                "Directory containing configuration files to be modified");
        option.setRequired(true);
        option.setArgName("DIR");
        options.addOption(option);

        option = new Option(OUTPUT_DIRECTORY, true,
                "Directory where modified configurations are to be stored");
        option.setArgName("OUTPUT");
        option.setRequired(true);
        options.addOption(option);

        return options;
    }

    /**
     * Determine where configuration files are stored.
     * @return the path to a directory containing the configuration files
     */
    public String getConfigsDirectory() {
        return configsDirectory;
    }

    /**
     * Determine where modified configuration files are to be stored.
     * @return the path to a directory where the modified
     * configuration files should be stored.
     */
    public String getOutputDirectory() {
        return outputDirectory;
    }


}
