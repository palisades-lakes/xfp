package xfp.java.polynomial;

import static xfp.java.numbers.BigFloat.axpy;

import xfp.java.numbers.BigFloat;

/** Exact cubic {@link Polynomial} using {@link BigFloat}.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-05
 */

@SuppressWarnings("unchecked")
public final class BigFloatCubic 
implements Polynomial<BigFloat> {

  private final double _a0;
  private final double _a1;
  private final double _a2;
  private final double _a3;

  @Override
  public final boolean isExact () { return true; }

  @Override
  public final BigFloat value (final double x) {
    return axpy(x,axpy(x,axpy(x,_a3,_a2),_a1),_a0); }

  @Override
  public final BigFloat[] value (final double[] x) {
    final int n = x.length;
    final BigFloat[] z = new BigFloat[n];
    for (int i=0;i<n;i++) {
      z[i] = value(x[i]); }
    return z; }

  @Override
  public final double doubleValue (final double x) {
    return value(x).doubleValue(); }

  //--------------------------------------------------------------

  private BigFloatCubic (final double a0,
                         final double a1, 
                         final double a2,
                         final double a3) {
    _a0=a0; _a1=a1; _a2=a2; _a3 = a3;}

  public static final BigFloatCubic make (final double a0,
                                          final double a1, 
                                          final double a2,
                                          final double a3) {
    return new BigFloatCubic(a0,a1,a2,a3); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

