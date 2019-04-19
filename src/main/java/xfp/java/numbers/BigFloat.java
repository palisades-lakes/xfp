package xfp.java.numbers;

import static xfp.java.numbers.Numbers.hiBit;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import xfp.java.Classes;
import xfp.java.exceptions.Exceptions;

/** A {@link BigInteger} significand times 2 to a 
 * <code>int</code> exponent.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-17
 */

public final class BigFloat 
extends Number
implements Comparable<BigFloat> {

  private static final long serialVersionUID = 1L;

  //--------------------------------------------------------------
  // instance fields and methods
  //--------------------------------------------------------------

  private final BigInteger _significand;
  public final BigInteger significand () { return _significand; }

  private final int _exponent;
  public final int exponent () { return _exponent; }

  //--------------------------------------------------------------

  //  private static final boolean isNegative (final BigInteger i) {
  //    return 0 > i.signum(); }

  private static final boolean isZero (final BigInteger i) {
    return 0 == i.signum(); }

  public final boolean isZero () { return isZero(significand()); }

  public final boolean isOne () { 
    return BigFloat.ONE.equals(this); }

  //--------------------------------------------------------------

  public final BigFloat negate () {
    if (isZero()) { return this; }
    return valueOf(significand().negate(),exponent()); }

  //--------------------------------------------------------------
  // TODO: optimize denominator == 1 cases.

  private final BigFloat add (final BigInteger n1,
                              final int e1) {
    final BigInteger n0 = significand();
    final int e0 = exponent();
    if (e0 == e1) {
      return valueOf(n0.add(n1),e1); }
    if (e0 > e1) {
      return valueOf(n0.shiftLeft(e0-e1).add(n1),e1); }
    return valueOf(n0.add(n1.shiftLeft(e1-e0)),e0); }

  public final BigFloat add (final BigFloat q) {
    if (isZero()) { return q; }
    if (q.isZero()) { return this; }
    return add(q.significand(),q.exponent()); }

  public final BigFloat add (final double z) {
    assert Double.isFinite(z);
    final boolean s = Doubles.nonNegative(z);
    final int e1 = Doubles.exponent(z);
    final long t = Doubles.significand(z);
    final BigInteger n1 = BigInteger.valueOf(s ? t : -t);
    final BigInteger n0 = significand();
    final int e0 = exponent();
    if (e0 >= e1) {
      return valueOf(n0.shiftLeft(e0-e1).add(n1),e1); }
    return valueOf(n0.add(n1.shiftLeft(e1-e0)),e0); }

  //--------------------------------------------------------------

  private final BigFloat multiply (final BigInteger t,
                                   final int e) {
    return valueOf(significand().multiply(t), exponent() + e); }

  public final BigFloat multiply (final BigFloat q) {
    //    if (isZero() ) { return ZERO; }
    //    if (q.isZero()) { return ZERO; }
    //    if (q.isOne()) { return this; }
    //    if (isOne()) { return q; }
    return multiply(q.significand(),q.exponent()); }

  //--------------------------------------------------------------

  public final BigFloat add2 (final double z) { 
    assert Double.isFinite(z);
    final BigInteger n = significand();
    final int e = exponent();

    final boolean s = Doubles.nonNegative(z);
    final long t = (s ? 1L : -1L) * Doubles.significand(z);
    final int e01 = 2*Doubles.exponent(z);
    final int de = e - e01;

    final BigInteger tt = BigInteger.valueOf(t);
    final BigInteger n0 = tt.multiply(tt);

    final int e2;
    final BigInteger n2;
    if (0 == de) { e2 = e; n2 = n.add(n0); }
    else if (0 < de) {
      e2 = e01; n2 = n.shiftLeft(de).add(n0); }
    else { e2 = e; n2 = n.add(n0.shiftRight(de)); }

    return valueOf(n2,e2); }

  //--------------------------------------------------------------

  public final BigFloat addProduct (final double z0,
                                    final double z1) { 
    assert Double.isFinite(z0);
    assert Double.isFinite(z1);
    final BigInteger n = significand();
    final int e = exponent();

    final boolean s = 
      ! (Doubles.nonNegative(z0) ^ Doubles.nonNegative(z1));
    final long t0 = (s ? 1L : -1L) * Doubles.significand(z0);
    final long t1 = Doubles.significand(z1);
    final int e01 = Doubles.exponent(z0) + Doubles.exponent(z1);
    final int de = e - e01;

    final BigInteger n0 = 
      BigInteger.valueOf(t0).multiply(BigInteger.valueOf(t1));
    final int e2;
    final BigInteger n2;
    if (0 == de) { e2 = e; n2 = n.add(n0); }
    else if (0 < de) {
      e2 = e01; n2 = n.shiftLeft(de).add(n0); }
    else { e2 = e; n2 = n.add(n0.shiftRight(de)); }

    return valueOf(n2,e2); }

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
    return significand().shiftLeft(exponent()); }

  public final Rational rationalValue () { 
    if (0 <= exponent()) {
      return Rational.valueOf(
        significand().shiftLeft(exponent()),BigInteger.ONE); }
    return Rational.valueOf(
      significand(),BigInteger.ONE.shiftLeft(-exponent())); }

  //--------------------------------------------------------------
  /** Half-even rounding from {@link BigInteger} ratio to 
   * <code>float</code>.
   * @param n significand
   * @param d positive denominator
   * @return closest half-even rounded <code>float</code> to n / d.
   */

  @Override
  public final float floatValue () { 
    final int s = significand().signum();
    if (s == 0) { return 0.0F; }
    final boolean neg = (s < 0);
    final BigInteger n0 = (neg ? significand().negate() : significand());
    final BigInteger d0 = BigInteger.ONE;

    // TODO: fix this hack
    final boolean large = (exponent() >= 0);
    final BigInteger n00 = large ? n0.shiftLeft(exponent()) : n0;
    final BigInteger d00 = large ? d0 : d0.shiftLeft(-exponent());

    // choose exponent, and shift significand and denominator so
    // quotient has the right number of bits.
    final int e0 = hiBit(n00) - hiBit(d00) - 1;
    final boolean small = (e0 > 0);
    final BigInteger n1 = small ? n00 : n00.shiftLeft(-e0);
    final BigInteger d1 = small ? d00.shiftLeft(e0) : d00;

    // ensure significand is less than 2x denominator
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
   * @return closest half-even rounded <code>double</code> 
   */

  @Override
  public final double doubleValue () { 
    final int s = significand().signum();
    if (s == 0) { return 0.0; }
    final boolean nonNegative = (s > 0);
    final BigInteger s0 = 
      (nonNegative ? significand() : significand().negate());
    final int e0 = exponent();
    final int e1 = Numbers.hiBit(s0)-Doubles.SIGNIFICAND_BITS;
    final int e2 = e0 + e1;

    if (e2 >= Doubles.MAXIMUM_EXPONENT_INTEGRAL_SIGNIFICAND) {
      return (nonNegative 
        ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY); }

    if (e2 < Doubles.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND) {
      return (nonNegative ? 0.0 : -0.0); }

    final long s1;
    final int e3;
    // subnormal
    if (e2 == Doubles.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND) {
      s1 = s0.shiftRight(e1+1).longValue(); 
      e3 = e2-1; }
    // normal
    else { 
      s1 = s0.shiftRight(e1).longValue();   
      e3 = e2; }

    try {
      return Doubles.makeDouble(! nonNegative,e3,s1); }
    catch (final Throwable t) {
      System.err.println(
        Classes.className(this) + ".doubleValue() failed on\n"
          + this  
          + "\ns0=" + s0.toString(0x10)
          + "\ne0=" + e0
          + "\ne1=" + e1
          + "\ne2=" + e2
          + "\ne3=" + e3
          + "\ns1=" + Long.toHexString(s1)
          + "\nhiBit(s1)=" + Numbers.hiBit(s1));
      throw t; } }

  //--------------------------------------------------------------
  // Comparable methods
  //--------------------------------------------------------------

  @Override
  public final int compareTo (final BigFloat q) {
    final BigInteger n0 = significand();
    final BigInteger n1 = q.significand();
    final int e0 = exponent();
    final int e1 = q.exponent();
    if (e0 <= e1) { return n0.compareTo(n1.shiftLeft(e1-e0)); }
    return n0.shiftLeft(e0-e1).compareTo(n1); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  public final boolean equals (final BigFloat q) {
    if (this == q) { return true; }
    if (null == q) { return false; }
    // assuming reduced
    return 
      exponent() == q.exponent()
      &&
      significand().equals(q._significand); }

  @Override
  public boolean equals (final Object o) {
    if (!(o instanceof BigFloat)) { return false; }
    return equals((BigFloat) o); }

  @Override
  public int hashCode () {
    int h = 17;
    h = 31*h + exponent();
    h = 31*h + Objects.hash(significand());
    return h; }

  @Override
  public final String toString () {
    return 
      significand().toString(0x10)  
      //+ "\n " 
      + "*"
      //+ "\n" 
      + "2^" + exponent()
      //+ "\n"
      ; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private BigFloat (final BigInteger t0,
                    final int e0) {
    super();
    final int e1 = Numbers.loBit(t0);
    _significand = (e1 != 0) ? t0.shiftRight(e1) : t0;
    _exponent = Math.addExact(e0,e1); }

  //--------------------------------------------------------------

  public static final BigFloat valueOf (final BigInteger n,
                                        final int e) {
    if (n == BigInteger.ZERO) { return ZERO; }
    return new BigFloat(n,e); } 

  public static final BigFloat valueOf (final long n,
                                        final int e) {
    return valueOf(BigInteger.valueOf(n),e); }

  public static final BigFloat valueOf (final int n,
                                        final int e) {
    return valueOf(BigInteger.valueOf(n),e); }

  //--------------------------------------------------------------

  private static final BigFloat valueOf (final boolean nonNegative,
                                         final int e,
                                         final long t)  {
    if (0L == t) { return ZERO; }
    final long tt = nonNegative ? t : -t;
    final BigInteger n = BigInteger.valueOf(tt);
    return valueOf(n,e); } 

  public static final BigFloat valueOf (final double x)  {
    return valueOf(
      Doubles.nonNegative(x),
      Doubles.exponent(x),
      Doubles.significand(x)); } 

  //--------------------------------------------------------------

  private static final BigFloat valueOf (final boolean nonNegative,
                                         final int e,
                                         final int t)  {
    if (0 == t) { return ZERO; }
    final BigInteger n0 = BigInteger.valueOf(t);
    final BigInteger n1 = nonNegative ? n0 : n0.negate();
    return valueOf(n1,e); } 

  public static final BigFloat valueOf (final float x)  {
    return valueOf(
      Floats.nonNegative(x),
      Floats.exponent(x),
      Floats.significand(x)); } 

  //--------------------------------------------------------------

  public static final BigFloat valueOf (final byte x)  {
    return valueOf(BigInteger.valueOf(x),0); }

  public static final BigFloat valueOf (final short x)  {
    return valueOf(BigInteger.valueOf(x),0); }

  public static final BigFloat valueOf (final int x)  {
    return valueOf(BigInteger.valueOf(x),0); }

  public static final BigFloat valueOf (final long x)  {
    return valueOf(BigInteger.valueOf(x),0); }

  //--------------------------------------------------------------

  public static final BigFloat valueOf (final Double x)  {
    return valueOf(x.doubleValue()); }

  public static final BigFloat valueOf (final Float x)  {
    return valueOf(x.floatValue()); }

  public static final BigFloat valueOf (final Byte x)  {
    return valueOf(x.byteValue()); }

  public static final BigFloat valueOf (final Short x)  {
    return valueOf(x.shortValue()); }

  public static final BigFloat valueOf (final Integer x)  {
    return valueOf(x.intValue()); }

  public static final BigFloat valueOf (final Long x)  {
    return valueOf(x.longValue()); }

  public static final BigFloat valueOf (final BigDecimal x)  {
    throw Exceptions.unsupportedOperation(null,"valueOf",x); }
  //    return valueOf(x, BigInteger.ONE); }

  public static final BigFloat valueOf (final BigInteger x)  {
    return valueOf(x,0); }

  public static final BigFloat valueOf (final Number x)  {
    if (x instanceof BigFloat) { return (BigFloat) x; }
    if (x instanceof Double) { return valueOf((Double) x); }
    if (x instanceof Float) { return valueOf((Float) x); }
    if (x instanceof Byte) { return valueOf((Byte) x); }
    if (x instanceof Short) { return valueOf((Short) x); }
    if (x instanceof Integer) { return valueOf((Integer) x); }
    if (x instanceof Long) { return valueOf((Long) x); }
    if (x instanceof BigInteger) { return valueOf((BigInteger) x); }
    if (x instanceof BigDecimal) { return valueOf((BigDecimal) x); }
    throw Exceptions.unsupportedOperation(null,"valueOf",x); }

  public static final BigFloat valueOf (final Object x)  {
    return valueOf((Number) x); }

  //--------------------------------------------------------------

  public static final BigFloat ZERO = 
    new BigFloat(BigInteger.ZERO,0);

  public static final BigFloat ONE = 
    new BigFloat(BigInteger.ONE,0);

  public static final BigFloat TWO = 
    new BigFloat(BigInteger.ONE,1);

  public static final BigFloat TEN = 
    new BigFloat(BigInteger.valueOf(5),1);

  public static final BigFloat MINUS_ONE = ONE.negate();

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
