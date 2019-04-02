package xfp.java.accumulators;

/** Naive sum of <code>double</code> values, using fma.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-01
 */
strictfp
public final class StrictDoubleFmaAccumulator 
implements Accumulator<StrictDoubleFmaAccumulator> {

  private double _sum;

  //--------------------------------------------------------------
  // start with only immediate needs

  @Override
  public final double doubleValue () { return _sum; }

  @Override
  public final StrictDoubleFmaAccumulator clear () { 
    _sum = 0.0; 
    return this; }

  @Override
  public final StrictDoubleFmaAccumulator add (final double z) { 
    _sum += z; 
    return this; }

  @Override
  public final StrictDoubleFmaAccumulator 
  addProduct (final double z0,
              final double z1) { 
    _sum = StrictMath.fma(z0,z1,_sum);
    return this; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private StrictDoubleFmaAccumulator () { super(); _sum = 0.0; }

  public static final StrictDoubleFmaAccumulator make () {
    return new StrictDoubleFmaAccumulator(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
