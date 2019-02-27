package xfp.java.test.numbers;

//----------------------------------------------------------------
/** Test desired properties of BigFractions. 
 * <p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/numbers/BigFractionTest test > BigFractionTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-26
 */

public final class BigFractionTest {

  // TODO: doubleValue doesn't round to nearest. 
  // Test below fails with both random double -> BigFraction
  // and random long,long -> BigFraction.
  // Implement something that does.

//  private static final boolean correctRounding (final BigFraction f) {
//    // TODO: this is necessary but not sufficient to ensure 
//    // rounding was correct?
//    final double d0 = f.doubleValue();
//    final BigFraction f0 = new BigFraction(d0).reduce();
//    final int r = f.compareTo(f0);
//    if (r < 0) {
//      final double d1 = Math.nextDown(d0);
//      final BigFraction f1 = new BigFraction(d1).reduce();
//      return f1.compareTo(f) < 0; }
//    else if (r > 0) {
//      final double d1 = Math.nextUp(d0);
//      final BigFraction f1 = new BigFraction(d1).reduce();
//      return f1.compareTo(f) > 0; } 
//    else { return true; }}

//  private static final int TRYS = 1023;

  
//  @SuppressWarnings({ "static-method" })
//  @Test
//  public final void roundingTest () {
//    // TODO: BigFraction from longs generator
//    // TODO: does this generator really cover all longs?
//    final Generator g = 
//      //Generators.longGenerator(
//        Generators.doubleGenerator(
//        PRNG.well44497b(
//          Seeds.seed("seeds/Well44497b-2019-01-05.txt")));
//    for (int i=0;i<TRYS;i++) {
//      // some longs will not be exactly representable as doubles
//      final double x = g.nextDouble();
//      final BigFraction f = new BigFraction(x).reduce();
////      final long n = g.nextLong();
////      final long d = g.nextLong();
////      final BigFraction f = new BigFraction(n,d).reduce();
//      assertTrue(
//        correctRounding(f),
//        "\n" + i +
//        "\n" + f + " incorrectly rounded -> " + 
//        f.doubleValue() + 
//        "\n" + 
//        new BigFraction(Math.nextDown(f.doubleValue())) + 
//        "\n" + 
//        new BigFraction(f.doubleValue()) +
//        "\n" + 
//        new BigFraction(Math.nextUp(f.doubleValue()))); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
