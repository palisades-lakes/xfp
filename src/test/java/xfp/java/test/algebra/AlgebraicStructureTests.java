package xfp.java.test.algebra;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;

import xfp.java.algebra.Set;
import xfp.java.algebra.Structure;
import xfp.java.linear.Dn;
import xfp.java.linear.Fn;
import xfp.java.linear.Qn;
import xfp.java.linear.RationalFloatsN;
import xfp.java.numbers.BigFloats;
import xfp.java.numbers.Doubles;
import xfp.java.numbers.Floats;
import xfp.java.numbers.Q;
import xfp.java.numbers.RationalFloats;
import xfp.java.prng.PRNG;

//----------------------------------------------------------------
/** <pre>
 * mvn -q -Dtest=xfp/java/test/algebra/AlgebraicStructureTests test > AST.txt
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-15
 */

@SuppressWarnings("unchecked")
public final class AlgebraicStructureTests {
  private static final int TRYS = 31;
  static final int SPACE_TRYS = 5;

  //--------------------------------------------------------------

  private static final void
  structureTests (final Structure s,
                  final int n) {
    SetTests.tests(s);
    final Map<Set,Supplier> generators =
      s.generators(
        ImmutableMap.of(
          Set.URP,
          PRNG.well44497b("seeds/Well44497b-2019-01-09.txt")));
    for(final Predicate law : s.laws()) {
      for (int i=0; i<n; i++) {
        final boolean result = law.test(generators);
        assertTrue(result,
          s.toString() + " : " + law.toString()); } } }

  //--------------------------------------------------------------

  @SuppressWarnings({ "static-method" })
  @Test
  public final void tests () {

    //Debug.DEBUG=false;
    structureTests(BigFloats.ADDITIVE_MAGMA,TRYS);
    structureTests(BigFloats.MULTIPLICATIVE_MAGMA,TRYS);
    structureTests(BigFloats.RING,TRYS);

    structureTests(RationalFloats.ADDITIVE_MAGMA,TRYS);
    structureTests(RationalFloats.MULTIPLICATIVE_MAGMA,TRYS);
    structureTests(RationalFloats.FIELD,TRYS);

    structureTests(Q.FIELD,TRYS);

     structureTests(Floats.ADDITIVE_MAGMA,TRYS);
    structureTests(Floats.MULTIPLICATIVE_MAGMA,TRYS);
    structureTests(Floats.FLOATING_POINT,TRYS);

    structureTests(Doubles.ADDITIVE_MAGMA,TRYS);
    structureTests(Doubles.MULTIPLICATIVE_MAGMA,TRYS);
    structureTests(Doubles.FLOATING_POINT,TRYS);

    for (final int n : new int[] { 1, 3, 63, 257 }) {
      structureTests(RationalFloatsN.group(n),SPACE_TRYS);
      structureTests(RationalFloatsN.space(n),SPACE_TRYS);

      structureTests(Qn.group(n),SPACE_TRYS);
      structureTests(Qn.space(n),SPACE_TRYS);

      structureTests(Fn.magma(n),SPACE_TRYS);
      structureTests(Fn.space(n),SPACE_TRYS);

      structureTests(Dn.magma(n),SPACE_TRYS);
      structureTests(Dn.space(n),SPACE_TRYS);
    }
    //Debug.DEBUG=false;
  }


  //--------------------------------------------------------------
}
//--------------------------------------------------------------
