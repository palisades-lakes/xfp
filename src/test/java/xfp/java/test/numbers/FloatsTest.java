package xfp.java.test.numbers;

import static java.lang.Float.MAX_EXPONENT;

//----------------------------------------------------------------
/** Test desired properties of doubles.
 * <p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/numbers/FloatsTest test > FloatsTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-01
 */

public final class FloatsTest {

  //--------------------------------------------------------------

  //  private static final double makeFloat (final int s,
  //                                         final int ue,
  //                                         final long t) {
  //    assert ((0 == s) || (1 ==s)) : "Invalid sign bit:" + s;
  //    System.out.println(ue);
  //    assert (SUBNORMAL_EXPONENT <= ue) && (ue <= MAX_EXPONENT) :
  //      "invalid (unbiased) exponent:" + toHexString(ue);
  //    final int e = ue + EXPONENT_BIAS;
  //    assert (0 <= e) :
  //      "Negative exponent:" + Integer.toHexString(e);
  //    assert (e <= MAXIMUM_BIASED_EXPONENT) :
  //      "Exponent too large:" + Integer.toHexString(e) +
  //      ">" + Integer.toHexString(MAXIMUM_BIASED_EXPONENT);
  //    assert (0 <= t) :
  //      "Negative significand:" + Long.toHexString(t);
  //    assert (t <= STORED_SIGNIFICAND_MASK) :
  //      "Significand too large:" + Long.toHexString(t) +
  //      ">" + Long.toHexString(STORED_SIGNIFICAND_MASK);
  //
  //    final long ss = ((long) s) << (EXPONENT_BITS + STORED_SIGNIFICAND_BITS);
  //    final long se = ((long) e) << STORED_SIGNIFICAND_BITS;
  //    assert (0L == (ss & se & t));
  //    final double x = longBitsToFloat(ss | se | t);
  //
  ////    System.out.println("-1^" + s + "*" + Long.toHexString(t)
  ////    + "*2^" + ue);
  ////    System.out.println(toHexString(x));
  ////    System.out.println(
  ////      Integer.toHexString(biasedExponent(x)));
  ////    System.out.println(
  ////      Integer.toHexString(unbiasedExponent(x)));
  ////    System.out.println();
  //
  //    return x; }


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
  //    for (int i=0;i<2;i++) { g.nextFloat(); }
  //  }

  //--------------------------------------------------------------
  //  @SuppressWarnings({ "static-method" })
  //  @Test
  //  public final void roundingTest () {
  //
  //    final double dx = Floats.MAX_INTEGER;
  //    final double dx1 = dx + 1.0;
  //    final long ldx = (long) dx;
  //    final long ldx1 = ldx + 1L;
  //    final double dldx1 = ldx1;
  //    final long ldldx1 = (long) dldx1;
  //    final double udx = Math.nextUp(dx);
  //    final long ludx = (long) udx;
  //
  //    System.out.println(dx + ", " + Float.toHexString(dx));
  //    System.out.println(ldx);
  //    System.out.println(dx1 + ", " + Float.toHexString(dx));
  //    System.out.println(ldx1);
  //    System.out.println(dldx1 + ", " + Float.toHexString(dx));
  //    System.out.println(ldldx1);
  //    System.out.println(udx + ", " + Float.toHexString(udx));
  //    System.out.println(ludx);
  //    }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
