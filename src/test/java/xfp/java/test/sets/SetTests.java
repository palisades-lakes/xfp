package xfp.java.test.sets;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Supplier;

import xfp.java.prng.PRNG;
import xfp.java.prng.Seeds;
import xfp.java.sets.Set;
import xfp.java.sets.Sets;

//----------------------------------------------------------------
/** Common code for testing sets. 
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-01-29
 */

@SuppressWarnings("unchecked")
public final class SetTests {

  private static final int TRYS = 1000;
  
  private static final void testMembership (final Set set) {
    final Supplier g = 
      set.generator( 
      PRNG.well44497b(
        Seeds.seed("seeds/Well44497b-2019-01-05.txt")));
    for (int i=0; i<TRYS; i++) {
      //System.out.println("set=" + set);
      final Object x = g.get();
      //System.out.println("element=" + x);
      assertTrue(
        set.contains(x),
        () -> set.toString() + "\n does not contain \n" + x); } }

  private static final void testEquivalence (final Set set) {
    final Supplier g = 
      set.generator( 
      PRNG.well44497b(
        Seeds.seed("seeds/Well44497b-2019-01-07.txt")));
    for (int i=0; i<TRYS; i++) {
      assertTrue(Sets.isReflexive(set,g));
      assertTrue(Sets.isSymmetric(set,g)); } }

  public static final void tests (final Set set) {
    testMembership(set);
    testEquivalence(set); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
