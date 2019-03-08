package xfp.java.accumulators;

import java.math.BigDecimal;

/** Naive sum of <code>double</code> values with BigDecimal 
 * accumulator (for testing).
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-07
 */
public final class BigDecimalSum implements Accumulator {

  private BigDecimal _sum;
  
  //--------------------------------------------------------------
  // start with only immediate needs
  
  @Override
  public final Accumulator clear () { 
    _sum = BigDecimal.ZERO;
    return this; }

  @Override
  public final double doubleValue () { 
    return _sum.doubleValue(); }

  @Override
  public final Accumulator add (final double z) { 
    _sum = _sum.add(new BigDecimal(z));
    return this; }
  
//  @Override
//  public final Accumulator addAll (final double[] z)  {
//    for (final double zi : z) { 
//      _sum = _sum.add(new BigDecimal(zi)); }
//    return this; }

  @Override
  public final Accumulator addProduct (final double z0,
                                       final double z1) { 
    _sum = _sum.add(
      new BigDecimal(z0)
      .multiply(
        new BigDecimal(z1)));
    return this; }
  
//  @Override
//  public final Accumulator addProducts (final double[] z0,
//                                        final double[] z1)  {
//    final int n = z0.length;
//    assert n == z1.length;
//    for (int i=0;i<n;i++) { addProduct(z0[i],z1[i]); }
//    return this; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  
  private BigDecimalSum () { super(); clear(); }
  
  public static final BigDecimalSum make () {
    return new BigDecimalSum(); }
  
  //--------------------------------------------------------------
}
//--------------------------------------------------------------
