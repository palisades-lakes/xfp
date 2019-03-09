package xfp.java.test.numbers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import xfp.java.accumulators.RationalSum;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;
import xfp.java.prng.PRNG;

//----------------------------------------------------------------
/** Test desired properties of RationalSum. 
 * <p>
 * <pre>
 * mvn -Dtest=xfp/java/test/numbers/RationalSumTest test > RationalSumTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-08
 */

public final class RationalSumTest {

  //--------------------------------------------------------------

  /** Conversion to and from RationalSum. 
   */
  private static final boolean 
  correctRounding (final RationalSum f) {
    // TODO: this is necessary but not sufficient to ensure 
    // rounding was correct?
    final double x = f.doubleValue();
    final RationalSum fx = RationalSum.make().add(x);
    final int r = f.compareTo(fx);
    final boolean result;
    if (r < 0) { // fx > f
      final double x1o = Math.nextDown(x);
      final RationalSum flo = RationalSum.valueOf(x1o);
      result = flo.compareTo(f) < 0;}
    else if (r > 0) { // fx < f
      final double xhi = Math.nextUp(x);
      final RationalSum fhi = RationalSum.valueOf(xhi);
      result = f.compareTo(fhi) < 0; } 
    else { result = true; }
    return result; }

  //--------------------------------------------------------------

  private static final int TRYS = 32 * 1024;

  @SuppressWarnings({ "static-method" })
  @Test
  public final void roundingTest () {
    final RationalSum f = RationalSum.valueOf(13,11);
    assertTrue(correctRounding(f)); }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void fromEIntegersRoundingTest () {
    final Generator gn = 
      Generators.bigIntegerGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    final Generator gd = 
      Generators.nonzeroBigIntegerGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-07.txt"));
    for (int i=0;i<TRYS;i++) {
      // some longs will not be exactly representable as doubles
      final BigInteger n = (BigInteger) gn.next();
      final BigInteger d = (BigInteger) gd.next();
      final RationalSum f = RationalSum.valueOf(n,d);
      assertTrue(correctRounding(f)); } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void fromLongsRoundingTest () {
    final Generator g = 
      Generators.longGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      // some longs will not be exactly representable as doubles
      final long n = g.nextLong();
      final long d = g.nextLong();
      final RationalSum f = RationalSum.valueOf(n,d);
      assertTrue(correctRounding(f)); } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void finiteDoubleRoundingTest () {
    final Generator g = 
      Generators.finiteDoubleGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final RationalSum f = RationalSum.valueOf(x);
      assertTrue(correctRounding(f)); } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void subnormalDoubleRoundingTest () {
    final Generator g = 
      Generators.subnormalDoubleGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final RationalSum f = RationalSum.valueOf(x);
      assertTrue(correctRounding(f)); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
