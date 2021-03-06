package xfp.java.prng;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.CollectionSampler;
import org.apache.commons.rng.sampling.distribution.ContinuousSampler;
import org.apache.commons.rng.sampling.distribution.ContinuousUniformSampler;

import xfp.java.numbers.Doubles;

/** Generators of primitives or Objects as zero-arity 'functions'
 * that return different values on each call.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-24
 */

@SuppressWarnings("unchecked")
public final class Generators {

  //--------------------------------------------------------------
  // TODO; Integer[], Double[], etc., generators?
  // TODO: move Generator definitions into Set classes

  public static final Generator
  byteGenerator (final UniformRandomProvider urp) {
    return new GeneratorBase ("byteGenerator") {
      @Override
      public final byte nextByte () { return (byte) urp.nextInt(); }
      @Override
      public final Object next () {
        return Byte.valueOf(nextByte()); } }; }

  public static final Generator
  byteGenerator (final int n,
                 final UniformRandomProvider urp) {
    return new GeneratorBase ("byteGenerator:" + n) {
      final Generator g = byteGenerator(urp);
      @Override
      public final Object next () {
        final byte[] z = new byte[n];
        for (int i=0;i<n;i++) { z[i] = g.nextByte(); }
        return z; } }; }

  public static final Generator
  shortGenerator (final UniformRandomProvider urp) {
    return new GeneratorBase ("shortGenerator") {
      @Override
      public final short nextShort () { return (short) urp.nextInt(); }
      @Override
      public final Object next () {
        return Short.valueOf(nextShort()); } }; }

  public static final Generator
  shortGenerator (final int n,
                  final UniformRandomProvider urp) {
    return new GeneratorBase ("shortGenerator:" + n) {
      final Generator g = shortGenerator(urp);
      @Override
      public final Object next () {
        final short[] z = new short[n];
        for (int i=0;i<n;i++) { z[i] = g.nextShort(); }
        return z; } }; }

  //--------------------------------------------------------------

  public static final Generator
  intGenerator (final UniformRandomProvider urp) {
    return new GeneratorBase ("intGenerator") {
      @Override
      public final int nextInt () { return urp.nextInt(); }
      @Override
      public final Object next () {
        return Integer.valueOf(nextInt()); } }; }

  public static final Generator
  intGenerator (final int n,
                final UniformRandomProvider urp) {
    return new GeneratorBase ("intGenerator:" + n) {
      final Generator g = intGenerator(urp);
      @Override
      public final Object next () {
        final int[] z = new int[n];
        for (int i=0;i<n;i++) { z[i] = g.nextInt(); }
        return z; } }; }

  //--------------------------------------------------------------

