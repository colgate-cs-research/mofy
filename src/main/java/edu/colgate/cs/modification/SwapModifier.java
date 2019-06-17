package edu.colgate.cs.modification;

import edu.colgate.cs.config.Settings;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.representation.cisco.NetworkObjectGroupAddressSpecifier;
import static org.batfish.datamodel.ConfigurationFormat.ARISTA;
import static org.batfish.datamodel.ConfigurationFormat.ARUBAOS;
import static org.batfish.datamodel.ConfigurationFormat.CISCO_ASA;
import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.batfish.datamodel.ConfigurationFormat.CISCO_NX;
import org.batfish.representation.cisco.WildcardAddressSpecifier;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.cisco.CiscoCombinedParser;

import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.grammar.cisco.CiscoParserBaseListener;
import org.batfish.grammar.cisco.CiscoParser.*;
import org.batfish.representation.cisco.AccessListAddressSpecifier;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import edu.colgate.cs.config.Settings;
import java.io.File;
import java.io.IOException;

public class SwapModifier{

    private TokenStreamRewriter rewriter;

    /** Current ACL ModifierSetting to be applied */
    private int percent;
    private long seed;
    private Random generator;
    private String hostname;
    private static ArrayList<Standard_access_list_tailContext> all_list;
    private static ArrayList<Extended_access_list_tailContext> all_list1;
    private static int count_standard;
    private static int count_extended;


    /**
     * Creates an ACLModifier for a set of configuration.
     * @param configs List of Configs for the network to be modified.
     */
    public SwapModifier(List<Config> configs, Settings setting){
      generator = new Random(setting.getSeed());
      this.percent = setting.getPercent();
      this.seed = setting.getSeed();
      this.generator = new Random(this.seed);
    }

