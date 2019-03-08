package xfp.java.accumulators;

import java.math.BigInteger;

import clojure.lang.Numbers;
import clojure.lang.Ratio;

/** Naive sum of <code>double</code> values with Ratio 
 * accumulator (for testing).
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-07
 */
public final class RatioSum implements Accumulator {

  /** From apache commons math4 BigFraction.
   * <p>
   * Create a fraction given the double value.
   * <p>
   * This constructor behaves <em>differently</em> from
   * {@link #BigFraction(double, double, int)}. It converts the 
   * double value exactly, considering its internal bits 
   * representation. This works for all values except NaN and 
   * infinities and does not requires any loop or convergence 
   * threshold.
   * </p>
   * <p>
   * Since this conversion is exact and since double numbers are 
   * sometimes approximated, the fraction created may seem strange 
   * in some cases. For example, calling 
   * <code>new BigFraction(1.0 / 3.0)</code> does <em>not</em> 
   * create the fraction 1/3, but the fraction 
   * 6004799503160661 / 18014398509481984, because the double 
   * number passed to the constructor is not exactly 1/3
   * (this number cannot be stored exactly in IEEE754).
   * </p>
   * @see #BigFraction(double, double, int)
   * @param x the double value to convert to a fraction.
   * @exception IllegalArgumentException if value is not finite
   */

  public static final Ratio toRatio (final double x) 
    throws IllegalArgumentException {
    if (! Double.isFinite(x)) {
      throw new IllegalArgumentException(
        x + " is not rational."); }

    // compute m and k such that value = m * 2^k
    final long bits     = Double.doubleToLongBits(x);
    final long sign     = bits & 0x8000000000000000L;
    final long exponent = bits & 0x7ff0000000000000L;
    long m              = bits & 0x000fffffffffffffL;
    if (exponent != 0) {
      // this was a normalized number, 
      // add the implicit most significant bit
      m |= 0x0010000000000000L; }
    if (sign != 0) { m = -m; }
    int k = ((int) (exponent >> 52)) - 1075;
    while (((m & 0x001ffffffffffffeL) != 0) && ((m & 0x1) == 0)) {
      m >>= 1; ++k; }
    final BigInteger numerator;
    final BigInteger denominator;
    if (k < 0) { 
      numerator   = BigInteger.valueOf(m);
      denominator = BigInteger.ZERO.flipBit(-k); } 
    else {
      numerator   = BigInteger.valueOf(m)
        .multiply(BigInteger.ZERO.flipBit(k));
      denominator = BigInteger.ONE; }
    return new Ratio(numerator,denominator); }

  private static final Ratio add (final Ratio q0, 
                                  final Ratio q1) {
    return Numbers.toRatio(Numbers.add(q0,q1)); } 

  private static final Ratio multiply (final Ratio q0, 
                                  final Ratio q1) {
    return Numbers.toRatio(Numbers.multiply(q0,q1)); } 

  private static final Ratio ZERO = 
    new Ratio(BigInteger.ZERO,BigInteger.ONE);

  //--------------------------------------------------------------

  private Ratio _sum;

  //--------------------------------------------------------------
  // start with only immediate needs

  @Override
  public final double doubleValue () { 
    return _sum.doubleValue(); }

  @Override
  public final Accumulator clear () { _sum = ZERO; return this; }

  @Override
  public final Accumulator add (final double z) { 
    _sum = add(_sum,toRatio(z));
    return this; }

//  @Override
//  public final Accumulator addAll (final double[] z)  {
//    for (final double zi : z) { 
//      _sum = add(_sum,toRatio(zi)); }
//    return this; }

  @Override
  public final Accumulator addProduct (final double z0,
                                       final double z1) { 
    _sum = add(_sum,multiply(toRatio(z0),toRatio(z1)));
    return this; }

//@Override
//public final Accumulator addProducts (final double[] z0,
//                                        final double[] z1)  {
//    final int n = z0.length;
//    assert n == z1.length;
//    for (int i=0;i<n;i++) { 
//      _sum = add(_sum,multiply(toRatio(z0[i]),toRatio(z1[i]))); }
//    return this; }
  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private RatioSum () { super(); clear(); }

  public static final RatioSum make () {
    return new RatioSum(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
