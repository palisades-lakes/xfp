package xfp.java.test.numbers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import xfp.java.numbers.BigFloat;
import xfp.java.numbers.Doubles;
import xfp.java.numbers.Floats;
import xfp.java.numbers.Rational;
import xfp.java.numbers.RationalFloat;
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
 * @version 2019-05-28
 */

public final class RoundtripTests {

  private static final int TRYS = 32*1024;

  //--------------------------------------------------------------

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
  /** Rational should be able to represent any double exactly.
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
          "Rational.doubleValue:" + Doubles.isNormal(x) + "\n"
          + "exponent: " + Doubles.exponent(x) + "\n"
          + "significand: "
          + Long.toHexString(Doubles.significand(x)) + "\nn"
          + x + " :x\n" + xf + " : xf\n\n" +
          Double.toHexString(x) + " :x\n" +
          Double.toHexString(xf) + " :xf\n\n" +
          f.numerator() + "\n" +
          f.denominator() + "\n\n" +
          f.numerator().toString(0x10) + "\n" +
          f.denominator().toString(0x10));
        return false; } }
    return true; }

  //--------------------------------------------------------------
  /** RationalFloat should be able to represent any double exactly.
   */

  private static final boolean double2RF2Double () {
    final Generator g =
      finiteDoubles();
    //subnormalDoubles();
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final RationalFloat f = RationalFloat.valueOf(x);
      final double xf = f.doubleValue();
      if (x != xf) {
        System.out.println("\n\n" +
          "RationalFloat.doubleValue:" + Doubles.isNormal(x) + "\n"
          + "exponent: " + Doubles.exponent(x) + "\n"
          + "significand: "
          + Long.toHexString(Doubles.significand(x)) + "\nn"
          + x + " :x\n" + xf + " : xf\n\n" +
          Double.toHexString(x) + " :x\n" +
          Double.toHexString(xf) + " :xf\n\n" +
          f.numerator() + "\n" +
          f.denominator() + "\n\n" +
          f.numerator().toString(0x10) + "\n" +
          f.denominator().toString(0x10));
        return false; } }
    return true; }

  //--------------------------------------------------------------
  /** BigFloat should be able to represent any double exactly.
   */

  private static final boolean double2BF2Double () {
    final Generator g =
      finiteDoubles();
    //subnormalDoubles();
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final BigFloat f = BigFloat.valueOf(x);
      final double xf = f.doubleValue();
      if (x != xf) {
        System.out.println("\n\n" +
          "BigFloat.doubleValue:" + Doubles.isNormal(x) + "\n"
          + "exponent: " + Doubles.exponent(x) + "\n"
          + "significand: "
          + Long.toHexString(Doubles.significand(x)) + "\nn"
          + x + " :x\n" + xf + " : xf\n\n" +
          Double.toHexString(x) + " :x\n" +
          Double.toHexString(xf) + " :xf\n\n" +
          f.toString(0x10));
        return false; } }
    return true; }

  //--------------------------------------------------------------
  /** check for round trip consistency:
   * double -&gt; rational -&gt; double
   * should be an identity transform.
   */
  @SuppressWarnings({ "static-method" })
  @Test
  public final void doubleRoundTripTest () {

    assertTrue(double2Rational2Double());
    assertTrue(double2RF2Double());
    assertTrue(double2BF2Double());
    assertTrue(double2BigDecimal2Double());
  }
  //--------------------------------------------------------------

  public static final Generator finiteFloats () {
    return
      Floats.finiteGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt")); }

  public static final Generator subnormalFloats () {
    return
      Floats.subnormalGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt")); }

  public static final Generator normalFloats () {
    return
      Floats.normalGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt")); }

  //--------------------------------------------------------------
  /** BigDecimal should be able to represent any float exactly.
   */

  private static final boolean float2BigDecimal2Float () {
    final Generator g =
      finiteFloats();
    //subnormalFloats();
    for (int i=0;i<TRYS;i++) {
      final float x = g.nextFloat();
      final BigDecimal f = new BigDecimal(x);
      final float xf = f.floatValue();
      if (x != xf) {
        System.out.println("\n\n" +
          "BigDecimal.floatValue():" + Floats.isNormal(x) +"\n"
          + x + "\n" + xf + "\n\n" +
          Float.toHexString(x) + "\n" +
          Float.toHexString(xf) + "\n\n" +
          f + "\n"
          //+ f.toHexString(f) + "\n"
          );
        return false; } }
    return true; }

  //--------------------------------------------------------------
  /** Rational should be able to represent any float exactly.
   */

  private static final boolean float2RBF2Float () {
    final Generator g =
      finiteFloats();
    //subnormalFloats();
    for (int i=0;i<TRYS;i++) {
      final float x = g.nextFloat();
      final RationalFloat f = RationalFloat.valueOf(x);
      final float xf = f.floatValue();
      if (x != xf) {
        System.out.println("\n\n" +
          "Rational.floatValue:" + Floats.isNormal(x) + "\n"
          + "exponent: " + Floats.exponent(x) + "\n"
          + "significand: "
          + Long.toHexString(Floats.significand(x)) + "\nn"
          + x + " :x\n" + xf + " : xf\n\n" +
          Float.toHexString(x) + " :x\n" +
          Float.toHexString(xf) + " :xf\n\n" +
          f.numerator() + "\n" +
          f.denominator() + "\n\n" +
          f.numerator().toString(0x10) + "\n" +
          f.denominator().toString(0x10));
        return false; } }
    return true; }

  //--------------------------------------------------------------
  /** Rational should be able to represent any float exactly.
   */

  private static final boolean float2Rational2Float () {
    final Generator g =
      finiteFloats();
    //subnormalFloats();
    for (int i=0;i<TRYS;i++) {
      final float x = g.nextFloat();
      final Rational f = Rational.valueOf(x);
      final float xf = f.floatValue();
      if (x != xf) {
        System.out.println("\n\n" +
          "Rational.floatValue:" + Floats.isNormal(x) + "\n"
          + "exponent: " + Floats.exponent(x) + "\n"
          + "significand: "
          + Long.toHexString(Floats.significand(x)) + "\nn"
          + x + " :x\n" + xf + " : xf\n\n" +
          Float.toHexString(x) + " :x\n" +
          Float.toHexString(xf) + " :xf\n\n" +
          f.numerator() + "\n" +
          f.denominator() + "\n\n" +
          f.numerator().toString(0x10) + "\n" +
          f.denominator().toString(0x10));
        return false; } }
    return true; }

  //--------------------------------------------------------------
  /** check for round trip consistency:
   * float -&gt; rational -&gt; float
   * should be an identity transform.
   */
  @SuppressWarnings({ "static-method" })
  @Test
  public final void floatRoundTripTest () {

    assertTrue(float2Rational2Float());
    assertTrue(float2RBF2Float());
    assertTrue(float2BigDecimal2Float());
  }
  //--------------------------------------------------------------
}
//--------------------------------------------------------------
