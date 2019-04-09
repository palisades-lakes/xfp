package xfp.java.accumulators;

/** Naive sum of <code>double</code> values.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-09
 */

public final class DoubleAccumulator 
implements Accumulator<DoubleAccumulator> {

  private double _sum;

  //--------------------------------------------------------------

  @Override
  public final boolean isExact () { return false; }

  // TODO: test for overflow and throw an exception?
  @Override
  public final boolean noOverflow () { return false; }

  @Override
  public final double doubleValue () { return _sum; }

  @Override
  public final DoubleAccumulator clear () { 
    _sum = 0.0; return this; }

  @Override
  public final DoubleAccumulator add (final double z) { 
    assert Double.isFinite(z);
    _sum += z; 
    return this; }

  @Override
  public final DoubleAccumulator addAll (final double[] z) { 
    for (final double zi : z) { 
      assert Double.isFinite(zi);
      _sum += zi; } 
    return this; }

  @Override
  public final DoubleAccumulator add2 (final double z) { 
    assert Double.isFinite(z);
    _sum += z*z;
    return this; }

  @Override
  public final DoubleAccumulator add2All (final double[] z) { 
    for (final double zi : z) { 
      assert Double.isFinite(zi);
      _sum += zi*zi; } 
    return this; }

  @Override
  public final DoubleAccumulator addProduct (final double z0,
                                             final double z1) { 
    assert Double.isFinite(z0);
    assert Double.isFinite(z1);
    _sum += z0*z1;
    return this; }

  @Override
  public final DoubleAccumulator addProducts (final double[] z0,
                                              final double[] z1) { 
    final int n = z0.length;
    assert n == z1.length;
    for (int i=0;i<n;i++) { 
      assert Double.isFinite(z0[i]);
      assert Double.isFinite(z1[i]);
            _sum += z0[i]*z1[i]; }
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
