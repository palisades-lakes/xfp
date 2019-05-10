package xfp.java.accumulators;

import xfp.java.numbers.BigFloatN;

/** Naive sum of <code>double</code> values with a BigFloat-valued 
 * accumulator (for testing).
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-05-07
 */
public final class BigFloatNAccumulator 

implements Accumulator<Accumulator> {

  private BigFloatN _sum;

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
  public final Accumulator clear () { 
    _sum = BigFloatN.ZERO;
    return this; }

  @Override
  public final Accumulator add (final double z) { 
    assert Double.isFinite(z);
    _sum = _sum.add(z);
    return this; }

  @Override
  public final Accumulator add2 (final double z) { 
    assert Double.isFinite(z);
    _sum = _sum.add2(z);
    return this; }

  @Override
  public final Accumulator addProduct (final double z0,
                                               final double z1) { 
    assert Double.isFinite(z0);
    assert Double.isFinite(z1);
    _sum = _sum.addProduct(z0,z1);
    return this; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private BigFloatNAccumulator () { super(); clear(); }

  public static final BigFloatNAccumulator make () {
    return new BigFloatNAccumulator(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
