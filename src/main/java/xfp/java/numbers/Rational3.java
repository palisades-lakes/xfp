package xfp.java.numbers;

import static xfp.java.numbers.Numbers.description;
import static xfp.java.numbers.Numbers.hiBit;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import xfp.java.Debug;
import xfp.java.exceptions.Exceptions;

/** Ratios of {@link BigInteger}.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-25
 */

public final class Rational3 
extends Number
implements Comparable<Rational3> {

  private static final long serialVersionUID = 1L;

  //--------------------------------------------------------------
  // instance fields and methods
  //--------------------------------------------------------------

  private final BigInteger _numerator;
  public final BigInteger numerator () { return _numerator; }
  private final BigInteger _denominator;
  public final BigInteger denominator () { return _denominator; }

  //--------------------------------------------------------------

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
  // TODO: does BigInteger optimize multiply by ONE?

  private final Rational3 add (final BigInteger n,
                              final BigInteger d) {
    assert !isZero(d);
    if (0 == n.signum()) { return this; }
    if (isZero()) { return valueOf(n,d); }
    return valueOf(
      numerator().multiply(d).add(n.multiply(denominator())),
      denominator().multiply(d)); }

  private final Rational3 multiply (final BigInteger n,
                                   final BigInteger d) {
    assert !isZero(d);
    if (isZero() ) { return ZERO; }
    if (isZero(n)) { return ZERO; }
    if (isOne(n,d)) { return this; }
    if (isOne()) { return valueOf(n,d); }
    return 
      valueOf(
        numerator().multiply(n), 
        denominator().multiply(d)); }

  //--------------------------------------------------------------

  public final Rational3 negate () {
    if (isZero()) { return this; }
    return valueOf(numerator().negate(),denominator()); }

  public final Rational3 reciprocal () {
    assert !isZero(numerator());
    return valueOf(denominator(),numerator()); }

  public final Rational3 add (final Rational3 q) {
    return add(q.numerator(),q.denominator()); }

  public final Rational3 multiply (final Rational3 q) {
    return multiply(q.numerator(),q.denominator()); }

  //--------------------------------------------------------------
 
  public static final BigInteger[] toRatio (final double x) {
    
    if (! Double.isFinite(x)) {
      throw new IllegalArgumentException(
       "RationalSum"  + " cannot handle "+ x); }
  
    final BigInteger numerator;
    final BigInteger denominator;
  
    // compute m and k such that x = m * 2^k
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

  public final Rational3 add (final double q) {
    final BigInteger[] nd = toRatio(q);
    return add(nd[0],nd[1]); }

  public final Rational3 addProduct (final double z0,
                                    final double z1) { 
    final BigInteger[] nd0 = toRatio(z0);
    final BigInteger[] nd1 = toRatio(z1);
    return add(
      nd0[0].multiply(nd1[0]),
      nd0[1].multiply(nd1[1])); }

  //--------------------------------------------------------------
  // Number methods
  //--------------------------------------------------------------

  @Override
  public final int intValue () {
    return numerator().divide(denominator()).intValue(); }

  @Override
  public final long longValue () {
    return numerator().divide(denominator()).longValue(); }

  public final BigInteger 
  bigIntegerValue () { return numerator().divide(denominator()); }

  //--------------------------------------------------------------
  // Half-even rounding to float.
  //--------------------------------------------------------------
  /** Handle over and under flow (too many/few bits in q).
   * @param subnormal 
   * @param e7 exponent
   * @param q7 rounded(n/d)
   * @param s sign bit: 0 positive, 1 negative.
   * @return (-1)^s * (2^e) * q.
   */

  private static final float floatValue7 (final boolean negative,
                                          final boolean subnormal,
                                          final int e7, 
                                          final int q7) {
    Debug.println();
    Debug.println("floatValue7(long,int,int)");
    Debug.println(description("q7",q7));
    Debug.println("negative= " + negative + ", e7= " + e7
      + ", subnormal=" + subnormal);
    if (subnormal) {
      assert e7 == Floats.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND;
      assert 0 < q7;
      assert q7 < (1 << Floats.STORED_SIGNIFICAND_BITS); }
    else {
      assert Floats.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND <= e7 : 
        Integer.toString(e7);
      assert e7 < Floats.MAXIMUM_EXPONENT_INTEGRAL_SIGNIFICAND : 
        Integer.toString(e7);
      assert (1 << Floats.STORED_SIGNIFICAND_BITS) <= q7 
        : description("q7",q7); 
      assert q7 <= (1 << (Floats.STORED_SIGNIFICAND_BITS+1)); }

    final int e70 = subnormal ? (e7 - 1) : e7;

    if (subnormal) { 
      assert e70 == Floats.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND - 1 : 
        e70; }
    else {
      assert Floats.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND <= e70 :
        Integer.toString(e70);
      assert e70 < Floats.MAXIMUM_EXPONENT_INTEGRAL_SIGNIFICAND : 
        Integer.toString(e70); }

    final float z = Floats.makeFloat(negative,e70,q7);

    return z; }

  //--------------------------------------------------------------
  /** Handle carry (too many bits in q).
   * @param subnormal TODO
   * @param e6 exponent
   * @param q6 rounded(n/d)
   * @param s sign bit: 0 positive, 1 negative.
   * @return (-1)^s * (2^e) * q.
   */

  private static final float floatValue6 (final boolean negative,
                                          final boolean subnormal,
                                          final int e6, 
                                          final BigInteger q6) {
    Debug.println();
    Debug.println("floatValue6(BigInteger,boolean,int,boolean)");
    Debug.println("negative= " + negative + ", e= " + e6
      + ", subnormal=" + subnormal);
    Debug.println(description("q6",q6));

    assert q6.signum() == 1;
    if (subnormal) {
      assert e6 == Floats.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND;
      //      assert hiBit(q) <= NUMERATOR_BITS 
      //        : "too many bits:" + description("q",q);
      assert q6.compareTo(TWO_23) <= 0; }
    else {
      assert Floats.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND <= e6 : 
        Integer.toString(e6);
      assert e6 < Floats.MAXIMUM_EXPONENT_INTEGRAL_SIGNIFICAND : 
        Integer.toString(e6);
      assert TWO_23.compareTo(q6) <= 0;
      assert q6.compareTo(TWO_24) < 0; }

    final BigInteger q60;
    final int e60;
    if (hiBit(q6) <= Floats.SIGNIFICAND_BITS) { 
      q60 = q6; e60 = e6; }
    // handle carry
    else { q60 = q6.shiftRight(1); e60 = e6+1; }
    final float z = 
      floatValue7(negative,subnormal,e60, q60.intValueExact()); 

    return z; }

  //--------------------------------------------------------------

  private static final BigInteger TWO_23 = 
    BigInteger.ONE.shiftLeft(Floats.STORED_SIGNIFICAND_BITS);

  private static final BigInteger TWO_24 = TWO_23.shiftLeft(1);

  /** Is q or (q+1) closer to n/d?.
   * @param negative extracted sign info.
   * @param subnormal is this a subnormal value?
   * @param e5 exponent
   * @param q5 floor(n5/d5)
   * @param r5 d5*((n5/d5) - floor(n5/d5))
   * @param d5 positive denominator
   * @return closest half-even rounded float to 
   * (-1)^s * (2^e) * (q + (r / d).
   */

  private static final float floatValue5 (final boolean negative,
                                          final boolean subnormal,
                                          final int e5,
                                          final BigInteger q5,
                                          final BigInteger r5,
                                          final BigInteger d5) {
    Debug.println();
    Debug.println("floatValue6(BigInteger,BigInteger,boolean,int,boolean,BigInteger,BigInteger)");
    Debug.println(description("d5",d5));
    Debug.println("negative= " + negative + ", e= " + e5
      + ", subnormal=" + subnormal);
    Debug.println(description("q5",q5));
    Debug.println(description("r5",r5));

    assert d5.signum() == 1;
    assert q5.signum() == 1;
    assert r5.signum() >= 0;
    assert r5.compareTo(d5) < 0;
    if (subnormal) {
      assert e5 == Floats.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND;
      assert q5.compareTo(TWO_23) < 0; }
    else {
      assert Floats.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND <= e5 :
        Integer.toString(e5);
      assert e5 < Floats.MAXIMUM_EXPONENT_INTEGRAL_SIGNIFICAND : 
        Integer.toString(e5);
      // TODO: faster test!!!
      assert TWO_23.compareTo(q5) <= 0;
      assert q5.compareTo(TWO_24) < 0; }

    final BigInteger q50;
    final BigInteger r50 = r5.shiftLeft(1);
    if (r50.compareTo(d5) <= 0) { q50 = q5;  }
    else { q50 = q5.add(BigInteger.ONE); }
    Debug.println(description("q50",q50));

    Debug.println(description("q50",q50));

    final float z = floatValue6(negative,subnormal,e5,q50);

    return z; }

  //--------------------------------------------------------------

  /** Divide numerator by denominator to get quotient and
   * remainder.
   * @param e4 initial exponent
   * @param n4 positive numerator
   * @param d4 positive denominator
   * @param s sign bit: 0 positive, 1 negative.
   * @return closest half-even rounded float to 
   * (-1)^s * (2^e4) * n4 / d4.
   */

  private static final float floatValue4 (final boolean negative,
                                          final boolean subnormal,
                                          final int e4,
                                          final BigInteger n4,
                                          final BigInteger d4) {
    Debug.println();
    Debug.println("floatValue5(BigInteger,BigInteger,boolean,int,boolean)");
    Debug.println(description("n4",n4));
    Debug.println(description("d4",d4));
    Debug.println("negative= " + negative + ", e= " + e4
      + ", subnormal=" + subnormal);

    assert n4.signum() == 1;
    assert d4.signum() == 1;
    if (subnormal) {
      assert e4 == Floats.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND;
      assert TWO_23.compareTo(n4) <= 0; 
      assert n4.compareTo(d4.shiftLeft(Floats.STORED_SIGNIFICAND_BITS)) < 0; }
    else {
      assert Floats.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND <= e4 : 
        Integer.toString(e4);
      assert e4 < Floats.MAXIMUM_EXPONENT_INTEGRAL_SIGNIFICAND :
        Integer.toString(e4);
      assert d4.shiftLeft(Floats.STORED_SIGNIFICAND_BITS).compareTo(n4) <= 0;
      // TODO: faster test!!!
      assert n4.compareTo(d4.shiftLeft(Floats.STORED_SIGNIFICAND_BITS+1)) < 0; }

    final BigInteger[] qr = n4.divideAndRemainder(d4);
    final BigInteger q4 = qr[0];
    final BigInteger r4 = qr[1];

    final float z = floatValue5(negative,subnormal,e4,q4,r4,d4); 

    return z; }

  //--------------------------------------------------------------
  /** Shift <code>n3</code> {@link Floats#STORED_SIGNIFICAND_BITS}
   * left, so the quotient will have enough bits (in the normal
   * case) for the significand ( as as many bits as possible in
   * the subnormal case).
   * @param e3 exponent
   * @param n3 positive numerator
   * @param d3 positive denominator
   * @param s sign bit: 0 positive, 1 negative.
   * @return closest half-even rounded float to 
   * (-1)^s * (2^e) * n / d.
   */

  private static final float floatValue3 (final boolean negative,
                                          final boolean subnormal,
                                          final int e3,
                                          final BigInteger n3,
                                          final BigInteger d3) {
    Debug.println();
    Debug.println("floatValue3(BigInteger,BigInteger,boolean,int,boolean)");
    Debug.println(description("n",n3));
    Debug.println(description("d",d3));
    Debug.println("negative= " + negative + ", e= " + e3
      + ", subnormal=" + subnormal);

    assert n3.signum() == 1;
    assert d3.signum() == 1;
    if (subnormal) {
      assert e3 == Float.MIN_EXPONENT;
      assert n3.compareTo(d3) < 0; }
    else {
      assert Float.MIN_EXPONENT <= e3 : Integer.toString(e3);
      assert e3 <= Float.MAX_EXPONENT : Integer.toString(e3);
      assert d3.compareTo(n3) <= 0;
      // TODO: faster test!!!
      assert n3.compareTo(d3.shiftLeft(1)) < 0; }


    final BigInteger n30 = n3.shiftLeft(Floats.STORED_SIGNIFICAND_BITS);
    final int e30 = e3 - Floats.STORED_SIGNIFICAND_BITS;

    Debug.println(description("n30",n30));
    Debug.println("e30=" + e30);

    final float z = floatValue4(negative,subnormal,e30,n30,d3);

    return z; }

  //--------------------------------------------------------------
  /** Detect zero, subnormal, normal, and infinite values.
   * .
   * @param n2 positive numerator
   * @param d2 positive denominator
   * @param s sign bit: 0 positive, 1 negative.
   * @return closest half-even rounded float to 
   * -1^s * 2<sup>e2</sup> * n / d.
   */

  private static final float floatValue2 (final boolean negative,
                                          final int e2,
                                          final BigInteger n2,
                                          final BigInteger d2) {
    Debug.println();
    Debug.println("floatValue2(BigInteger,BigInteger,boolean,int)");
    Debug.println(description("n2",n2));
    Debug.println(description("d2",d2));
    Debug.println("e2=" + e2);
    Debug.println("neg=" + negative);

    assert n2.signum() == 1;
    assert d2.signum() == 1;
    assert d2.compareTo(n2) <= 0;
    // TODO: faster test!!!
    assert n2.compareTo(d2.shiftLeft(1)) < 0;

    final float z;
    if (e2 > Float.MAX_EXPONENT) {
      z = negative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY; }
    else if (e2 < Floats.MINIMUM_SUBNORMAL_EXPONENT) {
      z = negative ? -0.0F : 0.0F; }
    else if (e2 < Float.MIN_EXPONENT) {
      z = floatValue3(
        negative,true,
        Float.MIN_EXPONENT,n2,d2.shiftLeft(Float.MIN_EXPONENT-e2)); }
    else {
      z = floatValue3(negative,false,e2,n2,d2); }

    return z; }

  //--------------------------------------------------------------
  /** Extract exponent <code>e</code> so that
   * <code> n1 / d1 == 2<sup>e11</sup> * n11 / d12</code>
   * and <code>d12 &le; n11 &lt; 2*d12</code>.
   * @param negative flag
   * @param n1 positive numerator
   * @param d1 positive denominator
   * 
   * @return closest half-even rounded float to -1^s * n / d.
   */

  private static final float floatValue1 (final boolean negative,
                                          final BigInteger n1,
                                          final BigInteger d1) {
    Debug.println();
    Debug.println("floatValue1(BigInteger,BigInteger,boolean)");
    Debug.println(description("n1",n1));
    Debug.println(description("d1",d1));
    Debug.println("neg=" + negative);

    assert n1.signum() == 1  : 
      "numernator not strictly positive: " 
      + n1.toString(0x10);
    assert d1.signum() == 1 : 
      "denominator not strictly positive: " 
      + d1.toString(0x10);

    final int n1h = hiBit(n1);
    final int d1h = hiBit(d1);
    final int e10 = n1h - d1h - 1;

    Debug.println("n1h,d1h,e10=" + n1h + "," + d1h + "," + e10);

    final BigInteger n10, d10;
    if (e10 > 0) { n10 = n1; d10 = d1.shiftLeft(e10); }
    else if (e10 == 0) { n10 = n1; d10 = d1; }
    else if (e10 < 0) { n10 = n1.shiftLeft(-e10); d10 = d1; }
    else { throw new RuntimeException("can't get here"); }

    Debug.println(description("n10",n10));
    Debug.println(description("d10",d10));

    // TODO: more efficient test!!!
    final BigInteger d11 = d10.shiftLeft(1);
    final BigInteger d12;
    final int e11;
    if (n10.compareTo(d11) < 0) { d12 = d10; e11 = e10;}
    else { d12 = d11; e11 = e10 + 1; }

    Debug.println(description("n10",n10));
    Debug.println(description("d12",d12));
    Debug.println("e11=" + e11);

    final float z = floatValue2(negative,e11,n10,d12);

    return z; }

  //--------------------------------------------------------------
  /** Extract sign bit, so numerator and denominator are both
   * positive. Also reduce by gcd.
   * <p>
   * <em>TODO:</em> measure performance effect of reducing/not
   * reducing?
   * 
   * @param n0 numerator
   * @param d0 positive denominator
   * @return closest half-even rounded <code>float</code> to 
   * exact value of <code>n0 / d0</code>.
   */

  private static final float floatValue0 (final BigInteger n0,
                                          final BigInteger d0) {
    Debug.println();
    Debug.println("floatValue0(BigInteger,BigInteger)");
    Debug.println(description("n0",n0));
    Debug.println(description("d0",d0));

    assert d0.signum() == 1 : 
      "denominator not strictly positive: " 
      + d0.toString(0x10);

    // extract sign
    final int n0s = n0.signum();

    Debug.println("n0.signum= " + n0s);

    if (n0s == 0) { return 0.0F; }
    final boolean negative = (n0s < 0);
    final BigInteger n00 = negative ? n0.negate() : n0;

    Debug.println(description("n00",n00));

    // reduce fraction (optional step)
    // TODO: test performance effect
    final BigInteger gcd = n00.gcd(d0);

    Debug.println("gcd=" + gcd);

    final BigInteger n01,d01;
    if (BigInteger.ONE.equals(gcd)) { n01 = n00; d01 = d0; }
    else { n01 = n00.divide(gcd); d01 = d0.divide(gcd); }

    Debug.println(description("n01",n01));
    Debug.println(description("d01",d01));

    final float z = floatValue1(negative,n01,d01);

    return z; }

  //--------------------------------------------------------------
  /** Half-even rounding from {@link BigInteger} ratio to 
   * <code>float</code>.
   * @param n numerator
   * @param d positive denominator
   * @return closest half-even rounded float to n / d.
   */

  public static final float floatValue (final BigInteger n,
                                        final BigInteger d) {
    assert d.signum() == 1 : 
      "denominator not strictly positive: " 
      + d.toString(0x10);

    final float z = floatValue0(n,d);

    return z; }

  @Override
  public final float floatValue () {
     return floatValue(numerator(),denominator()); }

  //--------------------------------------------------------------
  // Half-even rounding to double
  //--------------------------------------------------------------

  public static final BigInteger TWO_52 = 
    BigInteger.ONE.shiftLeft(Doubles.STORED_SIGNIFICAND_BITS);

  public static final BigInteger TWO_53 = TWO_52.shiftLeft(1);

  //--------------------------------------------------------------
  /** Divide numerator by denominator to get quotient and
   * remainder.
   * @param e4 initial exponent
   * @param n4 positive numerator
   * @param d4 positive denominator
   * @param s sign bit: 0 positive, 1 negative.
   * @return closest half-even rounded double to 
   * (-1)^s * (2^e4) * n4 / d4.
   */

  public static final double doubleValue4 (final boolean negative,
                                           final boolean subnormal,
                                           final int e4,
                                           final BigInteger n4,
                                           final BigInteger d4) {
    Debug.println();
    Debug.println("doubleValue5(BigInteger,BigInteger,boolean,int,boolean)");
    Debug.println(description("n4",n4));
    Debug.println(description("d4",d4));
    Debug.println("negative= " + negative + ", e= " + e4
      + ", subnormal=" + subnormal);

    assert n4.signum() == 1;
    assert d4.signum() == 1;
    if (subnormal) {
      assert e4 == Doubles.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND;
      assert TWO_52.compareTo(n4) <= 0; 
      assert n4.compareTo(d4.shiftLeft(Doubles.STORED_SIGNIFICAND_BITS)) < 0; }
    else {
      assert Doubles.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND <= e4 : 
        Integer.toString(e4);
      assert e4 < Doubles.MAXIMUM_EXPONENT_INTEGRAL_SIGNIFICAND : 
        Integer.toString(e4);
      assert d4.shiftLeft(Doubles.STORED_SIGNIFICAND_BITS).compareTo(n4) <= 0;
      // TODO: faster test!!!
      assert n4.compareTo(d4.shiftLeft(Doubles.STORED_SIGNIFICAND_BITS+1)) < 0; }

    final BigInteger[] qr = n4.divideAndRemainder(d4);
    final BigInteger q4 = qr[0];
    final BigInteger r4 = qr[1];

    final double z = doubleValue5(negative,subnormal,e4,q4,r4,d4); 

    return z; }

  //--------------------------------------------------------------
  /** Handle over and under flow (too many/few bits in q).
   * @param subnormal 
   * @param e7 exponent
   * @param q7 rounded(n/d)
   * @param s sign bit: 0 positive, 1 negative.
   * @return (-1)^s * (2^e) * q.
   */

  public static final double doubleValue7 (final boolean negative,
                                           final boolean subnormal,
                                           final int e7, 
                                           final long q7) {
    Debug.println();
    Debug.println("doubleValue7(long,int,int)");
    Debug.println(description("q7",q7));
    Debug.println("negative= " + negative + ", e7= " + e7
      + ", subnormal=" + subnormal);
    if (subnormal) {
      assert e7 == Doubles.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND;
      assert 0L < q7;
      assert q7 < (1L << Doubles.STORED_SIGNIFICAND_BITS); }
    else {
      assert Doubles.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND <= e7 : 
        Integer.toString(e7);
      assert e7 < Doubles.MAXIMUM_EXPONENT_INTEGRAL_SIGNIFICAND : 
        Integer.toString(e7);
      assert (1L << Doubles.STORED_SIGNIFICAND_BITS) <= q7 
        : description("q7",q7); 
      assert q7 <= (1L << (Doubles.STORED_SIGNIFICAND_BITS+1)); }

    final int e70 = subnormal ? (e7 - 1) : e7;

    if (subnormal) { 
      assert e70 == Doubles.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND - 1 : 
        Integer.toString(e70); }
    else {
      assert Doubles.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND <= e70 : 
        Integer.toString(e70);
      assert e70 < Doubles.MAXIMUM_EXPONENT_INTEGRAL_SIGNIFICAND : 
        Integer.toString(e70); }

    final double z = Doubles.makeDouble(negative,e70,q7);

    return z; }

  //--------------------------------------------------------------
  /** Handle carry (too many bits in q).
   * @param subnormal TODO
   * @param e6 exponent
   * @param q6 rounded(n/d)
   * @param s sign bit: 0 positive, 1 negative.
   * @return (-1)^s * (2^e) * q.
   */

  public static final double doubleValue6 (final boolean negative,
                                           final boolean subnormal,
                                           final int e6, 
                                           final BigInteger q6) {
    Debug.println();
    Debug.println("doubleValue6(BigInteger,boolean,int,boolean)");
    Debug.println("negative= " + negative + ", e= " + e6
      + ", subnormal=" + subnormal);
    Debug.println(description("q6",q6));

    assert q6.signum() == 1;
    if (subnormal) {
      assert e6 == Doubles.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND;
      //      assert hiBit(q) <= NUMERATOR_BITS 
      //        : "too many bits:" + description("q",q);
      assert q6.compareTo(Rational3.TWO_52) <= 0; }
    else {
      assert Doubles.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND <= e6 : Integer.toString(e6);
      assert e6 < Doubles.MAXIMUM_EXPONENT_INTEGRAL_SIGNIFICAND : Integer.toString(e6);
      assert TWO_52.compareTo(q6) <= 0;
      assert q6.compareTo(TWO_53) < 0; }

    final BigInteger q60;
    final int e60;
    if (hiBit(q6) <= Doubles.SIGNIFICAND_BITS) { 
      q60 = q6; e60 = e6; }
    // handle carry
    else { q60 = q6.shiftRight(1); e60 = e6+1; }
    final double z = 
      doubleValue7(negative,subnormal,e60, q60.longValueExact()); 

    return z; }

  //--------------------------------------------------------------
  /** Is q or (q+1) closer to n/d?.
   * @param negative extracted sign info.
   * @param subnormal is this a subnormal value?
   * @param e5 exponent
   * @param q5 floor(n5/d5)
   * @param r5 d5*((n5/d5) - floor(n5/d5))
   * @param d5 positive denominator
   * @return closest half-even rounded double to 
   * (-1)^s * (2^e) * (q + (r / d).
   */

  public static final double doubleValue5 (final boolean negative,
                                           final boolean subnormal,
                                           final int e5,
                                           final BigInteger q5,
                                           final BigInteger r5,
                                           final BigInteger d5) {
    Debug.println();
    Debug.println("doubleValue6(BigInteger,BigInteger,boolean,int,boolean,BigInteger,BigInteger)");
    Debug.println(description("d5",d5));
    Debug.println("negative= " + negative + ", e= " + e5
      + ", subnormal=" + subnormal);
    Debug.println(description("q5",q5));
    Debug.println(description("r5",r5));

    assert d5.signum() == 1;
    assert q5.signum() == 1;
    assert r5.signum() >= 0;
    assert r5.compareTo(d5) < 0;
    if (subnormal) {
      assert e5 == Doubles.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND;
      assert q5.compareTo(TWO_52) < 0; }
    else {
      assert Doubles.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND <= e5 : 
        Integer.toString(e5);
      assert e5 < Doubles.MAXIMUM_EXPONENT_INTEGRAL_SIGNIFICAND : 
        Integer.toString(e5);
      // TODO: faster test!!!
      assert TWO_52.compareTo(q5) <= 0;
      assert q5.compareTo(TWO_53) < 0; }

    final BigInteger q50;
    final BigInteger r50 = r5.shiftLeft(1);
    if (r50.compareTo(d5) <= 0) { q50 = q5;  }
    else { q50 = q5.add(BigInteger.ONE); }
    Debug.println(description("q50",q50));

    Debug.println(description("q50",q50));

    final double z = doubleValue6(negative,subnormal,e5,q50);

    return z; }

  //--------------------------------------------------------------
  /** Shift <code>n3</code> {@link Doubles#STORED_SIGNIFICAND_BITS}
   * left, so the quotient will have enough bits (in the normal
   * case) for the significand ( as as many bits as possible in
   * thje subnormal case).
   * @param e3 exponent
   * @param n3 positive numerator
   * @param d3 positive denominator
   * @param s sign bit: 0 positive, 1 negative.
   * @return closest half-even rounded double to 
   * (-1)^s * (2^e) * n / d.
   */

  public static final double doubleValue3 (final boolean negative,
                                           final boolean subnormal,
                                           final int e3,
                                           final BigInteger n3,
                                           final BigInteger d3) {
    Debug.println();
    Debug.println("doubleValue3(BigInteger,BigInteger,boolean,int,boolean)");
    Debug.println(description("n",n3));
    Debug.println(description("d",d3));
    Debug.println("negative= " + negative + ", e= " + e3
      + ", subnormal=" + subnormal);

    assert n3.signum() == 1;
    assert d3.signum() == 1;
    if (subnormal) {
      assert e3 == Double.MIN_EXPONENT;
      assert n3.compareTo(d3) < 0; }
    else {
      assert Double.MIN_EXPONENT <= e3 : Integer.toString(e3);
      assert e3 <= Double.MAX_EXPONENT : Integer.toString(e3);
      assert d3.compareTo(n3) <= 0;
      // TODO: faster test!!!
      assert n3.compareTo(d3.shiftLeft(1)) < 0; }


    final BigInteger n30 = n3.shiftLeft(Doubles.STORED_SIGNIFICAND_BITS);
    final int e30 = e3 - Doubles.STORED_SIGNIFICAND_BITS;

    Debug.println(description("n30",n30));
    Debug.println("e30=" + e30);

    final double z = doubleValue4(negative,subnormal,e30,n30,d3);
    return z; }

  //--------------------------------------------------------------
  /** Detect zero, subnormal, normal, and infinite values.
   * .
   * @param n2 positive numerator
   * @param d2 positive denominator
   * @param s sign bit: 0 positive, 1 negative.
   * @return closest half-even rounded double to 
   * -1^s * 2<sup>e2</sup> * n / d.
   */

  public static final double doubleValue2 (final boolean negative,
                                           final int e2,
                                           final BigInteger n2,
                                           final BigInteger d2) {
    Debug.println();
    Debug.println("doubleValue2(BigInteger,BigInteger,boolean,int)");
    Debug.println(description("n2",n2));
    Debug.println(description("d2",d2));
    Debug.println("e2=" + e2);
    Debug.println("neg=" + negative);

    assert n2.signum() == 1;
    assert d2.signum() == 1;
    assert d2.compareTo(n2) <= 0;
    // TODO: faster test!!!
    assert n2.compareTo(d2.shiftLeft(1)) < 0;

    final double z;
    if (e2 > Double.MAX_EXPONENT) {
      z = negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY; }
    else if (e2 < Doubles.MINIMUM_SUBNORMAL_EXPONENT) {
      z = negative ? -0.0 : 0.0; }
    else if (e2 < Double.MIN_EXPONENT) {
      z = doubleValue3(
        negative,true,
        Double.MIN_EXPONENT,n2,d2.shiftLeft(Double.MIN_EXPONENT-e2)); }
    else {
      z = doubleValue3(negative,false,e2,n2,d2); }

    return z; }

  //--------------------------------------------------------------
  /** Extract exponent <code>e</code> so that
   * <code> n1 / d1 == 2<sup>e11</sup> * n11 / d12</code>
   * and <code>d12 &le; n11 &lt; 2*d12</code>.
   * @param negative flag
   * @param n1 positive numerator
   * @param d1 positive denominator
   * 
   * @return closest half-even rounded double to -1^s * n / d.
   */

  public static final double doubleValue1 (final boolean negative,
                                           final BigInteger n1,
                                           final BigInteger d1) {
    Debug.println();
    Debug.println("doubleValue1(BigInteger,BigInteger,boolean)");
    Debug.println(description("n1",n1));
    Debug.println(description("d1",d1));
    Debug.println("neg=" + negative);

    assert n1.signum() == 1  : 
      "numernator not strictly positive: " 
      + n1.toString(0x10);
    assert d1.signum() == 1 : 
      "denominator not strictly positive: " 
      + d1.toString(0x10);

    final int n1h = hiBit(n1);
    final int d1h = hiBit(d1);
    final int e10 = n1h - d1h - 1;

    Debug.println("n1h,d1h,e10=" + n1h + "," + d1h + "," + e10);

    final BigInteger n10, d10;
    if (e10 > 0) { n10 = n1; d10 = d1.shiftLeft(e10); }
    else if (e10 == 0) { n10 = n1; d10 = d1; }
    else if (e10 < 0) { n10 = n1.shiftLeft(-e10); d10 = d1; }
    else { throw new RuntimeException("can't get here"); }

    Debug.println(description("n10",n10));
    Debug.println(description("d10",d10));

    // TODO: more efficient test!!!
    final BigInteger d11 = d10.shiftLeft(1);
    final BigInteger d12;
    final int e11;
    if (n10.compareTo(d11) < 0) { d12 = d10; e11 = e10;}
    else { d12 = d11; e11 = e10 + 1; }

    Debug.println(description("n10",n10));
    Debug.println(description("d12",d12));
    Debug.println("e11=" + e11);

    final double z = doubleValue2(negative,e11,n10,d12);

    return z; }

  //--------------------------------------------------------------
  /** Extract sign bit, so numerator and denominator are both
   * positive. Also reduce by gcd.
   * <p>
   * <em>TODO:</em> measure performance effect of reducing/not
   * reducing?
   * 
   * @param n0 numerator
   * @param d0 positive denominator
   * @return closest half-even rounded <code>double</code> to 
   * exact value of <code>n0 / d0</code>.
   */

  public static final double doubleValue0 (final BigInteger n0,
                                           final BigInteger d0) {
    Debug.println();
    Debug.println("doubleValue0(BigInteger,BigInteger)");
    Debug.println(description("n0",n0));
    Debug.println(description("d0",d0));

    assert d0.signum() == 1 : 
      "denominator not strictly positive: " 
      + d0.toString(0x10);

    // extract sign
    final int n0s = n0.signum();

    Debug.println("n0.signum= " + n0s);

    if (n0s == 0) { return 0.0; }
    final boolean negative = (n0s < 0);
    final BigInteger n00 = negative ? n0.negate() : n0;

    Debug.println(description("n00",n00));

    // reduce fraction (optional step)
    // TODO: test performance effect
    final BigInteger gcd = n00.gcd(d0);

    Debug.println("gcd=" + gcd);

    final BigInteger n01,d01;
    if (BigInteger.ONE.equals(gcd)) { n01 = n00; d01 = d0; }
    else { n01 = n00.divide(gcd); d01 = d0.divide(gcd); }

    Debug.println(description("n01",n01));
    Debug.println(description("d01",d01));

    final double z = doubleValue1(negative,n01,d01);

    return z; }

  //--------------------------------------------------------------
  /** Half-even rounding from {@link BigInteger} ratio to 
   * <code>double</code>.
   * @param n numerator
   * @param d positive denominator
   * @return closest half-even rounded double to n / d.
   */

  public static final double doubleValue (final BigInteger n,
                                          final BigInteger d) {
    assert d.signum() == 1 : 
      "denominator not strictly positive: " 
      + d.toString(0x10);

    final double z = doubleValue0(n,d);

    return z; }

  //--------------------------------------------------------------

  @Override
  public final double doubleValue () { 
    return doubleValue(numerator(),denominator()); }

  //--------------------------------------------------------------
  // Comparable methods
  //--------------------------------------------------------------

  @Override
  public final int compareTo (final Rational3 o) {
    final BigInteger n0d1 = numerator().multiply(o.denominator());
    final BigInteger n1d0 = o.numerator().multiply(denominator());
    return n0d1.compareTo(n1d0); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  public final boolean equals (final Rational3 q) {
    if (this == q) { return true; }
    if (null == q) { return false; }
    final BigInteger n0 = numerator(); 
    final BigInteger d0 = denominator(); 
    final BigInteger n1 = q.numerator(); 
    final BigInteger d1 = q.denominator(); 
    return n0.multiply(d1).equals(n1.multiply(d0)); }

  @Override
  public boolean equals (final Object o) {
    if (!(o instanceof Rational3)) { return false; }
    return equals((Rational3) o); }

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

  private Rational3 (final BigInteger numerator,
                    final BigInteger denominator) {
    super();
    assert 1 == denominator.signum();
    _numerator = numerator;
    _denominator = denominator; }

  //--------------------------------------------------------------

  private static final Rational3 reduced (final BigInteger n,
                                         final BigInteger d) {

    if (d.signum() < 0) { return reduced(n.negate(),d.negate()); }

    if (n == BigInteger.ZERO) { 
      return new Rational3(n,BigInteger.ONE); }

    // TODO: any value in this test?
    if ((n == BigInteger.ZERO) || (d == BigInteger.ZERO)) {
      return new Rational3(n,d); }

    final BigInteger gcd = n.gcd(d);
    // TODO: any value in this test?
    if (gcd.compareTo(BigInteger.ONE) > 0) {
      return new Rational3(n.divide(gcd),d.divide(gcd)); } 

    return new Rational3(n,d); }

  //--------------------------------------------------------------

  public static final Rational3 valueOf (final BigInteger n,
                                        final BigInteger d) {
    // TODO: is it better to keep ratio in reduced form or not?
    // return new Rational(n,d); }
    return reduced(n,d); }

  public static final Rational3 valueOf (final long n,
                                        final long d) {
    return valueOf(BigInteger.valueOf(n),BigInteger.valueOf(d)); }

  public static final Rational3 valueOf (final int n,
                                        final int d) {
    return valueOf(BigInteger.valueOf(n),BigInteger.valueOf(d)); }

  //--------------------------------------------------------------

  public static final Rational3 valueOf (final double x)  {
    final BigInteger[] nd = toRatio(x);
    return valueOf(nd[0], nd[1]); }

  public static final Rational3 valueOf (final float x)  {
    final BigInteger[] nd = Floats.toRatio(x);
    return valueOf(nd[0], nd[1]); }

  public static final Rational3 valueOf (final byte x)  {
    return valueOf(BigInteger.valueOf(x), BigInteger.ONE); }

  public static final Rational3 valueOf (final short x)  {
    return valueOf(BigInteger.valueOf(x), BigInteger.ONE); }

  public static final Rational3 valueOf (final int x)  {
    return valueOf(BigInteger.valueOf(x), BigInteger.ONE); }

  public static final Rational3 valueOf (final long x)  {
    return valueOf(BigInteger.valueOf(x), BigInteger.ONE); }

  //--------------------------------------------------------------

  public static final Rational3 valueOf (final Double x)  {
    return valueOf(x.doubleValue()); }

  public static final Rational3 valueOf (final Float x)  {
    return valueOf(x.floatValue()); }

  public static final Rational3 valueOf (final Byte x)  {
    return valueOf(x.byteValue()); }

  public static final Rational3 valueOf (final Short x)  {
    return valueOf(x.shortValue()); }

  public static final Rational3 valueOf (final Integer x)  {
    return valueOf(x.intValue()); }

  public static final Rational3 valueOf (final Long x)  {
    return valueOf(x.longValue()); }

  public static final Rational3 valueOf (final BigDecimal x)  {
    throw Exceptions.unsupportedOperation(null,"valueOf",x); }
  //    return valueOf(x, BigInteger.ONE); }

  public static final Rational3 valueOf (final BigInteger x)  {
    return valueOf(x, BigInteger.ONE); }

  public static final Rational3 valueOf (final Number x)  {
    if (x instanceof Rational3) { return (Rational3) x; }
    if (x instanceof Double) { return valueOf((Double) x); }
    if (x instanceof Float) { return valueOf((Float) x); }
    if (x instanceof Byte) { return valueOf((Byte) x); }
    if (x instanceof Short) { return valueOf((Short) x); }
    if (x instanceof Integer) { return valueOf((Integer) x); }
    if (x instanceof Long) { return valueOf((Long) x); }
    if (x instanceof BigInteger) { return valueOf((BigInteger) x); }
    if (x instanceof BigDecimal) { return valueOf((BigDecimal) x); }
    throw Exceptions.unsupportedOperation(null,"valueOf",x); }

  public static final Rational3 valueOf (final Object x)  {
    return valueOf((Number) x); }

  //--------------------------------------------------------------

  public static final Rational3 ZERO = 
    Rational3.valueOf(BigInteger.ZERO,BigInteger.ONE);

  public static final Rational3 ONE = 
    Rational3.valueOf(BigInteger.ONE,BigInteger.ONE);

  public static final Rational3 TWO = 
    Rational3.valueOf(BigInteger.TWO,BigInteger.ONE);

  public static final Rational3 TEN = 
    Rational3.valueOf(BigInteger.TEN,BigInteger.ONE);

  public static final Rational3 MINUS_ONE = 
    Rational3.valueOf(BigInteger.ONE.negate(),BigInteger.ONE);

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
