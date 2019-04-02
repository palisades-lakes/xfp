package xfp.java.accumulators;

/** Naive sum of <code>double</code> values.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-02
 */

public final class DoubleAccumulator 
implements Accumulator<DoubleAccumulator> {

  private double _sum;

  //--------------------------------------------------------------

  @Override
  public final boolean isExact () { return false; }

  @Override
  public final double doubleValue () { return _sum; }

  @Override
  public final DoubleAccumulator clear () { 
    _sum = 0.0; return this; }

  @Override
  public final DoubleAccumulator add (final double z) { 
    _sum += z; 
    return this; }

  @Override
  public final DoubleAccumulator add2 (final double z) { 
    _sum += z*z;
    return this; }

  @Override
  public final DoubleAccumulator addProduct (final double z0,
                                             final double z1) { 
    _sum += z0*z1;
    return this; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private DoubleAccumulator () { super(); _sum = 0.0; }

  public static final DoubleAccumulator make () {
    return new DoubleAccumulator(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
