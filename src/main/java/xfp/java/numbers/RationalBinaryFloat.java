package xfp.java.numbers;

import static xfp.java.numbers.Numbers.hiBit;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import xfp.java.exceptions.Exceptions;

/** Representing a rational number as a ratio of 
 * {@link BigInteger} times 2 to a <code>long</code> exponent.
 * 
 * The idea is that most data will start as <code>double</code>;
 * extracting the resulting powers of 2 from the numerator and
 * denominator should keep the BigIntegers smaller, and make
 * arithmetic on them faster.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-27
 */

public final class RationalBinaryFloat 
extends Number
implements Comparable<RationalBinaryFloat> {

  private static final long serialVersionUID = 1L;

  //--------------------------------------------------------------
  // instance fields and methods
  //--------------------------------------------------------------

  private final BigInteger _numerator;
  public final BigInteger numerator () { return _numerator; }
  private final BigInteger _denominator;
  public final BigInteger denominator () { return _denominator; }

  private final int _exponent;
  public final int exponent () { return _exponent; }

  //--------------------------------------------------------------

//  private static final boolean isNegative (final BigInteger i) {
//    return 0 > i.signum(); }

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

  private static final RationalBinaryFloat 
  reduced (final BigInteger n,
           final BigInteger d,
           final int e) {

    if (d.signum() < 0) { 
      return reduced(n.negate(),d.negate(),e); }
    if (n == BigInteger.ZERO) { return ZERO; }

    final int en = Numbers.loBit(n);
    final int ed = Numbers.loBit(d);
    final BigInteger n0 = n.shiftRight(en-1);
    final BigInteger d0 = d.shiftRight(ed-1);
    final int e0 = e + en - ed;

    final BigInteger gcd = n.gcd(d);
    // TODO: any value in this test?
    if (gcd.compareTo(BigInteger.ONE) > 0) {
      return 
        new RationalBinaryFloat(n0.divide(gcd),d0.divide(gcd),e0); } 

    return new RationalBinaryFloat(n0,d0,e0); }

  //--------------------------------------------------------------

  public final RationalBinaryFloat negate () {
    if (isZero()) { return this; }
    return valueOf(numerator().negate(),denominator(),exponent()); }

  public final RationalBinaryFloat reciprocal () {
    assert !isZero(numerator());
    return valueOf(denominator(),numerator(),-exponent()); }

  //--------------------------------------------------------------
  // TODO: optimize denominator == 1 cases.

  private final RationalBinaryFloat add (final BigInteger n1,
                                         final BigInteger d1,
                                         final int e1) {
    final BigInteger n0d1 = numerator().multiply(d1);
    final BigInteger n1d0 = n1.multiply(denominator());
    final BigInteger d0d1 = denominator().multiply(d1);
    final int e0 = exponent();
    if (e0 >= e1) {
      return valueOf(n0d1.shiftLeft(e0-e1).add(n1d0),d0d1,e1); }
    return valueOf(n0d1.add(n1d0.shiftLeft(e1-e0)),d0d1,e0); }

  public final RationalBinaryFloat add (final RationalBinaryFloat q) {
    if (isZero()) { return q; }
    if (q.isZero()) { return this; }
    return add(q.numerator(),q.denominator(),q.exponent()); }

  public final RationalBinaryFloat add (final double q) {
    final boolean s = Doubles.nonNegative(q);
    final int e1 = Doubles.exponent(q);
    final long t = Doubles.significand(q);
    final BigInteger n1 = BigInteger.valueOf(s ? t : -t);
    final BigInteger n0 = numerator();
    final BigInteger d0 = denominator();
    final int e0 = exponent();
    final BigInteger n1d0 = n1.multiply(denominator());
    if (e0 >= e1) {
      return valueOf(n0.shiftLeft(e0-e1).add(n1d0),d0,e1); }
    return valueOf(n0.add(n1d0.shiftLeft(e1-e0)),d0,e0); }

  //--------------------------------------------------------------

  private final RationalBinaryFloat multiply (final BigInteger n,
                                              final BigInteger d,
                                              final int e) {
    return 
      valueOf(
        numerator().multiply(n), 
        denominator().multiply(d),
        exponent() + e); }

  public final RationalBinaryFloat multiply (final RationalBinaryFloat q) {
    if (isZero() ) { return ZERO; }
    if (q.isZero()) { return ZERO; }
    if (q.isOne()) { return this; }
    if (isOne()) { return q; }
    return multiply(q.numerator(),q.denominator(),q.exponent()); }

  //--------------------------------------------------------------

  public final RationalBinaryFloat addProduct (final double z0,
                                               final double z1) { 
    final boolean s = 
      ! (Doubles.nonNegative(z0) ^ Doubles.nonNegative(z1));
    final int e1 = Doubles.exponent(z0) + Doubles.exponent(z1);
    final long t0 = (s ? 1L : -1L) * Doubles.significand(z0);
    final long t1 = Doubles.significand(z1);
    final BigInteger n = 
      BigInteger.valueOf(t0).multiply(BigInteger.valueOf(t1));
    final BigInteger n1d0 = n.multiply(denominator());
    final int e0 = exponent();
    if (e0 >= e1) {
      return valueOf(
        numerator().shiftLeft(e0-e1).add(n1d0),
        denominator(),
        e1); }
    return valueOf(
      numerator().add(n1d0.shiftLeft(e1-e0)),
      denominator(),
      e0); }

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
    return 
      numerator().divide(denominator()).shiftLeft(exponent()); }

  public final Rational rationalValue () { 
    if (0 <= exponent()) {
      return Rational.valueOf(
        numerator().shiftLeft(exponent()),denominator()); }
    return Rational.valueOf(
      numerator(),denominator().shiftLeft(-exponent())); }

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

    // TODO: fix this hack
    final boolean large = (exponent() >= 0);
    final BigInteger n00 = large ? n0.shiftLeft(exponent()) : n0;
    final BigInteger d00 = large ? d0 : d0.shiftLeft(-exponent());

    // choose exponent, and shift numerator and denominator so
    // quotient has the right number of bits.
    final int e0 = hiBit(n00) - hiBit(d00) - 1;
    final boolean small = (e0 > 0);
    final BigInteger n1 = small ? n00 : n00.shiftLeft(-e0);
    final BigInteger d1 = small ? d00.shiftLeft(e0) : d00;

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
    final boolean down = (qr[1].shiftLeft(1).compareTo(d3) <= 0);
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

    // TODO: fix this hack
    final boolean large = (exponent() >= 0);
    final BigInteger n00 = large ? n0.shiftLeft(exponent()) : n0;
    final BigInteger d00 = large ? d0 : d0.shiftLeft(-exponent());

    // choose exponent, and shift numerator and denominator so
    // quotient has the right number of bits.
    final int e0 = hiBit(n00) - hiBit(d00) - 1;
    final boolean small = (e0 > 0);
    final BigInteger n1 = small ? n00 : n00.shiftLeft(-e0);
    final BigInteger d1 = small ? d00.shiftLeft(e0) : d00;

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
    final boolean down = (qr[1].shiftLeft(1).compareTo(d3) <= 0);
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
  public final int compareTo (final RationalBinaryFloat q) {
    final BigInteger n0d1 = numerator().multiply(q.denominator());
    final BigInteger n1d0 = q.numerator().multiply(denominator());
    final int e0 = exponent();
    final int e1 = q.exponent();
    if (e0 <= e1) { return n0d1.compareTo(n1d0.shiftLeft(e1-e0)); }
    return n0d1.shiftLeft(e0-e1).compareTo(n1d0); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  public final boolean equals (final RationalBinaryFloat q) {
    if (this == q) { return true; }
    if (null == q) { return false; }
    // assuming reduced
    return 
      exponent() == q.exponent()
      &&
      numerator().equals(q._numerator)
      && 
      denominator().equals(q.denominator()); }

  @Override
  public boolean equals (final Object o) {
    if (!(o instanceof RationalBinaryFloat)) { return false; }
    return equals((RationalBinaryFloat) o); }

  @Override
  public int hashCode () {
    int h = 17;
    h = 31*h + exponent();
    h = 31*h + Objects.hash(numerator(),denominator());
    return h; }

  @Override
  public final String toString () {
    return 
      "2^" + exponent() 
      + "\n * "
      + "\n" + numerator().toString(0x10) 
      + "\n / "
      + "\n" + denominator().toString(0x10); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private RationalBinaryFloat (final BigInteger numerator,
                               final BigInteger denominator,
                               final int exponent) {
    super();
    assert 1 == denominator.signum() :
      "\nn= " + numerator.toString(0x10) 
      + "\nd= " + denominator.toString(0x10)
      + "\ne= " + exponent;
    _numerator = numerator;
    _denominator = denominator;
    _exponent = exponent; }

  //--------------------------------------------------------------

  public static final RationalBinaryFloat valueOf (final BigInteger n,
                                                   final BigInteger d,
                                                   final int e) {
    return reduced(n,d,e); }

  public static final RationalBinaryFloat valueOf (final long n,
                                                   final long d,
                                                   final int e) {
    return 
      valueOf(BigInteger.valueOf(n),BigInteger.valueOf(d),e); }

  public static final RationalBinaryFloat valueOf (final int n,
                                                   final int d, 
                                                   final int e) {
    return 
      valueOf(BigInteger.valueOf(n),BigInteger.valueOf(d),e); }

  //--------------------------------------------------------------

  private static final RationalBinaryFloat valueOf (final boolean nonNegative,
                                                    final int e,
                                                    final long t)  {
    if (0L == t) { return ZERO; }
    final BigInteger n0 = BigInteger.valueOf(t);
    final BigInteger n1 = nonNegative ? n0 : n0.negate();
    return valueOf(n1,BigInteger.ONE,e); } 

  public static final RationalBinaryFloat valueOf (final double x)  {
    return valueOf(
      Doubles.nonNegative(x),
      Doubles.exponent(x),
      Doubles.significand(x)); } 

  //--------------------------------------------------------------

  private static final RationalBinaryFloat valueOf (final boolean nonNegative,
                                                    final int e,
                                                    final int t)  {
    if (0 == t) { return ZERO; }
    final BigInteger n0 = BigInteger.valueOf(t);
    final BigInteger n1 = nonNegative ? n0 : n0.negate();
    return valueOf(n1,BigInteger.ONE,e); } 

  public static final RationalBinaryFloat valueOf (final float x)  {
    return valueOf(
      Floats.nonNegative(x),
      Floats.exponent(x),
      Floats.significand(x)); } 

  //--------------------------------------------------------------

  public static final RationalBinaryFloat valueOf (final byte x)  {
    return valueOf(BigInteger.valueOf(x), BigInteger.ONE,0); }

  public static final RationalBinaryFloat valueOf (final short x)  {
    return valueOf(BigInteger.valueOf(x), BigInteger.ONE,0); }

  public static final RationalBinaryFloat valueOf (final int x)  {
    return valueOf(BigInteger.valueOf(x), BigInteger.ONE,0); }

  public static final RationalBinaryFloat valueOf (final long x)  {
    return valueOf(BigInteger.valueOf(x), BigInteger.ONE,0); }

  //--------------------------------------------------------------

  public static final RationalBinaryFloat valueOf (final Double x)  {
    return valueOf(x.doubleValue()); }

  public static final RationalBinaryFloat valueOf (final Float x)  {
    return valueOf(x.floatValue()); }

  public static final RationalBinaryFloat valueOf (final Byte x)  {
    return valueOf(x.byteValue()); }

  public static final RationalBinaryFloat valueOf (final Short x)  {
    return valueOf(x.shortValue()); }

  public static final RationalBinaryFloat valueOf (final Integer x)  {
    return valueOf(x.intValue()); }

  public static final RationalBinaryFloat valueOf (final Long x)  {
    return valueOf(x.longValue()); }

  public static final RationalBinaryFloat valueOf (final BigDecimal x)  {
    throw Exceptions.unsupportedOperation(null,"valueOf",x); }
  //    return valueOf(x, BigInteger.ONE); }

  public static final RationalBinaryFloat valueOf (final BigInteger x)  {
    return valueOf(x, BigInteger.ONE,0); }

  public static final RationalBinaryFloat valueOf (final Number x)  {
    if (x instanceof RationalBinaryFloat) { return (RationalBinaryFloat) x; }
    if (x instanceof Double) { return valueOf((Double) x); }
    if (x instanceof Float) { return valueOf((Float) x); }
    if (x instanceof Byte) { return valueOf((Byte) x); }
    if (x instanceof Short) { return valueOf((Short) x); }
    if (x instanceof Integer) { return valueOf((Integer) x); }
    if (x instanceof Long) { return valueOf((Long) x); }
    if (x instanceof BigInteger) { return valueOf((BigInteger) x); }
    if (x instanceof BigDecimal) { return valueOf((BigDecimal) x); }
    throw Exceptions.unsupportedOperation(null,"valueOf",x); }

  public static final RationalBinaryFloat valueOf (final Object x)  {
    return valueOf((Number) x); }

  //--------------------------------------------------------------

  public static final RationalBinaryFloat ZERO = 
    new RationalBinaryFloat(BigInteger.ZERO,BigInteger.ONE,0);

  public static final RationalBinaryFloat ONE = 
    new RationalBinaryFloat(BigInteger.ONE,BigInteger.ONE,0);

  public static final RationalBinaryFloat TWO = 
    new RationalBinaryFloat(BigInteger.ONE,BigInteger.ONE,1);

  public static final RationalBinaryFloat TEN = 
    new RationalBinaryFloat(BigInteger.TEN,BigInteger.ONE,0);

  public static final RationalBinaryFloat MINUS_ONE = 
    ONE.negate();

  //--------------------------------------------------------------
}
//--------------------------------------------------------------