package xfp.java.numbers;

import static java.lang.Float.MAX_EXPONENT;
import static java.lang.Float.MIN_EXPONENT;
import static java.lang.Float.MIN_VALUE;
import static java.lang.Float.floatToIntBits;
import static java.lang.Float.floatToRawIntBits;
import static java.lang.Float.toHexString;

import java.math.BigInteger;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.AhrensDieterExponentialSampler;
import org.apache.commons.rng.sampling.distribution.ContinuousSampler;
import org.apache.commons.rng.sampling.distribution.ContinuousUniformSampler;
import org.apache.commons.rng.sampling.distribution.DiscreteSampler;
import org.apache.commons.rng.sampling.distribution.DiscreteUniformSampler;
import org.apache.commons.rng.sampling.distribution.GaussianSampler;
import org.apache.commons.rng.sampling.distribution.NormalizedGaussianSampler;
import org.apache.commons.rng.sampling.distribution.ZigguratNormalizedGaussianSampler;

import xfp.java.algebra.OneSetOneOperation;
import xfp.java.algebra.OneSetTwoOperations;
import xfp.java.algebra.Set;
import xfp.java.prng.Generator;
import xfp.java.prng.GeneratorBase;

/** Utilities for <code>float</code>, <code>float[]</code>.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-05-11
 */
public final class Floats implements Set {

  //--------------------------------------------------------------
  // TODO: cleanly separate stuff treating significand as
  // integer from stuff treating is as a binary fraction
  // with one digit before the 'decimal' point.
  // In other words:
  // Most descriptions of floating point formats refer to the
  // significand as 1.xxx...xxx
  // It's often more convenient to treat it as an integer, which
  // requires us to subtract 23 from the exponent to get the same
  // value.
  //--------------------------------------------------------------

  public static final int SIGN_BITS = 1;
  public static final int EXPONENT_BITS = 8;
  public static final int STORED_SIGNIFICAND_BITS = 23;

  public static final int SIGNIFICAND_BITS =
    STORED_SIGNIFICAND_BITS + 1;

  public static final int STORED_SIGNIFICAND_MASK =
    (1 << STORED_SIGNIFICAND_BITS) - 1;

  public static final int NORMAL_SIGNIFICAND_MASK =
    (1 << STORED_SIGNIFICAND_BITS) | STORED_SIGNIFICAND_MASK;

  public static final int MIN_SUBNORMAL_SIGNIFICAND = 1;
  public static final int MAX_SUBNORMAL_SIGNIFICAND =
    STORED_SIGNIFICAND_MASK;
  public static final int MIN_NORMAL_SIGNIFICAND =
    MAX_SUBNORMAL_SIGNIFICAND + 1;
  public static final int MAX_NORMAL_SIGNIFICAND =
    NORMAL_SIGNIFICAND_MASK;

  public static final int SIGN_MASK =
    1 << (EXPONENT_BITS + STORED_SIGNIFICAND_BITS);

  public static final int EXPONENT_MASK =
    ((1 << EXPONENT_BITS) - 1) << STORED_SIGNIFICAND_BITS;

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

  /** Unbiased exponent for {@link Float#NaN},
   * {@link Float#NEGATIVE_INFINITY}, or
   * {@link Float#POSITIVE_INFINITY}.
   */
  public static final int INFINITE_OR_NAN_EXPONENT =
    MAX_EXPONENT + 1;

  //--------------------------------------------------------------
  /** Inclusive lower bound on exponents for rounding to float.
   */

  public static final int MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND =
    MIN_EXPONENT - STORED_SIGNIFICAND_BITS;

  /** Exclusive upper bound on exponents for rounding to float.
   */

  public static final int MAXIMUM_EXPONENT_INTEGRAL_SIGNIFICAND =
    (MAX_EXPONENT - STORED_SIGNIFICAND_BITS) + 1;

