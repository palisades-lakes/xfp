package xfp.java.test.numbers;

import org.junit.jupiter.api.Test;

import xfp.java.numbers.UnNatural;
import xfp.java.numbers.UnNatural0;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test desired properties of natural number implementations.
 * <p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/numbers/NaturalTest test > NaturalTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-29
 */

public final class NaturalTest {

  @SuppressWarnings({ "static-method" })
  @Test
  public final void unNatural () {
    Common.naturalTest(
      (z) -> UnNatural.valueOf(z),
      (z) -> ((UnNatural) z).bigIntegerValue()); }

//  @SuppressWarnings({ "static-method" })
//  @Test
//  public final void unNatural0 () {
//    Common.naturalTest(
//      (z) -> UnNatural0.valueOf(z),
//      (z) -> ((UnNatural0) z).bigIntegerValue()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
