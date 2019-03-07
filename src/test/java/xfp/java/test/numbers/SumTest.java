package xfp.java.test.numbers;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Test;

import xfp.java.linear.BigDecimalsN;
import xfp.java.linear.BigFractionsN;
import xfp.java.linear.Dn;
import xfp.java.linear.ERationalsN;
import xfp.java.linear.Fn;
import xfp.java.linear.Qn;
import xfp.java.linear.RatiosN;
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
 * @version 2019-03-06
 */

// no actual tests here (yet)

public final class SumTest {

  //--------------------------------------------------------------
  /** Maximum exponent for double generation such that sum 
   * <code>dim</code> <code>double</code>s will be finite.
   */
  private static final int delta (final int dim) { 
    final int d =
      //Floats.MAXIMUM_BIASED_EXPONENT  
      Doubles.MAXIMUM_BIASED_EXPONENT  
      - 30 
      + Integer.numberOfLeadingZeros(dim); 
    System.out.println("delta=" + d);
    return d; }

  private static double[] sampleDoubles (final Generator g,
                                         final UniformRandomProvider urp) {
    double[] x = (double[]) g.next();
//   x = Dn.concatenate(x,Dn.minus(x));
//    ListSampler.shuffle(urp,Arrays.asList(x));
    return x; }
  
  private static double[][] sampleDoubles (final int dim,
                                           final int n) {
    final UniformRandomProvider urp = 
      PRNG.well44497b("seeds/Well44497b-2019-01-05.txt");
    final Generator g = 
      Generators.finiteDoubleGenerator(dim,urp);
    //Generators.finiteDoubleGenerator(dim,urp,delta(dim));
    
    final double[][] x = new double[n][];
    for (int i=0;i<n;i++) { x[i] = sampleDoubles(g,urp); }
    return x; }
  
  private static final int DIM = 2 * 1024;
  private static final int N = 8;

  @SuppressWarnings({ "static-method" })
  @Test
  public final void naiveSumTest () {

    final double[][] x = sampleDoubles(DIM,N);
    
    // should be zero with current construction
    final double[] truth = new double[N];
    // assuming ERational is correct!!!
    for (int i=0;i<N;i++) { truth[i] = ERationalsN.naiveSum(x[i]); }
    
    for (int i=0;i<N;i++) { 
      System.out.println(
        i + " : " + truth[i] + ", " + Dn.maxAbs(x[i])); }
    
    double maxError;
    long t;

    maxError = 0.0;
    t = System.nanoTime();
    for (int i=0;i<N;i++) { 
      final double sum = BigDecimalsN.naiveSum(x[i]); 
      maxError = Math.max(maxError,Math.abs(truth[i]-sum)); }
    t = (System.nanoTime()-t);
    System.out.println(maxError + " in " + (t*1.0e-9) + 
      " secs BigDecimalsN.naiveSum");

    maxError = 0.0;
    t = System.nanoTime();
    for (int i=0;i<N;i++) { 
      final double sum = BigFractionsN.naiveSum(x[i]); 
      maxError = Math.max(maxError,Math.abs(truth[i]-sum)); }
    t = (System.nanoTime()-t);
    System.out.println(maxError + " in " + (t*1.0e-9) + 
      " secs BigFractionsN.naiveSum");

    t = System.nanoTime();
    maxError = 0.0;
    for (int i=0;i<N;i++) { 
      final double sum = Dn.naiveSum(x[i]); 
      maxError = Math.max(maxError,Math.abs(truth[i]-sum)); }
    t = (System.nanoTime()-t);
    System.out.println(maxError + " in " + (t*1.0e-9) + 
      " secs Dn.naiveSum");

    t = System.nanoTime();
    maxError = 0.0;
    for (int i=0;i<N;i++) { 
      final double sum = ERationalsN.naiveSum(x[i]); 
      maxError = Math.max(maxError,Math.abs(truth[i]-sum)); }
    t = (System.nanoTime()-t);
    System.out.println(maxError + " in " + (t*1.0e-9) + ""
      + " secs ERationalsN.naiveSum");

    t = System.nanoTime();
    maxError = 0.0;
    for (int i=0;i<N;i++) { 
      final double sum = Fn.naiveSum(x[i]); 
      maxError = Math.max(maxError,Math.abs(truth[i]-sum)); }
    t = (System.nanoTime()-t);
    System.out.println(maxError + " in " + (t*1.0e-9) + 
      " secs Fn.naiveSum");

    maxError = 0.0;
    t = System.nanoTime();
    for (int i=0;i<N;i++) { 
      final double sum = Qn.naiveSum(x[i]); 
      maxError = Math.max(maxError,Math.abs(truth[i]-sum)); }
    t = (System.nanoTime()-t);
    System.out.println(maxError + " in " + (t*1.0e-9) + 
      " secs Qn.naiveSum");

    maxError = 0.0;
    t = System.nanoTime();
    for (int i=0;i<N;i++) { 
      final double sum = RatiosN.naiveSum(x[i]); 
      maxError = Math.max(maxError,Math.abs(truth[i]-sum)); }
    t = (System.nanoTime()-t);
    System.out.println(maxError + " in " + (t*1.0e-9) + 
      " secs RatiosN.naiveSum");

  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
