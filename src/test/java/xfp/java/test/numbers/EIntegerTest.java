package xfp.java.test.numbers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import com.upokecenter.numbers.EInteger;

import xfp.java.numbers.ERationals;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;
import xfp.java.prng.PRNG;

//----------------------------------------------------------------
/** Test desired properties of EInteger. 
 * <p>
 * <pre>
 * mvn -Dtest=xfp/java/test/numbers/EIntegerTest test > EIntegerTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-05
 */

public final class EIntegerTest {

  private static final int TRYS = 32;
  //--------------------------------------------------------------
  /** Check conversion from BigInteger to EInteger to BigInteger */
  
  @SuppressWarnings({ "static-method" })
  @Test
  public final void fromBigIntegerTest () {
    final Generator g = 
      Generators.bigIntegerGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      // some longs will not be exactly representable as doubles
      final BigInteger b0 = (BigInteger) g.next();
      final EInteger ei = EInteger.FromBytes(b0.toByteArray(),false);
      //System.out.println(i);
      //System.out.println(b0);
      //System.out.println(ei);
      final BigInteger b1 = new BigInteger(ei.ToBytes(false));
      assertTrue(b0.equals(b1)); } }

  /** Check conversion from EInteger to BigInteger to EInteger */
  
  @SuppressWarnings({ "static-method" })
  @Test
  public final void fromEIntegerTest () {
    final Generator g = 
      ERationals.eIntegerGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      // some longs will not be exactly representable as doubles
      final EInteger e0 = (EInteger) g.next();
      final BigInteger b = new BigInteger(e0.ToBytes(false));
      final EInteger e1 = EInteger.FromBytes(b.toByteArray(),false);
      //System.out.println(i);
      //System.out.println(e0);
      //System.out.println(b);
      assertTrue(e0.equals(e1)); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
