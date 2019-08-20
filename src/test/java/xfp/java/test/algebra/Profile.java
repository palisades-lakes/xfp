package xfp.java.test.algebra;

import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;

import xfp.java.algebra.Set;
import xfp.java.algebra.Sets;
import xfp.java.algebra.Structure;
import xfp.java.prng.PRNG;

//----------------------------------------------------------------
/** Profiling rational vector spaces.
 *
 * jy --source 12 src/scripts/java/xfp/java/scripts/QnProfile.java
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-21
 */

@SuppressWarnings("unchecked")
public final class Profile {

  public static final int TRYS = 127;

  public static final int[] DIMENSIONS =
    new int[] { //1, 3,
                32*1024 };

  //--------------------------------------------------------------

  public static final boolean testMembership (final Set set,
                                              final int ntrys) {
    final Supplier g =
      set.generator(
        ImmutableMap.of(
          Set.URP,
          PRNG.well44497b(
            "seeds/Well44497b-2019-01-05.txt")));
    for (int i=0; i<ntrys; i++) {
      final Object x = g.get();
      if (! set.contains(x)) { return false; } }
    return true; }

  public static final boolean testEquivalence (final Set set,
                                               final int ntrys) {
    final Supplier g =
      set.generator(
        ImmutableMap.of(
          Set.URP,
          PRNG.well44497b("seeds/Well44497b-2019-01-07.txt")));
    for (int i=0; i<ntrys; i++) {
      if (! Sets.isReflexive(set,g)) { return false; }
      if (! Sets.isSymmetric(set,g)) { return false; } }
    return true; }

  public static final boolean setTests (final Set set,
                                        final int ntrys) {
    return
      testMembership(set,ntrys)
      &&
      testEquivalence(set,ntrys); }

  //--------------------------------------------------------------

  public static final boolean
  structureTests (final Structure s,
                  final int ntrys) {
    final Map<Set,Supplier> generators =
      s.generators(
        ImmutableMap.of(
          Set.URP,
          PRNG.well44497b("seeds/Well44497b-2019-01-09.txt")));
    for(final Predicate<Map<Set,Supplier>> law : s.laws()) {
      //System.out.println(law);
      for (int i=0; i<ntrys; i++) {
        if (! law.test(generators)) { return false; } } }
    return true; }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
