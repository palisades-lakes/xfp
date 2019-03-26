package xfp.java.scripts;

import static xfp.java.numbers.Numbers.description;

import java.math.BigInteger;

import xfp.java.Debug;
import xfp.java.numbers.Doubles;
import xfp.java.numbers.Rational;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;
import xfp.java.prng.PRNG;

/** BigInteger pair: divide and round to double.
 * 
 * <pre>
 * j --source 11 src/scripts/java/xfp/java/scripts/DivideDouble.java > divide.txt 2>&1
 * </pre>
 * Profiling:
 * <pre>
 * jy --source 11 src/scripts/java/xfp/java/scripts/DivideDouble.java
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-25
 */

@SuppressWarnings("unchecked")
public final class DivideDouble {

  //--------------------------------------------------------------

  public static final double roundingTest (final BigInteger n,
                                           final BigInteger d) {
    final double ze = Debug.ToDouble(n,d);
    Debug.println(Double.toHexString(ze) + " :E");
    try {
      final double z = Rational.valueOf(n,d).doubleValue();
      Debug.println(Double.toHexString(z) + " :D");
      assert ze == z : 
        "\n" 
        + Double.toHexString(ze) + " :E\n"
        + Double.toHexString(z); 
      return z; } 
    catch (final Throwable t) {
      System.err.println("failed on:");
      System.err.println(description("n",n)); 
      System.err.println(description("d",d)); 
      throw t; } }

  public static final double roundingTest (final long n,
                                           final long d) {
    final double z = roundingTest(
      BigInteger.valueOf(n),
      BigInteger.valueOf(d));
    return z; } 

  public static final double roundingTest (final double x) {
    //    Debug.println();
    //    Debug.println("roundingTest(" 
    //      + Double.toHexString(x) + ")");
    //    Debug.println("signBit=" + signBit(x));
    //    Debug.println("significand=" 
    //      + Long.toHexString(fullSignificand(x)));
    //    Debug.println("significand=" 
    //      + Long.toBinaryString(fullSignificand(x)));
    //    Debug.println("significand=" 
    //      + Long.toBinaryString(SIGNIFICAND_MASK));
    //    Debug.println("unbiasedExp=" 
    //      + Doubles.unbiasedExponent(x));
    final BigInteger[] nd = Doubles.toRatio(x);
    final BigInteger n = nd[0];
    final BigInteger d = nd[1];
    try {
      final double z = roundingTest(n,d);
      assert z == x : 
        "E:\n" 
        + Double.toHexString(x) + "\n"
        + Double.toHexString(z); 
      return z; } 
    catch (final Throwable t) {
      System.err.println("failed on x= " + Double.toHexString(x)); 
      throw t; } }

  //--------------------------------------------------------------

  private static final int TRYS = 16 * 1024;

  public static final void fromBigIntegersRoundingTest () {
    final Generator gn = 
      Generators.bigIntegerGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    final Generator gd = 
      Generators.positiveBigIntegerGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-07.txt"));
    for (int i=0;i<TRYS/64;i++) {
      final BigInteger n = (BigInteger) gn.next();
      final BigInteger d = (BigInteger) gd.next();
      roundingTest(n,d); } }

  public static final void fromLongsRoundingTest () {
    final Generator gn = 
      Generators.longGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    final Generator gd = 
      Generators.positiveLongGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-07.txt"));
    for (int i=0;i<TRYS;i++) {
      // some longs will not be exactly representable as doubles
      final long n = gn.nextLong();
      final long d = gd.nextLong();
      roundingTest(n,d); } }

  public static final void finiteDoubleRoundingTest () {
    final Generator g = 
      Doubles.finiteGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      roundingTest(x); } }

  public static final void normalDoubleRoundingTest () {
    final Generator g = 
      Doubles.normalGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-07.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      roundingTest(x); } }

  public static final void subnormalDoubleRoundingTest () {
    final Generator g = 
      Doubles.subnormalGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-09.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      roundingTest(x); } }

  //--------------------------------------------------------------

  public static final void main (final String[] args) {
    final long t = System.nanoTime();
    Debug.DEBUG = false;
    // test numbers outside double range
    final BigInteger[] nd = 
      Doubles.toRatio(Double.MAX_VALUE);
    final BigInteger a = nd[0].multiply(BigInteger.TEN);
    final BigInteger b = nd[1];
    roundingTest(a,b);
    roundingTest(a.negate(),b);
    roundingTest(b,a);
    roundingTest(b.negate(),a);
    final BigInteger a2 = a.multiply(BigInteger.TWO);
    roundingTest(a2,b);
    roundingTest(a2.negate(),b);
    roundingTest(b,a2);
    roundingTest(b.negate(),a2);
    final BigInteger a10 = a.multiply(BigInteger.TEN);
    roundingTest(a10,b);
    roundingTest(a10.negate(),b);
    roundingTest(b,a10);
    roundingTest(b.negate(),a10);
    //DEBUG=true;
    roundingTest(0x0.0000000000001p-1022);
    roundingTest(0x0.1000000000001p-1022);
    roundingTest(0x0.1000000000000p-1022);
    roundingTest(0x1.0000000000001p-1022);
    roundingTest(0x1.0000000000000p-1022);
    roundingTest(0x0.0000000000001p-1022);
    roundingTest(0x0.033878c4999b7p-1022);
    roundingTest(0x1.33878c4999b6ap-1022);
    roundingTest(-0x1.76c4ebe6d57c8p-924);
    roundingTest(0x1.76c4ebe6d57c8p-924);
    roundingTest(-0x1.76c4ebe6d57c8p924);
    roundingTest(0x1.76c4ebe6d57c8p924);
    roundingTest(1L,3L);
    fromLongsRoundingTest();
    normalDoubleRoundingTest();
    subnormalDoubleRoundingTest();
    //finiteDoubleRoundingTest(); 
    fromBigIntegersRoundingTest(); 
    System.out.printf("total secs: %8.2f\n",
      Double.valueOf((System.nanoTime()-t)*1.0e-9)); 
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
