package xfp.java.test.accumulators;

import java.util.List;

import org.junit.jupiter.api.Test;

import xfp.java.Debug;
import xfp.java.accumulators.BigFloatAccumulator1;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test summation algorithms. 
 * <p>
 * <pre>
 * mvn -q test -Dtest=xfp/java/test/accumulators/RationalAccumulatorTest > RAT.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-05-31
 */

public final class RationalAccumulatorTest {

  //--------------------------------------------------------------
  private static final int DIM = 256;
  private static final List<String> accumulators =
    List.of("xfp.java.accumulators.RationalAccumulator");
  
  @SuppressWarnings("static-method")
  @Test
  public final void tests () {
    Debug.DEBUG=false;
    //Debug.println();
    //Debug.println(Classes.className(this));
    Common.sumTests(
      Common.generators(DIM),
      Common.makeAccumulators(accumulators),
      BigFloatAccumulator1.make()); 
    Common.l2Tests(
      Common.generators(DIM),
      Common.makeAccumulators(accumulators),
      BigFloatAccumulator1.make()); 
    Common.dotTests(
      Common.generators(DIM),
      Common.makeAccumulators(accumulators),
      BigFloatAccumulator1.make());
    Debug.DEBUG=false;
    }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
