package xfp.java.test.accumulators;

import static xfp.java.test.Common.accumulators;
import static xfp.java.test.Common.dotTests;
import static xfp.java.test.Common.generators;
import static xfp.java.test.Common.l2Tests;
import static xfp.java.test.Common.makeAccumulators;
import static xfp.java.test.Common.sumTests;
import static xfp.java.test.Common.zeroSumGenerators;

import org.junit.jupiter.api.Test;

import xfp.java.Debug;
import xfp.java.accumulators.RationalFloatAccumulator;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test summation algorithms. 
 * <p>
 * <pre>
 * mvn -Dtest=xfp/java/test/accumulators/SumTest test > SumTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-09
 */

public final class SumTest {

  private static final int DIM = 1 * 256 * 1024;

  @SuppressWarnings("static-method")
  @Test
  public final void nanSum () {
    Debug.DEBUG=false;
    Debug.println();
    Debug.println("infinite");
    Common.nonFiniteTests(
      Common.makeAccumulators(accumulators())); }

  @SuppressWarnings("static-method")
  @Test
  public final void infiniteSum () {
    Debug.DEBUG=false;
    Debug.println();
    Debug.println("infinite");
    Common.infinityTests(
      Common.makeAccumulators(accumulators())); }

  @SuppressWarnings("static-method")
  @Test
  public final void overflowSum () {
    Debug.DEBUG=false;
    Debug.println();
    Debug.println("overflow");
    Common.overflowTests(
      Common.makeAccumulators(accumulators())); }

  @SuppressWarnings("static-method")
  @Test
  public final void zeroSum () {
    Debug.DEBUG = false;
    Debug.println();
    Debug.println("sum");
    Common.zeroSumTests(
      zeroSumGenerators(DIM),
      makeAccumulators(accumulators())); }

  @SuppressWarnings("static-method")
  @Test
  public final void sum () {
    Debug.DEBUG = false;
    Debug.println();
    Debug.println("sum");
    sumTests(
      generators(DIM),
      makeAccumulators(accumulators()),
      RationalFloatAccumulator.make()); }

  @SuppressWarnings("static-method")
  @Test
  public final void l2 () {
    Debug.DEBUG = false;
    Debug.println();
    Debug.println("l2");
    l2Tests(
      generators(DIM),
      makeAccumulators(accumulators()),
      RationalFloatAccumulator.make()); }

  @SuppressWarnings("static-method")
  @Test
  public final void dot () {
    Debug.DEBUG = false;
    Debug.println();
    Debug.println("dot");
    dotTests(
      generators(DIM),
      makeAccumulators(accumulators()),
      RationalFloatAccumulator.make()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
