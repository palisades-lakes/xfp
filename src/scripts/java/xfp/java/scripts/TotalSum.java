package xfp.java.scripts;

import xfp.java.accumulators.Accumulator;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;

/** Benchmark sums.
 *
 * <pre>
 * jy --source 12 src/scripts/java/xfp/java/scripts/TotalSum.java
 * j --source 12 src/scripts/java/xfp/java/scripts/TotalSum.java
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-08-27
 */
@SuppressWarnings("unchecked")
public final class TotalSum {

  public static final void main (final String[] args) {
    final int dim = 2*1024*1024;
    final int trys = 8 * 1024;
    //final Generator g = Generators.make("exponential",dim);
    final Generator g = Generators.make("finite",dim);
    //final Generator g = Generators.make("gaussian",dim);
    //final Generator g = Generators.make("laplace",dim);
    //final Generator g = Generators.make("uniform",dim);
    final Accumulator a0 = 
      xfp.java.accumulators.BigFloatAccumulator0.make();
    final Accumulator a1 = 
      xfp.java.accumulators.BigFloatAccumulator.make();
    assert a0.isExact();
    assert a1.isExact();
    //Debug.DEBUG=true;
    for (int i=0;i<trys;i++) {
      final double[] x0 = (double[]) g.next();
      final double s1 = a1.clear().addAll(x0).doubleValue();
      assert Double.isFinite(s1);
      final double s0 = a0.clear().addAll(x0).doubleValue();
      assert Double.isFinite(s0);
      }
   }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
