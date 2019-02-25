package xfp.java.prng;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.CollectionSampler;
import org.apache.commons.rng.sampling.distribution.ContinuousSampler;
import org.apache.commons.rng.sampling.distribution.ContinuousUniformSampler;

import clojure.lang.Numbers;
import clojure.lang.Ratio;
import xfp.java.numbers.Doubles;
import xfp.java.numbers.Floats;

/** Generators of primitives or Objects as zero-arity 'functions'
 * that return different values on each call.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-25
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

  public static final Generator 
  floatGenerator (final UniformRandomProvider urp,
                  final int delta) {
    return new Generator () {
      @Override
      public final float nextFloat () { 
        return Floats.makeFloat(
          urp.nextInt(2),
          urp.nextInt(delta),
          urp.nextInt() & Floats.SIGNIFICAND_MASK); } 
      @Override
      public final Object next () {
        return Float.valueOf(nextFloat()); } }; }

  public static final Generator 
  floatGenerator (final int n,
                  final UniformRandomProvider urp,
                  final int delta) {
    return new Generator () {
      final Generator g = floatGenerator(urp,delta);
      @Override
      public final Object next () {
        final float[] z = new float[n];
        for (int i=0;i<n;i++) { z[i] = g.nextFloat(); }
        return z; } }; }

  public static final Generator 
  floatGenerator (final UniformRandomProvider urp) {
    return 
      floatGenerator(urp,1+Floats.MAXIMUM_BIASED_EXPONENT); } 

  public static final Generator 
  floatGenerator (final int n,
                  final UniformRandomProvider urp) {
    return new Generator () {
      final Generator g = floatGenerator(urp);
      @Override
      public final Object next () {
        final float[] z = new float[n];
        for (int i=0;i<n;i++) { z[i] = g.nextFloat(); }
        return z; } }; }

  public static final Generator 
  doubleGenerator (final UniformRandomProvider urp,
                   final int delta) {
    return new Generator () {
      @Override
      public final double nextDouble () { 
        return Doubles.makeDouble(
          urp.nextInt(2),
          urp.nextInt(delta),
          urp.nextLong() & Doubles.SIGNIFICAND_MASK); } 
      @Override
      public final Object next () {
        return Double.valueOf(nextDouble()); } }; }

  public static final Generator 
  doubleGenerator (final int n,
                   final UniformRandomProvider urp,
                   final int delta) {
    return new Generator () {
      final Generator g = doubleGenerator(urp,delta);
      @Override
      public final Object next () {
        final double[] z = new double[n];
        for (int i=0;i<n;i++) { z[i] = g.nextDouble(); }
        return z; } }; }

  public static final Generator 
  doubleGenerator (final UniformRandomProvider urp) {
    return 
      doubleGenerator(urp,1+Doubles.MAXIMUM_BIASED_EXPONENT); } 

  public static final Generator 
  doubleGenerator (final int n,
                   final UniformRandomProvider urp) {
    return new Generator () {
      final Generator g = doubleGenerator(urp);
      @Override
      public final Object next () {
        final double[] z = new double[n];
        for (int i=0;i<n;i++) { z[i] = g.nextDouble(); }
        return z; } }; }

  //--------------------------------------------------------------

  public static final Generator 
  finiteFloatGenerator (final UniformRandomProvider urp,
                        final int delta) {
    final Generator f = floatGenerator(urp,delta);
    return new Generator () {
      @Override
      public final float nextFloat () {
        // TODO: fix infinite loop
        for (;;) {
          final float x = f.nextFloat();
          if (Float.isFinite(x)) { return x; } } } 
      @Override
      public final Object next () {
        return Float.valueOf(nextFloat()); } }; }

  public static final Generator 
  finiteFloatGenerator (final int n,
                        final UniformRandomProvider urp,
                        final int delta) {
    return new Generator () {
      final Generator g = finiteFloatGenerator(urp,delta);
      @Override
      public final Object next () {
        final float[] z = new float[n];
        for (int i=0;i<n;i++) { z[i] = g.nextFloat(); }
        return z; } }; }

  public static final Generator 
  finiteFloatGenerator (final UniformRandomProvider urp) {
    return 
      finiteFloatGenerator(urp,1+Floats.MAXIMUM_BIASED_EXPONENT); } 

  public static final Generator 
  finiteFloatGenerator (final int n,
                        final UniformRandomProvider urp) {
    return new Generator () {
      final Generator g = finiteFloatGenerator(urp);
      @Override
      public final Object next () {
        final float[] z = new float[n];
        for (int i=0;i<n;i++) { z[i] = g.nextFloat(); }
        return z; } }; }

  //--------------------------------------------------------------
  // TODO: Double[] generators
  
  public static final Generator 
  finiteDoubleGenerator (final UniformRandomProvider urp,
                         final int delta) {
    final Generator d = doubleGenerator(urp,delta);
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
  finiteDoubleGenerator (final int n,
                         final UniformRandomProvider urp,
                         final int delta) {
    return new Generator () {
      final Generator g = finiteDoubleGenerator(urp,delta);
      @Override
      public final Object next () {
        final double[] z = new double[n];
        for (int i=0;i<n;i++) { z[i] = g.nextDouble(); }
        return z; } }; }

  public static final Generator 
  finiteDoubleGenerator (final UniformRandomProvider urp) {
    return 
      finiteDoubleGenerator(
        urp,1+Doubles.MAXIMUM_BIASED_EXPONENT); } 

  public static final Generator 
  finiteDoubleGenerator (final int n,
                         final UniformRandomProvider urp) {
    return new Generator () {
      final Generator g = finiteDoubleGenerator(urp);
      @Override
      public final Object next () {
        final double[] z = new double[n];
        for (int i=0;i<n;i++) { z[i] = g.nextDouble(); }
        return z; } }; }

  //--------------------------------------------------------------
  // TODO: options?
  // TODO: using a DoubleSampler: those are (?) the most likely
  // values to see, but could do something to extend the 
  // range to values not representable as double.

  /** Intended primarily for testing. Sample a random double
   * (see {@link xfp.java.prng.DoubleSampler})
   * and convert to <code>BigDecimal</code>
   * with {@link #DOUBLE_P} probability;
   * otherwise return {@link BigDecimal#ZERO} or 
   * {@link BigDecimal#ONE}, {@link BigDecimal#TEN},  
   * with equal probability (these are potential edge cases).
   * 
   * TODO: sample rounding modes?
   */

  public static final Generator 
  bigDecimalGenerator (final UniformRandomProvider urp) {
    final double dp = 0.9;
    return new Generator () {
      private final ContinuousSampler choose = 
        new ContinuousUniformSampler(urp,0.0,1.0);
      private final Generator fdg = finiteDoubleGenerator(urp);
      private final CollectionSampler edgeCases = 
        new CollectionSampler(
          urp,
          List.of(
            BigDecimal.ZERO,
            BigDecimal.ONE,
            BigDecimal.TEN));
      @Override
      public Object next () { 
        final boolean edge = choose.sample() > dp;
        if (edge) { return edgeCases.sample(); }
        return new BigDecimal(fdg.nextDouble()); } }; }

  public static final Generator 
  bigDecimalGenerator (final int n,
                       final UniformRandomProvider urp) {
    return new Generator () {
      final Generator g = bigDecimalGenerator(urp);
      @Override
      public final Object next () {
        final BigDecimal[] z = new BigDecimal[n];
        for (int i=0;i<n;i++) { z[i] = (BigDecimal) g.next(); }
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
  bigFractionGenerator (final UniformRandomProvider urp) {
    final double dp = 0.9;
    return new Generator () {
      private final ContinuousSampler choose = 
        new ContinuousUniformSampler(urp,0.0,1.0);
      private final Generator fdg = finiteDoubleGenerator(urp);
      private final CollectionSampler edgeCases = 
        new CollectionSampler(
          urp,
          List.of(
            BigFraction.ZERO,
            BigFraction.ONE,
            BigFraction.MINUS_ONE));
      @Override
      public Object next () { 
        final boolean edge = choose.sample() > dp;
        if (edge) { return edgeCases.sample(); }
        return new BigFraction(fdg.nextDouble()); } }; }

  public static final Generator 
  bigFractionGenerator (final int n,
                        final UniformRandomProvider urp) {
    return new Generator () {
      final Generator g = bigFractionGenerator(urp);
      @Override
      public final Object next () {
        final BigFraction[] z = new BigFraction[n];
        for (int i=0;i<n;i++) { z[i] = (BigFraction) g.next(); }
        return z; } }; }

  //--------------------------------------------------------------
  // TODO: options?
  // TODO: using a DoubleSampler: those are (?) the most likely
  // values to see, but could do something to extend the 
  // range to values not representable as double.

  /** Intended primarily for testing. Sample a random double
   * (see {@link xfp.java.prng.DoubleSampler})
   * and convert to <code>BigDecimal</code>
   * with {@link #DOUBLE_P} probability;
   * otherwise return {@link Ratio#ZERO} or 
   * {@link Ratio#ONE}, {@link Ratio#TEN},  
   * with equal probability (these are potential edge cases).
   * 
   * TODO: sample rounding modes?
   */

  public static final Generator 
  ratioGenerator (final UniformRandomProvider urp) {
    final double dp = 0.9;
    return new Generator () {
      private final ContinuousSampler choose = 
        new ContinuousUniformSampler(urp,0.0,1.0);
      private final Generator fdg = finiteDoubleGenerator(urp);
      private final CollectionSampler edgeCases = 
        new CollectionSampler(
          urp,
          List.of(
            new Ratio(BigInteger.ZERO,BigInteger.ONE),
            new Ratio(BigInteger.ONE,BigInteger.ONE),
            new Ratio(BigInteger.TWO,BigInteger.TWO),
            new Ratio(BigInteger.TEN,BigInteger.TEN),
            new Ratio(BigInteger.ONE,BigInteger.ONE),
            new Ratio(BigInteger.TWO,BigInteger.ONE),
            new Ratio(BigInteger.TEN,BigInteger.ONE),
            new Ratio(BigInteger.ONE,BigInteger.TWO),
            new Ratio(BigInteger.TWO,BigInteger.TWO),
            new Ratio(BigInteger.TEN,BigInteger.TWO),
            new Ratio(BigInteger.ONE,BigInteger.TEN),
            new Ratio(BigInteger.TWO,BigInteger.TEN),
            new Ratio(BigInteger.TEN,BigInteger.TEN)));
      @Override
      public Object next () { 
        final boolean edge = choose.sample() > dp;
        if (edge) { return edgeCases.sample(); }
        return Numbers.toRatio(
          new BigDecimal(fdg.nextDouble())); } }; }

  public static final Generator 
  ratioGenerator (final int n,
                  final UniformRandomProvider urp) {
    return new Generator () {
      final Generator g = ratioGenerator(urp);
      @Override
      public final Object next () {
        final Ratio[] z = new Ratio[n];
        for (int i=0;i<n;i++) { z[i] = (Ratio) g.next(); }
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
            floatGenerator(urp),
            doubleGenerator(urp),
            bigFractionGenerator(urp)));
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
            finiteFloatGenerator(urp),
            finiteDoubleGenerator(urp),
            bigFractionGenerator(urp)));
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
        final Number[] z = new Number[n];
        for (int i=0;i<n;i++) { z[i] = (Number) g.next(); }
        return z; } }; }

  //--------------------------------------------------------------
  /** Generate arrays representing vectors in an n-dimnesional
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
            finiteFloatGenerator(n,urp),
            finiteDoubleGenerator(n,urp),
            bigFractionGenerator(n,urp),
            finiteNumberGenerator(n,urp)));
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