  //--------------------------------------------------------------
  //    static {
  //      assert ((~0L) == (SIGN_MASK | EXPONENT_MASK | SIGNIFICAND_MASK));
  //      assert (0L == (SIGN_MASK & EXPONENT_MASK));
  //      assert (0L == (EXPONENT_MASK & SIGNIFICAND_MASK));
  //      assert (0L == (SIGN_MASK & SIGNIFICAND_MASK));
  //    }
  //--------------------------------------------------------------

  public static final int signBit (final float x) {
    return  (SIGN_MASK & floatToRawIntBits(x))
      >> (EXPONENT_BITS + STORED_SIGNIFICAND_BITS); }

  public static final boolean nonNegative (final float x) {
    return  0 == signBit(x); }

  //--------------------------------------------------------------
  /** Actual 23 stored bits, without the implied leading 1 bit
   * for normal numbers.
   */

  public static final int significandLowBits (final float x) {
    return STORED_SIGNIFICAND_MASK & floatToRawIntBits(x); }

  /** 24 bit significand.
   * Adds the implicit leading bit to the stored bits,
   * if there is one.
   */

  public static final int significand (final float x) {
    final int t = significandLowBits(x);
    // signed zero or subnormal
    if (biasedExponent(x) == 0) { return t; }
    return t + STORED_SIGNIFICAND_MASK + 1; }

  //--------------------------------------------------------------

  public static final int biasedExponent (final float x) {
    return
      ((EXPONENT_MASK & floatToRawIntBits(x))
        >> STORED_SIGNIFICAND_BITS); }

  //--------------------------------------------------------------

  public static final int unbiasedExponent (final float x) {
    return biasedExponent(x) - EXPONENT_BIAS; }

  //--------------------------------------------------------------
  /** Exponent if significand is treated as an integer, not a
   * binary fraction.
   */
  public static final int exponent (final float x) {
    // subnormal numbers have an exponent one less that what it
    // should really be, as a way of coding the initial zero bit
    // in the significand
    return
      Math.max(
        unbiasedExponent(x) - STORED_SIGNIFICAND_BITS,
        MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND); }

  //--------------------------------------------------------------

  public static final boolean isEven (final float x) {
    return
      // TODO: is excluding non-finite right?
      // otherwise infinities are even, NaN odd
      Float.isFinite(x)
      &&
      (0 == (significand(x) & 0x1)); }

  //--------------------------------------------------------------

  public static final boolean isNormal (final float x) {
    final int be = biasedExponent(x);
    return (0.0 == x) || ((0 != be) && (0xFF != be)); }

  //--------------------------------------------------------------
  // TODO: change exponent ranges so that significand can be taken
  // as its actual value?

  /**
   * @param sign sign bit, must be 0 or 1
   * @param exponent unbiased exponent, must be in
   * [{@link #SUBNORMAL_EXPONENT},{@link Float#MAX_EXPONENT}]
   * When {@link Float#MIN_EXPONENT} &le; <code>e</code>
   * &le; {@link Float#MAX_EXPONENT}, te result is a normal
   * number.
   * @param significand Must be in
   * [0,{@link #STORED_SIGNIFICAND_MASK}].
   * Treated as <code>significand * 2<sup>-52</sup></code>.
   * @return <code>(-1)<sup>sign</sup> * 2<sup>exponent</sup>
   * * significand * 2<sup>-{@link #STORED_SIGNIFICAND_BITS}</sup></code>
   */

  public static final float mergeBits (final int sign,
                                       final int exponent,
                                       final int significand) {

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
        "subnormal significand too small:" + Integer.toHexString(significand);
      assert significand <= MAX_SUBNORMAL_SIGNIFICAND :
        "subnormal significand too large:" + Integer.toHexString(significand); }
    else if (INFINITE_OR_NAN_EXPONENT == exponent) {
      // no leading 1 bit for infinity or NaN
      assert (0 <= significand) :
        "infinite or NaN significand too small:"
        + Integer.toHexString(significand);
      assert significand <= MAX_SUBNORMAL_SIGNIFICAND :
        "infinite or NaN significand too large:"
        + Integer.toHexString(significand); }
    else { // normal numbers
      assert (MIN_NORMAL_SIGNIFICAND <= significand) :
        "Normal significand too small:"
        + Integer.toHexString(significand);
      assert (significand <= MAX_NORMAL_SIGNIFICAND) :
        "Normal significand too large:"
        + Integer.toHexString(significand); }

