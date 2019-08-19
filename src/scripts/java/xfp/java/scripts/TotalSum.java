package xfp.java.scripts;

import xfp.java.Debug;
import xfp.java.accumulators.Accumulator;
import xfp.java.accumulators.BigFloatAccumulator;
import xfp.java.accumulators.BigFloatAccumulator0;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;

/** Benchmark sums.
 *
 * <pre>
 * jy --source 11 src/scripts/java/xfp/java/scripts/TotalSum.java
 * j --source 11 src/scripts/java/xfp/java/scripts/TotalSum.java
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-08-18
 */
@SuppressWarnings("unchecked")
public final class TotalSum {

  public static final void main (final String[] args) {
    final int dim = 2*1024*1024;
    final int trys = 8 * 1024;
    //final Generator g = Generators.make("exponential",dim);
    //final Generator g = Generators.make("finite",dim);
    final Generator g = Generators.make("gaussian",dim);
    //final Generator g = Generators.make("laplace",dim);
    //final Generator g = Generators.make("uniform",dim);
    final Accumulator a = BigFloatAccumulator.make();
    assert a.isExact();
    Debug.DEBUG=true;
    for (int i=0;i<trys;i++) {
      final double[] x0 = (double[]) g.next();
      final double s = a.clear().addAll(x0).doubleValue();
      assert Double.isFinite(s);
      }
//    System.out.println(
//      NaturalLE.zeroLo + "/" + NaturalLE.instances
//      + " = " 
//        + ((NaturalLE.zeroLo*100)/NaturalLE.instances) + "%");
//    System.out.println(
//      NaturalLE.maxHi + "/" + NaturalLE.instances
//      + " = " 
//        + ((NaturalLE.maxHi*100)/NaturalLE.instances) + "%");
   }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
