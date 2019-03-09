package xfp.java.accumulators;

/** Naive sum of <code>double</code> values with float 
 * accumulator (for testing).
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-08
 */
public final class FloatFmaSum implements Accumulator<FloatFmaSum> {

  private float _sum;

  //--------------------------------------------------------------
  // start with only immediate needs

  @Override
  public final double doubleValue () { return _sum; }

  @Override
  public final FloatFmaSum clear () { _sum = 0.0F; return this; }

  @Override
  public final FloatFmaSum add (final double z) { 
    _sum += (float) z; 
    return this; }

//  @Override
//  public final FloatFmaSum addAll (final double[] z)  {
//    for (final double zi : z) { _sum += (float) zi; }
//    return this; }

  @Override
  public final FloatFmaSum addProduct (final double z0,
                                       final double z1) { 
    _sum = Math.fma((float) z0, (float) z1, _sum);
    return this; }

//@Override
//public final FloatFmaSum addProducts (final double[] z0,
//                                        final double[] z1)  {
//    final int n = z0.length;
//    assert n == z1.length;
//    for (int i=0;i<n;i++) { 
//      _sum += ((float) z0[i])*((float) z1[i]); }
//    return this; }
  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private FloatFmaSum () { super(); _sum = 0.0F; }

  public static final FloatFmaSum make () {
    return new FloatFmaSum(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
