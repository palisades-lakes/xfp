package xfp.java.numbers;

import static xfp.java.numbers.Numbers.unsigned;

import java.math.BigInteger;
import java.util.Arrays;

/** immutable arbitrary-precision non-negative integers
 * (natural number) represented by big-endian
 * unsigned <code>int[]</code>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-09
 */

public final class NaturalBEI //extends Number
implements Natural {

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private static final int[] stripLeadingZeros (final int[] m) {
    final int n = m.length;
    int start = 0;
    while ((start < n) && (m[start] == 0)) { start++; }
    return (0==start) ? m : Arrays.copyOfRange(m,start,n); }

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
    return unsafe(stripLeadingZeros(ws)); }

  @Override
  public final int startWord () { return 0; }
  @Override
  public final int endWord () { return words().length; }

  public final int[] copyWords () {
    return Arrays.copyOfRange(words(),0,endWord()); }

  //--------------------------------------------------------------
  // Mutability
  //-------------------------------------------------------------

  @Override
  public final Natural immutable () { return this; }

  @Override
  public final Natural recyclable (final Natural init) {
    if (null==init) {
      return NaturalBEIMutable.make(words().length); }
    return NaturalBEIMutable.valueOf(init); }

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

  // assume no leading zeros
  public static final NaturalBEI valueOf (final int[] m) {
    return unsafe(Arrays.copyOf(m,m.length)); }

  public static final NaturalBEI valueOf (final byte[] a) {
    //return valueOf(b,0,b.length); }
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

  //-------------------------------------------------------------

  private static final int[] EMPTY = new int[0];


  // string parsing

  // bitsPerDigit in the given radix times 1024
  // Rounded up to avoid under-allocation.

  private static final long[] bitsPerDigit =
  { 0, 0, 1024, 1624, 2048, 2378, 2648, 2875, 3072, 3247, 3402,
    3543, 3672, 3790, 3899, 4001, 4096, 4186, 4271, 4350, 4426,
    4498, 4567, 4633, 4696, 4756, 4814, 4870, 4923, 4975, 5025,
    5074, 5120, 5166, 5210, 5253, 5295 };

  private static final int[] digitsPerInt =
  { 0, 0, 30, 19, 15, 13, 11, 11, 10, 9, 9, 8, 8, 8, 8, 7, 7, 7,
    7, 7, 7, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 5 };

  private static final int[] intRadix =
  { 0, 0, 0x40000000, 0x4546b3db, 0x40000000, 0x48c27395,
    0x159fd800, 0x75db9c97, 0x40000000, 0x17179149, 0x3b9aca00,
    0xcc6db61, 0x19a10000, 0x309f1021, 0x57f6c100, 0xa2f1b6f,
    0x10000000, 0x18754571, 0x247dbc80, 0x3547667b, 0x4c4b4000,
    0x6b5a6e1d, 0x6c20a40, 0x8d2d931, 0xb640000, 0xe8d4a51,
    0x1269ae40, 0x17179149, 0x1cb91000, 0x23744899, 0x2b73a840,
    0x34e63b41, 0x40000000, 0x4cfa3cc1, 0x5c13d840, 0x6d91b519,
    0x39aa400 };

  // Multiply x array times word y in place, and add word z

  private static final void destructiveMulAdd (final int[] x,
                                               final int y,
                                               final int z) {
    final long ylong = unsigned(y);
    final long zlong = unsigned(z);
    final int lm1 = x.length-1;
    long product = 0;
    long carry = 0;
    for (int i = lm1; i >= 0; i--) {
      product = (ylong * unsigned(x[i])) + carry;
      x[i] = (int) product;
      carry = product >>> 32; }
    long sum = unsigned(x[lm1]) + zlong;
    x[lm1] = (int) sum;
    carry = sum >>> 32;
    for (int i = lm1-1; i >= 0; i--) {
      sum = unsigned(x[i]) + carry;
      x[i] = (int) sum;
      carry = sum >>> 32; } }

  private static final int[] toInts (final String s,
                                     final int radix) {
    final int len = s.length();
    assert 0 < len;
    assert Character.MIN_RADIX <= radix;
    assert radix <= Character.MAX_RADIX;
    assert 0 > s.indexOf('-');
    assert 0 > s.indexOf('+');

    int cursor = 0;
    // skip leading '0' --- not strictly necessary?
    while ((cursor < len)
      && (Character.digit(s.charAt(cursor),radix) == 0)) {
      cursor++; }
    if (cursor == len) { return EMPTY; }

    final int nDigits = len - cursor;

    // might be bigger than needed,
    // but stripLeadingZeros(int[]) handles that
    final long nBits =
      ((nDigits * bitsPerDigit[radix]) >>> 10) + 1;
    final int nWords = (int) (nBits + 31) >>> 5;
    final int[] m = new int[nWords];

    // Process first (potentially short) digit group
    int firstGroupLen = nDigits % digitsPerInt[radix];
    if (firstGroupLen == 0) { firstGroupLen = digitsPerInt[radix]; }
    String group = s.substring(cursor,cursor += firstGroupLen);
    m[nWords-1] = Integer.parseInt(group,radix);
    if (m[nWords-1] < 0) {
      throw new NumberFormatException("Illegal digit"); }

    // Process remaining digit groups
    final int superRadix = intRadix[radix];
    int groupVal = 0;
    while (cursor < len) {
      group = s.substring(cursor,cursor += digitsPerInt[radix]);
      groupVal = Integer.parseInt(group,radix);
      if (groupVal < 0) {
        throw new NumberFormatException("Illegal digit"); }
      destructiveMulAdd(m,superRadix,groupVal); }
    return stripLeadingZeros(m); }

  public static final NaturalBEI valueOf (final String s,
                                          final int radix) {
    return unsafe(toInts(s,radix)); }

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

  public static final NaturalBEI ZERO = new NaturalBEI(EMPTY);
  public static final NaturalBEI ONE = valueOf(1);
  public static final NaturalBEI TWO = valueOf(2);
  public static final NaturalBEI TEN = valueOf(10);

  @Override
  public final Natural empty () { return ZERO; }

  @Override
  public final Natural one () { return ONE; }

  //--------------------------------------------------------------

  private static final int[] toInts (final long u) {
    assert 0<=u;
    final int hi = (int) (u>>>32);
    final int lo = (int) u;
    if (0==hi) {
      if (0==lo) { return EMPTY; }
      return new int[] { lo, }; }
    return new int[] { hi, lo, }; }
  
  public static final NaturalBEI valueOf (final long x) {
    if (x==0) { return ZERO; }
    assert 0L < x;
    if (x <= MAX_CONSTANT) { return posConst[(int) x]; }
    return unsafe(toInts(x)); }

  //--------------------------------------------------------------

  @Override
  public final Natural from (final long u) { return valueOf(u); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

