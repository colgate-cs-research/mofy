package edu.colgate.cs.config;

import java.io.File;

public class Settings {

    public Settings(String[] args){
        //TODO: Fill-in
        setConfigsDir(args[0]);
        outputFile = new File(args[1]);
    }

    /* Makeshift methods TODO: Properly Implement Settings*/

    String configsDir = "";


    File outputFile;

    public void setConfigsDir(String configsDir){
        this.configsDir = configsDir;
    }

    public String getConfigsDir(){
        return configsDir;
    }

    public File getOutputDir() {
        return outputFile;
    }

}
