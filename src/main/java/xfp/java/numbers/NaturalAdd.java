package xfp.java.numbers;

import static xfp.java.numbers.Numbers.hiWord;
import static xfp.java.numbers.Numbers.loWord;
import static xfp.java.numbers.Numbers.unsigned;

/** Division, gcd, etc., of natural numbers.
 * 
 * Non-instantiable.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-08-15
 */

@SuppressWarnings("unchecked")
public final class NaturalAdd {

  //--------------------------------------------------------------
  // add (shifted) long to little endian unsigned ints
  //--------------------------------------------------------------

  public static final int[]  add (final int[] tt,
                                  final int nt,
                                  final long u) {
    //assert isValid();
    //assert 0L<=u;
    //if (0>=nt) { return new int[0]; }
    //if (0L==u) { return Arrays.copyOf(tt,nt); }
    final int[] vv = new int[Math.max(2,nt+1)];
    long sum = loWord(u);
    if (0<nt) { sum += unsigned(tt[0]); }
    vv[0] = (int) sum;
    sum = (sum>>>32);
    sum += hiWord(u);
    if (1<nt) { sum += unsigned(tt[1]); }
    vv[1] = (int) sum;
    sum = (sum>>>32);
    int i=2;
    for (;i<nt;i++) {
      if (0L==sum) { break; }
      sum += unsigned(tt[i]);
      vv[i] = (int) sum;
      sum = (sum>>>32); }
    for (;i<nt;i++) { vv[i] = tt[i]; }
    if (0L!=sum) { vv[nt] = (int) sum; }
    return vv; }

  //--------------------------------------------------------------

  private static final long addByWords (final int[] tt,
                                        final int nt,
                                        final long u,
                                        final int iShift,
                                        final int[] vv) {
    long sum = loWord(u);
    int i = iShift;
    if (i<nt) { sum += unsigned(tt[i]); }
    vv[i++] = (int) sum; 
    sum = hiWord(u) + (sum>>>32);
    if (i<nt) { sum += unsigned(tt[i]); }
    vv[i] = (int) sum; 
    return (sum>>>32); }

  private static final long addByBits (final int[] tt,
                                       final int nt,
                                       final long u,
                                       final int iShift,
                                       final int bShift,
                                       final int[] vv) {
    final int uhi = (int) (u>>>32);
    final int ulo = (int) u;
    final int rShift = 32-bShift;
    int i=iShift;
    long sum = unsigned(ulo<<bShift);
    if (i<nt) { sum += unsigned(tt[i]); }
    vv[i++] = (int) sum; 
    sum = (sum>>>32);
    sum += unsigned((uhi<<bShift)|(ulo>>>rShift));
    if (i<nt) { sum += unsigned(tt[i]); }
    vv[i++] = (int) sum; 
    sum = (sum>>>32);
    sum += unsigned(uhi>>>rShift);
    if (i<nt) { sum += unsigned(tt[i]); }
    vv[i++] = (int) sum; 
    return (sum>>>32); }

  public static final int[] add (final int[] tt,
                                 final int nt,
                                 final long u,
                                 final int upShift) {
    //assert 0<nt;
    //assert nt<=tt.length;
    //assert 0<=u;
    //assert 0<=upShift;
    //if (0>=nt) { return new int[0]; }
    //if (0L==u) { return Arrays.copyOf(tt,nt); }
    //if (0==upShift) { return add(tt,nt,u); }

    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    final int nu = iShift+2;
    final int n = Math.max(nt,nu);
    final int[] vv = new int[n+1];
    for (int i=0;i<Math.min(iShift,nt);i++) { vv[i] = tt[i]; }
    //System.arraycopy(tt,0,vv,0,Math.min(nt,iShift));

    long sum = 0L;
    int i;
    if (0==bShift) { 
      sum = addByWords(tt,nt,u,iShift,vv); 
      i=iShift+2; }
    else { 
      sum = addByBits(tt,nt,u,iShift,bShift,vv); 
      i=iShift+3; }

    for (;i<nt;i++) {
      if (0L==sum) { break; }
      sum += unsigned(tt[i]);
      vv[i] = (int) sum; 
      sum = (sum>>>32); }

    for (int j=i;j<nt;j++) { vv[j] = tt[j]; }
    //if (i<nt) { System.arraycopy(tt,i,vv,i,nt-i); }
    if (0L!=sum) { vv[n] = (int) sum; }

    return vv; }

