package xfp.java.scripts;

import org.apache.commons.rng.UniformRandomProvider;

import xfp.java.Debug;
import xfp.java.accumulators.Accumulator;
import xfp.java.accumulators.BigFloatAccumulator;
import xfp.java.accumulators.BigFloatAccumulator0;
import xfp.java.numbers.Doubles;
import xfp.java.numbers.NaturalLE;
import xfp.java.prng.Generator;
import xfp.java.prng.PRNG;
import xfp.java.test.Common;

/** Benchmark sums.
 *
 * <pre>
 * jy --source 11 src/scripts/java/xfp/java/scripts/TotalSum.java
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-08-09
 */
@SuppressWarnings("unchecked")
public final class TotalSum {

  public static final void main (final String[] args) {
    final int n = (8*1024*1024) - 1;
    final int trys = 1;// * 8;//1024;
    final UniformRandomProvider urp =
      PRNG.well44497b("seeds/Well44497b-2019-01-09.txt");
    final int emax = Common.deMax(n)/2;
    final Generator g = Doubles.finiteGenerator(n,urp,emax);
    final Accumulator a = BigFloatAccumulator.make();
    assert a.isExact();
    Debug.DEBUG=false;
    for (int i=0;i<trys;i++) {
      final double[] x0 = (double[]) g.next();
      //final double[] x1 = (double[]) g.next();
      //final double s = a.addAll(x0).addAll(x1).doubleValue();
      final double s = a.addAll(x0).doubleValue();
      assert Double.isFinite(s);
//      final double d = a.addProducts(x0,x1).doubleValue();
//      assert Double.isFinite(d);
      }
    System.out.println(
      NaturalLE.zeroLo + "/" + NaturalLE.instances
      + " = " 
        + ((NaturalLE.zeroLo*100)/NaturalLE.instances) + "%");
   }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
