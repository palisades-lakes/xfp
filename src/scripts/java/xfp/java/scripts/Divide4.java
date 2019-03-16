package xfp.java.scripts;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xfp.java.numbers.Doubles.MAXIMUM_EXPONENT;
import static xfp.java.numbers.Doubles.MINIMUM_EXPONENT;
import static xfp.java.numbers.Doubles.MINIMUM_NORMAL_EXPONENT;
import static xfp.java.numbers.Doubles.MINIMUM_SUBNORMAL_EXPONENT;
import static xfp.java.numbers.Doubles.SIGNIFICAND_BITS;
import static xfp.java.numbers.Doubles.SIGNIFICAND_MASK;
import static xfp.java.numbers.Doubles.makeDouble;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import com.upokecenter.numbers.EContext;
import com.upokecenter.numbers.EFloat;
import com.upokecenter.numbers.EInteger;

import xfp.java.accumulators.RationalSum;
import xfp.java.numbers.Doubles;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;
import xfp.java.prng.PRNG;

/** BigInteger divie and round to double.
 * 
 * <pre>
 * j --source 11 src/scripts/java/xfp/java/scripts/Divide.java
 * </pre>
 * Profiling:
 * <pre>
 * jy --source 11 src/scripts/java/xfp/java/scripts/Divide.java
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-11
 */

@SuppressWarnings("unchecked")
public final class Divide4 {

  //--------------------------------------------------------------

  private static final double ToDouble (final BigInteger n,
                                        final BigInteger d) {
    final EInteger ni = EInteger.FromBytes(n.toByteArray(), false);
    final EInteger di = EInteger.FromBytes(d.toByteArray(), false);
    final EFloat nf = EFloat.FromEInteger(ni); 
    final EFloat df = EFloat.FromEInteger(di); 
    final EFloat f = nf.Divide(df, EContext.Binary64);
    return f.ToDouble(); }

  private static final double ToDouble (final int s,
                                        final BigInteger n,
                                        final BigInteger d) {
    return (s == 0) ? ToDouble(n,d) : ToDouble(n.negate(),d); }

  private static final double ToDouble (final long n,
                                        final long d) {
    return ToDouble(
      BigInteger.valueOf(n),
      BigInteger.valueOf(d)); }

  //--------------------------------------------------------------
  /**
   * @param n positive significand
   * @param s sign bit: 0 positive, 1 negative.
   * @param e initial exponent
   * @return closest half-even rounded double to 
   * (-1)^s * (2^e) * n.
   */

  private static final double divide (final BigInteger n,
                                      final int s,
                                      final int e) {

    System.out.println();
    System.out.println("divide(BigInteger,int,int)");
    System.out.println("n= " + n.toString(0x10));
    System.out.println("n.bitLength= " + n.bitLength());
    System.out.println("s= " + s);
    System.out.println("e= " + e);

    assert n.signum() == 1;
    assert (s == 0) || (s == 1);

    final int nb = n.bitLength();
    int shift = Math.max(
      nb - SIGNIFICAND_BITS - 1,
      MINIMUM_NORMAL_EXPONENT - e);
    final int e0 = e + shift;

    if (e0 < MINIMUM_SUBNORMAL_EXPONENT) {
      if (s == 0) { return 0.0; }
      return -0.0; }
    if (e0 > MAXIMUM_EXPONENT) {
      if (s == 0) { return POSITIVE_INFINITY; }
      return NEGATIVE_INFINITY; }

    final BigInteger n0 = n.shiftRight(shift);
    final long t = n0.longValue() & SIGNIFICAND_MASK;

    final double z = makeDouble(s,t,e);
    final BigInteger n1 = 
      n.shiftLeft(Math.max(0,e-SIGNIFICAND_BITS));
    final BigInteger d1 = 
      BigInteger.ONE.shiftLeft(Math.max(0,SIGNIFICAND_BITS-e+shift));
    final double ze = ToDouble(s,n1,d1);
    if (z != ze) {
      System.out.println();
      System.out.println("n = " + n.toString(0x10));
      System.out.println("s = " + s);
      System.out.println("e = " + e);
      System.out.println("nb = " + nb + " : " + shift);
      System.out.println("n0 = " + n0.toString(0x10));
      System.out.println("t = " + Long.toHexString(t));
      System.out.println("ToDouble(" + n1.toString(0x10) + ")");
      System.out.println(Double.toHexString(ze) + " :E");
      System.out.println(Double.toHexString(z));
      System.out.println(Double.toString(ze/z));
    }
    return z; }


  /**
   * @param n positive numerator
   * @param d positive denominator
   * @param s sign bit: 0 positive, 1 negative.
   * @param e initial exponent
   * @return closest half-even rounded double to 
   * (-1)^s * (2^e) * n / d.
   */

