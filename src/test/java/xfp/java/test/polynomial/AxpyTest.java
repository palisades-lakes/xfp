package xfp.java.test.polynomial;

import org.junit.jupiter.api.Test;

import xfp.java.polynomial.BigFloatAxpy;
import xfp.java.polynomial.DoubleAxpy;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test desired properties of axpy calculators.
 * <p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/polynomial/AxpyTest test > AT.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-02
 */

public final class AxpyTest {

//  @SuppressWarnings({ "static-method" })
//  @Test
//  public final void bigFloatAxpy () { 
//    Common.daxpy(new BigFloatAxpy()); } 

  @SuppressWarnings({ "static-method" })
  @Test
  public final void doubleAxpy () { 
    Common.daxpy(new DoubleAxpy()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
