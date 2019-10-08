package xfp.java.polynomial;

import static java.lang.Math.fma;

import xfp.java.linear.Dn;

/** Approximate cubic {@link Polynomial} 
 * using <code>double</code>.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-08
 */

@SuppressWarnings("unchecked")
public final class MonomialDouble 
implements Polynomial<Double> {

  private final double[] _a;

  //--------------------------------------------------------------

  @Override
  public final double doubleValue (final double x) {
    int i = _a.length-1;
    if (0>i) { return 0.0; }
    double tmp = _a[i--];
    while (0<=i) { tmp = fma(x,tmp,_a[i--]); }
    return tmp; }

  //--------------------------------------------------------------

  @Override
  public final Double value (final double x) {
    return Double.valueOf(doubleValue(x)); }

  //--------------------------------------------------------------
  /** Unsafe, retains reference to <code>a</code>. */
  
  private MonomialDouble (final double[] a) {
    assert 0.0!=a[a.length-1];
    _a=a; }

  public static final MonomialDouble make (final double[] a) {
    return new MonomialDouble(Dn.copyWoutTrailingZeros(a)); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

