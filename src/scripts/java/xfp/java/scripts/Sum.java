package xfp.java.scripts;

import java.util.Arrays;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.ListSampler;

import xfp.java.accumulators.Accumulator;
import xfp.java.accumulators.RationalFloatAccumulator;
import xfp.java.linear.Dn;
import xfp.java.numbers.Doubles;
import xfp.java.prng.Generator;
import xfp.java.prng.PRNG;
import xfp.java.test.Common;

// java -ea --illegal-access=warn -jar target/benchmarks.jar

/** Benchmark algebraic structure tests.
 *
 * <pre>
 * jy --source 11 src/scripts/java/xfp/java/scripts/Sum.java
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-14
 */
@SuppressWarnings("unchecked")
public final class Sum {

  //--------------------------------------------------------------
  //  /** See {@link Integer#numberOfLeadingZeros(int)}. */
  //  private static final int floorLog2 (final int k) {
  //    return Integer.SIZE - 1- Integer.numberOfLeadingZeros(k); }

  // TODO: more efficient via bits?
  private static final boolean isEven (final int k) {
    return k == (2*(k/2)); }

  private static double[] sampleDoubles (final Generator g,
                                         final UniformRandomProvider urp) {
    double[] x = (double[]) g.next();
    // exact sum is 0.0
    x = Dn.concatenate(x,Dn.minus(x));
    ListSampler.shuffle(urp,Arrays.asList(x));
    return x; }

  private static double[][] sampleDoubles (final int dim,
                                           final int n) {
    assert isEven(dim);
    final UniformRandomProvider urp =
      PRNG.well44497b("seeds/Well44497b-2019-01-05.txt");
    final Generator g =
      Doubles.finiteGenerator(dim/2,urp,Common.deMax(dim));

    final double[][] x = new double[n][];
    for (int i=0;i<n;i++) { x[i] = sampleDoubles(g,urp); }
    return x; }

  private static final int DIM = 1024*1024;

  private static final int TRYS = 32;

  public static final void main (final String[] args)
    throws InterruptedException {
    final double[] x0 = sampleDoubles(DIM,1)[0];

    final Accumulator a = RationalFloatAccumulator.make();
    Thread.sleep(16*1024);
    final long t = System.nanoTime();
    for (int i=0;i<TRYS;i++) {
      a.clear();
      a.addAll(x0);
      if ((2.0*Math.ulp(1.0)) > a.doubleValue()) {
        System.out.println("false"); } }
    System.out.printf("total secs: %8.2f\n",
      Double.valueOf((System.nanoTime()-t)*1.0e-9));
    Thread.sleep(16*1024); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
