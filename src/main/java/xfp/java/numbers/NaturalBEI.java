package xfp.java.numbers;

import static xfp.java.numbers.Numbers.unsigned;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/** immutable arbitrary-precision non-negative integers
 * (natural number) represented by big-endian
 * unsigned <code>int[]</code>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-08
 */

public final class NaturalBEI //extends Number
implements Natural {

  //--------------------------------------------------------------
  // int[] ops
  //--------------------------------------------------------------

  static final int[] EMPTY = new int[0];

  static final int[] toInts (final long u) {
    assert 0<=u;
    final int hi = (int) (u>>>32);
    final int lo = (int) u;
    if (0==hi) {
      if (0==lo) { return EMPTY; }
      return new int[] { lo, }; }
    return new int[] { hi, lo, }; }

  //-------------------------------------------------------------
  // string parsing

  // bitsPerDigit in the given radix times 1024
  // Rounded up to avoid under-allocation.

  private static final long[] bitsPerDigit =
  { 0, 0, 1024, 1624, 2048, 2378, 2648, 2875, 3072, 3247, 3402,
    3543, 3672, 3790, 3899, 4001, 4096, 4186, 4271, 4350, 4426,
    4498, 4567, 4633, 4696, 4756, 4814, 4870, 4923, 4975, 5025,
    5074, 5120, 5166, 5210, 5253, 5295 };

  private static final int[] digitsPerInt =
  { 0, 0, 30, 19, 15, 13, 11, 11, 10, 9, 9, 8, 8, 8, 8, 7, 7, 7,
    7, 7, 7, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 5 };

  private static final int[] intRadix =
  { 0, 0, 0x40000000, 0x4546b3db, 0x40000000, 0x48c27395,
    0x159fd800, 0x75db9c97, 0x40000000, 0x17179149, 0x3b9aca00,
    0xcc6db61, 0x19a10000, 0x309f1021, 0x57f6c100, 0xa2f1b6f,
    0x10000000, 0x18754571, 0x247dbc80, 0x3547667b, 0x4c4b4000,
    0x6b5a6e1d, 0x6c20a40, 0x8d2d931, 0xb640000, 0xe8d4a51,
    0x1269ae40, 0x17179149, 0x1cb91000, 0x23744899, 0x2b73a840,
    0x34e63b41, 0x40000000, 0x4cfa3cc1, 0x5c13d840, 0x6d91b519,
    0x39aa400 };

  // Multiply x array times word y in place, and add word z

  private static final void destructiveMulAdd (final int[] x,
                                               final int y,
                                               final int z) {
    final long ylong = unsigned(y);
    final long zlong = unsigned(z);
    final int lm1 = x.length-1;
    long product = 0;
    long carry = 0;
    for (int i = lm1; i >= 0; i--) {
      product = (ylong * unsigned(x[i])) + carry;
      x[i] = (int) product;
      carry = product >>> 32; }
    long sum = unsigned(x[lm1]) + zlong;
    x[lm1] = (int) sum;
    carry = sum >>> 32;
    for (int i = lm1-1; i >= 0; i--) {
      sum = unsigned(x[i]) + carry;
      x[i] = (int) sum;
      carry = sum >>> 32; } }

  private static final int[] toInts (final String s,
                                     final int radix) {
    final int len = s.length();
    assert 0 < len;
    assert Character.MIN_RADIX <= radix;
    assert radix <= Character.MAX_RADIX;
    assert 0 > s.indexOf('-');
    assert 0 > s.indexOf('+');

    int cursor = 0;
    // skip leading '0' --- not strictly necessary?
    while ((cursor < len)
      && (Character.digit(s.charAt(cursor),radix) == 0)) {
      cursor++; }
    if (cursor == len) { return EMPTY; }

    final int nDigits = len - cursor;

    // might be bigger than needed,
    // but stripLeadingZeros(int[]) handles that
    final long nBits =
      ((nDigits * bitsPerDigit[radix]) >>> 10) + 1;
    final int nWords = (int) (nBits + 31) >>> 5;
    final int[] m = new int[nWords];

    // Process first (potentially short) digit group
    int firstGroupLen = nDigits % digitsPerInt[radix];
    if (firstGroupLen == 0) { firstGroupLen = digitsPerInt[radix]; }
    String group = s.substring(cursor,cursor += firstGroupLen);
    m[nWords-1] = Integer.parseInt(group,radix);
    if (m[nWords-1] < 0) {
      throw new NumberFormatException("Illegal digit"); }

    // Process remaining digit groups
    final int superRadix = intRadix[radix];
    int groupVal = 0;
    while (cursor < len) {
      group = s.substring(cursor,cursor += digitsPerInt[radix]);
      groupVal = Integer.parseInt(group,radix);
      if (groupVal < 0) {
        throw new NumberFormatException("Illegal digit"); }
      destructiveMulAdd(m,superRadix,groupVal); }
    return stripLeadingZeros(m); }

  //--------------------------------------------------------------

  private static final boolean isZero (final int[] z) {
    for (final int element : z) {
      if (0!=element) { return false; } }
    return true; }

  private static final int[] stripLeadingZeros (final int[] m) {
    final int n = m.length;
    int start = 0;
    while ((start < n) && (m[start] == 0)) { start++; }
    return (0==start) ? m : Arrays.copyOfRange(m,start,n); }

  private static final int[] stripLeadingZeros (final byte a[],
                                                final int off,
                                                final int len) {
    final int indexBound = off + len;
    int keep = off;
    while ((keep < indexBound) && (a[keep] == 0)) { keep++; }
    final int intLength = ((indexBound - keep) + 3) >>> 2;
    final int[] result = new int[intLength];
    int b = indexBound - 1;
    for (int i = intLength - 1; i >= 0; i--) {
      result[i] = a[b--] & 0xff;
      final int bytesRemaining = (b - keep) + 1;
      final int bytesToTransfer = Math.min(3,bytesRemaining);
      for (int j = 8; j <= (bytesToTransfer << 3); j += 8) {
        result[i] |= ((a[b--] & 0xff) << j); } }
    return result; }

  //--------------------------------------------------------------
  /** TODO: move, useful as a general array op.
   */
  private static final byte[] toByteArray (final int[] m) {
    final int byteLen = (hiBit(m) / 8) + 1;
    final byte[] byteArray = new byte[byteLen];
    for (
      int i = byteLen-1,
      bytesCopied = 4,
      nextInt = 0,
      intIndex = 0;
      i >= 0;
      i--) {
      if (bytesCopied == 4) {
        nextInt = getInt(m,intIndex++);
        bytesCopied = 1; }
      else {
        nextInt >>>= 8; bytesCopied++; }
      byteArray[i] = (byte) nextInt; }
    return byteArray; }

  //--------------------------------------------------------------
  // Bit Operations
  //--------------------------------------------------------------
  // TODO: BigInteger caches this with the instance. Is it worth
  // having a cache map for these?

  private static final int bitCount (final int[] m) {
    int bc = 0;
    for (final int mi : m) { bc += Integer.bitCount(mi); }
    return bc + 1; }

  //--------------------------------------------------------------
  // shifts
  //--------------------------------------------------------------

  private static final int[] shiftUp (final int[] m,
                                      final int bitShift) {
    assert 0<=bitShift;
    if (bitShift==0) { return m; }
    if (isZero(m)) { return EMPTY; }
    final int intShift = (bitShift >>> 5);
    final int remShift = (bitShift & 0x1f);
    final int n = m.length;
    if (remShift==0) {
      return Arrays.copyOfRange(m,0,n+intShift); }
    int m1[] = null;
    int i = 0;
    final int downShift = 32 - remShift;
    final int highBits = (m[0] >>> downShift);
    if (highBits != 0) {
      m1 = new int[n + intShift + 1];
      m1[i++] = highBits; }
    else { m1 = new int[n + intShift]; }
    int j = 0;
    while (j < (n - 1)) {
      m1[i++] = (m[j++] << remShift) | (m[j] >>> downShift); }
    m1[i] = m[j] << remShift;
    return m1; }

  //--------------------------------------------------------------

  static final int[] shiftUpLong (final long m,
                                  final int shift) {
    final int hi = (int) Numbers.hiWord(m);
    final int lo = (int) Numbers.loWord(m);
    if (0==hi) {
      if (0==lo) { return new int[0]; }
      return shiftUp(new int[] { lo },shift); }
    return shiftUp(new int[] { hi, lo, },shift); }

  //--------------------------------------------------------------

  private static final int[] shiftDown (final int[] m,
                                        final int shift) {
    assert 0<=shift;
    if (isZero(m)) { return EMPTY; }
    if (0==shift) { return stripLeadingZeros(m); }
    final int iShift = (shift>>>5);
    // Special case: entire contents shifted off the end
    final int n0 = m.length;
    if (iShift >= n0) { return EMPTY; }

    final int rShift = (shift & 0x1f);
    int m1[] = null;

    if (rShift == 0) {
      final int newMagLen = n0 - iShift;
      m1 = Arrays.copyOf(m,newMagLen); }
    else {
      int i = 0;
      final int highBits = m[0] >>> rShift;
      if (highBits != 0) {
        m1 = new int[n0 - iShift];
        m1[i++] = highBits; }
      else {
        m1 = new int[n0 - iShift - 1]; }

      final int nBits2 = 32 - rShift;
      int j = 0;
      while (j < (n0 - iShift - 1)) {
        m1[i++] = (m[j++] << nBits2) | (m[j] >>> rShift); } }
    return m1; }

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final int[] _words;
  private final int[] words () { return _words; }

  @Override
  public final int word (final int i) {
    assert 0<=i : "Negative index: " + i;
    final int n = words().length;
    final int ii = n-i-1;
    if ((0<=ii) && (ii<n)) { return words()[ii]; }
    return 0; }

  @Override
  public final Natural setWord (final int i,
                                final int w) {
    assert 0<=i;
    final int n0 = words().length;
    final int n1 = Math.max(i+1,n0);
    assert i<n1;
    final int[] ws = new int[n1];
    System.arraycopy(words(),0,ws,n1-n0,n0);
    ws[n1-1-i] = w;
    return unsafe(stripLeadingZeros(ws)); }

  @Override
  public final int startWord () { return 0; }
  @Override
  public final int endWord () { return words().length; }

  public final int[] copyWords () {
    return Arrays.copyOfRange(words(),0,endWord()); }

  //--------------------------------------------------------------
  // Modular Arithmetic
  //--------------------------------------------------------------

//  private static final int mulAdd (final int[] out,
//                                   final int[] in,
//                                   int offset,
//                                   final int n,
//                                   final long k) {
//    long carry = 0;
//    offset = out.length-offset-1;
//    for (int j=n-1; j>=0; j--) {
//      final long product =
//        (unsigned(in[j])*k) + unsigned(out[offset]) + carry;
//      out[offset--] = (int) product;
//      carry = (product >>> 32); }
//    return (int) carry; }
//
//  private static final void addOne (final int[] a,
//                                    final int offset,
//                                    final int mlen,
//                                    final int carry) {
//    int i = a.length-1-mlen-offset;
//    final long t = unsigned(a[i]) + unsigned(carry);
//    a[i] = (int) t;
//    if ((t >>> 32) == 0) { return; }
//    int j = mlen;
//    while ((--j>=0)&&(--i>=0)) { // Carry out of number
//      a[i]++; if (a[i]!=0) { break; } }
//    return; }
//
//  // shifts a up to n elements up shift bits assumes,
//  // 0<=shift<32
//
//  private static void shiftUp1Bit (final int[] a) {
//    final int n = a.length;
//    for (int i=1;i<n;i++) { a[i-1] = (a[i-1]<<1) | (a[i]>>>31); }
//    a[n-1] = (a[n-1]<<1); }

  //--------------------------------------------------------------
  // square
  //--------------------------------------------------------------
  // The algorithm used here is adapted from Colin Plumb's C
  // library.
  // Technique: Consider the partial products in the
  // multiplication of "abcde" by itself:
  // a b c d e  * a b c d e
  // ==================
  // ae be ce de ee
  // ad bd cd dd de
  // ac bc cc cd ce
  // ab bb bc bd be
  // aa ab ac ad ae
  // Note that everything above the main diagonal:
  // ae be ce de = (abcd) * e
  // ad bd cd = (abc) * d
  // ac bc = (ab) * c
  // ab = (a) * b
  // is a copy of everything below the main diagonal:
  // de
  // cd ce
  // bc bd be
  // ab ac ad ae
  // Thus, the sum is 2 * (off the diagonal) + diagonal.
  // This is accumulated beginning with the diagonal (which
  // consist of the squares of the digits of the input), which
  // is then divided by two, the off-diagonal added, and
  // multiplied by two again. The low bit is simply a copy of
  // the low bit of the input, so it doesn't need special care.

//  @Override
//  public final Natural squareSimple () {
//    final int[] x = words();
//    final int n = endWord();
//    final int n2 = (n<<1);
//    int[] z = new int[n2]; 
//    // Store the squares, right shifted one bit (divided by 2)
//    int lo = 0;
//    for (int j=0,i=0;j<n;j++) {
//      final long xj = unsigned(x[j]);
//      final long xj2 = xj*xj;
//      z[i++] = ((lo<<31) | (int) (xj2>>>33));
//      z[i++] = (int) (xj2>>>1);
//      lo = (int) xj2; }
//    // Add in off-diagonal sums
//    for (int i=n-1,offset=1;i>=0;i--,offset+=2) {
//      final long xi = unsigned(x[i]);
//      final int carry = mulAdd(z,x,offset,i,xi);
//      addOne(z,offset-1,i+1,carry); }
//    // Shift back up and set low bit
//    shiftUp1Bit(z);
//    z[n2-1] |= (x[n-1]&1);
//    return unsafe(stripLeadingZeros(z)); }

  //--------------------------------------------------------------
  // Division
  //--------------------------------------------------------------

  @Override
  public final List<Natural>
  divideAndRemainderBurnikelZiegler (final Natural that) {
    final NaturalBEI u = (NaturalBEI) that;
    final NaturalBEIMutable q = NaturalBEIMutable.make();
    final NaturalBEIMutable n = NaturalBEIMutable.valueOf(words());
    final NaturalBEIMutable d = NaturalBEIMutable.valueOf(u.words());
    final NaturalBEIMutable r =
      n.divideAndRemainderBurnikelZiegler(d,q);
    return List.of(q.immutable(), r.immutable()); }

  //--------------------------------------------------------------
  // gcd
  //--------------------------------------------------------------

  @Override
  public final Natural gcd (final Natural that) {
    // UNSAFE!!!
    final NaturalBEIMutable a = NaturalBEIMutable.valueOf(words());
    final NaturalBEIMutable b = (NaturalBEIMutable) that.recyclable(that);
    return a.hybridGCD(b).immutable(); }

  public static final Natural[] reduce (final NaturalBEI n0,
                                        final NaturalBEI d0) {
    final int shift = Math.min(n0.loBit(),d0.loBit());
    final NaturalBEI n =
      (NaturalBEI) ((shift != 0) ? n0.shiftDown(shift) : n0);
    final NaturalBEI d =
      (NaturalBEI) ((shift != 0) ? d0.shiftDown(shift) : d0);
    if (n.equals(d)) { return new NaturalBEI[] { ONE, ONE, }; }
    if (d.isOne()) { return new NaturalBEI[] { n, ONE, }; }
    if (n.isOne()) { return new NaturalBEI[] { ONE, d, }; }
    final Natural gcd = n.gcd(d);
    if (gcd.compareTo(ONE) > 0) {
      return new Natural[] { n.divide(gcd), d.divide(gcd), }; }
    return new Natural[] { n, d, }; }

  //--------------------------------------------------------------
  // Bit Operations
  //--------------------------------------------------------------

  @Override
  public final Natural shiftUp (final int bitShift) {
    assert 0<=bitShift;
    if (bitShift==0) { return this; }
    if (isZero()) { return ZERO; }
    final int iShift = (bitShift >>> 5);
    final int lShift = (bitShift & 0x1f);
    final int[] m0 = words();
    final int n0 = m0.length;
    final int n1 = n0+iShift;
    if (lShift==0) { return unsafe(Arrays.copyOfRange(m0,0,n1)); }
    final int m1[];
    int i = 0;
    final int rShift = 32 - lShift;
    final int hi = (m0[0] >>> rShift);
    if (hi != 0) {
      m1 = new int[n1+1];
      m1[i++] = hi; }
    else { m1 = new int[n1]; }
    int j = 0;
    while (j < (n0-1)) {
      m1[i++] = (m0[j++] << lShift) | (m0[j] >>> rShift); }
    m1[i] = m0[j] << lShift;
    return unsafe(m1); }

  @Override
  public final Natural shiftDown (final int shift) {
    assert 0<=shift;
    return unsafe(shiftDown(words(),shift)); }

  //--------------------------------------------------------------

  private static final int getInt (final int[] m,
                                   final int n) {
    if (n < 0) { return 0; }
    if (n >= m.length) { return 0; }
    final int mInt = m[m.length - n - 1];
    return mInt; }

  private static final int hiBit (final int[] m) {
    final int len = m.length;
    if (len == 0) { return(0); }
    // Calculate the bit length of the magnitude
    //Debug.println("m[0]=" + Integer.toHexString(m[0]));
    final int n = ((len-1)<<5) + Numbers.bitLength(m[0]);
    return n; }

  public final int bitCount () { return bitCount(words()); }

  //--------------------------------------------------------------
  // Comparable interface+
  //--------------------------------------------------------------

  public static final int compareTo (final NaturalBEI u0,
                                     final long u1) {
    assert 0L<=u1;
    final int n0 = u0.endWord();
    final long m10 = Numbers.hiWord(u1);
    final long m11 = Numbers.loWord(u1);
    final int n1 = (0L!=m10) ? 2 : ((0L!=m11) ? 1 : 0);
    if (n0<n1) { return -1; }
    if (n0>n1) { return 1; }
    final long m00 = unsigned(u0._words[0]);
    if (m00<m10) { return -1; }
    if (m00>m10) { return 1; }
    final long m01 = unsigned(u0._words[1]);
    if (m01<m11) { return -1; }
    if (m01>m11) { return 1; }
    return 0; }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { return uintsHashCode(); }

  @Override
  public final boolean equals (final Object x) {
    if (x==this) { return true; }
    if (!(x instanceof NaturalBEI)) { return false; }
    return uintsEquals((NaturalBEI) x); }

  /** hex string. */
  @Override
  public final String toString () { return toHexString(); }

  //--------------------------------------------------------------
  // 'Number' interface+
  //--------------------------------------------------------------

  @Override
  public final byte[] toByteArray () {
    return toByteArray(words()); }

  //--------------------------------------------------------------

  @Override
  public final float floatValue () {
    if (isZero()) { return 0.0F; }

    final int exponent =
      (((endWord() - 1) << 5) + Numbers.bitLength(_words[0])) - 1;

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

    int twiceSignifFloor;
    // twiceSignifFloor will be ==
    // abs().shiftDown(shift).intValue()
    // We do the shift into an int directly to improve
    // performance.

    final int nBits = shift & 0x1f;
    final int nBits2 = 32 - nBits;

    if (nBits == 0) { twiceSignifFloor = _words[0]; }
    else {
      twiceSignifFloor = _words[0] >>> nBits;
      if (twiceSignifFloor == 0) {
        twiceSignifFloor =
          (_words[0] << nBits2) | (_words[1] >>> nBits); } }

    int signifFloor = (twiceSignifFloor >> 1);
    signifFloor &= Floats.STORED_SIGNIFICAND_MASK;
    // We round up if either the fractional part of signif is
    // strictly greater than 0.5 (which is true if the 0.5 bit is
    // set and any lower bit is set), or if the fractional part of
    // signif is >= 0.5 and signifFloor is odd (which is true if
    // both the 0.5 bit and the 1 bit are set). This is equivalent
    // to the desired HALF_EVEN rounding.
    final boolean increment =
      ((twiceSignifFloor
        & 1) != 0) && (((signifFloor & 1) != 0)
          || (loBit() < shift));
    final int signifRounded =
      increment ? signifFloor + 1 : signifFloor;
    int bits =
      ((exponent
        + Floats.EXPONENT_BIAS)) << (Floats.SIGNIFICAND_BITS - 1);
    bits += signifRounded;
    /*
     * If signifRounded == 2^24, we'd need to set all of the
     * significand
     * bits to zero and add 1 to the exponent. This is exactly the
     * behavior
     * we get from just adding signifRounded to bits directly. If
     * the
     * exponent is Float.MAX_EXPONENT, we round up (correctly) to
     * Float.POSITIVE_INFINITY.
     */
    bits |= 1 & Floats.SIGN_MASK;
    return Float.intBitsToFloat(bits); }

  //--------------------------------------------------------------

  @Override
  public final double doubleValue () {
    if (isZero()) { return 0.0; }

    final int exponent =
      (((endWord() - 1) << 5) + Numbers.bitLength(_words[0])) - 1;

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

    long twiceSignifFloor;
    // twiceSignifFloor will be ==
    // abs().shiftDown(shift).longValue()
    // We do the shift into a long directly to improve
    // performance.

    final int nBits = shift & 0x1f;
    final int nBits2 = 32 - nBits;

    int highBits;
    int lowBits;
    if (nBits == 0) {
      highBits = _words[0];
      lowBits = _words[1]; }
    else {
      highBits = _words[0] >>> nBits;
      lowBits = (_words[0] << nBits2) | (_words[1] >>> nBits);
      if (highBits == 0) {
        highBits = lowBits;
        lowBits = (_words[1] << nBits2) | (_words[2] >>> nBits); } }

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
  // Mutability
  //-------------------------------------------------------------

  @Override
  public final Natural immutable () { return this; }

  @Override
  public final Natural recyclable (final Natural init) {
    if (null==init) {
      return NaturalBEIMutable.make(words().length); }
    return NaturalBEIMutable.valueOf(init); }

  @Override
  public final Natural recyclable (final int n) {
    assert 0<=n;
    return NaturalBEIMutable.make(n); }

  @Override
  public final boolean isImmutable () { return true; }

  //--------------------------------------------------------------
  // construction
  //-------------------------------------------------------------

  private NaturalBEI (final int[] m) { _words = m; }

  // assume no leading zeros
  // TODO: change to implementation where leading zeros are ok
  public static final NaturalBEI unsafe (final int[] m) {
    return new NaturalBEI(m); }

  // assume no leading zeros
  public static final NaturalBEI valueOf (final int[] m) {
    return unsafe(Arrays.copyOf(m,m.length)); }

  public static final NaturalBEI valueOf (final byte[] b,
                                          final int off,
                                          final int len) {
    return unsafe(stripLeadingZeros(b,off,len)); }

  public static final NaturalBEI valueOf (final byte[] b) {
    return valueOf(b,0,b.length); }

  public static final NaturalBEI valueOf (final BigInteger bi) {
    return valueOf(bi.toByteArray()); }

  //-------------------------------------------------------------

  public static final NaturalBEI valueOf (final String s,
                                          final int radix) {
    return unsafe(toInts(s,radix)); }

  public static final NaturalBEI valueOf (final String s) {
    return valueOf(s,0x10); }

  //--------------------------------------------------------------
  // cached values
  //--------------------------------------------------------------

  private static final int MAX_CONSTANT = 16;
  private static final NaturalBEI posConst[] =
    new NaturalBEI[MAX_CONSTANT+1];

  private static volatile NaturalBEI[][] powerCache;

  /** The cache of logarithms of radices for base conversion. */
  private static final double[] logCache;

  static {
    for (int i = 1; i <= MAX_CONSTANT; i++) {
      final int[] magnitude = new int[1];
      magnitude[0] = i;
      posConst[i] = unsafe(magnitude); }
    // Initialize the cache of radix^(2^x) values used for base
    // conversion with just the very first value. Additional
    // values will be created on demand.
    powerCache = new NaturalBEI[Character.MAX_RADIX + 1][];
    logCache = new double[Character.MAX_RADIX + 1];
    for (
      int i = Character.MIN_RADIX;
      i <= Character.MAX_RADIX;
      i++) {
      powerCache[i] = new NaturalBEI[] { NaturalBEI.valueOf(i) };
      logCache[i] = Math.log(i); } }

  public static final NaturalBEI ZERO = new NaturalBEI(EMPTY);
  public static final NaturalBEI ONE = valueOf(1);
  public static final NaturalBEI TWO = valueOf(2);
  public static final NaturalBEI TEN = valueOf(10);

  @Override
  public final Natural empty () { return ZERO; }

  @Override
  public final Natural one () { return ONE; }

  //--------------------------------------------------------------

  public static final NaturalBEI valueOf (final long x) {
    if (x==0) { return ZERO; }
    assert 0L < x;
    if (x <= MAX_CONSTANT) { return posConst[(int) x]; }
    return unsafe(toInts(x)); }

  //--------------------------------------------------------------

  public static final NaturalBEI valueOf (final long x,
                                          final int upShift) {
    if (0L==x) { return ZERO; }
    assert 0L < x;
    return unsafe(shiftUpLong(x,upShift)); }

  //--------------------------------------------------------------

  @Override
  public final Natural from (final long u) { return valueOf(u); }

  @Override
  public final Natural from (final long u,
                             final int shift) {
    return valueOf(u,shift); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

