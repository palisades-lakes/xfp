package xfp.java.test.numbers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.upokecenter.numbers.EInteger;
import com.upokecenter.numbers.ERational;

import xfp.java.prng.Generator;
import xfp.java.prng.Generators;
import xfp.java.prng.PRNG;

//----------------------------------------------------------------
/** Test desired properties of ERational. 
 * <p>
 * <pre>
 * mvn -Dtest=xfp/java/test/numbers/ERationalTest test > ERationalTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-05
 */

public final class ERationalTest {

  //--------------------------------------------------------------

  /** Conversion to and from BigInteger. 
   */
  private static final boolean 
  correctRounding (final ERational f) {
    // TODO: this is necessary but not sufficient to ensure 
    // rounding was correct?
    final double x = f.ToDouble();
    final ERational fx = ERational.FromDouble(x);
    final int r = f.compareTo(fx);
    final boolean result;
    if (r < 0) { // fx > f
      final double x1o = Math.nextDown(x);
      final ERational flo = ERational.FromDouble(x1o);
      result = flo.compareTo(f) < 0;}
    else if (r > 0) { // fx < f
      final double xhi = Math.nextUp(x);
      final ERational fhi = ERational.FromDouble(xhi);
      result = f.compareTo(fhi) < 0; } 
    else { result = true; }
    return result; }

  //--------------------------------------------------------------

  private static final int TRYS = 32 * 1024;

  @SuppressWarnings({ "static-method" })
  @Test
  public final void roundingTest () {
    final ERational f = ERational.Create(13,11);
    assertTrue(correctRounding(f)); }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void longRoundingTest () {
    final ERational f = 
      ERational.Create(
        EInteger.FromInt64(0x789f09858446ad92L),
        EInteger.FromInt64(0x19513ea5d70c32eL));
    assertTrue(correctRounding(f)); }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void fromEIntegersRoundingTest () {
    final Generator gn = 
      Generators.eIntegerGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    final Generator gd = 
      Generators.nonzeroEIntegerGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-07.txt"));
    for (int i=0;i<TRYS;i++) {
      // some longs will not be exactly representable as doubles
      final EInteger n = (EInteger) gn.next();
      final EInteger d = (EInteger) gd.next();
      final ERational f = ERational.Create(n,d);
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
      final ERational f = ERational.Create(
        EInteger.FromInt64(n),
        EInteger.FromInt64(d));
      assertTrue(correctRounding(f)); } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void finiteDoubleRoundingTest () {
    final Generator g = 
      Generators.finiteDoubleGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final ERational f = ERational.FromDouble(x);
      assertTrue(correctRounding(f)); } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void subnormalDoubleRoundingTest () {
    final Generator g = 
      Generators.subnormalDoubleGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final ERational f = ERational.FromDouble(x);
      assertTrue(correctRounding(f)); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
