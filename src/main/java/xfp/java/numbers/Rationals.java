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
import xfp.java.exceptions.Exceptions;
import xfp.java.prng.Generator;
import xfp.java.prng.GeneratorBase;
import xfp.java.prng.Generators;

/** The set of rational numbers represented by
 * <code>Rational</code>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-05-27
 */
@SuppressWarnings("unchecked")
public final class Rationals implements Set {

  //--------------------------------------------------------------

  public static final Rational[] toRational (final Number[] x) {
    final int n = x.length;
    final Rational[] y = new Rational[n];
    for (int i=0;i<n;i++) { y[i] = Rational.valueOf(x[i]); }
    return y; }

  public static final Rational[] toRational (final double[] x) {
    final int n = x.length;
    final Rational[] y = new Rational[n];
    for (int i=0;i<n;i++) { y[i] = Rational.valueOf(x[i]); }
    return y; }

  public static final Rational[] toRational (final float[] x) {
    final int n = x.length;
    final Rational[] y = new Rational[n];
    for (int i=0;i<n;i++) { y[i] = Rational.valueOf(x[i]); }
    return y; }

  public static final Rational[] toRational (final long[] x) {
    final int n = x.length;
    final Rational[] y = new Rational[n];
    for (int i=0;i<n;i++) { y[i] = Rational.valueOf(x[i]); }
    return y; }

  public static final Rational[] toRational (final int[] x) {
    final int n = x.length;
    final Rational[] y = new Rational[n];
    for (int i=0;i<n;i++) { y[i] = Rational.valueOf(x[i]); }
    return y; }

  public static final Rational[] toRational (final short[] x) {
    final int n = x.length;
    final Rational[] y = new Rational[n];
    for (int i=0;i<n;i++) { y[i] = Rational.valueOf(x[i]); }
    return y; }

  public static final Rational[] toRational (final byte[] x) {
    final int n = x.length;
    final Rational[] y = new Rational[n];
    for (int i=0;i<n;i++) { y[i] = Rational.valueOf(x[i]); }
    return y; }

  //--------------------------------------------------------------

  public static final Rational[] toRational (final Object x) {

    if (x instanceof Rational[]) { return (Rational[]) x; }

    if (x instanceof byte[]) {
      return toRational((byte[]) x); }

    if (x instanceof short[]) {
      return toRational((short[]) x); }

    if (x instanceof int[]) {
      return toRational((int[]) x); }

    if (x instanceof long[]) {
      return toRational((long[]) x); }

    if (x instanceof float[]) {
      return toRational((float[]) x); }

    if (x instanceof double[]) {
      return toRational((double[]) x); }

    if (x instanceof Number[]) {
      return toRational((Number[]) x); }

    throw Exceptions.unsupportedOperation(
      Rationals.class,"toRational",x); }

  //--------------------------------------------------------------
  // operations for algebraic structures over Rationals.
  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  private final Rational add (final Rational q0,
                              final Rational q1) {
    assert contains(q0);
    assert contains(q1);
    return q0.add(q1); }

  public final BinaryOperator<Rational> adder () {
    return new BinaryOperator<Rational> () {
      @Override
      public final String toString () { return "Rationals.add()"; }
      @Override
      public final Rational apply (final Rational q0,
                                   final Rational q1) {
        return Rationals.this.add(q0,q1); } }; }

  //--------------------------------------------------------------

  @SuppressWarnings("static-method")
  public final Rational additiveIdentity () {
    return Rational.ZERO; }

  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  private final Rational negate (final Rational q) {
    assert contains(q);
    return q.negate(); }

  public final UnaryOperator<Rational> additiveInverse () {
    return new UnaryOperator<Rational> () {
      @Override
      public final String toString () { return "Rationals.negate()"; }
      @Override
      public final Rational apply (final Rational q) {
        return Rationals.this.negate(q); } }; }

  //--------------------------------------------------------------

  private final Rational multiply (final Rational q0,
                                   final Rational q1) {
    assert contains(q0);
    assert contains(q1);
    return q0.multiply(q1); }

  public final BinaryOperator<Rational> multiplier () {
    return new BinaryOperator<Rational>() {
      @Override
      public final String toString () { 
        return "Rationals.multiply()"; }
      @Override
      public final Rational apply (final Rational q0,
                                   final Rational q1) {
        return Rationals.this.multiply(q0,q1); } }; }

  //--------------------------------------------------------------

  @SuppressWarnings("static-method")
  public final Rational multiplicativeIdentity () {
    return Rational.ONE; }

  //--------------------------------------------------------------

  private final Rational reciprocal (final Rational q) {
    assert contains(q);
    // only a partial inverse
    if (q.isZero()) { return null; }
    return q.reciprocal();  }

  public final UnaryOperator<Rational> multiplicativeInverse () {
    return new UnaryOperator<Rational> () {
      @Override
      public final String toString () { 
        return "Rationals.inverse()"; }
      @Override
      public final Rational apply (final Rational q) {
        return Rationals.this.reciprocal(q); } }; }

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    return element instanceof Rational; }

  //--------------------------------------------------------------

  @Override
  public final BiPredicate equivalence () {
    return new BiPredicate<Rational,Rational>() {
      @Override
      public final boolean test (final Rational q0,
                                 final Rational q1) {
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
        Generators.positiveBigIntegerGenerator(urp);
      private final CollectionSampler edgeCases =
        new CollectionSampler(
          urp,
          List.of(
            Rational.ZERO,
            Rational.ONE,
            Rational.TWO,
            Rational.TEN,
            Rational.MINUS_ONE));
      @Override
      public Object next () {
        final boolean edge = choose.sample() > dp;
        if (edge) { return edgeCases.sample(); }
        final BigInteger z0 = (BigInteger) g0.next();
        final BigInteger z1 = (BigInteger) g1.next();
        return RationalFloat.valueOf(
          0<=z0.signum(),
          UnNatural.valueOf(z0.abs()),
          UnNatural.valueOf(z1)); } }; } 

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
            Rational.ZERO,
            Rational.ONE,
            Rational.TWO,
            Rational.TEN,
            Rational.MINUS_ONE));
      @Override
      public Object next () {
        final boolean edge = choose.sample() > dp;
        if (edge) { return edgeCases.sample(); }
        return Rational.valueOf(g.nextDouble()); } }; }

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
        final Rational[] z = new Rational[n];
        for (int i=0;i<n;i++) { z[i] = (Rational) g.next(); }
        return z; } }; }

  public static final Generator
  generator (final int n,
             final UniformRandomProvider urp) {
    return new GeneratorBase ("rationalGenerator:" + n) {
      final Generator g = generator(urp);
      @Override
      public final Object next () {
        final Rational[] z = new Rational[n];
        for (int i=0;i<n;i++) { z[i] = (Rational) g.next(); }
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
    return that instanceof Rationals; }

  @Override
  public final String toString () { return "Rationals"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------


  private Rationals () { }

  private static final Rationals SINGLETON = new Rationals();

  public static final Rationals get () { return SINGLETON; }

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

