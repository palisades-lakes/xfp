package xfp.java.numbers;

import static xfp.java.numbers.Numbers.unsigned;

import java.util.Arrays;

import xfp.java.exceptions.Exceptions;

/** mutable arbitrary-precision non-negative integers
 * (natural numbers) represented by little-endian
 * unsigned <code>int[]</code>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-09-03
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

  @Override
  public final NaturalLEMutable words (final int i0,
                                       final int i1) {
    //assert 0<=i0;
    //assert i0<i1;
    if ((0==i0) && (hiInt()<=i1)) { return unsafe(copyWords()); }
    final int n = Math.max(0,i1-i0);
    if (0>=n) { return (NaturalLEMutable) zero(); }
    final int[] tt = words();
    final int[] vv = new int[n];
    for (int i=0;i<n;i++) { vv[i] =  tt[i+i0]; }
    return unsafe(vv); }

  // TODO: grow faster?
  private final void expandTo (final int i) {
    //assert isValid();
    final int n = Math.max(i+1,hiInt());
    if (hiInt()<n) { _words = Arrays.copyOf(words(),n); } }

  //--------------------------------------------------------------
  // Natural
  //--------------------------------------------------------------

  @Override
  public final Natural add (final Natural u,
                            final int shift) {
    throw Exceptions.unsupportedOperation(this,"add",u,shift); }

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

  @Override
  public final Natural absDiff (final Natural u) {
    //assert isValid();
    //assert u.isValid();
    final int c = compareTo(u);
    if (c==0) { return zero(); }
    if (c<0) { return u.subtract(this); }
    return subtract(u); }

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
  public final long uword (final int i) {
    //assert isValid();
    //assert 0<=i : "Negative index: " + i;
    if (endWord()<=i) { return 0L; }
    return unsigned(_words[i]); }

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
  public final int loInt () {
    // Search for lowest order nonzero int
    final int n = hiInt(); // might be 0
    for (int i=startWord();i<n;i++) {
      if (0!=word(i)) { return i; } }
    //assert 0==n;
    return 0; }

  @Override
  public final int hiInt () {
    final int start = startWord();
    for (int i = endWord()-1;i>=start;i--) {
      if (0!=word(i) ) { return i+1; } }
    //assert 0==start;
    return 0; }

  @Override
  public final int loBit () {
    // Search for lowest order nonzero int
    final int i=loInt(); 
    if (i==hiInt()) { return 0; } // all bits zero
    return (i<<5) + Integer.numberOfTrailingZeros(_words[i]); }

  @Override
  public final int hiBit () {
    final int i = hiInt()-1;
    if (0>i) { return 0; }
    final int wi = _words[i];
    return (i<<5)+Integer.SIZE-Integer.numberOfLeadingZeros(wi); }

  //--------------------------------------------------------------

  private final Natural shiftDownWords (final int iShift) {
    //assert isValid();
    //assert 0<=iShift;
    if (0==iShift) { return this; }
    if (isZero()) { return this; }
    final int n0 = hiInt();
    final int n1 = n0-iShift;
    if (0>=n1) { return zero(); }
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
    if (0>=n1) { return zero(); }
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

  @Override
  public final int compareTo (final Natural u) {
    //assert isValid();
    //assert u.isValid();
    // TODO: should really compare hiBits
    final int b0 = hiBit();
    final int b1 = u.hiBit();
    if (b0<b1) { return -1; }
    if (b0>b1) { return 1; }
    final int end = Math.max(hiInt(),u.hiInt()) - 1;
    final int start = Math.min(startWord(),u.startWord());
    for (int i=end;i>=start;i--) {
      final long u0i = uword(i);
      final long u1i = u.uword(i);
      if (u0i<u1i) { return -1; }
      if (u0i>u1i) { return 1; } }
    return 0; }

  //--------------------------------------------------------------

  public final int compareTo (final long u) {
    //assert isValid();
    //assert 0L<=u;
    final int n0 = hiInt();
    final long lo1 = Numbers.loWord(u);
    final long hi1 = Numbers.hiWord(u);
    final int n1 = ((0L!=hi1) ? 2 : (0L!=lo1) ? 1 : 0);
    if (n0<n1) { return -1; }
    if (n0>n1) { return 1; }
    final long hi0 = uword(1);
    if (hi0<hi1) { return -1; }
    if (hi0>hi1) { return 1; }
    final long lo0 = uword(0);
    if (lo0<lo1) { return -1; }
    if (lo0>lo1) { return 1; }
    return 0; }

  //--------------------------------------------------------------
  // 'Number' methods
  //--------------------------------------------------------------

  @Override
  public final int intValue () { 
    throw Exceptions.unsupportedOperation(this,"intValue"); }
//   //assert isValid();
//    return word(0); }

  @Override
  public final long longValue () {
    throw Exceptions.unsupportedOperation(this,"longValue"); }
//    return (uword(1)<<32) | uword(0); }

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
    if (x==this) { return true; }
    if (!(x instanceof NaturalLEMutable)) { return false; }
    final NaturalLEMutable u = (NaturalLEMutable) x;
    final int nt = hiInt();
    if (nt!=u.hiInt()) { return false; }
    for (int i=0; i<nt; i++) {
      if (_words[i]!=u._words[i]) { return false; } }
    return true; }

  public final String toHexString () {
    final StringBuilder b = new StringBuilder("");
    final int n = hiInt()-1;
    if (0>n) { b.append('0'); }
    else {
      b.append(String.format("%x",Long.valueOf(uword(n))));
      for (int i=n-1;i>=0;i--) {
        //b.append(" ");
        b.append(String.format("%08x",Long.valueOf(uword(i)))); } }
    return b.toString(); }

  /** hex string. */
  @Override
  public final String toString () { 
    //if (! isValid()) { return "invalid NaturalLEMutable"; }
    return toHexString(); }

  //--------------------------------------------------------------
  // Transience
  //--------------------------------------------------------------

  private final Natural recycle () { 
    //assert isValid();
    final int[] t = _words;
    _words = null;
    return unsafe(t); }

   @Override
  public final Natural copy () { 
    //assert isValid();
    return unsafe(copyWords()); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  /** UNSAFE: doesn't copy <code>words</code>. */
  private NaturalLEMutable (final int[] words) { _words = words; }

  public static final NaturalLEMutable 
  unsafe (final int[] words) {
    return new NaturalLEMutable(words); }

  /** Zero instance with space for <code>n</code> words. 
   */
  public static final NaturalLEMutable make (final int n) {
    return unsafe(new int[n]); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

