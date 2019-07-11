package xfp.java.algebra;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import org.apache.commons.rng.UniformRandomProvider;

import com.google.common.collect.ImmutableMap;

import clojure.lang.Keyword;
import xfp.java.exceptions.Exceptions;
import xfp.java.prng.PRNG;

/** General, possibly unbounded, sets of <code>Object</code>s,
 * and primitive values, as opposed to <code>java.util.Set</code>
 * (and primitive variants) which are enumerated finite sets.
 *
 * A usable set provides 2 basic functionalities:
 * <ol>
 * <li> Given a thing, a way to tell if that thing is in the set
 * (<code>contains</code>).
 * <li> A way to get at least some elements of the (non-empty)
 * set.
 * <ol>
 * <li> What you have to specify to determine a particular
 * element will depend on the details of that set,
 * so <code>getXxx</code> methods take an opaque
 * <code>options</code> argument.
 * It's also useful, for testing, to require some simple
 * <code>sample</code> methods, that default to something
 * reasonable in the no-arg case, allow seeded randomization
 * in the 1-arg case, and accept a general <code>options</code>
 * when desired.
 * </ol>
 *
 * This might be more accurately called
 * (see <a href="https://en.wikipedia.org/wiki/Setoid">
 * <code>Setoid</code></a>.
 *
 * Default <code>contains</code> return <code>false</code>
 * for every thing (ie, default is empty set).
 *
 * <b>TODO:</b> replace sampling iterators with 0-arg functions?
 *
 * <b>TODO:<.b> should a Set require an equality relation,
 * not necessarily the same as <code>equals</code>?
 * ...since multiple classes might be used to represent the
 * elements. Or the set might really be the set of equivalence
 * classes, represented by some element of each equivalence class.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-05
 */

public interface Set {

  // TODO: would it be better treat base sets as collections
  // of objects with eq/identity equivalence, and add quotient
  // sets (sets of equivalence classes) on top of that?

  // TODO: equivalence for primitives? Need to replace
  // BiPredicate with an interface that handles all pairs of
  // primtives? 8x8 = 64 methods, though we might reduce to
  // (boolean,boolean), (char,char) + 6x6 number primitives

  // TODO: would identity (eq, ==) be better than Object.equals()
  // as the default?

  /** The equivalence relation that's used to map implementation
   * objects to the true elements of the set, which are
   * equivalence classes of objects.
   */

  default BiPredicate equivalence () {
    return Sets.OBJECT_EQUALS; }

  //--------------------------------------------------------------
  // TODO: should the default be an exception?
  @SuppressWarnings("unused")
  default boolean contains (final Object element) {
    return false; }

  @SuppressWarnings("unused")
  default boolean contains (final boolean element) {
    return false; }

  @SuppressWarnings("unused")
  default boolean contains (final byte element) {
    return false; }

  @SuppressWarnings("unused")
  default boolean contains (final short element) {
    return false; }

  @SuppressWarnings("unused")
  default boolean contains (final int element) {
    return false; }

  @SuppressWarnings("unused")
  default boolean contains (final long element) {
    return false; }

  @SuppressWarnings("unused")
  default boolean contains (final float element) {
    return false; }

  @SuppressWarnings("unused")
  default boolean contains (final double element) {
    return false; }

  @SuppressWarnings("unused")
  default boolean contains (final char element) {
    return false; }

  //--------------------------------------------------------------

  default String toString (final Object e) {
    assert contains(e);
    if (e instanceof byte[]) {
      return Arrays.toString((byte[]) e); }
    if (e instanceof boolean[]) {
      return Arrays.toString((boolean[]) e); }
    if (e instanceof char[]) {
      return Arrays.toString((char[]) e); }
    if (e instanceof int[]) {
      return Arrays.toString((int[]) e); }
    if (e instanceof long[]) {
      return Arrays.toString((long[]) e); }
    if (e instanceof float[]) {
      return Arrays.toString((float[]) e); }
    if (e instanceof double[]) {
      return Arrays.toString((double[]) e); }
    if (e instanceof Object[]) {
      return Arrays.toString((Object[]) e); }
    return Objects.toString(e); }

  //--------------------------------------------------------------
  // TODO: is this just a Function that maps options to values?
  // That is, Is a set a function from all java objects to its
  // elements?

