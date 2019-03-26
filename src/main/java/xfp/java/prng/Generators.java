package xfp.java.prng;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.CollectionSampler;
import org.apache.commons.rng.sampling.distribution.ContinuousSampler;
import org.apache.commons.rng.sampling.distribution.ContinuousUniformSampler;

import xfp.java.numbers.Doubles;
import xfp.java.numbers.Floats;
import xfp.java.numbers.Rationals;

/** Generators of primitives or Objects as zero-arity 'functions'
 * that return different values on each call.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-22
 */

@SuppressWarnings("unchecked")
public final class Generators {

  //--------------------------------------------------------------
  // TODO; Integer[], Double[], etc., generators?
  // TODO: move Generator definitions into Set classes

  public static final Generator 
  byteGenerator (final UniformRandomProvider urp) {
    return new Generator () {
      @Override
      public final byte nextByte () { return (byte) urp.nextInt(); } 
      @Override
      public final Object next () {
        return Byte.valueOf(nextByte()); } }; }

  public static final Generator 
  byteGenerator (final int n,
                 final UniformRandomProvider urp) {
    return new Generator () {
      final Generator g = byteGenerator(urp);
      @Override
      public final Object next () {
        final byte[] z = new byte[n];
        for (int i=0;i<n;i++) { z[i] = g.nextByte(); }
        return z; } }; }

  public static final Generator 
  shortGenerator (final UniformRandomProvider urp) {
    return new Generator () {
      @Override
      public final short nextShort () { return (short) urp.nextInt(); } 
      @Override
      public final Object next () {
        return Short.valueOf(nextShort()); } }; }

  public static final Generator 
  shortGenerator (final int n,
                  final UniformRandomProvider urp) {
    return new Generator () {
      final Generator g = shortGenerator(urp);
      @Override
      public final Object next () {
        final short[] z = new short[n];
        for (int i=0;i<n;i++) { z[i] = g.nextShort(); }
        return z; } }; }

  //--------------------------------------------------------------

  public static final Generator 
  intGenerator (final UniformRandomProvider urp) {
    return new Generator () {
      @Override
      public final int nextInt () { return urp.nextInt(); } 
      @Override
      public final Object next () {
        return Integer.valueOf(nextInt()); } }; }

  public static final Generator 
  intGenerator (final int n,
                final UniformRandomProvider urp) {
    return new Generator () {
      final Generator g = intGenerator(urp);
      @Override
      public final Object next () {
        final int[] z = new int[n];
        for (int i=0;i<n;i++) { z[i] = g.nextInt(); }
        return z; } }; }

  //--------------------------------------------------------------

  public static final Generator 
  positiveIntGenerator (final UniformRandomProvider urp) {
    return new Generator () {
      @Override
      public final int nextInt () { 
        // TODO: fix infinite loop?
        for (;;) {
          final int x = urp.nextInt();
          if (x != 0) { return Math.abs(x); } } }
      @Override
      public final Object next () {

        return Long.valueOf(nextLong()); } }; }

  public static final Generator 
  positiveIntGenerator (final int n,
                        final UniformRandomProvider urp) {
    return new Generator () {
      final Generator g = positiveLongGenerator(urp);
      @Override
      public final Object next () {
        final int[] z = new int[n];
        for (int i=0;i<n;i++) { z[i] = g.nextInt(); }
        return z; } }; }

  //--------------------------------------------------------------

  public static final Generator 
  longGenerator (final UniformRandomProvider urp) {
    return new Generator () {
      @Override
      public final long nextLong () { return urp.nextLong(); } 
      @Override
      public final Object next () {
        return Long.valueOf(nextLong()); } }; }

  public static final Generator 
  longGenerator (final int n,
                 final UniformRandomProvider urp) {
    return new Generator () {
      final Generator g = longGenerator(urp);
      @Override
      public final Object next () {
        final long[] z = new long[n];
        for (int i=0;i<n;i++) { z[i] = g.nextLong(); }
        return z; } }; }

  //--------------------------------------------------------------

  public static final Generator 
  positiveLongGenerator (final UniformRandomProvider urp) {
    return new Generator () {
      @Override
      public final long nextLong () { 
        // TODO: fix infinite loop?
        for (;;) {
          final long x = urp.nextLong();
          if (x != 0L) { return Math.abs(x); } } }
      @Override
      public final Object next () {

        return Long.valueOf(nextLong()); } }; }

