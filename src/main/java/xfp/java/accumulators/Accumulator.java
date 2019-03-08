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

  public default double doubleValue () {
    throw Exceptions.unsupportedOperation(this,"doubleValue"); }

  public default Accumulator clear () {
    throw Exceptions.unsupportedOperation(this,"clear"); }

  public default Accumulator add (final double z) {
    throw Exceptions.unsupportedOperation(this,"add",z); }

  public default Accumulator addAll (final double[] z)  {
    for (final double zi : z) { add(zi); }
    return this; }

  public default Accumulator addProduct (final double z0,
                                         final double z1) {
    throw Exceptions.unsupportedOperation(this,"addProduct",z0,z1); }

  
  public default Accumulator addProducts (final double[] z0,
                                        final double[] z1)  {
    final int n = z0.length;
    assert n == z1.length;
    for (int i=0;i<n;i++) { addProduct(z0[i],z1[i]); }
    return this; }

  //--------------------------------------------------------------
}
