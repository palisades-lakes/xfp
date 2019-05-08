package xfp.java.test.accumulators;

import org.junit.jupiter.api.Test;

import xfp.java.Debug;
import xfp.java.accumulators.BigFloatAccumulator;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test summation algorithms. 
 * <p>
 * <pre>
 * mvn -Dtest=xfp/java/test/accumulators/SumTest test > SumTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-05-07
 */

public final class SumTest {

  private static final int DIM = 16 * 1024;

  @SuppressWarnings("static-method")
  @Test
  public final void overflowSum () {
    Debug.DEBUG=false;
    Debug.println();
    Debug.println("overflow");
    Common.overflowTests(
      Common.makeAccumulators(Common.accumulators())); }

  @SuppressWarnings("static-method")
  @Test
  public final void infiniteSum () {
    Debug.DEBUG=false;
    Debug.println();
    Debug.println("infinite");
    Common.infinityTests(
      Common.makeAccumulators(Common.accumulators())); }

  @SuppressWarnings("static-method")
  @Test
  public final void zeroSum () {
    Debug.DEBUG = false;
    Debug.println();
    Debug.println("sum");
    Common.zeroSumTests(
      Common.zeroSumGenerators(DIM),
      Common.makeAccumulators(Common.accumulators())); }

  @SuppressWarnings("static-method")
  @Test
  public final void sum () {
    Debug.DEBUG = false;
    Debug.println();
    Debug.println("sum");
    Common.sumTests(
      Common.generators(DIM),
      Common.makeAccumulators(Common.accumulators()),
      BigFloatAccumulator.make()); }

  @SuppressWarnings("static-method")
  @Test
  public final void l2 () {
    Debug.DEBUG = false;
    Debug.println();
    Debug.println("l2");
    Common.l2Tests(
      Common.generators(DIM),
      Common.makeAccumulators(Common.accumulators()),
      BigFloatAccumulator.make()); }

  @SuppressWarnings("static-method")
  @Test
  public final void dot () {
    Debug.DEBUG = false;
    Debug.println();
    Debug.println("dot");
    Common.dotTests(
      Common.generators(DIM),
      Common.makeAccumulators(Common.accumulators()),
      BigFloatAccumulator.make()); }

  // TODO: choose expected behavior with non-finite input
//@SuppressWarnings("static-method")
//@Test
//public final void nanSum () {
//  Debug.DEBUG=false;
//  Debug.println();
//  Debug.println("nonFInite");
//  Common.nonFiniteTests(
//    Common.makeAccumulators(Common.accumulators())); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
