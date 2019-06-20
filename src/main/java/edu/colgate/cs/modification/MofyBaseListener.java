package edu.colgate.cs.modification;

import edu.colgate.cs.config.Settings;
import java.util.Random;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.batfish.grammar.cisco.CiscoParserBaseListener;
import org.batfish.grammar.cisco.CiscoParser.*;

public class MofyBaseListener extends  CiscoParserBaseListener {

    protected TokenStreamRewriter rewriter;
    protected int percent;
    protected Random generator;
    protected Config config;

    public MofyBaseListener(Random generator, int percent, Config config, 
            TokenStreamRewriter rewriter) {
        this.rewriter = rewriter;
        this.percent = percent;
        this.config = config;
        this.generator = generator;
    }

    protected void logChange(Settings.modtype type, String subtype, 
            ParserRuleContext ctx) {
        String[] stanzaIdentifier = getStanzaIdentifier(ctx).split(" ");
        System.out.printf("{\"type\":\"%s\",\"subtype\":\"%s\","
                + "\"stanzatype\":\"%s\",\"stanzaname\":\"%s\","
                + "\"hostname\":\"%s\",\"line\":%d}\n", 
                type, subtype, stanzaIdentifier[0], stanzaIdentifier[1],
                config.getHostname(), ctx.getStart().getLine());
    }

    protected String getStanzaIdentifier(ParserRuleContext ctx) {
        if (ctx instanceof Access_list_ip_rangeContext) {
            return getStanzaIdentifier((Access_list_ip_rangeContext)ctx);
        }
        else if (ctx instanceof If_ip_addressContext) {
            return getStanzaIdentifier((If_ip_addressContext)ctx);
        }
        else if (ctx instanceof Ro_networkContext) {
            return getStanzaIdentifier((Ro_networkContext)ctx);
        }
        else if (ctx instanceof Network_bgp_tailContext) {
            return getStanzaIdentifier((Network_bgp_tailContext)ctx);
        }
        else if (ctx instanceof Rs_routeContext) {
            return getStanzaIdentifier((Rs_routeContext)ctx);
        }
        return "null null";
    }

    protected String getStanzaIdentifier(Access_list_ip_rangeContext ctx) {
        ParserRuleContext parent = ctx.getParent();
        if (parent instanceof Extended_access_list_tailContext) {
            return getStanzaIdentifier(
                    (Extended_access_list_tailContext)parent);
        }
        else if (parent instanceof Standard_access_list_tailContext) {
            return getStanzaIdentifier(
                    (Standard_access_list_tailContext)parent);
        }
        return null;
    }

    protected String getStanzaIdentifier(Extended_access_list_tailContext ctx) {
        Extended_access_list_stanzaContext extendedAcl = 
                (Extended_access_list_stanzaContext)ctx.getParent();
        String id = "";
        if (extendedAcl.name != null) {
            id = extendedAcl.name.getText();
        } 
        else if (extendedAcl.num != null) {
            id = extendedAcl.num.getText();
        }
        else if (extendedAcl.shortname != null) {
            id = extendedAcl.shortname.getText();
        }
        return "ExtendedAccessList " + id;
    }

    protected String getStanzaIdentifier(Standard_access_list_tailContext ctx) {
        Standard_access_list_stanzaContext standardAcl = 
                (Standard_access_list_stanzaContext)ctx.getParent();
        String id = "";
        if (standardAcl.name != null) {
            id = standardAcl.name.getText();
        } 
        else if (standardAcl.num != null) {
            id = standardAcl.num.getText();
        }
        return "StandardAccessList "+id;
    }

    protected String getStanzaIdentifier(If_ip_addressContext ctx) {
        S_interfaceContext iface = 
                (S_interfaceContext)ctx.getParent().getParent();
        return "Interface " + iface.iname.getText();
    }

    protected String getStanzaIdentifier(Ro_networkContext ctx) {
        S_router_ospfContext ospf = (S_router_ospfContext)ctx.getParent();
        return "OSPFNetwork " + ospf.name.getText();
    }

    protected String getStanzaIdentifier(Network_bgp_tailContext ctx) {
        ParserRuleContext grandparent = ctx.getParent().getParent();
//        if (grandparent instanceof Address_family_rb_stanzaContext) {
//        if (grandparent instanceof Af_group_rb_stanzaContext) {
//        if (grandparent instanceof Neighbor_block_address_familyContext) {
//        if (grandparent instanceof Neighbor_block_rb_stanzaContext) {
//        if (grandparent instanceof Neighbor_flat_rb_stanzaContext) {
//        if (grandparent instanceof Vrf_block_rb_stanzaContext) {
//        if (grandparent instanceof Router_bgp_stanza_tailContext) {
//        if (grandparent instanceof Session_group_rb_stanzaContext) {
//        if (grandparent instanceof Template_peer_address_familyContext) {
//        if (grandparent instanceof Template_peer_policy_rb_stanzaContext) {
//        if (grandparent instanceof Template_peer_session_rb_stanzaContext) {
        return "BGPNetwork null";
    }

    
    protected String getStanzaIdentifier(Rs_routeContext ctx) {
        return "StaticRoute null";
    }
}
