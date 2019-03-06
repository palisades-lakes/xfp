package xfp.java.test.numbers;

import org.apache.commons.rng.UniformRandomProvider;
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
 * mvn -q -Dtest=xfp/java/test/numbers/DotTest test > DotTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-05
 */

//no actual tests here (yet)

public final class DotTest {

  private static final int DIM = 1 * 1024;
  private static final int DELTA = 2 +
    ((Doubles.MAXIMUM_BIASED_EXPONENT  
    - 30 
    + Integer.numberOfLeadingZeros(2*DIM)) / 2);
  private static final int TRYS = 1;
  private static final int WARMUP = 8;
  
  @SuppressWarnings({ "static-method" })
  @Test
  public final void naiveSumTest () {

    final UniformRandomProvider urp = 
      PRNG.well44497b("seeds/Well44497b-2019-01-05.txt");
    final Generator g = 
      Generators.finiteDoubleGenerator(DIM,urp,DELTA);
    double[] x0 = (double[]) g.next();
    x0 = Dn.concatenate(x0,Dn.get(DIM).negate(x0));
    double[] x1 = (double[]) g.next();
    x1 = Dn.concatenate(x1,x1);
    // TODO: shuffle together...
    double mean;
    long t;

    mean = 0.0;
    for (int i=0;i<WARMUP;i++) { mean += Dn.naiveDot(x0,x1); }
    t = System.nanoTime();
    for (int i=0;i<TRYS;i++) { mean += Dn.naiveDot(x0,x1); }
    t = (System.nanoTime()-t);
    mean /= (WARMUP+TRYS);
    System.out.println(mean + " in " + (t*1.0e-9) + " secs Dn.naiveDot");

    mean = 0.0;
    for (int i=0;i<WARMUP;i++) { mean += Dn.fmaDot(x0,x1); }
    t = System.nanoTime();
    for (int i=0;i<TRYS;i++) { mean += Dn.fmaDot(x0,x1); }
    t = (System.nanoTime()-t);
    mean /= (WARMUP+TRYS);
    System.out.println(mean + " in " + (t*1.0e-9) + " secs Dn.fmaDot");

    mean = 0.0;
    for (int i=0;i<WARMUP;i++) { mean += Qn.naiveDot(x0,x1); }
    t = System.nanoTime();
    for (int i=0;i<TRYS;i++) { mean += Qn.naiveDot(x0,x1); }
    t = (System.nanoTime()-t);
    mean /= (WARMUP+TRYS);
    System.out.println(mean + " in " + (t*1.0e-9) + " secs Qn.naiveSum");

    mean = 0.0;
    for (int i=0;i<WARMUP;i++) { mean += BigFractionsN.naiveDot(x0,x1); }
    t = System.nanoTime();
    for (int i=0;i<TRYS;i++) { mean += BigFractionsN.naiveDot(x0,x1); }
    t = (System.nanoTime()-t);
    mean /= (WARMUP+TRYS);
    System.out.println(mean + " in " + (t*1.0e-9) + " secs BigFractionsN.naiveSum");
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
