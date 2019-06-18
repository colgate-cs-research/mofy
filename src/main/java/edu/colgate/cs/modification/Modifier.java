package edu.colgate.cs.modification;

import org.apache.commons.io.FileUtils;
import edu.colgate.cs.config.Settings;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import edu.colgate.cs.modification.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.grammar.cisco.CiscoCombinedParser;

import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.grammar.cisco.CiscoParserBaseListener;
import org.batfish.grammar.cisco.CiscoParser.*;


/**
 * A network Configuration modifier.
 */
public class Modifier {

    /** How many times has a config been modified with ACLModifier? */

    private TokenStreamRewriter rewriter;

    private int percentage;

    private long seed;

    private Settings.modtype mod;

    private static Random generator;

    private boolean ifchange;

    private boolean largescale;

    public Modifier(List<Config> configs, Settings setting){
        generator = new Random(setting.getSeed());
        this.percentage = setting.getPercent();
        this.seed = setting.getSeed();
        this.mod = setting.getmod();
        this.ifchange = setting.getifchange();
        this.largescale = setting.getscale();
        this.generator = new Random(this.seed);
    }

    /**
     * Generate .cfg files for each host in the network,
     * with any ModifierSettings applied.
     * @param outputDir Path to directory where modified configs are to be stored.
     */
    public void generateModifiedConfigs(String outputDir, Config config, boolean _cfg){
        try {
            File output = new File(outputDir);
            if (!output.exists()) {
                output.mkdir();
            }
            if (_cfg){
              FileUtils.writeStringToFile(new File(output, String.format("%s.cfg",config.getHostname())),
                      config.getText());
            }
            else{
              FileUtils.writeStringToFile(new File(output, String.format("%s",config.getHostname())),
                      config.getText());
                  }
            // for (String host: hostToConfigMap.keySet()){
            //     FileUtils.writeStringToFile(new File(output, String.format("%s",host)),
            //             hostToConfigMap.get(host).getText());
            // }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Perform given ModifierSetting.
     */
    public Config modify(Config config){
      ListTokenSource tokenSource = new ListTokenSource(config.getTokens());
      CommonTokenStream commonTokenStream = new CommonTokenStream(tokenSource);
      commonTokenStream.fill();
      rewriter = new TokenStreamRewriter(commonTokenStream);
      ParseTreeWalker walker = new ParseTreeWalker();
      switch(this.mod){
        case Permit:
          PermitWalkListener Permitlistener = new PermitWalkListener(generator,percentage,config,rewriter);
          walker.walk(Permitlistener,config.getParseTree());
          break;
        case Subnet:
          SubnetWalkListener Subnetlistener = new SubnetWalkListener(largescale,ifchange,generator,percentage,config,rewriter);
          walker.walk(Subnetlistener,config.getParseTree());
          break;
        case Ip:
          IpWalkListener Iplistener = new IpWalkListener(ifchange,generator,percentage,config,rewriter);
          walker.walk(Iplistener,config.getParseTree());
          break;
      }
      Config newconfig = new Config(rewriter.getText(),config.getHostname());
      return newconfig;
    }

}
