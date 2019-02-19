package xfp.java.algebra;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.rng.UniformRandomProvider;

import xfp.java.numbers.BigDecimals;
import xfp.java.numbers.BigFractions;
import xfp.java.numbers.Q;
import xfp.java.numbers.Ratios;

/** One set plus 2 operations.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-19
 */
@SuppressWarnings("unchecked")
public final class OneSetTwoOperations implements Set {

  // Two operations:
  // TODO: additive identity usually doesn't have a
  // multiplicative inverse. how should we indicate that?

  // operation 0
  private final BinaryOperator _add;
  // may be null
  private final Object _additiveIdentity;
  // may be null
  private final UnaryOperator _additiveInverse;

  // operation 1
  private final BinaryOperator _multiply;
  // may be null
  private final Object _multiplicativeIdentity;
  // may be null
  private final UnaryOperator _multiplicativeInverse;

  // one set
  private final Set _elements;

  //--------------------------------------------------------------
  // methods 
  //--------------------------------------------------------------

  public final BinaryOperator add () { return _add; }
  public final UnaryOperator additiveInverse () { 
    return _additiveInverse; }
  // TODO: return a Supplier (nullary operator) instead?
  public final Object additiveIdentity () { 
    return _additiveIdentity; }

  public final BinaryOperator multiply () { return _multiply; }
  /** Applying the <code>multiplicativeInverse</code> to the
   * <code>additiveIdentity</code> will throw an exception.
   * TODO: is that always true?
   */
  public final UnaryOperator multiplicativeInverse () { 
    return _multiplicativeInverse; }
  // TODO: return a Supplier (nullary operator) instead?
  public final Object multiplicativeIdentity () { 
    return _multiplicativeIdentity; }

  public final Set elements () { return _elements; }

  //--------------------------------------------------------------
  // laws for some specific algebraic structures, for testing

  public final List<Predicate> 
  semiringLaws () {
    return Laws.semiring(
      add(),additiveIdentity(),
      multiply(),multiplicativeIdentity(),
      elements()); }

  public final List<Predicate> 
  ringLaws () {
    return Laws.ring(
      add(),additiveIdentity(),additiveInverse(),
      multiply(),multiplicativeIdentity(),
      elements()); }

  public final List<Predicate> 
  commutativeRingLaws () {
    return Laws.commutativeRing(
      add(),additiveIdentity(),additiveInverse(),
      multiply(),multiplicativeIdentity(),
      elements()); }

  public final List<Predicate> 
  divisionRingLaws () {
    return Laws.divisionRing(
      add(),additiveIdentity(),additiveInverse(),
      multiply(),multiplicativeIdentity(),multiplicativeInverse(),
      elements()); } 

