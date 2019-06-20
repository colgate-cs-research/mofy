package edu.colgate.cs.modification;

import edu.colgate.cs.config.Settings;
import java.util.Random;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.*;
import org.batfish.grammar.cisco.CiscoParser.*;

public class SubnetWalkListener extends MofyBaseListener {

    private boolean ifchange;
    private boolean largescale;

    public SubnetWalkListener(boolean largescale, boolean ifchange, 
            Random generator, int percent, Config config, 
            TokenStreamRewriter rewriter) {
        super(generator, percent, config, rewriter);
        this.ifchange = ifchange;
        this.largescale = largescale;
    }

    private void mutateSubnet(Token ipToken, Token subnetMaskToken, 
            ParserRuleContext ctx) {
      //System.out.println("1 " +ip.getText()+" "+ mask.getText());
      Ip ip = Ip.parse(ipToken.getText());
      Ip subnetMask = Ip.parse(subnetMaskToken.getText());
      int subnetBits = Prefix.MAX_PREFIX_LENGTH - Long.numberOfTrailingZeros(subnetMask.asLong());
      Integer replacement = mutate(subnetBits);
      if (replacement!=null){
        rewriter.replace(subnetMaskToken.getTokenIndex(),
            Ip.numSubnetBitsToSubnetMask(replacement).toString());
        logChange(Settings.modtype.Subnet, "Mask", ctx);
      }
    }

    private void mutateWildcard(Token ipToken, Token wildcardMaskToken,
            ParserRuleContext ctx) {
      //System.out.println("2 "+ip.getText()+" "+ mask.getText());
      Ip ip = Ip.parse(ipToken.getText());
      Ip wildcardMask = Ip.parse(wildcardMaskToken.getText());
      int wildcardBits = Integer.numberOfLeadingZeros((int)wildcardMask.asLong());
      Integer replacement = mutate(wildcardBits);
      if (replacement!=null){
        rewriter.replace(wildcardMaskToken.getTokenIndex(),
                    Ip.create((1L << (Prefix.MAX_PREFIX_LENGTH - replacement)) - 1).toString());
        logChange(Settings.modtype.Subnet, "Wildcard", ctx);
      }
  }

    private void mutatePrefix(Token prefixToken, ParserRuleContext ctx) {
      //System.out.println("3 "+ prefix.getText());
      ConcreteInterfaceAddress prefix = ConcreteInterfaceAddress.parse(prefixToken.getText());
      Ip ip = prefix.getIp();
      int subnetBits = prefix.getNetworkBits();
      Integer replacement = mutate(subnetBits);
      if (replacement != null){
        rewriter.replace(prefixToken.getTokenIndex(), Prefix.create(ip, replacement).toString());
        logChange(Settings.modtype.Subnet, "Prefix", ctx);
      }
     }

    private void mutateAddress(Token ipToken, ParserRuleContext ctx){
      Ip ip = Ip.parse(ipToken.getText());
      int subnetBits = Long.numberOfTrailingZeros(ip.asLong());
      Integer replacement = mutate(subnetBits);
      if (replacement != null){
        rewriter.replace(ipToken.getTokenIndex(), Prefix.create(ip,replacement).toString());
        logChange(Settings.modtype.Subnet, "AutoPrefix", ctx);
      }
    }

    private Integer mutate(int orig) {
      Double num = generator.nextDouble()*100;
      int check = generator.nextInt(2);
      if (num<this.percent){
        if (orig == 32 && !largescale){
          orig --;
        }
        else if (orig >24 && largescale){
          orig = 32;
        }
        else if (orig == 32 && largescale){
          orig = orig - 8;
        }
        else if (!largescale){
          orig ++;
        }
        else if (largescale){
          orig = orig + 8;
        }
        return orig;
      }
      else{
        return null;
      }
    }

    @Override
    public void exitAccess_list_ip_range(Access_list_ip_rangeContext ctx) {
      if (ctx.wildcard != null) {
          mutateWildcard(ctx.ip, ctx.wildcard, ctx);
      } else if (ctx.prefix != null) {
          mutatePrefix(ctx.prefix, ctx);
      }
    }

    @Override
    public void exitIf_ip_address(If_ip_addressContext ctx) {
      if (ifchange){
        if (ctx.subnet != null) {
          mutateSubnet(ctx.ip, ctx.subnet, ctx);
        } else if (ctx.prefix != null) {
          mutatePrefix(ctx.prefix, ctx);
        }
      }
    }

    @Override
    public void exitRo_network(Ro_networkContext ctx) {
      if (ctx.wildcard != null) {
        mutateWildcard(ctx.ip, ctx.wildcard, ctx);
      } else if (ctx.prefix != null) {
        mutatePrefix(ctx.prefix, ctx);
      }
    }

    @Override
    public void exitNetwork_bgp_tail(Network_bgp_tailContext ctx) {
      if (ctx.ip!= null && ctx.mask == null){
        mutateAddress(ctx.ip, ctx);
      }
      if (ctx.mask != null) {
        mutateSubnet(ctx.ip, ctx.mask, ctx);
      } else if (ctx.prefix != null) {
        mutatePrefix(ctx.prefix, ctx);
      }
    }
  }
