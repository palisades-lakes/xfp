package xfp.java.numbers;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.CollectionSampler;
import org.apache.commons.rng.sampling.distribution.ContinuousSampler;
import org.apache.commons.rng.sampling.distribution.ContinuousUniformSampler;

import xfp.java.algebra.OneSetOneOperation;
import xfp.java.algebra.OneSetTwoOperations;
import xfp.java.algebra.Set;
import xfp.java.prng.Generator;
import xfp.java.prng.GeneratorBase;
import xfp.java.prng.Generators;

/** The set of rational numbers represented by
 * <code>RationalFloatBI</code>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-05-23
 */
@SuppressWarnings("unchecked")
public final class RationalFloatsBI implements Set {

  //--------------------------------------------------------------
  // operations for algebraic structures over RationalFloatBIs.
  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  private final RationalFloatBI add (final RationalFloatBI q0,
                                   final RationalFloatBI q1) {
    assert contains(q0);
    assert contains(q1);
    return q0.add(q1); }

  public final BinaryOperator<RationalFloatBI> adder () {
    return new BinaryOperator<RationalFloatBI> () {
      @Override
      public final String toString () { return "BF.add()"; }
      @Override
      public final RationalFloatBI apply (final RationalFloatBI q0,
                                        final RationalFloatBI q1) {
        return RationalFloatsBI.this.add(q0,q1); } }; }

  //--------------------------------------------------------------

  @SuppressWarnings("static-method")
  public final RationalFloatBI additiveIdentity () {
    return RationalFloatBI.ZERO; }

  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  private final RationalFloatBI negate (final RationalFloatBI q) {
    assert contains(q);
    return q.negate(); }

  public final UnaryOperator<RationalFloatBI> additiveInverse () {
    return new UnaryOperator<RationalFloatBI> () {
      @Override
      public final String toString () { return "BF.negate()"; }
      @Override
      public final RationalFloatBI apply (final RationalFloatBI q) {
        return RationalFloatsBI.this.negate(q); } }; }

  //--------------------------------------------------------------

  private final RationalFloatBI multiply (final RationalFloatBI q0,
                                        final RationalFloatBI q1) {
    assert contains(q0);
    assert contains(q1);
    return q0.multiply(q1); }

  public final BinaryOperator<RationalFloatBI> multiplier () {
    return new BinaryOperator<RationalFloatBI>() {
      @Override
      public final String toString () { return "BF.multiply()"; }
      @Override
      public final RationalFloatBI apply (final RationalFloatBI q0,
                                        final RationalFloatBI q1) {
        return RationalFloatsBI.this.multiply(q0,q1); } }; }

  //--------------------------------------------------------------

  @SuppressWarnings("static-method")
  public final RationalFloatBI multiplicativeIdentity () {
    return RationalFloatBI.ONE; }

  //--------------------------------------------------------------

  private final RationalFloatBI reciprocal (final RationalFloatBI q) {
    assert contains(q);
    // only a partial inverse
    if (q.isZero()) { return null; }
    return q.reciprocal();  }

