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

public class SwapModifier extends Modifier<SwapModification>{

    private TokenStreamRewriter rewriter;

    /** Current ACL Modification to be applied */
    private SwapModification SwapModification;

    private int percentage;
    private long seed;
    private static Random generator;
    private static ArrayList<Standard_access_list_tailContext> all_list;
    private static int count;

    /**
     * Creates an ACLModifier for a set of configuration.
     * @param configs List of Configs for the network to be modified.
     */
    public SwapModifier(List<Config> configs, Settings setting){
        super(configs);
        hostToConfigMap = new HashMap<>();
        for (Config config: configs){
            hostToConfigMap.put(config.getHostname(),config);
        }
        modificationHistoryMap = new HashMap<>();
        for (Config config: configs){
            modificationHistoryMap.put(config.getHostname(), 0);
        }
        generator = new Random(setting.getSeed());
    }

    /**
     * Add ACL (specified by param) into chosen config.
     * @param modification Modification needs to be made.
     */
    public void modify(SwapModification Modification){
        this.all_list = new ArrayList<Standard_access_list_tailContext>();
        this.SwapModification = Modification;
        String hostname = SwapModification.getHost();
        if (!hostToConfigMap.containsKey(hostname)){
            System.out.printf("Host %s : NOT FOUND!", hostname);
            return;
        }
        this.percentage = SwapModification.getPercent();
        this.seed =  SwapModification.getSeed();
        this.count = 0;
        Config config = hostToConfigMap.get(hostname);

        ListTokenSource tokenSource = new ListTokenSource(config.getTokens());
        CommonTokenStream commonTokenStream = new CommonTokenStream(tokenSource);
        commonTokenStream.fill();
        rewriter = new TokenStreamRewriter(commonTokenStream);
        SwapWalkListener listener = new SwapWalkListener(SwapModification, rewriter);
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(listener,config.getParseTree());
        changelist();
        ChangeWalkerListener listener1 = new ChangeWalkerListener(SwapModification, rewriter);
        walker.walk(listener1,config.getParseTree());
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

    private void changelist(){
      HashSet check = new HashSet<Integer>();
      for (int i = 0; i < all_list.size()-1; i++){
        for (int j = i+1; j < all_list.size(); j++){
          if (overlap(all_list.get(i),all_list.get(j))&&(!check.contains(all_list.get(i).ala.getStart().getLine()))&&(!check.contains(all_list.get(j).ala.getStart().getLine()))){
            Double num = generator.nextDouble()*100;
            if (num>(100-SwapModification.getPercent())){
              Standard_access_list_tailContext temp = all_list.get(i);
              System.out.println("swap change at configuration "+SwapModification.getHost()+" line: "+all_list.get(i).ala.getStart().getLine());
              check.add(all_list.get(i).ala.getStart().getLine());
              System.out.println("swap change at configuration "+SwapModification.getHost()+" line: "+all_list.get(j).ala.getStart().getLine());
              check.add(all_list.get(i).ala.getStart().getLine());
              all_list.set(i, all_list.get(j));
              all_list.set(j, temp);
              break;
            }
          }
      }
    }
  }

    private boolean overlap(Standard_access_list_tailContext ctx1, Standard_access_list_tailContext ctx2){
      if (ctx1.ipr.prefix != null && ctx2.ipr.prefix !=null){
      int subnet_1 = Integer.parseInt(ctx1.ipr.prefix.getText());
      int subnet_2 = Integer.parseInt(ctx2.ipr.prefix.getText());
      int common;
      if (subnet_1>subnet_2){
        common = subnet_2;
      }
      else{
        common = subnet_1;
      }
      for (int i =0; i < common; i++){
        if (Ip.getBitAtPosition(new Ip(ctx1.ipr.ip.getText()), i) != Ip.getBitAtPosition(new Ip(ctx2.ipr.ip.getText()), i)){
          return false;
        }
      }
      return true;
    }
    if (ctx1.ipr.wildcard != null && ctx2.ipr.wildcard !=null){
    IpWildcard card_1 = new IpWildcard(new Ip(ctx1.ipr.ip.getText()), new Ip(ctx1.ipr.wildcard.getText()));
    IpWildcard card_2 = new IpWildcard(new Ip(ctx2.ipr.ip.getText()), new Ip(ctx2.ipr.wildcard.getText()));
    int subnet_1 = card_1.toPrefix().getPrefixLength();
    int subnet_2 = card_2.toPrefix().getPrefixLength();
    int common;
    if (subnet_1>subnet_2){
      common = subnet_2;
    }
    else{
      common = subnet_1;
    }
    for (int i =0; i < common; i++){
      if (Ip.getBitAtPosition(new Ip(ctx1.ipr.ip.getText()), i) != Ip.getBitAtPosition(new Ip(ctx2.ipr.ip.getText()), i)){
        return false;
      }
    }
    return true;
    }
    return false;
    }


    static class SwapWalkListener extends  CiscoParserBaseListener{

        SwapModification SwapModification;
        TokenStreamRewriter rewriter;

        SwapWalkListener(SwapModification SwapModification,
                               TokenStreamRewriter rewriter) {
            this.SwapModification = SwapModification;
            this.rewriter = rewriter;
        }

        @Override
        public void exitStandard_access_list_tail(Standard_access_list_tailContext ctx) {
          all_list.add(ctx);
          }
        }
    static class ChangeWalkerListener extends CiscoParserBaseListener{
      SwapModification SwapModification;
      TokenStreamRewriter rewriter;
      ChangeWalkerListener(SwapModification SwapModification,
                              TokenStreamRewriter rewriter){
            this.SwapModification = SwapModification;
            this.rewriter = rewriter;
          }
        @Override
        public void exitStandard_access_list_tail(Standard_access_list_tailContext ctx){
          if (ctx.ipr.ip != null){
          rewriter.replace(ctx.ipr.ip,all_list.get(count).ipr.ip.getText());}
          if (ctx.ipr.prefix !=null){
          rewriter.replace(ctx.ipr.prefix,all_list.get(count).ipr.prefix.getText());}
          rewriter.replace(ctx.ala.getStart(), all_list.get(count).ala.getStart().getText());
          count++;
          }
        }
      }
