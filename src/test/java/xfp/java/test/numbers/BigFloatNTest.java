package xfp.java.test.numbers;

import java.util.function.BinaryOperator;

import org.junit.jupiter.api.Test;

import xfp.java.numbers.BigFloatN;
import xfp.java.numbers.Natural;
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
 * @version 2019-05-09
 */

public final class BigFloatNTest {

  private static final BinaryOperator<Comparable> dist =
    (q0,q1) -> ((BigFloatN) q0).subtract((BigFloatN) q1).abs();
    
  @SuppressWarnings({ "static-method" })
  @Test
  public final void testRounding () {
    //Debug.DEBUG = true;
    final BigFloatN[] f = 
    { 
      BigFloatN.valueOf(
        true,Natural.valueOf("232330747ceeab",0x10),-23),
      BigFloatN.valueOf(
        false,Natural.valueOf("232330747ceeab",0x10),-23),
     BigFloatN.valueOf(
       true,Natural.valueOf("2366052b8b801d",0x10),-22),
     BigFloatN.valueOf(
       false,Natural.valueOf("21ab528c4dbc181",0x10),-26),
     BigFloatN.valueOf(
       true,Natural.valueOf("8d9814ae2e0074",0x10),-25),
     BigFloatN.valueOf(
       true,Natural.valueOf("2c94d1dcb123a56b9c1",0x10),-43), };
    for (final BigFloatN fi : f) {
      Common.doubleRoundingTest(
        BigFloatN::valueOf,Numbers::doubleValue,dist,
        Object::toString,fi); 
      Common.floatRoundingTest(
        BigFloatN::valueOf,Numbers::floatValue,dist,
        Object::toString,fi);  }
    //Debug.DEBUG = false;

    Common.doubleRoundingTests(
      null,BigFloatN::valueOf,Numbers::doubleValue,dist,
      Object::toString); 

    Common.floatRoundingTests(
      null,BigFloatN::valueOf,Numbers::floatValue,dist,
      Object::toString); 
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
