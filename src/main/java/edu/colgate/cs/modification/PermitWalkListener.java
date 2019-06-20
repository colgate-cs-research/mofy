package edu.colgate.cs.modification;

import edu.colgate.cs.config.Settings;
import java.util.Random;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.grammar.cisco.CiscoParser.*;

public class PermitWalkListener extends MofyBaseListener {

    public PermitWalkListener(Random generator, int percent, Config config,
                           TokenStreamRewriter rewriter) {
        super(generator, percent, config, rewriter);
    }

    @Override
    public void exitStandard_access_list_tail(Standard_access_list_tailContext ctx) {
      Double num = generator.nextDouble()*100;
      if (num>(100-percent)){
        logChange(Settings.modtype.Permit, null, ctx);
        if (ctx.ala.getStart().toString().contains("permit")){
          rewriter.replace(ctx.ala.getStart(),"deny");}
        else{
          rewriter.replace(ctx.ala.getStart(),"permit");}
      }
    }
    @Override
    public void exitExtended_access_list_tail(Extended_access_list_tailContext ctx) {
      Double num = generator.nextDouble()*100;
      if (num>(100-percent)){
        logChange(Settings.modtype.Permit, null, ctx);
        if (ctx.ala.getStart().toString().contains("permit")){
          rewriter.replace(ctx.ala.getStart(),"deny");}
        else{
          rewriter.replace(ctx.ala.getStart(),"permit");}
      }
    }
  }
