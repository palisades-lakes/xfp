package xfp.java.numbers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.CollectionSampler;

import xfp.java.algebra.Set;
import xfp.java.exceptions.Exceptions;
import xfp.java.prng.Generator;
import xfp.java.prng.GeneratorBase;
import xfp.java.prng.Generators;

/** Utilities for Object and primitive numbers.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-08-13
 */
@SuppressWarnings("unchecked")
public final class Numbers implements Set {

  /** (int & UNSIGNED_MASK) returns long containing unsigned int. */
  public static final long UNSIGNED_MASK = 0xFFFFFFFFL;

  /** (int & UNSIGNED_MASK) returns long containing unsigned int. */
  public static final long unsigned (final int i) {
    return i & UNSIGNED_MASK; }

  public static final long loWord (final long i) {
    return i & UNSIGNED_MASK; }

  public static final long hiWord (final long i) {
    return i >>> 32; }

  //--------------------------------------------------------------
  /** Like {@link Arrays#toString(double[])}. */

  public static final String toHexString (final double[] x) {
    if (null == x) { return "null"; }
    final StringBuilder b = new StringBuilder("[");
    if (x.length > 0) { b.append(Double.toHexString(x[0])); }
    for (int i=1;i<x.length;i++) {
      // Arrays.toString wastes space with commas
      //b.append(",");
      b.append(" "); b.append(Double.toHexString(x[i])); }
    return b.append("]").toString(); }

  public static final String toHexString (final int[] x) {
    if (null == x) { return "null"; }
    final StringBuilder b = new StringBuilder("[");
    if (x.length > 0) { b.append(Integer.toHexString(x[0])); }
    for (int i=1;i<x.length;i++) {
      // Arrays.toString wastes space with commas
      //b.append(",");
      b.append(" "); b.append(Integer.toHexString(x[i])); }
    return b.append("]").toString(); }

  public static final String toHexString (final long[] x) {
    if (null == x) { return "null"; }
    final StringBuilder b = new StringBuilder("[");
    if (x.length > 0) { b.append(Long.toHexString(x[0])); }
    for (int i=1;i<x.length;i++) {
      // Arrays.toString wastes space with commas
      //b.append(",");
      b.append(" "); b.append(Long.toHexString(x[i])); }
    return b.append("]").toString(); }

  public static final String toHexString (final long x) {
    return Long.toHexString(x); }

  public static final String toHexString (final int x) {
    return Integer.toHexString(x); }

  public static final String toHexString (final double x) {
    return Double.toHexString(x); }

  public static final String toHexString (final BigInteger x) {
    return x.toString(0x10); }

  //--------------------------------------------------------------
  // useful class methods
  //--------------------------------------------------------------
  // Useful for passing as a Function method reference

  public static final double doubleValue (final Object x) {
    if (x instanceof BigFloat) { 
      return ((BigFloat) x).doubleValue(); }
    if (x instanceof RationalFloat) { 
      return ((RationalFloat) x).doubleValue(); }
    if (x instanceof Rational) { 
      return ((Rational) x).doubleValue(); }
    if (x instanceof Natural) { 
      return ((Natural) x).doubleValue(); }
    if (x instanceof Number) { 
      return ((Number) x).doubleValue(); }
    throw Exceptions.unsupportedOperation(null,"doubleValue",x); }

  public static float floatValue (final Object x) {
    if (x instanceof BigFloat) { 
      return ((BigFloat) x).floatValue(); }
    if (x instanceof RationalFloat) { 
      return ((RationalFloat) x).floatValue(); }
    if (x instanceof Rational) { 
      return ((Rational) x).floatValue(); }
    if (x instanceof Natural) { 
      return ((Natural) x).floatValue(); }
    if (x instanceof Number) { 
      return ((Number) x).floatValue(); }
    throw Exceptions.unsupportedOperation(null,"floatValue",x); }

  //--------------------------------------------------------------

  /** inclusive */
  public static final int loBit (final BigInteger i) {
    return i.getLowestSetBit(); }

  /** exclusive */
  public static final int hiBit (final BigInteger i) {
    return i.bitLength(); }

  /** inclusive */
  public static final int loBit (final Uints i) {
    return i.loBit(); }

  /** exclusive */
  public static final int hiBit (final Uints i) {
     return i.hiBit(); }

  /** inclusive */
  public static final int loBit (final int i) {
    return Integer.numberOfTrailingZeros(i) % 32; }

  /** exclusive */
  public static final int hiBit (final int i) {
    return Integer.SIZE -  Integer.numberOfLeadingZeros(i); }

  /** inclusive. Returns 0 for 0. */
  public static final int loBit (final long i) {
    return Long.numberOfTrailingZeros(i) % 64; }

  /** exclusive */
  public static final int hiBit (final long i) {
    return Long.SIZE - Long.numberOfLeadingZeros(i); }

   //--------------------------------------------------------------

  public static final boolean isZero (final double x) {
    return 0.0 == x; }

  public static final boolean isZero (final float x) {
    return 0.0F == x; }

  public static final boolean isZero (final byte x) {
    return 0 == x; }

  public static final boolean isZero (final short x) {
    return 0 == x; }

  public static final boolean isZero (final int x) {
    return 0 == x; }

  public static final boolean isZero (final long x) {
    return 0L == x; }

  public static final boolean isZero (final BigInteger x) {
    return 0 == x.signum(); }

  public static final boolean isZero (final BigDecimal x) {
    return 0 == BigDecimal.valueOf(0L).compareTo(x); }

