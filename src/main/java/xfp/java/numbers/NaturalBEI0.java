package xfp.java.numbers;

import static xfp.java.numbers.Numbers.hiWord;
import static xfp.java.numbers.Numbers.loWord;
import static xfp.java.numbers.Numbers.unsigned;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/** immutable arbitrary-precision non-negative integers
 * (natural number) represented by big-endian 
 * unsigned <code>int[]</code>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-06-18
 */

public final class NaturalBEI0 extends Number
implements Ringlike<NaturalBEI0> {

  private static final long serialVersionUID = 1L;

  private final int[] _mag;
  public final int[] copyWords () { 
    return Arrays.copyOf(_mag,_mag.length); }

  public final boolean isZero () { return 0==_mag.length; }

  //--------------------------------------------------------------
  // arithmetic
  //--------------------------------------------------------------

  @Override
  public final NaturalBEI0 add (final NaturalBEI0 m) {
    return unsafe(Bei0.add(_mag,m._mag)); }

  public final NaturalBEI0 add (final long m) {
    assert 0L<=m;
    return unsafe(Bei0.add(_mag,m)); }

  public static final NaturalBEI0 add (final long t0,
                                       final long t1,
                                       final int bitShift) {
    assert 0L<=t0;
    assert 0L<=t1;
    assert 0<=bitShift;
    final int[] u = Bei0.add(t0,t1,bitShift);
    return unsafe(u); }

  public final NaturalBEI0 add (final long m,
                                final int shift) {
    assert 0L<=m;
    final int[] u = Bei0.add(_mag,m,shift);
    return unsafe(u); }

  //--------------------------------------------------------------
  // only when val <= this

  public final NaturalBEI0 subtract (final long m) {
    assert 0L<=m;
    final int[] u = Bei0.subtract(_mag,m);
    return unsafe(u); }

  // only when val <= this

  @Override
  public final NaturalBEI0 subtract (final NaturalBEI0 m) {
    return unsafe(Bei0.subtract(_mag,m._mag)); }

  // only when (m << leftShift) <= this
  public final NaturalBEI0 subtract (final long m,
                                     final int leftShift) {
    assert 0L<=m;
    final int[] u = Bei0.subtract(_mag,m,leftShift);
    return unsafe(u); }

  // only when (m1 << leftShift) <= m0
  public static final NaturalBEI0 subtract (final long m0,
                                            final long m1,
                                            final int leftShift) {
    assert 0L<=m0;
    assert 0L<=m1;
    assert 0<=leftShift;
    final int[] u = Bei0.subtract(m0,m1,leftShift);
    return unsafe(u); }

  // only when (m1 << leftShift) <= m0
  public static final NaturalBEI0 subtract (final long m0,
                                            final int leftShift,
                                            final long m1) {
    assert 0L<=m0;
    final int[] u = Bei0.subtract(Bei0.valueOf(m0,leftShift),m1);
    return unsafe(u); }

  //--------------------------------------------------------------
  // only when this <= (m << leftShift)

  public final NaturalBEI0 subtractFrom (final long m,
                                         final int leftShift) {
    assert 0L<=m;
    final int[] ms = Bei0.shiftLeft(m,leftShift);
    final int[] u = Bei0.subtract(ms,_mag);
    return unsafe(u); }

  public static final NaturalBEI0 subtractFrom (final long m0,
                                                final long m1,
                                                final int leftShift) {
    assert 0L<=m1;
    final int[] ms = Bei0.shiftLeft(m1,leftShift);
    final int[] u = Bei0.subtract(ms,Bei0.valueOf(m0));
    return unsafe(u); }

  // only when this <= m

  public final NaturalBEI0 subtractFrom (final long m) {
    assert 0L<=m;
    final int[] u = Bei0.subtract(m,_mag);
    return unsafe(u); }

  //--------------------------------------------------------------

  public static final NaturalBEI0 absDifference (final NaturalBEI0 u0,
                                                 final NaturalBEI0 u1) {
    final int c01 = u0.compareTo(u1);
    if (0<c01) { return u0.subtract(u1); }
    if (0>c01) { return u1.subtract(u0); }
    return ZERO; }

  //--------------------------------------------------------------

  public static final NaturalBEI0 multiply (final long t0,
                                            final long t1) {
    assert 0L<=t0;
    assert 0L<=t1;
    final long hi0 = hiWord(t0);
    final long lo0 = loWord(t0);
    final long hi1 = hiWord(t1);
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
    final long hi = hiWord(t);
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

  public final NaturalBEI0 square () {
    if (isZero()) { return ZERO; }
    if (ONE.equals(this)) { return ONE; }
    return unsafe(Bei0.square(_mag,false)); }

  public final NaturalBEI0 multiply (final long that) {
    assert 1L<=that;
    return unsafe(Bei0.multiply(_mag,that)); }

  // TODO: multiply by shifted long
  public final NaturalBEI0 multiply (final long that,
                                     final int shift) {
    assert 1L<=that;
    return multiply(valueOf(that,shift)); }

  @Override
  public final NaturalBEI0 multiply (final NaturalBEI0 that) {
    return unsafe(Bei0.multiply(_mag,that._mag)); }

  //--------------------------------------------------------------
  // Division
  //--------------------------------------------------------------

  private static final boolean 
  useKnuthDivision (final NaturalBEI0 num,
                    final NaturalBEI0 den) {
    return Bei0.useKnuthDivision(num._mag,den._mag); }

  //--------------------------------------------------------------
  // Knuth algorithm
  //--------------------------------------------------------------

  private final NaturalBEI0 
  divideKnuth (final NaturalBEI0 that) {
    final MutableNaturalBEI0 q = MutableNaturalBEI0.make();
    final MutableNaturalBEI0 num = MutableNaturalBEI0.valueOf(this._mag);
    final MutableNaturalBEI0 den = MutableNaturalBEI0.valueOf(that._mag);
    num.divideKnuth(den,q,false);
    return valueOf(q.getValue()); }

  private final NaturalBEI0[] 
    divideAndRemainderKnuth (final NaturalBEI0 that) {
    final MutableNaturalBEI0 q = MutableNaturalBEI0.make();
    final MutableNaturalBEI0 num = MutableNaturalBEI0.valueOf(this._mag);
    final MutableNaturalBEI0 den = MutableNaturalBEI0.valueOf(that._mag);
    final MutableNaturalBEI0 r = num.divideKnuth(den,q,true);
    return new NaturalBEI0[] 
      { valueOf(q.getValue()),
        valueOf(r.getValue()), }; }

  private final NaturalBEI0 remainderKnuth (final NaturalBEI0 that) {
    final MutableNaturalBEI0 q = MutableNaturalBEI0.make();
    final MutableNaturalBEI0 num = MutableNaturalBEI0.valueOf(this._mag);
    final MutableNaturalBEI0 den = MutableNaturalBEI0.valueOf(that._mag);
    final MutableNaturalBEI0 r = num.divideKnuth(den,q,true);
    return valueOf(r.getValue()); }

  //--------------------------------------------------------------

  private final NaturalBEI0[] 
    divideAndRemainderBurnikelZiegler (final NaturalBEI0 that) {
    final MutableNaturalBEI0 q = MutableNaturalBEI0.make();
    final MutableNaturalBEI0 num = MutableNaturalBEI0.valueOf(this._mag);
    final MutableNaturalBEI0 den = MutableNaturalBEI0.valueOf(that._mag);
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
  public final NaturalBEI0 
  divide (final NaturalBEI0 that) {
    assert (! that.isZero());
    if (ONE.equals(that)) { return this; }
    if (useKnuthDivision(this,that)) { return divideKnuth(that); }
    return divideBurnikelZiegler(that); }

  @Override
  public List<NaturalBEI0> 
  divideAndRemainder (final NaturalBEI0 that) {
    assert (! that.isZero());
    if (useKnuthDivision(this,that)) {
      return Arrays.asList(divideAndRemainderKnuth(that)); }
    return 
      Arrays.asList(divideAndRemainderBurnikelZiegler(that)); }

  @Override
  public final NaturalBEI0 remainder (final NaturalBEI0 that) {
    assert (! that.isZero());
    if (useKnuthDivision(this,that)) {
      return remainderKnuth(that); }
    return remainderBurnikelZiegler(that); }

  //--------------------------------------------------------------
  // gcd
  //--------------------------------------------------------------

  @Override
  public final NaturalBEI0 gcd (final NaturalBEI0 that) {
    final MutableNaturalBEI0 a = MutableNaturalBEI0.valueOf(_mag);
    final MutableNaturalBEI0 b = MutableNaturalBEI0.valueOf(that._mag);
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

  public static final NaturalBEI0[] reduce (final NaturalBEI0 n0,
                                            final NaturalBEI0 d0) {
    final int shift = 
      Math.min(
        Bei0.getLowestSetBit(n0._mag),
        Bei0.getLowestSetBit(d0._mag));
    final NaturalBEI0 n = (shift != 0) ? n0.shiftRight(shift) : n0;
    final NaturalBEI0 d = (shift != 0) ? d0.shiftRight(shift) : d0;
    if (n.equals(d)) { 
      return new NaturalBEI0[] { ONE, ONE, }; }
    if (NaturalBEI0.ONE.equals(d)) { 
      return new NaturalBEI0[] { n, ONE, }; }
    if (NaturalBEI0.ONE.equals(n)) {
      return new NaturalBEI0[] { ONE, d, }; }
    final NaturalBEI0 gcd = n.gcd(d);
    if (gcd.compareTo(ONE) > 0) {
      return new NaturalBEI0[] { n.divide(gcd), d.divide(gcd), }; } 
    return new NaturalBEI0[] { n, d, }; }

  //--------------------------------------------------------------
  // Bit Operations
  //--------------------------------------------------------------

  public final NaturalBEI0 shiftLeft (final int n) {
    assert 0<=n;
    return unsafe(Bei0.shiftLeft(_mag,n)); }

  public final NaturalBEI0 shiftRight (final int n) {
    assert 0<=n;
    return unsafe(Bei0.shiftRight(_mag,n)); }

  // get the least significant int words of (m >>> shift)

  public final int getShiftedInt (final int n) {
    assert 0<=n;
    return Bei0.getShiftedInt(_mag,n); }

  // get the least significant two int words of (m >>> shift) as a
  // long

  public final long getShiftedLong (final int n) {
    assert 0<=n;
    return Bei0.getShiftedLong(_mag,n); }

  public final boolean testBit (final int n) {
    return Bei0.testBit(_mag,n); }

  public final NaturalBEI0 setBit (final int n) {
    return unsafe(Bei0.setBit(_mag,n)); }

  public final NaturalBEI0 clearBit (final int n) {
    return unsafe(Bei0.clearBit(_mag,n)); }

  public final NaturalBEI0 flipBit (final int n) {
    return unsafe(Bei0.flipBit(_mag,n)); }

  public final int getLowestSetBit () {
    return Bei0.getLowestSetBit(_mag); }

  public final int loBit () { return getLowestSetBit(); }

  public final int bitLength () { return Bei0.bitLength(_mag); }

  public final int hiBit () { return bitLength(); }

  public final int bitCount () { return Bei0.bitCount(_mag); }

  //--------------------------------------------------------------
  // Comparable interface+
  //--------------------------------------------------------------

  @Override
  public final int compareTo (final NaturalBEI0 y) {
    return Bei0.compare(_mag,y._mag); }

  public final int compareTo (final int leftShift,
                              final NaturalBEI0 y) {
    return shiftLeft(leftShift).compareTo(y); }

  public final int compareTo (final long y) {
    assert 0L<=y;
    return Bei0.compare(_mag,y); }

  public final int compareTo (final long y,
                              final int leftShift) {
    assert 0L<=y;
    return Bei0.compare(_mag,y,leftShift); }

  //--------------------------------------------------------------

  public final NaturalBEI0 min (final NaturalBEI0 that) {
    return (compareTo(that) < 0 ? this : that); }

  public final NaturalBEI0 max (final NaturalBEI0 that) {
    return (compareTo(that) > 0 ? this : that); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public int hashCode () {
    int hashCode = 0;
    for (final int element : _mag) {
      hashCode = (int) ((31 * hashCode) + unsigned(element)); }
    return hashCode; }

  @Override
  public boolean equals (final Object x) {
    if (x==this) { return true; }
    if (!(x instanceof NaturalBEI0)) { return false; }
    final NaturalBEI0 xInt = (NaturalBEI0) x;
    final int[] m = _mag;
    final int len = m.length;
    final int[] xm = xInt._mag;
    if (len != xm.length) { return false; }
    for (int i = 0; i < len; i++) {
      if (xm[i] != m[i]) { return false; } }
    return true; }

  /** hex string. */
  @Override
  public String toString () { return Bei0.toHexString(_mag); }

  /** hex string. */
  @Override
  public String toString (final int radix) { 
    assert radix==0x10;
    return Bei0.toHexString(_mag); }

  //--------------------------------------------------------------
  // Number interface+
  //--------------------------------------------------------------

  public final byte[] toByteArray () {
    return Bei0.toByteArray(_mag); }

  public final BigInteger bigIntegerValue () {
    return Bei0.bigIntegerValue(_mag); }

  @Override
  public final int intValue () { return Bei0.intValue(_mag); }

  @Override
  public final long longValue () { return Bei0.longValue(_mag); }

  //--------------------------------------------------------------

  @Override
  public final float floatValue () {
    return Bei0.floatValue(_mag); }

  @Override
  public final double doubleValue () {
    return Bei0.doubleValue(_mag); }

  //--------------------------------------------------------------
  // construction
  //-------------------------------------------------------------

  private NaturalBEI0 (final int[] mag) { _mag = mag; }

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
                                           final int leftShift) {
    if (0L==x) { return ZERO; }
    assert 0L < x;
    return unsafe(Bei0.shiftLeft(x,leftShift)); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

