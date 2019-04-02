package xfp.java.test.accumulators;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Streams;

import xfp.java.Classes;
import xfp.java.accumulators.Accumulator;
import xfp.java.accumulators.RBFAccumulator;
import xfp.java.numbers.Doubles;
import xfp.java.prng.Generator;
import xfp.java.prng.PRNG;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test summation algorithms. 
 * <p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/accumulators/SumTest test > SumTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-01
 */

public final class SumTest {

  //--------------------------------------------------------------

  private static final int DIM = 8 * 1024 * 1024;

  @Test
  public final void sumTests () {
    System.out.println();
    System.out.println(Classes.className(this));

    final UniformRandomProvider urp0 = 
      PRNG.well44497b("seeds/Well44497b-2019-01-05.txt");
    final UniformRandomProvider urp1 = 
      PRNG.well44497b("seeds/Well44497b-2019-01-07.txt");
    final UniformRandomProvider urp2 = 
      PRNG.well44497b("seeds/Well44497b-2019-01-09.txt");
    final UniformRandomProvider urp3 = 
      PRNG.well44497b("seeds/Well44497b-2019-01-11.txt");
    final UniformRandomProvider urp4 = 
      PRNG.well44497b("seeds/Well44497b-2019-04-01.txt");
    
    final double dmax = Common.dMax(DIM);
    final List<Generator> gs0 =
      List.of(
        Doubles.finiteGenerator(DIM,urp0,Common.deMax(DIM)),
        Doubles.gaussianGenerator(DIM,urp1,0.0,dmax),
        Doubles.laplaceGenerator(DIM,urp2,0.0,dmax),
        Doubles.uniformGenerator(DIM,urp3,-dmax,dmax));
    final List<Generator> gs1 =
      gs0.stream().map(Doubles::zeroSumGenerator)
    .collect(Collectors.toUnmodifiableList());
    final List<Generator> gs2 =
      gs1.stream().map((g) -> Doubles.shuffledGenerator(g,urp4))
    .collect(Collectors.toUnmodifiableList());
    final List<Generator> gs =
      Streams.concat(gs0.stream(),gs1.stream(), gs2.stream())
      .collect(Collectors.toUnmodifiableList());


    final List<Accumulator> accumulators = 
      Common.makeAccumulators(new String[] 
        {
         "xfp.java.accumulators.DoubleAccumulator",
         //"xfp.java.accumulators.DoubleFmaAccumulator",
         //"xfp.java.accumulators.StrictDoubleAccumulator",
         //"xfp.java.accumulators.StrictDoubleFmaAccumulator",
         //"xfp.java.accumulators.FloatAccumulator",
         //"xfp.java.accumulators.FloatFmaAccumulator",
         //"xfp.java.accumulators.RationalAccumulator",
         "xfp.java.accumulators.RBFAccumulator",
        });

    Common.sumTests(gs,accumulators,RBFAccumulator.make()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
