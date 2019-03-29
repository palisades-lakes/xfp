package xfp.java.accumulators;

/** Naive sum of <code>double</code> values with float 
 * accumulator (for testing).
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-29
 */
public final class FloatAccumulator 

implements Accumulator<FloatAccumulator> {

  private float _sum;

  //--------------------------------------------------------------
  // start with only immediate needs

  @Override
  public final double doubleValue () { return _sum; }

  @Override
  public final FloatAccumulator clear () { _sum = 0.0F; return this; }

  @Override
  public final FloatAccumulator add (final double z) { 
    _sum += (float) z; 
    return this; }

  //  @Override
  //  public final FloatAccumulator addAll (final double[] z)  {
  //    for (final double zi : z) { _sum += (float) zi; }
  //    return this; }

  @Override
  public final FloatAccumulator addProduct (final double z0,
                                            final double z1) { 
    _sum += ((float) z0)*((float) z1);
    return this; }

  //@Override
  //public final FloatAccumulator addProducts (final double[] z0,
  //                                        final double[] z1)  {
  //    final int n = z0.length;
  //    assert n == z1.length;
  //    for (int i=0;i<n;i++) { 
  //      _sum += ((float) z0[i])*((float) z1[i]); }
  //    return this; }
  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private FloatAccumulator () { super(); _sum = 0.0F; }

  public static final FloatAccumulator make () {
    return new FloatAccumulator(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
