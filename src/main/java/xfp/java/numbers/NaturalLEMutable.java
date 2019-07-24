package xfp.java.numbers;

import java.math.BigInteger;
import java.util.Arrays;

/** mutable arbitrary-precision non-negative integers
 * (natural numbers) represented by little-endian
 * unsigned <code>int[]</code>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-23
 */

public final class NaturalLEMutable implements Natural {

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private int[] _words;
  private final int[] words () { 
    assert isValid();
    return _words; }

  public final int[] copyWords () { 
    assert isValid();
    return Arrays.copyOf(words(),words().length); }

  // TODO: grow faster?
  private final void expandTo (final int i) {
    assert isValid();
    final int n = Math.max(i+1,hiInt());
    if (hiInt()<n) { _words = Arrays.copyOf(words(),n); } }

  //--------------------------------------------------------------
  // Natural
  //--------------------------------------------------------------

  @Override
  public final Natural zero () { 
    assert isValid();
    return unsafe(Ints.EMPTY); }

  @Override
  public final Natural one () { 
    assert isValid();
    return unsafe(new int[] { 1 }); }

  //--------------------------------------------------------------
  // Uints
  //--------------------------------------------------------------

  @Override
  public final int startWord () { 
    assert isValid();
    return 0; }
  
  @Override
  public final int endWord () { 
    assert isValid();
    return words().length; }

  @Override
  public final int word (final int i) {
    assert isValid();
    assert 0<=i : "Negative index: " + i;
    if (endWord()<=i) { return 0; }
    return words()[i]; }

  @Override
  public final Natural setWord (final int i,
                                final int w) {
    assert isValid();
    assert 0<=i;
    expandTo(i);
    words()[i] = w;
    return recycle(); }
    //return this; }

  @Override
  public final Natural empty () { 
    assert isValid();
    // safe because Ints.EMPTY is immutable
    return unsafe(Ints.EMPTY); }

  @Override
  public final Natural from (final int u) {
    assert isValid();
    assert 0<=u;
    return valueOf(u);  }

  @Override
  public final Natural from (final long u) {
    assert isValid();
    assert 0<=u;
    return valueOf(u);  }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

   /** DANGER!!! Mutable!!! */
  @Override
  public final int hashCode () { 
    assert isValid();
    return 0; }

   /** DANGER!!! Mutable!!! used in testing only!!! */
  @Override
  public final boolean equals (final Object x) {
    assert isValid();
    if (x==this) { return true; }
    if (!(x instanceof NaturalLEMutable)) { return false; }
    return uintsEquals((NaturalLEMutable) x); }

  /** hex string. */
  @Override
  public final String toString () { return toHexString(); }

  //--------------------------------------------------------------
  // Transience
  //--------------------------------------------------------------

  @Override
  public boolean isImmutable () { 
    assert isValid();
    return false; }

  @Override
  public final Natural immutable () { 
    assert isValid();
    return NaturalLE.make(words()); }

  @Override
  public final Natural recyclable (final Natural init) { 
    assert isValid();
   return copy(init); }

  @Override
  public final Natural recyclable (final int init) {
    assert isValid();
    return NaturalLEMutable.make(init); }

  @Override
  public final Natural recyclable (final Natural init,
                                   final int nWords) {
    assert isValid();
    if (null==init) {
      return NaturalLEMutable.make(nWords); }
    if (init instanceof NaturalLEMutable) {
      return NaturalLEMutable.make(words(),nWords); }
    return init.recyclable(init,nWords); }

  @Override
  public final Natural copy () { 
    assert isValid();
    return copy(this); }

  @Override
  public final Natural recycle () { 
    assert isValid();
   final int[] t = _words;
    _words = null;
    return unsafe(t); }

  @Override
  public final boolean isValid () { return null!=_words; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  /** UNSAFE: doesn't copy <code>words</code>. */
  private NaturalLEMutable (final int[] words) { _words = words; }

  public static final NaturalLEMutable 
  unsafe (final int[] words) {
    return new NaturalLEMutable(words); }

  /** Copy <code>words</code>. 
   */
  public static final NaturalLEMutable make (final int[] words) {
    return unsafe(Arrays.copyOf(words,words.length)); }

  /** Copy <code>words</code>, allowing space for at least
   * <code>n</code> words in the resulting natural number. 
   */
  public static final NaturalLEMutable make (final int[] words,
                                             final int n) {
    final int nn = Math.max(n,words.length);
    final int[] w = new int[nn];
    System.arraycopy(words,0,w,0,nn);
    return unsafe(w); }

  /** Zero instance with space for <code>n</code> words. 
   */
  public static final NaturalLEMutable make (final int n) {
    return unsafe(new int[n]); }

  //--------------------------------------------------------------
  /** From a big endian {@code byte[]}, as produced by
   * {@link BigInteger#toByteArray()}.
   */

  private static final NaturalLEMutable valueOf (final byte[] a) {
    final int nBytes = a.length;
    int keep = 0;
    while ((keep<nBytes) && (a[keep]==0)) { keep++; }
    final int nInts = ((nBytes-keep) + 3) >>> 2;
    final int[] result = new int[nInts];
    int b = nBytes-1;
    for (int i = nInts - 1; i >= 0; i--) {
      result[i] = a[b--] & 0xff;
      final int bytesRemaining = (b - keep) + 1;
      final int bytesToTransfer = Math.min(3,bytesRemaining);
      for (int j = 8; j <= (bytesToTransfer << 3); j += 8) {
        result[i] |= ((a[b--] & 0xff) << j); } }
    Ints.reverse(result);
    return unsafe(result); }

  public static final NaturalLEMutable valueOf (final BigInteger u) {
    assert 0<=u.signum();
    return valueOf(u.toByteArray()); }

  //-------------------------------------------------------------

  public static final NaturalLEMutable valueOf (final String s,
                                                final int radix) {
    return unsafe(Ints.littleEndian(s,radix)); }

  public static final NaturalLEMutable valueOf (final String s) {
    return valueOf(s,0x10); }

  public static final NaturalLEMutable valueOf (final long u) {
    assert 0L<=u;
    return unsafe(Ints.littleEndian(u)); }

  public static final NaturalLEMutable valueOf (final int u) {
    assert 0L<=u;
    return unsafe(new int[] {u}); }

  //--------------------------------------------------------------

  public static final NaturalLEMutable copy (final NaturalLE u) { 
    return unsafe(u.copyWords()); }

  public static final NaturalLEMutable 
  copy (final NaturalLEMutable u) { return unsafe(u.copyWords()); }

  public static final NaturalLEMutable copy (final Natural u) { 
    if (u instanceof NaturalLEMutable) {
      return copy((NaturalLEMutable) u); }
    if (u instanceof NaturalLE) {
      return copy((NaturalLE) u); }
    final int n = u.hiInt();
    final int[] w = new int[n];
    for (int i=0;i<n;i++) { w[i] = u.word(i); }
    return unsafe(w); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

