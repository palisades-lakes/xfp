package xfp.java.test.accumulators;

import java.util.List;

import org.junit.jupiter.api.Test;

import xfp.java.accumulators.KahanAccumulator0;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test summation algorithms.
 * <p>
 * <pre>
 * mvn -q test -Dtest=xfp/java/test/accumulators/KahanAccumulatorTest > KAT.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-29
 */

public final class KahanAccumulatorTest {

  //--------------------------------------------------------------
  private static final int DIM = 517;
  private static final List<String> accumulators =
    List.of("xfp.java.accumulators.KahanAccumulator");

  @SuppressWarnings("static-method")
  @Test
  public final void tests () {
    //Debug.DEBUG=true;
    //Debug.println();
    //Debug.println(Classes.className(this));
    Common.l2Tests(
      Common.generators(DIM),
      Common.makeAccumulators(accumulators),
      KahanAccumulator0.make());
    Common.sumTests(
      Common.generators(DIM),
      Common.makeAccumulators(accumulators),
      KahanAccumulator0.make());
    Common.dotTests(
      Common.generators(DIM),
      Common.makeAccumulators(accumulators),
      KahanAccumulator0.make());
    //Debug.DEBUG=false;
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