  //  default boolean getBoolean (final Map options) {
  //    throw Exceptions.unsupportedOperation(
  //      this,"getBoolean",options); }
  //
  //  default byte getByte (final Map options) {
  //    throw Exceptions.unsupportedOperation(
  //      this,"getByte",options); }
  //
  //  default short getShort (final Map options) {
  //    throw Exceptions.unsupportedOperation(
  //      this,"getShort",options); }
  //
  //  default int getInt (final Map options) {
  //    throw Exceptions.unsupportedOperation(
  //      this,"getInt",options); }
  //
  //  default long getLong (final Map options) {
  //    throw Exceptions.unsupportedOperation(
  //      this,"getLong",options); }
  //
  //  default float getFloat (final Map options) {
  //    throw Exceptions.unsupportedOperation(
  //      this,"getFloat",options); }
  //
  //  default double getDouble (final Map options) {
  //    throw Exceptions.unsupportedOperation (
  //      this,"getDouble",options); }
  //
  //  default char getChar (final Map options) {
  //    throw Exceptions.unsupportedOperation(
  //      this,"getChar",options); }

  //  default Object get (final Map options) {
  //    throw Exceptions.unsupportedOperation(
  //      this,"get",options); }

  //--------------------------------------------------------------
  // TODO: is there a reasonable way to specify generating
  // n_k elements from k=1..m sets?
  // In clojure, might take a map from set to count and return
  // a map from set to list of elements?

  // TODO: replace Supplier with an interface that has
  // generateArray(), generateList(), etc, convenience methods.

  // TODO: replace Supplier with an interface than has 0-arity
  // <i>primititive</i>Value() methods?

  public static final Keyword URP = Keyword.intern("urp");
  public static final Keyword SEED = Keyword.intern("seed");

  /** Get a <code>UniformRandomProvider</code>
   * based on the <code>options</code>:
   * <ol>
   * <li> First look for <code>UniformRandomProvider</code> value
   * associated with the key * <code>:urp</code>. If not null,
   * asn the right type, return that.
   * <li> Then look for an <code>int[]</code> value for the
   * <code>:seed</code> key. NUll or not, the seed value is
   * passed to {@link PRNG#well44497b(int[])} to create the
   * <code>UniformRandomProvider</code>. A null seed is
   * equivalent to choosing a new 'random' seed for the
   * <code>UniformRandomProvider</code>.
   * <p>
   * In the future may use other options to choose a
   * <code>UniformRandomProvider</code>.
   */

  public static UniformRandomProvider urp (final Map options) {
    final UniformRandomProvider u0 =
      (UniformRandomProvider) URP.invoke(options);
    if (null != u0) { return u0; }
    final Object seed = SEED.invoke(options);
    return PRNG.well44497b(seed);  }

  default Supplier generator (final Map options) {
    throw Exceptions.unsupportedOperation(
      this,"generator",options); }

  /** Return as a trivial map to make it easier to merge into
   * results for more complicated structures of which this set
   * might be a component.
   */
  default ImmutableMap<Set,Supplier>
  generators (final Map options) {
    return ImmutableMap.of(this, generator(options)); }

  //--------------------------------------------------------------
  /** should eventually cover the whole set.
   * might be used for testing, so might make sense 'travel fast',
   * and go back to fill in.
   *
   * TODO: unify with {@link #generator(UniformRandomProvider, Map)}
   * via options?
   */
  default Supplier iterator (final Map options) {
    throw Exceptions.unsupportedOperation(
      this,"iterator",options); }

  default Supplier iterator () {
    return iterator(Collections.emptyMap()); }

  public static Set EMPTY_SET = new Set() {};

  /** Contains all Java Objects and primitives. */
  public static Set ALL_JAVA_VALUES = new Set() {

    @Override
    @SuppressWarnings("unused")
    public boolean contains (final Object element) {
      return true; }

    @Override
    @SuppressWarnings("unused")
    public boolean contains (final boolean element) {
      return true; }

    @Override
    @SuppressWarnings("unused")
    public boolean contains (final byte element) {
      return true; }

    @Override
    @SuppressWarnings("unused")
    public boolean contains (final short element) {
      return true; }

    @Override
    @SuppressWarnings("unused")
    public boolean contains (final int element) {
      return true; }

    @Override
    @SuppressWarnings("unused")
    public boolean contains (final long element) {
      return true; }

    @Override
    @SuppressWarnings("unused")
    public boolean contains (final float element) {
      return true; }

    @Override
    @SuppressWarnings("unused")
    public boolean contains (final double element) {
      return true; }

    @Override
    @SuppressWarnings("unused")
    public boolean contains (final char element) {
      return true; } };

      //--------------------------------------------------------------
}
//--------------------------------------------------------------

