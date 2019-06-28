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
 * @version 2019-06-28
 */

@SuppressWarnings("unchecked")
public interface Ringlike<T extends Ringlike> 
extends Comparable<Ringlike> {

  //--------------------------------------------------------------
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

  public default boolean isZero () {
    throw Exceptions.unsupportedOperation(this,"isZero"); }

  public default T abs () {
    throw Exceptions.unsupportedOperation(this,"abs"); }

  public default T absDiff (final T x) {
    return (T) subtract(x).abs(); }

  //--------------------------------------------------------------

  public default T square () {
    throw Exceptions.unsupportedOperation(this,"square"); }

  public default T multiply (final T x) {
    throw Exceptions.unsupportedOperation(this,"multiply",x); }

  public default T divide (final T x) {
    throw Exceptions.unsupportedOperation(this,"divide",x); }

  public default T invert () {
    throw Exceptions.unsupportedOperation(this,"invert"); }

  public default T one () {
    throw Exceptions.unsupportedOperation(this,"one"); }

  public default boolean isOne() {
    throw Exceptions.unsupportedOperation(this,"isOne"); }

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
  // 'Number' interface
  //--------------------------------------------------------------

  public default int intValue () { 
  throw Exceptions.unsupportedOperation(this,"intValue"); }

  public default long longValue () { 
  throw Exceptions.unsupportedOperation(this,"longValue"); }

  public default float floatValue () { 
  throw Exceptions.unsupportedOperation(this,"floatValue"); }

  public default double doubleValue () { 
  throw Exceptions.unsupportedOperation(this,"doubleValue"); }

  //--------------------------------------------------------------

  public default T min (final T that) {
    return (compareTo(that) < 0 ? (T) this : that); }

  public default T max (final T that) {
    return (compareTo(that) > 0 ? (T) this : that); }

  //--------------------------------------------------------------

  public default String toString (final int radix) {
    throw Exceptions.unsupportedOperation(this,"toString",radix); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

