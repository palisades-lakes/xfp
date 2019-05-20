package xfp.java;

import java.io.PrintStream;

/** Debugging output.
 * Hacky substitute for mess of dependencies and
 * configuration needed by java logging libraries.
 * Intended only for use during development;
 * no Debug.* references should persist in 'production' code.
 *
 * Static methods only; no state.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-27
 */

public final class Debug {

  public static boolean DEBUG = false;

  public static PrintStream OUT = System.out;

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------
  /** Hex string representation of the bits
   * implementing the double. Not at all the same as
   * {@link Double#toHexString}.
   */

  public static final String hexBits (final double x) {
    return
      Long.toHexString(Double.doubleToLongBits(x))
      .toUpperCase(); }

  //--------------------------------------------------------------

  public static final void println () {
    if (DEBUG) { OUT.println(); } }

  public static final void println (final String msg) {
    if (DEBUG) { OUT.println(msg); } }

  public static final void println (final boolean msg) {
    if (DEBUG) { OUT.println(msg); } }

  public static final void println (final int msg) {
    if (DEBUG) { OUT.println(msg); } }

  //--------------------------------------------------------------

  public static final void printf (final String format,
                                   final Object... args) {
    if (DEBUG) { OUT.printf(format,args); } }

  //--------------------------------------------------------------

  public static final void printf (final String format,
                                   final boolean arg) {
    if (DEBUG) { OUT.printf(format,Boolean.valueOf(arg)); } }

  //--------------------------------------------------------------

  public static final void printf (final String format,
                                   final int arg) {
    if (DEBUG) { OUT.printf(format,Integer.valueOf(arg)); } }

  //--------------------------------------------------------------

  public static final void printf (final String format,
                                   final double arg) {
    if (DEBUG) { OUT.printf(format,Double.valueOf(arg)); } }

  //--------------------------------------------------------------

  public static final void printf (final String format,
                                   final double arg0,
                                   final double arg1) {
    if (DEBUG) {
      OUT.printf(format,
        Double.valueOf(arg0), Double.valueOf(arg1)); } }

  //--------------------------------------------------------------
  // disable constructor
  //--------------------------------------------------------------

  private Debug () {
    throw new UnsupportedOperationException(
      "can't instantiate " + getClass()); }


  //--------------------------------------------------------------
}
//--------------------------------------------------------------
