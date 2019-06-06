package edu.colgate.cs.modification;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.apache.commons.io.FileUtils;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.VendorConfigurationFormatDetector;
import org.batfish.grammar.cisco.CiscoCombinedParser;
import org.batfish.grammar.cisco.CiscoControlPlaneExtractor;
import org.batfish.main.Batfish;
import org.batfish.main.ParserBatfishException;
import org.batfish.vendor.VendorConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Configuration of a device in network.
 */

public class Config {

    private static org.batfish.config.Settings batfishSettings;

    private static Warnings batfishWarnings;

    private String hostname;

    private String text;

    private ParserRuleContext parseTree;

    private List<? extends Token> tokens;

    private ConfigurationFormat format;

    private CiscoCombinedParser combinedParser;

    private ControlPlaneExtractor extractor;

    private VendorConfiguration vendorConfiguration;

    private Configuration genericConfiguration;

    public Config(String text, String hostname) {
        this.hostname = hostname;
        this.text = text;

        this.format = VendorConfigurationFormatDetector
                .identifyConfigurationFormat(this.text);

        if (null == batfishSettings) {
            batfishSettings = new org.batfish.config.Settings();
            batfishSettings.setLogger(new BatfishLogger(
                    batfishSettings.getLogLevel(),
                    batfishSettings.getTimestamp(), System.out));

            batfishSettings.setDisableUnrecognized(true);
            batfishWarnings = new Warnings(true, true, false);
        }

        switch(this.format){
            case CISCO_IOS:
            case CISCO_IOS_XR:
            case CISCO_NX:
                this.combinedParser = new CiscoCombinedParser(this.text,
                        batfishSettings, this.format);

                this.extractor = new CiscoControlPlaneExtractor(this.text,
                        this.combinedParser, this.format, batfishWarnings);
                break;
            default:
                throw new BatfishException("Invalid configuration");
        }
    }

    public Config(File file) throws IOException {
        this(FileUtils.readFileToString(file),
                file.getName().toString().replaceAll("\\.(cfg|conf)$",""));
    }

    public List<? extends Token> getTokens(){
        if (null==this.tokens){
            this.combinedParser.getLexer().reset();
            this.tokens = this.combinedParser.getLexer().getAllTokens();
        }
        return this.tokens;
    }

    public ParserRuleContext getParseTree() {
        if (null == parseTree){
            this.combinedParser.getLexer().reset();

            try{
                this.parseTree = Batfish.parse(
                        this.combinedParser,
                        batfishSettings.getLogger(),batfishSettings);
            }catch(ParserBatfishException e){
                e.printStackTrace();
            }
        }

        return this.parseTree;
    }

    public VendorConfiguration getVendorConfiguration() {
        if (null == this.vendorConfiguration){
            try{
                extractor.processParseTree(this.getParseTree());
            }
            catch (BatfishException be){
                be.printStackTrace();
            }
            this.vendorConfiguration = extractor.getVendorConfiguration();
            this.vendorConfiguration.setVendor(this.format);
            this.vendorConfiguration.setWarnings(this.batfishWarnings);
        }
        return this.vendorConfiguration;
    }

    public Configuration getGenericConfiguration(){
        // if (null == this.genericConfiguration){
        //     this.genericConfiguration = this.getVendorConfiguration()
        //             .toVendorIndependentConfiguration();
        // }
        return this.genericConfiguration;
    }

    public String getHostname() {
        return hostname;
    }

    public String getText() {
        return text;
    }
}
