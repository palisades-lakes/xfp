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
 * @version 2019-05-11
 */

public final class RationalFloat extends Number
implements Comparable<RationalFloat> {

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

  private static final RationalFloat 
  reduced (final BigInteger n,
           final BigInteger d,
           final int e) {

    if (d.signum() < 0) { 
      return reduced(n.negate(),d.negate(),e); }

    if (n == BigInteger.ZERO) { return ZERO; }

    final int en = Numbers.loBit(n);
    final int ed = Numbers.loBit(d);
    final BigInteger n0 = (en != 0) ? n.shiftRight(en) : n;
    final BigInteger d0 = (ed != 0) ? d.shiftRight(ed) : d;
    final int e0 = e + en - ed;

    //    if (BigInteger.ONE.equals(d0)) {
    //      return new RationalFloat(n0,d0,e0); } 

    final BigInteger gcd = n0.gcd(d0);
    // TODO: any value in this test?
    if (gcd.compareTo(BigInteger.ONE) > 0) {
      final BigInteger n1 = n0.divide(gcd);
      final BigInteger d1 = d0.divide(gcd);
      //      assert d1.signum() == 1 :
      //        "non positive denominator:"
      //        + "\nn= " + n.toString(0x10)
      //        + "\nd= " + d.toString(0x10)
      //        + "\ne= " + e 
      //        + "\n"
      //        + "\nn0= " + n0.toString(0x10)
      //        + "\nd0= " + d0.toString(0x10)
      //        + "\ne0= " + e0 
      //        + "\n"
      //        + "\ngcd= " + gcd.toString(0x10)
      //        + "\n"
      //        + "\nn1= " + n1.toString(0x10)
      //        + "\nd1= " + d1.toString(0x10)
      //        + "\ne1= " + e0 
      //        + "\n"
      //        ;
      return new RationalFloat(n1,d1,e0); } 

    return new RationalFloat(n0,d0,e0); }

  //--------------------------------------------------------------

  public final RationalFloat negate () {
    if (isZero()) { return this; }
    return 
      valueOf(numerator().negate(),denominator(),exponent()); }

  public final RationalFloat reciprocal () {
    assert !isZero(numerator());
    return valueOf(denominator(),numerator(),-exponent()); }

  //--------------------------------------------------------------
  // TODO: optimize denominator == 1 cases.

  private final RationalFloat add (final BigInteger n1,
                                   final BigInteger d1,
                                   final int e1) {
    final BigInteger n0 = numerator();
    final BigInteger d0 = denominator();
    final int e0 = exponent();
    final BigInteger n0d1 = n0.multiply(d1);
    final BigInteger n1d0 = n1.multiply(d0);
    final BigInteger d0d1 = d0.multiply(d1);
    if (e0 == e1) {
      return valueOf(n0d1.add(n1d0),d0d1,e1); }
    if (e0 > e1) {
      return valueOf(n0d1.shiftLeft(e0-e1).add(n1d0),d0d1,e1); }
    return valueOf(n0d1.add(n1d0.shiftLeft(e1-e0)),d0d1,e0); }

  public final RationalFloat add (final RationalFloat q) {
    if (isZero()) { return q; }
    if (q.isZero()) { return this; }
    return add(q.numerator(),q.denominator(),q.exponent()); }

  public final RationalFloat add (final double z) {
    assert Double.isFinite(z);
    final boolean s = Doubles.nonNegative(z);
    final int e1 = Doubles.exponent(z);
    final long t = Doubles.significand(z);
    final BigInteger n1 = BigInteger.valueOf(s ? t : -t);
    final BigInteger n0 = numerator();
    final BigInteger d0 = denominator();
    final int e0 = exponent();
    final BigInteger n1d0 = 
      BigInteger.ONE.equals(d0) ? n1 : n1.multiply(d0);
    if (e0 >= e1) {
      return valueOf(n0.shiftLeft(e0-e1).add(n1d0),d0,e1); }
    return valueOf(n0.add(n1d0.shiftLeft(e1-e0)),d0,e0); }

  //--------------------------------------------------------------

  public final RationalFloat subtract (final RationalFloat q) {
    if (isZero()) { return q.negate(); }
    if (q.isZero()) { return this; }
    return add(q.numerator().negate(),q.denominator(),q.exponent()); }

  public final RationalFloat abs () {
    // TODO: direct signum
    final int s = numerator().signum();
    if (0<=s) { return this; }
    return negate(); }

  //--------------------------------------------------------------

  private final RationalFloat multiply (final BigInteger n,
                                        final BigInteger d,
                                        final int e) {
    return 
      valueOf(
        numerator().multiply(n), 
        denominator().multiply(d),
        exponent() + e); }

  public final RationalFloat multiply (final RationalFloat q) {
    //    if (isZero() ) { return ZERO; }
    //    if (q.isZero()) { return ZERO; }
    //    if (q.isOne()) { return this; }
    //    if (isOne()) { return q; }
    return multiply(q.numerator(),q.denominator(),q.exponent()); }

  //--------------------------------------------------------------

  public final RationalFloat add2 (final double z) { 
    assert Double.isFinite(z);
    final BigInteger n = numerator();
    final BigInteger d = denominator();
    final int e = exponent();

    final boolean s = Doubles.nonNegative(z);
    final long t = (s ? 1L : -1L) * Doubles.significand(z);
    final int e01 = 2*Doubles.exponent(z);
    final int de = e - e01;

    final BigInteger tt = BigInteger.valueOf(t);
    final BigInteger n0 = tt.multiply(tt);
    final BigInteger n1 = 
      (BigInteger.ONE.equals(d) ? n0 : n0.multiply(d));
    final int e2;
    final BigInteger n2;
    if (0 == de) { e2 = e; n2 = n.add(n1); }
    else if (0 < de) {
      e2 = e01; n2 = n.shiftLeft(de).add(n1); }
    else { e2 = e; n2 = n.add(n1.shiftRight(de)); }

    return valueOf(n2,d,e2); }

  //--------------------------------------------------------------

  public final RationalFloat addProduct (final double z0,
                                         final double z1) { 
    assert Double.isFinite(z0);
    assert Double.isFinite(z1);
    final BigInteger n = numerator();
    final BigInteger d = denominator();
    final int e = exponent();

    final boolean s = 
      ! (Doubles.nonNegative(z0) ^ Doubles.nonNegative(z1));
    final long t0 = (s ? 1L : -1L) * Doubles.significand(z0);
    final long t1 = Doubles.significand(z1);
    final int e01 = Doubles.exponent(z0) + Doubles.exponent(z1);
    final int de = e - e01;

    final BigInteger n0 = 
      BigInteger.valueOf(t0).multiply(BigInteger.valueOf(t1));
    final BigInteger n1 = 
      (BigInteger.ONE.equals(d) ? n0 : n0.multiply(d));
    final int e2;
    final BigInteger n2;
    if (0 == de) { e2 = e; n2 = n.add(n1); }
    else if (0 < de) {
      e2 = e01; n2 = n.shiftLeft(de).add(n1); }
    else { e2 = e; n2 = n.add(n1.shiftRight(de)); }

    return valueOf(n2,d,e2); }

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
    // TODO: keep sign 'bit' separate; num and den both nonNegative.
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
    
    //Debug.println("num=" + n3.toString(0x10));
    //Debug.println("den=" + d3.toString(0x10));
    
    final BigInteger[] qr = n3.divideAndRemainder(d3);
    
    //Debug.println("quo=" + qr[0].toString(0x10));
    //Debug.println("quo=" + Long.toHexString(qr[0].longValueExact()));
    //Debug.println("rem=" + qr[1].toString(0x10));

    // round down or up? 
    // want to know if remainder/denominator is more or less than 1/2
    // comparing 2*remainder to denominator
    // TODO: faster way to do this?
    final int c = qr[1].shiftLeft(1).compareTo(d3);
    final int q4 = qr[0].intValueExact(); 
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
  /** Half-even rounding from {@link BigInteger} ratio to 
   * <code>double</code>.
   * @param n numerator
   * @param d positive denominator
   * @return closest half-even rounded <code>double</code> to n / d.
   */

  @Override
  public final double doubleValue () { 
    // TODO: keep sign 'bit' separate; num and den both nonNegative.
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
    
    //Debug.println("num=" + n3.toString(0x10));
    //Debug.println("den=" + d3.toString(0x10));
    
    final BigInteger[] qr = n3.divideAndRemainder(d3);
    
    //Debug.println("quo=" + qr[0].toString(0x10));
    //Debug.println("quo=" + Long.toHexString(qr[0].longValueExact()));
    //Debug.println("rem=" + qr[1].toString(0x10));

    // round down or up? 
    // want to know if remainder/denominator is more or less than 1/2
    // comparing 2*remainder to denominator
    // TODO: faster way to do this?
    final int c = qr[1].shiftLeft(1).compareTo(d3);
    final long q4 = qr[0].longValueExact(); 
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
  public final int compareTo (final RationalFloat q) {
    final BigInteger n0d1 = numerator().multiply(q.denominator());
    final BigInteger n1d0 = q.numerator().multiply(denominator());
    final int e0 = exponent();
    final int e1 = q.exponent();
    if (e0 <= e1) { return n0d1.compareTo(n1d0.shiftLeft(e1-e0)); }
    return n0d1.shiftLeft(e0-e1).compareTo(n1d0); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  public final boolean equals (final RationalFloat q) {
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
    if (!(o instanceof RationalFloat)) { return false; }
    return equals((RationalFloat) o); }

  @Override
  public int hashCode () {
    int h = 17;
    h = 31*h + exponent();
    h = 31*h + Objects.hash(numerator(),denominator());
    return h; }

  @Override
  public final String toString () {
    final boolean neg = (numerator().signum() < 0);
    final String n = numerator().toString(0x10).toUpperCase();
    return 
      (neg ? "-" : "") + "0x"
      + (neg ? n.substring(1) : n) 
      + "p" + exponent() 
      + " / "
      + denominator().toString(0x10).toUpperCase(); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private RationalFloat (final BigInteger numerator,
                         final BigInteger denominator,
                         final int exponent) {
//    super();
//    assert 1 == denominator.signum() :
//      "\nn= " + numerator.toString(0x10) 
//      + "\nd= " + denominator.toString(0x10)
//      + "\ne= " + exponent;
    _numerator = numerator;
    _denominator = denominator;
    _exponent = exponent; }

  //--------------------------------------------------------------

  public static final RationalFloat valueOf (final BigInteger n,
                                             final BigInteger d,
                                             final int e) {
    return reduced(n,d,e); }

  public static final RationalFloat valueOf (final BigInteger n,
                                             final int e) {
    return reduced(n,BigInteger.ONE,e); }

  public static final RationalFloat valueOf (final BigInteger n,
                                             final BigInteger d) {
    return valueOf(n,d,0); }

  public static final RationalFloat valueOf (final long n,
                                             final long d,
                                             final int e) {
    if (d < 0) { return valueOf(-n,-d,e); }
    return 
      valueOf(BigInteger.valueOf(n),BigInteger.valueOf(d),e); }

  public static final RationalFloat valueOf (final int n,
                                             final int d, 
                                             final int e) {
    if (d < 0) { return valueOf(-n,-d,e); }
    return 
      valueOf(BigInteger.valueOf(n),BigInteger.valueOf(d),e); }

  //--------------------------------------------------------------

  private static final RationalFloat valueOf (final boolean nonNegative,
                                              final int e,
                                              final long t)  {
    if (0L == t) { return ZERO; }
    final long tt = nonNegative ? t : -t;
    final BigInteger n = BigInteger.valueOf(tt);
    return valueOf(n,e); } 

  public static final RationalFloat valueOf (final double x)  {
    return valueOf(
      Doubles.nonNegative(x),
      Doubles.exponent(x),
      Doubles.significand(x)); } 

  //--------------------------------------------------------------

  private static final RationalFloat valueOf (final boolean nonNegative,
                                              final int e,
                                              final int t)  {
    if (0 == t) { return ZERO; }
    final BigInteger n0 = BigInteger.valueOf(t);
    final BigInteger n1 = nonNegative ? n0 : n0.negate();
    return valueOf(n1,e); } 

  public static final RationalFloat valueOf (final float x)  {
    return valueOf(
      Floats.nonNegative(x),
      Floats.exponent(x),
      Floats.significand(x)); } 

  //--------------------------------------------------------------

  public static final RationalFloat valueOf (final byte x)  {
    return valueOf(BigInteger.valueOf(x)); }

  public static final RationalFloat valueOf (final short x)  {
    return valueOf(BigInteger.valueOf(x)); }

  public static final RationalFloat valueOf (final int x)  {
    return valueOf(BigInteger.valueOf(x)); }

  public static final RationalFloat valueOf (final long x)  {
    return valueOf(BigInteger.valueOf(x)); }

  //--------------------------------------------------------------

  public static final RationalFloat valueOf (final Double x)  {
    return valueOf(x.doubleValue()); }

  public static final RationalFloat valueOf (final Float x)  {
    return valueOf(x.floatValue()); }

  public static final RationalFloat valueOf (final Byte x)  {
    return valueOf(x.byteValue()); }

  public static final RationalFloat valueOf (final Short x)  {
    return valueOf(x.shortValue()); }

  public static final RationalFloat valueOf (final Integer x)  {
    return valueOf(x.intValue()); }

  public static final RationalFloat valueOf (final Long x)  {
    return valueOf(x.longValue()); }

  public static final RationalFloat valueOf (final BigDecimal x)  {
    throw Exceptions.unsupportedOperation(null,"valueOf",x); }
  //    return valueOf(x, BigInteger.ONE); }

  public static final RationalFloat valueOf (final BigInteger x)  {
    return valueOf(x, BigInteger.ONE,0); }

  public static final RationalFloat valueOf (final Number x)  {
    if (x instanceof RationalFloat) { return (RationalFloat) x; }
    if (x instanceof Double) { return valueOf((Double) x); }
    if (x instanceof Float) { return valueOf((Float) x); }
    if (x instanceof Byte) { return valueOf((Byte) x); }
    if (x instanceof Short) { return valueOf((Short) x); }
    if (x instanceof Integer) { return valueOf((Integer) x); }
    if (x instanceof Long) { return valueOf((Long) x); }
    if (x instanceof BigInteger) { return valueOf((BigInteger) x); }
    if (x instanceof BigDecimal) { return valueOf((BigDecimal) x); }
    throw Exceptions.unsupportedOperation(null,"valueOf",x); }

  public static final RationalFloat valueOf (final Object x)  {
    return valueOf((Number) x); }

  //--------------------------------------------------------------
  // Note: these need to be reduced.

  public static final RationalFloat ZERO = 
    new RationalFloat(BigInteger.ZERO,BigInteger.ONE,0);

  public static final RationalFloat ONE = 
    new RationalFloat(BigInteger.ONE,BigInteger.ONE,0);

  public static final RationalFloat TWO = 
    new RationalFloat(BigInteger.ONE,BigInteger.ONE,1);

  public static final RationalFloat TEN = 
    new RationalFloat(BigInteger.valueOf(5),BigInteger.ONE,1);

  public static final RationalFloat MINUS_ONE = 
    ONE.negate();

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
