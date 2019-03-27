package xfp.java.test.accumulators;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import xfp.java.accumulators.MutableRationalSum;
import xfp.java.numbers.Doubles;
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
 * @version 2019-03-26
 */

public final class RationalSumTest {

  //--------------------------------------------------------------

  @SuppressWarnings({ "static-method" })
  @Test
  public final void compareToTest () {
    final MutableRationalSum f0 = MutableRationalSum.valueOf(-1.0);
    final MutableRationalSum f1 = MutableRationalSum.valueOf(1.0);
    assertTrue(f0.compareTo(f1) < 0); }

  //--------------------------------------------------------------

  /** Conversion to and from RationalSum. */

  private static final boolean 
  correctRounding (final MutableRationalSum f) {
    // TODO: is necessary but not sufficient to ensure 
    // rounding was correct?
    final double x = f.doubleValue();
    // can't add non-finite doubles to RationalSum
    if (! Double.isFinite(x)) { return true; }
    final MutableRationalSum fx = MutableRationalSum.valueOf(x);
    final int r = f.compareTo(fx);
    final boolean ok;
    if (r < 0) { // fx > f
      final double xlo = Math.nextDown(x);
      final MutableRationalSum flo = MutableRationalSum.valueOf(xlo);
      ok = flo.compareTo(f) < 0;
      if (! ok) {
        System.out.println("lo");
        System.out.println(Double.toHexString(xlo));
        System.out.println(Double.toHexString(x));
        System.out.println(flo);
        System.out.println(f);
        System.out.println(fx);
        System.out.println(); } }
    else if (r > 0) { // fx < f
      final double xhi = Math.nextUp(x);
      final MutableRationalSum fhi = MutableRationalSum.valueOf(xhi);
      ok = f.compareTo(fhi) < 0; 
      if (! ok) {
        System.out.println("hi");
        System.out.println(Double.toHexString(x));
        System.out.println(Double.toHexString(xhi));
        System.out.println(fx);
        System.out.println(f);
        System.out.println(fhi);
        System.out.println(); } }
    else { ok = true; }
    return ok; }

  //--------------------------------------------------------------

  private static final int TRYS = 1 * 1024;

  @SuppressWarnings({ "static-method" })
  @Test
  public final void rounding1311Test () {
    final MutableRationalSum f = MutableRationalSum.valueOf(13,11);
    assertTrue(correctRounding(f)); }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void fromBigIntegersRoundingTest () {
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
      final MutableRationalSum f = MutableRationalSum.valueOf(n,d);
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
      final MutableRationalSum f = MutableRationalSum.valueOf(n,d);
      assertTrue(correctRounding(f)); } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void finiteDoubleRoundingTest () {
    final Generator g = 
      Doubles.finiteGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final MutableRationalSum f = MutableRationalSum.valueOf(x);
      assertTrue(correctRounding(f)); } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void subnormalDoubleRoundingTest () {
    final Generator g = 
      Doubles.subnormalGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final MutableRationalSum f = MutableRationalSum.valueOf(x);
      assertTrue(correctRounding(f)); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
