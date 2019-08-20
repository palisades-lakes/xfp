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
    int i=iShift;
    final long us = (u<<bShift);
    long sum = loWord(us);
    if (i<nt) { sum += unsigned(tt[i]); }
    vv[i++] = (int) sum; 
    sum = (sum>>>32);
    sum += hiWord(us);
    if (i<nt) { sum += unsigned(tt[i]); }
    vv[i++] = (int) sum; 
    sum = (sum>>>32);
    sum += (u>>>(64-bShift));
    if (i<nt) { sum += unsigned(tt[i]); }
    vv[i] = (int) sum; 
    return (sum>>>32); }

  public static final int[] add (final int[] tt,
                                 final int nt,
                                 final long u,
                                 final int upShift) {
    //assert 0<=u;
    //assert 0<=upShift;
    final int iShift = (upShift>>>5);
    final int bShift = (upShift&0x1f);
    final int nu = iShift+((0==bShift)?1:2);
    final int n = Math.max(nt,nu);
    final int[] vv = new int[n+1];
    for (int i=0;i<Math.min(iShift,nt);i++) { vv[i] = tt[i]; }

    long sum = 0L;
    if (0==bShift) { sum = addByWords(tt,nt,u,iShift,vv);}
    else { sum = addByBits(tt,nt,u,iShift,bShift,vv); }

    int i=nu+1;
    for (;i<nt;i++) {
      if (0L==sum) { break; }
      sum += unsigned(tt[i]);
      vv[i] = (int) sum; 
      sum = (sum>>>32); }
    for (;i<nt;i++) { vv[i] = tt[i]; }
    final int vvn = (int) sum;
    if (0!=vvn) { vv[i] = vvn; }
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
