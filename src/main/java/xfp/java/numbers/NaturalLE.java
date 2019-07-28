package xfp.java.numbers;

import java.math.BigInteger;
import java.util.Arrays;

/** immutable arbitrary-precision non-negative integers
 * (natural numbers) represented by little-endian
 * unsigned <code>int[]</code>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-27
 */

public final class NaturalLE implements Natural {

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------
  /** This array is never modified.
   */

  private final int[] _words;
  private final int[] words () { return _words; }

  /** Don't drop trailing zeros. */
  public final int[] copyWords () { 
    return Arrays.copyOf(words(),words().length); }

  // ought to be _words.length, but safer to compute and cache
  // in case we want to allow over-large arrays in the future
  private final int _hiInt;
  @Override
  public final int hiInt () { return _hiInt; }

  //--------------------------------------------------------------
  // Natural
  //--------------------------------------------------------------

  @Override
  public final Natural square (final long t) {
    assert isValid();
    assert 0L<=t;
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

  @Override
  public final Natural product (final long t0,
                                final long t1) {
    assert isValid();
    assert 0L<=t0;
    assert 0L<=t1;
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
    assert 0<=shift;
    //if (isZero()) { return u.shiftUp(shift); }
    //if (u.isZero()) { return this; }
    //if (0==shift) { return add(u); }
    final int iShift = (shift>>>5);
    final int bShift = (shift&0x1f);
    // TODO: special case whole word shift?
    final int rShift = 32-bShift;
    final int n0 = hiInt();
    final int n1 = u.hiInt()+iShift+1;
    final int n = Math.max(n0,n1);
    final int[] v = new int[n];
    int i=0;
    for (;i<iShift;i++) { v[i] = word(i); }
    long carry = 0L;
    int u0 = 0;
    // TODO: separate cases where one term's words are known zero
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
    assert 0<=u;
    assert 0<=upShift;
    //if (isZero()) { return from(u,upShift); }
    //if (0L==u) { return this; }
    //if (0==upShift) { return add(u); }
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    final int hi = (int) (u>>>32);
    final int lo = (int) u;
    final int uu0,uu1,uu2;
    if (0==bShift) { uu0=lo; uu1=hi; uu2=0; }
    else {
      final int rShift = 32-bShift;
      uu0 = (lo<<bShift);
      uu1 = ((hi<<bShift)|(lo>>>rShift));
      uu2 =  (hi>>>rShift); }
    final int n0 = hiInt();
    final int n = Math.max(n0,iShift+3);
    final int[] v = new int[n+1];
    int i=0;
    for (;i<Math.min(iShift,n0);i++) { v[i] = word(i); }
    long carry = 0L;
    i=iShift;
    final long u0 = Numbers.unsigned(uu0);
    final long sum0 = (uword(i) + u0) + carry;
    carry = (sum0>>>32);
    v[i++] = (int) sum0; 
    final long u1 = Numbers.unsigned(uu1);
    final long sum1 = (uword(i) + u1) + carry;
    carry = (sum1>>>32);
    v[i++] = (int) sum1; 
    final long u2 = Numbers.unsigned(uu2);
    final long sum2 = (uword(i) + u2) + carry;
    carry = (sum2>>>32);
    v[i++] = (int) sum2; 
    for (;i<n;i++) {
      final long sum = uword(i) + carry;
      carry = (sum>>>32);
      v[i] = (int) sum; }
    v[n] = (int) carry; 
    return unsafe(v); }

//  @Override
//  public final Natural add (final long u,
//                            final int upShift) {
//    assert 0<=u;
//    assert 0<=upShift;
//    //if (isZero()) { return from(u,upShift); }
//    //if (0L==u) { return this; }
//    //if (0==upShift) { return add(u); }
//    final int iShift = (upShift>>>5);
//    final int bShift = (upShift&0x1f);
//    final int[] uu = Ints.littleEndian(u,bShift);
//    
//    final int n0 = hiInt();
//    final int n1 = iShift+uu.length;
//    final int n = Math.max(n0,n1);
//    final int[] v = new int[n+1];
//    int i=0;
//    for (;i<Math.min(iShift,n0);i++) { v[i] = word(i); }
//    long carry = 0L;
//    i=iShift;
//    for (;i<n1;i++) {
//      final long ui = Numbers.unsigned(uu[i-iShift]);
//      final long sum = (uword(i) + ui) + carry;
//      carry = (sum>>>32);
//      v[i] = (int) sum; }
//    for (;i<n;i++) {
//      final long sum = uword(i) + carry;
//      carry = (sum>>>32);
//      v[i] = (int) sum; }
//    v[n] = (int) carry; 
//    return unsafe(v); }

  //--------------------------------------------------------------

  @Override
  public final Natural subtract (final long u,
                                 final int upShift) {
    assert 0L<=u;
    assert 0<=upShift;
    //if (0L==u) { return this; }
    //if (0==upShift) { return subtract(u); }
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    final int n0 = hiInt();
    final int hi = (int) (u>>>32);
    final int lo = (int) u;
    final int uu0,uu1,uu2;
    if (0==bShift) { uu0=lo; uu1=hi; uu2=0; }
    else {
      final int rShift = 32-bShift;
      uu0 = (lo<<bShift);
      uu1 = ((hi<<bShift)|(lo>>>rShift));
      uu2 =  (hi>>>rShift); }
    // extra elements in case uu1,uu2 are 0
    final int[] v = new int[n0+2];
    int i=0;
    for (;i<iShift;i++) { v[i] = word(i); }
    long borrow = 0L;
    i=iShift;
    final long u0 = Numbers.unsigned(uu0);
    final long dif0 = (uword(i)-u0) + borrow;
    borrow = (dif0>>32);
    v[i++] = (int) dif0; 
    final long u1 = Numbers.unsigned(uu1);
    final long dif1 = (uword(i)-u1) + borrow;
    borrow = (dif1>>32);
    v[i++] = (int) dif1; 
    final long u2 = Numbers.unsigned(uu2);
    final long dif2 = (uword(i)-u2) + borrow;
    borrow = (dif2>>32);
    v[i++] = (int) dif2; 
    for (;i<n0;i++) {
      final long dif = uword(i) + borrow;
      borrow = (dif>>32);
      v[i] = (int) dif; }
    //assert 0L==borrow;
    return unsafe(v); }
  
//  @Override
//  public final Natural subtract (final long u,
//                                 final int upShift) {
//    assert 0L<=u;
//    assert 0<=upShift;
//    //if (0L==u) { return this; }
//    //if (0==upShift) { return subtract(u); }
//    final int iShift = (upShift>>>5);
//    final int bShift = (upShift&0x1f);
//    final int[] uu = Ints.littleEndian(u,bShift);
//    final int n0 = hiInt();
//    final int n1 = iShift+uu.length;
//    //assert n1<=n0;
//    final int[] v = new int[n0];
//    for (int i=0;i<iShift;i++) { v[i] = word(i); }
//    long borrow = 0L;
//    for (int i=iShift;i<n1;i++) {
//      final long ui = Numbers.unsigned(uu[i-iShift]);
//      final long dif = (uword(i)-ui) + borrow;
//      borrow = (dif>>32);
//      v[i] = (int) dif; }
//    for (int i=n1;i<n0;i++) {
//      final long dif = uword(i) + borrow;
//      borrow = (dif>>32);
//      v[i] = (int) dif; }
//    //assert 0L==borrow;
//    return unsafe(v); }
  
  //--------------------------------------------------------------
  // Ringlike
  //--------------------------------------------------------------

  private static final NaturalLE ONE = unsafe(new int[] { 1 });

  @Override
  public final Natural one () { return ONE; }

  //--------------------------------------------------------------

  @Override
  public final Natural add (final Natural u) {
    if (isZero()) { return u; }
    if (u.isZero()) { return this; }
    // TODO: optimize by summing over joint range
    // and just carrying after that?
    final int n = Math.max(hiInt(),u.hiInt());
    final int[] v = new int[n+1];
    long sum = 0L;
    long carry = 0L;
    int i=0;
    for (;i<n;i++) {
      sum = uword(i) + u.uword(i) + carry;
      carry = (sum>>>32);
      v[i] = (int) sum; }
    if (0L!=carry) { v[i] = (int) carry; }
    return unsafe(v); }

  //--------------------------------------------------------------

  @Override
  public final Natural subtract (final Natural u) {
    //if (u.isZero()) { return this; }
    assert 0<=compareTo(u);
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
      final long dif = uword(i) + borrow;
      borrow = (dif>>32);
      v[i] = (int) dif; }
    //assert 0L==borrow;
    return unsafe(v); }

