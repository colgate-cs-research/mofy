package edu.colgate.cs.modification;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A network Configuration modifier.
 */
public abstract class Modifier<E extends Modification> {

    protected Map<String, Config> hostToConfigMap;

    /** How many times has a config been modified with ACLModifier? */
    protected Map<String, Integer> modificationHistoryMap;

    protected Modifier(List<Config> configs){
        hostToConfigMap = new HashMap<>();
        modificationHistoryMap = new HashMap<>();
        for (Config config: configs){
            hostToConfigMap.put(config.getHostname(), config);
            modificationHistoryMap.put(config.getHostname(), 0);
        }
    }

    /**
     * Generate .cfg files for each host in the network,
     * with any modifications applied.
     * @param outputDir Path to directory where modified configs are to be stored.
     */
    public void generateModifiedConfigs(String outputDir){
        try {
            File output = new File(outputDir);
            if (!output.exists()) {
                output.mkdir();
            }
            for (String host: hostToConfigMap.keySet()){
                FileUtils.writeStringToFile(new File(output, String.format("%s",host)),
                        hostToConfigMap.get(host).getText());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Construct a config object containing ACL modifications made.
     * @return Modified Config Object
     */
    public Config getModifiedConfig(String hostname){
        return hostToConfigMap.get(hostname);
    }

    /**
     * Perform given modification.
     */
    public abstract void modify(E modification, String hostname);

}
