package xfp.java.numbers;

import static java.lang.Double.MAX_EXPONENT;
import static java.lang.Double.MIN_EXPONENT;
import static java.lang.Double.MIN_VALUE;
import static java.lang.Double.doubleToRawLongBits;
import static java.lang.Double.longBitsToDouble;
import static java.lang.Double.toHexString;

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

/** Utilities for <code>double</code>, <code>double[]</code>.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-21
 */
public final class Doubles implements Set {

  //--------------------------------------------------------------

  public static final int SIGN_BITS = 1;
  public static final int EXPONENT_BITS = 11;
  public static final int STORED_SIGNIFICAND_BITS = 52;

  public static final int SIGNIFICAND_BITS = 
    STORED_SIGNIFICAND_BITS + 1;

  public static final long STORED_SIGNIFICAND_MASK =
    (1L << STORED_SIGNIFICAND_BITS) - 1L;

  public static final long NORMAL_SIGNIFICAND_MASK =
    (1L << STORED_SIGNIFICAND_BITS) | STORED_SIGNIFICAND_MASK;

  public static final long MIN_SUBNORMAL_SIGNIFICAND = 1L;
  public static final long MAX_SUBNORMAL_SIGNIFICAND =
    STORED_SIGNIFICAND_MASK;
  public static final long MIN_NORMAL_SIGNIFICAND =
    MAX_SUBNORMAL_SIGNIFICAND + 1L;
  public static final long MAX_NORMAL_SIGNIFICAND =
    NORMAL_SIGNIFICAND_MASK;

  public static final long SIGN_MASK =
    1L << (EXPONENT_BITS + STORED_SIGNIFICAND_BITS);

  public static final long EXPONENT_MASK =
    ((1L << EXPONENT_BITS) - 1L) << STORED_SIGNIFICAND_BITS;

  /** inclusive */
  public static final int MAXIMUM_BIASED_EXPONENT =
    (1 << EXPONENT_BITS) - 1;

  public static final int EXPONENT_BIAS =
    (1 << (EXPONENT_BITS - 1)) - 1;

  /** Unbiased exponent for subnormal numbers, with implied
   * leading bit = 0.
   */
  public static final int SUBNORMAL_EXPONENT = 
    MIN_EXPONENT - 1;

  /** Unbiased exponent of smallest non zero value. */
  public static final int MINIMUM_SUBNORMAL_EXPONENT =
    MIN_EXPONENT - STORED_SIGNIFICAND_BITS;

  /** Unbiased exponent for {@link Double#NaN},
   * {@link Double#NEGATIVE_INFINITY}, or
   * {@link Double#POSITIVE_INFINITY}.
   */
  public static final int INFINITE_OR_NAN_EXPONENT = 
    MAX_EXPONENT + 1;

  //    static {
  //      assert ((~0L) == (SIGN_MASK | EXPONENT_MASK | SIGNIFICAND_MASK));
  //      assert (0L == (SIGN_MASK & EXPONENT_MASK));
  //      assert (0L == (EXPONENT_MASK & SIGNIFICAND_MASK));
  //      assert (0L == (SIGN_MASK & SIGNIFICAND_MASK));
  //    }
  //--------------------------------------------------------------

  public static final int signBit (final double x) {
    return  (int)
      ((SIGN_MASK & doubleToRawLongBits(x))
        >> (EXPONENT_BITS + STORED_SIGNIFICAND_BITS)); }

  public static final boolean nonNegative (final double x) {
    return  0 == signBit(x); }

  //--------------------------------------------------------------

  /** Actual 52 stored bits, without the implied leading 1 bit
   * for normal numbers.
   */

  public static final long significandLowBits (final double x) {
    return STORED_SIGNIFICAND_MASK & doubleToRawLongBits(x); }

  /** 53 bit significand.
   * Adds the implicit leading bit to the stored bits, 
   * if there is one. 
   */

