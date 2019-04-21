package xfp.java.test.numbers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import xfp.java.numbers.Doubles;
import xfp.java.numbers.RationalFloat;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;
import xfp.java.prng.PRNG;

//----------------------------------------------------------------
/** Test desired properties of RationalFloat. 
 * <p>
 * <pre>
 * mvn -Dtest=xfp/java/test/numbers/RationalFloatTest test > RationalFloatTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-30
 */

public final class RationalFloatTest {

  //--------------------------------------------------------------

  private static final boolean correctRounding (final RationalFloat f) {
    // TODO: this is necessary but not sufficient to ensure 
    // rounding was correct?
    final double x = f.doubleValue();
    // not really true, but can't check easily
    // f.numerator().divide(f.denominator()).doubleValue?
    if (! Double.isFinite(x)) { 
      final BigInteger q = f.numerator().divide(f.denominator()).shiftLeft(f.exponent());
      return ! Double.isFinite(q.doubleValue()); }
    final RationalFloat fx = RationalFloat.valueOf(x);
    final int r = f.compareTo(fx);
    final boolean result;
    if (r < 0) { // fx > f
      final double x1o = Math.nextDown(x);
      final RationalFloat flo = RationalFloat.valueOf(x1o);
      result = flo.compareTo(f) < 0;}
    else if (r > 0) { // fx < f
      final double xhi = Math.nextUp(x);
      final RationalFloat fhi = RationalFloat.valueOf(xhi);
      result = f.compareTo(fhi) < 0; } 
    else { result = true; }
    return result; }

  //--------------------------------------------------------------

  private static final int TRYS = 1 * 1024;

  @SuppressWarnings({ "static-method" })
  @Test
  public final void roundingTest () {
    final RationalFloat f = 
      RationalFloat.valueOf(13,11,0);
    assertTrue(correctRounding(f)); }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void longRoundingTest () {
    final RationalFloat f = 
      RationalFloat.valueOf(
        BigInteger.valueOf(0x789f09858446ad92L),
        BigInteger.valueOf(0x19513ea5d70c32eL),
        0);
    assertTrue(correctRounding(f)); }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void fromBigIntegersRoundingTest () {
    final Generator gn = 
      Generators.bigIntegerGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    final Generator gd = 
      Generators.positiveBigIntegerGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-07.txt"));
    for (int i=0;i<TRYS;i++) {
      final BigInteger n = (BigInteger) gn.next();
      final BigInteger d = (BigInteger) gd.next();
      assertEquals(d.signum(),1);
      final RationalFloat f = 
        RationalFloat.valueOf(n,d,0);
      assertTrue(correctRounding(f),
        () -> 
      "\nn= " + n.toString(0x10) 
      + "\nd= " + d.toString(0x10) 
      + "\n\nf= " + f.toString()
      + "\n\nxf= " + Double.toHexString(f.doubleValue())); } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void fromLongsRoundingTest () {
    final Generator g0 = 
      Generators.longGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    final Generator g1 = 
      Generators.positiveLongGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-07.txt"));
    for (int i=0;i<TRYS;i++) {
      final long n = g0.nextLong();
      final long d = g1.nextLong();
      final RationalFloat f = RationalFloat.valueOf(
        BigInteger.valueOf(n),
        BigInteger.valueOf(d),
        0);
      assertTrue(correctRounding(f)); } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void uniformDoubleRoundingTest () {
    final Generator g = 
      Doubles.uniformGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"),
        -Double.MAX_VALUE, Double.MAX_VALUE);
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final RationalFloat f = RationalFloat.valueOf(x);
      assertTrue(correctRounding(f),
        () -> 
      "\n" + Double.toHexString(x) 
      + "\n" + f.toString()
      + "\n" + Double.toHexString(f.doubleValue())); } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void gaussianDoubleRoundingTest () {
    final Generator g = 
      Doubles.gaussianGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"),
        0.0, Double.MAX_VALUE/1000.0);
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final RationalFloat f = RationalFloat.valueOf(x);
      assertTrue(correctRounding(f),
        () -> 
      "\n" + Double.toHexString(x) 
      + "\n" + f.toString()
      + "\n" + Double.toHexString(f.doubleValue())); } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void finiteDoubleRoundingTest () {
    final Generator g = 
      Doubles.finiteGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final RationalFloat f = RationalFloat.valueOf(x);
      assertTrue(correctRounding(f),
        () -> 
      "\n" + Double.toHexString(x) 
      + "\n" + f.toString()
      + "\n" + Double.toHexString(f.doubleValue())); } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void subnormalDoubleRoundingTest () {
    final Generator g = 
      Doubles.subnormalGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final RationalFloat f = RationalFloat.valueOf(x);
      assertTrue(correctRounding(f)); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------