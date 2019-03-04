package xfp.java.numbers;

import java.math.BigInteger;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.apache.commons.rng.UniformRandomProvider;

import com.upokecenter.numbers.EInteger;
import com.upokecenter.numbers.ERational;

import xfp.java.algebra.OneSetOneOperation;
import xfp.java.algebra.OneSetTwoOperations;
import xfp.java.algebra.Set;
import xfp.java.exceptions.Exceptions;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;

/** The set of rational numbers represented by 
 * <code>ERational</code>
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-03
 */
public final class ERationals implements Set {

  //--------------------------------------------------------------
  // convert representation to ERational[] as default.
  // higher performance methods use raw representation where
  // computations are exact.
  //--------------------------------------------------------------

  public static final ERational toERational (final double x) {
    return ERational.FromDouble(x); }

  public static final ERational toERational (final float x) {
    return ERational.FromSingle(x); }

  public static final ERational toERational (final long x) {
    return ERational.FromInt64(x); }

  public static final ERational toERational (final int x) {
    return ERational.FromInt32(x); }

  public static final ERational toERational (final short x) {
    return ERational.FromInt16(x); }

  public static final ERational toERational (final byte x) {
    return ERational.FromByte(x); }

  public static final ERational toERational (final Number x) {
    // TODO: contribute or fork ERational extends Number?
    //if (x instanceof ERational) { return (ERational) x; }
    //    if (x instanceof BigDecimal) {
    //      return toERational(((BigDecimal) x).intValue()); }
    if (x instanceof BigInteger) {
      // TODO: is this correct?
      return ERational.FromEInteger(
        EInteger.FromBytes(
          ((BigInteger) x).toByteArray(),
          false)); }
    if (x instanceof Double) { 
      return toERational(((Double) x).doubleValue()); }
    if (x instanceof Integer) {
      return toERational(((Integer) x).intValue()); }
    if (x instanceof Long) { 
      return toERational(((Long) x).longValue()); }
    if (x instanceof Float) {
      return toERational(((Float) x).floatValue()); }
    if (x instanceof Short) {
      return toERational(((Short) x).intValue()); }
    if (x instanceof Byte) {
      return toERational(((Byte) x).intValue()); }
    //    if (x instanceof EInteger) {
    //      return ERational.FromEInteger((EInteger) x); }
    throw Exceptions.unsupportedOperation(
      ERationals.class,"toERational",x); } 

  //--------------------------------------------------------------

  public static final ERational[] toERational (final Number[] x) {
    final int n = x.length;
    final ERational[] y = new ERational[n];
    for (int i=0;i<n;i++) { y[i] = toERational(x[i]); }
    return y; }

  public static final ERational[]
    toERational (final double[] x) {
    final int n = x.length;
    final ERational[] y = new ERational[n];
    for (int i=0;i<n;i++) { y[i] = toERational(x[i]); }
    return y; }

  public static final ERational[]
    toERational (final float[] x) {
    final int n = x.length;
    final ERational[] y = new ERational[n];
    for (int i=0;i<n;i++) { y[i] = toERational(x[i]); }
    return y; }

  public static final ERational[]
    toERational (final long[] x) {
    final int n = x.length;
    final ERational[] y = new ERational[n];
    for (int i=0;i<n;i++) { y[i] = toERational(x[i]); }
    return y; }

  public static final ERational[]
    toERational (final int[] x) {
    final int n = x.length;
    final ERational[] y = new ERational[n];
    for (int i=0;i<n;i++) { y[i] = toERational(x[i]); }
    return y; }

  public static final ERational[]
    toERational (final short[] x) {
    final int n = x.length;
    final ERational[] y = new ERational[n];
    for (int i=0;i<n;i++) { y[i] = toERational(x[i]); }
    return y; }

  public static final ERational[] 
    toERational (final byte[] x) {
    final int n = x.length;
    final ERational[] y = new ERational[n];
    for (int i=0;i<n;i++) { y[i] = toERational(x[i]); }
    return y; }

  //--------------------------------------------------------------

  public static final Object toERational (final Object x) {

    if (x instanceof ERational) { return x; }
    if (x instanceof Number) { 
      return toERational(((Number) x)); }

    if (x instanceof ERational[]) { return x; }

    if (x instanceof byte[]) { 
      return toERational((byte[]) x); }

    if (x instanceof short[]) { 
      return toERational((short[]) x); }

    if (x instanceof int[]) { 
      return toERational((int[]) x); }

    if (x instanceof long[]) { 
      return toERational((long[]) x); }

    if (x instanceof float[]) { 
      return toERational((float[]) x); }

    if (x instanceof double[]) { 
      return toERational((double[]) x); }

    if (x instanceof Number[]) { 
      return toERational((Number[]) x); }

    throw Exceptions.unsupportedOperation(
      ERationals.class,"toERational",x); }

