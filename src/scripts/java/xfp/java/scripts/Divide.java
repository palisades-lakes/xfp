package xfp.java.scripts;

import static xfp.java.numbers.Doubles.SIGNIFICAND_BITS;
import static xfp.java.numbers.Doubles.SIGNIFICAND_MASK;

import java.math.BigInteger;

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
 * j --source 11 src/scripts/java/xfp/java/scripts/Divide.java > divide.txt 2>&1
 * </pre>
 * Profiling:
 * <pre>
 * jy --source 11 src/scripts/java/xfp/java/scripts/Divide.java
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-19
 */

@SuppressWarnings("unchecked")
public final class Divide {

  //--------------------------------------------------------------

  private static boolean DEBUG = false;

  private static final void debug () {
    if (DEBUG) { System.out.println(); } }

  private static final void debug (final String msg) {
    if (DEBUG) { System.out.println(msg); } }

  //--------------------------------------------------------------

  /** inclusive */
  private static final int loBit (final BigInteger i) {
    return i.getLowestSetBit(); }

  /** exclusive */
  private static final int hiBit (final BigInteger i) {
    return i.bitLength(); }

  private static final int loBit (final long i) {
    return Long.numberOfTrailingZeros(i); }

  private static final int hiBit (final long i) {
    return 64 -  Long.numberOfLeadingZeros(i); }

  //--------------------------------------------------------------

  private static final String description (final String name,
                                           final BigInteger i) {

    return name 
      + "[lo,hi)=" + loBit(i) + "," + hiBit(i) + ")"
      + " : " + i.toString(0x10); }

  public static final String description (final String name,
                                          final long i) {

    return name + " = " 
      + Long.toHexString(i) + "; " + Long.toString(i) + "\n"
      + "lo,hi bits= [" + 
      loBit(i) + "," + hiBit(i) + ")"; }

  //--------------------------------------------------------------
  // to compare with numbers-java results

  private static final double ToDouble (final BigInteger n,
                                        final BigInteger d) {

    final EInteger ni = EInteger.FromBytes(n.toByteArray(), false);
    final EInteger di = EInteger.FromBytes(d.toByteArray(), false);
    final EFloat nf = EFloat.FromEInteger(ni); 
    final EFloat df = EFloat.FromEInteger(di); 
    final EFloat f = nf.Divide(df, EContext.Binary64);
    final double ze = f.ToDouble(); 
    //    debug();
    //    debug("ToDouble(BigInteger,BigInteger,int,int)");
    //    debug(description("n",n));
    //    debug(description("d",d));
    //    debug("-> " + Double.toHexString(ze));
    return ze;}

  private static final double ToDouble (final BigInteger n,
                                        final BigInteger d,
                                        final boolean negative) {
    return negative ? ToDouble(n.negate(),d) : ToDouble(n,d) ; }

  private static final double ToDouble (final BigInteger n,
                                        final BigInteger d,
                                        final boolean negative,
                                        final int e) {
    if (e < 0) { return ToDouble(n,d.shiftLeft(-e),negative); }
    if (e > 0) { return ToDouble(n.shiftLeft(e),d,negative); }
    return ToDouble(n,d,negative); }

  private static final double ToDouble (final BigInteger q,
                                        final boolean negative,
                                        final int e) {
    return ToDouble(q,BigInteger.ONE,negative,e); }

  public static final double ToDouble (final long q,
                                       final boolean negative,
                                       final int e) {
    return ToDouble(BigInteger.valueOf(q),negative,e); }

  private static final double ToDouble (final long n,
                                        final long d) {
    return ToDouble(
      BigInteger.valueOf(n),
      BigInteger.valueOf(d)); }

  //--------------------------------------------------------------

  private static final int NUMERATOR_BITS = SIGNIFICAND_BITS + 1;

  /** Inclusive lower bound on exponents for rounding to double.
   */
  private static final int MINIMUM_EXPONENT =
    Double.MIN_EXPONENT - Doubles.SIGNIFICAND_BITS;

  /** Exclusive upper bound on exponents for rounding to double.
   */
  private static final int MAXIMUM_EXPONENT =
    Double.MAX_EXPONENT - Doubles.SIGNIFICAND_BITS + 1;

  //--------------------------------------------------------------
  /** Handle over and under flow (too many/few bits in q).
   * 
   * @param q rounded(n/d)
   * @param s sign bit: 0 positive, 1 negative.
   * @param e exponent
   * @return (-1)^s * (2^e) * q.
   */

