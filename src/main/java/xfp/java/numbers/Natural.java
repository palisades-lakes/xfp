package xfp.java.numbers;

import static xfp.java.numbers.Numbers.loWord;
import static xfp.java.numbers.Numbers.unsigned;

import java.math.BigInteger;
import java.util.List;

import xfp.java.exceptions.Exceptions;

/** An interface for nonnegative integers represented as
 * a sequence of <code>int</code> words, treated as unsigned.
 *
 * The value of the natural number is
 * <code>sum<sub>i=startWord()</sub><sup>i&lt;hiInt</sup>
 *  uword(i) * 2<sup>32*i</sup></code>.
 *
 * TODO: utilities class to hide private stuff?
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-22
 */

@SuppressWarnings("unchecked")
public interface Natural
extends Uints<Natural>, Ringlike<Natural> {

  //--------------------------------------------------------------
  // ordering
  //--------------------------------------------------------------

  @Override
  default int compareTo (final Natural u) {
    // TODO: should really compare hiBits
    final int b0 = hiBit();
    final int b1 = u.hiBit();
    if (b0<b1) { return -1; }
    if (b0>b1) { return 1; }
    final int end = Math.max(hiInt(),u.hiInt()) - 1;
    final int start = Math.min(startWord(),u.startWord());
    for (int i=end;i>=start;i--) {
      final long u0i = uword(i);
      final long u1i = u.uword(i);
      if (u0i<u1i) { return -1; }
      if (u0i>u1i) { return 1; } }
    return 0; }

  default int compareTo (final Natural u,
                         final int upShift) {
    assert 0<=upShift;
    if (0==upShift) { return compareTo(u); }
    final int bShift = (upShift&0x1F);
    if (0!=bShift) { return compareTo(u.shiftUp(upShift)); }
    final int iShift = (upShift>>>5);
    final int n0 = hiInt() - iShift;
    final int n1 = u.hiInt();
    if (n0 < n1) { return -1; }
    if (n0 > n1) { return 1; }
    // TODO: is this faster than unsigned long conversion?
    for (int i = n0-1; i>=0; i--) {
      final int c = 
        Integer.compareUnsigned(word(i+iShift),u.word(i));
      if (0!=c) { return c; } }
    return 0; }

  default int compareTo (final int upShift,
                         final Natural u) {
    return - u.compareTo(this,upShift); }

  default int compareTo (final long u) {
    assert 0L<=u;
    if (2<hiInt()) { return 1; }
    final long hi0 = uword(1);
    final long hi1 = Numbers.hiWord(u);
    if (hi0<hi1) { return -1; }
    if (hi0>hi1) { return 1; }
    final long lo0 = uword(0);
    final long lo1 = Numbers.loWord(u);
    if (lo0<lo1) { return -1; }
    if (lo0>lo1) { return 1; }
    return 0; }

  default int compareTo (final long u,
                         final int upShift) {
    assert 0L<=u;
    assert 0<=upShift : "upShift=" + upShift;
    if (0==upShift) { return compareTo(u); }
    if (0L==u) {
      if (isZero()) { return 0; }
      return 1; }

    final int n0 = hiBit();
    final int n1 = Numbers.hiBit(u) + upShift;
    if (n0<n1) { return -1; }
    if (n0>n1) { return 1; }

    final int iShift = (upShift>> 5);
    final int rShift = (upShift&0x1f);

    // compare non-zero words from u<<upShift
    if (0==rShift) {
      final long hi0 = uword(iShift+1);
      final long hi1 = Numbers.hiWord(u);
      if (hi0<hi1) { return -1; }
      if (hi0>hi1) { return 1; }
      final long lo0 = uword(iShift);
      final long lo1 = Numbers.loWord(u);
      if (lo0<lo1) { return -1; }
      if (lo0>lo1) { return 1; } }
    else {
      // most significant word in u << upShift
      final long hi0 = uword(iShift+2);
      final long hi1 = (u>>>(64-rShift));
      if (hi0<hi1) { return -1; }
      if (hi0>hi1) { return 1; }

      final long us = (u << rShift);
      final long mid0 = uword(iShift+1);
      final long mid1 = Numbers.hiWord(us);
      if (mid0<mid1) { return -1; }
      if (mid0>mid1) { return 1; }

      final long lo0 = uword(iShift);
      final long lo1 = Numbers.loWord(us);
      if (lo0<lo1) { return -1; }
      if (lo0>lo1) { return 1; } }

    // check this for any non-zero words in zeros of u<<upShift
    for (int i=iShift-1;i>=startWord();i--) {
      if (0!=uword(i)) { return 1; } }

    return 0; }

  //--------------------------------------------------------------
  // arithmetic
  //--------------------------------------------------------------
  // additive group
  //--------------------------------------------------------------
  // implementations usually return a pre-allocated constant

  @Override
  default Natural zero () { return empty(); }

  @Override
  default boolean isZero () {
    for (int i=0;i<hiInt();i++) {
      if (0!=word(i)) { return false; } }
    return true; }

  // Natural numbers are nonnegative

  @Override
  default Natural abs () { return this; }

  //--------------------------------------------------------------

  @Override
  default Natural add (final Natural u) {
    Natural t = zero();
    if (isZero()) { return u; }
    if (u.isZero()) { return this; }
    // TODO: optimize by summing over joint range
    // and just carrying after that
    final int end = Math.max(hiInt(),u.hiInt());
    long sum = 0L;
    long carry = 0L;
    int i=0;
    for (;i<end;i++) {
      sum = uword(i) + u.uword(i) + carry;
      carry = (sum>>>32);
      t = t.setWord(i,(int) sum); }
    if (0L!=carry) { t = t.setWord(i,(int) carry); }
    return t; }

  default Natural add (final Natural u,
                       final int shift) {
    assert 0<=shift;
    if (isZero()) { return u.shiftUp(shift); }
    if (u.isZero()) { return this; }
    if (0==shift) { return add(u); }
    // TODO: reduce to single op?
    // TODO: shiftUp mutable version and add this to that
    return add(u.shiftUp(shift)); }

  default Natural add (final long u) {
    assert 0L<=u;
    if (0L==u) { return this; }
    if (isZero()) { return from(u); }
    Natural v = this;
    long sum = uword(0) + Numbers.loWord(u);
    v = v.setWord(0,(int) sum);
    long carry = (sum>>>32);
    sum = uword(1) + Numbers.hiWord(u) + carry;
    v = v.setWord(1,(int) sum);
    carry = (sum>>>32);
    int i=2;
    final int n = hiInt();
    for (;(0L!=carry)&&(i<n);i++) {
      sum = uword(i) + carry;
      v = v.setWord(i,(int) sum);
      carry = (sum>>>32); }
    if (0L!=carry) { v = v.setWord(i,(int) carry); }
    return v; }

  default Natural add (final long u,
                       final int upShift) {
    assert 0<=upShift;
    if (0==upShift) { return add(u); }
    assert 0L<=u;
    if (0L==u) { return this; }
    return add(from(u,upShift)); }

  //--------------------------------------------------------------

  @Override
  default Natural subtract (final Natural u) {
    // TODO: fast correct check of u<=this?
    assert 0<=compareTo(u);
    if (u.isZero()) { return this; }
    assert ! isZero();
    Natural v = this;
    long dif = 0L;
    long borrow = 0L;
    final int n = Math.max(hiInt(),u.hiInt());
    int i=0;
    // TODO: optimize by differencing over shared range
    // and then just borrowing
    for (;i<n;i++) {
      dif = (uword(i) - u.uword(i)) + borrow;
      borrow = (dif>>32);
      v = v.setWord(i,(int) dif); }
    assert 0L==borrow;
    return v; }

  default Natural subtract (final long u) {
    assert 0L<=u;
    assert 0<=compareTo(u);
    if (0L==u) { return this; }
    assert 0L<=u;
    if (0L==u) { return this; }
    assert ! isZero();
    final long lo = Numbers.loWord(u);
    final long hi = Numbers.hiWord(u);
    if (0L!=hi) { assert 2<=hiInt(); }
    if (0L!=lo) { assert 1<=hiInt(); }
    Natural v = this;
    long dif = uword(0)-lo;
    v = v.setWord(0,(int) dif);
    long borrow = (dif>>32);
    dif = (uword(1)-hi)+borrow;
    v = v.setWord(1,(int) dif);
    borrow = (dif>>32);
    int i=2;
    final int n = hiInt();
    for (;(0L!=borrow)&&(i<n);i++) {
      dif = uword(i)+borrow;
      v = v.setWord(i,(int) dif);
      borrow = (dif>>32); }
    assert 0L==borrow : borrow;
    return v; }

  default Natural subtract (final long u,
                            final int upShift) {
    assert 0L<=u;
    assert 0<=upShift;
    assert compareTo(u,upShift)>=0;
    return subtract(from(u,upShift)); }

  //--------------------------------------------------------------

  default Natural subtractFrom (final long u) {
    assert 0L<=u;
    assert compareTo(u)<=0;
    return from(u).subtract(this); }

  default Natural subtractFrom (final long u,
                                final int upShift) {
    assert 0L<=u;
    assert 0<=upShift;
    assert compareTo(u,upShift)<=0;
    return from(u,upShift).subtract(this); }

  //--------------------------------------------------------------

  @Override
  default Natural absDiff (final Natural u) {
    final int c = compareTo(u);
    if (c==0) { return zero(); }
    if (c<0) { return u.subtract(this); }
    return subtract(u); }

  //--------------------------------------------------------------
  // multiplicative monoid
  //--------------------------------------------------------------
  // TODO: singleton class for one() and zero()?

  @Override
  default Natural one () {
    throw Exceptions.unsupportedOperation(this,"one"); }

  default Natural ones (final int n) {
    throw Exceptions.unsupportedOperation(this,"ones",n); }

  @Override
  default boolean isOne () {
    if (1!=word(0)) { return false; }
    for (int i=Math.max(1,startWord());i<hiInt();i++) {
      if (0!=word(i)) { return false; } }
    return true; }

  //--------------------------------------------------------------
  // square
  //--------------------------------------------------------------

  @Override
  default Natural square () {
    return NaturalMultiply.square(this); }

  //--------------------------------------------------------------
  /** Return a {@link Natural} whose value is <code>t</code>.
   * @see #multiply(long,long) 
   */

  default Natural square (final long t) {
    assert 0L<=t;
    final long hi = Numbers.hiWord(t);
    final long lo = loWord(t);
    long sum = lo*lo;
    final int m0 = (int) sum;
    sum = (sum>>>32) + ((hi*lo)<<1);
    final int m1 = (int) sum;
    sum = (sum>>>32) +  hi*hi ;
    final int m2 = (int) sum;
    final int m3 = (int) (sum>>>32);
    Natural u = zero();
    if (0!=m0) { u = u.setWord(0,m0); }
    if (0!=m1) { u = u.setWord(1,m1); }
    if (0!=m2) { u = u.setWord(2,m2); }
    if (0!=m3) { u = u.setWord(3,m3); }
    return u; }

  //--------------------------------------------------------------
  // multiply
  //--------------------------------------------------------------

  @Override
  default Natural multiply (final Natural u) {
    return NaturalMultiply.multiply(this,u); }

  //--------------------------------------------------------------

  default Natural multiply (final long u) {
    return NaturalMultiply.multiply(this,u); }

  default Natural multiply (final long u,
                            final int upShift) {
    assert 0L<=u;
    if (0L==u) { return zero(); }
    assert 0<=upShift;
    if (0==upShift) { return multiply(u); }
    return multiply(from(u,upShift)); }

  default Natural fromProduct (final long t0,
                               final long t1) {
    assert 0L<=t0;
    assert 0L<=t1;
    final long hi0 = Numbers.hiWord(t0);
    final long lo0 = loWord(t0);
    final long hi1 = Numbers.hiWord(t1);
    final long lo1 = loWord(t1);
    final long lolo = lo0*lo1;
    final long hilo2 = (hi0*lo1) + (hi1*lo0);
    final long hihi = hi0*hi1;
    long sum = lolo;
    final int m0 = (int) sum;
    sum = (sum>>>32) + hilo2;
    final int m1 = (int) sum;
    sum = (sum>>>32) + hihi ;
    final int m2 = (int) sum;
    final int m3 = (int) (sum>>>32);
    Natural u = zero();
    if (0!=m0) { u = u.setWord(0,m0); }
    if (0!=m1) { u = u.setWord(1,m1); }
    if (0!=m2) { u = u.setWord(2,m2); }
    if (0!=m3) { u = u.setWord(3,m3); }
    return u; }

  //--------------------------------------------------------------
  // division
  //-------------------------------------------------------------

  // for testing
  default List<Natural> divideAndRemainderKnuth (final Natural u) {
    return NaturalDivide.divideAndRemainderKnuth(this,u); }

  // for testing
  default List<Natural> divideAndRemainderBurnikelZiegler (final Natural u) {
    return NaturalDivide.divideAndRemainderBurnikelZiegler(this,u); }

  @Override
  default List<Natural> divideAndRemainder (final Natural u) {
    return NaturalDivide.divideAndRemainder(this,u); }

  @Override
  default Natural divide (final Natural u) {
    return divideAndRemainder(u).get(0); }

  @Override
  default Natural remainder (final Natural u) {
    return divideAndRemainder(u).get(1); }

  //--------------------------------------------------------------
  // gcd
  //--------------------------------------------------------------
  /** Use Euclid until the numbers are approximately the
   * same length, then use the Knuth algorithm.
   */

  @Override
  default Natural gcd (final Natural u) { 
    return NaturalDivide.gcd(this,u); }

  //--------------------------------------------------------------

  @Override
  default List<Natural> reduce (final Natural d) {
    return NaturalDivide.reduce(this,d); }

  //--------------------------------------------------------------

  @Override
  default Natural invert () { 
    throw Exceptions.unsupportedOperation(this,"invert"); }

  //--------------------------------------------------------------
  // 'Number' interface
  //--------------------------------------------------------------
  // TODO: exceptions on truncation?

  @Override
  default int intValue () { return word(0); }

  @Override
  default long longValue () {
    return (uword(1)<<32) | uword(0); }

  //--------------------------------------------------------------

  @Override
  default float floatValue () {
    if (isZero()) { return 0.0F; }
    final int n = hiInt()-1;
    final int exponent = hiBit()-1;
    // exponent == floor(log2(abs(this)))
    if (exponent < (Long.SIZE - 1)) { return longValue(); }
    else if (exponent > Float.MAX_EXPONENT) {
      return Float.POSITIVE_INFINITY; }

    // We need the top SIGNIFICAND_WIDTH bits, including the
    // "implicit" one bit. To make rounding easier, we pick out
    // the top SIGNIFICAND_WIDTH + 1 bits, so we have one to help
    //us round up or down. twiceSignifFloor will contain the top
    // SIGNIFICAND_WIDTH + 1 bits, and signifFloor the top
    // SIGNIFICAND_WIDTH.
    // It helps to consider the real number signif = abs(this) *
    // 2^(SIGNIFICAND_WIDTH - 1 - exponent).
    final int shift = exponent - Floats.SIGNIFICAND_BITS;
    final int nBits = shift & 0x1f;
    final int nBits2 = 32 - nBits;
    final int w0 = word(n);
    final int w1 = word(n-1);
    // twiceSignifFloor == abs().shiftDown(shift).intValue()
    // shift into an int directly 
    int twiceSignifFloor;
    if (nBits == 0) { twiceSignifFloor = w0; }
    else {
      twiceSignifFloor = (w0 >>> nBits);
      if (twiceSignifFloor == 0) {
        twiceSignifFloor = (w0 << nBits2) | (w1 >>> nBits); } }

    int signifFloor = (twiceSignifFloor >> 1);
    signifFloor &= Floats.STORED_SIGNIFICAND_MASK;
    // We round up if either the fractional part of signif is
    // strictly greater than 0.5 (which is true if the 0.5 bit is
    // set and any lower bit is set), or if the fractional part of
    // signif is >= 0.5 and signifFloor is odd (which is true if
    // both the 0.5 bit and the 1 bit are set). This is equivalent
    // to the desired HALF_EVEN rounding.
    final boolean increment =
      ((twiceSignifFloor & 1) != 0) 
      && 
      (((signifFloor & 1) != 0) || (loBit() < shift));
    final int signifRounded = signifFloor+(increment ? 1 : 0);
    int bits = ((exponent+Floats.EXPONENT_BIAS)) << 
      (Floats.SIGNIFICAND_BITS-1);
    bits += signifRounded;
    // If signifRounded == 2^24, we'd need to set all of the
    // significand bits to zero and add 1 to the exponent. This is 
    // exactly the behavior we get from just adding signifRounded 
    // to bits directly. If the exponent is Float.MAX_EXPONENT, we 
    // round up (correctly) to Float.POSITIVE_INFINITY.
    bits |= 1 & Floats.SIGN_MASK;
    return Float.intBitsToFloat(bits); }

  //--------------------------------------------------------------

  @Override
  default double doubleValue () {
    if (isZero()) { return 0.0; }
    final int n = hiInt()-1;
    final int exponent = hiBit()-1;
    // exponent == floor(log2(abs(this))Double)
    if (exponent < (Long.SIZE - 1)) { return longValue(); }
    else if (exponent > Double.MAX_EXPONENT) {
      return Double.POSITIVE_INFINITY; }

    // We need the top SIGNIFICAND_WIDTH bits, including the
    // "implicit" one bit. To make rounding easier, we pick out
    // the top SIGNIFICAND_WIDTH + 1 bits, so we have one to help
    // us round up or down. twiceSignifFloor will contain the top
    // SIGNIFICAND_WIDTH + 1 bits, and signifFloor the top
    // SIGNIFICAND_WIDTH.
    // It helps to consider the real number signif = abs(this) *
    // 2^(SIGNIFICAND_WIDTH - 1 - exponent).
    final int shift = exponent - Doubles.SIGNIFICAND_BITS;
    final int nBits = shift & 0x1f;
    final int nBits2 = 32 - nBits;
    final int w0 = word(n);
    final int w1 = word(n-1);
    int highBits;
    int lowBits;
    // twiceSignifFloor == abs().shiftDown(shift).intValue()
    // shift into an int directly 
    long twiceSignifFloor;
    if (nBits == 0) {
      highBits = w0;
      lowBits = w1; }
    else {
      highBits = (w0 >>> nBits);
      lowBits = (w0 << nBits2) | (w1 >>> nBits);
      if (highBits == 0) {
        final int w2 = word(n-2);
        highBits = lowBits;
        lowBits = (w1 << nBits2) | (w2 >>> nBits); } }

    twiceSignifFloor =
      (unsigned(highBits) << 32) | unsigned(lowBits);

    // remove the implied bit
    final long signifFloor =
      (twiceSignifFloor >> 1) & Doubles.STORED_SIGNIFICAND_MASK;
    // We round up if either the fractional part of signif is
    // strictly greater than 0.5 (which is true if the 0.5 bit
    // is set and any lower bit is set), or if the fractional
    // part of signif is >= 0.5 and signifFloor is odd (which is
    // true if both the 0.5 bit and the 1 bit are set). This is
    // equivalent to the desired HALF_EVEN rounding.

    final boolean increment =
      ((twiceSignifFloor
        & 1) != 0) && (((signifFloor & 1) != 0)
          || (loBit() < shift));
    final long signifRounded =
      increment ? signifFloor + 1 : signifFloor;
    long bits =
      (long) ((exponent
        + Doubles.EXPONENT_BIAS)) << Doubles.STORED_SIGNIFICAND_BITS;
    bits += signifRounded;
    // If signifRounded == 2^53, we'd need to set all of the
    // significand bits to zero and add 1 to the exponent. This is
    // exactly the behavior we get from just adding signifRounded
    // to bits directly. If the exponent is Double.MAX_EXPONENT,
    // we round up (correctly) to Double.POSITIVE_INFINITY.
    bits |= 1 & Doubles.SIGN_MASK;
    return Double.longBitsToDouble(bits); }

  //--------------------------------------------------------------
  // factories for default implementation
  //--------------------------------------------------------------

//  static Natural get (final long u) {
//    assert 0L<=u;
//    return NaturalBEI.valueOf(u); }
//
//  static Natural get (final BigInteger u) {
//    assert 0<=u.signum();
//    return NaturalBEI.valueOf(u); }
//
//  static Natural get (final String u,
//                      final int radix) {
//    return NaturalBEI.valueOf(u,radix); }

  static Natural get (final String u,
                      final int radix) {
    return NaturalLE.valueOf(u,radix); }

  static Natural get (final BigInteger u) {
    assert 0<=u.signum();
    return NaturalLE.valueOf(u); }

  static Natural get (final long u) {
    assert 0L<=u;
    return NaturalLE.valueOf(u); }

  static Natural get (final int u) {
    return get(Numbers.unsigned(u)); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
