package edu.colgate.cs.mofy;

import edu.wisc.cs.arc.configs.Config;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.grammar.cisco.CiscoParserBaseListener;
import org.batfish.grammar.cisco.CiscoParser.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

public class ASTModifier extends CiscoParserBaseListener{

    private Config config;
    private PrintStream output;

    public ASTModifier(Config config, File outputFile){
        this.config = config;
        try {
            output = new PrintStream(outputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void modify(){
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this,config.getParseTree());
        output.println("end");
        output.close();
    }

    /**
     * Print the tokens of a stanza.
     * @param ctx stanza of tokens to be printed
     */
    public void printTokens(ParserRuleContext ctx) {
        List<? extends Token> tokenList = this.config.getTokens(
            ctx.getStart().getTokenIndex(), ctx.getStop().getTokenIndex());
        output.print(" ");
        for (Token tok : tokenList){
            output.print(tok.getText());
        }
        output.println();
    }

    /**
     * Print a stanza separator.
     */
    public void printStanzaSeparator() {
        output.println("!");
    }


    @Override
    public void exitRouter_hsrp_if(Router_hsrp_ifContext ctx){
        printTokens(ctx);
    }
    @Override
    public void exitRoute_policy_stanza(Route_policy_stanzaContext ctx){
        printTokens(ctx);
    }
    @Override
    public void exitIp_route_stanza(Ip_route_stanzaContext ctx) {
        printTokens(ctx);
    }
    @Override
    public void exitPrefix_set_stanza(Prefix_set_stanzaContext ctx){
        printTokens(ctx);
    }
    @Override
    public void exitS_hostname(S_hostnameContext ctx) {
        printTokens(ctx);
        this.printStanzaSeparator();
    }
    @Override
    public void exitS_vrf_definition(S_vrf_definitionContext ctx){
        printTokens(ctx);
        this.printStanzaSeparator();
    }
    @Override
    public void exitS_router_static(S_router_staticContext ctx){
        printTokens(ctx);

    }
    @Override
    public void exitAs_path_set_stanza(As_path_set_stanzaContext ctx){
        printTokens(ctx);
    }
    @Override
    public void enterS_interface(S_interfaceContext ctx) {
        output.println("interface "+ctx.iname.getText());
        // output.println(ParseTreePrettyPrinter.print(ctx, config.combinedParser));
    }
    @Override
    public void exitIf_description(If_descriptionContext ctx){
        printTokens(ctx);
    }

    @Override
    public void exitIf_ip_access_group(If_ip_access_groupContext ctx){
        printTokens(ctx);
    }
    @Override
    public void exitIf_ip_address(If_ip_addressContext ctx){
        printTokens(ctx);
    }
    @Override
    public void exitIf_ip_address_secondary(If_ip_address_secondaryContext ctx){
        printTokens(ctx);
    }
    @Override
    public void exitIf_ip_ospf_area(If_ip_ospf_areaContext ctx){
        printTokens(ctx);
    }
    @Override
    public void exitIf_ip_ospf_cost(If_ip_ospf_costContext ctx){
        printTokens(ctx);
    }
    @Override
    public void exitIf_ip_ospf_passive_interface(If_ip_ospf_passive_interfaceContext ctx){
        printTokens(ctx);
    }   @Override
    public void exitIf_ip_policy(If_ip_policyContext ctx){
        printTokens(ctx);
    }   @Override
    public void exitIf_ip_router_ospf_area(If_ip_router_ospf_areaContext ctx){
        printTokens(ctx);
    }   @Override
    public void exitIf_no_ip_address(If_no_ip_addressContext ctx){
        printTokens(ctx);
    }   @Override
    public void exitIf_shutdown(If_shutdownContext ctx){
        printTokens(ctx);
    }
    @Override
    public void exitIf_switchport(If_switchportContext ctx){
        printTokens(ctx);
    }
    @Override
    public void exitIf_switchport_access(If_switchport_accessContext ctx){
        printTokens(ctx);
    }
    @Override
    public void exitIf_switchport_mode(If_switchport_modeContext ctx){
        printTokens(ctx);
    }
    @Override
    public void exitIf_switchport_trunk_allowed(If_switchport_trunk_allowedContext ctx){
        printTokens(ctx);
    }
    @Override
    public void exitIf_vrf(If_vrfContext ctx){
        printTokens(ctx);
    }   @Override
    public void exitIf_vrf_forwarding(If_vrf_forwardingContext ctx){
        printTokens(ctx);
    }   @Override
    public void exitIf_vrf_member(If_vrf_memberContext ctx){
        printTokens(ctx);
    }
    @Override
    public void exitS_interface(S_interfaceContext ctx) {
        this.printStanzaSeparator();
    }
    @Override
    public void exitS_policy_map(S_policy_mapContext ctx){
        printTokens(ctx);
    }
    @Override
    public void exitS_class_map(S_class_mapContext ctx){
        printTokens(ctx);
    }
    @Override
    public void exitRouter_bgp_stanza(Router_bgp_stanzaContext ctx) {
        printTokens(ctx);
        this.printStanzaSeparator();
    }

    @Override
    public void exitRoute_map_stanza(Route_map_stanzaContext ctx){
        printTokens(ctx);
        this.printStanzaSeparator();
    }
    @Override
    public void exitIp_prefix_list_stanza(Ip_prefix_list_stanzaContext ctx){
        printTokens(ctx);
    }

    @Override
    public void exitS_router_ospf(S_router_ospfContext ctx) {
        printTokens(ctx);
        this.printStanzaSeparator();
    }

    @Override
    public void exitS_router_rip(S_router_ripContext ctx){
        printTokens(ctx);
        this.printStanzaSeparator();
    }

    @Override
    public void exitStandard_access_list_stanza(
        Standard_access_list_stanzaContext ctx) {
        printTokens(ctx);
        this.printStanzaSeparator();
    }

    @Override
    public void exitS_vlan(S_vlanContext ctx){
        printTokens(ctx);
    }

    @Override
    public void exitExtended_access_list_stanza(
        Extended_access_list_stanzaContext ctx) {
        printTokens(ctx);
        this.printStanzaSeparator();
    }


}
