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

public class SubnetModifier extends Modifier<SubnetModification>{

    private TokenStreamRewriter rewriter;

    /** Current ACL Modification to be applied */
    private SubnetModification SubnetModification;

    private int percentage;
    private long seed;
    private static Random generator;

    /**
     * Creates an ACLModifier for a set of configuration.
     * @param configs List of Configs for the network to be modified.
     */
    public SubnetModifier(List<Config> configs, Settings setting){
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
    public void modify(SubnetModification Modification){
        this.SubnetModification = Modification;
        String hostname = SubnetModification.getHost();
        if (!hostToConfigMap.containsKey(hostname)){
            System.out.printf("Host %s : NOT FOUND!", hostname);
            return;
        }
        this.percentage = SubnetModification.getPercent();
        this.seed =  SubnetModification.getSeed();
        Config config = hostToConfigMap.get(hostname);

        ListTokenSource tokenSource = new ListTokenSource(config.getTokens());
        CommonTokenStream commonTokenStream = new CommonTokenStream(tokenSource);
        commonTokenStream.fill();
        rewriter = new TokenStreamRewriter(commonTokenStream);
        SubnetWalkListener listener = new SubnetWalkListener(SubnetModification, rewriter);
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(listener,config.getParseTree());

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

    static class SubnetWalkListener extends  CiscoParserBaseListener{

        SubnetModification SubnetModification;
        TokenStreamRewriter rewriter;

        SubnetWalkListener(SubnetModification SubnetModification,
                               TokenStreamRewriter rewriter) {
            this.SubnetModification = SubnetModification;
            this.rewriter = rewriter;
        }

        private void mutateSubnet(Token ip, Token mask) {
          InterfaceAddress ifaceAddr = new InterfaceAddress(new Ip(ip.getText()),
              new Ip(mask.getText()));
          IpWildcard replacement = mutate(new IpWildcard(ifaceAddr.getPrefix()));
          rewriter.replace(mask.getTokenIndex(),
              replacement.getWildcard().toString());
        }

        private void mutate(Token ip, Token mask) {
          IpWildcard replacement = mutate(new IpWildcard(new Ip(ip.getText()),
              new Ip(mask.getText())));
          rewriter.replace(mask.getTokenIndex(),
              replacement.getWildcard().toString());
        }

        private void mutate(Token prefix) {
          IpWildcard replacement = mutate(new IpWildcard(prefix.getText()));
          rewriter.replace(prefix.getTokenIndex(), replacement.toString());
        }

        private IpWildcard mutate(IpWildcard orig) {
          Double num = generator.nextDouble()*100;
          if (num>(100-SubnetModification.getPercent())){
            int prefixLen = orig.toPrefix().getPrefixLength();
            if (prefixLen != 32){
              prefixLen++;
            }
            else {
              prefixLen--;
            }
            return new IpWildcard(new Prefix(orig.getIp(), prefixLen));
          }
          else{
            return orig;
          }
        }

        @Override
        public void exitAccess_list_ip_range(Access_list_ip_rangeContext ctx) {
          if (ctx.wildcard != null) {
            mutate(ctx.ip, ctx.wildcard);
          } else if (ctx.prefix != null) {
            mutate(ctx.prefix);
          }
        }

        @Override
        public void exitIf_ip_address(If_ip_addressContext ctx) {
          if (ctx.subnet != null) {
            mutateSubnet(ctx.ip, ctx.subnet);
          } else if (ctx.prefix != null) {
            mutate(ctx.prefix);
          }
        }

        @Override
        public void exitRo_network(Ro_networkContext ctx) {
          if (ctx.wildcard != null) {
            mutate(ctx.ip, ctx.wildcard);
          } else if (ctx.prefix != null) {
            mutate(ctx.prefix);
          }
        }

        @Override
        public void exitNetwork_bgp_tail(Network_bgp_tailContext ctx) {
          if (ctx.mask != null) {
            mutate(ctx.ip, ctx.mask);
          } else if (ctx.prefix != null) {
            mutate(ctx.prefix);
          }
        }
      }
}
