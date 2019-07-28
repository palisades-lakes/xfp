package xfp.java.scripts;

import org.apache.commons.rng.UniformRandomProvider;

import xfp.java.accumulators.Accumulator;
import xfp.java.accumulators.BigFloatAccumulator;
import xfp.java.numbers.Doubles;
import xfp.java.prng.Generator;
import xfp.java.prng.PRNG;
import xfp.java.test.Common;

/** Benchmark partial l2 norms
 *
 * <pre>
 * jy --source 11 src/scripts/java/xfp/java/scripts/PartialL1s.java
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-28
 */
@SuppressWarnings("unchecked")
public final class PartialL1s {

  public static final void main (final String[] args) {
    //Debug.DEBUG=false;
    final int n = (8*1024*1024) - 1;
    final int trys = 1 * 1024;
    final UniformRandomProvider urp =
      PRNG.well44497b("seeds/Well44497b-2019-01-09.txt");
    final int emax = Common.deMax(n)/2;
    final Generator g = Doubles.finiteGenerator(n,urp,emax);
    final Accumulator a = BigFloatAccumulator.make();
    assert a.isExact();
    for (int i=0;i<trys;i++) {
      //Debug.println();
      //Debug.println(g.name());
      final double[] x = (double[]) g.next();
      final double[] z = a.partialL1s(x); 
      assert ! Double.isNaN(z[n-1]);} }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
