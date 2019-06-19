package xfp.java.numbers;

import static java.lang.Double.MAX_EXPONENT;
import static java.lang.Double.MIN_EXPONENT;
import static java.lang.Double.MIN_VALUE;
import static java.lang.Double.doubleToRawLongBits;
import static java.lang.Double.longBitsToDouble;
import static java.lang.Double.toHexString;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.ListSampler;
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

/** Utilities for <code>double</code>, <code>double[]</code>.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-06-19
 */
public final class Doubles implements Set {

  //--------------------------------------------------------------
  // TODO: cleanly separate stuff treating significand as
  // 53 bit integer from stuff treating is as a binary fraction
  // with one digit before the 'decimal' point.
  // In other words:
  // Most descriptions of floating point formats refer to the
  // significand as 1.xxx...xxx (with STORED_SIGNIFICAND_BITS x's).
  // It's often more convenient to treat it as an integer, which
  // requires us to subtract STORED_SIGNIFICAND_BITS from the
  // exponent to get the same
  // value.
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

  /** Unbiased exponent for subnormal numbers with fractional
   * significand, with implied
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

  /** Inclusive lower bound on exponents for rounding to double,
   * treating the significand as an integer.
   */
  public static final int MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND =
    Double.MIN_EXPONENT - STORED_SIGNIFICAND_BITS;

  /** Exclusive upper bound on exponents for rounding to double
   * treating the significand as an integer.
   */
  public static final int MAXIMUM_EXPONENT_INTEGRAL_SIGNIFICAND =
    (Double.MAX_EXPONENT - STORED_SIGNIFICAND_BITS) + 1;

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

  /** Remember 0.0 can be negative. */
  public static final boolean nonNegative (final double x) {
    return  0 == signBit(x); }

  //--------------------------------------------------------------

  /** Actual STORED_SIGNIFICAND_BITS stored bits, without the
   * implied leading 1 bit for normal numbers.
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
        >>> STORED_SIGNIFICAND_BITS); }

  //--------------------------------------------------------------

  public static final int unbiasedExponent (final double x) {
    return biasedExponent(x) - EXPONENT_BIAS; }

  //--------------------------------------------------------------
  /** Exponent if significand is treated as an integer, not a
   * binary fraction.
   */

  public static final int exponent (final double x) {
    // subnormal numbers have an exponent one less that what it
    // should really be, as a way of coding the initial zero bit
    // in the significand
    return
      Math.max(
        unbiasedExponent(x) - STORED_SIGNIFICAND_BITS,
        MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND); }

  //--------------------------------------------------------------

  public static final boolean isEven (final double x) {
    return
      // TODO: is excluding non-finite right?
      // otherwise infinities are even, NaN odd
      Double.isFinite(x)
      &&
      (0L == (significand(x) & 0x1L)); }

  //--------------------------------------------------------------

  public static final boolean isNormal (final double x) {
    final int be = biasedExponent(x);
    return (0.0 == x) || ((0 != be) && (0x7FF != be)); }

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
   * Treated as <code>significand *
   * 2<sup>-STORED_SIGNIFICAND_BITS</sup></code>.
   * @return <code>(-1)<sup>sign</sup> * 2<sup>exponent</sup>
   * * significand * 2<sup>-{@link #STORED_SIGNIFICAND_BITS}</sup></code>
   */

  public static final double mergeBits (final int sign,
                                        final int exponent,
                                        final long significand) {

    assert ((0 == sign) || (1 ==sign)) :
      "Invalid sign bit:" + sign;

    assert (SUBNORMAL_EXPONENT <= exponent)  :
      "(unbiased) exponent too small: " + exponent;

    assert (exponent <= MAX_EXPONENT) :
      "(unbiased) exponent too large: " + exponent;

    final int be = exponent + EXPONENT_BIAS;

    assert (0 <= be) :
      "Negative exponent:" + Integer.toHexString(be) + " : " + be
      + "\n" + SUBNORMAL_EXPONENT + "<=" + exponent + "<="
      + MAX_EXPONENT
      + "\n" + MIN_VALUE + " " + toHexString(MIN_VALUE)
      + "\n" + EXPONENT_BIAS;

    assert (be <= MAXIMUM_BIASED_EXPONENT) :
      "Exponent too large:" + Integer.toHexString(be) +
      ">" + Integer.toHexString(MAXIMUM_BIASED_EXPONENT);

    if (SUBNORMAL_EXPONENT == exponent) {
      assert (0 <= significand) :
        "subnormal significand too small:"
        + Long.toHexString(significand);
      assert significand <= MAX_SUBNORMAL_SIGNIFICAND :
        "subnormal significand too large:"
        + Long.toHexString(significand); }
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
        "Normal significand too small: 0x"
        + Long.toHexString(significand)
        + "p" + exponent;
      assert (significand <= MAX_NORMAL_SIGNIFICAND) :
        "Normal significand too large:"
        + Long.toHexString(significand)
        + "p" + exponent; }

    final long s = ((long) sign) <<
      (EXPONENT_BITS + STORED_SIGNIFICAND_BITS);
    final long e = ((long) be) << STORED_SIGNIFICAND_BITS;
    final long t = significand & STORED_SIGNIFICAND_MASK;
    assert (0L == (s & e & t));
    final double x = longBitsToDouble(s | e | t);
    return x; }

