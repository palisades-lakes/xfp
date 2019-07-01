package xfp.java.numbers;

import java.math.BigInteger;

import xfp.java.exceptions.Exceptions;

// TODO: wrap rather than inherit?

/** Code common to {@link NaturalLE} and {@link NaturalLEMutable}.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-06-26
 */

public abstract class NaturalLEBase extends Number {

  private static final long serialVersionUID = 1L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------
  /** This array is never modified in {@link NaturalLE}.
   * Frequently modified in {@link NaturalLEMutable}.
   * <p>
   * It holds the value of this natural number as little endian 
   * <code>int</code> words, ignoring everything outside the
   * <code>[i0,i1)</code> index range.
   * The value in the <code>i</code>th element corresponds to 
   * <code>iword(i) * 2<sup>32*i+startWord()</sup></code>.
   */

  protected final int[] _words;
  protected final int[] words () { return _words; }

  protected final int _i0;
  private final int i0 () { return _i0; }
  protected final int _i1;
  private final int i1 () { return _i1; }
  private final int iLength () { return i1() - i0(); }
  private final int iword (final int ii) { 
    if (ii<i0()) { return 0; }
    if (i1()<=ii) { return 0; }
    return _words[ii]; }

  protected final int _startWord;
  
  public final int startWord () { return _startWord; }
  public final int endWord () { return startWord() + iLength(); }
  public final int word (final int i) { return iword(i-startWord()); }

  //--------------------------------------------------------------
  // Number methods
  //--------------------------------------------------------------

  public final byte[] toByteArray () {
    throw Exceptions.unsupportedOperation(this,"toByteArray"); }

  public final BigInteger bigIntegerValue () {
    return new BigInteger(toByteArray()); }

  @Override
  public final int intValue () {
    throw Exceptions.unsupportedOperation(this,"intValue"); }

  @Override
  public final long longValue () {
    throw Exceptions.unsupportedOperation(this,"longValue"); }

  @Override
  public final float floatValue () {
    throw Exceptions.unsupportedOperation(this,"floatValue"); }

  @Override
  public final double doubleValue () {
    throw Exceptions.unsupportedOperation(this,"doubleValue"); }

  //--------------------------------------------------------------
  // construction
  //-------------------------------------------------------------

  NaturalLEBase (final int[] words,
                     final int i0,
                     final int i1,
                     final int startWord) {
    assert 0<=i0;
    assert i0<=i1;
    assert i1<words.length;
    assert 0<=startWord;
    _words = words;
    _i0 = i0;
    _i1 = i1;
    _startWord = startWord; }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

