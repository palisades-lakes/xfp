package xfp.java.test.numbers;

import java.math.BigInteger;
import java.util.function.BinaryOperator;

import org.junit.jupiter.api.Test;

import xfp.java.Debug;
import xfp.java.numbers.Numbers;
import xfp.java.numbers.RationalFloatBI;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test desired properties of RationalFloatBI.
 * <p>
 * <pre>
 * mvn -Dtest=xfp/java/test/numbers/RationalFloatBITest test > RationalFloatBITest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-05-23
 */

public final class RationalFloatBITest {

  private static final BinaryOperator<Comparable> dist = (q0,q1) ->
  ((RationalFloatBI) q0).subtract((RationalFloatBI) q1).abs();

  @SuppressWarnings({ "static-method" })
  @Test
  public final void testRounding () {
    Debug.DEBUG = false;
    final RationalFloatBI[] f =
    {
     RationalFloatBI.valueOf(
       new BigInteger("-2366052b8b801d",0x10),-22),
     RationalFloatBI.valueOf(
       new BigInteger("2366052b8b801d",0x10),-22),
     RationalFloatBI.valueOf(
       new BigInteger("232330747ceeab",0x10),-23),
     RationalFloatBI.valueOf(
       new BigInteger("-232330747ceeab",0x10),-23),
     RationalFloatBI.valueOf(
       new BigInteger("-21ab528c4dbc181",0x10),-26),
     RationalFloatBI.valueOf(
       new BigInteger("8d9814ae2e0074",0x10),-25),
     RationalFloatBI.valueOf(
       new BigInteger("2c94d1dcb123a56b9c1",0x10),-43), 
     };
    for (final RationalFloatBI fi : f) {
      Common.doubleRoundingTest(
        RationalFloatBI::valueOf, Numbers::doubleValue, dist,
        Object::toString, fi);
      Common.floatRoundingTest(
        RationalFloatBI::valueOf, Numbers::floatValue, dist,
        Object::toString, fi);  }
    Debug.DEBUG = false;

    Common.doubleRoundingTests(
      RationalFloatBI::valueOf, RationalFloatBI::valueOf,
      Numbers::doubleValue, dist, Object::toString);

    Common.floatRoundingTests(
      RationalFloatBI::valueOf, RationalFloatBI::valueOf,
      Numbers::floatValue, dist, Object::toString);
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
