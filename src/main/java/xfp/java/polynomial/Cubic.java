package xfp.java.polynomial;

import xfp.java.exceptions.Exceptions;

/** Evaluators for cubic polynomials specified
 * with <code>double</code> coefficients.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-01
 */

@SuppressWarnings("unchecked")
public interface Cubic<T extends Cubic> {

  //--------------------------------------------------------------
  // start with only immediate needs
  //--------------------------------------------------------------
  /** An <em>exact</em> accumulator returns values equivalent
   * to half-even rounding to nearest of infinite precision
   * calculation.
   */
  default boolean isExact () {
    throw Exceptions.unsupportedOperation(this,"isExact"); }

  /** Intermediate results will never silently <em>overflow</em>
   * to an absorbing 'infinity' state. They may fail due to
   * implementation bounds on memory, etc.
   */
  default boolean noOverflow () {
    throw Exceptions.unsupportedOperation(this,"noOverflow"); }

  /** General polynomials provide this. */
  default Object value (final double z) {
    throw
    Exceptions.unsupportedOperation(this,"value",z); }

  /** Half-even rounding to nearest <code>double</code>. */
  default double doubleValue (final double z) {
    throw
    Exceptions.unsupportedOperation(this,"doubleValue",z); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