  public static final long significand (final double x) {
    final long t = significandLowBits(x);
    // signed zero or subnormal
    if (biasedExponent(x) == 0) { return t; }
    return t + STORED_SIGNIFICAND_MASK + 1; }

  //--------------------------------------------------------------

  public static final int biasedExponent (final double x) {
    return
      (int)
      ((EXPONENT_MASK & doubleToRawLongBits(x))
        >> STORED_SIGNIFICAND_BITS); }

  //--------------------------------------------------------------

  public static final int unbiasedExponent (final double x) {
    return biasedExponent(x) - EXPONENT_BIAS; }

  //--------------------------------------------------------------

  public static final boolean isNormal (final double x) {
    final int be = biasedExponent(x);
    return (0.0 == x) || ((0 != be) && (0x7ff != be)); }

  //--------------------------------------------------------------
  // TODO: change exponent ranges so that significand can be taken
  // as its actual value?

  /**
   * @param sign sign bit, must be 0 or 1
   * @param exponent unbiased exponent, must be in 
   * [{@link #SUBNORMAL_EXPONENT},{@link Double#MAX_EXPONENT}]
   * When {@link Double#MIN_EXPONENT} &le; <code>e</code>
   * &le; {@link Double#MAX_EXPONENT}, te result is a normal
   * number.
   * @param significand Must be in 
   * [0,{@link #STORED_SIGNIFICAND_MASK}].
   * Treated as <code>significand * 2<sup>-52</sup></code>.
   * @return <code>(-1)<sup>sign</sup> * 2<sup>exponent</sup>
   * * significand * 2<sup>-52</sup></code>
   */

  public static final double makeDouble (final int sign,
                                         final int exponent,
                                         final long significand) {

    assert ((0 == sign) || (1 ==sign)) : "Invalid sign bit:" + sign;

    assert (SUBNORMAL_EXPONENT <= exponent)  : 
      "(unbiased) exponent too small: " + exponent;
    assert (exponent <= MAX_EXPONENT) :
      "(unbiased) exponent too large: " + exponent;
    final int be = exponent + EXPONENT_BIAS;
    assert (0 <= be) :
      "Negative exponent:" + Integer.toHexString(be) + " : " + be 
      + "\n" + SUBNORMAL_EXPONENT + "<=" + exponent + "<=" + MAX_EXPONENT
      + "\n" + MIN_VALUE + " " + toHexString(MIN_VALUE)
      + "\n" + EXPONENT_BIAS;
    assert (be <= MAXIMUM_BIASED_EXPONENT) :
      "Exponent too large:" + Integer.toHexString(be) +
      ">" + Integer.toHexString(MAXIMUM_BIASED_EXPONENT);

    if (SUBNORMAL_EXPONENT == exponent) {
      assert (0 <= significand) :
        "subnormal significand too small:" + Long.toHexString(significand);
      assert significand <= MAX_SUBNORMAL_SIGNIFICAND :
        "subnormal significand too large:" + Long.toHexString(significand); }
    else if (INFINITE_OR_NAN_EXPONENT == exponent) {
      // no leading 1 bit for infinity or NaN
      assert (0 <= significand) :
        "infinite or NaN significand too small:" 
        + Long.toHexString(significand);
      assert significand <= MAX_SUBNORMAL_SIGNIFICAND :
        "infinite or NaN significand too large:" 
        + Long.toHexString(significand); }
    else { // normal numbers
      assert (MIN_NORMAL_SIGNIFICAND <= significand) :
        "Normal significand too small:" 
        + Long.toHexString(significand);
      assert (significand <= MAX_NORMAL_SIGNIFICAND) :
        "Normal significand too large:" 
        + Long.toHexString(significand); }

    final long s = ((long) sign) << (EXPONENT_BITS + STORED_SIGNIFICAND_BITS);
    final long e = ((long) be) << STORED_SIGNIFICAND_BITS;
    final long t = significand & STORED_SIGNIFICAND_MASK;
    assert (0L == (s & e & t));
    final double x = longBitsToDouble(s | e | t);
    return x; }

