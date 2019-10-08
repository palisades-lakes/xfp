package xfp.java.polynomial;

import xfp.java.linear.Dn;
import xfp.java.numbers.RationalFloat;

/** Approximate cubic {@link Polynomial} 
 * using <code>double</code> coefficients with 
 * {@link RationalFloat} accumulator for exactness.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-08
 */

@SuppressWarnings("unchecked")
public final class MonomialDoubleRF 
implements Polynomial<RationalFloat> {

  private final double[] _a;

  //--------------------------------------------------------------

  @Override
  public final boolean isExact () { return true; }

  //--------------------------------------------------------------

  @Override
  public final RationalFloat value (final double x) {
    int i = _a.length-1;
    if (0>i) { return RationalFloat.ZERO; }
    if (0==i) { return RationalFloat.valueOf(_a[0]); }
    RationalFloat tmp = RationalFloat.axpy(x,_a[i],_a[i-1]);
    i=i-2;
    for (;0<=i;i--) { tmp = RationalFloat.axpy(x,tmp,_a[i]); }
    return tmp; }

  //--------------------------------------------------------------

  @Override
  public final double doubleValue (final double x) {
    final int n = _a.length;
    if (0==n) { return 0.0; }
    if (1==n) { return _a[0]; }
    return value(x).doubleValue(); }

  //--------------------------------------------------------------
  /** Unsafe, retains reference to <code>a</code>. */
  
  private MonomialDoubleRF (final double[] a) {
    assert 0.0!=a[a.length-1];
    _a=a; }

  public static final MonomialDoubleRF make (final double[] a) {
    return new MonomialDoubleRF(Dn.copyWoutTrailingZeros(a)); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

