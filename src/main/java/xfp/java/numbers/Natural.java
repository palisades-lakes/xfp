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
 * @version 2019-06-24
 */

@SuppressWarnings("unchecked")
public interface
Natural<T extends Natural> extends Ringlike<T> {

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  
  public default NaturalBuilder<T> builder () {
    throw Exceptions.unsupportedOperation(this,"builder"); }

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

  public default long uword (final int i) {
    return Numbers.unsigned(word(i)); }

  /** Inclusive lower bound on non-zero words:
   * <code>0L==uword(i)</code> for
   * <code>startWord()>i</code>.
   * Doesn't guarantee that <code>0L!=uword(i)</code> for
   * <code>startWord()<=i</code>.
   * Zero may have no words, in which case this may be -1.
   * <p>
   * TODO: should this guarantee the maximal lower bound?
   */

  int startWord ();

  /** Exclusive upper bound for non-zero words:
   * <code>0L==uword(i)</code> for
   * <code>endWord()<=i</code>.
   * Doesn't guarantee that <code>0L!=uword(i)</code> for
   * <code>endWord()>i</code>.
   * <p>
   * TODO: should this guarantee the minimal upper bound?
   */

  int endWord ();

  //--------------------------------------------------------------
  // bit ops
  //--------------------------------------------------------------

  public default int loInt () {
    // Search for lowest order nonzero int
    // TODO: not necessary if startWord promises 
    // maximal lower bound 
    int i=startWord(); // might be -1
    if (i<0) { return -1; } // no set ints
    final int n = endWord(); // might be 0
    while (i<n) {
      final int w = word(i);
      if (0!=w) { return i; }
      i++; }
    // all words are zero
    return -1; }

  public default int loBit () {
    // Search for lowest order nonzero int
    final int i=loInt(); // might be -1
    if (i<0) { return -1; } // no set bits
    return (i << 5) + Integer.numberOfTrailingZeros(word(i)); }

  //--------------------------------------------------------------

  // wouldn't need to do this if endWord()
  // promised minimal upper bound

  public default int hiInt () {
    final int end = endWord();
    //Debug.println("end=" + end);
    if (0==end) { return(0); }
    int n = end-1;
    final int start = startWord();
    //Debug.println("start=" + start);
    while ((start<=n)&&(0==word(n))) { n--; }
    return n+1; }

  public default int hiBit () {
    final int n = hiInt()-1;
    //Debug.println("n=" + n);
    //Debug.println("word(" + n + ")=" + Integer.toHexString(word(n)));
    //Debug.println("bitlength(" + Integer.toHexString(word(n)) 
    //+ ")=" + Numbers.bitLength(word(n)));
    return (n<<5) + Numbers.bitLength(word(n)); }

  //--------------------------------------------------------------

  public default boolean testBit (final int n) {
    assert 0<=n;
    final int w = word(n>>>5);
    final int b = (1 << (n&0x1F));
    return 0!=(w&b); }

  //--------------------------------------------------------------
  /** get the least significant int words of (m >>> shift) */

  public default int getShiftedInt (final int downShift) {
    assert 0<=downShift;
    final int iShift = (downShift>>>5);
    if (endWord()<=iShift) { return 0; }
    final int rShift = (downShift & 0x1f);
    if (0==rShift) { return word(iShift); }
    final int r2 = 32-rShift;
    // TODO: optimize using startWord and endWord.
    final long lo = (uword(iShift) >>> rShift);
    final long hi = (uword(iShift+1) << r2);
    return (int) (hi | lo); }

  /** get the least significant two int words of (this >>> shift) 
   * as a long.
   */

  public default long getShiftedLong (final int downShift) {
    assert 0<=downShift;
    final int iShift = (downShift>>>5);
    if (endWord()<=iShift) { return 0L; }
    final int rShift = (downShift & 0x1f);
    if (0==rShift) { 
      return ((uword(iShift+1)<<32) | uword(iShift)); }
    // TODO: optimize using startWord and endWord.
    final int r2 = 32-rShift;
    final long lo0 = (uword(iShift)>>>rShift);
    final long u1 = uword(iShift+1);
    final long lo1 = (u1<<r2);
    final long lo = lo1 | lo0;
    final long hi0 = (u1>>>rShift);
    final long hi1 = (uword(iShift+2)<<r2);
    final long hi = hi1 | hi0;
    return (hi << 32) | lo; }

  /** get the least significant two int words of thios as
   * a long.
   */

  public default long getLong () {
    return (uword(1)<<32) | uword(0); }

  //--------------------------------------------------------------
  // TODO: need (builder?) api for constructing new Naturals

  public default T setBit (final int n) {
    assert 0<=n;
    return (T) builder().set((T) this).setBit(n).build(); }

  public default T clearBit (final int n) {
    assert 0<=n;
    return (T) builder().set((T) this).clearBit(n).build(); }

  public default T flipBit (final int n) {
    assert 0<=n;
    return (T) builder().set((T) this).flipBit(n).build(); }

  //--------------------------------------------------------------

  public default T shiftDown (final int shift) {
    assert 0<=shift;
    if (isZero()) { return (T) this; }
    final int iShift = (shift>>>5);
    // all non-zero bits shifted past zero
    if (iShift >= endWord()) { return zero(); }
    return 
      (T) builder().set((T) this).shiftDown(shift).build(); }
 
  public default T shiftUp (final int bitShift) {
    assert 0<=bitShift;
    if (isZero()) { return (T) this; }
    return 
      (T) builder().set((T) this).shiftUp(bitShift).build(); }

  //--------------------------------------------------------------
  // ordering
  //--------------------------------------------------------------

  @Override
  public default int compareTo (final T u) {
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
                                final T u) {
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

    final int iShift = (upShift >>> 5);
    final int rShift = (upShift & 0x1f);

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
  public default T zero () {
    throw Exceptions.unsupportedOperation(this,"zero"); }

  // Natural numbers are nonnegative
  
  @Override
  public default T abs () { return (T) this; }

  //--------------------------------------------------------------

  @Override
  public default T add (final T u) {
    if (endWord()<u.endWord()) { return (T) u.add(this); }
    if (isZero()) { return u; }
    if (u.isZero()) { return (T) this; }
    return 
      (T) builder().set((T) this).increment(u).build(); }

//  @Override
//  public default T add (final T u) {
//    throw Exceptions.unsupportedOperation(
//      this,"add",u); }
  
  public default T add (final T u,
                        final int bitShift) {
    assert 0<=bitShift;
    throw Exceptions.unsupportedOperation(
      this,"add",u,Integer.valueOf(bitShift)); }

  public default T add (final long u) {
    throw Exceptions.unsupportedOperation(this,"add",u); }

  public default T add (final long u,
                        final int upShift) {
    assert 0L<=u;
    assert 0<=upShift;
    throw Exceptions.unsupportedOperation(
      this,"add",u,upShift); }

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
                             final int upShift) {
    assert 0L<=u;
    assert 0<=upShift;
    assert compareTo(u,upShift)>=0;
    throw Exceptions.unsupportedOperation(
      this,"subtract",u,upShift); }

  //--------------------------------------------------------------

  public default T subtractFrom (final long u) {
    assert 0L<=u;
    assert compareTo(u)<=0;
    throw Exceptions.unsupportedOperation(
      this,"subtractFrom",u); }

  public default T subtractFrom (final long u,
                                 final int upShift) {
    assert 0L<=u;
    assert 0<=upShift;
    assert compareTo(u,upShift)<=0;
    throw Exceptions.unsupportedOperation(
      this,"subtractFrom",u,upShift); }

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
                             final int upShift) {
    assert 0L<=x;
    assert 0<=upShift;
    throw Exceptions.unsupportedOperation(
      this,"multiply",x,upShift); }

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

  public default String toHexString () {
    final StringBuilder b = new StringBuilder("");
    final int n = endWord();
    if (0 == n) { b.append('0'); }
    else {
      b.append("[");
      b.append(String.format("%08x",Long.valueOf(uword(0))));
      for (int i=1;i<n;i++) {
        b.append(" ");
        b.append(
          String.format("%08x",Long.valueOf(uword(i)))); } 
      b.append("]"); }
    return b.toString(); }

  //--------------------------------------------------------------
}

