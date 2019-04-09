package xfp.java.numbers;

import static java.lang.Math.fma;

//----------------------------------------------------------------
/** Mutable! Not thread safe!
 * 
 * TODO: test impact of immutable version.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-09
 */
public final class Double2 {

  public double z0 = 0.0;
  public double z1 = 0.0;

  //--------------------------------------------------------------
  
  public final void zhuHayesNoBranch (final double x0, 
                                      final double x1) {
    
    assert Double.isFinite(x0) && Double.isFinite(x1) :
      Double.toHexString(x0) + " + " + Double.toHexString(x1);

    z0 = x0 + x1;
    final double z = z0 - x0;
    z1 = (x0 - (z0 - z)) + (x1 - z);
    
    assert Double.isFinite(z0) && Double.isFinite(z1) :
      Double.toHexString(z0) + " + " + Double.toHexString(z1); 
  }

  //--------------------------------------------------------------

  public final void zhuHayesBranch (final double x0, 
                                    final double x1) {

    assert Double.isFinite(x0) && Double.isFinite(x1) :
      Double.toHexString(x0) + " + " + Double.toHexString(x1);

    z0 = x0 + x1;
    if (Doubles.biasedExponent(x0) > Doubles.biasedExponent(x1)) {
      z1 = x1 - (z0 - x0); }
    else {
      z1 = x0 - (z0 - x1); }
 
    assert Double.isFinite(z0) && Double.isFinite(z1) :
      Double.toHexString(z0) + " + " + Double.toHexString(z1); 
  }

  //--------------------------------------------------------------
/** Muller et al 2010 Algorithm 4.4 */

  public final void mullerTwoSum (final double x0,
                                  final double x1) {
    assert Double.isFinite(x0) && Double.isFinite(x1) :
      Double.toHexString(x0) + " + " + Double.toHexString(x1);

    final double s1 = x0+x1;
    final double ds1 = s1 - x1;
    final double ds2 = s1 - ds1;
    final double dp1 = x0 - ds1;
    final double dp2 = x1 - ds2;
    z0 = s1;
    z1 = dp1+dp2; 
    
    assert Double.isFinite(z0) && Double.isFinite(z1) :
      Double.toHexString(z0) + " + " + Double.toHexString(z1); 
  }

  //--------------------------------------------------------------

  public final void twoProductFMA (final double x0,
                                   final double x1) {
    assert Double.isFinite(x0) && Double.isFinite(x1) :
      Double.toHexString(x0) + " + " + Double.toHexString(x1);

    final double z = x0 * x1;
    z0 = z;
    z1 = fma(x0,x1,-z); 
    
    assert Double.isFinite(z0) && Double.isFinite(z1) :
      Double.toHexString(z0) + " + " + Double.toHexString(z1); 
  }

  //--------------------------------------------------------------
  
  public Double2 () {}
  
  //--------------------------------------------------------------
}
//--------------------------------------------------------------