  //--------------------------------------------------------------
  // for BigFloat
  //--------------------------------------------------------------

  private static final BigFloat
  add0 (final boolean p0,
        final NaturalLE t0,
        final boolean p1,
        final long t1,
        final int upShift,
        final int e) {
    //assert 0L<=t1;
    //assert 0<=upShift
    if (p0^p1) { // different signs
      final int c = t0.compareTo(t1,upShift);
      if (0==c) { return BigFloat.ZERO; }
      // t1 > t0
      if (0>c) {
        return BigFloat.valueOf(p1,t0.subtractFrom(t1,upShift),e); }
      // t0 > t1
      return BigFloat.valueOf(p0,t0.subtract(t1,upShift),e); }
    return BigFloat.valueOf(p0,t0.add(t1,upShift),e); }

  //  private static final BigFloat
  //  add0 (final boolean p0,
  //                   final NaturalLE t0,
  //                   final boolean p1,
  //                   final long t1,
  //                   final int upShift,
  //                   final int e) {
  //    //assert 0L<=t1;
  //    //assert 0<=upShift
  //    if (p0^p1) { // different signs
  //      final int c = t0.compareTo(t1,upShift);
  //      if (0==c) { return BigFloat.ZERO; }
  //      // t1 > t0
  //      if (0>c) {
  //        return BigFloat.valueOf(p1,t0.subtractFrom(t1,upShift),e); }
  //      // t0 > t1
  //      return BigFloat.valueOf(p0,t0.subtract(t1,upShift),e); }
  //    return BigFloat.valueOf(p0,t0.add(t1,upShift),e); }

  //--------------------------------------------------------------

  static final BigFloat
  add1 (final boolean p0,
        final NaturalLE t0,
        final int upShift,
        final boolean p1,
        final long t1,
        final int e) {
    //assert 0L<=t1;
    //assert 0<=upShift
    if (p0^p1) { // different signs
      final int c = t0.compareTo(upShift,t1);
      if (0==c) { return BigFloat.ZERO; }
      // t1 > t0
      if (0 > c) {
        return BigFloat.valueOf(p1,t0.subtractFrom(upShift,t1),e); }
      // t0 > t1
      return BigFloat.valueOf(p0,t0.subtract(upShift,t1),e); }
    return BigFloat.valueOf(p0,t0.add(upShift,t1),e); }

  //--------------------------------------------------------------

  static final BigFloat
  add2 (final boolean p0,
        final NaturalLE t0,
        final boolean p1,
        final long t1,
        final int e) {
    //assert 0L<=t1;
    //assert 0<=upShift
    if (p0^p1) { // different signs
      final int c = t0.compareTo(t1);
      if (0==c) { return BigFloat.ZERO; }
      // t1 > t0
      if (0 > c) {
        return BigFloat.valueOf(p1,t0.subtractFrom(t1),e); }
      // t0 > t1
      return BigFloat.valueOf(p0,t0.subtract(t1),e); }
    return BigFloat.valueOf(p0,t0.add(t1),e); }

  //--------------------------------------------------------------

  static final BigFloat
  add (final boolean p0,
       final Natural t0,
       final int e0,
       final boolean p1,
       final long t11,
       final int e11) {
    //assert 0L<=t11;
    //if (0L==t11) { return this; }
    // minimize long bits
    final int shift = Numbers.loBit(t11);
    final long t1 = (t11>>>shift);
    final int e1 = e11+shift;
    //Debug.println("e0=" + e0 + ", e11=" + e11 + ",shift=" + shift);
    if (e0<=e1) { 
      return add0(p0,((NaturalLE) t0),p1,t1,e1-e0,e0); }
    return add1(p0,((NaturalLE) t0),e0-e1,p1,t1,e1); }
  // 1-2% slower
  //return add2(p0,((NaturalLE) t0).shiftUp(e0-e1),p1,t1,e1); }

  //--------------------------------------------------------------
  // disable constructor
  //--------------------------------------------------------------

  private NaturalAdd () {
    throw new
    UnsupportedOperationException(
      "can't instantiate " + getClass().getCanonicalName()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
