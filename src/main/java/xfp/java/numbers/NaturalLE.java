package xfp.java.numbers;

import java.math.BigInteger;
import java.util.Arrays;

/** immutable arbitrary-precision non-negative integers
 * (natural numbers) represented by little-endian
 * unsigned <code>int[]</code>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-24
 */

public final class NaturalLE implements Natural {

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------
  /** This array is never modified.
   */

  private final int[] _words;
  private final int[] words () { return _words; }

  /** Don't drop trailing zeros. */
  public final int[] copyWords () { 
    return Arrays.copyOf(words(),words().length); }
  
  // ought to be _words.length, but safer to compute and cache
  // in case we want to allow over-large arrays in the future
  private final int _hiInt;
  @Override
  public final int hiInt () { return _hiInt; }

  //--------------------------------------------------------------
  // Natural
  //--------------------------------------------------------------

  private static final NaturalLE ONE = unsafe(new int[] { 1 });

  @Override
  public final Natural one () { return ONE; }

  @Override
  public final Natural subtract (final Natural u) {
    assert isValid();
    assert u.isValid();
    return recyclable(this).subtract(u).immutable();  }
  
  //--------------------------------------------------------------
  // Uints
  //--------------------------------------------------------------

  @Override
  public final int startWord () { return 0; }
  @Override
  public final int endWord () { return words().length; }

  @Override
  public final int word (final int i) {
    assert 0<=i : "Negative index: " + i;
    if (endWord()<=i) { return 0; }
    return words()[i]; }

  @Override
  public final Natural setWord (final int i,
                                final int w) {
    assert 0<=i;
    final int n = Math.max(i+1,hiInt());
    final  int[] u = Arrays.copyOf(words(),n);
    u[i] = w;
    return unsafe(u); }

  /** Singleton.<br>
   * TODO: Better to use a new array?
   */
  private static final NaturalLE ZERO = new NaturalLE(Ints.EMPTY); 

  @Override
  public final Natural empty () { return ZERO; }

  /** Return a {@link NaturalLE} equivalent to the unsigned 
   * value of <code>u</code>.
   */
  @Override
  public final Natural from (final int u) { return valueOf(u); }

  /** <code>0<=u</code>.*/
  @Override
  public final Natural from (final long u) {
    assert 0<=u;
    return valueOf(u);  }

  @Override
  public final Natural shiftDownWords (final int iShift) {
    assert 0<=iShift;
    return recyclable(this).shiftDownWords(iShift).immutable(); }

  @Override
  public final Natural shiftDown (final int shift) {
    return recyclable(this).shiftDown(shift).immutable(); }

  //--------------------------------------------------------------
  // Transience
  //--------------------------------------------------------------

  @Override
  public final boolean isValid () { return true; }
  
  @Override
  public final Natural recyclable (final Natural init) {
    return NaturalLEMutable.copy(init); }

  @Override
  public final Natural recyclable (final int init) {
    return NaturalLEMutable.make(init); }

  @Override
  public final Natural recyclable (final Natural init,
                                   final int nWords) {
    if (null==init) {
      return NaturalLEMutable.make(nWords); }
    if (init instanceof NaturalLE) {
      return NaturalLEMutable.make(words(),nWords); }
    return init.recyclable(init,nWords); }

  @Override
  public boolean isImmutable () { return true; }

  @Override
  public final Natural immutable () { return this; }

  @Override
  public final Natural copy () {  return copy(this); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { return uintsHashCode(); }

  @Override
  public final boolean equals (final Object x) {
    if (x==this) { return true; }
    if (!(x instanceof Natural)) { return false; }
    return uintsEquals((Natural) x); }

  /** hex string. */
  @Override
  public final String toString () { return toHexString(); }

  //--------------------------------------------------------------
  // construction
  //-------------------------------------------------------------

  /** UNSAFE: doesn't copy <code>words</code>. */
  private NaturalLE (final int[] words) { 
    _words = words; 
    _hiInt = Ints.hiInt(words); }

  /** Doesn't copy <code>words</code>. 
   */

  static final NaturalLE unsafe (final int[] words) {
    return new NaturalLE(words); }

  /** Copy <code>words</code>. 
   *  */
  public static final NaturalLE make (final int[] words) {
    final int end = Ints.hiInt(words);
    return unsafe(Arrays.copyOf(words,end)); }
    //return unsafe(Arrays.copyOf(words,words.length)); }

  //--------------------------------------------------------------
  /** From a big endian {@code byte[]}, as produced by
   * {@link BigInteger#toByteArray()}.
   */

  private static final NaturalLE valueOf (final byte[] a) {
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
    return make(result); }

  public static final NaturalLE valueOf (final BigInteger u) {
    assert 0<=u.signum();
    return valueOf(u.toByteArray()); }

  //-------------------------------------------------------------

  public static final NaturalLE valueOf (final String s,
                                         final int radix) {
    return make(Ints.littleEndian(s,radix)); }

  public static final NaturalLE valueOf (final String s) {
    return valueOf(s,0x10); }

  /** <code>0L<=u</code>. */
  public static final NaturalLE valueOf (final long u) {
    assert 0L<=u;
    if (u==0L) { return ZERO; }
    return make(Ints.littleEndian(u)); }

  
  /** Return a {@link NaturalLE} equivalent to the unsigned 
   * value of <code>u</code>.
   */
  public static final NaturalLE valueOf (final int u) {
    if (u==0) { return valueOf(0L); }
    return unsafe(new int[] {u}); }

  //--------------------------------------------------------------

  public static final NaturalLE 
  copy (final NaturalLE u) { return make(u.words()); }

  public static final NaturalLE 
  copy (final NaturalLEMutable u) { 
    return make(u.copyWords()); }

  public static final NaturalLE
  copy (final Natural u) { 
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

