package xfp.java.test.polynomial;

import org.junit.jupiter.api.Test;

import xfp.java.polynomial.BigFloatAxpy;
import xfp.java.polynomial.DoubleAxpy;
import xfp.java.polynomial.RationalFloatAxpy;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test desired properties of axpy calculators.
 * <p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/polynomial/AxpyTest test > AT.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-07
 */

public final class AxpyTest {

//  @SuppressWarnings({ "static-method" })
//  @Test
//  public final void bigFloat () { 
//
//    final double a = 0x0.dc04c54d5843ap-1022;
//    final double x = -0x1.867594e2aa17dp192;
//    final double y = -0x1.d2c1a150855f7p292;
//    
//    final EFloat ae = EFloat.FromDouble(a);
//    final EFloat xe = EFloat.FromDouble(x);
//    final EFloat ye = EFloat.FromDouble(y);
//    final EFloat axe = ae.Multiply(xe); 
//    final double axed = axe.ToDouble();
//    final EFloat axpye = axe.Add(ye);
//    final double axpyed = axpye.ToDouble();
//    
//    final BigFloat ab = BigFloat.valueOf(a);
//    final BigFloat xb = BigFloat.valueOf(x);
//    final BigFloat yb = BigFloat.valueOf(y);
//    final BigFloat axb0 = ab.multiply(xb);
//    final double axb0d = axb0.doubleValue();
//    
//    final double axb1d = BigFloat.ZERO.addProduct(a,x).doubleValue();
//    
//    Assertions.assertEquals(axed,axb0d,
//      () -> "\n"
//    + "\na=" + Double.toHexString(a)
//    + "\nx=" + Double.toHexString(x)
//    + "\na*x=" + Double.toHexString(a*x)
//    + "\naxe=  " + Double.toHexString(axed)
//    + "\naxb=  " + Double.toHexString(axb0d)
//    + "\n"); 
//
//    Assertions.assertEquals(axed,axb1d,
//      () -> "\n"
//    + "\na=" + Double.toHexString(a)
//    + "\nx=" + Double.toHexString(x)
//    + "\na*x=" + Double.toHexString(a*x)
//    + "\naxe=  " + Double.toHexString(axed)
//    + "\naxb=  " + Double.toHexString(axb1d)
//    + "\n"); 
//
//    } 

  @SuppressWarnings({ "static-method" })
  @Test
  public final void bigFloatAxpy () { 
    Common.daxpy(new BigFloatAxpy()); } 

  @SuppressWarnings({ "static-method" })
  @Test
  public final void rationalFloatAxpy () { 
    Common.daxpy(new RationalFloatAxpy()); } 

  @SuppressWarnings({ "static-method" })
  @Test
  public final void doubleAxpy () { 
    Common.daxpy(new DoubleAxpy()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
