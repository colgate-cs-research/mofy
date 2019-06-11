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
public class IpWalkListener extends  CiscoParserBaseListener{

    TokenStreamRewriter rewriter;
    int percent;
    Random generator;
    Config config;
    boolean ifchange;

    public IpWalkListener(Boolean ifchange, Random generator, int percent, Config config,
                           TokenStreamRewriter rewriter) {
        this.rewriter = rewriter;
        this.percent = percent;
        this.config = config;
        this.generator = generator;
        this.ifchange = ifchange;
    }


    private void mutateIp(Token ipToken){
      String ip = ipToken.getText();
      String replacement = mutate(ip);
      if (replacement != null){
        rewriter.replace(ipToken.getTokenIndex(),
            replacement);
        System.out.println("Ip change (ip) at configuration "+config.getHostname()+" line: "+ipToken.getLine());
      }
    }

    private void mutatePrefix(Token prefixToken){
      InterfaceAddress prefix = new InterfaceAddress(prefixToken.getText());
      String ip = prefix.getIp().toString();
      String replacement = mutate(ip);
      if (replacement != null){
        rewriter.replace(prefixToken.getTokenIndex(),
            Prefix.create(Ip.parse(replacement), prefix.getNetworkBits()).toString());
        System.out.println("Ip change (prefix) at configuration "+config.getHostname()+" line: "+prefixToken.getLine());
      }
    }

    private String mutate(String IpString){
      String[] addrArray = IpString.split("\\.");
      Double num = generator.nextDouble()*100;
      int check = generator.nextInt(4);
      if (num < this.percent){
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
      if (this.ifchange){
        if (ctx.ip != null) {
          mutateIp(ctx.ip);
        } else if (ctx.prefix != null) {
          mutatePrefix(ctx.prefix);
        }
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

    @Override
    public void exitRs_route(Rs_routeContext ctx) {
      if (ctx.prefix !=null ){
        mutatePrefix(ctx.prefix);
      }
      if (ctx.nhip != null){
        mutateIp(ctx.nhip);
      }
    }

  }