  public static final double 
  unsafeMergeBits (final int sign,
                   final int exponent,
                   final long significand) {
    final int be = exponent + EXPONENT_BIAS;
    final long s = ((long) sign) <<
      (EXPONENT_BITS + STORED_SIGNIFICAND_BITS);
    final long e = ((long) be) << STORED_SIGNIFICAND_BITS;
    final long t = significand & STORED_SIGNIFICAND_MASK;
    final double x = longBitsToDouble(s | e | t);
    return x; }

  //--------------------------------------------------------------
  private static final long SIGN_0 = 0x0L;
  private static final long SIGN_1 =
    (0x1L << (EXPONENT_BITS + STORED_SIGNIFICAND_BITS));

  /**
   * @param nonNegative sign 'bit'
   * @param exponent unbiased exponent, must be in
   * [{@link #SUBNORMAL_EXPONENT},{@link Double#MAX_EXPONENT}]
   * When {@link Double#MIN_EXPONENT} &le; <code>e</code>
   * &le; {@link Double#MAX_EXPONENT}, te result is a normal
   * number.
   * @param significand Must be in
   * [0,{@link #SIGNIFICAND_MASK}].
   * Treated as <code>significand *
   * 2<sup>-STORED_SIGNIFICAND_BITS</sup></code>.
   * @return <code>(-1)<sup>sign</sup> * 2<sup>exponent</sup>
   * * significand * 2<sup>-{@link #STORED_SIGNIFICAND_BITS}</sup></code>
   */

