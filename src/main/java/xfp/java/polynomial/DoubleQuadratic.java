package xfp.java.polynomial;

import static java.lang.Math.fma;

/** Approximate {@link Quadratic} using <code>double</code>.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-04
 */

@SuppressWarnings("unchecked")
public final class DoubleQuadratic 
implements Quadratic<Double> {

  private final double _a0;
  private final double _a1;
  private final double _a2;
  
  @Override
  public final double doubleValue (final double x) {
    return fma(x,fma(x,_a2,_a1),_a0); }

  //--------------------------------------------------------------

  private DoubleQuadratic (final double a0,
                             final double a1, 
                             final double a2) {
    _a0=a0; _a1=a1; _a2=a2; }
  
  public static final DoubleQuadratic make (final double a0,
                                              final double a1, 
                                              final double a2) {
    return new DoubleQuadratic(a0,a1,a2); }

//--------------------------------------------------------------
}
//--------------------------------------------------------------

