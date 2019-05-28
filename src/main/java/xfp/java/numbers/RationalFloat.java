package xfp.java.numbers;

import static xfp.java.numbers.Numbers.hiBit;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import xfp.java.exceptions.Exceptions;

/** Representing a rational number as a ratio of
 * {@link UnNatural} times 2 to a <code>long</code> exponent.
 *
 * The idea is that most data will start as <code>double</code>;
 * extracting the resulting powers of 2 from the numerator and
 * denominator should keep the UnNaturals smaller, and make
 * arithmetic on them faster.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-05-28
 */

public final class RationalFloat extends Number
implements Ringlike<RationalFloat> {

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

  private final int _exponent;
  public final int exponent () { return _exponent; }

  //--------------------------------------------------------------

  public final boolean isZero () { return numerator().isZero(); }

  //  private static final boolean isOne (final UnNatural i) {
  //    return UnNatural.ONE.equals(i); }

  private static final boolean isOne (final UnNatural n,
                                      final UnNatural d) {
    return n.equals(d); }

  public final boolean isOne () {
    return isOne(numerator(),denominator()); }

  //--------------------------------------------------------------

  @Override
  public final RationalFloat negate () {
    if (isZero()) { return this; }
    return
      valueOf(
        !nonNegative(),numerator(),denominator(),exponent()); }

  public final RationalFloat reciprocal () {
    assert !(numerator().isZero());
    return valueOf(
      nonNegative(),
      denominator(),
      numerator(),
      -exponent()); }

  //--------------------------------------------------------------
  // TODO: optimize denominator == 1 cases?

  private static final RationalFloat add (final boolean p0,
                                          final UnNatural n0,
                                          final UnNatural d0,
                                          final int e0,
                                          final boolean p1,
                                          final UnNatural n1,
                                          final UnNatural d1,
                                          final int e1) {
    final UnNatural n0d1 = n0.multiply(d1);
    final UnNatural n1d0 = n1.multiply(d0);

    final UnNatural a;
    final UnNatural b;
    final int e;
    if (e0 == e1) { a = n0d1; b = n1d0; e = e0; }
    else if (e0 > e1) {
      a = n0d1.shiftLeft(e0-e1); b = n1d0; e = e1; }
    else { a = n0d1; b = n1d0.shiftLeft(e1-e0); e = e0; }

    final boolean p;
    final UnNatural n;
    if (p0) {
      if (p1) { n = a.add(b); p = true; }
      else {
        final int c = a.compareTo(b);
        if (0 <= c) { n = a.subtract(b); p = true; }
        else { n = b.subtract(a); p = false; } } }
    else { 
      if (p1) {
        final int c = b.compareTo(a);
        if (0 <= c) { n = b.subtract(a); p = true; }
        else { n = a.subtract(b); p = false; } }
      else { n = a.add(b); p = false; } } 

    final UnNatural d = d0.multiply(d1);
    return valueOf(p,n,d,e); }

  private final RationalFloat add (final boolean p1,
                                   final UnNatural n1,
                                   final UnNatural d1,
                                   final int e1) {
    return add(
      nonNegative(),numerator(),denominator(),exponent(),
      p1,n1,d1,e1); }

  @Override
  public final RationalFloat add (final RationalFloat that) {
    if (isZero()) { return that; }
    if (that.isZero()) { return this; }
    return add(
      that.nonNegative(),
      that.numerator(),
      that.denominator(),
      that.exponent()); }

  //--------------------------------------------------------------

  private static final RationalFloat add (final boolean p0,
                                          final UnNatural n0,
                                          final UnNatural d0,
                                          final int e0,
                                          final boolean p1,
                                          final long n1,
                                          final int e1) {
    final UnNatural n1d0 = d0.multiply(n1);

    final UnNatural a;
    final UnNatural b;
    final int e;
    if (e0 == e1) { a = n0; b = n1d0; e = e0; }
    else if (e0 > e1) {
      a = n0.shiftLeft(e0-e1); b = n1d0; e = e1; }
    else { 
      a = n0; b = n1d0.shiftLeft(e1-e0); e = e0; }

    final boolean p;
    final UnNatural n;
    if (p0) {
      if (p1) { n = a.add(b); p = true; }
      else {
        final int c = a.compareTo(b);
        if (0 <= c) { n = a.subtract(b); p = true; }
        else { n = b.subtract(a); p = false; } } }
    else { 
      if (p1) {
        final int c = b.compareTo(a);
        if (0 <= c) { n = b.subtract(a); p = true; }
        else { n = a.subtract(b); p = false; } }
      else { n = a.add(b); p = false; } } 

    return valueOf(p,n,d0,e); }

  //--------------------------------------------------------------
  // 0 leftShift
  
  private static final RationalFloat add (final boolean n0,
                                          final UnNatural t0,
                                          final int e,
                                          final boolean n1,
                                          final long t1) {
    //assert t0.signum() >= 0;
    if (n0 ^ n1) { // different signs
      final int c01 = t0.compareTo(t1);
      if (0 == c01) { return ZERO; }
      // t1 > t0
      if (0 > c01) {
        return valueOf(n1, t0.subtractFrom(t1), e); }
      // t0 > t1
      return valueOf(n0,t0.subtract(t1),e); }
    return valueOf(n0,t0.add(t1),e); }

  /** both denominators 1. 
   * significands adjusted to the same exponent.
   * 2nd arg starts as <code>double</code>.
   */

  private static final RationalFloat add (final boolean n0,
                                          final UnNatural t0,
                                          final int e,
                                          final boolean n1,
                                          final long t1,
                                          final int leftShift) {
    //assert t0.signum() >= 0;
    if (n0 ^ n1) { // different signs
      final int c01 = t0.compareTo(t1,leftShift);
      if (0 == c01) { return ZERO; }
      // t1 > t0
      if (0 > c01) {
        return valueOf(n1, t0.subtractFrom(t1,leftShift), e); }
      // t0 > t1
      return valueOf(n0,t0.subtract(t1,leftShift),e); }
    return valueOf(n0,t0.add(t1,leftShift),e); }

  private final RationalFloat add (final boolean n1,
                                   final long t1,
                                   final int e1) {
    if (0 == t1) { return this; }
    if (UnNatural.ONE.equals(denominator())) {
      //assert 0 < t1;

      final boolean n0 = nonNegative();
      final UnNatural t0 = numerator();
      final int e0 = exponent();

      // adjust significands to the same exponent
      final int de = e1 - e0;
      if (de >= 0) { return add(n0,t0,e0,n1,t1,de); }
      return add(n0,t0.shiftLeft(-de),e1,n1,t1); } 

    return add(
      nonNegative(),numerator(),denominator(),exponent(),
      n1,t1,e1); }

  public final RationalFloat add (final double z) {
    assert Double.isFinite(z);
    return add(
      Doubles.nonNegative(z),
      Doubles.significand(z),
      Doubles.exponent(z));}

  //--------------------------------------------------------------

  @Override
  public final RationalFloat subtract (final RationalFloat q) {
    if (isZero()) { return q.negate(); }
    if (q.isZero()) { return this; }
    return add(
      ! q.nonNegative(),
      q.numerator(),
      q.denominator(),
      q.exponent()); }

  @Override
  public final RationalFloat abs () {
    if (nonNegative()) { return this; }
    return negate(); }

  //--------------------------------------------------------------

  private final RationalFloat multiply (final boolean p,
                                        final UnNatural n,
                                        final UnNatural d,
                                        final int e) {
    return valueOf(
      !(nonNegative() ^ p),
      numerator().multiply(n),
      denominator().multiply(d),
      exponent() + e); }

  @Override
  public final RationalFloat multiply (final RationalFloat q) {
    //    if (isZero() ) { return ZERO; }
    //    if (q.isZero()) { return ZERO; }
    //    if (q.isOne()) { return this; }
    //    if (isOne()) { return q; }
    return multiply(
      q.nonNegative(),
      q.numerator(),
      q.denominator(),
      q.exponent()); }

  //--------------------------------------------------------------

  public final RationalFloat add2 (final double z) {
    assert Double.isFinite(z);
    final UnNatural n1 = 
      UnNatural.valueOf(Doubles.significand(z));
    // TODO: use optimized square
    final UnNatural n2 = n1.multiply(n1);
    final int e2 = 2*Doubles.exponent(z);
    return add(true,n2,UnNatural.ONE,e2); }

  //--------------------------------------------------------------

  public final RationalFloat addProduct (final double z0,
                                         final double z1) {
    assert Double.isFinite(z0);
    assert Double.isFinite(z1);
    final boolean p = 
      ! (Doubles.nonNegative(z0) ^ Doubles.nonNegative(z1));
    // TODO: optimize long*long -> UnNatural?
    final UnNatural n =
      UnNatural.valueOf(Doubles.significand(z0))
      .multiply(UnNatural.valueOf(Doubles.significand(z1)));
    final int e = Doubles.exponent(z0) + Doubles.exponent(z1);
    return add(p,n,UnNatural.ONE,e); }

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
    final BigInteger x = 
      numerator().divide(denominator()).bigIntegerValue(); 
    return (nonNegative() ? x : x.negate()); }

  //  public final Rational rationalValue () {
  //    if (0 <= exponent()) {
  //      return Rational.valueOf(
  //        numerator().shiftLeft(exponent()),denominator()); }
  //    return Rational.valueOf(
  //      numerator(),denominator().shiftLeft(-exponent())); }

  //--------------------------------------------------------------
  /** Half-even rounding from {@link UnNatural} ratio to
   * <code>float</code>.
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

    // TODO: fix this hack
    final boolean large = (exponent() >= 0);
    final UnNatural n00 = large ? n0.shiftLeft(exponent()) : n0;
    final UnNatural d00 = large ? d0 : d0.shiftLeft(-exponent());

    // choose exponent, and shift numerator and denominator so
    // quotient has the right number of bits.
    final int e0 = hiBit(n00) - hiBit(d00) - 1;
    final boolean small = (e0 > 0);
    final UnNatural n1 = small ? n00 : n00.shiftLeft(-e0);
    final UnNatural d1 = small ? d00.shiftLeft(e0) : d00;

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

    //Debug.println("num=" + n3.toString(0x10));
    //Debug.println("den=" + d3.toString(0x10));

    final UnNatural[] qr = 
      n3.divideAndRemainder(d3).toArray(new UnNatural[0]);

    //Debug.println("quo=" + qr[0].toString(0x10));
    //Debug.println("quo=" + Long.toHexString(qr[0].longValueExact()));
    //Debug.println("rem=" + qr[1].toString(0x10));

    // round down or up?
    // want to know if remainder/denominator is more or less than 1/2
    // comparing 2*remainder to denominator
    // TODO: faster way to do this?
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
  /** Half-even rounding from {@link UnNatural} ratio to
   * <code>double</code>.
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

    // TODO: fix this hack
    final boolean large = (exponent() >= 0);
    final UnNatural n00 = large ? n0.shiftLeft(exponent()) : n0;
    final UnNatural d00 = large ? d0 : d0.shiftLeft(-exponent());

    // choose exponent, and shift numerator and denominator so
    // quotient has the right number of bits.
    final int e0 = hiBit(n00) - hiBit(d00) - 1;
    final boolean small = (e0 > 0);
    final UnNatural n1 = small ? n00 : n00.shiftLeft(-e0);
    final UnNatural d1 = small ? d00.shiftLeft(e0) : d00;

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
      return neg ? -0.0 : 0.0; }

    // subnormal numbers need slightly different handling
    final boolean sub = (e2 < Double.MIN_EXPONENT);
    final int e3 = sub ? Double.MIN_EXPONENT : e2;
    final UnNatural d3 = sub ? d2.shiftLeft(e3-e2) : d2;
    final UnNatural n3 = n1.shiftLeft(Doubles.STORED_SIGNIFICAND_BITS);

    final int e4 = e3 - Doubles.STORED_SIGNIFICAND_BITS;

    //Debug.println("num=" + n3.toString(0x10));
    //Debug.println("den=" + d3.toString(0x10));

    final UnNatural[] qr = 
      n3.divideAndRemainder(d3).toArray(new UnNatural[0]);

    //(new Throwable()).printStackTrace();
    //Debug.println("quo=" + qr[0].toString(0x10));
    //Debug.println("quo=" + Long.toHexString(qr[0].longValue()));
    //Debug.println("rem=" + qr[1].toString(0x10));

    // round down or up?
    // want to know if remainder/denominator is more or less than 1/2
    // comparing 2*remainder to denominator
    // TODO: faster way to do this?
    final int c = qr[1].shiftLeft(1).compareTo(d3);
    //Debug.println("c=" + c);
    final long q4 = qr[0].longValue();
    final boolean even = (0x0L == (q4 & 0x1L));
    final boolean down = (c < 0) || ((c == 0) && even);
    final long q;
    final int e;
    if (down) {
      q = q4;
      e = (sub ? e4-1 : e4); }
    else {
      final long q5 = q4+1;
      // handle carry if needed after round up
      final boolean carry = (hiBit(q5) > Doubles.SIGNIFICAND_BITS);
      q = (carry ? q5 >>> 1 : q5);
      e = (sub ? (carry ? e4 : e4 - 1) : (carry ? e4 + 1 : e4)); }
    //Debug.println("neg=" + neg);
    //Debug.println("q=" + Long.toHexString(q));
    //Debug.println("e=" + e);
    return Doubles.makeDouble(neg,e,q); }

  //--------------------------------------------------------------
  // Comparable methods
  //--------------------------------------------------------------

  @Override
  public final int compareTo (final RationalFloat q) {
    if (nonNegative() && (! q.nonNegative())) { return 1; }
    if ((! nonNegative()) && q.nonNegative()) { return -1; }
    // same signs
    final UnNatural n0d1 = numerator().multiply(q.denominator());
    final UnNatural n1d0 = q.numerator().multiply(denominator());
    final int e0 = exponent();
    final int e1 = q.exponent();
    final int c;
    if (e0 <= e1) { c = n0d1.compareTo(n1d0.shiftLeft(e1-e0)); }
    else { c = n0d1.shiftLeft(e0-e1).compareTo(n1d0); }
    return (nonNegative() ? c : -c); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  public final boolean equals (final RationalFloat q) {
    if (this == q) { return true; }
    if (null == q) { return false; }
    // assuming reduced
    return
      (nonNegative() == q.nonNegative())
      &&
      (exponent() == q.exponent())
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
    h = (31*h) + (nonNegative() ? 1 : 0);
    h = (31*h) + exponent();
    h = (31*h) + Objects.hash(numerator(),denominator());
    return h; }

  @Override
  public final String toString () {
    final boolean neg = ! nonNegative();
    final String n = numerator().toString(0x10);
    return
      (neg ? "-" : "") + "0x"
      + n
      + "p" + exponent()
      + " / "
      + denominator().toString(0x10); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private RationalFloat (final boolean nonNegative,
                         final UnNatural numerator,
                         final UnNatural denominator,
                         final int exponent) {
    //    super();
    //    assert 1 == denominator.signum() :
    //      "\nn= " + numerator.toString(0x10)
    //      + "\nd= " + denominator.toString(0x10)
    //      + "\ne= " + exponent;
    _nonNegative = nonNegative;
    _numerator = numerator;
    _denominator = denominator;
    _exponent = exponent; }

  //--------------------------------------------------------------
  /** optimize denominator == 1 case. */

  private static final RationalFloat
  reduced (final boolean nonNegative,
           final UnNatural n,
           final int e) {

    if (n.isZero()) { return ZERO; }

    // TODO: is numerator 1 case worth optimizing?
    if (UnNatural.ONE.equals(n)) {
      return new RationalFloat(
        nonNegative,UnNatural.ONE,UnNatural.ONE,e); }
    final int en = Numbers.loBit(n);
    final UnNatural n0 = (en != 0) ? n.shiftRight(en) : n;
    final int e0 = (e + en);
    return new RationalFloat(nonNegative,n0,UnNatural.ONE,e0); }


  private static final RationalFloat
  reduced (final boolean nonNegative,
           final UnNatural n,
           final UnNatural d,
           final int e) {

    if (n.isZero()) { return ZERO; }

    if (UnNatural.ONE.equals(d)) { 
      return reduced(nonNegative,n,e); }

    // TODO: is numerator 1 case worth optimizing?
    if (UnNatural.ONE.equals(n)) {
      final int ed = Numbers.loBit(d);
      final UnNatural d0 = (ed != 0) ? d.shiftRight(ed) : d;
      final int e0 = e - ed;
      return new RationalFloat(nonNegative,UnNatural.ONE,d0,e0); }

    final int en = Numbers.loBit(n);
    final int ed = Numbers.loBit(d);
    final UnNatural n0 = (en != 0) ? n.shiftRight(en) : n;
    final UnNatural d0 = (ed != 0) ? d.shiftRight(ed) : d;
    final int e0 = (e + en) - ed;

    // might have numerator or denominator 1 after shift
    if (UnNatural.ONE.equals(d0)) {
      if (UnNatural.ONE.equals(n0)) {
        return new RationalFloat(
          nonNegative,UnNatural.ONE,UnNatural.ONE,e0); }
      return new RationalFloat(nonNegative,n0,UnNatural.ONE,e0); }
    if (UnNatural.ONE.equals(n0)) {
      return new RationalFloat(nonNegative,UnNatural.ONE,d0,e0); }

    final UnNatural gcd = n0.gcd(d0);
    // TODO: any value in this test?
    if (gcd.compareTo(UnNatural.ONE) > 0) {
      final UnNatural n1 = n0.divide(gcd);
      final UnNatural d1 = d0.divide(gcd);
      // assert d1.signum() == 1 :
      //   "non positive denominator:"
      //   + "\nn= " + n.toString(0x10)
      //   + "\nd= " + d.toString(0x10)
      //   + "\ne= " + e
      //   + "\n"
      //   + "\nn0= " + n0.toString(0x10)
      //   + "\nd0= " + d0.toString(0x10)
      //   + "\ne0= " + e0
      //   + "\n"
      //   + "\ngcd= " + gcd.toString(0x10)
      //   + "\n"
      //   + "\nn1= " + n1.toString(0x10)
      //   + "\nd1= " + d1.toString(0x10)
      //   + "\ne1= " + e0
      //   + "\n"
      //   ;
      return new RationalFloat(nonNegative,n1,d1,e0); }

    return new RationalFloat(nonNegative,n0,d0,e0); }

  //--------------------------------------------------------------

  public static final RationalFloat valueOf (final boolean nonNegative,
                                             final UnNatural n,
                                             final UnNatural d,
                                             final int e) {
    return reduced(nonNegative,n,d,e); }

  public static final RationalFloat valueOf (final boolean nonNegative,
                                             final UnNatural n,
                                             final int e) {
    return reduced(nonNegative,n,UnNatural.ONE,e); }

  public static final RationalFloat valueOf (final boolean nonNegative,
                                             final UnNatural n,
                                             final UnNatural d) {
    return valueOf(nonNegative,n,d,0); }

  public static final RationalFloat valueOf (final boolean nonNegative,
                                             final UnNatural x)  {
    return valueOf(nonNegative, x, 0); }

  public static final RationalFloat valueOf (final BigInteger n,
                                             final BigInteger d) {
    return valueOf(
      0 <= (n.signum()*d.signum()),
      UnNatural.valueOf(n.abs()),
      UnNatural.valueOf(d.abs()),
      0); }

  public static final RationalFloat valueOf (final long n,
                                             final long d,
                                             final int e) {
    if (0L > d) { return valueOf(-n,-d,e); }
    assert 0L < d;
    final boolean p = (0L<=n); 
    return
      valueOf(
        p,
        UnNatural.valueOf(p ? n : -n),
        UnNatural.valueOf(d),
        e); }

  public static final RationalFloat valueOf (final int n,
                                             final int d,
                                             final int e) {
    if (0 > d) { return valueOf(-n,-d,e); }
    assert 0 < d;
    final boolean p = (0<=n); 
    return
      valueOf(
        p,
        UnNatural.valueOf(p ? n : -n),
        UnNatural.valueOf(d),
        e); }

  //--------------------------------------------------------------

  private static final RationalFloat valueOf (final boolean nonNegative,
                                              final int e,
                                              final long t)  {
    if (0L == t) { return ZERO; }
    assert 0L < t;
    return valueOf(nonNegative,UnNatural.valueOf(t),e); }

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
    assert 0 < t;
    return valueOf(nonNegative,UnNatural.valueOf(t),e); }

  public static final RationalFloat valueOf (final float x)  {
    return valueOf(
      Floats.nonNegative(x),
      Floats.exponent(x),
      Floats.significand(x)); }

  //--------------------------------------------------------------

  public static final RationalFloat valueOf (final byte x)  {
    final boolean nonNegative = (0 <= x);
    return valueOf(
      nonNegative,
      UnNatural.valueOf(nonNegative ? x : -x)); }

  public static final RationalFloat valueOf (final short x)  {
    final boolean nonNegative = (0 <= x);
    return valueOf(
      nonNegative,
      UnNatural.valueOf(nonNegative ? x : -x)); }

  public static final RationalFloat valueOf (final int x)  {
    final boolean nonNegative = (0 <= x);
    return valueOf(
      nonNegative,
      UnNatural.valueOf(nonNegative ? x : -x)); }

  public static final RationalFloat valueOf (final long x)  {
    final boolean nonNegative = (0L <= x);
    return valueOf(
      nonNegative,
      UnNatural.valueOf(nonNegative ? x : -x)); }

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
  //    return valueOf(x, UnNatural.ONE); }

  public static final RationalFloat valueOf (final Number x)  {
    if (x instanceof RationalFloat) { return (RationalFloat) x; }
    if (x instanceof Double) { return valueOf((Double) x); }
    if (x instanceof Float) { return valueOf((Float) x); }
    if (x instanceof Byte) { return valueOf((Byte) x); }
    if (x instanceof Short) { return valueOf((Short) x); }
    if (x instanceof Integer) { return valueOf((Integer) x); }
    if (x instanceof Long) { return valueOf((Long) x); }
    if (x instanceof UnNatural) { return valueOf(x); }
    if (x instanceof BigDecimal) { return valueOf((BigDecimal) x); }
    throw Exceptions.unsupportedOperation(null,"valueOf",x); }

  public static final RationalFloat valueOf (final Object x)  {
    return valueOf((Number) x); }

  //--------------------------------------------------------------
  // Note: these need to be reduced.

  public static final RationalFloat ZERO =
    new RationalFloat(true,UnNatural.ZERO,UnNatural.ONE,0);

  public static final RationalFloat ONE =
    new RationalFloat(true,UnNatural.ONE,UnNatural.ONE,0);

  public static final RationalFloat TWO =
    new RationalFloat(true,UnNatural.ONE,UnNatural.ONE,1);

  public static final RationalFloat TEN =
    new RationalFloat(true,UnNatural.valueOf(5),UnNatural.ONE,1);

  public static final RationalFloat MINUS_ONE =
    ONE.negate();

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
