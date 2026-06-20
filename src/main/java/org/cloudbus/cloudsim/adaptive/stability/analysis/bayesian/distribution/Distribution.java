package org.cloudbus.cloudsim.adaptive.stability.analysis.bayesian.distribution;

import java.io.Serializable;
import java.util.Random;

import org.cloudbus.cloudsim.adaptive.stability.analysis.bayesian.variables.Assignment;
import org.cloudbus.cloudsim.adaptive.stability.analysis.bayesian.variables.Variable;

/**
* This class defines and handles Distribution.
*
* <p> For an example of use follow this link </p>
* <p> <a href="http://amidst.github.io/toolbox/CodeExamples.html#bnmodifyexample"> http://amidst.github.io/toolbox/CodeExamples.html#bnmodifyexample </a>  </p>
*
*/
public abstract class Distribution implements Serializable {

   /** Represents the serial version ID for serializing the object. */
   private static final long serialVersionUID = -3436599636425587512L;

   /** Represents the variable associated with the distribution. */
   protected Variable var;

   /**
    * Returns the parameters of this Distribution.
    * @return an Array of doubles containing the parameters of this Distribution.
    */
   public abstract double[] getParameters();

   /**
    * Returns the total number of parameters in this Distribution.
    * @return the total number of parameters in this Distribution.
    */
   public abstract int getNumberOfParameters();

   /**
    * Returns the variable of this Distribution.
    * @return a {@link Variable} object.
    */
   public Variable getVariable() {
       return this.var;
   }

   /**
    * Sets the variable of the distribution.
    * WARNING: This method should only be used in exceptional cases.
    * It may affect the coherence of the graphical model.
    * @param var, A variable object.
    */
   public void setVar(Variable var) {
       this.var = var;
   }

   /**
    * Returns the name of this Distribution.
    * @return a String representing the name of this Distribution.
    */
   public abstract String label();

   /**
    * Randomly initializes this Distribution.
    * @param random a {@link java.util.Random} object.
    */
   public abstract void randomInitialization(Random random);

   /**
    * Tests if  a given distribution is equal to this Distribution.
    * Two distributions are considered equal if the difference is lower than a given threshold.
    * @param dist a Distribution object.
    * @param threshold a threshold.
    * @return true if the two distributions are equal, false otherwise.
    */
   public abstract boolean equalDist(Distribution dist, double threshold);

   /**
    * Returns a textual representation of this Distribution.
    * @return a String describing this Distribution.
    */
   public abstract String toString();

   /**
    * Returns the log probability of an {@link Assignment} object according to this Distribution.
    * @param assignment an {@link Assignment} object.
    * @return the log probability of an assignment.
    */
   public abstract double getLogProbability(Assignment assignment);
}