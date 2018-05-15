package edu.colgate.cs.mofy;

import edu.colgate.cs.config.Settings;
import edu.wisc.cs.arc.Logger;
import edu.wisc.cs.arc.configs.Config;
import org.batfish.datamodel.*;
import org.batfish.main.Batfish;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Mofy{

    //Find the set (call CHANGES) all possible (independent) legal changes
    // that may be made to the configuration.
    //For each applicable change in P(CHANGES), apply the change and save
    // modified config.

    private Settings settings;
    private List<Path> configPaths;
    private List<Config> configs;


    public Mofy(String[] args) {
       settings = new Settings(args);
       configs = new ArrayList<>();
    }

    public void run(){
        try {
            configPaths = Batfish.listAllFiles(Paths.get(settings.getConfigsDir()));
        }catch(Exception e){
            e.printStackTrace();
        }

        /* Create Configuration Files */
        for (Path cfgFilePath: configPaths){
            if (!cfgFilePath.toString().endsWith("cfg"))
                continue;
            File file = new File(cfgFilePath.toString());

            try {
                Config config = new Config(file, Logger.getInstance(Logger.Level.DEBUG));
                configs.add(config);

//                (new ASTModifier(config, settings.getOutputFile())).modify();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        deduceACLModifications();

    }


    /*
     * Populate
     */
    private void deduceACLModifications(){
        Configuration genericConfiguration;
        for (Config config: configs){
            genericConfiguration = config.getGenericConfiguration();
            Map<String, Interface> interfaceMap = genericConfiguration.getInterfaces();
            for (String interfaceName : interfaceMap.keySet()){
                Interface iface = interfaceMap.get(interfaceName);
                System.out.println(iface.getPrefix().getNetworkPrefix());
            }
        }
    }

}
