package xfp.java.polynomial;

import xfp.java.numbers.BigFloat;

/** Cubic polynomial evaluator using Horner's algorithm
 * with a {@link BigFloat} accumulator.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-01
 */
public final class BigFloatCubic
extends ExactCubic<BigFloatCubic> {

  //--------------------------------------------------------------

  @Override
  public final boolean noOverflow () { return true; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private BigFloatCubic () { super(); }

  public static final BigFloatCubic make () {
    return new BigFloatCubic(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
