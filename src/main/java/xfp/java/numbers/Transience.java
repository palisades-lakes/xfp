package xfp.java.numbers;

import xfp.java.exceptions.Exceptions;

/** An interface for (im)mutable data structures.
 * <p>
 * This interface covers both mutable and immutable objects,
 * with 'mutating' methods returning a 
 * object that may or may not be new. Methods are free
 * to return a new instance while invalidating the target of the 
 * method (similar to the behavior of transient data structures
 * in Clojure).
 * 
 * TODO: compare to builder paradigm.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-06-28
 */

@SuppressWarnings("unchecked")
public interface Transience {

  /** Like <code>(transient foo)</code> in Clojure.
   * <p>
   * Return an equivalent sequence that acts like a 
   * <code>transient</code> data structure in Clojure,
   * in the sense that mutating operations return a new reference,
   * which may point to a new instance, and may invalidate the 
   * method's target instance.
   * In other words, only the returned reference is trustworthy.
   * <p>
   * Example:
   * <pre>
   * Uints u = x.recyclable();
   * for (int i=0;i<13;i++) { u = u.setWord(i,i); }
   * return u.immutable(); 
   * </pre>
   * <p>
   * Optional method.
   * @see <a href="https://clojure.org/reference/transients">
   * Clojure: Transient Data Structures</a>
   */

  public default Transience recyclable () {
    throw Exceptions.unsupportedOperation(this,"recyclable"); }

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

  public default Transience immutable () {
    throw Exceptions.unsupportedOperation(this,"immutable"); }

  //--------------------------------------------------------------
}

