package xfp.java.accumulators;

import xfp.java.numbers.RationalFloat;

/** Naive sum of <code>double</code> values with a Rational-valued
 * accumulator (for testing).
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-20
 */
public final class RationalFloatBIAccumulator

implements Accumulator<RationalFloatBIAccumulator> {

  private RationalFloat _sum;

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
  public final RationalFloatBIAccumulator clear () {
    _sum = RationalFloat.ZERO;
    return this; }

  @Override
  public final RationalFloatBIAccumulator add (final double z) {
    assert Double.isFinite(z);
    _sum = _sum.add(z);
    return this; }

  @Override
  public final RationalFloatBIAccumulator add2 (final double z) {
    assert Double.isFinite(z);
    _sum = _sum.add2(z);
    return this; }

  @Override
  public final RationalFloatBIAccumulator addProduct (final double z0,
                                                    final double z1) {
    assert Double.isFinite(z0);
    assert Double.isFinite(z1);
    _sum = _sum.addProduct(z0,z1);
    return this; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private RationalFloatBIAccumulator () { super(); clear(); }

  public static final RationalFloatBIAccumulator make () {
    return new RationalFloatBIAccumulator(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------