package xfp.java.numbers;

import static java.lang.Math.fma;

//----------------------------------------------------------------
/*** Mutable! Not thread safe!
*      
* @author palisades dot lakes at gmail dot com
* @version 2019-03-29
*/
public final class Double2 {

  public double z0 = 0.0;
  public double z1 = 0.0;

  public final void noBranch (final double a, 
                              final double b) {
//    assert Double.isFinite(a) && Double.isFinite(b) :
//      Double.toHexString(a) + " + " + Double.toHexString(b);
    final double x = a + b;
    final double z = x - a;
    z1 = (a - (x - z)) + (b - z);
    z0 = x;
//    assert Double.isFinite(z0) && Double.isFinite(z1) :
//      Double.toHexString(z0) + " + " + Double.toHexString(z1); 
    }

  public final void branch (final double a, 
                            final double b) {
//    assert Double.isFinite(a) && Double.isFinite(b) :
//      Double.toHexString(a) + " + " + Double.toHexString(b);
    final double x = a + b;
    z0 = x;
    if (Doubles.biasedExponent(a) > Doubles.biasedExponent(b)) {
      z1 = b - (x - a); }
    else {
      z1 = a - (x - b); }
//    assert Double.isFinite(z0) && Double.isFinite(z1) :
//      Double.toHexString(z0) + " + " + Double.toHexString(z1); 
    }

  /** Muller et al 2010 Algorithm 4.4 */

  public final void twoSum (final double p,
                            final double s) {
    final double s1 = p+s;
    final double ds1 = s1 - s;
    final double ds2 = s1 - ds1;
    final double dp1 = p - ds1;
    final double dp2 = s - ds2;
    z0 = s1;
    z1 = dp1+dp2; }

  public final void twoProductFMA (final double x0,
                                   final double x1) {
    final double z = x0 * x1;
    z0 = z;
    z1 = fma(x0,x1,-z); }

  public Double2 () {} }