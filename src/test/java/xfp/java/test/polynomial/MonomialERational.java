package xfp.java.test.polynomial;

import static com.upokecenter.numbers.ERational.FromDouble;

import com.upokecenter.numbers.ERational;

import xfp.java.linear.Dn;
import xfp.java.polynomial.Polynomial;

/** Approximate cubic {@link Polynomial} 
 * using <code>ERational</code>.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-08
 */

@SuppressWarnings("unchecked")
public final class MonomialERational 
implements Polynomial<ERational> {

  private final ERational[] _a;

  //--------------------------------------------------------------

  @Override
  public final boolean isExact () { return true; }

  //--------------------------------------------------------------

  private static final ERational fma (final double a,
                                   final ERational b,
                                   final ERational c) {
    return FromDouble(a).Multiply(b).Add(c); }

  @Override
  public final ERational value (final double x) {
    int i = _a.length-1;
    if (0>i) { return ERational.Zero; }
    ERational tmp = _a[i--];
    while (0<=i) { tmp = fma(x,tmp,_a[i--]); }
    return tmp; }

  //--------------------------------------------------------------

  @Override
  public final double doubleValue (final double x) {
    return value(x).ToDouble(); }

  //--------------------------------------------------------------
  /** Unsafe, retains reference to <code>a</code>. */

  private MonomialERational (final double[] a) {
    final int n = a.length;
    assert 0.0!=a[a.length-1];
    final ERational[] b = new ERational[n];
    for (int i=0;i<n;i++) { b[i] = ERational.FromDouble(a[i]); }
    _a=b; }

  public static final MonomialERational make (final double[] a) {
    return new MonomialERational(Dn.stripTrailingZeros(a)); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

