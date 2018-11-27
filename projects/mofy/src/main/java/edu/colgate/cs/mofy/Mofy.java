package edu.colgate.cs.mofy;

import edu.colgate.cs.config.Settings;
import edu.colgate.cs.modification.ACLModification;
import edu.colgate.cs.modification.ACLModifier;
import edu.colgate.cs.modification.PermitModifier;
import edu.colgate.cs.modification.PermitModification;
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

    private ACLModifier aclmodifier;

    private PermitModifier permitmodifier;

    private List<ACLModification> aclModifications;

    private List<PermitModification> permitModifications;


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
            if (!cfgFilePath.toString().endsWith("cfg"))
                continue;
            File file = new File(cfgFilePath.toString());

            try {
                configs.add(new Config(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.percentage = settings.getPercent();
        this.seed = settings.getSeed();
        if (settings.getacl()){
          aclmodifier = new ACLModifier(configs);
          deduceACLModifications();

          for (ACLModification mod:aclModifications) {
              aclmodifier.modify(mod);
              break;
          }
          if (settings.getOutputDirectory()!=null){
              System.out.printf("Generating modified configs in : %s\n", settings.getOutputDirectory());
              aclmodifier.generateModifiedConfigs(settings.getOutputDirectory());
          }
        }

        if (settings.getpermit()){
          permitmodifier = new PermitModifier(configs,settings);
          deducePermitmodifications();
          for (PermitModification mod:permitModifications){
              permitmodifier.modify(mod);
              break;
            }
            if (settings.getOutputDirectory()!=null){
                System.out.printf("Generating modified configs in : %s\n", settings.getOutputDirectory());
                permitmodifier.generateModifiedConfigs(settings.getOutputDirectory());
            }
          }
        }

    /*
     * Playing around with Lambdas. TODO: Replace this with simple method.
     */
    interface CreateModification{
        void addACLmod(String host, List<Interface> ifaces, Prefix network);
    }
    interface CreatePermitModification{
        void addPermitmod(String host);
    }

    /*
     * Deduce the set of all possible ACLs that may be
     * added to the network configuration files.
     */
    private void deduceACLModifications(){

        Set<Prefix> prefixes = new TreeSet<>();
        Map<String,List<Interface>> hostToIfaces = new TreeMap<>();

        Configuration genericConfiguration;
        for (Config config: configs){
            genericConfiguration = config.getGenericConfiguration();
            Map<String, Interface> interfaceMap = genericConfiguration.getInterfaces();
            hostToIfaces.put(config.getHostname(), new TreeList<>());

            for (String interfaceName : interfaceMap.keySet()){
                Interface iface = interfaceMap.get(interfaceName);
                hostToIfaces.get(config.getHostname()).add(iface);
                prefixes.add(iface.getAddress().getPrefix());
            }
        }

        aclModifications = new TreeList<>();

        CreateModification createACLmods = (h, ifaces, network) -> {
            for(Interface i : ifaces){
                if (!network.contains(i.getAddress().getIp())) {
                    aclModifications.add(new ACLModification(h, i, network, true, true, percentage, seed));
                }
            }
        };

        for (Prefix network: prefixes){
            for (String host: hostToIfaces.keySet()){
                createACLmods.addACLmod(host, hostToIfaces.get(host), network);
            }
        }

    }


    private void deducePermitmodifications(){
      Set<Prefix> prefixes = new TreeSet<>();
      Map<String,List<Interface>> hostToIfaces = new TreeMap<>();

      Configuration genericConfiguration;
      for (Config config: configs){
          genericConfiguration = config.getGenericConfiguration();
          Map<String, Interface> interfaceMap = genericConfiguration.getInterfaces();
          hostToIfaces.put(config.getHostname(), new TreeList<>());

          for (String interfaceName : interfaceMap.keySet()){
              Interface iface = interfaceMap.get(interfaceName);
              hostToIfaces.get(config.getHostname()).add(iface);
              prefixes.add(iface.getAddress().getPrefix());
          }
      }
      permitModifications = new TreeList<>();
      CreatePermitModification createPermitmods = (h) -> {
        for (String host : hostToIfaces.keySet()){
          permitModifications.add(new PermitModification(host, percentage, seed));
        }
      };
      for (String host: hostToIfaces.keySet()){
          createPermitmods.addPermitmod(host);
      }
    }

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
                return name.endsWith(".cfg");
            }
        });
        List<Path> cfgPaths = new ArrayList<>();
        for (File cfgFile: cfgFiles){
            cfgPaths.add(cfgFile.toPath());
        }
        return cfgPaths;
    }
}
