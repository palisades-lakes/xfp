package xfp.java.scripts;

import org.apache.commons.rng.UniformRandomProvider;

import xfp.java.Debug;
import xfp.java.accumulators.Accumulator;
import xfp.java.accumulators.BigFloatAccumulator;
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
 * @version 2019-06-03
 */
@SuppressWarnings("unchecked")
public final class PartialSums {

  public static final void main (final String[] args) {
    Debug.DEBUG=false;
    final int n = (8*1024*1024) - 1;
    final int trys = 1 * 1024;
    final UniformRandomProvider urp =
      PRNG.well44497b("seeds/Well44497b-2019-01-09.txt");
    final int emax = Common.deMax(n)/2;
    //Debug.println("emax=" + emax);
    final Generator g = Doubles.finiteGenerator(n,urp,emax);
    final Accumulator a = BigFloatAccumulator.make();
    //final Accumulator a = RationalFloatAccumulator.make();
    //final Accumulator a = ZhuHayesAccumulator.make();
    //final Accumulator a = DistilledAccumulator.make();
    assert a.isExact();
    final long t = System.nanoTime();
    for (int i=0;i<trys;i++) {
      //Debug.println();
      //Debug.println(g.name());
      final double[] x = (double[]) g.next();
      final double[] z = a.partialSums(x);
      if (0.0 != z[z.length-1]) {
        Debug.println(Double.toHexString(0.0)
          + " != " + Double.toHexString(z[n-1])); } 
        }
    Debug.printf("total secs: %8.2f\n",
      (System.nanoTime()-t)*1.0e-9);
    }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
