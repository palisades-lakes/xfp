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

/** The set of arbitrary precision floating point numbers 
 * represented by <code>BigFloat</code>
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-17
 */
@SuppressWarnings("unchecked")
public final class BigFloats implements Set {

  //--------------------------------------------------------------
  // operations for algebraic structures over BigFloats.
  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  private final BigFloat add (final BigFloat q0, 
                              final BigFloat q1) {
    assert contains(q0);
    assert contains(q1);
    return q0.add(q1); } 

  public final BinaryOperator<BigFloat> adder () {
    return new BinaryOperator<BigFloat> () {
      @Override
      public final String toString () { return "BF.add()"; }
      @Override
      public final BigFloat apply (final BigFloat q0, 
                                   final BigFloat q1) {
        return BigFloats.this.add(q0,q1); } }; }

  //--------------------------------------------------------------

  @SuppressWarnings("static-method")
  public final BigFloat additiveIdentity () {
    return BigFloat.ZERO; }

  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  private final BigFloat negate (final BigFloat q) {
    assert contains(q);
    return q.negate(); } 

  public final UnaryOperator<BigFloat> additiveInverse () {
    return new UnaryOperator<BigFloat> () {
      @Override
      public final String toString () { return "BF.negate()"; }
      @Override
      public final BigFloat apply (final BigFloat q) {
        return BigFloats.this.negate(q); } }; }

  //--------------------------------------------------------------

  private final BigFloat multiply (final BigFloat q0, 
                                   final BigFloat q1) {
    assert contains(q0);
    assert contains(q1);
    return q0.multiply(q1); } 

  public final BinaryOperator<BigFloat> multiplier () {
    return new BinaryOperator<BigFloat>() {
      @Override
      public final String toString () { return "BF.multiply"; }
      @Override
      public final BigFloat apply (final BigFloat q0, 
                                   final BigFloat q1) {
        return BigFloats.this.multiply(q0,q1); } }; }

  //--------------------------------------------------------------

  @SuppressWarnings("static-method")
  public final BigFloat multiplicativeIdentity () {
    return BigFloat.ONE; }

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    return element instanceof BigFloat; }

  //--------------------------------------------------------------

  @Override
  public final BiPredicate equivalence () {
    return new BiPredicate<BigFloat,BigFloat>() {
      @Override
      public final boolean test (final BigFloat q0, 
                                 final BigFloat q1) {
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
      private final Generator g2 = 
        Generators.intGenerator(urp);
      private final CollectionSampler edgeCases = 
        new CollectionSampler(
          urp,
          List.of(
            BigFloat.ZERO,
            BigFloat.ONE,
            BigFloat.TWO,
            BigFloat.TEN,
            BigFloat.MINUS_ONE));
      @Override
      public Object next () { 
        final boolean edge = choose.sample() > dp;
        if (edge) { return edgeCases.sample(); }
        return BigFloat.valueOf(
          (BigInteger) g0.next(),g2.nextInt()); } }; }

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
            BigFloat.ZERO,
            BigFloat.ONE,
            BigFloat.TWO,
            BigFloat.TEN,
            BigFloat.MINUS_ONE));
      @Override
      public Object next () { 
        final boolean edge = choose.sample() > dp;
        if (edge) { return edgeCases.sample(); }
        return BigFloat.valueOf(g.nextDouble()); } }; }

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
        final BigFloat[] z = new BigFloat[n];
        for (int i=0;i<n;i++) { z[i] = (BigFloat) g.next(); }
        return z; } }; }

  public static final Generator 
  generator (final int n,
             final UniformRandomProvider urp) {
    return new GeneratorBase ("rationalGenerator:" + n) {
      final Generator g = generator(urp);
      @Override
      public final Object next () {
        final BigFloat[] z = new BigFloat[n];
        for (int i=0;i<n;i++) { z[i] = (BigFloat) g.next(); }
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
    return that instanceof BigFloats; }

  @Override
  public final String toString () { return "BF"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private BigFloats () { }

  private static final BigFloats SINGLETON = new BigFloats();

  public static final BigFloats get () { return SINGLETON; } 

  //--------------------------------------------------------------

  public static final OneSetOneOperation ADDITIVE_MAGMA = 
    OneSetOneOperation.magma(get().adder(),get());

  public static final OneSetOneOperation MULTIPLICATIVE_MAGMA = 
    OneSetOneOperation.magma(get().multiplier(),get());

  public static final OneSetTwoOperations RING = 
    OneSetTwoOperations.commutativeRing(
      get().adder(),
      get().additiveIdentity(),
      get().additiveInverse(),
      get().multiplier(),
      get().multiplicativeIdentity(),
      get());

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
