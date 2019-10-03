package xfp.java.polynomial;

import xfp.java.numbers.BigFloat;

/** Exact {@link Axpy} using {@link BigFloat} for the exact 
 * values.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-03
 */

@SuppressWarnings("unchecked")
public final class BigFloatAxpy implements Axpy<BigFloat> {

  @Override
  public final boolean isExact () { return true; }

  @Override
  public final BigFloat axpy (final double a,
                              final double x,
                              final double y) {
    return axpy(BigFloat.valueOf(a),x,y); }
  //return BigFloat.axpy(a,x,y); }

  @Override
  public final BigFloat[] axpy (final double[] a,
                                final double[] x,
                                final double[] y) {
    return BigFloat.axpy(a,x,y); }

  @Override
  public final BigFloat axpy (final BigFloat a,
                              final double x,
                              final double y) {
    return BigFloat.axpy(a,x,y); }

  @Override
  public final BigFloat[] axpy (final BigFloat[] a,
                                final double[] x,
                                final double[] y) {
    return BigFloat.axpy(a,x,y); }

  @Override
  public final double daxpy (final double a,
                             final double x,
                             final double y) {
    return BigFloat.axpy(a,x,y).doubleValue(); }

  @Override
  public final double[] daxpy (final double[] a,
                               final double[] x,
                               final double[] y) {    
    final int n = a.length;
    assert n==x.length;
    assert n==y.length;
    final double[] z = new double[n];
    for (int i=0;i<n;i++) { z[i] = daxpy(a[i],x[i],y[i]); }
    return z; }

  public static final BigFloatAxpy make () {
    return new BigFloatAxpy(); }

}
//--------------------------------------------------------------

