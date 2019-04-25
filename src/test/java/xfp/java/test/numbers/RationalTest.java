package xfp.java.test.numbers;

import java.math.BigInteger;

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
 * @version 2019-04-24
 */

public final class RationalTest {

  @SuppressWarnings({ "static-method" })
  @Test
  public final void testRounding () {

    //Debug.DEBUG = true;
    Common.doubleRoundingTest(
      Rational::valueOf,
      Numbers::doubleValue,
      (q0,q1) -> ((Rational) q0).subtract((Rational) q1).abs(),
      Object::toString,
      Rational.valueOf(
        new BigInteger("2c94d1dcb123a56b9c1",0x10),
        BigInteger.ONE.shiftLeft(43))); 
    Common.floatRoundingTest(
      Rational::valueOf,
      Numbers::floatValue,
      (q0,q1) -> ((Rational) q0).subtract((Rational) q1).abs(),
      Object::toString,
      Rational.valueOf(
        new BigInteger("2c94d1dcb123a56b9c1",0x10),
        BigInteger.ONE.shiftLeft(43))); 
    //Debug.DEBUG = false;

    Common.doubleRoundingTests(
      Rational::valueOf,
      Rational::valueOf,
      Numbers::doubleValue,
      (q0,q1) -> ((Rational) q0).subtract((Rational) q1).abs(),
      Object::toString); 

    Common.floatRoundingTests(
      Rational::valueOf,
      Rational::valueOf,
      Numbers::floatValue,
      (q0,q1) -> ((Rational) q0).subtract((Rational) q1).abs(),
      Object::toString); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
