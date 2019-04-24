package xfp.java.test.numbers;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import xfp.java.numbers.BigFloat;
import xfp.java.numbers.Numbers;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test desired properties of BigFloat. 
 * <p>
 * <pre>
 * mvn -Dtest=xfp/java/test/numbers/BigFloatTest test > BigFloatTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-23
 */

public final class BigFloatTest {

  //--------------------------------------------------------------

  @SuppressWarnings({ "static-method" })
  @Test
  public final void testRounding () {

    //Debug.DEBUG = true;
    Common.doubleRoundingTest(
      BigFloat::valueOf,
      Numbers::doubleValue,
      (q0,q1) -> ((BigFloat) q0).subtract((BigFloat) q1).abs(),
      Object::toString,
      BigFloat.valueOf(
        new BigInteger("2c94d1dcb123a56b9c1",0x10),
        -43)); 
    //Debug.DEBUG = false;

    Common.doubleRoundingTests(
      null,
      BigFloat::valueOf,
      Numbers::doubleValue,
      (q0,q1) -> ((BigFloat) q0).subtract((BigFloat) q1).abs(),
      Object::toString); 

    Common.floatRoundingTests(
      null,
      BigFloat::valueOf,
      Numbers::floatValue,
      (q0,q1) -> ((BigFloat) q0).subtract((BigFloat) q1).abs(),
      Object::toString); 
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
