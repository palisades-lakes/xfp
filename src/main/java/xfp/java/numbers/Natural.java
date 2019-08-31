package xfp.java.numbers;

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
 * @version 2019-08-30
 */

@SuppressWarnings("unchecked")
public interface Natural
extends Ringlike<Natural>, Transience<Natural> {

  //--------------------------------------------------------------
  // word ops
  //--------------------------------------------------------------
  /** The <code>i</code>th (little endian) word as an 
   * <code>int</code>. <br>
   * <em>WARNING:</em> content used as unsigned.
   */

  int word (final int i);

  /** Return a sequence where the <code>i</code>th 
   * (little endian) word is <code>w</code>.
   * May return a new sequence, or may modify <code>this</code>.
   * Existing references to <code>this</code> may no longer be
   * valid.
   */

  default Natural setWord (final int i,
                           final int w) {
    throw Exceptions.unsupportedOperation(this,"setWord",i,w); }

  /** The value of the unsigned <code>i</code>th (little endian) 
   * word as a <code>long</code>.
   */

  long uword (final int i);

  //  default long uword (final int i) {
  //    return unsigned(word(i)); }

  //--------------------------------------------------------------
  /** Inclusive lower bound on non-zero words:
   * <code>0L==uword(i)</code> for
   * <code>startWord()>i</code>.
   * Doesn't guarantee that <code>0L!=uword(i)</code> for
   * <code>startWord()&lt;=i</code>.
   * Zero may have no words, in which case,
   * {@link #startWord()} <code>==</code> {@link #endWord()}
   * <code>==0</code>.
   * <p>
   * This is intended to be a fast to return, looser bound than
   * @{link {@link #loInt()}.
   */

  int startWord ();

  /** Exclusive upper bound for non-zero words:
   * <code>0L==uword(i)</code> for
   * <code>endWord()<=i</code>.
   * Doesn't guarantee that <code>0L!=uword(i)</code> for
   * <code>endWord()&gt;i</code>.
   * <p>
   * This is intended to be a fast to return, looser bound than
   * @{link {@link #hiInt()}.
   */

  int endWord ();

  //--------------------------------------------------------------
  /** Return the index of the lowest non-zero word,
   * unless all words are zero, in which case,
   * {@link #loInt()} <code>==</code> {@link #hiInt()}
   * <code>==0</code>.
   * Guarantees <code>0!=word(loInt())</code> unless
   * {@link #loInt()} <code>==</code> {@link #hiInt()}
   * <code>==0</code>.
   */

  default int loInt () {
    // Search for lowest order nonzero int
    final int n = hiInt(); // might be 0
    for (int i=startWord();i<n;i++) {
      if (0!=word(i)) { return i; } }
    //assert 0==n;
    return 0; }

  /** Return the index of the highest non-zero word.
   * unless all words are zero, in which case,
   * {@link #loInt()} <code>==</code> {@link #hiInt()}
   * <code>==0</code>.
   * Guarantees <code>0!=word(hiInt()-1)</code> unless
   * {@link #loInt()} <code>==</code> {@link #hiInt()}
   * <code>==0</code>.
   */

  default int hiInt () {
    final int start = startWord();
    for (int i = endWord()-1;i>=start;i--) {
      if (0!=word(i) ) { return i+1; } }
    //assert 0==start;
    return 0; }

  //--------------------------------------------------------------

  //  default Natural clear () {
  //    throw Exceptions.unsupportedOperation(this,"clear"); }

  //--------------------------------------------------------------

  //  default Natural empty () {
  //    throw Exceptions.unsupportedOperation(this,"empty"); }

  //--------------------------------------------------------------
  /** Return the <code>[i0,i1)</code> words as a new 
   * <code>Natural</code> with <code>[0,i1-i0)</code> words.
   */

  default Natural words (final int i0,
                         final int i1) {
    //assert 0<=i0;
    //assert i0<i1;
    if ((0==i0) && (hiInt()<=i1)) { return copy(); }
    final int n = Math.max(0,i1-i0);
    if (0>=n) { return zero(); }
    Natural u = zero();
    for (int i=0;i<n;i++) { u =  u.setWord(i,word(i+i0)); }
    return u; }

  //--------------------------------------------------------------
  /** Return a sequence whose value is the same as <code>u</code>.
   * May be the same object as <code>this</code>, if mutable.
   * May be <code>u</code>, if immutable.
   * May be a copy of <code>u</code>.
   */

  //  default Natural set (final Natural u) {
  //    Natural x = clear();
  //    for (int i=u.startWord();i<u.endWord();i++) {
  //      x = x.setWord(i,u.word(i)); }
  //    return this; }

  /** Return a Natural whose value is the same as <code>u</code>.
   */

  //  default Natural set (final long u) {
  //    //assert 0<=u;
  //    // TODO: optimize zeroing internal array?
  //    clear();
  //    final long lo = Numbers.loWord(u);
  //    if (0!=lo) { setWord(0,(int) lo); }
  //    final long hi = Numbers.hiWord(u);
  //    if (0!=hi) { setWord(1,(int) hi); }
  //    return this; }

  //--------------------------------------------------------------
  // bit ops
  //--------------------------------------------------------------

  /** Return the index of the lowest non-zero bit,
   * unless all bits are zero, in which case,
   * {@link #loBit()} <code>==</code> {@link #hiBit()}
   * <code>==0</code>.
   */

  default int loBit () {
    // Search for lowest order nonzero int
    final int i=loInt(); 
    if (i==hiInt()) { return 0; } // all bits zero
    return (i<<5) + Integer.numberOfTrailingZeros(word(i)); }

  /** Return <code>1 +</code> index of the highest non-zero bit.
   * If all bits are zero, 
   * {@link #loBit()} <code>==</code> {@link #hiBit()}
   * <code>==0</code>.
   */

  default int hiBit () {
    //Debug.println("hiBit this=" + this);
    final int i = hiInt()-1;
    if (0>i) { return 0; }
    final int wi = word(i);
    return (i<<5)+Integer.SIZE-Integer.numberOfLeadingZeros(wi); }

  //--------------------------------------------------------------

  default boolean testBit (final int n) {
    //assert 0<=n;
    final int w = word(n>>>5);
    final int b = (1 << (n&0x1F));
    return 0!=(w&b); }

  default Natural setBit (final int i) {
    //assert 0<=i;
    final int iw = (i>>>5);
    final int w = word(iw);
    final int ib = (i&0x1F);
    return setWord(iw,(w|(1<<ib))); }

  //  default Natural clearBit (final int n) {
  //    //assert 0<=n;
  //    final int iw = (n>>>5);
  //    final int w = word(iw);
  //    final int ib = (n&0x1F);
  //    return setWord(iw,(w&(~(1<<ib)))); }

  //  default Natural flipBit (final int n) {
  //    //assert 0<=n;
  //    final int iw = (n>>>5);
  //    final int w = word(iw);
  //    final int ib = (n&0x1F);
  //    return setWord(iw,(w^(1<<ib))); }

  //--------------------------------------------------------------

  default Natural shiftDown (final int shift) {
    throw Exceptions.unsupportedOperation(this,"shiftUp",shift); }

  //--------------------------------------------------------------

  default Natural shiftUp (final int shift) {
    throw Exceptions.unsupportedOperation(this,"shiftUp",shift); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  /** Return a new Natural whose value is <code>u</code>.
   * Does not modify <code>this</code>.
   */

  default Natural from (final long u) {
    throw Exceptions.unsupportedOperation(this,"from",u); }

  /** Return a new Natural whose value is <code>u</code>,
   * interpreted as unsigned.
   * Does not modify <code>this</code>.
   */

  //  default Natural from (final int u) { return from(unsigned(u)); }

  /** Return a new Natural whose value is
   * <code>u * 2<sup>shift</sup></code>.
   * Does not modify <code>this</code>.
   *
   * <code>0&lt;=shift</code>
   */

  default Natural from (final long u,
                        final int upShift) {
    throw Exceptions.unsupportedOperation(this,"from",u,upShift); }
  //    return (T) from(u).shiftUp(shift); }

  //--------------------------------------------------------------
  // 'Number' interface
  //--------------------------------------------------------------

  default byte[] littleEndianBytes () {
    final int hi = hiBit();
    // an extra zero byte to avoid getting a negative
    // two's complement input to new BigInteger(b).
    final int n = 1 + ((hi)/8);
    final byte[] b = new byte[n];
    int j = 0;
    int w = 0;
    for (int i=0;i<n;i++) {
      if (0==(i%4)) { w = word(j++); }
      else { w = (w>>>8); }
      b[i] = (byte) w; }
    return b; }

  default byte[] bigEndianBytes () {
    final int hi = hiBit();
    // an extra zero byte to avoid getting a negative
    // two's complement input to new BigInteger(b).
    final int n = 1 + ((hi)/8);
    final byte[] b = new byte[n];
    int j = 0;
    int w = 0;
    for (int i=0;i<n;i++) {
      if (0==(i%4)) { w = word(j++); }
      else { w = (w>>>8); }
      b[n-1-i] = (byte) w; }
    return b; }

  default BigInteger bigIntegerValue () {
    return new BigInteger(bigEndianBytes()); }

  default String toHexString () {
    final StringBuilder b = new StringBuilder("");
    final int n = hiInt()-1;
    if (0>n) { b.append('0'); }
    else {
      b.append(String.format("%x",Long.valueOf(uword(n))));
      for (int i=n-1;i>=0;i--) {
        //b.append(" ");
        b.append(String.format("%08x",Long.valueOf(uword(i)))); } }
    return b.toString(); }

  //--------------------------------------------------------------
  // 'Object' interface
  //--------------------------------------------------------------

  default int uintsHashCode () {
    int hashCode = 0;
    for (int i=startWord(); i<hiInt(); i++) {
      hashCode = (int) ((31 * hashCode) + uword(i)); }
    return hashCode; }

  // DANGER: equality across classes
  default boolean uintsEquals (final Natural x) {
    if (x==this) { return true; }
    final Natural u = x;
    final int n0 = startWord();
    if (n0!=u.startWord()) { return false; }
    final int n1 = hiInt();
    if (n1!=u.hiInt()) { return false; }
    for (int i=n0; i<n1; i++) {
      if (word(i)!=u.word(i)) { return false; } }
    return true; }

  //--------------------------------------------------------------
  // ordering
  //--------------------------------------------------------------

  @Override
  default int compareTo (final Natural u) {
    //assert isValid();
    //assert u.isValid();
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
    return compareTo(u.shiftUp(upShift)); }

  default int compareTo (final int upShift,
                         final long u) {
    return shiftUp(upShift).compareTo(u); }

  default int compareTo (final int upShift,
                         final Natural u) {
    //assert isValid();
    //assert u.isValid();
    return -u.compareTo(this,upShift); }

  default int compareTo (final long u) {
    //assert isValid();
    //assert 0L<=u;
    final int n0 = hiInt();
    final long lo1 = Numbers.loWord(u);
    final long hi1 = Numbers.hiWord(u);
    final int n1 = ((0L!=hi1) ? 2 : (0L!=lo1) ? 1 : 0);
    if (n0<n1) { return -1; }
    if (n0>n1) { return 1; }
    final long hi0 = uword(1);
    if (hi0<hi1) { return -1; }
    if (hi0>hi1) { return 1; }
    final long lo0 = uword(0);
    if (lo0<lo1) { return -1; }
    if (lo0>lo1) { return 1; }
    return 0; }

  default int compareTo (final long u,
                         final int upShift) {
    //assert isValid();
    //assert 0L<=u;
    //assert 0<=upShift : "upShift=" + upShift;

    if (0==upShift) { return compareTo(u); }
    if (0L==u) { return (isZero() ? 0 : 1); }

    final int m0 = hiBit();
    final int m1 = Numbers.hiBit(u) + upShift;
    if (m0<m1) { return -1; }
    if (m0>m1) { return 1; }

    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);

    // compare non-zero words from u<<upShift
    if (0==bShift) {
      final long hi0 = uword(iShift+1);
      final long hi1 = Numbers.hiWord(u);
      if (hi0<hi1) { return -1; }
      if (hi0>hi1) { return 1; }
      final long lo0 = uword(iShift);
      final long lo1 = Numbers.loWord(u);
      if (lo0<lo1) { return -1; }
      if (lo0>lo1) { return 1; } }
    else {
      // most significant word in u<<upShift
      final long hi0 = uword(iShift+2);
      final long hi1 = (u>>>(64-bShift));
      if (hi0<hi1) { return -1; }
      if (hi0>hi1) { return 1; }

      final long us = (u<<bShift);
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
  default Natural zero () { 
    throw Exceptions.unsupportedOperation(this,"zero"); }

  @Override
  default boolean isZero () {
    //assert isValid();
    for (int i=0;i<hiInt();i++) {
      if (0!=word(i)) { return false; } }
    return true; }

  // Natural numbers are nonnegative

  @Override
  default Natural abs () {
    //assert isValid();
    return this; }

  //--------------------------------------------------------------

  Natural add (final Natural u,
               final int upShift);

  //--------------------------------------------------------------

  @Override
  default Natural absDiff (final Natural u) {
    //assert isValid();
    //assert u.isValid();
    final int c = compareTo(u);
    if (c==0) { return zero(); }
    if (c<0) { return u.subtract(this); }
    return subtract(u); }

  //--------------------------------------------------------------
  // multiplicative monoid
  //--------------------------------------------------------------
  // TODO: singleton class for one() and zero()?

  default Natural ones (final int n) {
    throw Exceptions.unsupportedOperation(this,"ones",n); }

  @Override
  default boolean isOne () {
    //assert isValid();
    if (1!=word(0)) { return false; }
    for (int i=Math.max(1,startWord());i<endWord();i++) {
      if (0!=word(i)) { return false; } }
    return true; }

  //--------------------------------------------------------------
  // square
  //--------------------------------------------------------------

  @Override
  default Natural square () {
    //assert isValid();
    return NaturalMultiply.square(this); }

  //--------------------------------------------------------------
  // multiply
  //--------------------------------------------------------------

  @Override
  default Natural multiply (final Natural u) {
    //assert isValid();
    //assert u.isValid();
    return NaturalMultiply.multiply(this,u); }

  //--------------------------------------------------------------

  default Natural multiply (final long u) {
    //assert isValid();
    return NaturalMultiply.multiply(this,u); }

  default Natural multiply (final long u,
                            final int upShift) {
    //assert isValid();
    //assert 0L<=u;
    //assert 0<=upShift;
    if (0L==u) { return zero(); }
    if (0==upShift) { return multiply(u); }
    if (isZero()) { return this; }
    return multiply(from(u,upShift)); }

  //--------------------------------------------------------------
  // division
  //-------------------------------------------------------------

  // for testing
  default List<Natural> divideAndRemainderKnuth (final Natural u) {
    //assert isValid();
    //assert u.isValid();
    return NaturalDivide.divideAndRemainderKnuth(this,u); }

  // for testing
  default List<Natural> divideAndRemainderBurnikelZiegler (final Natural u) {
    //assert isValid();
    //assert u.isValid();
    return NaturalDivide.divideAndRemainderBurnikelZiegler(this,u); }

  @Override
  default List<Natural> divideAndRemainder (final Natural u) {
    //assert isValid();
    //assert u.isValid();
    return NaturalDivide.divideAndRemainder(this,u); }

  @Override
  default Natural divide (final Natural u) {
    //assert isValid();
    //assert u.isValid();
    return divideAndRemainder(u).get(0); }

  @Override
  default Natural remainder (final Natural u) {
    //assert isValid();
    //assert u.isValid();
    return divideAndRemainder(u).get(1); }

  //--------------------------------------------------------------
  // gcd
  //--------------------------------------------------------------
  /** Use Euclid until the numbers are approximately the
   * same length, then use the Knuth algorithm.
   */

  @Override
  default Natural gcd (final Natural u) { 
    //assert isValid();
    //assert u.isValid();
    return NaturalDivide.gcd(this,u); }

  //--------------------------------------------------------------

  @Override
  default List<Natural> reduce (final Natural d) {
    //assert isValid();
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
  default int intValue () { 
    //assert isValid();
    return word(0); }

  @Override
  default long longValue () {
    //assert isValid();
    return (uword(1)<<32) | uword(0); }

  //--------------------------------------------------------------

//  @Override
//  default float floatValue () {
//    //assert isValid();
//    if (isZero()) { return 0.0F; }
//    final int n = hiInt()-1;
//    final int exponent = hiBit()-1;
//    // exponent == floor(log2(abs(this)))
//    if (exponent < (Long.SIZE - 1)) { return longValue(); }
//    else if (exponent > Float.MAX_EXPONENT) {
//      return Float.POSITIVE_INFINITY; }
//
//    // We need the top SIGNIFICAND_WIDTH bits, including the
//    // "implicit" one bit. To make rounding easier, we pick out
//    // the top SIGNIFICAND_WIDTH + 1 bits, so we have one to help
//    //us round up or down. twiceSignifFloor will contain the top
//    // SIGNIFICAND_WIDTH + 1 bits, and signifFloor the top
//    // SIGNIFICAND_WIDTH.
//    // It helps to consider the real number signif = abs(this) *
//    // 2^(SIGNIFICAND_WIDTH - 1 - exponent).
//    final int shift = exponent - Floats.SIGNIFICAND_BITS;
//    final int nBits = shift & 0x1f;
//    final int nBits2 = 32 - nBits;
//    final int w0 = word(n);
//    final int w1 = word(n-1);
//    // twiceSignifFloor == abs().shiftDown(shift).intValue()
//    // shift into an int directly 
//    int twiceSignifFloor;
//    if (nBits == 0) { twiceSignifFloor = w0; }
//    else {
//      twiceSignifFloor = (w0 >>> nBits);
//      if (twiceSignifFloor == 0) {
//        twiceSignifFloor = (w0 << nBits2) | (w1 >>> nBits); } }
//
//    int signifFloor = (twiceSignifFloor >> 1);
//    signifFloor &= Floats.STORED_SIGNIFICAND_MASK;
//    // We round up if either the fractional part of signif is
//    // strictly greater than 0.5 (which is true if the 0.5 bit is
//    // set and any lower bit is set), or if the fractional part of
//    // signif is >= 0.5 and signifFloor is odd (which is true if
//    // both the 0.5 bit and the 1 bit are set). This is equivalent
//    // to the desired HALF_EVEN rounding.
//    final boolean increment =
//      ((twiceSignifFloor & 1) != 0) 
//      && 
//      (((signifFloor & 1) != 0) || (loBit() < shift));
//    final int signifRounded = signifFloor+(increment ? 1 : 0);
//    int bits = ((exponent+Floats.EXPONENT_BIAS)) << 
//      (Floats.SIGNIFICAND_BITS-1);
//    bits += signifRounded;
//    // If signifRounded == 2^24, we'd need to set all of the
//    // significand bits to zero and add 1 to the exponent. This is 
//    // exactly the behavior we get from just adding signifRounded 
//    // to bits directly. If the exponent is Float.MAX_EXPONENT, we 
//    // round up (correctly) to Float.POSITIVE_INFINITY.
//    bits |= 1 & Floats.SIGN_MASK;
//    return Float.intBitsToFloat(bits); }

  //--------------------------------------------------------------

  @Override
  default double doubleValue () {
    //assert isValid();
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
      (Numbers.unsigned(highBits)<<32)|Numbers.unsigned(lowBits);

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
  //    //assert 0L<=u;
  //    return NaturalBEI.valueOf(u); }
  //
  //  static Natural get (final BigInteger u) {
  //    //assert 0<=u.signum();
  //    return NaturalBEI.valueOf(u); }
  //
  //  static Natural get (final String u,
  //                      final int radix) {
  //    return NaturalBEI.valueOf(u,radix); }

  //--------------------------------------------------------------

  static Natural valueOf (final String u,
                          final int radix) {
    return NaturalLE.valueOf(u,radix); }

  static Natural valueOf (final BigInteger u) {
    //assert 0<=u.signum();
    return NaturalLE.valueOf(u); }

  static Natural valueOf (final long u) {
    //assert 0L<=u;
    return NaturalLE.valueOf(u); }

  //--------------------------------------------------------------

  //  static Natural get (final String u,
  //                      final int radix) {
  //    return NaturalLEMutable.valueOf(u,radix); }
  //
  //  static Natural get (final BigInteger u) {
  //    //assert 0<=u.signum();
  //    return NaturalLEMutable.valueOf(u); }
  //
  //  static Natural get (final long u) {
  //    //assert 0L<=u;
  //    return NaturalLEMutable.valueOf(u); }

  //--------------------------------------------------------------

  static Natural valueOf (final int u) {
    return valueOf(Numbers.unsigned(u)); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
