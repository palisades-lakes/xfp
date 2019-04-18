package xfp.java.test.numbers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import xfp.java.numbers.BigFloat;
import xfp.java.numbers.Doubles;
import xfp.java.prng.Generator;
import xfp.java.prng.PRNG;

//----------------------------------------------------------------
/** Test desired properties of BigFloat. 
 * <p>
 * <pre>
 * mvn -Dtest=xfp/java/test/numbers/BigFloatTest test > BigFloatTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-17
 */

public final class BigFloatTest {

  //--------------------------------------------------------------

  private static final boolean correctRounding (final BigFloat f) {
    // TODO: this is necessary but not sufficient to ensure 
    // rounding was correct?
    final double x = f.doubleValue();
    // not really true, but can't check easily
    // f.numerator().divide(f.denominator()).doubleValue?
    if (! Double.isFinite(x)) { 
      final BigInteger q = f.significand().shiftLeft(f.exponent());
      return ! Double.isFinite(q.doubleValue()); }
    final BigFloat fx = BigFloat.valueOf(x);
    final int r = f.compareTo(fx);
    final boolean result;
    if (r < 0) { // fx > f
      final double x1o = Math.nextDown(x);
      final BigFloat flo = BigFloat.valueOf(x1o);
      result = flo.compareTo(f) < 0;}
    else if (r > 0) { // fx < f
      final double xhi = Math.nextUp(x);
      final BigFloat fhi = BigFloat.valueOf(xhi);
      result = f.compareTo(fhi) < 0; } 
    else { result = true; }
    return result; }

  //--------------------------------------------------------------

  private static final int TRYS = 1 * 1024;

  @SuppressWarnings({ "static-method" })
  @Test
  public final void longRoundingTest () {
    final BigFloat f = 
      BigFloat.valueOf(
        BigInteger.valueOf(0x789f09858446ad92L),
        0);
    assertTrue(correctRounding(f)); }

//  @SuppressWarnings({ "static-method" })
//  @Test
//  public final void fromBigIntegersRoundingTest () {
//    final Generator gn = 
//      Generators.bigIntegerGenerator(
//        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
//    final Generator ge = 
//      Generators.intGenerator(
//        PRNG.well44497b("seeds/Well44497b-2019-01-07.txt"));
//    for (int i=0;i<TRYS;i++) {
//      final BigInteger n = (BigInteger) gn.next();
//      final int e = ge.nextInt();
//      final BigFloat f = BigFloat.valueOf(n,e);
//      assertTrue(correctRounding(f),
//        () -> 
//      "\nn= " + n.toString(0x10) 
//      + "\nd= " + Integer.toHexString(e) 
//      + "\n\nf= " + f.toString()
//      + "\n\nxf= " + Double.toHexString(f.doubleValue())); } }

//  @SuppressWarnings({ "static-method" })
//  @Test
//  public final void fromLongsRoundingTest () {
//    final Generator g0 = 
//      Generators.longGenerator(
//        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
//    final Generator g1 = 
//      Generators.intGenerator(
//        PRNG.well44497b("seeds/Well44497b-2019-01-07.txt"));
//    for (int i=0;i<TRYS;i++) {
//      final long n = g0.nextLong();
//      final int e = g1.nextInt();
//      final BigFloat f = BigFloat.valueOf(n,e);
//      assertTrue(correctRounding(f)); } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void uniformDoubleRoundingTest () {
    final Generator g = 
      Doubles.uniformGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"),
        -Double.MAX_VALUE, Double.MAX_VALUE);
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final BigFloat f = BigFloat.valueOf(x);
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
      final BigFloat f = BigFloat.valueOf(x);
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
      final BigFloat f = BigFloat.valueOf(x);
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
      final BigFloat f = BigFloat.valueOf(x);
      assertTrue(correctRounding(f)); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
