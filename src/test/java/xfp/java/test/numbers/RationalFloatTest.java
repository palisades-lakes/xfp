package xfp.java.test.numbers;

import java.util.function.BinaryOperator;

import org.junit.jupiter.api.Test;

import xfp.java.numbers.Natural;
import xfp.java.numbers.Numbers;
import xfp.java.numbers.RationalFloat;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test desired properties of RationalFloat.
 * <p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/numbers/RationalFloatTest test > RationalFloatTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-08-30
 */

public final class RationalFloatTest {

  private static final BinaryOperator<Comparable> dist = (q0,q1) ->
  ((RationalFloat) q0).subtract((RationalFloat) q1).abs();

  @SuppressWarnings({ "static-method" })
  @Test
  public final void testRounding () {
    //Debug.DEBUG=false;
    final RationalFloat[] f =
    {
     RationalFloat.valueOf(
       false,Natural.valueOf("2366052b8b801d",0x10),-22),
     RationalFloat.valueOf(
       true,Natural.valueOf("2366052b8b801d",0x10),-22),
     RationalFloat.valueOf(
       true,Natural.valueOf("232330747ceeab",0x10),-23),
     RationalFloat.valueOf(
       false,Natural.valueOf("232330747ceeab",0x10),-23),
     RationalFloat.valueOf(
       false,Natural.valueOf("21ab528c4dbc181",0x10),-26),
     RationalFloat.valueOf(
       true,Natural.valueOf("8d9814ae2e0074",0x10),-25),
     RationalFloat.valueOf(
       true,Natural.valueOf("2c94d1dcb123a56b9c1",0x10),-43), };
    for (final RationalFloat fi : f) {
      //Debug.println(fi.toString());
      Common.doubleRoundingTest(
        RationalFloat::valueOf, Numbers::doubleValue, dist,
        Object::toString, fi);
      Common.floatRoundingTest(
        RationalFloat::valueOf, Numbers::floatValue, dist,
        Object::toString, fi);  }
    //Debug.DEBUG=false;

    Common.doubleRoundingTests(
      RationalFloat::valueOf, RationalFloat::valueOf,
      Numbers::doubleValue, dist, Object::toString);

    Common.floatRoundingTests(
      RationalFloat::valueOf, RationalFloat::valueOf,
      Numbers::floatValue, dist, Object::toString);
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