  //--------------------------------------------------------------
  // from ERational to other numbers
  // adapted from clojure.lang.Ratio
  //--------------------------------------------------------------

//  public static final EInteger 
//  bigIntegerValue (final ERational f){
//    return f.getNumerator().divide(f.getDenominator()); }
//
//  public static final BigDecimal 
//  decimalValue (final ERational f,
//                final MathContext mc) {
//    final BigDecimal numerator = 
//      new BigDecimal(f.getNumerator());
//    final BigDecimal denominator = 
//      new BigDecimal(f.getDenominator());
//    return numerator.divide(denominator, mc); }
//
//  public static final BigDecimal 
//  decimalValue (final ERational f) {
//    return decimalValue(f,MathContext.UNLIMITED); }

  public static final double doubleValue (final ERational f) {
    return f.ToDouble(); }

  public static final int intValue (final ERational f) {
    return f.ToInt32Checked(); }

  public static final long longValue (final ERational f) {
    return f.ToInt64Checked(); }

  public static final float floatValue (final ERational f) {
    return f.ToSingle(); }

  //--------------------------------------------------------------
  // operations for algebraic structures over ERationals.
  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  private final ERational add (final ERational q0, 
                               final ERational q1) {
    assert contains(q0);
    assert contains(q1);
    return q0.Add(q1); } 

  public final BinaryOperator<ERational> adder () {
    return new BinaryOperator<ERational> () {
      @Override
      public final String toString () { return "BF.add()"; }
      @Override
      public final ERational apply (final ERational q0, 
                                    final ERational q1) {
        return ERationals.this.add(q0,q1); } }; }

  //--------------------------------------------------------------

  @SuppressWarnings("static-method")
  public final ERational additiveIdentity () {
    return ERational.Zero; }

  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  private final ERational negate (final ERational q) {
    assert contains(q);
    return q.Negate(); } 

  public final UnaryOperator<ERational> additiveInverse () {
    return new UnaryOperator<ERational> () {
      @Override
      public final String toString () { return "BF.negate()"; }
      @Override
      public final ERational apply (final ERational q) {
        return ERationals.this.negate(q); } }; }

  //--------------------------------------------------------------

  private final ERational multiply (final ERational q0, 
                                    final ERational q1) {
    assert contains(q0);
    assert contains(q1);
    return q0.Multiply(q1); } 

  public final BinaryOperator<ERational> multiplier () {
    return new BinaryOperator<ERational>() {
      @Override
      public final String toString () { return "BF.multiply()"; }
      @Override
      public final ERational apply (final ERational q0, 
                                    final ERational q1) {
        return ERationals.this.multiply(q0,q1); } }; }

  //--------------------------------------------------------------

  @SuppressWarnings("static-method")
  public final ERational multiplicativeIdentity () {
    return ERational.One; }

  //--------------------------------------------------------------

  private final ERational reciprocal (final ERational q) {
    assert contains(q);
    // only a partial inverse
    if (ERational.Zero.equals(q)) { return null; }
    return 
      ERational.Create(q.getDenominator(),q.getNumerator());  } 

  public final UnaryOperator<ERational> multiplicativeInverse () {
    return new UnaryOperator<ERational> () {
      @Override
      public final String toString () { return "BF.inverse()"; }
      @Override
      public final ERational apply (final ERational q) {
        return ERationals.this.reciprocal(q); } }; }

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    return element instanceof ERational; }

  //--------------------------------------------------------------
  // ERational.equals reduces both arguments before checking
  // numerator and denominators are equal.
  // Guessing our ERationals are usually already reduced.
  // Try n0*d1 == n1*d0 instead
  // TODO: use EInteger.bitLength() to decide
  // which method to use?

  @SuppressWarnings("static-method")
  public final boolean equals (final ERational q0, 
                               final ERational q1) {
    if (q0 == q1) { return true; }
    if (null == q0) {
      if (null == q1) { return true; }
      return false; }
    if (null == q1) { return false; }
    final EInteger n0 = q0.getNumerator(); 
    final EInteger d0 = q0.getDenominator(); 
    final EInteger n1 = q1.getNumerator(); 
    final EInteger d1 = q1.getDenominator(); 
    return n0.Multiply(d1).equals(n1.Multiply(d0)); }

  @Override
  public final BiPredicate equivalence () {
    return new BiPredicate<ERational,ERational>() {
      @Override
      public final boolean test (final ERational q0, 
                                 final ERational q1) {
        return ERationals.this.equals(q0,q1); } }; }

  //--------------------------------------------------------------

  @Override
  public final Supplier generator (final Map options) {
    final UniformRandomProvider urp = Set.urp(options);
    final Generator bfs = Generators.eRationalGenerator(urp);
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
    return that instanceof ERationals; }

  @Override
  public final String toString () { return "BF"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------


  private ERationals () { }

  private static final ERationals SINGLETON = new ERationals();

  public static final ERationals get () { return SINGLETON; } 

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

