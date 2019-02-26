package xfp.java.algebra;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/** Module-like structures, including linear (vector) spaces.
 * <p>
 * Two sets, 'elements' and 'scalars'.
 * One operation: 'multiplication' of elements by scalars.
 * <p>
 * The scalars are (usually) an instance of some one set two 
 * operation structure, like a ring or a field.
 * <p>
 * The elements are (usually) a group-like structure.
 * <p>
 * What kind of module-like structure an instance is is determined
 * by the laws satisfied by the element-element operation,
 * the operations on the scalar structure, and, less often,
 * by scalar-element operation.
 * <p>
 * It is nearly always assumed that scalar multiplication
 * distributes over element addition:
 * <code>a*(v+w) = (a*v) + (a*w)</code>.
 * <p>
 * Note that this doesn't work if we want to generalize
 * linear to affine spaces, etc.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-26
 */
@SuppressWarnings("unchecked")
public final class TwoSetsOneOperation extends Structure {

  //--------------------------------------------------------------

  private final Set _scalars;

  /** Secondary set.
   * <p>
   * Typically a ring (giving a module) or a field (giving a 
   * linear space).
   */
  public final Set scalars () { return _scalars; }

  //--------------------------------------------------------------
  // Operation over primary X secondary sets.
  
  private final BiFunction _scale;

  /** Multiply an element by a scalar. */
  public final BiFunction scale () { return _scale; }

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final ImmutableMap<Set,Supplier> 
  generators (final Map options) {
    final ImmutableMap.Builder<Set,Supplier> b = 
      ImmutableMap.builder();
    b.putAll(elements().generators(options));
    b.putAll(scalars().generators(options));
    return b.build(); }
  
  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { 
    return 
      Objects.hash(
        scale(),
        elements(),
        scalars()); }

  @Override
  public boolean equals (Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final TwoSetsOneOperation other = (TwoSetsOneOperation) obj;
    if (! Objects.equals(scale(),other.scale())) {
      return false; }
    if (! Objects.equals(elements(),other.elements())) { 
      return false; }
    if (! Objects.equals(scalars(),other.scalars())) { 
      return false; }
    return true; }

  @Override
  public final String toString () { 
    return "S2O1[" + 
      //",\n" + scale () + "," + 
      elements() + "," + scalars() +
      "]"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private 
  TwoSetsOneOperation (final BiFunction multiply,
                        final Set elements,
                        final Set scalars,
                        final ImmutableList<Predicate<Map<Set,Supplier>>> laws) { 
    super(elements,laws);
    assert Objects.nonNull(multiply);
    assert Objects.nonNull(scalars);
    _scale = multiply;
    _scalars = scalars; }

  //--------------------------------------------------------------

  public static final TwoSetsOneOperation 
  make (final BiFunction multiply,
        final Set elements,
        final Set scalars,
        final ImmutableList<Predicate<Map<Set,Supplier>>> laws) {

    return new TwoSetsOneOperation(
      multiply,
      elements,
      scalars,
      laws); }

  //--------------------------------------------------------------
  /** Whether this returns a linear space, a module, or something
   * else is determined by the laws contained in the elements and
   * scalars.
   */
  
  public static final TwoSetsOneOperation 
  linearSpaceLike (final BiFunction multiply,
                   final Set elements,
                   final Set scalars) {
    return make(
      multiply,
      elements,
      scalars,
      Laws.linearSpaceLike(
        multiply,
        (OneSetOneOperation) elements,
        (OneSetTwoOperations) scalars)); }

  //--------------------------------------------------------------
  /** Same operations as module/linear space, but few laws apply,
   * due to floating point rounding.
   */
  
  public static final TwoSetsOneOperation 
  floatingPointSpace (final BiFunction multiply,
                   final Set elements,
                   final Set scalars) {
    return make(
      multiply,
      elements,
      scalars,
      Laws.floatingPointSpace(
        multiply,
        (OneSetOneOperation) elements,
        (OneSetTwoOperations) scalars)); }

  //--------------------------------------------------------------
}