  public static final Generator
  positiveIntGenerator (final UniformRandomProvider urp) {
    return new GeneratorBase ("positiveIntGenerator") {
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
    return new GeneratorBase ("positiveIntGenerator:" + n) {
      final Generator g = positiveLongGenerator(urp);
      @Override
      public final Object next () {
        final int[] z = new int[n];
        for (int i=0;i<n;i++) { z[i] = g.nextInt(); }
        return z; } }; }

  //--------------------------------------------------------------

  public static final Generator
  longGenerator (final UniformRandomProvider urp) {
    return new GeneratorBase ("longGenerator") {
      @Override
      public final long nextLong () { return urp.nextLong(); }
      @Override
      public final Object next () {
        return Long.valueOf(nextLong()); } }; }

  public static final Generator
  longGenerator (final int n,
                 final UniformRandomProvider urp) {
    return new GeneratorBase ("longGenerator:" + n) {
      final Generator g = longGenerator(urp);
      @Override
      public final Object next () {
        final long[] z = new long[n];
        for (int i=0;i<n;i++) { z[i] = g.nextLong(); }
        return z; } }; }

  //--------------------------------------------------------------

  public static final Generator
  positiveLongGenerator (final UniformRandomProvider urp) {
    return new GeneratorBase ("positiveLongGenerator") {
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
    return new GeneratorBase ("positiveLongGenerator:" + n) {
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

  //--------------------------------------------------------------
  /** Intended primarily for testing. <b>
   * Generate enough bytes to at least cover the range of
   * <code>double</code> values.
   */

  public static final Generator
  bigIntegerGenerator (final UniformRandomProvider urp) {
    final double dp = 0.99;
    return new GeneratorBase ("bigIntegerGenerator") {
      private final ContinuousSampler choose =
        new ContinuousUniformSampler(urp,0.0,1.0);
      private final CollectionSampler edgeCases =
        new CollectionSampler(
          urp,
          List.of(
            BigInteger.valueOf(0L),
            BigInteger.ONE,
            BigInteger.valueOf(2L),
            BigInteger.valueOf(10L)));
      @Override
      public Object next () {
        final boolean edge = choose.sample() > dp;
        if (edge) { return edgeCases.sample(); }
        return new BigInteger(nextBytes(urp,1024)); } }; }

  public static final Generator
  bigIntegerGenerator (final int n,
                       final UniformRandomProvider urp) {
    return new GeneratorBase ("bigIntegerGenerator:" + n) {
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
    return new GeneratorBase ("nonzeroBigIntegerGenerator") {
      private final ContinuousSampler choose =
        new ContinuousUniformSampler(urp,0.0,1.0);
      private final CollectionSampler edgeCases =
        new CollectionSampler(
          urp,
          List.of(
            BigInteger.ONE,
            BigInteger.valueOf(2L),
            BigInteger.valueOf(10L)));
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
    return new GeneratorBase ("nonzeroBigIntegerGenerator:" + n) {
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
    return new GeneratorBase ("positiveBigIntegerGenerator") {
      private final ContinuousSampler choose =
        new ContinuousUniformSampler(urp,0.0,1.0);
      private final CollectionSampler edgeCases =
        new CollectionSampler(
          urp,
          List.of(
            BigInteger.ONE,
            BigInteger.valueOf(2L),
            BigInteger.valueOf(10L)));
      @Override
      public Object next () {
        final boolean edge = choose.sample() > dp;
        if (edge) { return edgeCases.sample(); }
        // TODO: bound infinite loop?
        for (;;) {
          final BigInteger b = new BigInteger(nextBytes(urp,1024));
          if (0 != b.signum()) { return b.abs(); } } } }; }

  public static final Generator
  positiveBigIntegerGenerator (final int n,
                               final UniformRandomProvider urp) {
    return new GeneratorBase ("positiveBigIntegerGenerator:" + n) {
      final Generator g = positiveBigIntegerGenerator(urp);
      @Override
      public final Object next () {
        final BigInteger[] z = new BigInteger[n];
        for (int i=0;i<n;i++) { z[i] = (BigInteger) g.next(); }
        return z; } }; }

  //--------------------------------------------------------------

  private static final String SEED0 =
    "seeds/Well44497b-2019-01-05.txt";

  //  private static final String SEED1 =
  //    "seeds/Well44497b-2019-01-07.txt";

  public static final Map<String,IntFunction<Generator>>
  factories =
  Map.of(
    "exponential",
    new IntFunction<Generator>() {
      @Override
      public final Generator apply (final int dim) {
        final UniformRandomProvider urp0 = PRNG.well44497b(SEED0);
        //final UniformRandomProvider urp1 = PRNG.well44497b(SEED1);
        final int emax = Doubles.deMax(dim)/2;
        final double dmax = (1<<emax);
        return
          //Doubles.shuffledGenerator(
          //Doubles.zeroSumGenerator(
          Doubles.exponentialGenerator(dim,urp0,0.0,dmax)
          //),
          //urp1)
          ; }
    },
    "finite",
    new IntFunction<Generator>() {
      @Override
      public final Generator apply (final int dim) {
        final UniformRandomProvider urp0 = PRNG.well44497b(SEED0);
        //final UniformRandomProvider urp1 = PRNG.well44497b(SEED1);
        final int emax = Doubles.deMax(dim)/2;
        return
          //Doubles.shuffledGenerator(
          //Doubles.zeroSumGenerator(
          Doubles.finiteGenerator(dim,urp0,emax)
          //),
          //urp1)
          ; }
    },
    "gaussian",
    new IntFunction<Generator>() {
      @Override
      public final Generator apply (final int dim) {
        final UniformRandomProvider urp0 = PRNG.well44497b(SEED0);
        //final UniformRandomProvider urp1 = PRNG.well44497b(SEED1);
        final int emax = Doubles.deMax(dim)/2;
        final double dmax = (1<<emax);
        return
          //Doubles.shuffledGenerator(
          //Doubles.zeroSumGenerator(
          Doubles.gaussianGenerator(dim,urp0,0.0,dmax)
          //),
          //urp1)
          ; }
    },
    "laplace",
    new IntFunction<Generator>() {
      @Override
      public final Generator apply (final int dim) {
        final UniformRandomProvider urp0 = PRNG.well44497b(SEED0);
        //final UniformRandomProvider urp1 = PRNG.well44497b(SEED1);
        final int emax = Doubles.deMax(dim)/2;
        final double dmax = (1<<emax);
        return
          //Doubles.shuffledGenerator(
          //Doubles.zeroSumGenerator(
          Doubles.laplaceGenerator(dim,urp0,0.0,dmax)
          //),urp1)
          ; }
    },
    "uniform",
    new IntFunction<Generator>() {
      @Override
      public final Generator apply (final int dim) {
        final UniformRandomProvider urp0 = PRNG.well44497b(SEED0);
        //final UniformRandomProvider urp1 = PRNG.well44497b(SEED1);
        final int emax = Doubles.deMax(dim)/2;
        final double dmax = (1<<emax);
        return
          //Doubles.shuffledGenerator(
          //Doubles.zeroSumGenerator(
          Doubles.uniformGenerator(dim,urp0,-dmax,dmax)
          //),
          //urp1)
          ; }
    }
    );

  public static final Generator make (final String name,
                                      final int dim) {
    return factories.get(name).apply(dim); }
  
  //--------------------------------------------------------------
  // disable constructor
  //--------------------------------------------------------------

  private Generators () {
    throw new UnsupportedOperationException(
      "can't instantiate " + getClass()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

