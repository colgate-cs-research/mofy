package edu.colgate.cs.modification;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import edu.colgate.cs.config.Settings;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.grammar.cisco.CiscoParserBaseListener;
import org.batfish.grammar.cisco.CiscoParser.*;
import java.util.Random;

public class PermitWalkListener extends  CiscoParserBaseListener{

    TokenStreamRewriter rewriter;
    int percent;
    Random generator;
    Config config;

    public PermitWalkListener(Random generator, int percent, Config config,
                           TokenStreamRewriter rewriter) {
        this.rewriter = rewriter;
        this.percent = percent;
        this.config = config;
        this.generator = generator;
    }

    @Override
    public void exitStandard_access_list_tail(Standard_access_list_tailContext ctx) {
      Double num = generator.nextDouble()*100;
      if (num>(100-percent)){
        System.out.println("permit change at configuration "+config.getHostname()+" line: "+ctx.ala.getStart().getLine());
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
        System.out.println("permit change at configuration "+config.getHostname()+" line: "+ctx.ala.getStart().getLine());
        if (ctx.ala.getStart().toString().contains("permit")){
          rewriter.replace(ctx.ala.getStart(),"deny");}
        else{
          rewriter.replace(ctx.ala.getStart(),"permit");}
      }
    }
  }
