package edu.colgate.cs.mofy;

import edu.wisc.cs.arc.configs.Config;

import org.antlr.v4.runtime.*;

import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.grammar.cisco.CiscoParserBaseListener;
import org.batfish.grammar.cisco.CiscoParser.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ACLModifier extends CiscoParserBaseListener{

    private Map<String, Config> hostToConfigMap;

    private TokenStreamRewriter rewriter;

    private ACLModification aclModification;

    /**
     * Creates an ACLModifier for a set of configuration.
     * @param configs List of Configs for the network to be modified.
     */
    public ACLModifier(List<Config> configs){
        hostToConfigMap = new HashMap<>();
        for (Config config: configs){
            hostToConfigMap.put(config.getHostname(),config);
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

        //Test Printout
        System.out.println(rewriter.getText());
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
