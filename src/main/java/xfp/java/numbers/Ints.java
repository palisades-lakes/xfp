package xfp.java.numbers;

import static xfp.java.numbers.Numbers.loWord;
import static xfp.java.numbers.Numbers.unsigned;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.apache.commons.rng.UniformRandomProvider;

import xfp.java.algebra.OneSetOneOperation;
import xfp.java.algebra.Set;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;

/** Utilities for <code>int</code>, <code>int[]</code>.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-14
 */
public final class Ints implements Set {

  //--------------------------------------------------------------
  // int ops
  //--------------------------------------------------------------
  /** Divides a long by an (unsigned) int.
   * Returns a long where high unsigned int is the remainder
   * and low unsigned int is the quotient.
   */

  public static final long divWord (final long n, 
                                    final long d) {
    //assert 0L<=n;
    assert 0L<d;
    if (d == 1L) { return loWord(n); }
    long q = (n>>>1) / (d>>>1);
    long r = n-(q*d);
    while (r<0L) { r+=d; q--; }
    while (r>=d) { r-=d; q++; }
    return (r<<32) | loWord(q); }

  //-------------------------------------------------------------
  // gcd
  //-------------------------------------------------------------
  /** a and b interpreted as unsigned integers.
   */

  public static final int unsignedGcd (int a, int b) {
    if (b == 0) { return a; }
    if (a == 0) { return b; }

    // Down shift a & b till their last bits equal to 1.
    final int aZeros = Integer.numberOfTrailingZeros(a);
    final int bZeros = Integer.numberOfTrailingZeros(b);
    a >>>= aZeros;
    b >>>= bZeros;

    final int t = (aZeros < bZeros ? aZeros : bZeros);

    while (a != b) {
      if ((a+0x80000000) > (b+0x80000000)) {  // a > b as unsigned
        a -= b;
        a >>>= Integer.numberOfTrailingZeros(a); }
      else {
        b -= a;
        b >>>= Integer.numberOfTrailingZeros(b); } }
    return a<<t; }

  //--------------------------------------------------------------
  // int[] ops
  // TODO: non-instantiable IntUtils?
  //--------------------------------------------------------------
  // TODO: is this worth anything vs new int[] everywhere?

  public static final int[] EMPTY = new int[0];

  public static final boolean isZero (final int[] z) {
    for (final int element : z) {
      if (0!=element) { return false; } }
    return true; }

  public static final int hiInt (final int[] x) {
    final int n = x.length;
    int k = -1;
    for (int i=0;i<n;i++) { if (0!=x[i]) { k = i; } }
    return k+1; }

  /** <em>DANGER:</em> may return <code>m</code> or a new array. 
   */
  public static final int[] stripLeadingZeros (final int[] m) {
    final int n = m.length;
    int start = 0;
    while ((start < n) && (m[start] == 0)) { start++; }
    return (0==start) ? m : Arrays.copyOfRange(m,start,n); }

  /** <em>DANGER:</em> may return <code>m</code> or a new array. 
   */
  public static final int[] stripTrailingZeros (final int[] m) {
    final int n = hiInt(m);
    return (m.length==n) ? m : Arrays.copyOfRange(m,0,n); }

  public static final void reverse (final int[] x) {
    // hotspot would probably create temps anyway
    final int n = x.length;
    final int n1 = n-1;
    for (int i=0;i<n/2;i++) {
      final int n1i = n1-i;
      final int t = x[n1i];
      x[n1i] = x[i];
      x[i] = t; } }

  //--------------------------------------------------------------

  public static final void copyAndShift (final int[] src,
                                         final int isrc,
                                         final int n,
                                         final int[] dst,
                                         final int idst,
                                         final int lShift) {
    assert 0<lShift;
    assert lShift<32;
      final int rShift = 32-lShift;
      int c=src[isrc];
      for (int i0=idst,i1=isrc;i0<idst+n-1; i0++) {
        final int b = c;
        c = src[++i1];
        dst[i0] = (b << lShift) | (c >>> rShift); }
      dst[idst+n-1] = (c<<lShift); } 

  //-------------------------------------------------------------
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

