package xfp.java.accumulators;

import xfp.java.numbers.RationalFloat;

/** Naive sum of <code>double</code> values with a
 * {@link RationalFloat}-valued
 * accumulator.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-06-06
 */
public final class RationalFloatAccumulator1

extends ExactAccumulator<RationalFloatAccumulator1> {

  private RationalFloat _sum;

  //--------------------------------------------------------------

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
  public final RationalFloatAccumulator1 clear () {
    _sum = RationalFloat.ZERO;
    return this; }

  @Override
  public final RationalFloatAccumulator1 add (final double z) {
    assert Double.isFinite(z);
    _sum = _sum.add(z);
    return this; }

  @Override
  public final RationalFloatAccumulator1 add2 (final double z) {
    assert Double.isFinite(z);
    _sum = _sum.add2(z);
    return this; }

  @Override
  public final RationalFloatAccumulator1 addProduct (final double z0,
                                                     final double z1) {
    assert Double.isFinite(z0);
    assert Double.isFinite(z1);
    _sum = _sum.addProduct(z0,z1);
    return this; }

  @Override
  public final RationalFloatAccumulator1 addL2 (final double x0,
                                                final double x1) {

    _sum = _sum.addL2(x0,x1);
    return this; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private RationalFloatAccumulator1 () { super(); clear(); }

  public static final RationalFloatAccumulator1 make () {
    return new RationalFloatAccumulator1(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
