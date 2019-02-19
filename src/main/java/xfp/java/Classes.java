package xfp.java;

/** Null-safe class utilities.
 *
 * Static methods only; no state.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2018-09-27
 */

public final class Classes {

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------
  /** Return the class of <code>x</code>,
   * or <code>null</code> if <code>x</code> is <code>null</code>.
   */

  public static final Class getClass (final Object x) {
    if (null == x) { return null; }
    return x.getClass(); }

  //--------------------------------------------------------------
  /** Return the simple name of <code>x</code>,
   *  or <code>null</code> if <code>x</code> is <code>null</code>.
   */

  public static final String getSimpleName (final Class x) {
    if (null == x) { return "null"; }
    return x.getSimpleName(); }

  //--------------------------------------------------------------
  // disable constructor
  //--------------------------------------------------------------

  private Classes () {
    throw new UnsupportedOperationException(
      "can't instantiate " + getClass()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
