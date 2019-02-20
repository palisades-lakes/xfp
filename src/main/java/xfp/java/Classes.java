package xfp.java;

/** Null-safe class utilities.
 *
 * Static methods only; no state.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-19
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

  public static final String simpleName (final Class x) {
    if (null == x) { return "null"; }
    return x.getSimpleName(); }

  //--------------------------------------------------------------
  /** Return the simple class name of <code>x</code>,
   * or <code>null</code> if <code>x</code> is <code>null</code>.
   */

  public static final String className (final Object x) {
    return simpleName(getClass(x)); }

  //--------------------------------------------------------------
  // disable constructor
  //--------------------------------------------------------------

  private Classes () {
    throw new UnsupportedOperationException(
      "can't instantiate " + getClass()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
