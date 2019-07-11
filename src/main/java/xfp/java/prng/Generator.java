package xfp.java.prng;

import xfp.java.exceptions.Exceptions;

/** Generators of primitives or Objects as zero-arity 'functions'
 * that return different values on each call.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-01
 */

@SuppressWarnings("unchecked")
public interface Generator {

  // default methods throw UnsupportetOperationException.

  default String name () {
    throw Exceptions.unsupportedOperation(this,"name"); }

  default Object next () {
    throw Exceptions.unsupportedOperation(this,"next"); }

  default boolean nextBoolean () {
    throw Exceptions.unsupportedOperation(this,"nextBoolean"); }

  default byte nextByte () {
    throw Exceptions.unsupportedOperation(this,"nextByte"); }

  default char nextChar () {
    throw Exceptions.unsupportedOperation(this,"nextChar"); }

  default short nextShort () {
    throw Exceptions.unsupportedOperation(this,"nextShort"); }

  default int nextInt () {
    throw Exceptions.unsupportedOperation(this,"nextInt"); }

  default long nextLong () {
    throw Exceptions.unsupportedOperation(this,"nextLong"); }

  default float nextFloat () {
    throw Exceptions.unsupportedOperation(this,"nextFloat"); }

  default double nextDouble () {
    throw Exceptions.unsupportedOperation(this,"nextDouble"); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

