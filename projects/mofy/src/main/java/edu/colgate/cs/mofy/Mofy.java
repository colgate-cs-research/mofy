package edu.colgate.cs.mofy;

import edu.colgate.cs.config.Settings;
import edu.colgate.cs.modification.ModifierSetting;
import edu.colgate.cs.modification.Modifier;
import edu.colgate.cs.modification.IpModifier;
import edu.colgate.cs.modification.PermitModifier;
import edu.colgate.cs.modification.SubnetModifier;
import edu.colgate.cs.modification.SwapModifier;
import edu.colgate.cs.modification.Config;
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

    private PermitModifier permitmodifier;

    private SubnetModifier subnetmodifier;

    private SwapModifier swapmodifier;

    private IpModifier ipmodifier;

    private Modifier modifier;

    private ModifierSetting ModifierSetting;

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
            // if (!cfgFilePath.toString().endsWith("cfg"))
            //     continue;
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
        this.ModifierSetting = new ModifierSetting (this.percentage, this.seed);

        switch(this.mod){
          case Permit:
            this.modifier = new PermitModifier(configs,settings);
            break;
          case Subnet:
            this.modifier = new SubnetModifier(configs,settings);
            break;
          case Ip:
            this.modifier = new IpModifier(configs,settings);
            break;
          case Swap:
            this.modifier = new SwapModifier(configs,settings);
            break;
          default:
            System.out.println("invalid Modification type");
            break;
        }

          HashSet<String> hostnames = new HashSet<String>();

          Configuration genericConfiguration;
          for (Config config: configs){
            hostnames.add(config.getHostname());
          }

        for (String host: hostnames){
          modifier.modify(ModifierSetting, host);
        }
        if (settings.getOutputDirectory()!=null){
            System.out.printf("Generating modified configs in : %s\n", settings.getOutputDirectory());
            this.modifier.generateModifiedConfigs(settings.getOutputDirectory());
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
