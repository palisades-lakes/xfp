package xfp.java.test.numbers;

import java.util.function.BinaryOperator;

import org.junit.jupiter.api.Test;

import xfp.java.Debug;
import xfp.java.numbers.Numbers;
import xfp.java.numbers.RationalFloat0;
import xfp.java.numbers.UnNatural0;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test desired properties of RationalFloat0.
 * <p>
 * <pre>
 * mvn -Dtest=xfp/java/test/numbers/RationalFloat0Test test > RationalFloat0Test.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-05-23
 */

public final class RationalFloat0Test {

  private static final BinaryOperator<Comparable> dist = (q0,q1) ->
  ((RationalFloat0) q0).subtract((RationalFloat0) q1).abs();

  @SuppressWarnings({ "static-method" })
  @Test
  public final void testRounding () {
    Debug.DEBUG = false;
    final RationalFloat0[] f =
    {
     RationalFloat0.valueOf(
       false,UnNatural0.valueOf("2366052b8b801d",0x10),-22),
     RationalFloat0.valueOf(
       true,UnNatural0.valueOf("2366052b8b801d",0x10),-22),
     RationalFloat0.valueOf(
       true,UnNatural0.valueOf("232330747ceeab",0x10),-23),
     RationalFloat0.valueOf(
       false,UnNatural0.valueOf("232330747ceeab",0x10),-23),
     RationalFloat0.valueOf(
       false,UnNatural0.valueOf("21ab528c4dbc181",0x10),-26),
     RationalFloat0.valueOf(
       true,UnNatural0.valueOf("8d9814ae2e0074",0x10),-25),
     RationalFloat0.valueOf(
       true,UnNatural0.valueOf("2c94d1dcb123a56b9c1",0x10),-43), };
    for (final RationalFloat0 fi : f) {
      Debug.println(fi.toString());
      Common.doubleRoundingTest(
        RationalFloat0::valueOf, Numbers::doubleValue, dist,
        Object::toString, fi);
      Common.floatRoundingTest(
        RationalFloat0::valueOf, Numbers::floatValue, dist,
        Object::toString, fi);  }
    Debug.DEBUG = false;

    Common.doubleRoundingTests(
      RationalFloat0::valueOf, RationalFloat0::valueOf,
      Numbers::doubleValue, dist, Object::toString);

    Common.floatRoundingTests(
      RationalFloat0::valueOf, RationalFloat0::valueOf,
      Numbers::floatValue, dist, Object::toString);
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
