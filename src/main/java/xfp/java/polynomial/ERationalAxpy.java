package xfp.java.polynomial;

import com.upokecenter.numbers.ERational;

/** Exact {@link Axpy} using {@link ERational} for the exact 
 * values.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-07
 */

@SuppressWarnings("unchecked")
public final class ERationalAxpy implements Axpy<ERational> {

  @Override
  public final boolean isExact () { return true; }

  @Override
  public final ERational axpy (final double a,
                               final double x,
                               final double y) {
    final ERational aa = ERational.FromDouble(a);
    final ERational xx = ERational.FromDouble(x);
    final ERational yy = ERational.FromDouble(y);
    return aa.Multiply(xx).Add(yy); }

  @Override
  public final ERational[] axpy (final double[] a,
                                 final double[] x,
                                 final double[] y) {
    final int n = a.length;
    assert n==x.length;
    assert n==y.length;
    final ERational[] z = new ERational[n];
    for (int i=0;i<n;i++) { z[i] = axpy(a[i],x[i],y[i]); }
    return z; }

  @Override
  public final ERational axpy (final ERational a,
                               final double x,
                               final double y) {
    final ERational xx = ERational.FromDouble(x);
    final ERational yy = ERational.FromDouble(y);
    return a.Multiply(xx).Add(yy); }

  @Override
  public final ERational[] axpy (final ERational[] a,
                                 final double[] x,
                                 final double[] y) {
    final int n = a.length;
    assert n==x.length;
    assert n==y.length;
    final ERational[] z = new ERational[n];
    for (int i=0;i<n;i++) { z[i] = axpy(a[i],x[i],y[i]); }
    return z; }

  @Override
  public final double daxpy (final double a,
                             final double x,
                             final double y) {
    return axpy(a,x,y).ToDouble(); }

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

  public static final ERationalAxpy make () {
    return new ERationalAxpy(); }

  //--------------------------------------------------------------
}
