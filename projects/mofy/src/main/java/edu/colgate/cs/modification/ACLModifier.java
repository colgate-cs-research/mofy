package edu.colgate.cs.modification;

import edu.wisc.cs.arc.Logger;
import edu.wisc.cs.arc.configs.Config;

import org.antlr.v4.runtime.*;

import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.io.FileUtils;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.grammar.cisco.CiscoParserBaseListener;
import org.batfish.grammar.cisco.CiscoParser.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ACLModifier extends CiscoParserBaseListener{

    private Map<String, Config> hostToConfigMap;

    private TokenStreamRewriter rewriter;

    /** Current ACL Modification to be applied */
    private ACLModification aclModification;

    /** How many times has a config been modified with ACLModifier? */
    private Map<String, Integer> modificationHistoryMap;

    /**
     * Creates an ACLModifier for a set of configuration.
     * @param configs List of Configs for the network to be modified.
     */
    public ACLModifier(List<Config> configs){
        hostToConfigMap = new HashMap<>();
        for (Config config: configs){
            hostToConfigMap.put(config.getHostname(),config);
        }
        modificationHistoryMap = new HashMap<String, Integer>();
        for (Config config: configs){
            modificationHistoryMap.put(config.getHostname(), 0);
        }
    }

    /**
     * Add ACL (specified by param) into chosen config.
     * @param modification Modification needs to be made.
     */
    public void modify(ACLModification modification){
        this.aclModification = modification;
        String hostname = modification.getHost();
        if (!hostToConfigMap.containsKey(hostname)){
            System.out.printf("Host %s : NOT FOUND!", hostname);
            return;
        }

        Config config = hostToConfigMap.get(hostname);

        ListTokenSource tokenSource = new ListTokenSource(config.getTokens());
        CommonTokenStream commonTokenStream = new CommonTokenStream(tokenSource);
        commonTokenStream.fill();
        rewriter = new TokenStreamRewriter(commonTokenStream);
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this,config.getParseTree());

        updateConfigMap(hostname);
        //Test Printout
        System.out.println(rewriter.getText());
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
                FileUtils.writeStringToFile(new File(output, String.format("%s.cfg",host)),
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
     * Checks if Interface Stanza Context object matches the interface
     * where ACL needs to be applied.
     * @param ctx Interface Stanza context to check.
     * @return True if this is the interface to be changed.
     */
    private boolean isMatchingInterface(S_interfaceContext ctx){
        if (ctx.if_ip_address().size()>0){
            String ipAddressStr = ctx.if_ip_address(0).ip.getText();
            String subnetMaskStr= ctx.if_ip_address(0).subnet.getText();
            Ip ipAddress = new Ip(ipAddressStr);
            Ip netMask = new Ip(subnetMaskStr);

            InterfaceAddress interfaceAddress = new InterfaceAddress(ipAddress, netMask);

            if (interfaceAddress.equals(aclModification.getIface().getAddress())){
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the hostnameToConfigMap with modified config.
     * @param hostname Hostname for config to be updated in the map.
     */
    private void updateConfigMap(String hostname){
        Config config = new Config(rewriter.getText(),
                hostname,
                Logger.getInstance(Logger.Level.DEBUG));
        hostToConfigMap.put(hostname, config);
        modificationHistoryMap.put(hostname,
                modificationHistoryMap.get(hostname)+1);
    }

    @Override
    public void enterS_hostname(S_hostnameContext ctx) {
        super.enterS_hostname(ctx);
        rewriter.insertAfter(ctx.getStop(), String.format("!\n%s\n", aclModification.getACLEntry()));
    }

    @Override
    public void enterS_interface(S_interfaceContext ctx) {
        super.enterS_interface(ctx);
        if (isMatchingInterface(ctx)){
            rewriter.insertBefore(ctx.getStop(),String.format("\n%s", aclModification.getACLIfaceLine()));
        }
    }
}
