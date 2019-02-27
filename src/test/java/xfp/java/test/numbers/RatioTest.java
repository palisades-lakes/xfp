package xfp.java.test.numbers;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import clojure.lang.Ratio;

//----------------------------------------------------------------
/** Tests showing why I use <code>BigFraction</code> for 
 * rationals rather than <code>clojure.lang.Ratio</code>. 
 * <p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/sets/RatioTest test > RatioTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-18
 */

public final class RatioTest {

  // (1) equals is wrong, unless always in reduced form?
  // not true if you call the public constructor
  //
  // (2) need clojure tests to demonstrate surprising coercions
  
  @SuppressWarnings({ "static-method" })
  @Test
  public final void equivalenceFailure () {
    final Ratio q0 = new Ratio(BigInteger.ONE,BigInteger.ONE);
    final Ratio q1 = new Ratio(BigInteger.TWO,BigInteger.TWO);
    // WRONG: this should be true, but clojure Ratio is broken.
    assertFalse(q0.equals(q1)); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
