package xfp.java.numbers;

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
 * @version 2019-07-02
 */

@SuppressWarnings("unchecked")
public interface Natural
extends Uints, Ringlike<Natural>, Transience<Natural> {

  //--------------------------------------------------------------
  // ordering
  //--------------------------------------------------------------

  @Override
  public default int compareTo (final Natural u) {
    // TODO: should really compare hiBits
    final int b0 = hiBit();
    final int b1 = u.hiBit();
    if (b0<b1) { return -1; }
    if (b0>b1) { return 1; }
    final int end = Math.max(endWord(),u.endWord()) - 1;
    final int start = Math.min(startWord(),u.startWord());
    for (int i=end;i>=start;i--) {
      final long u0i = uword(i);
      final long u1i = u.uword(i);
      if (u0i<u1i) { return -1; }
      if (u0i>u1i) { return 1; } }
    return 0; }

  public default int compareTo (final int upShift,
                                final Natural u) {
    return ((Natural) shiftUp(upShift)).compareTo(u); }

  public default int compareTo (final long u) {
    assert 0L<=u;
    if (2<hiInt()) { return 1; }
    final long hi0 = uword(1);
    final long hi1 = Numbers.hiWord(u);
    if (hi0<hi1) { return -1; }
    if (hi0>hi1) { return 1; }
    final long lo0 = uword(0);
    final long lo1 = Numbers.loWord(u);
    if (lo0<lo1) { return -1; }
    if (lo0>lo1) { return 1; }
    return 0; }

  public default int compareTo (final long u,
                                final int upShift) {
    assert 0L<=u;
    assert 0<=upShift : "upShift=" + upShift;
    if (0==upShift) { return compareTo(u); }
    if (0L==u) {
      if (isZero()) { return 0; }
      return 1; }

    final int n0 = hiBit();
    final int n1 = Numbers.hiBit(u) + upShift;
    if (n0<n1) { return -1; }
    if (n0>n1) { return 1; }

    final int iShift = (upShift>> 5);
    final int rShift = (upShift&0x1f);

    // compare non-zero words from u<<upShift
    if (0==rShift) {
      final long hi0 = uword(iShift+1);
      final long hi1 = Numbers.hiWord(u);
      if (hi0<hi1) { return -1; }
      if (hi0>hi1) { return 1; }
      final long lo0 = uword(iShift);
      final long lo1 = Numbers.loWord(u);
      if (lo0<lo1) { return -1; }
      if (lo0>lo1) { return 1; } }
    else {
      // most significant word in u << upShift
      final long hi0 = uword(iShift+2);
      final long hi1 = (u>>>(64-rShift));
      if (hi0<hi1) { return -1; }
      if (hi0>hi1) { return 1; }

      final long us = (u << rShift);
      final long mid0 = uword(iShift+1);
      final long mid1 = Numbers.hiWord(us);
      if (mid0<mid1) { return -1; }
      if (mid0>mid1) { return 1; }

      final long lo0 = uword(iShift);
      final long lo1 = Numbers.loWord(us);
      if (lo0<lo1) { return -1; }
      if (lo0>lo1) { return 1; } }

    // check this for any non-zero words in zeros of u<<upShift
    for (int i=iShift-1;i>=startWord();i--) {
      if (0!=uword(i)) { return 1; } }

    return 0; }

  //--------------------------------------------------------------
  // arithmetic
  //--------------------------------------------------------------
  // implementations usually return a pre-allocated constant

  @Override
  public default Natural zero () {
    throw Exceptions.unsupportedOperation(this,"zero"); }

  @Override
  public default boolean isZero () {
    for (int i=0;i<endWord();i++) {
      if (0!=word(i)) { return false; } }
    return true; }

  // Natural numbers are nonnegative

  @Override
  public default Natural abs () { return this; }

  //--------------------------------------------------------------

  @Override
  public default Natural add (final Natural u) {
    Natural t = recyclable();
    if (isZero()) { return u; }
    if (u.isZero()) { return this; }
    // TODO: optimize by summing over joint range
    // and just carrying after that
    final int end = Math.max(endWord(),u.endWord());
    long sum = 0L;
    long carry = 0L;
    int i=0;
    for (;i<end;i++) {
      sum = uword(i) + u.uword(i) + carry;
      carry = (sum>>>32);
      t = (Natural) t.setWord(i,(int) sum); }
    if (0L!=carry) { t = (Natural) t.setWord(i,(int) carry); }
    return t.immutable(); }

  public default Natural add (final Natural u,
                              final int shift) {
    assert 0<=shift;
    if (isZero()) { return (Natural) u.shiftUp(shift); }
    if (u.isZero()) { return this; }
    if (0==shift) { return add(u); }
    // TODO: reduce to single builder op?
    // TODO: shiftUp mutable version and add this to that
    return add((Natural) u.shiftUp(shift)); }

  public default Natural add (final long u) {
    assert 0L<=u;
    if (0L==u) { return immutable(); }
    if (isZero()) { return ((Natural) from(u)).immutable(); }
    Uints v = recyclable();
    long sum = uword(0) + Numbers.loWord(u);
    v = v.setWord(0,(int) sum);
    long carry = (sum>>>32);
    sum = uword(1) + Numbers.hiWord(u) + carry;
    v = v.setWord(1,(int) sum);
    carry = (sum>>>32);
    int i=2;
    final int n = endWord();
    for (;(0L!=carry)&&(i<n);i++) {
      sum = uword(i) + carry;
      v = v.setWord(i,(int) sum);
      carry = (sum>>>32); }
    if (0L!=carry) { v = v.setWord(i,(int) carry); }
    return ((Natural) v).immutable(); }

  public default Natural add (final long u,
                              final int upShift) {
    assert 0<=upShift;
    if (0==upShift) { return add(u); }
    assert 0L<=u;
    if (0L==u) { return immutable(); }
    return add((Natural) from(u,upShift)).immutable(); }

  //--------------------------------------------------------------

  @Override
  public default Natural subtract (final Natural u) {
    // TODO: fast correct check of u<=this
    assert 0<=compareTo(u);
    if (u.isZero()) { return this; }
    assert ! isZero();
    Uints v = recyclable();
    long dif = 0L;
    long borrow = 0L;
    final int n = Math.max(endWord(),u.endWord());
    int i=0;
    // TODO: optimize by differencing over shared range
    // and then just borrowing
    for (;i<n;i++) {
      dif = (uword(i) - u.uword(i)) + borrow;
      borrow = (dif>>32);
      v = v.setWord(i,(int) dif); }
    assert 0L==borrow;
    return ((Natural) v).immutable(); }

  public default Natural subtract (final long u) {
    assert 0L<=u;
    assert 0<=compareTo(u);
    if (0L==u) { return this; }
    assert 0L<=u;
    if (0L==u) { return this; }
    assert ! isZero();
    final long lo = Numbers.loWord(u);
    final long hi = Numbers.hiWord(u);
    if (0L!=hi) { assert 2<=endWord(); }
    if (0L!=lo) { assert 1<=endWord(); }
    Uints v = recyclable();
    long dif = uword(0)-lo;
    v = v.setWord(0,(int) dif);
    long borrow = (dif>>32);
    dif = (uword(1)-hi)+borrow;
    v = v.setWord(1,(int) dif);
    borrow = (dif>>32);
    int i=2;
    final int n = endWord();
    for (;(0L!=borrow)&&(i<n);i++) {
      dif = uword(i)+borrow;
      v = v.setWord(i,(int) dif);
      borrow = (dif>>32); }
    assert 0L==borrow : borrow;
    return ((Natural) v).immutable(); }

  public default Natural subtract (final long u,
                                   final int upShift) {
    assert 0L<=u;
    assert 0<=upShift;
    assert compareTo(u,upShift)>=0;
    return subtract((Natural) from(u,upShift)); }

  //--------------------------------------------------------------

  public default Natural subtractFrom (final long u) {
    assert 0L<=u;
    assert compareTo(u)<=0;
    return ((Natural) from(u)).subtract(this); }

  public default Natural subtractFrom (final long u,
                                       final int upShift) {
    assert 0L<=u;
    assert 0<=upShift;
    assert compareTo(u,upShift)<=0;
    return ((Natural) from(u,upShift)).subtract(this); }

  //--------------------------------------------------------------

  @Override
  public default Natural absDiff (final Natural u) {
    final int c = compareTo(u);
    if (c==0) { return zero(); }
    if (c<0) { return u.subtract(this); }
    return subtract(u); }

  //--------------------------------------------------------------

  @Override
  public default Natural square () {
    throw Exceptions.unsupportedOperation(this,"square"); }

  //--------------------------------------------------------------

  @Override
  public default boolean isOne () {
    if (1!=word(0)) { return false; }
    for (int i=Math.max(1,startWord());i<endWord();i++) {
      if (0!=word(i)) { return false; } }
    return true; }

  @Override
  public default Natural multiply (final Natural u) {
    throw Exceptions.unsupportedOperation(this,"multiply",u); }

  public default Natural multiply (final long u) {
    assert 0L<=u;
    throw Exceptions.unsupportedOperation(this,"multiply",u); }

  public default Natural multiply (final long u,
                                   final int upShift) {
    assert 0L<=u;
    assert 0<=upShift;
    throw Exceptions.unsupportedOperation(
      this,"multiply",u,upShift); }

  //--------------------------------------------------------------

  @Override
  public default Natural divide (final Natural x) {
    throw Exceptions.unsupportedOperation(this,"divide",x); }

  @Override
  public default Natural invert () {
    throw Exceptions.unsupportedOperation(this,"invert"); }

  @Override
  public default Natural one () {
    throw Exceptions.unsupportedOperation(this,"one"); }

  @Override
  public default List<Natural> divideAndRemainder (final Natural x) {
    final Natural d = divide(x);
    final Natural r = subtract(x.multiply(d));
    return List.of(d,r); }

  @Override
  public default Natural remainder (final Natural x) {
    final Natural d = divide(x);
    final Natural r = subtract(x.multiply(d));
    return r; }

  @Override
  public default Natural gcd (final Natural x) {
    throw Exceptions.unsupportedOperation(this,"gcd",x); }

  //--------------------------------------------------------------
  // 'Number' interface
  //--------------------------------------------------------------
  // TODO: move to Ringlike?

  //  @Override
  //  public default byte[] toByteArray () {
  //    throw Exceptions.unsupportedOperation(this,"toByteArray"); }

  //  @Override
  //  public default BigInteger bigIntegerValue () {
  //    return new BigInteger(toByteArray()); }

  @Override
  public default int intValue () { return word(0); }

  @Override
  public default long longValue () {
    return (uword(1)<<32) | uword(0); }

  //--------------------------------------------------------------
}

