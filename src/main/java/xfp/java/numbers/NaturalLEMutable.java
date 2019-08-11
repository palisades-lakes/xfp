package xfp.java.numbers;

import java.math.BigInteger;
import java.util.Arrays;

/** mutable arbitrary-precision non-negative integers
 * (natural numbers) represented by little-endian
 * unsigned <code>int[]</code>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-29
 */

public final class NaturalLEMutable implements Natural {

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private int[] _words;
  final int[] words () { 
    //assert isValid();
    return _words; }

  public final int[] copyWords () { 
    //assert isValid();
    return Arrays.copyOf(words(),words().length); }

  // TODO: grow faster?
  private final void expandTo (final int i) {
    //assert isValid();
    final int n = Math.max(i+1,hiInt());
    if (hiInt()<n) { _words = Arrays.copyOf(words(),n); } }

  //--------------------------------------------------------------
  // Natural
  //--------------------------------------------------------------

  @Override
  public final Natural fromSquare (final long t) {
    //assert isValid();
    //assert 0L<=t;
    final long hi = Numbers.hiWord(t);
    final long lo = Numbers.loWord(t);
    final long lolo = lo*lo;
    // TODO: overflow?
    //final long hilo2 = ((hi*lo)<<1);
    final long hilo2 = Math.multiplyExact(2,hi*lo);
    final long hihi = hi*hi;
    long sum = lolo;
    final int m0 = (int) sum;
    sum = (sum>>>32) + hilo2;
    final int m1 = (int) sum;
    sum = (sum>>>32) + hihi ;
    final int m2 = (int) sum;
    final int m3 = (int) (sum>>>32);
    return unsafe(new int[] {m0,m1,m2,m3,}); }

  //--------------------------------------------------------------

  @Override
  public final Natural product (final long t0,
                                final long t1) {
    //assert isValid();
    //assert 0L<=t0;
    //assert 0L<=t1;
    final long hi0 = Numbers.hiWord(t0);
    final long lo0 = Numbers.loWord(t0);
    final long hi1 = Numbers.hiWord(t1);
    final long lo1 = Numbers.loWord(t1);
    final long lolo = lo0*lo1;
    // TODO: overflow?
    //final long hilo2 = (hi0*lo1) + (hi1*lo0);
    final long hilo2 = Math.addExact(hi0*lo1,hi1*lo0);
    final long hihi = hi0*hi1;
    long sum = lolo;
    final int m0 = (int) sum;
    sum = (sum>>>32) + hilo2;
    final int m1 = (int) sum;
    sum = (sum>>>32) + hihi ;
    final int m2 = (int) sum;
    final int m3 = (int) (sum>>>32);
    return unsafe(new int[] {m0,m1,m2,m3,}); }

  //--------------------------------------------------------------

  @Override
  public final Natural add (final Natural u,
                            final int shift) {
    //assert isValid();
    //assert u.isValid();
    //assert 0<=shift;
    if (isZero()) { return u.shiftUp(shift); }
    if (u.isZero()) { return this; }
    if (0==shift) { return add(u); }
    final int iShift = (shift>>>5);
    final int bShift = (shift&0x1f);
    // TODO: special case whole word shift?
    final int rShift = 32-bShift;
    final int n0 = hiInt();
    final int n1 = u.hiInt()+iShift+1;
    final int n = Math.max(n0,n1);
    //final Natural us = u.shiftUp(shift);
    final int[] v = new int[n];
    int i=0;
    for (;i<iShift;i++) { v[i] = word(i); }
    long carry = 0L;
    int u0 = 0;
    for (;i<n;i++) {
      final int u1 = u.word(i-iShift);
      final int ui = 
        ((bShift==0) ? u1 : ((u1<<bShift)|(u0>>>rShift)));
      u0 = u1;
      final long sum = uword(i) + Numbers.unsigned(ui) + carry;
      carry = (sum>>>32);
      v[i] = (int) sum; }
    if (0L!=carry) { v[i] = (int) carry; }
    return unsafe(v); }

