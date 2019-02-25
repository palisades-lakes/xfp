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

import xfp.java.algebra.OneSetOneOperation;
import xfp.java.algebra.OneSetTwoOperations;
import xfp.java.algebra.Set;
import xfp.java.exceptions.Exceptions;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;

/** The set of rational numbers represented by 
 * <code>BigFraction</code>
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-23
 */
public final class BigFractions implements Set {

  //--------------------------------------------------------------
  // convert representation to BigFraction[] as default.
  // higher performance methods use raw representation where
  // computations are exact.
  //--------------------------------------------------------------

  public static final BigFraction toBigFraction (final double x) {
    return new BigFraction(x); }

  public static final BigFraction toBigFraction (final float x) {
    return new BigFraction(x); }

  public static final BigFraction toBigFraction (final long x) {
    return new BigFraction(x); }

  public static final BigFraction toBigFraction (final int x) {
    return new BigFraction(x); }

  public static final BigFraction toBigFraction (final short x) {
    return new BigFraction(x); }

  public static final BigFraction toBigFraction (final byte x) {
    return new BigFraction(x); }

  public static final BigFraction toBigFraction (final Number x) {
    if (x instanceof BigFraction) { return (BigFraction) x; }
    if (x instanceof Double) { 
      return new BigFraction(((Double) x).doubleValue()); }
    if (x instanceof Integer) {
      return new BigFraction(((Integer) x).intValue()); }
    if (x instanceof Long) { 
      final BigInteger bi = BigInteger.valueOf(((Long) x).longValue());
      return new BigFraction(bi); }
//    return new BigFraction(((Long) x).longValue()); }
    if (x instanceof Float) {
      return new BigFraction(((Float) x).floatValue()); }
    if (x instanceof Short) {
      return new BigFraction(((Short) x).intValue()); }
    if (x instanceof Byte) {
      return new BigFraction(((Byte) x).intValue()); }
    if (x instanceof BigInteger) {
      return new BigFraction(((BigInteger) x)); }
    throw Exceptions.unsupportedOperation(
      BigFractions.class,"toBigFraction",x); } 

  //--------------------------------------------------------------

  public static final BigFraction[] toBigFraction (final Number[] x) {
    final int n = x.length;
    final BigFraction[] y = new BigFraction[n];
    for (int i=0;i<n;i++) { y[i] = toBigFraction(x[i]); }
    return y; }

  public static final BigFraction[]
    toBigFraction (final double[] x) {
    final int n = x.length;
    final BigFraction[] y = new BigFraction[n];
    for (int i=0;i<n;i++) { y[i] = toBigFraction(x[i]); }
    return y; }

  public static final BigFraction[]
    toBigFraction (final float[] x) {
    final int n = x.length;
    final BigFraction[] y = new BigFraction[n];
    for (int i=0;i<n;i++) { y[i] = toBigFraction(x[i]); }
    return y; }

  public static final BigFraction[]
    toBigFraction (final long[] x) {
    final int n = x.length;
    final BigFraction[] y = new BigFraction[n];
    for (int i=0;i<n;i++) { y[i] = toBigFraction(x[i]); }
    return y; }

  public static final BigFraction[]
    toBigFraction (final int[] x) {
    final int n = x.length;
    final BigFraction[] y = new BigFraction[n];
    for (int i=0;i<n;i++) { y[i] = toBigFraction(x[i]); }
    return y; }

  public static final BigFraction[]
    toBigFraction (final short[] x) {
    final int n = x.length;
    final BigFraction[] y = new BigFraction[n];
    for (int i=0;i<n;i++) { y[i] = toBigFraction(x[i]); }
    return y; }

  public static final BigFraction[] 
    toBigFraction (final byte[] x) {
    final int n = x.length;
    final BigFraction[] y = new BigFraction[n];
    for (int i=0;i<n;i++) { y[i] = toBigFraction(x[i]); }
    return y; }

  //--------------------------------------------------------------

  public static final Object toBigFraction (final Object x) {

    if (x instanceof BigFraction) { return x; }
    if (x instanceof Number) { 
      return toBigFraction(((Number) x)); }
 
    if (x instanceof BigFraction[]) { return x; }

    if (x instanceof byte[]) { 
      return toBigFraction((byte[]) x); }

    if (x instanceof short[]) { 
      return toBigFraction((short[]) x); }

    if (x instanceof int[]) { 
      return toBigFraction((int[]) x); }

    if (x instanceof long[]) { 
      return toBigFraction((long[]) x); }

    if (x instanceof float[]) { 
      return toBigFraction((float[]) x); }

    if (x instanceof double[]) { 
      return toBigFraction((double[]) x); }

    if (x instanceof Number[]) { 
      return toBigFraction((Number[]) x); }

    throw Exceptions.unsupportedOperation(
      BigFractions.class,"toBigFraction",x); }

