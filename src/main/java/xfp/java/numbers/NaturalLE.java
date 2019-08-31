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

import xfp.java.exceptions.Exceptions;
import xfp.java.prng.Generator;
import xfp.java.prng.GeneratorBase;

/** immutable arbitrary-precision non-negative integers
 * (natural numbers) represented by little-endian
 * unsigned <code>int[]</code>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-08-30
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

  //  private final int _loInt;
  //  @Override
  //  public final int loInt () { return _loInt; }

  private final int _hiInt;
  @Override
  public final int hiInt () { return _hiInt; }
//  @Override
//  public final int hiInt () { return _words.length; }

  //--------------------------------------------------------------

  @Override
  public final int startWord () { return 0; }
  @Override
  public final int endWord () { return hiInt(); }

  @Override
  public final int word (final int i) {
    //assert 0<=i : "Negative index: " + i;
    if (hiInt()<=i) { return 0; }
    return _words[i]; }

  @Override
  public final long uword (final int i) {
    //assert 0<=i : "Negative index: " + i;
    if (hiInt()<=i) { return 0L; }
    return unsigned(_words[i]); }

  public final int[] copyWords () { 
    return Arrays.copyOf(words(),hiInt()); }

  //--------------------------------------------------------------
  /** Return the <code>[i0,i1)</code> words as a new 
   * <code>NaturalLE</code> with <code>[0,i1-i0)</code> words.
   */

  @Override
  public final NaturalLE words (final int i0,
                         final int i1) {
    //assert 0<=i0;
    //assert i0<i1;
    if ((0==i0) && (hiInt()<=i1)) { return copy(); }
    final int n = Math.max(0,i1-i0);
    if (0>=n) { return zero(); }
    final int[] tt = words();
    final int[] vv = new int[n];
    for (int i=0;i<n;i++) { vv[i] =  tt[i+i0]; }
    return unsafe(vv,n); }

  @Override
  public final NaturalLE setWord (final int i,
                                  final int w) {
    //assert 0<=i;
    if (0==w) {
      if (i>=hiInt()) { return this; }
      final int[] u = Arrays.copyOf(words(),hiInt());
      u[i] = 0;
      return unsafe(u); }
    final int n = Math.max(i+1,hiInt());
    final  int[] u = Arrays.copyOf(words(),n);
    u[i] = w;
    return unsafe(u); }

  /** Singleton.<br>
   */
  public static final NaturalLE ZERO = 
    new NaturalLE(new int[0],0); 

  @Override
  public final boolean isZero () { return 0==hiInt(); }

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
    final int start = Math.max(0,u.startWord());
    int i = hiInt()-1;
    for (;i>=start;i--) {
      final long u0i = uword(i);
      final long u1i = u.uword(i);
      if (u0i<u1i) { return -1; }
      if (u0i>u1i) { return 1; } }
    for (;i>=0;i--) { 
      if (0!=word(i)) { return 1; } }
    for (;i>=u.startWord();i--) { 
      if (0!=u.word(i)) { return -1; } }
    return 0; }

  @Override
  public final int compareTo (final Natural u,
                              final int upShift) {
    return compareTo(u.shiftUp(upShift)); }

  //--------------------------------------------------------------

  @Override
  public final  int compareTo (final long u) {
    //assert 0L<=u;
    final int nt = hiInt();
    final long ulo = loWord(u);
    final long uhi = hiWord(u);
    final int nu = ((0L!=uhi) ? 2 : (0L!=ulo) ? 1 : 0);
    if (nt<nu) { return -1; }
    if (nt>nu) { return 1; }
    final int[] tt = words();
    if (2==nu) { 
      final long tti = unsigned(tt[1]);
      if (tti<uhi) { return -1; }
      if (tti>uhi) { return 1; } }
    if (1<=nu) { 
      final long tti = unsigned(tt[0]);
      if (tti<ulo) { return -1; }
      if (tti>ulo) { return 1; } }
    return 0; }

  //--------------------------------------------------------------

  @Override
  public final int compareTo (final long u,
                              final int upShift) {
    //assert 0L<=u;
    //assert 0<=upShift : "upShift=" + upShift;

    if (0L==u) { return (isZero() ? 0 : 1); }
    if (0==upShift) { return compareTo(u); }

    final int m0 = hiBit();
    final int m1 = Numbers.hiBit(u) + upShift;
    if (m0<m1) { return -1; }
    if (m0>m1) { return 1; }

    //final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1F);
    final int nt = hiInt();
    final int[] tt = words();
    int i = nt-1;
    long tti = unsigned(tt[i--]);
    if (0==bShift) {
      final long uhi = hiWord(u);
      final long ulo = loWord(u);
      if (0L!=uhi) {
        if (tti<uhi) { return -1; }
        if (tti>uhi) { return 1; }
        tti = unsigned(tt[i--]);
        if (tti<ulo) { return -1; }
        if (tti>ulo) { return 1; } }
      else {
        if (tti<ulo) { return -1; }
        if (tti>ulo) { return 1; } } }
    else {
      final long uhi = (u>>>(64-bShift));
      if (0L!=uhi) {
        if (tti<uhi) { return -1; }
        if (tti>uhi) { return 1; }
        tti = unsigned(tt[i--]);
        final long us = (u<<bShift);
        final long umid = hiWord(us);
        if (tti<umid) { return -1; }
        if (tti>umid) { return 1; }
        tti = unsigned(tt[i--]);
        final long ulo = loWord(us);
        if (tti<ulo) { return -1; }
        if (tti>ulo) { return 1; } }
      else {
        final long us = (u<<bShift);
        final long umid = hiWord(us);
        if (0L!=umid) {
          if (tti<umid) { return -1; }
          if (tti>umid) { return 1; }
          tti = unsigned(tt[i--]);
          final long ulo = loWord(us);
          if (tti<ulo) { return -1; }
          if (tti>ulo) { return 1; } }
        else {
          final long ulo = loWord(us);
          if (tti<ulo) { return -1; }
          if (tti>ulo) { return 1; } } } }

    while (i>=0) { if (0!=tt[i--]) { return 1; } }
    return 0; }

  //--------------------------------------------------------------

  @Override
  public final int compareTo (final int upShift,
                              final long u) {
    //assert 0<=upShift : "upShift=" + upShift;
    //assert 0L<=u;

    if (0L==u) { return (isZero() ? 0 : 1); }
    if (0==upShift) { return compareTo(u); }
    if (isZero()) { return Long.compareUnsigned(0,u); }

    // wrong if we don't exclude this==0
    final int m0 = hiBit() + upShift;
    final int m1 = Numbers.hiBit(u);
    if (m0<m1) { return -1; }
    if (m0>m1) { return 1; }

    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);

    // at most 2 non-zero words in this<<upShift, 
    // from hiBit comparison
    if (0==bShift) { 
      final int hi0 = ((1>=iShift) ? word(1-iShift) : 0);
      final int hi1 = (int) hiWord(u);
      final int chi = compareUnsigned(hi0,hi1);
      if (0!=chi) { return chi; }
      final int lo0 = ((0>=iShift) ? word(0) : 0);
      final int lo1 = (int) u;
      final int clo = compareUnsigned(lo0,lo1);
      return clo; } 

    final int rShift = 32-bShift;
    // hiBit comparison should take care of this
    final long hi = ((1>=iShift) ? uword(1-iShift) : 0L);
    final long lo = ((0>=iShift) ? uword(0) : 0L);
    assert (0L==(hi>>>rShift));
    final int mid0 = (int) ((hi<<bShift)|(lo>>>rShift));
    final int mid1 = (int) hiWord(u);
    final int cmid = compareUnsigned(mid0,mid1);
    if (0!=cmid) { return cmid; }
    final int lo0 = (int) (lo<<bShift);
    final int lo1 = (int) u;
    final int clo = compareUnsigned(lo0,lo1);
    return clo; } 

  //--------------------------------------------------------------
  // long based factories
  //--------------------------------------------------------------

  static final NaturalLE sum (final long u,
                              final long v,
                              final int upShift) {
    //assert 0L<=u;
    //assert 0<=upShift;
    //if (0L==u) { return valueOf(v); }
    //if (0L==v) { return valueOf(u); }
    //if (0==upShift) { return sum(u,v); }
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
    sum = unsigned(ww[i]) + vv1 + hiWord(sum);
    ww[i] = (int) sum;
    i++;
    sum = unsigned(ww[i]) + vv2 + hiWord(sum);
    ww[i] = (int) sum;
    assert 0L==hiWord(sum);
    return unsafe(ww); }

  static final NaturalLE difference (final long u,
                                     final long v,
                                     final int upShift) {
    //assert 0L<=u;
    //assert 0<=upShift;
    // assert upShift<64L;
    //assert 0L<=v;
    //assert compareTo(u,v,upShift)>=0;
    // TODO: overflow?
    final long dm = u-(v<<upShift);
    //assert 0L<=dm;
    return valueOf(dm); }

  static final NaturalLE difference (final long v,
                                     final int upShift,
                                     final long u) {
    //assert 0L<=u;
    //assert 0<=upShift;
    //assert 0L<=v;
    //assert compareTo(u,upShift,v)>=0;
    //    if (0L==v) { 
    //      //assert 0L==v;
    //      return zero(); }
    //if (0==upShift) { return difference(v,u); }
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

  static final NaturalLE product (final long t0,
                                  final long t1) {
    //assert 0L<=t0;
    //assert 0L<=t1;
    //if ((0L==t0||(0L==t1))) { return zero(); }

    final long lo0 = loWord(t0);
    final long lo1 = loWord(t1);
    final long hi0 = hiWord(t0);
    final long hi1 = hiWord(t1);

    long sum = lo0*lo1;
    final int w0 = (int) sum;
    // TODO: fix lurking overflow issue
    // works here because t0,t1 53 bit double significands
    //final long hilo2 = Math.addExact(hi0*lo1,hi1*lo0);
    sum = hiWord(sum) + (hi0*lo1) + (hi1*lo0);
    final int w1 = (int) sum;
    sum = hiWord(sum) + hi0*hi1;
    final int w2 = (int) sum;
    final int w3 = (int) hiWord(sum);
    if (0!=w3) { return unsafe(new int[] {w0,w1,w2,w3,},4); }
    if (0!=w2) { return unsafe(new int[] {w0,w1,w2,},3); }
    if (0!=w1) { return unsafe(new int[] {w0,w1,},2); }
    if (0!=w0) { return unsafe(new int[] {w0,},1); }
    return ZERO; }

  // TODO: fix lurking overflow issue
  // probably only works as long as t is double significand

  static final NaturalLE fromSquare (final long t) {
    //assert 0L<=t;
    //if (0L==t) { return zero(); }
    final long hi = hiWord(t);
    final long lo = loWord(t);
    final long lolo = lo*lo;
    final long hilo2 = ((hi*lo)<<1);
    //final long hilo2 = Math.multiplyExact(2,hi*lo);
    final long hihi = hi*hi;
    long sum = lolo;
    final int w0 = (int) sum;
    sum = hiWord(sum) + hilo2;
    final int w1 = (int) sum;
    sum = hiWord(sum) + hihi ;
    final int w2 = (int) sum;
    final int w3 = (int) hiWord(sum);

    if (0!=w3) { return unsafe(new int[] { w0,w1,w2,w3,},4); }
    if (0!=w2) { return unsafe(new int[] { w0,w1,w2, },3); }
    if (0!=w1) { return unsafe(new int[] {w0,w1},2); }
    if (0!=w0) { return unsafe(new int[] {w0},1); }
    return ZERO; }

  //--------------------------------------------------------------
  // add (non-negative) longs
  //--------------------------------------------------------------

  public final NaturalLE add (final long u) {
    //assert 0L<u;
    //if (0L==u) { return this; }
    final int nt = hiInt();
    //if (0==nt) { return valueOf(u); }
    final long uhi = hiWord(u);
    final long ulo = loWord(u);
    final int nu = ((0L!=uhi)?2:(0L!=ulo)?1:0);
    final int nv = Math.max(nu,nt);
    if (0==nv) { return ZERO; }
    final int[] tt = words();
    final int[] vv = new int[nv];
    long sum = ulo;
    if (0<nt) { sum += unsigned(tt[0]); } 
    vv[0] = (int) sum;
    sum = hiWord(sum);
    if (1<nv) { 
      sum += uhi;
      if (1<nt) { sum += unsigned(tt[1]); }
      vv[1] = (int) sum; 
      sum = hiWord(sum); }

    int i=2;

    for (;i<nt;i++) {
      if (0L==sum) { break; }
      sum += unsigned(tt[i]);
      vv[i] = (int) sum;
      sum = hiWord(sum); }
//    if (0L!=sum) { 
//      final int[] vvv = Arrays.copyOf(vv,nv+1);
//      vvv[nv] = 1; 
//      return unsafe(vvv,nv+1); }
    if (0L!=sum) { 
      //vv[nv] = (int) sum; 
      final int[] vvv = new int[nv+1];
      for (int j=0;j<nv;j++) { vvv[j]=vv[j]; } 
      vvv[nv] = 1; 
      return unsafe(vvv,nv+1); }
    
    for (;i<nt;i++) { vv[i] = tt[i]; }
    return unsafe(vv,nv); }
    
  //--------------------------------------------------------------

  private final NaturalLE addByWords (final long u,
                                      final int iShift) {
    final int nt = hiInt();
    final int[] tt = words();
    final long hi = hiWord(u);
    final int nu = iShift+((0L==hi)?1:2);
    final int nv = Math.max(nt,nu);
    final int[] vv = new int[nv];
    for (int i=0;i<Math.min(iShift,nt);i++) { vv[i] = tt[i]; }
    long sum = loWord(u);
    int i = iShift;
    if (i<nt) { sum += unsigned(tt[i]); }
    vv[i++] = (int) sum;
    sum = hiWord(sum);
    if (i<nu) { 
      sum += hi;
      if (i<nt) { sum += unsigned(tt[i]); }
      vv[i++] = (int) sum; 
      sum = hiWord(sum); }
    for (;i<nt;i++) {
      if (0L==sum) { break; }
      sum += unsigned(tt[i]);
      vv[i] = (int) sum; 
      sum = hiWord(sum); }
    if (0L!=sum) { 
      final int[] vvv = new int[nv+1];
      for (int j=0;j<nv;j++) { vvv[j]=vv[j]; } 
      vvv[nv] = 1; 
      return unsafe(vvv,nv+1); }
    for (;i<nt;i++) { vv[i] = tt[i]; }
    return unsafe(vv,i); }

  private final NaturalLE addByBits (final long u,
                                     final int iShift,
                                     final int bShift) {
    final int nt = hiInt();
    final int[] tt = words();
    final long us = (u<<bShift);
    final long mid = hiWord(us);
    final long hi = (u>>>(64-bShift));
    final int nu = iShift+((0L==hi)?((0L==mid)?1:2):3);
    final int nv = Math.max(nt,nu);
    final int[] vv = new int[nv];
    for (int i=0;i<Math.min(iShift,nt);i++) { vv[i] = tt[i]; }
    long sum = loWord(us);
    int i=iShift;
    if (i<nt) { sum += unsigned(tt[i]); }
    vv[i++] = (int) sum; 
    sum = hiWord(sum);
    if (i<nu) {
      sum += mid;
      if (i<nt) { sum += unsigned(tt[i]); }
      vv[i++] = (int) sum; 
      sum = hiWord(sum);
      if (i<nu) {
        sum += hi;
        if (i<nt) { sum += unsigned(tt[i]); }
        vv[i++] = (int) sum; 
        sum = hiWord(sum); } }

    for (;i<nt;i++) {
      if (0L==sum) { break; }
      sum += unsigned(tt[i]);
      vv[i] = (int) sum; 
      sum = hiWord(sum); }
 
    if (0L!=sum) { 
      final int[] vvv = new int[nv+1];
      for (int j=0;j<nv;j++) { vvv[j]=vv[j]; } 
      vvv[nv] = 1; 
      return unsafe(vvv,nv+1); }
    
    for (;i<nt;i++) { vv[i] = tt[i]; }
    return unsafe(vv,nv); }

  public final NaturalLE add (final long u,
                              final int upShift) {
    //assert 0<u;
    //assert 0<upShift;
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1F);
    if (0==bShift) { return addByWords(u,iShift);}
    return addByBits(u,iShift,bShift); }

  //--------------------------------------------------------------

  private final NaturalLE addByWords (final int iShift,
                                      final long u) {
    final int nt = hiInt();
    final int[] tt = words();
    final int n = Math.max(nt+iShift,2);
    final int[] vv = new int[n+1];
    long sum = loWord(u);
    if (0==iShift) { sum += uword(0); } 
    vv[0] = (int) sum;
    sum = hiWord(sum);  
    sum += hiWord(u);
    if (1>=iShift) { sum += uword(1-iShift); } 
    vv[1] = (int) sum;
    sum = hiWord(sum);  
    int i=2;
    for (;i<nt+iShift;i++) {
      if (0L==sum) { break; }
      if (i>=iShift) { sum += unsigned(tt[i-iShift]); }
      vv[i] = (int) sum;
      sum = hiWord(sum); } 
    vv[i] = (int) sum; 
    return unsafe(vv); }

  private final NaturalLE addByBits (final int iShift,
                                     final int bShift,
                                     final long u) {
    final int nt = hiInt();
    final int[] tt = words();
    final int n = Math.max(nt+iShift,2);
    final int[] vv = new int[n+1];
    final int rShift = 32-bShift;
    int t1 = 0;
    long sum = loWord(u);
    if (0==iShift) { 
      t1 = word(0);
      final int ti = (t1<<bShift);
      sum += unsigned(ti); } 
    vv[0] = (int) sum;
    sum = hiWord(sum);  
    int t0 = t1;
    sum += hiWord(u);
    if (1>=iShift) { 
      t1 = word(1-iShift);
      final int ti = ((t1<<bShift)|(t0>>>rShift));
      t0 = t1;
      sum += unsigned(ti) ; } 
    vv[1] = (int) sum;
    sum = hiWord(sum);  
    int i=2;
    for (;i<=nt+iShift;i++) {
      if (0L==sum) { break; }
      if (i>=iShift) { 
        t1 = tt[(i-iShift)];
        final int ti = ((t1<<bShift)|(t0>>>rShift));
        t0 = t1;
        sum += unsigned(ti) ; } 
      vv[i] = (int) sum;
      sum = hiWord(sum); } 
    i=Math.max(i,iShift);
    for (;i<=nt+iShift;i++) {
      t1 = tt[(i-iShift)];
      final int ti = ((t1<<bShift)|(t0>>>rShift));
      t0 = t1;
      vv[i] = ti; } 
    //assert 0L==sum; 
    return unsafe(vv); }

  public final NaturalLE add (final int upShift,
                              final long u) {
    //assert 0<=upShift;
    //assert 0<=u;
    //if (isZero()) { return from(u); }
    //if (0L==u) { return shiftUp(upShift); }
    //if (0==upShift) { return add(u); }

    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1F);
    if (0==bShift) { return addByWords(iShift,u); }
    return addByBits(iShift,bShift,u); }

  //--------------------------------------------------------------
  // subtract (non-negative) longs
  //--------------------------------------------------------------

  public final NaturalLE subtract (final long u) {
    //assert 0L<=u;
    //assert 0<=compareTo(u);
    //if (0L==u) { return this; }
    final int nt = hiInt();
    final int[] tt = words();
    final int[] vv = new int[nt];
    // at least 1 element in tt or u==0
    long dif = unsigned(tt[0])-loWord(u);
    vv[0] = (int) dif;
    dif = (dif>>32);
    if (1<nt) {
      dif = (unsigned(tt[1])-hiWord(u))+dif;
      vv[1] = (int) dif;
      dif = (dif>>32); }
    int i=2;
    for (;i<nt;i++) {
      if (0L==dif) { break; }
      dif = unsigned(tt[i])+dif;
      vv[i] = (int) dif;
      dif = (dif>>32); }
    for (;i<nt;i++) { vv[i] = tt[i]; } 
    //assert 0L==dif : dif;

    final int nv = Ints.hiInt(vv);
    if (nv==nt) { return unsafe(vv,nv); } 
    final int[] vvv = new int[nv];
    for (int j=0;j<nv;j++) { vvv[j]=vv[j]; }
    return unsafe(vvv,nv); }

  //--------------------------------------------------------------

  private final NaturalLE subtractByWords (final long u,
                                           final int iShift) {
    final int nt = hiInt();
    final int[] tt = words();
    final int[] vv = new int[nt];
    // assert iShift<=n || 0L==u
    for (int i=0;i<iShift;i++) { vv[i] = tt[i]; }

    int i=iShift;
    long dif = unsigned(tt[i])-loWord(u);
    vv[i++] = (int) dif; 
    dif = (dif>>32); 
    if (i<nt) { // else high word is 0
      dif += unsigned(tt[i])-hiWord(u);
      vv[i] = (int) dif; 
      dif = (dif>>32); }

    i = iShift+2;
    for (;i<nt;i++) {
      if (0L==dif) { break; }
      dif += unsigned(tt[i]);
      vv[i] = (int) dif; 
      dif = (dif>>32); } 
    //assert 0L==dif;

    for (;i<nt;i++) { vv[i] = tt[i]; } 

    final int nv = Ints.hiInt(vv);
    if (nv==nt) { return unsafe(vv,nv); } 
    final int[] vvv = new int[nv];
    for (int j=0;j<nv;j++) { vvv[j]=vv[j]; }
    return unsafe(vvv,nv); }

  private final NaturalLE subtractByBits (final long u,
                                          final int iShift,
                                          final int bShift)  {
    final int nt = hiInt();
    // assert iShift<=nt || 0L==u
    final int[] tt = words();
    final int[] vv = new int[nt];
    for (int i=0;i<iShift;i++) { vv[i] = tt[i]; }

    final long us = (u<<bShift);
    int i=iShift;
    long dif = unsigned(tt[i])-loWord(us);
    vv[i++] = (int) dif; 
    dif = (dif>>32); 
    if (i<nt) { // else upper 2 words must be 0
      dif += unsigned(tt[i])-hiWord(us);
      vv[i++] = (int) dif; 
      dif = (dif>>32); 
      if (i<nt) {// else upper word must be 0
        dif += unsigned(tt[i])-(u>>>(64-bShift)); 
        vv[i] = (int) dif; 
        dif = (dif>>32); } }

    i = iShift+3;
    for (;i<nt;i++) {
      if (0L==dif) { break; }
      dif += unsigned(tt[i]);
      vv[i] = (int) dif; 
      dif = (dif>>32); } 
    //assert 0L==dif;

    for (;i<nt;i++) { vv[i] = tt[i]; } 
    
    final int nv = Ints.hiInt(vv);
    if (nv==nt) { return unsafe(vv,nv); } 
    final int[] vvv = new int[nv];
    for (int j=0;j<nv;j++) { vvv[j]=vv[j]; }
    return unsafe(vvv,nv); }

  public final NaturalLE subtract (final long u,
                                   final int upShift) {
    //assert 0L<=u;
    //assert 0<=upShift;
    //if (0L==u) { return this; }
    //if (0==upShift) { return subtract(u); }
    //if (isZero()) { assert 0L==u; return this; }
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    if (0==bShift) { return subtractByWords(u,iShift);  }
    return subtractByBits(u,iShift,bShift); }

  //--------------------------------------------------------------


  private final NaturalLE subtractByWords (final int iShift,
                                           final long u) {
    final int nt = hiInt();
    final int[] vv = new int[nt+iShift+1];
    long dif = -loWord(u);
    if (0==iShift) { dif += uword(0); } 
    vv[0] = (int) dif;
    dif = (dif>>32);  
    dif -= hiWord(u);
    if (1>=iShift) { dif += uword(1-iShift); } 
    vv[1] = (int) dif;
    dif = (dif>>32);  
    int i=2;
    for (;i<nt+iShift;i++) {
      if (i>=iShift) { dif += uword(i-iShift); }
      vv[i] = (int) dif;
      dif = hiWord(dif); } 
    vv[i] = (int) dif; 
    return unsafe(vv); }

  private final NaturalLE subtractByBits (final int iShift,
                                          final int bShift,
                                          final long u) {
    final int nt = hiInt();
    final int[] vv = new int[nt+iShift+1];
    final int rShift = 32-bShift;
    int t0 = 0;
    int t1 = 0;
    long dif = -loWord(u);
    if (0==iShift) { 
      t1 = word(0);
      final int ti = ((t1<<bShift)|(t0>>>rShift));
      t0 = t1;
      dif += unsigned(ti) ; } 
    vv[0] = (int) dif;
    dif = (dif>>32);  
    dif -= hiWord(u);
    if (1>=iShift) { 
      t1 = word(1-iShift);
      final int ti = ((t1<<bShift)|(t0>>>rShift));
      t0 = t1;
      dif += unsigned(ti) ; } 
    vv[1] = (int) dif;
    dif = (dif>>32);  
    int i=2;
    for (;i<=nt+iShift;i++) {
      if (0L==dif) { break; }
      if (i>=iShift) { 
        t1 = word(i-iShift);
        final int ti = ((t1<<bShift)|(t0>>>rShift));
        t0 = t1;
        dif += unsigned(ti) ; } 
      vv[i] = (int) dif;
      dif = (dif>>32); } 
    i=Math.max(i,iShift);
    for (;i<=nt+iShift;i++) {
      t1 = word(i-iShift);
      final int ti = ((t1<<bShift)|(t0>>>rShift));
      t0 = t1;
      vv[i] = ti; } 
    //assert 0L==dif; 
    return unsafe(vv); }

  public final NaturalLE subtract (final int upShift,
                                   final long u) {
    //assert 0<=upShift;
    //assert 0<=u;
    //if (isZero()) { return from(u); }
    //if (0L==u) { return shiftUp(upShift); }
    //if (0==upShift) { return subtract(u); }

    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1F);
    if (0==bShift) { return subtractByWords(iShift,u); }
    return subtractByBits(iShift,bShift,u); }

  //--------------------------------------------------------------

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

  private final NaturalLE subtractFromByWords (final long u,
                                               final int iShift) {
    final int nt = hiInt();
    final int[] tt = words();
    final int[] vv = new int[iShift+3];
    // assert iShift<=n || 0L==u
    long dif = 0;
    int i=0;
    for (;i<Math.min(nt,iShift);i++) { 
      dif -= unsigned(tt[i]);
      vv[i] = (int) dif;
      dif = (dif>>32); }
    for (;i<iShift;i++) { 
      vv[i] = (int) dif;
      dif = (dif>>32); }
    dif += loWord(u);
    i=iShift;
    if (i<nt) { dif -= unsigned(tt[i]); }
    vv[i++] = (int) dif; 
    dif = (dif>>32);
    dif += hiWord(u);
    if (i<nt) { dif -= unsigned(tt[i]); }
    vv[i] = (int) dif; 
    assert 0L==(dif>>32); 
    return unsafe(vv); }

  private final NaturalLE subtractFromByBits (final long u,
                                              final int iShift,
                                              final int bShift) {
    final int nt = hiInt();
    final int[] tt = words();
    final int[] vv = new int[iShift+3];
    // assert iShift<=n || 0L==u
    long dif = 0;
    int i=0;
    for (;i<Math.min(nt,iShift);i++) { 
      dif -= unsigned(tt[i]);
      vv[i] = (int) dif;
      dif = (dif>>32); }
    for (;i<iShift;i++) { 
      vv[i] = (int) dif;
      dif = (dif>>32); }
    i=iShift;
    final int hi = (int) hiWord(u);
    final int lo = (int) u;
    final int rShift = 32-bShift;
    dif += unsigned(lo<<bShift);
    if (i<nt) { dif -= unsigned(tt[i]); }
    vv[i++] = (int) dif; 
    dif = (dif>>32);
    dif += unsigned((hi<<bShift)|(lo>>>rShift));
    if (i<nt) { dif -= unsigned(tt[i]); }
    vv[i++] = (int) dif; 
    dif = (dif>>32);
    dif += unsigned(hi>>>rShift);
    if (i<nt) { dif -= unsigned(tt[i]); }
    vv[i++] = (int) dif; 
    assert 0L==(dif>>32); 
    return unsafe(vv); }

  public final NaturalLE subtractFrom (final long u,
                                       final int upShift) {
    //assert 0L<=u;
    //assert 0<=upShift;
    //if (0L==u) { return this; }
    //if (0==upShift) { return subtractFrom(u); }
    //if (isZero()) { return from(u,upShift); }
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    if (0==bShift) { 
      return subtractFromByWords(u,iShift); }
    return subtractFromByBits(u,iShift,bShift); }

  //--------------------------------------------------------------

  public final NaturalLE  subtractFrom (final int upShift,
                                        final long u) {
    //assert 0<=upShift;
    //assert 0L<=u;
    //assert compareTo(upShift,u)<=0;
    return shiftUp(upShift).subtractFrom(u); }

  //--------------------------------------------------------------
  // arithmetic with shifted Naturals
  //--------------------------------------------------------------
  /** <code>add(u<<(32*iShift))</code> */

  private final NaturalLE addByWords (final NaturalLE u,
                                      final int iShift) {
    final int nt = hiInt();
    final int[] tt = words();
    final int[] uu = u.words();
    //assert 0<u.hiInt();
    final int nu = u.hiInt()+iShift;
    final int n = Math.max(nt,nu);
    final int[] vv = new int[n+1];
    for (int i=0;i<Math.min(nt,iShift);i++) { vv[i] = tt[i]; }
    long sum = 0L;
    int i=iShift;
    for (;i<nu;i++) {
      sum += unsigned(uu[i-iShift]);
      if (i<nt) { sum += unsigned(tt[i]); }
      vv[i] = (int) sum; 
      sum = hiWord(sum); }
    for (;i<nt;i++) { 
      if (0L==sum) { break; }
      sum += unsigned(tt[i]);
      vv[i] = (int) sum; 
      sum = hiWord(sum); }
    if (0L!=sum) { vv[i] = (int) sum; return unsafe(vv,i+1); }
    for (;i<nt;i++) { vv[i] = tt[i]; }
    return unsafe(vv,i); }

  private final NaturalLE addByBits (final NaturalLE u,
                                     final int iShift,
                                     final int bShift) {
    final int nt = hiInt();
    final int[] tt = words();
    final int[] uu = u.words();
    //assert 0<u.hiInt();
    final int nu = u.hiInt()+iShift;
    final int n = Math.max(nt,nu);
    final int[] vv = new int[n+1];
    for (int i=0;i<Math.min(nt,iShift);i++) { vv[i] = tt[i]; }
    final int rShift = 32-bShift;
    long sum = 0L;
    int u0 = 0;
    int i=iShift;
    for (;i<nu;i++) {
      final int u1 = uu[i-iShift];
      sum += unsigned((u1<<bShift)|(u0>>>rShift));
      u0 = u1;
      if (i<nt) { sum += unsigned(tt[i]); }
      vv[i] = (int) sum; 
      sum = hiWord(sum); }
    final long ui = unsigned(u0>>>rShift);
    if (0L!=ui) {
      sum += ui;
      if (i<nt) { sum += unsigned(tt[i]); }
      vv[i++] = (int) sum; 
      sum = hiWord(sum); }
    for (;i<nt;i++) { 
      if (0L==sum) { break; }
      sum += unsigned(tt[i]);
      vv[i] = (int) sum; 
      sum = hiWord(sum); }
    if (0L!=sum) { vv[i] = (int) sum; return unsafe(vv,i+1); }
    for (;i<nt;i++) { vv[i] = tt[i]; }
    return unsafe(vv,i); }

  @Override
  public final NaturalLE add (final Natural u,
                              final int upShift) {
    //assert 0<=upShift;
    //if (0==upShift) { return add(u); }
    //if (isZero()) { return u.shiftUp(upShift); }
    if (u.isZero()) { return this; }
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    if (0==bShift) { return addByWords((NaturalLE)u,iShift); }
    return addByBits((NaturalLE)u,iShift,bShift); }

  //--------------------------------------------------------------

  private final NaturalLE subtractByWords (final Natural u,
                                           final int iShift) {
    //assert nus<=hiInt();
    final int nt = hiInt();
    final int[] tt = words();
    final int nus = u.hiInt() + iShift;
    final int[] vv = new int[nt];
    for (int i=0;i<iShift;i++) { vv[i] = tt[i]; }
    long dif = 0L;
    int i=iShift;
    for (;i<nus;i++) {
      dif += unsigned(tt[i]);
      dif -= u.uword(i-iShift);
      vv[i] = (int) dif; 
      dif = (dif>>32); }
    for (;i<nt;i++) {
      if (0L==dif) { break; }
      dif += unsigned(tt[i]);
      vv[i] = (int) dif; 
      dif = (dif>>32); }
    for (;i<nt;i++) { vv[i] = tt[i]; }
    //if (i<nt) { System.arraycopy(words(),i,vv,i,nt-i); }
    return unsafe(vv); }

  private final NaturalLE subtractByBits (final Natural u,
                                          final int iShift,
                                          final int bShift) {
    //assert 0<bShift;
    //assert nus<=hiInt();
    final int nus = u.hiInt() + iShift + 1;
    final int nt = hiInt();
    final int[] tt = words();
    final int[] vv = new int[nt];
    for (int i=0;i<iShift;i++) { vv[i] = tt[i]; }
    final int rShift=32-bShift;
    long dif = 0L;
    int i=iShift;
    int u0 = 0;
    for (;i<nus;i++) {
      final int u1 = u.word(i-iShift);
      final long ui = unsigned(((u1<<bShift)|(u0>>>rShift)));
      u0 = u1;
      dif += unsigned(tt[i])-ui;
      vv[i] = (int) dif; 
      dif = (dif>>32); }
    for (;i<nt;i++) {
      if (0L==dif) { break; }
      dif += unsigned(tt[i]);
      vv[i] = (int) dif; 
      dif = (dif>>32); }
    for (;i<nt;i++) { vv[i] = tt[i]; }
    //if (i<nt) { System.arraycopy(words(),i,vv,i,nt-i); }
    return unsafe(vv); }

  public final NaturalLE subtract (final Natural u,
                                   final int upShift) {
    //assert 0<=upShift;
    //if (isZero()) { assert u.isZero(); return zero(); }
    //if (u.isZero()) { return this; }
    //if (0==upShift) { return subtract(u); }
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    if (0==bShift) { return subtractByWords(u,iShift); }
    return subtractByBits(u,iShift,bShift); }

  //--------------------------------------------------------------
  // Ringlike
  //--------------------------------------------------------------

  @Override
  public final NaturalLE add (final Natural u) {
    final int nt = hiInt();
    final int nu = u.hiInt();
    if (nt<nu) { return (NaturalLE) u.add(this); }
    final int[] tt = words();
    final int[] uu = ((NaturalLE) u).words();
    final int[] vv = new int[nt+1];
    long sum = 0L;
    int i=0;
    for (;i<nu;i++) {
      sum += unsigned(tt[i]) + unsigned(uu[i]);
      vv[i] = (int) sum; 
      sum = hiWord(sum);}
    for (;i<nt;i++) {
      if (0L==sum) { break; }
      sum += unsigned(tt[i]);
      vv[i] = (int) sum; 
      sum = hiWord(sum);}
    for (;i<nt;i++) { vv[i] = tt[i]; }
    if (0L!=sum) { vv[nt] = (int) sum; return unsafe(vv,nt+1); }
    return unsafe(vv,nt); }

  //--------------------------------------------------------------

  @Override
  public final NaturalLE subtract (final Natural u) {
    //assert 0<=compareTo(u);
    final int nt = hiInt();
    final int nu = u.hiInt();
    //assert nu<=nt;
    final int[] tt = words();
    final int[] uu = ((NaturalLE) u).words();
    if (0>=nu) { return this; }
    final int[] vv = new int[nt];
    long dif = 0L;
    int i=0;
    for (;i<nu;i++) {
      dif += unsigned(tt[i])-unsigned(uu[i]);
      vv[i] = (int) dif;
      dif= (dif>>32); }
    for (;i<nt;i++) {
      if (0L==dif) { break; }
      dif += unsigned(tt[i]);
      vv[i] = (int) dif;
      dif = (dif>>32); }
    //assert 0L==dif;
    if (nt<=i) { return unsafe(vv,Ints.hiInt(vv)); }
    for (;i<nt;i++) { vv[i] = tt[i]; }
    return unsafe(vv,nt); }

  //--------------------------------------------------------------
  // multiplicative monoid
  //--------------------------------------------------------------
  // TODO: singleton class for one() and zero()?

  private static final NaturalLE ONE = unsafe(new int[] {1},1);

  @Override
  public final NaturalLE one () { return ONE; }

  @Override
  public final NaturalLE ones (final int n) {
    throw Exceptions.unsupportedOperation(this,"ones",n); }

  @Override
  public final boolean isOne () {
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
        carry = hiWord(prod); 
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
    return valueOf(u,upShift); }
  //    //assert 0<=u;
  //    //assert 0<=upShift;
  //    //if (0L==u) { return zero(); }
  //    //if (0==upShift) { return from(u); }
  //    final int iShift = (upShift>>>5);
  //    final int bShift = (upShift&0x1f);
  //    if (0==bShift) { 
  //      final int[] vv = new int[iShift+2];
  //      vv[iShift] = (int) u;
  //      vv[iShift+1] = (int) hiWord(u);
  //      return unsafe(vv); }
  //    final int rShift = 32-bShift;
  //    final int lo = (int) u;
  //    final int hi = (int) hiWord(u);
  //    final int[] vv = new int[iShift+3];
  //    vv[iShift] = (lo<<bShift);
  //    vv[iShift+1] = ((hi<<bShift)|(lo>>>rShift));
  //    vv[iShift+2] =  (hi>>>rShift); 
  //    return unsafe(vv); }

  /** get the least significant int word of (this >>> shift) */

  public final int getShiftedInt (final int downShift) {
    //assert 0<=downShift;
    final int iShift = (downShift>>>5);
    if (hiInt()<=iShift) { return 0; }
    final int rShift = (downShift & 0x1f);
    if (0==rShift) { return word(iShift); }
    final int r2 = 32-rShift;
    // TODO: optimize using startWord and endWord.
    final long lo = (uword(iShift) >>> rShift);
    final long hi = (uword(iShift+1) << r2);
    return (int) (hi | lo); }

  /** get the least significant two int words of (this >>> shift)
   * as a long.
   */

  public final long getShiftedLong (final int downShift) {
    //assert 0<=downShift;
    final int iShift = (downShift>>>5);
    if (hiInt()<=iShift) { return 0L; }
    final int rShift = (downShift & 0x1f);
    if (0==rShift) {
      return ((uword(iShift+1)<<32) | uword(iShift)); }
    // TODO: optimize using startWord and endWord.
    final int r2 = 32-rShift;
    final long lo0 = (uword(iShift)>>>rShift);
    final long u1 = uword(iShift+1);
    final long lo1 = (u1<<r2);
    final long lo = lo1 | lo0;
    final long hi0 = (u1>>>rShift);
    final long hi1 = (uword(iShift+2)<<r2);
    final long hi = hi1 | hi0;
    return (hi << 32) | lo; }

  //--------------------------------------------------------------

  private final NaturalLE shiftDownByWords (final int iShift) {
    final int nt = hiInt();
    final int nv = nt-iShift;
    if (0>=nv) { return zero(); }
    final int[] vv = new int[nv];
    for (int i=0;i<nv;i++) { vv[i] = word(i+iShift); }
    //System.arraycopy(words(),iShift,vv,0,nv);
    return unsafe(vv,nv); }

  private final NaturalLE shiftDownByBits (final int iShift,
                                           final int bShift) {
    final int nt = hiInt();
    final int nv = nt-iShift;
    // shifting all bits off the end, covers zero input case
    if (0>=nv) { return zero(); }

    final int[] vv = new int[nv];
    final int rShift = 32-bShift;
    int w0 = word(iShift);
    for (int i=0,j=iShift+1;i<nv;i++,j++) { 
      final int w1 = word(j);
      final int w = ((w1<<rShift) | (w0>>>bShift));
      w0 = w1;
      vv[i] = w; }
    return unsafe(vv); }

  @Override
  public final NaturalLE shiftDown (final int downShift) {
    //assert 0<=downShift;
    if (0==downShift) { return this; }
    final int iShift = (downShift>>>5);
    final int bShift = (downShift&0x1F);
    if (0==bShift) { return shiftDownByWords(iShift); }
    return shiftDownByBits(iShift,bShift); }

  //  @Override
  //  public final NaturalLE shiftDown (final int downShift) {
  //    //assert 0<=downShift;
  //    //if (0==downShift) { return this; }
  //    final int iShift = (downShift>>>5);
  //    final int nt = hiInt();
  //    if (iShift>=nt) { return zero(); }
  //
  //    final int bShift = (downShift & 0x1F);
  //
  //    if (0==bShift) {
  //      final int nv = nt-iShift;
  //      final int[] vv = new int[nv];
  //      for (int i=0;i<nv;i++) { vv[i] = word(i+iShift); }
  //      //System.arraycopy(words(),iShift,u,0,n1);
  //      return unsafe(vv,nv); }
  //
  //    final int rShift = 32-bShift;
  //    final int hi = (word(nt-1)>>>bShift);
  //    final int nv = nt-iShift-1;
  //    final int[] vv;
  //    if (0==hi) { vv = new int[nv]; }
  //    else { vv = new int[nv+1]; vv[nv] = hi; }
  //    int w0 = word(iShift);
  //    for (int i=0;i<nv;i++) { 
  //      final int w1 = word(i+iShift+1);
  //      final int w = ((w1<<rShift) | (w0>>>bShift));
  //      w0 = w1;
  //      vv[i] = w; }
  //    return unsafe(vv,nv+((0==hi)?0:1)); }


  //--------------------------------------------------------------

  private final NaturalLE shiftUpBywords (final int iShift) {
    final int nt = hiInt();
    final int nv = nt+iShift;
    final int[] tt = words();
    final int[] vv = new int[nv];
    for (int i=0;i<nt;i++) { vv[i+iShift] = tt[i]; }
    //System.arraycopy(words(),0,u,iShift,n0);
    return unsafe(vv,nv); }

  private final NaturalLE shiftUpByBits (final int iShift,
                                         final int bShift) {
    final int nt = hiInt();
    final int nv = nt+iShift;
    final int rShift = 32-bShift;
    final int[] tt = words();
    final int[] vv = new int[nv+1];
    int w0 = tt[0];
    vv[iShift] = (w0<<bShift);
    for (int i=1;i<nt;i++) { 
      final int w1 = tt[i];
      final int w = ((w1<<bShift)|(w0>>>rShift));
      w0 = w1;
      vv[i+iShift] = w; }
    final int vvn = (w0>>>rShift);
    if (0!=vvn) { vv[nv] = vvn; return unsafe(vv,nv+1); }
    return unsafe(vv,nv); }

  @Override
  public final NaturalLE shiftUp (final int upShift) {
    //assert 0<=shift;
    //if (0==upShift) { return this; }
    if (isZero()) { return this; }
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    if (0==bShift) { return shiftUpBywords(iShift); }
    return shiftUpByBits(iShift,bShift); }

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

  private NaturalLE (final int[] words,
                     final int hiInt) { 
    //assert null!=words;
    //assert 0<=hiInt;
    _words = words; 
    _hiInt = hiInt; 
    }
  private static final NaturalLE unsafe (final int[] words,
                                         final int hiInt){
//    if (hiInt<words.length) {
//      final int[] ww = new int[hiInt];
//      for (int i=0;i<hiInt;i++) { ww[i] = words[i]; }
//      return new NaturalLE(ww,hiInt); }
    return new NaturalLE(words,hiInt); }

