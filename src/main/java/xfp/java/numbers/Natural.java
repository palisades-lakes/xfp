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
 * @version 2019-10-06
 */

@SuppressWarnings("unchecked")
public final class Natural implements Ringlike<Natural> {

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------
  /** This array is never modified.
   */

  private final int[] _words;
  private final int[] words () { return _words; }

  //  private final int _hiInt;
  //
  //  public final int hiInt () { return _hiInt; }

  public final int hiInt () { return _words.length; }

  private final int loInt () {
    // Search for lowest order nonzero int
    final int nt = hiInt(); // might be 0
    final int[] tt = words();
    for (int i=0;i<nt;i++) {
      if (0!=tt[i]) { return i; } }
    //assert 0==n;
    return 0; }

  public final int hiBit () {
    final int i = hiInt()-1;
    if (0>i) { return 0; }
    final int wi = _words[i];
    return (i<<5)+Integer.SIZE-Integer.numberOfLeadingZeros(wi); }

  public final int loBit () {
    // Search for lowest order nonzero int
    final int i=loInt(); 
    if (i==hiInt()) { return 0; } // all bits zero
    return (i<<5) + Integer.numberOfTrailingZeros(_words[i]); }

  //--------------------------------------------------------------

  public final int word (final int i) {
    //assert 0<=i : "Negative index: " + i;
    if (hiInt()<=i) { return 0; }
    return _words[i]; }

  public final long uword (final int i) {
    //assert 0<=i : "Negative index: " + i;
    if (hiInt()<=i) { return 0L; }
    return unsigned(_words[i]); }

  //--------------------------------------------------------------
  /** Return the <code>[i0,i1)</code> words as a new 
   * <code>Natural</code> with <code>[0,i1-i0)</code> words.
   */

  public final Natural words (final int i0,
                              final int i1) {
    //assert 0<=i0;
    //assert i0<i1;
    if ((0==i0) && (hiInt()<=i1)) { return this; }
    final int n = Math.max(0,i1-i0);
    if (0>=n) { return zero(); }
    final int[] tt = words();
    final int[] vv = new int[n];
    for (int i=0;i<n;i++) { vv[i] =  tt[i+i0]; }
    return unsafe(vv,n); }

