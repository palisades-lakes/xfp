package xfp.java.numbers;

import java.math.BigInteger;
import java.util.List;

import xfp.java.exceptions.Exceptions;

/** An interface for nonnegative integers represented as
 * a sequence of <code>int</code> words, treated as unsigned.
 *
 * The value of the natural number is
 * <code>sum<sub>i=startWord()</sub><sup>i&lt;endWord</sup>
 *  uword(i) * 2<sup>32*i</sup></code>.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-06-20
 */

@SuppressWarnings("unchecked")
public interface
Natural<T extends Natural> extends Ringlike<T> {

  //--------------------------------------------------------------
  // word ops
  //--------------------------------------------------------------
  /** The <code>i</code>th word as an <code>int</code>. <br>
   * <em>WARNING:</em> content used as unsigned.
   */

  int word (final int i);


  /** The value of the unsigned <code>i</code>th word as a
   * <code>long</code>.
   */

  long uword (final int i);

  /** Inclusive lower bound on non-zero words:
   * <code>0L==uword(i)</code> for
   * <code>startWord()>i</code>.
   * Doesn't guarantee that <code>0L!=uword(i)</code> for
   * <code>startWord()<=i</code>.
   * Zero may have no words, in which case this may be -1.
   */

  int startWord ();

  /** Exclusive upper bound for non-zero words:
   * <code>0L==uword(i)</code> for
   * <code>endWord()<=i</code>.
   * Doesn't guarantee that <code>0L!=uword(i)</code> for
   * <code>endWord()>i</code>.
   */

  int endWord ();

  //--------------------------------------------------------------
  // bit ops
  //--------------------------------------------------------------

  public default int loBit () {
    // Search for lowest order nonzero int
    int i=startWord(); // might be -1
    if (i<0) { return -1; } // no set bits
    final int n = endWord(); // might be 0
    while (i<n) {
      final int w = word(i);
      if (0!=w) {
        return (i << 5) + Integer.numberOfTrailingZeros(w); }
      i++; }
    // all words are zero
    return -1; }

  public default int hiBit () {
    throw Exceptions.unsupportedOperation(
      this,"hiBit"); }

  public default T shiftRight (final int bitShift) {
    assert 0<=bitShift;
    throw Exceptions.unsupportedOperation(
      this,"shiftRight",bitShift); }

  public default T shiftLeft (final int bitShift) {
    assert 0<=bitShift;
    throw Exceptions.unsupportedOperation(
      this,"shiftLeft",bitShift); }

  public default boolean testBit (final int n) {
    assert 0<=n;
    throw Exceptions.unsupportedOperation(
      this,"testBit",n); }

  public default T setBit (final int n) {
    assert 0<=n;
    throw Exceptions.unsupportedOperation(
      this,"setBit",n); }

  public default T clearBit (final int n) {
    assert 0<=n;
    throw Exceptions.unsupportedOperation(
      this,"clearBit",n); }

  public default T flipBit (final int n) {
    assert 0<=n;
    throw Exceptions.unsupportedOperation(
      this,"flipBit",n); }

  /** get the least significant int words of (m >>> shift) */

  public default int getShiftedInt (final int leftShift) {
    assert 0<=leftShift;
    throw Exceptions.unsupportedOperation(
      this,"getShiftedInt",leftShift); }

  /** get the least significant two int words of (m >>> shift) as
   * a long.
   */

  public default long getShiftedLong (final int leftShift) {
    assert 0<=leftShift;
    throw Exceptions.unsupportedOperation(
      this,"getShiftedLong",leftShift); }

  //--------------------------------------------------------------
  // ordering
  //--------------------------------------------------------------

  public default int compareTo (final long u) {
    assert 0L<=u;
    throw Exceptions.unsupportedOperation(this,"compareTo",u); }

  public default int compareTo (final long u,
                                final int leftShift) {
    assert 0L<=u;
    assert 0<=leftShift;
    throw Exceptions.unsupportedOperation(
      this,"compareTo",u,leftShift); }

  public default int compareTo (final int leftShift,
                                final T m) {
    return shiftLeft(leftShift).compareTo(m); }

  //--------------------------------------------------------------
  // arithmetic
  //--------------------------------------------------------------

  @Override
  public default T negate () {
    throw Exceptions.unsupportedOperation(this,"negate"); }

  @Override
  public default T zero () {
    throw Exceptions.unsupportedOperation(this,"zero"); }

  @Override
  public default T abs () {
    throw Exceptions.unsupportedOperation(this,"abs"); }

  //--------------------------------------------------------------

  @Override
  public default T add (final T u) {
    throw Exceptions.unsupportedOperation(this,"add",u); }

  public default T add (final T u,
                        final int bitShift) {
    assert 0<=bitShift;
    throw Exceptions.unsupportedOperation(
      this,"add",u,Integer.valueOf(bitShift)); }

  public default T add (final long u) {
    throw Exceptions.unsupportedOperation(this,"add",u); }

  public default T add (final long u,
                        final int leftShift) {
    assert 0L<=u;
    assert 0<=leftShift;
    throw Exceptions.unsupportedOperation(
      this,"add",u,leftShift); }

  //--------------------------------------------------------------

  @Override
  public default T subtract (final T u) {
    throw Exceptions.unsupportedOperation(this,"subtract",u); }

  public default T subtract (final long u) {
    assert 0L<=u;
    assert 0<=compareTo(u);
    throw Exceptions.unsupportedOperation(
      this,"subtract",u); }

  public default T subtract (final long u,
                             final int leftShift) {
    assert 0L<=u;
    assert 0<=leftShift;
    assert compareTo(u,leftShift)>=0;
    throw Exceptions.unsupportedOperation(
      this,"subtract",u,leftShift); }

  //--------------------------------------------------------------

  public default T subtractFrom (final long u) {
    assert 0L<=u;
    assert compareTo(u)<=0;
    throw Exceptions.unsupportedOperation(
      this,"subtractFrom",u); }

  public default T subtractFrom (final long u,
                                 final int leftShift) {
    assert 0L<=u;
    assert 0<=leftShift;
    assert compareTo(u,leftShift)<=0;
    throw Exceptions.unsupportedOperation(
      this,"subtractFrom",u,leftShift); }

  //--------------------------------------------------------------

  @Override
  public default T absDiff (final T x) {
    final int c = compareTo(x);
    if (c==0) { return zero(); }
    if (c<0) { return (T) x.subtract(this); }
    return subtract(x); }

  //--------------------------------------------------------------

  @Override
  public default T square () {
    throw Exceptions.unsupportedOperation(this,"square"); }

  //--------------------------------------------------------------

  @Override
  public default T multiply (final T x) {
    throw Exceptions.unsupportedOperation(this,"multiply",x); }

  public default T multiply (final long x) {
    assert 0L<=x;
    throw Exceptions.unsupportedOperation(this,"multiply",x); }

  public default T multiply (final long x,
                             final int leftShift) {
    assert 0L<=x;
    assert 0<=leftShift;
    throw Exceptions.unsupportedOperation(
      this,"multiply",x,leftShift); }

  //--------------------------------------------------------------

  @Override
  public default T divide (final T x) {
    throw Exceptions.unsupportedOperation(this,"divide",x); }

  @Override
  public default T invert () {
    throw Exceptions.unsupportedOperation(this,"invert"); }

  @Override
  public default T one () {
    throw Exceptions.unsupportedOperation(this,"one"); }

  @Override
  public default List<T> divideAndRemainder (final T x) {
    final T d = divide(x);
    final T r = subtract((T) x.multiply(d));
    return List.of(d,r); }

  @Override
  public default T remainder (final T x) {
    final T d = divide(x);
    final T r = subtract((T) x.multiply(d));
    return r; }

  @Override
  public default T gcd (final T x) {
    throw Exceptions.unsupportedOperation(this,"gcd",x); }

  //--------------------------------------------------------------
  // conversions
  //--------------------------------------------------------------
  // TODO: move to Ringlike?

  public default byte[] toByteArray () {
    throw Exceptions.unsupportedOperation(this,"toByteArray"); }

  public default BigInteger bigIntegerValue () {
    throw Exceptions.unsupportedOperation(this,"bigIntegerValue"); }

  public default int intValue () {
    throw Exceptions.unsupportedOperation(this,"intValue"); }

  public default long longValue () {
    throw Exceptions.unsupportedOperation(this,"longValue"); }

  public default float floatValue () {
    throw Exceptions.unsupportedOperation(this,"floatValue"); }

  public default double doubleValue () {
    throw Exceptions.unsupportedOperation(this,"doubleValue"); }

  //--------------------------------------------------------------
}