    final int s = (sign) << (EXPONENT_BITS + STORED_SIGNIFICAND_BITS);
    final int e = (be) << STORED_SIGNIFICAND_BITS;
    final int t = significand & STORED_SIGNIFICAND_MASK;
    assert (0 == (s & e & t));
    final float x = Float.intBitsToFloat(s | e | t);
    return x; }

  //--------------------------------------------------------------
  private static final int SIGN_0 = 0x0;
  private static final int SIGN_1 =
    (0x1 << (EXPONENT_BITS + STORED_SIGNIFICAND_BITS));

  /**
   * @param exponent unbiased exponent, must be in
   * [{@link #SUBNORMAL_EXPONENT},{@link Float#MAX_EXPONENT}]
   * When {@link Float#MIN_EXPONENT} &le; <code>e</code>
   * &le; {@link Float#MAX_EXPONENT}, te result is a normal
   * number.
   * @param significand Must be in
   * [0,{@link #STORED_SIGNIFICAND_MASK}].
   * Treated as <code>significand * 2<sup>-52</sup></code>.
   * @return <code>(-1)<sup>sign</sup> * 2<sup>exponent</sup>
   * * significand * 2<sup>-{@link #STORED_SIGNIFICAND_BITS}</sup></code>
   */

  public static final float unsafeBits (final boolean nonNegative,
                                        final int exponent,
                                        final int significand) {
    final int s = (nonNegative ? SIGN_0 : SIGN_1);
    final int be = exponent + EXPONENT_BIAS;
    final int e = be << STORED_SIGNIFICAND_BITS;
    final int t = significand & STORED_SIGNIFICAND_MASK;
    final float x = Float.intBitsToFloat(s | e | t);
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
   * [{@link Float#MIN_EXPONENT}, {@link Float#MAX_EXPONENT}].
   * @return <code>(-1)<sup>sign</sup> * 2<sup>exponent</sup>
   * * significand</code>
   */

  public static final float makeFloat (final boolean negative,
                                       final int exponent,
                                       final int significand) {
    return mergeBits(
      (negative ? 1 : 0),
      exponent + STORED_SIGNIFICAND_BITS,
      significand); }

  //--------------------------------------------------------------
  // operations for algebraic structures over
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
    return new BinaryOperator<> () {
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
    return new UnaryOperator<> () {
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
    return new BinaryOperator<>() {
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
    return new UnaryOperator<> () {
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
  public final Supplier generator (final Map options) {
    final UniformRandomProvider urp = Set.urp(options);
    final Generator g = finiteGenerator(urp);
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
    return that instanceof Floats; }

  @Override
  public final String toString () { return "D"; }

  //--------------------------------------------------------------
  /** From apache commons math4 BigFraction.
   * <p>
   * Create a fraction given the float value.
   * <p>
   * This constructor behaves <em>differently</em> from
   * {@link #BigFraction(double, double, int)}. It converts the
   * float value exactly, considering its internal bits
   * representation. This works for all values except NaN and
   * infinities and does not requires any loop or convergence
   * threshold.
   * </p>
   * @see #BigFraction(double, double, int)
   * @param x the float value to convert to a fraction.
   * @exception IllegalArgumentException if value is not finite
   */

  public static final BigInteger[] toRatio (final float x)  {

    if (! Float.isFinite(x)) {
      throw new IllegalArgumentException(
        "toRatio"  + " cannot handle "+ x); }

    final BigInteger numerator;
    final BigInteger denominator;

    // compute m and k such that x = m * 2^k
    final int bits     = floatToIntBits(x);
    final int sign     = bits & SIGN_MASK;
    final int exponent = bits & EXPONENT_MASK;
    int m              = bits & STORED_SIGNIFICAND_MASK;
    if (exponent == 0) { // subnormal
      if (0 == m) {
        numerator   = BigInteger.ZERO;
        denominator = BigInteger.ONE; }
      else {
        if (sign != 0) { m = -m; }
        numerator   = BigInteger.valueOf(m);
        denominator =
          BigInteger.ZERO.setBit(-MINIMUM_SUBNORMAL_EXPONENT); } }
    else { // normal
      // add the implicit most significant bit
      m |= (1L << STORED_SIGNIFICAND_BITS);
      if (sign != 0) { m = -m; }
      int k =
        ((exponent >> STORED_SIGNIFICAND_BITS)
          + MINIMUM_SUBNORMAL_EXPONENT) - 1;
      while (((m & (STORED_SIGNIFICAND_MASK - 1)) != 0)
        &&
        ((m & 0x1) == 0)) {
        m >>= 1;
    ++k; }
      if (k < 0) {
        numerator   = BigInteger.valueOf(m);
        denominator = BigInteger.ZERO.flipBit(-k); }
      else {
        numerator   = BigInteger.valueOf(m)
          .multiply(BigInteger.ZERO.flipBit(k));
        denominator = BigInteger.ONE; } }

    return new BigInteger[]{ numerator, denominator}; }

  //--------------------------------------------------------------
  // generators
  //--------------------------------------------------------------

  private static final Generator
  arrayGenerator (final int n,
                  final Generator g) {
    return new GeneratorBase (g.name() + ":" + n) {
      @Override
      public final Object next () {
        final float[] z = new float[n];
        for (int i=0;i<n;i++) { z[i] = g.nextFloat(); }
        return z; } }; }

  private static final Generator
  arrayGenerator (final int m,
                  final int n,
                  final Generator g) {
    return new GeneratorBase (g.name() + ":" + m + "x" + n) {
      @Override
      public final Object next () {
        final float[][] z = new float[m][n];
        for (int i=0;i<m;i++) {
          for (int j=0;j<n;j++) { z[i][j] = g.nextFloat(); } }
        return z; } }; }

  //--------------------------------------------------------------
  /** Conventional 'uniform' distribution sampler, as 'uniform'
   * as it can be projected on <code>float</code>.
   * @param urp source of randomness
   * @param zmin inclusive min or generated numbers
   * @param zmax inclusive max of generated numbers
   * (TODO: should this be exclusive? commons rng doesn't
   * actually specify.)
   */
  public static final Generator
  uniformGenerator (final UniformRandomProvider urp,
                    final float zmin,
                    final float zmax) {
    return new GeneratorBase (
      "uniformGenerator[" + zmin + "," + zmax + "]") {
      private final ContinuousSampler s =
        new ContinuousUniformSampler(urp,zmin,zmax);
      @Override
      public final float nextFloat () {
        return (float) s.sample(); } }; }

  /** Conventional 'uniform' distribution sampler, as 'uniform'
   * as it can be projected on <code>float</code>.
   * @param urp source of randomness
   * @param zmin inclusive min or generated numbers
   * @param zmax inclusive max of generated numbers
   * (TODO: should this be exclusive? commons rng doesn't
   * actually specify.)
   */
  public static final Generator
  uniformGenerator (final int n,
                    final UniformRandomProvider urp,
                    final float zmin,
                    final float zmax) {
    return arrayGenerator(n,uniformGenerator(urp,zmin,zmax)); }

  /** Conventional 'uniform' distribution sampler, as 'uniform'
   * as it can be projected on <code>float</code>.
   * @param urp source of randomness
   * @param zmin inclusive min or generated numbers
   * @param zmax inclusive max of generated numbers
   * (TODO: should this be exclusive? commons rng doesn't
   * actually specify.)
   */
  public static final Generator
  uniformGenerator (final int m,
                    final int n,
                    final UniformRandomProvider urp,
                    final float zmin,
                    final float zmax) {
    return arrayGenerator(m,n,uniformGenerator(urp,zmin,zmax)); }

  //--------------------------------------------------------------

  public static final Generator
  gaussianGenerator (final NormalizedGaussianSampler ngs,
                     final float mu,
                     final float sigma) {
    return new GeneratorBase (
      "gaussianGenerator(" + mu + "," + sigma + ")") {
      private final ContinuousSampler s =
        new GaussianSampler(ngs,mu,sigma);
      @Override
      public final float nextFloat () {
        return (float) s.sample(); } }; }

  public static final Generator
  gaussianGenerator (final UniformRandomProvider urp,
                     final float mu,
                     final float sigma) {
    return gaussianGenerator(
      //new BoxMullerNormalizedGaussianSampler(urp),
      //new MarsagliaNormalizedGaussianSampler(urp),​
      new ZigguratNormalizedGaussianSampler(urp),
      mu,sigma); }

  public static final Generator
  gaussianGenerator (final int n,
                     final UniformRandomProvider urp,
                     final float mu,
                     final float sigma) {
    return arrayGenerator(n,gaussianGenerator(urp,mu,sigma)); }

  public static final Generator
  gaussianGenerator (final int m,
                     final int n,
                     final UniformRandomProvider urp,
                     final float mu,
                     final float sigma) {
    return arrayGenerator(m,n,gaussianGenerator(urp,mu,sigma)); }

  //--------------------------------------------------------------

  public static final Generator
  laplaceGenerator (final UniformRandomProvider urp,
                    final float mu,
                    final float sigma) {
    return new GeneratorBase (
      "laplaceGenerator(" + mu + "," + sigma + ")") {
      private final DiscreteSampler b =
        new DiscreteUniformSampler(urp,0,1);
      private final ContinuousSampler e =
        new AhrensDieterExponentialSampler(urp,sigma);
      @Override
      public final float nextFloat () {
        final int sign = (2*b.sample()) - 1;
        return mu + (sign*((float) e.sample())); } }; }


  public static final Generator
  laplaceGenerator (final int n,
                    final UniformRandomProvider urp,
                    final float mu,
                    final float sigma) {
    return arrayGenerator(n,laplaceGenerator(urp,mu,sigma)); }

  public static final Generator
  laplaceGenerator (final int m,
                    final int n,
                    final UniformRandomProvider urp,
                    final float mu,
                    final float sigma) {
    return arrayGenerator(m,n,laplaceGenerator(urp,mu,sigma)); }

  //--------------------------------------------------------------
  // These treat (a subset of) floats as a finite set, and sample
  // uniformly from the discrete elements of that set.
  //--------------------------------------------------------------

  public static final Generator
  subnormalGenerator (final UniformRandomProvider urp,
                      final int eMax) {
    final Generator d = generator(urp,eMax);
    return new GeneratorBase ("subnormalGenerator:" + eMax) {
      @Override
      public final float nextFloat () {
        // TODO: fix infinite loop
        for (;;) {
          final float x = d.nextFloat();
          if ((Float.isFinite(x)) && (! isNormal(x))) {
            return x; } } }
      @Override
      public final Object next () {
        return Float.valueOf(nextFloat()); } }; }

  public static final Generator
  subnormalGenerator (final UniformRandomProvider urp) {
    return subnormalGenerator(urp,Float.MAX_EXPONENT); }

  public static final Generator
  subnormalGenerator (final int n,
                      final UniformRandomProvider urp,
                      final int eMax) {
    return arrayGenerator(n,subnormalGenerator(urp,eMax)); }

  public static final Generator
  subnormalGenerator (final int n,
                      final UniformRandomProvider urp) {
    return arrayGenerator(n,subnormalGenerator(urp)); }

  //--------------------------------------------------------------
  /** Discretely uniform over 'normal' floats,
   *  as opposed to 'subnormal' floats. <
   *  em>Not gaussian!</em>
   * @param urp
   * @param eMax
   * @return
   */

  public static final Generator
  normalGenerator (final UniformRandomProvider urp,
                   final int eMax) {
    final Generator d = generator(urp,eMax);
    return new GeneratorBase ("normalGenerator:" + eMax) {
      @Override
      public final float nextFloat () {
        // TODO: fix infinite loop
        for (;;) {
          final float x = d.nextFloat();
          if (Float.isFinite(x) && isNormal(x)) {
            return x; } } }
      @Override
      public final Object next () {
        return Float.valueOf(nextFloat()); } }; }

  public static final Generator
  normalGenerator (final UniformRandomProvider urp) {
    return normalGenerator(urp,Float.MAX_EXPONENT); }

  public static final Generator
  normalGenerator (final int n,
                   final UniformRandomProvider urp) {
    return arrayGenerator(n,normalGenerator(urp)); }

  public static final Generator
  normalGenerator (final int n,
                   final UniformRandomProvider urp,
                   final int eMax) {
    return arrayGenerator(n,normalGenerator(urp,eMax)); }

  //--------------------------------------------------------------

  public static final Generator
  finiteGenerator (final UniformRandomProvider urp,
                   final int eMax) {
    final Generator d = generator(urp,eMax);
    return new GeneratorBase ("finiteGenerator:" + eMax) {
      @Override
      public final float nextFloat () {
        // TODO: fix infinite loop
        for (;;) {
          final float x = d.nextFloat();
          if (Float.isFinite(x)) { return x; } } }
      @Override
      public final Object next () {
        return Float.valueOf(nextFloat()); } }; }

  public static final Generator
  finiteGenerator (final UniformRandomProvider urp) {
    return finiteGenerator(urp,Float.MAX_EXPONENT); }

  public static final Generator
  finiteGenerator (final int n,
                   final UniformRandomProvider urp,
                   final int eMax) {
    return arrayGenerator(n,normalGenerator(urp,eMax)); }

  public static final Generator
  finiteGenerator (final int n,
                   final UniformRandomProvider urp) {
    return arrayGenerator(n,normalGenerator(urp)); }

  public static final Generator
  finiteGenerator (final int m,
                   final int n,
                   final UniformRandomProvider urp,
                   final int eMax) {
    return arrayGenerator(m,n,normalGenerator(urp,eMax)); }

  public static final Generator
  finiteGenerator (final int m,
                   final int n,
                   final UniformRandomProvider urp) {
    return arrayGenerator(m,n,normalGenerator(urp)); }

  //--------------------------------------------------------------

  public static final Generator
  generator (final UniformRandomProvider urp,
             final int eMin,
             // exclusive
             final int eMax) {
    assert eMin >= SUBNORMAL_EXPONENT;
    assert eMax <= (INFINITE_OR_NAN_EXPONENT + 1);
    assert eMin < eMax;
    return new GeneratorBase (
      "floatGenerator(" + eMin + "," + eMax + ")") {
      final int eRan = eMax-eMin;
      @Override
      public final float nextFloat () {
        final int s = urp.nextInt(2);
        final int d = urp.nextInt(eRan);
        final int e = d + eMin; // unbiased exponent
        assert (eMin <= e) && (e < eMax);
        final int u = urp.nextInt()
          & STORED_SIGNIFICAND_MASK;
        final int t;
        if ((e == SUBNORMAL_EXPONENT)
          || (e == INFINITE_OR_NAN_EXPONENT)) {
          t = u; }
        else {
          t = u + MIN_NORMAL_SIGNIFICAND; }
        final float x = mergeBits(s,e,t);
        return x;}
      @Override
      public final Object next () {
        return Float.valueOf(nextFloat()); } }; }

  public static final Generator
  generator (final UniformRandomProvider urp,
             final int eMax) {
    return generator(urp,SUBNORMAL_EXPONENT,eMax); }

  public static final Generator
  generator (final UniformRandomProvider urp) {
    return
      generator(urp,SUBNORMAL_EXPONENT,MAX_EXPONENT+1); }

  public static final Generator
  generator (final int n,
             final UniformRandomProvider urp,
             final int eMin,
             final int eMax) {
    return arrayGenerator(n,generator(urp,eMin,eMax)); }

  public static final Generator
  generator (final int n,
             final UniformRandomProvider urp,
             final int eMax) {
    return arrayGenerator(n,generator(urp,eMax)); }

  public static final Generator
  generator (final int n,
             final UniformRandomProvider urp) {
    return arrayGenerator(n,generator(urp)); }

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

