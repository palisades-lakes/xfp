package xfp.java.accumulators;

import xfp.java.numbers.BigFloat4;

/** Naive sum of <code>double</code> values with a BigFloat-valued 
 * accumulator (for testing).
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-05-06
 */
public final class BigFloatAccumulator4 

implements Accumulator<BigFloatAccumulator4> {

  private BigFloat4 _sum;

  //--------------------------------------------------------------

  @Override
  public final boolean isExact () { return true; }

  @Override
  public final boolean noOverflow () { return true; }

  @Override
  public final Object value () { return _sum; }

  @Override
  public final double doubleValue () { 
    return _sum.doubleValue(); }

  @Override
  public final float floatValue () { 
    return _sum.floatValue(); }

  @Override
  public final BigFloatAccumulator4 clear () { 
    _sum = BigFloat4.ZERO;
    return this; }

  @Override
  public final BigFloatAccumulator4 add (final double z) { 
    assert Double.isFinite(z);
    _sum = _sum.add(z);
    return this; }

  @Override
  public final BigFloatAccumulator4 add2 (final double z) { 
    assert Double.isFinite(z);
    _sum = _sum.add2(z);
    return this; }

  @Override
  public final BigFloatAccumulator4 addProduct (final double z0,
                                               final double z1) { 
    assert Double.isFinite(z0);
    assert Double.isFinite(z1);
    _sum = _sum.addProduct(z0,z1);
    return this; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private BigFloatAccumulator4 () { super(); clear(); }

  public static final BigFloatAccumulator4 make () {
    return new BigFloatAccumulator4(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
