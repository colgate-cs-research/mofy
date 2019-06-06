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

public class SubnetModifier extends Modifier<ModifierSetting>{

    private TokenStreamRewriter rewriter;

    private ModifierSetting SubnetModifierSetting;
    private static String hostname;

    /**
     * Creates an SubnetModifier for a set of configuration.
     * @param configs List of Configs for the network to be modified.
     */
    public SubnetModifier(List<Config> configs, Settings setting){
        super(configs,setting);
    }

    /**
     * Add ACL (specified by param) into chosen config.
     * @param ModifierSetting ModifierSetting needs to be made.
     */
    public void modify(ModifierSetting ModifierSetting, String host){
        this.SubnetModifierSetting = ModifierSetting;
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
        SubnetWalkListener listener = new SubnetWalkListener(SubnetModifierSetting, rewriter);
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

    static class SubnetWalkListener extends  CiscoParserBaseListener{

        ModifierSetting SubnetModifierSetting;
        TokenStreamRewriter rewriter;

        SubnetWalkListener(ModifierSetting SubnetModifierSetting,
                               TokenStreamRewriter rewriter) {
            this.SubnetModifierSetting = SubnetModifierSetting;
            this.rewriter = rewriter;
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
            System.out.println("subnet change (subnet) at configuration "+hostname+" line: "+ipToken.getLine());
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
            System.out.println("subnet change (wildcard) at configuration "+hostname+" line: "+ipToken.getLine());
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
            System.out.println("subnet change (prefix) at configuration "+hostname+" line: "+prefixToken.getLine());
          }
         }

        private void mutateAddress(Token ipToken){
          Ip ip = Ip.parse(ipToken.getText());
          int subnetBits = Long.numberOfTrailingZeros(ip.asLong());
          Integer replacement = mutate(subnetBits);
          if (replacement != null){
            rewriter.replace(ipToken.getTokenIndex(), Prefix.create(ip,replacement).toString());
            System.out.println("subnet change (prefix, no mask) at configuration "+hostname+" line: "+ipToken.getLine());
          }
        }

        private Integer mutate(int orig) {
          Double num = generator.nextDouble()*100;
          int check = generator.nextInt(2);
          if (num<SubnetModifierSetting.getPercent()){
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
          if (ctx.subnet != null) {
            mutateSubnet(ctx.ip, ctx.subnet);
          } else if (ctx.prefix != null) {
            mutatePrefix(ctx.prefix);
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
}