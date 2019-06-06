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
import java.util.Random;

public class PermitModifier extends Modifier<ModifierSetting>{

    private TokenStreamRewriter rewriter;

    /** Current ACL ModifierSetting to be applied */
    private ModifierSetting PermitModifierSetting;
    private static String hostname;

    /**
     * Creates an ACLModifier for a set of configuration.
     * @param configs List of Configs for the network to be modified.
     */
    public PermitModifier(List<Config> configs, Settings setting){
        super(configs, setting);
    }

    /**
     * Add ACL (specified by param) into chosen config.
     * @param ModifierSetting ModifierSetting needs to be made.
     */
    public void modify(ModifierSetting ModifierSetting, String host){
        this.PermitModifierSetting = ModifierSetting;
        this.hostname = host;
        if (!hostToConfigMap.containsKey(hostname)){
            System.out.printf("Host %s : NOT FOUND!", hostname);
            return;
        }
        Config config = hostToConfigMap.get(hostname);

        ListTokenSource tokenSource = new ListTokenSource(config.getTokens());
        CommonTokenStream commonTokenStream = new CommonTokenStream(tokenSource);
        commonTokenStream.fill();
        rewriter = new TokenStreamRewriter(commonTokenStream);
        PermitWalkListener listener = new PermitWalkListener(PermitModifierSetting, rewriter);
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(listener,config.getParseTree());

        updateConfigMap(hostname);
        //Test Printout
        //System.out.println(rewriter.getText());
    }

    /**
     * Updates the hostnameToConfigMap with modified config.
     * @param hostname Hostname for config to be updated in the map.
     */
    private void updateConfigMap(String hostname){
        Config config = new Config(rewriter.getText(),
                hostname);
        hostToConfigMap.put(hostname, config);
        ModifierSettingHistoryMap.put(hostname,
                ModifierSettingHistoryMap.get(hostname)+1);
    }

    static class PermitWalkListener extends  CiscoParserBaseListener{

        ModifierSetting PermitModifierSetting;
        TokenStreamRewriter rewriter;

        PermitWalkListener(ModifierSetting PermitModifierSetting,
                               TokenStreamRewriter rewriter) {
            this.PermitModifierSetting = PermitModifierSetting;
            this.rewriter = rewriter;
        }

        @Override
        public void exitStandard_access_list_tail(Standard_access_list_tailContext ctx) {
          Double num = generator.nextDouble()*100;
          if (num>(100-PermitModifierSetting.getPercent())){
            System.out.println("permit change at configuration "+hostname+" line: "+ctx.ala.getStart().getLine());
            if (ctx.ala.getStart().toString().contains("permit")){
              rewriter.replace(ctx.ala.getStart(),"deny");}
            else{
              rewriter.replace(ctx.ala.getStart(),"permit");}
          }
        }
        @Override
        public void exitExtended_access_list_tail(Extended_access_list_tailContext ctx) {
          Double num = generator.nextDouble()*100;
          if (num>(100-PermitModifierSetting.getPercent())){
            System.out.println("permit change at configuration "+hostname+" line: "+ctx.ala.getStart().getLine());
            if (ctx.ala.getStart().toString().contains("permit")){
              rewriter.replace(ctx.ala.getStart(),"deny");}
            else{
              rewriter.replace(ctx.ala.getStart(),"permit");}
          }
        }
      }
    }
