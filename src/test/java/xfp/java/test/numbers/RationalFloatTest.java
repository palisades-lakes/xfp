package xfp.java.test.numbers;

import org.junit.jupiter.api.Test;

import xfp.java.numbers.Numbers;
import xfp.java.numbers.RationalFloat;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test desired properties of RationalFloat. 
 * <p>
 * <pre>
 * mvn -Dtest=xfp/java/test/numbers/RationalFloatTest test > RationalFloatTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-22
 */

public final class RationalFloatTest {

  //--------------------------------------------------------------

  @SuppressWarnings({ "static-method" })
  @Test
  public final void testRounding () {

    Common.doubleRoundingTests(
      RationalFloat::valueOf,
      RationalFloat::valueOf,
      Numbers::doubleValue,
      (q0,q1) -> 
      ((RationalFloat) q0).subtract((RationalFloat) q1).abs(),
      Object::toString); 
    
    Common.floatRoundingTests(
      RationalFloat::valueOf,
      RationalFloat::valueOf,
      Numbers::floatValue,
      (q0,q1) -> 
      ((RationalFloat) q0).subtract((RationalFloat) q1).abs(),
      Object::toString); 
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
