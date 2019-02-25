package xfp.java.numbers;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.apache.commons.rng.UniformRandomProvider;

import xfp.java.algebra.OneSetOneOperation;
import xfp.java.algebra.OneSetTwoOperations;
import xfp.java.algebra.Set;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;

/** Utilities for <code>double</code>, <code>double[]</code>.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-25
 */
public final class Doubles implements Set {

  //--------------------------------------------------------------
  // private
  //--------------------------------------------------------------

  //  private static final int SIGN_BITS = 1;
  private static final int EXPONENT_BITS = 11;
  private static final int SIGNIFICAND_BITS = 52;

  //  private static final long SIGN_MASK =
  //    1L << (EXPONENT_BITS + SIGNIFICAND_BITS);

  //  private static final long EXPONENT_MASK =
  //    ((1L << EXPONENT_BITS) - 1L) << SIGNIFICAND_BITS;

  public static final long SIGNIFICAND_MASK =
    (1L << SIGNIFICAND_BITS) - 1L;

  //  private static final int EXPONENT_BIAS =
  //    (1 << (EXPONENT_BITS - 1)) - 1;

  public static final int MAXIMUM_BIASED_EXPONENT =
    (1 << EXPONENT_BITS) - 1;

  //  private static final int MAXIMUM_EXPONENT =
  //    EXPONENT_BIAS;

  //  private static final int MINIMUM_NORMAL_EXPONENT =
  //    1 - MAXIMUM_EXPONENT;

  //  private static final int MINIMUM_SUBNORMAL_EXPONENT =
  //    MINIMUM_NORMAL_EXPONENT - SIGNIFICAND_BITS;

  public static final double makeDouble (final int s,
                                         final int e,
                                         final long t) {
    assert ((0 == s) || (1 ==s)) : "Invalid sign bit:" + s;
    assert (0 <= e) :
      "Negative exponent:" + Integer.toHexString(e);
    assert (e <= MAXIMUM_BIASED_EXPONENT) :
      "Exponent too large:" + Integer.toHexString(e) +
      ">" + Integer.toHexString(MAXIMUM_BIASED_EXPONENT);
    assert (0 <= t) :
      "Negative significand:" + Long.toHexString(t);
    assert (t <= SIGNIFICAND_MASK) :
      "Significand too large:" + Long.toHexString(t) +
      ">" + Long.toHexString(SIGNIFICAND_MASK);

    final long ss = ((long) s) << (EXPONENT_BITS + SIGNIFICAND_BITS);
    final long se = ((long) e) << SIGNIFICAND_BITS;

    assert (0L == (ss & se & t));
    return Double.longBitsToDouble(ss | se | t); }

  //--------------------------------------------------------------
  // operations for algebraic structures over Doubles.
  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  @SuppressWarnings("static-method")
  private final Double add (final Double q0, 
                            final Double q1) {
    assert null != q0;
    assert null != q1;
    return Double.valueOf(q0.doubleValue() + q1.doubleValue()); } 

  public final BinaryOperator<Double> adder () {
    return new BinaryOperator<Double> () {
      @Override
      public final String toString () { return "D.add()"; }
      @Override
      public final Double apply (final Double q0, 
                                 final Double q1) {
        return Doubles.this.add(q0,q1); } }; }

  //--------------------------------------------------------------

  private static final Double ZERO = Double.valueOf(0.0);

  @SuppressWarnings("static-method")
  public final Double additiveIdentity () { return ZERO; }

  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  @SuppressWarnings("static-method")
  private final Double negate (final Double q) {
    assert null != q;
    return  Double.valueOf(- q.doubleValue()); } 

  public final UnaryOperator<Double> additiveInverse () {
    return new UnaryOperator<Double> () {
      @Override
      public final String toString () { return "D.negate()"; }
      @Override
      public final Double apply (final Double q) {
        return Doubles.this.negate(q); } }; }

  //--------------------------------------------------------------

  @SuppressWarnings("static-method")
  private final Double multiply (final Double q0, 
                                 final Double q1) {
    assert null != q0;
    assert null != q1;
    return Double.valueOf(q0.doubleValue() * q1.doubleValue()); } 

  public final BinaryOperator<Double> multiplier () {
    return new BinaryOperator<Double>() {
      @Override
      public final String toString () { return "D.multiply()"; }
      @Override
      public final Double apply (final Double q0, 
                                 final Double q1) {
        return Doubles.this.multiply(q0,q1); } }; }

  //--------------------------------------------------------------

  private static final Double ONE = Double.valueOf(1.0);

  @SuppressWarnings("static-method")
  public final Double multiplicativeIdentity () { return ONE; }

  //--------------------------------------------------------------

  @SuppressWarnings("static-method")
  private final Double reciprocal (final Double q) {
    assert null != q;
    final double z = q.doubleValue();
    // only a partial inverse
    if (0.0 == z) { return null; }
    return Double.valueOf(1.0/z);  } 

  public final UnaryOperator<Double> multiplicativeInverse () {
    return new UnaryOperator<Double> () {
      @Override
      public final String toString () { return "D.inverse()"; }
      @Override
      public final Double apply (final Double q) {
        return Doubles.this.reciprocal(q); } }; }

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    return element instanceof Double; }

  @Override
  public final boolean contains (final double element) {
    return true; }

  //--------------------------------------------------------------
  // Double.equals reduces both arguments before checking
  // numerator and denominators are equal.
  // Guessing our Doubles are usually already reduced.
  // Try n0*d1 == n1*d0 instead
  // TODO: use BigInteger.bitLength() to decide
  // which method to use?

  @SuppressWarnings("static-method")
  public final boolean equals (final Double q0, 
                               final Double q1) {
    assert null != q0;
    assert null != q1;
    return q0.equals(q1); }

  @Override
  public final BiPredicate equivalence () {
    return new BiPredicate<Double,Double>() {
      @Override
      public final boolean test (final Double q0, 
                                 final Double q1) {
        return Doubles.this.equals(q0,q1); } }; }

  //--------------------------------------------------------------

  @Override
  public final Supplier generator (final UniformRandomProvider urp,
                                   final Map options) {
    final Generator g = Generators.finiteDoubleGenerator(urp);
    return 
      new Supplier () {
      @Override
      public final Object get () { return g.next(); } }; }

  @Override
  public final Supplier generator (final UniformRandomProvider urp) {
    return generator(urp,Collections.emptyMap()); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { return 0; }

  // singleton
  @Override
  public final boolean equals (final Object that) {
    return that instanceof Doubles; }

  @Override
  public final String toString () { return "D"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private Doubles () { }

  private static final Doubles SINGLETON = new Doubles();

  public static final Doubles get () { return SINGLETON; } 

  //--------------------------------------------------------------
  // pre-defined structures
  //--------------------------------------------------------------

  public static final OneSetOneOperation ADDITIVE_MAGMA = 
  OneSetOneOperation.magma(get().adder(),get());

  public static final OneSetOneOperation MULTIPLICATIVE_MAGMA = 
  OneSetOneOperation.magma(get().multiplier(),get());

  public static final OneSetTwoOperations FLOATING_POINT = 
  OneSetTwoOperations.floatingPoint(
    get().adder(),
    get().additiveIdentity(),
    get().additiveInverse(),
    get().multiplier(),
    get().multiplicativeIdentity(),
    get().multiplicativeInverse(),
    get());

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

