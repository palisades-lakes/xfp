package xfp.java.scripts;

import xfp.java.accumulators.Accumulator;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;

/** Profile partial sums.
 *
 * <pre>
 * jy --source 12 src/scripts/java/xfp/java/scripts/PartialSums.java
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-11
 */
@SuppressWarnings("unchecked")
public final class PartialSums {

  public static final void main (final String[] args) {
    final int dim = (1*1024*1024) - 1;
    final int trys = 8 * 1024;
    //final Generator g = Generators.make("exponential",dim);
    //final Generator g = Generators.make("finite",dim);
    //final Generator g = Generators.make("gaussian",dim);
    //final Generator g = Generators.make("laplace",dim);
    final Generator g = Generators.make("uniform",dim);
    final Accumulator a = 
      xfp.java.accumulators.RationalFloatAccumulator.make();
    //xfp.java.accumulators.BigFloatAccumulator.make();
    assert a.isExact();
    for (int i=0;i<trys;i++) {
      final double[] x = (double[]) g.next();
      final double[] s = a.partialSums(x);
      assert ! Double.isNaN(s[dim-1]); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
