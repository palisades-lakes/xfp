package xfp.java.scripts;

import xfp.java.accumulators.BigFloatAccumulator0;
import xfp.java.accumulators.DistilledAccumulator;
import xfp.java.prng.Generator;
import xfp.java.test.Common;

/** Profile accumulators.
 *
 * <pre>
 * jy --source 11 src/scripts/java/xfp/java/scripts/DistilledProfile.java
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-06-04
 */
@SuppressWarnings("unchecked")
public final class DistilledProfile {

  public static final void main (final String[] args) {
    final int dim = (64*1024*1024) + 1;
    final int trys = 128;
    for (final Generator g : Common.generators(dim)) {
      System.out.println();
      System.out.println(g.name());
      final double[] x = (double[]) g.next();
      final BigFloatAccumulator0 b =
        BigFloatAccumulator0.make().add2All(x);
      final double z1 = b.doubleValue();
      final long t = System.nanoTime();
      for (int i=0;i<trys;i++) {
        DistilledAccumulator a = DistilledAccumulator.make();
        for (final double xi : x) { a = a.add2(xi); }
        final double z0 = a.doubleValue();
        if (z1 != z0) {
          System.out.println(Double.toHexString(z1)
            + " != " + Double.toHexString(z0)
            ); }
      }
      System.out.printf("total secs: %8.2f\n",
        Double.valueOf((System.nanoTime()-t)*1.0e-9)); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
