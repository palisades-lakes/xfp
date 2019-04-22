package xfp.java.test.numbers;

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
 * @version 2019-04-22
 */

public final class RationalTest {

  @SuppressWarnings({ "static-method" })
  @Test
  public final void testRounding () {
    Common.doubleRoundingTests(
      Rational::valueOf,
      Rational::valueOf,
      Numbers::doubleValue,
      (q0,q1) -> ((Rational) q0).subtract((Rational) q1).abs(),
      Object::toString); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
