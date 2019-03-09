package xfp.java.accumulators;

/** Naive sum of <code>double</code> values.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-08
 */
public final class DoubleFmaSum implements Accumulator<DoubleFmaSum> {

  private double _sum;
  
  //--------------------------------------------------------------
  // start with only immediate needs
  
  @Override
  public final double doubleValue () { return _sum; }

  @Override
  public final DoubleFmaSum clear () { _sum = 0.0; return this; }

  @Override
  public final DoubleFmaSum add (final double z) { 
    _sum += z; 
    return this; }
  
//  @Override
//  public final DoubleFmaSum addAll (final double[] z)  {
//    for (final double zi : z) { _sum += zi; } 
//    return this; }
  
  @Override
  public final DoubleFmaSum addProduct (final double z0,
                                       final double z1) { 
    _sum = Math.fma(z0,z1,_sum);
    return this; }

//  @Override
//  public final Accumulator addProducts (final double[] z0,
//                                          final double[] z1)  {
//      final int n = z0.length;
//      assert n == z1.length;
//      for (int i=0;i<n;i++) { _sum += z0[i]*z1[i]; }
//      return this; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  
  private DoubleFmaSum () { super(); _sum = 0.0; }
  
  public static final DoubleFmaSum make () {
    return new DoubleFmaSum(); }
  
  //--------------------------------------------------------------
}
//--------------------------------------------------------------
