package xfp.java.prng;

import xfp.java.exceptions.Exceptions;

/** Generators of primitives or Objects as zero-arity 'functions'
 * that return different values on each call.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-01-29
 */

@SuppressWarnings("unchecked")
public interface Generator {

  // default methods throw UnsupportetOperationException.

  public default Object next () {
    throw Exceptions.unsupportedOperation(this,"next"); }

  public default boolean nextBoolean () {
    throw Exceptions.unsupportedOperation(this,"nextBoolean"); }

  public default byte nextByte () {
    throw Exceptions.unsupportedOperation(this,"nextByte"); }

  public default char nextChar () {
    throw Exceptions.unsupportedOperation(this,"nextChar"); }

  public default short nextShort () {
    throw Exceptions.unsupportedOperation(this,"nextShort"); }

  public default int nextInt () {
    throw Exceptions.unsupportedOperation(this,"nextInt"); }

  public default long nextLong () {
    throw Exceptions.unsupportedOperation(this,"nextLong"); }

  public default float nextFloat () {
    throw Exceptions.unsupportedOperation(this,"nextFloat"); }

  public default double nextDouble () {
    throw Exceptions.unsupportedOperation(this,"nextDouble"); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