  //--------------------------------------------------------------

  @Override
  public final Natural add (final long u,
                            final int upShift) {
    //assert isValid();
    //assert 0<=u;
    //assert 0<=upShift;
    if (isZero()) { return from(u,upShift); }
    if (0L==u) { return this; }
    if (0==upShift) { return add(u); }
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    final int[] uu = Ints.littleEndian(u,bShift);
    final int n0 = hiInt();
    final int n1 = iShift+uu.length;
    final int n = Math.max(n0,n1)+1;
    //final Natural us = u.shiftUp(shift);
    final int[] t = new int[n];
    int i=0;
    for (;i<iShift;i++) { t[i] = word(i); }
    long carry = 0L;
    for (;i<n1;i++) {
      final long ui = Numbers.unsigned(uu[i-iShift]);
      final long sum = (uword(i) + ui) + carry;
      carry = (sum>>>32);
      t[i] = (int) sum; }
    for (;i<n;i++) {
      final long sum = uword(i) + carry;
      carry = (sum>>>32);
      t[i] = (int) sum; }
    if (0L!=carry) { t[i] = (int) carry; }
    return unsafe(t); }

  //--------------------------------------------------------------

  @Override
  public final Natural subtract (final long u,
                                 final int upShift) {
    //assert isValid();
    //assert 0L<=u;
    //assert 0<=upShift;
    if (0L==u) { return this; }
    if (0==upShift) { return subtract(u); }
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    final int[] uu = Ints.littleEndian(u,bShift);
    final int n0 = hiInt();
    final int n1 = iShift+uu.length;
    //assert n1<=n0;
    final int[] v = new int[n0];
    for (int i=0;i<iShift;i++) { v[i] = word(i); }
    long borrow = 0L;
    for (int i=iShift;i<n1;i++) {
      final long ui = Numbers.unsigned(uu[i-iShift]);
      final long dif = (uword(i)-ui) + borrow;
      borrow = (dif>>32);
      v[i] = (int) dif; }
    for (int i=n1;i<n0;i++) {
      final long dif = uword(i) + borrow;
      borrow = (dif>>32);
      v[i] = (int) dif; }
    //assert 0L==borrow;
    return unsafe(v); }

  //--------------------------------------------------------------
  // Ringlike
  //--------------------------------------------------------------

  @Override
  public final Natural zero () { 
    //assert isValid();
    return unsafe(Ints.EMPTY); }

  @Override
  public final Natural one () { 
    //assert isValid();
    return unsafe(new int[] { 1 }); }

  //--------------------------------------------------------------

  @Override
  public final Natural add (final Natural u) {
    //assert isValid();
    //assert u.isValid();
    if (isZero()) { return u; }
    if (u.isZero()) { return this; }
    // TODO: optimize by summing over joint range
    // and just carrying after that?
    final int n = Math.max(hiInt(),u.hiInt());
    final int[] t = new int[n+1];
    long sum = 0L;
    long carry = 0L;
    int i=0;
    for (;i<n;i++) {
      sum = uword(i) + u.uword(i) + carry;
      carry = (sum>>>32);
      t[i] = (int) sum; }
    if (0L!=carry) { t[i] = (int) carry; }
    return unsafe(t); }

  //--------------------------------------------------------------

  @Override
  public final Natural subtract (final Natural u) {
    //assert isValid();
    //assert u.isValid();
    if (u.isZero()) { return this; }
    //assert 0<=compareTo(u);
    final int n0 = hiInt();
    final int n1 = u.hiInt();
    //assert n1<=n0;
    final int[] v = new int[n0];
    long borrow = 0L;
    int i=0;
    for (;i<n1;i++) {
      final long dif = (uword(i)-u.uword(i)) + borrow;
      borrow = (dif>>32);
      v[i] = (int) dif; }
    //assert n1==i;
    for (;i<n0;i++) {
      if (0L==borrow) { break; } 
      final long dif = uword(i) + borrow;
      borrow = (dif>>32);
      v[i] = (int) dif; }
    //assert 0L==borrow;
    for (;i<n0;i++) { 
      final long dif = uword(i);
      //assert 0L==(dif>>32);
      v[i] = (int) dif; }
    return unsafe(v); }

