package xfp.java.test.algebra;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;

import xfp.java.algebra.Set;
import xfp.java.algebra.Structure;
import xfp.java.linear.BigDecimalsN;
import xfp.java.linear.BigFractionsN;
import xfp.java.linear.Dn;
import xfp.java.linear.ERationalsN;
import xfp.java.linear.Fn;
import xfp.java.linear.Qn;
import xfp.java.linear.RatiosN;
import xfp.java.numbers.BigDecimals;
import xfp.java.numbers.BigFractions;
import xfp.java.numbers.Doubles;
import xfp.java.numbers.ERationals;
import xfp.java.numbers.Floats;
import xfp.java.numbers.Q;
import xfp.java.numbers.Ratios;
import xfp.java.prng.PRNG;

//----------------------------------------------------------------
/** Common code for testing sets. 
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-05
 */

@SuppressWarnings("unchecked")
public final class AlgebraicStructureTests {

  private static final int TRYS = 512;
  static final int SPACE_TRYS = 64;

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
        assertTrue(result); } } }

  //--------------------------------------------------------------

  @SuppressWarnings({ "static-method" })
  @Test
  public final void tests () {

    structureTests(ERationals.ADDITIVE_MAGMA,TRYS);
    structureTests(ERationals.MULTIPLICATIVE_MAGMA,TRYS); 
    structureTests(ERationals.FIELD,TRYS);  

    structureTests(BigDecimals.ADDITIVE_MAGMA,TRYS);
    structureTests(BigDecimals.MULTIPLICATIVE_MAGMA,TRYS); 
    structureTests(BigDecimals.RING,TRYS);  

    structureTests(BigFractions.ADDITIVE_MAGMA,TRYS);
    structureTests(BigFractions.MULTIPLICATIVE_MAGMA,TRYS); 
    structureTests(BigFractions.FIELD,TRYS); 

    structureTests(Ratios.ADDITIVE_MAGMA,TRYS);
    structureTests(Ratios.MULTIPLICATIVE_MAGMA,TRYS); 
    structureTests(Ratios.FIELD,TRYS); 

    structureTests(Q.FIELD,TRYS); 

    structureTests(Floats.ADDITIVE_MAGMA,TRYS);
    structureTests(Floats.MULTIPLICATIVE_MAGMA,TRYS); 
    structureTests(Floats.FLOATING_POINT,TRYS); 

    structureTests(Doubles.ADDITIVE_MAGMA,TRYS);
    structureTests(Doubles.MULTIPLICATIVE_MAGMA,TRYS); 
    structureTests(Doubles.FLOATING_POINT,TRYS); 

    for (final int n : new int[] { 1, 3, 255}) {
      structureTests(ERationalsN.group(n),SPACE_TRYS); 
      structureTests(ERationalsN.space(n),SPACE_TRYS); 

      structureTests(BigDecimalsN.group(n),SPACE_TRYS); 
      structureTests(BigDecimalsN.space(n),SPACE_TRYS); 

      structureTests(BigFractionsN.group(n),SPACE_TRYS); 
      structureTests(BigFractionsN.space(n),SPACE_TRYS); 

      structureTests(RatiosN.group(n),SPACE_TRYS); 
      structureTests(RatiosN.space(n),SPACE_TRYS); 

      structureTests(Qn.group(n),SPACE_TRYS);
      structureTests(Qn.space(n),SPACE_TRYS); 

      structureTests(Fn.magma(n),SPACE_TRYS); 
      structureTests(Fn.space(n),SPACE_TRYS); 

      structureTests(Dn.magma(n),SPACE_TRYS); 
      structureTests(Dn.space(n),SPACE_TRYS); 
    } }


  //--------------------------------------------------------------
}
//--------------------------------------------------------------