  //--------------------------------------------------------------
  /**
   * @param negative boolean version of sign bit
   * @param exponent unbiased, must be in 
   * [{@link #SUBNORMAL_EXPONENT},
   * {@link #INFINITE_OR_NAN_EXPONENT}]
   * @param significand 
   * Must be in [0,{@link #MAX_SUBNORMAL_SIGNIFICAND}] 
   * if <code>exponent</code> is {@link #SUBNORMAL_EXPONENT} or
   * {@link #INFINITE_OR_NAN_EXPONENT}].
   * Must be in 
   * [{@link #MIN_NORMAL_SIGNIFICAND},{@link #MAX_NORMAL_SIGNIFICAND}]
   * if <code>exponent</code> is in 
   * [{@link Double#MIN_EXPONENT}, {@link Double#MAX_EXPONENT}].
   * @return equivalent <code>double</code> value
   */

  public static final double makeDouble (final boolean negative,
                                         final int exponent,
                                         final long significand) {
    return makeDouble(
      negative ? 1 : 0, 
        exponent,
        significand); }

  //--------------------------------------------------------------
  /** The largest integer that can be represented exactly 
   * in <code>double</code>.
   */

  public static final double MAX_INTEGER = 9007199254740992D;

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
  public final Supplier generator (final Map options) {
    final UniformRandomProvider urp = Set.urp(options);
    final Generator g = Doubles.finiteGenerator(urp);
    return 
      new Supplier () {
      @Override
      public final Object get () { return g.next(); } }; }

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
  // generators
  //--------------------------------------------------------------

  public static final Generator 
  subnormalGenerator (final int n,
                      final UniformRandomProvider urp) {
    return new Generator () {
      final Generator g = subnormalGenerator(urp);
      @Override
      public final Object next () {
        final double[] z = new double[n];
        for (int i=0;i<n;i++) { z[i] = g.nextDouble(); }
        return z; } }; }

  public static final Generator 
  subnormalGenerator (final UniformRandomProvider urp) {
    return subnormalGenerator(urp,Double.MAX_EXPONENT); }

  public static final Generator 
  subnormalGenerator (final int n,
                      final UniformRandomProvider urp,
                      final int eMax) {
    return new Generator () {
      final Generator g = subnormalGenerator(urp,eMax);
      @Override
      public final Object next () {
        final double[] z = new double[n];
        for (int i=0;i<n;i++) { z[i] = g.nextDouble(); }
        return z; } }; }

  public static final Generator 
  subnormalGenerator (final UniformRandomProvider urp,
                      final int eMax) {
    final Generator d = generator(urp,eMax);
    return new Generator () {
      @Override
      public final double nextDouble () {
        // TODO: fix infinite loop
        for (;;) {
          final double x = d.nextDouble();
          if ((Double.isFinite(x)) && (! isNormal(x))) { 
            return x; } } } 
      @Override
      public final Object next () {
        return Double.valueOf(nextDouble()); } }; }

  public static final Generator 
  normalGenerator (final int n,
                   final UniformRandomProvider urp) {
    return new Generator () {
      final Generator g = normalGenerator(urp);
      @Override
      public final Object next () {
        final double[] z = new double[n];
        for (int i=0;i<n;i++) { z[i] = g.nextDouble(); }
        return z; } }; }

  public static final Generator 
  normalGenerator (final UniformRandomProvider urp) {
    return normalGenerator(urp,Double.MAX_EXPONENT); }

  public static final Generator 
  normalGenerator (final int n,
                   final UniformRandomProvider urp,
                   final int eMax) {
    return new Generator () {
      final Generator g = normalGenerator(urp,eMax);
      @Override
      public final Object next () {
        final double[] z = new double[n];
        for (int i=0;i<n;i++) { z[i] = g.nextDouble(); }
        return z; } }; }

