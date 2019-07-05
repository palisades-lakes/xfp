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
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-05
 */

@SuppressWarnings("unchecked")
public interface Uints<T extends Uints> extends Transience<T> {

  //--------------------------------------------------------------
  // word ops
  //--------------------------------------------------------------
  /** The <code>i</code>th word as an <code>int</code>. <br>
   * <em>WARNING:</em> content used as unsigned.
   */

  int word (final int i);

  /** Return a sequence where the <code>i</code>th word is
   * <code>w</code>.
   * May return a new sequence, or may modify <code>this</code>.
   * Existing references to <code>this</code> may no longer be
   * valid.
   */

  public default T setWord (final int i,
                            final int w) {
    throw Exceptions.unsupportedOperation(this,"setWord",i,w); }

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

  public default T clear () {
    throw Exceptions.unsupportedOperation(this,"clear"); }

  //--------------------------------------------------------------

  T empty ();
  
  //  public default T empty () {
//    throw Exceptions.unsupportedOperation(this,"empty"); }

  //--------------------------------------------------------------
  /** Return the <code>[i0,i1)</code> words as a new 
   * <code>Uints</code> with <code>[0,i1-i0)</code> words.
   */
  public default T words (final int i0,
                          final int i1) {
    assert 0<=i1;
    assert i0<i1;
    if ((0==i0) && (endWord()<=i1)) { return immutable(); }
    final int n = Math.max(0,i1-i0);
    if (0>=n) { return empty(); }
    T u = recyclable(n);
    for (int i=0;i<n;i++) { u = (T) u.setWord(i,word(i+i0)); }
    return (T) u.immutable(); }

  //--------------------------------------------------------------
  /** Return a sequence whose value is the same as <code>u</code>.
   * May be the same object as <code>this</code>, if mutable.
   * May be <code>u</code>, if immutable.
   * May be a copy of <code>u</code>.
   */

  public default T set (final Uints u) {
    Uints x = clear();
    for (int i=u.startWord();i<u.endWord();i++) {
      x = x.setWord(i,u.word(i)); }
    return (T) this; }

  /** Return a Uints whose value is the same as <code>u</code>.
   */

  public default T set (final long u) {
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

  public default int hiInt () {
    final int end = endWord();
    //Debug.println("hiInt end=" + end);
    if (0==end) { return 0; }
    int n = end-1;
    final int start = startWord();
    //Debug.println("hiInt start=" + start);
    while ((start<n)&&(0==word(n))) { n--; }
    //Debug.println("hiInt n=" + n);
    return n+1; }

  //--------------------------------------------------------------
  // bit ops
  //--------------------------------------------------------------

  public default int loBit () {
    // Search for lowest order nonzero int
    final int i=loInt(); // might be -1
    if (i<0) { return -1; } // no set bits
    return (i << 5) + Integer.numberOfTrailingZeros(word(i)); }

  public default int hiBit () {
    //Debug.println("hiBit this=" + this);
    final int n = hiInt()-1;
    if (0>n) { return 0; }
    //Debug.println("hiBit n=" + n);
    //Debug.println("hiBit word(" + n + ")=" + Integer.toHexString(word(n)));
    //Debug.println("hiBit bitlength(" + Integer.toHexString(word(n)) + ")=" + Numbers.bitLength(word(n)));
    return (n<<5) + Numbers.bitLength(word(n)); }

  //--------------------------------------------------------------

  public default boolean testBit (final int n) {
    assert 0<=n;
    final int w = word(n>>>5);
    final int b = (1 << (n&0x1F));
    return 0!=(w&b); }

  //--------------------------------------------------------------
  /** get the least significant int word of (this >>> shift) */

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

  public default T setBit (final int i) {
    assert 0<=i;
    final int iw = (i>>>5);
    final int w = word(iw);
    final int ib = (i&0x1F);
    return setWord(iw,(w|(1<<ib))); }

  public default T clearBit (final int n) {
    assert 0<=n;
    final int iw = (n>>>5);
    final int w = word(iw);
    final int ib = (n&0x1F);
    return setWord(iw,(w&(~(1<<ib)))); }

  public default T flipBit (final int n) {
    assert 0<=n;
    final int iw = (n>>>5);
    final int w = word(iw);
    final int ib = (n&0x1F);
    return setWord(iw,(w^(1<<ib))); }

  //--------------------------------------------------------------

  public default T shiftDown (final int bitShift) {
    throw
    Exceptions.unsupportedOperation(this,"shiftDown",bitShift); }

  public default T shiftUp (final int bitShift) {
    throw
    Exceptions.unsupportedOperation(this,"shiftUp",bitShift); }

  /** Return a Uints whose value is
   * <code>u * 2<sup>shift</sup></code>.
   * Usually the same object as <code>this</code>.
   *
   * <code>0&lt;=shift</code>
   */

  public default T shiftUp (final T u,
                            final int shift) {
    // TODO: optimize as single op
    return (T) set(u).shiftUp(shift); }

  /** Return a Uints whose value is
   * <code>u * 2<sup>shift</sup></code>.
   * Usually the same object as <code>this</code>.
   *
   * <code>0&lt;=shift</code>
   */

  public default T shiftUp (final long u,
                            final int shift) {
    // TODO: optimize as single op
    return (T) set(u).shiftUp(shift); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  /** Return a new Uints whose value is <code>u</code>.
   * Does not modify <code>this</code>.
   */

  public default T from (final long u) {
    throw Exceptions.unsupportedOperation(
      this,"from",u); }

  /** Return a new Uints whose value is
   * <code>u * 2<sup>shift</sup></code>.
   * Does not modify <code>this</code>.
   *
   * <code>0&lt;=shift</code>
   */

  public default T from (final long u,
                         final int shift) {
    throw Exceptions.unsupportedOperation(
      this,"from",u,shift); }

  //--------------------------------------------------------------
  // 'Number' interface
  //--------------------------------------------------------------
  // TODO: move to Ringlike?

  public default byte[] toByteArray () {
    throw Exceptions.unsupportedOperation(this,"toByteArray"); }

  public default BigInteger bigIntegerValue () {
    return new BigInteger(toByteArray()); }

  public default String toHexString () {
    final StringBuilder b = new StringBuilder("");
    final int n = endWord()-1;
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

  public default int uintsHashCode () {
    int hashCode = 0;
    for (int i=0; i<endWord(); i++) {
      hashCode = (int) ((31 * hashCode) + uword(i)); }
    return hashCode; }

  // DANGER: equality across classes
  public default boolean uintsEquals (final Uints x) {
    if (x==this) { return true; }
    final Uints u = x;
    final int n = Math.max(endWord(),u.endWord());
    for (int i=0; i<n; i++) {
      if (word(i)!=u.word(i)) { return false; } }
    return true; }

  //--------------------------------------------------------------
}