  private static final double divide8 (final long q,
                                       final boolean negative,
                                       final int e) {
    assert q >= 1;
    assert MINIMUM_EXPONENT <= e : Integer.toString(e);
    assert e < MAXIMUM_EXPONENT : Integer.toString(e);

    //    debug();
    //    debug("divide7(long,int,int)");
    //    debug(description("q",q));
    //    debug("neg=" + negative + ", e= " + e);

    assert hiBit(q) <= NUMERATOR_BITS;
    final long q1 = q & SIGNIFICAND_MASK; 
    final int e1;
    if (hiBit(q) < NUMERATOR_BITS) { // subnormal
      e1 = e + NUMERATOR_BITS - 2; }
    else { e1 = e + NUMERATOR_BITS - 1; }

    // losing the high bit, if set
    //    debug(description("q1",q1));
    //    debug("e1= " + e1);

    final double z = Doubles.makeDouble(negative,q1,e1);
    //    final double ze = ToDouble(q,negative,e);
    //    assert ze == z :
    //      "\n" 
    //      + Double.toHexString(ze) + " :E\n"
    //      + Double.toHexString(z); 
    return z; }

  //--------------------------------------------------------------
  /** Handle carry (too many bits in q).
   * 
   * @param q rounded(n/d)
   * @param s sign bit: 0 positive, 1 negative.
   * @param e exponent
   * @return (-1)^s * (2^e) * q.
   */

  private static final double divide7 (final BigInteger q,
                                       final boolean negative,
                                       final int e) {
    debug();
    debug("divide7(BigInteger,int,int)");
    debug(description("q",q));
    debug("neg=" + negative + ", e= " + e);

    assert q.signum() == 1;
    assert MINIMUM_EXPONENT <= e : Integer.toString(e);
    assert e < MAXIMUM_EXPONENT : Integer.toString(e);
    assert hiBit(q) <= NUMERATOR_BITS + 1;

    final BigInteger qq;
    final int ee;
    if (hiBit(q) <= NUMERATOR_BITS) { qq = q; ee =e; }
    // handle carry
    else { qq = q.shiftRight(1); ee = e+1; }
    final double z = divide8(qq.longValue(),negative,ee); 

    //    final double ze = ToDouble(qq,s,ee);
    //    assert ze == z :
    //      "\n" 
    //      + Double.toHexString(ze) + " :E\n"
    //      + Double.toHexString(z); 

    return z; }

  //--------------------------------------------------------------
  /** Is q or (q+1) mod carry closer to n/d?.
   * 
   * TODO: Ought to be able to determine this without
   * forming all the intermediate BigIntegers.
   * 
   * @param n positive numerator
   * @param d positive denominator
   * @param s sign bit: 0 positive, 1 negative.
   * @param e exponent
   * @param q floor(n/d)
   * @param r d*((n/d) - floor(n/d))
   * @return closest half-even rounded double to 
   * (-1)^s * (2^e) * n / d.
   */

  private static final double divide6 (final BigInteger n,
                                       final BigInteger d,
                                       final boolean negative,
                                       final int e,
                                       final BigInteger q,
                                       final BigInteger r) {
    debug();
    debug("divide6(BigInteger,BigInteger,int,int,BigInteger,BigInteger)");
    debug(description("n",n));
    debug(description("d",d));
    debug("neg=" + negative + ", e= " + e);
    debug(description("q",q));
    debug(description("r",r));

    assert n.signum() == 1;
    assert d.signum() == 1;
    assert MINIMUM_EXPONENT <= e : Integer.toString(e);
    assert e < MAXIMUM_EXPONENT : Integer.toString(e);
    assert q.signum() == 1;
    assert hiBit(q) <= NUMERATOR_BITS 
      : "too many bits:" + description("q",q);
    assert r.signum() >= 0;
    assert q.compareTo(n) <= 0;
    assert r.compareTo(d) < 0;

    final BigInteger q1 = q.add(BigInteger.ONE);
    debug(description("q1",q1));
    //    final BigInteger q1dmn = q1.multiply(d).subtract(n);
    //    debug(description("q1*d-n",q1dmn));
    //    debug(r.compareTo(q1dmn));

    //final BigInteger r2 = r.shiftLeft(1);
    final int c = 
      q.multiply(d).subtract(n).abs()
      .compareTo(
        q1.multiply(d).subtract(n).abs());

    //    debug(description("r2",r2));
    //    debug("r2<=d: " + c);

    if (c < 0) { debug("down"); }
    if (c > 0) { debug("up"); }
    if (c == 0) { debug("tie"); }

    final BigInteger qq = (c <= 0) ? q : q.add(BigInteger.ONE);

    debug(description("qq",qq));

    final double z = divide7(qq,negative,e);

    final BigInteger n1 = q.multiply(d).add(r);
    final double ze0 = ToDouble(n1,d,negative,e);
    final double ze1 = ToDouble(qq,negative,e);
    assert ze0 == z :
      "\n" 
      + Double.toHexString(ze0) + " :E0\n"
      + Double.toHexString(ze1) + " :E1\n"
      + Double.toHexString(z); 

    return z; }