  public static final Generator 
  positiveLongGenerator (final int n,
                         final UniformRandomProvider urp) {
    return new Generator () {
      final Generator g = positiveLongGenerator(urp);
      @Override
      public final Object next () {
        final long[] z = new long[n];
        for (int i=0;i<n;i++) { z[i] = g.nextLong(); }
        return z; } }; }

  //--------------------------------------------------------------

  public static final byte[] 
    nextBytes (final UniformRandomProvider urp,
               final int n) {
    final byte[] b = new byte[n];
    urp.nextBytes(b);
    return b; }

  /** Intended primarily for testing. <b>
   * Generate enough bytes to at least cover the range of 
   * <code>double</code> values.
   */

  public static final Generator 
  bigIntegerGenerator (final UniformRandomProvider urp) {
    final double dp = 0.99;
    return new Generator () {
      private final ContinuousSampler choose = 
        new ContinuousUniformSampler(urp,0.0,1.0);
      private final CollectionSampler edgeCases = 
        new CollectionSampler(
          urp,
          List.of(
            BigInteger.ZERO,
            BigInteger.ONE,
            BigInteger.TWO,
            BigInteger.TEN));
      @Override
      public Object next () { 
        final boolean edge = choose.sample() > dp;
        if (edge) { return edgeCases.sample(); }
        return new BigInteger(nextBytes(urp,1024)); } }; }

  public static final Generator 
  bigIntegerGenerator (final int n,
                       final UniformRandomProvider urp) {
    return new Generator () {
      final Generator g = bigIntegerGenerator(urp);
      @Override
      public final Object next () {
        final BigInteger[] z = new BigInteger[n];
        for (int i=0;i<n;i++) { z[i] = (BigInteger) g.next(); }
        return z; } }; }

  /** Intended primarily for testing. <b>
   * Generate enough bytes to at least cover the range of 
   * <code>double</code> values.
   */

  public static final Generator 
  nonzeroBigIntegerGenerator (final UniformRandomProvider urp) {
    final double dp = 0.99;
    return new Generator () {
      private final ContinuousSampler choose = 
        new ContinuousUniformSampler(urp,0.0,1.0);
      private final CollectionSampler edgeCases = 
        new CollectionSampler(
          urp,
          List.of(
            BigInteger.ONE,
            BigInteger.TWO,
            BigInteger.TEN));
      @Override
      public Object next () { 
        final boolean edge = choose.sample() > dp;
        if (edge) { return edgeCases.sample(); }
        // TODO: bound infinite loop?
        for (;;) {
          final BigInteger b =
            new BigInteger(nextBytes(urp,1024)); 
          if (0 != b.signum()) { return b; } } } }; }

  public static final Generator 
  nonzeroBigIntegerGenerator (final int n,
                              final UniformRandomProvider urp) {
    return new Generator () {
      final Generator g = nonzeroBigIntegerGenerator(urp);
      @Override
      public final Object next () {
        final BigInteger[] z = new BigInteger[n];
        for (int i=0;i<n;i++) { z[i] = (BigInteger) g.next(); }
        return z; } }; }

  /** Intended primarily for testing. <b>
   * Generate enough bytes to at least cover the range of 
   * <code>double</code> values.
   */

  public static final Generator 
  positiveBigIntegerGenerator (final UniformRandomProvider urp) {
    final double dp = 0.99;
    return new Generator () {
      private final ContinuousSampler choose = 
        new ContinuousUniformSampler(urp,0.0,1.0);
      private final CollectionSampler edgeCases = 
        new CollectionSampler(
          urp,
          List.of(
            BigInteger.ONE,
            BigInteger.TWO,
            BigInteger.TEN));
      @Override
      public Object next () { 
        final boolean edge = choose.sample() > dp;
        if (edge) { return edgeCases.sample(); }
        // TODO: bound infinite loop?
        for (;;) {
          final BigInteger b =
            new BigInteger(nextBytes(urp,1024)); 
          if (0 != b.signum()) { return b.abs(); } } } }; }

  public static final Generator 
  positiveBigIntegerGenerator (final int n,
                               final UniformRandomProvider urp) {
    return new Generator () {
      final Generator g = positiveBigIntegerGenerator(urp);
      @Override
      public final Object next () {
        final BigInteger[] z = new BigInteger[n];
        for (int i=0;i<n;i++) { z[i] = (BigInteger) g.next(); }
        return z; } }; }

