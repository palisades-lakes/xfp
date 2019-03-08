package xfp.java.accumulators;

/** Naive sum of <code>double</code> values.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-07
 */
public final class DoubleNaiveSum implements Accumulator {

  private double _sum;
  
  //--------------------------------------------------------------
  // start with only immediate needs
  
  @Override
  public final void clear () { _sum = 0.0; }

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
  
  private DoubleNaiveSum () { super(); _sum = 0.0; }
  
  public static final DoubleNaiveSum make () {
    return new DoubleNaiveSum(); }
  
  //--------------------------------------------------------------
}
//--------------------------------------------------------------
