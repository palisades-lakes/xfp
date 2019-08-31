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
 * <code>RationalFloat</code>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-08-30
 */
@SuppressWarnings({"unchecked","static-method"})
public final class RationalFloats implements Set {

  //--------------------------------------------------------------
  // operations for algebraic structures over RationalFloats.
  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  private final RationalFloat add (final RationalFloat q0,
                                   final RationalFloat q1) {
    //assert contains(q0);
    //assert contains(q1);
    return q0.add(q1); }

  public final BinaryOperator<RationalFloat> adder () {
    return new BinaryOperator<> () {
      @Override
      public final String toString () { return "RF.add()"; }
      @Override
      public final RationalFloat apply (final RationalFloat q0,
                                        final RationalFloat q1) {
        return RationalFloats.this.add(q0,q1); } }; }

  //--------------------------------------------------------------

  public final RationalFloat additiveIdentity () {
    return RationalFloat.valueOf(0L); }

  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  private final RationalFloat negate (final RationalFloat q) {
    //assert contains(q);
    return q.negate(); }

  public final UnaryOperator<RationalFloat> additiveInverse () {
    return new UnaryOperator<> () {
      @Override
      public final String toString () { return "RF.negate()"; }
      @Override
      public final RationalFloat apply (final RationalFloat q) {
        return RationalFloats.this.negate(q); } }; }

  //--------------------------------------------------------------

  private final RationalFloat multiply (final RationalFloat q0,
                                        final RationalFloat q1) {
    //assert contains(q0);
    //assert contains(q1);
    return q0.multiply(q1); }

  public final BinaryOperator<RationalFloat> multiplier () {
    return new BinaryOperator<>() {
      @Override
      public final String toString () { return "RF.multiply()"; }
      @Override
      public final RationalFloat apply (final RationalFloat q0,
                                        final RationalFloat q1) {
        return RationalFloats.this.multiply(q0,q1); } }; }

  //--------------------------------------------------------------

  public final RationalFloat multiplicativeIdentity () {
    return RationalFloat.ONE; }

  //--------------------------------------------------------------

  private final RationalFloat reciprocal (final RationalFloat q) {
    //assert contains(q);
    // only a partial inverse
    if (q.isZero()) { return null; }
    return q.reciprocal();  }

  public final UnaryOperator<RationalFloat> multiplicativeInverse () {
    return new UnaryOperator<> () {
      @Override
      public final String toString () { return "RF.inverse()"; }
      @Override
      public final RationalFloat apply (final RationalFloat q) {
        return RationalFloats.this.reciprocal(q); } }; }

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    return element instanceof RationalFloat; }

  //--------------------------------------------------------------

  @Override
  public final BiPredicate equivalence () {
    return new BiPredicate<RationalFloat,RationalFloat>() {
      @Override
      public final boolean test (final RationalFloat q0,
                                 final RationalFloat q1) {
        return q0.equals(q1); } }; }

  //--------------------------------------------------------------

  /** Intended primarily for testing. Sample a random double
   * (see {@link xfp.java.prng.DoubleSampler})
   * and convert to <code></code>
   * with {@link #DOUBLE_P} probability;
   * otherwise return {@link #EMPTY} or
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
        Generators.positiveBigIntegerGenerator(urp);
      private final Generator g2 =
        Generators.intGenerator(urp);
      private final CollectionSampler edgeCases =
        new CollectionSampler(
          urp,
          List.of(
            RationalFloat.valueOf(0L),
            RationalFloat.ONE,
            RationalFloat.valueOf(2L),
            RationalFloat.valueOf(10L),
            RationalFloat.valueOf(-1L)));
      @Override
      public Object next () {
        final boolean edge = choose.sample() > dp;
        if (edge) { return edgeCases.sample(); }
        final BigInteger z0 = (BigInteger) g0.next();
        final BigInteger z1 = (BigInteger) g1.next();
        return RationalFloat.valueOf(
          0<=z0.signum(),
          NaturalLE.valueOf(z0.abs()),
          NaturalLE.valueOf(z1),
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
            RationalFloat.valueOf(0L),
            RationalFloat.ONE,
            RationalFloat.valueOf(2L),
            RationalFloat.valueOf(10L),
            RationalFloat.valueOf(-1L)));
      @Override
      public Object next () {
        final boolean edge = choose.sample() > dp;
        if (edge) { return edgeCases.sample(); }
        return RationalFloat.valueOf(g.nextDouble()); } }; }

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
        final RationalFloat[] z = new RationalFloat[n];
        for (int i=0;i<n;i++) { z[i] = (RationalFloat) g.next(); }
        return z; } }; }

  public static final Generator
  generator (final int n,
             final UniformRandomProvider urp) {
    return new GeneratorBase ("rationalGenerator:" + n) {
      final Generator g = generator(urp);
      @Override
      public final Object next () {
        final RationalFloat[] z = new RationalFloat[n];
        for (int i=0;i<n;i++) { z[i] = (RationalFloat) g.next(); }
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
    return that instanceof RationalFloats; }

  @Override
  public final String toString () { return "RF"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------


  private RationalFloats () { }

  private static final RationalFloats SINGLETON =
    new RationalFloats();

  public static final RationalFloats get () { return SINGLETON; }

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

