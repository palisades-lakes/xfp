package xfp.java.numbers;

import java.util.List;

import xfp.java.exceptions.Exceptions;

/** arithmetic operations.
 *
 * 'Ringlike' because many number-like objects will define these
 * operations, but they won't obey the required properties
 * (eq associativity).
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-05-21
 */

@SuppressWarnings("unchecked")
public interface
Ringlike<T extends Ringlike>
extends Comparable<T>{

  // TODO: fma?
  //--------------------------------------------------------------

  public default T add (final T x) {
    throw Exceptions.unsupportedOperation(this,"add",x); }

  public default T subtract (final T x) {
    throw Exceptions.unsupportedOperation(this,"subtract",x); }

  public default T negate () {
    throw Exceptions.unsupportedOperation(this,"negate"); }

  public default T zero () {
    throw Exceptions.unsupportedOperation(this,"zero"); }

  public default T abs () {
    throw Exceptions.unsupportedOperation(this,"abs"); }

  public default T absDiff (final T x) { return (T) subtract(x).abs(); }

  //--------------------------------------------------------------

  public default T multiply (final T x) {
    throw Exceptions.unsupportedOperation(this,"multiply",x); }

  public default T divide (final T x) {
    throw Exceptions.unsupportedOperation(this,"divide",x); }

  public default T invert () {
    throw Exceptions.unsupportedOperation(this,"invert"); }

  public default T one () {
    throw Exceptions.unsupportedOperation(this,"one"); }

  public default List<T> divideAndRemainder (final T x) {
    final T d = divide(x);
    final T r = subtract((T) x.multiply(d));
    return List.of(d,r); }

  public default T remainder (final T x) {
    final T d = divide(x);
    final T r = subtract((T) x.multiply(d));
    return r; }

  public default T gcd (final T x) {
    throw Exceptions.unsupportedOperation(this,"gcd",x); }

  //--------------------------------------------------------------

  public default String toString (final int radix) {
    throw Exceptions.unsupportedOperation(this,"toString",radix); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

