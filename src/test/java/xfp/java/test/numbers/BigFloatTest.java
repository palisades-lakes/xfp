package xfp.java.test.numbers;

import java.math.BigInteger;
import java.util.function.BinaryOperator;

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
 * @version 2019-04-29
 */

public final class BigFloatTest {

  private static final BinaryOperator<Comparable> dist =
    (q0,q1) -> ((BigFloat) q0).subtract((BigFloat) q1).abs();
    
  @SuppressWarnings({ "static-method" })
  @Test
  public final void testRounding () {
    //Debug.DEBUG = true;
    final BigFloat[] f = 
    { 
      BigFloat.valueOf(
        new BigInteger("232330747ceeab",0x10),-23),
      BigFloat.valueOf(
        new BigInteger("-232330747ceeab",0x10),-23),
     BigFloat.valueOf(
       new BigInteger("2366052b8b801d",0x10),-22),
     BigFloat.valueOf(
       new BigInteger("-21ab528c4dbc181",0x10),-26),
     BigFloat.valueOf(
       new BigInteger("8d9814ae2e0074",0x10),-25),
     BigFloat.valueOf(
       new BigInteger("2c94d1dcb123a56b9c1",0x10),-43), };
    for (final BigFloat fi : f) {
      Common.doubleRoundingTest(
        BigFloat::valueOf,Numbers::doubleValue,dist,
        Object::toString,fi); 
      Common.floatRoundingTest(
        BigFloat::valueOf,Numbers::floatValue,dist,
        Object::toString,fi);  }
    //Debug.DEBUG = false;

    Common.doubleRoundingTests(
      null,BigFloat::valueOf,Numbers::doubleValue,dist,
      Object::toString); 

    Common.floatRoundingTests(
      null,BigFloat::valueOf,Numbers::floatValue,dist,
      Object::toString); 
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
