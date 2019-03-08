package xfp.java.accumulators;

import xfp.java.exceptions.Exceptions;

/** Convenience interface for mutable, <em>non-</em>thread safe
 * objects used for general kinds of reductions of data sets,
 * typically online. Supports both updating (adding items),
 *  and downdating (removing items) from the accumulation.
 * <p>
 * All methods are optional.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-07
 */
public interface Accumulator {

  //--------------------------------------------------------------
  // start with only immediate needs

  // TODO: return Accumulator to handle immutable implementation?
  public default void clear () {
    throw Exceptions.unsupportedOperation(this,"clear"); }

  public default double doubleValue () {
    throw Exceptions.unsupportedOperation(this,"doubleValue"); }

  // TODO: return Accumulator to handle immutable implementation?
  public default void add (final double z) {
    throw Exceptions.unsupportedOperation(this,"add",z); }

  // TODO: return Accumulator to handle immutable implementation?
  public default void addAll (final double[] z)  {
    throw Exceptions.unsupportedOperation(this,"add",z); }

}
