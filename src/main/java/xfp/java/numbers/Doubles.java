package xfp.java.numbers;

/** Utilities for <code>double</code>, <code>double[]</code>.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-01-29
 */
public final class Doubles  {

  //--------------------------------------------------------------
  // private
  //--------------------------------------------------------------

  //  private static final int SIGN_BITS = 1;
  private static final int EXPONENT_BITS = 11;
  private static final int SIGNIFICAND_BITS = 52;

  //  private static final long SIGN_MASK =
  //    1L << (EXPONENT_BITS + SIGNIFICAND_BITS);

  //  private static final long EXPONENT_MASK =
  //    ((1L << EXPONENT_BITS) - 1L) << SIGNIFICAND_BITS;

  public static final long SIGNIFICAND_MASK =
    (1L << SIGNIFICAND_BITS) - 1L;

  //  private static final int EXPONENT_BIAS =
  //    (1 << (EXPONENT_BITS - 1)) - 1;

  public static final int MAXIMUM_BIASED_EXPONENT =
    (1 << EXPONENT_BITS) - 1;

  //  private static final int MAXIMUM_EXPONENT =
  //    EXPONENT_BIAS;

  //  private static final int MINIMUM_NORMAL_EXPONENT =
  //    1 - MAXIMUM_EXPONENT;

  //  private static final int MINIMUM_SUBNORMAL_EXPONENT =
  //    MINIMUM_NORMAL_EXPONENT - SIGNIFICAND_BITS;

  public static final double makeDouble (final int s,
                                         final int e,
                                         final long t) {
    assert ((0 == s) || (1 ==s)) : "Invalid sign bit:" + s;
    assert (0 <= e) :
      "Negative exponent:" + Integer.toHexString(e);
    assert (e <= MAXIMUM_BIASED_EXPONENT) :
      "Exponent too large:" + Integer.toHexString(e) +
      ">" + Integer.toHexString(MAXIMUM_BIASED_EXPONENT);
    assert (0 <= t) :
      "Negative significand:" + Long.toHexString(t);
    assert (t <= SIGNIFICAND_MASK) :
      "Significand too large:" + Long.toHexString(t) +
      ">" + Long.toHexString(SIGNIFICAND_MASK);

    final long ss = ((long) s) << (EXPONENT_BITS + SIGNIFICAND_BITS);
    final long se = ((long) e) << SIGNIFICAND_BITS;

    assert (0L == (ss & se & t));
    return Double.longBitsToDouble(ss | se | t); }

  //--------------------------------------------------------------
  // disable constructor
  //--------------------------------------------------------------

  private Doubles () {
    throw new UnsupportedOperationException(
      "can't instantiate " + getClass()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

