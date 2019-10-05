package xfp.java.polynomial;

import static xfp.java.numbers.BigFloat.axpy;

import xfp.java.numbers.BigFloat;

/** Exact {@link Quadratic} using {@link BigFloat} for the exact 
 * values.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-04
 */

@SuppressWarnings("unchecked")
public final class BigFloatQuadratic 
implements Quadratic<BigFloat> {

  private final double _a0;
  private final double _a1;
  private final double _a2;
  
  @Override
  public final boolean isExact () { return true; }

  @Override
  public final BigFloat value (final double x) {
    return axpy(x,axpy(x,_a2,_a1),_a0); }

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

  private BigFloatQuadratic (final double a0,
                             final double a1, 
                             final double a2) {
    _a0=a0; _a1=a1; _a2=a2; }
  
  public static final BigFloatQuadratic make (final double a0,
                                              final double a1, 
                                              final double a2) {
    return new BigFloatQuadratic(a0,a1,a2); }

//--------------------------------------------------------------
}
//--------------------------------------------------------------

