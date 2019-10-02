package xfp.java.polynomial;

import xfp.java.exceptions.Exceptions;

/** Calculator for <code>a*x+y</code> over <code>double</code>
 * and <code>double[]</code>, which returns instances of 
 * <code>T</code> representing exact values (if possible)
 * and <code>double</code> approximations.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-02
 */

@SuppressWarnings("unchecked")
public interface Axpy<T> {

  //--------------------------------------------------------------
  /** Can this Axpy calculator return a representation of the
   * exact value?
   */

  default boolean isExact () { return false; }

  //--------------------------------------------------------------
  /** Return a representation of the exact value of
   * <code>a*x+y</code>.
   * <p>
   * Optional operation.
   */

  default T axpy (final double a,
                  final double x,
                  final double y) {
    throw Exceptions.unsupportedOperation(this,"axpy",a,x,y); }

  /** Return a representation of the exact value of
   * <code>a*x+y</code>. 
   * Needed for Horner evaluation of polynomials.
   * <p>
   * Optional operation.
   */

  default T axpy (final T a,
                  final double x,
                  final double y) {
    throw Exceptions.unsupportedOperation(this,"axpy",a,x,y); }

  //--------------------------------------------------------------
  /** Return a representation of the exact value of
   * <code>a*x+y</code>.
   * <p>
   * Optional operation.
   */

  default T[] axpy (final double[] a,
                    final double[] x,
                    final double[] y) {
    throw Exceptions.unsupportedOperation(this,"axpy",a,x,y); }

  /** Return a representation of the exact value of
   * <code>a*x+y</code>. 
   * Needed for Horner evaluation of polynomials.
   * <p>
   * Optional operation.
   */

  default T[] axpy (final T[] a,
                    final double[] x,
                    final double[] y) {
    throw Exceptions.unsupportedOperation(this,"axpy",a,x,y); }

  //--------------------------------------------------------------
  /** Return the exact value of <code>a*x+y</code> rounded
   * to nearest, even ties. (Note that this is just 
   * {@link Math#fma(double, double, double)}.)
   */

  default double daxpy (final double a,
                        final double x,
                        final double y) {
    return Math.fma(a,x,y); }

  /** Return the exact value of <code>a*x+y</code> rounded
   * to nearest, even ties. (Note that this is just 
   * {@link Math#fma(double, double, double)}.)
   */

  default double[] daxpy (final double[] a,
                          final double[] x,
                          final double[] y) {
    final int n = a.length;
    assert n==x.length;
    assert n==y.length;
    final double[] z = new double[n];
    for (int i=0;i<n;i++) { z[i] = Math.fma(a[i],x[i],y[i]); }
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

