package xfp.java.test.accumulators;

import java.util.List;

import org.junit.jupiter.api.Test;

import xfp.java.accumulators.BigFloatAccumulator0;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test summation algorithms.
 * <p>
 * <pre>
 * mvn -q test -Dtest=xfp/java/test/accumulators/RationalAccumulatorTest > RAT.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-24
 */

public final class RationalAccumulatorTest {

  //--------------------------------------------------------------
  private static final int DIM = 63;//256;
  private static final List<String> accumulators =
    List.of("xfp.java.accumulators.RationalAccumulator");

  @SuppressWarnings("static-method")
  @Test
  public final void tests () {
    //Debug.DEBUG=true;
    //Debug.println();
    //Debug.println(Classes.className(this));
    Common.l2Tests(
      Common.generators(DIM),
      Common.makeAccumulators(accumulators),
      BigFloatAccumulator0.make());
    Common.sumTests(
      Common.generators(DIM),
      Common.makeAccumulators(accumulators),
      BigFloatAccumulator0.make());
    Common.dotTests(
      Common.generators(DIM),
      Common.makeAccumulators(accumulators),
      BigFloatAccumulator0.make());
    //Debug.DEBUG=false;
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
