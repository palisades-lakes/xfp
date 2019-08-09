package xfp.java.numbers;

import static java.lang.Integer.compareUnsigned;
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

import xfp.java.Debug;
import xfp.java.exceptions.Exceptions;
import xfp.java.prng.Generator;
import xfp.java.prng.GeneratorBase;

/** immutable arbitrary-precision non-negative integers
 * (natural numbers) represented by little-endian
 * unsigned <code>int[]</code>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-08-09
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

  private final int _loInt;
  @Override
  public final int loInt () { return _loInt; }
  private final int _hiInt;
  @Override
  public final int hiInt () { return _hiInt; }

  //--------------------------------------------------------------

  @Override
  public final int startWord () { return _loInt; }
  @Override
  public final int endWord () { return _hiInt; }

  @Override
  public final int word (final int i) {
    //assert 0<=i : "Negative index: " + i;
    if (i<loInt()) { return 0; }
    if (hiInt()<=i) { return 0; }
    return words()[i]; }

  /** Don't drop trailing zeros. */
  public final int[] copyWords () { 
    return Arrays.copyOf(words(),words().length); }

  @Override
  public final NaturalLE setWord (final int i,
                                  final int w) {
    //assert 0<=i;
    if (0==w) {
      if (i>=hiInt()) { return this; }
      final int[] u = Arrays.copyOf(words(),words().length);
      u[i] = 0;
      return unsafe(u); }
    final int n = Math.max(i+1,hiInt());
    final  int[] u = Arrays.copyOf(words(),n);
    u[i] = w;
    return unsafe(u); }

  /** Singleton.<br>
   */
  public static final NaturalLE ZERO = new NaturalLE(new int[0],0,0); 

  @Override
  public final boolean isZero () { return _loInt==_hiInt; }

  @Override
  public final NaturalLE empty () { return ZERO; }

  @Override
  public final NaturalLE zero () { return ZERO; }

  //--------------------------------------------------------------
  // Natural
  //--------------------------------------------------------------
  // ordering
  //--------------------------------------------------------------

  @Override
  public final int compareTo (final Natural u) {
    final int b0 = hiBit();
    final int b1 = u.hiBit();
    if (b0<b1) { return -1; }
    if (b0>b1) { return 1; }
    final int start = Math.max(loInt(),u.loInt());
    int i = hiInt()-1;
    for (;i>=start;i--) {
      final long u0i = uword(i);
      final long u1i = u.uword(i);
      if (u0i<u1i) { return -1; }
      if (u0i>u1i) { return 1; } }
    for (;i>=loInt();i--) { if (0!=word(i)) { return 1; } }
    for (;i>=u.loInt();i--) { if (0!=u.word(i)) { return -1; } }
    return 0; }

  @Override
  public final int compareTo (final Natural u,
                              final int upShift) {
    //assert 0<=upShift;
    if (0==upShift) { return compareTo(u); }
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1F);
    if (0!=bShift) { return compareTo(u.shiftUp(upShift)); }
    final int n0 = hiInt();
    final int n1 = u.hiInt()+iShift;
    if (n0 < n1) { return -1; }
    if (n0 > n1) { return 1; }
    // TODO: is this faster than unsigned long conversion?
    int i = n0-1;
    final int m = Math.min(loInt(),u.loInt()+iShift);
    for (; i>m; i--) {
      final int c = compareUnsigned(word(i),u.word(i-iShift));
      if (0!=c) { return c; } }
    for (;i>=0;i--) { if (0!=word(i)) { return 1; } }
    return 0; }

  @Override
  public final  int compareTo (final long u) {
    //assert isValid();
    //assert 0L<=u;
    final int n0 = hiInt();
    final int lo1 = (int) u;
    final int hi1 = (int) (u>>>32);
    final int n1 = ((0!=hi1) ? 2 : (0L!=lo1) ? 1 : 0);
    if (n0<n1) { return -1; }
    if (n0>n1) { return 1; }
    final int hi0 = word(1);
    final int chi = compareUnsigned(hi0,hi1);
    if (0!=chi) { return chi; }
    final int lo0 = word(0);
    final int clo = compareUnsigned(lo0,lo1);
    if (0!=clo) { return clo; }
    return 0; }

  @Override
  public final int compareTo (final long u,
                              final int upShift) {
    //assert isValid();
    //assert 0L<=u;
    //assert 0<=upShift : "upShift=" + upShift;

    if (0L==u) { return (isZero() ? 0 : 1); }
    if (0==upShift) { return compareTo(u); }

    final int m0 = hiBit();
    final int m1 = Numbers.hiBit(u) + upShift;
    if (m0<m1) { return -1; }
    if (m0>m1) { return 1; }

    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);

    // compare non-zero words from u<<upShift
    if (0==bShift) {
      final int hi0 = word(iShift+1);
      final int hi1 = (int) (u>>>32);
      final int chi = compareUnsigned(hi0,hi1);
      if (0!=chi) { return chi; }
      final int lo0 = word(iShift);
      final int lo1 = (int) u;
      final int clo = compareUnsigned(lo0,lo1);
      if (0!=clo) { return clo; } }
    else {
      // most significant word in u<<upShift
      final int hi0 = word(iShift+2);
      final int hi1 = (int) (u>>>(64-bShift));
      final int chi = compareUnsigned(hi0,hi1);
      if (0!=chi) { return chi; }

      final long us = (u<<bShift);
      final int mid0 = word(iShift+1);
      final int mid1 = (int) (us>>>32);
      final int cmid = compareUnsigned(mid0,mid1);
      if (0!=cmid) { return cmid; }

      final int lo0 = word(iShift);
      final int lo1 = (int) us;
      final int clo = compareUnsigned(lo0,lo1);
      if (0!=clo) { return clo; } }

    // check this for any non-zero words in zeros of u<<upShift
    for (int i=iShift-1;i>=startWord();i--) {
      if (0!=word(i)) { return 1; } }

    return 0; }

  //--------------------------------------------------------------
  // long based factories
  //--------------------------------------------------------------

  @Override
  public final  NaturalLE sum (final long u,
                               final long v) {
    //assert isValid();
    //assert 0L<=u;
    if (0L==u) { return from(v); }
    if (0L==v) { return from(u); }
    long sum = loWord(u) + loWord(v);
    final int w0 = (int) sum;
    sum = hiWord(u) + hiWord(v) + (sum>>>32);
    final int w1 = (int) sum;
    final int w2 = (int) (sum>>>32);
    if (0L!=w2) { return unsafe(new int[] {w0,w1,w2}); }
    if (0L!=w1) { return unsafe(new int[] {w0,w1}); }
    return unsafe(new int[] {w0},0,1); }

  @Override
  public final  NaturalLE sum (final long u,
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
  public final NaturalLE difference (final long u,
                                     final long v) {
    //assert isValid();
    //assert 0L<=u;
    //assert 0L<=v;
    //assert compareTo(u,v)>=0;
    if (0L==u) { 
      //assert 0L==v;
      return zero(); }
    if (0L==v) { return from(u); }
    final long duv = u-v;
    // assert 0L<=duv;
    return from(duv); }

  @Override
  public final NaturalLE difference (final long u,
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

  @Override
  public final NaturalLE difference (final long v,
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
    // TODO: should these be int?
    final long vlo = loWord(v);
    final long vhi = hiWord(v);
    final int vv0,vv1,vv2;
    if (0==bShift) { vv0=(int)vlo; vv1=(int)vhi; vv2=0; }
    else {
      final int rShift = 32-bShift;
      vv0 = (int) (vlo<<bShift);
      vv1 = (int) ((vhi<<bShift)|(vlo>>>rShift));
      vv2 = (int) (vhi>>>rShift); }
    int i = iShift;
    final int[] ww = new int[n];
    ww[i++] = vv0; ww[i++] = vv1; ww[i] = vv2;
    long dif = (unsigned(ww[0])-loWord(u));
    ww[0] = (int) dif;
    dif = (dif>>32);
    dif += (unsigned(ww[1])-hiWord(u));
    ww[1] = (int) dif;
    dif = (dif>>32);
    i=2;
    for (;i<n;i++) {
      if (0L==dif) { break; }
      dif += unsigned(ww[i]);
      ww[i] = (int) dif; 
      dif = (dif>>32); }
    //assert 0L==dif;
    return unsafe(ww); }

  //--------------------------------------------------------------
  // TODO: fix lurking overflow issue
  // probably only works as long as t0,t1 double significands

  @Override
  public final NaturalLE product (final long t0,
                                  final long t1) {
    //assert isValid();
    //assert 0L<=t0;
    //assert 0L<=t1;
    if ((0L==t0||(0L==t1))) { return zero(); }
    final long hi0 = hiWord(t0);
    final long lo0 = loWord(t0);
    final long hi1 = hiWord(t1);
    final long lo1 = loWord(t1);
    final long lolo = lo0*lo1;
    final long hilo2 = (hi0*lo1) + (hi1*lo0);
    //final long hilo2 = Math.addExact(hi0*lo1,hi1*lo0);
    final long hihi = hi0*hi1;
    long sum = lolo;
    final int w0 = (int) sum;
    sum = (sum>>>32) + hilo2;
    final int w1 = (int) sum;
    sum = (sum>>>32) + hihi ;
    final int w2 = (int) sum;
    final int w3 = (int) (sum>>>32);
    if (0!=w3) { return unsafe(new int[] { w0,w1,w2,w3, }); }
    if (0!=w2) { return unsafe(new int[] { w0,w1,w2, }); }
    if (0!=w1) { 
      return unsafe(new int[] { w0,w1, }, (0==w0)?1:0,2); }
    return unsafe(new int[] { w0, },0,1); }

  // TODO: fix lurking overflow issue
  // probably only works as long as t is double significand

  @Override
  public final NaturalLE fromSquare (final long t) {
    //assert isValid();
    //assert 0L<=t;
    if (0L==t) { return zero(); }
    final long hi = hiWord(t);
    final long lo = loWord(t);
    final long lolo = lo*lo;
    final long hilo2 = ((hi*lo)<<1);
    //final long hilo2 = Math.multiplyExact(2,hi*lo);
    final long hihi = hi*hi;
    long sum = lolo;
    final int w0 = (int) sum;
    sum = (sum>>>32) + hilo2;
    final int w1 = (int) sum;
    sum = (sum>>>32) + hihi ;
    final int w2 = (int) sum;
    final int w3 = (int) (sum>>>32);

    if (0!=w3) { return unsafe(new int[] { w0,w1,w2,w3, }); }
    if (0!=w2) { return unsafe(new int[] { w0,w1,w2, }); }
    if (0!=w1) { 
      return unsafe(new int[] {w0,w1}, (0==w0)?1:0,2); }
    return unsafe(new int[] {w0},0,1); }

  //--------------------------------------------------------------
  // add longs
  //--------------------------------------------------------------

  @Override
  public final NaturalLE add (final long u) {
    //assert isValid();
    //assert 0L<=u;
    if (0L==u) { return this; }
    if (isZero()) { return from(u); }
    final int n = hiInt();
    final int[] vv = new int[Math.max(2,n+1)];
    long sum = loWord(u);
    if (0<n) { sum += uword(0); }
    vv[0] = (int) sum;
    sum = (sum>>>32);
    sum += hiWord(u);
    if (1<n) { sum += uword(1); }
    vv[1] = (int) sum;
    sum = (sum>>>32);
    int i=2;
    for (;i<n;i++) {
      if (0L==sum) { break; }
      sum += uword(i);
      vv[i] = (int) sum;
      sum = (sum>>>32); }
    for (;i<n;i++) { vv[i] = word(i); }
    if (0L!=sum) { vv[n] = (int) sum; }
    return unsafe(vv); }

  //--------------------------------------------------------------

  private final NaturalLE addWithWordShift (final long u,
                                            final int iShift) {
    final int nt = hiInt();
    final int nu = iShift+2;
    final int n = Math.max(nt,nu);
    final int[] vv = new int[n+1];
    for (int i=0;i<Math.min(iShift,nt);i++) { vv[i] = word(i); }

    int i=iShift;
    final long uu0 = loWord(u);
    final long uu1 = hiWord(u);
    long sum = uu0;
    if (i<nt) { sum += uword(i); }
    vv[i++] = (int) sum; 
    sum = uu1 + (sum>>>32);
    if (i<nt) { sum += uword(i); }
    vv[i++] = (int) sum; 
    sum = (sum>>>32);

    for (;i<nt;i++) {
      if (0L==sum) { break; }
      sum = uword(i) + sum;
      vv[i] = (int) sum; 
      sum = (sum>>>32); }
    for (int j=i;j<nt;j++) { vv[j] = word(j); }
    if (0L!=sum) { vv[n] = (int) sum; }
    return unsafe(vv); }

  @Override
  public final NaturalLE add (final long u,
                              final int upShift) {
    //assert 0<=u;
    //assert 0<=upShift;
    if (isZero()) { return from(u,upShift); }
    if (0L==u) { return this; }
    if (0==upShift) { return add(u); }

    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    if (0==bShift) { return addWithWordShift(u,iShift); }

    final int nt = hiInt();
    final int nu = iShift+2;
    final int n = Math.max(nt,nu);
    final int[] vv = new int[n+1];
    for (int i=loInt();i<Math.min(iShift,nt);i++) { vv[i] = word(i); }

    final int uhi = (int) (u>>>32);
    final int ulo = (int) u;
    final int rShift = 32-bShift;
    final long uu0 = unsigned(ulo<<bShift);
    final long uu1 = unsigned((uhi<<bShift)|(ulo>>>rShift));
    final long uu2 = unsigned(uhi>>>rShift); 
    int i=iShift;
    long sum = uu0;
    if (i<nt) { sum += uword(i); }
    vv[i++] = (int) sum; 
    sum = (sum>>>32);
    sum = uu1 + sum;
    if (i<nt) { sum += uword(i); }
    vv[i++] = (int) sum; 
    sum = (sum>>>32);
    sum = uu2 + sum;
    if (i<nt) { sum += uword(i); }
    vv[i++] = (int) sum; 
    sum = (sum>>>32);

    for (;i<nt;i++) {
      if (0L==sum) { break; }
      sum = uword(i) + sum;
      vv[i] = (int) sum; 
      sum = (sum>>>32); }
    for (int j=i;j<nt;j++) { vv[j] = word(j); }
    if (0L!=sum) { vv[n] = (int) sum; }
    return unsafe(vv); }

  //--------------------------------------------------------------
  // subtract longs
  //--------------------------------------------------------------

  @Override
  public final NaturalLE subtract (final long u) {
    //assert isValid();
    //assert 0L<=u;
    //assert 0<=compareTo(u);
    if (0L==u) { return this; }
    final int n = hiInt();
    final int[] vv = new int[n];
    // at least 1 element in tt or u==0
    long dif = uword(0)-loWord(u);
    vv[0] = (int) dif;
    dif = (dif>>32);
    if (1<n) {
      dif = (uword(1)-hiWord(u))+dif;
      vv[1] = (int) dif;
      dif = (dif>>32);
      int i=2;
      for (;i<n;i++) {
        if (0L==dif) { break; }
        dif = uword(i)+dif;
        vv[i] = (int) dif;
        dif = (dif>>32); }
      for (;i<n;i++) { vv[i] = word(i); } }
    //assert 0L==dif : dif;
    return unsafe(vv); }

  //--------------------------------------------------------------
  // TODO: extract bodies as 2 middle routines that return i,
  // share outer code in main method.

  private final NaturalLE subtractWithWordShift (final long u,
                                                 final int iShift) {
    //assert 0L<=u;
    //assert 0<=iShift;

    final int nt = hiInt();
    final int[] vv = new int[nt];
    // assert iShift<=n || 0L==u
    for (int i=loInt();i<iShift;i++) { vv[i] = word(i); }

    final long uu0 = loWord(u);
    final long uu1 = hiWord(u);
    int i=iShift;
    long dif = -uu0;
    if (i<nt) { dif += uword(i); }
    vv[i++] = (int) dif; 
    dif = (dif>>32);
    if (i<nt) { // or 0L=uu1
      final long u1 = uword(i)-uu1;
      dif = u1 + dif;
      vv[i++] = (int) dif; 
      dif = (dif>>32); 
      for (;i<nt;i++) {
        if (0L==dif) { break; }
        dif = uword(i) + dif;
        vv[i] = (int) dif; 
        dif = (dif>>32); }
      //assert 0L==dif;
      for (int j=i;j<nt;j++) { vv[j] = word(j); } }
    return unsafe(vv); }

  @Override
  public final NaturalLE subtract (final long u,
                                   final int upShift) {
//    Debug.println("subtract: " + 
//      words().length + ", " + loInt() + ", " + hiInt() 
//      + ", " + (upShift>>>5));
    //Debug.println(toString());
    //Debug.println(Long.toHexString(u));
    //assert 0L<=u;
    //assert 0<=upShift;
    if (0L==u) { return this; }
    if (0==upShift) { return subtract(u); }
    //if (isZero()) { assert 0L==u; return this; }
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    if (0==bShift) { return subtractWithWordShift(u,iShift); }

    final int nt = hiInt();
    final int[] vv = new int[nt];
    // assert iShift<=n || 0L==u
    for (int i=loInt();i<iShift;i++) { vv[i] = word(i); }

    final int uhi = (int) (u>>>32);
    final int ulo = (int) u;
    final int rShift = 32-bShift;
    final long uu0 = unsigned(ulo<<bShift);
    final long uu1 = unsigned((uhi<<bShift)|(ulo>>>rShift));
    final long uu2 = unsigned(uhi>>>rShift); 
    int i=iShift;
    long dif = -uu0;
    if (i<nt) { dif += uword(i); }
    vv[i++] = (int) dif; 
    dif = (dif>>32);
    if (i<nt) { // or must have 0L=uu1
      dif = uword(i)-uu1 + dif;
      vv[i++] = (int) dif; 
      dif = (dif>>32); }
    if (i<nt) { // or 0L=uu2
      final long u2 = uword(i)-uu2;
      final long dif2 = u2 + dif;
      vv[i++] = (int) dif2; 
      dif = (dif2>>32); }
    for (;i<nt;i++) {
      if (0L==dif) { break; }
      dif = uword(i) + dif;
      vv[i] = (int) dif; 
      dif = (dif>>32); } 
    //assert 0L==dif;

    for (int j=i;j<nt;j++) { vv[j] = word(j); } 
    return unsafe(vv); }

  //--------------------------------------------------------------

  @Override
  public final NaturalLE subtractFrom (final long u) {
    //assert 0L<=u;
    //assert 0>=compareTo(u);
    //if (0L==u) { return this; }
    // at least 1 element in tt or u==0
    long dif = loWord(u)-uword(0);
    final int vv0 = (int) dif;
    dif = (hiWord(u)-uword(1))+(dif>>32);
    final int vv1 = (int) dif;
    //assert 0L== (dif>>32) :  (dif>>32);
    if (0==vv1) { return unsafe(new int[] {vv0}); }
    return unsafe(new int[] {vv0,vv1}); }


  //--------------------------------------------------------------
  // TODO: extract bodies as 2 middle routines that return i,
  // share outer code in main method.

  private final NaturalLE subtractFromWithWordShift (final long u,
                                                     final int iShift) {
    //assert 0L<=u;
    //assert 0<=iShift;

    final int nt = hiInt();
    final int[] vv = new int[iShift+2];
    // assert iShift<=n || 0L==u
    long dif = 0;
    int i = loInt();
    for (;i<Math.min(nt,iShift);i++) { 
      dif -= uword(i);
      vv[i] = (int) dif;
      dif = (dif>>32); }
    for (;i<iShift;i++) { 
      vv[i] = (int) dif;
      dif = (dif>>32); }

    final long lo = loWord(u);
    final long hi = hiWord(u);
    dif += lo;
    if (i<nt) { dif -= uword(i); }
    vv[i++] = (int) dif; 
    dif = (dif>>32);
    dif += hi;
    if (i<nt) { dif -= uword(i); }
    vv[i++] = (int) dif; 
    //assert 0L==(dif>>32);
    return unsafe(vv); }

  @Override
  public final NaturalLE subtractFrom (final long u,
                                       final int upShift) {
    //assert 0L<=u;
    //assert 0<=upShift;
    //if (0L==u) { return this; }
    if (0==upShift) { return subtractFrom(u); }
    if (isZero()) { return from(u,upShift); }
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    if (0==bShift) { return subtractFromWithWordShift(u,iShift); }

    final int nt = hiInt();
    final int[] vv = new int[iShift+3];
    // assert iShift<=n || 0L==u
    long dif = 0;
    int i=loInt();
    for (;i<Math.min(nt,iShift);i++) { 
      dif -= uword(i);
      vv[i] = (int) dif;
      dif = (dif>>32); }
    for (;i<iShift;i++) { 
      if(0L==dif) { break; }
      vv[i] = (int) dif;
      dif = (dif>>32); }
    i=iShift;
    final int hi = (int) (u>>>32);
    final int lo = (int) u;
    final int rShift = 32-bShift;
    dif += unsigned(lo<<bShift);
    if (i<nt) { dif -= uword(i); }
    vv[i++] = (int) dif; 
    dif = (dif>>32);
    dif += unsigned((hi<<bShift)|(lo>>>rShift));
    if (i<nt) { dif -= uword(i); }
    vv[i++] = (int) dif; 
    dif = (dif>>32);
    dif += unsigned(hi>>>rShift);
    if (i<nt) { dif -= uword(i); }
    vv[i++] = (int) dif; 
    //assert 0L==(dif>>32);
    return unsafe(vv); }

  //--------------------------------------------------------------
  // arithmetic with shifted Naturals
  //--------------------------------------------------------------
  /** <code>add(u<<(32*iShift))</code> */

  private final NaturalLE addWords (final Natural u,
                                    final int iShift) {
    //assert 0<=iShift;
    final int n0 = hiInt();
    final int n1 = u.hiInt()+iShift+1;
    final int n = Math.max(n0,n1);
    final int[] vv = new int[n];
    int i=loInt();
    for (;i<Math.min(n0,iShift);i++) { vv[i] = word(i); }
    i=iShift;
    long sum = 0L;
    for (;i<n1;i++) {
      final long ui = u.uword(i-iShift);
      sum = uword(i) + ui + sum;
      vv[i] = (int) sum; 
      sum = (sum>>>32); }
    for (;i<n0;i++) { 
      if(0L==sum) { break; }
      sum = uword(i) + sum;
      vv[i] = (int) sum;  
      sum = (sum>>>32); }
    //assert 0L==sum;
    for (;i<n0;i++) { vv[i] = word(i); }
    return unsafe(vv); }

  @Override
  public final NaturalLE add (final Natural u,
                              final int upShift) {
    //assert 0<=upShift;
    if (0==upShift) { return add(u); }
    //if (isZero()) { return u.shiftUp(upShift); }
    //if (u.isZero()) { return this; }
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    if (0==bShift) { return addWords(u,iShift); }
    final int rShift = 32-bShift;
    final int n0 = hiInt();
    final int n1 = u.hiInt()+iShift+1;
    final int n = Math.max(n0,n1);
    final int[] vv = new int[n];
    int i=loInt();
    for (;i<Math.min(n0,iShift);i++) { vv[i] = word(i); }
    i=iShift;
    long sum = 0L;
    int u0 = 0;
    for (;i<n1;i++) {
      final int u1 = u.word(i-iShift);
      final int ui = ((u1<<bShift)|(u0>>>rShift));
      u0 = u1;
      sum += uword(i) + unsigned(ui);
      vv[i] = (int) sum; 
      sum = (sum>>>32); }
    for (;i<n0;i++) { 
      if(0L==sum) { break; }
      sum += uword(i);
      vv[i] = (int) sum; 
      sum = (sum>>>32); }
    //assert 0L==sum;
    for (;i<n0;i++) { vv[i] = word(i); }
    return unsafe(vv); }

  //--------------------------------------------------------------

  @Override
  public final NaturalLE subtract (final Natural u,
                                   final int upShift) {
    //assert 0<=upShift;
    if (isZero()) { assert u.isZero(); return zero(); }
    if (u.isZero()) { return this; }
    if (0==upShift) { return subtract(u); }
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    final int rShift = 32-bShift;
    final int nt = hiInt();
    final int nu = u.hiInt()+iShift;
    //assert nu<=nt : nu + " <= " + nt;
    final int[] vv = new int[nt];
    for (int i=loInt();i<iShift;i++) { vv[i] = word(i); }
    int i=iShift;
    long dif = 0L;
    int u0 = 0;
    for (;i<nu;i++) {
      final int u1 = ((i<nu) ? u.word(i-iShift) : 0);
      final long ui = 
        unsigned(
          ((bShift==0) ? u1 : ((u1<<bShift)|(u0>>>rShift))));
      u0 = u1;
      if (i>=nt) { 
        //assert 0L==dif; 
        break; }
      dif += uword(i)-ui;
      vv[i] = (int) dif; 
      dif = (dif>>32); }
    //assert i==nu;
    if (nu<nt) {
      final long ui = unsigned(((bShift==0) ? 0 : (u0>>>rShift)));
      dif += uword(nu)-ui;
      vv[nu] = (int) dif; 
      dif = (dif>>32); }
    i=nu+1;
    for (;i<nt;i++) {
      if (0L==dif) { break; }
      dif += uword(i);
      vv[i] = (int) dif; 
      dif = (dif>>32); }
    for (int j=i;j<nt;j++) { vv[j] = word(j); }
    //assert (0L==dif);
    return unsafe(vv); }

  //--------------------------------------------------------------
  // Ringlike
  //--------------------------------------------------------------

  @Override
  public final NaturalLE add (final Natural u) {
    final int nt = hiInt();
    final int nu = u.hiInt();
    if (nt<nu) { return (NaturalLE) u.add(this); }
    final int[] vv = new int[nt+1];
    long sum = 0L;
    int i=0;
    for (;i<nu;i++) {
      sum += uword(i) + u.uword(i);
      vv[i] = (int) sum; 
      sum = (sum>>>32);}
    for (;i<nt;i++) {
      if (0L==sum) { break; }
      sum += uword(i);
      vv[i] = (int) sum; 
      sum = (sum>>>32);}
    for (int j=i;j<nt;j++) { vv[j] = word(j); }
    vv[nt] = (int) sum; 
    return unsafe(vv); }

  //--------------------------------------------------------------

  @Override
  public final NaturalLE subtract (final Natural u) {
    //assert 0<=compareTo(u);
    final int nt = hiInt();
    final int nu = u.hiInt();
    //if (0>=nt) { return ZERO; } // u must be zero
    if (0>=nu) { return this; }
    final int[] vv = new int[nt];
    long dif = 0L;
    int i=0;
    for (;i<nu;i++) {
      dif += uword(i)-u.uword(i);
      vv[i] = (int) dif;
      dif= (dif>>32); }
    for (;i<nt;i++) {
      if (0L==dif) { break; }
      dif += uword(i);
      vv[i] = (int) dif;
      dif = (dif>>32); }
    //assert 0L==dif;
    for (int j=i;j<nt;j++) { vv[j] = word(j); }
    return unsafe(vv); }

  //--------------------------------------------------------------
  // multiplicative monoid
  //--------------------------------------------------------------
  // TODO: singleton class for one() and zero()?

  private static final NaturalLE ONE = unsafe(new int[] {1},0,1);

  @Override
  public final NaturalLE one () { return ONE; }

  @Override
  public final NaturalLE ones (final int n) {
    throw Exceptions.unsupportedOperation(this,"ones",n); }

  @Override
  public final boolean isOne () {
    //assert isValid();
    if (0!=loInt()) { return false; }
    if (1!=hiInt()) { return false; }
    if (1!=words()[0]) { return false; }
    return true; }

  //--------------------------------------------------------------
  // square
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

  public final NaturalLE squareSimple () {
    final int n = hiInt();
    final int[] vv = new int[2*n];
    // diagonal
    for (int i=0;i<n;i++) {
      final long tti = uword(i);
      final long prod = tti*tti; 
      final int i2 = 2*i;
      vv[i2] = (int) prod;
      vv[i2+1] = (int) hiWord(prod); }
    // off diagonal
    for (int i0=0;i0<n;i0++) {
      long prod = 0L;
      long carry = 0L;
      final long tt0 = uword(i0);
      int i2 = 0;
      for (int i1=0;i1<i0;i1++) {
        i2 = i0+i1;
        prod = unsigned(vv[i2]) + carry; 
        carry = hiWord(prod); 
        long vvi2 = loWord(prod); 
        if (i0!=i1) {
          final long tt1 = uword(i1);
          final long tt01 = tt0*tt1;
          prod = vvi2 + tt01; 
          carry = hiWord(prod) + carry;
          vvi2 = loWord(prod);
          prod = vvi2 + tt01; 
          carry = hiWord(prod) + carry; 
          vv[i2] = (int) prod; } }
      while ((0L!=carry)&&(i2<2*n)) {
        i2++;
        prod = unsigned(vv[i2]) + carry;
        carry = Numbers.hiWord(prod); 
        vv[i2] = (int) prod;  }
      //assert 0L==carry; 
    }
    return unsafe(vv); }

  //--------------------------------------------------------------
  // multiply
  //--------------------------------------------------------------

  //--------------------------------------------------------------
  // divide
  //--------------------------------------------------------------

  //--------------------------------------------------------------
  // gcd
  //--------------------------------------------------------------

  //--------------------------------------------------------------
  // Uints
  //--------------------------------------------------------------

  @Override
  public final NaturalLE from (final long u) {
    //assert 0<=u;
    return valueOf(u);  }

  @Override
  public final NaturalLE from (final long u,
                               final int upShift) {
    //assert 0<=u;
    //assert 0<=upShift;
    if (0L==u) { return zero(); }
    if (0==upShift) { return from(u); }
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    if (0==bShift) { 
      final int[] vv = new int[iShift+2];
      vv[iShift] = (int) u;
      vv[iShift+1] = (int) (u>>>32);
      return unsafe(vv); }
    final int rShift = 32-bShift;
    final int lo = (int) u;
    final int hi = (int) (u>>>32);
    final int[] vv = new int[iShift+3];
    vv[iShift] = (lo<<bShift);
    vv[iShift+1] = ((hi<<bShift)|(lo>>>rShift));
    vv[iShift+2] =  (hi>>>rShift); 
    return unsafe(vv); }

  //--------------------------------------------------------------

  @Override
  public final NaturalLE shiftDownWords (final int iShift) {
    //assert isValid();
    //assert 0<=iShift;
    if (0==iShift) { return this; }
    if (isZero()) { return this; }
    final int n0 = hiInt();
    final int n1 = n0-iShift;
    if (0>=n1) { return empty(); }
    final int[] u = new int[n1];
    for (int i=0;i<n1;i++) { u[i] = word(i+iShift); }
    return unsafe(u); }

  @Override
  public final NaturalLE shiftDown (final int shift) {
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
  public final NaturalLE shiftUpWords (final int iShift) {
    //assert isValid();
    //assert 0<=iShift;
    if (0==iShift) { return this; }
    if (isZero()) { return this; }
    final int n = hiInt();
    if (0==n) { return this; }
    final int[] u = new int[n+iShift];
    for (int i=loInt();i<n;i++) { u[i+iShift] = word(i); }
    return unsafe(u); }

  @Override
  public final NaturalLE shiftUp (final int shift) {
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
    for (int i=loInt();i<n0;i++) { 
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
  public final NaturalLEMutable recyclable (final Natural init) {
    return NaturalLEMutable.copy(init); }

  @Override
  public final NaturalLEMutable recyclable (final int init) {
    return NaturalLEMutable.make(init); }

  @Override
  public final NaturalLEMutable recyclable (final Natural init,
                                            final int nWords) {
    if (null==init) {
      return NaturalLEMutable.make(nWords); }
    if (init instanceof NaturalLE) {
      return NaturalLEMutable.make(words(),nWords); }
    return (NaturalLEMutable) init.recyclable(init,nWords); }

  @Override
  public boolean isImmutable () { return true; }

  @Override
  public final NaturalLE recycle () { return this; }

  @Override
  public final NaturalLE immutable () { return this; }

  @Override
  public final NaturalLE copy () {  return this; }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { return uintsHashCode(); }

  @Override
  public final boolean equals (final Object x) {
    if (x==this) { return true; }
    if (!(x instanceof NaturalLE)) { return false; }
    return uintsEquals((NaturalLE) x); }

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
            ZERO,
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
  /** UNSAFE: doesn't copy <code>words</code> or check 
   * <code>loInt</code> or <code>hiInt</code.
   */

  public static int instances = 0;
  public static int zeroLo = 0;
  
  private NaturalLE (final int[] words,
                     final int loInt,
                     final int hiInt) { 
    instances++;
    if (0==loInt) { zeroLo++; }
    //assert 0<=loInt;
    //assert loInt<=hiInt : 
    //  "\n" + loInt + "<=" + hiInt 
    //  + "\n" + Arrays.toString(words);
    //assert hiInt<=words.length;
    //Debug.println("init:" + words.length + ", " + loInt + ", " + hiInt);
    _words = words; 
    _loInt = loInt; 
    _hiInt = hiInt; }

  /** Doesn't copy <code>words</code> or check <code>loInt</code>
   * or <code>hiInt</code>. 
   */

  private static final NaturalLE unsafe (final int[] words,
                                         final int loInt,
                                         final int hiInt){
    return new NaturalLE(words,loInt,hiInt); }
  /** Doesn't copy <code>words</code>. 
   */

  static final NaturalLE unsafe (final int[] words) {
    final int lo = Ints.loInt(words);
    final int hi = Ints.hiInt(words);
    return unsafe(words,lo,hi); }

  /** Copy <code>words</code>. 
   *  */
  public static final NaturalLE make (final int[] words) {
    final int end = Ints.hiInt(words);
    final int start = Ints.loInt(words);
    return unsafe(Arrays.copyOf(words,end),start,end); }

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
    final int lo = (int) u;
    final int hi = (int) (u>>>32);
    if (0==hi) { return unsafe(new int[] {lo},0,1); }
    if (0==lo) { return unsafe(new int[] { lo,hi },1,2); }
    return unsafe(new int[] { lo,hi },0,2); }

  /** Return a {@link NaturalLE} equivalent to the unsigned 
   * value of <code>u</code>.
   */
  public static final NaturalLE valueOf (final int u) {
    if (u==0) { return ZERO; }
    return unsafe(new int[] {u},0,1); }

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
    for (int i=u.loInt();i<n;i++) { w[i] = u.word(i); }
    return unsafe(w,u.loInt(),n); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