  //--------------------------------------------------------------
  // operations for algebraic structures over BigFractions.
  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  private final BigFraction add (final BigFraction q0, 
                                 final BigFraction q1) {
    assert contains(q0);
    assert contains(q1);
    return q0.add(q1); } 

  public final BinaryOperator<BigFraction> adder () {
    return new BinaryOperator<BigFraction> () {
      @Override
      public final String toString () { return "BF.add()"; }
      @Override
      public final BigFraction apply (final BigFraction q0, 
                                      final BigFraction q1) {
        return BigFractions.this.add(q0,q1); } }; }

  //--------------------------------------------------------------

  @SuppressWarnings("static-method")
  public final BigFraction additiveIdentity () {
    return BigFraction.ZERO; }
  
  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  private final BigFraction negate (final BigFraction q) {
    assert contains(q);
    return q.negate(); } 

  public final UnaryOperator<BigFraction> additiveInverse () {
    return new UnaryOperator<BigFraction> () {
      @Override
      public final String toString () { return "BF.negate()"; }
      @Override
      public final BigFraction apply (final BigFraction q) {
        return BigFractions.this.negate(q); } }; }

  //--------------------------------------------------------------

  private final BigFraction multiply (final BigFraction q0, 
                                      final BigFraction q1) {
    assert contains(q0);
    assert contains(q1);
    return q0.multiply(q1); } 

  public final BinaryOperator<BigFraction> multiplier () {
    return new BinaryOperator<BigFraction>() {
      @Override
      public final String toString () { return "BF.multiply()"; }
      @Override
      public final BigFraction apply (final BigFraction q0, 
                                      final BigFraction q1) {
        return BigFractions.this.multiply(q0,q1); } }; }

  //--------------------------------------------------------------

  @SuppressWarnings("static-method")
  public final BigFraction multiplicativeIdentity () {
    return BigFraction.ONE; }
  
  //--------------------------------------------------------------

  private final BigFraction reciprocal (final BigFraction q) {
    assert contains(q);
    // only a partial inverse
    if (BigFraction.ZERO.equals(q)) { return null; }
    return q.reciprocal();  } 

  public final UnaryOperator<BigFraction> multiplicativeInverse () {
    return new UnaryOperator<BigFraction> () {
      @Override
      public final String toString () { return "BF.inverse()"; }
      @Override
      public final BigFraction apply (final BigFraction q) {
        return BigFractions.this.reciprocal(q); } }; }

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    return element instanceof BigFraction; }

  //--------------------------------------------------------------
  // BigFraction.equals reduces both arguments before checking
  // numerator and denominators are equal.
  // Guessing our BigFractions are usually already reduced.
  // Try n0*d1 == n1*d0 instead
  // TODO: use BigInteger.bitLength() to decide
  // which method to use?

  @SuppressWarnings("static-method")
  public final boolean equals (final BigFraction q0, 
                               final BigFraction q1) {
    if (q0 == q1) { return true; }
    if (null == q0) {
      if (null == q1) { return true; }
      return false; }
    if (null == q1) { return false; }
    final BigInteger n0 = q0.getNumerator(); 
    final BigInteger d0 = q0.getDenominator(); 
    final BigInteger n1 = q1.getNumerator(); 
    final BigInteger d1 = q1.getDenominator(); 
    return n0.multiply(d1).equals(n1.multiply(d0)); }

  @Override
  public final BiPredicate equivalence () {
    return new BiPredicate<BigFraction,BigFraction>() {
      @Override
      public final boolean test (final BigFraction q0, 
                                 final BigFraction q1) {
        return BigFractions.this.equals(q0,q1); } }; }

  //--------------------------------------------------------------

  @Override
  public final Supplier generator (final UniformRandomProvider urp,
                                   final Map options) {
    final Generator bfs = Generators.bigFractionGenerator(urp);
    return 
      new Supplier () {
      @Override
      public final Object get () { return bfs.next(); } }; }

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
    return that instanceof BigFractions; }

  @Override
  public final String toString () { return "BF"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------


  private BigFractions () { }

  private static final BigFractions SINGLETON = new BigFractions();

  public static final BigFractions get () { return SINGLETON; } 

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
