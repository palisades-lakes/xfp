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

/** Utilities for <code>float</code>, <code>float[]</code>.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-25
 */
public final class Floats implements Set {

  //--------------------------------------------------------------
  // private
  //--------------------------------------------------------------

  //  private static final int SIGN_BITS = 1;
  private static final int EXPONENT_BITS = 8;
  private static final int SIGNIFICAND_BITS = 23;

  //  private static final long SIGN_MASK =
  //    1L << (EXPONENT_BITS + SIGNIFICAND_BITS);

  //  private static final long EXPONENT_MASK =
  //    ((1L << EXPONENT_BITS) - 1L) << SIGNIFICAND_BITS;

  public static final int SIGNIFICAND_MASK =
    (1 << SIGNIFICAND_BITS) - 1;

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

  // TODO: is this correct? quick change from Floats.

  public static final float makeFloat (final int s,
                                       final int e,
                                       final int t) {
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

    final int ss = s << (EXPONENT_BITS + SIGNIFICAND_BITS);
    final int se = e << SIGNIFICAND_BITS;

    assert (0 == (ss & se & t));
    return Float.intBitsToFloat(ss | se | t); }

  //--------------------------------------------------------------
  // operations for algebraic structures over Floats.
  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  @SuppressWarnings("static-method")
  private final Float add (final Float q0, 
                           final Float q1) {
    assert null != q0;
    assert null != q1;
    return Float.valueOf(q0.floatValue() + q1.floatValue()); } 

  public final BinaryOperator<Float> adder () {
    return new BinaryOperator<Float> () {
      @Override
      public final String toString () { return "D.add()"; }
      @Override
      public final Float apply (final Float q0, 
                                final Float q1) {
        return Floats.this.add(q0,q1); } }; }

  //--------------------------------------------------------------

  private static final Float ZERO = Float.valueOf(0.0F);

  @SuppressWarnings("static-method")
  public final Float additiveIdentity () { return ZERO; }

  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  @SuppressWarnings("static-method")
  private final Float negate (final Float q) {
    assert null != q;
    return  Float.valueOf(- q.floatValue()); } 

  public final UnaryOperator<Float> additiveInverse () {
    return new UnaryOperator<Float> () {
      @Override
      public final String toString () { return "D.negate()"; }
      @Override
      public final Float apply (final Float q) {
        return Floats.this.negate(q); } }; }

  //--------------------------------------------------------------

  @SuppressWarnings("static-method")
  private final Float multiply (final Float q0, 
                                final Float q1) {
    assert null != q0;
    assert null != q1;
    return Float.valueOf(q0.floatValue() * q1.floatValue()); } 

  public final BinaryOperator<Float> multiplier () {
    return new BinaryOperator<Float>() {
      @Override
      public final String toString () { return "D.multiply()"; }
      @Override
      public final Float apply (final Float q0, 
                                final Float q1) {
        return Floats.this.multiply(q0,q1); } }; }

  //--------------------------------------------------------------

  private static final Float ONE = Float.valueOf(1.0F);

  @SuppressWarnings("static-method")
  public final Float multiplicativeIdentity () { return ONE; }

  //--------------------------------------------------------------

  @SuppressWarnings("static-method")
  private final Float reciprocal (final Float q) {
    assert null != q;
    final float z = q.floatValue();
    // only a partial inverse
    if (0.0 == z) { return null; }
    return Float.valueOf(1.0F/z);  } 

  public final UnaryOperator<Float> multiplicativeInverse () {
    return new UnaryOperator<Float> () {
      @Override
      public final String toString () { return "D.inverse()"; }
      @Override
      public final Float apply (final Float q) {
        return Floats.this.reciprocal(q); } }; }

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    return element instanceof Float; }

  @Override
  public final boolean contains (final float element) {
    return true; }

  //--------------------------------------------------------------
  // Float.equals reduces both arguments before checking
  // numerator and denominators are equal.
  // Guessing our Floats are usually already reduced.
  // Try n0*d1 == n1*d0 instead
  // TODO: use BigInteger.bitLength() to decide
  // which method to use?

  @SuppressWarnings("static-method")
  public final boolean equals (final Float q0, 
                               final Float q1) {
    assert null != q0;
    assert null != q1;
    return q0.equals(q1); }

  @Override
  public final BiPredicate equivalence () {
    return new BiPredicate<Float,Float>() {
      @Override
      public final boolean test (final Float q0, 
                                 final Float q1) {
        return Floats.this.equals(q0,q1); } }; }

  //--------------------------------------------------------------

  @Override
  public final Supplier generator (final UniformRandomProvider urp,
                                   final Map options) {
    final Generator g = Generators.finiteFloatGenerator(urp);
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
    return that instanceof Floats; }

  @Override
  public final String toString () { return "D"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private Floats () { }

  private static final Floats SINGLETON = new Floats();

  public static final Floats get () { return SINGLETON; } 

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

