package xfp.java.accumulators;

import xfp.java.numbers.Rational0;

/** Naive sum of <code>double</code> values with a Rational0-valued 
 * accumulator (for testing).
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-22
 */
public final class Rational0Sum implements Accumulator<Rational0Sum> {

  private Rational0 _sum;

  //--------------------------------------------------------------
  // start with only immediate needs

  @Override
  public final double doubleValue () { 
    return _sum.doubleValue(); }

  @Override
  public final Rational0Sum clear () { 
    _sum = Rational0.ZERO;
    return this; }

  @Override
  public final Rational0Sum add (final double z) { 
    _sum = _sum.add(z);
    return this; }

  @Override
  public final Rational0Sum addProduct (final double z0,
                                        final double z1) { 
    _sum = _sum.addProduct(z0,z1);
    return this; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private Rational0Sum () { super(); clear(); }

  public static final Rational0Sum make () {
    return new Rational0Sum(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
