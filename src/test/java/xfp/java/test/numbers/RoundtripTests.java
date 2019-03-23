package xfp.java.test.numbers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.upokecenter.numbers.EFloat;
import com.upokecenter.numbers.ERational;

import xfp.java.accumulators.EFloatSum;
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
 * @version 2019-03-22
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
  // continued fraction like

  //  private static final double toDoubleGT1 (final BigInteger n,
  //                                           final BigInteger d) {
  //    assert 0 < n.compareTo(d);
  //    final BigInteger[] qr = n.divideAndRemainder(d);
  //    if (0 == qr[1].signum()) { return qr[0].doubleValue(); }
  //    return qr[0].doubleValue() + (1.0/toDoubleGT0(d,qr[1])); }
  //
  //
  //  private static final double toDoubleGT0 (final BigInteger n,
  //                                           final BigInteger d) {
  //    final int c = n.compareTo(d);
  //    if (0 == c) { return 1.0; }
  //    if (0 > c) { return 1.0/toDoubleGT1(d,n); }
  //    return toDoubleGT1(n,d); }

  /** Trial and error.
   */
  //  public static final double toDouble (final BigFraction f) {
  //    final BigFraction fr = f.reduce();
  //    final BigInteger n = fr.getNumerator();
  //    final BigInteger d = fr.getDenominator();
  //    assert 1 == d.signum() :
  //      n + "\n" + d;
  //    final int ns = n.signum();
  //    if (0 == ns) { return 0.0; }
  //    if (-1 == ns) { return - toDoubleGT0(n.negate(),d); }
  //    return toDoubleGT0(n,d);  }

  //--------------------------------------------------------------
  // shift 1 hack

  //  private static final double toDouble (final BigInteger n,
  //                                        final BigInteger d) {
  //    final double nx = n.doubleValue();
  //    final double dx = d.doubleValue();
  //    System.out.println(
  //      n + "\n" + d + "\n" + 
  //        Double.toHexString(nx) + "\n" +
  //        Double.toHexString(dx));
  //    if (Double.isInfinite(nx) || Double.isInfinite(dx)) {
  //      return toDouble(n.shiftRight(1),d.shiftRight(1)); }
  //    return nx / dx;  }
  //
  //  public static final double toDouble (final BigFraction f) {
  //    final BigFraction fr = f.reduce();
  //    final BigInteger n = fr.getNumerator();
  //    final BigInteger d = fr.getDenominator();
  //    assert 1 == d.signum() :
  //      n + "\n" + d;
  //    final int ns = n.signum();
  //    if (0 == ns) { return 0.0; }
  //    if (-1 == ns) { return - toDouble(n.negate(),d); }
  //    return toDouble(n,d);  }

  //--------------------------------------------------------------
  // BigFraction
  // within an ulp, not exact...

  //  private static final double toDouble (final BigFraction f) {
  //    //final BigFraction fr = f.reduce();
  //    final BigInteger n = f.getNumerator();
  //    final int ns = n.signum();
  //    if (0 == ns) { return 0.0; }
  //    if (0 > ns) { return - toDouble(f.negate()); }
  //    final BigInteger d = f.getDenominator();
  //    final BigDecimal n10 = new BigDecimal(n);
  //    final BigDecimal d10 = new BigDecimal(d);
  //    //final MathContext mc = MathContext.DECIMAL128;
  //    final MathContext mc = new MathContext(128,RoundingMode.HALF_UP);
  //    final BigDecimal x10 = n10.divide(d10,mc); 
  //    final double x = x10.doubleValue();
  //    if (Doubles.isNormal(x)) { return x; }
  //    return 2.0*x; }
  //  //    return toDouble(f.multiply(2)); }

  //--------------------------------------------------------------


  //  public static final boolean double2ERational2Double () {
  //    final double x = -0x0.19c0ba819d5c3p-1022;
  //    final ERational f = ERational.FromDouble(x);
  //    final double xf = f.ToDouble();
  //    System.out.println("\n\n" + 
  //      "ERational.ToDouble:" + Doubles.isNormal(x) +"\n" +
  //      x + "\n" +
  //      xf + "\n\n" +
  //      Double.toHexString(x) + "\n" +
  //      Double.toHexString(xf) + "\n\n" +
  ////      f.getNumerator() + "\n" +
  ////      f.getDenominator() + "\n\n" +
  //      f.getNumerator().ToRadixString(16) + "\n" +
  //      f.getDenominator().ToRadixString(16));
  //    //final double dx = Math.abs(x - xf);
  //    //if (dx > Math.ulp(x)) { 
  //    if (x != xf) { 
  //      return false; } 
  //    return true; }

  //  public static final boolean double2BigFraction2Double () {
  //    final double x = -0x0.19c0ba819d5c3p-1022;
  //    final BigFraction f = new BigFraction(x);
  //    final double xf = toDouble(f);
  //    System.out.println("\n\n" + 
  //      "toDouble:" + Doubles.isNormal(x) +"\n" +
  //      x + "\n" +
  //      xf + "\n\n" +
  //      Double.toHexString(x) + "\n" +
  //      Double.toHexString(xf) + "\n\n" +
  ////      f.getNumerator() + "\n" +
  ////      f.getDenominator() + "\n\n" +
  //      f.getNumerator().toString(16) + "\n" +
  //      f.getDenominator().toString(16));
  //    //final double dx = Math.abs(x - xf);
  //    //if (dx > Math.ulp(x)) { 
  //    if (x != xf) { 
  //      return false; } 
  //    return true; }

  //  public static final boolean double2BigFraction2Double () {
  //    final Generator g = 
  //      finiteDoubles();
  //    //      subnormalDoubles();
  //    for (int i=0;i<TRYS;i++) {
  //      final double x = g.nextDouble();
  //      final BigFraction f = new BigFraction(x);
  //      final double xf = toDouble(f);
  //      if (x != xf) { 
  //        //        final double dx = Math.abs(x - xf);
  //        //        if (dx > Math.ulp(x)) { 
  //        System.out.println("\n\n" + 
  //          "toDouble:" + i + " " + Doubles.isNormal(x) +"\n" +
  //          x + "\n" +
  //          xf + "\n\n" +
  //          Double.toHexString(x) + "\n" +
  //          Double.toHexString(xf) + "\n\n" +
  //          f.getNumerator().toString(16) + "\n" +
  //          f.getDenominator().toString(16));
  //        return false; } } 
  //    return true; }

  //--------------------------------------------------------------

  //  // Fails.
  //  /** Based on Rational.doubleValue() from jscience 4.3.1
  //   */
  //
  //  public static final double 
  //  toDoubleJScience (final BigFraction f) {
  //    final BigInteger _dividend = f.getNumerator();
  //    final BigInteger _divisor = f.getDenominator();
  //    // Avoid negative numbers (ref. bitLength) 
  //    if (-1 == _dividend.signum()) {
  //      return - toDoubleJScience(f.abs()); }
  //
  //    // Normalize to 63 bits (minimum).
  //    final int dividendBitLength = _dividend.bitLength();
  //    final int divisorBitLength = _divisor.bitLength();
  //    if (dividendBitLength > divisorBitLength) {
  //      // Normalizes the divisor to 63 bits.
  //      final int shift = divisorBitLength - 63;
  //      final long divisor = _divisor.shiftRight(shift).longValue();
  //      final BigInteger dividend = _dividend.shiftRight(shift);
  //      return dividend.doubleValue() / divisor; }
  //    // Normalizes the dividend to 63 bits.
  //    final int shift = dividendBitLength - 63;
  //    final long dividend = _dividend.shiftRight(shift).longValue();
  //    final BigInteger divisor = _divisor.shiftRight(shift);
  //    return dividend / divisor.doubleValue(); }

  //  //Fails.
  //  /** BigFraction should be able to represent any double exactly.
  //   */
  //  public static final boolean double2BigFractionJS () {
  //    final Generator g = finiteDoubles();
  //    for (int i=0;i<TRYS;i++) {
  //      final double x = g.nextDouble();
  //      final BigFraction f = new BigFraction(x);
  //      final double xf = toDoubleJScience(f);
  //      if (x != xf) { 
  //        System.out.println("\n" + 
  //          "double2BigFraction:" + i + " " + Doubles.isNormal(x) +"\n" +
  //          Double.toHexString(x) + "\n" +
  //          Double.toHexString(xf) + "\n" +
  //          f.getNumerator() + "\n" +
  //          f.getDenominator());
  //        return false; } } 
  //    return true; }

  //  // Fails.
  //  /** BigFraction should be able to represent any double exactly.
  //   */
  //  public static final boolean double2BigFraction () {
  //    final Generator g = finiteDoubles();
  //    for (int i=0;i<TRYS;i++) {
  //      final double x = g.nextDouble();
  //      final BigFraction f = new BigFraction(x);
  //      final double xf = f.doubleValue();
  //      if (x != xf) { 
  //        //        System.out.println("\n" + 
  //        //          "double2BigFraction:" + i + " " + Doubles.isNormal(x) +"\n" +
  //        //          Double.toHexString(x) + "\n" +
  //        //          Double.toHexString(xf) + "\n" +
  //        //          f.getNumerator() + "\n" +
  //        //          f.getDenominator());
  //        return false; } } 
  //    return true; }

  //  // Fails. Off in least significant bit for small numbers.
  //  /** Ratio should be able to represent any double exactly.
  //   */
  //  public static final boolean double2Ratio () {
  //    final Generator g = finiteDoubles();
  //    for (int i=0;i<TRYS;i++) {
  //      final double x = g.nextDouble();
  //      final Ratio f = Ratios.toRatio(x);
  //      final double xf = f.doubleValue();
  //      if (x != xf) { 
  //        System.out.println("\n" + 
  //          "double2Ratio:" + i + " " + Doubles.isNormal(x) + "\n" + 
  //          Double.toHexString(x) + "\n" +
  //          Double.toHexString(xf) + "\n" +
  //          f.numerator + "\n" +
  //          f.denominator);
  //        return false; } } 
  //    return true; }

  //// Fails.
  //  /** Ratio should be able to represent any double exactly.
  //   */
  //  public static final boolean double2Rationalize () {
  //    final Generator g = finiteDoubles();
  //    for (int i=0;i<TRYS;i++) {
  //      final double x = g.nextDouble();
  //      // Might return Ratio or BigInt
  //      final Number f = Numbers.rationalize(Double.valueOf(x));
  //      final double xf = f.doubleValue();
  //      if (x != xf) { 
  //        //        System.out.println("\n" + 
  //        //          "double2Rationalize:" + i + " " + Doubles.isNormal(x) + "\n" + 
  //        //          Double.toHexString(x) + "\n" +
  //        //          Double.toHexString(xf) + "\n" +
  //        //          f);
  //        return false; } } 
  //    return true; }

  // This fails.
  /** Compare the results of various double -&gt; rational
   * methods to see if the problem is in the first conversion
   * or the 2nd.
   * <p>
   * <em>Answer:<em> get 3 different (numerator,denominator)
   * combinations. No simple way to say who is right, but
   * BigFraction code looks more ambitious.
   */
  //  public static final boolean rationalizers () {
  //    final Generator g = finiteDoubles();
  //    for (int i=0;i<TRYS;i++) {
  //      final double x = g.nextDouble();
  //      
  //      final BigFraction f0 = new BigFraction(x).reduce();
  //      final BigInteger f0n = f0.getNumerator();
  //      final BigInteger f0d = f0.getDenominator();
  //
  //      // Ratio doesn't have a reduce operation
  //      final Ratio f1 = Ratios.toRatio(x);
  //      final BigInteger f1n = f1.numerator;
  //      final BigInteger f1d = f1.denominator;
  //      
  //      // Might return Ratio or BigInt
  //      final Number f2 = Numbers.rationalize(Double.valueOf(x));
  //      final BigInteger f2n, f2d;
  //      if (f2 instanceof BigInt) {
  //        f2n = ((BigInt) f2).toBigInteger();
  //        f2d = BigInteger.ONE; }
  //      else if (f2 instanceof Ratio) {
  //        f2n = ((Ratio) f2).numerator;
  //        f2d = ((Ratio) f2).denominator; }
  //      else { throw new RuntimeException("can't get here"); }
  //      
  //      if (! ((f0n == f1n) && (f1n == f2n) && (f0d == f1d) && (f1d == f2d))) { 
  //        System.out.println("\n" + 
  //          "rationalizers:" + i + " " + Doubles.isNormal(x) + "\n" + 
  //          Double.toHexString(x) + "\n\n" +
  //          "BigFraction\n" + f0n + "\n" + f0d + "\n\n" +
  //          "toRatio\n" + f1n + "\n" + f1d + "\n\n" +
  //          "rationalize\n" + f2n + "\n" + f2d + "\n\n");
  //        return false; } } 
  //    return true; }

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
          "BigDecimal.ToDouble:" + Doubles.isNormal(x) +"\n" +
          x + "\n" +
          xf + "\n\n" +
          Double.toHexString(x) + "\n" +
          Double.toHexString(xf) + "\n\n" +
          f + "\n" 
          //+ f.toHexString(f) + "\n" 
          );
        return false; } }
    return true; }

  //--------------------------------------------------------------
  /** EFloat should be able to represent any double exactly.
   */

  private static final boolean double2EFloat2Double () {
    final Generator g = 
      finiteDoubles();
      //subnormalDoubles();
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final EFloat f = EFloat.FromDouble(x);
      final double xf = f.ToDouble();
      if (x != xf) { 
        System.out.println("\n\n" + 
          "EFloat.ToDouble:" + Doubles.isNormal(x) +"\n" +
          x + "\n" +
          xf + "\n\n" +
          Double.toHexString(x) + "\n" +
          Double.toHexString(xf) + "\n\n" +
          f + "\n" +
          EFloatSum.toHexString(f) + "\n" );
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
  /** ERational should be able to represent any double exactly.
   */

  private static final boolean double2ERational2Double () {
    final Generator g = 
      finiteDoubles();
      //subnormalDoubles();
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final ERational f = ERational.FromDouble(x);
      final double xf = f.ToDouble();
      if (x != xf) { 
        System.out.println("\n\n" + 
          "ERational.ToDouble:" + Doubles.isNormal(x) +"\n" +
          x + "\n" +
          xf + "\n\n" +
          Double.toHexString(x) + "\n" +
          Double.toHexString(xf) + "\n\n" +
          f.getNumerator() + "\n" +
          f.getDenominator() + "\n\n" +
          f.getNumerator().ToRadixString(16) + "\n" +
          f.getDenominator().ToRadixString(16));
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
      final ERational xe = ERational.FromDouble(x);
      if (x != xf) { 
        System.out.println("\n\n" + 
          "RationalSum: isNormal=" + Doubles.isNormal(x) +"\n" 
          + x + "\n"
          + xf + "\n\n"
          + Double.toHexString(x) + "\n" 
          + Double.toHexString(xf) + "\n\n"
          + f.numerator().toString(16).toUpperCase() + "\n" 
          + xe.getNumerator().ToRadixString(16) + "\n\n"
          + f.denominator().toString(16) + "\n" 
          + xe.getDenominator().ToRadixString(16) + "\n\n"
//          f.getNumerator() + "\n" +
//          f.getDenominator() + "\n\n" +
//          f.getNumerator().ToRadixString(16) + "\n" +
//          f.getDenominator().ToRadixString(16)
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
    assertTrue(double2ERational2Double());
    assertTrue(double2EFloat2Double());
    //assertTrue(double2BigFraction2Double());

    // This should be true.
    // BigFraction should be able to represent any double exactly.
    //assertTrue(double2BigFractionJS());

    // This should be true. Last significand bit wrong for some
    // small numbers.
    // BigFraction should be able to represent any double exactly.
    // BigFraction.doubleValue() is broken.
    //assertTrue(double2BigFraction());

    // This should be true. Last significand bit wrong for some
    // small numbers.
    // Ratio should be able to represent any double exactly.
    // Not sure whether Ratio.doubleValue() or 
    // Ratios.toRatio(double) is broken.
    //    assertTrue(double2Ratio());

    // This should be true.
    // Ratio should be able to represent any double exactly.
    // Not sure whether Ratio.doubleValue() or 
    // Ratios.toRatio(double) is broken.
    //assertTrue(double2Rationalize());

    // check where to-rational conversions differ
    // if they all did exact conversion to rational, 
    // numerators and denominators would be the same
    // assertTrue(rationalizers());
  }
  //--------------------------------------------------------------
}
//--------------------------------------------------------------
