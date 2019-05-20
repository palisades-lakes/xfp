package xfp.java.algebra;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/** Base class for mathematical structures on collections of
 * sets.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-23
 */
@SuppressWarnings("unchecked")
public abstract class Structure implements Set {

  private final Set _elements;
  /** The 'primary' set, if there is one. */
  public final Set elements () { return _elements; }

  private final ImmutableList<Predicate<Map<Set,Supplier>>> _laws;

  /** Each laws is a predicate that takes a map from
   * some of the sets involved in the structure to generators
   * of test elements of those sets.
   */
  public final Iterable<Predicate<Map<Set,Supplier>>> laws () {
    return _laws; }

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object x) {
    return _elements.contains(x); }

  // TODO: should there be an _equivalence slot?
  // instead of inheriting from _elements?
  // Would it be a good idea to allow an equivalence relation
  // different from the element set?
  // ---probably not. could always have a wrapper set that changes
  // the equivalence relation.

  @Override
  public final BiPredicate equivalence () {
    return _elements.equivalence(); }

  @Override
  public final Supplier generator (final Map options) {
    return _elements.generator(options); }

  // TODO: more general specification for generators
  @Override
  public ImmutableMap<Set,Supplier>
  generators (final Map options) {
    return elements().generators(options); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  public
  Structure (final Set elements,
             final ImmutableList<Predicate<Map<Set,Supplier>>> laws) {
    assert Objects.nonNull(elements);
    assert Objects.nonNull(laws);
    _elements = elements;
    _laws = laws; }

  //--------------------------------------------------------------
}
