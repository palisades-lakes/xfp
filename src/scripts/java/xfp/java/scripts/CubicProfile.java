package xfp.java.scripts;

import xfp.java.polynomial.BigFloatCubic;
import xfp.java.polynomial.Polynomial;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;

/** Benchmark exact cubic polynomials.
 *
 * <pre>
 * jy --source 12 src/scripts/java/xfp/java/scripts/CubicProfile.java
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-06
 */
@SuppressWarnings("unchecked")
public final class CubicProfile {

  public static final void main (final String[] args) {
    final int dim = 1*1024*1024 - 1;
    final int trys = 8 * 1024;
    //final Generator g = Generators.make("exponential",dim);
    //final Generator g = Generators.make("finite",dim);
    final Generator g = Generators.make("gaussian",dim);
    //final Generator g = Generators.make("laplace",dim);
    //final Generator g = Generators.make("uniform",dim);
    for (int i=0;i<trys;i++) {
      final double a0 = g.nextDouble();
      final double a1 = g.nextDouble();
      final double a2 = g.nextDouble();
      final double a3 = g.nextDouble();
      final double[] x = (double[]) g.next();
      final Polynomial q = BigFloatCubic.make(a0,a1,a2,a3);
      final double[] z = q.doubleValue(x);
      for (int j=0;j<dim;j++) {
      if (!Double.isFinite(z[j])) {
        System.out.println(
          i + "," + j + "," + Double.toHexString(z[j])); } } } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
