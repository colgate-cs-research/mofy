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
import java.util.List;
import java.util.Random;

public class SubnetWalkListener extends  CiscoParserBaseListener{

    TokenStreamRewriter rewriter;
    int percent;
    Random generator;
    Config config;
    boolean ifchange;

    public SubnetWalkListener(boolean ifchange, Random generator, int percent, Config config,
                           TokenStreamRewriter rewriter) {
        this.rewriter = rewriter;
        this.config = config;
        this.percent = percent;
        this.generator = generator;
        this.ifchange = ifchange;
    }

    private void mutateSubnet(Token ipToken, Token subnetMaskToken) {
      //System.out.println("1 " +ip.getText()+" "+ mask.getText());
      Ip ip = Ip.parse(ipToken.getText());
      Ip subnetMask = Ip.parse(subnetMaskToken.getText());
      int subnetBits = Prefix.MAX_PREFIX_LENGTH - Long.numberOfTrailingZeros(subnetMask.asLong());
      Integer replacement = mutate(subnetBits);
      if (replacement!=null){
        rewriter.replace(subnetMaskToken.getTokenIndex(),
            Ip.numSubnetBitsToSubnetMask(replacement).toString());
        System.out.println("subnet change (subnet) at configuration "+config.getHostname()+" line: "+ipToken.getLine());
      }
    }

    private void mutateWildcard(Token ipToken, Token wildcardMaskToken) {
      //System.out.println("2 "+ip.getText()+" "+ mask.getText());
      Ip ip = Ip.parse(ipToken.getText());
      Ip wildcardMask = Ip.parse(wildcardMaskToken.getText());
      int wildcardBits = Integer.numberOfLeadingZeros((int)wildcardMask.asLong());
      Integer replacement = mutate(wildcardBits);
      if (replacement!=null){
        rewriter.replace(wildcardMaskToken.getTokenIndex(),
                    Ip.create((1L << (Prefix.MAX_PREFIX_LENGTH - replacement)) - 1).toString());
        System.out.println("subnet change (wildcard) at configuration "+config.getHostname()+" line: "+ipToken.getLine());
      }
  }

    private void mutatePrefix(Token prefixToken) {
      //System.out.println("3 "+ prefix.getText());
      InterfaceAddress prefix = new InterfaceAddress(prefixToken.getText());
      Ip ip = prefix.getIp();
      int subnetBits = prefix.getNetworkBits();
      Integer replacement = mutate(subnetBits);
      if (replacement != null){
        rewriter.replace(prefixToken.getTokenIndex(), Prefix.create(ip, replacement).toString());
        System.out.println("subnet change (prefix) at configuration "+config.getHostname()+" line: "+prefixToken.getLine());
      }
     }

    private void mutateAddress(Token ipToken){
      Ip ip = Ip.parse(ipToken.getText());
      int subnetBits = Long.numberOfTrailingZeros(ip.asLong());
      Integer replacement = mutate(subnetBits);
      if (replacement != null){
        rewriter.replace(ipToken.getTokenIndex(), Prefix.create(ip,replacement).toString());
        System.out.println("subnet change (prefix, no mask) at configuration "+config.getHostname()+" line: "+ipToken.getLine());
      }
    }

    private Integer mutate(int orig) {
      Double num = generator.nextDouble()*100;
      int check = generator.nextInt(2);
      if (num<this.percent){
        if (orig == 32){
          orig --;
        }
        else {
          orig ++;
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
          mutateWildcard(ctx.ip, ctx.wildcard);
      } else if (ctx.prefix != null) {
          mutatePrefix(ctx.prefix);
      }
    }

    @Override
    public void exitIf_ip_address(If_ip_addressContext ctx) {
      if (ifchange){
        if (ctx.subnet != null) {
          mutateSubnet(ctx.ip, ctx.subnet);
        } else if (ctx.prefix != null) {
          mutatePrefix(ctx.prefix);
        }
      }
    }

    @Override
    public void exitRo_network(Ro_networkContext ctx) {
      if (ctx.wildcard != null) {
        mutateWildcard(ctx.ip, ctx.wildcard);
      } else if (ctx.prefix != null) {
        mutatePrefix(ctx.prefix);
      }
    }

    @Override
    public void exitNetwork_bgp_tail(Network_bgp_tailContext ctx) {
      if (ctx.ip!= null && ctx.mask == null){
        mutateAddress(ctx.ip);
      }
      if (ctx.mask != null) {
        mutateSubnet(ctx.ip, ctx.mask);
      } else if (ctx.prefix != null) {
        mutatePrefix(ctx.prefix);
      }
    }
  }
