package edu.colgate.cs.modification;

import edu.colgate.cs.config.Settings;
import java.util.Random;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.batfish.datamodel.*;
import org.batfish.grammar.cisco.CiscoParser.*;

public class IpWalkListener extends MofyBaseListener {

    private boolean ifchange;

    public IpWalkListener(boolean ifchange, Random generator, int percent, 
            Config config, TokenStreamRewriter rewriter) {
        super(generator, percent, config, rewriter);
        this.ifchange = ifchange;
    }

    private void mutateIp(Token ipToken, ParserRuleContext ctx){
      String ip = ipToken.getText();
      String replacement = mutate(ip);
      if (replacement != null){
        rewriter.replace(ipToken.getTokenIndex(),
            replacement);
        logChange(Settings.modtype.Ip, "Address", ctx);
      }
    }

    private void mutatePrefix(Token prefixToken, ParserRuleContext ctx){
      ConcreteInterfaceAddress prefix = ConcreteInterfaceAddress.parse(prefixToken.getText());
      String ip = prefix.getIp().toString();
      String replacement = mutate(ip);
      if (replacement != null){
        rewriter.replace(prefixToken.getTokenIndex(),
            Prefix.create(Ip.parse(replacement), prefix.getNetworkBits()).toString());
        logChange(Settings.modtype.Ip, "Prefix", ctx);
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
          mutateIp(ctx.ip, ctx);
      } else if (ctx.prefix != null) {
          mutatePrefix(ctx.prefix, ctx);
      }
    }

    @Override
    public void exitIf_ip_address(If_ip_addressContext ctx) {
      if (this.ifchange){
        if (ctx.ip != null) {
          mutateIp(ctx.ip, ctx);
        } else if (ctx.prefix != null) {
          mutatePrefix(ctx.prefix, ctx);
        }
      }
    }

    @Override
    public void exitRo_network(Ro_networkContext ctx) {
      if (ctx.ip != null) {
        mutateIp(ctx.ip, ctx);
      } else if (ctx.prefix != null) {
        mutatePrefix(ctx.prefix, ctx);
      }
    }

    @Override
    public void exitNetwork_bgp_tail(Network_bgp_tailContext ctx) {
      if (ctx.ip!= null ){
        mutateIp(ctx.ip, ctx);
      }
      else if (ctx.prefix != null) {
        mutatePrefix(ctx.prefix, ctx);
      }
    }

    @Override
    public void exitRs_route(Rs_routeContext ctx) {
      // if (ctx.prefix !=null ){
      //   mutatePrefix(ctx.prefix);
      // }
      if (ctx.nhip != null){
        mutateIp(ctx.nhip, ctx);
      }
    }

  }