  //--------------------------------------------------------------
  // Uints
  //--------------------------------------------------------------

  @Override
  public final int startWord () { return 0; }
  @Override
  public final int endWord () { return words().length; }

  @Override
  public final int word (final int i) {
    assert 0<=i : "Negative index: " + i;
    if (endWord()<=i) { return 0; }
    return words()[i]; }

  @Override
  public final Natural setWord (final int i,
                                final int w) {
    assert 0<=i;
    final int n = Math.max(i+1,hiInt());
    final  int[] u = Arrays.copyOf(words(),n);
    u[i] = w;
    return unsafe(u); }

  /** Singleton.<br>
   * TODO: Better to use a new array?
   */
  private static final NaturalLE ZERO = 
    new NaturalLE(Ints.EMPTY,0); 

  @Override
  public final Natural empty () { return ZERO; }

  /** Return a {@link NaturalLE} equivalent to the unsigned 
   * value of <code>u</code>.
   */
  @Override
  public final Natural from (final int u) { return valueOf(u); }

  /** <code>0<=u</code>.*/
  @Override
  public final Natural from (final long u) {
    assert 0<=u;
    return valueOf(u);  }

  //--------------------------------------------------------------

  @Override
  public final Natural shiftDownWords (final int iShift) {
    assert isValid();
    assert 0<=iShift;
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
    assert isValid();
    assert 0<=shift;
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

  @Override
  public final Natural shiftUpWords (final int iShift) {
    assert isValid();
    assert 0<=iShift;
    if (0==iShift) { return this; }
    if (isZero()) { return this; }
    final int n = hiInt();
    if (0==n) { return this; }
    final int[] u = new int[n+iShift];
    for (int i=0;i<n;i++) { u[i+iShift] = word(i); }
    return unsafe(u); }

  @Override
  public final Natural shiftUp (final int shift) {
    assert 0<=shift;
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
  // Transience
  //--------------------------------------------------------------

  @Override
  public final boolean isValid () { return true; }

  @Override
  public final Natural recyclable (final Natural init) {
    return NaturalLEMutable.copy(init); }

  @Override
  public final Natural recyclable (final int init) {
    return NaturalLEMutable.make(init); }

  @Override
  public final Natural recyclable (final Natural init,
                                   final int nWords) {
    if (null==init) {
      return NaturalLEMutable.make(nWords); }
    if (init instanceof NaturalLE) {
      return NaturalLEMutable.make(words(),nWords); }
    return init.recyclable(init,nWords); }

  @Override
  public boolean isImmutable () { return true; }

  @Override
  public final Natural recycle () { return this; }

  @Override
  public final Natural immutable () { return this; }

  @Override
  public final Natural copy () {  return this; }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { return uintsHashCode(); }

  @Override
  public final boolean equals (final Object x) {
    if (x==this) { return true; }
    if (!(x instanceof Natural)) { return false; }
    return uintsEquals((Natural) x); }

  /** hex string. */
  @Override
  public final String toString () { return toHexString(); }

  //--------------------------------------------------------------
  // construction
  //-------------------------------------------------------------

  /** UNSAFE: doesn't copy <code>words</code>. */
  private NaturalLE (final int[] words,
                     final int hiInt) { 
    _words = words; 
    _hiInt = hiInt; }

  /** Doesn't copy <code>words</code>. 
   */

  static final NaturalLE unsafe (final int[] words) {
    return new NaturalLE(words,Ints.hiInt(words)); }

  /** Copy <code>words</code>. 
   *  */
  public static final NaturalLE make (final int[] words) {
    //    final int end = Ints.hiInt(words);
    //    return unsafe(Arrays.copyOf(words,end)); }
    return unsafe(Arrays.copyOf(words,words.length)); }

  //--------------------------------------------------------------
  /** From a big endian {@code byte[]}, as produced by
   * {@link BigInteger#toByteArray()}.
   */

  private static final NaturalLE valueOf (final byte[] a) {
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
    return make(result); }

  public static final NaturalLE valueOf (final BigInteger u) {
    assert 0<=u.signum();
    return valueOf(u.toByteArray()); }

  //-------------------------------------------------------------

  public static final NaturalLE valueOf (final String s,
                                         final int radix) {
    return make(Ints.littleEndian(s,radix)); }

  public static final NaturalLE valueOf (final String s) {
    return valueOf(s,0x10); }

  /** <code>0L<=u</code>. */
  public static final NaturalLE valueOf (final long u) {
    assert 0L<=u;
    if (u==0L) { return ZERO; }
    return make(Ints.littleEndian(u)); }


  /** Return a {@link NaturalLE} equivalent to the unsigned 
   * value of <code>u</code>.
   */
  public static final NaturalLE valueOf (final int u) {
    if (u==0) { return valueOf(0L); }
    return unsafe(new int[] {u}); }

  //--------------------------------------------------------------

  public static final NaturalLE 
  copy (final NaturalLE u) { return make(u.words()); }

  public static final NaturalLE 
  copy (final NaturalLEMutable u) { 
    return make(u.copyWords()); }

  public static final NaturalLE
  copy (final Natural u) { 
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

