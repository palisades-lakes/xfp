package xfp.java.accumulators;

import java.math.BigInteger;

import xfp.java.numbers.Doubles;
import xfp.java.numbers.Rational;

/** Naive sum of <code>double</code> values with BigInteger pair 
 * accumulator (for testing).
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-29
 */
public final class MutableRationalAccumulator 

implements 
Accumulator<MutableRationalAccumulator>, 
Comparable<MutableRationalAccumulator> {

  //--------------------------------------------------------------
  // instance fields and methods
  //--------------------------------------------------------------

  private BigInteger _numerator;
  public final BigInteger numerator () { return _numerator; }
  private BigInteger _denominator;
  public final BigInteger denominator () { return _denominator; }

  //--------------------------------------------------------------

  public final MutableRationalAccumulator reduce () {
    if (_numerator == BigInteger.ZERO) {
      _denominator = BigInteger.ONE; }
    else {
      final BigInteger gcd = _numerator.gcd(_denominator);
      if (gcd.compareTo(BigInteger.ONE) > 0) {
        _numerator = _numerator.divide(gcd);
        _denominator = _denominator.divide(gcd); } }
    return this; }

  private final MutableRationalAccumulator add (final BigInteger n,
                                                final BigInteger d) {
    if (0 == _numerator.signum()) {
      _numerator = n;
      _denominator = d; }
    else {
      _numerator = 
        _numerator.multiply(d).add(n.multiply(_denominator));
      _denominator = _denominator.multiply(d); }
    return this; }

  //--------------------------------------------------------------
  // Accumulator interface
  //--------------------------------------------------------------
  // start with only immediate needs

  @Override
  public final double doubleValue () { 
    return 
      Rational.valueOf(numerator(),denominator()).doubleValue(); }

  @Override
  public final float floatValue () { 
    return 
      Rational.valueOf(numerator(),denominator()).floatValue(); }

  @Override
  public final MutableRationalAccumulator clear () { 
    _numerator = BigInteger.ZERO;
    _denominator = BigInteger.ONE;
    return this; }

  @Override
  public final MutableRationalAccumulator add (final double z) { 
    // would be nice to have multiple value return...
    final BigInteger[] nd = Doubles.toRatio(z);
    return add(nd[0],nd[1])
      .reduce()
      ; }

  //  @Override
  //  public final RationalAccumulator addAll (final double[] z)  {
  //    for (final double zi : z) { 
  //      _sum = _sum.add(Rational.valueOf(zi)); }
  //    return this; }

  @Override
  public final MutableRationalAccumulator addProduct (final double z0,
                                                      final double z1) { 
    final BigInteger[] nd0 = Doubles.toRatio(z0);
    final BigInteger[] nd1 = Doubles.toRatio(z1);
    return add(
      nd0[0].multiply(nd1[0]),
      nd0[1].multiply(nd1[1]))
      .reduce()
      ; }

  //@Override
  //public final RationalAccumulator addProducts (final double[] z0,
  //                                      final double[] z1)  {
  //    final int n = z0.length;
  //    assert n == z1.length;
  //    for (int i=0;i<n;i++) { 
  //      sum = _sum.Add(
  //        Rational.valueOf(z0[i])
  //        .multiply(Rational.valueOf(z1[i])));}
  //    return this; }

  //--------------------------------------------------------------
  // Comparable methods
  //--------------------------------------------------------------

  @Override
  public final int compareTo (final MutableRationalAccumulator o) {
    final BigInteger n0d1 = _numerator.multiply(o._denominator);
    final BigInteger n1d0 = o._numerator.multiply(_denominator);
    return n0d1.compareTo(n1d0); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final String toString () {
    return 
      "(" + _numerator.toString(0x10) 
      + " / " + _denominator.toString(0x10) 
      + ")"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private MutableRationalAccumulator (final BigInteger numerator,
                                      final BigInteger denominator) { 
    super(); 
    assert 0 != denominator.signum();

    if (denominator.signum() < 0) {
      _numerator = numerator.negate();
      _denominator = denominator.negate(); } 
    else {
      _numerator = numerator;
      _denominator = denominator; } 
    reduce(); }

  public static final MutableRationalAccumulator valueOf (final BigInteger n,
                                                          final BigInteger d) {
    return new MutableRationalAccumulator(n,d); }

  public static final MutableRationalAccumulator valueOf (final long n,
                                                          final long d) {
    return valueOf(BigInteger.valueOf(n),BigInteger.valueOf(d)); }

  public static final MutableRationalAccumulator valueOf (final int n,
                                                          final int d) {
    return valueOf(BigInteger.valueOf(n),BigInteger.valueOf(d)); }

  public static final MutableRationalAccumulator make () {
    return valueOf(BigInteger.ZERO,BigInteger.ONE); }

  public static final MutableRationalAccumulator valueOf (final double z) {
    return make().add(z); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
