package xfp.java.test.polynomial;

import org.junit.jupiter.api.Test;

import xfp.java.polynomial.BigFloatCubic;
import xfp.java.polynomial.RationalFloatCubic;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test desired properties of axpy calculators.
 * <p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/polynomial/CubicTest test > CT.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-07
 */

public final class CubicTest {

  @SuppressWarnings({ "static-method" })
  @Test
  public final void rationalFloatCubic () { 
    Common.cubic(RationalFloatCubic.class); } 

  @SuppressWarnings({ "static-method" })
  @Test
  public final void bigFloatCubic () { 
    Common.cubic(BigFloatCubic.class); } 

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
