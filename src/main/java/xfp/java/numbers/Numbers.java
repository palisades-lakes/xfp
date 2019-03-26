package xfp.java.numbers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.CollectionSampler;

import xfp.java.algebra.Set;
import xfp.java.exceptions.Exceptions;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;

/** Utilities for Object and primitive numbers.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-25
 */
@SuppressWarnings("unchecked")
public final class Numbers implements Set {

  //--------------------------------------------------------------
  // useful class methods
  //--------------------------------------------------------------

  /** inclusive */
  public static final int loBit (final BigInteger i) {
    return i.getLowestSetBit(); }

  /** exclusive */
  public static final int hiBit (final BigInteger i) {
    return i.bitLength(); }

  /** inclusive */
  public static final int loBit (final int i) {
    return Integer.numberOfTrailingZeros(i); }

  /** exclusive */
  public static final int hiBit (final int i) {
    return Integer.SIZE -  Integer.numberOfLeadingZeros(i); }

  /** inclusive */
  public static final int loBit (final long i) {
    return Long.numberOfTrailingZeros(i); }

  /** exclusive */
  public static final int hiBit (final long i) {
    return Long.SIZE -  Long.numberOfLeadingZeros(i); }

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
    return 0 == BigDecimal.ZERO.compareTo(x); }
  
  public static final boolean isZero (final Number x) {
    if (x instanceof Rational) { return ((Rational) x).isZero(); }
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
    assert null != x0;
    assert null != x1;
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
    return new Generator () {
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
    return new Generator () {
      final Generator g = numberGenerator(urp);
      @Override
      public final Object next () {
        final Number[] z = new Number[n];
        for (int i=0;i<n;i++) { z[i] = (Number) g.next(); }
        return z; } }; }

  public static final Generator 
  numberGenerator (final UniformRandomProvider urp) {
    return new Generator () {
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

