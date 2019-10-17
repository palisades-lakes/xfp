package xfp.java.test.accumulators;

import org.junit.jupiter.api.Test;

import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test summation algorithms.
 * <p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/accumulators/SumTest test > Sum.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-08-08
 */

public final class SumTest {

  private static final int DIM = 517;//1024;

  @SuppressWarnings("static-method")
  @Test
  public final void zeroSum () {
    //Debug.DEBUG=true;
    Common.zeroSumTests(
      Common.zeroSumGenerators(DIM),
      Common.makeAccumulators(Common.accumulators()));
    //Debug.DEBUG=false;
  }

  @SuppressWarnings("static-method")
  @Test
  public final void overflowSum () {
    //Debug.DEBUG=false;
    //Debug.println();
    //Debug.println("overflow");
    Common.overflowTests(
      Common.makeAccumulators(Common.accumulators()));
    //Debug.DEBUG=false;
  }

  @SuppressWarnings("static-method")
  @Test
  public final void sum () {
    //Debug.DEBUG=false;
    //Debug.println();
    //Debug.println("sum");
    Common.sumTests(
      Common.generators(DIM),
      Common.makeAccumulators(Common.accumulators()),
      EFloatAccumulator.make());
    //Debug.DEBUG=false;
  }

  @SuppressWarnings("static-method")
  @Test
  public final void l2Distance () {
    //Debug.DEBUG=false;
    //Debug.println();
    //Debug.println("l2");
    Common.l2DistanceTests(
      Common.generators(DIM),
      Common.makeAccumulators(Common.accumulators()),
      EFloatAccumulator.make());
    //Debug.DEBUG=false;
  }

  @SuppressWarnings("static-method")
  @Test
  public final void l1Distance () {
    //Debug.DEBUG=false;
    //Debug.println();
    //Debug.println("l2");
    Common.l1DistanceTests(
      Common.generators(DIM),
      Common.makeAccumulators(Common.accumulators()),
      EFloatAccumulator.make());
    //Debug.DEBUG=false;
  }

  @SuppressWarnings("static-method")
  @Test
  public final void dot () {
    //Debug.DEBUG=false;
    //Debug.println();
    //Debug.println("dot");
    Common.dotTests(
      Common.generators(DIM),
      Common.makeAccumulators(Common.accumulators()),
      EFloatAccumulator.make());
    //Debug.DEBUG=false;
  }


  @SuppressWarnings("static-method")
  @Test
  public final void l2 () {
    //Debug.DEBUG=false;
    //Debug.println();
    //Debug.println("l2");
    Common.l2Tests(
      Common.generators(DIM),
      Common.makeAccumulators(Common.accumulators()),
      EFloatAccumulator.make());
    //Debug.DEBUG=false;
  }

  @SuppressWarnings("static-method")
  @Test
  public final void infiniteSum () {
    //Debug.DEBUG=false;
    //Debug.println();
    //Debug.println("infinite");
    Common.infinityTests(
      Common.makeAccumulators(Common.accumulators()));
    //Debug.DEBUG=false;
  }

  // TODO: choose expected behavior with non-finite input
  //@SuppressWarnings("static-method")
  //@Test
  //public final void nanSum () {
  //  //Debug.DEBUG=false;
  //  //Debug.println();
  //  //Debug.println("nonFInite");
  //  Common.nonFiniteTests(
  //    Common.makeAccumulators(Common.accumulators())); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
