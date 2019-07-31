package xfp.java.scripts;

import org.apache.commons.rng.UniformRandomProvider;

import xfp.java.accumulators.Accumulator;
import xfp.java.accumulators.BigFloatAccumulator;
import xfp.java.accumulators.BigFloatAccumulator0;
import xfp.java.numbers.Doubles;
import xfp.java.prng.Generator;
import xfp.java.prng.PRNG;
import xfp.java.test.Common;

/** Benchmark partial sums.
 *
 * <pre>
 * jy --source 11 src/scripts/java/xfp/java/scripts/PartialSums.java
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-30
 */
@SuppressWarnings("unchecked")
public final class PartialSums {

  public static final void main (final String[] args) {
    final int n = (8*1024*1024) - 1;
    final int trys = 1 * 1024;
    final UniformRandomProvider urp =
      PRNG.well44497b("seeds/Well44497b-2019-01-09.txt");
    final int emax = Common.deMax(n)/2;
    final Generator g = Doubles.finiteGenerator(n,urp,emax);
    final Accumulator a = BigFloatAccumulator.make();
//    final double[] s = new double[n];
    assert a.isExact();
    for (int i=0;i<trys;i++) {
      final double[] x = (double[]) g.next();
//      final double[] s =a.partialSums(x,s);
      final double[] s =a.partialSums(x);
      assert ! Double.isNaN(s[n-1]); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
