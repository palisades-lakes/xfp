package xfp.java.accumulators;

import java.math.BigInteger;

import com.upokecenter.numbers.EInteger;
import com.upokecenter.numbers.ERational;

import xfp.java.Classes;

/** Naive sum of <code>double</code> values with ERational 
 * accumulator (for testing).
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-08
 */
public final class RationalSum implements Accumulator<RationalSum> {

  //--------------------------------------------------------------
  // class field and methods
  //--------------------------------------------------------------

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

  public final BigInteger[] toRatio (final double x) 
    throws IllegalArgumentException {

    if (! Double.isFinite(x)) {
      throw new IllegalArgumentException(
        Classes.className(this)  + " cannot handle "+ x); }

    final BigInteger numerator;
    final BigInteger denominator;

    // compute m and k such that value = m * 2^k
    final long bits     = Double.doubleToLongBits(x);
    final long sign     = bits & 0x8000000000000000L;
    final long exponent = bits & 0x7ff0000000000000L;
    long m              = bits & 0x000fffffffffffffL;
    if (exponent == 0) { // subnormal
      if (0L == m) {
        numerator   = BigInteger.ZERO;
        denominator = BigInteger.ONE; }
      else {
        if (sign != 0L) { m = -m; }
        numerator   = BigInteger.valueOf(m);
        denominator = BigInteger.ZERO.flipBit(1074); } }
    else { // normal
      // add the implicit most significant bit
      m |= 0x0010000000000000L; 
      if (sign != 0L) { m = -m; }
      int k = ((int) (exponent >> 52)) - 1075;
      while (((m & 0x001ffffffffffffeL) != 0L) 
        &&
        ((m & 0x1L) == 0L)) {
        m >>= 1; 
        ++k; }
      if (k < 0) { 
        numerator   = BigInteger.valueOf(m);
        denominator = BigInteger.ZERO.flipBit(-k); } 
      else {
        numerator   = BigInteger.valueOf(m)
          .multiply(BigInteger.ZERO.flipBit(k));
        denominator = BigInteger.ONE; } } 

    return new BigInteger[]{ numerator, denominator}; }

  //--------------------------------------------------------------
  // instance fields and methods
  //--------------------------------------------------------------

  private BigInteger _numerator;
  public final BigInteger numerator () { return _numerator; }
  private BigInteger _denominator;
  public final BigInteger denominator () { return _denominator; }

  //--------------------------------------------------------------

  public final RationalSum reduce () {
    if (_numerator == BigInteger.ZERO) {
      _denominator = BigInteger.ONE; }
    else {
      final BigInteger gcd = _numerator.gcd(_denominator);
      if (gcd.compareTo(BigInteger.ONE) > 0) {
        _numerator = _numerator.divide(gcd);
        _denominator = _denominator.divide(gcd); } }
    return this; }

  private final RationalSum add (final BigInteger n,
                                 final BigInteger d) {
    if (BigInteger.ZERO.equals(_numerator)) {
      _numerator = n;
      _denominator = d; }
    else {
      _numerator = 
        _numerator.multiply(d).add(n.multiply(_denominator));
      _denominator = _denominator.multiply(d); }
    return this; }

  //--------------------------------------------------------------
  // Accumulator interface
  //--------------------------------------------------------------
  // start with only immediate needs

  @Override
  public final double doubleValue () { 
    final EInteger n = 
      EInteger.FromBytes(_numerator.toByteArray(), false);
    final EInteger d = 
      EInteger.FromBytes(_denominator.toByteArray(), false);
    return 
      ERational.Create(n,d).ToDouble(); }

  @Override
  public final RationalSum clear () { 
    _numerator = BigInteger.ZERO;
    _denominator = BigInteger.ONE;
    return this; }

  @Override
  public final RationalSum add (final double z) { 
    // would be nice to have multiple value return...
    final BigInteger[] nd = toRatio(z);
    return add(nd[0],nd[1])
      .reduce()
      ; }

  //  @Override
  //  public final RationalSum addAll (final double[] z)  {
  //    for (final double zi : z) { 
  //      _sum = _sum.Add(ERational.FromDouble(zi)); }
  //    return this; }

  @Override
  public final RationalSum addProduct (final double z0,
                                       final double z1) { 
    final BigInteger[] nd0 = toRatio(z0);
    final BigInteger[] nd1 = toRatio(z1);
    return add(
      nd0[0].multiply(nd1[0]),
      nd0[1].multiply(nd1[1]))
      .reduce()
      ; }

  //@Override
  //public final RationalSum addProducts (final double[] z0,
  //                                      final double[] z1)  {
  //    final int n = z0.length;
  //    assert n == z1.length;
  //    for (int i=0;i<n;i++) { 
  //      sum = _sum.Add(
  //        ERational.FromDouble(z0[i])
  //        .Multiply(ERational.FromDouble(z1[i])));}
  //    return this; }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final String toString () {
    return 
      "(" + _numerator.toString(0x10) 
      + " / " + _denominator.toString(0x10) 
      + ")"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private RationalSum () { super(); clear(); }

  public static final RationalSum make () {
    return new RationalSum(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
