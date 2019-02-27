package xfp.java.test.numbers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;

import org.apache.commons.math3.fraction.BigFraction;
import org.junit.jupiter.api.Test;

import xfp.java.prng.Generator;
import xfp.java.prng.Generators;
import xfp.java.prng.PRNG;
import xfp.java.prng.Seeds;

//----------------------------------------------------------------
/** Test desired properties of BigFractions. 
 * <p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/numbers/BigFractionTest test > BigFractionTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-26
 */

public final class BigFractionTest {

  // TODO: BigFraction.doubleValue doesn't round to nearest. 
  // Test below fails with both random double -> BigFraction
  // and random long,long -> BigFraction.
  // Implement something that does.
  // Try the same test for Ratio, which converts the numerator
  // and denominator to BigDecimal, and divides them to get
  // another bigDecimal, which

  private static final int compare (final double x,
                                    final BigFraction f) {
    final BigFraction fx = new BigFraction(x);
    return fx.compareTo(f); }

  private static final double round (final double xlo,
                                     final BigFraction f,
                                     final double xhi) {
    assert 0 >= compare(xlo,f);
    assert 0 <= compare(xhi,f) :
      "\n" + xhi + 
      "\n->" + new BigFraction(xhi) + 
      "\n>=" + f;
    final BigFraction flo = new BigFraction(xlo);
    final BigFraction fhi = new BigFraction(xhi);
    final BigFraction dlo = f.subtract(flo);
    final BigFraction dhi = f.subtract(fhi);
    // TODO: rounding down, need round to even
    final int c = dlo.compareTo(dhi);
    if (0 > c) { return xlo; }
    else if (0 < c) { return xhi; }
    else { return xlo; }   }

  /** find a double interval that brackets f, assuming x &lt; f.
   */
  private static final double searchUp (final BigFraction f,
                                        final double x) {
    assert (0 > compare(x,f));
    double xlo = x;
    double xhi = Math.nextUp(xlo);
    while (0 > compare(xhi,f)) {
      xlo = xhi; xhi = Math.nextUp(xlo); }
    return round(xlo,f,xhi);  }

  /** find a double interval that brackets f, assuming x &lt; f.
   */
  private static final double searchDown (final BigFraction f,
                                          final double x) {
    assert (0 < compare(x,f));
    double xhi = x;
    double xlo = Math.nextDown(xhi);
    while (0 < compare(xlo,f)) {
      xhi = xlo; xlo = Math.nextDown(xhi); }
    return round(xlo,f,xhi);  }

  private static final double doubleValue (final BigFraction f) {
    //return f.doubleValue(); }
    //    return BigFractions.doubleValue(f); }
    final double x = f.doubleValue();
    final int c = compare(x,f);
    if (0 == c) { return x; }
    else if (0 > c) { return searchUp(f,x); }
    else { return searchDown(f,x); } }

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
    final BigFraction f0 = new BigFraction(x).reduce();
    final int r = f.compareTo(f0);
    final boolean result;
    if (r < 0) {
      final double d1 = Math.nextDown(x);
      final BigFraction f1 = new BigFraction(d1).reduce();
      result = f1.compareTo(f) < 0; }
    else if (r > 0) {
      final double d1 = Math.nextUp(x);
      final BigFraction f1 = new BigFraction(d1).reduce();
      result = f1.compareTo(f) > 0; } 
    else { result = true; }
    if (! result) { System.out.println(msg(f)); }
    return result; }

  //--------------------------------------------------------------

  private static final int TRYS = 1023;

  @SuppressWarnings({ "static-method" })
  @Test
  public final void roundingTest () {
    final BigFraction f0 = new BigFraction(13,11);
    assertTrue(correctRounding(f0), msg(f0));
    // TODO: BigFraction from longs generator
    // TODO: does this generator really cover all longs?
    final Generator g = 
      Generators.longGenerator(
        //Generators.doubleGenerator(
        PRNG.well44497b(
          Seeds.seed("seeds/Well44497b-2019-01-05.txt")));
    for (int i=0;i<TRYS;i++) {
      //      final double x = g.nextDouble();
      //      final BigFraction f = new BigFraction(x).reduce();
      // some longs will not be exactly representable as doubles
      final long n = g.nextLong();
      final long d = g.nextLong();
      final BigFraction f = new BigFraction(n,d).reduce();
      assertTrue(correctRounding(f),"\n" + i + msg(f)); } 
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
