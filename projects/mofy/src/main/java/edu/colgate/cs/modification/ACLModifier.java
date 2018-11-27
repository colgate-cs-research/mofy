package edu.colgate.cs.modification;

import edu.colgate.cs.config.Settings;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.grammar.cisco.CiscoParserBaseListener;
import org.batfish.grammar.cisco.CiscoParser.*;

import java.util.HashMap;
import java.util.List;

public class ACLModifier extends Modifier<ACLModification>{

    private TokenStreamRewriter rewriter;

    /** Current ACL Modification to be applied */
    private ACLModification aclModification;

    /**
     * Creates an ACLModifier for a set of configuration.
     * @param configs List of Configs for the network to be modified.
     */
    public ACLModifier(List<Config> configs){
        super(configs);
        hostToConfigMap = new HashMap<>();
        for (Config config: configs){
            hostToConfigMap.put(config.getHostname(),config);
        }
        modificationHistoryMap = new HashMap<>();
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
        ACLWalkListener listener = new ACLWalkListener(aclModification, rewriter);
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(listener,config.getParseTree());

        updateConfigMap(hostname);
        //Test Printout
        System.out.println(rewriter.getText());
    }

    /**
     * Updates the hostnameToConfigMap with modified config.
     * @param hostname Hostname for config to be updated in the map.
     */
    private void updateConfigMap(String hostname){
        Config config = new Config(rewriter.getText(),
                hostname);
        hostToConfigMap.put(hostname, config);
        modificationHistoryMap.put(hostname,
                modificationHistoryMap.get(hostname)+1);
    }

    static class ACLWalkListener extends  CiscoParserBaseListener{

        ACLModification aclModification;
        TokenStreamRewriter rewriter;

        ACLWalkListener(ACLModification aclModification,
                               TokenStreamRewriter rewriter) {
            this.aclModification = aclModification;
            this.rewriter = rewriter;
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

        @Override
        public void enterS_hostname(S_hostnameContext ctx) {
            rewriter.insertAfter(ctx.getStop(), String.format("!\n%s\n", aclModification.getACLEntry()));
        }

        @Override
        public void enterS_interface(S_interfaceContext ctx) {
            if (isMatchingInterface(ctx)){
                rewriter.insertBefore(ctx.getStop(),String.format("\n%s", aclModification.getACLIfaceLine()));
            }
        }
        @Override
        public void exitStandard_access_list_tail(Standard_access_list_tailContext ctx) {
          System.out.println(ctx.ala.getStart());
          rewriter.replace(ctx.ala.getStart(),"XXXX");
        }


    }


}
