package xfp.java.scripts;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import xfp.java.algebra.Set;
import xfp.java.algebra.Structure;
import xfp.java.algebra.TwoSetsTwoOperations;
import xfp.java.linear.Qn;
import xfp.java.prng.PRNG;
import xfp.java.prng.Seeds;
import xfp.java.test.algebra.SetTests;

//----------------------------------------------------------------
/** Profiling rational vector spaces. 
 *
 * jy --source 11 src/scripts/java/xfp/java/scripts/QnProfile.java
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-20
 */

@SuppressWarnings("unchecked")
public final class QnProfile {

  private static final int TRYS = 1023;

  private static final void 
  structureTests (final Structure s,
                  final int n) {
    SetTests.tests(s);
    final Map<Set,Supplier> samplers = 
      s.samplers(
        PRNG.well44497b(
          Seeds.seed("seeds/Well44497b-2019-01-09.txt")));
    for(final Predicate law : s.laws()) {
      for (int i=0; i<n; i++) {
        assertTrue(law.test(samplers)); } } }


  //--------------------------------------------------------------

  public static final void main (final String[] args) {
    for (final int n : new int[] { 1, 3, 13, 127, 1023}) {
      System.out.println(n);
      final TwoSetsTwoOperations qn = Qn.getQnSpace(n);
      structureTests(qn,n); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
