package xfp.java.numbers;

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
 * @version 2019-09-03
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

  Natural setWord (final int i,
                   final int w);

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

  Natural words (final int i0,
                 final int i1);

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

  Natural shiftDown (final int shift);

  Natural shiftUp (final int shift);

  //--------------------------------------------------------------
  // arithmetic
  //--------------------------------------------------------------

  Natural add (final Natural u,
               final int upShift);

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
