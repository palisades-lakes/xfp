package xfp.java.test.accumulators;

import static java.lang.Double.toHexString;

import java.util.Arrays;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.ListSampler;
import org.junit.jupiter.api.Test;

import xfp.java.Classes;
import xfp.java.accumulators.Accumulator;
import xfp.java.accumulators.BigDecimalSum;
import xfp.java.accumulators.DoubleFmaSum;
import xfp.java.accumulators.DoubleSum;
import xfp.java.accumulators.MutableRationalSum;
import xfp.java.accumulators.RationalSum;
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
 * @version 2019-03-25
 */

//no actual tests here (yet)

public final class DotTest {

  //--------------------------------------------------------------
  //  /** See {@link Integer#numberOfLeadingZeros(int)}. */
  //  private static final int floorLog2 (final int k) {
  //    return Integer.SIZE - 1- Integer.numberOfLeadingZeros(k); }

  /** See {@link Integer#numberOfLeadingZeros(int)}. */
  private static final int ceilLog2 (final int k) {
    return Integer.SIZE - Integer.numberOfLeadingZeros(k-1); }

  // TODO: more efficient via bits?
  private static final boolean isEven (final int k) {
    return k == 2*(k/2); }

  /** Maximum exponent for double generation such that the sum 
   * of <code>dim</code> <code>double</code>s will be finite
   * (with high enough probability).
   */
  //  private static final int deMax (final int dim) { 
  //    final int d = Doubles.MAXIMUM_EXPONENT - ceilLog2(dim);
  //    System.out.println("emax=" + d);
  //    return d; }

  /** Maximum exponent for double generation such that a float sum 
   * of <code>dim</code> <code>double</code>s will be finite
   * (with high enough probability).
   */
  private static final int feMax (final int dim) { 
    final int d = Float.MAX_EXPONENT - ceilLog2(dim);
    //System.out.println("emax=" + d);
    return d; }

  // exact sum is 0.0
  private static double[] sampleDoubles (final Generator g,
                                         final UniformRandomProvider urp) {
    double[] x = (double[]) g.next();
    x = Dn.concatenate(x,Dn.minus(x));
    ListSampler.shuffle(urp,Arrays.asList(x));
    return x; }


  private static double[][] sampleDoubles (final int dim,
                                           final int n) {
    assert isEven(dim);
    final UniformRandomProvider urp = 
      PRNG.well44497b("seeds/Well44497b-2019-01-05.txt");
    final Generator g = 
      Doubles.finiteGenerator(dim/2,urp,feMax(dim));

    final double[][] x = new double[n][];
    for (int i=0;i<n;i++) { x[i] = sampleDoubles(g,urp); }
    return x; }

  private static final int DIM = 1 * 1024;
  private static final int N = 8;

  //--------------------------------------------------------------

  @SuppressWarnings({ "static-method" })
  @Test
  public final void dotTest () {

    final double[][] x0 = sampleDoubles(DIM,N);
    final double[][] x1 = sampleDoubles(DIM,N);

    // should be zero with current construction
    final double[] truth = new double[N];
    final double[] pred = new double[N];
    // assuming ERational is correct!!!
    for (int i=0;i<N;i++) { 
      truth[i] = 
        RationalSum.make().addProducts(x0[i],x1[i]).doubleValue(); }

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
     BigDecimalSum.make(),
     DoubleSum.make(),
     DoubleFmaSum.make(),
//     FloatSum.make(),
//     FloatFmaSum.make(),
     MutableRationalSum.make(),
     RationalSum.make(),
    };

    for (final Accumulator a : accumulators) {
      long t;
      t = System.nanoTime();
      for (int i=0;i<N;i++) { 
        pred[i] = 
          a.clear().addProducts(x0[i],x1[i]).doubleValue(); }
      t = (System.nanoTime()-t);
      System.out.println(toHexString(Dn.l1Dist(truth,pred)) + 
        " in " + (t*1.0e-9) 
        + " secs " + Classes.className(a)); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