    /**
     * Add ACL (specified by param) into chosen config.
     * @param ModifierSetting ModifierSetting needs to be made.
     */
    public Config modify(Config config){
        this.all_list = new ArrayList<Standard_access_list_tailContext>();
        this.all_list1 = new ArrayList<Extended_access_list_tailContext>();
        this.count_standard = 0;
        this.count_extended = 0;
        this.hostname = config.getHostname();
        ListTokenSource tokenSource = new ListTokenSource(config.getTokens());
        CommonTokenStream commonTokenStream = new CommonTokenStream(tokenSource);
        commonTokenStream.fill();
        rewriter = new TokenStreamRewriter(commonTokenStream);
        SwapWalkListener listener = new SwapWalkListener(rewriter);
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(listener,config.getParseTree());
        changelist();
        ChangeWalkerListener listener1 = new ChangeWalkerListener(generator,percent,rewriter);
        walker.walk(listener1,config.getParseTree());
        Config newconfig = new Config(rewriter.getText(),config.getHostname());
        return newconfig;
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

    private void changelist(){
      HashSet check = new HashSet<Integer>();
      for (int i = 0; i < all_list.size()-1; i++){
        for (int j = i+1; j < all_list.size(); j++){
          if (overlap(all_list.get(i),all_list.get(j))&&(!check.contains(all_list.get(i).ala.getStart().getLine()))&&(!check.contains(all_list.get(j).ala.getStart().getLine()))){
            Double num = generator.nextDouble()*100;
            if (num>(100- percent)){
              Standard_access_list_tailContext temp = all_list.get(i);
              System.out.println("swap change(Standard) at configuration "+hostname+" line: "+all_list.get(i).ala.getStart().getLine());
              check.add(all_list.get(i).ala.getStart().getLine());
              System.out.println("swap change(Standard) at configuration "+hostname+" line: "+all_list.get(j).ala.getStart().getLine());
              check.add(all_list.get(i).ala.getStart().getLine());
              all_list.set(i, all_list.get(j));
              all_list.set(j, temp);
              break;
            }
          }
      }
    }
      check.clear();
      for (int i = 0; i < all_list1.size()-1; i++){
        for (int j = i+1; j < all_list1.size(); j++){
          if (overlap(all_list1.get(i),all_list1.get(j))&&(!check.contains(all_list1.get(i).ala.getStart().getLine()))&&(!check.contains(all_list1.get(j).ala.getStart().getLine()))){
            Double num = generator.nextDouble()*100;
            if (num>(100-percent)){
              Extended_access_list_tailContext temp = all_list1.get(i);
              System.out.println("swap change(Extended) at configuration "+hostname+" line: "+all_list1.get(i).ala.getStart().getLine());
              check.add(all_list1.get(i).ala.getStart().getLine());
              System.out.println("swap change(Extended) at configuration "+hostname+" line: "+all_list1.get(j).ala.getStart().getLine());
              check.add(all_list1.get(i).ala.getStart().getLine());
              all_list1.set(i, all_list1.get(j));
              all_list1.set(j, temp);
              break;
            }
          }
      }
    }
  }
    private boolean overlap(Access_list_ip_rangeContext ipr1, Access_list_ip_rangeContext ipr2){
      int subnet_1 = 0;
      int subnet_2 = 0;
      int common = 0;
      if (ipr1.prefix != null && ipr2.prefix !=null){
      Prefix Prefix1 = Prefix.parse(ipr1.prefix.getText());
      Prefix Prefix2 = Prefix.parse(ipr2.prefix.getText());
      subnet_1 = Prefix1.getPrefixLength();
      subnet_2 = Prefix2.getPrefixLength();
      if (subnet_1>subnet_2){
        common = subnet_2;
      }
      else{
        common = subnet_1;
      }
      for (int i =0; i < common; i++){
        if (Ip.getBitAtPosition(Prefix1.getStartIp(),i) != Ip.getBitAtPosition(Prefix2.getStartIp(),i)){
          return false;
        }
      }
      return true;
    }
    if (ipr1.wildcard != null && ipr2.wildcard !=null){
    IpWildcard card_1 = IpWildcard.ipWithWildcardMask(Ip.parse(ipr1.ip.getText()), Ip.parse(ipr1.wildcard.getText()));
    IpWildcard card_2 = IpWildcard.ipWithWildcardMask(Ip.parse(ipr2.ip.getText()), Ip.parse(ipr2.wildcard.getText()));
    try{
      subnet_1 = card_1.toPrefix().getPrefixLength();
      subnet_2 = card_2.toPrefix().getPrefixLength();}
    catch(IllegalStateException e){
      System.out.println("Illegal wildcard");
      return false;
    }
    common = 0;
    if (subnet_1>subnet_2){
      common = subnet_2;
    }
    else{
      common = subnet_1;
    }
    for (int i =0; i < common; i++){
      if (Ip.getBitAtPosition(Ip.parse(ipr1.ip.getText()), i) != Ip.getBitAtPosition(Ip.parse(ipr2.ip.getText()), i)){
        return false;
      }
    }
    return true;
    }
    return false;
    }

    private boolean overlap(Standard_access_list_tailContext ctx1, Standard_access_list_tailContext ctx2){
      return overlap(ctx1.ipr,ctx2.ipr);

    }

    private boolean overlap(Extended_access_list_tailContext ctx1, Extended_access_list_tailContext ctx2){
      return (overlap(ctx1.srcipr,ctx2.srcipr)||overlap(ctx1.dstipr,ctx2.dstipr));
    }

    static class SwapWalkListener extends  CiscoParserBaseListener{

        TokenStreamRewriter rewriter;

        SwapWalkListener(TokenStreamRewriter rewriter) {
            this.rewriter = rewriter;
        }

        @Override
        public void exitStandard_access_list_tail(Standard_access_list_tailContext ctx) {
          all_list.add(ctx);
          }

        @Override
        public void exitExtended_access_list_tail(Extended_access_list_tailContext ctx) {
          all_list1.add(ctx);
        }
      }
    static class ChangeWalkerListener extends CiscoParserBaseListener{
      TokenStreamRewriter rewriter;
      ChangeWalkerListener(Random generator, int percent,
                              TokenStreamRewriter rewriter){
            this.rewriter = rewriter;
          }
        @Override
        public void exitStandard_access_list_tail(Standard_access_list_tailContext ctx){
          if (ctx.ipr.prefix !=null ){
          rewriter.replace(ctx.ipr.prefix,all_list.get(count_standard).ipr.prefix.getText());}
          else if (ctx.ipr.ip != null && ctx.ipr.wildcard!= null){
          rewriter.replace(ctx.ipr.ip,all_list.get(count_standard).ipr.ip.getText());
          rewriter.replace(ctx.ipr.wildcard, all_list.get(count_standard).ipr.wildcard.getText());}
          rewriter.replace(ctx.ala.getStart(), all_list.get(count_standard).ala.getStart().getText());
          count_standard++;
          }
        @Override
        public void exitExtended_access_list_tail(Extended_access_list_tailContext ctx){
          if (ctx.srcipr.ip != null ){
            if (all_list1.get(count_extended).srcipr.ip!= null){
                rewriter.replace(ctx.srcipr.ip,all_list1.get(count_extended).srcipr.ip.getText());}
            else {rewriter.replace(ctx.srcipr.ip, null);}
          }
          if (ctx.srcipr.prefix!= null ){
            if (all_list1.get(count_extended).srcipr.prefix!= null){
                rewriter.replace(ctx.srcipr.prefix,all_list1.get(count_extended).srcipr.prefix.getText());}
            else {rewriter.replace(ctx.srcipr.prefix, null);}
          }
          if (ctx.dstipr.ip != null ){
            if (all_list1.get(count_extended).dstipr.ip!= null){
                rewriter.replace(ctx.dstipr.ip,all_list1.get(count_extended).dstipr.ip.getText());}
            else {rewriter.replace(ctx.dstipr.ip, null);}
          }
          if (ctx.dstipr.prefix!= null ){
            if (all_list1.get(count_extended).dstipr.prefix!= null){
                rewriter.replace(ctx.dstipr.prefix,all_list1.get(count_extended).dstipr.prefix.getText());}
            else {rewriter.replace(ctx.dstipr.prefix, null);}
          }
          rewriter.replace(ctx.ala.getStart(), all_list1.get(count_extended).ala.getStart().getText());
          count_extended++;
          }
        }
      }
