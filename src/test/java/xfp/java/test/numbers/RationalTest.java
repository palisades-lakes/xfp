package xfp.java.test.numbers;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import xfp.java.Debug;
import xfp.java.numbers.Doubles;
import xfp.java.numbers.Numbers;
import xfp.java.numbers.Rational;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;
import xfp.java.prng.PRNG;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test desired properties of Rational. 
 * <p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/numbers/RationalTest test > RationalTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-21
 */

public final class RationalTest {

  //--------------------------------------------------------------

  private static final void correctRounding (final Rational f) {
    Common.doubleRoundingTest(
      Rational::valueOf,
      Numbers::doubleValue,
      f); }

  //--------------------------------------------------------------

  private static final int TRYS = 1 * 1024;

  @SuppressWarnings({ "static-method" })
  @Test
  public final void roundingTest () {
    correctRounding(Rational.valueOf(13,11)); }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void anotherRoundingTest () {
    Debug.DEBUG = true;
    final Rational f = 
      Rational.valueOf(
        BigInteger.valueOf(-0x331c0c32d0072fL),
        BigInteger.valueOf(0x1000000L));
    correctRounding(f); 
    Debug.DEBUG = false; }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void longRoundingTest () {
    final Rational f = 
      Rational.valueOf(
        BigInteger.valueOf(0x789f09858446ad92L),
        BigInteger.valueOf(0x19513ea5d70c32eL));
    correctRounding(f); }

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
      // some longs will not be exactly representable as doubles
      final BigInteger n = (BigInteger) gn.next();
      final BigInteger d = (BigInteger) gd.next();
      final Rational f = Rational.valueOf(n,d);
      correctRounding(f); } }

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
      // some longs will not be exactly representable as doubles
      final long n = g0.nextLong();
      final long d = g1.nextLong();
      final Rational f = Rational.valueOf(
        BigInteger.valueOf(n),
        BigInteger.valueOf(d));
      correctRounding(f); } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void finiteDoubleRoundingTest () {
    final Generator g = 
      Doubles.finiteGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final Rational f = Rational.valueOf(x);
      correctRounding(f); } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void subnormalDoubleRoundingTest () {
    final Generator g = 
      Doubles.subnormalGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final Rational f = Rational.valueOf(x);
      correctRounding(f); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
