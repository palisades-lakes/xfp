package xfp.java.polynomial;

import xfp.java.linear.Dn;
import xfp.java.numbers.BigFloat;

/** Approximate cubic {@link Polynomial} 
 * using <code>double</code> coefficients with 
 * {@link BigFloat} accumulator for exactness.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-08
 */

@SuppressWarnings("unchecked")
public final class MonomialDoubleBF 
implements Polynomial<BigFloat> {

  private final double[] _a;

  //--------------------------------------------------------------

  @Override
  public final boolean isExact () { return true; }

  //--------------------------------------------------------------

  @Override
  public final BigFloat value (final double x) {
    int i = _a.length-1;
    if (0>i) { return BigFloat.ZERO; }
    if (0==i) { return BigFloat.valueOf(_a[0]); }
    BigFloat tmp = BigFloat.axpy(x,_a[i],_a[i-1]);
    //System.out.println(tmp);
    //System.out.println(Double.toHex)
    i=i-2;
    for (;0<=i;i--) { tmp = BigFloat.axpy(x,tmp,_a[i]); }
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
  
  private MonomialDoubleBF (final double[] a) {
    assert 0.0!=a[a.length-1];
    _a=a; }

  public static final MonomialDoubleBF make (final double[] a) {
    return new MonomialDoubleBF(Dn.copyWoutTrailingZeros(a)); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

