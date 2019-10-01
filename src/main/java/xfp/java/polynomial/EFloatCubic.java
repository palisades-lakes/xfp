package xfp.java.polynomial;

import com.upokecenter.numbers.EFloat;

/** Cubic polynomial evaluator using Horner's algorithm
 * with an {@link EFloat} accumulator.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-01
 */
public final class EFloatCubic
extends ExactCubic<EFloatCubic> {

   //--------------------------------------------------------------
  // Cubic methods
  //--------------------------------------------------------------

  @Override
  public final boolean noOverflow () { return true; }

   //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private EFloatCubic () { super(); }

  public static final EFloatCubic make () {
    return new EFloatCubic(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
