package xfp.java.numbers;

/** Utilities for <code>float</code>, <code>float[]</code>.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-01-29
 */
public final class Floats  {

  //--------------------------------------------------------------
  // private
  //--------------------------------------------------------------

  //  private static final int SIGN_BITS = 1;
  private static final int EXPONENT_BITS = 8;
  private static final int SIGNIFICAND_BITS = 23;

  //  private static final long SIGN_MASK =
  //    1L << (EXPONENT_BITS + SIGNIFICAND_BITS);

  //  private static final long EXPONENT_MASK =
  //    ((1L << EXPONENT_BITS) - 1L) << SIGNIFICAND_BITS;

  public static final int SIGNIFICAND_MASK =
    (1 << SIGNIFICAND_BITS) - 1;

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

  // TODO: is this correct? quick change from Doubles.

  public static final float makeFloat (final int s,
                                       final int e,
                                       final int t) {
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

    final int ss = s << (EXPONENT_BITS + SIGNIFICAND_BITS);
    final int se = e << SIGNIFICAND_BITS;

    assert (0 == (ss & se & t));
    return Float.intBitsToFloat(ss | se | t); }

  //--------------------------------------------------------------
  // disable constructor
  //--------------------------------------------------------------

  private Floats () {
    throw new UnsupportedOperationException(
      "can't instantiate " + getClass()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

