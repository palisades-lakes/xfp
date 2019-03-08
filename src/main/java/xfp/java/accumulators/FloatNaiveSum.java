package xfp.java.accumulators;

/** Naive sum of <code>double</code> values with float 
 * accumulator (for testing).
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-07
 */
public final class FloatNaiveSum implements Accumulator {

  private float _sum;
  
  //--------------------------------------------------------------
  // start with only immediate needs
  
  @Override
  public final void clear () { _sum = 0.0F; }

  @Override
  public final double doubleValue () { return _sum; }

  @Override
  public final void add (final double z) { _sum += z; }
  
  @Override
  public final void addAll (final double[] z)  {
    for (final double zi : z) { _sum += zi; } }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  
  private FloatNaiveSum () { super(); _sum = 0.0F; }
  
  public static final FloatNaiveSum make () {
    return new FloatNaiveSum(); }
  
  //--------------------------------------------------------------
}
//--------------------------------------------------------------
