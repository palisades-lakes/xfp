package xfp.java.numbers;

import static xfp.java.numbers.Numbers.UNSIGNED_MASK;
import static xfp.java.numbers.Numbers.loWord;
import static xfp.java.numbers.Numbers.unsigned;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import xfp.java.Debug;

/** immutable arbitrary-precision non-negative integers.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-05-22
 */

public final class UnNatural0 extends Number
implements Ringlike<UnNatural0> {

  private static final long serialVersionUID = 1L;

  // Danger!!!
  public final int[] _mag;

  public final boolean isZero () { return 0 == _mag.length; }

  // The following fields are stable variables. A stable
  // variable's value changes at most once from the default zero
  // value to a non-zero stable value. A stable value is
  // calculated lazily on demand.

  private int bitCountPlusOne;
  private int bitLengthPlusOne;
  private int lowestSetBitPlusTwo;
  //private int firstNonzeroIntNumPlusTwo;

  /** This constant limits {@code mag.length} of Naturals to
   * the supported range.
   */
  private static final int MAX_MAG_LENGTH =
    (Integer.MAX_VALUE / Integer.SIZE) + 1; // (1 << 26)

  //--------------------------------------------------------------
  // operations on int[] representing non-negative integers,
  // with zeroth element the most significant.
  // TODO: may be useful to share, move somewhere else?
  //--------------------------------------------------------------

  private static final int[] shiftLeft (final int[] mag,
                                        final int n) {
    final int nInts = n >>> 5;
    final int nBits = n & 0x1f;
    final int magLen = mag.length;
    int newMag[] = null;
    if (nBits == 0) {
      newMag = new int[magLen + nInts];
      System.arraycopy(mag,0,newMag,0,magLen); }
    else {
      int i = 0;
      final int nBits2 = 32 - nBits;
      final int highBits = mag[0] >>> nBits2;
      if (highBits != 0) {
        newMag = new int[magLen + nInts + 1];
        newMag[i++] = highBits; }
      else { newMag = new int[magLen + nInts]; }
      int j = 0;
      while (j < (magLen - 1)) {
        newMag[i++] = (mag[j++] << nBits) | (mag[j] >>> nBits2); }
      newMag[i] = mag[j] << nBits; }
    return newMag; }

  private static final int[] shiftLeft (final long x,
                                        final int leftShift) {
    final int[] xs = { (int) (x >>> 32),
                       (int) (x & 0xFFFFFFFFL), };
    return shiftLeft(xs,leftShift); }

  private static final int[] unsafeStripLeadingZeroInts (final int m[]) {

    final int n = m.length;
    int keep = 0;
    while ((keep < n) && (m[keep] == 0)) { keep++; }
    return keep == 0 ? m : Arrays.copyOfRange(m,keep,n); }

  private static final int[] stripLeadingZeroBytes (final byte a[],
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

  private static final int compareMagnitude (final int[] m1,
                                             final int[] m2) {
    final int len1 = m1.length;
    final int len2 = m2.length;
    if (len1 < len2) { return -1; }
    if (len1 > len2) { return 1; }
    for (int i = 0; i < len1; i++) {
      final int a = m1[i];
      final int b = m2[i];
      if (a != b) { return (unsigned(a) < unsigned(b)) ? -1 : 1; } }
    return 0; }

  private static final int compareMagnitude (final int[] m1,
                                             final long m2) {
    assert 0L <= m2;
    final int len = m1.length;
    if (len > 2) { return 1; }
    final int highWord = (int) (m2 >>> 32);
    if (highWord == 0) {
      if (len < 1) { return -1; }
      if (len > 1) { return 1; }
      final int a = m1[0];
      final int b = (int) m2;
      if (a != b) {
        return ((unsigned(a) < unsigned(b)) ? -1 : 1); }
      return 0; }
    if (len < 2) { return -1; }
    int a = m1[0];
    int b = highWord;
    if (a != b) {
      return ((unsigned(a) < unsigned(b)) ? -1 : 1); }
    a = m1[1];
    b = (int) m2;
    if (a != b) { return ((unsigned(a) < unsigned(b)) ? -1 : 1); }
    return 0; }

  //--------------------------------------------------------------
  // add
  //--------------------------------------------------------------

  private static final int[] add (final int[] x,
                                  final long val) {
    assert 0L <= val;
    long sum = 0;
    int xIndex = x.length;
    int[] result;
    final int highWord = (int) (val >>> 32);
    if (highWord == 0) {
      result = new int[xIndex];
      sum = unsigned(x[--xIndex]) + val;
      result[xIndex] = (int) sum; }
    else {
      if (xIndex == 1) {
        result = new int[2];
        sum = val + unsigned(x[0]);
        result[1] = (int) sum;
        result[0] = (int) (sum >>> 32);
        return result; }
      result = new int[xIndex];
      sum = unsigned(x[--xIndex]) + loWord(val);
      result[xIndex] = (int) sum;
      sum =
        unsigned(x[--xIndex]) + unsigned(highWord)
        + (sum >>> 32);
      result[xIndex] = (int) sum; }

    // Copy remainder of longer number while carry propagation is
    // required
    boolean carry = ((sum >>> 32) != 0);
    while ((xIndex > 0) && carry) {
      carry = ((result[--xIndex] = x[xIndex] + 1) == 0); }
    // Copy remainder of longer number
    while (xIndex > 0) { result[--xIndex] = x[xIndex]; }
    // Grow result if necessary
    if (carry) {
      final int bigger[] = new int[result.length + 1];
      System.arraycopy(result,0,bigger,1,result.length);
      bigger[0] = 0x01;
      return bigger; }
    return result; }

  //--------------------------------------------------------------
  /** Adds the contents of the int arrays x and y. Allocate a new
   * int array to hold the answer.
   */

  private static final int[] add (final int[] x,
                                  final int[] y) {
    // If x is shorter, swap the two arrays
    if (x.length < y.length) { return add(y,x); }
    int xIndex = x.length;
    int yIndex = y.length;
    final int result[] = new int[xIndex];
    long sum = 0;
    if (yIndex == 1) {
      sum = unsigned(x[--xIndex]) + unsigned(y[0]);
      result[xIndex] = (int) sum; }
    else {
      // Add common parts of both numbers
      while (yIndex > 0) {
        sum =
          unsigned(x[--xIndex]) + unsigned(y[--yIndex])
          + (sum >>> 32);
        result[xIndex] = (int) sum; } }
    // Copy remainder of longer number while carry propagation is
    // required
    boolean carry = ((sum >>> 32) != 0);
    while ((xIndex > 0) && carry) {
      carry = ((result[--xIndex] = x[xIndex] + 1) == 0); }
    // Copy remainder of longer number
    while (xIndex > 0) { result[--xIndex] = x[xIndex]; }
    // Grow result if necessary
    if (carry) {
      final int bigger[] = new int[result.length + 1];
      System.arraycopy(result,0,bigger,1,result.length);
      bigger[0] = 0x01;
      return bigger; }
    return result; }
  //--------------------------------------------------------------

  private static final int[] add (final int[] x,
                                  final long y,
                                  final int bitShift)  {
    assert 0L < y;
    if (0 == bitShift) { return add(x,y); }

    final int intShift = bitShift >>> 5;
    final int remShift = bitShift & 0x1f;
    final boolean yCarry = (64 < (remShift + Numbers.hiBit(y)));
    final int ny = intShift + (yCarry ? 3 : 2);

    final int nx = x.length;
    int ix=nx-1;

    final int nr = Math.max(nx,ny);
    final int[] r0 = new int[nr];
    int ir=nr-1;
    final int iy=nr-intShift-1;

    //Debug.println("nx>ny=" + (nx>ny));
    //Debug.println("x=" + toHexString(x));
    //Debug.println("y=" + toHexString(y));
    //Debug.println("shift y="
    //+ Numbers.toHexString(shiftLeft(y,bitShift)));
    //Debug.println("bitShift=" + bitShift);
    //Debug.println("intShift=" + intShift);
    //Debug.println("remShift=" + remShift);
    //Debug.println("hiBit(y)=" + hiBit(y));
    //Debug.println("yCarry=" + yCarry);
    //Debug.println("ny=" + ny);
    //Debug.println("iy=" + iy);
    //Debug.println("nx=" + nx);
    //Debug.println("ix=" + ix);
    //Debug.println("nr=" + nr);
    //Debug.println("ir=" + ir);

    // copy unaffected low order x to result
    while ((iy<ir) && (0<=ix)) { r0[ir--] = x[ix--]; }
    //Debug.println("r0=" + toHexString(r0));
    ir = iy;

    long sum;
    // add y words to x with carry
    final long mid = (y << remShift);
    sum = (mid & 0xFFFFFFFFL);
    //Debug.println("ylo=" + toHexString(mid & 0xFFFFFFFFL));
    if (0<=ix) {
      //Debug.println("x[" + ix + "]=" + toHexString(x[ix]));
      sum += unsigned(x[ix--]); }
    //Debug.println("sum=" + toHexString(sum));
    r0[ir--] = (int) sum;
    //Debug.println("r0=" + toHexString(r0));
    sum = (mid >>> 32) + (sum >>> 32);
    //Debug.println("ymi=" + toHexString(mid >>> 32));
    //Debug.println("sum=" + toHexString(sum));
    if (0<=ix) {
      //Debug.println("x[" + ix + "]=" + toHexString(x[ix]));
      sum += unsigned(x[ix--]); }
    //Debug.println("sum=" + toHexString(sum));
    r0[ir--] = (int) sum;
    //Debug.println("r0=" + toHexString(r0));
    // TODO: i==0 implies remShift==0 ?
    if ((0<=ir) && yCarry) {
      sum = (y >>> (64 - remShift)) + (sum >>> 32);
      //Debug.println("yhi=" + toHexString(y >>> (64 - remShift)));
      //Debug.println("sum=" + toHexString(sum));
      if (0<=ix) {
        //Debug.println("x[" + ix + "]=" + toHexString(x[ix]));
        sum += unsigned(x[ix--]); }
      //Debug.println("sum=" + toHexString(sum));
      r0[ir--] = (int) sum;
      //Debug.println("r0=" + toHexString(r0));
    }

    // handle carry propagation
    boolean carry = ((sum >>> 32) != 0);
    while ((0<=ir) && carry) {
      sum = 0x01L;
      if (0<=ix) { sum += unsigned(x[ix--]); }
      final int is = (int) sum;
      r0[ir--] = is;
      carry = (is == 0); }

    // grow result if one more carry
    if (carry) {
      final int r1[] = new int[nr + 1];
      System.arraycopy(r0,0,r1,1,nr);
      r1[0] = 0x01;
      return r1; }

    // copy remainder of x if any
    while ((0<=ix) && (0<=ir)) { r0[ir--] = x[ix--]; }

    return r0; }

  //--------------------------------------------------------------
  // subtract -- only where answer is positive
  //--------------------------------------------------------------

  private static final int[] subtract (final int[] big,
                                       final long val) {
    final int highWord = (int) (val >>> 32);
    int bigIndex = big.length;
    final int result[] = new int[bigIndex];
    long difference = 0;
    if (highWord == 0) {
      difference = unsigned(big[--bigIndex]) - val;
      result[bigIndex] = (int) difference; }
    else {
      difference = unsigned(big[--bigIndex]) - loWord(val);
      result[bigIndex] = (int) difference;
      difference =
        (unsigned(big[--bigIndex])
          - unsigned(highWord))
        + (difference >> 32);
      result[bigIndex] = (int) difference; }
    // Subtract remainder of longer number while borrow propagates
    boolean borrow = ((difference >> 32) != 0);
    while ((bigIndex > 0) && borrow) {
      borrow = ((result[--bigIndex] = big[bigIndex] - 1) == -1); }
    // Copy remainder of longer number
    while (bigIndex > 0) { result[--bigIndex] = big[bigIndex]; }
    return result; }

  //--------------------------------------------------------------

  //  private static final int[] subtract (final long val,
  //                                       final int[] little) {
  //    final int highWord = (int) (val >>> 32);
  //    if (highWord == 0) {
  //      final int result[] = new int[1];
  //      result[0] = (int) (val - unsigned(little[0]));
  //      return result; }
  //    final int result[] = new int[2];
  //    if (little.length == 1) {
  //      final long difference =
  //        unsigned((int) val) - unsigned(little[0]);
  //      result[1] = (int) difference;
  //      // Subtract remainder of longer number while borrow
  //      // propagates
  //      final boolean borrow = ((difference >> 32) != 0);
  //      if (borrow) { result[0] = highWord - 1; }
  //      // Copy remainder of longer number
  //      else { result[0] = highWord; }
  //      return result; }
  //    long difference =
  //      unsigned((int) val) - unsigned(little[1]);
  //    result[1] = (int) difference;
  //    difference =
  //      (unsigned(highWord) - unsigned(little[0]))
  //      + (difference >> 32);
  //    result[0] = (int) difference;
  //    return result; }

  //--------------------------------------------------------------
  // only valid when little <= big
  private static final int[] subtract (final int[] big,
                                       final int[] little) {
    int bigIndex = big.length;
    final int result[] = new int[bigIndex];
    int littleIndex = little.length;
    long difference = 0;

    // Subtract common parts of both numbers
    while (littleIndex > 0) {
      difference =
        (unsigned(big[--bigIndex])
          - unsigned(little[--littleIndex]))
        + (difference >> 32);
      result[bigIndex] = (int) difference; }

    // Subtract remainder of longer number while borrow propagates
    boolean borrow = ((difference >> 32) != 0);
    while ((bigIndex > 0) && borrow) {
      borrow = ((result[--bigIndex] = big[bigIndex] - 1) == -1); }

    // Copy remainder of longer number
    while (bigIndex > 0) {
      result[--bigIndex] = big[bigIndex]; }

    final int[] r = unsafeStripLeadingZeroInts(result);
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

  private static final int[] subtract (final int[] big,
                                       final long little,
                                       final int bitShift) {
    if (0 == bitShift) { return subtract(big,little); }
    final int intShift = bitShift >>> 5;
    final int n = big.length;
    final int result[] = new int[n];
    int i=n-1;
    // copy unaffected low order ints
    for (;i>(n-intShift-1);i--) { result[i] = big[i]; }

    final int remShift = bitShift & 0x1f;
    final long mid = (little << remShift);

    long difference = 0;

    // Subtract common parts of both numbers
    difference = (unsigned(big[i]) - (mid & 0xFFFFFFFFL));
    result[i] = (int) difference;
    i--;
    difference = (unsigned(big[i]) - (mid >>> 32))
      + (difference >> 32);
    result[i] = (int) difference;
    i--;
    // TODO: i==0 implies remShift==0 ?
    if ((0<=i) && (0<remShift)) {
      difference =
        (unsigned(big[i]) - (little >>> (64 - remShift)))
        + (difference >> 32);
      result[i] = (int) difference;
      i--; }

    // Subtract remainder of longer number while borrow propagates
    boolean borrow = ((difference >> 32) != 0);
    while ((0<=i) && borrow) {
      borrow = ((result[i] = big[i] - 1) == -1); i--;}
    // Copy remainder of longer number
    while (0<=i) { result[i] = big[i]; i--; }
    return unsafeStripLeadingZeroInts(result); }

  //--------------------------------------------------------------
  // arithmetic
  //--------------------------------------------------------------

  @Override
  public final UnNatural0 add (final UnNatural0 val) {
    if (val.isZero()) { return this; }
    if (isZero()) { return val; }
    return make(add(_mag,val._mag)); }

  public final UnNatural0 add (final long val) {
    assert 0L <= val;
    if (0L == val) { return this; }
    if (isZero()) { return valueOf(val); }
    return make(add(_mag,val)); }

  public final UnNatural0 add (final long val,
                               final int leftShift) {
    if (isZero()) { return make(shiftLeft(val,leftShift)); }
    return make(add(_mag,val,leftShift)); }

  // only when val <= this
  public final UnNatural0 subtract (final long val) {
    assert 0L <= val;
    if (0L == val) { return this; }
    final int c = compareTo(val);
    assert 0 <= c;
    if (0 == c) { return ZERO; }
    return make(subtract(_mag,val)); }

  // only when val <= this
  @Override
  public final UnNatural0 subtract (final UnNatural0 val) {
    if (val.isZero()) { return this; }
    final int c = compareTo(val);
    assert 0 <= c :
      "\nc=" + c
      + "\nthis=" + toString(0x10)
      + "\nthat=" + val.toString(0x10);
    if (c == 0) { return ZERO; }
    return make(subtract(_mag,val._mag)); }

  // only when (val << leftShift) <= this
  public final UnNatural0 subtract (final long val,
                                    final int leftShift) {
    assert 0L <= val;
    if (0L == val) { return this; }
    return make(subtract(_mag,val,leftShift)); }

  // only when this <= (val << leftShift)
  public final UnNatural0 subtractFrom (final long val,
                                        final int leftShift) {
    assert 0L <= val;
    if (0L == val) { assert isZero(); return ZERO; }
    if (isZero()) { return make(shiftLeft(val,leftShift)); }
    return make(subtract(shiftLeft(val,leftShift),_mag)); }

  //--------------------------------------------------------------
  // squaring --- used in multiplication
  //--------------------------------------------------------------
  /** The threshold value for using Karatsuba squaring. If the
   * number of ints in the number are larger than this value,
   * Karatsuba squaring will be used. This value is found
   * experimentally to work well.
   */

  private static final int KARATSUBA_SQUARE_THRESHOLD = 128;

  /** The threshold value for using Toom-Cook squaring. If the
   * number of ints in the number are larger than this value,
   * Toom-Cook squaring will be used. This value is found
   * experimentally to work well.
   */

  private static final int TOOM_COOK_SQUARE_THRESHOLD = 216;

  /** Returns a Natural whose value is {@code (this<sup>2</sup>)}.
   * If the invocation is recursive certain overflow checks are
   * skipped.
   * @param isRecursion whether this is a recursive invocation
   * @return {@code this<sup>2</sup>}
   */

  private final UnNatural0 square (final boolean isRecursion) {
    if (isZero()) { return ZERO; }
    final int len = _mag.length;

    if (len < KARATSUBA_SQUARE_THRESHOLD) {
      final int[] z = squareToLen(_mag,len,null);
      return make(z); }
    if (len < TOOM_COOK_SQUARE_THRESHOLD) {
      return squareKaratsuba(); }
    // For a discussion of overflow detection see multiply()
    if (!isRecursion) {
      if (bitLength(_mag,_mag.length) > (16L * MAX_MAG_LENGTH)) {
        reportOverflow(); } }
    return squareToomCook3(); }

  private final UnNatural0 square () { return square(false); }

  /**
   * Squares the contents of the int array x. The result is placed
   * into the
   * int array z. The contents of x are not changed.
   */
  private static final int[] squareToLen (final int[] x,
                                          final int len,
                                          int[] z) {
    final int zlen = len << 1;
    if ((z == null) || (z.length < zlen)) {
      z = new int[zlen]; }

    // Execute checks before calling intrinsic method.
    implSquareToLenChecks(x,len,z,zlen);
    return implSquareToLen(x,len,z,zlen); }

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

  /** Java Runtime may use intrinsic for this method.
   */

  private static int[] implSquareToLen (final int[] x,
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

    return z;
  }

  /** Squares a Natural using the Karatsuba squaring algorithm.
   * It should be used when both numbers are larger than a certain
   * threshold (found experimentally). It is a recursive
   * divide-and-conquer algorithm that has better asymptotic
   * performance than the algorithm used in squareToLen.
   */
  private final UnNatural0 squareKaratsuba () {
    final int half = (_mag.length + 1) / 2;

    final UnNatural0 xl = getLower(half);
    final UnNatural0 xh = getUpper(half);

    final UnNatural0 xhs = xh.square();  // xhs = xh^2
    final UnNatural0 xls = xl.square();  // xls = xl^2

    // xh^2 << 64 + (((xl+xh)^2 - (xh^2 + xl^2)) << 32) + xl^2
    return xhs.shiftLeft(half * 32)
      .add(xl.add(xh).square().subtract(xhs.add(xls)))
      .shiftLeft(half * 32).add(xls); }

  /** Squares a Natural using the 3-way Toom-Cook squaring
   * algorithm. It should be used when both numbers are larger
   * than a certain threshold (found experimentally). It is a
   * recursive divide-and-conquer algorithm that has better
   * asymptotic performance than the algorithm used in
   * squareToLen or squareKaratsuba.
   */
  private final UnNatural0 squareToomCook3 () {
    final int len = _mag.length;

    // k is the size (in ints) of the lower-order slices.
    final int k = (len + 2) / 3;   // Equal to ceil(largest/3)

    // r is the size (in ints) of the highest-order slice.
    final int r = len - (2 * k);

    // Obtain slices of the numbers. a2 is the most significant
    // bits of the number, and a0 the least significant.
    UnNatural0 a0, a1, a2;
    a2 = getToomSlice(k,r,0,len);
    a1 = getToomSlice(k,r,1,len);
    a0 = getToomSlice(k,r,2,len);
    UnNatural0 v0, v1, v2, vm1, vinf, t1, t2, tm1, da1;

    v0 = a0.square(true);
    da1 = a2.add(a0);
    vm1 = da1.subtract(a1).square(true);
    da1 = da1.add(a1);
    v1 = da1.square(true);
    vinf = a2.square(true);
    v2 = da1.add(a2).shiftLeft(1).subtract(a0).square(true);

    // The algorithm requires two divisions by 2 and one by 3.
    // All divisions are known to be exact, that is, they do not
    // produce
    // remainders, and all results are positive. The divisions by
    // 2 are
    // implemented as right shifts which are relatively efficient,
    // leaving
    // only a division by 3.
    // The division by 3 is done by an optimized algorithm for
    // this case.
    t2 = v2.subtract(vm1).exactDivideBy3();
    tm1 = v1.subtract(vm1).shiftRight(1);
    t1 = v1.subtract(v0);
    t2 = t2.subtract(t1).shiftRight(1);
    t1 = t1.subtract(tm1).subtract(vinf);
    t2 = t2.subtract(vinf.shiftLeft(1));
    tm1 = tm1.subtract(t2);

    // Number of bits to shift left.
    final int ss = k * 32;

    return vinf.shiftLeft(ss).add(t2).shiftLeft(ss).add(t1)
      .shiftLeft(ss).add(tm1).shiftLeft(ss).add(v0); }

  //--------------------------------------------------------------
  // multiply
  //--------------------------------------------------------------
  /** The threshold value for using squaring code to perform
   * multiplication of a {@code Natural} instance by itself. If
   * the number of ints in the number are larger than this value,
   * {@code multiply(this)} will return {@code square()}.
   */
  private static final int MULTIPLY_SQUARE_THRESHOLD = 20;

  //--------------------------------------------------------------
  /** The threshold value for using Karatsuba multiplication. If
   * the number of ints in both arrays are greater, then
   * Karatsuba multiplication will be used.
   */

  private static final int KARATSUBA_THRESHOLD = 80;

  /** Returns a new Natural representing n lower ints of the
   * number. Used by Karatsuba multiplication and Karatsuba
   * squaring.
   */

  private final UnNatural0 getLower (final int n) {
    final int len = _mag.length;
    if (len <= n) { return this; }
    final int lowerInts[] = new int[n];
    System.arraycopy(_mag,len - n,lowerInts,0,n);
    return valueOf(lowerInts); }

  /** Returns a new Natural representing mag.length-n upper
   * ints of the number. This is used by Karatsuba multiplication
   * and * Karatsuba squaring.
   */

  private final UnNatural0 getUpper (final int n) {
    final int len = _mag.length;
    if (len <= n) { return ZERO; }
    final int upperLen = len - n;
    final int upperInts[] = new int[upperLen];
    System.arraycopy(_mag,0,upperInts,0,upperLen);
    return valueOf(upperInts); }

  /** Multiplies two Naturals using the Karatsuba multiplication
   * algorithm. This is a recursive divide-and-conquer algorithm
   * which is more efficient for large numbers than what is
   * commonly called the grade-school" algorithm used in
   * multiplyToLen. If the numbers to be multiplied have length n,
   * the "grade-school" algorithm has an asymptotic complexity of
   * O(n^2). In contrast, the Karatsuba algorithm has complexity
   * of O(n^(log2(3))), or O(n^1.585). It achieves this
   * increased performance by doing 3 multiplies instead of 4 when
   * evaluating the product. As it has some overhead, should be
   * used when both numbers are larger than a certain threshold
   * (found experimentally).
   *
   * See: http://en.wikipedia.org/wiki/Karatsuba_algorithm
   */

  private static final UnNatural0 multiplyKaratsuba (final UnNatural0 x,
                                                     final UnNatural0 y) {
    final int xlen = x._mag.length;
    final int ylen = y._mag.length;

    // The number of ints in each half of the number.
    final int half = (Math.max(xlen,ylen) + 1) / 2;

    // xl and yl are the lower halves of x and y respectively,
    // xh and yh are the upper halves.
    final UnNatural0 xl = x.getLower(half);
    final UnNatural0 xh = x.getUpper(half);
    final UnNatural0 yl = y.getLower(half);
    final UnNatural0 yh = y.getUpper(half);

    final UnNatural0 p1 = xh.multiply(yh);  // p1 = xh*yh
    final UnNatural0 p2 = xl.multiply(yl);  // p2 = xl*yl

    // p3=(xh+xl)*(yh+yl)
    final UnNatural0 p3 = xh.add(xl).multiply(yh.add(yl));

    // result = p1 * 2^(32*2*half) + (p3 - p1 - p2) * 2^(32*half)
    // + p2
    final UnNatural0 result =
      p1.shiftLeft(32 * half).add(p3.subtract(p1).subtract(p2))
      .shiftLeft(32 * half).add(p2);

    return result; }

  //--------------------------------------------------------------
  /** The threshold value for using 3-way Toom-Cook multiplication.
   * If the number of ints in each array is greater than the
   * Karatsuba threshold, and the number of ints in at least one
   * of the arrays is greater than this threshold, then
   * Toom-Cook multiplication will be used.
   */
  private static final int TOOM_COOK_THRESHOLD = 240;

  /** Returns a slice of a Natural for use in Toom-Cook
   * multiplication.
   * @param lowerSize The size of the lower-order bit slices.
   * @param upperSize The size of the higher-order bit slices.
   * @param slice The index of which slice is requested, which
   * must be a number from 0 to size-1. Slice 0 is the highest-
   * order bits, and slice size-1 are the lowest-order bits.
   * Slice 0 may be of different size than the other slices.
   * @param fullsize The size of the larger integer array, used to
   * align slices to the appropriate position when multiplying
   * different-sized numbers.
   */
  private final UnNatural0 getToomSlice (final int lowerSize,
                                         final int upperSize,
                                         final int slice,
                                         final int fullsize) {
    final int len = _mag.length;
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
    if ((start == 0) && (sliceSize >= len)) { return this; }
    final int intSlice[] = new int[sliceSize];
    System.arraycopy(_mag,start,intSlice,0,sliceSize);
    return make(intSlice); }

  /** Does an exact division (that is, the remainder is known to
   * be zero) of the specified number by 3. This is used in
   * Toom-Cook multiplication. This is an efficient algorithm that
   * runs in linear time. If the argument is not exactly divisible
   * by 3, results are undefined. Note that this is expected to be
   * called with positive arguments only.
   */
  private final UnNatural0 exactDivideBy3 () {
    final int len = _mag.length;
    int[] result = new int[len];
    long x, w, q, borrow;
    borrow = 0L;
    for (int i = len - 1; i >= 0; i--) {
      x = unsigned(_mag[i]);
      w = x - borrow;
      if (borrow > x) { // Did we make the number go negative?
        borrow = 1L; }
      else { borrow = 0L; }
      // 0xAAAAAAAB is the modular inverse of 3 (mod 2^32). Thus,
      // the effect of this is to divide by 3 (mod 2^32).
      // This is much faster than division on most architectures.
      q = (w * 0xAAAAAAABL) & UNSIGNED_MASK;
      result[i] = (int) q;
      // Now check the borrow. The second check can of course be
      // eliminated if the first fails.
      if (q >= 0x55555556L) {
        borrow++;
        if (q >= 0xAAAAAAABL) { borrow++; } } }
    result = unsafeStripLeadingZeroInts(result);
    return new UnNatural0(result); }

  /** Multiplies two Naturals using a 3-way Toom-Cook
   * multiplication algorithm. This is a recursive
   * divide-and-conquer algorithm which is more efficient for
   * large numbers than what is commonly called the "grade-school"
   * algorithm used in multiplyToLen. If the numbers to be
   * multiplied have length n, the "grade-school" algorithm has an
   * asymptotic complexity of O(n^2). In contrast, 3-way Toom-Cook
   * has a complexity of about O(n^1.465). It achieves this
   * increased asymptotic performance by breaking each number into
   * three parts and by doing 5 multiplies instead of 9 when
   * evaluating the product. Due to overhead (additions, shifts,
   * and one division) in the Toom-Cook algorithm, it should only
   * be used when both numbers are larger than a certain threshold
   * (found experimentally). This threshold is generally larger
   * than that for Karatsuba multiplication, so this algorithm is
   * generally only used when numbers become significantly larger.
   *
   * The algorithm used is the "optimal" 3-way Toom-Cook algorithm
   * outlined by Marco Bodrato.
   *
   * See: http://bodrato.it/toom-cook/
   * http://bodrato.it/papers/#WAIFI2007
   *
   * "Towards Optimal Toom-Cook Multiplication for Univariate and
   * Multivariate Polynomials in Characteristic 2 and 0." by Marco
   * BODRATO;
   * In C.Carlet and B.Sunar, Eds., "WAIFI'07 proceedings", p.
   * 116-133,
   * LNCS #4547. Springer, Madrid, Spain, June 21-22, 2007.
   */

  private static final UnNatural0 multiplyToomCook3 (final UnNatural0 a,
                                                     final UnNatural0 b) {
    Debug.println("a=" + a.toString(0x10));
    final int alen = a._mag.length;
    final int blen = b._mag.length;

    final int largest = Math.max(alen,blen);

    // k is the size (in ints) of the lower-order slices.
    final int k = (largest + 2) / 3;   // Equal to ceil(largest/3)

    // r is the size (in ints) of the highest-order slice.
    final int r = largest - (2 * k);

    // Obtain slices of the numbers. a2 and b2 are the most
    // significant bits of the numbers a and b, and a0 and b0 the
    // least significant.
    final UnNatural0 a2 = a.getToomSlice(k,r,0,largest);
    final UnNatural0 a1 = a.getToomSlice(k,r,1,largest);
    final UnNatural0 a0 = a.getToomSlice(k,r,2,largest);
    final UnNatural0 b2 = b.getToomSlice(k,r,0,largest);
    final UnNatural0 b1 = b.getToomSlice(k,r,1,largest);
    final UnNatural0 b0 = b.getToomSlice(k,r,2,largest);

    final UnNatural0 v0 = a0.multiply(b0,true);
    UnNatural0 da1 = a2.add(a0);
    UnNatural0 db1 = b2.add(b0);
    // might be negative
    final UnNatural0 da1_a1;
    final int ca = da1.compareTo(a1);
    if (0 < ca) { da1_a1 = da1.subtract(a1); }
    else { da1_a1 = a1.subtract(da1); }
    // might be negative
    final UnNatural0 db1_b1;
    final int cb = db1.compareTo(b1);
    if (0 < cb) { db1_b1 = db1.subtract(b1); }
    else { db1_b1 = b1.subtract(db1); }
    final int cv = ca * cb;
    final UnNatural0 vm1 = da1_a1.multiply(db1_b1,true);

    da1 = da1.add(a1);
    db1 = db1.add(b1);
    final UnNatural0 v1 = da1.multiply(db1,true);
    final UnNatural0 v2 = da1.add(a2).shiftLeft(1).subtract(a0)
      .multiply(
        db1.add(b2).shiftLeft(1).subtract(b0),true);
    final UnNatural0 vinf = a2.multiply(b2,true);

    // The algorithm requires two divisions by 2 and one by 3.
    // All divisions are known to be exact, that is, they do not
    // produce remainders, and all results are positive. The
    // divisions by 2 are implemented as right shifts which are
    // relatively efficient, leaving only an exact division by 3,
    // which is done by a specialized linear-time algorithm.
    UnNatural0 t2;
    // handle missing sign of vm1
    if (0 < cv) { t2 = v2.subtract(vm1).exactDivideBy3(); }
    else { t2 = v2.add(vm1).exactDivideBy3(); }
    
    UnNatural0 tm1;
    // handle missing sign of vm1
    if (0 < cv) { tm1 = v1.subtract(vm1).shiftRight(1); }
    else { tm1 = v1.add(vm1).shiftRight(1);}
    
    UnNatural0 t1 = v1.subtract(v0);
    t2 = t2.subtract(t1).shiftRight(1);
    t1 = t1.subtract(tm1).subtract(vinf);
    t2 = t2.subtract(vinf.shiftLeft(1));
    tm1 = tm1.subtract(t2);

    // Number of bits to shift left.
    final int ss = k * 32;

    return vinf
      .shiftLeft(ss).add(t2)
      .shiftLeft(ss).add(t1)
      .shiftLeft(ss).add(tm1)
      .shiftLeft(ss).add(v0); }

  //--------------------------------------------------------------

  private static final UnNatural0 multiply (final int[] x,
                                            final int y) {
    if (Integer.bitCount(y) == 1) {
      return make(shiftLeft(x,Integer.numberOfTrailingZeros(y))); }
    final int xlen = x.length;
    int[] rmag = new int[xlen + 1];
    long carry = 0;
    final long yl = y & UNSIGNED_MASK;
    int rstart = rmag.length - 1;
    for (int i = xlen - 1; i >= 0; i--) {
      final long product = (unsigned(x[i]) * yl) + carry;
      rmag[rstart--] = (int) product;
      carry = product >>> 32; }
    if (carry == 0L) {
      rmag = Arrays.copyOfRange(rmag,1,rmag.length); }
    else {
      rmag[rstart] = (int) carry; }
    return make(rmag); }


  //  private final Natural multiply (final long v) {
  //    assert 0L <= v;
  //    if (v == 0L) { return ZERO; }
  //    final long dh = v >>> 32;      // higher order bits
  //    final long dl = v & UNSIGNED_MASK; // lower order bits
  //    final int xlen = _mag.length;
  //    final int[] value = _mag;
  //    int[] rmag =
  //      (dh == 0L) ? (new int[xlen + 1]) : (new int[xlen + 2]);
  //      long carry = 0;
  //      int rstart = rmag.length - 1;
  //      for (int i = xlen - 1; i >= 0; i--) {
  //        final long product = (unsigned(value[i]) * dl) + carry;
  //        rmag[rstart--] = (int) product;
  //        carry = product >>> 32; }
  //      rmag[rstart] = (int) carry;
  //      if (dh != 0L) {
  //        carry = 0;
  //        rstart = rmag.length - 2;
  //        for (int i = xlen - 1; i >= 0; i--) {
  //          final long product =
  //            (unsigned(value[i]) * dh)
  //            + unsigned(rmag[rstart]) + carry;
  //          rmag[rstart--] = (int) product;
  //          carry = product >>> 32; }
  //        rmag[0] = (int) carry; }
  //      if (carry == 0L) {
  //        rmag = Arrays.copyOfRange(rmag,1,rmag.length); }
  //      return make(rmag); }

  //--------------------------------------------------------------

  private static final void multiplyToLenCheck (final int[] array,
                                                final int length) {
    // not an error because multiplyToLen won't execute
    // if len <= 0
    if (length <= 0) { return; }
    Objects.requireNonNull(array);
    if (length > array.length) {
      throw new ArrayIndexOutOfBoundsException(length-1); } }

  private static final int[] implMultiplyToLen (final int[] x,
                                                final int xlen,
                                                final int[] y,
                                                final int ylen,
                                                int[] z) {
    final int xstart = xlen - 1;
    final int ystart = ylen - 1;
    if ((z == null) || (z.length < (xlen + ylen))) {
      z = new int[xlen + ylen]; }
    long carry = 0;
    for (int j = ystart, k = ystart + 1 + xstart;
      j >= 0;
      j--, k--) {
      final long product =
        (unsigned(y[j]) * unsigned(x[xstart])) + carry;
      z[k] = (int) product;
      carry = product >>> 32; }
    z[xstart] = (int) carry;
    for (int i = xstart - 1; i >= 0; i--) {
      carry = 0;
      for (int j = ystart, k = ystart + 1 + i; j >= 0; j--, k--) {
        final long product =
          (unsigned(y[j]) * unsigned(x[i]))
          + unsigned(z[k]) + carry;
        z[k] = (int) product;
        carry = product >>> 32; }
      z[i] = (int) carry; }
    return z; }

  /** Multiplies leading elements of x and y into z.
   * No leading zeros in z.
   */

  private static final int[] multiplyToLen (final int[] x,
                                            final int xlen,
                                            final int[] y,
                                            final int ylen,
                                            final int[] z) {
    multiplyToLenCheck(x,xlen);
    multiplyToLenCheck(y,ylen);
    return implMultiplyToLen(x,xlen,y,ylen,z); }

  //--------------------------------------------------------------

  private final UnNatural0 multiply (final UnNatural0 that,
                                     final boolean isRecursion) {
    Debug.println("multiply " + isRecursion);
    Debug.println("this=" + this.toString(0x10));
    Debug.println("that=" + that.toString(0x10));
    //(new Throwable()).printStackTrace(System.out);
    if ((that.isZero()) || (isZero())) { return ZERO; }
    final int xlen = _mag.length;
    if ((that == this) && (xlen > MULTIPLY_SQUARE_THRESHOLD)) {
      return square(); }

    final int ylen = that._mag.length;

    if ((xlen < KARATSUBA_THRESHOLD)
      || (ylen < KARATSUBA_THRESHOLD)) {
      if (that._mag.length == 1) {
        return multiply(_mag,that._mag[0]); }
      if (_mag.length == 1) {
        return multiply(that._mag,_mag[0]); }
      int[] result = multiplyToLen(_mag,xlen,that._mag,ylen,null);
      result = unsafeStripLeadingZeroInts(result);
      return make(result); }
    if ((xlen < TOOM_COOK_THRESHOLD)
      && (ylen < TOOM_COOK_THRESHOLD)) {
      return multiplyKaratsuba(this,that); }
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
    // examning data lengths alone and requires further
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
    // were to be MAX_MAG_LENGTH and mag[0] < 0, then there would
    // be overflow. As a result the leftmost bit (of mag[0])
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
      if ((bitLength(_mag,_mag.length) + bitLength(that._mag,
        that._mag.length)) > (32L * MAX_MAG_LENGTH)) {
        reportOverflow(); } }

    return multiplyToomCook3(this,that); }

  //--------------------------------------------------------------

  @Override
  public final UnNatural0 multiply (final UnNatural0 that) {
    return multiply(that,false); }

  //--------------------------------------------------------------
  // Division
  //--------------------------------------------------------------

  public static final int BURNIKEL_ZIEGLER_THRESHOLD = 80;
  public static final int BURNIKEL_ZIEGLER_OFFSET = 40;

  private static final boolean 
  useKnuthDivision (final UnNatural0 num,
                    final UnNatural0 den) {
    final int nn = num._mag.length;
    final int nd = den._mag.length;
    return 
      (nd < BURNIKEL_ZIEGLER_THRESHOLD)
      || 
      ((nn-nd) < BURNIKEL_ZIEGLER_OFFSET); }
  
  //--------------------------------------------------------------
  // Knuth algorithm
  //--------------------------------------------------------------

  private final UnNatural0 
  divideKnuth (final UnNatural0 that) {
    final MutableUnNatural q = new MutableUnNatural();
    final MutableUnNatural a = MutableUnNatural.valueOf(this._mag);
    final MutableUnNatural b = MutableUnNatural.valueOf(that._mag);
    a.divideKnuth(b,q,false);
    return valueOf(q.getMagnitudeArray()); }

  /** Long division */
  private final UnNatural0[] 
    divideAndRemainderKnuth (final UnNatural0 that) {
    final MutableUnNatural q = new MutableUnNatural();
    final MutableUnNatural num = MutableUnNatural.valueOf(this._mag);
    final MutableUnNatural den = MutableUnNatural.valueOf(that._mag);
    final MutableUnNatural r = num.divideKnuth(den,q);
    return new UnNatural0[] 
      { valueOf(q.getMagnitudeArray()),
        valueOf(r.getMagnitudeArray()), }; }

  private final UnNatural0 remainderKnuth (final UnNatural0 that) {
    final MutableUnNatural q = new MutableUnNatural();
    final MutableUnNatural num = MutableUnNatural.valueOf(this._mag);
    final MutableUnNatural den = MutableUnNatural.valueOf(that._mag);
    final MutableUnNatural r = num.divideKnuth(den,q);
    return valueOf(r.getMagnitudeArray()); }

  //--------------------------------------------------------------

  private final UnNatural0[] 
    divideAndRemainderBurnikelZiegler (final UnNatural0 that) {
    final MutableUnNatural q = new MutableUnNatural();
    final MutableUnNatural num = MutableUnNatural.valueOf(this._mag);
    final MutableUnNatural den = MutableUnNatural.valueOf(that._mag);
    final MutableUnNatural r =
      num.divideAndRemainderBurnikelZiegler(den,q);
    final UnNatural0 qq = 
      q.isZero() ? ZERO : valueOf(q.getMagnitudeArray());
    final UnNatural0 rr = 
      r.isZero() ? ZERO :valueOf(r.getMagnitudeArray());
    return new UnNatural0[] { qq, rr }; }

  private final UnNatural0 
  divideBurnikelZiegler (final UnNatural0 that) {
    return divideAndRemainderBurnikelZiegler(that)[0]; }

  private final UnNatural0 
  remainderBurnikelZiegler (final UnNatural0 that) {
    return divideAndRemainderBurnikelZiegler(that)[1]; }

  //--------------------------------------------------------------
  // division Ringlike api
  //--------------------------------------------------------------

  @Override
  public final UnNatural0 
  divide (final UnNatural0 that) {
    if (useKnuthDivision(this,that)) { return divideKnuth(that); }
    return divideBurnikelZiegler(that); }

  @Override
  public List<UnNatural0> 
  divideAndRemainder (final UnNatural0 that) {
    if (useKnuthDivision(this,that)) {
      return Arrays.asList(divideAndRemainderKnuth(that)); }
    return 
      Arrays.asList(divideAndRemainderBurnikelZiegler(that)); }

  @Override
  public final UnNatural0 remainder (final UnNatural0 that) {
    if (useKnuthDivision(this,that)) {
      return remainderKnuth(that); }
    return remainderBurnikelZiegler(that); }


  //--------------------------------------------------------------
  // shifts a up to len left n bits assumes no leading zeros,
  // 0<=n<32
  private static final void 
  primitiveLeftShift (final int[] a, final int len,
                                  final int n) {
    if ((len == 0) || (n == 0)) { return; }

    final int n2 = 32 - n;
    for (int i = 0, c = a[i], m = (i + len) - 1; i < m; i++) {
      final int b = c;
      c = a[i + 1];
      a[i] = (b << n) | (c >>> n2);
    }
    a[len - 1] <<= n;
  }

  /**
   * Calculate bitlength of contents of the first len elements an
   * int array,
   * assuming there are no leading zero ints.
   */
  private static int bitLength (final int[] val, final int len) {
    if (len == 0) { return 0; }
    return ((len - 1) << 5) + Numbers.bitLength(val[0]);
  }

  //--------------------------------------------------------------
  // Modular Arithmetic
  //--------------------------------------------------------------

  //  /**
  //   * Returns a Natural whose value is {@code (this mod m}).
  //   * This method
  //   * differs from {@code remainder} in that it always returns a
  //   * <i>non-negative</i> Natural.
  //   *
  //   * @param m
  //   *          the modulus.
  //   * @return {@code this mod m}
  //   * @throws ArithmeticException
  //   *           {@code m} &le; 0
  //   * @see #remainder
  //   */
  //  public Natural mod (final Natural m) {
  //    if (m.signum <= 0) {
  //      throw new ArithmeticException(
  //        "Natural: modulus not positive");
  //    }
  //
  //    final Natural result = this.remainder(m);
  //    return (result.signum >= 0 ? result : result.add(m));
  //  }
  //
  //  static int[] bnExpModThreshTable =
  //  { 7, 25, 81, 241, 673, 1793, Integer.MAX_VALUE }; // Sentinel

  /**
   * Multiply an array by one word k and add to result, return the
   * carry
   */
  static int mulAdd (final int[] out, final int[] in,
                     final int offset, final int len,
                     final int k) {
    implMulAddCheck(out,in,offset,len);
    return implMulAdd(out,in,offset,len,k);
  }

  private static void implMulAddCheck (final int[] out,
                                       final int[] in,
                                       final int offset,
                                       final int len) {
    if (len > in.length) {
      throw new IllegalArgumentException(
        "input length is out of bound: " + len + " > "
          + in.length);
    }
    if (offset < 0) {
      throw new IllegalArgumentException(
        "input offset is invalid: " + offset);
    }
    if (offset > (out.length - 1)) {
      throw new IllegalArgumentException(
        "input offset is out of bound: " + offset + " > "
          + (out.length - 1));
    }
    if (len > (out.length - offset)) {
      throw new IllegalArgumentException(
        "input len is out of bound: " + len + " > "
          + (out.length - offset));
    }
  }

  private static final int implMulAdd (final int[] out,
                                       final int[] in,
                                       int offset,
                                       final int len,
                                       final int k) {
    final long kLong = k & UNSIGNED_MASK;
    long carry = 0;

    offset = out.length - offset - 1;
    for (int j = len - 1; j >= 0; j--) {
      final long product =
        (unsigned(in[j]) * kLong) + unsigned(out[offset])
        + carry;
      out[offset--] = (int) product;
      carry = product >>> 32;
    }
    return (int) carry;
  }

  /**
   * Add one word to the number a mlen words into a. Return the
   * resulting
   * carry.
   */
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
        return 1;
      }
      a[offset]++;
      if (a[offset] != 0) { return 0; }
    }
    return 1;
  }

  //--------------------------------------------------------------
  // Shift Operations
  //--------------------------------------------------------------

  /**
   * Returns a Natural whose value is {@code (this << n)}.
   * The shift distance, {@code n}, may be negative, in which case
   * this method performs a right shift.
   * (Computes <code>floor(this * 2<sup>n</sup>)</code>.)
   *
   * @param n
   *          shift distance, in bits.
   * @return {@code this << n}
   * @see #shiftRight
   */
  public final UnNatural0 shiftLeft (final int n) {
    if (isZero()) { return ZERO; }
    if (n > 0) { return make(shiftLeft(_mag,n)); }
    else if (n == 0) { return this; }
    // Possible int overflow in (-n) is not a trouble,
    // because shiftRightImpl considers its argument unsigned
    else { return shiftRightImpl(-n); } }

  /**
   * Returns a Natural whose value is {@code (this >> n)}. Sign
   * extension is performed. The shift distance, {@code n}, may be
   * negative, in which case this method performs a left shift.
   * (Computes <code>floor(this / 2<sup>n</sup>)</code>.)
   *
   * @param n
   *          shift distance, in bits.
   * @return {@code this >> n}
   * @see #shiftLeft
   */
  public final UnNatural0 shiftRight (final int n) {
    if (isZero()) { return ZERO; }
    if (n > 0) { return shiftRightImpl(n); }
    else if (n == 0) { return this; }
    // Possible int overflow in {@code -n} is not a trouble,
    // because shiftLeft considers its argument unsigned
    else { return make(shiftLeft(_mag,-n)); } }

  /**
   * Returns a Natural whose value is {@code (this >> n)}. The
   * shift
   * distance, {@code n}, is considered unsigned.
   * (Computes <code>floor(this * 2<sup>-n</sup>)</code>.)
   *
   * @param n
   *          unsigned shift distance, in bits.
   * @return {@code this >> n}
   */
  private final UnNatural0 shiftRightImpl (final int n) {
    final int nInts = n >>> 5;
    final int nBits = n & 0x1f;
    final int magLen = _mag.length;
    int newMag[] = null;

    // Special case: entire contents shifted off the end
    if (nInts >= magLen) { return ZERO; }

    if (nBits == 0) {
      final int newMagLen = magLen - nInts;
      newMag = Arrays.copyOf(_mag,newMagLen);
    }
    else {
      int i = 0;
      final int highBits = _mag[0] >>> nBits;
      if (highBits != 0) {
        newMag = new int[magLen - nInts];
        newMag[i++] = highBits;
      }
      else {
        newMag = new int[magLen - nInts - 1];
      }

      final int nBits2 = 32 - nBits;
      int j = 0;
      while (j < (magLen - nInts - 1)) {
        newMag[i++] = (_mag[j++] << nBits2) | (_mag[j] >>> nBits);
      }
    }
    return new UnNatural0(newMag); }

  //  private static final int[] javaIncrement (int[] val) {
  //    int lastSum = 0;
  //    for (int i = val.length - 1; (i >= 0) && (lastSum == 0);
  //      i--) {
  //      lastSum = (val[i] += 1);
  //    }
  //    if (lastSum == 0) {
  //      val = new int[val.length + 1];
  //      val[0] = 1;
  //    }
  //    return val;
  //  }

  //--------------------------------------------------------------
  // Bitwise Operations
  //--------------------------------------------------------------

  //  /**
  //   * Returns a Natural whose value is {@code (this & val)}.
  //   * (This
  //   * method returns a negative Natural if and only if this and
  //   * val are
  //   * both negative.)
  //   *
  //   * @param val
  //   *          value to be AND'ed with this Natural.
  //   * @return {@code this & val}
  //   */
  //  public Natural and (final Natural val) {
  //    final int[] result =
  //      new int[Math.max(intLength(),val.intLength())];
  //    for (int i = 0; i < result.length; i++) {
  //      result[i] =
  //        (getInt(result.length - i - 1)
  //          & val.getInt(result.length - i - 1));
  //    }
  //
  //    return valueOf(result);
  //  }
  //
  //  /**
  //   * Returns a Natural whose value is {@code (this | val)}.
  //   * (This method
  //   * returns a negative Natural if and only if either this or
  //   * val is
  //   * negative.)
  //   *
  //   * @param val
  //   *          value to be OR'ed with this Natural.
  //   * @return {@code this | val}
  //   */
  //  public Natural or (final Natural val) {
  //    final int[] result =
  //      new int[Math.max(intLength(),val.intLength())];
  //    for (int i = 0; i < result.length; i++) {
  //      result[i] =
  //        (getInt(result.length - i - 1)
  //          | val.getInt(result.length - i - 1));
  //    }
  //
  //    return valueOf(result);
  //  }
  //
  //  /**
  //   * Returns a Natural whose value is {@code (this ^ val)}.
  //   * (This method
  //   * returns a negative Natural if and only if exactly one of
  //   * this and
  //   * val are negative.)
  //   *
  //   * @param val
  //   *          value to be XOR'ed with this Natural.
  //   * @return {@code this ^ val}
  //   */
  //  public Natural xor (final Natural val) {
  //    final int[] result =
  //      new int[Math.max(intLength(),val.intLength())];
  //    for (int i = 0; i < result.length; i++) {
  //      result[i] =
  //        (getInt(result.length - i - 1)
  //          ^ val.getInt(result.length - i - 1));
  //    }
  //
  //    return valueOf(result);
  //  }
  //
  //  /**
  //   * Returns a Natural whose value is {@code (~this)}. (This
  //   * method
  //   * returns a negative value if and only if this Natural is
  //   * non-negative.)
  //   *
  //   * @return {@code ~this}
  //   */
  //  public Natural not () {
  //    final int[] result = new int[intLength()];
  //    for (int i = 0; i < result.length; i++) {
  //      result[i] = ~getInt(result.length - i - 1);
  //    }
  //
  //    return valueOf(result);
  //  }
  //
  //  /**
  //   * Returns a Natural whose value is {@code (this & ~val)}.
  //   * This
  //   * method, which is equivalent to {@code and(val.not())}, is
  //   * provided as
  //   * a convenience for masking operations. (This method returns a
  //   * negative
  //   * Natural if and only if {@code this} is negative and
  //   * {@code val} is
  //   * positive.)
  //   *
  //   * @param val
  //   *          value to be complemented and AND'ed with this
  //   *          Natural.
  //   * @return {@code this & ~val}
  //   */
  //  public Natural andNot (final Natural val) {
  //    final int[] result =
  //      new int[Math.max(intLength(),val.intLength())];
  //    for (int i = 0; i < result.length; i++) {
  //      result[i] =
  //        (getInt(result.length - i - 1)
  //          & ~val.getInt(result.length - i - 1));
  //    }
  //
  //    return valueOf(result);
  //  }

  //--------------------------------------------------------------
  // Single Bit Operations
  //--------------------------------------------------------------
  // TODO: check that high int is non-negative
  /**
   * Returns {@code true} if and only if the designated bit is
   * set.
   * (Computes {@code ((this & (1<<n)) != 0)}.)
   *
   * @param n
   *          index of bit to test.
   * @return {@code true} if and only if the designated bit is
   *         set.
   * @throws ArithmeticException
   *           {@code n} is negative.
   */
  public final boolean testBit (final int n) {
    if (n < 0) {
      throw new ArithmeticException("Negative bit address"); }
    return (getInt(n >>> 5) & (1 << (n & 31))) != 0;
  }

  /**
   * Returns a Natural whose value is equivalent to this
   * Natural
   * with the designated bit set. (Computes
   * {@code (this | (1<<n))}.)
   *
   * @param n
   *          index of bit to set.
   * @return {@code this | (1<<n)}
   * @throws ArithmeticException
   *           {@code n} is negative.
   */
  public final UnNatural0 setBit (final int n) {
    if (n < 0) {
      throw new ArithmeticException("Negative bit address"); }

    final int intNum = n >>> 5;
    final int[] result = new int[Math.max(intLength(),intNum + 2)];

    for (int i = 0; i < result.length; i++) {
      result[result.length - i - 1] = getInt(i); }
    result[result.length - intNum - 1] |= (1 << (n & 31));
    return make(result); }

  /**
   * Returns a Natural whose value is equivalent to this
   * Natural
   * with the designated bit cleared.
   * (Computes {@code (this & ~(1<<n))}.)
   *
   * @param n
   *          index of bit to clear.
   * @return {@code this & ~(1<<n)}
   * @throws ArithmeticException
   *           {@code n} is negative.
   */
  public UnNatural0 clearBit (final int n) {
    if (n < 0) {
      throw new ArithmeticException("Negative bit address"); }

    final int intNum = n >>> 5;
    final int[] result =
      new int[Math.max(intLength(),((n + 1) >>> 5) + 1)];

    for (int i = 0; i < result.length; i++) {
      result[result.length - i - 1] = getInt(i); }

    result[result.length - intNum - 1] &= ~(1 << (n & 31));
    return make(result); }

  /**
   * Returns a Natural whose value is equivalent to this
   * Natural
   * with the designated bit flipped.
   * (Computes {@code (this ^ (1<<n))}.)
   *
   * @param n
   *          index of bit to flip.
   * @return {@code this ^ (1<<n)}
   * @throws ArithmeticException
   *           {@code n} is negative.
   */
  public UnNatural0 flipBit (final int n) {
    if (n < 0) {
      throw new ArithmeticException("Negative bit address"); }
    final int intNum = n >>> 5;
    final int[] result =
      new int[Math.max(intLength(),intNum + 2)];
    for (int i = 0; i < result.length; i++) {
      result[result.length - i - 1] = getInt(i); }
    result[result.length - intNum - 1] ^= (1 << (n & 31));
    return make(result); }

  /**
   * Returns the index of the rightmost (lowest-order) one bit in
   * this
   * Natural (the number of zero bits to the right of the
   * rightmost
   * one bit). Returns -1 if this Natural contains no one bits.
   * (Computes {@code (this == 0? -1 : log2(this & -this))}.)
   *
   * @return index of the rightmost one bit in this Natural.
   */
  public int getLowestSetBit () {
    int lsb = lowestSetBitPlusTwo - 2;
    if (lsb == -2) {  // lowestSetBit not initialized yet
      lsb = 0;
      if (isZero()) {
        lsb -= 1;
      }
      else {
        // Search for lowest order nonzero int
        int i, b;
        for (i = 0; (b = getInt(i)) == 0; i++) {

        }
        lsb += (i << 5) + Integer.numberOfTrailingZeros(b);
      }
      lowestSetBitPlusTwo = lsb + 2;
    }
    return lsb;
  }

  //--------------------------------------------------------------
  // Miscellaneous Bit Operations
  //--------------------------------------------------------------

  /**
   * Returns the number of bits in the minimal two's-complement
   * representation of this Natural, <em>excluding</em> a sign
   * bit.
   * For positive Naturals, this is equivalent to the number of
   * bits in
   * the ordinary binary representation. For zero this method
   * returns
   * {@code 0}. (Computes
   * {@code (ceil(log2(this < 0 ? -this : this+1)))}.)
   *
   * @return number of bits in the minimal two's-complement
   *         representation of this Natural, <em>excluding</em>
   *         a sign bit.
   */
  public final int bitLength () {
    int n = bitLengthPlusOne - 1;
    if (n == -1) { // bitLength not initialized yet
      final int[] m = _mag;
      final int len = m.length;
      if (len == 0) { // offset by one to initialize
        n = 0; }
      else {
        // Calculate the bit length of the magnitude
        final int magBitLength =
          ((len - 1) << 5) + Numbers.bitLength(_mag[0]);
        n = magBitLength; }
      bitLengthPlusOne = n + 1; }
    return n; }

  /**
   * Returns the number of bits in the two's complement
   * representation
   * of this Natural that differ from its sign bit. This method
   * is
   * useful when implementing bit-vector style sets atop
   * Naturals.
   *
   * @return number of bits in the two's complement representation
   *         of this Natural that differ from its sign bit.
   */
  public final int bitCount () {
    int bc = bitCountPlusOne - 1;
    if (bc == -1) {  // bitCount not initialized yet
      bc = 0;      // offset by one to initialize
      // Count the bits in the magnitude
      for (final int element : _mag) {
        bc += Integer.bitCount(element); }
      bitCountPlusOne = bc + 1; }
    return bc; }

  //--------------------------------------------------------------
  // Comparable interface+
  //--------------------------------------------------------------

  @Override
  public final int compareTo (final UnNatural0 val) {
    return compareMagnitude(_mag,val._mag); }

  public final int compareTo (final long val) {
    if (val < 0L) { return -1; }
    if (val == 0L) {
      if (isZero()) { return 0; }
      return -1; }
    if (isZero()) { return -1; }
    return compareMagnitude(_mag,val); }

  public final int compareTo (final long val,
                              final int leftShift) {
    assert 0L <= val;
    return compareMagnitude(_mag,shiftLeft(val,leftShift)); }

  //--------------------------------------------------------------

  public final UnNatural0 min (final UnNatural0 val) {
    return (compareTo(val) < 0 ? this : val); }

  public final UnNatural0 max (final UnNatural0 val) {
    return (compareTo(val) > 0 ? this : val); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public int hashCode () {
    int hashCode = 0;
    for (final int element : _mag) {
      hashCode = (int) ((31 * hashCode) + unsigned(element)); }
    return hashCode; }

  @Override
  public boolean equals (final Object x) {
    if (x == this) { return true; }
    if (!(x instanceof UnNatural0)) { return false; }
    final UnNatural0 xInt = (UnNatural0) x;
    final int[] m = _mag;
    final int len = m.length;
    final int[] xm = xInt._mag;
    if (len != xm.length) { return false; }
    for (int i = 0; i < len; i++) {
      if (xm[i] != m[i]) { return false; } }
    return true; }


  /** hex string. */
  @Override
  public String toString () { return toString(0x10); }

  /** hex string. */
  @Override
  public String toString (final int radix) {
    assert radix == 0x10;
    return Bei.toHexString(_mag); }

  //--------------------------------------------------------------
  // Number interface+
  //--------------------------------------------------------------
  /** Returns a byte array containing the two's-complement
   * representation of this Natural. The byte array will be in
   * <i>big-endian</i> byte-order: the most significant byte is in
   * the zeroth element. The array will contain the minimum number
   * of bytes required to represent this Natural, including at
   * least one sign bit, which is {@code (ceil((this.bitLength() +
   * 1)/8))}. (This representation is compatible with the
   * {@link #Natural(byte[]) (byte[])} constructor.)
   *
   * @return a byte array containing the two's-complement
   *         representation of
   *         this Natural.
   * @see #Natural(byte[])
   */
  public final byte[] toByteArray () {
    final int byteLen = (bitLength() / 8) + 1;
    final byte[] byteArray = new byte[byteLen];
    for (
      int i = byteLen - 1,
      bytesCopied = 4,
      nextInt = 0,
      intIndex = 0;
      i >= 0;
      i--) {
      if (bytesCopied == 4) {
        nextInt = getInt(intIndex++);
        bytesCopied = 1; }
      else {
        nextInt >>>= 8; bytesCopied++; }
      byteArray[i] = (byte) nextInt; }
    return byteArray; }

  public final BigInteger bigIntegerValue () {
    return new BigInteger(toByteArray()); }

  @Override
  public final int intValue () { 
    assert 1 >= _mag.length;
    if (isZero()) { return 0; }
    return getInt(0); }

  @Override
  public final long longValue () {
    assert 2 >= _mag.length;
    if (isZero()) { return 0L; }
    long result = 0;
    for (int i = 1; i >= 0; i--) {
      result = (result << 32) + unsigned(getInt(i)); }
    return result; }

  //--------------------------------------------------------------

  @Override
  public final float floatValue () {
    if (isZero()) { return 0.0f; }

    final int exponent =
      (((_mag.length - 1) << 5) + Numbers.bitLength(_mag[0])) - 1;

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
    // abs().shiftRight(shift).intValue()
    // We do the shift into an int directly to improve
    // performance.

    final int nBits = shift & 0x1f;
    final int nBits2 = 32 - nBits;

    if (nBits == 0) { twiceSignifFloor = _mag[0]; }
    else {
      twiceSignifFloor = _mag[0] >>> nBits;
      if (twiceSignifFloor == 0) {
        twiceSignifFloor =
          (_mag[0] << nBits2) | (_mag[1] >>> nBits); } }

    int signifFloor = twiceSignifFloor >> 1;
        signifFloor &= Floats.STORED_SIGNIFICAND_MASK; // remove the
        // implied bit
        // We round up if either the fractional part of signif is
        // strictly
        // greater than 0.5 (which is true if the 0.5 bit is set and
        // any lower
        // bit is set), or if the fractional part of signif is >= 0.5
        // and
        // signifFloor is odd (which is true if both the 0.5 bit and
        // the 1 bit
        // are set). This is equivalent to the desired HALF_EVEN
        // rounding.
        final boolean increment =
          ((twiceSignifFloor
            & 1) != 0) && (((signifFloor & 1) != 0)
              || (getLowestSetBit() < shift));
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
        return Float.intBitsToFloat(bits);
  }

  /**
   * Converts this Natural to a {@code double}. This
   * conversion is similar to the
   * <i>narrowing primitive conversion</i> from {@code double} to
   * {@code float} as defined in
   * <cite>The Java&trade; Language Specification</cite>:
   * if this Natural has too great a magnitude
   * to represent as a {@code double}, it will be converted to
   * {@link Double#NEGATIVE_INFINITY} or {@link
   * Double#POSITIVE_INFINITY} as appropriate. Note that even when
   * the return value is finite, this conversion can lose
   * information about the precision of the Natural value.
   *
   * @return this Natural converted to a {@code double}.
   * @jls 5.1.3 Narrowing Primitive Conversion
   */
  @Override
  public final double doubleValue () {
    if (isZero()) { return 0.0; }

    final int exponent =
      (((_mag.length - 1) << 5) + Numbers.bitLength(_mag[0])) - 1;

    // exponent == floor(log2(abs(this))Double)
    if (exponent < (Long.SIZE - 1)) {
      return longValue();
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
      highBits = _mag[0];
      lowBits = _mag[1];
    }
    else {
      highBits = _mag[0] >>> nBits;
      lowBits = (_mag[0] << nBits2) | (_mag[1] >>> nBits);
      if (highBits == 0) {
        highBits = lowBits;
        lowBits = (_mag[1] << nBits2) | (_mag[2] >>> nBits);
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
          || (getLowestSetBit() < shift));
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
    return Double.longBitsToDouble(bits);
  }

  //--------------------------------------------------------------
  /* These two arrays are the integer analog of above.
   */
  private static int digitsPerInt[] =
  { 0, 0, 30, 19, 15, 13, 11, 11, 10, 9, 9, 8, 8, 8, 8, 7, 7, 7,
    7, 7, 7, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 5 };

  private static int intRadix[] =
  { 0, 0, 0x40000000, 0x4546b3db, 0x40000000, 0x48c27395,
    0x159fd800, 0x75db9c97, 0x40000000, 0x17179149, 0x3b9aca00,
    0xcc6db61, 0x19a10000, 0x309f1021, 0x57f6c100, 0xa2f1b6f,
    0x10000000, 0x18754571, 0x247dbc80, 0x3547667b, 0x4c4b4000,
    0x6b5a6e1d, 0x6c20a40, 0x8d2d931, 0xb640000, 0xe8d4a51,
    0x1269ae40, 0x17179149, 0x1cb91000, 0x23744899, 0x2b73a840,
    0x34e63b41, 0x40000000, 0x4cfa3cc1, 0x5c13d840, 0x6d91b519,
    0x39aa400 };

  /**
   * Returns the length of the two's complement representation in
   * ints,
   * including space for at least one sign bit.
   */
  private final int intLength () { return (bitLength() >>> 5) + 1; }

  /**
   * Returns the specified int of the little-endian two's
   * complement
   * representation (int 0 is the least significant). The int
   * number can
   * be arbitrarily high (values are logically preceded by
   * infinitely many
   * sign ints).
   */
  private final int getInt (final int n) {
    if (n < 0) { return 0; }
    if (n >= _mag.length) { return 0; }
    final int magInt = _mag[_mag.length - n - 1];
    return magInt; }

  //-------------------------------------------------------------
  // construction
  //-------------------------------------------------------------

  private static final void reportOverflow () {
    throw new ArithmeticException(
      "Natural would overflow supported range"); }

  private static final void checkMagnitude (final int[] m) {
    assert m.length < MAX_MAG_LENGTH; }

  //-------------------------------------------------------------

  private UnNatural0 (final int[] mag) {
    if ((null != mag) && (0 < mag.length)) { assert 0 != mag[0]; }
    _mag = mag; }

  private static final UnNatural0 make (final int[] m) {
    final int[] m1 = unsafeStripLeadingZeroInts(m);
    checkMagnitude(m1);
    return new UnNatural0(m1); }

  // TODO: a little faster to use a safe stripLeadingZeros
  public static final UnNatural0 valueOf (final int[] m) {
    return make(Arrays.copyOf(m,m.length)); }

  public static final UnNatural0 valueOf (final byte[] b,
                                          final int off,
                                          final int len) {
    return make(stripLeadingZeroBytes(b,off,len)); }

  public static final UnNatural0 valueOf (final byte[] b) {
    return valueOf(b,0,b.length); }

  public static final UnNatural0 valueOf (final BigInteger bi) {
    return valueOf(bi.toByteArray()); }

  //-------------------------------------------------------------
  // string parsing
  //-------------------------------------------------------------
  // bitsPerDigit in the given radix times 1024
  // Rounded up to avoid under-allocation.

  private static long bitsPerDigit[] =
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

  public static final UnNatural0 valueOf (final String s,
                                          final int radix) {
    //Debug.println("UnNatural0");
    //Debug.println("s=" + s);
    //Debug.println("radix=" + radix);
    final int len = s.length();
    //Debug.println("len=" + len);
    assert 0 < len;
    assert Character.MIN_RADIX <= radix;
    assert radix <= Character.MAX_RADIX;
    assert 0 > s.indexOf('-');
    assert 0 > s.indexOf('+');

    int cursor = 0;
    while ((cursor<len)
      && (Character.digit(s.charAt(cursor),radix) == 0)) {
      cursor++; }
    if (cursor==len) { return ZERO; }

    final int numDigits = len-cursor;
    //Debug.println("numDigits=" + numDigits);

    // might be bigger than needed, but make(int[]) handles that
    final long numBits = ((numDigits*bitsPerDigit[radix])>>>10)+1;
    //Debug.println("numBits=" + numBits);
    if ((numBits + 31) >= (1L << 32)) { reportOverflow(); }
    final int numWords = (int) (numBits + 31) >>> 5;
    //Debug.println("numWords=" + numWords);
    final int[] magnitude = new int[numWords];

    // first (potentially short) digit group
    int firstGroupLen = numDigits % digitsPerInt[radix];
    if (firstGroupLen == 0) { firstGroupLen = digitsPerInt[radix]; }
    //Debug.println("firstGroupLen=" + firstGroupLen);
    String group = s.substring(cursor,cursor += firstGroupLen);
    magnitude[numWords-1] = Integer.parseInt(group,radix);
    assert 0<=magnitude[numWords-1] : "Illegal digit";

    final int superRadix = intRadix[radix];
    int groupVal = 0;
    while (cursor<len) {
      group = s.substring(cursor,cursor+=digitsPerInt[radix]);
      groupVal = Integer.parseInt(group,radix);
      assert 0<=groupVal : "Illegal digit";
      destructiveMulAdd(magnitude,superRadix,groupVal); }

    return make(magnitude); }

  public static final UnNatural0 valueOf (final String s) {
    return valueOf(s,0x10); }

  //--------------------------------------------------------------
  // cached values
  //--------------------------------------------------------------

  private static final int MAX_CONSTANT = 16;
  private static final UnNatural0 posConst[] =
    new UnNatural0[MAX_CONSTANT+1];

  /** The cache of powers of each radix. This allows us to not
   * have to recalculate powers of radix^(2^n) more than once.
   * This speeds Schoenhage recursive base conversion
   * significantly.
   */
  private static volatile UnNatural0[][] powerCache;

  /** The cache of logarithms of radices for base conversion. */
  private static final double[] logCache;

  //private static final double LOG_TWO = Math.log(2.0);

  static {
    for (int i = 1; i <= MAX_CONSTANT; i++) {
      final int[] magnitude = new int[1];
      magnitude[0] = i;
      posConst[i] = make(magnitude); }
    // Initialize the cache of radix^(2^x) values used for base
    // conversion with just the very first value. Additional
    // values will be created on demand.
    powerCache = new UnNatural0[Character.MAX_RADIX + 1][];
    logCache = new double[Character.MAX_RADIX + 1];
    for (
      int i = Character.MIN_RADIX;
      i <= Character.MAX_RADIX;
      i++) {
      powerCache[i] = new UnNatural0[] { UnNatural0.valueOf(i) };
      logCache[i] = Math.log(i); } }

  public static final UnNatural0 ZERO = new UnNatural0(new int[0]);
  public static final UnNatural0 ONE = valueOf(1);
  public static final UnNatural0 TWO = valueOf(2);
  public static final UnNatural0 TEN = valueOf(10);

  //--------------------------------------------------------------

  public static final UnNatural0 valueOf (final long val) {
    assert 0 <= val;

    if (val == 0) { return ZERO; }
    if ((val > 0) && (val <= MAX_CONSTANT)) {
      return posConst[(int) val]; }

    // make(int[]) will strip a leading zero.
    final int[] m = { (int) (val >>> 32), (int) val, };
    return make(m); }

  //--------------------------------------------------------------

  public static final UnNatural0 valueOf (final long x,
                                          final int leftShift) {
    if (0L == x) { return ZERO; }
    assert 0L < x;
    return make(shiftLeft(x,leftShift)); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

