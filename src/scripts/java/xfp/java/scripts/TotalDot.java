package xfp.java.scripts;

import xfp.java.accumulators.Accumulator;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;

/** Profile dot products.
 *
 * <pre>
 * jy --source 12 src/scripts/java/xfp/java/scripts/TotalDot.java
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-08-26
 */
@SuppressWarnings("unchecked")
public final class TotalDot {

  public static final void main (final String[] args) {
    final int dim = (2*1024*1024);
    final int trys = 8 * 1024;
    final Generator g = Generators.make("exponential",dim);
    final Accumulator a =
      xfp.java.accumulators.BigFloatAccumulator.make();
    assert a.isExact();
    //Debug.DEBUG = true;
    for (int i=0;i<trys;i++) {
      final double[] x0 = (double[]) g.next();
      final double[] x1 = (double[]) g.next();
      final double z = a.clear().addProducts(x0,x1).doubleValue();
      assert Double.isFinite(z); } 
    //    System.out.println("SWAPS/CALLS=" 
    //      + ((double) BigFloat.SWAPS)/BigFloat.CALLS);
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
