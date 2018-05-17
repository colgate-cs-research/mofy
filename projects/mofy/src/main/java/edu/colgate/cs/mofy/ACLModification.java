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
    boolean isDeny;

    ACLModification(String hostName,
                    Interface iface,
                    Prefix filterCriteria,
                    boolean inboundAcl,
                    boolean isDeny){
        this.host = hostName;
        this.iface = iface;
        if (inboundAcl) {
            this.inboundFilterNetwork = filterCriteria;
        }else{
            this.outboundFilterNetwork = filterCriteria;
        }
        isInbound = inboundAcl;
        this.isDeny = isDeny;
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


    /*
     * Construct a standard ACL Entry from the modification information
     * eg:- "access-list 1 deny 10.2.0.0 0.0.255.255\n access-list 1 permit any"
     * @return string representing Standard ACL stanza
     */
    public String getACLEntry(){
        int aclNum = 1; //TODO: Fix this , use an available number.
        //TODO:  Fix filter network (from 10.0.0.0/24 -> 10.0.0.0 0.255.255.255)
        String permitLine = String.format("access-list %d permit any any", aclNum);
        String accessList = String.format("access-list %d %s %s \n%s",
                aclNum,
                isDeny?"deny":"permit",
                inboundFilterNetwork.toString(),
                isDeny?permitLine:"");
        return accessList;
    }

    @Override
    public String toString() {
        return String.format("Host: %s | Iface: %s | Filter: %s | Is Inbound? : %b",
                host, iface.getName(),
                isInbound?inboundFilterNetwork:outboundFilterNetwork,
                isInbound);
    }
}
