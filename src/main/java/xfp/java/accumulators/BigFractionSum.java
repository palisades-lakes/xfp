package xfp.java.accumulators;

import org.apache.commons.math3.fraction.BigFraction;

/** Naive sum of <code>double</code> values with BigFraction 
 * accumulator (for testing).
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-07
 */
public final class BigFractionSum implements Accumulator {

  private BigFraction _sum;
  
  //--------------------------------------------------------------
  // start with only immediate needs
  
  @Override
  public final void clear () { 
    _sum = BigFraction.ZERO; }

  @Override
  public final double doubleValue () { 
    return _sum.doubleValue(); }

  @Override
  public final void add (final double z) { 
    _sum = _sum.add(new BigFraction(z)); }
  
  @Override
  public final void addAll (final double[] z)  {
    for (final double zi : z) { 
      _sum = _sum.add(new BigFraction(zi)); } }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  
  private BigFractionSum () { super(); clear(); }
  
  public static final BigFractionSum make () {
    return new BigFractionSum(); }
  
  //--------------------------------------------------------------
}
//--------------------------------------------------------------