  private static final double divide (final BigInteger n,
                                      final BigInteger d,
                                      final int s,
                                      final int e) {
    assert n.signum() == 1;
    assert d.signum() == 1;
    assert (s == 0) || (s == 1);

    System.out.println();
    System.out.println("divide(BigInteger,BigInteger,int,int)");
    System.out.println("n= " + n.toString(0x10));
    System.out.println("n.bitLength= " + n.bitLength());
    System.out.println("d= " + d.toString(0x10));
    System.out.println("d.bitLength= " + d.bitLength());
    System.out.println("s= " + s);
    System.out.println("e= " + e);

    // common (?) special case, when inputs start as floating point
    if (BigInteger.ONE.equals(d)) { return divide(n,s,e); }
    throw new IllegalStateException("not now " + d.toString(0x10)); }
  //    
  //     final long nb = n.bitLength();
  //    final long db = d.bitLength();
  //    int shift = (int) (db - nb + SIGNIFICAND_BITS + 1);
  //    final BigInteger n0 = n.shiftLeft(shift); 
  //    final int e0 = e -shift;
  //    // eager return, might still be too small otherwise
  //    if (e0 < MINIMUM_SUBNORMAL_EXPONENT) {
  //      if (s == 0) { return 0.0; }
  //      return -0.0; }
  //    
  //    final BigInteger[] qr = n0.divideAndRemainder(d);
  //    final BigInteger q = qr[0];
  //    final BigInteger qd = q.multiply(d);
  //    final BigInteger nmqd = n0.subtract(qd);
  //    final BigInteger nmqdmd = d.subtract(nmqd);
  //    final int iround = nmqdmd.compareTo(nmqd);
  //    final long ql = q.longValue();
  //    long tt; 
  //    if (iround < 0) { tt = ql + 1L; }
  //    else if (iround > 0) { tt = ql; }
  //    // round ties to even
  //    else if (0L == (ql & 0x1L)) { tt = ql; }
  //    else { tt = ql + 1L; }
  //    final int adjust = 64 - Long.numberOfLeadingZeros(tt) - SIGNIFICAND_BITS - 1; 
  //    if (adjust != 0 ) { tt = tt >>> adjust; shift -= 1; }
  //    final long t = tt & SIGNIFICAND_MASK;
  //    final int e1 = SIGNIFICAND_BITS-shift;
  //    final double z = makeDouble(s,t,e);
  //    final double ze = ToDouble(n,d);
  //    if ((nmqd.signum() < 0)
  //      ||
  //      (nmqdmd.signum() < 0)
  //      || 
  //      (z != ze)) {
  //      System.out.println();
  //      System.out.println("n = " + n.toString(0x10));
  //      //System.out.println("np = " + np.toString(0x10));
  //      System.out.println("d = " + d.toString(0x10));
  //      System.out.println("nb,db= " + nb + ", " + db + " : " + shift);
  //      System.out.println("ns = " + n0.toString(0x10));
  //      System.out.println("q  = " + q.toString(0x10));
  //      System.out.println("qd = " + qd.toString(0x10));
  //      System.out.println("nmqdm1 = " + nmqdmd.toString(0x10));
  //      System.out.println("nmqd   = " + nmqd.toString(0x10));
  //      System.out.println("iround = " + iround);
  //      System.out.println("ql = " + Long.toHexString(ql) + " " + (64L - Long.numberOfLeadingZeros(ql)));
  //      System.out.println("tt = " + Long.toHexString(tt) + " " + (64L - Long.numberOfLeadingZeros(tt)));
  //      System.out.println(toHexString(s,t,e));
  //      System.out.println(Double.toHexString(ze) + " :E");
  //      System.out.println(Double.toHexString(z));
  //      System.out.println(Double.toHexString(ze/z));
  //      System.out.println(Double.toString(ze/z));
  //
  //    }
  //    return z; }

  /**
   * @param n positive numerator
   * @param d positive denominator
   * @param s sign bit: 0 positive, 1 negative.
   * @return closest half-even rounded double to -1^s * n / d.
   */

  private static final double divide (final BigInteger n,
                                      final BigInteger d,
                                      int s) {
    System.out.println();
    System.out.println("divide(BigInteger,BigInteger,int)");
    System.out.println("n= " + n.toString(0x10));
    System.out.println("n.bitLength= " + n.bitLength());
    System.out.println("d= " + d.toString(0x10));
    System.out.println("d.bitLength= " + d.bitLength());
    System.out.println("s= " + s);

    assert n.signum() == 1;
    assert d.signum() == 1;
    assert (s == 0) || (s == 1);

    final BigInteger gcd = n.gcd(d);
    //    System.out.println("gcd=" + gcd);
    final BigInteger n0,d0;
    if (BigInteger.ONE.equals(gcd)) { n0 = n; d0 = d; }
    else { n0 = n.divide(gcd); d0 = d.divide(gcd); }

    final int en = 
      Math.min(
        n0.getLowestSetBit(), 
        n0.bitLength()-SIGNIFICAND_BITS-1);
    final int ed = d0.getLowestSetBit();
    //    System.out.println("en, ed = " + en + ", " + ed);

    final BigInteger n1 = 
      (en > 0) ? n0.shiftRight(en) : n0;
      final BigInteger d1 = 
        (ed > 0) ? d0.shiftRight(ed) : d0; 
        System.out.println("n1= " + n1.toString(0x10));
        System.out.println("d1= " + d1.toString(0x10));
        return divide(n1,d1,s,en-ed+SIGNIFICAND_BITS); }

