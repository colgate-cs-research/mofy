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
    private File outputDir;

    private PrintStream output;

    private TokenStreamRewriter rewriter;

    private ACLModification aclModification;

    private static int MOD_ID = 1;

    public ACLModifier(List<Config> configs, File outputDir){
        hostToConfigMap = new HashMap<>();
        for (Config config: configs){
            hostToConfigMap.put(config.getHostname()
                    ,config);
        }
        this.outputDir = outputDir;
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
        System.out.println(config.getTokens().size());
        CommonTokenStream commonTokenStream = new CommonTokenStream(tokenSource);
        commonTokenStream.fill();
        rewriter = new TokenStreamRewriter(commonTokenStream);
        try {
            File hostDir = new File (outputDir, config.getHostname());
            if (!hostDir.exists()) hostDir.mkdir();
            output = new PrintStream(new File(outputDir,
                    String.format("%s/%s_%d.cfg",
                            config.getHostname(),
                            config.getHostname(),
                            MOD_ID)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this,config.getParseTree());
        output.println("end");
        output.close();
        MOD_ID++;
        System.out.println(rewriter.getText());


    }

    @Override
    public void enterS_hostname(S_hostnameContext ctx) {
        super.enterS_hostname(ctx);
        rewriter.insertAfter(ctx.getStop(), String.format("!\n%s\n", aclModification.getACLEntry()));
    }

    @Override
    public void enterCisco_configuration(Cisco_configurationContext ctx) {
        super.enterCisco_configuration(ctx);
        //Need to create ACL stanza as a child .
        //Idea  :  Use TokenStreamRewriter
        System.out.println(ctx.getStart() + " " + ctx.getStop());
        rewriter.insertAfter(ctx.getStart(), "<Insertion>");
    }
}
