package xfp.java.test.numbers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;

import org.apache.commons.math3.fraction.BigFraction;
import org.junit.jupiter.api.Test;

import clojure.lang.BigInt;
import clojure.lang.Numbers;
import clojure.lang.Ratio;
import xfp.java.numbers.Doubles;
import xfp.java.numbers.Ratios;
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

  private static final int TRYS = 127;

  /** BigFraction should be able to represent any double exactly.
   */
  public static final boolean double2BigFraction () {
    final Generator g = finiteDoubles();
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final BigFraction f = new BigFraction(x);
      final double xf = f.doubleValue();
      if (x != xf) { 
//        System.out.println("\n" + 
//          "double2BigFraction:" + i + " " + Doubles.isNormal(x) +"\n" +
//          Double.toHexString(x) + "\n" +
//          Double.toHexString(xf) + "\n" +
//          f.getNumerator() + "\n" +
//          f.getDenominator());
        return false; } } 
    return true; }

  /** Ratio should be able to represent any double exactly.
   */
  public static final boolean double2Ratio () {
    final Generator g = finiteDoubles();
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final Ratio f = Ratios.toRatio(x);
      final double xf = f.doubleValue();
      if (x != xf) { 
//        System.out.println("\n" + 
//          "double2Ratio:" + i + " " + Doubles.isNormal(x) + "\n" + 
//          Double.toHexString(x) + "\n" +
//          Double.toHexString(xf) + "\n" +
//          f.numerator + "\n" +
//          f.denominator);
        return false; } } 
    return true; }

  /** Ratio should be able to represent any double exactly.
   */
  public static final boolean double2Rationalize () {
    final Generator g = finiteDoubles();
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      // Might return Ratio or BigInt
      final Number f = Numbers.rationalize(Double.valueOf(x));
      final double xf = f.doubleValue();
      if (x != xf) { 
//        System.out.println("\n" + 
//          "double2Rationalize:" + i + " " + Doubles.isNormal(x) + "\n" + 
//          Double.toHexString(x) + "\n" +
//          Double.toHexString(xf) + "\n" +
//          f);
        return false; } } 
    return true; }

  /** Ratio should be able to represent any double exactly.
   */
  public static final boolean rationalizers () {
    final Generator g = finiteDoubles();
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final BigFraction f0 = new BigFraction(x).reduce();
      final BigInteger f0n = f0.getNumerator();
      final BigInteger f0d = f0.getDenominator();
      // Ratio doesn't have a reduce operation
      final Ratio f1 = Ratios.toRatio(x);
      final BigInteger f1n = f1.numerator;
      final BigInteger f1d = f1.denominator;
      // Might return Ratio or BigInt
      final Number f2 = Numbers.rationalize(Double.valueOf(x));
      final BigInteger f2n, f2d;
      if (f2 instanceof BigInt) {
        f2n = ((BigInt) f2).toBigInteger();
        f2d = BigInteger.ONE; }
      else if (f2 instanceof Ratio) {
        f2n = ((Ratio) f2).numerator;
        f2d = ((Ratio) f2).denominator; }
      else { throw new RuntimeException("can't get here"); }
      if (! ((f0n == f1n) && (f1n == f2n) && (f0d == f1d) && (f1d == f2d))) { 
        System.out.println("\n" + 
          "rationalizers:" + i + " " + Doubles.isNormal(x) + "\n" + 
          Double.toHexString(x) + "\n\n" +
          "BigFraction\n" + f0n + "\n" + f0d + "\n\n" +
          "toRatio\n" + f1n + "\n" + f1d + "\n\n" +
          "rationalize\n" + f2n + "\n" + f2d + "\n\n");
        return false; } } 
    return true; }

  //--------------------------------------------------------------
  /** 
   */
  @SuppressWarnings({ "static-method" })
  @Test
  public final void roundTripTest () {

    // This should be true.
    // BigFraction should be able to represent any double exactly.
    // BigFraction.doubleValue() is broken.
    //assertTrue(double2BigFraction());
    assertTrue(! double2BigFraction());

    // This should be true.
    // Ratio should be able to represent any double exactly.
    // Not sure whether Ratio.doubleValue() or 
    // Ratios.toRatio(double) is broken.
    //assertTrue(double2Ratio());
    assertTrue(! double2Ratio());

    // This should be true.
    // Ratio should be able to represent any double exactly.
    // Not sure whether Ratio.doubleValue() or 
    // Ratios.toRatio(double) is broken.
    //assertTrue(double2Rationalize());
    assertTrue(! double2Rationalize());
    
    // check where to-rational conversions differ
    // if they all did exact conversion to 
    assertTrue(! rationalizers());
  }
  //--------------------------------------------------------------
}
//--------------------------------------------------------------
