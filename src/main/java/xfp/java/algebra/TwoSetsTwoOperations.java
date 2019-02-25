package xfp.java.algebra;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.rng.UniformRandomProvider;

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
 * @version 2019-02-24
 */
@SuppressWarnings("unchecked")
public final class TwoSetsTwoOperations extends Structure {

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
  public final BiFunction multiply () { return _scale; }

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final ImmutableMap<Set,Supplier> 
  generators (final UniformRandomProvider urp) {
    final ImmutableMap.Builder<Set,Supplier> b = 
      ImmutableMap.builder();
    b.putAll(elements().generators(urp));
    b.putAll(scalars().generators(urp));
    return b.build(); }
  
  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { 
    return 
      Objects.hash(
        multiply(),
        elements(),
        scalars()); }

  @Override
  public boolean equals (Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final TwoSetsTwoOperations other = (TwoSetsTwoOperations) obj;
    if (! Objects.equals(multiply(),other.multiply())) {
      return false; }
    if (! Objects.equals(elements(),other.elements())) { 
      return false; }
    if (! Objects.equals(scalars(),other.scalars())) { 
      return false; }
    return true; }

  @Override
  public final String toString () { 
    return "S2O2[" + 
      //",\n" + multiply() + "," + 
      elements() + "," + scalars() +
      "]"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private 
  TwoSetsTwoOperations (final BiFunction multiply,
                        final Set elements,
                        final Set scalars,
                        final ImmutableList<Predicate<Map<Set,Supplier>>> laws) { 
    super(elements,laws);
    assert Objects.nonNull(multiply);
    assert Objects.nonNull(scalars);
    _scale = multiply;
    _scalars = scalars; }

  //--------------------------------------------------------------

  public static final TwoSetsTwoOperations 
  make (final BiFunction multiply,
        final Set elements,
        final Set scalars,
        final ImmutableList<Predicate<Map<Set,Supplier>>> laws) {

    return new TwoSetsTwoOperations(
      multiply,
      elements,
      scalars,
      laws); }

  //--------------------------------------------------------------
  /** Whether this returns a linear space, a module, or something
   * else is determined by the laws contained in the elements and
   * scalars.
   */
  public static final TwoSetsTwoOperations 
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
}
