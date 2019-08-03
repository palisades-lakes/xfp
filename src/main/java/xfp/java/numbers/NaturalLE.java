package xfp.java.numbers;

import static xfp.java.numbers.Numbers.hiWord;
import static xfp.java.numbers.Numbers.loWord;
import static xfp.java.numbers.Numbers.unsigned;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.CollectionSampler;
import org.apache.commons.rng.sampling.distribution.ContinuousSampler;
import org.apache.commons.rng.sampling.distribution.ContinuousUniformSampler;

import xfp.java.prng.Generator;
import xfp.java.prng.GeneratorBase;

/** immutable arbitrary-precision non-negative integers
 * (natural numbers) represented by little-endian
 * unsigned <code>int[]</code>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-08-02
 */

@SuppressWarnings("unchecked")
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
  public final  Natural sum (final long u,
                             final long v) {
    //assert isValid();
    //assert 0L<=u;
    if (0L==u) { return from(v); }
    if (0L==v) { return from(u); }
    final int[] ww = new int[3];
    long sum = loWord(u) + loWord(v);
    ww[0] = (int) sum;
    sum = hiWord(u) + hiWord(v) + (sum>>>32);
    ww[1] = (int) sum;
    ww[2] = (int) (sum>>>32);
    return unsafe(ww); }

  @Override
  public final  Natural sum (final long u,
                             final long v,
                             final int upShift) {
    //assert isValid();
    //assert 0L<=u;
    //assert 0<=upShift;
    if (0L==u) { return from(v); }
    if (0L==v) { return from(u); }
    if (0==upShift) { return sum(u,v); }
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    final int[] ww = new int[iShift+3];
    ww[0] = (int) loWord(u);
    ww[1] = (int) hiWord(u);
    final long vlo = loWord(v);
    final long vhi = hiWord(v);
    final long vv0,vv1,vv2;
    if (0==bShift) { vv0=vlo; vv1=vhi; vv2=0; }
    else {
      final int rShift = 32-bShift;
      vv0 = unsigned((int) (vlo<<bShift));
      vv1 = unsigned((int) ((vhi<<bShift)|(vlo>>>rShift)));
      vv2 = unsigned((int) (vhi>>>rShift)); }
    int i = iShift;
    long sum = unsigned(ww[i]) + vv0;
    ww[i] = (int) sum;
    i++;
    sum = unsigned(ww[i]) + vv1 + (sum>>>32);
    ww[i] = (int) sum;
    i++;
    sum = unsigned(ww[i]) + vv2 + (sum>>>32);
    ww[i] = (int) sum;
    assert 0L==(sum>>>32);
    return unsafe(ww); }

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

  //  private final Natural add (final NaturalLE u,
  //                             final int upShift) {
  //    //assert 0<=upShift;
  //    if (isZero()) { return u.shiftUp(upShift); }
  //    if (u.isZero()) { return this; }
  //    if (0==upShift) { return add(u); }
  //    final int iShift = (upShift>>>5);
  //    final int bShift = (upShift&0x1f);
  //    // TODO: special case whole word shift?
  //    final int rShift = 32-bShift;
  //    final int nt = hiInt();
  //    final int n0 = u.hiInt()+iShift;
  //    final int n1 = n0+1;
  //    final int n = Math.max(nt,n1);
  //    final int[] tt = words();
  //    final int[] uu = u.words();
  //    final int[] vv = new int[n];
  //    int i=0;
  //    System.arraycopy(tt,0,vv,0,Math.min(nt,iShift));
  //    i=iShift;
  //    long carry = 0L;
  //    int u0 = 0;
  //    for (;i<Math.min(nt,n1);i++) {
  //      final int u1 = ((i<n0) ? uu[i-iShift] : 0);
  //      final int ui = 
  //        ((bShift==0) ? u1 : ((u1<<bShift)|(u0>>>rShift)));
  //      u0 = u1;
  //      final long sum = unsigned(tt[i]) + unsigned(ui) + carry;
  //      carry = (sum>>>32);
  //      vv[i] = (int) sum; }
  //    for (;i<n1;i++) {
  //      if (0L==carry) { break; }
  //      final int u1 = ((i<n0) ? uu[i-iShift] : 0);
  //      final int ui = 
  //        ((bShift==0) ? u1 : ((u1<<bShift)|(u0>>>rShift)));
  //      u0 = u1;
  //      final long sum = unsigned(ui) + carry;
  //      carry = (sum>>>32);
  //      vv[i] = (int) sum; }
  //    for (;i<n1;i++) {
  //      final int u1 = ((i<n0) ? uu[i-iShift] : 0);
  //      final int ui = 
  //        ((bShift==0) ? u1 : ((u1<<bShift)|(u0>>>rShift)));
  //      u0 = u1;
  //      vv[i] = ui; }
  //    for (;i<nt;i++) {
  //      if (0L==carry) { break; }
  //      final long sum = unsigned(tt[i]) + carry;
  //      carry = (sum>>>32);
  //      vv[i] = (int) sum; }
  //    if (i<nt) { System.arraycopy(tt,i,vv,i,nt-i); }
  //    if (0L!=carry) { vv[n-1] = (int) carry; }
  //    return unsafe(vv); }
  //
  //  @Override
  //  public final Natural add (final Natural u,
  //                            final int upShift) {
  //    if (u instanceof NaturalLE) { 
  //      return add((NaturalLE) u,upShift); }
  //    return Natural.super.add(u,upShift); }

  @Override
  public final Natural add (final Natural u,
                            final int upShift) {
    //assert 0<=upShift;
    //if (isZero()) { return u.shiftUp(upShift); }
    //if (u.isZero()) { return this; }
    //if (0==upShift) { return add(u); }
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
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
  public final Natural add (final long u) {
    //assert isValid();
    //assert 0L<=u;
    if (0L==u) { return this; }
    if (isZero()) { return from(u); }
    final int n = hiInt();
    final int[] tt = words();
    final int[] vv = new int[n+1];
    long sum = ((n<=0) ? 0L : unsigned(tt[0])) + loWord(u);
    vv[0] = (int) sum;
    long carry = (sum>>>32);
    sum = ((n<=1) ? 0L : unsigned(tt[1])) + hiWord(u) + carry;
    vv[1] = (int) sum;
    carry = (sum>>>32);
    int i=2;
    for (;i<n;i++) {
      if (0L==carry) { break; }
      sum = unsigned(tt[i]) + carry;
      vv[i] = (int) sum;
      carry = (sum>>>32); }
    for (;i<n;i++) { vv[i] = tt[i]; }
    if (0L!=carry) { vv[n] = (int) carry; }
    return unsafe(vv); }

  //--------------------------------------------------------------
  // TODO: compare performance to: copy shifted u to vv, and then 
  // add tt.

  @Override
  public final Natural add (final long u,
                            final int upShift) {
    //assert 0<=u;
    //assert 0<=upShift;
    if (isZero()) { return from(u,upShift); }
    if (0L==u) { return this; }
    if (0==upShift) { return add(u); }
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
    final int nt = hiInt();
    final int nu = iShift+2;
    final int n = Math.max(nt,nu);
    final int[] tt = words();
    final int[] vv = new int[n+1];
    System.arraycopy(tt,0,vv,0,Math.min(iShift,nt));
    int i=iShift;
    long sum = ((i<nt)?unsigned(tt[i]):0L) + unsigned(uu0);
    long carry = (sum>>>32);
    vv[i++] = (int) sum; 
    sum = ((i<nt)?unsigned(tt[i]):0L) + unsigned(uu1) + carry;
    carry = (sum>>>32);
    vv[i++] = (int) sum; 
    sum = ((i<nt)?unsigned(tt[i]):0L) + unsigned(uu2) + carry;
    carry = (sum>>>32);
    vv[i++] = (int) sum; 
    for (;i<nt;i++) {
      if (0L==carry) { break; }
      sum = unsigned(tt[i]) + carry;
      carry = (sum>>>32);
      vv[i] = (int) sum; }
    if (i<nt) { System.arraycopy(tt,i,vv,i,nt-i); }
    if (0L!=carry) { vv[n] = (int) carry; }
    return unsafe(vv); }

  //--------------------------------------------------------------

  private final Natural subtract (final NaturalLE u,
                                  final int upShift) {
    //assert 0<=upShift;
    if (isZero()) { 
      assert u.isZero();
      return zero(); }
    if (u.isZero()) { return this; }
    if (0==upShift) { return subtract(u); }
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    final int rShift = 32-bShift;
    final int nt = hiInt();
    final int nu = u.hiInt()+iShift;
    assert nu<=nt : nu + " <= " + nt;
    final int[] tt = words();
    final int[] uu = u.words();
    final int[] vv = new int[nt];
    System.arraycopy(tt,0,vv,0,iShift);
    int i=iShift;
    long borrow = 0L;
    int u0 = 0;
    for (;i<nu;i++) {
      final int u1 = ((i<nu) ? uu[i-iShift] : 0);
      final int ui = 
        ((bShift==0) ? u1 : ((u1<<bShift)|(u0>>>rShift)));
      u0 = u1;
      if (i>=nt) { assert 0L==borrow; break; }
      final long tti = ((i<nt)?unsigned(tt[i]):0);
      final long dif = (tti-unsigned(ui))+borrow;
      borrow = (dif>>32);
      vv[i] = (int) dif; }
    assert i==nu;
    if (nu<nt) {
      final int ui = ((bShift==0) ? 0 : (u0>>>rShift));
      final long tti = unsigned(tt[nu]);
      final long dif = (tti-unsigned(ui))+borrow;
      borrow = (dif>>32);
      vv[nu] = (int) dif; }
    i=nu+1;
    for (;i<nt;i++) {
      if (0L==borrow) { break; }
      final long dif = unsigned(tt[i]) + borrow;
      borrow = (dif>>32);
      vv[i] = (int) dif; }
    if (i<nt) { System.arraycopy(tt,i,vv,i,nt-i); }
    assert (0L==borrow);
    return unsafe(vv); }

  @Override
  public final Natural subtract (final Natural u,
                                 final int upShift) {
    if (u instanceof NaturalLE) { 
      return subtract((NaturalLE) u,upShift); }
    return Natural.super.subtract(u,upShift); }

  //--------------------------------------------------------------

  //  private final Natural subtract (final int upShift,
  //                                  final NaturalLE u) {
  //    //assert 0<=upShift;
  //    if (isZero()) { assert u.isZero(); return zero(); }
  //    if (u.isZero()) { return shiftUp(upShift); }
  //    if (0==upShift) { return subtract(u); }
  //    final int iShift = (upShift>>>5);
  //    final int bShift = (upShift&0x1f);
  //    final int rShift = 32-bShift;
  //    final int nt = hiInt()+iShift;
  //    final int nu = u.hiInt();
  //    assert nu<=nt+1 : nu + " <= " + nt+1;
  //    final int[] tt = words();
  //    final int[] uu = u.words();
  //    final int[] vv = new int[nt+1];
  //    long borrow = 0L;
  //    int i=0;
  //    for (;i<Math.min(nu,iShift);i++) {
  //      final long dif = -unsigned(uu[i])+borrow;
  //      borrow = (dif>>32);
  //      vv[i] = (int) dif; }
  //    int tt0 = 0;
  //    for (;i<Math.max(nu,iShift);i++) {
  //      final int tt1 = ((i<iShift)?0:tt[i-iShift]);
  //      final int tti = 
  //        ((bShift==0) ? tt1 : ((tt1<<bShift)|(tt0>>>rShift)));
  //      tt0 = tt1;
  //      final long uui = ((i<nu)?unsigned(uu[i]):0L);
  //      final long dif = (unsigned(tti)-uui)+borrow;
  //      borrow = (dif>>32);
  //      vv[i] = (int) dif; }
  //    for (;i<nt;i++) {
  //      if (0L==borrow) { break; }
  //      final int tt1 = tt[i-iShift];
  //      final int tti = 
  //        ((bShift==0) ? tt1 : ((tt1<<bShift)|(tt0>>>rShift)));
  //      tt0 = tt1;
  //      final long dif = unsigned(tti)+borrow;
  //      borrow = (dif>>32);
  //      vv[i] = (int) dif; }
  //    for (;i<nt;i++) {
  //      final int tt1 = tt[i-iShift];
  //      final int tti = 
  //        ((bShift==0) ? tt1 : ((tt1<<bShift)|(tt0>>>rShift)));
  //      tt0 = tt1;
  //      vv[i] = tti; }
  //    final int tti = ((bShift==0) ? 0 : (tt0>>>rShift));
  //    final long dif = unsigned(tti)+borrow;
  //    borrow = (dif>>32);
  //    vv[nt] = (int) dif; 
  //    assert (0L==borrow);
  //    return unsafe(vv); }
  //
  //  @Override
  //  public final Natural subtract (final int upShift,
  //                                 final Natural u) {
  //    if (u instanceof NaturalLE) { 
  //      return subtract(upShift,(NaturalLE) u); }
  //    return Natural.super.subtract(upShift,u); }

  //--------------------------------------------------------------

  @Override
  public final Natural subtract (final long u) {
    //assert isValid();
    //assert 0L<=u;
    //assert 0<=compareTo(u);
    if (0L==u) { return this; }
    final int n = hiInt();
    final int[] tt = words();
    final int[] v = new int[n];
    // at least 1 element in tt or u==0
    long dif = unsigned(tt[0])-loWord(u);
    v[0] = (int) dif;
    long borrow = (dif>>32);
    if (1<n) {
      final long tt1 = (1>=n) ? 0L : unsigned(tt[1]);
      dif = (tt1-hiWord(u))+borrow;
      v[1] = (int) dif;
      borrow = (dif>>32);
      int i=2;
      for (;i<n;i++) {
        if (0L==borrow) { break; }
        dif = unsigned(tt[i])+borrow;
        v[i] = (int) dif;
        borrow = (dif>>32); }
      for (;i<n;i++) { v[i] = tt[i]; } }
    //assert 0L==borrow : borrow;
    return unsafe(v); }

  //--------------------------------------------------------------

  @Override
  public final Natural subtract (final long u,
                                 final int upShift) {
    //assert 0L<=u;
    //assert 0<=upShift;
    if (isZero()) { assert 0L==u; return this; }
    if (0L==u) { return this; }
    if (0==upShift) { return subtract(u); }
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    final int hi = (int) (u>>>32);
    final int lo = (int) u;
    final long uu0,uu1,uu2;
    if (0==bShift) { 
      uu0=unsigned(lo); 
      uu1=unsigned(hi); 
      uu2=0L; }
    else {
      final int rShift = 32-bShift;
      uu0 = unsigned(lo<<bShift);
      uu1 = unsigned((hi<<bShift)|(lo>>>rShift));
      uu2 = unsigned(hi>>>rShift); }
    final int n = hiInt();
    final int[] tt = words();
    final int[] vv = new int[n];
    // assert iShift<=n || 0L==u
    System.arraycopy(tt,0,vv,0,iShift);
    int i=iShift;
    long tti = ((i<n)?unsigned(tt[i]) : 0L);
    long dif = tti-uu0;
    long borrow = (dif>>32);
    vv[i++] = (int) dif; 
    if (i<n) { // or 0L=uu1
      final long u1 = unsigned(tt[i])-uu1;
      dif = u1 + borrow;
      borrow = (dif>>32);
      vv[i++] = (int) dif; }
    if (i<n) { // or 0L=uu1
      final long u2 = unsigned(tt[i])-uu2;
      final long dif2 = u2 + borrow;
      borrow = (dif2>>32);
      vv[i++] = (int) dif2; }
    for (;i<n;i++) {
      if (0L==borrow) { break; }
      dif = unsigned(tt[i]) + borrow;
      borrow = (dif>>32);
      vv[i] = (int) dif; }
    if (i<n) { System.arraycopy(tt,i,vv,i,n-i); }
    return unsafe(vv); }

  //--------------------------------------------------------------

  @Override
  public final Natural difference (final long v,
                                   final int upShift,
                                   final long u) {
    //assert isValid();
    //assert 0L<=u;
    //assert 0<=upShift;
    //assert 0L<=v;
    //assert compareTo(u,upShift,v)>=0;
    if (0L==v) { 
      //assert 0L==v;
      return zero(); }
    if (0==upShift) { return difference(v,u); }
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    final int n = iShift+3;
    final int[] ww = new int[n];
    final long vlo = loWord(v);
    final long vhi = hiWord(v);
    final int vv0,vv1,vv2;
    if (0==bShift) { 
      vv0=(int)vlo; 
      vv1=(int)vhi; 
      vv2=0; }
    else {
      final int rShift = 32-bShift;
      vv0 = (int) (vlo<<bShift);
      vv1 = (int) ((vhi<<bShift)|(vlo>>>rShift));
      vv2 = (int) (vhi>>>rShift); }
    int i = iShift;
    ww[i++] = vv0; ww[i++] = vv1; ww[i] = vv2;
    long dif = (unsigned(ww[0])-loWord(u));
    long borrow = (dif>>32);
    ww[0] = (int) dif;
    dif = (unsigned(ww[1])-hiWord(u)) + borrow;
    borrow = (dif>>32);
    ww[1] = (int) dif;
    i=2;
    for (;i<n;i++) {
      if (0L==borrow) { break; }
      dif = unsigned(ww[i]) + borrow;
      borrow = (dif>>32);
      ww[i] = (int) dif; }
    assert 0L==borrow;
    return unsafe(ww); }

  @Override
  public final Natural difference (final long u,
                                   final long v,
                                   final int upShift) {
    //assert isValid();
    //assert 0L<=u;
    //assert 0<=upShift;
    // assert upShift<64L;
    //assert 0L<=v;
    //assert compareTo(u,v,upShift)>=0;
    // TODO: overflow?
    final long dm = u-(v<<upShift);
    //assert 0L<=dm;
    return from(dm); }

  //--------------------------------------------------------------
  // Ringlike
  //--------------------------------------------------------------

  private static final NaturalLE ONE = unsafe(new int[] { 1 });

  @Override
  public final Natural one () { return ONE; }

  //--------------------------------------------------------------

  private final Natural add (final NaturalLE u) {
    final int nt = hiInt();
    final int nu = u.hiInt();
    if (nt<nu) { return u.add(this); }
    final int[] tt = words();
    final int[] uu = u.words();
    final int[] vv = new int[nt+1];
    long carry = 0L;
    int i=0;
    for (;i<nu;i++) {
      final long sum = unsigned(tt[i]) + unsigned(uu[i]) + carry;
      carry = (sum>>>32);
      vv[i] = (int) sum; }
    for (;i<nt;i++) {
      if (0L==carry) { break; }
      final long sum = unsigned(tt[i]) + carry;
      carry = (sum>>>32);
      vv[i] = (int) sum; }
    if (i<nt) { System.arraycopy(tt,i,vv,i,nt-i); }
    //if (0L!=carry) { vv[nt] = (int) carry; }
    vv[nt] = (int) carry; 
    return unsafe(vv); }

  @Override
  public final Natural add (final Natural u) {
    if (u instanceof NaturalLE) { return add((NaturalLE) u); }
    return Natural.super.add(u); }

  //  @Override
  //  public final Natural add (final Natural u) {
  //    if (u instanceof NaturalLE) { return add((NaturalLE) u); }
  //    final int n = Math.max(hiInt(),u.hiInt());
  //    final int[] v = new int[n+1];
  //    long carry = 0L;
  //    int i=0;
  //    for (;i<n;i++) {
  //      final long sum = uword(i) + u.uword(i) + carry;
  //      carry = (sum>>>32);
  //      v[i] = (int) sum; }
  //    if (0L!=carry) { v[n] = (int) carry; }
  //    return unsafe(v); }

  //--------------------------------------------------------------

  private final Natural subtract (final NaturalLE u) {
    //assert 0<=compareTo(u);
    final int nt = hiInt();
    final int nu = u.hiInt();
    //if (0>=nt) { return ZERO; } // u must be zero
    if (0>=nu) { return this; }
    final int[] tt = words();
    final int[] uu = u.words();
    final int[] vv = new int[nt];
    long borrow = 0L;
    int i=0;
    for (;i<nu;i++) {
      final long dif = (unsigned(tt[i])-unsigned(uu[i]))+borrow;
      borrow = (dif>>32);
      vv[i] = (int) dif; }
    for (;i<nt;i++) {
      if (0L==borrow) { break; }
      final long dif = unsigned(tt[i]) + borrow;
      borrow = (dif>>32);
      vv[i] = (int) dif; }
    if (i<nt) { System.arraycopy(tt,i,vv,i,nt-i); }
    //assert 0L==borrow;
    return unsafe(vv); }

  @Override
  public final Natural subtract (final Natural u) {
    if (u instanceof NaturalLE) { 
      return subtract((NaturalLE) u); }
    return Natural.super.subtract(u); }

  //  @Override
  //  public final Natural subtract (final Natural u) {
  //    if (u instanceof NaturalLE) { 
  //      return subtract((NaturalLE) u); }
  //    //if (u.isZero()) { return this; }
  //    //assert 0<=compareTo(u);
  //    final int nt = hiInt();
  //    final int nu = u.hiInt();
  //    //assert nu<=nt;
  //    final int[] tt = words();
  //    final int[] vv = new int[nt];
  //    long borrow = 0L;
  //    int i=0;
  //    for (;i<nu;i++) {
  //      final long dif = (unsigned(tt[i])-u.uword(i)) + borrow;
  //      borrow = (dif>>32);
  //      vv[i] = (int) dif; }
  //    //assert n1==i;
  //    for (;i<nt;i++) {
  //      if (0L==borrow) { break; }
  //      final long dif = unsigned(tt[i]) + borrow;
  //      borrow = (dif>>32);
  //      vv[i] = (int) dif; }
  //    for (;i<nt;i++) { vv[i] = tt[i]; }
  //    //assert 0L==borrow;
  //    return unsafe(vv); }

  //--------------------------------------------------------------
  /** From {@link java.math.BigInteger}:
   * <p>
   * The algorithm used here is adapted from Colin Plumb's C
   * library.
   * <p>
   * Technique: Consider the partial products in the
   * multiplication of "abcde" by itself:
   *<pre>
   * a b c d e
   * * a b c d e
   * ==================
   * ae be ce de ee
   * ad bd cd dd de
   * ac bc cc cd ce
   * ab bb bc bd be
   * aa ab ac ad ae
   * </pre>
   * Note that everything above the main diagonal:
   * <pre>
   * ae be ce de = (abcd) * e
   * ad bd cd = (abc) * d
   * ac bc = (ab) * c
   * ab = (a) * b
   * </pre>
   * is a copy of everything below the main diagonal:
   * <pre>
   * de
   * cd ce
   * bc bd be
   * ab ac ad ae
   * </pre>
   * Thus, the sum is 2 * (off the diagonal) + diagonal.
   * This is accumulated beginning with the diagonal (which
   * consist of the squares of the digits of the input), which
   * is then divided by two, the off-diagonal added, and 
   * multiplied by two again. The low bit is simply a copy of 
   * the low bit of the input, so it doesn't need special care.
   */

  public final Natural squareSimple () {
    final int n = hiInt();
    final int[] tt = words();
    final int[] vv = new int[2*n];
    // diagonal
    for (int i=0;i<n;i++) {
      final long tti = unsigned(tt[i]);
      final long prod = tti*tti; 
      final int i2 = 2*i;
      vv[i2] = (int) prod;
      vv[i2+1] = (int) Numbers.hiWord(prod); }
    // off diagonal
    for (int i0=0;i0<n;i0++) {
      long prod = 0L;
      long carry = 0L;
      final long tt0 = unsigned(tt[i0]);
      int i2 = 0;
      for (int i1=0;i1<i0;i1++) {
        i2 = i0+i1;
        prod = unsigned(vv[i2]) + carry; 
        carry = Numbers.hiWord(prod); 
        long vvi2 = Numbers.loWord(prod); 
        if (i0!=i1) {
          final long tt1 = unsigned(tt[i1]);
          final long tt01 = tt0*tt1;
          prod = vvi2 + tt01; 
          carry = Numbers.hiWord(prod) + carry;
          vvi2 = Numbers.loWord(prod);
          prod = vvi2 + tt01; 
          carry = Numbers.hiWord(prod) + carry; 
          vv[i2] = (int) prod; } }
      while ((0L!=carry)&&(i2<2*n)) {
        i2++;
        prod = unsigned(vv[i2]) + carry;
        carry = Numbers.hiWord(prod); 
        vv[i2] = (int) prod;  }
      //assert 0L==carry; 
    }
    return unsafe(vv); }

  //  // Slow version, computes off-diagonals twice
  //  public final Natural squareSimple () {
  //    final int n = hiInt();
  //    final int[] tt = words();
  //    final int[] vv = new int[2*n];
  //    long carry = 0L;
  //    for (int i0=0;i0<n;i0++) {
  //      carry = 0L;
  //      final long tt0 = unsigned(tt[i0]);
  //      for (int i1=0;i1<n;i1++) {
  //        final int i2 = i0+i1;
  //        final long tt1 = unsigned(tt[i1]);
  //        final long prod = (tt0*tt1) + unsigned(vv[i2]) + carry;
  //        vv[i2] = (int) prod;
  //        carry = (prod>>>32); }
  //      final int i2 = i0+n;
  //      vv[i2] = (int) carry; }
  //    return unsafe(vv); }

  //--------------------------------------------------------------
  // Uints
  //--------------------------------------------------------------

  @Override
  public final int startWord () { return 0; }
  @Override
  public final int endWord () { return words().length; }

  @Override
  public final int word (final int i) {
    //assert 0<=i : "Negative index: " + i;
    if (endWord()<=i) { return 0; }
    return words()[i]; }

  @Override
  public final Natural setWord (final int i,
                                final int w) {
    //assert 0<=i;
    final int n = Math.max(i+1,hiInt());
    final  int[] u = Arrays.copyOf(words(),n);
    u[i] = w;
    return unsafe(u); }

  /** Singleton.<br>
   */
  public static final NaturalLE ZERO = 
    new NaturalLE(new int[0],0); 

  @Override
  public final Natural empty () { return ZERO; }

  //--------------------------------------------------------------

  @Override
  public final Natural from (final long u) {
    //assert 0<=u;
    return valueOf(u);  }

  @Override
  public final Natural from (final long u,
                             final int upShift) {
    //assert 0<=u;
    //assert 0<=upShift;
    if (0L==u) { return zero(); }
    if (0==upShift) { return from(u); }
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    final int hi = (int) (u>>>32);
    final int lo = (int) u;
    final int uu0,uu1,uu2;
    if (0==bShift) { 
      uu0=lo; 
      uu1=hi; 
      uu2=0; }
    else {
      final int rShift = 32-bShift;
      uu0 = (lo<<bShift);
      uu1 = ((hi<<bShift)|(lo>>>rShift));
      uu2 =  (hi>>>rShift); }
    final int n = iShift+3;
    final int[] vv = new int[n+1];
    vv[iShift] = uu0; 
    vv[iShift+1] = uu1; 
    vv[iShift+2] = uu2; 
    return unsafe(vv); }

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

  @Override
  public final Natural shiftUpWords (final int iShift) {
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
  // Is this characteristic of most inputs?

  public static final Generator
  fromDoubleGenerator (final UniformRandomProvider urp) {
    final double dp = 0.9;
    return new GeneratorBase ("fromDoubleGenerator") {
      private final ContinuousSampler choose =
        new ContinuousUniformSampler(urp,0.0,1.0);
      private final Generator g = Doubles.finiteGenerator(urp);
      private final CollectionSampler edgeCases =
        new CollectionSampler(
          urp,
          List.of(
            valueOf(0L),
            valueOf(1L),
            valueOf(2L),
            valueOf(10L),
            valueOf(-1L)));
      @Override
      public Object next () {
        final boolean edge = choose.sample() > dp;
        if (edge) { return edgeCases.sample(); }
        return valueOf(Doubles.significand(g.nextDouble())); } }; }

  /** Intended primarily for testing. <b>
   * Generate enough bytes to at least cover the range of
   * <code>double</code> values.
   */

  public static final Generator
  generator (final UniformRandomProvider urp)  {
    return fromDoubleGenerator(urp); }

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
    final int end = Ints.hiInt(words);
    return unsafe(Arrays.copyOf(words,end)); }

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
    //assert 0<=u.signum();
    return valueOf(u.toByteArray()); }

  //-------------------------------------------------------------

  public static final NaturalLE valueOf (final String s,
                                         final int radix) {
    return make(Ints.littleEndian(s,radix)); }

  public static final NaturalLE valueOf (final String s) {
    return valueOf(s,0x10); }

  /** <code>0L<=u</code>. */
  public static final NaturalLE valueOf (final long u) {
    //assert 0L<=u;
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

