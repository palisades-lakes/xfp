package xfp.java.polynomial;

import static com.upokecenter.numbers.ERational.FromDouble;

import com.upokecenter.numbers.ERational;

/** Exact {@link Polynomial} using {@link ERational}.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-07
 */

@SuppressWarnings("unchecked")
public final class ERationalQuadratic 
implements Polynomial<ERational> {

  private final ERational _a0;
  private final ERational _a1;
  private final ERational _a2;

  @Override
  public final boolean isExact () { return true; }

  private static final ERational fma (final double a,
                                      final ERational b,
                                      final ERational c) {
    return FromDouble(a).Multiply(b).Add(c); }

  @Override
  public final ERational value (final double x) {
    return fma(x,fma(x,_a2,_a1),_a0); }

  @Override
  public final ERational[] value (final double[] x) {
    final int n = x.length;
    final ERational[] z = new ERational[n];
    for (int i=0;i<n;i++) {
      z[i] = value(x[i]); }
    return z; }

  @Override
  public final double doubleValue (final double x) {
    return value(x).ToDouble(); }

  //--------------------------------------------------------------

  private ERationalQuadratic (final double a0,
                              final double a1, 
                              final double a2) {
    _a0=FromDouble(a0); _a1=FromDouble(a1); _a2=FromDouble(a2); }

  public static final ERationalQuadratic make (final double a0,
                                               final double a1, 
                                               final double a2) {
    return new ERationalQuadratic(a0,a1,a2); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
