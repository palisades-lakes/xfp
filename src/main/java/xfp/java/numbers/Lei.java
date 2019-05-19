package xfp.java.numbers;

import static xfp.java.numbers.Numbers.hiBit;
import static xfp.java.numbers.Numbers.hiWord;
import static xfp.java.numbers.Numbers.loWord;
import static xfp.java.numbers.Numbers.unsigned;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

/** operations on <code>int[]</code>, interpreted
 * as little-endian arbitrary-precision non-negative integers.
 *
 * TODO: ensure no leading zeros in inputs
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-05-18
 */

public final class Lei {

  //--------------------------------------------------------------
  // immutable!

  public static final int[] ZERO = new int[0];

  public static final boolean isZero (final int[] z) {
    return 0 == z.length; }

  //--------------------------------------------------------------

  public static final int[] stripLeadingZeros (final int[] m) {
    int i = m.length-1;
    while ((0<=i) && (0==m[i])) { i--; }
    final int n = i+1;
    if (0==n) { return ZERO; }
    if (n==m.length) { return m; }
    return Arrays.copyOf(m,n); }

  //-------------------------------------------------------------- 
  // fixing
  //--------------------------------------------------------------  

  private static final int[] shiftRightImpl (final int[] m,
                                             final int bitShift) {
    final int intShift = bitShift >>> 5;
    final int rmeShift = bitShift & 0x1f;
    final int n = m.length;
    if (intShift >= n) { return ZERO; }
    if (rmeShift == 0) { return  Arrays.copyOf(m,n-intShift); }

    int newMag[] = null;
    int i = 0;
    final int highBits = m[0] >>> rmeShift;
    if (highBits != 0) {
      newMag = new int[n - intShift];
      newMag[i++] = highBits; }
    else {
      newMag = new int[n - intShift - 1]; }

    final int nBits2 = 32 - rmeShift;
    int j = 0;
    while (j < (n - intShift - 1)) {
      newMag[i++] = (m[j++] << nBits2) | (m[j] >>> rmeShift); } 
    return newMag; }

  public static final int[] shiftLeft (final int[] m,
                                       final int n) {
    if (isZero(m)) { return ZERO; }
    if (n == 0) { return stripLeadingZeros(m); }
    if (n < 0) { return shiftRightImpl(m,-n); }
    final int nInts = n >>> 5;
    final int nBits = n & 0x1f;
    final int mLen = m.length;
    int newMag[] = null;
    if (nBits == 0) {
      newMag = new int[mLen + nInts];
      System.arraycopy(m,0,newMag,0,mLen); }
    else {
      int i = 0;
      final int nBits2 = 32 - nBits;
      final int highBits = m[0] >>> nBits2;
      if (highBits != 0) {
        newMag = new int[mLen + nInts + 1];
        newMag[i++] = highBits; }
      else { newMag = new int[mLen + nInts]; }
      int j = 0;
      while (j < (mLen - 1)) {
        newMag[i++] = (m[j++] << nBits) | (m[j] >>> nBits2); }
      newMag[i] = m[j] << nBits; }
    return newMag; }

  //-------------------------------------------------------------- 
  // fixed to here
  //--------------------------------------------------------------  

  public static final int[] shiftLeft (final long m,
                                       final int shift) {
    final int[] m0 = { (int) (m >>> 32),
                       (int) (m & 0xFFFFFFFFL), };
    return shiftLeft(m0,shift); }

