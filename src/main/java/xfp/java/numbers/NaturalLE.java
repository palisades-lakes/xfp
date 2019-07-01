package xfp.java.numbers;

import java.util.Arrays;

/** immutable arbitrary-precision non-negative integers
 * (natural number) represented by big-endian
 * unsigned <code>int[]</code>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-01
 */

public final class NaturalLE extends NaturalLEBase
implements Natural {

  private static final long serialVersionUID = 1L;

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { return defaultHashCode(); }

  @Override
  public final boolean equals (final Object x) {
    if (x==this) { return true; }
    if (!(x instanceof NaturalLE)) { return false; }
    return equals((NaturalLE) x); }

  /** hex string. */
  @Override
  public final String toString () { return toHexString(); }

  //--------------------------------------------------------------
  // construction
  //-------------------------------------------------------------

  private NaturalLE (final int[] words,
                     final int i0,
                     final int i1,
                     final int startWord) {
    super(words,i0,i1,startWord); }

  //-------------------------------------------------------------
  // TODO: move these to NaturalLEMutable?
  /** Take args as given. */

  private static final NaturalLE literal (final int[] words,
                                          final int i0,
                                          final int i1,
                                          final int startWord) {
    return new NaturalLE(words,i0,i1,startWord); }

  /** Adjust i0, i1, and startWord to exclude zeros. */

  static final NaturalLE unsafe (final int[] words,
                                         final int i0,
                                         final int i1,
                                         final int startWord) {
    int j0 = i0;
    while ((j0<i1)&&(0==words[j0])) { j0++; }
    int j1 = i1;
    while ((j0<j1)&&(0==words[j1-1])) { j1--; }
    return literal(words,j0,j1,startWord+j0);}

  /** Copy non-zero elements from words to minimal size array. 
   * This is safe, because it always copies the elements*/

  public static final NaturalLE make (final int[] words,
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

  @Override
  public final Natural recyclable () {
    return NaturalLEMutable.make(); }


  //  public static final NaturalLE valueOf (final byte[] b,
  //                                          final int off,
  //                                          final int len) {
  //    return unsafe(stripLeadingZeros(b,off,len)); }

  //  public static final NaturalLE valueOf (final byte[] b) {
  //    return valueOf(b,0,b.length); }

  //  public static final NaturalLE valueOf (final BigInteger bi) {
  //    return valueOf(bi.toByteArray()); }

  //-------------------------------------------------------------

  //  public static final NaturalLE valueOf (final String s,
  //                                          final int radix) {
  //    return unsafe(toInts(s,radix)); }

  //  public static final NaturalLE valueOf (final String s) {
  //    return valueOf(s,0x10); }

  //--------------------------------------------------------------
  // cached values
  //--------------------------------------------------------------

  //  private static final int MAX_CONSTANT = 16;
  //  private static final NaturalLE posConst[] =
  //    new NaturalLE[MAX_CONSTANT+1];
  //
  //  private static volatile NaturalLE[][] powerCache;
  //
  //  /** The cache of logarithms of radices for base conversion. */
  //  private static final double[] logCache;
  //
  //  static {
  //    for (int i = 1; i <= MAX_CONSTANT; i++) {
  //      final int[] magnitude = new int[1];
  //      magnitude[0] = i;
  //      posConst[i] = unsafe(magnitude); }
  //    // Initialize the cache of radix^(2^x) values used for base
  //    // conversion with just the very first value. Additional
  //    // values will be created on demand.
  //    powerCache = new NaturalLE[Character.MAX_RADIX + 1][];
  //    logCache = new double[Character.MAX_RADIX + 1];
  //    for (
  //      int i = Character.MIN_RADIX;
  //      i <= Character.MAX_RADIX;
  //      i++) {
  //      powerCache[i] = new NaturalLE[] { NaturalLE.valueOf(i) };
  //      logCache[i] = Math.log(i); } }

  //  public static final NaturalLE ZERO = new NaturalLE(EMPTY);
  //  public static final NaturalLE ONE = valueOf(1);
  //  public static final NaturalLE TWO = valueOf(2);
  //  public static final NaturalLE TEN = valueOf(10);

  //--------------------------------------------------------------

  //  public static final NaturalLE valueOf (final long x) {
  //    if (x==0) { return ZERO; }
  //    assert 0L < x;
  //    if (x <= MAX_CONSTANT) { return posConst[(int) x]; }
  //    return unsafe(toInts(x)); }

  //--------------------------------------------------------------

  //  public static final NaturalLE valueOf (final long x,
  //                                          final int upShift) {
  //    if (0L==x) { return ZERO; }
  //    assert 0L < x;
  //    return unsafe(shiftUp(x,upShift)); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

