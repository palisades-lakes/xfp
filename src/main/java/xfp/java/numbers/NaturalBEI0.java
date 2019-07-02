package xfp.java.numbers;

import static xfp.java.numbers.Numbers.loWord;
import static xfp.java.numbers.Numbers.unsigned;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import xfp.java.Debug;

/** immutable arbitrary-precision non-negative integers
 * (natural number) represented by big-endian
 * unsigned <code>int[]</code>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-01
 */

public final class NaturalBEI0 extends Number
implements Natural {

  private static final long serialVersionUID = 1L;

  private final int[] _words;
  public final int[] copyWords () {
    return Arrays.copyOf(_words,_words.length); }

  @Override
  public final boolean isZero () { return 0==_words.length; }

  @Override
  public final int word (final int i) {
    final int n = _words.length;
    final int ii = n-i-1;
    if ((0<=ii) && (ii<n)) { return _words[ii]; }
    return 0; }

  @Override
  public final int startWord () { return 0; }
  @Override
  public final int endWord () { return _words.length; }

  //--------------------------------------------------------------
  // arithmetic
  //--------------------------------------------------------------

  @Override
  public final Natural add (final Natural m) {
    return unsafe(Bei0.add(_words,((NaturalBEI0) m)._words)); }

  @Override
  public final NaturalBEI0 add (final long m) {
    assert 0L<=m;
    return unsafe(Bei0.add(_words,m)); }

  public static final NaturalBEI0 add (final long t0,
                                       final long t1,
                                       final int bitShift) {
    assert 0L<=t0;
    assert 0L<=t1;
    assert 0<=bitShift;
    final int[] u = Bei0.add(t0,t1,bitShift);
    return unsafe(u); }

  @Override
  public final NaturalBEI0 add (final long m,
                                final int shift) {
    assert 0L<=m;
    final int[] u = Bei0.add(_words,m,shift);
    return unsafe(u); }

  //--------------------------------------------------------------
  // only when val <= this

  @Override
  public final Natural subtract (final long m) {
    assert 0L<=m;
    final int[] u = Bei0.subtract(_words,m);
    return unsafe(u); }

  // only when val <= this

  @Override
  public final Natural subtract (final Natural m) {
    return unsafe(
      Bei0.subtract(_words,((NaturalBEI0) m)._words)); }

  // only when (m << upShift) <= this
  @Override
  public final NaturalBEI0 subtract (final long m,
                                     final int upShift) {
    assert 0L<=m;
    final int[] u = Bei0.subtract(_words,m,upShift);
    return unsafe(u); }

  // only when (m1 << upShift) <= m0
  public static final NaturalBEI0 subtract (final long m0,
                                            final long m1,
                                            final int upShift) {
    assert 0L<=m0;
    assert 0L<=m1;
    assert 0<=upShift;
    final int[] u = Bei0.subtract(m0,m1,upShift);
    return unsafe(u); }

  // only when (m1 << upShift) <= m0
  public static final NaturalBEI0 subtract (final long m0,
                                            final int upShift,
                                            final long m1) {
    assert 0L<=m0;
    final int[] u = Bei0.subtract(Bei0.valueOf(m0,upShift),m1);
    return unsafe(u); }

  //--------------------------------------------------------------
  // only when this <= (m << upShift)

  @Override
  public final NaturalBEI0 subtractFrom (final long m,
                                         final int upShift) {
    assert 0L<=m;
    final int[] ms = Bei0.shiftUp(m,upShift);
    final int[] u = Bei0.subtract(ms,_words);
    return unsafe(u); }

  public static final NaturalBEI0 subtractFrom (final long m0,
                                                final long m1,
                                                final int upShift) {
    assert 0L<=m1;
    final int[] ms = Bei0.shiftUp(m1,upShift);
    final int[] u = Bei0.subtract(ms,Bei0.valueOf(m0));
    return unsafe(u); }

  // only when this <= m

  @Override
  public final NaturalBEI0 subtractFrom (final long m) {
    assert 0L<=m;
    final int[] u = Bei0.subtract(m,_words);
    return unsafe(u); }

  //--------------------------------------------------------------

  @Override
  public final Natural absDiff (final Natural u) {
    final int c01 = compareTo(u);
    if (0<c01) { return subtract(u); }
    if (0>c01) { return u.subtract(this); }
    return ZERO; }

  //--------------------------------------------------------------

  @Override
  public final boolean isOne () { return equals(ONE); }

  //--------------------------------------------------------------

  public static final NaturalBEI0 multiply (final long t0,
                                            final long t1) {
    assert 0L<=t0;
    assert 0L<=t1;
    final long hi0 = Numbers.hiWord(t0);
    final long lo0 = loWord(t0);
    final long hi1 = Numbers.hiWord(t1);
    final long lo1 = loWord(t1);
    final long lolo = lo0*lo1;
    final long hilo2 = (hi0*lo1) + (hi1*lo0);
    final long hihi = hi0*hi1;
    long sum = lolo;
    final int m3 = (int) sum;
    sum = (sum >>> 32) + hilo2;
    final int m2 = (int) sum;
    sum = (sum >>> 32) + hihi ;
    final int m1 = (int) sum;
    final int m0 = (int) (sum >>> 32);
    if (0!=m0) { return unsafe(new int[] { m0,m1,m2,m3, }); }
    if (0!=m1) { return unsafe(new int[] { m1,m2,m3, }); }
    if (0!=m2) { return unsafe(new int[] { m2,m3, }); }
    if (0!=m3) { return unsafe(new int[] { m3, }); }
    return ZERO; }

  public static final NaturalBEI0 square (final long t) {
    assert 0L<=t;
    final long hi = Numbers.hiWord(t);
    final long lo = loWord(t);
    final long lolo = lo*lo;
    final long hilo2 = (hi*lo) << 1;
    final long hihi = hi*hi;
    final int[] m = new int[4];
    long sum = lolo;
    m[3] = (int) sum;
    sum = (sum >>> 32) + hilo2;
    m[2] = (int) sum;
    sum = (sum >>> 32) + hihi ;
    m[1] = (int) sum;
    m[0] = (int) (sum >>> 32);
    return unsafe(Bei0.stripLeadingZeros(m)); }

  @Override
  public final NaturalBEI0 square () {
    if (isZero()) { return ZERO; }
    if (this.isOne()) { return ONE; }
    return unsafe(Bei0.square(_words,false)); }

  @Override
  public final NaturalBEI0 multiply (final long that) {
    assert 1L<=that;
    return unsafe(Bei0.multiply(_words,that)); }

  // TODO: multiply by shifted long
  @Override
  public final Natural multiply (final long that,
                                 final int shift) {
    assert 1L<=that;
    return multiply(valueOf(that,shift)); }

  @Override
  public final Natural multiply (final Natural that) {
    return unsafe(
      Bei0.multiply(_words,((NaturalBEI0) that)._words)); }

  //--------------------------------------------------------------
  // Division
  //--------------------------------------------------------------

  private static final boolean
  useKnuthDivision (final NaturalBEI0 num,
                    final NaturalBEI0 den) {
    return Bei0.useKnuthDivision(num._words,den._words); }

  //--------------------------------------------------------------
  // Knuth algorithm
  //--------------------------------------------------------------

  private final NaturalBEI0
  divideKnuth (final NaturalBEI0 that) {
    final MutableNaturalBEI0 q = MutableNaturalBEI0.make();
    final MutableNaturalBEI0 num = MutableNaturalBEI0.valueOf(this._words);
    final MutableNaturalBEI0 den = MutableNaturalBEI0.valueOf(that._words);
    num.divideKnuth(den,q,false);
    return valueOf(q.getValue()); }

  private final NaturalBEI0[]
    divideAndRemainderKnuth (final NaturalBEI0 that) {
    final MutableNaturalBEI0 q = MutableNaturalBEI0.make();
    final MutableNaturalBEI0 num = MutableNaturalBEI0.valueOf(this._words);
    final MutableNaturalBEI0 den = MutableNaturalBEI0.valueOf(that._words);
    final MutableNaturalBEI0 r = num.divideKnuth(den,q,true);
    return new NaturalBEI0[]
      { valueOf(q.getValue()),
        valueOf(r.getValue()), }; }

  private final NaturalBEI0 remainderKnuth (final NaturalBEI0 that) {
    final MutableNaturalBEI0 q = MutableNaturalBEI0.make();
    final MutableNaturalBEI0 num = MutableNaturalBEI0.valueOf(this._words);
    final MutableNaturalBEI0 den = MutableNaturalBEI0.valueOf(that._words);
    final MutableNaturalBEI0 r = num.divideKnuth(den,q,true);
    return valueOf(r.getValue()); }

  //--------------------------------------------------------------

  private final NaturalBEI0[]
    divideAndRemainderBurnikelZiegler (final NaturalBEI0 that) {
    final MutableNaturalBEI0 q = MutableNaturalBEI0.make();
    final MutableNaturalBEI0 num = MutableNaturalBEI0.valueOf(this._words);
    final MutableNaturalBEI0 den = MutableNaturalBEI0.valueOf(that._words);
    final MutableNaturalBEI0 r =
      num.divideAndRemainderBurnikelZiegler(den,q);
    final NaturalBEI0 qq =
      q.isZero() ? ZERO : valueOf(q.getValue());
    final NaturalBEI0 rr =
      r.isZero() ? ZERO : valueOf(r.getValue());
    return new NaturalBEI0[] { qq, rr }; }

  private final NaturalBEI0
  divideBurnikelZiegler (final NaturalBEI0 that) {
    return divideAndRemainderBurnikelZiegler(that)[0]; }

  private final NaturalBEI0
  remainderBurnikelZiegler (final NaturalBEI0 that) {
    return divideAndRemainderBurnikelZiegler(that)[1]; }

  //--------------------------------------------------------------
  // division Ringlike api
  //--------------------------------------------------------------

  @Override
  public final Natural
  divide (final Natural that) {
    assert (! that.isZero());
    final NaturalBEI0 u = (NaturalBEI0) that;
    if (that.isOne()) { return this; }
    if (useKnuthDivision(this,u)) { return divideKnuth(u); }
    return divideBurnikelZiegler(u); }

  @Override
  public List<Natural>
  divideAndRemainder (final Natural that) {
    assert (! that.isZero());
    final NaturalBEI0 u = (NaturalBEI0) that;
    if (useKnuthDivision(this,u)) {
      return Arrays.asList(divideAndRemainderKnuth(u)); }
    return
      Arrays.asList(divideAndRemainderBurnikelZiegler(u)); }

  @Override
  public final Natural remainder (final Natural that) {
    assert (! that.isZero());
    final NaturalBEI0 u = (NaturalBEI0) that;
    if (useKnuthDivision(this,u)) {
      return remainderKnuth(u); }
    return remainderBurnikelZiegler(u); }

  //--------------------------------------------------------------
  // gcd
  //--------------------------------------------------------------

  @Override
  public final Natural gcd (final Natural that) {
    final MutableNaturalBEI0 a = MutableNaturalBEI0.valueOf(_words);
    final NaturalBEI0 u = (NaturalBEI0) that;
    final MutableNaturalBEI0 b = MutableNaturalBEI0.valueOf(u._words);
    final MutableNaturalBEI0 result = a.hybridGCD(b);
    return valueOf(result.getValue()); }

  // remove common factors as if numerator and denominator
  //  public static final NaturalBEI[] reduce (final NaturalBEI n0,
  //                                          final NaturalBEI d0) {
  //    final MutableNaturalBEI0[] nd =
  //      MutableNaturalBEI0.reduce(
  //        MutableNaturalBEI0.valueOf(n0._mag),
  //        MutableNaturalBEI0.valueOf(d0._mag));
  //    return new NaturalBEI[]
  //      { valueOf(nd[0].getValue()),
  //        valueOf(nd[1].getValue()), }; }

  public static final Natural[] reduce (final NaturalBEI0 n0,
                                        final NaturalBEI0 d0) {
    final int shift =
      Math.min(
        Bei0.getLowestSetBit(n0._words),
        Bei0.getLowestSetBit(d0._words));
    final NaturalBEI0 n = (shift != 0) ? n0.shiftDown(shift) : n0;
    final NaturalBEI0 d = (shift != 0) ? d0.shiftDown(shift) : d0;
    if (n.equals(d)) { return new NaturalBEI0[] { ONE, ONE, }; }
    if (d.isOne()) { return new NaturalBEI0[] { n, ONE, }; }
    if (n.isOne()) { return new NaturalBEI0[] { ONE, d, }; }
    final Natural gcd = n.gcd(d);
    if (gcd.compareTo(ONE) > 0) {
      return new Natural[] { n.divide(gcd), d.divide(gcd), }; }
    return new Natural[] { n, d, }; }

  //--------------------------------------------------------------
  // Bit Operations
  //--------------------------------------------------------------

  @Override
  public final NaturalBEI0 shiftUp (final int n) {
    assert 0<=n;
    return unsafe(Bei0.shiftUp(_words,n)); }

  @Override
  public final NaturalBEI0 shiftDown (final int n) {
    assert 0<=n;
    return unsafe(Bei0.shiftDown(_words,n)); }

  // get the least significant int words of (m >>> shift)

  @Override
  public final int getShiftedInt (final int n) {
    assert 0<=n;
    return Bei0.getShiftedInt(_words,n); }

  // get the least significant two int words of (m >>> shift) as a
  // long

  @Override
  public final long getShiftedLong (final int n) {
    assert 0<=n;
    return Bei0.getShiftedLong(_words,n); }

  @Override
  public final boolean testBit (final int n) {
    return Bei0.testBit(_words,n); }

  @Override
  public final NaturalBEI0 setBit (final int n) {
    return unsafe(Bei0.setBit(_words,n)); }

  @Override
  public final NaturalBEI0 clearBit (final int n) {
    return unsafe(Bei0.clearBit(_words,n)); }

  @Override
  public final NaturalBEI0 flipBit (final int n) {
    return unsafe(Bei0.flipBit(_words,n)); }

  @Override
  public final int loBit () {
    return Bei0.getLowestSetBit(_words); }

  @Override
  public final int hiBit () { return Bei0.bitLength(_words); }

  public final int bitCount () { return Bei0.bitCount(_words); }

  //--------------------------------------------------------------
  // Comparable interface+
  //--------------------------------------------------------------

  @Override
  public final int compareTo (final Natural that) {
    final NaturalBEI0 u = (NaturalBEI0) that;
    return Bei0.compare(_words,u._words); }

  @Override
  public final int compareTo (final int upShift,
                              final Natural that) {
    return shiftUp(upShift).compareTo(that); }

  @Override
  public final int compareTo (final long y) {
    assert 0L<=y;
    return Bei0.compare(_words,y); }

  @Override
  public final int compareTo (final long that,
                              final int upShift) {
    assert 0L<=that;
    return Bei0.compare(_words,that,upShift); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public int hashCode () {
    int hashCode = 0;
    for (final int element : _words) {
      hashCode = (int) ((31 * hashCode) + unsigned(element)); }
    return hashCode; }

  @Override
  public boolean equals (final Object x) {
    if (x==this) { return true; }
    if (!(x instanceof NaturalBEI0)) { return false; }
    final NaturalBEI0 xInt = (NaturalBEI0) x;
    final int[] m = _words;
    final int len = m.length;
    final int[] xm = xInt._words;
    if (len != xm.length) { return false; }
    for (int i = 0; i < len; i++) {
      if (xm[i] != m[i]) { return false; } }
    return true; }

  /** hex string. */
  @Override
  public String toString () { return Debug.toHexString(_words); }

  //  /** hex string. */
  //  @Override
  //  public String toString (final int radix) {
  //    assert radix==0x10;
  //    return Debug.toHexString(_words); }

  //--------------------------------------------------------------
  // Number interface+
  //--------------------------------------------------------------

  @Override
  public final byte[] toByteArray () {
    return Bei0.toByteArray(_words); }

  @Override
  public final BigInteger bigIntegerValue () {
    return Bei0.bigIntegerValue(_words); }

  @Override
  public final int intValue () { return Bei0.intValue(_words); }

  @Override
  public final long longValue () { return Bei0.longValue(_words); }

  //--------------------------------------------------------------

  @Override
  public final float floatValue () {
    return Bei0.floatValue(_words); }

  @Override
  public final double doubleValue () {
    return Bei0.doubleValue(_words); }

  //--------------------------------------------------------------
  // construction
  //-------------------------------------------------------------

  private NaturalBEI0 (final int[] mag) { _words = mag; }

  // assume no leading zeros
  public static final NaturalBEI0 unsafe (final int[] m) {
    return new NaturalBEI0(m); }

  // TODO: m may leak into NaturalBEI without copy!!
  public static final NaturalBEI0 valueOf (final int[] m) {
    final int[] m1 = Bei0.stripLeadingZeros(m);
    return new NaturalBEI0(m1); }

  public static final NaturalBEI0 valueOf (final byte[] b,
                                           final int off,
                                           final int len) {
    return unsafe(Bei0.stripLeadingZeros(b,off,len)); }

  public static final NaturalBEI0 valueOf (final byte[] b) {
    return valueOf(b,0,b.length); }

  public static final NaturalBEI0 valueOf (final BigInteger bi) {
    return valueOf(bi.toByteArray()); }

  //-------------------------------------------------------------

  public static final NaturalBEI0 valueOf (final String s,
                                           final int radix) {
    return unsafe(Bei0.toInts(s,radix)); }

  public static final NaturalBEI0 valueOf (final String s) {
    return valueOf(s,0x10); }

  //--------------------------------------------------------------
  // cached values
  //--------------------------------------------------------------

  private static final int MAX_CONSTANT = 16;
  private static final NaturalBEI0 posConst[] =
    new NaturalBEI0[MAX_CONSTANT+1];

  private static volatile NaturalBEI0[][] powerCache;

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
    powerCache = new NaturalBEI0[Character.MAX_RADIX + 1][];
    logCache = new double[Character.MAX_RADIX + 1];
    for (
      int i = Character.MIN_RADIX;
      i <= Character.MAX_RADIX;
      i++) {
      powerCache[i] = new NaturalBEI0[] { NaturalBEI0.valueOf(i) };
      logCache[i] = Math.log(i); } }

  public static final NaturalBEI0 ZERO = new NaturalBEI0(Bei0.ZERO);
  public static final NaturalBEI0 ONE = valueOf(1);
  public static final NaturalBEI0 TWO = valueOf(2);
  public static final NaturalBEI0 TEN = valueOf(10);

  //--------------------------------------------------------------

  public static final NaturalBEI0 valueOf (final long x) {
    if (x==0) { return ZERO; }
    assert 0L < x;
    if (x <= MAX_CONSTANT) { return posConst[(int) x]; }
    return unsafe(Bei0.valueOf(x)); }

  //--------------------------------------------------------------

  public static final NaturalBEI0 valueOf (final long x,
                                           final int upShift) {
    if (0L==x) { return ZERO; }
    assert 0L < x;
    return unsafe(Bei0.shiftUp(x,upShift)); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

