package xfp.java.scripts;

import xfp.java.numbers.Rational;
import xfp.java.prng.Generator;
import xfp.java.test.Common;

/** Profile accumulators.
 *
 * <pre>
 * jy --source 12 src/scripts/java/xfp/java/scripts/RationalProfile.java
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-05-28
 */
@SuppressWarnings("unchecked")
public final class RationalProfile {

  public static final void main (final String[] args) {
    final int dim = (64*1024*1024) + 1;
    final int trys = 128;
    for (final Generator g : Common.generators(dim)) {
      System.out.println();
      System.out.println(g.name());
      final double[] x = (double[]) g.next();
      final long t = System.nanoTime();
      for (int i=0;i<trys;i++) {
        Rational a = Rational.getZero();
        for (final double xi : x) { a = a.add(xi); }
        final double z0 = a.doubleValue();
        if (0.0 != z0) {
          System.out.println(Double.toHexString(0.0)
            + " != " + Double.toHexString(z0)
            //+ "\n" + Double.toHexString(z2)
            ); }
      }
      System.out.printf("total secs: %8.2f\n",
        Double.valueOf((System.nanoTime()-t)*1.0e-9)); } }

  //--------------------------------------------------------------

  //  public static final void main (final String[] args) {
  //    final int dim = (64*1024*1024) + 1;
  //    final int trys = 128;
  //    final Accumulator a = RationalAccumulator.make();
  //    assert a.isExact();
  //    for (final Generator g : Common.generators(dim)) {
  //      System.out.println();
  //      System.out.println(g.name());
  //      final double[] x = (double[]) g.next();
  //      final long t = System.nanoTime();
  //      for (int i=0;i<trys;i++) {
  //        a.clear();
  //        a.addAll(x);
  //        final double z0 = a.doubleValue();
  //        //a.clear();
  //        //a.add2All(x);
  //        //final double z2 = a.doubleValue();
  //        if (0.0 != z0) {
  //          System.out.println(Double.toHexString(0.0)
  //            + " != " + Double.toHexString(z0)
  //            //+ "\n" + Double.toHexString(z2)
  //            ); }
  //        }
  //    System.out.printf("total secs: %8.2f\n",
  //      Double.valueOf((System.nanoTime()-t)*1.0e-9)); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
