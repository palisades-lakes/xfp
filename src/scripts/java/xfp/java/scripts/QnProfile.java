package xfp.java.scripts;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

import xfp.java.algebra.OneSetOneOperation;
import xfp.java.algebra.OneSetTwoOperations;
import xfp.java.algebra.Set;
import xfp.java.algebra.TwoSetsTwoOperations;
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
  twoSetsTwoOperationsTests (final TwoSetsTwoOperations space) {

    SetTests.tests(space);
    
    final Map<Set,List> lawLists = space.linearSpaceLaws();
    
    final OneSetOneOperation elements = 
      (OneSetOneOperation) space.elements();
    final Supplier vg = 
      space.elements().generator( 
        PRNG.well44497b(
          Seeds.seed("seeds/Well44497b-2019-01-09.txt")));
    for(final Object law : lawLists.get(elements)) {
      for (int i=0; i<TRYS; i++) {
        assertTrue(((Predicate) law).test(vg));} } 
    
    final OneSetTwoOperations scalars = 
      (OneSetTwoOperations) space.scalars();
    final Supplier sg = 
      space.scalars().generator( 
        PRNG.well44497b(
          Seeds.seed("seeds/Well44497b-2019-01-11.txt")));
    for(final Object law : lawLists.get(scalars)) {
      for (int i=0; i<TRYS; i++) {
        assertTrue(((Predicate) law).test(sg));} } 
  
    for(final Object law : lawLists.get(space)) {
      for (int i=0; i<TRYS; i++) {
        assertTrue(((BiPredicate) law).test(vg,sg));} } }

  //--------------------------------------------------------------

  public static final void main (final String[] args) {
    for (final int n : new int[] { 1, 3, 13, 127, 1023}) {
      System.out.println(n);
      final TwoSetsTwoOperations qn = 
        TwoSetsTwoOperations.getQn(n);
      twoSetsTwoOperationsTests(qn); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
