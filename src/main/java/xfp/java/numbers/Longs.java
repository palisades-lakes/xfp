package xfp.java.numbers;

import static xfp.java.numbers.Numbers.hiBit;

import xfp.java.algebra.Set;
import xfp.java.exceptions.Exceptions;

/** Utilities for <code>long</code>, <code>long[]</code>.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-31
 */

public final class Longs implements Set {

  //--------------------------------------------------------------

  public static final int compare (final long m0,
                                   final long m1,
                                   final int upShift) {
   //assert 0L<=m0;
   //assert 0L<=m1;
   //assert 0<=upShift : "upShift=" + upShift;
   if (0==upShift) { return Long.compare(m0,m1); }
   if (0L==m1) {
     if (0L==m0) { return 0; }
     return 1; }
   if (0L==m0) { return -1; }
   final int hiBit0 = hiBit(m0);
   final int hiBit1 = hiBit(m1) + upShift;
   if (hiBit0<hiBit1) { return -1; }
   if (hiBit0>hiBit1) { return 1; }
   // shifted m1 must fit in one long, since hiBit0 < 64
   final long m11 = (m1<<upShift);
   return Long.compare(m0,m11); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private Longs () { 
    Exceptions.unsupportedOperation(Longs.class,"new"); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

