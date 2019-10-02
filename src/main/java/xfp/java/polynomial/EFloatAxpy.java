package xfp.java.polynomial;

import com.upokecenter.numbers.EFloat;

/** Exact {@link Axpy} using {@link EFloat} for the exact 
 * values.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-02
 */

@SuppressWarnings("unchecked")
public final class EFloatAxpy implements Axpy<EFloat> {

  @Override
  public final boolean isExact () { return true; }

  @Override
  public final EFloat axpy (final double a,
                            final double x,
                            final double y) {
    final EFloat aa = EFloat.FromDouble(a);
    final EFloat xx = EFloat.FromDouble(x);
    final EFloat yy = EFloat.FromDouble(y);
    return aa.MultiplyAndAdd(xx,yy); }

  @Override
  public final EFloat[] axpy (final double[] a,
                              final double[] x,
                              final double[] y) {
    final int n = a.length;
    assert n==x.length;
    assert n==y.length;
    final EFloat[] z = new EFloat[n];
    for (int i=0;i<n;i++) { z[i] = axpy(a[i],x[i],y[i]); }
    return z; }

  @Override
  public final EFloat axpy (final EFloat a,
                            final double x,
                            final double y) {
    final EFloat xx = EFloat.FromDouble(x);
    final EFloat yy = EFloat.FromDouble(y);
    return a.MultiplyAndAdd(xx,yy); }

  @Override
  public final EFloat[] axpy (final EFloat[] a,
                              final double[] x,
                              final double[] y) {
    final int n = a.length;
    assert n==x.length;
    assert n==y.length;
    final EFloat[] z = new EFloat[n];
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

}
//--------------------------------------------------------------

