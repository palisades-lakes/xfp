package xfp.java.test.numbers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import xfp.java.prng.Generator;
import xfp.java.prng.Generators;
import xfp.java.prng.PRNG;

//----------------------------------------------------------------
/** Test desired properties of BigDecimal. 
 * <p>
 * <pre>
 * mvn -Dtest=xfp/java/test/numbers/BigDecimalTest test > BigDecimalTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-09
 */

public final class BigDecimalTest {

  //--------------------------------------------------------------

  /** Conversion to and from BigInteger. 
   */
  private static final boolean 
  correctRounding (final BigDecimal f) {
    // TODO: this is necessary but not sufficient to ensure 
    // rounding was correct?
    final double x = f.doubleValue();
    final BigDecimal fx = new BigDecimal(x);
    final int r = f.compareTo(fx);
    final boolean result;
    if (r < 0) { // fx > f
      final double x1o = Math.nextDown(x);
      final BigDecimal flo = new BigDecimal(x1o);
      result = flo.compareTo(f) < 0;}
    else if (r > 0) { // fx < f
      final double xhi = Math.nextUp(x);
      final BigDecimal fhi = new BigDecimal(xhi);
      result = f.compareTo(fhi) < 0; } 
    else { result = true; }
    return result; }

  //--------------------------------------------------------------

  private static final int TRYS = 32 * 1024;

// Fails: BigDecimal can't represent all rationals  
//  @SuppressWarnings({ "static-method" })
//  @Test
//  public final void roundingTest () {
//    final BigDecimal f = 
//      new BigDecimal(13).divide(new BigDecimal(11));
//    assertTrue(correctRounding(f)); }

//Fails: BigDecimal can't represent all rationals  
//  @SuppressWarnings({ "static-method" })
//  @Test
//  public final void fromBigIntegersRoundingTest () {
//    final Generator gn = 
//      Generators.eIntegerGenerator(
//        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
//    final Generator gd = 
//      Generators.nonzeroBigIntegerGenerator(
//        PRNG.well44497b("seeds/Well44497b-2019-01-07.txt"));
//    for (int i=0;i<TRYS;i++) {
//      // some longs will not be exactly representable as doubles
//      final BigInteger n = (BigInteger) gn.next();
//      final BigInteger d = (BigInteger) gd.next();
//      final BigDecimal f = 
//      new BigDecimal(n).divide(new BigDecimal(d));
//      assertTrue(correctRounding(f)); } }

//Fails: BigDecimal can't represent all rationals  
//  @SuppressWarnings({ "static-method" })
//  @Test
//  public final void fromLongsRoundingTest () {
//    final Generator g = 
//      Generators.longGenerator(
//        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
//    for (int i=0;i<TRYS;i++) {
//      // some longs will not be exactly representable as doubles
//      final long n = g.nextLong();
//      final long d = g.nextLong();
//      final BigDecimal f = 
//        new BigDecimal(n).divide(new BigDecimal(d));
//      assertTrue(correctRounding(f)); } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void finiteDoubleRoundingTest () {
    final Generator g = 
      Generators.finiteDoubleGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final BigDecimal f = new BigDecimal(x);
      assertTrue(correctRounding(f)); } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void subnormalDoubleRoundingTest () {
    final Generator g = 
      Generators.subnormalDoubleGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final BigDecimal f = new BigDecimal(x);
      assertTrue(correctRounding(f)); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
