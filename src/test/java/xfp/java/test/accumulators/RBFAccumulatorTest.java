package xfp.java.test.accumulators;

import java.util.List;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Test;

import xfp.java.Classes;
import xfp.java.accumulators.Accumulator;
import xfp.java.accumulators.RationalAccumulator;
import xfp.java.numbers.Doubles;
import xfp.java.prng.Generator;
import xfp.java.prng.PRNG;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test summation algorithms. 
 * <p>
 * <pre>
 * mvn test -Dtest=xfp/java/test/accumulators/RBFAccumulatorTest > RBFAccumulatorTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-01
 */

public final class RBFAccumulatorTest {

  //--------------------------------------------------------------
  private static final int DIM = 16 * 1024;

  @Test
  public final void tests () {
    System.out.println();
    System.out.println(Classes.className(this));

    final UniformRandomProvider urp = 
      PRNG.well44497b("seeds/Well44497b-2019-01-05.txt");
    final List<Generator> generators = 
    List.of(
      Doubles.finiteGenerator(DIM,urp,Common.deMax(DIM)-10),
      Doubles.gaussianGenerator(DIM,urp,0.0,Common.dMax(DIM)),
      Doubles.laplaceGenerator(DIM,urp,0.0,Common.dMax(DIM)),
      Doubles.uniformGenerator(DIM,urp,-Common.dMax(DIM),Common.dMax(DIM)));

    final List<Accumulator> accumulators = 
      Common.makeAccumulators(new String[] 
        { 
         "xfp.java.accumulators.RBFAccumulator",
        });

    Common.sumTests(
      generators,accumulators,RationalAccumulator.make()); 
    Common.dotTests(
      generators,accumulators,RationalAccumulator.make()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
