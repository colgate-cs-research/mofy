package edu.colgate.cs.modification;

import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Prefix;

/**
 * Information required to add ACLs
 * ONLY STANDARD ACL (1-99, 1300-1999)
 */
public class SubnetModification extends Modification{
    private String host;
    private int percentage;
    private long seed;



    public SubnetModification(String host, int percentage, long seed){
      this.host = host;
      this.percentage = percentage;
      this.seed = seed;

    }

    /**
     * Is the ACL denying packets from filter address? (opposed to permit)
     * @return True, if deny
     */
    public String getHost(){
      return host;
    }
    public int getPercent(){
      return percentage;
    }
    public long getSeed(){
      return seed;
    }
}
