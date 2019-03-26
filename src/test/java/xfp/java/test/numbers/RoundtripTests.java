package xfp.java.test.numbers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import xfp.java.accumulators.MutableRationalSum;
import xfp.java.numbers.Doubles;
import xfp.java.numbers.Rational;
import xfp.java.prng.Generator;
import xfp.java.prng.PRNG;

//----------------------------------------------------------------
/** Test number conversions expected to be lossless. 
 * <p>
 * <pre>
 * mvn -Dtest=xfp/java/test/numbers/RoundtripTests test > RoundtripTests.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-25
 */

public final class RoundtripTests {

  private static final int TRYS = 32*1024;

  public static final Generator finiteDoubles () {
    return
      Doubles.finiteGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt")); }

  public static final Generator subnormalDoubles () {
    return
      Doubles.subnormalGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt")); }

  public static final Generator normalDoubles () {
    return
      Doubles.normalGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt")); }

  //--------------------------------------------------------------
  /** BigDecimal should be able to represent any double exactly.
   */

  private static final boolean double2BigDecimal2Double () {
    final Generator g = 
      finiteDoubles();
      //subnormalDoubles();
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final BigDecimal f = new BigDecimal(x);
      final double xf = f.doubleValue();
      if (x != xf) { 
        System.out.println("\n\n" + 
          "BigDecimal.doubleValue():" + Doubles.isNormal(x) +"\n" 
          + x + "\n" + xf + "\n\n" +
          Double.toHexString(x) + "\n" +
          Double.toHexString(xf) + "\n\n" +
          f + "\n" 
          //+ f.toHexString(f) + "\n" 
          );
        return false; } }
    return true; }

  //--------------------------------------------------------------
  /** ERational should be able to represent any double exactly.
   */

  private static final boolean double2Rational2Double () {
    final Generator g = 
      finiteDoubles();
      //subnormalDoubles();
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final Rational f = Rational.valueOf(x);
      final double xf = f.doubleValue();
      if (x != xf) { 
        System.out.println("\n\n" + 
          "Rational.doubleValue:" + Doubles.isNormal(x) +"\n" +
          x + "\n" +
          xf + "\n\n" +
          Double.toHexString(x) + "\n" +
          Double.toHexString(xf) + "\n\n" +
          f.numerator() + "\n" +
          f.denominator() + "\n\n" +
          f.numerator().toString(0x10) + "\n" +
          f.denominator().toString(0x10));
        return false; } }
    return true; }

  //--------------------------------------------------------------
  /** RationalSum should be able to represent any double exactly.
   */

  private static final boolean double2RationalSum2Double () {
    final Generator g = 
      finiteDoubles();
    //normalDoubles();
    //subnormalDoubles();
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final MutableRationalSum f = MutableRationalSum.make().add(x);
      final double xf = f.doubleValue();
      final Rational xe = Rational.valueOf(x);
      if (x != xf) { 
        System.out.println("\n\n" + 
          "RationalSum: isNormal=" + Doubles.isNormal(x) +"\n" 
          + x + "\n"
          + xf + "\n\n"
          + Double.toHexString(x) + "\n" 
          + Double.toHexString(xf) + "\n\n"
          + f.numerator().toString(0x10).toUpperCase() + "\n" 
          + xe.numerator().toString(0x10) + "\n\n"
          + f.denominator().toString(0x10) + "\n" 
          + xe.denominator().toString(0x10) + "\n\n"
//          f.getNumerator() + "\n" +
//          f.getDenominator() + "\n\n" +
//          f.getNumerator().toString(0x10) + "\n" +
//          f.getDenominator().toString(0x10)
        );
        return false; } }
    return true; }

  //--------------------------------------------------------------
  /** check for round trip consistency:
   * double -&gt; rational -&gt; double
   * should be an identity transform.
   */
  @SuppressWarnings({ "static-method" })
  @Test
  public final void roundTripTest () {

    assertTrue(double2Rational2Double());
    assertTrue(double2BigDecimal2Double());
    assertTrue(double2RationalSum2Double());
  }
  //--------------------------------------------------------------
}
//--------------------------------------------------------------
