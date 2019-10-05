package xfp.java.polynomial;

import xfp.java.exceptions.Exceptions;

/** Calculator for 2nd order polynomials 
 * <code>a0 + a1*x + a2*x<sup>2</sup>*x+y</code> 
 * over <code>double</code>
 * and <code>double[]</code>, which returns instances of 
 * <code>T</code> representing exact values (if possible)
 * and <code>double</code> approximations.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-04
 */

@SuppressWarnings("unchecked")
public interface Quadratic<T> {
  
  // TODO: better default methods if we assume something about T.

  //--------------------------------------------------------------
  /** Can this Quadratic calculator return a representation of the
   * exact value?
   */

  default boolean isExact () { return false; }

  //--------------------------------------------------------------
  /** Return a representation of the exact value of
   * <code>a0 + a1*x + a2*x<sup>2</sup>*x+y</code>.
   * <p>
   * Optional operation.
   */

  default T value (final double x) {
    throw Exceptions.unsupportedOperation(this,"value",x); }

  //--------------------------------------------------------------
  /** Convenience method for elementwise 
   * {@link #value(double)}.
   */

  default T[] value (final double[] x) {
    throw Exceptions.unsupportedOperation(this,"value",x); }

  //--------------------------------------------------------------
  /** Return the exact value of <code>a*x+y</code> rounded
   * to nearest, even ties. (Note that this is just 
   * {@link Math#fma(double, double, double)}.)
   */

  double doubleValue (final double x);

  /** Convenience method for elementwise 
   * {@link #doubleValue(double)}.
   */

  default double[] doubleValue (final double[] x) {
    final int n = x.length;
    final double[] z = new double[n];
    for (int i=0;i<n;i++) { z[i] = doubleValue(x[i]); }
    return z; }

  //--------------------------------------------------------------
  /** Convert the exact representation to <code>double</code>. 
   * <p>
   * TODO: guarantee even round-to-nearest?
   */

  default double doubleValue (final T z) {
    throw Exceptions.unsupportedOperation(this,"doubleValue",z); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

