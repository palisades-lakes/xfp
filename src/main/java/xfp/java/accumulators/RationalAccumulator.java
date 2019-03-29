package xfp.java.accumulators;

import xfp.java.numbers.Rational;

/** Naive sum of <code>double</code> values with a Rational-valued 
 * accumulator (for testing).
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-29
 */
public final class RationalAccumulator 

implements Accumulator<RationalAccumulator> {

  private Rational _sum;

  public final Rational value () { return _sum; }
  
  //--------------------------------------------------------------
  // start with only immediate needs

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
    _sum = _sum.add(z);
    return this; }

  @Override
  public final RationalAccumulator addProduct (final double z0,
                                               final double z1) { 
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
