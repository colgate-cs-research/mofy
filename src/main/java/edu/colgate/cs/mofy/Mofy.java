package edu.colgate.cs.mofy;

import edu.colgate.cs.config.Settings;
import edu.colgate.cs.modification.*;
import org.apache.commons.cli.ParseException;
import org.apache.commons.collections4.list.TreeList;
import org.batfish.datamodel.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class Mofy{

    //Find the set (call CHANGES) all possible (independent) legal changes
    // that may be made to the configuration.
    //For each applicable change in P(CHANGES), apply the change and save
    // modified config.

    private Settings settings;
    private List<Path> configPaths;
    private List<Config> configs;
    private int percentage;
    private long seed;
    private boolean _cfg = false;
    private Settings.modtype mod;


    public Mofy(String[] args) {
        try {
            settings = new Settings(args);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        configs = new ArrayList<>();
    }


    public void run(){

        try {
            configPaths =listConfigFiles(Paths.get(settings.getConfigsDirectory()));
        }catch(Exception e){
            e.printStackTrace();
        }

        for (Path cfgFilePath: configPaths){
            if (cfgFilePath.toString().endsWith("cfg"))
                _cfg = true;
            File file = new File(cfgFilePath.toString());

            try {
                configs.add(new Config(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.percentage = settings.getPercent();
        this.seed = settings.getSeed();
        this.mod = settings.getmod();
        SwapModifier swapmodifier;
        Modifier modifier;
        Config newconfig;

        switch(this.mod){
          case Swap:
            swapmodifier = new SwapModifier(configs,settings);
            for (Config config: configs){
              newconfig = swapmodifier.modify(config);
              if (settings.getOutputDirectory()!=null){
                  System.out.printf("Generating modified configs in : %s\n", settings.getOutputDirectory());
                  swapmodifier.generateModifiedConfigs(settings.getOutputDirectory(), newconfig, _cfg);
              }
            }
            break;
          default:
            modifier = new Modifier(configs,settings);
            for (Config config: configs){
              newconfig = modifier.modify(config);
              if (settings.getOutputDirectory()!=null){
                  System.out.printf("Generating modified configs in : %s\n", settings.getOutputDirectory());
                  modifier.generateModifiedConfigs(settings.getOutputDirectory(), newconfig, _cfg);
              }
            }
            break;
        }
      }



    private List<Path> listConfigFiles(Path cfgDirPath){
        File configsDir = cfgDirPath.toFile();
        File[] cfgFiles = configsDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return true;
            }
        });
        List<Path> cfgPaths = new ArrayList<>();
        for (File cfgFile: cfgFiles){
            cfgPaths.add(cfgFile.toPath());
        }
        return cfgPaths;
    }
}
