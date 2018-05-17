package edu.colgate.cs.modification;

import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Prefix;

/**
 * Information required to add ACLs
 * ONLY STANDARD ACL (1-99, 1300-1999)
 */
public class ACLModification extends Modification{

    private String host;
    private Interface iface;
    private Prefix inboundFilterNetwork;
    private Prefix outboundFilterNetwork;
    private boolean isInbound;
    private boolean isDeny;

    private int aclNum;

    public ACLModification(String hostName,
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
        aclNum = 1; //TODO: Fix this , use an available number.

    }

    /**
     * Hostname of the device associated with modification.
     * @return HostName
     */
    public String getHost() {
        return host;
    }

    /**
     * Which interface is the ACL to be added in?
     * @return Interface object where the ACL must be applied.
     */
    public Interface getIface() {
        return iface;
    }

    /**
     * Network Address filter for the ACL, if inbound.
     * @return Prefix of the filter address.
     */
    public Prefix getInboundFilterNetwork() {
        return inboundFilterNetwork;
    }

    /**
     * Network Address filter for the ACL, if outbound.
     * @return Prefix of the filter address.
     */
    public Prefix getOutboundFilterNetwork() {
        return outboundFilterNetwork;
    }

    /**
     * Is the ACL inbound?
     * @return True, if ACL is inbound.
     */
    public boolean isInbound() {
        return isInbound;
    }

    /**
     * Is the ACL denying packets from filter address? (opposed to permit)
     * @return True, if deny
     */
    public boolean isDeny() {
        return isDeny;
    }

    /**
     * Convert from Prefix object to valid representation for ACL.
     * @param prefix Prefix object for network address to be filtered.
     * @return String representation of address with Wildcard Ip Address
     */
    private String prefixToACLString(Prefix prefix){
        return String.format("%s %s",
                prefix.getStartIp().toString(),
                prefix.getPrefixWildcard().toString());
    }

    /**
     * Construct a standard ACL Entry from the modification information
     * eg:- "access-list 1 deny 10.2.0.0 0.0.255.255\n access-list 1 permit any"
     * @return string representing Standard ACL stanza
     */
    public String getACLEntry(){
        String permitLine = String.format("access-list %d permit any any", aclNum);
        String accessList = String.format("access-list %d %s %s \n%s",
                aclNum,
                isDeny?"deny":"permit",
                prefixToACLString(inboundFilterNetwork),
                isDeny?permitLine:"");
        return accessList;
    }

    /**
     * ACL line in Interface stanza
     * @return Line for applying acl to designated interface
     */
    public String getACLIfaceLine() {
        return String.format("ip access-group %d %s", aclNum, isInbound?"in":"out");
    }

    @Override
    public String toString() {
        return String.format("Host: %s | Iface: %s | Filter: %s | Is Inbound? : %b",
                host, iface.getName(),
                isInbound?inboundFilterNetwork:outboundFilterNetwork,
                isInbound);
    }

}
