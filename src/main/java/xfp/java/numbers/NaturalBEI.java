package xfp.java.numbers;

import java.math.BigInteger;
import java.util.Arrays;

/** immutable arbitrary-precision non-negative integers
 * (natural number) represented by big-endian
 * unsigned <code>int[]</code>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-16
 */

public final class NaturalBEI //extends Number
implements Natural {

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final int[] _words;
  private final int[] words () { return _words; }

  @Override
  public final int word (final int i) {
    assert 0<=i : "Negative index: " + i;
    final int n = words().length;
    final int ii = n-i-1;
    if ((0<=ii) && (ii<n)) { return words()[ii]; }
    return 0; }

  @Override
  public final Natural setWord (final int i,
                                final int w) {
    assert 0<=i;
    final int n0 = words().length;
    final int n1 = Math.max(i+1,n0);
    assert i<n1;
    final int[] ws = new int[n1];
    System.arraycopy(words(),0,ws,n1-n0,n0);
    ws[n1-1-i] = w;
    return unsafe(Ints.stripLeadingZeros(ws)); }

  @Override
  public final int startWord () { return 0; }
  @Override
  public final int endWord () { return words().length; }

  public final int[] copyWords () {
    return Arrays.copyOfRange(words(),0,endWord()); }

  public final void copyWords (final int[] dst,
                               final int start) {
    final int n = Math.min(words().length,dst.length-start);
    Arrays.fill(dst,0);
    System.arraycopy(words(),0,dst,start,n); }

  //--------------------------------------------------------------
  // Mutability
  //-------------------------------------------------------------

  @Override
  public final Natural immutable () { return this; }

  @Override
  public final Natural copy () { return this; }

  @Override
  public final Natural recyclable (final Natural init) {
    if (null==init) {
      return NaturalBEIMutable.make(words().length); }
    return NaturalBEIMutable.valueOf(init); }

  @Override
  public final Natural recyclable (final Natural init,
                                   final int nWords) {
    if (null==init) {
      return NaturalBEIMutable.make(nWords); }
    return NaturalBEIMutable.make(init,nWords); }

  @Override
  public final Natural recyclable (final int n) {
    assert 0<=n;
    return NaturalBEIMutable.make(n); }

  @Override
  public final boolean isImmutable () { return true; }

  //--------------------------------------------------------------
 // Object methods
 //--------------------------------------------------------------

 @Override
 public final int hashCode () { return uintsHashCode(); }

 @Override
 public final boolean equals (final Object x) {
   if (x==this) { return true; }
   if (!(x instanceof NaturalBEI)) { return false; }
   return uintsEquals((NaturalBEI) x); }

 /** hex string. */
 @Override
 public final String toString () { return toHexString(); }

  //--------------------------------------------------------------
  // construction
  //-------------------------------------------------------------

  private NaturalBEI (final int[] m) { _words = m; }

  // assume no leading zeros
  // TODO: change to implementation where leading zeros are ok
  public static final NaturalBEI unsafe (final int[] m) {
    return new NaturalBEI(m); }

  @Override
  public final Natural ones (final int n) {
    final int[] w = new int[n];
    Arrays.fill(w, 0xFFFFFFFF);
    return unsafe(w); }

  // assume no leading zeros
  public static final NaturalBEI valueOf (final int[] m) {
    return unsafe(Arrays.copyOf(m,m.length)); }

  public static final NaturalBEI valueOf (final byte[] a) {
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
    return unsafe(result); }

  public static final NaturalBEI valueOf (final BigInteger bi) {
    return valueOf(bi.toByteArray()); }

  public static final NaturalBEI valueOf (final String s,
                                          final int radix) {
    return unsafe(Ints.bigEndian(s,radix)); }

  public static final NaturalBEI valueOf (final String s) {
    return valueOf(s,0x10); }

  //--------------------------------------------------------------
  // cached values
  //--------------------------------------------------------------

  private static final int MAX_CONSTANT = 16;
  private static final NaturalBEI posConst[] =
    new NaturalBEI[MAX_CONSTANT+1];

  private static volatile NaturalBEI[][] powerCache;

  /** The cache of logarithms of radices for base conversion. */
  private static final double[] logCache;

  static {
    for (int i = 1; i <= MAX_CONSTANT; i++) {
      final int[] magnitude = new int[1];
      magnitude[0] = i;
      posConst[i] = unsafe(magnitude); }
    // Initialize the cache of radix^(2^x) values used for base
    // conversion with just the very first value. Additional
    // values will be created on demand.
    powerCache = new NaturalBEI[Character.MAX_RADIX + 1][];
    logCache = new double[Character.MAX_RADIX + 1];
    for (
      int i = Character.MIN_RADIX;
      i <= Character.MAX_RADIX;
      i++) {
      powerCache[i] = new NaturalBEI[] { NaturalBEI.valueOf(i) };
      logCache[i] = Math.log(i); } }

  public static final NaturalBEI ZERO = new NaturalBEI(Ints.EMPTY);
  public static final NaturalBEI ONE = valueOf(1);
  public static final NaturalBEI TWO = valueOf(2);
  public static final NaturalBEI TEN = valueOf(10);

  @Override
  public final Natural empty () { return ZERO; }

  @Override
  public final Natural one () { return ONE; }

  //--------------------------------------------------------------

  public static final NaturalBEI valueOf (final long x) {
    if (x==0L) { return ZERO; }
    assert 0L < x;
    if (x <= MAX_CONSTANT) { return posConst[(int) x]; }
    return unsafe(Ints.bigEndian(x)); }

  public static final NaturalBEI valueOf (final int x) {
    if (x==0) { return ZERO; }
    assert 0L < x;
    if (x <= MAX_CONSTANT) { return posConst[x]; }
    return unsafe(new int[] {x}); }

  //--------------------------------------------------------------

  @Override
  public final Natural from (final long u) { return valueOf(u); }

  @Override
  public final Natural from (final int u) { 
    return from(Numbers.unsigned(u)); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

