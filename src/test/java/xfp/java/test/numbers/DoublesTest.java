package xfp.java.test.numbers;

import static java.lang.Double.MAX_EXPONENT;

//----------------------------------------------------------------
/** Test desired properties of doubles.
 * <p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/numbers/DoublesTest test > DoublesTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-01
 */

public final class DoublesTest {

  //--------------------------------------------------------------

  public static final int maxExponent (final int dim) {
    final int d =
      //Floats.MAXIMUM_BIASED_EXPONENT
      (MAX_EXPONENT
        - 31)
      + Integer.numberOfLeadingZeros(dim);
    System.out.println("delta=" + d);
    return d; }

  //--------------------------------------------------------------

  //  @SuppressWarnings({ "static-method" })
  //  @Test
  //  public final void finiteGeneratorTest () {
  //    final UniformRandomProvider urp =
  //      PRNG.well44497b("seeds/Well44497b-2019-01-05.txt");
  //    final Generator g = doubleGenerator(
  //      urp, SUBNORMAL_EXPONENT, MAX_EXPONENT);
  //    for (int i=0;i<2;i++) { g.nextDouble(); }
  //  }

  //--------------------------------------------------------------
  //  @SuppressWarnings({ "static-method" })
  //  @Test
  //  public final void roundingTest () {
  //
  //    final double dx = Doubles.MAX_INTEGER;
  //    final double dx1 = dx + 1.0;
  //    final long ldx = (long) dx;
  //    final long ldx1 = ldx + 1L;
  //    final double dldx1 = ldx1;
  //    final long ldldx1 = (long) dldx1;
  //    final double udx = Math.nextUp(dx);
  //    final long ludx = (long) udx;
  //
  //    System.out.println(dx + ", " + Double.toHexString(dx));
  //    System.out.println(ldx);
  //    System.out.println(dx1 + ", " + Double.toHexString(dx));
  //    System.out.println(ldx1);
  //    System.out.println(dldx1 + ", " + Double.toHexString(dx));
  //    System.out.println(ldldx1);
  //    System.out.println(udx + ", " + Double.toHexString(udx));
  //    System.out.println(ludx);
  //    }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
