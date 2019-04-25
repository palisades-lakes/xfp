package xfp.java.numbers;

import static xfp.java.numbers.Numbers.hiBit;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import xfp.java.exceptions.Exceptions;

/** Ratios of {@link BigInteger}.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-22
 */

public final class Rational extends Number
implements Comparable<Rational> {

  private static final long serialVersionUID = 1L;

  //--------------------------------------------------------------
  // instance fields and methods
  //--------------------------------------------------------------

  private final BigInteger _numerator;
  public final BigInteger numerator () { return _numerator; }
  private final BigInteger _denominator;
  public final BigInteger denominator () { return _denominator; }

  //--------------------------------------------------------------

  private static final boolean isNegative (final BigInteger i) {
    return 0 > i.signum(); }

  private static final boolean isZero (final BigInteger i) {
    return 0 == i.signum(); }

  public final boolean isZero () { return isZero(numerator()); }

  //  private static final boolean isOne (final BigInteger i) {
  //    return BigInteger.ONE.equals(i); }

  private static final boolean isOne (final BigInteger n,
                                      final BigInteger d) {
    return n.equals(d); }

  public final boolean isOne () { 
    return isOne(numerator(),denominator()); }

  //--------------------------------------------------------------

  private static final Rational reduced (final BigInteger n,
                                         final BigInteger d) {
    assert 0 != d.signum();
    
    if (d.signum() < 0) { return reduced(n.negate(),d.negate()); }

    if (n == BigInteger.ZERO) { return ZERO; }

    // TODO: any value in this test?
    if ((n == BigInteger.ONE) || (d == BigInteger.ONE)) {
      return new Rational(n,d); }

    final BigInteger gcd = n.gcd(d);
    if (gcd.compareTo(BigInteger.ONE) > 0) {
      return new Rational(n.divide(gcd),d.divide(gcd)); } 

    return new Rational(n,d); }

  //--------------------------------------------------------------

  public final Rational negate () {
    if (isZero()) { return this; }
    return valueOf(numerator().negate(),denominator()); }

  public final Rational reciprocal () {
    assert !isZero(numerator());
    return valueOf(denominator(),numerator()); }

  //--------------------------------------------------------------

  private final Rational add (final BigInteger n,
                              final BigInteger d) {
    return valueOf(
      numerator().multiply(d).add(n.multiply(denominator())),
      denominator().multiply(d)); }

  public final Rational add (final Rational q) {
    if (isZero()) { return q; }
    if (q.isZero()) { return this; }
    return add(q.numerator(),q.denominator()); }

  public final Rational add (final double z) {
    assert Double.isFinite(z);
    final boolean s = Doubles.nonNegative(z);
    final int e = Doubles.exponent(z);
    final long t = Doubles.significand(z);
    final BigInteger u = BigInteger.valueOf(s ? t : -t);
    final BigInteger du = denominator().multiply(u);
    if (0 <= e) {
      return 
        valueOf(
          numerator().add(du.shiftLeft(e)),
          denominator()); }
    return 
      valueOf(
        numerator().shiftLeft(-e).add(du),
        denominator().shiftLeft(-e)); }

  //--------------------------------------------------------------

  public final Rational subtract (final Rational q) {
    if (isZero()) { return q.negate(); }
    if (q.isZero()) { return this; }
    return add(q.numerator().negate(),q.denominator()); }

  public final Rational abs () {
    // TODO: direct signum
    final int s = numerator().signum();
    if (0<=s) { return this; }
    return negate(); }

  //--------------------------------------------------------------

  private final Rational multiply (final BigInteger n,
                                   final BigInteger d) {
    return 
      valueOf(
        numerator().multiply(n), 
        denominator().multiply(d)); }

  public final Rational multiply (final Rational q) {
    if (isZero() ) { return ZERO; }
    if (q.isZero()) { return ZERO; }
    if (q.isOne()) { return this; }
    if (isOne()) { return q; }
    return multiply(q.numerator(),q.denominator()); }

  //--------------------------------------------------------------

  public final Rational add2 (final double z) { 
    assert Double.isFinite(z);
    final boolean s = Doubles.nonNegative(z);
    final int e = 2*Doubles.exponent(z);
    final long t = (s ? 1L : -1L) * Doubles.significand(z);
    final BigInteger tt = BigInteger.valueOf(t);
    final BigInteger n = tt.multiply(tt);
    final BigInteger dn = denominator().multiply(n);
    if (0 <= e) {
      return valueOf(
        numerator().add(dn.shiftLeft(e)),
        denominator()); }
    return valueOf(
      numerator().shiftLeft(-e).add(dn),
      denominator().shiftLeft(-e)); }

  //--------------------------------------------------------------

  public final Rational addProduct (final double z0,
                                    final double z1) { 
    assert Double.isFinite(z0);
    assert Double.isFinite(z1);
    final boolean s = 
      ! (Doubles.nonNegative(z0) ^ Doubles.nonNegative(z1));
    final int e = Doubles.exponent(z0) + Doubles.exponent(z1);
    final long t0 = (s ? 1L : -1L) * Doubles.significand(z0);
    final long t1 = Doubles.significand(z1);
    final BigInteger n = 
      BigInteger.valueOf(t0).multiply(BigInteger.valueOf(t1));
    final BigInteger dn = denominator().multiply(n);
    if (0 <= e) {
      return valueOf(
        numerator().add(dn.shiftLeft(e)),
        denominator()); }
    return valueOf(
      numerator().shiftLeft(-e).add(dn),
      denominator().shiftLeft(-e)); }

  //--------------------------------------------------------------
  // Number methods
  //--------------------------------------------------------------

  /** Returns the low order bits of the truncated quotient.
   * 
   * TODO: should it really truncate or round instead? Or
   * should there be more explicit round, floor, ceil, etc.?
   */
  @Override
  public final int intValue () {
    return bigIntegerValue().intValue(); }

  /** Returns the low order bits of the truncated quotient.
   * 
   * TODO: should it really truncate or round instead? Or
   * should there be more explicit round, floor, ceil, etc.?
   */
  @Override
  public final long longValue () {
    return bigIntegerValue().longValue(); }

  /** Returns the truncated quotient.
   * 
   * TODO: should it round instead? Or
   * should there be more explicit round, floor, ceil, etc.?
   */
  public final BigInteger bigIntegerValue () { 
    return numerator().divide(denominator()); }

  //--------------------------------------------------------------
  /** Half-even rounding from {@link BigInteger} ratio to 
   * <code>float</code>.
   * @param n numerator
   * @param d positive denominator
   * @return closest half-even rounded <code>float</code> to n / d.
   */

  @Override
  public final float floatValue () { 
    final int s = numerator().signum();
    if (s == 0) { return 0.0F; }
    final boolean neg = (s < 0);
    final BigInteger n0 = (neg ? numerator().negate() : numerator());
    final BigInteger d0 = denominator();

    // choose exponent, and shift numerator and denominator so
    // quotient has the right number of bits.
    final int e0 = hiBit(n0) - hiBit(d0) - 1;
    final boolean small = (e0 > 0);
    final BigInteger n1 = small ? n0 : n0.shiftLeft(-e0);
    final BigInteger d1 = small ? d0.shiftLeft(e0) : d0;

    // ensure numerator is less than 2x denominator
    final BigInteger d11 = d1.shiftLeft(1);
    final BigInteger d2;
    final int e2;
    if (n1.compareTo(d11) < 0) { d2 = d1; e2 = e0;}
    else { d2 = d11; e2 = e0 + 1; }

    // check for out of range
    if (e2 > Float.MAX_EXPONENT) {
      return neg ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY; }
    if (e2 < Floats.MINIMUM_SUBNORMAL_EXPONENT) {
      return neg ? -0.0F : 0.0F; }

    // subnormal numbers need slightly different handling
    final boolean sub = (e2 < Float.MIN_EXPONENT);
    final int e3 = sub ? Float.MIN_EXPONENT : e2;
    final BigInteger d3 = sub ? d2.shiftLeft(e3-e2) : d2;
    final BigInteger n3 = n1.shiftLeft(Floats.STORED_SIGNIFICAND_BITS);
    final int e4 = e3 - Floats.STORED_SIGNIFICAND_BITS;
    final BigInteger[] qr = n3.divideAndRemainder(d3);

    // round down or up? <= implies half-even (?)
    final boolean down = (qr[1].shiftLeft(1).compareTo(d3) < 0);
    final int q4 = qr[0].intValueExact() + (down ? 0 : 1 );

    // handle carry if needed after round up
    final boolean carry = (hiBit(q4) > Floats.SIGNIFICAND_BITS);
    final int q = carry ? q4 >>> 1 : q4;
    final int e = (sub ? (carry ? e4 : e4 - 1) : (carry ? e4 + 1 : e4));
    return Floats.makeFloat(neg,e,q); }

  //--------------------------------------------------------------
  /** Half-even rounding from {@link BigInteger} ratio to 
   * <code>double</code>.
   * @param n numerator
   * @param d positive denominator
   * @return closest half-even rounded <code>double</code> to n / d.
   */

  @Override
  public final double doubleValue () { 
    final int s = numerator().signum();
    if (s == 0) { return 0.0; }
    final boolean neg = (s < 0);
    final BigInteger n0 = (neg ? numerator().negate() : numerator());
    final BigInteger d0 = denominator();

    // choose exponent, and shift numerator and denominator so
    // quotient has the right number of bits.
    final int e0 = hiBit(n0) - hiBit(d0) - 1;
    final boolean small = (e0 > 0);
    final BigInteger n1 = small ? n0 : n0.shiftLeft(-e0);
    final BigInteger d1 = small ? d0.shiftLeft(e0) : d0;

    // ensure numerator is less than 2x denominator
    final BigInteger d11 = d1.shiftLeft(1);
    final BigInteger d2;
    final int e2;
    if (n1.compareTo(d11) < 0) { d2 = d1; e2 = e0;}
    else { d2 = d11; e2 = e0 + 1; }

    // check for out of range
    if (e2 > Double.MAX_EXPONENT) {
      return neg ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY; }
    if (e2 < Doubles.MINIMUM_SUBNORMAL_EXPONENT) {
      return neg ? -0.0 : 0.0; }

    // subnormal numbers need slightly different handling
    final boolean sub = (e2 < Double.MIN_EXPONENT);
    final int e3 = sub ? Double.MIN_EXPONENT : e2;
    final BigInteger d3 = sub ? d2.shiftLeft(e3-e2) : d2;
    final BigInteger n3 = n1.shiftLeft(Doubles.STORED_SIGNIFICAND_BITS);
    final int e4 = e3 - Doubles.STORED_SIGNIFICAND_BITS;
    final BigInteger[] qr = n3.divideAndRemainder(d3);

    // round down or up? <= implies half-even (?)
    final boolean down = (qr[1].shiftLeft(1).compareTo(d3) < 0);
    final long q4 = qr[0].longValueExact() + (down ? 0L : 1L );

    // handle carry if needed after round up
    final boolean carry = (hiBit(q4) > Doubles.SIGNIFICAND_BITS);
    final long q = carry ? q4 >>> 1 : q4;
    final int e = (sub ? (carry ? e4 : e4 - 1) : (carry ? e4 + 1 : e4));
    return Doubles.makeDouble(neg,e,q); }

  //--------------------------------------------------------------
  // Comparable methods
  //--------------------------------------------------------------

  @Override
  public final int compareTo (final Rational o) {
    final BigInteger n0d1 = numerator().multiply(o.denominator());
    final BigInteger n1d0 = o.numerator().multiply(denominator());
    return n0d1.compareTo(n1d0); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  public final boolean equals (final Rational q) {
    if (this == q) { return true; }
    if (null == q) { return false; }
    final BigInteger n0 = numerator(); 
    final BigInteger d0 = denominator(); 
    final BigInteger n1 = q.numerator(); 
    final BigInteger d1 = q.denominator(); 
    return n0.multiply(d1).equals(n1.multiply(d0)); }

  @Override
  public boolean equals (final Object o) {
    if (!(o instanceof Rational)) { return false; }
    return equals((Rational) o); }

  @Override
  public int hashCode () {
    return Objects.hash(numerator(),denominator()); }

  @Override
  public final String toString () {
    return 
      "(" + numerator().toString(0x10) 
      + " / " + denominator().toString(0x10) 
      + ")"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private Rational (final BigInteger numerator,
                    final BigInteger denominator) {
    super();
    assert 1 == denominator.signum() :
      numerator.toString(0x10) 
      + "\n"
      + denominator.toString(0x10);
    _numerator = numerator;
    _denominator = denominator; }

  //--------------------------------------------------------------

  public static final Rational valueOf (final BigInteger n,
                                        final BigInteger d) {
    assert 0 != d.signum();
    if (isNegative(d)) {
      return valueOf(n.negate(),d.negate()); }
    return reduced(n,d); }

  public static final Rational valueOf (final long n,
                                        final long d) {
    assert 0L != d;
    if (0L > d) { return valueOf(-n,-d); }
    return valueOf(
      BigInteger.valueOf(n),BigInteger.valueOf(d)); }

  public static final Rational valueOf (final int n,
                                        final int d) {
    assert 0 != d;
    if (0 > d) { return valueOf(-n,-d); }
    return valueOf(BigInteger.valueOf(n),BigInteger.valueOf(d)); }

  //--------------------------------------------------------------

  private static final Rational valueOf (final boolean nonNegative,
                                         final int e,
                                         final long t)  {
    if (0L == t) { return ZERO; }
    final BigInteger n0 = BigInteger.valueOf(t);
    final BigInteger n1 = nonNegative ? n0 : n0.negate();
    if (0 == e) {  return valueOf(n1); }
    if (0 < e) { return valueOf(n1.shiftLeft(e)); }
    return valueOf(n1,BigInteger.ZERO.setBit(-e)); } 

  public static final Rational valueOf (final double x)  {
    return valueOf(
      Doubles.nonNegative(x),
      Doubles.exponent(x),
      Doubles.significand(x)); } 

  //--------------------------------------------------------------

  private static final Rational valueOf (final boolean nonNegative,
                                         final int e,
                                         final int t)  {
    if (0 == t) { return ZERO; }
    final BigInteger n0 = BigInteger.valueOf(t);
    final BigInteger n1 = nonNegative ? n0 : n0.negate();
    if (0 == e) {  return valueOf(n1); }
    if (0 < e) { return valueOf(n1.shiftLeft(e)); }
    return valueOf(n1,BigInteger.ZERO.setBit(-e)); } 

  public static final Rational valueOf (final float x)  {
    return valueOf(
      Floats.nonNegative(x),
      Floats.exponent(x),
      Floats.significand(x)); } 

  //--------------------------------------------------------------

  public static final Rational valueOf (final byte x)  {
    return valueOf(BigInteger.valueOf(x), BigInteger.ONE); }

  public static final Rational valueOf (final short x)  {
    return valueOf(BigInteger.valueOf(x), BigInteger.ONE); }

  public static final Rational valueOf (final int x)  {
    return valueOf(BigInteger.valueOf(x), BigInteger.ONE); }

  public static final Rational valueOf (final long x)  {
    return valueOf(BigInteger.valueOf(x), BigInteger.ONE); }

  //--------------------------------------------------------------

  public static final Rational valueOf (final Double x)  {
    return valueOf(x.doubleValue()); }

  public static final Rational valueOf (final Float x)  {
    return valueOf(x.floatValue()); }

  public static final Rational valueOf (final Byte x)  {
    return valueOf(x.byteValue()); }

  public static final Rational valueOf (final Short x)  {
    return valueOf(x.shortValue()); }

  public static final Rational valueOf (final Integer x)  {
    return valueOf(x.intValue()); }

  public static final Rational valueOf (final Long x)  {
    return valueOf(x.longValue()); }

  public static final Rational valueOf (final BigDecimal x)  {
    throw Exceptions.unsupportedOperation(null,"valueOf",x); }
  //    return valueOf(x, BigInteger.ONE); }

  public static final Rational valueOf (final BigInteger x)  {
    return valueOf(x, BigInteger.ONE); }

  public static final Rational valueOf (final Number x)  {
    if (x instanceof Rational) { return (Rational) x; }
    if (x instanceof Double) { return valueOf((Double) x); }
    if (x instanceof Float) { return valueOf((Float) x); }
    if (x instanceof Byte) { return valueOf((Byte) x); }
    if (x instanceof Short) { return valueOf((Short) x); }
    if (x instanceof Integer) { return valueOf((Integer) x); }
    if (x instanceof Long) { return valueOf((Long) x); }
    if (x instanceof BigInteger) { return valueOf((BigInteger) x); }
    if (x instanceof BigDecimal) { return valueOf((BigDecimal) x); }
    throw Exceptions.unsupportedOperation(null,"valueOf",x); }

  public static final Rational valueOf (final Object x)  {
    return valueOf((Number) x); }

  //--------------------------------------------------------------

  public static final Rational ZERO = 
    new Rational(BigInteger.ZERO,BigInteger.ONE);

  public static final Rational ONE = 
    new Rational(BigInteger.ONE,BigInteger.ONE);

  public static final Rational TWO = 
    new Rational(BigInteger.TWO,BigInteger.ONE);

  public static final Rational TEN = 
    new Rational(BigInteger.TEN,BigInteger.ONE);

  public static final Rational MINUS_ONE = ONE.negate();

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
