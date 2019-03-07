package xfp.java.numbers;

import java.math.BigInteger;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.apache.commons.rng.UniformRandomProvider;

import clojure.lang.Numbers;
import clojure.lang.Ratio;
import xfp.java.algebra.OneSetOneOperation;
import xfp.java.algebra.OneSetTwoOperations;
import xfp.java.algebra.Set;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;

/** The set of rational numbers represented by 
 * <code>Ratio</code>.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-26
 */
public final class Ratios implements Set {

  //--------------------------------------------------------------
  // Ratio utils
  //--------------------------------------------------------------

  //  public static final Ratio toRatio (final double q) {
  //    return clojure.lang.Numbers.toRatio(new BigDecimal(q)); }

  /** From apache commons math4 BigFraction.
   * <p>
   * Create a fraction given the double value.
   * <p>
   * This constructor behaves <em>differently</em> from
   * {@link #BigFraction(double, double, int)}. It converts the 
   * double value exactly, considering its internal bits 
   * representation. This works for all values except NaN and 
   * infinities and does not requires any loop or convergence 
   * threshold.
   * </p>
   * <p>
   * Since this conversion is exact and since double numbers are 
   * sometimes approximated, the fraction created may seem strange 
   * in some cases. For example, calling 
   * <code>new BigFraction(1.0 / 3.0)</code> does <em>not</em> 
   * create the fraction 1/3, but the fraction 
   * 6004799503160661 / 18014398509481984, because the double 
   * number passed to the constructor is not exactly 1/3
   * (this number cannot be stored exactly in IEEE754).
   * </p>
   * @see #BigFraction(double, double, int)
   * @param x the double value to convert to a fraction.
   * @exception IllegalArgumentException if value is not finite
   */
  
  public static final Ratio toRatio (final double x) 
    throws IllegalArgumentException {
    if (! Double.isFinite(x)) {
      throw new IllegalArgumentException(
        x + " is not rational."); }

    // compute m and k such that value = m * 2^k
    final long bits     = Double.doubleToLongBits(x);
    final long sign     = bits & 0x8000000000000000L;
    final long exponent = bits & 0x7ff0000000000000L;
    long m              = bits & 0x000fffffffffffffL;
    if (exponent != 0) {
      // this was a normalized number, 
      // add the implicit most significant bit
      m |= 0x0010000000000000L; }
    if (sign != 0) { m = -m; }
    int k = ((int) (exponent >> 52)) - 1075;
    while (((m & 0x001ffffffffffffeL) != 0) && ((m & 0x1) == 0)) {
      m >>= 1; ++k; }
    final BigInteger numerator;
    final BigInteger denominator;
    if (k < 0) { 
      numerator   = BigInteger.valueOf(m);
      denominator = BigInteger.ZERO.flipBit(-k); } 
    else {
      numerator   = BigInteger.valueOf(m)
        .multiply(BigInteger.ZERO.flipBit(k));
      denominator = BigInteger.ONE; }
    return new Ratio(numerator,denominator); }
  
  //--------------------------------------------------------------
  // operations for algebraic structures over Ratios.
  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  public final Ratio add (final Ratio q0, 
                          final Ratio q1) {
    assert contains(q0);
    assert contains(q1);
    return Numbers.toRatio(Numbers.add(q0,q1)); } 

  public final BinaryOperator<Ratio> adder () {
    return new BinaryOperator<Ratio> () {
      @Override
      public final String toString () { return "Ratio.add()"; }
      @Override
      public final Ratio apply (final Ratio q0, 
                                final Ratio q1) {
        return Ratios.this.add(q0,q1); } }; }

  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  public final Ratio negate (final Ratio q) {
    assert contains(q);
    return Numbers.toRatio(Numbers.minus(q)); } 

  public final UnaryOperator<Ratio> additiveInverse () {
    return new UnaryOperator<Ratio> () {
      @Override
      public final String toString () { return "Ratio.negate()"; }
      @Override
      public final Ratio apply (final Ratio q) {
        return Ratios.this.negate(q); } }; }

  //--------------------------------------------------------------

  @SuppressWarnings("static-method")
  private final Ratio zero () {
    return new Ratio(BigInteger.ZERO,BigInteger.ONE); }

  public final Object additiveIdentity () {
    return zero(); }

  //--------------------------------------------------------------

  public final Ratio multiply (final Ratio q0, 
                               final Ratio q1) {
    assert contains(q0);
    assert contains(q1);
    return Numbers.toRatio(Numbers.multiply(q0,q1)); } 

  public final BinaryOperator<Ratio> multiplier () {
    return new BinaryOperator<Ratio>() {
      @Override
      public final String toString () { return "Ratio.multiply()"; }
      @Override
      public final Ratio apply (final Ratio q0, 
                                final Ratio q1) {
        return Ratios.this.multiply(q0,q1); } }; }

  //--------------------------------------------------------------

  @SuppressWarnings("static-method")
  private final Ratio one () {
    return new Ratio(BigInteger.ONE,BigInteger.ONE); }

  public final Object multiplicativeIdentity () {
    return one(); }

  //--------------------------------------------------------------

  private final Ratio reciprocal (final Ratio q) {
    assert contains(q);
    // only a partial inverse
    final BigInteger n = q.numerator;
    final BigInteger d = q.denominator;
    // only a partial inverse
    if (BigInteger.ZERO.equals(n)) { return null; }
    return new Ratio(d,n); } 

  public final UnaryOperator<Ratio> multiplicativeInverse () {
    return new UnaryOperator<Ratio> () {
      @Override
      public final String toString () { return "Ratio.inverse()"; }
      @Override
      public final Ratio apply (final Ratio q) {
        return Ratios.this.reciprocal(q); } }; }

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    return element instanceof Ratio; }

  //--------------------------------------------------------------
  // Ratio.equals reduces both arguments before checking
  // numerator and denominators are equal.
  // Guessing our Ratios are usually already reduced.
  // Try n0*d1 == n1*d0 instead
  // TODO: use BigInteger.bitLength() to decide
  // which method to use?

  // clojure.lang.Ratio doesn't equate 1/1 and 2/2!

  @SuppressWarnings("static-method")
  public final boolean equals (final Ratio q0, 
                               final Ratio q1) {
    if (q0 == q1) { return true; }
    if (null == q0) {
      if (null == q1) { return true; }
      return false; }
    if (null == q1) { return false; }
    final BigInteger n0 = q0.numerator; 
    final BigInteger d0 = q0.denominator; 
    final BigInteger n1 = q1.numerator; 
    final BigInteger d1 = q1.denominator; 
    return n0.multiply(d1).equals(n1.multiply(d0)); }

  @Override
  public final BiPredicate equivalence () {
    return new BiPredicate<Ratio,Ratio>() {
      @Override
      public final boolean test (final Ratio q0, 
                                 final Ratio q1) {
        return Ratios.this.equals(q0,q1); } }; }

  //--------------------------------------------------------------

  @Override
  public final Supplier generator (final Map options) {
    final UniformRandomProvider urp = Set.urp(options);
    final Generator bfs = Generators.ratioGenerator(urp);
    return 
      new Supplier () {
      @Override
      public final Object get () { return bfs.next(); } }; }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { return 0; }

  // singleton
  @Override
  public final boolean equals (final Object that) {
    return that instanceof Ratios; }

  @Override
  public final String toString () { return "Ratios"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private Ratios () { }

  private static final Ratios SINGLETON = new Ratios();

  public static final Ratios get () { return SINGLETON; } 

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

