package edu.colgate.cs.mofy;

import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Prefix;

/*
 * Information required to make modification..
 * ONLY STANDARD ACL (1-99, 1300-1999)
 */
class ACLModification {

    String host;
    Interface iface;
    Prefix inboundFilterNetwork;
    Prefix outboundFilterNetwork;

    boolean isInbound;

    ACLModification(String hostName,
                    Interface iface,
                    Prefix filterCriteria,
                    boolean inboundAcl){
        this.host = hostName;
        this.iface = iface;
        if (inboundAcl) {
            this.inboundFilterNetwork = filterCriteria;
        }else{
            this.outboundFilterNetwork = filterCriteria;
        }
        isInbound = inboundAcl;
    }

    public String getHost() {
        return host;
    }

    public Interface getIface() {
        return iface;
    }

    public Prefix getInboundFilterNetwork() {
        return inboundFilterNetwork;
    }

    public Prefix getOutboundFilterNetwork() {
        return outboundFilterNetwork;
    }

    public boolean isInbound() {
        return isInbound;
    }

    @Override
    public String toString() {
        return String.format("Host: %s | Iface: %s | Filter: %s | Is Inbound? : %b",
                host, iface.getName(),
                isInbound?inboundFilterNetwork:outboundFilterNetwork,
                isInbound);
    }
}
