package xfp.java.test.accumulators;

import static java.lang.Double.toHexString;

import java.util.Arrays;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Test;

import xfp.java.Classes;
import xfp.java.accumulators.Accumulator;
import xfp.java.accumulators.DoubleAccumulator;
import xfp.java.accumulators.DoubleFmaAccumulator;
import xfp.java.accumulators.RBFAccumulator;
import xfp.java.linear.Dn;
import xfp.java.numbers.Doubles;
import xfp.java.prng.Generator;
import xfp.java.prng.PRNG;

//----------------------------------------------------------------
/** Test summation algorithms. 
 * <p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/numbers/DotTest test > DotTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-29
 */

//no actual tests here (yet)

public final class DotTest {

  //--------------------------------------------------------------
  /** See {@link Integer#numberOfLeadingZeros(int)}. */
  private static final int ceilLog2 (final int k) {
    return Integer.SIZE - Integer.numberOfLeadingZeros(k-1); }

  /** Maximum exponent for double generation such that a float sum 
   * of <code>dim</code> <code>double</code>s will be finite
   * (with high enough probability).
   */
  private static final int feMax (final int dim) { 
    final int d = Float.MAX_EXPONENT - ceilLog2(dim);
    //System.out.println("emax=" + d);
    return d; }

  private static double[][] sampleDoubles (final Generator g,
                                           final int n) {
    final double[][] x = new double[n][];
    for (int i=0;i<n;i++) { x[i] = (double[]) g.next(); }
    return x; }

  private static final int DIM = 16 * 1024;
  private static final int N = 2;

  //--------------------------------------------------------------

  @SuppressWarnings({ "static-method" })
  @Test
  public final void dotTest () {

    final UniformRandomProvider urp = 
      PRNG.well44497b("seeds/Well44497b-2019-01-05.txt");
    final Generator g = 
      Doubles.finiteGenerator(DIM,urp,feMax(DIM));

    final double[][] x0 = sampleDoubles(g,N);
    final double[][] x1 = sampleDoubles(g,N);

    final double[] truth = new double[N];
    final double[] pred = new double[N];
    for (int i=0;i<N;i++) { 
      truth[i] = 
        RBFAccumulator.make().addProducts(x0[i],x1[i]).doubleValue(); }

    System.out.println();
    for (int i=0;i<N;i++) { 
      System.out.println(
        i + " : " 
          + Double.toHexString(truth[i]) 
          + ", " 
          + Double.toHexString(Dn.maxAbs(x0[i])) 
          + ", " 
          + Double.toHexString(Dn.maxAbs(x1[i]))); }
    System.out.println();
    final Accumulator[] accumulators = 
    {
     DoubleAccumulator.make(),
     DoubleFmaAccumulator.make(),
     //     FloatAccumulator.make(),
     //     FloatFmaAccumulator.make(),
     //     RationalAccumulator.make(),
     RBFAccumulator.make(),
    };

    for (final Accumulator a : accumulators) {
      long t;
      t = System.nanoTime();
      for (int i=0;i<N;i++) { 
        pred[i] = 
          a.clear().addProducts(x0[i],x1[i]).doubleValue(); }
      t = (System.nanoTime()-t);
      System.out.println(Arrays.toString(truth)); 
      System.out.println(Arrays.toString(pred)); 
        System.out.println(toHexString(Dn.l1Dist(truth,pred)) + 
        " in " + (t*1.0e-9) 
        + " secs " + Classes.className(a)); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
