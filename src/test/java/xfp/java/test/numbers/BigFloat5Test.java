package xfp.java.test.numbers;

import java.util.function.BinaryOperator;

import org.junit.jupiter.api.Test;

import xfp.java.numbers.BigFloat5;
import xfp.java.numbers.BigInteger;
import xfp.java.numbers.Numbers;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test desired properties of BigFloat. 
 * <p>
 * <pre>
 * mvn -Dtest=xfp/java/test/numbers/Test test > Test.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-05-07
 */

public final class BigFloat5Test {

  private static final BinaryOperator<Comparable> dist =
    (q0,q1) -> ((BigFloat5) q0).subtract((BigFloat5) q1).abs();
    
  @SuppressWarnings({ "static-method" })
  @Test
  public final void testRounding () {
    //Debug.DEBUG = true;
    final BigFloat5[] f = 
    { 
      BigFloat5.valueOf(
        new BigInteger("232330747ceeab",0x10),-23),
      BigFloat5.valueOf(
        new BigInteger("-232330747ceeab",0x10),-23),
     BigFloat5.valueOf(
       new BigInteger("2366052b8b801d",0x10),-22),
     BigFloat5.valueOf(
       new BigInteger("-21ab528c4dbc181",0x10),-26),
     BigFloat5.valueOf(
       new BigInteger("8d9814ae2e0074",0x10),-25),
     BigFloat5.valueOf(
       new BigInteger("2c94d1dcb123a56b9c1",0x10),-43), };
    for (final BigFloat5 fi : f) {
      Common.doubleRoundingTest(
        BigFloat5::valueOf,Numbers::doubleValue,dist,
        Object::toString,fi); 
      Common.floatRoundingTest(
        BigFloat5::valueOf,Numbers::floatValue,dist,
        Object::toString,fi);  }
    //Debug.DEBUG = false;

    Common.doubleRoundingTests(
      null,BigFloat5::valueOf,Numbers::doubleValue,dist,
      Object::toString); 

    Common.floatRoundingTests(
      null,BigFloat5::valueOf,Numbers::floatValue,dist,
      Object::toString); 
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