  public final List<Predicate> 
  fieldLaws () {
    return Laws.field(
      add(),
      additiveIdentity(),
      additiveInverse(),
      multiply(),
      multiplicativeIdentity(),
      multiplicativeInverse(),
      elements()); } 

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object x) {
    return _elements.contains(x); }

  @Override
  public final BiPredicate equivalence () {
    return _elements.equivalence(); }

  @Override
  public final Supplier generator (final UniformRandomProvider prng,
                                 final Map options) { 
    return _elements.generator(prng,options); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { 
    return 
      Objects.hash(
        add(),
        additiveIdentity(),
        additiveInverse(),
        multiply(),
        multiplicativeIdentity(),
        multiplicativeInverse(),
        elements()); }

  @Override
  public boolean equals (Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final OneSetTwoOperations other = (OneSetTwoOperations) obj;
    if (! Objects.equals(add(),other.add())) { 
      return false; }
    if (! Objects.equals(
      additiveIdentity(),other.additiveIdentity())) { 
      return false; }
    if (! Objects.equals(
      additiveInverse(),other.additiveInverse())) { 
      return false; }
    if (! Objects.equals(multiply(),other.multiply())) {
      return false; }
    if (! Objects.equals(
      multiplicativeIdentity(),other.multiplicativeIdentity())) { 
      return false; }
    if (! Objects.equals
      (multiplicativeInverse(),other.multiplicativeInverse())) { 
      return false; }
    if (! Objects.equals(elements(),other.elements())) { 
      return false; }
    return true; }

  @Override
  public final String toString () { 
    return "S1O2[" + 
      // add() + 
      //"," + additiveIdentity() + 
      //"," + additiveInverse() + 
      //",\n" + multiply() + 
      //"," + multiplicativeIdentity() + 
      //"," + multiplicativeInverse() +
      //",\n" + 
      elements() +
      "]"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------


  private OneSetTwoOperations (final BinaryOperator add,
                               final Object additiveIdentity,
                               final UnaryOperator additiveInverse,
                               final BinaryOperator multiply,
                               final Object multiplicativeIdentity,
                               final UnaryOperator multiplicativeInverse,
                               final Set elements) { 
    assert Objects.nonNull(add);
    assert Objects.nonNull(additiveIdentity);
    assert Objects.nonNull(additiveInverse);
    assert Objects.nonNull(multiply);
    assert Objects.nonNull(multiplicativeIdentity);
    // rings won't have multiplicative inverses
    //assert Objects.nonNull(multiplicativeInverse);
    assert Objects.nonNull(elements);
    _add = add;
    _additiveIdentity = additiveIdentity;
    _additiveInverse = additiveInverse;
    _multiply = multiply;
    _multiplicativeIdentity = multiplicativeIdentity;
    _multiplicativeInverse = multiplicativeInverse;
    _elements= elements; }

  //--------------------------------------------------------------
  // TODO: is it worth implementing singleton constraint?

  //  private static final Map<Magma,Magma> _cache = 
  //    new HashMap();

  //--------------------------------------------------------------

  public static final OneSetTwoOperations 
  make (final BinaryOperator add,
        final Object additiveIdentity,
        final UnaryOperator additiveInverse,
        final BinaryOperator multiply,
        final Object multiplicativeIdentity,
        final UnaryOperator multiplicativeInverse,
        final Set elements) {

    return new OneSetTwoOperations(
      add,
      additiveIdentity,
      additiveInverse,
      multiply,
      multiplicativeIdentity,
      multiplicativeInverse,
      elements); }

  //--------------------------------------------------------------

  public static final OneSetTwoOperations BIGDECIMALS_RING = 
    OneSetTwoOperations.make(
      BigDecimals.ADD,
      BigDecimal.ZERO,
      BigDecimals.ADDITIVE_INVERSE,
      BigDecimals.MULTIPLY,
      BigDecimal.ONE,
      // no multiplicative inverse for BigDecimal
      // divide can result in non-terminating decimal expansion
      null, 
      BigDecimals.get());

  public static final OneSetTwoOperations BIGFRACTIONS_FIELD = 
    OneSetTwoOperations.make(
      BigFractions.ADD,
      BigFraction.ZERO,
      BigFractions.ADDITIVE_INVERSE,
      BigFractions.MULTIPLY,
      BigFraction.ONE,
      BigFractions.MULTIPLICATIVE_INVERSE,
      BigFractions.get());

  public static final OneSetTwoOperations RATIOS_FIELD = 
    OneSetTwoOperations.make(
      Ratios.ADD,
      Ratios.ZERO,
      Ratios.ADDITIVE_INVERSE,
      Ratios.MULTIPLY,
      Ratios.ONE,
      Ratios.MULTIPLICATIVE_INVERSE,
      Ratios.get());

  public static final OneSetTwoOperations Q_FIELD = 
    OneSetTwoOperations.make(
      Q.ADD,
      BigFraction.ZERO,
      Q.ADDITIVE_INVERSE,
      Q.MULTIPLY,
      BigFraction.ONE,
      Q.MULTIPLICATIVE_INVERSE,
      Q.get());

  //--------------------------------------------------------------
}
