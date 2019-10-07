package xfp.java.polynomial;

import xfp.java.numbers.RationalFloat;

/** Exact {@link Axpy} using {@link RationalFloat} for the exact 
 * values.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-07
 */

@SuppressWarnings("unchecked")
public final class RationalFloatAxpy implements Axpy<RationalFloat> {

  @Override
  public final boolean isExact () { return true; }

  @Override
  public final RationalFloat axpy (final double a,
                                   final double x,
                                   final double y) {
    return axpy(RationalFloat.valueOf(a),x,y); }
  //return RationalFloat.axpy(a,x,y); }

  @Override
  public final RationalFloat[] axpy (final double[] a,
                                     final double[] x,
                                     final double[] y) {
    return RationalFloat.axpy(a,x,y); }

  @Override
  public final RationalFloat axpy (final RationalFloat a,
                                   final double x,
                                   final double y) {
    return RationalFloat.axpy(x,a,y); }

  @Override
  public final RationalFloat[] axpy (final RationalFloat[] a,
                                     final double[] x,
                                     final double[] y) {
    return RationalFloat.axpy(x,a,y); }

  @Override
  public final double daxpy (final double a,
                             final double x,
                             final double y) {
    return RationalFloat.axpy(a,x,y).doubleValue(); }

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

  public static final RationalFloatAxpy make () {
    return new RationalFloatAxpy(); }

}
//--------------------------------------------------------------

