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
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;

/** The set of rational numbers, accepting any 'reasonable' 
 * representation. Calculation converts to Number where
 * necessary.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-22
 */

public final class Q implements Set {

  //--------------------------------------------------------------
  // operations for algebraic structures over (rational) NUubers.
  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  private final Number add (final Number x0, 
                            final Number x1) {
    assert(contains(x0));
    assert(contains(x1));
    final BigFraction q0 = BigFractions.toBigFraction(x0);
    final BigFraction q1 = BigFractions.toBigFraction(x1);
    return q0.add(q1); } 

  public final BinaryOperator<Number> adder () {
    return new BinaryOperator<Number> () {
      @Override
      public final String toString () { return "Q.add()"; }
      @Override
      public final Number apply (final Number q0, 
                                 final Number q1) {
        return Q.this.add(q0,q1); } }; }

  //--------------------------------------------------------------

  @SuppressWarnings("static-method")
  public final Object additiveIdentity () { 
    return BigFraction.ZERO; }
  
  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  private final Number negate (final Number x) {
    assert contains(x);
    return BigFractions.toBigFraction(x).negate(); } 

  public final UnaryOperator<Number> additiveInverse () {
    return new UnaryOperator<Number> () {
      @Override
      public final String toString () { return "Q.negate()"; }
      @Override
      public final Number apply (final Number q) {
        return Q.this.negate(q); } }; }

  //--------------------------------------------------------------

  private final Number multiply (final Number x0, 
                                 final Number x1) {
    assert(contains(x0));
    assert(contains(x1));
    final BigFraction q0 = BigFractions.toBigFraction(x0);
    final BigFraction q1 = BigFractions.toBigFraction(x1);
    return q0.multiply(q1); } 

  public final BinaryOperator<Number> multiplier () {
    return new BinaryOperator<Number>() {
      @Override
      public final String toString () { return "Q.multiply()"; }
      @Override
      public final Number apply (final Number q0, 
                                 final Number q1) {
        return Q.this.multiply(q0,q1); } }; }

  //--------------------------------------------------------------

  @SuppressWarnings("static-method")
  public final Object multiplicativeIdentity () {
    return BigFraction.ONE; }

  //--------------------------------------------------------------

  private final Number reciprocal (final Number x) {
    assert contains(x);
    final BigFraction q = BigFractions.toBigFraction(x);
    // only a partial inverse
    if (BigInteger.ZERO.equals(q.getNumerator())) { return null; }
    return q.reciprocal();  } 

  public final UnaryOperator<Number> multiplicativeInverse () {
    return new UnaryOperator<Number> () {
      @Override
      public final String toString () { return "Q.inverse()"; }
      @Override
      public final Number apply (final Number q) {
        return Q.this.reciprocal(q); } }; }

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------
  // All known java numbers are rational, meaning there's an 
  // exact, loss-less conversion to Number, used by methods
  // below. But we can't know how to convert unknown
  // implementations of java.lang.Number, so we have to exclude 
  // those, for the start.
  // Also, we only want immutable classes here...
  // TODO: collect some stats and order tests by frequency?

  public static final boolean knownRational (final Object x) {
    if (x instanceof Number) { return true; }
//    if (x instanceof Double) { return true; }
//    if (x instanceof Integer) { return true; }
//    if (x instanceof Long) { return true; }
//    if (x instanceof Float) { return true; }
//    if (x instanceof Short) { return true; }
//    if (x instanceof Byte) { return true; }
//    if (x instanceof BigInteger){ return true; }
//    if (x instanceof BigFraction){ return true; }
    return false; }

  public static final boolean knownRational (final Class c) {
    if (Number.class.isAssignableFrom(c)) { return true; }
//    if (BigFraction.class.equals(c)) { return true; }
//    if (BigDecimal.class.equals(c)) { return true; }
//    if (Ratio.class.equals(c)) { return true; }
//    if (BigInteger.class.equals(c)) { return true; }
//    if (Byte.class.equals(c)) { return true; }
//    if (Short.class.equals(c)) { return true; }
//    if (Integer.class.equals(c)) { return true; }
//    if (Long.class.equals(c)) { return true; }
//    if (Float.class.equals(c)) { return true; }
//    if (Double.class.equals(c)) { return true; }
    if (Byte.TYPE.equals(c)) { return true; }
    if (Short.TYPE.equals(c)) { return true; }
    if (Integer.TYPE.equals(c)) { return true; }
    if (Long.TYPE.equals(c)) { return true; }
    if (Float.TYPE.equals(c)) { return true; }
    if (Double.TYPE.equals(c)) { return true; }
    return false; }

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

  private final boolean equals (final Number x0, 
                                final Number x1) {
    assert(contains(x0));
    assert(contains(x1));
    final BigFraction q0 = BigFractions.toBigFraction(x0);
    final BigFraction q1 = BigFractions.toBigFraction(x1);
    return BigFractions.get().equals(q0,q1); } 

  @Override
  public final BiPredicate equivalence () { 
    return  
      new BiPredicate<Number,Number>() {
      @Override
      public final boolean test (final Number x0, 
                                 final Number x1) {
        return Q.this.equals(x0,x1); } }; }

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

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

