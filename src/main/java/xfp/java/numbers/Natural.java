package xfp.java.numbers;

import static xfp.java.numbers.Numbers.loWord;

import java.util.List;

import xfp.java.exceptions.Exceptions;

/** An interface for nonnegative integers represented as
 * a sequence of <code>int</code> words, treated as unsigned.
 *
 * The value of the natural number is
 * <code>sum<sub>i=startWord()</sub><sup>i&lt;endWord</sup>
 *  uword(i) * 2<sup>32*i</sup></code>.
 *
 * TODO: utilities class to hide private stuff?
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-06
 */

@SuppressWarnings("unchecked")
public interface Natural
extends Uints<Natural>, Ringlike<Natural> {

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
    return shiftUp(upShift).compareTo(u); }

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
  // additive group
  //--------------------------------------------------------------
  // implementations usually return a pre-allocated constant

  @Override
  public default Natural zero () { return empty(); }

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
    Natural t = recyclable(this);
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
      t = t.setWord(i,(int) sum); }
    if (0L!=carry) { t = t.setWord(i,(int) carry); }
    return t.immutable(); }

  public default Natural add (final Natural u,
                              final int shift) {
    assert 0<=shift;
    if (isZero()) { return u.shiftUp(shift); }
    if (u.isZero()) { return this; }
    if (0==shift) { return add(u); }
    // TODO: reduce to single builder op?
    // TODO: shiftUp mutable version and add this to that
    return add(u.shiftUp(shift)); }

  public default Natural add (final long u) {
    assert 0L<=u;
    if (0L==u) { return immutable(); }
    if (isZero()) { return from(u).immutable(); }
    Uints v = recyclable(this);
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
    return add(from(u,upShift)).immutable(); }

  //--------------------------------------------------------------

  @Override
  public default Natural subtract (final Natural u) {
    // TODO: fast correct check of u<=this
    assert 0<=compareTo(u);
    if (u.isZero()) { return this; }
    assert ! isZero();
    Uints v = recyclable(this);
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
    Uints v = recyclable(this);
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
    return subtract(from(u,upShift)); }

  //--------------------------------------------------------------

  public default Natural subtractFrom (final long u) {
    assert 0L<=u;
    assert compareTo(u)<=0;
    return from(u).subtract(this); }

  public default Natural subtractFrom (final long u,
                                       final int upShift) {
    assert 0L<=u;
    assert 0<=upShift;
    assert compareTo(u,upShift)<=0;
    return from(u,upShift).subtract(this); }

  //--------------------------------------------------------------

  @Override
  public default Natural absDiff (final Natural u) {
    final int c = compareTo(u);
    if (c==0) { return zero(); }
    if (c<0) { return u.subtract(this); }
    return subtract(u); }

  //--------------------------------------------------------------
  // multiplicative monoid
  //--------------------------------------------------------------

  @Override
  public default Natural one () {
    throw Exceptions.unsupportedOperation(this,"one"); }

  @Override
  public default boolean isOne () {
    if (1!=word(0)) { return false; }
    for (int i=Math.max(1,startWord());i<endWord();i++) {
      if (0!=word(i)) { return false; } }
    return true; }

  //--------------------------------------------------------------

  @Override
  public default Natural square () {
    throw Exceptions.unsupportedOperation(this,"square"); }

  //--------------------------------------------------------------
  // multiply
  //--------------------------------------------------------------

  static final int MULTIPLY_SQUARE_THRESHOLD = 20;
  static final int KARATSUBA_THRESHOLD = 80;
  static final int TOOM_COOK_THRESHOLD = 240;

  //--------------------------------------------------------------

  public default Natural multiplySimple (final Natural u) {
    //System.out.println("multiplySimple");
    final int n0 = endWord();
    final int n1 = u.endWord();
    Natural v = recyclable(n0+n1+1);
    long carry = 0L;
    for (int i0=0;i0<n0;i0++) {
      carry = 0L;
      for (int i1=0;i1<n1;i1++) {
        final int i2 = i0+i1;
        final long product =
          (u.uword(i1)*uword(i0)) + v.uword(i2) + carry;
        v = v.setWord(i2, (int) product);
        carry = (product>>>32); }
      final int i2 = i0+n1;
      v = v.setWord(i2, (int) carry); }
    return v.immutable(); }

  //--------------------------------------------------------------

  public default Natural multiplyKaratsuba (final Natural u) {
    //System.out.println("multiplyKaratsuba");
    final int n0 = endWord();
    final int n1 = u.endWord();
    final int half = (Math.max(n0,n1) + 1) / 2;
    final Natural xl = words(0,half);
    final Natural xh = words(half,endWord());
    final Natural yl = u.words(0,half);
    final Natural yh = u.words(half,u.endWord());
    final Natural p1 = xh.multiply(yh);
    final Natural p2 = xl.multiply(yl);
    final Natural p3 = xh.add(xl).multiply(yh.add(yl));
    final int h32 = half*32;
    final Natural p4 = p1.shiftUp(h32);
    final Natural p5 =
      p4.add(p3.subtract(p1).subtract(p2)).shiftUp(h32);
    return p5.add(p2); }

  //--------------------------------------------------------------

  public default Natural exactDivideBy3 () {
    final int n = endWord();
    Natural t = recyclable(n);
    long borrow = 0L;
    for (int i=0;i<n;i++) {
      final long x = uword(i);
      final long w = x-borrow;
      if (x<borrow) { borrow = 1L; }
      else { borrow = 0L; }
      // 0xAAAAAAAB is the modular inverse of 3 (mod 2^32). Thus,
      // the effect of this is to divide by 3 (mod 2^32).
      // This is much faster than division on most architectures.
      final long q = loWord(w*0xAAAAAAABL);
      t= t.setWord(i,(int) q);
      // Check the borrow. 
      if (q>=0x55555556L) {
        borrow++;
        if (q>=0xAAAAAAABL) { borrow++; } } }
    return t.immutable(); }

  public default Natural getToomSlice (final int lowerSize,
                                       final int upperSize,
                                       final int slice,
                                       final int fullsize) {
    final int n = endWord();
    final int offset = fullsize-n;
    int start;
    final int end;
    if (0==slice) {
      start = 0-offset;
      end = upperSize-1-offset; }
    else {
      start = upperSize+((slice-1)*lowerSize)-offset;
      end = start+lowerSize-1; }
    if (start < 0) { start = 0; }
    if (end < 0) { return zero(); }
    final int sliceSize = (end-start) + 1;
    if (sliceSize<=0) { return zero(); }
    // While performing Toom-Cook, all slices are positive and
    // the sign is adjusted when the final number is composed.
    if ((0==start) && (sliceSize>=n)) { return this; }
    final int i1 = n-start;
    final int i0 = i1-sliceSize;
    return words(i0,i1); }
  //    throw Exceptions.unsupportedOperation(this,"getToomSlice",
  //      lowerSize,upperSize,slice,fullsize); }

  public default Natural multiplyToomCook3 (final Natural u) {
    //System.out.println("multiplyToomCook3");
    final int n0 = endWord();
    final int n1 = u.endWord();
    final int largest = Math.max(n0,n1);
    // words in the lower-order slices.
    final int k = (largest+2)/3; // ceil(largest/3)
    // words the highest-order slice.
    final int r = largest - (2 * k);

    // Obtain slices of the numbers. a2 and b2 are the most
    // significant bits of the numbers a and b, and a0 and b0 the
    // least significant.
    final Natural a2 = getToomSlice(k,r,0,largest);
    final Natural a1 = getToomSlice(k,r,1,largest);
    final Natural a0 = getToomSlice(k,r,2,largest);
    final Natural b2 = u.getToomSlice(k,r,0,largest);
    final Natural b1 = u.getToomSlice(k,r,1,largest);
    final Natural b0 = u.getToomSlice(k,r,2,largest);
    final Natural v0 = a0.multiply(b0);
    Natural da1 = a2.add(a0);
    Natural db1 = b2.add(b0);

    // might be negative
    final Natural da1_a1;
    final int ca = da1.compareTo(a1);
    if (0 < ca) { da1_a1 = da1.subtract(a1); }
    else { da1_a1 = a1.subtract(da1); }
    // might be negative
    final Natural db1_b1;
    final int cb = db1.compareTo(b1);
    if (0 < cb) { db1_b1 = db1.subtract(b1); }
    else { db1_b1 = b1.subtract(db1); }
    final int cv = ca * cb;
    final Natural vm1 = da1_a1.multiply(db1_b1);

    da1 = da1.add(a1);
    db1 = db1.add(b1);
    final Natural v1 = da1.multiply(db1);
    final Natural v2 =
      da1.add(a2).shiftUp(1).subtract(a0)
      .multiply(
        db1.add(b2).shiftUp(1).subtract(b0));

    final Natural vinf = a2.multiply(b2);

    // The algorithm requires two divisions by 2 and one by 3.
    // All divisions are known to be exact, that is, they do not
    // produce remainders, and all results are positive. The
    // divisions by 2 are implemented as right shifts which are
    // relatively efficient, leaving only an exact division by 3,
    // which is done by a specialized linear-time algorithm.
    Natural t2;
    // handle missing sign of vm1
    if (0 < cv) { t2 = v2.subtract(vm1).exactDivideBy3(); }
    else { t2 = v2.add(vm1).exactDivideBy3();}

    Natural tm1;
    // handle missing sign of vm1
    if (0 < cv) { tm1 = v1.subtract(vm1); }
    else { tm1 = v1.add(vm1); }
    tm1 = tm1.shiftDown(1);

    Natural t1 = v1.subtract(v0);
    t2 = t2.subtract(t1).shiftDown(1);
    t1 = t1.subtract(tm1).subtract(vinf);
    t2 = t2.subtract(vinf.shiftUp(1));
    tm1 = tm1.subtract(t2);

    // Number of bits to shift left.
    final int ss = k * 32;

    return
      vinf.shiftUp(ss).add(t2).shiftUp(ss).add(t1)
      .shiftUp(ss).add(tm1).shiftUp(ss).add(v0); }

  //--------------------------------------------------------------

  @Override
  public default Natural multiply (final Natural u) {
    if ((isZero()) || (u.isZero())) { return zero(); }
    final int n0 = endWord();
    if (equals(u) && (n0>MULTIPLY_SQUARE_THRESHOLD)) {
      return square(); }
    if (n0==1) { return u.multiply(uword(0)); }
    final int n1 = u.endWord();
    if (n1==1) { return multiply(u.uword(0)); }
    if ((n0<KARATSUBA_THRESHOLD) || (n1<KARATSUBA_THRESHOLD)) {
      return multiplySimple(u); }
    if ((n0<TOOM_COOK_THRESHOLD) && (n1<TOOM_COOK_THRESHOLD)) {
      return multiplyKaratsuba(u); }
    return multiplyToomCook3(u); }

  //--------------------------------------------------------------

  public default Natural multiply (final long u) {
    if (0L==u) { return zero(); }
    assert 0L < u;
    final long hi = Numbers.hiWord(u);
    final long lo = Numbers.loWord(u);
    final int n0 = endWord();
    Natural t = recyclable(n0+3);
    long carry = 0;
    int i=0;
    for (;i<n0;i++) {
      final long product = (uword(i)*lo) + carry;
      t = t.setWord(i,(int) product);
      carry = (product>>>32); }
    t = t.setWord(i,(int) carry);
    if (hi != 0L) {
      carry = 0;
      i=0;
      for (;i<n0;i++) {
        final int i1 = i+1;
        final long product = (uword(i)*hi) + t.uword(i1) + carry;
        t = t.setWord(i1,(int) product);
        carry = product >>> 32; }
      t = t.setWord(i+1,(int) carry); }
    return t.immutable(); }

  public default Natural multiply (final long u,
                                   final int upShift) {
    assert 0L<=u;
    if (0L==u) { return zero(); }
    assert 0<=upShift;
    if (0==upShift) { return multiply(u); }
    return multiply(from(u,upShift)); }

  public default Natural fromProduct (final long t0,
                                      final long t1) {
    assert 0L<=t0;
    assert 0L<=t1;
    final long hi0 = Numbers.hiWord(t0);
    final long lo0 = loWord(t0);
    final long hi1 = Numbers.hiWord(t1);
    final long lo1 = loWord(t1);
    final long lolo = lo0*lo1;
    final long hilo2 = (hi0*lo1) + (hi1*lo0);
    final long hihi = hi0*hi1;
    long sum = lolo;
    final int m0 = (int) sum;
    sum = (sum>>>32) + hilo2;
    final int m1 = (int) sum;
    sum = (sum>>>32) + hihi ;
    final int m2 = (int) sum;
    final int m3 = (int) (sum>>> 32);
    final Natural u = recyclable(4);
    if (0!=m0) { u.setWord(0,m0); }
    if (0!=m1) { u.setWord(1,m1); }
    if (0!=m2) { u.setWord(2,m2); }
    if (0!=m3) { u.setWord(3,m3); }
    return u.immutable(); }

  //--------------------------------------------------------------
  // division
  //--------------------------------------------------------------

  static final int BURNIKEL_ZIEGLER_THRESHOLD = 80;
  static final int BURNIKEL_ZIEGLER_OFFSET = 40;

  default boolean
  useKnuthDivision (final Natural u) {
    final int nn = endWord();
    final int nd = u.endWord();
    return
      (nd < Natural.BURNIKEL_ZIEGLER_THRESHOLD)
      ||
      ((nn-nd) < Natural.BURNIKEL_ZIEGLER_OFFSET); }

  //--------------------------------------------------------------

  default List<Natural>
  divideAndRemainderKnuth (final Natural u) {
    return recyclable(this).divideAndRemainderKnuth(u); }

  default List<Natural>
  divideAndRemainderBurnikelZiegler (final Natural u) {
    throw Exceptions.unsupportedOperation(
      this,"divideAndRemainderBurnikelZiegler",u); }

  @Override
  public default List<Natural> divideAndRemainder (final Natural u) {
    assert (! u.isZero());
    if (useKnuthDivision(u)) {
      return divideAndRemainderKnuth(u); }
    return
      divideAndRemainderBurnikelZiegler(u); }

  public default List<Natural> divideAndRemainder (final int u) {
    assert 0<u;
    throw Exceptions.unsupportedOperation(
      this,"divideAndRemainder",u); }
 
  //--------------------------------------------------------------

  default Natural divideKnuth (final Natural u) {
    final Natural n = recyclable(this);
    final Natural d = recyclable(u);
    return n.divideKnuth(d).immutable(); }

  @Override
  public default Natural divide (final Natural u) {
    assert (! u.isZero());
    if (u.isOne()) { return this; }
    if (useKnuthDivision(u)) { return divideKnuth(u); }
    return divideAndRemainderBurnikelZiegler(u).get(0); }

  //--------------------------------------------------------------

  @Override
  public default Natural remainder (final Natural u) {
    return divideAndRemainder(u).get(1); }

  //--------------------------------------------------------------

  @Override
  public default Natural gcd (final Natural x) {
    throw Exceptions.unsupportedOperation(this,"gcd",x); }

  //--------------------------------------------------------------

  @Override
  public default Natural invert () { return one().divide(this); }

  //--------------------------------------------------------------
  // 'Number' interface
  //--------------------------------------------------------------
  // TODO: exceptions on truncation?
  
  @Override
  public default int intValue () { return word(0); }

  @Override
  public default long longValue () {
    return (uword(1)<<32) | uword(0); }

  //--------------------------------------------------------------
}

