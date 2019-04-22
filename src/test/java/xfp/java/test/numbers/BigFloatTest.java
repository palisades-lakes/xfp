package xfp.java.test.numbers;

import org.junit.jupiter.api.Test;

import xfp.java.numbers.BigFloat;
import xfp.java.numbers.Numbers;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test desired properties of BigFloat. 
 * <p>
 * <pre>
 * mvn -Dtest=xfp/java/test/numbers/BigFloatTest test > BigFloatTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-22
 */

public final class BigFloatTest {

  //--------------------------------------------------------------

  @SuppressWarnings({ "static-method" })
  @Test
  public final void testRounding () {
    Common.doubleRoundingTests(
      null,
      BigFloat::valueOf,
      Numbers::doubleValue,
      (q0,q1) -> ((BigFloat) q0).subtract((BigFloat) q1).abs(),
      Object::toString); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