  //--------------------------------------------------------------
  // TODO: options?
  // TODO: using a DoubleSampler: those are (?) the most likely
  // values to see, but could do something to extend the 
  // range to values not representable as double.

  /** Intended primarily for testing. Sample a random double
   * (see {@link xfp.java.prng.DoubleSampler})
   * and convert to <code>BigFraction</code>
   * with {@link #DOUBLE_P} probability;
   * otherwise return {@link BigFraction#ZERO} or 
   * {@link BigFractrion#ONE}, {@link BigFractrion#MINUS_ONE},  
   * with equal probability (these are potential edge cases).
   */

  public static final Generator 
  numberGenerator (final UniformRandomProvider urp) {
    return new Generator () {
      private final CollectionSampler<Generator> generators = 
        new CollectionSampler(
          urp,
          List.of(
            byteGenerator(urp),
            shortGenerator(urp),
            intGenerator(urp),
            longGenerator(urp),
            Floats.generator(urp),
            Doubles.generator(urp),
            bigIntegerGenerator(urp),
            //bigDecimalGenerator(urp),
            Rationals.generator(urp)
            ));
      @Override
      public final Object next () {
        return generators.sample().next(); } }; }

  public static final Generator 
  numberGenerator (final int n,
                   final UniformRandomProvider urp) {
    return new Generator () {
      final Generator g = numberGenerator(urp);
      @Override
      public final Object next () {
        final Number[] z = new Number[n];
        for (int i=0;i<n;i++) { z[i] = (Number) g.next(); }
        return z; } }; }


  /** Intended primarily for testing. Sample a random double
   * (see {@link xfp.java.prng.DoubleSampler})
   * and convert to <code>BigFraction</code>
   * with {@link #DOUBLE_P} probability;
   * otherwise return {@link BigFraction#ZERO} or 
   * {@link BigFractrion#ONE}, {@link BigFractrion#MINUS_ONE},  
   * with equal probability (these are potential edge cases).
   */

  public static final Generator 
  finiteNumberGenerator (final UniformRandomProvider urp) {
    return new Generator () {
      private final CollectionSampler<Generator> generators = 
        new CollectionSampler(
          urp,
          List.of(
            byteGenerator(urp),
            shortGenerator(urp),
            intGenerator(urp),
            longGenerator(urp),
            Floats.finiteGenerator(urp),
            Doubles.finiteGenerator(urp),
            bigIntegerGenerator(urp),
            //bigDecimalGenerator(urp),
            Rationals.generator(urp)));
      @Override
      public final Object next () {
        return generators.sample().next(); } }; }

  public static final Generator 
  finiteNumberGenerator (final int n,
                         final UniformRandomProvider urp) {
    return new Generator () {
      final Generator g = finiteNumberGenerator(urp);
      @Override
      public final Object next () {
        final Object[] z = new Object[n];
        for (int i=0;i<n;i++) { z[i] = g.next(); }
        return z; } }; }

  //--------------------------------------------------------------
  /** Generate arrays representing vectors in an n-dimensional
   * rational linear space, returning all possible number array 
   * types.
   */

  public static final Generator 
  qnGenerator (final int n,
               final UniformRandomProvider urp) {
    return new Generator () {
      private final CollectionSampler<Generator> generators = 
        new CollectionSampler(
          urp,
          List.of(
            byteGenerator(n,urp),
            shortGenerator(n,urp),
            intGenerator(n,urp),
            longGenerator(n,urp),
            bigIntegerGenerator(n,urp),
            //bigDecimalGenerator(n,urp),
            Floats.finiteGenerator(n,urp),
            Doubles.finiteGenerator(n,urp),
            Rationals.generator(urp)
            //            ERationals.eIntegerGenerator(n,urp),
            //            ERationals.eRationalFromDoubleGenerator(n,urp)
            // clojure.lang.Ratio doesn't round correctly
            // BigFraction.doubleValue() doesn't round correctly.
            //,bigFractionGenerator(n,urp),
            ,finiteNumberGenerator(n,urp)));
      @Override
      public final Object next () {
        return generators.sample().next(); } }; }

  //--------------------------------------------------------------
  // disable constructor
  //--------------------------------------------------------------

  private Generators () {
    throw new UnsupportedOperationException(
      "can't instantiate " + getClass()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

