package xfp.java.accumulators;

import xfp.java.numbers.Rational;

/** Naive sum of <code>double</code> values with a Rational-valued 
 * accumulator (for testing).
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-27
 */
public final class RationalBinaryFloatSum implements Accumulator<RationalBinaryFloatSum> {

  private Rational _sum;

  //--------------------------------------------------------------
  // start with only immediate needs

  @Override
  public final double doubleValue () { 
    return _sum.doubleValue(); }

  @Override
  public final float floatValue () { 
    return _sum.floatValue(); }

  @Override
  public final RationalBinaryFloatSum clear () { 
    _sum = Rational.ZERO;
    return this; }

  @Override
  public final RationalBinaryFloatSum add (final double z) { 
    _sum = _sum.add(z);
    return this; }

  @Override
  public final RationalBinaryFloatSum addProduct (final double z0,
                                                  final double z1) { 
    _sum = _sum.addProduct(z0,z1);
    return this; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private RationalBinaryFloatSum () { super(); clear(); }

  public static final RationalBinaryFloatSum make () {
    return new RationalBinaryFloatSum(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------