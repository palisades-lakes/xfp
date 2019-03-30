package xfp.java.test.accumulators;

import static java.lang.Double.toHexString;

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
 * mvn -q -Dtest=xfp/java/test/numbers/SumTest test > SumTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-30
 */

// no actual tests here (yet)

public final class SumTest {

  //--------------------------------------------------------------
  //  /** See {@link Integer#numberOfLeadingZeros(int)}. */
  //  private static final int floorLog2 (final int k) {
  //    return Integer.SIZE - 1- Integer.numberOfLeadingZeros(k); }

  /** See {@link Integer#numberOfLeadingZeros(int)}. */
  private static final int ceilLog2 (final int k) {
    return Integer.SIZE - Integer.numberOfLeadingZeros(k-1); }

    /** Maximum exponent for double generation such that a float sum 
   * of <code>dim</code> <code>double</code>s will be finite
   * (with high enough probability).
   */
  private static final int eMax (final int dim) { 
    final int d = Double.MAX_EXPONENT - ceilLog2(dim);
    return d; }

  /** Maximum exponent for double generation such that a float sum 
   * of <code>dim</code> <code>double</code>s will be finite
   * (with high enough probability).
   */
  private static final double dMax (final int dim) { 
    return 1024*(1 << eMax(dim)); }

  private static final int DIM = 2 * 1024 * 1024;
  private static final int N = 2;

  private static final void sumTest (final Generator g) {

    final double[][] x = (double[][]) g.next();

    final double[] truth = new double[N];
    final double[] pred = new double[N];
    for (int i=0;i<N;i++) { 
      truth[i] =  RBFAccumulator.make().addAll(x[i]).doubleValue(); }

    System.out.println();
    System.out.println(Classes.className(g));
//    for (int i=0;i<N;i++) { 
//      System.out.println(
//        i + " : " 
//          + Double.toHexString(truth[i]) 
//          + ", " 
//          + Double.toHexString(Dn.maxAbs(x[i]))); }

    System.out.println();
    final Accumulator[] accumulators = 
    {
     DoubleAccumulator.make(),
     DoubleFmaAccumulator.make(),
     // FloatAccumulator.make(),
     // RationalAccumulator.make(),
     RBFAccumulator.make(),
    };
    for (final Accumulator a : accumulators) {
      long t;
      t = System.nanoTime();
      for (int i=0;i<N;i++) { 
        pred[i] = a.clear().addAll(x[i]).doubleValue(); }
      t = (System.nanoTime()-t);
      final double l1d = Dn.l1Dist(truth,pred);
      final double l1n = Math.max(1.0,Dn.l1Norm(truth));
      System.out.println(toHexString(l1d) 
        + " / " + toHexString(l1n) + " = " + (l1d/l1n) 
        + " in " + (t*1.0e-9) 
        + " secs " + Classes.className(a)); } }

  @Test
  public final void sumTests () {

    System.out.println();
    System.out.println(Classes.className(this));
    final UniformRandomProvider urp = 
      PRNG.well44497b("seeds/Well44497b-2019-01-05.txt");
    final Generator[] gs = 
    { Doubles.finiteGenerator(N,DIM,urp,eMax(DIM)+1),
      Doubles.gaussianGenerator(N,DIM,urp,0.0,dMax(DIM)),
      Doubles.laplaceGenerator(N,DIM,urp,0.0,dMax(DIM)),
      Doubles.uniformGenerator(N,DIM,urp,-dMax(DIM),dMax(DIM)),
    };

    for (final Generator g : gs) { sumTest(g); } }


  //--------------------------------------------------------------
}
//--------------------------------------------------------------
