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
 * @version 2019-06-14
 */

public final class UnNatural extends Number
implements Ringlike<UnNatural> {

  private static final long serialVersionUID = 1L;

  private final int[] _mag;
  public final int[] magnitude () { 
    return Arrays.copyOf(_mag,_mag.length); }

  public final boolean isZero () { return 0==_mag.length; }

  //--------------------------------------------------------------
  // arithmetic
  //--------------------------------------------------------------

  @Override
  public final UnNatural add (final UnNatural m) {
    return unsafe(Bei.add(_mag,m._mag)); }

  public final UnNatural add (final UnNatural m,
                              final int leftShift) {
    return unsafe(Bei.add(_mag,m._mag,leftShift)); }

  public final UnNatural add (final long m) {
    assert 0L<=m;
    //if (0L==m) { return this; }
    //if (isZero()) { return valueOf(m); }
    return unsafe(Bei.add(_mag,m)); }

  public static final UnNatural add (final long t0,
                                     final long t1,
                                     final int bitShift) {
    assert 0L<=t0;
    assert 0L<=t1;
    assert 0<=bitShift;
    final int[] u = Bei.add(t0,t1,bitShift);
    return unsafe(u); }

  public final UnNatural add (final long m,
                              final int shift) {
    assert 0L<=m;
    final int[] u = Bei.add(_mag,m,shift);
    return unsafe(u); }

  //--------------------------------------------------------------
  // only when val <= this

  public final UnNatural subtract (final long m) {
    assert 0L<=m;
    final int[] u = Bei.subtract(_mag,m);
    return unsafe(u); }

  // only when val <= this
  
  @Override
  public final UnNatural subtract (final UnNatural m) {
    return unsafe(Bei.subtract(_mag,m._mag)); }

  // only when (m << leftShift) <= this
  public final UnNatural subtract (final long m,
                                   final int leftShift) {
    assert 0L<=m;
    final int[] u = Bei.subtract(_mag,m,leftShift);
    return unsafe(u); }

  // only when (m1 << leftShift) <= m0
  public static final UnNatural subtract (final long m0,
                                          final long m1,
                                          final int leftShift) {
    assert 0L<=m0;
    assert 0L<=m1;
    assert 0<=leftShift;
    final int[] u = Bei.subtract(m0,m1,leftShift);
    return unsafe(u); }

  // only when (m1 << leftShift) <= m0
  public static final UnNatural subtract (final long m0,
                                          final int leftShift,
                                          final long m1) {
    assert 0L<=m0;
    final int[] u = Bei.subtract(Bei.valueOf(m0,leftShift),m1);
    return unsafe(u); }

  //--------------------------------------------------------------
  // only when this <= (m << leftShift)

  public final UnNatural subtractFrom (final long m,
                                       final int leftShift) {
    assert 0L<=m;
    final int[] ms = Bei.shiftLeft(m,leftShift);
    final int[] u = Bei.subtract(ms,_mag);
    return unsafe(u); }

  public static final UnNatural subtractFrom (final long m0,
                                              final long m1,
                                              final int leftShift) {
    assert 0L<=m1;
    final int[] ms = Bei.shiftLeft(m1,leftShift);
    final int[] u = Bei.subtract(ms,Bei.valueOf(m0));
    return unsafe(u); }

  // only when this <= m

  public final UnNatural subtractFrom (final long m) {
    assert 0L<=m;
    final int[] u = Bei.subtract(m,_mag);
    return unsafe(u); }

  //--------------------------------------------------------------

  public static final UnNatural absDifference (final UnNatural u0,
                                               final UnNatural u1) {
    final int c01 = u0.compareTo(u1);
    if (0<c01) { return u0.subtract(u1); }
    if (0>c01) { return u1.subtract(u0); }
    return ZERO; }

  //--------------------------------------------------------------

  public static final UnNatural multiply (final long t0,
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

  public static final UnNatural square (final long t) {
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
    return unsafe(Bei.stripLeadingZeros(m)); }

  public final UnNatural square () {
    if (isZero()) { return ZERO; }
    if (ONE.equals(this)) { return ONE; }
    return unsafe(Bei.square(_mag,false)); }

  public final UnNatural multiply (final long that) {
    assert 1L<=that;
    return unsafe(Bei.multiply(_mag,that)); }

  // TODO: multiply by shifted long
  public final UnNatural multiply (final long that,
                                   final int shift) {
    assert 1L<=that;
    return multiply(valueOf(that,shift)); }

  @Override
  public final UnNatural multiply (final UnNatural that) {
    return unsafe(Bei.multiply(_mag,that._mag)); }

  //--------------------------------------------------------------
  // Division
  //--------------------------------------------------------------

  private static final boolean 
  useKnuthDivision (final UnNatural num,
                    final UnNatural den) {
    return Bei.useKnuthDivision(num._mag,den._mag); }

  //--------------------------------------------------------------
  // Knuth algorithm
  //--------------------------------------------------------------

  private final UnNatural 
  divideKnuth (final UnNatural that) {
    final MutableUnNatural q = MutableUnNatural.make();
    final MutableUnNatural num = MutableUnNatural.valueOf(this._mag);
    final MutableUnNatural den = MutableUnNatural.valueOf(that._mag);
    num.divideKnuth(den,q,false);
    return valueOf(q.getValue()); }

  private final UnNatural[] 
    divideAndRemainderKnuth (final UnNatural that) {
    final MutableUnNatural q = MutableUnNatural.make();
    final MutableUnNatural num = MutableUnNatural.valueOf(this._mag);
    final MutableUnNatural den = MutableUnNatural.valueOf(that._mag);
    final MutableUnNatural r = num.divideKnuth(den,q,true);
    return new UnNatural[] 
      { valueOf(q.getValue()),
        valueOf(r.getValue()), }; }

  private final UnNatural remainderKnuth (final UnNatural that) {
    final MutableUnNatural q = MutableUnNatural.make();
    final MutableUnNatural num = MutableUnNatural.valueOf(this._mag);
    final MutableUnNatural den = MutableUnNatural.valueOf(that._mag);
    final MutableUnNatural r = num.divideKnuth(den,q,true);
    return valueOf(r.getValue()); }

  //--------------------------------------------------------------

  private final UnNatural[] 
    divideAndRemainderBurnikelZiegler (final UnNatural that) {
    final MutableUnNatural q = MutableUnNatural.make();
    final MutableUnNatural num = MutableUnNatural.valueOf(this._mag);
    final MutableUnNatural den = MutableUnNatural.valueOf(that._mag);
    final MutableUnNatural r =
      num.divideAndRemainderBurnikelZiegler(den,q);
    final UnNatural qq = 
      q.isZero() ? ZERO : valueOf(q.getValue());
    final UnNatural rr = 
      r.isZero() ? ZERO : valueOf(r.getValue());
    return new UnNatural[] { qq, rr }; }

  private final UnNatural 
  divideBurnikelZiegler (final UnNatural that) {
    return divideAndRemainderBurnikelZiegler(that)[0]; }

  private final UnNatural 
  remainderBurnikelZiegler (final UnNatural that) {
    return divideAndRemainderBurnikelZiegler(that)[1]; }

  //--------------------------------------------------------------
  // division Ringlike api
  //--------------------------------------------------------------

  @Override
  public final UnNatural 
  divide (final UnNatural that) {
    assert (! that.isZero());
    if (ONE.equals(that)) { return this; }
    if (useKnuthDivision(this,that)) { return divideKnuth(that); }
    return divideBurnikelZiegler(that); }

  @Override
  public List<UnNatural> 
  divideAndRemainder (final UnNatural that) {
    assert (! that.isZero());
    if (useKnuthDivision(this,that)) {
      return Arrays.asList(divideAndRemainderKnuth(that)); }
    return 
      Arrays.asList(divideAndRemainderBurnikelZiegler(that)); }

  @Override
  public final UnNatural remainder (final UnNatural that) {
    assert (! that.isZero());
    if (useKnuthDivision(this,that)) {
      return remainderKnuth(that); }
    return remainderBurnikelZiegler(that); }

  //--------------------------------------------------------------
  // gcd
  //--------------------------------------------------------------

  @Override
  public final UnNatural gcd (final UnNatural that) {
    final MutableUnNatural a = MutableUnNatural.valueOf(_mag);
    final MutableUnNatural b = MutableUnNatural.valueOf(that._mag);
    final MutableUnNatural result = a.hybridGCD(b);
    return valueOf(result.getValue()); }

  // remove common factors as if numerator and denominator
  //  public static final UnNatural[] reduce (final UnNatural n0,
  //                                          final UnNatural d0) {
  //    final MutableUnNatural[] nd =
  //      MutableUnNatural.reduce(
  //        MutableUnNatural.valueOf(n0._mag),
  //        MutableUnNatural.valueOf(d0._mag));
  //    return new UnNatural[] 
  //      { valueOf(nd[0].getValue()), 
  //        valueOf(nd[1].getValue()), }; }

  public static final UnNatural[] reduce (final UnNatural n0,
                                          final UnNatural d0) {
    final int shift = 
      Math.min(Numbers.loBit(n0),Numbers.loBit(d0));
    final UnNatural n = (shift != 0) ? n0.shiftRight(shift) : n0;
    final UnNatural d = (shift != 0) ? d0.shiftRight(shift) : d0;
    if (n.equals(d)) { 
      return new UnNatural[] { ONE, ONE, }; }
    if (UnNatural.ONE.equals(d)) { 
      return new UnNatural[] { n, ONE, }; }
    if (UnNatural.ONE.equals(n)) {
      return new UnNatural[] { ONE, d, }; }
    final UnNatural gcd = n.gcd(d);
    if (gcd.compareTo(ONE) > 0) {
      return new UnNatural[] { n.divide(gcd), d.divide(gcd), }; } 
    return new UnNatural[] { n, d, }; }

  //--------------------------------------------------------------
  // Bit Operations
  //--------------------------------------------------------------

  public final UnNatural shiftLeft (final int n) {
    assert 0<=n;
    return unsafe(Bei.shiftLeft(_mag,n)); }

  public final UnNatural shiftRight (final int n) {
    assert 0<=n;
    return unsafe(Bei.shiftRight(_mag,n)); }

  // get the least significant int words of (m >>> shift)

  public final int getShiftedInt (final int n) {
    assert 0<=n;
    return Bei.getShiftedInt(_mag,n); }

  // get the least significant two int words of (m >>> shift) as a
  // long

  public final long getShiftedLong (final int n) {
    assert 0<=n;
    return Bei.getShiftedLong(_mag,n); }

  public final boolean testBit (final int n) {
    return Bei.testBit(_mag,n); }

  public final UnNatural setBit (final int n) {
    return unsafe(Bei.setBit(_mag,n)); }

  public final UnNatural clearBit (final int n) {
    return unsafe(Bei.clearBit(_mag,n)); }

  public final UnNatural flipBit (final int n) {
    return unsafe(Bei.flipBit(_mag,n)); }

  public final int getLowestSetBit () {
    return Bei.getLowestSetBit(_mag); }

  public final int loBit () { return getLowestSetBit(); }

  public final int bitLength () { return Bei.bitLength(_mag); }

  public final int hiBit () { return bitLength(); }

  public final int bitCount () { return Bei.bitCount(_mag); }

  //--------------------------------------------------------------
  // Comparable interface+
  //--------------------------------------------------------------

  @Override
  public final int compareTo (final UnNatural y) {
    return Bei.compare(_mag,y._mag); }

  public final int compareTo (final int leftShift,
                              final UnNatural y) {
    return shiftLeft(leftShift).compareTo(y); }

  public final int compareTo (final long y) {
    assert 0L<=y;
    return Bei.compare(_mag,y); }

  public final int compareTo (final long y,
                              final int leftShift) {
    assert 0L<=y;
    return Bei.compare(_mag,y,leftShift); }

  //--------------------------------------------------------------

  public final UnNatural min (final UnNatural that) {
    return (compareTo(that) < 0 ? this : that); }

  public final UnNatural max (final UnNatural that) {
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
    if (!(x instanceof UnNatural)) { return false; }
    final UnNatural xInt = (UnNatural) x;
    final int[] m = _mag;
    final int len = m.length;
    final int[] xm = xInt._mag;
    if (len != xm.length) { return false; }
    for (int i = 0; i < len; i++) {
      if (xm[i] != m[i]) { return false; } }
    return true; }

  /** hex string. */
  @Override
  public String toString () { return Bei.toHexString(_mag); }

  /** hex string. */
  @Override
  public String toString (final int radix) { 
    assert radix==0x10;
    return Bei.toHexString(_mag); }

  //--------------------------------------------------------------
  // Number interface+
  //--------------------------------------------------------------

  public final byte[] toByteArray () {
    return Bei.toByteArray(_mag); }

  public final BigInteger bigIntegerValue () {
    return Bei.bigIntegerValue(_mag); }

  @Override
  public final int intValue () { return Bei.intValue(_mag); }

  @Override
  public final long longValue () { return Bei.longValue(_mag); }

  //--------------------------------------------------------------

  @Override
  public final float floatValue () {
    return Bei.floatValue(_mag); }

  @Override
  public final double doubleValue () {
    return Bei.doubleValue(_mag); }

  //--------------------------------------------------------------
  // construction
  //-------------------------------------------------------------

  private UnNatural (final int[] mag) { _mag = mag; }

  // assume no leading zeros
  // TODO: change to implementation where leading zeros are ook
  public static final UnNatural unsafe (final int[] m) {
    return new UnNatural(m); }

  // assume no leading zeros
  public static final UnNatural valueOf (final int[] m) {
    return unsafe(Arrays.copyOf(m,m.length)); }

  public static final UnNatural valueOf (final byte[] b,
                                         final int off,
                                         final int len) {
    return unsafe(Bei.stripLeadingZeros(b,off,len)); }

  public static final UnNatural valueOf (final byte[] b) {
    return valueOf(b,0,b.length); }

  public static final UnNatural valueOf (final BigInteger bi) {
    return valueOf(bi.toByteArray()); }

  //-------------------------------------------------------------

  public static final UnNatural valueOf (final String s,
                                         final int radix) {
    return unsafe(Bei.valueOf(s,radix)); }

  public static final UnNatural valueOf (final String s) {
    return valueOf(s,0x10); }

  //--------------------------------------------------------------
  // cached values
  //--------------------------------------------------------------

  private static final int MAX_CONSTANT = 16;
  private static final UnNatural posConst[] =
    new UnNatural[MAX_CONSTANT+1];

  private static volatile UnNatural[][] powerCache;

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
    powerCache = new UnNatural[Character.MAX_RADIX + 1][];
    logCache = new double[Character.MAX_RADIX + 1];
    for (
      int i = Character.MIN_RADIX;
      i <= Character.MAX_RADIX;
      i++) {
      powerCache[i] = new UnNatural[] { UnNatural.valueOf(i) };
      logCache[i] = Math.log(i); } }

  public static final UnNatural ZERO = new UnNatural(Bei.ZERO);
  public static final UnNatural ONE = valueOf(1);
  public static final UnNatural TWO = valueOf(2);
  public static final UnNatural TEN = valueOf(10);

  //--------------------------------------------------------------

  public static final UnNatural valueOf (final long x) {
    if (x==0) { return ZERO; }
    assert 0L < x;
    if (x <= MAX_CONSTANT) { return posConst[(int) x]; }
    return unsafe(Bei.valueOf(x)); }

  //--------------------------------------------------------------

  public static final UnNatural valueOf (final long x,
                                         final int leftShift) {
    if (0L==x) { return ZERO; }
    assert 0L < x;
    return unsafe(Bei.shiftLeft(x,leftShift)); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

