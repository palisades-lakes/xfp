package xfp.java.accumulators;

import xfp.java.numbers.RationalBinaryFloat;

/** Naive sum of <code>double</code> values with a Rational-valued 
 * accumulator (for testing).
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-02
 */
public final class RBFAccumulator 

implements Accumulator<RBFAccumulator> {

  private RationalBinaryFloat _sum;

  public final RationalBinaryFloat value () { return _sum; }

  //--------------------------------------------------------------
  @Override
  public final boolean isExact () { return true; }


  @Override
  public final double doubleValue () { 
    return _sum.doubleValue(); }

  @Override
  public final float floatValue () { 
    return _sum.floatValue(); }

  @Override
  public final RBFAccumulator clear () { 
    _sum = RationalBinaryFloat.ZERO;
    return this; }

  @Override
  public final RBFAccumulator add (final double z) { 
    _sum = _sum.add(z);
    return this; }

  @Override
  public final RBFAccumulator add2 (final double z) { 
    _sum = _sum.add2(z);
    return this; }

  @Override
  public final RBFAccumulator addProduct (final double z0,
                                          final double z1) { 
    _sum = _sum.addProduct(z0,z1);
    return this; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private RBFAccumulator () { super(); clear(); }

  public static final RBFAccumulator make () {
    return new RBFAccumulator(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------