  public final UnaryOperator<RationalFloatBI> multiplicativeInverse () {
    return new UnaryOperator<RationalFloatBI> () {
      @Override
      public final String toString () { return "BF.inverse()"; }
      @Override
      public final RationalFloatBI apply (final RationalFloatBI q) {
        return RationalFloatsBI.this.reciprocal(q); } }; }

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    return element instanceof RationalFloatBI; }

  //--------------------------------------------------------------

  @Override
  public final BiPredicate equivalence () {
    return new BiPredicate<RationalFloatBI,RationalFloatBI>() {
      @Override
      public final boolean test (final RationalFloatBI q0,
                                 final RationalFloatBI q1) {
        return q0.equals(q1); } }; }

  //--------------------------------------------------------------

  /** Intended primarily for testing. Sample a random double
   * (see {@link xfp.java.prng.DoubleSampler})
   * and convert to <code></code>
   * with {@link #DOUBLE_P} probability;
   * otherwise return {@link #ZERO} or
   * {@link #ONE}, {@link #MINUS_ONE},
   * with equal probability (these are potential edge cases).
   */

  public static final Generator
  fromBigIntegerGenerator (final UniformRandomProvider urp) {
    final double dp = 0.9;
    return new GeneratorBase ("fromBigIntegerGenerator") {
      private final ContinuousSampler choose =
        new ContinuousUniformSampler(urp,0.0,1.0);
      private final Generator g0 =
        Generators.bigIntegerGenerator(urp);
      private final Generator g1 =
        Generators.bigIntegerGenerator(urp);
      private final Generator g2 =
        Generators.intGenerator(urp);
      private final CollectionSampler edgeCases =
        new CollectionSampler(
          urp,
          List.of(
            RationalFloatBI.ZERO,
            RationalFloatBI.ONE,
            RationalFloatBI.TWO,
            RationalFloatBI.TEN,
            RationalFloatBI.MINUS_ONE));
      @Override
      public Object next () {
        final boolean edge = choose.sample() > dp;
        if (edge) { return edgeCases.sample(); }
        return RationalFloatBI.valueOf(
          (BigInteger) g0.next(),
          (BigInteger) g1.next(),
          g2.nextInt()); } }; }

  // Is this characteristic of most inputs?
  public static final Generator
  fromDoubleGenerator (final UniformRandomProvider urp) {
    final double dp = 0.9;
    return new GeneratorBase ("fromDoubleGenerator") {
      private final ContinuousSampler choose =
        new ContinuousUniformSampler(urp,0.0,1.0);
      private final Generator g = Doubles.finiteGenerator(urp);
      private final CollectionSampler edgeCases =
        new CollectionSampler(
          urp,
          List.of(
            RationalFloatBI.ZERO,
            RationalFloatBI.ONE,
            RationalFloatBI.TWO,
            RationalFloatBI.TEN,
            RationalFloatBI.MINUS_ONE));
      @Override
      public Object next () {
        final boolean edge = choose.sample() > dp;
        if (edge) { return edgeCases.sample(); }
        return RationalFloatBI.valueOf(g.nextDouble()); } }; }

  // Is this characteristic of most inputs?
  public static final Generator
  generator (final UniformRandomProvider urp) {
    return fromDoubleGenerator(urp); }

  public static final Generator
  fromDoubleGenerator (final int n,
                       final UniformRandomProvider urp) {
    return new GeneratorBase ("fromDoubleGenerator:" + n) {
      final Generator g = fromDoubleGenerator(urp);
      @Override
      public final Object next () {
        final RationalFloatBI[] z = new RationalFloatBI[n];
        for (int i=0;i<n;i++) { z[i] = (RationalFloatBI) g.next(); }
        return z; } }; }

  public static final Generator
  generator (final int n,
             final UniformRandomProvider urp) {
    return new GeneratorBase ("rationalGenerator:" + n) {
      final Generator g = generator(urp);
      @Override
      public final Object next () {
        final RationalFloatBI[] z = new RationalFloatBI[n];
        for (int i=0;i<n;i++) { z[i] = (RationalFloatBI) g.next(); }
        return z; } }; }

  // TODO: determine which generator from options.
  @Override
  public final Supplier generator (final Map options) {
    final UniformRandomProvider urp = Set.urp(options);
    final Generator g = generator(urp);
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
    return that instanceof RationalFloatsBI; }

  @Override
  public final String toString () { return "RF"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------


  private RationalFloatsBI () { }

  private static final RationalFloatsBI SINGLETON =
    new RationalFloatsBI();

  public static final RationalFloatsBI get () { return SINGLETON; }

  //--------------------------------------------------------------

  public static final OneSetOneOperation ADDITIVE_MAGMA =
    OneSetOneOperation.magma(get().adder(),get());

  public static final OneSetOneOperation MULTIPLICATIVE_MAGMA =
    OneSetOneOperation.magma(get().multiplier(),get());

  public static final OneSetTwoOperations FIELD =
    OneSetTwoOperations.field(
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

