package xfp.java.scripts;

import xfp.java.Debug;
import xfp.java.accumulators.Accumulator;
import xfp.java.accumulators.DistilledAccumulator;
import xfp.java.prng.Generator;
import xfp.java.test.Common;

/** Benchmark partial sums.
 * 
 * <pre>
 * jy --source 11 src/scripts/java/xfp/java/scripts/PartialSums.java
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-30
 */
@SuppressWarnings("unchecked")
public final class PartialSums {

  //--------------------------------------------------------------

  public static final void main (final String[] args) {
    Debug.DEBUG = true;
    final int n = (8*1024*1024) - 1;
    //final Accumulator a = BigFloatAccumulator.make();
    final Accumulator a = DistilledAccumulator.make();
    //assert a.isExact();
    final long t = System.nanoTime();
    for (final Generator g : Common.generators(n)) {
      Debug.println();
      Debug.println(g.name());
      final double[] x = (double[]) g.next();
      final double[] z = a.partialSums(x);
      if (0.0 != z[z.length-1]) {
        Debug.println(Double.toHexString(0.0) 
          + " != " + Double.toHexString(z[n-1])); } }
    Debug.printf("total secs: %8.2f\n",
      (System.nanoTime()-t)*1.0e-9); } 

//--------------------------------------------------------------
}
//--------------------------------------------------------------
