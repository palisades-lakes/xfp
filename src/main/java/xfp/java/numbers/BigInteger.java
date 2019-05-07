package xfp.java.numbers;

import static xfp.java.numbers.Numbers.UNSIGNED_MASK;

import java.io.ObjectStreamField;
import java.util.Arrays;
import java.util.Objects;

/** immutable arbitrary-precision integers for arbitrary precision 
 * floats.
 * 
 * TODO: convert to purely non-negative numbers.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-05-06
 */

@SuppressWarnings("hiding")
public final class BigInteger extends Number
implements Comparable<BigInteger> {

  final int signum;
  final int[] mag;

  // The following fields are stable variables. A stable 
  // variable's value changes at most once from the default zero 
  // value to a non-zero stable value. A stable value is 
  // calculated lazily on demand.

  private int bitCountPlusOne;
  private int bitLengthPlusOne;
  private int lowestSetBitPlusTwo;
  private int firstNonzeroIntNumPlusTwo;

  /** This constant limits {@code mag.length} of BigIntegers to 
   * the supported range.
   */
  private static final int MAX_MAG_LENGTH =
    (Integer.MAX_VALUE / Integer.SIZE) + 1; // (1 << 26)

  //-------------------------------------------------------------
  // construction
  //-------------------------------------------------------------

  /**
   * Translates a byte sub-array containing the two's-complement
   * binary
   * representation of a BigInteger into a BigInteger. The
   * sub-array is
   * specified via an offset into the array and a length. The
   * sub-array is
   * assumed to be in <i>big-endian</i> byte-order: the most
   * significant
   * byte is the element at index {@code off}. The {@code val}
   * array is
   * assumed to be unchanged for the duration of the constructor
   * call.
   *
   * An {@code IndexOutOfBoundsException} is thrown if the length
   * of the array
   * {@code val} is non-zero and either {@code off} is negative,
   * {@code len}
   * is negative, or {@code off+len} is greater than the length of
   * {@code val}.
   *
   * @param val
   *          byte array containing a sub-array which is the
   *          big-endian
   *          two's-complement binary representation of a
   *          BigInteger.
   * @param off
   *          the start offset of the binary representation.
   * @param len
   *          the number of bytes to use.
   * @throws NumberFormatException
   *           {@code val} is zero bytes long.
   * @throws IndexOutOfBoundsException
   *           if the provided array offset and
   *           length would cause an index into the byte array to
   *           be
   *           negative or greater than or equal to the array
   *           length.
   * @since 9
   */
  public BigInteger (final byte[] val, final int off,
                     final int len) {
    if (val.length == 0) {
      throw new NumberFormatException("Zero length BigInteger");
    }
    Objects.checkFromIndexSize(off,len,val.length);

    if (val[off] < 0) {
      mag = makePositive(val,off,len);
      signum = -1;
    }
    else {
      mag = stripLeadingZeroBytes(val,off,len);
      signum = (mag.length == 0 ? 0 : 1);
    }
    if (mag.length >= MAX_MAG_LENGTH) {
      checkRange();
    }
  }

  /**
   * Translates a byte array containing the two's-complement
   * binary
   * representation of a BigInteger into a BigInteger. The input
   * array is
   * assumed to be in <i>big-endian</i> byte-order: the most
   * significant
   * byte is in the zeroth element. The {@code val} array is
   * assumed to be
   * unchanged for the duration of the constructor call.
   *
   * @param val
   *          big-endian two's-complement binary representation of
   *          a
   *          BigInteger.
   * @throws NumberFormatException
   *           {@code val} is zero bytes long.
   */
  public BigInteger (final byte[] val) {
    this(val,0,val.length);
  }

  /** This private constructor translates an int array containing
   * the
   * two's-complement binary representation of a BigInteger into a
   * BigInteger. The input array is assumed to be in
   * <i>big-endian</i>
   * int-order: the most significant int is in the zeroth element.
   * The
   * {@code val} array is assumed to be unchanged for the duration
   * of
   * the constructor call.
   */

  public BigInteger (final int[] val) {
    if (val.length == 0) {
      throw new NumberFormatException("empty BigInteger"); }
    if (val[0] < 0) {
      mag = makePositive(val);
      signum = -1; }
    else {
      mag = trustedStripLeadingZeroInts(val);
      signum = (mag.length == 0 ? 0 : 1); }
    if (mag.length >= MAX_MAG_LENGTH) { checkRange(); } }

  //  /**
  //   * Translates the sign-magnitude representation of a BigInteger
  //   * into a
  //   * BigInteger. The sign is represented as an integer signum
  //   * value: -1 for
  //   * negative, 0 for zero, or 1 for positive. The magnitude is a
  //   * sub-array of
  //   * a byte array in <i>big-endian</i> byte-order: the most
  //   * significant byte
  //   * is the element at index {@code off}. A zero value of the
  //   * length
  //   * {@code len} is permissible, and will result in a BigInteger
  //   * value of 0,
  //   * whether signum is -1, 0 or 1. The {@code magnitude} array is
  //   * assumed to
  //   * be unchanged for the duration of the constructor call.
  //   *
  //   * An {@code IndexOutOfBoundsException} is thrown if the length
  //   * of the array
  //   * {@code magnitude} is non-zero and either {@code off} is
  //   * negative,
  //   * {@code len} is negative, or {@code off+len} is greater than
  //   * the length of
  //   * {@code magnitude}.
  //   *
  //   * @param signum
  //   *          signum of the number (-1 for negative, 0 for zero, 1
  //   *          for positive).
  //   * @param magnitude
  //   *          big-endian binary representation of the magnitude of
  //   *          the number.
  //   * @param off
  //   *          the start offset of the binary representation.
  //   * @param len
  //   *          the number of bytes to use.
  //   * @throws NumberFormatException
  //   *           {@code signum} is not one of the three
  //   *           legal values (-1, 0, and 1), or {@code signum} is 0
  //   *           and
  //   *           {@code magnitude} contains one or more non-zero
  //   *           bytes.
  //   * @throws IndexOutOfBoundsException
  //   *           if the provided array offset and
  //   *           length would cause an index into the byte array to
  //   *           be
  //   *           negative or greater than or equal to the array
  //   *           length.
  //   * @since 9
  //   */
  //  public BigInteger (final int signum, 
  //                     final byte[] magnitude,
  //                     final int off, 
  //                     final int len) {
  //    if ((signum < -1) || (signum > 1)) {
  //      throw (new NumberFormatException("Invalid signum value"));
  //    }
  //    Objects.checkFromIndexSize(off,len,magnitude.length);
  //
  //    // stripLeadingZeroBytes() returns a zero length array if len
  //    // == 0
  //    this.mag = stripLeadingZeroBytes(magnitude,off,len);
  //
  //    if (this.mag.length == 0) {
  //      this.signum = 0;
  //    }
  //    else {
  //      if (signum == 0) {
  //        throw (new NumberFormatException(
  //          "signum-magnitude mismatch"));
  //      }
  //      this.signum = signum;
  //    }
  //    if (mag.length >= MAX_MAG_LENGTH) {
  //      checkRange();
  //    }
  //  }

  //  /**
  //   * Translates the sign-magnitude representation of a BigInteger
  //   * into a
  //   * BigInteger. The sign is represented as an integer signum
  //   * value: -1 for
  //   * negative, 0 for zero, or 1 for positive. The magnitude is a
  //   * byte array
  //   * in <i>big-endian</i> byte-order: the most significant byte is
  //   * the
  //   * zeroth element. A zero-length magnitude array is permissible,
  //   * and will
  //   * result in a BigInteger value of 0, whether signum is -1, 0 or
  //   * 1. The
  //   * {@code magnitude} array is assumed to be unchanged for the
  //   * duration of
  //   * the constructor call.
  //   *
  //   * @param signum
  //   *          signum of the number (-1 for negative, 0 for zero, 1
  //   *          for positive).
  //   * @param magnitude
  //   *          big-endian binary representation of the magnitude of
  //   *          the number.
  //   * @throws NumberFormatException
  //   *           {@code signum} is not one of the three
  //   *           legal values (-1, 0, and 1), or {@code signum} is 0
  //   *           and
  //   *           {@code magnitude} contains one or more non-zero
  //   *           bytes.
  //   */
  //  public BigInteger (final int signum, final byte[] magnitude) {
  //    this(signum,magnitude,0,magnitude.length);
  //  }

  // /**
  // * A constructor for internal use that translates the
  // sign-magnitude
  // * representation of a BigInteger into a BigInteger. It checks
  // the
  // * arguments and copies the magnitude so this constructor
  // would be
  // * safe for external use. The {@code magnitude} array is
  // assumed to be
  // * unchanged for the duration of the constructor call.
  // */
  // private BigInteger(final int signum, final int[] magnitude) {
  // this.mag = stripLeadingZeroInts(magnitude);
  //
  // if ((signum < -1) || (signum > 1)) {
  // throw(new NumberFormatException("Invalid signum value"));
  // }
  //
  // if (this.mag.length == 0) {
  // this.signum = 0;
  // } else {
  // if (signum == 0) {
  // throw(new NumberFormatException("signum-magnitude
  // mismatch"));
  // }
  // this.signum = signum;
  // }
  // if (mag.length >= MAX_MAG_LENGTH) {
  // checkRange();
  // }
  // }

  /**
   * Translates the String representation of a BigInteger in the
   * specified radix into a BigInteger. The String representation
   * consists of an optional minus or plus sign followed by a
   * sequence of one or more digits in the specified radix. The
   * character-to-digit mapping is provided by {@code
   * Character.digit}. The String may not contain any extraneous
   * characters (whitespace, for example).
   *
   * @param val
   *          String representation of BigInteger.
   * @param radix
   *          radix to be used in interpreting {@code val}.
   * @throws NumberFormatException
   *           {@code val} is not a valid representation
   *           of a BigInteger in the specified radix, or
   *           {@code radix} is
   *           outside the range from {@link Character#MIN_RADIX}
   *           to
   *           {@link Character#MAX_RADIX}, inclusive.
   * @see Character#digit
   */
  public BigInteger (final String val, final int radix) {
    int cursor = 0, numDigits;
    final int len = val.length();

    if ((radix < Character.MIN_RADIX)
      || (radix > Character.MAX_RADIX)) {
      throw new NumberFormatException("Radix out of range");
    }
    if (len == 0) {
      throw new NumberFormatException("Zero length BigInteger");
    }

    // Check for at most one leading sign
    int sign = 1;
    final int index1 = val.lastIndexOf('-');
    final int index2 = val.lastIndexOf('+');
    if (index1 >= 0) {
      if ((index1 != 0) || (index2 >= 0)) {
        throw new NumberFormatException(
          "Illegal embedded sign character");
      }
      sign = -1;
      cursor = 1;
    }
    else if (index2 >= 0) {
      if (index2 != 0) {
        throw new NumberFormatException(
          "Illegal embedded sign character");
      }
      cursor = 1;
    }
    if (cursor == len) {
      throw new NumberFormatException("Zero length BigInteger");
    }

    // Skip leading zeros and compute number of digits in
    // magnitude
    while ((cursor < len)
      && (Character.digit(val.charAt(cursor),radix) == 0)) {
      cursor++;
    }

    if (cursor == len) {
      signum = 0;
      mag = ZERO.mag;
      return;
    }

    numDigits = len - cursor;
    signum = sign;

    // Pre-allocate array of expected size. May be too large but
    // can
    // never be too small. Typically exact.
    final long numBits =
      ((numDigits * bitsPerDigit[radix]) >>> 10) + 1;
    if ((numBits + 31) >= (1L << 32)) {
      reportOverflow();
    }
    final int numWords = (int) (numBits + 31) >>> 5;
    final int[] magnitude = new int[numWords];

    // Process first (potentially short) digit group
    int firstGroupLen = numDigits % digitsPerInt[radix];
    if (firstGroupLen == 0) {
      firstGroupLen = digitsPerInt[radix];
    }
    String group = val.substring(cursor,cursor += firstGroupLen);
    magnitude[numWords - 1] = Integer.parseInt(group,radix);
    if (magnitude[numWords - 1] < 0) {
      throw new NumberFormatException("Illegal digit");
    }

    // Process remaining digit groups
    final int superRadix = intRadix[radix];
    int groupVal = 0;
    while (cursor < len) {
      group = val.substring(cursor,cursor += digitsPerInt[radix]);
      groupVal = Integer.parseInt(group,radix);
      if (groupVal < 0) {
        throw new NumberFormatException("Illegal digit");
      }
      destructiveMulAdd(magnitude,superRadix,groupVal);
    }
    // Required for cases where the array was over allocated.
    mag = trustedStripLeadingZeroInts(magnitude);
    if (mag.length >= MAX_MAG_LENGTH) {
      checkRange();
    }
  }

  // bitsPerDigit in the given radix times 1024
  // Rounded up to avoid underallocation.
  private static long bitsPerDigit[] =
  { 0, 0, 1024, 1624, 2048, 2378, 2648, 2875, 3072, 3247, 3402,
    3543, 3672, 3790, 3899, 4001, 4096, 4186, 4271, 4350, 4426,
    4498, 4567, 4633, 4696, 4756, 4814, 4870, 4923, 4975, 5025,
    5074, 5120, 5166, 5210, 5253, 5295 };

  // Multiply x array times word y in place, and add word z
  private static void destructiveMulAdd (final int[] x,
                                         final int y,
                                         final int z) {
    // Perform the multiplication word by word
    final long ylong = y & UNSIGNED_MASK;
    final long zlong = z & UNSIGNED_MASK;
    final int len = x.length;

    long product = 0;
    long carry = 0;
    for (int i = len - 1; i >= 0; i--) {
      product = (ylong * (x[i] & UNSIGNED_MASK)) + carry;
      x[i] = (int) product;
      carry = product >>> 32;
    }

    // Perform the addition
    long sum = (x[len - 1] & UNSIGNED_MASK) + zlong;
    x[len - 1] = (int) sum;
    carry = sum >>> 32;
    for (int i = len - 2; i >= 0; i--) {
      sum = (x[i] & UNSIGNED_MASK) + carry;
      x[i] = (int) sum;
      carry = sum >>> 32;
    }
  }

  //  /**
  //   * Translates the decimal String representation of a BigInteger
  //   * into a
  //   * BigInteger. The String representation consists of an optional
  //   * minus
  //   * sign followed by a sequence of one or more decimal digits.
  //   * The
  //   * character-to-digit mapping is provided by
  //   * {@code Character.digit}.
  //   * The String may not contain any extraneous characters
  //   * (whitespace, for
  //   * example).
  //   *
  //   * @param val
  //   *          decimal String representation of BigInteger.
  //   * @throws NumberFormatException
  //   *           {@code val} is not a valid representation
  //   *           of a BigInteger.
  //   * @see Character#digit
  //   */
  //  public BigInteger (final String val) {
  //    this(val,10);
  //  }

  public BigInteger (final int[] magnitude, final int signum) {
    this.signum = (magnitude.length == 0 ? 0 : signum);
    this.mag = magnitude;
    if (mag.length >= MAX_MAG_LENGTH) { checkRange(); } }

  // /**
  // * This private constructor is for internal use and assumes
  // that its
  // * arguments are correct. The {@code magnitude} array is
  // assumed to be
  // * unchanged for the duration of the constructor call.
  // */
  // private BigInteger(final byte[] magnitude, final int signum)
  // {
  // this.signum = (magnitude.length == 0 ? 0 : signum);
  // this.mag = stripLeadingZeroBytes(magnitude, 0,
  // magnitude.length);
  // if (mag.length >= MAX_MAG_LENGTH) {
  // checkRange();
  // }
  // }

  /**
   * Throws an {@code ArithmeticException} if the
   * {@code BigInteger} would be
   * out of the supported range.
   *
   * @throws ArithmeticException
   *           if {@code this} exceeds the supported range.
   */
  private void checkRange () {
    if ((mag.length > MAX_MAG_LENGTH)
      || ((mag.length == MAX_MAG_LENGTH) && (mag[0] < 0))) {
      reportOverflow();
    }
  }

  private static final void reportOverflow () {
    throw new ArithmeticException(
      "BigInteger would overflow supported range"); }

  public static final BigInteger valueOf (final long val) {
    // If -MAX_CONSTANT < val < MAX_CONSTANT, return stashed
    // constant
    if (val == 0) { return ZERO; }
    if ((val > 0) && (val <= MAX_CONSTANT)) {
      return posConst[(int) val]; }
    else if ((val < 0) && (val >= -MAX_CONSTANT)) {
      return negConst[(int) -val]; }
    return new BigInteger(val); }

  private static final int[] shiftLeft (final long x,
                                        final int leftShift) {
    //    final int s0 = leftShift / 32;
    //    final int s1 = leftShift % 32;
    //    final int n = s0 + 2 + ((s1 > 0) ? 1 : 0);
    //    final int[] x = new int[n];
    final int[] xs = { (int) (x >>> 32), 
                       (int) (x & 0xFFFFFFFFL), };
    return shiftLeft(xs,leftShift); }

  public static final BigInteger nonNegative (final long x,
                                              final int leftShift) {
    return new BigInteger(shiftLeft(x,leftShift),1); }

  /** Constructs a BigInteger with the specified value, which may
   * not be zero.
   */
  private BigInteger (long val) {
    if (val < 0) { val = -val; signum = -1; }
    else { signum = 1; }
    final int highWord = (int) (val >>> 32);
    if (highWord == 0) { 
      mag = new int[1];
      mag[0] = (int) val; }
    else {
      mag = new int[2];
      mag[0] = highWord;
      mag[1] = (int) val; } }

  /**
   * Returns a BigInteger with the given two's complement
   * representation.
   * Assumes that the input array will not be modified (the
   * returned
   * BigInteger will reference the input array if feasible).
   */
  private static BigInteger valueOf (final int val[]) {
    return (val[0] > 0
      ? new BigInteger(val,1)
        : new BigInteger(val)); }

  private static final int MAX_CONSTANT = 16;
  private static BigInteger posConst[] = 
    new BigInteger[MAX_CONSTANT + 1];
  private static BigInteger negConst[] =
    new BigInteger[MAX_CONSTANT + 1];

  /** The cache of powers of each radix. This allows us to not 
   * have to recalculate powers of radix^(2^n) more than once. 
   * This speeds Schoenhage recursive base conversion 
   * significantly.
   */
  private static volatile BigInteger[][] powerCache;

  /** The cache of logarithms of radices for base conversion. */
  private static final double[] logCache;

  /** used in computing cache indices.
   */
  private static final double LOG_TWO = Math.log(2.0);

  static {
    for (int i = 1; i <= MAX_CONSTANT; i++) {
      final int[] magnitude = new int[1];
      magnitude[0] = i;
      posConst[i] = new BigInteger(magnitude,1);
      negConst[i] = new BigInteger(magnitude,-1); }
    // Initialize the cache of radix^(2^x) values used for base
    // conversion with just the very first value. Additional 
    // values will be created on demand.
    powerCache = new BigInteger[Character.MAX_RADIX + 1][];
    logCache = new double[Character.MAX_RADIX + 1];
    for (int i = Character.MIN_RADIX; i <= Character.MAX_RADIX;
      i++) {
      powerCache[i] = new BigInteger[] { BigInteger.valueOf(i) };
      logCache[i] = Math.log(i); } }

  public static final BigInteger ZERO = 
    new BigInteger(new int[0],0);
  public static final BigInteger ONE = valueOf(1);
  public static final BigInteger TWO = valueOf(2);
  private static final BigInteger NEGATIVE_ONE = valueOf(-1);
  public static final BigInteger TEN = valueOf(10);

  //--------------------------------------------------------------
  // add
  //--------------------------------------------------------------
  /** Adds the contents of the int array x and long value val. 
   * This method allocates a new int array to hold the answer. 
   * Assumes x.length &gt; 0 and val is non-negative
   */
  private static final int[] addMagnitude (final int[] x, 
                                           final long val) {
    assert val >= 0L; 

    long sum = 0;
    int xIndex = x.length;
    int[] result;

    final int highWord = (int) (val >>> 32);
    if (highWord == 0) {
      result = new int[xIndex];
      sum = (x[--xIndex] & UNSIGNED_MASK) + val;
      result[xIndex] = (int) sum; }
    else {
      if (xIndex == 1) {
        result = new int[2];
        sum = val + (x[0] & UNSIGNED_MASK);
        result[1] = (int) sum;
        result[0] = (int) (sum >>> 32);
        return result; }
      result = new int[xIndex];
      sum = (x[--xIndex] & UNSIGNED_MASK) + (val & UNSIGNED_MASK);
      result[xIndex] = (int) sum;
      sum =
        (x[--xIndex] & UNSIGNED_MASK) + (highWord & UNSIGNED_MASK)
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

  public final BigInteger add (final long val) {
    if (val == 0) { return this; }
    if (signum == 0) { return valueOf(val); }
    if (Long.signum(val) == signum) {
      return new BigInteger(addMagnitude(mag,Math.abs(val)),signum); }
    final int cmp = compareMagnitude(val);
    if (cmp == 0) { return ZERO; }
    int[] resultMag =
      (cmp > 0 ? subtract(mag,Math.abs(val))
        : subtract(Math.abs(val),mag));
    resultMag = trustedStripLeadingZeroInts(resultMag);
    return new BigInteger(resultMag,cmp == signum ? 1 : -1); }

  //--------------------------------------------------------------
  /** Adds the contents of the int arrays x and y. Allocate a new 
   * int array to hold the answer.
   */

  public static final int[] add (int[] x, int[] y) {
    // If x is shorter, swap the two arrays
    if (x.length < y.length) {
      final int[] tmp = x; x = y; y = tmp; }
    int xIndex = x.length;
    int yIndex = y.length;
    final int result[] = new int[xIndex];
    long sum = 0;
    if (yIndex == 1) {
      sum = (x[--xIndex] & UNSIGNED_MASK) + (y[0] & UNSIGNED_MASK);
      result[xIndex] = (int) sum; }
    else {
      // Add common parts of both numbers
      while (yIndex > 0) {
        sum =
          (x[--xIndex] & UNSIGNED_MASK) + (y[--yIndex] & UNSIGNED_MASK)
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

  public final BigInteger add (final BigInteger val) {
    if (val.signum == 0) { return this; }
    if (signum == 0) { return val; }
    if (val.signum == signum) {
      return new BigInteger(add(mag,val.mag),signum); }
    final int cmp = compareMagnitude(val);
    if (cmp == 0) { return ZERO; }
    int[] resultMag =
      (cmp > 0 ? subtract(mag,val.mag) : subtract(val.mag,mag));
    resultMag = trustedStripLeadingZeroInts(resultMag);
    return new BigInteger(resultMag,cmp == signum ? 1 : -1); }

  //--------------------------------------------------------------

  public final BigInteger addMagnitude (final long val,
                                        final int leftShift) {
    if (0 == signum) {
      return new BigInteger(shiftLeft(val,leftShift),1); }

    assert 0 < signum;

    return new BigInteger(add(mag,shiftLeft(val,leftShift)),1); }

  //--------------------------------------------------------------
  // subtract
  //--------------------------------------------------------------

  private static final int[] subtract (final long val,
                                       final int[] little) {
    final int highWord = (int) (val >>> 32);
    if (highWord == 0) {
      final int result[] = new int[1];
      result[0] = (int) (val - (little[0] & UNSIGNED_MASK));
      return result; }
    final int result[] = new int[2];
    if (little.length == 1) {
      final long difference =
        ((int) val & UNSIGNED_MASK)
        - (little[0] & UNSIGNED_MASK);
      result[1] = (int) difference;
      // Subtract remainder of longer number while borrow
      // propagates
      final boolean borrow = ((difference >> 32) != 0);
      if (borrow) { result[0] = highWord - 1; }
      // Copy remainder of longer number
      else { result[0] = highWord; }
      return result; }
    long difference =
      ((int) val & UNSIGNED_MASK)
      - (little[1] & UNSIGNED_MASK);
    result[1] = (int) difference;
    difference =
      ((highWord & UNSIGNED_MASK)
        - (little[0] & UNSIGNED_MASK))
      + (difference >> 32);
    result[0] = (int) difference;
    return result; }

  //--------------------------------------------------------------
  /** Subtracts the contents of the second int arrays (little) 
   * from the first (big). The first int array (big) must 
   * represent a larger number than the second. This method 
   * allocates the space necessary to hold the answer.
   */

  private static final int[] subtract (final int[] big,
                                       final int[] little) {
    assert compareMagnitude(little,big) <= 0;
    int bigIndex = big.length;
    final int result[] = new int[bigIndex];
    int littleIndex = little.length;
    long difference = 0;

    // Subtract common parts of both numbers
    while (littleIndex > 0) {
      difference =
        ((big[--bigIndex] & UNSIGNED_MASK)
          - (little[--littleIndex] & UNSIGNED_MASK))
        + (difference >> 32);
      result[bigIndex] = (int) difference; }

    // Subtract remainder of longer number while borrow propagates
    boolean borrow = ((difference >> 32) != 0);
    while ((bigIndex > 0) && borrow) {
      borrow = ((result[--bigIndex] = big[bigIndex] - 1) == -1); }

    // Copy remainder of longer number
    while (bigIndex > 0) {
      result[--bigIndex] = big[bigIndex]; }

    return trustedStripLeadingZeroInts(result); }

  public final BigInteger subtract (final BigInteger val) {
    if (val.signum == 0) { return this; }
    if (signum == 0) { return val.negate(); }
    if (val.signum != signum) {
      return new BigInteger(add(mag,val.mag),signum); }
    final int cmp = compareMagnitude(val);
    if (cmp == 0) { return ZERO; }
    int[] resultMag =
      (cmp > 0 ? subtract(mag,val.mag) : subtract(val.mag,mag));
    resultMag = trustedStripLeadingZeroInts(resultMag);
    return new BigInteger(resultMag,cmp == signum ? 1 : -1); }

  //--------------------------------------------------------------
  /** Subtracts the contents of the second argument (val) from the
   * first (big). The first int array (big) must represent a
   * larger number than the second. This method allocates the 
   * space necessary to hold the answer. assumes val &gt;= 0
   */

  private static final int[] subtract (final int[] big,
                                       final long val) {
    final int highWord = (int) (val >>> 32);
    int bigIndex = big.length;
    final int result[] = new int[bigIndex];
    long difference = 0;
    if (highWord == 0) {
      difference = (big[--bigIndex] & UNSIGNED_MASK) - val;
      result[bigIndex] = (int) difference; }
    else {
      difference =
        (big[--bigIndex] & UNSIGNED_MASK) 
        - (val & UNSIGNED_MASK);
      result[bigIndex] = (int) difference;
      difference =
        ((big[--bigIndex] & UNSIGNED_MASK)
          - (highWord & UNSIGNED_MASK))
        + (difference >> 32);
      result[bigIndex] = (int) difference; }
    // Subtract remainder of longer number while borrow propagates
    boolean borrow = ((difference >> 32) != 0);
    while ((bigIndex > 0) && borrow) {
      borrow = ((result[--bigIndex] = big[bigIndex] - 1) == -1); }
    // Copy remainder of longer number
    while (bigIndex > 0) { result[--bigIndex] = big[bigIndex]; }
    return result; }

  public final BigInteger subtract (final long val) {
    //    assert signum >=0;
    //    assert val >= 0L;
    final int c = compareTo(val);
    if (0 == c) { return ZERO; }
    //    assert c > 0;
    if (0L == val) { return this; }
    return new BigInteger(subtract(mag,val),1); }

  //--------------------------------------------------------------

  public final BigInteger subtract (final long val,
                                    final int leftShift) {
    assert 0 < signum;
    if (0L == val) { return this; }
    assert 0L < val;
    return 
      new BigInteger(subtract(mag,shiftLeft(val,leftShift)),1); }

  public final BigInteger subtractFrom (final long val,
                                        final int leftShift) {
    if (0 == signum) {
      return new BigInteger(shiftLeft(val,leftShift),1); }
    assert 0 < signum;
    assert 0L < val;
    return 
      new BigInteger(subtract(shiftLeft(val,leftShift),mag),1); }

  //--------------------------------------------------------------
  // multiply
  //--------------------------------------------------------------
  /**
   * The threshold value for using Karatsuba multiplication. If
   * the number
   * of ints in both mag arrays are greater than this number, then
   * Karatsuba multiplication will be used. This value is found
   * experimentally to work well.
   */
  private static final int KARATSUBA_THRESHOLD = 80;

  /** The threshold value for using 3-way Toom-Cook multiplication.
   * If the number of ints in each mag array is greater than the
   * Karatsuba threshold, and the number of ints in at least one
   * of the mag arrays is greater than this threshold, then 
   * Toom-Cook multiplication will be used.
   */
  private static final int TOOM_COOK_THRESHOLD = 240;

  /** The threshold value for using squaring code to perform
   * multiplication of a {@code BigInteger} instance by itself. If 
   * the number of ints in the number are larger than this value, 
   * {@code multiply(this)} will return {@code square()}.
   */
  private static final int MULTIPLY_SQUARE_THRESHOLD = 20;

  /** Returns a BigInteger whose value is {@code (this * val)}. If
   * the invocation is recursive certain overflow checks are
   * skipped.
   *
   * @param val
   *          value to be multiplied by this BigInteger.
   * @param isRecursion
   *          whether this is a recursive invocation
   * @return {@code this * val}
   */
  private BigInteger multiply (final BigInteger val,
                               final boolean isRecursion) {
    if ((val.signum == 0) || (signum == 0)) { return ZERO; }

    final int xlen = mag.length;

    if ((val == this) && (xlen > MULTIPLY_SQUARE_THRESHOLD)) {
      return square();
    }

    final int ylen = val.mag.length;

    if ((xlen < KARATSUBA_THRESHOLD)
      || (ylen < KARATSUBA_THRESHOLD)) {
      final int resultSign = signum == val.signum ? 1 : -1;
      if (val.mag.length == 1) {
        return multiplyByInt(mag,val.mag[0],resultSign);
      }
      if (mag.length == 1) {
        return multiplyByInt(val.mag,mag[0],resultSign);
      }
      int[] result = multiplyToLen(mag,xlen,val.mag,ylen,null);
      result = trustedStripLeadingZeroInts(result);
      return new BigInteger(result,resultSign);
    }
    if ((xlen < TOOM_COOK_THRESHOLD)
      && (ylen < TOOM_COOK_THRESHOLD)) {
      return multiplyKaratsuba(this,val);
    }
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
      if ((bitLength(mag,mag.length) + bitLength(val.mag,
        val.mag.length)) > (32L * MAX_MAG_LENGTH)) {
        reportOverflow();
      }
    }

    return multiplyToomCook3(this,val);
  }

  private static BigInteger multiplyByInt (final int[] x,
                                           final int y,
                                           final int sign) {
    if (Integer.bitCount(y) == 1) {
      return new BigInteger(
        shiftLeft(x,Integer.numberOfTrailingZeros(y)),sign);
    }
    final int xlen = x.length;
    int[] rmag = new int[xlen + 1];
    long carry = 0;
    final long yl = y & UNSIGNED_MASK;
    int rstart = rmag.length - 1;
    for (int i = xlen - 1; i >= 0; i--) {
      final long product = ((x[i] & UNSIGNED_MASK) * yl) + carry;
      rmag[rstart--] = (int) product;
      carry = product >>> 32;
    }
    if (carry == 0L) {
      rmag = Arrays.copyOfRange(rmag,1,rmag.length);
    }
    else {
      rmag[rstart] = (int) carry;
    }
    return new BigInteger(rmag,sign);
  }

  /**
   * Sentinel value for {@link #intCompact} indicating the
   * significand information is only available from
   * {@code intVal}.
   */
  static final long INFLATED = Long.MIN_VALUE;

  /**
   * Package private methods used by BigDecimal code to multiply a
   * BigInteger
   * with a long. Assumes v is not equal to INFLATED.
   */
  BigInteger multiply (long v) {
    if ((v == 0) || (signum == 0)) { return ZERO; }
    if (v == INFLATED) { return multiply(BigInteger.valueOf(v)); }
    final int rsign = (v > 0 ? signum : -signum);
    if (v < 0) {
      v = -v;
    }
    final long dh = v >>> 32;      // higher order bits
    final long dl = v & UNSIGNED_MASK; // lower order bits

    final int xlen = mag.length;
    final int[] value = mag;
    int[] rmag =
      (dh == 0L) ? (new int[xlen + 1]) : (new int[xlen + 2]);
      long carry = 0;
      int rstart = rmag.length - 1;
      for (int i = xlen - 1; i >= 0; i--) {
        final long product = ((value[i] & UNSIGNED_MASK) * dl) + carry;
        rmag[rstart--] = (int) product;
        carry = product >>> 32;
      }
      rmag[rstart] = (int) carry;
      if (dh != 0L) {
        carry = 0;
        rstart = rmag.length - 2;
        for (int i = xlen - 1; i >= 0; i--) {
          final long product =
            ((value[i] & UNSIGNED_MASK) * dh)
            + (rmag[rstart] & UNSIGNED_MASK) + carry;
          rmag[rstart--] = (int) product;
          carry = product >>> 32;
        }
        rmag[0] = (int) carry;
      }
      if (carry == 0L) {
        rmag = java.util.Arrays.copyOfRange(rmag,1,rmag.length);
      }
      return new BigInteger(rmag,rsign);
  }

  /**
   * Multiplies int arrays x and y to the specified lengths and
   * places
   * the result into z. There will be no leading zeros in the
   * resultant array.
   */
  private static int[] multiplyToLen (final int[] x,
                                      final int xlen,
                                      final int[] y,
                                      final int ylen,
                                      final int[] z) {
    multiplyToLenCheck(x,xlen);
    multiplyToLenCheck(y,ylen);
    return implMultiplyToLen(x,xlen,y,ylen,z);
  }

  private static int[] implMultiplyToLen (final int[] x,
                                          final int xlen,
                                          final int[] y,
                                          final int ylen,
                                          int[] z) {
    final int xstart = xlen - 1;
    final int ystart = ylen - 1;

    if ((z == null) || (z.length < (xlen + ylen))) {
      z = new int[xlen + ylen];
    }

    long carry = 0;
    for (int j = ystart, k = ystart + 1 + xstart; j >= 0;
      j--, k--) {
      final long product =
        ((y[j] & UNSIGNED_MASK) * (x[xstart] & UNSIGNED_MASK)) + carry;
      z[k] = (int) product;
      carry = product >>> 32;
    }
    z[xstart] = (int) carry;

    for (int i = xstart - 1; i >= 0; i--) {
      carry = 0;
      for (int j = ystart, k = ystart + 1 + i; j >= 0; j--, k--) {
        final long product =
          ((y[j] & UNSIGNED_MASK) * (x[i] & UNSIGNED_MASK))
          + (z[k] & UNSIGNED_MASK) + carry;
        z[k] = (int) product;
        carry = product >>> 32;
      }
      z[i] = (int) carry;
    }
    return z;
  }

  private static void multiplyToLenCheck (final int[] array,
                                          final int length) {
    if (length <= 0) {
      return;  // not an error because multiplyToLen won't execute
      // if len <= 0
    }

    Objects.requireNonNull(array);

    if (length > array.length) {
      throw new ArrayIndexOutOfBoundsException(length - 1);
    }
  }

  /**
   * Multiplies two BigIntegers using the Karatsuba multiplication
   * algorithm. This is a recursive divide-and-conquer algorithm
   * which is
   * more efficient for large numbers than what is commonly called
   * the
   * "grade-school" algorithm used in multiplyToLen. If the
   * numbers to be
   * multiplied have length n, the "grade-school" algorithm has an
   * asymptotic complexity of O(n^2). In contrast, the Karatsuba
   * algorithm
   * has complexity of O(n^(log2(3))), or O(n^1.585). It achieves
   * this
   * increased performance by doing 3 multiplies instead of 4 when
   * evaluating the product. As it has some overhead, should be
   * used when
   * both numbers are larger than a certain threshold (found
   * experimentally).
   *
   * See: http://en.wikipedia.org/wiki/Karatsuba_algorithm
   */
  private static BigInteger multiplyKaratsuba (final BigInteger x,
                                               final BigInteger y) {
    final int xlen = x.mag.length;
    final int ylen = y.mag.length;

    // The number of ints in each half of the number.
    final int half = (Math.max(xlen,ylen) + 1) / 2;

    // xl and yl are the lower halves of x and y respectively,
    // xh and yh are the upper halves.
    final BigInteger xl = x.getLower(half);
    final BigInteger xh = x.getUpper(half);
    final BigInteger yl = y.getLower(half);
    final BigInteger yh = y.getUpper(half);

    final BigInteger p1 = xh.multiply(yh);  // p1 = xh*yh
    final BigInteger p2 = xl.multiply(yl);  // p2 = xl*yl

    // p3=(xh+xl)*(yh+yl)
    final BigInteger p3 = xh.add(xl).multiply(yh.add(yl));

    // result = p1 * 2^(32*2*half) + (p3 - p1 - p2) * 2^(32*half)
    // + p2
    final BigInteger result =
      p1.shiftLeft(32 * half).add(p3.subtract(p1).subtract(p2))
      .shiftLeft(32 * half).add(p2);

    if (x.signum != y.signum) { return result.negate(); }
    return result;
  }

  /**
   * Multiplies two BigIntegers using a 3-way Toom-Cook
   * multiplication
   * algorithm. This is a recursive divide-and-conquer algorithm
   * which is
   * more efficient for large numbers than what is commonly called
   * the
   * "grade-school" algorithm used in multiplyToLen. If the
   * numbers to be
   * multiplied have length n, the "grade-school" algorithm has an
   * asymptotic complexity of O(n^2). In contrast, 3-way Toom-Cook
   * has a
   * complexity of about O(n^1.465). It achieves this increased
   * asymptotic
   * performance by breaking each number into three parts and by
   * doing 5
   * multiplies instead of 9 when evaluating the product. Due to
   * overhead
   * (additions, shifts, and one division) in the Toom-Cook
   * algorithm, it
   * should only be used when both numbers are larger than a
   * certain
   * threshold (found experimentally). This threshold is generally
   * larger
   * than that for Karatsuba multiplication, so this algorithm is
   * generally
   * only used when numbers become significantly larger.
   *
   * The algorithm used is the "optimal" 3-way Toom-Cook algorithm
   * outlined
   * by Marco Bodrato.
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
   *
   */
  private static BigInteger multiplyToomCook3 (final BigInteger a,
                                               final BigInteger b) {
    final int alen = a.mag.length;
    final int blen = b.mag.length;

    final int largest = Math.max(alen,blen);

    // k is the size (in ints) of the lower-order slices.
    final int k = (largest + 2) / 3;   // Equal to ceil(largest/3)

    // r is the size (in ints) of the highest-order slice.
    final int r = largest - (2 * k);

    // Obtain slices of the numbers. a2 and b2 are the most
    // significant
    // bits of the numbers a and b, and a0 and b0 the least
    // significant.
    BigInteger a0, a1, a2, b0, b1, b2;
    a2 = a.getToomSlice(k,r,0,largest);
    a1 = a.getToomSlice(k,r,1,largest);
    a0 = a.getToomSlice(k,r,2,largest);
    b2 = b.getToomSlice(k,r,0,largest);
    b1 = b.getToomSlice(k,r,1,largest);
    b0 = b.getToomSlice(k,r,2,largest);

    BigInteger v0, v1, v2, vm1, vinf, t1, t2, tm1, da1, db1;

    v0 = a0.multiply(b0,true);
    da1 = a2.add(a0);
    db1 = b2.add(b0);
    vm1 = da1.subtract(a1).multiply(db1.subtract(b1),true);
    da1 = da1.add(a1);
    db1 = db1.add(b1);
    v1 = da1.multiply(db1,true);
    v2 =
      da1.add(a2).shiftLeft(1).subtract(a0)
      .multiply(db1.add(b2).shiftLeft(1).subtract(b0),true);
    vinf = a2.multiply(b2,true);

    // The algorithm requires two divisions by 2 and one by 3.
    // All divisions are known to be exact, that is, they do not
    // produce
    // remainders, and all results are positive. The divisions by
    // 2 are
    // implemented as right shifts which are relatively efficient,
    // leaving
    // only an exact division by 3, which is done by a specialized
    // linear-time algorithm.
    t2 = v2.subtract(vm1).exactDivideBy3();
    tm1 = v1.subtract(vm1).shiftRight(1);
    t1 = v1.subtract(v0);
    t2 = t2.subtract(t1).shiftRight(1);
    t1 = t1.subtract(tm1).subtract(vinf);
    t2 = t2.subtract(vinf.shiftLeft(1));
    tm1 = tm1.subtract(t2);

    // Number of bits to shift left.
    final int ss = k * 32;

    final BigInteger result =
      vinf.shiftLeft(ss).add(t2).shiftLeft(ss).add(t1)
      .shiftLeft(ss).add(tm1).shiftLeft(ss).add(v0);

    if (a.signum != b.signum) { return result.negate(); }
    return result;
  }

  /**
   * Returns a slice of a BigInteger for use in Toom-Cook
   * multiplication.
   *
   * @param lowerSize
   *          The size of the lower-order bit slices.
   * @param upperSize
   *          The size of the higher-order bit slices.
   * @param slice
   *          The index of which slice is requested, which must be
   *          a
   *          number from 0 to size-1. Slice 0 is the
   *          highest-order bits, and slice
   *          size-1 are the lowest-order bits. Slice 0 may be of
   *          different size than
   *          the other slices.
   * @param fullsize
   *          The size of the larger integer array, used to align
   *          slices to the appropriate position when multiplying
   *          different-sized
   *          numbers.
   */
  private BigInteger getToomSlice (final int lowerSize,
                                   final int upperSize,
                                   final int slice,
                                   final int fullsize) {
    int start, end, sliceSize, len, offset;

    len = mag.length;
    offset = fullsize - len;

    if (slice == 0) {
      start = 0 - offset;
      end = upperSize - 1 - offset;
    }
    else {
      start = (upperSize + ((slice - 1) * lowerSize)) - offset;
      end = (start + lowerSize) - 1;
    }

    if (start < 0) {
      start = 0;
    }
    if (end < 0) { return ZERO; }

    sliceSize = (end - start) + 1;

    if (sliceSize <= 0) { return ZERO; }

    // While performing Toom-Cook, all slices are positive and
    // the sign is adjusted when the final number is composed.
    if ((start == 0) && (sliceSize >= len)) { return this.abs(); }

    final int intSlice[] = new int[sliceSize];
    System.arraycopy(mag,start,intSlice,0,sliceSize);

    return new BigInteger(trustedStripLeadingZeroInts(intSlice),
      1);
  }

  /**
   * Does an exact division (that is, the remainder is known to be
   * zero)
   * of the specified number by 3. This is used in Toom-Cook
   * multiplication. This is an efficient algorithm that runs in
   * linear
   * time. If the argument is not exactly divisible by 3, results
   * are
   * undefined. Note that this is expected to be called with
   * positive
   * arguments only.
   */
  private BigInteger exactDivideBy3 () {
    final int len = mag.length;
    int[] result = new int[len];
    long x, w, q, borrow;
    borrow = 0L;
    for (int i = len - 1; i >= 0; i--) {
      x = (mag[i] & UNSIGNED_MASK);
      w = x - borrow;
      if (borrow > x) {      // Did we make the number go
        // negative?
        borrow = 1L;
      }
      else {
        borrow = 0L;
      }

      // 0xAAAAAAAB is the modular inverse of 3 (mod 2^32). Thus,
      // the effect of this is to divide by 3 (mod 2^32).
      // This is much faster than division on most architectures.
      q = (w * 0xAAAAAAABL) & UNSIGNED_MASK;
      result[i] = (int) q;

      // Now check the borrow. The second check can of course be
      // eliminated if the first fails.
      if (q >= 0x55555556L) {
        borrow++;
        if (q >= 0xAAAAAAABL) {
          borrow++;
        }
      }
    }
    result = trustedStripLeadingZeroInts(result);
    return new BigInteger(result,signum);
  }

  /**
   * Returns a new BigInteger representing n lower ints of the
   * number.
   * This is used by Karatsuba multiplication and Karatsuba
   * squaring.
   */
  private BigInteger getLower (final int n) {
    final int len = mag.length;

    if (len <= n) { return abs(); }

    final int lowerInts[] = new int[n];
    System.arraycopy(mag,len - n,lowerInts,0,n);

    return new BigInteger(trustedStripLeadingZeroInts(lowerInts),
      1);
  }

  /**
   * Returns a new BigInteger representing mag.length-n upper
   * ints of the number. This is used by Karatsuba multiplication
   * and
   * Karatsuba squaring.
   */
  private BigInteger getUpper (final int n) {
    final int len = mag.length;

    if (len <= n) { return ZERO; }

    final int upperLen = len - n;
    final int upperInts[] = new int[upperLen];
    System.arraycopy(mag,0,upperInts,0,upperLen);

    return new BigInteger(trustedStripLeadingZeroInts(upperInts),
      1);
  }

  /** Returns a BigInteger whose value is {@code (this * val)}.
   *
   * @implNote An implementation may offer better algorithmic
   *           performance when {@code val == this}.
   *
   * @param val
   *          value to be multiplied by this BigInteger.
   * @return {@code this * val}
   */
  public BigInteger multiply (final BigInteger val) {
    return multiply(val,false);
  }

  //--------------------------------------------------------------
  // Squaring
  //--------------------------------------------------------------

  /**
   * Returns a BigInteger whose value is
   * {@code (this<sup>2</sup>)}.
   *
   * @return {@code this<sup>2</sup>}
   */
  private BigInteger square () {
    return square(false);
  }

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

  /**
   * Returns a BigInteger whose value is
   * {@code (this<sup>2</sup>)}. If
   * the invocation is recursive certain overflow checks are
   * skipped.
   *
   * @param isRecursion
   *          whether this is a recursive invocation
   * @return {@code this<sup>2</sup>}
   */
  private BigInteger square (final boolean isRecursion) {
    if (signum == 0) { return ZERO; }
    final int len = mag.length;

    if (len < KARATSUBA_SQUARE_THRESHOLD) {
      final int[] z = squareToLen(mag,len,null);
      return new BigInteger(trustedStripLeadingZeroInts(z),1);
    }
    if (len < TOOM_COOK_SQUARE_THRESHOLD) {
      return squareKaratsuba();
    }
    //
    // For a discussion of overflow detection see multiply()
    //
    if (!isRecursion) {
      if (bitLength(mag,mag.length) > (16L * MAX_MAG_LENGTH)) {
        reportOverflow();
      }
    }

    return squareToomCook3();
  }

  /**
   * Squares the contents of the int array x. The result is placed
   * into the
   * int array z. The contents of x are not changed.
   */
  private static int[] squareToLen (final int[] x, final int len,
                                    int[] z) {
    final int zlen = len << 1;
    if ((z == null) || (z.length < zlen)) {
      z = new int[zlen];
    }

    // Execute checks before calling intrinsified method.
    implSquareToLenChecks(x,len,z,zlen);
    return implSquareToLen(x,len,z,zlen);
  }

  /**
   * Parameters validation.
   */
  private static void implSquareToLenChecks (final int[] x,
                                             final int len,
                                             final int[] z,
                                             final int zlen)
                                               throws RuntimeException {
    if (len < 1) {
      throw new IllegalArgumentException(
        "invalid input length: " + len);
    }
    if (len > x.length) {
      throw new IllegalArgumentException(
        "input length out of bound: " + len + " > " + x.length);
    }
    if ((len * 2) > z.length) {
      throw new IllegalArgumentException(
        "input length out of bound: " + (len * 2) + " > "
          + z.length);
    }
    if (zlen < 1) {
      throw new IllegalArgumentException(
        "invalid input length: " + zlen);
    }
    if (zlen > z.length) {
      throw new IllegalArgumentException(
        "input length out of bound: " + len + " > " + z.length);
    }
  }

  /**
   * Java Runtime may use intrinsic for this method.
   */

  private static int[] implSquareToLen (final int[] x,
                                        final int len,
                                        final int[] z,
                                        final int zlen) {
    /*
     * The algorithm used here is adapted from Colin Plumb's C
     * library.
     * Technique: Consider the partial products in the
     * multiplication
     * of "abcde" by itself:
     *
     * a b c d e
     * * a b c d e
     * ==================
     * ae be ce de ee
     * ad bd cd dd de
     * ac bc cc cd ce
     * ab bb bc bd be
     * aa ab ac ad ae
     *
     * Note that everything above the main diagonal:
     * ae be ce de = (abcd) * e
     * ad bd cd = (abc) * d
     * ac bc = (ab) * c
     * ab = (a) * b
     *
     * is a copy of everything below the main diagonal:
     * de
     * cd ce
     * bc bd be
     * ab ac ad ae
     *
     * Thus, the sum is 2 * (off the diagonal) + diagonal.
     *
     * This is accumulated beginning with the diagonal (which
     * consist of the squares of the digits of the input), which
     * is then
     * divided by two, the off-diagonal added, and multiplied by
     * two
     * again. The low bit is simply a copy of the low bit of the
     * input, so it doesn't need special care.
     */

    // Store the squares, right shifted one bit (i.e., divided by
    // 2)
    int lastProductLowWord = 0;
    for (int j = 0, i = 0; j < len; j++) {
      final long piece = (x[j] & UNSIGNED_MASK);
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
      addOne(z,offset - 1,i,t);
    }

    // Shift back up and set low bit
    primitiveLeftShift(z,zlen,1);
    z[zlen - 1] |= x[len - 1] & 1;

    return z;
  }

  /**
   * Squares a BigInteger using the Karatsuba squaring algorithm.
   * It should
   * be used when both numbers are larger than a certain threshold
   * (found
   * experimentally). It is a recursive divide-and-conquer
   * algorithm that
   * has better asymptotic performance than the algorithm used in
   * squareToLen.
   */
  private BigInteger squareKaratsuba () {
    final int half = (mag.length + 1) / 2;

    final BigInteger xl = getLower(half);
    final BigInteger xh = getUpper(half);

    final BigInteger xhs = xh.square();  // xhs = xh^2
    final BigInteger xls = xl.square();  // xls = xl^2

    // xh^2 << 64 + (((xl+xh)^2 - (xh^2 + xl^2)) << 32) + xl^2
    return xhs.shiftLeft(half * 32)
      .add(xl.add(xh).square().subtract(xhs.add(xls)))
      .shiftLeft(half * 32).add(xls);
  }

  /**
   * Squares a BigInteger using the 3-way Toom-Cook squaring
   * algorithm. It
   * should be used when both numbers are larger than a certain
   * threshold
   * (found experimentally). It is a recursive divide-and-conquer
   * algorithm
   * that has better asymptotic performance than the algorithm
   * used in
   * squareToLen or squareKaratsuba.
   */
  private BigInteger squareToomCook3 () {
    final int len = mag.length;

    // k is the size (in ints) of the lower-order slices.
    final int k = (len + 2) / 3;   // Equal to ceil(largest/3)

    // r is the size (in ints) of the highest-order slice.
    final int r = len - (2 * k);

    // Obtain slices of the numbers. a2 is the most significant
    // bits of the number, and a0 the least significant.
    BigInteger a0, a1, a2;
    a2 = getToomSlice(k,r,0,len);
    a1 = getToomSlice(k,r,1,len);
    a0 = getToomSlice(k,r,2,len);
    BigInteger v0, v1, v2, vm1, vinf, t1, t2, tm1, da1;

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
      .shiftLeft(ss).add(tm1).shiftLeft(ss).add(v0);
  }

  //--------------------------------------------------------------
  // Division
  //--------------------------------------------------------------

  /** The threshold value for using Burnikel-Ziegler division. If
   * the number of ints in the divisor are larger than this value,
   * Burnikel-Ziegler division may be used. This value is found 
   * experimentally to work well.
   */
  public static final int BURNIKEL_ZIEGLER_THRESHOLD = 80;

  /** The offset value for using Burnikel-Ziegler division. If the
   * number of ints in the divisor exceeds the Burnikel-Ziegler
   * threshold, and the number of ints in the dividend is greater 
   * than the number of ints in the divisor plus this value, 
   * Burnikel-Ziegler division will be used. This
   * value is found experimentally to work well.
   */
  public static final int BURNIKEL_ZIEGLER_OFFSET = 40;

  /**
   * Returns a BigInteger whose value is {@code (this / val)}.
   *
   * @param val
   *          value by which this BigInteger is to be divided.
   * @return {@code this / val}
   * @throws ArithmeticException
   *           if {@code val} is zero.
   */
  public BigInteger divide (final BigInteger val) {
    if ((val.mag.length < BURNIKEL_ZIEGLER_THRESHOLD)
      || ((mag.length
        - val.mag.length) < BURNIKEL_ZIEGLER_OFFSET)) {
      return divideKnuth(val);
    }
    return divideBurnikelZiegler(val);
  }

  /**
   * Returns a BigInteger whose value is {@code (this / val)}
   * using an O(n^2) algorithm from Knuth.
   *
   * @param val
   *          value by which this BigInteger is to be divided.
   * @return {@code this / val}
   * @throws ArithmeticException
   *           if {@code val} is zero.
   * @see MutableBigInteger#divideKnuth(MutableBigInteger,
   *      MutableBigInteger, boolean)
   */
  private BigInteger divideKnuth (final BigInteger val) {
    final MutableBigInteger q = new MutableBigInteger(),
      a = new MutableBigInteger(this.mag),
      b = new MutableBigInteger(val.mag);

    a.divideKnuth(b,q,false);
    return q.toBigInteger(this.signum * val.signum);
  }

  /**
   * Returns an array of two BigIntegers containing
   * {@code (this / val)}
   * followed by {@code (this % val)}.
   *
   * @param val
   *          value by which this BigInteger is to be divided, and
   *          the
   *          remainder computed.
   * @return an array of two BigIntegers: the quotient
   *         {@code (this / val)}
   *         is the initial element, and the remainder
   *         {@code (this % val)}
   *         is the final element.
   * @throws ArithmeticException
   *           if {@code val} is zero.
   */
  public BigInteger[] divideAndRemainder (final BigInteger val) {
    if ((val.mag.length < BURNIKEL_ZIEGLER_THRESHOLD)
      || ((mag.length
        - val.mag.length) < BURNIKEL_ZIEGLER_OFFSET)) {
      return divideAndRemainderKnuth(val);
    }
    return divideAndRemainderBurnikelZiegler(val);
  }

  /** Long division */
  private BigInteger[] divideAndRemainderKnuth (final BigInteger val) {
    final BigInteger[] result = new BigInteger[2];
    final MutableBigInteger q = new MutableBigInteger(),
      a = new MutableBigInteger(this.mag),
      b = new MutableBigInteger(val.mag);
    final MutableBigInteger r = a.divideKnuth(b,q);
    result[0] =
      q.toBigInteger(this.signum == val.signum ? 1 : -1);
    result[1] = r.toBigInteger(this.signum);
    return result;
  }

  /**
   * Returns a BigInteger whose value is {@code (this % val)}.
   *
   * @param val
   *          value by which this BigInteger is to be divided, and
   *          the
   *          remainder computed.
   * @return {@code this % val}
   * @throws ArithmeticException
   *           if {@code val} is zero.
   */
  public BigInteger remainder (final BigInteger val) {
    if ((val.mag.length < BURNIKEL_ZIEGLER_THRESHOLD)
      || ((mag.length
        - val.mag.length) < BURNIKEL_ZIEGLER_OFFSET)) {
      return remainderKnuth(val);
    }
    return remainderBurnikelZiegler(val);
  }

  /** Long division */
  private BigInteger remainderKnuth (final BigInteger val) {
    final MutableBigInteger q = new MutableBigInteger(),
      a = new MutableBigInteger(this.mag),
      b = new MutableBigInteger(val.mag);

    return a.divideKnuth(b,q).toBigInteger(this.signum);
  }

  /**
   * Calculates {@code this / val} using the Burnikel-Ziegler
   * algorithm.
   * 
   * @param val
   *          the divisor
   * @return {@code this / val}
   */
  private BigInteger divideBurnikelZiegler (final BigInteger val) {
    return divideAndRemainderBurnikelZiegler(val)[0];
  }

  /**
   * Calculates {@code this % val} using the Burnikel-Ziegler
   * algorithm.
   * 
   * @param val
   *          the divisor
   * @return {@code this % val}
   */
  private BigInteger remainderBurnikelZiegler (final BigInteger val) {
    return divideAndRemainderBurnikelZiegler(val)[1];
  }

  /**
   * Computes {@code this / val} and {@code this % val} using the
   * Burnikel-Ziegler algorithm.
   * 
   * @param val
   *          the divisor
   * @return an array containing the quotient and remainder
   */
  private BigInteger[] divideAndRemainderBurnikelZiegler (final BigInteger val) {
    final MutableBigInteger q = new MutableBigInteger();
    final MutableBigInteger r =
      new MutableBigInteger(this)
      .divideAndRemainderBurnikelZiegler(
        new MutableBigInteger(val),q);
    final BigInteger qBigInt =
      q.isZero() ? ZERO : q.toBigInteger(signum * val.signum);
    final BigInteger rBigInt =
      r.isZero() ? ZERO : r.toBigInteger(signum);
    return new BigInteger[] { qBigInt, rBigInt };
  }

  /**
   * Returns a BigInteger whose value is
   * <code>(this<sup>exponent</sup>)</code>.
   * Note that {@code exponent} is an integer rather than a
   * BigInteger.
   *
   * @param exponent
   *          exponent to which this BigInteger is to be raised.
   * @return <code>this<sup>exponent</sup></code>
   * @throws ArithmeticException
   *           {@code exponent} is negative. (This would
   *           cause the operation to yield a non-integer value.)
   */
  public BigInteger pow (final int exponent) {
    if (exponent < 0) {
      throw new ArithmeticException("Negative exponent");
    }
    if (signum == 0) { return (exponent == 0 ? ONE : this); }

    BigInteger partToSquare = this.abs();

    // Factor out powers of two from the base, as the
    // exponentiation of
    // these can be done by left shifts only.
    // The remaining part can then be exponentiated faster. The
    // powers of two will be multiplied back at the end.
    final int powersOfTwo = partToSquare.getLowestSetBit();
    final long bitsToShiftLong = (long) powersOfTwo * exponent;
    if (bitsToShiftLong > Integer.MAX_VALUE) {
      reportOverflow();
    }
    final int bitsToShift = (int) bitsToShiftLong;

    int remainingBits;

    // Factor the powers of two out quickly by shifting right, if
    // needed.
    if (powersOfTwo > 0) {
      partToSquare = partToSquare.shiftRight(powersOfTwo);
      remainingBits = partToSquare.bitLength();
      if (remainingBits == 1) {  // Nothing left but +/- 1?
        if ((signum < 0) && ((exponent & 1) == 1)) {
          return NEGATIVE_ONE.shiftLeft(bitsToShift);
        }
        return ONE.shiftLeft(bitsToShift);
      }
    }
    else {
      remainingBits = partToSquare.bitLength();
      if (remainingBits == 1) { // Nothing left but +/- 1?
        if ((signum < 0) && ((exponent & 1) == 1)) {
          return NEGATIVE_ONE;
        }
        return ONE;
      }
    }

    // This is a quick way to approximate the size of the result,
    // similar to doing log2[n] * exponent. This will give an
    // upper bound
    // of how big the result can be, and which algorithm to use.
    final long scaleFactor = (long) remainingBits * exponent;

    // Use slightly different algorithms for small and large
    // operands.
    // See if the result will safely fit into a long. (Largest
    // 2^63-1)
    if ((partToSquare.mag.length == 1) && (scaleFactor <= 62)) {
      // Small number algorithm. Everything fits into a long.
      final int newSign =
        ((signum < 0) && ((exponent & 1) == 1) ? -1 : 1);
      long result = 1;
      long baseToPow2 = partToSquare.mag[0] & UNSIGNED_MASK;

      int workingExponent = exponent;

      // Perform exponentiation using repeated squaring trick
      while (workingExponent != 0) {
        if ((workingExponent & 1) == 1) {
          result = result * baseToPow2;
        }

        if ((workingExponent >>>= 1) != 0) {
          baseToPow2 = baseToPow2 * baseToPow2;
        }
      }

      // Multiply back the powers of two (quickly, by shifting
      // left)
      if (powersOfTwo > 0) {
        if ((bitsToShift + scaleFactor) <= 62) { // Fits in long?
          return valueOf((result << bitsToShift) * newSign);
        }
        return valueOf(result * newSign).shiftLeft(bitsToShift);
      }
      return valueOf(result * newSign);
    }
    if ((((long) bitLength() * exponent)
      / Integer.SIZE) > MAX_MAG_LENGTH) {
      reportOverflow();
    }

    // Large number algorithm. This is basically identical to
    // the algorithm above, but calls multiply() and square()
    // which may use more efficient algorithms for large numbers.
    BigInteger answer = ONE;

    int workingExponent = exponent;
    // Perform exponentiation using repeated squaring trick
    while (workingExponent != 0) {
      if ((workingExponent & 1) == 1) {
        answer = answer.multiply(partToSquare);
      }

      if ((workingExponent >>>= 1) != 0) {
        partToSquare = partToSquare.square();
      }
    }
    // Multiply back the (exponentiated) powers of two (quickly,
    // by shifting left)
    if (powersOfTwo > 0) {
      answer = answer.shiftLeft(bitsToShift);
    }

    if ((signum < 0) && ((exponent & 1) == 1)) {
      return answer.negate();
    }
    return answer;
  }

  /**
   * Returns the integer square root of this BigInteger. The
   * integer square
   * root of the corresponding mathematical integer {@code n} is
   * the largest
   * mathematical integer {@code s} such that {@code s*s <= n}. It
   * is equal
   * to the value of {@code floor(sqrt(n))}, where {@code sqrt(n)}
   * denotes the
   * real square root of {@code n} treated as a real. Note that
   * the integer
   * square root will be less than the real square root if the
   * latter is not
   * representable as an integral value.
   *
   * @return the integer square root of {@code this}
   * @throws ArithmeticException
   *           if {@code this} is negative. (The square
   *           root of a negative integer {@code val} is
   *           {@code (i * sqrt(-val))} where <i>i</i> is the
   *           <i>imaginary unit</i> and is equal to
   *           {@code sqrt(-1)}.)
   * @since 9
   */
  public BigInteger sqrt () {
    if (this.signum < 0) {
      throw new ArithmeticException("Negative BigInteger");
    }

    return new MutableBigInteger(this.mag).sqrt().toBigInteger();
  }

  /**
   * Returns an array of two BigIntegers containing the integer
   * square root
   * {@code s} of {@code this} and its remainder
   * {@code this - s*s},
   * respectively.
   *
   * @return an array of two BigIntegers with the integer square
   *         root at
   *         offset 0 and the remainder at offset 1
   * @throws ArithmeticException
   *           if {@code this} is negative. (The square
   *           root of a negative integer {@code val} is
   *           {@code (i * sqrt(-val))} where <i>i</i> is the
   *           <i>imaginary unit</i> and is equal to
   *           {@code sqrt(-1)}.)
   * @see #sqrt()
   * @since 9
   */
  public BigInteger[] sqrtAndRemainder () {
    final BigInteger s = sqrt();
    final BigInteger r = this.subtract(s.square());
    assert r.compareTo(BigInteger.ZERO) >= 0;
    return new BigInteger[] { s, r };
  }

  /**
   * Returns a BigInteger whose value is the greatest common
   * divisor of
   * {@code abs(this)} and {@code abs(val)}. Returns 0 if
   * {@code this == 0 && val == 0}.
   *
   * @param val
   *          value with which the GCD is to be computed.
   * @return {@code GCD(abs(this), abs(val))}
   */
  public BigInteger gcd (final BigInteger val) {
    if (val.signum == 0) {
      return this.abs();
    }
    else if (this.signum == 0) { return val.abs(); }

    final MutableBigInteger a = new MutableBigInteger(this);
    final MutableBigInteger b = new MutableBigInteger(val);

    final MutableBigInteger result = a.hybridGCD(b);

    return result.toBigInteger(1);
  }

  /**
   * Package private method to return bit length for an integer.
   */
  static int bitLengthForInt (final int n) {
    return 32 - Integer.numberOfLeadingZeros(n);
  }

  // shifts a up to len right n bits assumes no leading zeros,
  // 0<n<32
  static void primitiveRightShift (final int[] a, final int len,
                                   final int n) {
    final int n2 = 32 - n;
    for (int i = len - 1, c = a[i]; i > 0; i--) {
      final int b = c;
      c = a[i - 1];
      a[i] = (c << n2) | (b >>> n);
    }
    a[0] >>>= n;
  }

  // shifts a up to len left n bits assumes no leading zeros,
  // 0<=n<32
  static void primitiveLeftShift (final int[] a, final int len,
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
    return ((len - 1) << 5) + bitLengthForInt(val[0]);
  }

  /**
   * Returns a BigInteger whose value is the absolute value of
   * this
   * BigInteger.
   *
   * @return {@code abs(this)}
   */
  public BigInteger abs () {
    return (signum >= 0 ? this : this.negate());
  }

  /**
   * Returns a BigInteger whose value is {@code (-this)}.
   *
   * @return {@code -this}
   */
  public BigInteger negate () {
    return new BigInteger(this.mag,-this.signum);
  }

  /**
   * Returns the signum function of this BigInteger.
   *
   * @return -1, 0 or 1 as the value of this BigInteger is
   *         negative, zero or
   *         positive.
   */
  public int signum () {
    return this.signum;
  }

  // Modular Arithmetic Operations

  /**
   * Returns a BigInteger whose value is {@code (this mod m}).
   * This method
   * differs from {@code remainder} in that it always returns a
   * <i>non-negative</i> BigInteger.
   *
   * @param m
   *          the modulus.
   * @return {@code this mod m}
   * @throws ArithmeticException
   *           {@code m} &le; 0
   * @see #remainder
   */
  public BigInteger mod (final BigInteger m) {
    if (m.signum <= 0) {
      throw new ArithmeticException(
        "BigInteger: modulus not positive");
    }

    final BigInteger result = this.remainder(m);
    return (result.signum >= 0 ? result : result.add(m));
  }

  static int[] bnExpModThreshTable =
  { 7, 25, 81, 241, 673, 1793, Integer.MAX_VALUE }; // Sentinel

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

  /**
   * Parameters validation.
   */
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

  /**
   * Java Runtime may use intrinsic for this method.
   */

  private static int implMulAdd (final int[] out, final int[] in,
                                 int offset, final int len,
                                 final int k) {
    final long kLong = k & UNSIGNED_MASK;
    long carry = 0;

    offset = out.length - offset - 1;
    for (int j = len - 1; j >= 0; j--) {
      final long product =
        ((in[j] & UNSIGNED_MASK) * kLong) + (out[offset] & UNSIGNED_MASK)
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
  static int addOne (final int[] a, int offset, int mlen,
                     final int carry) {
    offset = a.length - 1 - mlen - offset;
    final long t = (a[offset] & UNSIGNED_MASK) + (carry & UNSIGNED_MASK);

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

  // Shift Operations

  /**
   * Returns a BigInteger whose value is {@code (this << n)}.
   * The shift distance, {@code n}, may be negative, in which case
   * this method performs a right shift.
   * (Computes <code>floor(this * 2<sup>n</sup>)</code>.)
   *
   * @param n
   *          shift distance, in bits.
   * @return {@code this << n}
   * @see #shiftRight
   */
  public BigInteger shiftLeft (final int n) {
    if (signum == 0) { return ZERO; }
    if (n > 0) {
      return new BigInteger(shiftLeft(mag,n),signum);
    }
    else if (n == 0) {
      return this;
    }
    else {
      // Possible int overflow in (-n) is not a trouble,
      // because shiftRightImpl considers its argument unsigned
      return shiftRightImpl(-n);
    }
  }

  /**
   * Returns a magnitude array whose value is {@code (mag << n)}.
   * The shift distance, {@code n}, is considered unnsigned.
   * (Computes <code>this * 2<sup>n</sup></code>.)
   *
   * @param mag
   *          magnitude, the most-significant int ({@code mag[0]})
   *          must be non-zero.
   * @param n
   *          unsigned shift distance, in bits.
   * @return {@code mag << n}
   */
  private static int[] shiftLeft (final int[] mag, final int n) {
    final int nInts = n >>> 5;
    final int nBits = n & 0x1f;
    final int magLen = mag.length;
    int newMag[] = null;

    if (nBits == 0) {
      newMag = new int[magLen + nInts];
      System.arraycopy(mag,0,newMag,0,magLen);
    }
    else {
      int i = 0;
      final int nBits2 = 32 - nBits;
      final int highBits = mag[0] >>> nBits2;
      if (highBits != 0) {
        newMag = new int[magLen + nInts + 1];
        newMag[i++] = highBits;
      }
      else {
        newMag = new int[magLen + nInts];
      }
      int j = 0;
      while (j < (magLen - 1)) {
        newMag[i++] = (mag[j++] << nBits) | (mag[j] >>> nBits2);
      }
      newMag[i] = mag[j] << nBits;
    }
    return newMag;
  }

  /**
   * Returns a BigInteger whose value is {@code (this >> n)}. Sign
   * extension is performed. The shift distance, {@code n}, may be
   * negative, in which case this method performs a left shift.
   * (Computes <code>floor(this / 2<sup>n</sup>)</code>.)
   *
   * @param n
   *          shift distance, in bits.
   * @return {@code this >> n}
   * @see #shiftLeft
   */
  public BigInteger shiftRight (final int n) {
    if (signum == 0) { return ZERO; }
    if (n > 0) {
      return shiftRightImpl(n);
    }
    else if (n == 0) {
      return this;
    }
    else {
      // Possible int overflow in {@code -n} is not a trouble,
      // because shiftLeft considers its argument unsigned
      return new BigInteger(shiftLeft(mag,-n),signum);
    }
  }

  /**
   * Returns a BigInteger whose value is {@code (this >> n)}. The
   * shift
   * distance, {@code n}, is considered unsigned.
   * (Computes <code>floor(this * 2<sup>-n</sup>)</code>.)
   *
   * @param n
   *          unsigned shift distance, in bits.
   * @return {@code this >> n}
   */
  private BigInteger shiftRightImpl (final int n) {
    final int nInts = n >>> 5;
    final int nBits = n & 0x1f;
    final int magLen = mag.length;
    int newMag[] = null;

    // Special case: entire contents shifted off the end
    if (nInts >= magLen) {
      return (signum >= 0 ? ZERO : negConst[1]);
    }

    if (nBits == 0) {
      final int newMagLen = magLen - nInts;
      newMag = Arrays.copyOf(mag,newMagLen);
    }
    else {
      int i = 0;
      final int highBits = mag[0] >>> nBits;
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
        newMag[i++] = (mag[j++] << nBits2) | (mag[j] >>> nBits);
      }
    }

    if (signum < 0) {
      // Find out whether any one-bits were shifted off the end.
      boolean onesLost = false;
      for (int i = magLen - 1, j = magLen - nInts;
        (i >= j) && !onesLost; i--) {
        onesLost = (mag[i] != 0);
      }
      if (!onesLost && (nBits != 0)) {
        onesLost =
          ((mag[magLen - nInts - 1] << (32 - nBits)) != 0);
      }

      if (onesLost) {
        newMag = javaIncrement(newMag);
      }
    }

    return new BigInteger(newMag,signum);
  }

  private static final int[] javaIncrement (int[] val) {
    int lastSum = 0;
    for (int i = val.length - 1; (i >= 0) && (lastSum == 0);
      i--) {
      lastSum = (val[i] += 1);
    }
    if (lastSum == 0) {
      val = new int[val.length + 1];
      val[0] = 1;
    }
    return val;
  }

  // Bitwise Operations

  /**
   * Returns a BigInteger whose value is {@code (this & val)}.
   * (This
   * method returns a negative BigInteger if and only if this and
   * val are
   * both negative.)
   *
   * @param val
   *          value to be AND'ed with this BigInteger.
   * @return {@code this & val}
   */
  public BigInteger and (final BigInteger val) {
    final int[] result =
      new int[Math.max(intLength(),val.intLength())];
    for (int i = 0; i < result.length; i++) {
      result[i] =
        (getInt(result.length - i - 1)
          & val.getInt(result.length - i - 1));
    }

    return valueOf(result);
  }

  /**
   * Returns a BigInteger whose value is {@code (this | val)}.
   * (This method
   * returns a negative BigInteger if and only if either this or
   * val is
   * negative.)
   *
   * @param val
   *          value to be OR'ed with this BigInteger.
   * @return {@code this | val}
   */
  public BigInteger or (final BigInteger val) {
    final int[] result =
      new int[Math.max(intLength(),val.intLength())];
    for (int i = 0; i < result.length; i++) {
      result[i] =
        (getInt(result.length - i - 1)
          | val.getInt(result.length - i - 1));
    }

    return valueOf(result);
  }

  /**
   * Returns a BigInteger whose value is {@code (this ^ val)}.
   * (This method
   * returns a negative BigInteger if and only if exactly one of
   * this and
   * val are negative.)
   *
   * @param val
   *          value to be XOR'ed with this BigInteger.
   * @return {@code this ^ val}
   */
  public BigInteger xor (final BigInteger val) {
    final int[] result =
      new int[Math.max(intLength(),val.intLength())];
    for (int i = 0; i < result.length; i++) {
      result[i] =
        (getInt(result.length - i - 1)
          ^ val.getInt(result.length - i - 1));
    }

    return valueOf(result);
  }

  /**
   * Returns a BigInteger whose value is {@code (~this)}. (This
   * method
   * returns a negative value if and only if this BigInteger is
   * non-negative.)
   *
   * @return {@code ~this}
   */
  public BigInteger not () {
    final int[] result = new int[intLength()];
    for (int i = 0; i < result.length; i++) {
      result[i] = ~getInt(result.length - i - 1);
    }

    return valueOf(result);
  }

  /**
   * Returns a BigInteger whose value is {@code (this & ~val)}.
   * This
   * method, which is equivalent to {@code and(val.not())}, is
   * provided as
   * a convenience for masking operations. (This method returns a
   * negative
   * BigInteger if and only if {@code this} is negative and
   * {@code val} is
   * positive.)
   *
   * @param val
   *          value to be complemented and AND'ed with this
   *          BigInteger.
   * @return {@code this & ~val}
   */
  public BigInteger andNot (final BigInteger val) {
    final int[] result =
      new int[Math.max(intLength(),val.intLength())];
    for (int i = 0; i < result.length; i++) {
      result[i] =
        (getInt(result.length - i - 1)
          & ~val.getInt(result.length - i - 1));
    }

    return valueOf(result);
  }

  // Single Bit Operations

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
  public boolean testBit (final int n) {
    if (n < 0) {
      throw new ArithmeticException("Negative bit address");
    }

    return (getInt(n >>> 5) & (1 << (n & 31))) != 0;
  }

  /**
   * Returns a BigInteger whose value is equivalent to this
   * BigInteger
   * with the designated bit set. (Computes
   * {@code (this | (1<<n))}.)
   *
   * @param n
   *          index of bit to set.
   * @return {@code this | (1<<n)}
   * @throws ArithmeticException
   *           {@code n} is negative.
   */
  public BigInteger setBit (final int n) {
    if (n < 0) {
      throw new ArithmeticException("Negative bit address");
    }

    final int intNum = n >>> 5;
    final int[] result =
      new int[Math.max(intLength(),intNum + 2)];

    for (int i = 0; i < result.length; i++) {
      result[result.length - i - 1] = getInt(i);
    }

    result[result.length - intNum - 1] |= (1 << (n & 31));

    return valueOf(result);
  }

  /**
   * Returns a BigInteger whose value is equivalent to this
   * BigInteger
   * with the designated bit cleared.
   * (Computes {@code (this & ~(1<<n))}.)
   *
   * @param n
   *          index of bit to clear.
   * @return {@code this & ~(1<<n)}
   * @throws ArithmeticException
   *           {@code n} is negative.
   */
  public BigInteger clearBit (final int n) {
    if (n < 0) {
      throw new ArithmeticException("Negative bit address");
    }

    final int intNum = n >>> 5;
    final int[] result =
      new int[Math.max(intLength(),((n + 1) >>> 5) + 1)];

    for (int i = 0; i < result.length; i++) {
      result[result.length - i - 1] = getInt(i);
    }

    result[result.length - intNum - 1] &= ~(1 << (n & 31));

    return valueOf(result);
  }

  /**
   * Returns a BigInteger whose value is equivalent to this
   * BigInteger
   * with the designated bit flipped.
   * (Computes {@code (this ^ (1<<n))}.)
   *
   * @param n
   *          index of bit to flip.
   * @return {@code this ^ (1<<n)}
   * @throws ArithmeticException
   *           {@code n} is negative.
   */
  public BigInteger flipBit (final int n) {
    if (n < 0) {
      throw new ArithmeticException("Negative bit address");
    }

    final int intNum = n >>> 5;
    final int[] result =
      new int[Math.max(intLength(),intNum + 2)];

    for (int i = 0; i < result.length; i++) {
      result[result.length - i - 1] = getInt(i);
    }

    result[result.length - intNum - 1] ^= (1 << (n & 31));

    return valueOf(result);
  }

  /**
   * Returns the index of the rightmost (lowest-order) one bit in
   * this
   * BigInteger (the number of zero bits to the right of the
   * rightmost
   * one bit). Returns -1 if this BigInteger contains no one bits.
   * (Computes {@code (this == 0? -1 : log2(this & -this))}.)
   *
   * @return index of the rightmost one bit in this BigInteger.
   */
  public int getLowestSetBit () {
    int lsb = lowestSetBitPlusTwo - 2;
    if (lsb == -2) {  // lowestSetBit not initialized yet
      lsb = 0;
      if (signum == 0) {
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

  // Miscellaneous Bit Operations

  /**
   * Returns the number of bits in the minimal two's-complement
   * representation of this BigInteger, <em>excluding</em> a sign
   * bit.
   * For positive BigIntegers, this is equivalent to the number of
   * bits in
   * the ordinary binary representation. For zero this method
   * returns
   * {@code 0}. (Computes
   * {@code (ceil(log2(this < 0 ? -this : this+1)))}.)
   *
   * @return number of bits in the minimal two's-complement
   *         representation of this BigInteger, <em>excluding</em>
   *         a sign bit.
   */
  public int bitLength () {
    int n = bitLengthPlusOne - 1;
    if (n == -1) { // bitLength not initialized yet
      final int[] m = mag;
      final int len = m.length;
      if (len == 0) {
        n = 0; // offset by one to initialize
      }
      else {
        // Calculate the bit length of the magnitude
        final int magBitLength =
          ((len - 1) << 5) + bitLengthForInt(mag[0]);
        if (signum < 0) {
          // Check if magnitude is a power of two
          boolean pow2 = (Integer.bitCount(mag[0]) == 1);
          for (int i = 1; (i < len) && pow2; i++) {
            pow2 = (mag[i] == 0);
          }

          n = (pow2 ? magBitLength - 1 : magBitLength);
        }
        else {
          n = magBitLength;
        }
      }
      bitLengthPlusOne = n + 1;
    }
    return n;
  }

  /**
   * Returns the number of bits in the two's complement
   * representation
   * of this BigInteger that differ from its sign bit. This method
   * is
   * useful when implementing bit-vector style sets atop
   * BigIntegers.
   *
   * @return number of bits in the two's complement representation
   *         of this BigInteger that differ from its sign bit.
   */
  public int bitCount () {
    int bc = bitCountPlusOne - 1;
    if (bc == -1) {  // bitCount not initialized yet
      bc = 0;      // offset by one to initialize
      // Count the bits in the magnitude
      for (final int element : mag) {
        bc += Integer.bitCount(element);
      }
      if (signum < 0) {
        // Count the trailing zeros in the magnitude
        int magTrailingZeroCount = 0, j;
        for (j = mag.length - 1; mag[j] == 0; j--) {
          magTrailingZeroCount += 32;
        }
        magTrailingZeroCount +=
          Integer.numberOfTrailingZeros(mag[j]);
        bc += magTrailingZeroCount - 1;
      }
      bitCountPlusOne = bc + 1;
    }
    return bc;
  }

  // Comparison Operations

  /**
   * Compares this BigInteger with the specified BigInteger. This
   * method is provided in preference to individual methods for
   * each
   * of the six boolean comparison operators ({@literal <}, ==,
   * {@literal >}, {@literal >=}, !=, {@literal <=}). The
   * suggested
   * idiom for performing these comparisons is: {@code
   * (x.compareTo(y)} &lt;<i>op</i>&gt; {@code 0)}, where
   * &lt;<i>op</i>&gt; is one of the six comparison operators.
   *
   * @param val
   *          BigInteger to which this BigInteger is to be
   *          compared.
   * @return -1, 0 or 1 as this BigInteger is numerically less
   *         than, equal
   *         to, or greater than {@code val}.
   */
  @Override
  public int compareTo (final BigInteger val) {
    if (signum == val.signum) {
      switch (signum) {
      case 1:
        return compareMagnitude(val);
      case -1:
        return val.compareMagnitude(this);
      default:
        return 0; } }
    return signum > val.signum ? 1 : -1;
  }

  public final int compareTo (final long val) {
    assert val >= 0L;
    assert signum >= 0;
    if (val == 0L) {
      if (signum == 0) { return 0; }
      return -1; }
    if (signum == 0) { return -1; }
    return compareMagnitude(val); }

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
      if (a != b) {
        return ((a & UNSIGNED_MASK) < (b & UNSIGNED_MASK)) 
          ? -1 : 1; } }
    return 0; }

  public final int compareMagnitude (final BigInteger val) {
    return compareMagnitude(mag,val.mag); }

  public final int compareMagnitude (final long val) {
    assert 0L <= val;
    final int[] m1 = mag;
    final int len = m1.length;
    if (len > 2) { return 1; }
    final int highWord = (int) (val >>> 32);
    if (highWord == 0) {
      if (len < 1) { return -1; }
      if (len > 1) { return 1; }
      final int a = m1[0];
      final int b = (int) val;
      if (a != b) {
        return ((a & UNSIGNED_MASK) < (b & UNSIGNED_MASK)) ? -1 : 1;
      }
      return 0;
    }
    if (len < 2) { return -1; }
    int a = m1[0];
    int b = highWord;
    if (a != b) {
      return ((a & UNSIGNED_MASK) < (b & UNSIGNED_MASK)) ? -1 : 1;
    }
    a = m1[1];
    b = (int) val;
    if (a != b) {
      return ((a & UNSIGNED_MASK) < (b & UNSIGNED_MASK)) ? -1 : 1;
    }
    return 0;
  }

  public final int compareMagnitude (final long val,
                                     final int leftShift) {
    return compareMagnitude(mag,shiftLeft(val,leftShift)); }

  //--------------------------------------------------------------
  /**
   * Returns the minimum of this BigInteger and {@code val}.
   *
   * @param val
   *          value with which the minimum is to be computed.
   * @return the BigInteger whose value is the lesser of this
   *         BigInteger and
   *         {@code val}. If they are equal, either may be
   *         returned.
   */
  public BigInteger min (final BigInteger val) {
    return (compareTo(val) < 0 ? this : val); }

  /**
   * Returns the maximum of this BigInteger and {@code val}.
   *
   * @param val
   *          value with which the maximum is to be computed.
   * @return the BigInteger whose value is the greater of this and
   *         {@code val}. If they are equal, either may be
   *         returned.
   */
  public BigInteger max (final BigInteger val) {
    return (compareTo(val) > 0 ? this : val); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public int hashCode () {
    int hashCode = 0;
    for (final int element : mag) {
      hashCode = (int) ((31 * hashCode) + (element & UNSIGNED_MASK));
    }
    return hashCode * signum; }

  @Override
  public boolean equals (final Object x) {
    if (x == this) { return true; }
    if (!(x instanceof BigInteger)) { return false; }
    final BigInteger xInt = (BigInteger) x;
    if (xInt.signum != signum) { return false; }
    final int[] m = mag;
    final int len = m.length;
    final int[] xm = xInt.mag;
    if (len != xm.length) { return false; }
    for (int i = 0; i < len; i++) {
      if (xm[i] != m[i]) { return false; } }
    return true; }

  /** Returns the String representation of this BigInteger in the
   * given radix. If the radix is outside the range from {@link
   * Character#MIN_RADIX} to {@link Character#MAX_RADIX}
   * inclusive,
   * it will default to 10 (as is the case for
   * {@code Integer.toString}). The digit-to-character mapping
   * provided by {@code Character.forDigit} is used, and a minus
   * sign is prepended if appropriate. (This representation is
   * compatible with the {@link #BigInteger(String, int) (String,
   * int)} constructor.)
   *
   * @param radix
   *          radix of the String representation.
   * @return String representation of this BigInteger in the given
   *         radix.
   * @see Integer#toString
   * @see Character#forDigit
   * @see #BigInteger(java.lang.String, int)
   */
  public final String toString (int radix) {
    if (signum == 0) { return "0"; }
    if ((radix < Character.MIN_RADIX)
      || (radix > Character.MAX_RADIX)) {
      radix = 10;
    }

    // If it's small enough, use smallToString.
    if (mag.length <= SCHOENHAGE_BASE_CONVERSION_THRESHOLD) {
      return smallToString(radix);
    }

    // Otherwise use recursive toString, which requires positive
    // arguments.
    // The results will be concatenated into this StringBuilder
    final StringBuilder sb = new StringBuilder();
    if (signum < 0) {
      toString(this.negate(),sb,radix,0);
      sb.insert(0,'-');
    }
    else {
      toString(this,sb,radix,0);
    }

    return sb.toString();
  }

  private final String smallToString (final int radix) {
    if (signum == 0) { return "0"; }

    // Compute upper bound on number of digit groups and allocate
    // space
    final int maxNumDigitGroups = ((4 * mag.length) + 6) / 7;
    final String digitGroup[] = new String[maxNumDigitGroups];

    // Translate number to string, a digit group at a time
    BigInteger tmp = this.abs();
    int numGroups = 0;
    while (tmp.signum != 0) {
      final BigInteger d = longRadix[radix];

      final MutableBigInteger q = new MutableBigInteger(),
        a = new MutableBigInteger(tmp.mag),
        b = new MutableBigInteger(d.mag);
      final MutableBigInteger r = a.divide(b,q);
      final BigInteger q2 = q.toBigInteger(tmp.signum * d.signum);
      final BigInteger r2 = r.toBigInteger(tmp.signum * d.signum);

      digitGroup[numGroups++] =
        Long.toString(r2.longValue(),radix);
      tmp = q2;
    }

    // Put sign (if any) and first digit group into result buffer
    final StringBuilder buf =
      new StringBuilder((numGroups * digitsPerLong[radix]) + 1);
    if (signum < 0) {
      buf.append('-');
    }
    buf.append(digitGroup[numGroups - 1]);

    // Append remaining digit groups padded with leading zeros
    for (int i = numGroups - 2; i >= 0; i--) {
      // Prepend (any) leading zeros for this digit group
      final int numLeadingZeros =
        digitsPerLong[radix] - digitGroup[i].length();
      if (numLeadingZeros != 0) {
        buf.append(zeros[numLeadingZeros]);
      }
      buf.append(digitGroup[i]);
    }
    return buf.toString();
  }

  /** The threshold value for using Schoenhage recursive base
   * conversion. If the number of ints in the number are larger 
   * than this value, the Schoenhage algorithm will be used. In 
   * practice, it appears that the Schoenhage routine is faster 
   * for any threshold down to 2, and is relatively flat for 
   * thresholds between 2-25, so this choice may be
   * varied within this range for very small effect.
   */
  private static final int SCHOENHAGE_BASE_CONVERSION_THRESHOLD =
    20;
  /**
   * Converts the specified BigInteger to a string and appends to
   * {@code sb}. This implements the recursive Schoenhage
   * algorithm
   * for base conversions.
   * <p>
   * See Knuth, Donald, _The Art of Computer Programming_, Vol. 2,
   * Answers to Exercises (4.4) Question 14.
   *
   * @param u
   *          The number to convert to a string.
   * @param sb
   *          The StringBuilder that will be appended to in place.
   * @param radix
   *          The base to convert to.
   * @param digits
   *          The minimum number of digits to pad to.
   */
  private static void toString (final BigInteger u,
                                final StringBuilder sb,
                                final int radix,
                                final int digits) {
    // If we're smaller than a certain threshold, use the
    // smallToString
    // method, padding with leading zeroes when necessary.
    if (u.mag.length <= SCHOENHAGE_BASE_CONVERSION_THRESHOLD) {
      final String s = u.smallToString(radix);

      // Pad with internal zeros if necessary.
      // Don't pad if we're at the beginning of the string.
      if ((s.length() < digits) && (sb.length() > 0)) {
        for (int i = s.length(); i < digits; i++) {
          sb.append('0');
        }
      }

      sb.append(s);
      return;
    }

    int b, n;
    b = u.bitLength();

    // Calculate a value for n in the equation radix^(2^n) = u
    // and subtract 1 from that value. This is used to find the
    // cache index that contains the best value to divide u.
    n =
      (int) Math.round(
        (Math.log((b * LOG_TWO) / logCache[radix]) / LOG_TWO)
        - 1.0);
    final BigInteger v = getRadixConversionCache(radix,n);
    BigInteger[] results;
    results = u.divideAndRemainder(v);

    final int expectedDigits = 1 << n;

    // Now recursively build the two halves of each number.
    toString(results[0],sb,radix,digits - expectedDigits);
    toString(results[1],sb,radix,expectedDigits);
  }

  /**
   * Returns the value radix^(2^exponent) from the cache.
   * If this value doesn't already exist in the cache, it is
   * added.
   * <p>
   * This could be changed to a more complicated caching method
   * using
   * {@code Future}.
   */
  private static BigInteger getRadixConversionCache (final int radix,
                                                     final int exponent) {
    BigInteger[] cacheLine = powerCache[radix]; // volatile read
    if (exponent < cacheLine.length) {
      return cacheLine[exponent];
    }

    final int oldLength = cacheLine.length;
    cacheLine = Arrays.copyOf(cacheLine,exponent + 1);
    for (int i = oldLength; i <= exponent; i++) {
      cacheLine[i] = cacheLine[i - 1].pow(2);
    }

    BigInteger[][] pc = powerCache; // volatile read again
    if (exponent >= pc[radix].length) {
      pc = pc.clone();
      pc[radix] = cacheLine;
      powerCache = pc; // volatile write, publish
    }
    return cacheLine[exponent];
  }

  /* zero[i] is a string of i consecutive zeros. */
  private static String zeros[] = new String[64];
  static {
    zeros[63] =
      "000000000000000000000000000000000000000000000000000000000000000";
    for (int i = 0; i < 63; i++) {
      zeros[i] = zeros[63].substring(0,i);
    }
  }

  /**
   * Returns the decimal String representation of this BigInteger.
   * The digit-to-character mapping provided by
   * {@code Character.forDigit} is used, and a minus sign is
   * prepended if appropriate. (This representation is compatible
   * with the {@link #BigInteger(String) (String)} constructor,
   * and
   * allows for String concatenation with Java's + operator.)
   *
   * @return decimal String representation of this BigInteger.
   * @see Character#forDigit
   * @see #BigInteger(java.lang.String)
   */
  @Override
  public String toString () {
    return toString(10);
  }

  /**
   * Returns a byte array containing the two's-complement
   * representation of this BigInteger. The byte array will be in
   * <i>big-endian</i> byte-order: the most significant byte is in
   * the zeroth element. The array will contain the minimum number
   * of bytes required to represent this BigInteger, including at
   * least one sign bit, which is {@code (ceil((this.bitLength() +
   * 1)/8))}. (This representation is compatible with the
   * {@link #BigInteger(byte[]) (byte[])} constructor.)
   *
   * @return a byte array containing the two's-complement
   *         representation of
   *         this BigInteger.
   * @see #BigInteger(byte[])
   */
  public byte[] toByteArray () {
    final int byteLen = (bitLength() / 8) + 1;
    final byte[] byteArray = new byte[byteLen];

    for (int i = byteLen - 1, bytesCopied = 4, nextInt =
      0, intIndex = 0; i >= 0; i--) {
      if (bytesCopied == 4) {
        nextInt = getInt(intIndex++);
        bytesCopied = 1;
      }
      else {
        nextInt >>>= 8;
    bytesCopied++;
      }
      byteArray[i] = (byte) nextInt;
    }
    return byteArray;
  }

  /**
   * Converts this BigInteger to an {@code int}. This
   * conversion is analogous to a
   * <i>narrowing primitive conversion</i> from {@code long} to
   * {@code int} as defined in
   * <cite>The Java&trade; Language Specification</cite>:
   * if this BigInteger is too big to fit in an
   * {@code int}, only the low-order 32 bits are returned.
   * Note that this conversion can lose information about the
   * overall magnitude of the BigInteger value as well as return a
   * result with the opposite sign.
   *
   * @return this BigInteger converted to an {@code int}.
   * @see #intValueExact()
   * @jls 5.1.3 Narrowing Primitive Conversion
   */
  @Override
  public int intValue () {
    int result = 0;
    result = getInt(0);
    return result;
  }

  /**
   * Converts this BigInteger to a {@link java.math.BigInteger}.
   */
  public final java.math.BigInteger jmBigIntegerValue () {
    return new java.math.BigInteger(toByteArray());
  }

  /**
   * Converts this BigInteger to a {@code long}. This
   * conversion is analogous to a
   * <i>narrowing primitive conversion</i> from {@code long} to
   * {@code int} as defined in
   * <cite>The Java&trade; Language Specification</cite>:
   * if this BigInteger is too big to fit in a
   * {@code long}, only the low-order 64 bits are returned.
   * Note that this conversion can lose information about the
   * overall magnitude of the BigInteger value as well as return a
   * result with the opposite sign.
   *
   * @return this BigInteger converted to a {@code long}.
   * @see #longValueExact()
   * @jls 5.1.3 Narrowing Primitive Conversion
   */
  @Override
  public long longValue () {
    long result = 0;

    for (int i = 1; i >= 0; i--) {
      result = (result << 32) + (getInt(i) & UNSIGNED_MASK);
    }
    return result;
  }

  /**
   * Converts this BigInteger to a {@code float}. This
   * conversion is similar to the
   * <i>narrowing primitive conversion</i> from {@code double} to
   * {@code float} as defined in
   * <cite>The Java&trade; Language Specification</cite>:
   * if this BigInteger has too great a magnitude
   * to represent as a {@code float}, it will be converted to
   * {@link Float#NEGATIVE_INFINITY} or {@link
   * Float#POSITIVE_INFINITY} as appropriate. Note that even when
   * the return value is finite, this conversion can lose
   * information about the precision of the BigInteger value.
   *
   * @return this BigInteger converted to a {@code float}.
   * @jls 5.1.3 Narrowing Primitive Conversion
   */
  @Override
  public float floatValue () {
    if (signum == 0) { return 0.0f; }

    final int exponent =
      (((mag.length - 1) << 5) + bitLengthForInt(mag[0])) - 1;

    // exponent == floor(log2(abs(this)))
    if (exponent < (Long.SIZE - 1)) {
      return longValue();
    }
    else if (exponent > Float.MAX_EXPONENT) {
      return signum > 0 ? Float.POSITIVE_INFINITY
        : Float.NEGATIVE_INFINITY;
    }

    /*
     * We need the top SIGNIFICAND_WIDTH bits, including the
     * "implicit"
     * one bit. To make rounding easier, we pick out the top
     * SIGNIFICAND_WIDTH + 1 bits, so we have one to help us round
     * up or
     * down. twiceSignifFloor will contain the top
     * SIGNIFICAND_WIDTH + 1
     * bits, and signifFloor the top SIGNIFICAND_WIDTH.
     *
     * It helps to consider the real number signif = abs(this) *
     * 2^(SIGNIFICAND_WIDTH - 1 - exponent).
     */
    final int shift = exponent - Floats.SIGNIFICAND_BITS;

    int twiceSignifFloor;
    // twiceSignifFloor will be ==
    // abs().shiftRight(shift).intValue()
    // We do the shift into an int directly to improve
    // performance.

    final int nBits = shift & 0x1f;
    final int nBits2 = 32 - nBits;

    if (nBits == 0) {
      twiceSignifFloor = mag[0];
    }
    else {
      twiceSignifFloor = mag[0] >>> nBits;
      if (twiceSignifFloor == 0) {
        twiceSignifFloor =
          (mag[0] << nBits2) | (mag[1] >>> nBits);
      }
    }

    int signifFloor = twiceSignifFloor >> 1;
    signifFloor &= Floats.STORED_SIGNIFICAND_MASK; // remove the
    // implied bit

    /*
     * We round up if either the fractional part of signif is
     * strictly
     * greater than 0.5 (which is true if the 0.5 bit is set and
     * any lower
     * bit is set), or if the fractional part of signif is >= 0.5
     * and
     * signifFloor is odd (which is true if both the 0.5 bit and
     * the 1 bit
     * are set). This is equivalent to the desired HALF_EVEN
     * rounding.
     */
    final boolean increment =
      ((twiceSignifFloor
        & 1) != 0) && (((signifFloor & 1) != 0)
          || (abs().getLowestSetBit() < shift));
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
    bits |= signum & Floats.SIGN_MASK;
    return Float.intBitsToFloat(bits);
  }

  /**
   * Converts this BigInteger to a {@code double}. This
   * conversion is similar to the
   * <i>narrowing primitive conversion</i> from {@code double} to
   * {@code float} as defined in
   * <cite>The Java&trade; Language Specification</cite>:
   * if this BigInteger has too great a magnitude
   * to represent as a {@code double}, it will be converted to
   * {@link Double#NEGATIVE_INFINITY} or {@link
   * Double#POSITIVE_INFINITY} as appropriate. Note that even when
   * the return value is finite, this conversion can lose
   * information about the precision of the BigInteger value.
   *
   * @return this BigInteger converted to a {@code double}.
   * @jls 5.1.3 Narrowing Primitive Conversion
   */
  @Override
  public double doubleValue () {
    if (signum == 0) { return 0.0; }

    final int exponent =
      (((mag.length - 1) << 5) + bitLengthForInt(mag[0])) - 1;

    // exponent == floor(log2(abs(this))Double)
    if (exponent < (Long.SIZE - 1)) {
      return longValue();
    }
    else if (exponent > Double.MAX_EXPONENT) {
      return signum > 0 ? Double.POSITIVE_INFINITY
        : Double.NEGATIVE_INFINITY;
    }

    /*
     * We need the top SIGNIFICAND_WIDTH bits, including the
     * "implicit"
     * one bit. To make rounding easier, we pick out the top
     * SIGNIFICAND_WIDTH + 1 bits, so we have one to help us round
     * up or
     * down. twiceSignifFloor will contain the top
     * SIGNIFICAND_WIDTH + 1
     * bits, and signifFloor the top SIGNIFICAND_WIDTH.
     *
     * It helps to consider the real number signif = abs(this) *
     * 2^(SIGNIFICAND_WIDTH - 1 - exponent).
     */
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
      highBits = mag[0];
      lowBits = mag[1];
    }
    else {
      highBits = mag[0] >>> nBits;
    lowBits = (mag[0] << nBits2) | (mag[1] >>> nBits);
    if (highBits == 0) {
      highBits = lowBits;
      lowBits = (mag[1] << nBits2) | (mag[2] >>> nBits);
    }
    }

    twiceSignifFloor =
      ((highBits & UNSIGNED_MASK) << 32) | (lowBits & UNSIGNED_MASK);

    long signifFloor = twiceSignifFloor >> 1;
      signifFloor &= Doubles.STORED_SIGNIFICAND_MASK; // remove the
      // implied bit

      /*
       * We round up if either the fractional part of signif is
       * strictly
       * greater than 0.5 (which is true if the 0.5 bit is set and
       * any lower
       * bit is set), or if the fractional part of signif is >= 0.5
       * and
       * signifFloor is odd (which is true if both the 0.5 bit and
       * the 1 bit
       * are set). This is equivalent to the desired HALF_EVEN
       * rounding.
       */
      final boolean increment =
        ((twiceSignifFloor
          & 1) != 0) && (((signifFloor & 1) != 0)
            || (abs().getLowestSetBit() < shift));
      final long signifRounded =
        increment ? signifFloor + 1 : signifFloor;
      long bits =
        (long) ((exponent
          + Doubles.EXPONENT_BIAS)) << Doubles.STORED_SIGNIFICAND_BITS;
      bits += signifRounded;
      /*
       * If signifRounded == 2^53, we'd need to set all of the
       * significand
       * bits to zero and add 1 to the exponent. This is exactly the
       * behavior
       * we get from just adding signifRounded to bits directly. If
       * the
       * exponent is Double.MAX_EXPONENT, we round up (correctly) to
       * Double.POSITIVE_INFINITY.
       */
      bits |= signum & Doubles.SIGN_MASK;
      return Double.longBitsToDouble(bits);
  }

  // /**
  // * Returns a copy of the input array stripped of any leading
  // zero bytes.
  // */
  // private static int[] stripLeadingZeroInts(final int val[]) {
  // final int vlen = val.length;
  // int keep;
  //
  // // Find first nonzero byte
  // for (keep = 0; (keep < vlen) && (val[keep] == 0); keep++) {
  //
  // }
  // return java.util.Arrays.copyOfRange(val, keep, vlen);
  // }

  /**
   * Returns the input array stripped of any leading zero bytes.
   * Since the source is trusted the copying may be skipped.
   */
  private static int[] trustedStripLeadingZeroInts (final int val[]) {
    final int vlen = val.length;
    int keep;

    // Find first nonzero byte
    for (keep = 0; (keep < vlen) && (val[keep] == 0); keep++) {

    }
    return keep == 0 ? val
      : java.util.Arrays.copyOfRange(val,keep,vlen);
  }

  /**
   * Returns a copy of the input array stripped of any leading
   * zero bytes.
   */
  private static int[] stripLeadingZeroBytes (final byte a[],
                                              final int off,
                                              final int len) {
    final int indexBound = off + len;
    int keep;

    // Find first nonzero byte
    for (keep = off; (keep < indexBound) && (a[keep] == 0);
      keep++) {

    }

    // Allocate new array and copy relevant part of input array
    final int intLength = ((indexBound - keep) + 3) >>> 2;
    final int[] result = new int[intLength];
    int b = indexBound - 1;
    for (int i = intLength - 1; i >= 0; i--) {
      result[i] = a[b--] & 0xff;
      final int bytesRemaining = (b - keep) + 1;
      final int bytesToTransfer = Math.min(3,bytesRemaining);
      for (int j = 8; j <= (bytesToTransfer << 3); j += 8) {
        result[i] |= ((a[b--] & 0xff) << j);
      }
    }
    return result;
  }

  /**
   * Takes an array a representing a negative 2's-complement
   * number and
   * returns the minimal (no leading zero bytes) unsigned whose
   * value is -a.
   */
  private static int[] makePositive (final byte a[],
                                     final int off,
                                     final int len) {
    int keep, k;
    final int indexBound = off + len;

    // Find first non-sign (0xff) byte of input
    for (keep = off; (keep < indexBound) && (a[keep] == -1);
      keep++) {

    }

    /*
     * Allocate output array. If all non-sign bytes are 0x00, we
     * must
     * allocate space for one extra output byte.
     */
    for (k = keep; (k < indexBound) && (a[k] == 0); k++) {

    }

    final int extraByte = (k == indexBound) ? 1 : 0;
    final int intLength =
      (((indexBound - keep) + extraByte) + 3) >>> 2;
    final int result[] = new int[intLength];

    /*
     * Copy one's complement of input into output, leaving extra
     * byte (if it exists) == 0x00
     */
    int b = indexBound - 1;
    for (int i = intLength - 1; i >= 0; i--) {
      result[i] = a[b--] & 0xff;
      int numBytesToTransfer = Math.min(3,(b - keep) + 1);
      if (numBytesToTransfer < 0) {
        numBytesToTransfer = 0;
      }
      for (int j = 8; j <= (8 * numBytesToTransfer); j += 8) {
        result[i] |= ((a[b--] & 0xff) << j);
      }

      // Mask indicates which bits must be complemented
      final int mask = -1 >>> (8 * (3 - numBytesToTransfer));
        result[i] = ~result[i] & mask;
    }

    // Add one to one's complement to generate two's complement
    for (int i = result.length - 1; i >= 0; i--) {
      result[i] = (int) ((result[i] & UNSIGNED_MASK) + 1);
      if (result[i] != 0) {
        break;
      }
    }

    return result;
  }

  /**
   * Takes an array a representing a negative 2's-complement
   * number and
   * returns the minimal (no leading zero ints) unsigned whose
   * value is -a.
   */
  private static int[] makePositive (final int a[]) {
    int keep, j;

    // Find first non-sign (0xffffffff) int of input
    for (keep = 0; (keep < a.length) && (a[keep] == -1); keep++) {

    }

    /*
     * Allocate output array. If all non-sign ints are 0x00, we
     * must
     * allocate space for one extra output int.
     */
    for (j = keep; (j < a.length) && (a[j] == 0); j++) {

    }
    final int extraInt = (j == a.length ? 1 : 0);
    final int result[] = new int[(a.length - keep) + extraInt];

    /*
     * Copy one's complement of input into output, leaving extra
     * int (if it exists) == 0x00
     */
    for (int i = keep; i < a.length; i++) {
      result[(i - keep) + extraInt] = ~a[i];
    }

    // Add one to one's complement to generate two's complement
    for (int i = result.length - 1; ++result[i] == 0; i--) {

    }

    return result;
  }

  /*
   * The following two arrays are used for fast String
   * conversions. Both
   * are indexed by radix. The first is the number of digits of
   * the given
   * radix that can fit in a Java long without "going negative",
   * i.e., the
   * highest integer n such that radix**n < 2**63. The second is
   * the
   * "long radix" that tears each number into "long digits", each
   * of which
   * consists of the number of digits in the corresponding element
   * in
   * digitsPerLong (longRadix[i] = i**digitPerLong[i]). Both
   * arrays have
   * nonsense values in their 0 and 1 elements, as radixes 0 and 1
   * are not
   * used.
   */
  private static int digitsPerLong[] =
  { 0, 0, 62, 39, 31, 27, 24, 22, 20, 19, 18, 18, 17, 17, 16,
    16, 15, 15, 15, 14, 14, 14, 14, 13, 13, 13, 13, 13, 13, 12,
    12, 12, 12, 12, 12, 12, 12 };

  private static BigInteger longRadix[] =
  { null, null, valueOf(0x4000000000000000L),
    valueOf(0x383d9170b85ff80bL), valueOf(0x4000000000000000L),
    valueOf(0x6765c793fa10079dL), valueOf(0x41c21cb8e1000000L),
    valueOf(0x3642798750226111L), valueOf(0x1000000000000000L),
    valueOf(0x12bf307ae81ffd59L), valueOf(0xde0b6b3a7640000L),
    valueOf(0x4d28cb56c33fa539L), valueOf(0x1eca170c00000000L),
    valueOf(0x780c7372621bd74dL), valueOf(0x1e39a5057d810000L),
    valueOf(0x5b27ac993df97701L), valueOf(0x1000000000000000L),
    valueOf(0x27b95e997e21d9f1L), valueOf(0x5da0e1e53c5c8000L),
    valueOf(0xb16a458ef403f19L), valueOf(0x16bcc41e90000000L),
    valueOf(0x2d04b7fdd9c0ef49L), valueOf(0x5658597bcaa24000L),
    valueOf(0x6feb266931a75b7L), valueOf(0xc29e98000000000L),
    valueOf(0x14adf4b7320334b9L), valueOf(0x226ed36478bfa000L),
    valueOf(0x383d9170b85ff80bL), valueOf(0x5a3c23e39c000000L),
    valueOf(0x4e900abb53e6b71L), valueOf(0x7600ec618141000L),
    valueOf(0xaee5720ee830681L), valueOf(0x1000000000000000L),
    valueOf(0x172588ad4f5f0981L), valueOf(0x211e44f7d02c1000L),
    valueOf(0x2ee56725f06e5c71L),
    valueOf(0x41c21cb8e1000000L) };

  /*
   * These two arrays are the integer analogue of above.
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
   * These routines provide access to the two's complement
   * representation
   * of BigIntegers.
   */

  /**
   * Returns the length of the two's complement representation in
   * ints,
   * including space for at least one sign bit.
   */
  private int intLength () {
    return (bitLength() >>> 5) + 1;
  }

  /* Returns an int of sign bits */
  private int signInt () {
    return signum < 0 ? -1 : 0;
  }

  /**
   * Returns the specified int of the little-endian two's
   * complement
   * representation (int 0 is the least significant). The int
   * number can
   * be arbitrarily high (values are logically preceded by
   * infinitely many
   * sign ints).
   */
  private int getInt (final int n) {
    if (n < 0) { return 0; }
    if (n >= mag.length) { return signInt(); }

    final int magInt = mag[mag.length - n - 1];

    return (signum >= 0 ? magInt
      : (n <= firstNonzeroIntNum() ? -magInt : ~magInt));
  }

  /**
   * Returns the index of the int that contains the first nonzero
   * int in the
   * little-endian binary representation of the magnitude (int 0
   * is the
   * least significant). If the magnitude is zero, return value is
   * undefined.
   *
   * <p>
   * Note: never used for a BigInteger with a magnitude of zero.
   * 
   * @see #getInt.
   */
  private int firstNonzeroIntNum () {
    int fn = firstNonzeroIntNumPlusTwo - 2;
    if (fn == -2) { // firstNonzeroIntNum not initialized yet
      // Search for the first nonzero int
      int i;
      final int mlen = mag.length;
      for (i = mlen - 1; (i >= 0) && (mag[i] == 0); i--) {

      }
      fn = mlen - i - 1;
      firstNonzeroIntNumPlusTwo = fn + 2; // offset by two to
      // initialize
    }
    return fn;
  }

  /** use serialVersionUID from JDK 1.1. for interoperability */
  private static final long serialVersionUID =
    -8287574255936472291L;

  /**
   * Serializable fields for BigInteger.
   *
   * @serialField signum
   *                int
   *                signum of this BigInteger
   * @serialField magnitude
   *                byte[]
   *                magnitude array of this BigInteger
   * @serialField bitCount
   *                int
   *                appears in the serialized form for backward
   *                compatibility
   * @serialField bitLength
   *                int
   *                appears in the serialized form for backward
   *                compatibility
   * @serialField firstNonzeroByteNum
   *                int
   *                appears in the serialized form for backward
   *                compatibility
   * @serialField lowestSetBit
   *                int
   *                appears in the serialized form for backward
   *                compatibility
   */
  private static final ObjectStreamField[] serialPersistentFields =
  { new ObjectStreamField("signum",Integer.TYPE),
    new ObjectStreamField("magnitude",byte[].class),
    new ObjectStreamField("bitCount",Integer.TYPE),
    new ObjectStreamField("bitLength",Integer.TYPE),
    new ObjectStreamField("firstNonzeroByteNum",Integer.TYPE),
    new ObjectStreamField("lowestSetBit",Integer.TYPE) };

  /**
   * Converts this {@code BigInteger} to a {@code long}, checking
   * for lost information. If the value of this {@code BigInteger}
   * is out of the range of the {@code long} type, then an
   * {@code ArithmeticException} is thrown.
   *
   * @return this {@code BigInteger} converted to a {@code long}.
   * @throws ArithmeticException
   *           if the value of {@code this} will
   *           not exactly fit in a {@code long}.
   * @see BigInteger#longValue
   * @since 1.8
   */
  public long longValueExact () {
    if ((mag.length <= 2) && (bitLength() <= 63)) {
      return longValue();
    }
    throw new ArithmeticException("BigInteger out of long range");
  }

  /**
   * Converts this {@code BigInteger} to an {@code int}, checking
   * for lost information. If the value of this {@code BigInteger}
   * is out of the range of the {@code int} type, then an
   * {@code ArithmeticException} is thrown.
   *
   * @return this {@code BigInteger} converted to an {@code int}.
   * @throws ArithmeticException
   *           if the value of {@code this} will
   *           not exactly fit in an {@code int}.
   * @see BigInteger#intValue
   * @since 1.8
   */
  public int intValueExact () {
    if ((mag.length <= 1) && (bitLength() <= 31)) {
      return intValue();
    }
    throw new ArithmeticException("BigInteger out of int range");
  }

  /**
   * Converts this {@code BigInteger} to a {@code short}, checking
   * for lost information. If the value of this {@code BigInteger}
   * is out of the range of the {@code short} type, then an
   * {@code ArithmeticException} is thrown.
   *
   * @return this {@code BigInteger} converted to a {@code short}.
   * @throws ArithmeticException
   *           if the value of {@code this} will
   *           not exactly fit in a {@code short}.
   * @see BigInteger#shortValue
   * @since 1.8
   */
  public short shortValueExact () {
    if ((mag.length <= 1) && (bitLength() <= 31)) {
      final int value = intValue();
      if ((value >= Short.MIN_VALUE)
        && (value <= Short.MAX_VALUE)) {
        return shortValue();
      }
    }
    throw new ArithmeticException(
      "BigInteger out of short range");
  }

  /**
   * Converts this {@code BigInteger} to a {@code byte}, checking
   * for lost information. If the value of this {@code BigInteger}
   * is out of the range of the {@code byte} type, then an
   * {@code ArithmeticException} is thrown.
   *
   * @return this {@code BigInteger} converted to a {@code byte}.
   * @throws ArithmeticException
   *           if the value of {@code this} will
   *           not exactly fit in a {@code byte}.
   * @see BigInteger#byteValue
   * @since 1.8
   */
  public byte byteValueExact () {
    if ((mag.length <= 1) && (bitLength() <= 31)) {
      final int value = intValue();
      if ((value >= Byte.MIN_VALUE)
        && (value <= Byte.MAX_VALUE)) {
        return byteValue();
      }
    }
    throw new ArithmeticException("BigInteger out of byte range");
  }
}
