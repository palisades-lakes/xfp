package xfp.java.numbers;

import static xfp.java.numbers.Numbers.loWord;

/** Multiplication of natural numbers.
 * 
 * Non-instantiable.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-17
 */

@SuppressWarnings("unchecked")
public final class NaturalMultiply {

  //--------------------------------------------------------------
  // square
  //--------------------------------------------------------------

  private static final int KARATSUBA_SQUARE_THRESHOLD = 128;
  private static final int TOOM_COOK_SQUARE_THRESHOLD = 216;

  //--------------------------------------------------------------
  // TODO: about twice the ops necessary, compared to using
  // symmetry to optimize.

  private static final Natural squareSimple (final Natural u) {
    //    throw Exceptions.unsupportedOperation(this,"square"); }
    final int n = u.endWord();
    Natural v = u.recyclable(2*n+1);
    long carry = 0L;
    for (int i0=0;i0<n;i0++) {
      carry = 0L;
      final long u0 = u.uword(i0);
      for (int i1=0;i1<n;i1++) {
        final int i2 = i0+i1;
        // TODO: can this overflow? yes! 
        // why does it seem to work anyway?
        final long prod = (u.uword(i1)*u0) + v.uword(i2) + carry;
        v = v.setWord(i2, (int) prod);
        carry = (prod>>>32); }
      final int i2 = i0+n;
      v = v.setWord(i2, (int) carry); }
    return v.immutable(); }

  //--------------------------------------------------------------

  private static final Natural squareKaratsuba (final Natural u) {
    final int n = u.endWord();
    final int half = (n+1)/2;
    final Natural xl = u.words(0,half).immutable();
    final Natural xh = u.words(half,n).immutable();
    final Natural xhs = xh.square().immutable();
    final Natural xls = xl.square().immutable();
    // (xh^2<<64) + (((xl+xh)^2-(xh^2+xl^2))<<32) + xl^2
    final int h32 = half*32;
    return 
      xhs.shiftUp(h32)
      .add(
        xl.add(xh).square().subtract(xhs.add(xls))).shiftUp(h32)
      .add(xls); }

  //--------------------------------------------------------------

  private static final Natural squareToomCook3 (final Natural u) {
    //    final int[] z = squareToomCook3(words());
    //    return unsafe(stripLeadingZeros(z)); }
    final int n = u.endWord();
    // k is the size (in ints) of the lower-order slices.
    final int k = (n+2)/3;   // Equal to ceil(largest/3)
    // r is the size (in ints) of the highest-order slice.
    final int r = n-(2*k);
    // Obtain slices of the numbers. a2 is the most significant
    // bits of the number, and a0 the least significant.
    final Natural a2 = getToomSlice(u,k,r,0,n);
    final Natural a1 = getToomSlice(u,k,r,1,n);
    final Natural a0 = getToomSlice(u,k,r,2,n);
    final Natural v0 = a0.square();
    final Natural da0 = a2.add(a0);
    // subtract here causes errors due to negative answer
    final Natural vm1 = da0.absDiff(a1).square();
    final Natural da1 = da0.add(a1);
    final Natural v1 = da1.square();
    final Natural vinf = a2.square();
    final Natural v2 = da1.add(a2).shiftUp(1).subtract(a0).square();

    // The algorithm requires two divisions by 2 and one by 3.
    // All divisions are known to be exact, that is, they do not
    // produce remainders, and all results are positive. The 
    // divisions by 2 are implemented as right shifts which are 
    // relatively efficient, leaving only a division by 3.
    // The division by 3 is done by an optimized algorithm for
    // this case.
    Natural t2 = exactDivideBy3(v2.subtract(vm1));
    Natural tm1 = v1.subtract(vm1).shiftDown(1);
    Natural t1 = v1.subtract(v0);
    t2 = t2.subtract(t1).shiftDown(1);
    t1 = t1.subtract(tm1).subtract(vinf);
    t2 = t2.subtract(vinf.shiftUp(1));
    tm1 = tm1.subtract(t2);
    final int k32 = k*32;
    return 
      vinf.shiftUp(k32).add(t2).shiftUp(k32)
      .add(t1).shiftUp(k32)
      .add(tm1)
      .shiftUp(k32)
      .add(v0); }

  //--------------------------------------------------------------

  public static final Natural square (final Natural u) {
    if (u.isZero()) { return u.zero(); }
    if (u.isOne()) { return u; }
    final int n = u.endWord();
    if (n < NaturalMultiply.KARATSUBA_SQUARE_THRESHOLD) { 
      return squareSimple(u); }
    if (n < TOOM_COOK_SQUARE_THRESHOLD) {
      return squareKaratsuba(u); }
    // For a discussion of overflow detection see multiply()
    return squareToomCook3(u); }

  //--------------------------------------------------------------

  private static final int MULTIPLY_SQUARE_THRESHOLD = 20;
  private static final int KARATSUBA_THRESHOLD = 80;
  private static final int TOOM_COOK_THRESHOLD = 240;

  //--------------------------------------------------------------

  private static final Natural multiplySimple (final Natural u,
                                               final Natural v) {
    final int n0 = u.endWord();
    final int n1 = v.endWord();
    Natural w = u.recyclable(n0+n1+1);
    long carry = 0L;
    for (int i0=0;i0<n0;i0++) {
      carry = 0L;
      for (int i1=0;i1<n1;i1++) {
        final int i2 = i0+i1;
        final long product =
          (v.uword(i1)*u.uword(i0)) + w.uword(i2) + carry;
        w = w.setWord(i2, (int) product);
        carry = (product>>>32); }
      final int i2 = i0+n1;
      w = w.setWord(i2, (int) carry); }
    return w.immutable(); }

