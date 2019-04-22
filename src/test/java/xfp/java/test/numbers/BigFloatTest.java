package xfp.java.test.numbers;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import xfp.java.Debug;
import xfp.java.numbers.BigFloat;
import xfp.java.numbers.Doubles;
import xfp.java.numbers.Numbers;
import xfp.java.prng.Generator;
import xfp.java.prng.PRNG;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test desired properties of BigFloat. 
 * <p>
 * <pre>
 * mvn -Dtest=xfp/java/test/numbers/BigFloatTest test > BigFloatTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-21
 */

public final class BigFloatTest {

  //--------------------------------------------------------------

  private static final void correctRounding (final BigFloat f) {
    Common.doubleRoundingTest(
      BigFloat::valueOf,
      Numbers::doubleValue,
      f); }

  //--------------------------------------------------------------

  private static final int TRYS = 1 * 1024;

  @SuppressWarnings({ "static-method" })
  @Test
  public final void subnormalDoubleRoundingTest () {
    Debug.DEBUG=false;
    final Generator g = 
      Doubles.subnormalGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final BigFloat f = BigFloat.valueOf(x);
      correctRounding(f); } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void finiteDoubleRoundingTest () {
    final Generator g = 
      Doubles.finiteGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final BigFloat f = BigFloat.valueOf(x);
      correctRounding(f); } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void gaussianDoubleRoundingTest () {
    final Generator g = 
      Doubles.gaussianGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"),
        0.0, Double.MAX_VALUE/1000.0);
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final BigFloat f = BigFloat.valueOf(x);
      correctRounding(f); } }
  
  @SuppressWarnings({ "static-method" })
  @Test
  public final void uniformDoubleRoundingTest () {
    final Generator g = 
      Doubles.uniformGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"),
        -Double.MAX_VALUE, Double.MAX_VALUE);
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final BigFloat f = BigFloat.valueOf(x);
      try { correctRounding(f); }
      catch (final Throwable t) {
        System.err.println("failed:" + Double.toHexString(x));
        throw t; }
    } }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void doubleRoundingTest () {
    Debug.DEBUG=false;
    final double x = 0x1.0p-1;
    final BigFloat f = BigFloat.valueOf(x);
    correctRounding(f); } 

  @SuppressWarnings({ "static-method" })
  @Test
  public final void longRoundingTest () {
    final BigFloat f = 
      BigFloat.valueOf(
        BigInteger.valueOf(0x789f09858446ad92L),
        0);
    correctRounding(f); }

  //  @SuppressWarnings({ "static-method" })
  //  @Test
  //  public final void fromBigIntegersRoundingTest () {
  //    final Generator gn = 
  //      Generators.bigIntegerGenerator(
  //        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
  //    final Generator ge = 
  //      Generators.intGenerator(
  //        PRNG.well44497b("seeds/Well44497b-2019-01-07.txt"));
  //    for (int i=0;i<TRYS;i++) {
  //      final BigInteger n = (BigInteger) gn.next();
  //      final int e = ge.nextInt();
  //      final BigFloat f = BigFloat.valueOf(n,e);
  //      assertTrue(correctRounding(f),
  //        () -> 
  //      "\nn= " + n.toString(0x10) 
  //      + "\nd= " + Integer.toHexString(e) 
  //      + "\n\nf= " + f.toString()
  //      + "\n\nxf= " + Double.toHexString(f.doubleValue())); } }

  //  @SuppressWarnings({ "static-method" })
  //  @Test
  //  public final void fromLongsRoundingTest () {
  //    final Generator g0 = 
  //      Generators.longGenerator(
  //        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
  //    final Generator g1 = 
  //      Generators.intGenerator(
  //        PRNG.well44497b("seeds/Well44497b-2019-01-07.txt"));
  //    for (int i=0;i<TRYS;i++) {
  //      final long n = g0.nextLong();
  //      final int e = g1.nextInt();
  //      final BigFloat f = BigFloat.valueOf(n,e);
  //      correctRounding(f); } }




  //--------------------------------------------------------------
}
//--------------------------------------------------------------
