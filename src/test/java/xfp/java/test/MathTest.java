package xfp.java.test;


//----------------------------------------------------------------
/** Test consistency between Java and C.
 * <p>
 * <pre>
 * mvn -Dtest=palisades/lakes/cghzj/test/MathTest test > Math.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2018-05-31
 */

strictfp
public final class MathTest {

  //--------------------------------------------------------------
  //  private static final long hex (final double x) {
  //    return Double.doubleToLongBits(x); }

  //  @SuppressWarnings({ "static-method", "boxing" })
  //  @Test
  //  public final void test () {
  //
  ////    final int i = 25;
  ////    final double t = i+1;
  ////    System.out.printf("t=\n%23.15E %16X\n",t,hex(t));
  ////    final double sms = StrictMath.sqrt(t);
  ////    System.out.printf("StrictMath.sqrt(t)=\n%23.15E %16X\n",sms,hex(sms));
  ////    final double ms = Math.sqrt(t);
  ////    System.out.printf("Math.sqrt(t)=\n%23.15E %16X\n",ms,hex(ms));
  ////    final double smex = StrictMath.exp(1.0);
  ////    System.out.printf("StrictMath.exp(1.0)=\n%23.15E %16X\n",smex,hex(smex));
  ////    final double mex = Math.exp(1.0);
  ////    System.out.printf("Math.exp(1.0)=\n%23.15E %16X\n",mex,hex(mex));
  ////
  ////    final double delta =
  ////      (2.23606797749979000 - 2.23606797749978981);
  ////    System.out.printf("delta=%23.15E\n",delta);
  ////    System.out.printf("ulp=%23.15E\n",Math.ulp(ms));
  //
  //    final double x =
  //      Double.longBitsToDouble(0x3FE9C311A7AAD9EAL);
  //    System.out.printf("%26.20E %26.20E\n",x,Math.sqrt(x));
  //    System.out.printf("%16X %16X\n",hex(x),hex(Math.sqrt(x)));
  //    System.out.printf("%26.20E %26.20E\n",x,Math.exp(x));
  //    System.out.printf("%16X %16X\n",hex(x),hex(Math.exp(x)));
  //    System.out.printf("%26.20E %26.20E\n",x,Math.pow(Math.E,x));
  //    System.out.printf("%16X %16X\n",hex(x),hex(Math.pow(Math.E,x)));
  //    System.out.printf("%26.20E %26.20E\n",x,StrictMath.exp(x));
  //    System.out.printf("%16X %16X\n",hex(x),hex(StrictMath.exp(x)));
  //  }

  //--------------------------------------------------------------
}
