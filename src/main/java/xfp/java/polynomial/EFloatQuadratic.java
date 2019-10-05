package xfp.java.polynomial;

import static com.upokecenter.numbers.EFloat.FromDouble;

import com.upokecenter.numbers.EFloat;

/** Exact {@link Quadratic} using {@link EFloat} for the exact 
 * values.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-04
 */

@SuppressWarnings("unchecked")
public final class EFloatQuadratic 
implements Quadratic<EFloat> {

  private final EFloat _a0;
  private final EFloat _a1;
  private final EFloat _a2;

  @Override
  public final boolean isExact () { return true; }

  private static final EFloat fma (final double a,
                                   final EFloat b,
                                   final EFloat c) {
   return FromDouble(a).MultiplyAndAdd(b,c); }

  @Override
  public final EFloat value (final double x) {
    return fma(x,fma(x,_a2,_a1),_a0); }

  @Override
  public final EFloat[] value (final double[] x) {
    final int n = x.length;
    final EFloat[] z = new EFloat[n];
    for (int i=0;i<n;i++) {
      z[i] = value(x[i]); }
    return z; }

  @Override
  public final double doubleValue (final double x) {
    return value(x).ToDouble(); }

  //--------------------------------------------------------------

  private EFloatQuadratic (final double a0,
                           final double a1, 
                           final double a2) {
    _a0=FromDouble(a0); _a1=FromDouble(a1); _a2=FromDouble(a2); }

  public static final EFloatQuadratic make (final double a0,
                                            final double a1, 
                                            final double a2) {
    return new EFloatQuadratic(a0,a1,a2); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

