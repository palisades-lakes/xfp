package xfp.java.test.numbers;

import static java.lang.Double.toHexString;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import xfp.java.numbers.BigFractions;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;
import xfp.java.prng.PRNG;
import xfp.java.prng.Seeds;

//----------------------------------------------------------------
/** Test desired properties of BigFractions. 
 * <p>
 * <pre>
 * mvn -Dtest=xfp/java/test/numbers/BigFractionTest test > BigFractionTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-28
 */

public final class BigFractionTest {

  // TODO: BigFraction.doubleValue doesn't round to nearest. 
  // Test below fails with both random double -> BigFraction
  // and random long,long -> BigFraction.
  // Implement something that does.
  // Try the same test for Ratio, which converts the numerator
  // and denominator to BigDecimal, and divides them to get
  // another bigDecimal, which

  private static final void debugPrintln (final Object s) {
//    System.out.println(s.toString()); 
  }

  private static final void debugPrintln (final int s) {
//    System.out.println(s); 
  }

  private static final void debugPrintln (final double s) {
//    System.out.println(s); 
  }

  private static final void debugPrintln (final boolean s) {
//    System.out.println(s); 
  }

  private static final void debugPrintln () {
//    System.out.println(); 
  }

  //--------------------------------------------------------------
  // alternate starting points for doubleValue
  //--------------------------------------------------------------
  // default for BigFraction
  //--------------------------------------------------------------
  
  //  private static final double toDouble (final BigFraction f) {
  //    return f.doubleValue(); }

  //--------------------------------------------------------------
  // after BigFraction.doubleValue()
  //--------------------------------------------------------------

  //strictfp
  public static final double toDouble (final BigFraction f) {
    BigInteger fn = f.getNumerator();
    BigInteger fd = f.getDenominator();
    double xn = fn.doubleValue();
    double xd = fd.doubleValue();
    double result = xn / xd;
    debugPrintln(xn + " / " + xd);
    debugPrintln(toHexString(xn) + " / " + toHexString(xd));
    debugPrintln(result + " : " + toHexString(result));
    if (Double.isInfinite(xd)) {
      // Denominator must be out of range:
      final BigInteger[] qr = fd.divideAndRemainder(fn);
      final double xq = qr[0].doubleValue();
      final double xr = qr[1].doubleValue();
      xn = 1.0;
      xd =  xq + xr; 
      if (Double.isFinite(xd)) { result = xn / xd; }
      else { result = Double.NaN; }
      debugPrintln("xn/infinity");
      debugPrintln(xq + " + " + xr);
      debugPrintln(toHexString(xq) + " + " + toHexString(xr));
      debugPrintln(xn + " / " + xd);
      debugPrintln(toHexString(xn) + " / " + toHexString(xd));
      debugPrintln(result + " : " + toHexString(result)); }
    if (Double.isNaN(result)) {
      // Numerator and/or denominator must be out of range:
      // Calculate how far to shift them to put them in range.
      final int shift = 
        FastMath.max(fn.bitLength(),fd.bitLength()) 
        - 
        FastMath.getExponent(Double.MAX_VALUE);
      fn = fn.shiftRight(shift);
      fd = fd.shiftRight(shift);
      xn = fn.doubleValue();
      xd = fd.doubleValue();
      result = xn / xd;
      debugPrintln("NaN");
      debugPrintln(xn + " / " + xd);
      debugPrintln(toHexString(xn) + " / " + toHexString(xd));
      debugPrintln(result + " : " + toHexString(result)); }
    return result;
  }
  //--------------------------------------------------------------
  // adapted from clojure.lang.Ratio
  //--------------------------------------------------------------

  public static final BigDecimal 
  toBigDecimal (final BigFraction f) {
    final BigDecimal fn = new BigDecimal(f.getNumerator());
    final BigDecimal fd = new BigDecimal(f.getDenominator());
    return fn.divide(fd, MathContext.DECIMAL64); }

