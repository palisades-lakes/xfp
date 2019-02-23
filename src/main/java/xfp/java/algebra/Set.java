package xfp.java.algebra;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import org.apache.commons.rng.UniformRandomProvider;

import xfp.java.exceptions.Exceptions;

/** General, possibly unbounded, sets of <code>Object</code>s, 
 * and primitive values, as opposed to <code>java.util.Set</code>
 * (and primitve variants) which are enumerated finite sets.
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
 * @version 2019-02-22
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

  public default BiPredicate equivalence () {
    return Sets.OBJECT_EQUALS; }

  //--------------------------------------------------------------
  // TODO: should the default be an exception?
  @SuppressWarnings("unused")
  public default boolean contains (final Object element) {
    return false; }

  @SuppressWarnings("unused")
  public default boolean contains (final boolean element) {
    return false; }

  @SuppressWarnings("unused")
  public default boolean contains (final byte element) {
    return false; }

  @SuppressWarnings("unused")
  public default boolean contains (final short element) {
    return false; }

  @SuppressWarnings("unused")
  public default boolean contains (final int element) {
    return false; }

  @SuppressWarnings("unused")
  public default boolean contains (final long element) {
    return false; }

  @SuppressWarnings("unused")
  public default boolean contains (final float element) {
    return false; }

  @SuppressWarnings("unused")
  public default boolean contains (final double element) {
    return false; }

  @SuppressWarnings("unused")
  public default boolean contains (final char element) {
    return false; }

  //--------------------------------------------------------------
  
  public default String toString (final Object e) {
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

  //  public default boolean getBoolean (final Map options) {
  //    throw Exceptions.unsupportedOperation(
  //      this,"getBoolean",options); }
  //
  //  public default byte getByte (final Map options) {
  //    throw Exceptions.unsupportedOperation(
  //      this,"getByte",options); }
  //
  //  public default short getShort (final Map options) {
  //    throw Exceptions.unsupportedOperation(
  //      this,"getShort",options); }
  //
  //  public default int getInt (final Map options) {
  //    throw Exceptions.unsupportedOperation(
  //      this,"getInt",options); }
  //
  //  public default long getLong (final Map options) {
  //    throw Exceptions.unsupportedOperation(
  //      this,"getLong",options); }
  //
  //  public default float getFloat (final Map options) {
  //    throw Exceptions.unsupportedOperation(
  //      this,"getFloat",options); }
  //
  //  public default double getDouble (final Map options) {
  //    throw Exceptions.unsupportedOperation (
  //      this,"getDouble",options); }
  //
  //  public default char getChar (final Map options) {
  //    throw Exceptions.unsupportedOperation(
  //      this,"getChar",options); }

  //  public default Object get (final Map options) {
  //    throw Exceptions.unsupportedOperation(
  //      this,"get",options); }

  //--------------------------------------------------------------
  // TODO: don't always need a prng, might be iterating over a
  // set of edge cases, move prng to options?

  // TODO: is there a reasonable way to specify generating 
  // n_k elements from k=1..m sets?
  // In clojure, might take a map from set to count and return
  // a map from set to list of elements?

  // TODO: replace Supplier with an interface that has 
  // generateArray(), generateList(), etc, convenience methods.
  
  // TODO: replace Supplier with an interface than has 0-arity
  // <i>primititive</i>Value() methods?
  
  // TODO: move prng to options?

  public default Supplier generator (final UniformRandomProvider prng,
                                     final Map options) {
    throw Exceptions.unsupportedOperation(
      this,"generator",prng,options); }

  public default Supplier generator (final UniformRandomProvider prng) {
    return generator(prng,Collections.emptyMap()); }

  //--------------------------------------------------------------
  /** should eventually cover the whole set.
   * might be used for testing, so might make sense 'travel fast',
   * and go back to fill in.
   * 
   * TODO: unify with {@link #generator(UniformRandomProvider, Map)}
   * via options?
   */
  public default Supplier iterator (final Map options) {
    throw Exceptions.unsupportedOperation(
      this,"iterator",options); }

  public default Supplier iterator () {
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

