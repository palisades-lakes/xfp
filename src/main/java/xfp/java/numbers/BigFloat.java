package xfp.java.numbers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import xfp.java.exceptions.Exceptions;

/** A {@link BigInteger} significand times 2 to a 
 * <code>int</code> exponent.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-23
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
    return add(valueOf(z)); }

//  public final BigFloat add (final double z) {
//    assert Double.isFinite(z);
//    final boolean s = Doubles.nonNegative(z);
//    final int e1 = Doubles.exponent(z);
//    final long t = Doubles.significand(z);
//    final BigInteger n1 = BigInteger.valueOf(s ? t : -t);
//    final BigInteger n0 = significand();
//    final int e0 = exponent();
//    if (e0 >= e1) {
//      return valueOf(n0.shiftLeft(e0-e1).add(n1),e1); }
//    return valueOf(n0.add(n1.shiftLeft(e1-e0)),e0); }

  //--------------------------------------------------------------

  public final BigFloat subtract (final BigFloat q) {
    if (isZero()) { return q.negate(); }
    if (q.isZero()) { return this; }
    return add(q.significand().negate(),q.exponent()); }

  public final BigFloat abs () {
    // TODO: direct signum
    final int s = significand().signum();
    if (0<=s) { return this; }
    return negate(); }

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
  /** Adjust exponent from viewing signifcand as an integer
   * to significand as a binary 'decimal'.
   * <p>
   * TODO: make integer/fractional significand consistent
   * across all classes. Probably convert to integer 
   * interpretation everywhere.
   */

  private static final float floatMergeBits (final int sign,
                                             final int exponent,
                                             final int significand) { 
    assert Numbers.hiBit(significand) <= Floats.SIGNIFICAND_BITS;
    if (Numbers.hiBit(significand) < Floats.SIGNIFICAND_BITS) {
      return Floats.mergeBits(
        sign, 
        Floats.SUBNORMAL_EXPONENT, 
        significand); }
    return Floats.mergeBits(
      sign, 
      exponent + Floats.STORED_SIGNIFICAND_BITS, 
      significand); }

  /** Half-even rounding to nearest double, treating significand
   * as an integer, rather the usual decimal fraction.
   */

  private static final float toFloat (final boolean nonNegative,
                                      final BigInteger significand,
                                      final int exponent) { 
    final int sign = (nonNegative ? 0 : 1);
    final BigInteger s0 = significand;
    final int e0 = exponent;
    final int eh = Numbers.hiBit(s0);
    final int es = 
      Math.max(Floats.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND - e0,
        Math.min(
          Floats.MAXIMUM_EXPONENT_INTEGRAL_SIGNIFICAND - e0 -1,
          eh - Floats.SIGNIFICAND_BITS));
    if (0 == es) {
      return floatMergeBits(sign,e0,s0.intValue()); }
    if (0 > es) {
      final int e1 = e0 + es;
      final int s1 = (s0.intValue() << -es);
      return floatMergeBits(sign,e1,s1); }
    if (eh <= es) { return (nonNegative ? 0.0F : -0.0F); }
    // eh > es > 0
    final boolean up = s0.testBit(es); 
    // TODO: faster way to select the right bits as a long?
    final int s1 = s0.shiftRight(es).intValue();
    final int e1 = e0 + es;
    if (up) {
      final int s2 = s1 + 1;
      if (Numbers.hiBit(s2) > 53) { // carry
        // lost bit has to be zero, since there was just a carry
        // so no more rounding
        final int s3 = s2 >> 1;
      final int e3 = e1 + 1;
      return floatMergeBits(sign,e3,s3); }
      // no carry
      return floatMergeBits(sign,e1,s2); }
    // round down
    return floatMergeBits(sign,e1,s1); }

  /** @return closest half-even rounded <code>float</code> 
   */

  @Override
  public final float floatValue () { 
    final int sign = significand().signum();
    if (sign == 0) { return 0.0F; }
    final boolean nonNegative = (sign > 0);
    final BigInteger s = 
      (nonNegative ? significand() : significand().negate());
    return toFloat(nonNegative,s,exponent()); }

  //--------------------------------------------------------------
  /** Adjust exponent from viewing signifcand as an integer
   * to significand as a binary 'decimal'.
   * <p>
   * TODO: make integer/fractional significand consistent
   * across all classes. Probably convert to integer 
   * interpretation everywhere.
   */

  private static final double doubleMergeBits (final boolean nonNegative,
                                               final int exponent,
                                               final long significand) { 
    if (Numbers.hiBit(significand) > Doubles.SIGNIFICAND_BITS) {
      return 
        (nonNegative 
          ? Double.POSITIVE_INFINITY 
            : Double.NEGATIVE_INFINITY); }
    if (Numbers.hiBit(significand) < Doubles.SIGNIFICAND_BITS) {
      return Doubles.mergeBits(
        nonNegative ? 0 : 1, 
        Doubles.SUBNORMAL_EXPONENT, 
        significand); }
    return Doubles.mergeBits(
      nonNegative ? 0 : 1, 
      exponent + Doubles.STORED_SIGNIFICAND_BITS, 
      significand); }

  /** Half-even rounding to nearest double, treating significand
   * as an integer, rather the usual decimal fraction.
   */

  private static final double toDouble (final boolean nonNegative,
                                        final BigInteger significand,
                                        final int exponent) { 
    final BigInteger s0 = significand;
    final int e0 = exponent;
//    Debug.println("toDouble: " 
//    + nonNegative + ":0x" + significand.toString(0x10)
//    + "p" + exponent);
    final int eh = Numbers.hiBit(s0);
//    Debug.println("eh=" + eh);
    final int es = 
      Math.max(Doubles.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND - e0,
        Math.min(
          Doubles.MAXIMUM_EXPONENT_INTEGRAL_SIGNIFICAND - e0 -1,
          eh - Doubles.SIGNIFICAND_BITS));
//    Debug.println("es=" + es);
    if (0 == es) {
      return doubleMergeBits(nonNegative,e0,s0.longValue()); }
    if (0 > es) {
      final int e1 = e0 + es;
      final long s1 = (s0.longValue() << -es);
      return doubleMergeBits(nonNegative,e1,s1); }
    if (eh <= es) { return (nonNegative ? 0.0 : -0.0); }
    // eh > es > 0
    final boolean up = s0.testBit(es-1); 
//    Debug.println("up=" + up);
    // TODO: faster way to select the right bits as a long?
    final long s1 = s0.shiftRight(es).longValue();
    final int e1 = e0 + es;
    if (up) {
      final long s2 = s1 + 1L;
      if (Numbers.hiBit(s2) > 53) { // carry
        // lost bit has to be zero, since there was just a carry
        final long s3 = (s2 >> 1);
        final int e3 = e1 + 1;
        return doubleMergeBits(nonNegative,e3,s3); }
      // no carry
      return doubleMergeBits(nonNegative,e1,s2); }
    // round down
    return doubleMergeBits(nonNegative,e1,s1); }

  /** @return closest half-even rounded <code>double</code> 
   */

  @Override
  public final double doubleValue () { 
    final int sign = significand().signum();
    if (sign == 0) { return 0.0; }
    final boolean nonNegative = (sign > 0);
    final BigInteger s = 
      (nonNegative ? significand() : significand().negate());
    return toDouble(nonNegative,s,exponent()); }

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
      "0x" + significand().toString(0x10) 
      + "p" + exponent(); }

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
                                         final int e0,
                                         final long t0)  {
    if (0L == t0) { return ZERO; }
    final int e1 = Numbers.loBit(t0);
    final long t1 = (t0 >> e1);
    final long t2 = nonNegative ? t1 : -t1;
    return valueOf(BigInteger.valueOf(t2),e0+e1); } 

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