  //  public static final double toDouble (final BigFraction f) {
  //    return toBigDecimal(f).doubleValue(); }

  //--------------------------------------------------------------

  private static final int compare (final BigFraction f0,
                                    final BigFraction f1) {

    final BigInteger n0 = f0.getNumerator();
    final BigInteger d0 = f0.getDenominator();
    final BigInteger n1 = f1.getNumerator();
    final BigInteger d1 = f1.getDenominator();

    final int s0 = n0.signum();
    final int s1 = n1.signum();
    if (s0 != s1) { return (s0 > s1) ? 1 : -1; }
    if (s0 == 0) { return 0; }

    // something wrong here
    //    final int cn = n0.compareTo(n1);
    //    final int cd = d0.compareTo(d1);
    //    if ((0 == cn) && (0 == cd)) { return 0; }
    //    if ((0 > cn) && (0 < cd)) { return -1; }
    //    if ((0 < cn) && (0 > cd)) { return 1; }

    final BigInteger nOd = n0.multiply(d1);
    final BigInteger dOn = d0.multiply(n1);
    return nOd.compareTo(dOn); }

  //  private static final int compare (final double x,
  //                                    final BigFraction f) {
  //    final BigFraction fx = new BigFraction(x);
  //    return compare(fx,f); }

  private static final double round (final double xlo,
                                     final BigFraction flo,
                                     final BigFraction f,
                                     final double xhi,
                                     final BigFraction fhi) {
    assert 0 >= compare(flo,f);
    assert 0 <= compare(fhi,f) :
      "\n" + xhi + 
      "\n->" + new BigFraction(xhi) + 
      "\n>=" + f;
    final BigFraction dlo = f.subtract(flo).abs();
    final BigFraction dhi = f.subtract(fhi).abs();
    final int c = compare(dlo,dhi);
    if (c < 0) { return xlo; }
    if (c > 0) { return xhi; }
    // TODO: rounding down, need round to even: choose the one 
    // with zero in the least significant significand bit
    return xlo; }

  private static final String bracketString (final double xlo,
                                             final double xhi) {
    return "\n[" + xlo + ", " + xhi 
      + "]\n[" 
      + toHexString(xlo) + ", " 
      + toHexString(xhi) + "]"; }

  /** find a double interval that brackets f, assuming x &lt; f.
   */

  private static final double searchUp (final BigFraction f,
                                        final double x,
                                        final BigFraction fx) {
    double xlo = x;
    BigFraction flo = fx;
    assert (compare(flo,f) < 0);
    double xhi = Math.nextUp(xlo);
    BigFraction fhi = new BigFraction(xhi);
    int iterations = 0;
    while ((iterations < 16) && (compare(fhi,f) < 0))  {
      xlo = xhi; 
      flo = fhi;
      xhi = Math.nextUp(xlo); 
      fhi = new BigFraction(xhi); 
      assert compare(flo,fhi) < 0;
      assert compare(flo,f) < 0;
      iterations++;
      debugPrintln("searchUp:" + iterations + 
        bracketString(xlo,xhi)); 
    }
    assert compare(flo,f) < 0;
    assert compare(f,fhi) <= 0;
    debugPrintln("searchUp done:" + iterations + 
      bracketString(xlo,xhi)); 
    return round(xlo,new BigFraction(xlo),f,xhi,fhi);  }

  /** find a double interval that brackets f, assuming x &lt; f.
   */

