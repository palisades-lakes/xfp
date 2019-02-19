package xfp.java.numbers;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.rng.UniformRandomProvider;

import xfp.java.algebra.Set;
import xfp.java.exceptions.Exceptions;
import xfp.java.numbers.Q;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;

/** The set of rational numbers, accepting any 'reasonable' 
 * representation. Calculation converts to BigFraction where
 * necessary.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-01-29
 */
public final class Q implements Set {

  //--------------------------------------------------------------
  // class methods
  //--------------------------------------------------------------

  // All known java numbers are rational, meaning there's an 
  // exact, loss-less conversion to BigFraction, used by methods
  // below. But we can't know how to convert unknown
  // implementations of java.lang.Number, so we have to exclude 
  // those, for the start.
  // Also, we only want immutable classes here...
  // TODO: collect some stats and order tests by frequency?

  public static final boolean knownRational (final Object x) {
    if (x instanceof BigFraction) { return true; }
    if (x instanceof Double) { return true; }
    if (x instanceof Integer) { return true; }
    if (x instanceof Long) { return true; }
    if (x instanceof Float) { return true; }
    if (x instanceof Short) { return true; }
    if (x instanceof Byte) { return true; }
    if (x instanceof BigInteger){ return true; }
    return false; }

  public static final boolean knownRational (final Class c) {
    if (BigFraction.class.equals(c)) { return true; }
    if (BigInteger.class.equals(c)) { return true; }
    if (Byte.class.equals(c)) { return true; }
    if (Short.class.equals(c)) { return true; }
    if (Integer.class.equals(c)) { return true; }
    if (Long.class.equals(c)) { return true; }
    if (Float.class.equals(c)) { return true; }
    if (Double.class.equals(c)) { return true; }
    if (Byte.TYPE.equals(c)) { return true; }
    if (Short.TYPE.equals(c)) { return true; }
    if (Integer.TYPE.equals(c)) { return true; }
    if (Long.TYPE.equals(c)) { return true; }
    if (Float.TYPE.equals(c)) { return true; }
    if (Double.TYPE.equals(c)) { return true; }
    return false; }

  //--------------------------------------------------------------

  public static final BigFraction toBigFraction (final byte x) {
    return new BigFraction(x); }

  public static final BigFraction toBigFraction (final short x) {
    return new BigFraction(x); }

  public static final BigFraction toBigFraction (final int x) {
    return new BigFraction(x); }

  public static final BigFraction toBigFraction (final long x) {
    return new BigFraction(x); }

  public static final BigFraction toBigFraction (final float x) {
    return new BigFraction(x); }

  public static final BigFraction toBigFraction (final double x) {
    return new BigFraction(x); }

  public static final BigFraction toBigFraction (final Object x) {
    assert knownRational(x) : 
      x + " is not a known rational number type";
    if (x instanceof BigFraction) { 
      return (BigFraction) x; }
    if (x instanceof Double) { 
      return new BigFraction(((Double) x).doubleValue()); }
    if (x instanceof Integer) {
      return new BigFraction(((Integer) x).intValue()); }
    if (x instanceof Long) { 
      return new BigFraction(((Long) x).longValue()); }
    if (x instanceof Float) {
      return new BigFraction(((Float) x).floatValue()); }
    if (x instanceof Short) {
      return new BigFraction(((Short) x).intValue()); }
    if (x instanceof Byte) {
      return new BigFraction(((Byte) x).intValue()); }
    if (x instanceof BigInteger) {
      return new BigFraction(((BigInteger) x)); }
    throw Exceptions.unsupportedOperation(
      Q.class,"toBigFraction",x); }

  // BigFraction.equals reduces both arguments before checking
  // numerator and denominators are equal.
  // Guessing our BigFractions are usually already reduced.
  // Try n0*d1 == n1*d0 instead
  // TODO: try using BigINteger.bitLength() to decide
  // which method to use?

  private static final boolean 
  equalBigFractions (final BigFraction q0, 
                     final BigFraction q1) {
    if (q0 == q1) { return true; }
    if (null == q0) {
      if (null == q1) { return true; }
      return false; }
    final BigInteger n0 = q0.getNumerator(); 
    final BigInteger d0 = q0.getDenominator(); 
    final BigInteger n1 = q1.getNumerator(); 
    final BigInteger d1 = q1.getDenominator(); 
    return n0.multiply(d1).equals(n1.multiply(d0)); }

  private static final BiPredicate<Number,Number> EQUALS = 
    new BiPredicate<Number,Number>() {
    @Override
    public final boolean test (final Number x0, 
                               final Number x1) {
      final BigFraction q0 = toBigFraction(x0);
      final BigFraction q1 = toBigFraction(x1);
      return equalBigFractions(q0,q1); }
  };

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    return knownRational(element); }

  @Override
  public final boolean contains (final byte element) {
    // all java numbers are rational
    return true; }

  @Override
  public final boolean contains (final short element) {
    // all java numbers are rational
    return true; }

  @Override
  public final boolean contains (final int element) {
    // all java numbers are rational
    return true; }

  @Override
  public final boolean contains (final long element) {
    // all java numbers are rational
    return true; }

  @Override
  public final boolean contains (final float element) {
    // all java numbers are rational
    return true; }

  @Override
  public final boolean contains (final double element) {
    // all java numbers are rational
    return true; }

  //--------------------------------------------------------------

  @Override
  public final BiPredicate equivalence () { return EQUALS; }

  //--------------------------------------------------------------

  @Override
  public final Supplier generator (final UniformRandomProvider urp,
                                   final Map options) {
    final Generator fng = Generators.finiteNumberGenerator(urp); 
    return 
      new Supplier () {
      @Override
      public final Object get () { return fng.next(); } }; }

  @Override
  public final Supplier generator (final UniformRandomProvider urp) {
    return generator(urp,Collections.emptyMap()); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { return 0; }

  // singleton
  @Override
  public final boolean equals (final Object that) {
    return that instanceof Q; }

  @Override
  public final String toString () { return "Q"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private Q () { }

  private static final Q SINGLETON = new Q();

  public static final Q get () { return SINGLETON; } 

  public static final BinaryOperator<Number> ADD =
    new BinaryOperator<Number>() {
    @Override
    public final BigFraction apply (final Number x0, 
                                    final Number x1) {
      final BigFraction q0 = toBigFraction(x0);
      final BigFraction q1 = toBigFraction(x1);
      return q0.add(q1); } 
  };

  public static final UnaryOperator<Number>
  ADDITIVE_INVERSE =
  new UnaryOperator<Number>() {
    @Override
    public final Number apply (final Number x) {
      final BigFraction q = toBigFraction(x);
      return q.negate(); } 
  };

  public static final BinaryOperator<Number> MULTIPLY =
    new BinaryOperator<Number>() {
    @Override
    public final BigFraction apply (final Number x0, 
                                    final Number x1) {
      final BigFraction q0 = toBigFraction(x0);
      final BigFraction q1 = toBigFraction(x1);
      return q0.multiply(q1); } 
  };

  public static final UnaryOperator<Number>
  MULTIPLICATIVE_INVERSE =
  new UnaryOperator<Number>() {
    @Override
    public final BigFraction apply (final Number x) {
      final BigFraction q = toBigFraction(x);
      // only a partial inverse
      if (BigFraction.ZERO.equals(q)) { return null; }
      return q.reciprocal(); } 
  };

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