  public static final int[] stripLeadingZeros (final byte a[],
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

  // assuming m0 and m1 have no leading zeros
  public static final int compare (final int[] m0,
                                   final int[] m1) {
    final int n0 = m0.length;
    final int n1 = m1.length;
    if (n0<n1) { return -1; }
    if (n0>n1) { return 1; }
    for (int i=0;i<n0;i++) {
      final long m0i = unsigned(m0[i]);
      final long m1i = unsigned(m1[i]);
      if (m0i<m1i) { return -1; }
      if (m0i>m1i) { return 1; } }
    return 0; }

  // assuming m0 has no leading zeros
  public static final int compare (final int[] m0,
                                   final long m1) {
    if (m1 < 0L) { return 1; }
    final int n0 = m0.length;
    final long m10 = hiWord(m1);
    final long m11 = loWord(m1);
    final int n1 = (0L!=m10) ? 2 : ((0L!=m11) ? 1 : 0);
    if (n0<n1) { return -1; }
    if (n0>n1) { return 1; }
    final long m00 = unsigned(m0[0]);
    if (m00<m10) { return -1; }
    if (m00>m10) { return 1; }
    final long m01 = unsigned(m0[1]);
    if (m01<m11) { return -1; }
    if (m01>m11) { return 1; }
    return 0; }

  // assuming m0 has no leading zeros
  public static final int compare (final int[] m0,
                                   final long m1,
                                   final int bitShift) {
    if (0==bitShift) { return compare(m0,m1); }
    assert 0 < bitShift : "bitShift=" + bitShift;
    if (m1 < 0L) { return 1; }
    final int n0 = m0.length;
    final int intShift = bitShift >>> 5;
    final int remShift = bitShift & 0x1f;
    final boolean carry = (64 < (remShift + hiBit(m1)));
    final int n1 = intShift + (carry ? 3 : 2);
    if (n0<n1) { return -1; }
    if (n0>n1) { return 1; }
    final long m00 = unsigned(m0[0]);
    if (carry) {
      final long m10 = (m1 >>> (64 - remShift));
      if (m00<m10) { return -1; }
      if (m00>m10) { return 1; }
      final long m01 = unsigned(m0[1]);
      final long m1s = (m1 << remShift);
      final long m11 = hiWord(m1s);
      if (m01<m11) { return -1; }
      if (m01>m11) { return 1; }
      final long m02 = unsigned(m0[2]);
      final long m12 = loWord(m1s);
      if (m02<m12) { return -1; }
      if (m02>m12) { return 1; }
      for (int i=3;i<n0;i++) { if (0!=m0[i]) { return 1; } }
      return 0; }
    // no carry
    final long m1s = (m1 << remShift);
    final long m10 = hiWord(m1s);
    if (m00<m10) { return -1; }
    if (m00>m10) { return 1; }
    final long m01 = unsigned(m0[1]);
    final long m11 = loWord(m1s);
    if (m01<m11) { return -1; }
    if (m01>m11) { return 1; }
    for (int i=2;i<n0;i++) { if (0!=m0[i]) { return 1; } }
    return 0; }

  //  public static final int compare (final long m0,
  //                                   final int[] m1) {
  //    return -compare(m1,m0); }

  //--------------------------------------------------------------
  // add
  //--------------------------------------------------------------

  public static final int[] add (final int[] m0,
                                 final long m1) {
    assert 0L <= m1;
    //if (0L == m1) { return stripLeadingZeros(m0); }
    if (isZero(m0)) { return valueOf(m1); }
    long sum = 0;
    int n0 = m0.length;
    final int hi = (int) hiWord(m1);
    if (n0 == 1) { return valueOf(m1 + unsigned(m0[0])); }
    final int[] result = new int[n0];
    if (hi == 0) {
      sum = unsigned(m0[--n0]) + m1;
      result[n0] = (int) sum; }
    else {
      sum = unsigned(m0[--n0]) + loWord(m1);
      result[n0] = (int) sum;
      sum = unsigned(m0[--n0]) + unsigned(hi) + (sum >>> 32);
      result[n0] = (int) sum; }

    boolean carry = (hiWord(sum) != 0L);
    while ((n0 > 0) && carry) {
      carry = ((result[--n0] = m0[n0] + 1) == 0); }
    while (n0 > 0) { result[--n0] = m0[n0]; }
    if (carry) {
      final int[] bigger = new int[result.length+1];
      System.arraycopy(result,0,bigger,1,result.length);
      bigger[0] = 0x01;
      return bigger; }
    return result; }
  //  return stripLeadingZeros(bigger); }
  //return stripLeadingZeros(result); }

  //--------------------------------------------------------------

  public static final int[] add (final int[] m0,
                                 final int[] m1) {
    // If m0 is shorter, swap the two arrays
    if (m0.length < m1.length) { return add(m1,m0); }
    int i0 = m0.length;
    int i1 = m1.length;
    final int result[] = new int[i0];
    long sum = 0;
    if (i1 == 1) {
      sum = unsigned(m0[--i0]) + unsigned(m1[0]);
      result[i0] = (int) sum; }
    else {
      while (i1 > 0) {
        sum =
          unsigned(m0[--i0]) + unsigned(m1[--i1])
          + (sum >>> 32);
        result[i0] = (int) sum; } }
    boolean carry = ((sum >>> 32) != 0);
    while ((i0 > 0) && carry) {
      carry = ((result[--i0] = m0[i0] + 1) == 0); }
    while (i0 > 0) { result[--i0] = m0[i0]; }
    if (carry) {
      final int[] bigger = new int[result.length + 1];
      System.arraycopy(result,0,bigger,1,result.length);
      bigger[0] = 0x01;
      return bigger; }
    return result; }

  //--------------------------------------------------------------

  public static final int[] add (final int[] m0,
                                 final long m1,
                                 final int bitShift)  {
    //    if (isZero(m0)) {
    //      return stripLeadingZeros(shiftLeft(m1,bitShift)); }
    assert 0L < m1;
    if (0 == bitShift) { return add(m0,m1); }

    final int intShift = bitShift >>> 5;
    final int remShift = bitShift & 0x1f;
    final boolean yCarry = (64 < (remShift + Numbers.hiBit(m1)));
    final int ny = intShift + (yCarry ? 3 : 2);

    final int nx = m0.length;
    int ix=nx-1;

    final int nr = Math.max(nx,ny);
    final int[] r0 = new int[nr];
    int ir=nr-1;
    final int iy=nr-intShift-1;

    // copy unaffected low order x to result
    while ((iy<ir) && (0<=ix)) { r0[ir--] = m0[ix--]; }
    //Debug.println("r0=" + toHexString(r0));
    ir = iy;

    long sum;
    // add y words to x with carry
    final long mid = (m1 << remShift);
    sum = (mid & 0xFFFFFFFFL);
    if (0<=ix) {
      sum += unsigned(m0[ix--]); }
    r0[ir--] = (int) sum;
    sum = (mid >>> 32) + (sum >>> 32);
    if (0<=ix) {
      sum += unsigned(m0[ix--]); }
    r0[ir--] = (int) sum;
    // TODO: i==0 implies remShift==0 ?
    if ((0<=ir) && yCarry) {
      sum = (m1 >>> (64 - remShift)) + (sum >>> 32);
      if (0<=ix) {
        sum += unsigned(m0[ix--]); }
      r0[ir--] = (int) sum; }

    // handle carry propagation
    boolean carry = ((sum >>> 32) != 0);
    while ((0<=ir) && carry) {
      sum = 0x01L;
      if (0<=ix) { sum += unsigned(m0[ix--]); }
      final int is = (int) sum;
      r0[ir--] = is;
      carry = (is == 0); }

    // grow result if one more carry
    if (carry) {
      final int r1[] = new int[nr + 1];
      System.arraycopy(r0,0,r1,1,nr);
      r1[0] = 0x01;
      //return stripLeadingZeros(r1); }
      return r1; }

    // copy remainder of x if any
    while ((0<=ix) && (0<=ir)) { r0[ir--] = m0[ix--]; }

    return r0; }
  //return stripLeadingZeros(r0); }

  //--------------------------------------------------------------
  // subtract -- only where answer is positive
  //--------------------------------------------------------------
  // only when m1 <= m0

  public static final int[] subtract (final int[] m0,
                                      final long m1) {
    assert 0L <= m1;
    //if (0L == m1) { return stripLeadingZeros(m0); }
    if (0L == m1) { return m0; }
    final int c = compare(m0,m1);
    assert 0 <= c;
    if (0 == c) { return ZERO; }
    final int hi = (int) (m1 >>> 32);
    int i0 = m0.length;
    final int result[] = new int[i0];
    long difference = 0;
    if (hi == 0) {
      difference = unsigned(m0[--i0]) - m1;
      result[i0] = (int) difference; }
    else {
      difference = unsigned(m0[--i0]) - loWord(m1);
      result[i0] = (int) difference;
      difference =
        (unsigned(m0[--i0]) - unsigned(hi))
        + (difference >> 32);
      result[i0] = (int) difference; }
    // Subtract remainder of longer number while borrow propagates
    boolean borrow = ((difference >> 32) != 0);
    while ((i0 > 0) && borrow) {
      borrow = ((result[--i0] = m0[i0] - 1) == -1); }
    // Copy remainder of longer number
    while (i0 > 0) { result[--i0] = m0[i0]; }
    return stripLeadingZeros(result); }

  //--------------------------------------------------------------

  //  public static final int[] subtract (final long m0,
  //                                      final int[] m1) {
  //    assert 0L <= m0;
  //    //if (isZero(little)) { return valueOf(big); }
  //    final int c = -compare(m1,m0);
  //    assert 0 <= c;
  //    if (0 == c) { return ZERO; }
  //    final int highWord = (int) (m0 >>> 32);
  //    if (highWord == 0) {
  //      final int result[] = new int[1];
  //      result[0] = (int) (m0 - unsigned(m1[0]));
  //      return result; }
  //    final int result[] = new int[2];
  //    if (m1.length == 1) {
  //      final long difference = loWord(m0) - unsigned(m1[0]);
  //      result[1] = (int) difference;
  //      // Subtract remainder of longer number while borrow
  //      // propagates
  //      final boolean borrow = ((difference >> 32) != 0);
  //      if (borrow) { result[0] = highWord - 1; }
  //      // Copy remainder of longer number
  //      else { result[0] = highWord; }
  //      return result; }
  //    long difference = loWord(m0) - unsigned(m1[1]);
  //    result[1] = (int) difference;
  //    difference =
  //      (unsigned(highWord) - unsigned(m1[0]))
  //      + (difference >> 32);
  //    result[0] = (int) difference;
  //    return result; }

  //--------------------------------------------------------------
  // only valid when little <= big

  public static final int[] subtract (final int[] m0,
                                      final int[] m1) {

    //if (isZero(little)) { return stripLeadingZeros(big); }
    //final int c = compare(m0,m1);
    //assert 0L <= c;
    //if (c == 0) { return ZERO; }
    int bigIndex = m0.length;
    final int result[] = new int[bigIndex];
    int littleIndex = m1.length;
    long difference = 0;

    // Subtract common parts of both numbers
    while (littleIndex > 0) {
      difference =
        (unsigned(m0[--bigIndex])
          - unsigned(m1[--littleIndex]))
        + (difference >> 32);
      result[bigIndex] = (int) difference; }

    // Subtract remainder of longer number while borrow propagates
    boolean borrow = ((difference >> 32) != 0);
    while ((bigIndex > 0) && borrow) {
      borrow = ((result[--bigIndex] = m0[bigIndex] - 1) == -1); }

    // Copy remainder of longer number
    while (bigIndex > 0) {
      result[--bigIndex] = m0[bigIndex]; }

    final int[] r = stripLeadingZeros(result);
    ////Debug.println("4result=\n" + Numbers.toHexString(r));
    return r; }

  // assuming little*2<sup>bitShift</sup> <= big
  // big represents
  // sum<sub>i=0,n-1</sub> unsigned(big[n-1-i]) * 2 <sup>i*32</sup>
  // so big[0] is the most significant term; big[n-1] the least.
  //
  // if x = (little << (bitShift % 32)),
  // and intShift = bitShift/32,
  // then x fits in 3 unsigned ints, xlo, xmi, xhi,
  // where xhi == 0 if (bitShift % 32) == 0, and
  // little*2<sup>bitShift</sup> =
  // (xlo * 2<sup>intShift*32</sup) +
  // (xmi * 2<sup>(intShift+1)*32</sup) +
  // (xhi * 2<sup>(intShift+2)*32</sup)

  public static final int[] subtract (final int[] m0,
                                      final long m1,
                                      final int bitShift) {
    assert 0L <= m1;
    //if (0L == m1) { return stripLeadingZeros(m0); }
    //if (0L == m1) { return m0; }

    if (0 == bitShift) { return subtract(m0,m1); }
    final int intShift = bitShift >>> 5;
    final int n0 = m0.length;
    final int result[] = new int[n0];
    int i=n0-1;
    // copy unaffected low order ints
    for (;i>(n0-intShift-1);i--) { result[i] = m0[i]; }

    final int remShift = bitShift & 0x1f;
    final long mid = (m1 << remShift);

    long difference = 0;

    // Subtract common parts of both numbers
    difference = (unsigned(m0[i]) - loWord(mid));
    result[i] = (int) difference;
    i--;
    difference = (unsigned(m0[i]) - hiWord(mid))
      + (difference >> 32);
    result[i] = (int) difference;
    i--;
    // TODO: i==0 implies remShift==0 ?
    if ((0<=i) && (0<remShift)) {
      difference =
        (unsigned(m0[i]) - (m1 >>> (64 - remShift)))
        + (difference >> 32);
      result[i] = (int) difference;
      i--; }

    // Subtract remainder of longer number while borrow propagates
    boolean borrow = ((difference >> 32) != 0);
    while ((0<=i) && borrow) {
      borrow = ((result[i] = m0[i] - 1) == -1); i--;}
    // Copy remainder of longer number
    while (0<=i) { result[i] = m0[i]; i--; }
    return stripLeadingZeros(result); }

  //--------------------------------------------------------------
  // only when big <= (val << leftShift)

  public static final int[] subtract (final long m0,
                                      final int shift,
                                      final int[] m1) {
    assert 0L <= m0;
    if (0L == m0) {
      assert isZero(m1);
      return ZERO; }
    //if (isZero(big)) {
    //  return stripLeadingZeros(shiftLeft(m0,leftShift)); }
    return stripLeadingZeros(subtract(shiftLeft(m0,shift),m1)); }

  //--------------------------------------------------------------
  // squaring --- used in multiplication
  //--------------------------------------------------------------

  /** limits length of int[]s to the supported range.
   */
  private static final int MAX_MAG_LENGTH =
    (Integer.MAX_VALUE / Integer.SIZE) + 1; // (1 << 26)

  private static final int KARATSUBA_SQUARE_THRESHOLD = 128;

  private static final int TOOM_COOK_SQUARE_THRESHOLD = 216;

  private static final int[] square (final int[] m,
                                     final boolean isRecursion) {
    if (isZero(m)) { return ZERO; }
    final int len = m.length;

    if (len < KARATSUBA_SQUARE_THRESHOLD) {
      final int[] z = squareToLen(m,len,null);
      return stripLeadingZeros(z); }
    if (len < TOOM_COOK_SQUARE_THRESHOLD) {
      return squareKaratsuba(m); }
    // For a discussion of overflow detection see multiply()
    if (!isRecursion) {
      if (bitLength(m,m.length) > (16L * MAX_MAG_LENGTH)) {
        reportOverflow(); } }
    return squareToomCook3(m); }

  private static final int[] squareToLen (final int[] m,
                                          final int len,
                                          int[] z) {
    final int zlen = len << 1;
    if ((z == null) || (z.length < zlen)) {
      z = new int[zlen]; }

    // Execute checks before calling intrinsic method.
    implSquareToLenChecks(m,len,z,zlen);
    return implSquareToLen(m,len,z,zlen); }

  private static void implSquareToLenChecks (final int[] x,
                                             final int len,
                                             final int[] z,
                                             final int zlen)
                                               throws RuntimeException {
    if (len < 1) {
      throw new IllegalArgumentException(
        "invalid input length: " + len); }
    if (len > x.length) {
      throw new IllegalArgumentException(
        "input length out of bound: " + len + " > " + x.length); }
    if ((len * 2) > z.length) {
      throw new IllegalArgumentException(
        "input length out of bound: " + (len * 2) + " > "
          + z.length); }
    if (zlen < 1) {
      throw new IllegalArgumentException(
        "invalid input length: " + zlen); }
    if (zlen > z.length) {
      throw new IllegalArgumentException(
        "input length out of bound: " + len + " > " + z.length);
    }
  }

  // shifts a up to len left n bits assumes no leading zeros,
  // 0<=n<32
  private static void primitiveLeftShift (final int[] a,
                                          final int len,
                                          final int n) {
    if ((len == 0) || (n == 0)) { return; }
    final int n2 = 32 - n;
    for (int i = 0, c = a[i], m = (i + len) - 1; i < m; i++) {
      final int b = c;
      c = a[i + 1];
      a[i] = (b << n) | (c >>> n2); }
    a[len - 1] <<= n; }

  private static final int[] implSquareToLen (final int[] x,
                                              final int len,
                                              final int[] z,
                                              final int zlen) {

    // The algorithm used here is adapted from Colin Plumb's C
    // library.
    // Technique: Consider the partial products in the
    // multiplication
    // of "abcde" by itself:
    // a b c d e
    // * a b c d e
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
    // is then divided by two, the off-diagonal added, and multiplied by
    // two
    // again. The low bit is simply a copy of the low bit of the
    // input, so it doesn't need special care.

    // Store the squares, right shifted one bit (i.e., divided by
    // 2)
    int lastProductLowWord = 0;
    for (int j = 0, i = 0; j < len; j++) {
      final long piece = unsigned(x[j]);
      final long product = piece * piece;
      z[i++] =
        (lastProductLowWord << 31) | (int) (product >>> 33);
      z[i++] = (int) (product >>> 1);
      lastProductLowWord = (int) product;
    }

    // Add in off-diagonal sums
    for (int i = len, offset = 1; i > 0; i--, offset += 2) {
      int t = x[i - 1];
      t = mulAdd(z,x,offset,i - 1,t);
      addOne(z,offset - 1,i,t); }

    // Shift back up and set low bit
    primitiveLeftShift(z,zlen,1);
    z[zlen - 1] |= x[len - 1] & 1;

    return z; }

  private static final int[] squareKaratsuba (final int[] m) {
    final int half = (m.length + 1) / 2;
    final int[] xl = getLower(m,half);
    final int[] xh = getUpper(m,half);
    final int[] xhs = square(xh,false);  // xhs = xh^2
    final int[] xls = square(xl,false);  // xls = xl^2
    // xh^2 << 64 + (((xl+xh)^2 - (xh^2 + xl^2)) << 32) + xl^2
    final int h32 = half*32;
    return
      add(
        shiftLeft(
          add(
            shiftLeft(xhs,h32),
            subtract(square(add(xl,xh),false),add(xhs,xls))),
          h32),
        xls); }

  //--------------------------------------------------------------

  private static final int TOOM_COOK_THRESHOLD = 240;

  private static final int[] getToomSlice (final int[] m,
                                           final int lowerSize,
                                           final int upperSize,
                                           final int slice,
                                           final int fullsize) {
    final int len = m.length;
    final int offset = fullsize - len;
    int start;
    final int end;
    if (slice == 0) {
      start = 0 - offset;
      end = upperSize - 1 - offset; }
    else {
      start = (upperSize + ((slice - 1) * lowerSize)) - offset;
      end = (start + lowerSize) - 1; }
    if (start < 0) { start = 0; }
    if (end < 0) { return ZERO; }
    final int sliceSize = (end - start) + 1;
    if (sliceSize <= 0) { return ZERO; }
    // While performing Toom-Cook, all slices are positive and
    // the sign is adjusted when the final number is composed.
    if ((start == 0) && (sliceSize >= len)) { return stripLeadingZeros(m); }
    final int intSlice[] = new int[sliceSize];
    System.arraycopy(m,start,intSlice,0,sliceSize);
    return stripLeadingZeros(intSlice); }

  private static final int[] squareToomCook3 (final int[] m) {
    final int len = m.length;

    // k is the size (in ints) of the lower-order slices.
    final int k = (len + 2) / 3;   // Equal to ceil(largest/3)

    // r is the size (in ints) of the highest-order slice.
    final int r = len - (2 * k);

    // Obtain slices of the numbers. a2 is the most significant
    // bits of the number, and a0 the least significant.
    final int[] a2 = getToomSlice(m,k,r,0,len);
    final int[] a1 = getToomSlice(m,k,r,1,len);
    final int[] a0 = getToomSlice(m,k,r,2,len);
    final int[] v0 = square(a0,true);
    int[] da1 = add(a2,a0);
    final int[] vm1 = square(subtract(da1,a1),true);
    da1 = add(da1,a1);
    final int[] v1 = square(da1,true);
    final int[] vinf = square(a2,true);
    final int[] v2 = square(subtract(shiftLeft(add(da1,a2),1),a0),true);

    // The algorithm requires two divisions by 2 and one by 3.
    // All divisions are known to be exact, that is, they do not
    // produce remainders, and all results are positive. The 
    // divisions by 2 are implemented as right shifts which are 
    // relatively efficient, leaving only a division by 3.
    // The division by 3 is done by an optimized algorithm for
    // this case.
    int[] t2 = exactDivideBy3(subtract(v2,vm1));
    int[] tm1 = shiftRight(subtract(v1,vm1),1);
    int[] t1 = subtract(v1,v0);
    t2 = shiftRight(subtract(t2,t1),1);
    t1 = subtract(subtract(t1,tm1),vinf);
    t2 = shiftLeft(subtract(t2,vinf),1);
    tm1 = subtract(tm1,t2);

    // Number of bits to shift left.
    final int ss = k * 32;

    return
      stripLeadingZeros(
        add(
          shiftLeft(add(
            shiftLeft(add(
              shiftLeft(add(
                shiftLeft(vinf,ss),
                t2),ss),
              t1),ss),
            tm1),ss),
          v0)); }

  //--------------------------------------------------------------
  // multiply
  //--------------------------------------------------------------

  private static final int MULTIPLY_SQUARE_THRESHOLD = 20;

  private static final int KARATSUBA_THRESHOLD = 80;

  private static final int[] getLower (final int[] m,
                                       final int n) {
    final int len = m.length;
    if (len <= n) { return m; }
    final int lowerInts[] = new int[n];
    System.arraycopy(m,len - n,lowerInts,0,n);
    return stripLeadingZeros(lowerInts); }

  private static final int[] getUpper (final int[] m,
                                       final int n) {
    final int len = m.length;
    if (len <= n) { return ZERO; }
    final int upperLen = len - n;
    final int upperInts[] = new int[upperLen];
    System.arraycopy(m,0,upperInts,0,upperLen);
    return stripLeadingZeros(upperInts); }

  private static final int[] multiplyKaratsuba (final int[] x,
                                                final int[] y) {
    final int xlen = x.length;
    final int ylen = y.length;

    // The number of ints in each half of the number.
    final int half = (Math.max(xlen,ylen) + 1) / 2;

    // xl and yl are the lower halves of x and y respectively,
    // xh and yh are the upper halves.
    final int[] xl = getLower(x,half);
    final int[] xh = getUpper(x,half);
    final int[] yl = getLower(y,half);
    final int[] yh = getUpper(y,half);

    final int[] p1 = multiply(xh,yh);  // p1 = xh*yh
    final int[] p2 = multiply(xl,yl);  // p2 = xl*yl

    // p3=(xh+xl)*(yh+yl)
    final int[] p3 = multiply(add(xh,xl),add(yh,yl));

    // result = p1 * 2^(32*2*half) + (p3 - p1 - p2) * 2^(32*half)
    // + p2
    final int h32 = half*32;
    final int[] result =
      add(
        shiftLeft(add(
          shiftLeft(p1,h32),
          subtract(subtract(p3,p1),p2)),
          h32),
        p2);

    return result; }

  private static final int[] exactDivideBy3 (final int[] m) {
    final int len = m.length;
    int[] result = new int[len];
    long x, w, q, borrow;
    borrow = 0L;
    for (int i = len - 1; i >= 0; i--) {
      x = unsigned(m[i]);
      w = x - borrow;
      if (borrow > x) { // Did we make the number go negative?
        borrow = 1L; }
      else { borrow = 0L; }
      // 0xAAAAAAAB is the modular inverse of 3 (mod 2^32). Thus,
      // the effect of this is to divide by 3 (mod 2^32).
      // This is much faster than division on most architectures.
      q = loWord(w * 0xAAAAAAABL);
      result[i] = (int) q;
      // Now check the borrow. The second check can of course be
      // eliminated if the first fails.
      if (q >= 0x55555556L) {
        borrow++;
        if (q >= 0xAAAAAAABL) { borrow++; } } }
    return stripLeadingZeros(result); }

  private static final int[] multiplyToomCook3 (final int[] a,
                                                final int[] b) {
    final int alen = a.length;
    final int blen = b.length;

    final int largest = Math.max(alen,blen);

    // k is the size (in ints) of the lower-order slices.
    final int k = (largest + 2) / 3;   // Equal to ceil(largest/3)

    // r is the size (in ints) of the highest-order slice.
    final int r = largest - (2 * k);

    // Obtain slices of the numbers. a2 and b2 are the most
    // significant bits of the numbers a and b, and a0 and b0 the
    // least significant.
    final int[] a2 = getToomSlice(a,k,r,0,largest);
    final int[] a1 = getToomSlice(a,k,r,1,largest);
    final int[] a0 = getToomSlice(a,k,r,2,largest);
    final int[] b2 = getToomSlice(b,k,r,0,largest);
    final int[] b1 = getToomSlice(b,k,r,1,largest);
    final int[] b0 = getToomSlice(b,k,r,2,largest);
    final int[] v0 = multiply(a0,b0,true);
    int[] da1 = add(a2,a0);
    int[] db1 = add(b2,b0);
    final int[] vm1 = multiply(subtract(da1,a1),subtract(db1,b1),true);
    da1 = add(da1,a1);
    db1 = add(db1,b1);
    final int[] v1 = multiply(da1,db1,true);
    final int[] v2 =
      multiply(
        subtract(shiftLeft(add(da1,a2),1),a0),
        subtract(shiftLeft(add(db1,b2),1),b0)
        ,true);

    final int[] vinf = multiply(a2,b2,true);

    // The algorithm requires two divisions by 2 and one by 3.
    // All divisions are known to be exact, that is, they do not
    // produce remainders, and all results are positive. The
    // divisions by 2 are implemented as right shifts which are
    // relatively efficient, leaving only an exact division by 3,
    // which is done by a specialized linear-time algorithm.
    int[] t2 = exactDivideBy3(subtract(v2,vm1));
    int[] tm1 = shiftRight(subtract(v1,vm1),1);
    int[] t1 = subtract(v1,v0);
    t2 = shiftRight(subtract(t2,t1),1);
    t1 = subtract(subtract(t1,tm1),vinf);
    t2 = subtract(t2,shiftLeft(vinf,1));
    tm1 = subtract(tm1,t2);

    // Number of bits to shift left.
    final int ss = k * 32;

    final int[] result =
      add(shiftLeft(
        add(shiftLeft(
          add(shiftLeft(
            add(shiftLeft(vinf,ss),t2),
            ss),t1),
          ss),tm1),
        ss),v0);
    return stripLeadingZeros(result); }

  //--------------------------------------------------------------

  private static final int[] multiply (final int[] m0,
                                       final int m1) {
    if (Integer.bitCount(m1) == 1) {
      return
        stripLeadingZeros(
          shiftLeft(m0,Integer.numberOfTrailingZeros(m1))); }
    final int xlen = m0.length;
    int[] rm = new int[xlen + 1];
    long carry = 0;
    final long yl = loWord(m1);
    int rstart = rm.length - 1;
    for (int i = xlen - 1; i >= 0; i--) {
      final long product = (unsigned(m0[i]) * yl) + carry;
      rm[rstart--] = (int) product;
      carry = product >>> 32; }
    if (carry == 0L) {
      rm = Arrays.copyOfRange(rm,1,rm.length); }
    else {
      rm[rstart] = (int) carry; }
    return stripLeadingZeros(rm); }

  public static final int[] multiply (final int[] m0,
                                      final long m1) {
    assert 0L <= m1;
    if (m1 == 0L) { return ZERO; }
    final long dh = m1 >>> 32;      // higher order bits
    final long dl = loWord(m1); // lower order bits
    final int xlen = m0.length;
    final int[] value = m0;
    int[] rm =
      (dh == 0L) ? (new int[xlen + 1]) : (new int[xlen + 2]);
      long carry = 0;
      int rstart = rm.length - 1;
      for (int i = xlen - 1; i >= 0; i--) {
        final long product = (unsigned(value[i]) * dl) + carry;
        rm[rstart--] = (int) product;
        carry = product >>> 32; }
      rm[rstart] = (int) carry;
      if (dh != 0L) {
        carry = 0;
        rstart = rm.length - 2;
        for (int i = xlen - 1; i >= 0; i--) {
          final long product =
            (unsigned(value[i]) * dh)
            + unsigned(rm[rstart]) + carry;
          rm[rstart--] = (int) product;
          carry = product >>> 32; }
        rm[0] = (int) carry; }
      if (carry == 0L) {
        rm = Arrays.copyOfRange(rm,1,rm.length); }
      return stripLeadingZeros(rm); }

  //--------------------------------------------------------------

  private static final void multiplyToLenCheck (final int[] array,
                                                final int length) {
    // not an error because multiplyToLen won't execute
    // if len <= 0
    if (length <= 0) { return; }
    Objects.requireNonNull(array);
    if (length > array.length) {
      throw new ArrayIndexOutOfBoundsException(length-1); } }

  private static final int[] implMultiplyToLen (final int[] m0,
                                                final int n0,
                                                final int[] m1,
                                                final int n1,
                                                int[] z) {
    final int xstart = n0 - 1;
    final int ystart = n1 - 1;
    if ((z == null) || (z.length < (n0 + n1))) {
      z = new int[n0 + n1]; }
    long carry = 0;
    for (int j = ystart, k = ystart + 1 + xstart;
      j >= 0;
      j--, k--) {
      final long product =
        (unsigned(m1[j]) * unsigned(m0[xstart])) + carry;
      z[k] = (int) product;
      carry = product >>> 32; }
    z[xstart] = (int) carry;
    for (int i = xstart - 1; i >= 0; i--) {
      carry = 0;
      for (int j = ystart, k = ystart + 1 + i; j >= 0; j--, k--) {
        final long product =
          (unsigned(m1[j]) * unsigned(m0[i]))
          + unsigned(z[k]) + carry;
        z[k] = (int) product;
        carry = product >>> 32; }
      z[i] = (int) carry; }
    return z; }

  private static final int[] multiplyToLen (final int[] x,
                                            final int xlen,
                                            final int[] y,
                                            final int ylen,
                                            final int[] z) {
    multiplyToLenCheck(x,xlen);
    multiplyToLenCheck(y,ylen);
    return implMultiplyToLen(x,xlen,y,ylen,z); }

  //--------------------------------------------------------------

  private static final int[] multiply (final int[] m,
                                       final int[] val,
                                       final boolean isRecursion) {
    if ((isZero(val)) || (isZero(m))) { return ZERO; }
    final int xlen = m.length;
    if ((val == m)
      &&
      (xlen > MULTIPLY_SQUARE_THRESHOLD)) {
      return square(m,false); }

    final int ylen = val.length;

    if ((xlen < KARATSUBA_THRESHOLD)
      || (ylen < KARATSUBA_THRESHOLD)) {
      if (val.length == 1) {
        return multiply(m,val[0]); }
      if (m.length == 1) {
        return multiply(val,m[0]); }
      int[] result = multiplyToLen(m,xlen,val,ylen,null);
      result = stripLeadingZeros(result);
      return stripLeadingZeros(result); }
    if ((xlen < TOOM_COOK_THRESHOLD)
      && (ylen < TOOM_COOK_THRESHOLD)) {
      return multiplyKaratsuba(m,val); }
    //
    // In "Hacker's Delight" section 2-13, p.33, it is explained
    // that if x and y are unsigned 32-bit quantities and m and n
    // are their respective numbers of leading zeros within 32
    // bits,
    // then the number of leading zeros within their product as a
    // 64-bit unsigned quantity is either m + n or m + n + 1. If
    // their product is not to overflow, it cannot exceed 32 bits,
    // and so the number of leading zeros of the product within 64
    // bits must be at least 32, i.e., the leftmost set bit is at
    // zero-relative position 31 or less.
    //
    // From the above there are three cases:
    //
    // m + n leftmost set bit condition
    // ----- ---------------- ---------
    // >= 32 x <= 64 - 32 = 32 no overflow
    // == 31 x >= 64 - 32 = 32 possible overflow
    // <= 30 x >= 64 - 31 = 33 definite overflow
    //
    // The "possible overflow" condition cannot be detected by
    // examining data lengths alone and requires further
    // calculation.
    //
    // By analogy, if 'this' and 'val' have m and n as their
    // respective numbers of leading zeros within
    // 32*MAX_MAG_LENGTH
    // bits, then:
    //
    // m + n >= 32*MAX_MAG_LENGTH no overflow
    // m + n == 32*MAX_MAG_LENGTH - 1 possible overflow
    // m + n <= 32*MAX_MAG_LENGTH - 2 definite overflow
    //
    // Note however that if the number of ints in the result
    // were to be MAX_MAG_LENGTH and m[0] < 0, then there would
    // be overflow. As a result the leftmost bit (of m[0])
    // cannot
    // be used and the constraints must be adjusted by one bit to:
    //
    // m + n > 32*MAX_MAG_LENGTH no overflow
    // m + n == 32*MAX_MAG_LENGTH possible overflow
    // m + n < 32*MAX_MAG_LENGTH definite overflow
    //
    // The foregoing leading zero-based discussion is for clarity
    // only. The actual calculations use the estimated bit length
    // of the product as this is more natural to the internal
    // array representation of the magnitude which has no leading
    // zero elements.
    //
    if (!isRecursion) {
      // The bitLength() instance method is not used here as we
      // are only considering the magnitudes as non-negative. The
      // Toom-Cook multiplication algorithm determines the sign
      // at its end from the two signum values.
      if ((bitLength(m,m.length) + bitLength(val,
        val.length)) > (32L * MAX_MAG_LENGTH)) {
        reportOverflow(); } }

    return multiplyToomCook3(m,val); }

  //--------------------------------------------------------------

  public static final int[] multiply (final int[] m,
                                      final int[] val) {
    return multiply(m,val,false); }

  private static final int bitLength (final int n) {
    return 32 - Integer.numberOfLeadingZeros(n); }

  private static int bitLength (final int[] val,
                                final int len) {
    if (len == 0) { return 0; }
    return ((len - 1) << 5) + bitLength(val[0]); }

  //--------------------------------------------------------------
  // Modular Arithmetic
  //--------------------------------------------------------------

  private static final int mulAdd (final int[] out,
                                   final int[] in,
                                   final int offset,
                                   final int len,
                                   final int k) {
    implMulAddCheck(out,in,offset,len);
    return implMulAdd(out,in,offset,len,k); }

  private static final void implMulAddCheck (final int[] out,
                                             final int[] in,
                                             final int offset,
                                             final int len) {
    if (len > in.length) {
      throw new IllegalArgumentException(
        "input length is out of bound: " + len + " > "
          + in.length); }
    if (offset < 0) {
      throw new IllegalArgumentException(
        "input offset is invalid: " + offset); }
    if (offset > (out.length - 1)) {
      throw new IllegalArgumentException(
        "input offset is out of bound: " + offset + " > "
          + (out.length - 1)); }
    if (len > (out.length - offset)) {
      throw new IllegalArgumentException(
        "input len is out of bound: " + len + " > "
          + (out.length - offset)); } }

  private static final int implMulAdd (final int[] out,
                                       final int[] in,
                                       int offset,
                                       final int len,
                                       final int k) {
    final long kLong = loWord(k);
    long carry = 0;

    offset = out.length - offset - 1;
    for (int j = len - 1; j >= 0; j--) {
      final long product =
        (unsigned(in[j]) * kLong) + unsigned(out[offset])
        + carry;
      out[offset--] = (int) product;
      carry = product >>> 32; }
    return (int) carry; }

  private static final int addOne (final int[] a,
                                   int offset,
                                   int mlen,
                                   final int carry) {
    offset = a.length - 1 - mlen - offset;
    final long t = unsigned(a[offset]) + unsigned(carry);

    a[offset] = (int) t;
    if ((t >>> 32) == 0) { return 0; }
    while (--mlen >= 0) {
      if (--offset < 0) { // Carry out of number
        return 1; }
      a[offset]++;
      if (a[offset] != 0) { return 0; } }
    return 1; }

  //--------------------------------------------------------------
  // Shift Operations
  //--------------------------------------------------------------

  public static final int[] shiftRight (final int[] m,
                                        final int n) {
    //if (isZero(m)) { return ZERO; }
    if (n > 0) { return shiftRightImpl(m,n); }
    else if (n == 0) { return stripLeadingZeros(m); }
    // Possible int overflow in {@code -n} is not a trouble,
    // because shiftLeft considers its argument unsigned
    else { return stripLeadingZeros(shiftLeft(m,-n)); } }

  //--------------------------------------------------------------
  // Bitwise Operations
  //--------------------------------------------------------------

  public static final boolean testBit (final int[] m,
                                       final int n) {
    assert 0 < n;
    return (getInt(m,n >>> 5) & (1 << (n & 31))) != 0; }

  public static final int[] setBit (final int[] m,
                                    final int n) {
    assert 0 < n;
    final int intNum = n >>> 5;
    final int[] result = new int[Math.max(intLength(m),intNum + 2)];

    for (int i = 0; i < result.length; i++) {
      result[result.length - i - 1] = getInt(m,i); }
    result[result.length - intNum - 1] |= (1 << (n & 31));
    return stripLeadingZeros(result); }

  public static final int[] clearBit (final int[] m,
                                      final int n) {
    assert 0 < n;
    final int intNum = n >>> 5;
    final int[] result =
      new int[Math.max(intLength(m),((n + 1) >>> 5) + 1)];

    for (int i = 0; i < result.length; i++) {
      result[result.length - i - 1] = getInt(m,i); }

    result[result.length - intNum - 1] &= ~(1 << (n & 31));
    return stripLeadingZeros(result); }

  public static final int[] flipBit (final int[] m,
                                     final int n) {
    assert 0 < n;
    final int intNum = n >>> 5;
    final int[] result =
      new int[Math.max(intLength(m),intNum + 2)];
    for (int i = 0; i < result.length; i++) {
      result[result.length - i - 1] = getInt(m,i); }
    result[result.length - intNum - 1] ^= (1 << (n & 31));
    return stripLeadingZeros(result); }

  public static final int getLowestSetBit (final int[] m) {
    int lsb = 0;
    if (isZero(m)) { lsb -= 1; }
    else {
      // Search for lowest order nonzero int
      int i, b;
      for (i = 0; (b = getInt(m,i)) == 0; i++) { }
      lsb += (i << 5) + Integer.numberOfTrailingZeros(b); }
    return lsb; }

  //--------------------------------------------------------------
  // Miscellaneous Bit Operations
  //--------------------------------------------------------------
  // TODO: BigInteger caches this with the instance. Is it worth
  // having a cache map for these?

  public static final int bitLength (final int[] m) {
    final int len = m.length;
    if (len == 0) { return(0); }
    // Calculate the bit length of the magnitude
    final int n = ((len - 1) << 5) + bitLength(m[0]);
    return n; }

  // TODO: BigInteger caches this with the instance. Is it worth
  // having a cache map for these?

  public static final int bitCount (final int[] m) {
    int bc = 0;
    for (final int mi : m) { bc += Integer.bitCount(mi); }
    return bc + 1; }

  //--------------------------------------------------------------
  /** hex string. */

  public static final String toHexString (final int[] m) {
    final StringBuilder b = new StringBuilder("0x");
    if (0 == m.length) { b.append('0'); }
    for (final int mi : m) { b.append(Integer.toHexString(mi)); }
    return b.toString(); }

  //--------------------------------------------------------------
  // Number interface+
  //--------------------------------------------------------------

  public static final byte[] toByteArray (final int[] m) {
    final int byteLen = (bitLength(m) / 8) + 1;
    final byte[] byteArray = new byte[byteLen];
    for (
      int i = byteLen - 1,
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

  public static final BigInteger bigIntegerValue (final int[] m) {
    return new BigInteger(toByteArray(m)); }

  public static final int intValue (final int[] m) {
    return getInt(m,0); }

  public static final long longValue (final int[] m) {
    long result = 0;
    for (int i = 1; i >= 0; i--) {
      result = (result << 32) + unsigned(getInt(m,i)); }
    return result; }

  //--------------------------------------------------------------

  public static final float floatValue (final int[] m) {
    //if (isZero(m)) { return 0.0f; }

    final int exponent =
      (((m.length - 1) << 5) + bitLength(m[0])) - 1;

    // exponent == floor(log2(abs(this)))
    if (exponent < (Long.SIZE - 1)) { return longValue(m); }
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
    // abs().shiftRight(shift).intValue()
    // We do the shift into an int directly to improve
    // performance.

    final int nBits = shift & 0x1f;
    final int nBits2 = 32 - nBits;

    if (nBits == 0) { twiceSignifFloor = m[0]; }
    else {
      twiceSignifFloor = m[0] >>> nBits;
      if (twiceSignifFloor == 0) {
        twiceSignifFloor =
          (m[0] << nBits2) | (m[1] >>> nBits); } }

    int signifFloor = twiceSignifFloor >> 1;
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
          || (getLowestSetBit(m) < shift));
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

  public static final double doubleValue (final int[] m) {
    //if (isZero(m)) { return 0.0; }

    final int exponent =
      (((m.length - 1) << 5) + bitLength(m[0])) - 1;

    // exponent == floor(log2(abs(this))Double)
    if (exponent < (Long.SIZE - 1)) {
      return longValue(m);
    }
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
    // abs().shiftRight(shift).longValue()
    // We do the shift into a long directly to improve
    // performance.

    final int nBits = shift & 0x1f;
    final int nBits2 = 32 - nBits;

    int highBits;
    int lowBits;
    if (nBits == 0) {
      highBits = m[0];
      lowBits = m[1];
    }
    else {
      highBits = m[0] >>> nBits;
      lowBits = (m[0] << nBits2) | (m[1] >>> nBits);
      if (highBits == 0) {
        highBits = lowBits;
        lowBits = (m[1] << nBits2) | (m[2] >>> nBits);
      }
    }

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
          || (getLowestSetBit(m) < shift));
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
  /* These two arrays are the integer analog of above.
   */
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

  private static final int intLength (final int[] m) {
    return (bitLength(m) >>> 5) + 1; }

  private static final int getInt (final int[] m,
                                   final int n) {
    if (n < 0) { return 0; }
    if (n >= m.length) { return 0; }
    final int mInt = m[m.length - n - 1];
    return mInt; }

  //-------------------------------------------------------------
  // construction
  //-------------------------------------------------------------

  private static final void reportOverflow () {
    throw new ArithmeticException(
      "int[] would overflow supported range"); }

  //-------------------------------------------------------------

  public static final int[] valueOf (final byte[] b,
                                     final int off,
                                     final int len) {
    return stripLeadingZeros(stripLeadingZeros(b,off,len)); }

  public static final int[] valueOf (final byte[] b) {
    return valueOf(b,0,b.length); }

  public static final int[] valueOf (final BigInteger bi) {
    return valueOf(bi.toByteArray()); }

  //-------------------------------------------------------------
  // string parsing
  //-------------------------------------------------------------
  // bitsPerDigit in the given radix times 1024
  // Rounded up to avoid under-allocation.

  private static final long[] bitsPerDigit =
  { 0, 0, 1024, 1624, 2048, 2378, 2648, 2875, 3072, 3247, 3402,
    3543, 3672, 3790, 3899, 4001, 4096, 4186, 4271, 4350, 4426,
    4498, 4567, 4633, 4696, 4756, 4814, 4870, 4923, 4975, 5025,
    5074, 5120, 5166, 5210, 5253, 5295 };

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

  //-------------------------------------------------------------

  public static final int[] valueOf (final String s,
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
    if (cursor == len) { return ZERO; }

    final int numDigits = len - cursor;

    // might be bigger than needed, but stripLeadingZeroInts(int[]) handles that
    final long numBits =
      ((numDigits * bitsPerDigit[radix]) >>> 10) + 1;
    if ((numBits + 31) >= (1L << 32)) {
      reportOverflow(); }
    final int numWords = (int) (numBits + 31) >>> 5;
    final int[] m = new int[numWords];

    // Process first (potentially short) digit group
    int firstGroupLen = numDigits % digitsPerInt[radix];
    if (firstGroupLen == 0) { firstGroupLen = digitsPerInt[radix]; }
    String group = s.substring(cursor,cursor += firstGroupLen);
    m[numWords - 1] = Integer.parseInt(group,radix);
    if (m[numWords - 1] < 0) {
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

  public static final int[] valueOf (final String s) {
    return valueOf(s,10); }

  //--------------------------------------------------------------

  public static final int[] valueOf (final long val) {
    assert 0 <= val;
    if (val == 0) { return ZERO; }
    final int[] m = { (int) (val >>> 32), (int) val, };
    return stripLeadingZeros(m); }

  //--------------------------------------------------------------

  public static final int[] valueOf (final long x,
                                     final int leftShift) {
    if (0L == x) { return ZERO; }
    assert 0L < x;
    return stripLeadingZeros(shiftLeft(x,leftShift)); }

  //--------------------------------------------------------------
  // disable constructor
  //--------------------------------------------------------------

  private Lei () {
    throw new UnsupportedOperationException(
      "can't instantiate " + getClass()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