  public static final double unsafeBits (final boolean nonNegative,
                                         final int exponent,
                                         final long significand) {

    final long s = (nonNegative ? SIGN_0 : SIGN_1);
    final int be = exponent + EXPONENT_BIAS;
    final long e =
      (Numbers.UNSIGNED_MASK & be) << STORED_SIGNIFICAND_BITS;
    final long t = significand & STORED_SIGNIFICAND_MASK;
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
   * [{@link #MIN_NORMAL_SIGNIFICAND},
   * {@link #MAX_NORMAL_SIGNIFICAND}]
   * if <code>exponent</code> is in
   * [{@link Double#MIN_EXPONENT}, {@link Double#MAX_EXPONENT}].
   * @return <code>(-1)<sup>sign</sup> * 2<sup>exponent</sup>
   * * significand</code>
   */

  public static final double makeDouble (final boolean negative,
                                         final long significand,
                                         final int exponent) {

    final int e = exponent + STORED_SIGNIFICAND_BITS;
    if (e > MAX_EXPONENT) {
      return
        (negative
          ? Double.NEGATIVE_INFINITY
            : Double.POSITIVE_INFINITY); }

    return mergeBits(
      (negative ? 1 : 0),
      e,
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
    return that instanceof Doubles; }

  @Override
  public final String toString () { return "D"; }

  //--------------------------------------------------------------
  // generators
  //--------------------------------------------------------------

  // TODO: extend generators using function composition!!!

  private static final Generator
  arrayGenerator (final int n,
                  final Generator g) {
    return new GeneratorBase (g.name() + "-" + n) {
      @Override
      public final Object next () {
        final double[] z = new double[n];
        for (int i=0;i<n;i++) { z[i] = g.nextDouble(); }
        return z; } }; }

  private static final Generator
  arrayGenerator (final int m,
                  final int n,
                  final Generator g) {
    return new GeneratorBase (g.name() + "-" + m + "x" + n) {
      @Override
      public final Object next () {
        final double[][] z = new double[m][n];
        for (int i=0;i<m;i++) {
          for (int j=0;j<n;j++) { z[i][j] = g.nextDouble(); } }
        return z; } }; }

  //--------------------------------------------------------------

  private static final double[] concatenate (final double[] x0,
                                             final double[] x1) {
    final double[] x = new double[x0.length + x1.length];
    for (int i=0;i<x0.length;i++) { x[i] = x0[i]; }
    for (int i=0;i<x1.length;i++) { x[i+x0.length] = x1[i]; }
    return x; }

  private static final double[] minus (final double[] x) {
    final double[] y = new double[x.length];
    for (int i=0;i<x.length;i++) { y[i] = -x[i]; }
    return y; }

  /** exact sum of returned arrays is 0.0.
   * @param g must be a generator that returns
   * <code>double[]</code>.
   */

  public static final Generator
  zeroSumGenerator (final Generator g) {
    return new GeneratorBase ("zeroSum-" + g.name()) {
      @Override
      public final Object next () {
        final double[] z = (double[]) g.next();
        return concatenate(z,minus(z)); } }; }

  //--------------------------------------------------------------

  private static double[] shuffle (final double[] x,
                                   final UniformRandomProvider urp) {
    final double[] y = Arrays.copyOf(x,x.length);
    ListSampler.shuffle(urp,Arrays.asList(y));
    return y; }

  /** Shuffles the output of <code.g</code>.
   * @param g must be a generator that returns
   * <code>double[]</code>.
   */

  public static final Generator
  shuffledGenerator (final Generator g,
                     final UniformRandomProvider urp) {
    return new GeneratorBase ("shuffled-" + g.name()) {
      @Override
      public final Object next () {
        final double[] z = (double[]) g.next();
        return shuffle(z,urp); } }; }

  //--------------------------------------------------------------
  /** Sorts the output of <code.g</code>.
   * @param g must be a generator that returns
   * <code>double[]</code>.
   */

  public static final Generator
  sortedGenerator (final Generator g) {
    return new GeneratorBase ("sorted-" + g.name()) {
      @Override
      public final Object next () {
        final double[] z = (double[]) g.next();
        Arrays.sort(z);
        return z; } }; }

  //--------------------------------------------------------------
  /** Conventional 'uniform' distribution sampler, as 'uniform'
   * as it can be projected on <code>double</code>.
   * @param urp source of randomness
   * @param zmin inclusive min or generated numbers
   * @param zmax inclusive max of generated numbers
   * (TODO: should this be exclusive? commons rng doesn't
   * actually specify.)
   */
  public static final Generator
  uniformGenerator (final UniformRandomProvider urp,
                    final double zmin,
                    final double zmax) {
    return new GeneratorBase (
      "uniform-" + zmin + "-" + zmax) {
      private final ContinuousSampler s =
        new ContinuousUniformSampler(urp,zmin,zmax);
      @Override
      public final double nextDouble () {
        return s.sample(); } }; }

  /** Conventional 'uniform' distribution sampler, as 'uniform'
   * as it can be projected on <code>double</code>.
   * @param urp source of randomness
   * @param zmin inclusive min or generated numbers
   * @param zmax inclusive max of generated numbers
   * (TODO: should this be exclusive? commons rng doesn't
   * actually specify.)
   */
  public static final Generator
  uniformGenerator (final int n,
                    final UniformRandomProvider urp,
                    final double zmin,
                    final double zmax) {
    return arrayGenerator(n,uniformGenerator(urp,zmin,zmax)); }

  /** Conventional 'uniform' distribution sampler, as 'uniform'
   * as it can be projected on <code>double</code>.
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
                    final double zmin,
                    final double zmax) {
    return arrayGenerator(m,n,uniformGenerator(urp,zmin,zmax)); }

  //--------------------------------------------------------------

  public static final Generator
  gaussianGenerator (final NormalizedGaussianSampler ngs,
                     final double mu,
                     final double sigma) {
    return new GeneratorBase (
      "gaussian-" + mu + "-" + sigma) {
      private final ContinuousSampler s =
        new GaussianSampler(ngs,mu,sigma);
      @Override
      public final double nextDouble () { return s.sample(); } }; }

  public static final Generator
  gaussianGenerator (final UniformRandomProvider urp,
                     final double mu,
                     final double sigma) {
    return gaussianGenerator(
      //new BoxMullerNormalizedGaussianSampler(urp),
      //new MarsagliaNormalizedGaussianSampler(urp),​
      new ZigguratNormalizedGaussianSampler(urp),
      mu,sigma); }

  public static final Generator
  gaussianGenerator (final int n,
                     final UniformRandomProvider urp,
                     final double mu,
                     final double sigma) {
    return arrayGenerator(n,gaussianGenerator(urp,mu,sigma)); }

  public static final Generator
  gaussianGenerator (final int m,
                     final int n,
                     final UniformRandomProvider urp,
                     final double mu,
                     final double sigma) {
    return arrayGenerator(m,n,gaussianGenerator(urp,mu,sigma)); }

  //--------------------------------------------------------------

  public static final Generator
  exponentialGenerator (final UniformRandomProvider urp,
                        final double mu,
                        final double sigma) {
    return new GeneratorBase (
      "exponential-" + mu + "-" + sigma) {
      private final ContinuousSampler e =
        new AhrensDieterExponentialSampler(urp,sigma);
      @Override
      public final double nextDouble () {
        return (mu - sigma) + e.sample(); } }; }

  public static final Generator
  exponentialGenerator (final int n,
                        final UniformRandomProvider urp,
                        final double mu,
                        final double sigma) {
    return arrayGenerator(n,exponentialGenerator(urp,mu,sigma)); }

  public static final Generator
  exponentialGenerator (final int m,
                        final int n,
                        final UniformRandomProvider urp,
                        final double mu,
                        final double sigma) {
    return arrayGenerator(m,n,exponentialGenerator(urp,mu,sigma)); }

  //--------------------------------------------------------------

  public static final Generator
  laplaceGenerator (final UniformRandomProvider urp,
                    final double mu,
                    final double sigma) {
    return new GeneratorBase (
      "laplace-" + mu + "-" + sigma) {
      private final DiscreteSampler b =
        new DiscreteUniformSampler(urp,0,1);
      private final ContinuousSampler e =
        new AhrensDieterExponentialSampler(urp,sigma);
      @Override
      public final double nextDouble () {
        final int sign = (2*b.sample()) - 1;
        return mu + (sign*e.sample()); } }; }

  public static final Generator
  laplaceGenerator (final int n,
                    final UniformRandomProvider urp,
                    final double mu,
                    final double sigma) {
    return arrayGenerator(n,laplaceGenerator(urp,mu,sigma)); }

  public static final Generator
  laplaceGenerator (final int m,
                    final int n,
                    final UniformRandomProvider urp,
                    final double mu,
                    final double sigma) {
    return arrayGenerator(m,n,laplaceGenerator(urp,mu,sigma)); }

  //--------------------------------------------------------------
  // These treat (a subset of) doubles as a finite set, and sample
  // uniformly from the discrete elements of that set.
  //--------------------------------------------------------------

  public static final Generator
  subnormalGenerator (final UniformRandomProvider urp,
                      final int eMax) {
    final Generator d = generator(urp,eMax);
    return new GeneratorBase ("subnormal-" + eMax) {
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
  subnormalGenerator (final UniformRandomProvider urp) {
    return subnormalGenerator(urp,Double.MAX_EXPONENT); }

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
  /** Discretely uniform over 'normal' doubles,
   *  as opposed to 'subnormal' doubles. <
   *  em>Not gaussian!</em>
   * @param urp
   * @param eMax
   * @return
   */

  public static final Generator
  normalGenerator (final UniformRandomProvider urp,
                   final int eMax) {
    final Generator d = generator(urp,eMax);
    return new GeneratorBase ("normal-" + eMax) {
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
  normalGenerator (final UniformRandomProvider urp) {
    return normalGenerator(urp,Double.MAX_EXPONENT); }

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
    return new GeneratorBase ("finite-" + eMax) {
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
  finiteGenerator (final UniformRandomProvider urp) {
    return finiteGenerator(urp,Double.MAX_EXPONENT); }

  public static final Generator
  finiteGenerator (final int n,
                   final UniformRandomProvider urp,
                   final int eMax) {
    return arrayGenerator(n,finiteGenerator(urp,eMax)); }

  public static final Generator
  finiteGenerator (final int n,
                   final UniformRandomProvider urp) {
    return arrayGenerator(n,finiteGenerator(urp)); }

  public static final Generator
  finiteGenerator (final int m,
                   final int n,
                   final UniformRandomProvider urp,
                   final int eMax) {
    return arrayGenerator(m,n,finiteGenerator(urp,eMax)); }

  public static final Generator
  finiteGenerator (final int m,
                   final int n,
                   final UniformRandomProvider urp) {
    return arrayGenerator(m,n,finiteGenerator(urp)); }

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
      "double-" + eMin + "-" + eMax) {
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
        final double x = unsafeMergeBits(s,e,t);
        return x;}
      @Override
      public final Object next () {
        return Double.valueOf(nextDouble()); } }; }

  public static final Generator
  generator (final UniformRandomProvider urp,
             final int eMax) {
    return generator(urp,SUBNORMAL_EXPONENT,eMax); }

  public static final Generator
  generator (final UniformRandomProvider urp) {
    return
      generator(urp,SUBNORMAL_EXPONENT,Double.MAX_EXPONENT+1); }

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

