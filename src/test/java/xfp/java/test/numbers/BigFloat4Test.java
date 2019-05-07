package xfp.java.test.numbers;

import java.util.function.BinaryOperator;

import org.junit.jupiter.api.Test;

import xfp.java.numbers.BigFloat4;
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
 * @version 2019-05-06
 */

public final class BigFloat4Test {

  private static final BinaryOperator<Comparable> dist =
    (q0,q1) -> ((BigFloat4) q0).subtract((BigFloat4) q1).abs();
    
  @SuppressWarnings({ "static-method" })
  @Test
  public final void testRounding () {
    //Debug.DEBUG = true;
    final BigFloat4[] f = 
    { 
      BigFloat4.valueOf(
        new BigInteger("232330747ceeab",0x10),-23),
      BigFloat4.valueOf(
        new BigInteger("-232330747ceeab",0x10),-23),
     BigFloat4.valueOf(
       new BigInteger("2366052b8b801d",0x10),-22),
     BigFloat4.valueOf(
       new BigInteger("-21ab528c4dbc181",0x10),-26),
     BigFloat4.valueOf(
       new BigInteger("8d9814ae2e0074",0x10),-25),
     BigFloat4.valueOf(
       new BigInteger("2c94d1dcb123a56b9c1",0x10),-43), };
    for (final BigFloat4 fi : f) {
      Common.doubleRoundingTest(
        BigFloat4::valueOf,Numbers::doubleValue,dist,
        Object::toString,fi); 
      Common.floatRoundingTest(
        BigFloat4::valueOf,Numbers::floatValue,dist,
        Object::toString,fi);  }
    //Debug.DEBUG = false;

    Common.doubleRoundingTests(
      null,BigFloat4::valueOf,Numbers::doubleValue,dist,
      Object::toString); 

    Common.floatRoundingTests(
      null,BigFloat4::valueOf,Numbers::floatValue,dist,
      Object::toString); 
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
