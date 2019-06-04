package edu.colgate.cs.modification;

import edu.colgate.cs.config.Settings;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.cisco.CiscoParserBaseListener;
import org.batfish.grammar.cisco.CiscoParser.*;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class IpModifier extends Modifier<IpModification>{

    private TokenStreamRewriter rewriter;

    /** Current ACL Modification to be applied */
    private IpModification IpModification;

    private int percentage;
    private long seed;
    private static Random generator;

    /**
     * Creates an ACLModifier for a set of configuration.
     * @param configs List of Configs for the network to be modified.
     */
    public IpModifier(List<Config> configs, Settings setting){
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
    public void modify(IpModification Modification){
        this.IpModification = Modification;
        String hostname = IpModification.getHost();
        if (!hostToConfigMap.containsKey(hostname)){
            System.out.printf("Host %s : NOT FOUND!", hostname);
            return;
        }
        this.percentage = IpModification.getPercent();
        this.seed =  IpModification.getSeed();
        Config config = hostToConfigMap.get(hostname);

        ListTokenSource tokenSource = new ListTokenSource(config.getTokens());
        CommonTokenStream commonTokenStream = new CommonTokenStream(tokenSource);
        commonTokenStream.fill();
        rewriter = new TokenStreamRewriter(commonTokenStream);
        IpWalkListener listener = new IpWalkListener(IpModification, rewriter);
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
        modificationHistoryMap.put(hostname,
                modificationHistoryMap.get(hostname)+1);
    }

    static class IpWalkListener extends  CiscoParserBaseListener{

        IpModification IpModification;
        TokenStreamRewriter rewriter;

        IpWalkListener(IpModification IpModification,
                               TokenStreamRewriter rewriter) {
            this.IpModification = IpModification;
            this.rewriter = rewriter;
        }


        private void mutateIp(Token ipToken){
          String ip = ipToken.getText();
          String replacement = mutate(ip);
          if (replacement != null){
            rewriter.replace(ipToken.getTokenIndex(),
                replacement);
            System.out.println("Ip change (ip) at configuration "+IpModification.getHost()+" line: "+ipToken.getLine());
          }
        }

        private void mutatePrefix(Token prefixToken){
          InterfaceAddress prefix = new InterfaceAddress(prefixToken.getText());
          String ip = prefix.getIp().toString();
          String replacement = mutate(ip);
          if (replacement != null){
            rewriter.replace(prefixToken.getTokenIndex(),
                Prefix.create(Ip.parse(replacement), prefix.getNetworkBits()).toString());
            System.out.println("Ip change (prefix) at configuration "+IpModification.getHost()+" line: "+prefixToken.getLine());
          }
        }

        private String mutate(String IpString){
          System.out.println(IpModification.getPercent());
          String[] addrArray = IpString.split("\\.");
          Double num = generator.nextDouble()*100;
          int check = generator.nextInt(4);
          if (num < IpModification.getPercent()){
            if (addrArray[check].length()>1){
              addrArray[check] = addrArray[check].substring(0,addrArray[check].length()-1);
            }
            else{
              addrArray[check] = "0";
            }
            String result = addrArray[0]+"."+addrArray[1]+"."+addrArray[2]+"."+addrArray[3];
            return result;
          }
          return null;
        }

        @Override
        public void exitAccess_list_ip_range(Access_list_ip_rangeContext ctx) {
          if (ctx.ip != null) {
              mutateIp(ctx.ip);
          } else if (ctx.prefix != null) {
              mutatePrefix(ctx.prefix);
          }
        }

        @Override
        public void exitIf_ip_address(If_ip_addressContext ctx) {
          if (ctx.ip != null) {
            mutateIp(ctx.ip);
          } else if (ctx.prefix != null) {
            mutatePrefix(ctx.prefix);
          }
        }

        @Override
        public void exitRo_network(Ro_networkContext ctx) {
          if (ctx.ip != null) {
            mutateIp(ctx.ip);
          } else if (ctx.prefix != null) {
            mutatePrefix(ctx.prefix);
          }
        }

        @Override
        public void exitNetwork_bgp_tail(Network_bgp_tailContext ctx) {
          if (ctx.ip!= null ){
            mutateIp(ctx.ip);
          }
          else if (ctx.prefix != null) {
            mutatePrefix(ctx.prefix);
          }
        }

      }
    }
