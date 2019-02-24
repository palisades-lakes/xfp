package xfp.java.test.algebra;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import xfp.java.algebra.Set;
import xfp.java.algebra.Structure;
import xfp.java.linear.BigDecimalsN;
import xfp.java.linear.BigFractionsN;
import xfp.java.linear.Qn;
import xfp.java.linear.RatiosN;
import xfp.java.numbers.BigDecimals;
import xfp.java.numbers.BigFractions;
import xfp.java.numbers.Q;
import xfp.java.numbers.Ratios;
import xfp.java.prng.PRNG;
import xfp.java.prng.Seeds;

//----------------------------------------------------------------
/** Common code for testing sets. 
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-23
 */

@SuppressWarnings("unchecked")
public final class AlgebraicStructureTests {

  private static final int TRYS = 1023;
  static final int SPACE_TRYS = 127;

  //--------------------------------------------------------------

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

  @SuppressWarnings({ "static-method" })
  @Test
  public final void tests () {

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
    
    for (final int n : new int[] { 1, 3, 13, 127}) {
      structureTests(BigDecimalsN.bigDecimalsNGroup(n),SPACE_TRYS); 
      structureTests(BigFractionsN.bigFractionsNGroup(n),SPACE_TRYS); 
      structureTests(RatiosN.ratiosNGroup(n),SPACE_TRYS); 
      structureTests(Qn.qnGroup(n),SPACE_TRYS); 
      structureTests(BigDecimalsN.getBDnSpace(n),SPACE_TRYS); 
      structureTests(BigFractionsN.getBFnSpace(n),SPACE_TRYS); 
      structureTests(RatiosN.getRatiosnSpace(n),SPACE_TRYS); 
      structureTests(Qn.getQnSpace(n),SPACE_TRYS); 
      } }


  //--------------------------------------------------------------
}
//--------------------------------------------------------------
