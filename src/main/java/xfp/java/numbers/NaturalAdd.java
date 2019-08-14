package xfp.java.numbers;

import static xfp.java.numbers.Numbers.hiWord;
import static xfp.java.numbers.Numbers.loWord;
import static xfp.java.numbers.Numbers.unsigned;

import java.util.Arrays;

/** Division, gcd, etc., of natural numbers.
 * 
 * Non-instantiable.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-29
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
  // disable constructor
  //--------------------------------------------------------------

  private NaturalAdd () {
    throw new
    UnsupportedOperationException(
      "can't instantiate " + getClass().getCanonicalName()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
