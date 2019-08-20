package xfp.java.scripts;

import xfp.java.algebra.TwoSetsOneOperation;
import xfp.java.linear.Qn;
import xfp.java.test.algebra.Profile;

//----------------------------------------------------------------
/** Profiling rational vector spaces.
 *
 * jy --source 12 src/scripts/java/xfp/java/scripts/QnProfile.java
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-25
 */

@SuppressWarnings("unchecked")
public final class QnProfile {

  //--------------------------------------------------------------

  public static final void main (final String[] args) throws InterruptedException {
    //    int i = 0;
    //    for (final Object law : Qn.space(1).laws()) {
    //      System.out.println((i++) + ": " + law); }
    Thread.sleep(16*1024);
    final long t = System.nanoTime();
    for (final int n : Profile.DIMENSIONS) {
      final TwoSetsOneOperation space = Qn.space(n);
      if (! Profile.structureTests(space,Profile.TRYS)) {
        System.out.println("false"); } }
    System.out.printf("total secs: %8.2f\n",
      Double.valueOf((System.nanoTime()-t)*1.0e-9));
    Thread.sleep(16*1024); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