  //--------------------------------------------------------------
  /** Divide numerator by denominator to get quotient and
   * remainder.
   *  0 < n < (d << 53)
   *  
   * @param n positive numerator
   * @param d positive denominator
   * @param s sign bit: 0 positive, 1 negative.
   * @param e initial exponent
   * @return closest half-even rounded double to 
   * (-1)^s * (2^e) * n / d.
   */

  private static final double divide5 (final BigInteger n,
                                       final BigInteger d,
                                       final boolean negative,
                                       final int e) {
    debug();
    debug("divide5(BigInteger,BigInteger,int,int)");
    debug(description("n",n));
    debug(description("d",d));
    debug("neg=" + negative + ", e= " + e);

    assert n.signum() == 1;
    assert d.signum() == 1;
    assert d.compareTo(n) <= 0;
    //assert n.compareTo(d.shiftLeft(1)) < 0;
    assert MINIMUM_EXPONENT <= e : Integer.toString(e);
    assert e < MAXIMUM_EXPONENT : Integer.toString(e);

    final BigInteger[] qr = n.divideAndRemainder(d);
    final BigInteger q = qr[0];
    final BigInteger r = qr[1];

    final double z = divide6(n,d,negative,e,q,r); 

    final BigInteger n1 = q.multiply(d).add(r);
    final double ze = ToDouble(n1,d,negative,e);
    assert ze == z :
      "\n" 
      + Double.toHexString(ze) + " :E\n"
      + Double.toHexString(z); 

    return z; }

  //--------------------------------------------------------------
  /** Adjust n and d and e so that n is the maximum value
   * such that 0 &le; n/d &lt; 2^53
   * and -1074 &le; e &lt; 972.
   * 
   * @param n positive numerator
   * @param d positive denominator
   * @param s sign bit: 0 positive, 1 negative.
   * @param e initial exponent
   * @return closest half-even rounded double to 
   * (-1)^s * (2^e) * n / d.
   */

  private static final double divide3 (final BigInteger n,
                                       final BigInteger d,
                                       final boolean negative,
                                       final int e) {
    debug();
    debug("divide3(BigInteger,BigInteger,int,int)");
    debug(description("n",n));
    debug(description("d",d));
    debug("negative= " + negative + ", e= " + e);

    assert n.signum() == 1;
    assert d.signum() == 1;
    assert MINIMUM_EXPONENT <= e : Integer.toString(e);
    assert e < MAXIMUM_EXPONENT : Integer.toString(e);

    final int nh = hiBit(n);
    final int dh = hiBit(d);
    final int s0 = dh - nh;
    final int s1 = 
      Math.min(
        Math.max(s0,e-MAXIMUM_EXPONENT+1),
        e-MINIMUM_EXPONENT);
    final int e0 = e - s1;
    debug("s0=" + s0);
    debug("s1=" + s1);
    debug("e0=" + e0);

    final BigInteger n0, d0;
    if (nh < dh) { n0  = n.shiftLeft(s1); d0 = d; }
    else if (nh > dh) { n0 = n; d0 = d.shiftLeft(-s1); }
    else { n0 = n; d0 = d; }

    debug(description("n0",n0));
    debug(description("d0",d0));

    final BigInteger n1;
    final BigInteger d1;
    final int e1;
    // subnormal
    if (e0 == MINIMUM_EXPONENT) { n1 = n0; d1 = d0; e1 = e0;}
    else { // normal
      if (d0.compareTo(d0) <= 0) { n1 = n0; d1 = d0; e1 = e0;}
      else { 
        n1 = n0.shiftLeft(1);
        d1 = d0.shiftLeft(1);
        e1 = e0 -1;
        assert d0.compareTo(n1) <= 0;
        assert n1.compareTo(d1) < 0; } }

    debug(description("n1",n1));
    debug(description("d1",d1));

    final double z = divide5(n1,d1,negative,e1);
    final double ze = ToDouble(n0,d0,negative,e0);
    assert ze == z :
      "\n" 
      + Double.toHexString(ze) + " :E\n"
      + Double.toHexString(z); 

    return z; }

