package xfp.java.accumulators;

import xfp.java.numbers.BigFloat0;

/** Naive sum of <code>double</code> values with a BigFloat0-valued
 * accumulator.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-06-10
 */
public final class BigFloatAccumulator0
extends ExactAccumulator<BigFloatAccumulator0> {

  private BigFloat0 _sum;

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
  public final BigFloatAccumulator0 clear () {
    _sum = BigFloat0.ZERO;
    return this; }

  @Override
  public final BigFloatAccumulator0 add (final double z) {
    assert Double.isFinite(z);
    _sum = _sum.add(z);
    return this; }

  @Override
  public final BigFloatAccumulator0 add2 (final double z) {
    assert Double.isFinite(z);
    _sum = _sum.add2(z);
    return this; }

  @Override
  public final BigFloatAccumulator0 addProduct (final double z0,
                                                final double z1) {
    assert Double.isFinite(z0);
    assert Double.isFinite(z1);
    _sum = _sum.addProduct(z0,z1);
    return this; }

  @Override
  public final BigFloatAccumulator0 addL1 (final double x0,
                                           final double x1) {

    assert Double.isFinite(x0);
    assert Double.isFinite(x1);
    _sum = _sum.addL1(x0,x1);
    return this; }

  @Override
  public final BigFloatAccumulator0 addL2 (final double x0,
                                           final double x1) {
    assert Double.isFinite(x0);
    assert Double.isFinite(x1);
    _sum = _sum.addL2(x0,x1);
    return this; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private BigFloatAccumulator0 () { super(); clear(); }

  public static final BigFloatAccumulator0 make () {
    return new BigFloatAccumulator0(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
