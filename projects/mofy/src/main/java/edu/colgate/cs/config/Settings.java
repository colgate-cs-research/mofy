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
    private static final String Mofychoice = "Modification";
    private static final String PERCENT = "Percentage";
    private static final String SEED = "seed";

    /** Where are the configuration files stored? */
    private String configsDirectory;

    /** Where are the modified configuration files to be stored? */

    public enum modtype{
      Ip, Permit, Subnet, Swap
    }

    private String outputDirectory;

    // private boolean Aclmodification = false;
    //
    // private boolean Ipmodification = false;
    //
    // private boolean Permitmodification = false;
    //
    // private boolean Subnetmodification = false;
    //
    // private boolean Swapmodification = false;

    private int percent;

    private long seed;

    private String modifications;

    private modtype mod;

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
        modifications = line.getOptionValue(Mofychoice);
        percent = Integer.parseInt(line.getOptionValue(PERCENT));
        seed = Integer.parseInt(line.getOptionValue(SEED));


        switch(modifications){
          case "Permit":
            mod = modtype.Permit;
            break;
          case "Subnet":
            mod = modtype.Subnet;
            break;
          case "Ip":
            mod = modtype.Ip;
            break;
          case "Swap":
            mod = modtype.Swap;
            break;
          default:
            System.out.println("invalid modification type");
            break;
        }

        // if (modifications.equals("ACL")){
        //   this.Aclmodification = true;
        // }
        // else if (modifications.equals("Permit")){
        //   this.Permitmodification = true;
        // }
        // else if (modifications.equals("Subnet")){
        //   this.Subnetmodification = true;
        // }
        // else if (modifications.equals("Swap")){
        //   this.Swapmodification = true;
        // }
        // else if (modifications.equals("Ip")){
        //   this.Ipmodification = true;
        // }

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

        option = new Option(Mofychoice, true,"The modifications to be made");
        option.setArgName("Choice");
        option.setRequired(true);
        options.addOption(option);

        option = new Option(PERCENT, true, "Percent of the modification happens");
        option.setArgName("PERCENTAGE");
        option.setRequired(true);
        options.addOption(option);

        option = new Option(SEED, true, "The seed to generate random number");
        option.setArgName("SEED");
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

    public modtype getmod(){
      return mod;
    }

    // public boolean getacl(){
    //   return Aclmodification;
    // }
    //
    // public boolean getpermit(){
    //   return Permitmodification;
    // }
    //
    // public boolean getIp(){
    //   return Ipmodification;
    // }
    //
    // public boolean getsubnet(){
    //   return Subnetmodification;
    // }
    //
    // public boolean getswap(){
    //   return Swapmodification;
    // }

    public int getPercent(){
      return percent;
    }

    public long getSeed(){
      return seed;
    }
}