  public static final boolean isZero (final Number x) {
    if (x instanceof BigInteger) { return isZero((BigInteger) x); }
    if (x instanceof BigDecimal) { return isZero((BigDecimal) x); }
    if (x instanceof Double) {
      return isZero(((Double) x).doubleValue()); }
    if (x instanceof Float) {
      return isZero(((Float) x).floatValue()); }
    if (x instanceof Integer) {
      return isZero(((Integer) x).intValue()); }
    if (x instanceof Long) {
      return isZero(((Long) x).longValue()); }
    if (x instanceof Byte) {
      return isZero(((Byte) x).byteValue()); }
    if (x instanceof Short) {
      return isZero(((Short) x).shortValue()); }
    throw Exceptions.unsupportedOperation(null,"valueOf",x); }

  public static final boolean isZero (final Object x) {
    if (x instanceof Rational) { return ((Rational) x).isZero(); }
    if (x instanceof Number) { return isZero((Number) x); }
    throw Exceptions.unsupportedOperation(null,"valueOf",x); }

    //--------------------------------------------------------------

  public static final String description (final String name,
                                          final BigInteger i) {

    return name
      + "[lo,hi)=" + loBit(i) + "," + hiBit(i) + ")"
      + " : " + i.toString(0x10); }

  public static final String description (final String name,
                                          final int i) {

    return name + " = "
      + Integer.toHexString(i) + "; " + Integer.toString(i) + "\n"
      + "lo,hi bits= [" +
      loBit(i) + "," + hiBit(i) + ")"; }

  public static final String description (final String name,
                                          final long i) {

    return name + " = "
      + Long.toHexString(i) + "; " + Long.toString(i) + "\n"
      + "lo,hi bits= [" +
      loBit(i) + "," + hiBit(i) + ")"; }

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    return element instanceof Number; }

  @Override
  public final boolean contains (final float element) {
    return true; }

  @Override
  public final boolean contains (final double element) {
    return true; }

  @Override
  public final boolean contains (final byte element) {
    return true; }

  @Override
  public final boolean contains (final short element) {
    return true; }

  @Override
  public final boolean contains (final int element) {
    return true; }

  @Override
  public final boolean contains (final long element) {
    return true; }

  //--------------------------------------------------------------
  // Double.equals reduces both arguments before checking
  // numerator and denominators are equal.
  // Guessing our Doubles are usually already reduced.
  // Try n0*d1 == n1*d0 instead
  // TODO: use BigInteger.bitLength() to decide
  // which method to use?

  @SuppressWarnings("static-method")
  public final boolean equals (final Number x0,
                               final Number x1) {
    //assert null != x0;
    //assert null != x1;
    final Rational q0 = Rational.valueOf(x0);
    final Rational q1 = Rational.valueOf(x1);
    return q0.equals(q1); }

  @Override
  public final BiPredicate equivalence () {
    return new BiPredicate<Number,Number>() {
      @Override
      public final boolean test (final Number q0,
                                 final Number q1) {
        return Numbers.this.equals(q0,q1); } }; }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { return 0; }

  // singleton
  @Override
  public final boolean equals (final Object that) {
    return that instanceof Numbers; }

  @Override
  public final String toString () { return "D"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  public static final Generator
  finiteNumberGenerator (final int n,
                         final UniformRandomProvider urp) {
    return new GeneratorBase ("finiteNumberGenerator:" + n) {
      final Generator g = finiteNumberGenerator(urp);
      @Override
      public final Object next () {
        final Object[] z = new Object[n];
        for (int i=0;i<n;i++) { z[i] = g.next(); }
        return z; } }; }

  /** Intended primarily for testing. Sample a random double
   * (see {@link xfp.java.prng.DoubleSampler})
   * and convert to <code>BigFraction</code>
   * with {@link #DOUBLE_P} probability;
   * otherwise return {@link BigFraction#EMPTY} or
   * {@link BigFractrion#ONE}, {@link BigFractrion#MINUS_ONE},
   * with equal probability (these are potential edge cases).
   */

  public static final Generator
  finiteNumberGenerator (final UniformRandomProvider urp) {
    return new GeneratorBase ("finiteNumberGenerator") {
      private final CollectionSampler<Generator> generators =
        new CollectionSampler(
          urp,
          List.of(
            Generators.byteGenerator(urp),
            Generators.shortGenerator(urp),
            Generators.intGenerator(urp),
            Generators.longGenerator(urp),
            Floats.finiteGenerator(urp),
            Doubles.finiteGenerator(urp),
            Generators.bigIntegerGenerator(urp),
            //bigDecimalGenerator(urp),
            Rationals.generator(urp)));
      @Override
      public final Object next () {
        return generators.sample().next(); } }; }

  public static final Generator
  numberGenerator (final int n,
                   final UniformRandomProvider urp) {
    return new GeneratorBase ("numberGenerator:" + n) {
      final Generator g = numberGenerator(urp);
      @Override
      public final Object next () {
        final Number[] z = new Number[n];
        for (int i=0;i<n;i++) { z[i] = (Number) g.next(); }
        return z; } }; }

  public static final Generator
  numberGenerator (final UniformRandomProvider urp) {
    return new GeneratorBase ("numberGenerator") {
      private final CollectionSampler<Generator> generators =
        new CollectionSampler(
          urp,
          List.of(
            Generators.byteGenerator(urp),
            Generators.shortGenerator(urp),
            Generators.intGenerator(urp),
            Generators.longGenerator(urp),
            Floats.generator(urp),
            Doubles.generator(urp),
            Generators.bigIntegerGenerator(urp),
            //bigDecimalGenerator(urp),
            Rationals.generator(urp)
            ));
      @Override
      public final Object next () {
        return generators.sample().next(); } }; }

  private Numbers () { }

  private static final Numbers SINGLETON = new Numbers();

  public static final Numbers get () { return SINGLETON; }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