  private static final double searchDown (final BigFraction f,
                                          final double x,
                                          final BigFraction fx) {
    //assert (0 < compare(x,f));
    double xhi = x;
    BigFraction fhi = fx;
    assert (compare(f,fhi) < 0);
    double xlo = Math.nextDown(xhi);
    BigFraction flo = new BigFraction(xlo);
    int iterations = 0;
    while ((iterations < 16) && (compare(f,flo) < 0)) {
      xhi = xlo; 
      fhi = flo;
      xlo = Math.nextDown(xhi); 
      flo = new BigFraction(xlo); 
      assert compare(flo,fhi) < 0;
      assert compare(f,fhi) < 0;
      iterations++;
      //      debugPrintln("searchDown:" + iterations + 
      //        bracketString(xlo,xhi)); 
    }
    debugPrintln("searchDown done:" + iterations + 
      bracketString(xlo,xhi)); 
    assert compare(flo,f) <= 0;
    assert compare(f,fhi) < 0;
    return round(xlo,flo,f,xhi,new BigFraction(xhi));  }

  private static final double doubleValue (final BigFraction f) {
    final double x = toDouble(f);
    final BigFraction fx = new BigFraction(x);
    final int c = compare(fx,f);
    if (c > 0) { return searchDown(f,x,fx); }
    if (c < 0) { return searchUp(f,x,fx); }
    return x; }

  //--------------------------------------------------------------
  /** print the values with a common denominator,
   * subtracting 'central' value to make it easier to check by
   * eye.
   */

  private static final String msg (final BigFraction f) {
    final BigInteger fn = f.getNumerator();
    final BigInteger fd = f.getDenominator();
    final double x = doubleValue(f);
    final double xlo = Math.nextDown(x);
    final double xhi = Math.nextUp(x);
    final BigFraction flo = new BigFraction(xlo).reduce();
    final BigInteger flon = flo.getNumerator();
    final BigInteger flod = flo.getDenominator();
    final BigFraction fx = new BigFraction(x).reduce();
    final BigInteger fxn = fx.getNumerator();
    final BigInteger fxd = fx.getDenominator();
    final BigFraction fhi = new BigFraction(xhi).reduce();
    final BigInteger fhin = fhi.getNumerator();
    final BigInteger fhid = fhi.getDenominator();

    final BigInteger fni = fn.multiply(flod).multiply(fxd).multiply(fhid);
    final BigInteger floni = 
      flon.multiply(fd).multiply(fxd).multiply(fhid).subtract(fni);
    final BigInteger fxni = 
      fxn.multiply(fd).multiply(flod).multiply(fhid).subtract(fni);
    final BigInteger fhini =
      fhin.multiply(fd).multiply(fxd).multiply(flod).subtract(fni);
    return 
      "\n" + f + " incorrectly rounded -> " + x + " -> " +
      "\n" + xlo + " -> " + flo + " -> " + 
      "\n" +  floni +
      "\n" + x + " -> " + fx + " -> " + 
      "\n" +  fxni +
      "\n" + xhi + " -> " + fhi + " -> " + 
      "\n" + fhini; }

  //--------------------------------------------------------------

  private static final boolean 
  correctRounding (final BigFraction f) {
    // TODO: this is necessary but not sufficient to ensure 
    // rounding was correct?
    final double x = doubleValue(f);
    final BigFraction fx = new BigFraction(x).reduce();
    final int r = compare(f,fx);
    //    debugPrintln();
    //    debugPrintln("correctRounding");
    //    debugPrintln(x + " : " + toHexString(x));
    //    debugPrintln(r);
    final boolean result;
    if (r < 0) { // fx > f
      final double x1o = Math.nextDown(x);
      final BigFraction flo = new BigFraction(x1o).reduce();
      result = compare(flo,f) < 0;}
    else if (r > 0) { // fx < f
      final double xhi = Math.nextUp(x);
      final BigFraction fhi = new BigFraction(xhi).reduce();
      result = compare(f,fhi) < 0; } 
    else { result = true; }
    //    debugPrintln(result); 
    return result; }

  //--------------------------------------------------------------

  private static final int TRYS = 128 * 1024;

      @SuppressWarnings({ "static-method" })
      @Test
      public final void roundingTest () {
        final BigFraction f = new BigFraction(13,11);
        debugPrintln(f);
        assertTrue(correctRounding(f), 
          () -> { return msg(f); }); }
  
