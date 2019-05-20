package xfp.java.algebra;

import java.util.Map;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.google.common.collect.ImmutableList;

/** Group-like structures.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-24
 */
@SuppressWarnings("unchecked")
public final class OneSetOneOperation extends Structure {

  private final BinaryOperator _operation;
  public final BinaryOperator operation () { return _operation; }

  private final Object _identity;
  /** may be null. */
  public final Object identity () { return _identity; }

  private final UnaryOperator _inverse;
  /** may be null. */
  public final UnaryOperator inverse () { return _inverse; }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------
  // DANGER: relying on equivalence(), etc., returning equivalent
  // objects each time

  @Override
  public final int hashCode () {
    return Objects.hash(
      operation(),
      identity(),
      inverse(),
      equivalence(),
      elements()); }

  @Override
  public final boolean equals (final Object obj) {
    if (this == obj) { return true; }
    if (obj == null) { return false; }
    if (!(obj instanceof OneSetOneOperation)) { return false; }
    final OneSetOneOperation other = (OneSetOneOperation) obj;
    // WARNING: hard to tell if 2 operations are the same,
    // unless the implementing class has some kind of singleton
    // constraint.
    return
      Objects.equals(operation(),other.operation())
      &&
      Objects.equals(identity(),other.identity())
      &&
      Objects.equals(inverse(),other.inverse())
      &&
      Objects.equals(equivalence(),other.equivalence())
      &&
      Objects.equals(elements(),other.elements()); }

  @Override
  public final String toString () {
    return
      "S1O1[" +
      //operation() +
      //"," + identity() +
      //"," + inverse() + "," +
      elements()
      + "]"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private OneSetOneOperation (final BinaryOperator operation,
                              final Set elements,
                              final Object identity,
                              final UnaryOperator inverse,
                              final ImmutableList<Predicate<Map<Set,Supplier>>> laws) {
    super(elements,laws);
    assert Objects.nonNull(operation);
    _operation = operation;
    _identity = identity;
    _inverse = inverse; }

  //--------------------------------------------------------------

  public static final OneSetOneOperation
  make (final BinaryOperator operation,
        final Set elements,
        final Object identity,
        final UnaryOperator inverse,
        final ImmutableList<Predicate<Map<Set,Supplier>>> laws) {
    return new OneSetOneOperation(
      operation,elements,identity,inverse,laws); }

  public static final OneSetOneOperation
  make (final BinaryOperator operation,
        final Set elements,
        final ImmutableList<Predicate<Map<Set,Supplier>>> laws) {
    return make(operation,elements,null,null,laws); }

  //--------------------------------------------------------------

  public static final OneSetOneOperation
  magma (final BinaryOperator operation,
         final Set elements) {
    return
      make(operation,elements,Laws.magma(elements,operation)); }

  //--------------------------------------------------------------

  public static final OneSetOneOperation
  commutativeGroup (final BinaryOperator operation,
                    final Set elements,
                    final Object identity,
                    final UnaryOperator inverse) {
    return
      make(operation,elements,identity,inverse,
        Laws.commutativegroup(
          elements,operation,identity,inverse)); }

  //--------------------------------------------------------------
}
