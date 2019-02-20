package xfp.java.test.algebra;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import xfp.java.algebra.OneSetOneOperation;
import xfp.java.algebra.OneSetTwoOperations;
import xfp.java.algebra.Set;
import xfp.java.algebra.TwoSetsTwoOperations;
import xfp.java.prng.PRNG;
import xfp.java.prng.Seeds;

//----------------------------------------------------------------
/** Common code for testing sets. 
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-01-22
 */

@SuppressWarnings("unchecked")
public final class AlgebraicStructureTests {

  private static final int TRYS = 1000;
  static final int LINEARSPACE_TRYS = 127;

  // TODO: should each structure know what laws it obeys?
  // almost surely.
  // then only need one test method...
  
  public static final void 
  magmaTests (final OneSetOneOperation magma) {
    SetTests.tests(magma);
    final Supplier g = 
      magma.generator( 
        PRNG.well44497b(
          Seeds.seed("seeds/Well44497b-2019-01-09.txt")));
    for(final Predicate law : magma.magmaLaws()) {
      for (int i=0; i<TRYS; i++) {
        assertTrue(law.test(g)); } } }

  public static final void 
  commutativeGroupTests (final OneSetOneOperation group) {
    SetTests.tests(group);
    final Supplier g = 
      group.generator( 
        PRNG.well44497b(
          Seeds.seed("seeds/Well44497b-2019-01-09.txt")));
    for(final Predicate law : group.commutativeGroupLaws()) {
      for (int i=0; i<TRYS; i++) {
        assertTrue(law.test(g)); } } }

  public static final void 
  commutativeRingTests (final OneSetTwoOperations commutativeRing) {
    SetTests.tests(commutativeRing);
    final Supplier g = 
      commutativeRing.generator( 
        PRNG.well44497b(
          Seeds.seed("seeds/Well44497b-2019-01-11.txt")));

    for(final Predicate law : commutativeRing.commutativeRingLaws()) {
      for (int i=0; i<TRYS; i++) {
        assertTrue(law.test(g), law.toString() + " failed."); } } }

  public static final void 
  fieldTests (final OneSetTwoOperations field) {
    SetTests.tests(field);
    final Supplier g = 
      field.generator( 
        PRNG.well44497b(
          Seeds.seed("seeds/Well44497b-2019-01-11.txt")));

    for(final Predicate law : field.fieldLaws()) {
      for (int i=0; i<TRYS; i++) {
        assertTrue(law.test(g), law.toString() + " failed."); } } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void bigDecimals () {
    magmaTests(OneSetOneOperation.BIGDECIMALS_ADD);
    magmaTests(OneSetOneOperation.BIGDECIMALS_MULTIPLY); 
    commutativeRingTests(OneSetTwoOperations.BIGDECIMALS_RING);  }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void bigFractions () {
    magmaTests(OneSetOneOperation.BIGFRACTIONS_ADD);
    magmaTests(OneSetOneOperation.BIGFRACTIONS_MULTIPLY); 
    fieldTests(OneSetTwoOperations.BIGFRACTIONS_FIELD);  }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void ratios () {
    magmaTests(OneSetOneOperation.RATIOS_ADD);
    magmaTests(OneSetOneOperation.RATIOS_MULTIPLY); 
    fieldTests(OneSetTwoOperations.RATIOS_FIELD); }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void qTests () {
    fieldTests(OneSetTwoOperations.Q_FIELD); }

  //--------------------------------------------------------------

  public static final void 
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
      for (int i=0; i<LINEARSPACE_TRYS; i++) {
        assertTrue(((Predicate) law).test(vg));} } 
    
    final OneSetTwoOperations scalars = 
      (OneSetTwoOperations) space.scalars();
    final Supplier sg = 
      space.scalars().generator( 
        PRNG.well44497b(
          Seeds.seed("seeds/Well44497b-2019-01-11.txt")));
    for(final Object law : lawLists.get(scalars)) {
      for (int i=0; i<LINEARSPACE_TRYS; i++) {
        assertTrue(((Predicate) law).test(sg));} } 
  
    for(final Object law : lawLists.get(space)) {
      for (int i=0; i<LINEARSPACE_TRYS; i++) {
        assertTrue(((BiPredicate) law).test(vg,sg));} } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void bigDecimalN () {
    for (final int n : new int[] { 1, 3, 13, 127}) {
      final TwoSetsTwoOperations bdn = TwoSetsTwoOperations.getBDn(n);
      AlgebraicStructureTests.twoSetsTwoOperationsTests(bdn); } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void ratiosN () {
    for (final int n : new int[] { 1, 3, 13, 127}) {
      final TwoSetsTwoOperations ratiosN = 
        TwoSetsTwoOperations.getRatiosN(n);
      AlgebraicStructureTests.twoSetsTwoOperationsTests(ratiosN); } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void bigFractionN () {
    for (final int n : new int[] { 1, 3, 13, 127}) {
      final TwoSetsTwoOperations bfn = 
        TwoSetsTwoOperations.getBFn(n);
      AlgebraicStructureTests.twoSetsTwoOperationsTests(bfn); } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void qN () {
    for (final int n : new int[] { 1, 3, 13, 127}) {
      final TwoSetsTwoOperations qn = 
        TwoSetsTwoOperations.getQn(n);
      AlgebraicStructureTests.twoSetsTwoOperationsTests(qn); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
