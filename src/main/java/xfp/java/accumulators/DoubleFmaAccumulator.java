package xfp.java.accumulators;

/** Naive sum of <code>double</code> values, using fma.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-29
 */
public final class DoubleFmaAccumulator 
implements Accumulator<DoubleFmaAccumulator> {

  private double _sum;

  //--------------------------------------------------------------
  // start with only immediate needs

  @Override
  public final double doubleValue () { return _sum; }

  @Override
  public final DoubleFmaAccumulator clear () { 
    _sum = 0.0; 
    return this; }

  @Override
  public final DoubleFmaAccumulator add (final double z) { 
    _sum += z; 
    return this; }

  @Override
  public final DoubleFmaAccumulator addProduct (final double z0,
                                        final double z1) { 
    _sum = Math.fma(z0,z1,_sum);
    return this; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private DoubleFmaAccumulator () { super(); _sum = 0.0; }

  public static final DoubleFmaAccumulator make () {
    return new DoubleFmaAccumulator(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