  /**
   * @param n numerator
   * @param d positive denominator
   * @return closest half-even rounded double to n / d.
   */

  private static final double divide (final BigInteger n,
                                      final BigInteger d) {
    System.out.println();
    System.out.println("divide(BigInteger,BigInteger)");
    System.out.println("n= " + n.toString(0x10));
    System.out.println("n.bitLength= " + n.bitLength());
    System.out.println("d= " + d.toString(0x10));
    System.out.println("d.bitLength= " + d.bitLength());

    assert d.signum() == 1;
    final int c = n.signum();
    if (c == 0) { return 0.0; }
    if (c == -1) { return divide(n.negate(),d,1); }
    if (c == 1) { return divide(n,d,0); }
    throw new IllegalStateException("can't get here!"); }

  /**
   * @param n numerator
   * @param d positive denominator
   * @return closest half-even rounded double n / d.
   */

  private static final double divide (final long n,
                                      final long d) {
    assert d > 0L;
    return divide(BigInteger.valueOf(n),BigInteger.valueOf(d));  }

  //--------------------------------------------------------------

  private static final int TRYS = 1024 * 1024;

  private static final void fromBigIntegersRoundingTest () {
    final Generator gn = 
      Generators.bigIntegerGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    final Generator gd = 
      Generators.nonzeroBigIntegerGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-07.txt"));
    for (int i=0;i<TRYS;i++) {
      final BigInteger n = (BigInteger) gn.next();
      final BigInteger d = (BigInteger) gd.next();
      final double z = divide(n,d);
      final double ze = ToDouble(n,d);
      assert z == ze : i; } }

  private static final void fromLongsRoundingTest () {
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
      final double z = divide(n,d);
      final double ze = ToDouble(n,d);
      assert z == ze : i; } }

  private static final void finiteDoubleRoundingTest () {
    final Generator g = 
      Generators.finiteDoubleGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final BigInteger[] nd = RationalSum.toRatio(x);
      final BigInteger n = nd[0];
      final BigInteger d = nd[1];
      final double z = divide(n,d);
      final double ze = ToDouble(n,d);
      assert z == ze : i+ " : " + Double.toHexString(x); } }

  private static final void normalDoubleRoundingTest () {
    final Generator g = 
      Generators.normalDoubleGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final BigInteger[] nd = RationalSum.toRatio(x);
      final BigInteger n = nd[0];
      final BigInteger d = nd[1];
      final double z = divide(n,d);
      final double ze = ToDouble(n,d);
      assert z == ze : 
        i + "\n" 
        + Double.toHexString(x) + "\n"
        + Double.toHexString(ze) + "\n"
        + Double.toHexString(z); } }

  private static final void subnormalDoubleRoundingTest () {
    final Generator g = 
      Generators.subnormalDoubleGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      final BigInteger[] nd = RationalSum.toRatio(x);
      final BigInteger n = nd[0];
      final BigInteger d = nd[1];
      final double z = divide(n,d);
      final double ze = ToDouble(n,d);
      assert z == ze : i + " : " + Double.toHexString(x); } }

  private static final void roundingTest (final double x) {
    final BigInteger[] nd = RationalSum.toRatio(x);
    final BigInteger n = nd[0];
    final BigInteger d = nd[1];
    System.out.println("n=" + n.toString(0x10));
    System.out.println("n=" + n.setBit(n.bitLength()+1).toString(0x10));
       final double z = divide(n,d);
    final double ze = ToDouble(n,d);
    assert z == x : 
      "\n" 
      + Double.toHexString(x) + "\n"
      + Double.toHexString(ze) + "\n"
      + Double.toHexString(z); } 

  public static final void main (final String[] args) {
    //    roundingTest(0x1.76c4ebe6d57c8p924);
    //    roundingTest(0x1.76c4ebe6d57c8p-924);
    //    roundingTest(-0x1.76c4ebe6d57c8p924);
    //    roundingTest(-0x1.76c4ebe6d57c8p-924);
    roundingTest(0x1.33878c4999b6ap-1022);
    //    normalDoubleRoundingTest();
    //    subnormalDoubleRoundingTest();
    //    finiteDoubleRoundingTest(); 
    //    fromLongsRoundingTest();
    //    fromBigIntegersRoundingTest(); 
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
