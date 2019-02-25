package xfp.java.numbers;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.apache.commons.rng.UniformRandomProvider;

import xfp.java.algebra.OneSetOneOperation;
import xfp.java.algebra.OneSetTwoOperations;
import xfp.java.algebra.Set;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;

/** The set of rational numbers represented by 
 * <code>BigDecimal</code>.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-25
 */
public final class BigDecimals implements Set {

  //--------------------------------------------------------------
  // operations for algebraic structures over BigDecimals.
  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  private final BigDecimal add (final BigDecimal q0, 
                                final BigDecimal q1) {
    assert contains(q0);
    assert contains(q1);
    return q0.add(q1); } 

  public final BinaryOperator<BigDecimal> adder () {
    return new BinaryOperator<BigDecimal> () {
      @Override
      public final String toString () { return "BD.add()"; }
      @Override
      public final BigDecimal apply (final BigDecimal q0, 
                                     final BigDecimal q1) {
        return BigDecimals.this.add(q0,q1); } }; }

  //--------------------------------------------------------------

  @SuppressWarnings("static-method")
  public final BigDecimal additiveIdentity () {
    return BigDecimal.ZERO; }
  
  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  private final BigDecimal negate (final BigDecimal q) {
    assert contains(q);
    return q.negate(); } 

  public final UnaryOperator<BigDecimal> additiveInverse () {
    return new UnaryOperator<BigDecimal> () {
      @Override
      public final String toString () { return "BD.negate()"; }
      @Override
      public final BigDecimal apply (final BigDecimal q) {
        return BigDecimals.this.negate(q); } }; }

  //--------------------------------------------------------------

  private final BigDecimal multiply (final BigDecimal q0, 
                                     final BigDecimal q1) {
    assert contains(q0);
    assert contains(q1);
    return q0.multiply(q1); } 

  public final BinaryOperator<BigDecimal> multiplier () {
    return new BinaryOperator<BigDecimal>() {
      @Override
      public final String toString () { return "BD.multiply()"; }
      @Override
      public final BigDecimal apply (final BigDecimal q0, 
                                     final BigDecimal q1) {
        return BigDecimals.this.multiply(q0,q1); } }; }

  //--------------------------------------------------------------
  
  @SuppressWarnings("static-method")
  public final BigDecimal multiplicativeIdentity () {
    return BigDecimal.ONE; }

  //--------------------------------------------------------------

  private final BigDecimal reciprocal (final BigDecimal q) {
    assert contains(q);
    // only a partial inverse
    if (BigDecimal.ZERO.equals(q)) { return null; }
    return BigDecimal.ONE.divide(q);  } 


  public final UnaryOperator<BigDecimal> multiplicativeInverse () {
    return new UnaryOperator<BigDecimal> () {
      @Override
      public final String toString () { return "BD.inverse()"; }
      @Override
      public final BigDecimal apply (final BigDecimal q) {
        return BigDecimals.this.reciprocal(q); } }; }

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    return element instanceof BigDecimal; }

  //--------------------------------------------------------------

  /** Note: BigDecimal.equal doesn't test for equality as rational
   * numbers.
   */
  @SuppressWarnings("static-method")
  public final boolean equals (final BigDecimal q0, 
                               final BigDecimal q1) {
    if (q0 == q1) { return true; }
    if (null == q0) { 
      if (null == q1) { return true; }
      return false; }
    if (null == q1) { return false; }
    final int c = q0.compareTo(q1);
    return 0 == c; }

  @Override
  public final BiPredicate equivalence () {
    return new BiPredicate<BigDecimal,BigDecimal>() {
      @Override
      public final String toString () { return "BD.equals()"; }
      @Override
      public final boolean test (final BigDecimal q0, 
                                 final BigDecimal q1) {
        return BigDecimals.this.equals(q0,q1); } }; }

  //--------------------------------------------------------------

  @Override
  public final Supplier generator (final UniformRandomProvider urp,
                                   final Map options) {
    final Generator bfs = Generators.bigDecimalGenerator(urp);
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

  // singleton?
  @Override
  public final boolean equals (final Object that) {
    return that instanceof BigDecimals; }

  @Override
  public final String toString () { return "BD"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private BigDecimals () { }

  private static final BigDecimals SINGLETON = 
    new BigDecimals();

  public static final BigDecimals get () { return SINGLETON; } 

  //--------------------------------------------------------------
  // pre-defined structures
  //--------------------------------------------------------------

  public static final OneSetOneOperation ADDITIVE_MAGMA = 
  OneSetOneOperation.magma(get().adder(),get());

  public static final OneSetOneOperation MULTIPLICATIVE_MAGMA = 
  OneSetOneOperation.magma(get().multiplier(),get());

  public static final OneSetTwoOperations RING = 
  OneSetTwoOperations.commutativeRing(
    get().adder(),
    get().additiveIdentity(),
    get().additiveInverse(),
    get().multiplier(),
    get().multiplicativeIdentity(),
    // no multiplicative inverse for BigDecimal
    // divide can result in non-terminating decimal expansion
    null, 
    get());


}
//--------------------------------------------------------------

