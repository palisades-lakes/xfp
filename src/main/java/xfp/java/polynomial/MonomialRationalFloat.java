package xfp.java.polynomial;

import xfp.java.linear.Dn;
import xfp.java.numbers.RationalFloat;

/** Approximate cubic {@link Polynomial} 
 * using <code>RationalFloat</code>.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-11
 */

@SuppressWarnings("unchecked")
public final class MonomialRationalFloat 
implements Polynomial<RationalFloat> {

  private final RationalFloat[] _a;

  //--------------------------------------------------------------

  @Override
  public final boolean isExact () { return true; }

  //--------------------------------------------------------------

  private static final RationalFloat fma (final double a,
                                     final RationalFloat b,
                                     final RationalFloat c) {
    //return b.multiply(a).add(c); }
  return c.add(b.multiply(a)); }

  @Override
  public final RationalFloat value (final double x) {
    int i = _a.length-1;
    if (0>i) { return RationalFloat.ZERO; }
    RationalFloat tmp = _a[i--];
    while (0<=i) { tmp = fma(x,tmp,_a[i--]); }
    return tmp; }

  //--------------------------------------------------------------

  @Override
  public final double doubleValue (final double x) {
    return value(x).doubleValue(); }

  //--------------------------------------------------------------
  /** Unsafe, retains reference to <code>a</code>. */

  private MonomialRationalFloat (final double[] a) {
    final int n = a.length;
    assert 0.0!=a[a.length-1];
    final RationalFloat[] b = new RationalFloat[n];
    for (int i=0;i<n;i++) { b[i] = RationalFloat.valueOf(a[i]); }
    _a=b; }

  public static final MonomialRationalFloat 
  make (final double[] a) {
    return new MonomialRationalFloat(Dn.stripTrailingZeros(a)); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

