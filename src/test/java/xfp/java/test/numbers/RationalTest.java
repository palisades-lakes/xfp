package xfp.java.test.numbers;

import xfp.java.numbers.BigInteger;
import java.util.function.BinaryOperator;

import org.junit.jupiter.api.Test;

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
 * @version 2019-04-29
 */

public final class RationalTest {

  private static final BinaryOperator<Comparable> dist = (q0,q1) -> 
  ((Rational) q0).subtract((Rational) q1).abs();

  @SuppressWarnings({ "static-method" })
  @Test
  public final void testRounding () {

    //Debug.DEBUG = true;
    final Rational[] f = 
    { 
     Rational.valueOf(
       new BigInteger("-2366052b8b801d",0x10),
       BigInteger.ONE.shiftLeft(22)),
     Rational.valueOf(
       new BigInteger("2366052b8b801d",0x10),
       BigInteger.ONE.shiftLeft(22)),
     Rational.valueOf(
       new BigInteger("232330747ceeab",0x10),
       BigInteger.ONE.shiftLeft(23)),
     Rational.valueOf(
       new BigInteger("-232330747ceeab",0x10),
       BigInteger.ONE.shiftLeft(23)),
     Rational.valueOf(
       new BigInteger("-21ab528c4dbc181",0x10),
       BigInteger.ONE.shiftLeft(26)),
     Rational.valueOf(
       new BigInteger("8d9814ae2e0074",0x10),
       BigInteger.ONE.shiftLeft(25)),
     Rational.valueOf(
       new BigInteger("2c94d1dcb123a56b9c1",0x10),
       BigInteger.ONE.shiftLeft(43)), };
    for (final Rational fi : f) {
      Common.doubleRoundingTest(
        Rational::valueOf, Numbers::doubleValue, dist,
        Object::toString, fi); 
      Common.floatRoundingTest(
        Rational::valueOf, Numbers::floatValue, dist,
        Object::toString, fi);  }
    //Debug.DEBUG = false;
 
    Common.doubleRoundingTests(
      Rational::valueOf,Rational::valueOf,Numbers::doubleValue,
      dist, Object::toString); 

    Common.floatRoundingTests(
      Rational::valueOf, Rational::valueOf, Numbers::floatValue,
      dist, Object::toString); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
