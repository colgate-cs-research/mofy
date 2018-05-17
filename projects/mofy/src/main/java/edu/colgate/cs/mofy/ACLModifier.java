package edu.colgate.cs.mofy;

import edu.wisc.cs.arc.configs.Config;

import org.antlr.v4.runtime.*;

import org.antlr.v4.runtime.tree.ParseTreeWalker;
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


    public ACLModifier(List<Config> configs){
        hostToConfigMap = new HashMap<>();
        for (Config config: configs){
            hostToConfigMap.put(config.getHostname(),config);
        }
    }

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

        //Test Printout.
        System.out.println(rewriter.getText());
    }

    @Override
    public void enterS_hostname(S_hostnameContext ctx) {
        super.enterS_hostname(ctx);
        rewriter.insertAfter(ctx.getStop(), String.format("!\n%s\n", aclModification.getACLEntry()));
    }

}
