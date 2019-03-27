package xfp.java.test.numbers;

import static java.lang.Double.MAX_EXPONENT;

//----------------------------------------------------------------
/** Test desired properties of doubles. 
 * <p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/numbers/DoublesTest test > DoublesTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-26
 */

public final class DoublesTest {

  //--------------------------------------------------------------

//  private static final double makeDouble (final int s,
//                                         final int ue,
//                                         final long t) {
//    assert ((0 == s) || (1 ==s)) : "Invalid sign bit:" + s;
//    System.out.println(ue);
//    assert (SUBNORMAL_EXPONENT <= ue) && (ue <= MAX_EXPONENT) :
//      "invalid (unbiased) exponent:" + toHexString(ue);
//    final int e = ue + EXPONENT_BIAS;
//    assert (0 <= e) :
//      "Negative exponent:" + Integer.toHexString(e);
//    assert (e <= MAXIMUM_BIASED_EXPONENT) :
//      "Exponent too large:" + Integer.toHexString(e) +
//      ">" + Integer.toHexString(MAXIMUM_BIASED_EXPONENT);
//    assert (0 <= t) :
//      "Negative significand:" + Long.toHexString(t);
//    assert (t <= STORED_SIGNIFICAND_MASK) :
//      "Significand too large:" + Long.toHexString(t) +
//      ">" + Long.toHexString(STORED_SIGNIFICAND_MASK);
//
//    final long ss = ((long) s) << (EXPONENT_BITS + STORED_SIGNIFICAND_BITS);
//    final long se = ((long) e) << STORED_SIGNIFICAND_BITS;
//    assert (0L == (ss & se & t));
//    final double x = longBitsToDouble(ss | se | t);
//    
////    System.out.println("-1^" + s + "*" + Long.toHexString(t) 
////    + "*2^" + ue);
////    System.out.println(toHexString(x));
////    System.out.println(
////      Integer.toHexString(biasedExponent(x)));
////    System.out.println(
////      Integer.toHexString(unbiasedExponent(x)));
////    System.out.println();
//
//    return x; }

//  private static final Generator 
//  doubleGenerator (final UniformRandomProvider urp,
//                   final int eMin,
//                   final int eMax) {
//    return new Generator () {
//      private final int eRan = eMax - eMin; 
//      @Override
//      public final double nextDouble () { 
//        final int s = urp.nextInt(2);
//        final int d = urp.nextInt(eRan);
//        final int e = d + eMin; // unbiased exponent
//        final long t = urp.nextLong() & STORED_SIGNIFICAND_MASK;
//        final double x = makeDouble(s,e,t); 
//      return x;} 
//      @Override
//      public final Object next () {
//        return Double.valueOf(nextDouble()); } }; }

  public static final int maxExponent (final int dim) { 
    final int d =
      //Floats.MAXIMUM_BIASED_EXPONENT  
      MAX_EXPONENT  
      - 31 
      + Integer.numberOfLeadingZeros(dim); 
    System.out.println("delta=" + d);
    return d; }

  //--------------------------------------------------------------

//  @SuppressWarnings({ "static-method" })
//  @Test
//  public final void finiteGeneratorTest () {
//    final UniformRandomProvider urp = 
//      PRNG.well44497b("seeds/Well44497b-2019-01-05.txt");
//    final Generator g = doubleGenerator(
//      urp, SUBNORMAL_EXPONENT, MAX_EXPONENT);
//    for (int i=0;i<2;i++) { g.nextDouble(); }
//  }

  //--------------------------------------------------------------
  //  @SuppressWarnings({ "static-method" })
  //  @Test
  //  public final void roundingTest () {
  //
  //    final double dx = Doubles.MAX_INTEGER;
  //    final double dx1 = dx + 1.0;
  //    final long ldx = (long) dx;
  //    final long ldx1 = ldx + 1L;
  //    final double dldx1 = ldx1;
  //    final long ldldx1 = (long) dldx1;
  //    final double udx = Math.nextUp(dx);
  //    final long ludx = (long) udx;
  //    
  //    System.out.println(dx + ", " + Double.toHexString(dx));
  //    System.out.println(ldx);
  //    System.out.println(dx1 + ", " + Double.toHexString(dx));
  //    System.out.println(ldx1);
  //    System.out.println(dldx1 + ", " + Double.toHexString(dx));
  //    System.out.println(ldldx1);
  //    System.out.println(udx + ", " + Double.toHexString(udx));
  //    System.out.println(ludx);
  //    }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