      @SuppressWarnings({ "static-method" })
      @Test
      public final void fromLongsRoundingTest () {
        debugPrintln("longs");
        // TODO: BigFraction from longs generator
        // TODO: does this generator really cover all longs?
        final Generator g = 
          Generators.longGenerator(
            PRNG.well44497b(
              Seeds.seed("seeds/Well44497b-2019-01-05.txt")));
        for (int i=0;i<TRYS;i++) {
          // some longs will not be exactly representable as doubles
          final long n = g.nextLong();
          final long d = g.nextLong();
          final BigFraction f = new BigFraction(n,d).reduce();
          assertTrue(correctRounding(f), 
            () -> { return msg(f); }); } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void fromNormalDoubleRoundingTest () {
    final Generator g = 
      Generators.normalDoubleGenerator(
        PRNG.well44497b(
          Seeds.seed("seeds/Well44497b-2019-01-05.txt")));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      debugPrintln();
      debugPrintln("fromNormalDoubleRoundingTest:" + i);
      debugPrintln(x + " : " + toHexString(x));
      final BigFraction f = new BigFraction(x).reduce();
      debugPrintln(f);
      final double xf = BigFractions.doubleValue(f);
      debugPrintln(xf + " : " + toHexString(xf));
      final double xf0 = BigFractions.doubleValue(f);
      debugPrintln("Ratio: " + xf0 + " : " + toHexString(xf0));
      final double xf1 = f.doubleValue();
      debugPrintln("BigFraction: " + xf1 + " : " + toHexString(xf1));
      assertTrue(correctRounding(f), 
        () -> { return msg(f); }); } }

//    @SuppressWarnings({ "static-method" })
//    @Test
//    public final void fromSubnormalDoubleRoundingTest () {
//      final Generator g = 
//        Generators.subnormalDoubleGenerator(
//          PRNG.well44497b(
//            Seeds.seed("seeds/Well44497b-2019-01-05.txt")));
//      for (int i=0;i<TRYS;i++) {
//        final double x = g.nextDouble();
//        debugPrintln();
//        debugPrintln("fromSubnormalDoubleRoundingTest:" + i);
//        debugPrintln(x + " : " + toHexString(x));
//        final BigFraction f = new BigFraction(x).reduce();
//        debugPrintln(f);
//        final double xf = BigFractions.doubleValue(f);
//        debugPrintln(xf + " : " + toHexString(xf));
//        final double xf0 = BigFractions.doubleValue(f);
//        debugPrintln("Ratio: " + xf0 + " : " + toHexString(xf0));
//        final double xf1 = f.doubleValue();
//        debugPrintln("BigFraction: " + xf1 + " : " + toHexString(xf1));
//        assertTrue(correctRounding(f), 
//          () -> { return msg(f); }); } }

  //  @SuppressWarnings({ "static-method" })
  //  @Test
  //  public final void fromDoubleRoundingTest () {
  //    final Generator g = 
  //      Generators.finiteDoubleGenerator(
  //        PRNG.well44497b(
  //          Seeds.seed("seeds/Well44497b-2019-01-05.txt")));
  //    for (int i=0;i<TRYS;i++) {
  //      final double x = g.nextDouble();
  //      debugPrintln();
  //      debugPrintln("fromDoubleRoundingTest:" + i);
  //      debugPrintln(x + " : " + toHexString(x));
  //      final BigFraction f = new BigFraction(x).reduce();
  //      debugPrintln(f);
  //      final double xf = BigFractions.doubleValue(f);
  //      debugPrintln(xf + " : " + toHexString(xf));
  //      final double xf0 = BigFractions.doubleValue(f);
  //      debugPrintln("Ratio: " + xf0 + " : " + toHexString(xf0));
  //      final double xf1 = f.doubleValue();
  //      debugPrintln("BigFraction: " + xf1 + " : " + toHexString(xf1));
  //      assertTrue(correctRounding(f), 
  //        () -> { return msg(f); }); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
