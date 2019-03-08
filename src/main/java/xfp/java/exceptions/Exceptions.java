package xfp.java.exceptions;

import java.util.stream.Stream;

import xfp.java.Classes;

/** Exception utilities.
 * <p>
 * Static methods only; no state.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-07
 */

public final class Exceptions {

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------

  public static final UnsupportedOperationException 
  unsupportedOperation (final String receiver,
                        final String method,
                        final String... args) {
    return 
      new UnsupportedOperationException(
        "No " + receiver +
        "." + method + "(" + 
        String.join(",",args) + ")"); }

  public static final UnsupportedOperationException 
  unsupportedOperation (final Class receiver,
                        final String method,
                        final Class... args) {
    final String[] argClasses =
      Stream.of(args)
      .map(x -> Classes.simpleName(x))
      .toArray(String[]::new);
    return unsupportedOperation(
      receiver.getSimpleName(),method,argClasses); }


  public static final UnsupportedOperationException
  unsupportedOperation (final Object receiver,
                        final String method,
                        final Object... args) {
    final Class[] argClasses =
      Stream.of(args)
      .map(x -> Classes.getClass(x)) // null safe
      .toArray(Class[]::new);
    return unsupportedOperation(
      receiver.getClass(),method,argClasses); }

  @SuppressWarnings("unused")
  public static final UnsupportedOperationException
  unsupportedOperation (final Object receiver,
                        final String method,
                        final double arg) {
    return unsupportedOperation(
      receiver.getClass(),method,Double.TYPE); }

  @SuppressWarnings("unused")
  public static final UnsupportedOperationException
  unsupportedOperation (final Object receiver,
                        final String method,
                        final double z0,
                        final double z1) {
    return unsupportedOperation(
      receiver.getClass(),method,Double.TYPE,Double.TYPE); }

  //--------------------------------------------------------------
  // disable constructor
  //--------------------------------------------------------------

  private Exceptions () {
    throw new UnsupportedOperationException(
      "can't instantiate " + getClass()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