  //--------------------------------------------------------------

  private static final Natural multiplyKaratsuba (final Natural u,
                                                  final Natural v) {
    final int n0 = u.endWord();
    final int n1 = v.endWord();
    final int half = (Math.max(n0,n1) + 1) / 2;
    final Natural xl = u.words(0,half).immutable();
    final Natural xh = u.words(half,u.endWord()).immutable();
    final Natural yl = v.words(0,half).immutable();
    final Natural yh = v.words(half,v.endWord()).immutable();
    final Natural p1 = xh.multiply(yh);
    final Natural p2 = xl.multiply(yl);
    final Natural p3 = xh.add(xl).multiply(yh.add(yl));
    final int h32 = half*32;
    final Natural p4 = p1.shiftUp(h32);
    final Natural p5 =
      p4.add(p3.subtract(p1).subtract(p2)).shiftUp(h32);
    return p5.add(p2); }

  //--------------------------------------------------------------

  private static final Natural exactDivideBy3 (final Natural u) {
    final int n = u.endWord();
    Natural t = u.recyclable(n);
    long borrow = 0L;
    for (int i=0;i<n;i++) {
      final long x = u.uword(i);
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

  //--------------------------------------------------------------

  private static final Natural getToomSlice (final Natural u,
                                             final int lowerSize,
                                             final int upperSize,
                                             final int slice,
                                             final int fullsize) {
    final int n = u.endWord();
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
    if (end < 0) { return u.zero(); }
    final int sliceSize = (end-start) + 1;
    if (sliceSize<=0) { return u.zero(); }
    // While performing Toom-Cook, all slices are positive and
    // the sign is adjusted when the final number is composed.
    if ((0==start) && (sliceSize>=n)) { return u; }
    final int i1 = n-start;
    final int i0 = i1-sliceSize;
    return u.words(i0,i1).immutable(); }

  //--------------------------------------------------------------

  private static final Natural multiplyToomCook3 (final Natural u,
                                                  final Natural v) {
    //System.out.println("multiplyToomCook3");
    final int n0 = u.endWord();
    final int n1 = v.endWord();
    final int largest = Math.max(n0,n1);
    // words in the lower-order slices.
    final int k = (largest+2)/3; // ceil(largest/3)
    // words the highest-order slice.
    final int r = largest - (2 * k);

    // Obtain slices of the numbers. a2 and b2 are the most
    // significant bits of the numbers a and b, and a0 and b0 the
    // least significant.
    final Natural a2 = getToomSlice(u,k,r,0,largest);
    final Natural a1 = getToomSlice(u,k,r,1,largest);
    final Natural a0 = getToomSlice(u,k,r,2,largest);
    final Natural b2 = getToomSlice(v,k,r,0,largest);
    final Natural b1 = getToomSlice(v,k,r,1,largest);
    final Natural b0 = getToomSlice(v,k,r,2,largest);
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
    if (0 < cv) { t2 = exactDivideBy3(v2.subtract(vm1)); }
    else { t2 = exactDivideBy3(v2.add(vm1));}

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

  public static final Natural multiply (final Natural u,
                                        final Natural v) {
    if ((u.isZero()) || (v.isZero())) { return u.zero(); }
    final int n0 = u.endWord();
    if (u.equals(v) && (n0>MULTIPLY_SQUARE_THRESHOLD)) {
      return square(u); }
    if (n0==1) { return multiply(v,u.uword(0)); }
    final int n1 = v.endWord();
    if (n1==1) { return multiply(u,v.uword(0)); }
    if ((n0<KARATSUBA_THRESHOLD) || (n1<KARATSUBA_THRESHOLD)) {
      return multiplySimple(u,v); }
    if ((n0<TOOM_COOK_THRESHOLD) && (n1<TOOM_COOK_THRESHOLD)) {
      return multiplyKaratsuba(u,v); }
    return multiplyToomCook3(u,v); }

  public static final Natural multiply (final Natural u,
                                        final long v) {
    if (0L==v) { return u.zero(); }
    assert 0L < v;
    final long hi = Numbers.hiWord(v);
    final long lo = Numbers.loWord(v);
    final int n0 = u.endWord();
    Natural t = u.recyclable(n0+3);
    long carry = 0;
    int i=0;
    for (;i<n0;i++) {
      final long product = (u.uword(i)*lo) + carry;
      t = t.setWord(i,(int) product);
      carry = (product>>>32); }
    t = t.setWord(i,(int) carry);
    if (hi != 0L) {
      carry = 0;
      i=0;
      for (;i<n0;i++) {
        final int i1 = i+1;
        final long product = (u.uword(i)*hi) + t.uword(i1) + carry;
        t = t.setWord(i1,(int) product);
        carry = product >>> 32; }
      t = t.setWord(i+1,(int) carry); }
    return t.immutable(); }

  //--------------------------------------------------------------
  // disable constructor
  //--------------------------------------------------------------

  private NaturalMultiply () {
    throw new
    UnsupportedOperationException(
      "can't instantiate " + getClass().getCanonicalName()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
