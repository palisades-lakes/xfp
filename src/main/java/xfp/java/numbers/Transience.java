package xfp.java.numbers;

import xfp.java.exceptions.Exceptions;

/** An interface for (im)mutable data structures.
 * <p>
 * This interface covers both mutable and immutable objects,
 * with 'mutating' methods returning an object that may or may not
 * be new. Methods are free to return a new instance
 * and may or may not invalidate the target of the method (similar
 * to the behavior of transient data structures in Clojure).
 *
 * TODO: compare to builder paradigm.
 *
 * TODO: implement careful explicit recycle/invalidate.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-03
 */

@SuppressWarnings("unchecked")
public interface Transience<T extends Transience>  {

  /** If {@link #isImmutable()}, return this. <br>
   * If not, invalidate <code>this</code> instance, and return a
   * new instance, reusing components of <code>this</code> as
   * desired.
   *
   * TODO: options argument?
   * TODO: a more foolproof way to handle this? Use reflection to
   * set all fields to <code>null</code>, <code>NaN</code>, etc.?
   */

  default T recycle () {
    throw Exceptions.unsupportedOperation(this,"recycle"); }

  /** If {@link #isImmutable()}, return this. <br>
   * If not, return a new instance, without modifying 
   * {@code this}.
   *
   * TODO: options argument?
   * TODO: a more foolproof way to handle this? Use reflection to
   * set all fields to <code>null</code>, <code>NaN</code>, etc.?
   */

  default T copy () {
    throw Exceptions.unsupportedOperation(this,"copy"); }

  /** If <code>this</code> has been invalidated in recycling,
   * and its possibly mutable components used elsewhere,
   * return false.
   *
   * TODO: a more foolproof way to handle this? Use reflection to
   * set all fields to <code>null</code>, <code>NaN</code>, etc.?
   */

  default boolean isValid () {
    throw Exceptions.unsupportedOperation(this,"isValid"); }

  /** Like <code>(transient foo)</code> in Clojure.
   * <p>
   * Return an equivalent object that acts like a
   * <code>transient</code> data structure in Clojure,
   * in the sense that mutating operations return a new reference,
   * which may point to a new instance, and may invalidate the
   * method's target instance.
   * In other words, only the returned reference is trustworthy.
   * <p>
   * Example:
   * <pre>
   * Uints u = x.recyclable(x);
   * for (int i=0;i<13;i++) { u = u.setWord(i,i); }
   * return u.immutable();
   * </pre>
   * <p>
   * Optional method.
   * @param init If <code>null</code>, return an empty recyclable
   * version of <code>this</code>. Otherwise return a recyclable
   * whose value is equivalent to <code>init</code>. Most common
   * cases:
   * <code>recyclable(null)</code> (if the state will be
   * overwritten without use) and
   * <code>recyclable(this)</code> (if we a temp copy of
   * <code>this</code> that can be incrementally modified
   * without creating lots of temp copies.
   * @see <a href="https://clojure.org/reference/transients">
   * Clojure: Transient Data Structures</a>
   */

  default T recyclable (final T init) {
    throw Exceptions.unsupportedOperation(this,"recyclable",init); }

  default T recyclable (final int n) {
    throw Exceptions.unsupportedOperation(this,"recyclable",n); }

  default T recyclable (final T init,
                        final int n) {
    throw Exceptions.unsupportedOperation(
      this,"recyclable",init,Integer.valueOf(n)); }

  /** Like <code>(persistent! foo)</code> in Clojure.
   * <p>
   * Return an equivalent sequence that is guaranteed to be
   * immutable, and safely shared across threads..
   * <p>
   * Example:
   * <pre>
   * Uints u = x.recyclable();
   * for (int i=0;i<13;i++) { *   u = u.setWord(i,i); }
   * return u.immutable(); }
   * </pre>
   * <p>
   * Optional method.
   * @see <a href="https://clojure.org/reference/transients">
   * Clojure: Transient Data Structures</a>
   */

  default T immutable () {
    throw Exceptions.unsupportedOperation(this,"immutable"); }

  default boolean isImmutable () {
    throw Exceptions.unsupportedOperation(this,"isImmutable"); }

  //--------------------------------------------------------------
}

