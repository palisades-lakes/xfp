package xfp.java.accumulators;

/** Naive sum of <code>double</code> values, using fma.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-09
 */

public final class DoubleFmaAccumulator 
implements Accumulator<DoubleFmaAccumulator> {

  private double _sum;

  //--------------------------------------------------------------

  @Override
  public final boolean isExact () { return false; }

  @Override
  public final boolean noOverflow () { return false; }

  @Override
  public final double doubleValue () { return _sum; }

  @Override
  public final DoubleFmaAccumulator clear () { 
    _sum = 0.0; 
    return this; }

  @Override
  public final DoubleFmaAccumulator add (final double z) { 
    assert Double.isFinite(z);
    _sum += z; 
    return this; }

  @Override
  public final DoubleFmaAccumulator addAll (final double[] z) { 
    for (final double zi : z) { 
      assert Double.isFinite(zi);
      _sum += zi; } 
    return this; }

  @Override
  public final DoubleFmaAccumulator 
  add2 (final double z) { 
    assert Double.isFinite(z);
    _sum = Math.fma(z,z,_sum);
    return this; }

  @Override
  public final DoubleFmaAccumulator add2All (final double[] z) { 
    for (final double zi : z) { 
      assert Double.isFinite(zi);
      _sum = Math.fma(zi,zi,_sum); } 
    return this; }

  @Override
  public final DoubleFmaAccumulator 
  addProduct (final double z0,
              final double z1) { 
    assert Double.isFinite(z0);
    assert Double.isFinite(z1);
    _sum = Math.fma(z0,z1,_sum);
    return this; }

  @Override
  public final DoubleFmaAccumulator addProducts (final double[] z0,
                                                 final double[] z1) { 
    final int n = z0.length;
    assert n == z1.length;
    for (int i=0;i<n;i++) { 
      assert Double.isFinite(z0[i]);
      assert Double.isFinite(z1[i]);
      _sum = Math.fma(z0[i],z1[i],_sum); }
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
