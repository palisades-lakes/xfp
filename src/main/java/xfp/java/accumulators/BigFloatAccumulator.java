package xfp.java.accumulators;

import xfp.java.numbers.BigFloat;

/** Naive sum of <code>double</code> values with a BigFloat-valued
 * accumulator.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-06-16
 */
public final class BigFloatAccumulator
extends ExactAccumulator<BigFloatAccumulator> {

  private BigFloat _sum;

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
  public final BigFloatAccumulator clear () {
    _sum = BigFloat.ZERO;
    return this; }

  @Override
  public final BigFloatAccumulator add (final double z) {
    _sum = _sum.add(z);
    return this; }

  @Override
  public final BigFloatAccumulator add2 (final double z) {
    _sum = _sum.add2(z);
    return this; }

  @Override
  public final BigFloatAccumulator addProduct (final double z0,
                                               final double z1) {
    _sum = _sum.addProduct(z0,z1);
    return this; }

  @Override
  public final BigFloatAccumulator addL1 (final double z0,
                                          final double z1) {

    _sum = _sum.addL1(z0,z1);
    return this; }

  @Override
  public final BigFloatAccumulator addL2 (final double z0,
                                          final double z1) {
    _sum = _sum.addL2(z0,z1);
    return this; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private BigFloatAccumulator () { super(); clear(); }

  public static final BigFloatAccumulator make () {
    return new BigFloatAccumulator(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
