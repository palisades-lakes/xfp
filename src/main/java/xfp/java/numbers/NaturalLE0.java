package xfp.java.numbers;

import java.math.BigInteger;
import java.util.Arrays;

/** immutable arbitrary-precision non-negative integers
 * (natural number) represented by big-endian
 * unsigned <code>int[]</code>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-17
 */

public final class NaturalLE0 
extends NaturalLEBase
implements Natural {

  //--------------------------------------------------------------
  // Uints
  //--------------------------------------------------------------

  @Override
  public final Natural setWord (final int i,
                                final int w) {
    assert 0<=i;
    Natural u = recyclable(this);
    u = u.setWord(i,w);
    return u.immutable(); }

  //--------------------------------------------------------------
  // Transience
  //--------------------------------------------------------------

  @Override
  public final Natural recyclable (final Natural init) {
    return NaturalLEMutable0.copy((NaturalLEBase) init); }

  @Override
  public boolean isImmutable () { return true; }

  @Override
  public final Natural immutable () { return this; }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { return uintsHashCode(); }

  @Override
  public final boolean equals (final Object x) {
    if (x==this) { return true; }
    if (!(x instanceof NaturalLE0)) { return false; }
    return uintsEquals((NaturalLE0) x); }

  /** hex string. */
  @Override
  public final String toString () { return toHexString(); }

  //--------------------------------------------------------------
  // construction
  //-------------------------------------------------------------

  private NaturalLE0 (final int[] words,
                     final int i0,
                     final int i1,
                     final int startWord) {
    // UNSAFE: doesn't copy words!!!
    super(words,i0,i1,startWord); }

  //-------------------------------------------------------------
  // TODO: move these to NaturalLEMutable?
  /** Take args as given. */

  private static final NaturalLE0 literal (final int[] words,
                                          final int i0,
                                          final int i1,
                                          final int startWord) {
    return new NaturalLE0(words,i0,i1,startWord); }

  /** Adjust i0, i1, and startWord to exclude zeros. */

  private static final NaturalLE0 unsafe (final int[] words,
                                         final int i0,
                                         final int i1,
                                         final int startWord) {
    int j0 = i0;
    while ((j0<i1)&&(0==words[j0])) { j0++; }
    int j1 = i1;
    while ((j0<j1)&&(0==words[j1-1])) { j1--; }
    return literal(words,j0,j1,startWord+j0);}

  private static final NaturalLE0 unsafe (final int[] words) {
    return unsafe(words,0,words.length,0); }

  /** Copy non-zero elements from words to minimal size array.
   * This is safe, because it always copies the elements*/

  public static final NaturalLE0 make (final int[] words,
                                      final int i0,
                                      final int i1,
                                      final int startWord) {
    int j0 = i0;
    while ((j0<i1)&&(0==words[j0])) { j0++; }
    int j1 = i1;
    while ((j0<j1)&&(0==words[j1-1])) { j1--; }
    final int[] w = Arrays.copyOfRange(words,j0,j1);
    return literal(w,0,j1-j0,startWord+j0);}

  //--------------------------------------------------------------
  /** From a big endian {@code byte[]}, as produced by
   * {@link BigInteger#toByteArray()}.
   */

  private static final NaturalLE0 valueOf (final byte[] a) {
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

  public static final NaturalLE0 valueOf (final BigInteger bi) {
    return valueOf(bi.toByteArray()); }

  //-------------------------------------------------------------

  public static final NaturalLE0 valueOf (final String s,
                                         final int radix) {
    return unsafe(Ints.littleEndian(s,radix)); }

  public static final NaturalLE0 valueOf (final String s) {
    return valueOf(s,0x10); }

  //--------------------------------------------------------------

  public static final NaturalLE0 
  copy (final NaturalLEBase u) {
    return literal(
      Arrays.copyOf(u.words(),u.words().length),
      u.i0(),
      u.i1(),
      u.startWord()); }

  //--------------------------------------------------------------
  // cached values
  //--------------------------------------------------------------

  public static final NaturalLE0 ZERO = 
    new NaturalLE0(new int[0],-1,0,-1);
  //  public static final NaturalLE ONE = valueOf(1);
  //  public static final NaturalLE TWO = valueOf(2);
  //  public static final NaturalLE TEN = valueOf(10);

  @Override
  public final Natural empty () { return ZERO; }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

