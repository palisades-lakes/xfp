package xfp.java.polynomial;

import xfp.java.linear.Dn;
import xfp.java.numbers.BigFloat;

/** Approximate cubic {@link Polynomial} 
 * using <code>BigFloat</code>.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-11
 */

@SuppressWarnings("unchecked")
public final class MonomialBigFloat 
implements Polynomial<BigFloat> {

  private final BigFloat[] _a;

  //--------------------------------------------------------------

  @Override
  public final boolean isExact () { return true; }

  //--------------------------------------------------------------

  private static final BigFloat fma (final double a,
                                     final BigFloat b,
                                     final BigFloat c) {
    //return b.multiply(a).add(c); }
  return c.add(b.multiply(a)); }

  @Override
  public final BigFloat value (final double x) {
    int i = _a.length-1;
    if (0>i) { return BigFloat.ZERO; }
    BigFloat tmp = _a[i--];
    while (0<=i) { tmp = fma(x,tmp,_a[i--]); }
    return tmp; }

  //--------------------------------------------------------------

  @Override
  public final double doubleValue (final double x) {
    return value(x).doubleValue(); }

  //--------------------------------------------------------------
  /** Unsafe, retains reference to <code>a</code>. */

  private MonomialBigFloat (final double[] a) {
    final int n = a.length;
    assert 0.0!=a[a.length-1];
    final BigFloat[] b = new BigFloat[n];
    for (int i=0;i<n;i++) { b[i] = BigFloat.valueOf(a[i]); }
    _a=b; }

  public static final MonomialBigFloat make (final double[] a) {
    return new MonomialBigFloat(Dn.stripTrailingZeros(a)); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

