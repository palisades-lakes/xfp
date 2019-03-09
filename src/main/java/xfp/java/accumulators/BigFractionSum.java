package xfp.java.accumulators;

import org.apache.commons.math3.fraction.BigFraction;

/** Naive sum of <code>double</code> values with BigFraction 
 * accumulator (for testing).
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-07
 */
public final class BigFractionSum implements Accumulator<BigFractionSum> {

  private BigFraction _sum;

  //--------------------------------------------------------------
  // start with only immediate needs

  @Override
  public final double doubleValue () { 
    return _sum.doubleValue(); }

  @Override
  public final BigFractionSum clear () { 
    _sum = BigFraction.ZERO;
    return this; }

  @Override
  public final BigFractionSum add (final double z) { 
    _sum = _sum.add(new BigFraction(z));
    return this; }

//  @Override
//  public final BigFractionSum addAll (final double[] z)  {
//    for (final double zi : z) { 
//      _sum = _sum.add(new BigFraction(zi)); }
//    return this; } 

  @Override
  public final BigFractionSum addProduct (final double z0,
                                       final double z1) { 
    _sum = _sum.add(
      new BigFraction(z0)
      .multiply(
        new BigFraction(z1)));
    return this; }
  
  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private BigFractionSum () { super(); clear(); }

  public static final BigFractionSum make () {
    return new BigFractionSum(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
