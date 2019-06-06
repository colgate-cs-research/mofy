package edu.colgate.cs.modification;

import org.apache.commons.io.FileUtils;
import edu.colgate.cs.config.Settings;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A network Configuration modifier.
 */
public abstract class Modifier<E extends ModifierSetting> {

    protected Map<String, Config> hostToConfigMap;

    /** How many times has a config been modified with ACLModifier? */
    protected Map<String, Integer> ModifierSettingHistoryMap;

    protected static Random generator;

    protected Modifier(List<Config> configs, Settings setting){
        hostToConfigMap = new HashMap<>();
        ModifierSettingHistoryMap = new HashMap<>();
        for (Config config: configs){
            hostToConfigMap.put(config.getHostname(), config);
            ModifierSettingHistoryMap.put(config.getHostname(), 0);
        }
        generator = new Random(setting.getSeed());
    }

    /**
     * Generate .cfg files for each host in the network,
     * with any ModifierSettings applied.
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
     * Construct a config object containing ACL ModifierSettings made.
     * @return Modified Config Object
     */
    public Config getModifiedConfig(String hostname){
        return hostToConfigMap.get(hostname);
    }

    /**
     * Perform given ModifierSetting.
     */
    public abstract void modify(ModifierSetting ModifierSetting, String hostname);

}
