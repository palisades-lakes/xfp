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
  public final void clear () { 
    _sum = BigDecimal.ZERO; }

  @Override
  public final double doubleValue () { 
    return _sum.doubleValue(); }

  @Override
  public final void add (final double z) { 
    _sum = _sum.add(new BigDecimal(z)); }
  
  @Override
  public final void addAll (final double[] z)  {
    for (final double zi : z) { 
      _sum = _sum.add(new BigDecimal(zi)); } }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  
  private BigDecimalSum () { super(); clear(); }
  
  public static final BigDecimalSum make () {
    return new BigDecimalSum(); }
  
  //--------------------------------------------------------------
}
//--------------------------------------------------------------