  /** Reduce n/d by shifting out trailing 0 bits into exponent.
   * @param n positive numerator
   * @param d positive denominator
   * @param s sign bit: 0 positive, 1 negative.
   * @return closest half-even rounded double to -1^s * n / d.
   */

  private static final double divide2 (final BigInteger n,
                                       final BigInteger d,
                                       final boolean negative) {
    debug();
    debug("divide2(BigInteger,BigInteger,int)");
    debug(description("n",n));
    debug(description("d",d));
    debug("neg=" + negative);

    assert n.signum() == 1;
    assert d.signum() == 1;

    final int en = loBit(n);
    final int ed = loBit(d);
    final int e = en-ed;

    debug("ed-en= " + en + "-" + ed + "=" + e);

    final BigInteger n1 = n.shiftRight(en);
    final BigInteger d1 = d.shiftRight(ed);
    assert 0 == loBit(n1);
    assert 0 == loBit(n1);

    final double z = divide3(n1,d1,negative,e); 

    final double ze = ToDouble(n1,d1,negative,e);
    assert ze == z :
      "\n" 
      + Double.toHexString(ze) + " :E\n"
      + Double.toHexString(z); 

    return z; }

  /** Reduce n/d by gcd.
   * @param n positive numerator
   * @param d positive denominator
   * @param s sign bit: 0 positive, 1 negative.
   * @return closest half-even rounded double to -1^s * n / d.
   */

  private static final double divide1 (final BigInteger n,
                                       final BigInteger d,
                                       final boolean negative) {
    //    debug();
    //    debug("divide1(BigInteger,BigInteger,int)");
    //    debug(description("n",n));
    //    debug(description("d",d));
    //    debug("neg=" + negative);

    assert n.signum() == 1;
    assert d.signum() == 1;

    final BigInteger gcd = n.gcd(d);
    final BigInteger n0,d0;
    if (BigInteger.ONE.equals(gcd)) { n0 = n; d0 = d; }
    else { n0 = n.divide(gcd); d0 = d.divide(gcd); }

    debug("gcd=" + gcd);

    final double z = divide2(n0,d0,negative);

    final double ze0 = ToDouble(n,d,negative);
    final double ze1 = ToDouble(n0,d0,negative);
    assert ze0 == ze1 :
      "\n" 
      + Double.toHexString(ze0) + " :E0\n"
      + Double.toHexString(ze1) + " :E1"; 
    assert ze0 == z :
      "\n" 
      + Double.toHexString(ze0) + " :E\n"
      + Double.toHexString(z); 

    return z; }

  /** Extract sign bit, so numerator and denominator are both
   * positive.
   * @param n numerator
   * @param d positive denominator
   * @return closest half-even rounded double to n / d.
   */

  private static final double divide0 (final BigInteger n,
                                       final BigInteger d) {
    debug();
    debug("divide0(BigInteger,BigInteger)");
    debug(description("n",n));
    debug(description("d",d));

    assert d.signum() == 1;
    final int c = n.signum();
    if (c == 0) { return 0.0; }
    if (c == -1) { return divide1(n.negate(),d,true); }
    if (c == 1) { return divide1(n,d,false); }
    throw new IllegalStateException("can't get here!"); }

  /**
   * @param n numerator
   * @param d positive denominator
   * @return closest half-even rounded double to n / d.
   */

  public static final double divide (final BigInteger n,
                                     final BigInteger d) {
    return divide0(n,d); }

  /**
   * @param n numerator
   * @param d positive denominator
   * @return closest half-even rounded double n / d.
   */

  public static final double divide (final long n,
                                     final long d) {
    assert d > 0L;
    return divide(BigInteger.valueOf(n),BigInteger.valueOf(d));  }

  //--------------------------------------------------------------

