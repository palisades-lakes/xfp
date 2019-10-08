package xfp.java.polynomial;

import static com.upokecenter.numbers.EFloat.FromDouble;

import com.upokecenter.numbers.EFloat;

import xfp.java.linear.Dn;

/** Approximate cubic {@link Polynomial} 
 * using <code>EFloat</code>.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-08
 */

@SuppressWarnings("unchecked")
public final class MonomialEFloat 
implements Polynomial<EFloat> {

  private final EFloat[] _a;

  //--------------------------------------------------------------

  @Override
  public final boolean isExact () { return true; }

  //--------------------------------------------------------------

  private static final EFloat fma (final double a,
                                   final EFloat b,
                                   final EFloat c) {
    return FromDouble(a).MultiplyAndAdd(b,c); }

  @Override
  public final EFloat value (final double x) {
    int i = _a.length-1;
    if (0>i) { return EFloat.Zero; }
    EFloat tmp = _a[i--];
    while (0<=i) { tmp = fma(x,tmp,_a[i--]); }
    return tmp; }

  //--------------------------------------------------------------

  @Override
  public final double doubleValue (final double x) {
    return value(x).ToDouble(); }

  //--------------------------------------------------------------
  /** Unsafe, retains reference to <code>a</code>. */

  private MonomialEFloat (final double[] a) {
    final int n = a.length;
    assert 0.0!=a[a.length-1];
    final EFloat[] b = new EFloat[n];
    for (int i=0;i<n;i++) { b[i] = EFloat.FromDouble(a[i]); }
    _a=b; }

  public static final MonomialEFloat make (final double[] a) {
    return new MonomialEFloat(Dn.stripTrailingZeros(a)); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

