package xfp.java.test.algebra;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;

import xfp.java.Classes;
import xfp.java.algebra.Set;
import xfp.java.algebra.Sets;
import xfp.java.numbers.BigDecimals;
import xfp.java.numbers.BigFloats;
import xfp.java.numbers.Q;
import xfp.java.numbers.RationalFloats;
import xfp.java.prng.PRNG;

//----------------------------------------------------------------
/** Common code for testing sets.
 * <p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/algebra/SetTests test > Sets.txt
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-12
 */

@SuppressWarnings("unchecked")
public final class SetTests {

  private static final int TRYS = 1023;

  private static final void testMembership (final Set set) {
    final Supplier g =
      set.generator(
        ImmutableMap.of(
          Set.URP,
          PRNG.well44497b("seeds/Well44497b-2019-01-05.txt")));
    for (int i=0; i<TRYS; i++) {
      //System.out.println("set=" + set);
      final Object x = g.get();
      //System.out.println("element=" + x);
      assertTrue(
        set.contains(x),
        () -> set.toString() + "\n does not contain \n" +
          Classes.className(x) + ": " +
          x); } }

  private static final void testEquivalence (final Set set) {
    final Supplier g =
      set.generator(
        ImmutableMap.of(
          Set.URP,
          PRNG.well44497b("seeds/Well44497b-2019-01-07.txt")));
    for (int i=0; i<TRYS; i++) {
      assertTrue(Sets.isReflexive(set,g));
      assertTrue(Sets.isSymmetric(set,g)); } }

  public static final void tests (final Set set) {
    testMembership(set);
    testEquivalence(set); }

  //--------------------------------------------------------------

  @SuppressWarnings({ "static-method" })
  @Test
  public final void Q () {
    SetTests.tests(Q.get()); }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void BigFloats () {
    SetTests.tests(BigFloats.get()); }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void RationalFloats () {
    SetTests.tests(RationalFloats.get()); }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void bigDecimals () {
    SetTests.tests(BigDecimals.get()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
