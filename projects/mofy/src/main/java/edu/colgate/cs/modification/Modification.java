package edu.colgate.cs.modification;

public class Modification {

      private int percentage;
      private long seed;

      public Modification(int percentage, long seed){
        this.percentage = percentage;
        this.seed = seed;

      }

      /**
       * Is the ACL denying packets from filter address? (opposed to permit)
       * @return True, if deny
       */
      public int getPercent(){
        return percentage;
      }
      public long getSeed(){
        return seed;
      }
    }
