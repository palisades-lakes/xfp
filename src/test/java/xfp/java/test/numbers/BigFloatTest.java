package xfp.java.test.numbers;

import java.util.function.BinaryOperator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import xfp.java.numbers.BigFloat;
import xfp.java.numbers.BigFloats;
import xfp.java.numbers.NaturalBEI;
import xfp.java.numbers.Numbers;
import xfp.java.prng.Generator;
import xfp.java.prng.PRNG;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test desired properties of BigFloat.
 * <p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/numbers/BigFloatTest test > BFT.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-06-24
 */

public final class BigFloatTest {

  private static final BinaryOperator<Comparable> dist =
    (q0,q1) -> ((BigFloat) q0).subtract((BigFloat) q1).abs();

    @SuppressWarnings({ "static-method" })
    @Test
    public final void testRounding () {
      //Debug.DEBUG=false;
      final BigFloat[] f =
      {
       BigFloat.valueOf(
         true,NaturalBEI.valueOf("232330747ceeab",0x10),-23),
       BigFloat.valueOf(
         false,NaturalBEI.valueOf("232330747ceeab",0x10),-23),
       BigFloat.valueOf(
         true,NaturalBEI.valueOf("2366052b8b801d",0x10),-22),
       BigFloat.valueOf(
         false,NaturalBEI.valueOf("21ab528c4dbc181",0x10),-26),
       BigFloat.valueOf(
         true,NaturalBEI.valueOf("8d9814ae2e0074",0x10),-25),
       BigFloat.valueOf(
         true,NaturalBEI.valueOf("2c94d1dcb123a56b9c1",0x10),-43), };
      for (final BigFloat fi : f) {
        Common.doubleRoundingTest(
          BigFloat::valueOf,Numbers::doubleValue,dist,
          Object::toString,fi);
        Common.floatRoundingTest(
          BigFloat::valueOf,Numbers::floatValue,dist,
          Object::toString,fi);  }
      //Debug.DEBUG=false;

      Common.doubleRoundingTests(
        null,BigFloat::valueOf,Numbers::doubleValue,dist,
        Object::toString);

      Common.floatRoundingTests(
        null,BigFloat::valueOf,Numbers::floatValue,dist,
        Object::toString);
      //Debug.DEBUG=false; 
      }

    private static final int TRYS = 1024*1024;
    @SuppressWarnings("static-method")
    public final void squareTest () {
      final Generator g =
        BigFloats.fromBigIntegerGenerator(
          PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
      for (int i=0;i<TRYS;i++) {
        final BigFloat x = (BigFloat) g.next();
        final BigFloat x2 = x.square();
        final BigFloat xx = x.multiply(x);
        Assertions.assertEquals(x2,xx); } }

    //--------------------------------------------------------------
}
//--------------------------------------------------------------
