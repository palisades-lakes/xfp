package xfp.java.accumulators;

import com.upokecenter.numbers.ERational;

/** Naive sum of <code>double</code> values with ERational 
 * accumulator (for testing).
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-08
 */
public final class ERationalSum implements Accumulator<ERationalSum> {

  private ERational _sum;
  
  //--------------------------------------------------------------
  // start with only immediate needs
  
  @Override
  public final double doubleValue () { 
    return _sum.ToDouble(); }

  @Override
  public final ERationalSum clear () { 
    _sum = ERational.Zero;
    return this; }

  @Override
  public final ERationalSum add (final double z) { 
    _sum = _sum.Add(ERational.FromDouble(z));
    return this; }
  
//  @Override
//  public final ERationalSum addAll (final double[] z)  {
//    for (final double zi : z) { 
//      _sum = _sum.Add(ERational.FromDouble(zi)); }
//    return this; }


  @Override
  public final ERationalSum addProduct (final double z0,
                                       final double z1) { 
    _sum = _sum.Add(
      ERational.FromDouble(z0)
      .Multiply(
        ERational.FromDouble(z1)));
    return this; }
  
//@Override
//public final ERationalSum addProducts (final double[] z0,
//                                        final double[] z1)  {
//    final int n = z0.length;
//    assert n == z1.length;
//    for (int i=0;i<n;i++) { 
//  sum = _sum.Add(
//    ERational.FromDouble(z0[i])
//    .Multiply(
//      ERational.FromDouble(z1[i])));}
//    return this; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  
  private ERationalSum () { super(); clear(); }
  
  public static final ERationalSum make () {
    return new ERationalSum(); }
  
  //--------------------------------------------------------------
}
//--------------------------------------------------------------
