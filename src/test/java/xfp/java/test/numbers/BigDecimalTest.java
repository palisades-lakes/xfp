package xfp.java.test.numbers;

//import org.junit.jupiter.api.Test;

//----------------------------------------------------------------
/** Tests showing why I use <code>BigFraction</code> for 
 * rationals rather than <c ode.BigDecimal</code>. 
 * <p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/sets/BigDecimalTest test > BigDecimalTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-18
 */

public final class BigDecimalTest {

  // BigDecimal.valueOf(double) returns 
  // BigDecimal.valueOf(Double.toString(double))
  // rather than returning a possibly existing instance
  // equivalent to new BigDecimal(double)

  // Does the rounding mode contaminate calculations invisibly? 
  
  // DoesBigDecimal.doubleValue() round correctly?

  //  @SuppressWarnings({ "static-method" })
  //  @Test
  //  public final void problems () {
  // }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
