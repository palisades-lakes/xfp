package xfp.java.polynomial;

import static java.lang.Math.fma;

/** Approximate cubic {@link Polynomial} 
 * using <code>double</code>.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-05
 */

@SuppressWarnings("unchecked")
public final class DoubleCubic 
implements Polynomial<Double> {

  private final double _a0;
  private final double _a1;
  private final double _a2;
  private final double _a3;

  @Override
  public final double doubleValue (final double x) {
    return fma(x,fma(x,fma(x,_a3,_a2),_a1),_a0); }

  //--------------------------------------------------------------

  private DoubleCubic (final double a0,
                       final double a1, 
                       final double a2, 
                       final double a3) {
    _a0=a0; _a1=a1; _a2=a2; _a3=a3; }

  public static final DoubleCubic make (final double a0,
                                        final double a1, 
                                        final double a2, 
                                        final double a3) {
    return new DoubleCubic(a0,a1,a2,a3); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

