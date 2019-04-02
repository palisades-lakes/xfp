package xfp.java.accumulators;

import xfp.java.exceptions.Exceptions;

/** Convenience interface for mutable, <em>non-</em>thread safe
 * objects used for general kinds of reductions of data sets,
 * typically online. 
 * <p>
 * All methods are optional.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-02
 */

@SuppressWarnings("unchecked")
public interface Accumulator<T extends Accumulator> {

  //--------------------------------------------------------------
  // start with only immediate needs
  //--------------------------------------------------------------

  // TODO: numerical error bounds for inexact accumulators,
  // or some other predicate for testing that results are as 
  // as accurate as expected.
  
  /** An <em>exact</em> accumulator returns values equivalent
   * to half-even rounding to nearest of infinite precision
   * calculation.
   */
  public default boolean isExact () {
    throw Exceptions.unsupportedOperation(this,"isExact"); }
  
  /** Half-even rounding to nearest <code>double</code>. */
  public default double doubleValue () {
    throw 
    Exceptions.unsupportedOperation(this,"doubleValue"); }

  /** Half-even rounding to nearest <code>float</code>. */
  public default float floatValue () {
    throw 
    Exceptions.unsupportedOperation(this,"floatValue"); }

  public default T clear () {
    throw 
    Exceptions.unsupportedOperation(this,"clear"); }

  public default T add (final double z) {
    throw 
    Exceptions.unsupportedOperation(this,"add",z); }

  public default T addAll (final double[] z)  {
    for (final double zi : z) { add(zi); }
    return (T) this; }

  /** add <code>z<sup>2</sup></code> to the accumulator. */
  
  public default T add2 (final double z) {
    throw 
    Exceptions.unsupportedOperation(this,"add2",z); }

  /** add all <code>z<sub>i</sub><sup>2</sup></code> to the 
   * accumulator. 
   * */
  public default T add2All (final double[] z)  {
    for (final double zi : z) { add2(zi); }
    return (T) this; }

  public default T addProduct (final double z0,
                               final double z1) {
    throw 
    Exceptions.unsupportedOperation(this,"addProduct",z0,z1); }

  public default T addProducts (final double[] z0,
                                final double[] z1)  {
    final int n = z0.length;
    assert n == z1.length;
    for (int i=0;i<n;i++) { addProduct(z0[i],z1[i]); }
    return (T) this; }

  //--------------------------------------------------------------
}