  public final Natural setWord (final int i,
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

  /** Singleton. */
  public static final Natural ZERO = new Natural(new int[0]); 

  @Override
  public final boolean isZero () { return 0==hiInt(); }

  @Override
  public final Natural zero () { return ZERO; }

  //--------------------------------------------------------------
  // ordering
  //--------------------------------------------------------------

  @Override
  public final int compareTo (final Natural u) {
    final int b0 = hiBit();
    final int b1 = u.hiBit();
    if (b0<b1) { return -1; }
    if (b0>b1) { return 1; }
    int i = hiInt()-1;
    for (;i>=0;i--) {
      final long u0i = uword(i);
      final long u1i = u.uword(i);
      if (u0i<u1i) { return -1; }
      if (u0i>u1i) { return 1; } }
    return 0; }

  //--------------------------------------------------------------

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

  public final int compareTo (final long u,
                              final int upShift) {
    //assert 0L<=u;
    //assert 0<=upShift : "upShift=" + upShift;

    if (0==upShift) { return compareTo(u); }

    final int nt = hiInt();
    if (0==nt) { return ((0L==u) ? 0 : -1); }
    else if (0L==u) { return 0; }
    final int iShift = (upShift>>>5);
    if (nt<iShift) { return -1; }
    if (nt>iShift+3) { return 1; }

    int i = nt-1;
    final int[] tt = words();
    final int tthi = tt[i];
    final int mt = (i<<5) + Integer.SIZE - 
      Integer.numberOfLeadingZeros(tthi);
    final int mu = Numbers.hiBit(u) + upShift;
    if (mt<mu) { return -1; }
    if (mt>mu) { return 1; }

    long tti = unsigned(tthi);
    i--;
    final int bShift = (upShift&0x1F);
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
  // long based factories
  //--------------------------------------------------------------

  static final Natural sum (final long u,
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

  static final Natural difference (final long u,
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

  static final Natural difference (final long v,
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

  static final Natural product (final long t0,
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
    if (0!=w3) { return new Natural(new int[] {w0,w1,w2,w3,}); }
    if (0!=w2) { return new Natural(new int[] {w0,w1,w2,}); }
    if (0!=w1) { return new Natural(new int[] {w0,w1,}); }
    if (0!=w0) { return new Natural(new int[] {w0,}); }
    return ZERO; }

  // TODO: fix lurking overflow issue
  // probably only works as long as t is double significand

  static final Natural fromSquare (final long t) {
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

    if (0!=w3) { return new Natural(new int[] { w0,w1,w2,w3,}); }
    if (0!=w2) { return new Natural(new int[] { w0,w1,w2, }); }
    if (0!=w1) { return new Natural(new int[] {w0,w1}); }
    if (0!=w0) { return new Natural(new int[] {w0}); }
    return ZERO; }

  //--------------------------------------------------------------
  // add (non-negative) longs
  //--------------------------------------------------------------

  public final Natural add (final long u) {
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
      return new Natural(vvv); }

    for (;i<nt;i++) { vv[i] = tt[i]; }
    return new Natural(vv); }

  //--------------------------------------------------------------

  private final Natural addByWords (final long u,
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
      return new Natural(vvv); }

    for (;i<nt;i++) { vv[i] = tt[i]; }
    return new Natural(vv); }

  private final Natural addByBits (final long u,
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

    boolean nocarry = (0==(int)sum);
    for (;i<nt;i++) {
      if (nocarry) { break; }
      final long vvi = 1L + unsigned(tt[i]);
      vv[i] = (int) vvi; 
      nocarry = (0==(int)hiWord(vvi)); }

    if (!nocarry) { 
      final int[] vvv = new int[nv+1];
      for (int j=0;j<nv;j++) { vvv[j]=vv[j]; } 
      vvv[nv] = 1; 
      return new Natural(vvv); }

    for (;i<nt;i++) { vv[i] = tt[i]; }
    return new Natural(vv); }

  public final Natural add (final long u,
                            final int upShift) {
    //assert 0<u;
    //assert 0<upShift;
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1F);
    if (0==bShift) { return addByWords(u,iShift);}
    return addByBits(u,iShift,bShift); }

  //--------------------------------------------------------------
  // subtract (non-negative) longs
  //--------------------------------------------------------------

  public final Natural subtract (final long u) {
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

  private final Natural subtractByWords (final long u,
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

  private final Natural subtractByBits (final long u,
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

  public final Natural subtract (final long u,
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

  public final Natural subtractFrom (final long u) {
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

  private final Natural subtractFromByWords (final long u,
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

  private final Natural subtractFromByBits (final long u,
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

  public final Natural subtractFrom (final long u,
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
  // arithmetic with shifted Naturals
  //--------------------------------------------------------------
  /** <code>add(u<<(32*iShift))</code> */

  private final Natural addByWords (final Natural u,
                                    final int iShift) {
    final int nt = hiInt();
    final int[] tt = words();
    final int[] uu = u.words();
    //assert 0<u.hiInt();
    final int nu = u.hiInt()+iShift;
    final int nv = Math.max(nt,nu);
    final int[] vv = new int[nv];
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

    if (0L!=sum) { 
      final int[] vvv = new int[nv+1];
      for (int j=0;j<nv;j++) { vvv[j]=vv[j]; } 
      vvv[nv] = 1; 
      return new Natural(vvv); }

    for (;i<nt;i++) { vv[i] = tt[i]; }
    return new Natural(vv); }

  private final Natural addByBits (final Natural u,
                                   final int iShift,
                                   final int bShift) {
    final int nt = hiInt();
    final int[] tt = words();
    final int nu0 = u.hiInt();
    final int[] uu = u.words();
    //assert 0<u.hiInt();
    final int rShift = 32-bShift;
    final int uhi = (uu[nu0-1]>>rShift);
    final int nu1 = nu0+iShift;
    final int nv = Math.max(nt,nu1+((0==uhi)?0:1));
    final int[] vv = new int[nv];
    for (int i=0;i<Math.min(nt,iShift);i++) { vv[i] = tt[i]; }

    long sum = 0L;
    int u0 = 0;
    int i=iShift;
    for (;i<nu1;i++) {
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

    if (0L!=sum) { 
      final int[] vvv = new int[nv+1];
      for (int j=0;j<nv;j++) { vvv[j]=vv[j]; } 
      vvv[nv] = 1; 
      return new Natural(vvv); }

    for (;i<nt;i++) { vv[i] = tt[i]; }
    return new Natural(vv); }

  public final Natural add (final Natural u,
                            final int upShift) {
    //assert 0<=upShift;
    //if (0==upShift) { return add(u); }
    //if (isZero()) { return u.shiftUp(upShift); }
    if (u.isZero()) { return this; }
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    if (0==bShift) { return addByWords(u,iShift); }
    return addByBits(u,iShift,bShift); }

  //--------------------------------------------------------------
  // Ringlike
  //--------------------------------------------------------------

  @Override
  public final Natural abs () { return this; }

  //--------------------------------------------------------------

  @Override
  public final Natural add (final Natural u) {
    final int nt = hiInt();
    final int nu = u.hiInt();
    if (nt<nu) { return u.add(this); }
    final int[] tt = words();
    final int[] uu = u.words();
    final int[] vv = new int[nt];
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
    if (0L!=sum) { 
      //vv[nt] = (int) sum; return new Natural(vv); }
      final int[] vvv = new int[nt+1];
      for (int j=0;j<nt;j++) { vvv[j]=vv[j]; } 
      vvv[nt] = 1; 
      return new Natural(vvv); }

    for (;i<nt;i++) { vv[i] = tt[i]; }
    return new Natural(vv); }

  //--------------------------------------------------------------

  @Override
  public final Natural subtract (final Natural u) {
    //assert 0<=compareTo(u);
    final int nt = hiInt();
    final int nu = u.hiInt();
    //assert nu<=nt;
    final int[] tt = words();
    final int[] uu = u.words();
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

  @Override
  public final Natural absDiff (final Natural uu) {
    //assert isValid();
    //assert u.isValid();
    final Natural u = uu;
    final int c = compareTo(u);
    if (c==0) { return zero(); }
    if (c<0) { return u.subtract(this); }
    return subtract(u); }

  //--------------------------------------------------------------
  // multiplicative monoid
  //--------------------------------------------------------------
  // TODO: singleton class for one() and zero()?

  private static final Natural ONE = new Natural(new int[] {1});

  @Override
  public final Natural one () { return ONE; }

  public final static Natural ones (final int n) {
    final int[] vv = new int[n];
    Arrays.fill(vv, -1);
    return unsafe(vv,n); }

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

  private final Natural squareSimple () {
    final int nt = hiInt();
    final int[] tt = words();
    final int[] vv = new int[2*nt];
    // diagonal
    for (int i=0;i<nt;i++) {
      final long tti = unsigned(tt[i]);
      final long prod = tti*tti; 
      final int i2 = 2*i;
      vv[i2] = (int) prod;
      vv[i2+1] = (int) hiWord(prod); }
    // off diagonal
    for (int i0=0;i0<nt;i0++) {
      long prod = 0L;
      long carry = 0L;
      final long tt0 = unsigned(tt[i0]);
      int i2 = 0;
      for (int i1=0;i1<i0;i1++) {
        i2 = i0+i1;
        prod = unsigned(vv[i2]) + carry; 
        carry = hiWord(prod); 
        long vvi2 = loWord(prod); 
        if (i0!=i1) {
          final long tt1 = unsigned(tt[i1]);
          final long tt01 = tt0*tt1;
          prod = vvi2 + tt01; 
          carry = hiWord(prod) + carry;
          vvi2 = loWord(prod);
          prod = vvi2 + tt01; 
          carry = hiWord(prod) + carry; 
          vv[i2] = (int) prod; } }
      while ((0L!=carry)&&(i2<2*nt)) {
        i2++;
        prod = unsigned(vv[i2]) + carry;
        carry = hiWord(prod); 
        vv[i2] = (int) prod;  }
      //assert 0L==carry; 
    }
    return unsafe(vv); }

  //--------------------------------------------------------------

  @Override
  public final Natural square () {
    if (isZero()) { return zero(); }
    if (isOne()) { return one(); }
    final int n = hiInt();
    if (n < NaturalMultiply.KARATSUBA_SQUARE_THRESHOLD) { 
      return squareSimple(); }
    if (n < NaturalMultiply.TOOM_COOK_SQUARE_THRESHOLD) {
      return NaturalMultiply.squareKaratsuba(this); }
    // For a discussion of overflow detection see multiply()
    return NaturalMultiply.squareToomCook3(this); }

  //--------------------------------------------------------------
  // multiply
  //--------------------------------------------------------------

  @Override
  public final Natural multiply (final Natural u) {
    //assert isValid();
    //assert u.isValid();
    return NaturalMultiply.multiply(this,u); }

  //--------------------------------------------------------------

  //  public final Natural multiply (final long u) {
//    return NaturalMultiply.multiply(this,u); }

  public final Natural multiply (final long v) {
    if (0L==v) { return Natural.ZERO; }
    if (1L==v) { return this; }
    if (isZero()) { return Natural.ZERO; }
    //assert 0L < v;
    final long hi = Numbers.hiWord(v);
    final long lo = Numbers.loWord(v);
    final int n0 = hiInt();
    final int[] tt = words();
    // TODO: assume minimal carry and allocate smaller array;
    // then fix when needed
    final int nv = n0+((hi==0)?1:2);
    final int[] vv = new int[nv];
    long carry = 0;
    int i=0;
    for (;i<n0;i++) {
      final long product = (unsigned(tt[i])*lo) + carry;
      vv[i] = (int) product;
      carry = (product>>>32); }
    vv[i] = (int) carry;
    if (0!=hi) {
      carry = 0;
      i=0;
      for (;i<n0;i++) {
        final int i1 = i+1;
        final long product = (unsigned(tt[i])*hi) 
          + unsigned(vv[i1]) + carry;
        vv[i1] = (int) product;
        carry = (product>>>32); }
      vv[i+1]= (int) carry; }
    return Natural.unsafe(vv); }

  //--------------------------------------------------------------

  public final Natural multiply (final long u,
                                 final int upShift) {
    //assert isValid();
    //assert 0L<=u;
    //assert 0<=upShift;
    if (0L==u) { return zero(); }
    if (0==upShift) { return multiply(u); }
    if (isZero()) { return this; }
    return multiply(Natural.valueOf(u,upShift)); }

  //--------------------------------------------------------------
  // divide
  //--------------------------------------------------------------

  // for testing
  public final List<Natural> 
  divideAndRemainderKnuth (final Natural u) {
    //assert isValid();
    //assert u.isValid();
    return NaturalDivide.divideAndRemainderKnuth(this,u); }

  // for testing
  public final List<Natural> 
  divideAndRemainderBurnikelZiegler (final Natural u) {
    //assert isValid();
    //assert u.isValid();
    return NaturalDivide.divideAndRemainderBurnikelZiegler(this,u); }

  @Override
  public final List<Natural> 
  divideAndRemainder (final Natural u) {
    //assert isValid();
    //assert u.isValid();
    return NaturalDivide.divideAndRemainder(this,u); }

  @Override
  public final  Natural divide (final Natural u) {
    //assert isValid();
    //assert u.isValid();
    return divideAndRemainder(u).get(0); }

  @Override
  public final  Natural remainder (final Natural u) {
    //assert isValid();
    //assert u.isValid();
    return divideAndRemainder(u).get(1); }

  //--------------------------------------------------------------
  // gcd
  //--------------------------------------------------------------

  @Override
  public final Natural gcd (final Natural u) { 
    //assert isValid();
    //assert u.isValid();
    return NaturalDivide.gcd(this,u); }

  //--------------------------------------------------------------

  @Override
  public final List<Natural> reduce (final Natural d) {
    //assert isValid();
    return NaturalDivide.reduce(this,d); }

  //--------------------------------------------------------------
  // Uints
  //--------------------------------------------------------------
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

  /** get the least significant two int words of 
   * <code>(this>>>downShift)</code>
   * as a long.
   */

  public final long getShiftedLong (final int downShift) {
    //assert 0<=downShift;
    final int nt = hiInt();
    final int iShift = (downShift>>>5);
    if (nt<=iShift) { return 0L; }
    final long wi = unsigned(_words[iShift]);
    final int bShift = (downShift&0x1F);
    final int iShift1 = iShift+1;

    if (0==bShift) {
      if (nt==iShift1) { return wi; }
      return ((unsigned(_words[iShift1])<<32) | wi); }

    final long lo0 = (wi>>>bShift);
    if (nt==iShift1) { return lo0; }
    final long u1 = unsigned(_words[iShift1]);
    final int rShift = 32-bShift;
    final long lo1 = (u1<<rShift);
    final long lo = lo1 | lo0;
    final long hi0 = (u1>>>bShift);
    final int iShift2 = iShift+2;
    if (nt==iShift2) {   return (hi0 << 32) | lo; }
    final long hi1 = (unsigned(_words[iShift2])<<rShift);
    final long hi = hi1 | hi0;
    return (hi << 32) | lo; }

  //--------------------------------------------------------------

  private final Natural shiftDownByWords (final int iShift) {
    final int nt = hiInt();
    final int nv = nt-iShift;
    if (0>=nv) { return zero(); }
    final int[] vv = new int[nv];
    for (int i=0;i<nv;i++) { vv[i] = word(i+iShift); }
    //System.arraycopy(words(),iShift,vv,0,nv);
    return new Natural(vv); }

  private final Natural shiftDownByBits (final int iShift,
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

  public final Natural shiftDown (final int downShift) {
    //assert 0<=downShift;
    if (0==downShift) { return this; }
    final int iShift = (downShift>>>5);
    final int bShift = (downShift&0x1F);
    if (0==bShift) { return shiftDownByWords(iShift); }
    return shiftDownByBits(iShift,bShift); }

  //--------------------------------------------------------------

  private final Natural shiftUpBywords (final int iShift) {
    final int nt = hiInt();
    final int nv = nt+iShift;
    final int[] tt = words();
    final int[] vv = new int[nv];
    for (int i=0;i<nt;i++) { vv[i+iShift] = tt[i]; }
    //System.arraycopy(words(),0,u,iShift,n0);
    return new Natural(vv); }

  private final Natural shiftUpByBits (final int iShift,
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
    if (0!=vvn) { vv[nv] = vvn; return new Natural(vv); }
    final int[] vvv = new int[nv];
    for  (int i=0;i<nv;i++) { vvv[i] = vv[i]; }
    return new Natural(vvv); }

  public final Natural shiftUp (final int upShift) {
    //assert 0<=shift;
    //if (0==upShift) { return this; }
    if (isZero()) { return this; }
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    if (0==bShift) { return shiftUpBywords(iShift); }
    return shiftUpByBits(iShift,bShift); }

  public final boolean testBit (final int n) {
    //assert 0<=n;
    final int nn = (n>>>5);
    if (hiInt()<=nn) { return false; }
    return 0!=(_words[nn] & (1<<(n&0x1F))); }

  public final Natural setBit (final int i) {
    //assert 0<=i;
    final int iw = (i>>>5);
    final int w = word(iw);
    final int ib = (i&0x1F);
    return setWord(iw,(w|(1<<ib))); }

  //--------------------------------------------------------------
  // used in BigFloat.doubleValue();

  private static final boolean testBit (final int[] tt,
                                        final int nt,
                                        final int i) {
    //assert 0<=n;
    final int iShift = (i>>>5);
    if (nt<=iShift) { return false; }
    final int bShift = (i&0x1F);
    return 0!=(tt[iShift] & (1<<bShift)); }

  final boolean roundUp (final int e) {
    final int nt = hiInt();
    if (nt<=(e>>>5)) { return false; }
    final int[] tt = words();
    final int e1 = e-1;
    final int n1 = (e1>>>5);
    if (nt<=n1) { return false; }
    final int w1 = (tt[n1] & (1<<(e1&0x1F)));
    if (0==w1) { return false; }
    final int e2 = e-2;
    if (0<=e2) {
      final int n2 = (e2>>>5);
      if (nt<=n2) { return false; }
      final int tt2 = tt[n2];
      for (int i=e2-(n2<<5);i>=0;i--) {
        if (0!=(tt2&(1<<i))) { return true; } } 
      for (int i=n2-1;i>=0;i--) { if (0!=tt[i]) { return true; } } }
    return testBit(tt,nt,e); }

  //  final boolean roundUp (final int e) {
  //    final int nt = hiInt();
  //    if (nt<=(e>>>5)) { return false; }
  //    final int[] tt = words();
  //    final int e1 = e-1;
  //    final int n1 = (e1>>>5);
  //    if (nt<=n1) { return false; }
  //    final int w1 = (tt[n1] & (1<<(e1&0x1F)));
  //    if (0==w1) { return false; }
  //    final int e2 = e-2;
  //    if (0<=e2) {
  //    final int n2 = (e2>>>5);
  //    for (int i=e2;i>=(n2<<5);i--) {
  //      if (testBit(tt,nt,i)) { return true; } } 
  //    for (int i=n2-1;i>=0;i--) { if (0!=tt[i]) { return true; } } }
  //    return testBit(tt,nt,e); }

  //  final boolean roundUp (final int e) {
  //    final int nt = hiInt();
  //    if (nt<=(e>>>5)) { return false; }
  //    final int[] tt = words();
  //    if (! testBit(tt,nt,e-1)) { return false; }
  //    final int e2 = e-2;
  //    if (0<=e2) {
  //    final int n2 = (e2>>>5);
  //    for (int i=e2;i>=(n2<<5);i--) {
  //      if (testBit(tt,nt,i)) { return true; } } 
  //    for (int i=n2-1;i>=0;i--) { if (0!=tt[i]) { return true; } } }
  //    return testBit(tt,nt,e); }

  //  final boolean roundUp (final int e) {
  //    final int nt = hiInt();
  //    if (nt<=(e>>>5)) { return false; }
  //    final int[] tt = words();
  //    if (! testBit(tt,nt,e-1)) { return false; }
  //    for (int i=e-2;i>=0;i--) {
  //      if (testBit(tt,nt,i)) { return true; } }
  //    return testBit(tt,nt,e); }

  //  final boolean roundUp (final int e) {
  //    final int nt = hiInt();
  //    final int[] tt = words();
  //    if (! testBit(tt,nt,e-1)) { return false; }
  //    for (int i=e-2;i>=0;i--) {
  //      if (testBit(tt,nt,i)) { return true; } }
  //    return testBit(tt,nt,e); }

  //--------------------------------------------------------------
  // 'Number' methods
  //--------------------------------------------------------------

  @Override
  public final int intValue () { 
    // TODO: handle 'negative' words correctly!
    switch (hiInt()) {
    case 0: return 0;  
    case 1: return _words[0]; 
    default:
      throw new UnsupportedOperationException(
        "Too large for int:" + this); } }

  @Override
  public final long longValue () {
    switch (hiInt()) {
    case 0: return 0;  
    case 1: return unsigned(_words[0]); 
    case 2: 
      return (unsigned(_words[1])<<32) | unsigned(_words[0]);
    default:
      throw new UnsupportedOperationException(
        "Too large for long:" + this); } }

  private final byte[] bigEndianBytes () {
    final int hi = hiBit();
    // an extra zero byte to avoid getting a negative
    // two's complement input to new BigInteger(b).
    final int n = 1 + ((hi)/8);
    final byte[] b = new byte[n];
    int j = 0;
    int w = 0;
    for (int i=0;i<n;i++) {
      if (0==(i%4)) { w = word(j++); }
      else { w = (w>>>8); }
      b[n-1-i] = (byte) w; }
    return b; }

  public final  BigInteger bigIntegerValue () {
    return new BigInteger(bigEndianBytes()); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { 
    int hashCode = 0;
    for (int i=0; i<hiInt(); i++) {
      hashCode = ((31 * hashCode) + _words[i]); }
    return hashCode; }

  @Override
  public final boolean equals (final Object x) {
    if (x==this) { return true; }
    if (!(x instanceof Natural)) { return false; }
    final Natural u = (Natural) x;
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

  private Natural (final int[] words) { _words = words; }

//  /** Doesn't copy <code>words</code>, check <code>hiInt</code>
//   * or array length. 
//   */
//
//  private static final Natural reallyUnsafe (final int[] words) {
//    return new Natural(words); }

  /** Doesn't copy <code>words</code> or check <code>loInt</code>
   * or <code>hiInt</code>. 
   */

  private static final Natural unsafe (final int[] words,
                                       final int hiInt){
    if (hiInt<words.length) {
      final int[] ww = new int[hiInt];
      for (int i=0;i<hiInt;i++) { ww[i] = words[i]; }
      return new Natural(ww); }
    return new Natural(words); }

  /** Doesn't copy <code>words</code>. 
   */

  static final Natural unsafe (final int[] words) {
    final int hi = Ints.hiInt(words);
    return unsafe(words,hi); }

  /** Copy <code>words</code>. 
   *  */
  public static final Natural make (final int[] words) {
    final int end = Ints.hiInt(words);
    return new Natural(Arrays.copyOf(words,end)); }

  //--------------------------------------------------------------
  /** From a big endian {@code byte[]}, as produced by
   * {@link BigInteger#toByteArray()}.
   */

  private static final Natural fromBigEndianBytes (final byte[] a) {
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

  public static final Natural valueOf (final BigInteger u) {
    //assert 0<=u.signum();
    return fromBigEndianBytes(u.toByteArray()); }

  //-------------------------------------------------------------

  public static final Natural valueOf (final String s,
                                       final int radix) {
    return make(Ints.littleEndian(s,radix)); }

  public static final Natural valueOf (final String s) {
    return valueOf(s,0x10); }

  /** <code>0L<=u</code>. */

  public static final Natural valueOf (final long u) {
    //assert 0L<=u;
    //if (0L==u) { return zero(); }
    final int lo = (int) u;
    final int hi = (int) hiWord(u);
    if (0==hi) { 
      if (0==lo) { return new Natural(new int[0]); }
      return new Natural(new int[] {lo}); }
    return new Natural(new int[] { lo,hi }); }

  public static final Natural valueOf (final long u,
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
      return new Natural(vv); }
    if (0!=vv1) { 
      final int[] vv = new int[iShift+2];
      vv[iShift] = vv0;
      vv[iShift+1] = vv1;
      return new Natural(vv); }
    if (0!=vv0) { 
      final int[] vv = new int[iShift+1];
      vv[iShift] = vv0;
      return new Natural(vv); }
    return ZERO; }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