  public static final double roundingTest (final BigInteger n,
                                           final BigInteger d) {
    final double ze = ToDouble(n,d);
    debug(Double.toHexString(ze) + " :E");
    try {
      final double z = divide(n,d);
      debug(Double.toHexString(z) + " :D");
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
    //    debug();
    //    debug("roundingTest(" 
    //      + Double.toHexString(x) + ")");
    //    debug("signBit=" + signBit(x));
    //    debug("significand=" 
    //      + Long.toHexString(fullSignificand(x)));
    //    debug("significand=" 
    //      + Long.toBinaryString(fullSignificand(x)));
    //    debug("significand=" 
    //      + Long.toBinaryString(SIGNIFICAND_MASK));
    //    debug("unbiasedExp=" 
    //      + Doubles.unbiasedExponent(x));
    final BigInteger[] nd = RationalSum.toRatio(x);
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

  private static final int TRYS = 1024 * 1024;

  public static final void fromBigIntegersRoundingTest () {
    final Generator gn = 
      Generators.bigIntegerGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    final Generator gd = 
      Generators.nonzeroBigIntegerGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-07.txt"));
    for (int i=0;i<TRYS;i++) {
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
      Generators.finiteDoubleGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      roundingTest(x); } }

  public static final void normalDoubleRoundingTest () {
    final Generator g = 
      Generators.normalDoubleGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      roundingTest(x); } }

  public static final void subnormalDoubleRoundingTest () {
    final Generator g = 
      Generators.subnormalDoubleGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      roundingTest(x); } }

  //--------------------------------------------------------------

  public static final void main (final String[] args) {
    DEBUG = true;
    //final double x = 0x1.fffffffffffffp1021;
    final double x = 0x1.fffffffffffffp1021;
    System.out.println(Double.toHexString(x)
      + " : " + Double.toString(x));
    final BigInteger[] nd = RationalSum.toRatio(x);
    System.out.println(description("n",nd[0]));
    System.out.println(description("d",nd[1]));
    final double z = divide(nd[0],nd[1]);
    assert x == z;
    //    DEBUG= false;
    //    final BigInteger q6 = BigInteger.valueOf(0x130eb6938c0156L);
    //    final BigInteger q7 = BigInteger.valueOf(0x130eb6938c0157L);
    //    final BigInteger q8 = BigInteger.valueOf(0x130eb6938c0158L);
    //    final BigInteger n = BigInteger.valueOf(0x789f09858446ad92L).shiftLeft(52);
    //    final BigInteger d = BigInteger.valueOf(0x19513ea5d70c32eL).shiftLeft(6);
    //    final BigInteger dr6 = d.multiply(q6);
    //    final BigInteger dr7 = d.multiply(q7);
    //    final BigInteger dr8 = d.multiply(q8);
    //    final BigInteger nmdr6 = n.subtract(dr6);
    //    final BigInteger nmdr7 = n.subtract(dr7);
    //    final BigInteger nmdr8 = n.subtract(dr8);
    //    debug("n: " + n.toString(0x10));
    //    debug("d: " + d.toString(0x10));
    //    debug("6: " + nmdr6.toString(0x10));
    //    debug("7: " + nmdr7.toString(0x10));
    //    debug("8: " + nmdr8.toString(0x10));
    //    debug("c=" + nmdr6.abs().compareTo(nmdr7.abs()));
    //    debug();
    //    roundingTest(n.shiftRight(52),d);
    //    roundingTest(0x789f09858446ad92L,0x19513ea5d70c32eL);
    //    roundingTest(13L,3L);
    //    roundingTest(0x1.30eb6938c0156p6);
    //    roundingTest(0x1.30eb6938c0157p6);
    //    roundingTest(
    //      (0x789f09858446ad92L >>> 1) + 100L,
    //      (0x19513ea5d70c32eL << 5));
    //    roundingTest(
    //      (0x789f09858446ad92L >>> 1) + 10L,
    //      (0x19513ea5d70c32eL >>> 1));
    //    fromLongsRoundingTest();
    //    normalDoubleRoundingTest();
    //    DEBUG=true;
    //    roundingTest(0x0.0000000000001p-1022);
    //    //            roundingTest(0x0.1000000000001p-1022);
    //    //            roundingTest(0x0.1000000000000p-1022);
    //    //            roundingTest(0x1.0000000000001p-1022);
    //    //            roundingTest(0x1.0000000000000p-1022);
    //    //            roundingTest(0x0.0000000000001p-1022);
    //    //            roundingTest(0x0.033878c4999b7p-1022);
    //    //            roundingTest(0x1.33878c4999b6ap-1022);
    //    //            roundingTest(-0x1.76c4ebe6d57c8p-924);
    //    //            roundingTest(0x1.76c4ebe6d57c8p-924);
    //    //            roundingTest(-0x1.76c4ebe6d57c8p924);
    //    //            roundingTest(0x1.76c4ebe6d57c8p924);
    //    //            roundingTest(1L,3L);
    //    //        subnormalDoubleRoundingTest();
    //    //        finiteDoubleRoundingTest(); 
    //    //    fromBigIntegersRoundingTest(); 
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
