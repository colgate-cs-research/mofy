package edu.colgate.cs.mofy;

import edu.colgate.cs.config.Settings;
import edu.colgate.cs.modification.Modification;
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

    private Modification modification;

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
        this.modification = new Modification (this.percentage, this.seed);

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
          default:
            System.out.println("invalid modification type");
            break;
        }

          HashSet<String> hostnames = new HashSet<String>();

          Configuration genericConfiguration;
          for (Config config: configs){
            hostnames.add(config.getHostname());
          }

        for (String host: hostnames){
          modifier.modify(modification, host);
        }
        if (settings.getOutputDirectory()!=null){
            System.out.printf("Generating modified configs in : %s\n", settings.getOutputDirectory());
            this.modifier.generateModifiedConfigs(settings.getOutputDirectory());
        }

        // if (settings.getpermit()){
        //   permitmodifier = new PermitModifier(configs,settings);
        //   deducePermitmodifications();
        //   for (PermitModification mod:permitModifications){
        //       permitmodifier.modify(mod);
        //     }
        //     if (settings.getOutputDirectory()!=null){
        //         System.out.printf("Generating modified configs in : %s\n", settings.getOutputDirectory());
        //         permitmodifier.generateModifiedConfigs(settings.getOutputDirectory());
        //     }
        //   }
        //   if (settings.getsubnet()){
        //     subnetmodifier = new SubnetModifier(configs,settings);
        //     deduceSubnetmodifications();
        //     for (SubnetModification mod:subnetModifications){
        //       subnetmodifier.modify(mod);
        //     }
        //     if (settings.getOutputDirectory()!=null){
        //         System.out.printf("Generating modified configs in : %s\n", settings.getOutputDirectory());
        //         subnetmodifier.generateModifiedConfigs(settings.getOutputDirectory());
        //     }
        //   }
        //   if (settings.getswap()){
        //     swapmodifier = new SwapModifier(configs,settings);
        //     deduceSwapmodications();
        //     for (SwapModification mod:swapModifications){
        //       swapmodifier.modify(mod);
        //     }
        //     if(settings.getOutputDirectory()!=null){
        //       System.out.printf("Generating modified configs in : %s\n", settings.getOutputDirectory());
        //       swapmodifier.generateModifiedConfigs(settings.getOutputDirectory());
        //     }
        //   }
        //   if (settings.getIp()){
        //     ipmodifier = new IpModifier(configs,settings);
        //     deduceIpmodications();
        //     for (IpModification mod:ipModifications){
        //       ipmodifier.modify(mod);
        //     }
        //     if(settings.getOutputDirectory()!=null){
        //       System.out.printf("Generating modified configs in : %s\n", settings.getOutputDirectory());
        //       ipmodifier.generateModifiedConfigs(settings.getOutputDirectory());
        //     }
        //   }
        // }

    /*
     * Playing around with Lambdas. TODO: Replace this with simple method.
     */
    // interface CreatePermitModification{
    //     void addPermitmod(String host);
    // }
    // interface CreateSubnetModification{
    //     void addSubnetmod(String host);
    // }
    // interface CreateSwapModification{
    //     void addSwapmod(String host);
    // }
    // interface CreateIpModification{
    //     void addIpmod(String host);
    // }
    }



    /*
     * Deduce the set of all possible ACLs that may be
     * added to the network configuration files.
     */


    // private void deducePermitmodifications(){
    //   Set<Prefix> prefixes = new TreeSet<>();
    //   HashSet<String> hostnames = new HashSet<String>();
    //
    //   Configuration genericConfiguration;
    //   for (Config config: configs){
    //     hostnames.add(config.getHostname());
    //   }
    //   permitModifications = new TreeList<>();
    //   CreatePermitModification createPermitmods = (h) -> {
    //       permitModifications.add(new PermitModification(h, percentage, seed));
    //   };
    //   for (String host: hostnames){
    //       createPermitmods.addPermitmod(host);
    //     }
    //   }
    //
    // private void deduceSubnetmodifications(){
    //   Set<Prefix> prefixes = new TreeSet<>();
    //   HashSet<String> hostnames = new HashSet<String>();
    //
    //   Configuration genericConfiguration;
    //   for (Config config: configs){
    //     hostnames.add(config.getHostname());
    //   }
    //   subnetModifications = new TreeList<>();
    //   CreateSubnetModification createSubnetmods = (h) -> {
    //       subnetModifications.add(new SubnetModification(h, percentage, seed));
    //   };
    //   for (String host: hostnames){
    //       createSubnetmods.addSubnetmod(host);
    //     }
    //   }
    //
    // private void deduceSwapmodications(){
    //   Set<Prefix> prefixes = new TreeSet<>();
    //   HashSet<String> hostnames = new HashSet<String>();
    //
    //   Configuration genericConfiguration;
    //   for (Config config: configs){
    //     hostnames.add(config.getHostname());
    //   }
    //   swapModifications = new TreeList<>();
    //   CreateSwapModification createSwapmods = (h) -> {
    //       swapModifications.add(new SwapModification(h, percentage, seed));
    //   };
    //   for (String host: hostnames){
    //       createSwapmods.addSwapmod(host);
    //     }
    //   }
    //
    // private void deduceIpmodications(){
    //   Set<Prefix> prefixes = new TreeSet<>();
    //   HashSet<String> hostnames = new HashSet<String>();
    //
    //   Configuration genericConfiguration;
    //   for (Config config: configs){
    //     hostnames.add(config.getHostname());
    //   }
    //   ipModifications = new TreeList<>();
    //   CreateIpModification createIpmods = (h) -> {
    //       ipModifications.add(new IpModification(h, percentage, seed));
    //   };
    //   for (String host: hostnames){
    //       createIpmods.addIpmod(host);
    //   }
    // }
    /*
     * List all config files(ending with .cfg) in a directory.
     * @param dirPath Path to directory
     * @return list of config files
     */
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