  public static final Generator 
  normalGenerator (final UniformRandomProvider urp,
                   final int eMax) {
    final Generator d = generator(urp,eMax);
    return new Generator () {
      @Override
      public final double nextDouble () {
        // TODO: fix infinite loop
        for (;;) {
          final double x = d.nextDouble();
          if (Double.isFinite(x) && isNormal(x)) { 
            return x; } } } 
      @Override
      public final Object next () {
        return Double.valueOf(nextDouble()); } }; }

  public static final Generator 
  finiteGenerator (final int n,
                   final UniformRandomProvider urp) {
    return finiteGenerator(n,urp,Double.MAX_EXPONENT); }

  public static final Generator 
  finiteGenerator (final int n,
                   final UniformRandomProvider urp,
                   final int delta) {
    return new Generator () {
      final Generator g = finiteGenerator(urp,delta);
      @Override
      public final Object next () {
        final double[] z = new double[n];
        for (int i=0;i<n;i++) { z[i] = g.nextDouble(); }
        return z; } }; }

  public static final Generator 
  finiteGenerator (final UniformRandomProvider urp) {
    return finiteGenerator(urp,Double.MAX_EXPONENT); }

  public static final Generator 
  finiteGenerator (final UniformRandomProvider urp,
                   final int eMax) {
    final Generator d = generator(urp,eMax);
    return new Generator () {
      @Override
      public final double nextDouble () {
        // TODO: fix infinite loop
        for (;;) {
          final double x = d.nextDouble();
          if (Double.isFinite(x)) { return x; } } } 
      @Override
      public final Object next () {
        return Double.valueOf(nextDouble()); } }; }

  public static final Generator 
  generator (final int n,
             final UniformRandomProvider urp) {
    return 
      generator(n,urp,SUBNORMAL_EXPONENT,Double.MAX_EXPONENT+1); }

  public static final Generator 
  generator (final int n,
             final UniformRandomProvider urp,
             final int eMax) {
    return generator(n,urp,SUBNORMAL_EXPONENT,eMax); }

  public static final Generator 
  generator (final int n,
             final UniformRandomProvider urp,
             final int eMin,
             final int eMax) {
    return new Generator () {
      final Generator g = generator(urp,eMin,eMax);
      @Override
      public final Object next () {
        final double[] z = new double[n];
        for (int i=0;i<n;i++) { z[i] = g.nextDouble(); }
        return z; } }; }

  public static final Generator 
  generator (final UniformRandomProvider urp) {
    return 
      generator(urp,SUBNORMAL_EXPONENT,Double.MAX_EXPONENT+1); }

  public static final Generator 
  generator (final UniformRandomProvider urp,
             final int eMax) {
    return generator(urp,SUBNORMAL_EXPONENT,eMax); }

  public static final Generator 
  generator (final UniformRandomProvider urp,
             final int eMin,
             // exclusive
             final int eMax) {
    assert eMin >= SUBNORMAL_EXPONENT;
    assert eMax <= INFINITE_OR_NAN_EXPONENT + 1;
    assert eMin < eMax;
    return new Generator () {
      final int eRan = eMax-eMin;
      @Override
      public final double nextDouble () { 
        final int s = urp.nextInt(2);
        final int d = urp.nextInt(eRan);
        final int e = d + eMin; // unbiased exponent
        assert (eMin <= e) && (e < eMax); 
        final long u = urp.nextLong() 
          & STORED_SIGNIFICAND_MASK;
        final long t; 
        if ((e == SUBNORMAL_EXPONENT)
          || (e == INFINITE_OR_NAN_EXPONENT)) {
          t = u; }
        else {
          t = u + MIN_NORMAL_SIGNIFICAND; }
        final double x = makeDouble(s,e,t); 
        return x;} 
      @Override
      public final Object next () {
        return Double.valueOf(nextDouble()); } }; }

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

