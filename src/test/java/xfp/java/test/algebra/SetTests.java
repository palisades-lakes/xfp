package xfp.java.test.algebra;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import xfp.java.Classes;
import xfp.java.algebra.Set;
import xfp.java.algebra.Sets;
import xfp.java.numbers.BigDecimals;
import xfp.java.numbers.BigFractions;
import xfp.java.numbers.Ratios;
import xfp.java.prng.PRNG;
import xfp.java.prng.Seeds;

//----------------------------------------------------------------
/** Common code for testing sets. 
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-19
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
        () -> set.toString() + "\n does not contain \n" + 
          Classes.getSimpleName(Classes.getClass(x)) + ": " +
        x); } }

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

  @SuppressWarnings({ "static-method" })
  @Test
  public final void bigDecimals () { 
    SetTests.tests(BigDecimals.get()); }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void bigFractions () { 
    SetTests.tests(BigFractions.get()); }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void ratios () { 
    SetTests.tests(Ratios.get()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