  public static final int[] bigEndian (final String s,
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

  // TODO: get rid of reverse call
  public static final int[] littleEndian (final String s,
                                          final int radix) {
    final int[] x = bigEndian(s,radix);
    reverse(x);
    return x; }

  //--------------------------------------------------------------

  public static final int[] bigEndian (final long u) {
    assert 0<=u;
    final int hi = (int) (u>>>32);
    final int lo = (int) u;
    if (0==hi) {
      if (0==lo) { return Ints.EMPTY; }
      return new int[] { lo, }; }
    return new int[] { hi, lo, }; }

  public static final int[] littleEndian (final long u) {
    assert 0<=u;
    final int hi = (int) (u>>>32);
    final int lo = (int) u;
    if (0==hi) {
      if (0==lo) { return Ints.EMPTY; }
      return new int[] { lo, }; }
    return new int[] { lo, hi, }; }

  //--------------------------------------------------------------
  // operations for algebraic structures over
  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  @SuppressWarnings("static-method")
  private final Integer add (final Integer q0,
                             final Integer q1) {
    assert null != q0;
    assert null != q1;
    return Integer.valueOf(q0.intValue() + q1.intValue()); }

  public final BinaryOperator<Integer> adder () {
    return new BinaryOperator<> () {
      @Override
      public final String toString () { return "D.add()"; }
      @Override
      public final Integer apply (final Integer q0,
                                  final Integer q1) {
        return Ints.this.add(q0,q1); } }; }

  //--------------------------------------------------------------

  private static final Integer ZERO = Integer.valueOf(0);

  @SuppressWarnings("static-method")
  public final Integer additiveIdentity () { return ZERO; }

  //--------------------------------------------------------------

  // TODO: is consistency with other algebraic structure classes
  // worth the indirection?

  @SuppressWarnings("static-method")
  private final Integer negate (final Integer q) {
    assert null != q;
    return  Integer.valueOf(- q.intValue()); }

  public final UnaryOperator<Integer> additiveInverse () {
    return new UnaryOperator<> () {
      @Override
      public final String toString () { return "D.negate()"; }
      @Override
      public final Integer apply (final Integer q) {
        return Ints.this.negate(q); } }; }

  //--------------------------------------------------------------

  @SuppressWarnings("static-method")
  private final Integer multiply (final Integer q0,
                                  final Integer q1) {
    assert null != q0;
    assert null != q1;
    return Integer.valueOf(q0.intValue() * q1.intValue()); }

  public final BinaryOperator<Integer> multiplier () {
    return new BinaryOperator<>() {
      @Override
      public final String toString () { return "D.multiply()"; }
      @Override
      public final Integer apply (final Integer q0,
                                  final Integer q1) {
        return Ints.this.multiply(q0,q1); } }; }

  //--------------------------------------------------------------

  private static final Integer ONE = Integer.valueOf(1);

  @SuppressWarnings("static-method")
  public final Integer multiplicativeIdentity () { return ONE; }

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    return element instanceof Integer; }

  @Override
  public final boolean contains (final int element) {
    return true; }

  //--------------------------------------------------------------
  // Integer.equals reduces both arguments before checking
  // numerator and denominators are equal.
  // Guessing our Integers are usually already reduced.
  // Try n0*d1 == n1*d0 instead
  // TODO: use BigInteger.bitLength() to decide
  // which method to use?

  @SuppressWarnings("static-method")
  public final boolean equals (final Integer q0,
                               final Integer q1) {
    assert null != q0;
    assert null != q1;
    return q0.equals(q1); }

  @Override
  public final BiPredicate equivalence () {
    return new BiPredicate<Integer,Integer>() {
      @Override
      public final boolean test (final Integer q0,
                                 final Integer q1) {
        return Ints.this.equals(q0,q1); } }; }

  //--------------------------------------------------------------

  @Override
  public final Supplier generator (final Map options) {
    final UniformRandomProvider urp = Set.urp(options);
    final Generator g = generator(urp);
    return
      new Supplier () {
      @Override
      public final Object get () { return g.next(); } }; }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { return 0; }

  // singleton
  @Override
  public final boolean equals (final Object that) {
    return that instanceof Ints; }

  @Override
  public final String toString () { return "D"; }

  //--------------------------------------------------------------

  public static final Generator
  generator (final UniformRandomProvider urp) {
    return Generators.intGenerator(urp); }

  public static final Generator
  generator (final int n,
             final UniformRandomProvider urp) {
    return Generators.intGenerator(n,urp); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private Ints () { }

  private static final Ints SINGLETON = new Ints();

  public static final Ints get () { return SINGLETON; }

  //--------------------------------------------------------------
  // pre-defined structures
  //--------------------------------------------------------------

  public static final OneSetOneOperation ADDITIVE_MAGMA =
    OneSetOneOperation.magma(get().adder(),get());

  public static final OneSetOneOperation MULTIPLICATIVE_MAGMA =
    OneSetOneOperation.magma(get().multiplier(),get());

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

