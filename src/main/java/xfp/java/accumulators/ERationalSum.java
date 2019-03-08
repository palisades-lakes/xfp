package xfp.java.accumulators;

import com.upokecenter.numbers.ERational;

/** Naive sum of <code>double</code> values with ERational 
 * accumulator (for testing).
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-07
 */
public final class ERationalSum implements Accumulator {

  private ERational _sum;
  
  //--------------------------------------------------------------
  // start with only immediate needs
  
  @Override
  public final void clear () { 
    _sum = ERational.Zero; }

  @Override
  public final double doubleValue () { 
    return _sum.ToDouble(); }

  @Override
  public final void add (final double z) { 
    _sum = _sum.Add(ERational.FromDouble(z)); }
  
  @Override
  public final void addAll (final double[] z)  {
    for (final double zi : z) { 
      _sum = _sum.Add(ERational.FromDouble(zi)); } }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  
  private ERationalSum () { super(); clear(); }
  
  public static final ERationalSum make () {
    return new ERationalSum(); }
  
  //--------------------------------------------------------------
}
//--------------------------------------------------------------
