package xfp.java.numbers;

import java.math.BigInteger;

import xfp.java.exceptions.Exceptions;

/** An interface for sequences of <code>int</code> words,
 * treated as unsigned.
 * <p>
 * This interface covers both mutable and immutable sequences,
 * with methods like {@link #setWord(int,int)} returning a
 * sequence that may or may not be new. Methods are free
 * to return a new instance while invalidating the target of the
 * method (similar to the behavior of transient data structures
 * in Clojure).
 *
 * TODO: max valid range limited by int hiBit!
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-24
 */

@SuppressWarnings("unchecked")
public interface Uints<T extends Uints> 
extends Transience<T> {

  //--------------------------------------------------------------
  // word ops
  //--------------------------------------------------------------
  /** The <code>i</code>th (little endian) word as an 
   * <code>int</code>. <br>
   * <em>WARNING:</em> content used as unsigned.
   */

  int word (final int i);

  /** Return a sequence where the <code>i</code>th 
   * (little endian) word is <code>w</code>.
   * May return a new sequence, or may modify <code>this</code>.
   * Existing references to <code>this</code> may no longer be
   * valid.
   */

  default T setWord (final int i,
                     final int w) {
    throw Exceptions.unsupportedOperation(this,"setWord",i,w); }

  /** The value of the unsigned <code>i</code>th (little endian) 
   * word as a <code>long</code>.
   */

  default long uword (final int i) {
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

  default T clear () {
    throw Exceptions.unsupportedOperation(this,"clear"); }

  //--------------------------------------------------------------

  default T empty () {
    throw Exceptions.unsupportedOperation(this,"empty"); }

  //--------------------------------------------------------------
  /** Return the <code>[i0,i1)</code> words as a new 
   * <code>Uints</code> with <code>[0,i1-i0)</code> words.
   */

  default T words (final int i0,
                   final int i1) {
    assert 0<=i0;
    assert i0<i1;
    if ((0==i0) && (hiInt()<=i1)) { return copy(); }
    final int n = Math.max(0,i1-i0);
    if (0>=n) { return empty(); }
    T u = empty();
    for (int i=0;i<n;i++) { u = (T) u.setWord(i,word(i+i0)); }
    return u; }

  //--------------------------------------------------------------
  /** Return a sequence whose value is the same as <code>u</code>.
   * May be the same object as <code>this</code>, if mutable.
   * May be <code>u</code>, if immutable.
   * May be a copy of <code>u</code>.
   */

  default T set (final Uints u) {
    Uints x = clear();
    for (int i=u.startWord();i<u.hiInt();i++) {
      x = x.setWord(i,u.word(i)); }
    return (T) this; }

  /** Return a Uints whose value is the same as <code>u</code>.
   */

  default T set (final long u) {
    assert 0<=u;
    // TODO: optimize zeroing internal array?
    clear();
    final long lo = Numbers.loWord(u);
    if (0!=lo) { setWord(0,(int) lo); }
    final long hi = Numbers.hiWord(u);
    if (0!=hi) { setWord(1,(int) hi); }
    return (T) this; }

  //--------------------------------------------------------------
  // wouldn't need these if startWord()/endWord()
  // promised maximal/minimal bounds

  default int loInt () {
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

  default int hiInt () {
    final int end = endWord();
    if (0==end) { return 0; }
    int n = end-1;
    final int start = startWord();
    while ((start<n)&&(0==word(n))) { n--; }
    return n+1; }

  //--------------------------------------------------------------
  // bit ops
  //--------------------------------------------------------------

  default int loBit () {
    // Search for lowest order nonzero int
    final int i=loInt(); // might be -1
    if (i<0) { return -1; } // no set bits
    return (i << 5) + Integer.numberOfTrailingZeros(word(i)); }

  default int hiBit () {
    //Debug.println("hiBit this=" + this);
    final int n = hiInt()-1;
    if (0>n) { return 0; }
    return (n<<5) + Numbers.hiBit(word(n)); }

  //--------------------------------------------------------------

  default boolean testBit (final int n) {
    assert 0<=n;
    final int w = word(n>>>5);
    final int b = (1 << (n&0x1F));
    return 0!=(w&b); }

  //--------------------------------------------------------------
  /** get the least significant int word of (this >>> shift) */

  default int getShiftedInt (final int downShift) {
    assert 0<=downShift;
    final int iShift = (downShift>>>5);
    if (hiInt()<=iShift) { return 0; }
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

  default long getShiftedLong (final int downShift) {
    assert 0<=downShift;
    final int iShift = (downShift>>>5);
    if (hiInt()<=iShift) { return 0L; }
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

  default long getLong () {
    return (uword(1)<<32) | uword(0); }

  //--------------------------------------------------------------

  default T setBit (final int i) {
    assert 0<=i;
    final int iw = (i>>>5);
    final int w = word(iw);
    final int ib = (i&0x1F);
    return setWord(iw,(w|(1<<ib))); }

  default T clearBit (final int n) {
    assert 0<=n;
    final int iw = (n>>>5);
    final int w = word(iw);
    final int ib = (n&0x1F);
    return setWord(iw,(w&(~(1<<ib)))); }

  default T flipBit (final int n) {
    assert 0<=n;
    final int iw = (n>>>5);
    final int w = word(iw);
    final int ib = (n&0x1F);
    return setWord(iw,(w^(1<<ib))); }

  //--------------------------------------------------------------

  default T shiftDownWords (final int iShift) {
    assert 0<=iShift;
    if (0==iShift) { return (T) this; }
    final int n0 = hiInt();
    if (0==n0) { return (T) this; }
    final int n1 = n0-iShift;
    if (0>=n1) { return empty(); }
    T u = recyclable(n1);
    for (int i=0;i<n1;i++) { 
      u = (T) u.setWord(i,word(i+iShift)); }
    if (isImmutable()) { return (T) u.immutable(); }
    return u; }

  default T shiftDown (final int shift) {
    assert 0<=shift;
    if (shift==0) { return (T) this; }
    final int n0 = hiInt();
    if (0==n0) { return (T) this; }
    final int iShift = (shift>>>5);
    final int n1 = n0-iShift;
    if (0>=n1) { return empty(); }
    final int bShift = (shift & 0x1f);
    if (0==bShift) { return shiftDownWords(iShift); }
    T u = recyclable(n1);
    final int rShift = 32-bShift;
    int w0 = word(iShift);
    for (int j=0;j<n1;j++) { 
      final int w1 = word(j+iShift+1);
      final int w = ((w1<<rShift) | (w0>>>bShift));
      w0 = w1;
      u = (T) u.setWord(j,w); }
    if (isImmutable()) { return (T) u.immutable(); }
    return u; }

  //  default T shiftDown (final int shift) {
//    assert 0<=shift;
//    if (shift==0) { return (T) this; }
//    final int n0 = hiInt();
//    if (0==n0) { return (T) this; }
//    final int iShift = (shift >>> 5);
//    final int n1 = n0-iShift;
//    if (0>=n1) { return empty(); }
//    final int bShift = (shift & 0x1f);
//    if (0==bShift) { return shiftDownWords(iShift); }
//    T u = empty();
//    final int rShift = 32-bShift;
//    for (int j=0;j<n1;j++) { 
//      final int i = j+iShift;
//      final int w = ((word(i+1)<<rShift) | (word(i)>>>bShift));
//      u = (T) u.setWord(j,w); }
//    return u; }

  //--------------------------------------------------------------

  default T shiftUpWords (final int iShift) {
    assert 0<=iShift;
    if (0==iShift) { return (T) this; }
    final int n = hiInt();
    if (0==n) { return (T) this; }
    T u = empty();
    for (int i=0;i<iShift;i++) { u = (T) u.setWord(i,0); }
    for (int i=0;i<n;i++) { u = (T) u.setWord(i+iShift,word(i)); }
    return u; }

  default T shiftUp (final int shift) {
    assert 0<=shift;
    if (shift==0) { return (T) this; }
    final int n0 = hiInt();
    if (0==n0) { return (T) this; }
    //if (isZero()) { return valueOf(0L); }
    final int iShift = (shift >>> 5);
    final int bShift = (shift & 0x1f);
    if (0==bShift) { return shiftUpWords(iShift); }
    final int rShift = 32-bShift;
    final int n1 = n0+iShift;
    T u = empty();
    for (int i=0;i<iShift;i++) { u = (T) u.setWord(i,0); }
    u = (T) u.setWord(iShift,(word(0)<<bShift));
    for (int i=1;i<n0;i++) { 
      final int w = ((word(i)<<bShift) | (word(i-1)>>>rShift));
      u = (T) u.setWord(i+iShift,w); }
    u = (T) u.setWord(n1,(word(n0-1)>>>rShift));
    return u; }

  //--------------------------------------------------------------
  /** Return a Uints whose value is
   * <code>u * 2<sup>shift</sup></code>.
   * Usually the same object as <code>this</code>.
   *
   * <code>0&lt;=shift</code>
   */

  default T shiftUp (final T u,
                     final int shift) {
    // TODO: optimize as single op
    return (T) set(u).shiftUp(shift); }

  /** Return a Uints whose value is
   * <code>u * 2<sup>shift</sup></code>.
   * Usually the same object as <code>this</code>.
   *
   * <code>0&lt;=shift</code>
   */

  default T shiftUp (final long u,
                     final int shift) {
    // TODO: optimize as single op
    return (T) set(u).shiftUp(shift); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  /** Return a new Uints whose value is <code>u</code>.
   * Does not modify <code>this</code>.
   */

  default T from (final long u) {
    throw Exceptions.unsupportedOperation(
      this,"from",u); }

  /** Return a new Uints whose value is <code>u</code>,
   * interpreted as unsigned.
   * Does not modify <code>this</code>.
   */

  default T from (final int u) {
    throw Exceptions.unsupportedOperation(
      this,"from",u); }

  /** Return a new Uints whose value is
   * <code>u * 2<sup>shift</sup></code>.
   * Does not modify <code>this</code>.
   *
   * <code>0&lt;=shift</code>
   */

  default T from (final long u,
                  final int shift) {
    return (T) from(u).shiftUp(shift); }

  //--------------------------------------------------------------
  // 'Number' interface
  //--------------------------------------------------------------

  default byte[] littleEndianBytes () {
    final int hi = hiBit();
    // an extra zero byte to avoid getting a negative
    // two's complement input to new BigInteger(b).
    final int n = 1 + ((hi)/8);
    final byte[] b = new byte[n];
    int j = 0;
    int w = 0;
    for (int i=0;i<n;i++) {
      if (0==(i%4)) { w = word(j++); }
      else { w = (w>>>8); }
      b[i] = (byte) w; }
    return b; }

  default byte[] bigEndianBytes () {
    final int hi = hiBit();
    // an extra zero byte to avoid getting a negative
    // two's complement input to new BigInteger(b).
    final int n = 1 + ((hi)/8);
    final byte[] b = new byte[n];
    int j = 0;
    int w = 0;
    for (int i=0;i<n;i++) {
      if (0==(i%4)) { w = word(j++); }
      else { w = (w>>>8); }
      b[n-1-i] = (byte) w; }
    return b; }

  default BigInteger bigIntegerValue () {
    return new BigInteger(bigEndianBytes()); }

  default String toHexString () {
    final StringBuilder b = new StringBuilder("");
    final int n = hiInt()-1;
    if (0>n) { b.append('0'); }
    else {
      b.append(String.format("%x",Long.valueOf(uword(n))));
      for (int i=n-1;i>=0;i--) {
        //b.append(" ");
        b.append(String.format("%08x",Long.valueOf(uword(i)))); } }
    return b.toString(); }

  //--------------------------------------------------------------
  // 'Object' interface
  //--------------------------------------------------------------

  default int uintsHashCode () {
    int hashCode = 0;
    for (int i=0; i<hiInt(); i++) {
      hashCode = (int) ((31 * hashCode) + uword(i)); }
    return hashCode; }

  // DANGER: equality across classes
  default boolean uintsEquals (final Uints x) {
    if (x==this) { return true; }
    final Uints u = x;
    final int n = Math.max(hiInt(),u.hiInt());
    for (int i=0; i<n; i++) {
      if (word(i)!=u.word(i)) { return false; } }
    return true; }

  //--------------------------------------------------------------
}

