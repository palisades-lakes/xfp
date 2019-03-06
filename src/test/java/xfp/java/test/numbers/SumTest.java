package xfp.java.test.numbers;

import java.util.Arrays;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.ListSampler;
import org.junit.jupiter.api.Test;

import xfp.java.linear.BigFractionsN;
import xfp.java.linear.Dn;
import xfp.java.linear.Qn;
import xfp.java.numbers.Doubles;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;
import xfp.java.prng.PRNG;

//----------------------------------------------------------------
/** Test summation algorithms. 
 * <p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/numbers/SumTest test > SumTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-05
 */

// no actual tests here (yet)

public final class SumTest {

  private static final int DIM = 1 * 1024;
  private static final int DELTA = 
    Doubles.MAXIMUM_BIASED_EXPONENT  
    - 30 
    + Integer.numberOfLeadingZeros(2*DIM);
  private static final int TRYS = 4;

  @SuppressWarnings({ "static-method" })
  @Test
  public final void naiveSumTest () {

    final UniformRandomProvider urp = 
      PRNG.well44497b("seeds/Well44497b-2019-01-05.txt");
    final Generator g = 
      Generators.finiteDoubleGenerator(DIM,urp,DELTA);
    final double[] x0 = (double[]) g.next();
    final double[] x = Dn.concatenate(x0,Dn.get(DIM).negate(x0));
    ListSampler.shuffle(urp,Arrays.asList(x));
    double mean;
    long t;

    t = System.nanoTime();
    mean = 0.0;
    for (int i=0;i<TRYS;i++) { mean += Dn.naiveSum(x); }
    t = (System.nanoTime()-t);
    mean /= TRYS;
    System.out.println(mean + " in " + (t*1.0e-9) + " secs Dn.naiveSum");

    mean = 0.0;
    t = System.nanoTime();
    for (int i=0;i<TRYS;i++) { mean += Qn.naiveSum(x); }
    t = (System.nanoTime()-t);
    mean /= TRYS;
    System.out.println(mean + " in " + (t*1.0e-9) + " secs Qn.naiveSum");

    mean = 0.0;
    t = System.nanoTime();
    for (int i=0;i<TRYS;i++) { mean += BigFractionsN.naiveSum(x); }
    t = (System.nanoTime()-t);
    mean /= TRYS;
    System.out.println(mean + " in " + (t*1.0e-9) + " secs BigFractionsN.naiveSum");
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