//  private NaturalLE (final int[] words) { 
//    _words = words; 
//    }
//
//  /** Doesn't copy <code>words</code> or check <code>loInt</code>
//   * or <code>hiInt</code>. 
//   */
//
//  private static final NaturalLE unsafe (final int[] words,
//                                         final int hiInt){
//    if (hiInt<words.length) {
//      final int[] ww = new int[hiInt];
//      for (int i=0;i<hiInt;i++) { ww[i] = words[i]; }
//      return new NaturalLE(ww); }
//    return new NaturalLE(words); }


  /** Doesn't copy <code>words</code>. 
   */

  static final NaturalLE unsafe (final int[] words) {
    final int hi = Ints.hiInt(words);
    return unsafe(words,hi); }

  /** Copy <code>words</code>. 
   *  */
  public static final NaturalLE make (final int[] words) {
    final int end = Ints.hiInt(words);
    return unsafe(Arrays.copyOf(words,end),end); }

  //--------------------------------------------------------------
  /** From a big endian {@code byte[]}, as produced by
   * {@link BigInteger#toByteArray()}.
   */

  private static final NaturalLE valueOf (final byte[] a) {
    final int nBytes = a.length;
    int keep = 0;
    while ((keep<nBytes) && (0==a[keep])) { keep++; }
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
    //if (0L==u) { return zero(); }
    final int lo = (int) u;
    final int hi = (int) hiWord(u);
    if (0==hi) { 
      if (0==lo) { return unsafe(new int[0],0); }
      return unsafe(new int[] {lo},1); }
    if (0==lo) { return unsafe(new int[] { lo,hi },2); }
    return unsafe(new int[] { lo,hi },2); }

  public static final NaturalLE valueOf (final long u,
                                         final int upShift) {
    //assert 0<=u;
    //assert 0<=upShift;
    //assert 0<=u;
    //assert 0<=upShift;
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    if (0==bShift) { 
      final int[] vv = new int[iShift+2];
      vv[iShift] = (int) u;
      vv[iShift+1] = (int) hiWord(u);
      return unsafe(vv); }
    final long us = (u<<bShift);
    final int vv0 = (int) us;
    final int vv1 = (int) hiWord(us);
    final int vv2 = (int) (u>>>(64-bShift)); 
    if (0!=vv2) {
      final int[] vv = new int[iShift+3];
      vv[iShift] = vv0;
      vv[iShift+1] = vv1;
      vv[iShift+2] = vv2;
      return unsafe(vv,iShift+3); }
    if (0!=vv1) { 
      final int[] vv = new int[iShift+2];
      vv[iShift] = vv0;
      vv[iShift+1] = vv1;
      return unsafe(vv,iShift+2); }
    if (0!=vv0) { 
      final int[] vv = new int[iShift+1];
      vv[iShift] = vv0;
      return unsafe(vv,iShift+1); }
    return ZERO; }

  /** Return a {@link NaturalLE} equivalent to the unsigned 
   * value of <code>u</code>.
   */
  public static final NaturalLE valueOf (final int u) {
    if (0==u) { return ZERO; }
    return unsafe(new int[] {u},1); }

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
    for (int i=u.startWord();i<n;i++) { w[i] = u.word(i); }
    return unsafe(w,n); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

