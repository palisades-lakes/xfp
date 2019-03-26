package xfp.java.numbers;

import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.apache.commons.rng.UniformRandomProvider;

import xfp.java.Classes;
import xfp.java.algebra.OneSetOneOperation;
import xfp.java.algebra.OneSetTwoOperations;
import xfp.java.algebra.Set;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;

/** The set of rational numbers, accepting any 'reasonable' 
 * representation. Calculation converts to Number where
 * necessary.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-25
 */

// TODO: change name to 'QQ', imitating tex font?

public final class Q implements Set {

  //--------------------------------------------------------------
  // operations for algebraic structures over (rational) Numbers.
  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  private final Object add (final Object x0, 
                            final Object x1) {
    assert(contains(x0));
    assert(contains(x1));
    final Rational q0 = Rational.valueOf(x0);
    final Rational q1 = Rational.valueOf(x1);
    return q0.add(q1); } 

  public final BinaryOperator adder () {
    return new BinaryOperator () {
      @Override
      public final String toString () { return "Q.add()"; }
      @Override
      public final Object apply (final Object q0, 
                                 final Object q1) {
        return Q.this.add(q0,q1); } }; }

  //--------------------------------------------------------------

  @SuppressWarnings("static-method")
  public final Object additiveIdentity () { 
    return Rational.ZERO; }

  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  private final Rational negate (final Object x) {
    assert contains(x);
    return Rational.valueOf(x).negate(); } 

  public final UnaryOperator additiveInverse () {
    return new UnaryOperator () {
      @Override
      public final String toString () { return "Q.negate()"; }
      @Override
      public final Object apply (final Object q) {
        return Q.this.negate(q); } }; }

  //--------------------------------------------------------------

  private final Rational multiply (final Object x0, 
                                   final Object x1) {
    assert contains(x0) : 
      "x0 " + Classes.className(x0) + ":" + x0 + " not in " + this;
    assert contains(x1)  : 
      "x1 " + Classes.className(x1) + ":" + x1 + " not in " + this;
    final Rational q0 = Rational.valueOf(x0);
    final Rational q1 = Rational.valueOf(x1);
    return q0.multiply(q1); } 

  public final BinaryOperator multiplier () {
    return new BinaryOperator () {
      @Override
      public final String toString () { return "Q.multiply()"; }
      @Override
      public final Object apply (final Object q0, 
                                 final Object q1) {
        return Q.this.multiply(q0,q1); } }; }

  //--------------------------------------------------------------

  @SuppressWarnings("static-method")
  public final Object multiplicativeIdentity () {
    return Rational.ONE; }

  //--------------------------------------------------------------

  private final Rational reciprocal (final Object x) {
    assert contains(x);
    // only a partial inverse
    // TODO: throw exception
    if (Numbers.isZero((Number) x)) { return null; }
    final Rational q = Rational.valueOf(x);
    return q.reciprocal();  } 

  public final UnaryOperator multiplicativeInverse () {
    return new UnaryOperator () {
      @Override
      public final String toString () { return "Q.inverse()"; }
      @Override
      public final Object apply (final Object q) {
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
    return false; }

  public static final boolean knownRational (final Class c) {
    if (Number.class.isAssignableFrom(c)) { return true; }
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

  private final boolean equals (final Object x0, 
                                final Object x1) {
    assert(contains(x0));
    assert(contains(x1));
    final Rational q0 = Rational.valueOf(x0);
    final Rational q1 = Rational.valueOf(x1);
    return q0.equals(q1); } 

  @Override
  public final BiPredicate equivalence () { 
    return  
      new BiPredicate() {
      @Override
      public final boolean test (final Object x0, 
                                 final Object x1) {
        return Q.this.equals(x0,x1); } }; }

  //--------------------------------------------------------------

  @Override
  public final Supplier generator (final Map options) {
    final UniformRandomProvider urp = Set.urp(options);
    final Generator g = Generators.finiteNumberGenerator(urp); 
    //final Generator g = Rationals.generator(urp); 
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

