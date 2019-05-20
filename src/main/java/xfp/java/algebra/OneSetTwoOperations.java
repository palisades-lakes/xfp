package xfp.java.algebra;

import java.util.Map;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.google.common.collect.ImmutableList;

/** Ring-like structures
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-23
 */
@SuppressWarnings("unchecked")
public final class OneSetTwoOperations extends Structure {

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
  public boolean equals (final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
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

  private
  OneSetTwoOperations (final BinaryOperator add,
                       final Object additiveIdentity,
                       final UnaryOperator additiveInverse,
                       final BinaryOperator multiply,
                       final Object multiplicativeIdentity,
                       final UnaryOperator multiplicativeInverse,
                       final Set elements,
                       final ImmutableList<Predicate<Map<Set,Supplier>>> laws) {
    super(elements,laws);
    assert Objects.nonNull(add);
    assert Objects.nonNull(additiveIdentity);
    assert Objects.nonNull(additiveInverse);
    assert Objects.nonNull(multiply);
    assert Objects.nonNull(multiplicativeIdentity);
    // rings won't have multiplicative inverses
    //assert Objects.nonNull(multiplicativeInverse);
    _add = add;
    _additiveIdentity = additiveIdentity;
    _additiveInverse = additiveInverse;
    _multiply = multiply;
    _multiplicativeIdentity = multiplicativeIdentity;
    _multiplicativeInverse = multiplicativeInverse; }

  //--------------------------------------------------------------

  public static final OneSetTwoOperations
  make (final BinaryOperator add,
        final Object additiveIdentity,
        final UnaryOperator additiveInverse,
        final BinaryOperator multiply,
        final Object multiplicativeIdentity,
        final UnaryOperator multiplicativeInverse,
        final Set elements,
        final ImmutableList<Predicate<Map<Set,Supplier>>> laws) {

    return new OneSetTwoOperations(
      add,
      additiveIdentity,
      additiveInverse,
      multiply,
      multiplicativeIdentity,
      multiplicativeInverse,
      elements,
      laws); }

  //--------------------------------------------------------------

  public static final OneSetTwoOperations
  ring (final BinaryOperator add,
        final Object additiveIdentity,
        final UnaryOperator additiveInverse,
        final BinaryOperator multiply,
        final Object multiplicativeIdentity,
        final Set elements) {
    return make(
      add,additiveIdentity,additiveInverse,
      multiply,multiplicativeIdentity,null,
      elements,
      Laws.ring(
        add,additiveIdentity,additiveInverse,
        multiply,multiplicativeIdentity,elements)); }

  //--------------------------------------------------------------

  public static final OneSetTwoOperations
  commutativeRing (final BinaryOperator add,
                   final Object additiveIdentity,
                   final UnaryOperator additiveInverse,
                   final BinaryOperator multiply,
                   final Object multiplicativeIdentity,
                   final Set elements) {
    return make(
      add,additiveIdentity,additiveInverse,
      multiply,multiplicativeIdentity,null,
      elements,
      Laws.commutativeRing(
        add,additiveIdentity,additiveInverse,
        multiply,multiplicativeIdentity,elements)); }

  //--------------------------------------------------------------

  public static final OneSetTwoOperations
  field (final BinaryOperator add,
         final Object additiveIdentity,
         final UnaryOperator additiveInverse,
         final BinaryOperator multiply,
         final Object multiplicativeIdentity,
         final UnaryOperator multiplicativeInverse,
         final Set elements) {
    return make(
      add,additiveIdentity,additiveInverse,
      multiply,multiplicativeIdentity,multiplicativeInverse,
      elements,
      Laws.field(
        add,additiveIdentity,additiveInverse,
        multiply,multiplicativeIdentity,multiplicativeInverse,
        elements)); }

  //--------------------------------------------------------------
  /** Same operations as a field, but not associative or
   * distributive.
   */

  public static final OneSetTwoOperations
  floatingPoint (final BinaryOperator add,
                 final Object additiveIdentity,
                 final UnaryOperator additiveInverse,
                 final BinaryOperator multiply,
                 final Object multiplicativeIdentity,
                 final UnaryOperator multiplicativeInverse,
                 final Set elements) {
    return make(
      add,additiveIdentity,additiveInverse,
      multiply,multiplicativeIdentity,multiplicativeInverse,
      elements,
      Laws.floatingPoint(
        add,additiveIdentity,additiveInverse,
        multiply,multiplicativeIdentity,multiplicativeInverse,
        elements)); }

  //--------------------------------------------------------------
}
