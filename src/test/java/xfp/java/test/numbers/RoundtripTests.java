package xfp.java.test.numbers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.math3.fraction.BigFraction;
import org.junit.jupiter.api.Test;

import xfp.java.prng.Generator;
import xfp.java.prng.Generators;
import xfp.java.prng.PRNG;
import xfp.java.prng.Seeds;

//----------------------------------------------------------------
/** Test number conversions expected to be lossless. 
 * <p>
 * <pre>
 * mvn -Dtest=xfp/java/test/numbers/RoundtripTests test > RoundtripTests.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-01
 */

public final class RoundtripTests {

  private static final Generator finiteDoubles () {
    return
      Generators.finiteDoubleGenerator(
        PRNG.well44497b(
          Seeds.seed("seeds/Well44497b-2019-01-05.txt"))); }

  //--------------------------------------------------------------

  private static final int TRYS = 16 * 1024;

  /** BigFraction should be able to represent any double exactly.
   */
  @SuppressWarnings({ "static-method" })
  @Test
  public final void double2BigFractionTest () {
    final Generator g = finiteDoubles();
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final BigFraction f = new BigFraction(x);
      final double xf = f.doubleValue();
      assertTrue(
        x == xf,
        () -> { return 
          Double.toHexString(x) + "\n" +
          Double.toHexString(xf) + "\n" +
          f.getNumerator() + "\n" +
          f.getDenominator(); }); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
