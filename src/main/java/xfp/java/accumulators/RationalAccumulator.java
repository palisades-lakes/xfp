package xfp.java.accumulators;

import xfp.java.numbers.Rational;

/** Naive sum of <code>double</code> values with a Rational-valued 
 * accumulator (for testing).
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-05-28
 */
public final class RationalAccumulator 

implements Accumulator<RationalAccumulator> {

  private Rational _sum;

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
  public final RationalAccumulator clear () { 
    _sum = Rational.ZERO;
    return this; }

  @Override
  public final RationalAccumulator add (final double z) { 
    assert Double.isFinite(z);
    //Debug.println("z=" + Double.toHexString(z));
    _sum = _sum.add(z);
    //Debug.println("sum=" + _sum.toString());
    //Debug.println("sum=" + Double.toHexString(_sum.doubleValue()));
    return this; }

  @Override
  public final RationalAccumulator add2 (final double z) { 
    assert Double.isFinite(z);
    _sum = _sum.add2(z);
    return this; }

  @Override
  public final RationalAccumulator addProduct (final double z0,
                                               final double z1) { 
    assert Double.isFinite(z0);
    assert Double.isFinite(z1);
    _sum = _sum.addProduct(z0,z1);
    return this; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private RationalAccumulator () { super(); clear(); }

  public static final RationalAccumulator make () {
    return new RationalAccumulator(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------