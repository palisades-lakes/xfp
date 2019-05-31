package xfp.java.test.numbers;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import xfp.java.Debug;
import xfp.java.numbers.Bei;

//----------------------------------------------------------------
/**<p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/numbers/BeiTest test > BeiTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-05-30
 */

public final class BeiTest {

//  @SuppressWarnings({ "static-method" })
//  @Test
//  public final void testSubtract() {
//    Debug.DEBUG=false;
//
//    final int[] diff = new int[] { 101, };
//    final int[] a0 = new int[] {0xc9,};
//    final int[] d0 =  Bei.subtract(a0, Bei.valueOf(0x19L, 2));
//    Assertions.assertArrayEquals(
//      diff, d0,
//      "\ndiff=" + Arrays.toString(diff)
//      + "\nd0=" + Arrays.toString(d0));
//
//    Assertions.assertArrayEquals(
//      new int[] { 101, },
//      Bei.subtract(new int[] {0xc9,}, 0x19L, 2));
//
//    Assertions.assertArrayEquals(
//      new int[] { 101, },
//      Bei.subtract(new int[] {201,}, 25L, 2));
//
//    Debug.DEBUG=false; }

  // failing, need to handle long arg w < 52 bits
  //  @SuppressWarnings({ "static-method" })
  //  @Test
  //  public final void testCompare () {
  //    Debug.DEBUG=false;
  //    
  //    Assertions.assertEquals(
  //      1,
  //      Bei.compare(new int[] {0xc9,}, Bei.valueOf(0x19L, 2)));
  //    
  //    Assertions.assertEquals(
  //      1,
  //      Bei.compare(new int[] {0xc9,}, 0x19L, 2));
  //    
  //    Assertions.assertEquals(
  //      1,
  //      Bei.compare(new int[] {201,}, 25L, 2));
  //    
  //    Debug.DEBUG=false;
  //  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
