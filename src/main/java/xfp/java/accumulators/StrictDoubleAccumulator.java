package xfp.java.accumulators;

/** Naive sum of <code>double</code> values.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-01
 */

strictfp
public final class StrictDoubleAccumulator 
implements Accumulator<StrictDoubleAccumulator> {

  private double _sum;

  //--------------------------------------------------------------
  // start with only immediate needs

  @Override
  public final double doubleValue () { return _sum; }

  @Override
  public final StrictDoubleAccumulator clear () { 
    _sum = 0.0; return this; }

  @Override
  public final StrictDoubleAccumulator add (final double z) { 
    _sum += z; 
    return this; }

  @Override
  public final StrictDoubleAccumulator addProduct (final double z0,
                                                   final double z1) { 
    _sum += z0*z1;
    return this; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private StrictDoubleAccumulator () { super(); _sum = 0.0; }

  public static final StrictDoubleAccumulator make () {
    return new StrictDoubleAccumulator(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
