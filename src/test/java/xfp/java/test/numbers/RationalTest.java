package xfp.java.test.numbers;

import java.util.function.BinaryOperator;

import org.junit.jupiter.api.Test;

import xfp.java.Debug;
import xfp.java.numbers.NaturalBEI;
import xfp.java.numbers.Numbers;
import xfp.java.numbers.Rational;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test desired properties of Rational.
 * <p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/numbers/RationalTest test > RationalTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-06-18
 */

public final class RationalTest {

  private static final BinaryOperator<Comparable> dist = (q0,q1) ->
  ((Rational) q0).subtract((Rational) q1).abs();

  @SuppressWarnings({ "static-method" })
  @Test
  public final void testRounding () {

    Debug.DEBUG=false;
    final Rational[] f =
    {
     Rational.valueOf(
       false,
       NaturalBEI.valueOf("2366052b8b801d",0x10),
       NaturalBEI.ONE.shiftUp(22)),
     Rational.valueOf(
       true,
       NaturalBEI.valueOf("2366052b8b801d",0x10),
       NaturalBEI.ONE.shiftUp(22)),
     Rational.valueOf(
       true,
       NaturalBEI.valueOf("232330747ceeab",0x10),
       NaturalBEI.ONE.shiftUp(23)),
     Rational.valueOf(
       false,
       NaturalBEI.valueOf("232330747ceeab",0x10),
       NaturalBEI.ONE.shiftUp(23)),
     Rational.valueOf(
       false,
       NaturalBEI.valueOf("21ab528c4dbc181",0x10),
       NaturalBEI.ONE.shiftUp(26)),
     Rational.valueOf(
       true,
       NaturalBEI.valueOf("8d9814ae2e0074",0x10),
       NaturalBEI.ONE.shiftUp(25)),
     Rational.valueOf(
       true,
       NaturalBEI.valueOf("2c94d1dcb123a56b9c1",0x10),
       NaturalBEI.ONE.shiftUp(43)), };
    for (final Rational fi : f) {
      Common.doubleRoundingTest(
        Rational::valueOf,
        Numbers::doubleValue,
        dist,
        Object::toString, fi);
      Common.floatRoundingTest(
        Rational::valueOf,
        Numbers::floatValue,
        dist,
        Object::toString, fi);  }
    Debug.DEBUG=false;

    Common.doubleRoundingTests(
      Rational::valueOf,
      Rational::valueOf,
      Numbers::doubleValue,
      dist,
      Object::toString);

    Common.floatRoundingTests(
      Rational::valueOf,
      Rational::valueOf,
      Numbers::floatValue,
      dist,
      Object::toString); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
