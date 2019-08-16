package xfp.java.scripts;

import xfp.java.accumulators.Accumulator;
import xfp.java.accumulators.BigFloatAccumulator;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;

/** Benchmark sums.
 *
 * <pre>
 * jy --source 11 src/scripts/java/xfp/java/scripts/TotalL2Norm.java
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-08-16
 */
@SuppressWarnings("unchecked")
public final class TotalL2Norm {

  public static final void main (final String[] args) {
    final int dim = 524289;//(8*1024*1024) - 1;
    final int trys = 1 * 1024;
    final Generator g = Generators.make("finite",dim);
    final Accumulator a = BigFloatAccumulator.make();
    assert a.isExact();
    for (int i=0;i<trys;i++) {
      final double[] x = (double[]) g.next();
      final double z = a.clear().add2All(x).doubleValue();
      assert Double.isFinite(z); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
