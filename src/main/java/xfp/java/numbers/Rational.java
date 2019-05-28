package xfp.java.numbers;

import static xfp.java.numbers.Numbers.hiBit;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import xfp.java.exceptions.Exceptions;

/** Ratios of {@link UnNatural}.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-05-28
 */

public final class Rational extends Number
implements Ringlike<Rational> {

  private static final long serialVersionUID = 1L;

  //--------------------------------------------------------------
  // instance fields and methods
  //--------------------------------------------------------------

  private final boolean _nonNegative;
  public final boolean nonNegative () { return _nonNegative; }
  private final UnNatural _numerator;
  public final UnNatural numerator () { return _numerator; }
  private final UnNatural _denominator;
  public final UnNatural denominator () { return _denominator; }

  //--------------------------------------------------------------

  private static final boolean isZero (final UnNatural i) {
    return i.isZero(); }

  public final boolean isZero () { return isZero(numerator()); }

  private static final boolean isOne (final boolean nonNegative,
                                      final UnNatural n,
                                      final UnNatural d) {
    return nonNegative && n.equals(d); }

  public final boolean isOne () {
    return isOne(nonNegative(),numerator(),denominator()); }

  //--------------------------------------------------------------

  @Override
  public final Rational negate () {
    if (isZero()) { return this; }
    return valueOf(! nonNegative(), numerator(), denominator()); }

  public final Rational reciprocal () {
    assert !isZero(numerator());
    return valueOf(nonNegative(),denominator(),numerator()); }

  //--------------------------------------------------------------

  private static final Rational add (final boolean p0,
                                     final UnNatural n0,
                                     final UnNatural d0,
                                     final boolean p1,
                                     final UnNatural n1,
                                     final UnNatural d1) {
    final UnNatural n0d1 = n0.multiply(d1);
    final UnNatural n1d0 = n1.multiply(d0);
    final boolean p;
    final UnNatural n;
    if (p0) {
      if (p1) { n = n0d1.add(n1d0); p = true; }
      else {
        final int c = n0d1.compareTo(n1d0);
        if (0 == c) { return ZERO; }
        if (0 < c) { n = n0d1.subtract(n1d0); p = true; }
        else { n = n1d0.subtract(n0d1); p = false; } } }
    else { 
      if (p1) {
        final int c = n1d0.compareTo(n0d1);
        if (0 == c) { return ZERO; }
        if (0 < c) { n = n1d0.subtract(n0d1); p = true; }
        else { n = n0d1.subtract(n1d0); p = false; } }
      else { n = n0d1.add(n1d0); p = false; } } 
    final UnNatural d = d0.multiply(d1);
    return valueOf(p,n,d); }

  private final Rational add (final boolean p,
                              final UnNatural n,
                              final UnNatural d) {
    return add(nonNegative(),numerator(),denominator(),p,n,d); }

  @Override
  public final Rational add (final Rational q) {
    if (isZero()) { return q; }
    if (q.isZero()) { return this; }
    return add(q.nonNegative(),q.numerator(),q.denominator()); }

  //--------------------------------------------------------------

  public final Rational add (final double z) {
    assert Double.isFinite(z);
    return add(valueOf(z)); }

  //--------------------------------------------------------------

  @Override
  public final Rational subtract (final Rational q) {
    if (isZero()) { return q.negate(); }
    if (q.isZero()) { return this; }
    return add(q.negate()); }

  @Override
  public final Rational abs () {
    if (nonNegative()) { return this; }
    return negate(); }

  //--------------------------------------------------------------

  private final Rational multiply (final boolean p,
                                   final UnNatural n,
                                   final UnNatural d) {
    return
      valueOf(
        ! (nonNegative() ^ p),
        numerator().multiply(n),
        denominator().multiply(d)); }

  @Override
  public final Rational multiply (final Rational q) {
    if (isZero() ) { return ZERO; }
    if (q.isZero()) { return ZERO; }
    if (q.isOne()) { return this; }
    if (isOne()) { return q; }
    return multiply(
      q.nonNegative(),q.numerator(),q.denominator()); }

  //--------------------------------------------------------------

  public final Rational add2 (final double z) {
    assert Double.isFinite(z);
    final Rational q = valueOf(z);
    return add(q.multiply(q)); }

  //--------------------------------------------------------------

  public final Rational addProduct (final double z0,
                                    final double z1) {
    assert Double.isFinite(z0);
    assert Double.isFinite(z1);
    return add(valueOf(z0).multiply(valueOf(z1))); }

  //--------------------------------------------------------------
  // Number methods
  //--------------------------------------------------------------
  /** TODO: should it truncate or round? Or
   * should there be more explicit round, floor, ceil, etc.?
   */
  @Override
  public final int intValue () {
    return bigIntegerValue().intValue(); }

  /**  TODO: should it truncate or round? Or
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
  final BigInteger x = 
    numerator().divide(denominator()).bigIntegerValue(); 
  return (nonNegative() ? x : x.negate()); }

  //--------------------------------------------------------------
  /** Half-even rounding to <code>float</code>.
   * @param n numerator
   * @param d positive denominator
   * @return closest half-even rounded <code>float</code> to n / d.
   */

  @Override
  public final float floatValue () {
    if (isZero()) { return 0.0F; }
    final boolean neg = !nonNegative();
    final UnNatural n0 = numerator();
    final UnNatural d0 = denominator();

    // choose exponent, and shift numerator and denominator so
    // quotient has the right number of bits.
    final int e0 = hiBit(n0) - hiBit(d0) - 1;
    final boolean small = (e0 > 0);
    final UnNatural n1 = small ? n0 : n0.shiftLeft(-e0);
    final UnNatural d1 = small ? d0.shiftLeft(e0) : d0;

    // ensure numerator is less than 2x denominator
    final UnNatural d11 = d1.shiftLeft(1);
    final UnNatural d2;
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
    final UnNatural d3 = sub ? d2.shiftLeft(e3-e2) : d2;
    final UnNatural n3 = n1.shiftLeft(Floats.STORED_SIGNIFICAND_BITS);
    final int e4 = e3 - Floats.STORED_SIGNIFICAND_BITS;
    final UnNatural[] qr = 
      n3.divideAndRemainder(d3).toArray(new UnNatural[0]);

    // round down or up? <= implies half-even (?)
    final int c = qr[1].shiftLeft(1).compareTo(d3);
    final int q4 = qr[0].intValue();
    final boolean even = (0x0 == (q4 & 0x1));
    final boolean down = (c < 0) || ((c == 0) && even);

    final int q;
    final int e;
    if (down) {
      q = q4;
      e = (sub ? e4 - 1 : e4); }
    else {
      final int q5 = q4 + 1;
      // handle carry if needed after round up
      final boolean carry = (hiBit(q5) > Floats.SIGNIFICAND_BITS);
      q = carry ? q5 >>> 1 : q5;
    e = (sub ? (carry ? e4 : e4 - 1) : (carry ? e4 + 1 : e4)); }
    return Floats.makeFloat(neg,e,q); }

  //--------------------------------------------------------------
  /** Half-even rounding to <code>double</code>.
   * @param n numerator
   * @param d positive denominator
   * @return closest half-even rounded <code>double</code> to n / d.
   */

  @Override
  public final double doubleValue () {
    if (isZero()) { return 0.0; }
    final boolean neg = !nonNegative();
    final UnNatural n0 = numerator();
    final UnNatural d0 = denominator();

    // choose exponent, and shift numerator and denominator so
    // quotient has the right number of bits.
    final int e0 = hiBit(n0) - hiBit(d0) - 1;
    final boolean small = (e0 > 0);
    final UnNatural n1 = small ? n0 : n0.shiftLeft(-e0);
    final UnNatural d1 = small ? d0.shiftLeft(e0) : d0;

    // ensure numerator is less than 2x denominator
    final UnNatural d11 = d1.shiftLeft(1);
    final UnNatural d2;
    final int e2;
    if (n1.compareTo(d11) < 0) { d2 = d1; e2 = e0;}
    else { d2 = d11; e2 = e0 + 1; }

    // check for out of range
    if (e2 > Double.MAX_EXPONENT) {
      return neg ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY; }
    if (e2 < Doubles.MINIMUM_SUBNORMAL_EXPONENT) {
      return (neg ? -0.0 : 0.0); }

    // subnormal numbers need slightly different handling
    final boolean sub = (e2 < Double.MIN_EXPONENT);
    final int e3 = sub ? Double.MIN_EXPONENT : e2;
    final UnNatural d3 = sub ? d2.shiftLeft(e3-e2) : d2;
    final UnNatural n3 = n1.shiftLeft(Doubles.STORED_SIGNIFICAND_BITS);
    final int e4 = e3 - Doubles.STORED_SIGNIFICAND_BITS;
    final UnNatural[] qr = 
      n3.divideAndRemainder(d3).toArray(new UnNatural[0]);

    // round down or up? <= implies half-even (?)
    final int c = qr[1].shiftLeft(1).compareTo(d3);
    final long q4 = qr[0].longValue();
    final boolean even = (0x0L == (q4 & 0x1L));
    final boolean down = (c < 0) || ((c == 0) && even);

    final long q;
    final int e;
    if (down) {
      q = q4;
      e = (sub ? e4 - 1 : e4); }
    else {
      final long q5 = q4 + 1;
      // handle carry if needed after round up
      final boolean carry = (hiBit(q5) > Doubles.SIGNIFICAND_BITS);
      q = carry ? q5 >>> 1 : q5;
    e = (sub ? (carry ? e4 : e4 - 1) : (carry ? e4 + 1 : e4)); }
    return Doubles.makeDouble(neg,e,q); }

  //--------------------------------------------------------------
  // Comparable methods
  //--------------------------------------------------------------

  @Override
  public final int compareTo (final Rational q) {
    if (nonNegative() && (! q.nonNegative())) { return 1; }
    if ((! nonNegative()) && q.nonNegative()) { return -1; }
    // same signs
    final UnNatural n0d1 = numerator().multiply(q.denominator());
    final UnNatural n1d0 = q.numerator().multiply(denominator());
    final int c = n0d1.compareTo(n1d0); 
    return (nonNegative() ? c : -c); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  public final boolean equals (final Rational q) {
    if (this == q) { return true; }
    if (null == q) { return false; }
    if (nonNegative() != q.nonNegative()) { return false; }
    final UnNatural n0 = numerator();
    final UnNatural d0 = denominator();
    final UnNatural n1 = q.numerator();
    final UnNatural d1 = q.denominator();
    return n0.multiply(d1).equals(n1.multiply(d0)); }

  @Override
  public boolean equals (final Object o) {
    if (!(o instanceof Rational)) { return false; }
    return equals((Rational) o); }

  @Override
  public int hashCode () {
    return Objects.hash(
      Boolean.valueOf(nonNegative()),numerator(),denominator()); }

  @Override
  public final String toString () {
    return
      (nonNegative() ? "" : "-")
      + "(" 
      + numerator().toString(0x10)
      + " / " + denominator().toString(0x10)
      + ")"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private Rational (final boolean nonNegative,
                    final UnNatural numerator,
                    final UnNatural denominator) {
    //    super();
    _nonNegative = nonNegative;
    _numerator = numerator;
    _denominator = denominator; }

  //--------------------------------------------------------------

  private static final Rational reduced (final boolean nonNegative,
                                         final UnNatural n,
                                         final UnNatural d) {
    if (n == UnNatural.ZERO) { return ZERO; }
    // TODO: any value in this test?
    if ((n == UnNatural.ONE) || (d == UnNatural.ONE)) {
      return new Rational(nonNegative,n,d); }
    final UnNatural gcd = n.gcd(d);
    if (gcd.compareTo(UnNatural.ONE) > 0) {
      return 
        new Rational(nonNegative,n.divide(gcd),d.divide(gcd)); }
    return new Rational(nonNegative,n,d); }

  //--------------------------------------------------------------

  public static final Rational valueOf (final boolean nonNegative,
                                        final UnNatural n,
                                        final UnNatural d) {
    return reduced(nonNegative,n,d); }

  public static final Rational valueOf (final boolean nonNegative,
                                        final UnNatural n) {
    return reduced(nonNegative,n,UnNatural.ONE); }

  // TODO: compute gcd and reduce longs

  public static final Rational valueOf (final long n,
                                        final long d) {
    assert 0L != d;
    if (0L > d) { return valueOf(-n,-d); }
    final boolean nonNegative = (0L <= n);
    return valueOf(
      nonNegative,
      UnNatural.valueOf(nonNegative ? n : -n),
      UnNatural.valueOf(d)); }

  // TODO: compute gcd and reduce ints
  public static final Rational valueOf (final int n,
                                        final int d) {
    assert 0 != d;
    if (0 > d) { return valueOf(-n,-d); }
    final boolean nonNegative = (0 <= n);
    return valueOf(
      nonNegative,
      UnNatural.valueOf(nonNegative ? n : -n),
      UnNatural.valueOf(d)); }

  //--------------------------------------------------------------

  private static final Rational valueOf (final boolean nonNegative,
                                         final long t,
                                         final int e)  {
    if (0L == t) { return ZERO; }
    assert 0L < t;
    final UnNatural n0 = UnNatural.valueOf(t);
    if (0 == e) {  return valueOf(nonNegative,n0); }
    if (0 < e) { return valueOf(nonNegative,n0.shiftLeft(e)); }
    return valueOf(nonNegative,n0,UnNatural.ZERO.setBit(-e)); }

  public static final Rational valueOf (final double x)  {
    return valueOf(
      Doubles.nonNegative(x),
      Doubles.significand(x),
      Doubles.exponent(x)); }

  //--------------------------------------------------------------

  private static final Rational valueOf (final boolean nonNegative,
                                         final int e,
                                         final int t)  {
    if (0 == t) { return ZERO; }
    assert 0 < t;
    final UnNatural n0 = UnNatural.valueOf(t);
    if (0 == e) {  return valueOf(nonNegative,n0); }
    if (0 < e) { return valueOf(nonNegative,n0.shiftLeft(e)); }
    return valueOf(nonNegative,n0,UnNatural.ZERO.setBit(-e)); }

  public static final Rational valueOf (final float x)  {
    return valueOf(
      Floats.nonNegative(x),
      Floats.exponent(x),
      Floats.significand(x)); }

  //--------------------------------------------------------------

  public static final Rational valueOf (final byte x)  {
    final boolean nonNegative = (0 <= x);
    return valueOf(
      nonNegative,
      UnNatural.valueOf(nonNegative ? x : -x)); }

  public static final Rational valueOf (final short x)  {
    final boolean nonNegative = (0 <= x);
    return valueOf(
      nonNegative,
      UnNatural.valueOf(nonNegative ? x : -x)); }

  public static final Rational valueOf (final int x)  {
    final boolean nonNegative = (0 <= x);
    return valueOf(
      nonNegative,
      UnNatural.valueOf(nonNegative ? x : -x)); }

  public static final Rational valueOf (final long x)  {
    final boolean nonNegative = (0 <= x);
    return valueOf(
      nonNegative,
      UnNatural.valueOf(nonNegative ? x : -x)); }

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
  //    return valueOf(x, UnNatural.ONE); }

  public static final Rational valueOf (final UnNatural x)  {
    return valueOf(true,x); }

  public static final Rational valueOf (final BigInteger n,
                                        final BigInteger d) {
    return valueOf(
      0 <= (n.signum()*d.signum()),
      UnNatural.valueOf(n.abs()),
      UnNatural.valueOf(d.abs())); }

  public static final Rational valueOf (final BigInteger n) {
    return valueOf(
      0 <= (n.signum()),
      UnNatural.valueOf(n.abs())); }

  public static final Rational valueOf (final Number x)  {
    if (x instanceof Rational) { return (Rational) x; }
    if (x instanceof Double) { return valueOf((Double) x); }
    if (x instanceof Float) { return valueOf((Float) x); }
    if (x instanceof Byte) { return valueOf((Byte) x); }
    if (x instanceof Short) { return valueOf((Short) x); }
    if (x instanceof Integer) { return valueOf((Integer) x); }
    if (x instanceof Long) { return valueOf((Long) x); }
    if (x instanceof UnNatural) { return valueOf((UnNatural) x); }
    if (x instanceof BigInteger) { return valueOf((BigInteger) x); }
    if (x instanceof BigDecimal) { return valueOf((BigDecimal) x); }
    throw Exceptions.unsupportedOperation(
      Rational.class,"valueOf",x); }

  public static final Rational valueOf (final Object x)  {
    return valueOf((Number) x); }

  //--------------------------------------------------------------

  public static final Rational ZERO =
    new Rational(true,UnNatural.ZERO,UnNatural.ONE);

  public static final Rational ONE =
    new Rational(true,UnNatural.ONE,UnNatural.ONE);

  public static final Rational TWO =
    new Rational(true,UnNatural.TWO,UnNatural.ONE);

  public static final Rational TEN =
    new Rational(true,UnNatural.TEN,UnNatural.ONE);

  public static final Rational MINUS_ONE = ONE.negate();

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
