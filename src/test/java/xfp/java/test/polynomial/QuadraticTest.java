package xfp.java.test.polynomial;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.upokecenter.numbers.EFloat;

import xfp.java.numbers.BigFloat;
import xfp.java.polynomial.BigFloatQuadratic;
import xfp.java.polynomial.EFloatQuadratic;
import xfp.java.polynomial.RationalFloatQuadratic;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test desired properties of axpy calculators.
 * <p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/polynomial/QuadraticTest test > QT.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-07
 */

public final class QuadraticTest {

  @SuppressWarnings({ "static-method" })
  @Test
  public final void infinity () { 
  final double a0=-0x1.11c90a77bf955p10;
  final double a1=0x1.3641a568bd96fp182;
    final double a2=-0x1.091e534068ed3p328;
    final double x=-0x1.ec385ac66d78ap454;
    final EFloatQuadratic e = EFloatQuadratic.make(a0,a1,a2);
    final BigFloatQuadratic q = BigFloatQuadratic.make(a0,a1,a2);
    
    final EFloat ef = e.value(x);
    final BigFloat qf = q.value(x);
//    System.out.println(!ef.isNegative());
//    System.out.println(ef.getUnsignedMantissa().ToRadixString(16));
//    System.out.println(ef.getExponent());
//    System.out.println();
//    System.out.println(qf.nonNegative());
//    System.out.println(qf.significand());
//    System.out.println(qf.exponent());
    Assertions.assertEquals(ef.ToDouble(),qf.doubleValue());
  }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void rationalFloatQuadratic () { 
    Common.quadratic(RationalFloatQuadratic.class); } 

  @SuppressWarnings({ "static-method" })
  @Test
  public final void bigFloatQuadratic () { 
    Common.quadratic(BigFloatQuadratic.class); } 

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