  //--------------------------------------------------------------
  // Uints
  //--------------------------------------------------------------

  @Override
  public final int startWord () { 
    //assert isValid();
    return 0; }

  @Override
  public final int endWord () { 
    //assert isValid();
    return words().length; }

  @Override
  public final int word (final int i) {
    //assert isValid();
    //assert 0<=i : "Negative index: " + i;
    if (endWord()<=i) { return 0; }
    return words()[i]; }

  @Override
  public final Natural setWord (final int i,
                                final int w) {
    //assert isValid();
    //assert 0<=i;
    expandTo(i);
    words()[i] = w;
    return recycle(); }
  //return this; }

  @Override
  public final Natural empty () { 
    //assert isValid();
    // safe because Ints.EMPTY is immutable
    return unsafe(Ints.EMPTY); }

  @Override
  public final Natural from (final int u) {
    //assert isValid();
    //assert 0<=u;
    return valueOf(u);  }

  @Override
  public final Natural from (final long u) {
    //assert isValid();
    //assert 0<=u;
    return valueOf(u);  }

  //--------------------------------------------------------------

  @Override
  public final Natural shiftDownWords (final int iShift) {
    //assert isValid();
    //assert 0<=iShift;
    if (0==iShift) { return this; }
    if (isZero()) { return this; }
    final int n0 = hiInt();
    final int n1 = n0-iShift;
    if (0>=n1) { return empty(); }
    final int[] u = new int[n1];
    for (int i=0;i<n1;i++) { 
      u[i] = word(i+iShift); }
    return unsafe(u); }

  @Override
  public final Natural shiftDown (final int shift) {
    //assert isValid();
    //assert 0<=shift;
    if (shift==0) { return this; }
    if (isZero()) { return this; }
    final int iShift = (shift>>>5);
    final int n0 = hiInt();
    final int n1 = n0-iShift;
    if (0>=n1) { return empty(); }
    final int bShift = (shift & 0x1f);
    if (0==bShift) { return shiftDownWords(iShift); }
    final int[] u = new int[n1];
    final int rShift = 32-bShift;
    int w0 = word(iShift);
    for (int j=0;j<n1;j++) { 
      final int w1 = word(j+iShift+1);
      final int w = ((w1<<rShift) | (w0>>>bShift));
      w0 = w1;
      u[j] = w; }
    return unsafe(u); }

  //--------------------------------------------------------------

  private final Natural shiftUpWords (final int iShift) {
    //assert isValid();
    //assert 0<=iShift;
    if (0==iShift) { return this; }
    if (isZero()) { return this; }
    final int n = hiInt();
    if (0==n) { return this; }
    final int[] u = new int[n+iShift];
    for (int i=0;i<n;i++) { u[i+iShift] = word(i); }
    return unsafe(u); }

