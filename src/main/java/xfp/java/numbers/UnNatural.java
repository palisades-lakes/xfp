package xfp.java.numbers;

import static xfp.java.numbers.Bei.compare;
import static xfp.java.numbers.Numbers.unsigned;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/** immutable arbitrary-precision non-negative integers.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-05-27
 */

public final class UnNatural extends Number
implements Ringlike<UnNatural> {

  private static final long serialVersionUID = 1L;

  private final int[] _mag;

  public final boolean isZero () { return 0 == _mag.length; }

  /** This constant limits {@code mag.length} of Naturals to
   * the supported range.
   */
  private static final int MAX_MAG_LENGTH =
    (Integer.MAX_VALUE / Integer.SIZE) + 1; // (1 << 26)

  //--------------------------------------------------------------
  // arithmetic
  //--------------------------------------------------------------

  @Override
  public final UnNatural add (final UnNatural m) {
    //if (m.isZero()) { return this; }
    //if (isZero()) { return m; }
    return unsafe(Bei.add(_mag,m._mag)); }

  public final UnNatural add (final long m) {
    assert 0L <= m;
    if (0L == m) { return this; }
    //if (isZero()) { return valueOf(m); }
    return unsafe(Bei.add(_mag,m)); }

  public final UnNatural add (final long m,
                              final int shift) {
    //if (isZero()) { return unsafe(Bei.shiftLeft(m,shift)); }
    return unsafe(Bei.add(_mag,m,shift)); }

  // only when val <= this
  public final UnNatural subtract (final long m) {
    assert 0L <= m;
    if (0L == m) { return this; }
    //final int c = compareTo(m);
    //assert 0 <= c;
    //if (0 == c) { return ZERO; }
    return unsafe(Bei.subtract(_mag,m)); }

  // only when val <= this
  @Override
  public final UnNatural subtract (final UnNatural m) {
    //    if (m.isZero()) { return this; }
    //    final int c = compareTo(m);
    //    assert 0L <= c;
    //    if (c == 0) { return ZERO; }
    return unsafe(Bei.subtract(_mag,m._mag)); }

  // only when (m << leftShift) <= this
  public final UnNatural subtract (final long m,
                                   final int leftShift) {
    assert 0L <= m;
    if (0L == m) { return this; }
    return unsafe(Bei.subtract(_mag,m,leftShift)); }

  //--------------------------------------------------------------
  // only when this <= (m << leftShift)

  public final UnNatural subtractFrom (final long m,
                                       final int leftShift) {
    assert 0L <= m;
    if (0L == m) { assert isZero(); return ZERO; }
    //if (isZero()) { return make(Bei.shiftLeft(m,leftShift)); }
    return unsafe(Bei.subtract(Bei.shiftLeft(m,leftShift),_mag)); }

  // only when this <= m
  
  public final UnNatural subtractFrom (final long m) {
    assert 0L <= m;
    if (0L == m) { assert isZero(); return ZERO; }
    //if (isZero()) { return make(m); }
    return unsafe(Bei.subtract(m,_mag)); }

  //--------------------------------------------------------------

  public final UnNatural multiply (final long that) {
    if (ONE.equals(this)) { return UnNatural.valueOf(that); }
    return unsafe(Bei.multiply(_mag,that)); }

  // TODO: multiply by shifted long
  public final UnNatural multiply (final long that,
                                   final int shift) {
    if (ONE.equals(this)) { 
      return UnNatural.valueOf(that,shift); }
    return multiply(UnNatural.valueOf(that,shift)); }

  @Override
  public final UnNatural multiply (final UnNatural that) {
    if (ONE.equals(this)) { return that; }
    if (ONE.equals(that)) { return this; }
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
    if (useKnuthDivision(this,that)) { return divideKnuth(that); }
    return divideBurnikelZiegler(that); }

  @Override
  public List<UnNatural> 
  divideAndRemainder (final UnNatural that) {
    if (useKnuthDivision(this,that)) {
      return Arrays.asList(divideAndRemainderKnuth(that)); }
    return 
      Arrays.asList(divideAndRemainderBurnikelZiegler(that)); }

  @Override
  public final UnNatural remainder (final UnNatural that) {
    if (useKnuthDivision(this,that)) {
      return remainderKnuth(that); }
    return remainderBurnikelZiegler(that); }

  //--------------------------------------------------------------
  // gcd
  //--------------------------------------------------------------

  @Override
  public final UnNatural gcd (final UnNatural that) {
    if (that.isZero()) { return this; }
    else if (isZero()) { return that; }
    final MutableUnNatural a = MutableUnNatural.valueOf(_mag);
    final MutableUnNatural b = MutableUnNatural.valueOf(that._mag);
    final MutableUnNatural result = a.hybridGCD(b);
    return valueOf(result.getValue()); }


  //--------------------------------------------------------------
  // Bit Operations
  //--------------------------------------------------------------

  public final UnNatural shiftLeft (final int n) {
    //if (isZero()) { return ZERO; }
    return make(Bei.shiftLeft(_mag,n)); }

  public final UnNatural shiftRight (final int n) {
    //if (isZero()) { return ZERO; }
    return make(Bei.shiftRight(_mag,n)); }

  public final boolean testBit (final int n) {
    return Bei.testBit(_mag,n); }

  public final UnNatural setBit (final int n) {
    return make(Bei.setBit(_mag,n)); }

  public final UnNatural clearBit (final int n) {
    return make(Bei.clearBit(_mag,n)); }

  public final UnNatural flipBit (final int n) {
    return make(Bei.flipBit(_mag,n)); }

  public final int getLowestSetBit () {
    return Bei.getLowestSetBit(_mag); }

  public final int bitLength () {
    return Bei.bitLength(_mag); }

  public final int bitCount () {
    return Bei.bitCount(_mag); }

  //--------------------------------------------------------------
  // Comparable interface+
  //--------------------------------------------------------------

  @Override
  public final int compareTo (final UnNatural val) {
    return compare(_mag,val._mag); }

  public final int compareTo (final long val) {
    if (val < 0L) { return -1; }
    if (val == 0L) {
      if (isZero()) { return 0; }
      return -1; }
    if (isZero()) { return -1; }
    return compare(_mag,val); }

  public final int compareTo (final long y,
                              final int leftShift) {
    assert 0L <= y;
    return Bei.compare(_mag,y,leftShift); }

  //--------------------------------------------------------------

  public final UnNatural min (final UnNatural val) {
    return (compareTo(val) < 0 ? this : val); }

  public final UnNatural max (final UnNatural val) {
    return (compareTo(val) > 0 ? this : val); }

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
    if (x == this) { return true; }
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
    assert radix == 0x10;
    return Bei.toHexString(_mag); }

  //--------------------------------------------------------------
  // Number interface+
  //--------------------------------------------------------------

  public final byte[] toByteArray () {
    return Bei.toByteArray(_mag); }

  public final BigInteger bigIntegerValue () {
    return Bei.bigIntegerValue(_mag); }

  @Override
  public final int intValue () {
    return Bei.intValue(_mag); }

  @Override
  public final long longValue () {
    return Bei.longValue(_mag); }

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

  private static final void checkMagnitude (final int[] m) {
    assert m.length < MAX_MAG_LENGTH; }

  //-------------------------------------------------------------

  private UnNatural (final int[] mag) { _mag = mag; }

  private static final UnNatural make (final int[] m) {
    final int[] m1 = Bei.stripLeadingZeros(m);
    checkMagnitude(m1);
    return new UnNatural(m1); }

  // assume no leading zeros
  private static final UnNatural unsafe (final int[] m) {
    checkMagnitude(m);
    return new UnNatural(m); }

  public static final UnNatural valueOf (final int[] m) {
    return make(Arrays.copyOf(m,m.length)); }

  public static final UnNatural valueOf (final byte[] b,
                                         final int off,
                                         final int len) {
    return make(Bei.stripLeadingZeros(b,off,len)); }

  public static final UnNatural valueOf (final byte[] b) {
    return valueOf(b,0,b.length); }

  public static final UnNatural valueOf (final BigInteger bi) {
    return valueOf(bi.toByteArray()); }

  //-------------------------------------------------------------

  public static final UnNatural valueOf (final String s,
                                         final int radix) {
    return make(Bei.valueOf(s,radix)); }

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
      posConst[i] = make(magnitude); }
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

  public static final UnNatural valueOf (final long val) {
    assert 0 <= val;
    if (val == 0) { return ZERO; }
    if ((val > 0) && (val <= MAX_CONSTANT)) {
      return posConst[(int) val]; }
    return make(Bei.valueOf(val)); }

  //--------------------------------------------------------------

  public static final UnNatural valueOf (final long x,
                                         final int leftShift) {
    if (0L == x) { return ZERO; }
    assert 0L < x;
    return make(Bei.shiftLeft(x,leftShift)); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

