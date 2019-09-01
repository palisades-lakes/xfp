package xfp.java.numbers;

import java.math.BigInteger;
import java.util.List;

import xfp.java.exceptions.Exceptions;

/** An interface for nonnegative integers represented as
 * a sequence of <code>int</code> words, treated as unsigned.
 *
 * The value of the natural number is
 * <code>sum<sub>i=startWord()</sub><sup>i&lt;hiInt</sup>
 *  uword(i) * 2<sup>32*i</sup></code>.
 *
 * TODO: utilities class to hide private stuff?
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-08-31
 */

@SuppressWarnings("unchecked")
public interface Natural extends Ringlike<Natural> {

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

  default Natural setWord (final int i,
                           final int w) {
    throw Exceptions.unsupportedOperation(this,"setWord",i,w); }

  /** The value of the unsigned <code>i</code>th (little endian) 
   * word as a <code>long</code>.
   */

  long uword (final int i);

  //--------------------------------------------------------------
  /** Inclusive lower bound on non-zero words:
   * <code>0L==uword(i)</code> for
   * <code>startWord()>i</code>.
   * Doesn't guarantee that <code>0L!=uword(i)</code> for
   * <code>startWord()&lt;=i</code>.
   * Zero may have no words, in which case,
   * {@link #startWord()} <code>==</code> {@link #endWord()}
   * <code>==0</code>.
   * <p>
   * This is intended to be a fast to return, looser bound than
   * @{link {@link #loInt()}.
   */

  int startWord ();

  /** Exclusive upper bound for non-zero words:
   * <code>0L==uword(i)</code> for
   * <code>endWord()<=i</code>.
   * Doesn't guarantee that <code>0L!=uword(i)</code> for
   * <code>endWord()&gt;i</code>.
   * <p>
   * This is intended to be a fast to return, looser bound than
   * @{link {@link #hiInt()}.
   */

  int endWord ();

  //--------------------------------------------------------------
  /** Return the index of the lowest non-zero word,
   * unless all words are zero, in which case,
   * {@link #loInt()} <code>==</code> {@link #hiInt()}
   * <code>==0</code>.
   * Guarantees <code>0!=word(loInt())</code> unless
   * {@link #loInt()} <code>==</code> {@link #hiInt()}
   * <code>==0</code>.
   */

  int loInt ();

  /** Return the index of the highest non-zero word.
   * unless all words are zero, in which case,
   * {@link #loInt()} <code>==</code> {@link #hiInt()}
   * <code>==0</code>.
   * Guarantees <code>0!=word(hiInt()-1)</code> unless
   * {@link #loInt()} <code>==</code> {@link #hiInt()}
   * <code>==0</code>.
   */

  int hiInt ();

  //--------------------------------------------------------------

  Natural copy ();

  //--------------------------------------------------------------
  /** Return the <code>[i0,i1)</code> words as a new 
   * <code>Natural</code> with <code>[0,i1-i0)</code> words.
   */

  default Natural words (final int i0,
                         final int i1) {
    //assert 0<=i0;
    //assert i0<i1;
    if ((0==i0) && (hiInt()<=i1)) { return copy(); }
    final int n = Math.max(0,i1-i0);
    if (0>=n) { return zero(); }
    Natural u = zero();
    for (int i=0;i<n;i++) { u =  u.setWord(i,word(i+i0)); }
    return u; }

  //--------------------------------------------------------------
  // bit ops
  //--------------------------------------------------------------

  /** Return the index of the lowest non-zero bit,
   * unless all bits are zero, in which case,
   * {@link #loBit()} <code>==</code> {@link #hiBit()}
   * <code>==0</code>.
   */

  int loBit ();

  /** Return <code>1 +</code> index of the highest non-zero bit.
   * If all bits are zero, 
   * {@link #loBit()} <code>==</code> {@link #hiBit()}
   * <code>==0</code>.
   */

  int hiBit ();

  //--------------------------------------------------------------

  default boolean testBit (final int n) {
    //assert 0<=n;
    final int w = word(n>>>5);
    final int b = (1 << (n&0x1F));
    return 0!=(w&b); }

  default Natural setBit (final int i) {
    //assert 0<=i;
    final int iw = (i>>>5);
    final int w = word(iw);
    final int ib = (i&0x1F);
    return setWord(iw,(w|(1<<ib))); }

  //  default Natural clearBit (final int n) {
  //    //assert 0<=n;
  //    final int iw = (n>>>5);
  //    final int w = word(iw);
  //    final int ib = (n&0x1F);
  //    return setWord(iw,(w&(~(1<<ib)))); }

  //  default Natural flipBit (final int n) {
  //    //assert 0<=n;
  //    final int iw = (n>>>5);
  //    final int w = word(iw);
  //    final int ib = (n&0x1F);
  //    return setWord(iw,(w^(1<<ib))); }

  //--------------------------------------------------------------

  Natural shiftDown (final int shift);

  Natural shiftUp (final int shift);

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
    for (int i=startWord(); i<hiInt(); i++) {
      hashCode = ((31 * hashCode) + word(i)); }
    return hashCode; }

  // DANGER: equality across classes
  default boolean uintsEquals (final Natural x) {
    if (x==this) { return true; }
    final Natural u = x;
    final int n0 = startWord();
    if (n0!=u.startWord()) { return false; }
    final int n1 = hiInt();
    if (n1!=u.hiInt()) { return false; }
    for (int i=n0; i<n1; i++) {
      if (word(i)!=u.word(i)) { return false; } }
    return true; }

  //--------------------------------------------------------------
  // arithmetic
  //--------------------------------------------------------------

  Natural add (final Natural u,
               final int upShift);

  //--------------------------------------------------------------
  // multiply
  //--------------------------------------------------------------

  default Natural multiply (final long u) {
    //assert isValid();
    return NaturalMultiply.multiply(this,u); }

//  Natural multiply (final long u,
//                    final int upShift);

  default Natural multiply (final long u,
                            final int upShift) {
    //assert isValid();
    //assert 0L<=u;
    //assert 0<=upShift;
    if (0L==u) { return zero(); }
    if (0==upShift) { return multiply(u); }
    if (isZero()) { return this; }
    return multiply(NaturalLE.valueOf(u,upShift)); }

  //--------------------------------------------------------------
  // division
  //-------------------------------------------------------------

  // for testing
  default List<Natural> divideAndRemainderKnuth (final Natural u) {
    //assert isValid();
    //assert u.isValid();
    return NaturalDivide.divideAndRemainderKnuth(this,u); }

  // for testing
  default List<Natural> divideAndRemainderBurnikelZiegler (final Natural u) {
    //assert isValid();
    //assert u.isValid();
    return NaturalDivide.divideAndRemainderBurnikelZiegler(this,u); }

   //--------------------------------------------------------------
}
//--------------------------------------------------------------