  @Override
  public final Natural shiftUp (final int shift) {
    //assert 0<=shift;
    if (shift==0) { return this; }
    if (isZero()) { return this; }
    final int iShift = (shift>>>5);
    final int bShift = (shift&0x1f);
    if (0==bShift) { return shiftUpWords(iShift); }
    final int rShift = 32-bShift;
    final int n0 = hiInt();
    final int n1 = n0+iShift;
    final int[] u = new int[n1+1];
    int w0 = 0;
    for (int i=0;i<n0;i++) { 
      final int w1 = word(i);
      final int w = ((w1<<bShift) | (w0>>>rShift));
      w0 = w1;
      u[i+iShift] = w; }
    u[n1] = (w0>>>rShift);
    return unsafe(u); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  /** DANGER!!! Mutable!!! */
  @Override
  public final int hashCode () { 
    //assert isValid();
    return 0; }

  /** DANGER!!! Mutable!!! used in testing only!!! */
  @Override
  public final boolean equals (final Object x) {
    //assert isValid();
    if (x==this) { return true; }
    if (!(x instanceof Natural)) { return false; }
    return uintsEquals((Natural) x); }

  /** hex string. */
  @Override
  public final String toString () { 
    if (! isValid()) { return "invalid NaturalLEMutable"; }
    return toHexString(); }

  //--------------------------------------------------------------
  // Transience
  //--------------------------------------------------------------

  @Override
  public boolean isImmutable () { 
    //assert isValid();
    return false; }

  @Override
  public final Natural recycle () { 
    //assert isValid();
    final int[] t = _words;
    _words = null;
    return unsafe(t); }

  @Override
  public final Natural immutable () { 
    //assert isValid();
    //final int[] t = Ints.stripTrailingZeros(_words);
    final int[] t = _words;
    _words = null;
    return NaturalLE.unsafe(t); }

  @Override
  public final Natural recyclable (final Natural init) { 
    //assert isValid();
    return copy(init); }

  @Override
  public final Natural recyclable (final int init) {
    //assert isValid();
    return NaturalLEMutable.make(init); }

  @Override
  public final Natural recyclable (final Natural init,
                                   final int nWords) {
    //assert isValid();
    if (null==init) {
      return NaturalLEMutable.make(nWords); }
    if (init instanceof NaturalLEMutable) {
      return NaturalLEMutable.make(words(),nWords); }
    return init.recyclable(init,nWords); }

  @Override
  public final Natural copy () { 
    //assert isValid();
    return copy(this); }

  @Override
  public final boolean isValid () { return null!=_words; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  /** UNSAFE: doesn't copy <code>words</code>. */
  private NaturalLEMutable (final int[] words) { _words = words; }

  public static final NaturalLEMutable 
  unsafe (final int[] words) {
    return new NaturalLEMutable(words); }

  /** Copy <code>words</code>. 
   */
  public static final NaturalLEMutable make (final int[] words) {
    return unsafe(Arrays.copyOf(words,words.length)); }

  /** Copy <code>words</code>, allowing space for at least
   * <code>n</code> words in the resulting natural number. 
   */
  public static final NaturalLEMutable make (final int[] words,
                                             final int n) {
    final int nn = Math.max(n,words.length);
    final int[] w = new int[nn];
    System.arraycopy(words,0,w,0,nn);
    return unsafe(w); }

  /** Zero instance with space for <code>n</code> words. 
   */
  public static final NaturalLEMutable make (final int n) {
    return unsafe(new int[n]); }

  //--------------------------------------------------------------
  /** From a big endian {@code byte[]}, as produced by
   * {@link BigInteger#toByteArray()}.
   */

  private static final NaturalLEMutable valueOf (final byte[] a) {
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

  public static final NaturalLEMutable valueOf (final BigInteger u) {
    //assert 0<=u.signum();
    return valueOf(u.toByteArray()); }

  //-------------------------------------------------------------

  public static final NaturalLEMutable valueOf (final String s,
                                                final int radix) {
    return unsafe(Ints.littleEndian(s,radix)); }

  public static final NaturalLEMutable valueOf (final String s) {
    return valueOf(s,0x10); }

  public static final NaturalLEMutable valueOf (final long u) {
    //assert 0L<=u;
    return unsafe(Ints.littleEndian(u)); }

  public static final NaturalLEMutable valueOf (final int u) {
    //assert 0L<=u;
    return unsafe(new int[] {u}); }

  //--------------------------------------------------------------

  public static final NaturalLEMutable copy (final NaturalLE u) { 
    return unsafe(u.copyWords()); }

  public static final NaturalLEMutable 
  copy (final NaturalLEMutable u) { return unsafe(u.copyWords()); }

  public static final NaturalLEMutable copy (final Natural u) { 
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

