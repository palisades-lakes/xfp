package xfp.java.scripts;

import static xfp.java.numbers.Doubles.MINIMUM_EXPONENT;
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
 * @version 2019-03-16
 */

@SuppressWarnings("unchecked")
public final class Divide {

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

    return name + " = " + i.toString(0x10)
    //+ "\n"
    + "; lo,hi bits= [" + 
    loBit(i) + "," + hiBit(i) + ")"; }

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
    //    System.out.println();
    //    System.out.println("ToDouble(BigInteger,BigInteger,int,int)");
    //    System.out.println(description("n",n));
    //    System.out.println(description("d",d));
    //    System.out.println("-> " + Double.toHexString(ze));
    return ze;}

  private static final double ToDouble (final BigInteger n,
                                        final BigInteger d,
                                        final int s) {
    return (s == 0) ? ToDouble(n,d) : ToDouble(n.negate(),d); }

  private static final double ToDouble (final BigInteger n,
                                        final BigInteger d,
                                        final int s,
                                        final int e) {
    if (e < 0) { return ToDouble(n,d.shiftLeft(-e),s); }
    if (e > 0) { return ToDouble(n.shiftLeft(e),d,s); }
    return ToDouble(n,d,s); }

  private static final double ToDouble (final BigInteger q,
                                        final int s,
                                        final int e) {
    return ToDouble(q,BigInteger.ONE,s,e); }

  public static final double ToDouble (final long q,
                                       final int s,
                                       final int e) {
    return ToDouble(BigInteger.valueOf(q),s,e); }

  private static final double ToDouble (final long n,
                                        final long d) {
    return ToDouble(
      BigInteger.valueOf(n),
      BigInteger.valueOf(d)); }

  //--------------------------------------------------------------

  private static final int NUMERATOR_BITS = SIGNIFICAND_BITS + 1;


  //--------------------------------------------------------------
  /** Handle over and under flow (too many/few bits in q).
   * 
   * @param q rounded(n/d)
   * @param s sign bit: 0 positive, 1 negative.
   * @param e exponent
   * @return (-1)^s * (2^e) * q.
   */

  private static final double divide7 (final long q,
                                       final int s,
                                       final int e) {
    assert q >= 1;
    assert (s == 0) || (s == 1);

    //    System.out.println();
    //    System.out.println("divide7(long,int,int)");
    //    System.out.println(description("q",q));
    //    System.out.println("s= " + s + ", e= " + e);

    assert hiBit(q) <= NUMERATOR_BITS;
    final long q1 = q & SIGNIFICAND_MASK; 
    final int e1;
    if (hiBit(q) < NUMERATOR_BITS) { // subnormal
      e1 = e + NUMERATOR_BITS - 2; }
    else { e1 = e + NUMERATOR_BITS - 1; }

    // losing the high bit, if set
    //    System.out.println(description("q1",q1));
    //    System.out.println("e1= " + e1);

    final double z = Doubles.makeDouble(s,q1,e1);
    //    final double ze = ToDouble(q,s,e);
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

  private static final double divide6 (final BigInteger q,
                                       final int s,
                                       final int e) {
    assert q.signum() == 1;
    assert (s == 0) || (s == 1);
    assert hiBit(q) <= NUMERATOR_BITS + 1;

    //    System.out.println();
    //    System.out.println("divide6(BigInteger,int,int)");
    //    System.out.println(description("q",q));
    //    System.out.println("s= " + s + ", e= " + e);

    final BigInteger qq;
    final int ee;
    if (hiBit(q) <= NUMERATOR_BITS) { qq = q; ee =e; }
    // handle carry
    else { qq = q.shiftRight(1); ee = e+1; }
    final double z = divide7(qq.longValue(),s,ee); 

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

  private static final double divide5 (final BigInteger n,
                                       final BigInteger d,
                                       final int s,
                                       final int e,
                                       final BigInteger q,
                                       final BigInteger r) {
    assert n.signum() == 1;
    assert d.signum() == 1;
    assert q.signum() == 1;
    assert r.signum() >= 0;
    assert q.compareTo(n) <= 0;
    assert r.compareTo(d) < 0;
    assert (s == 0) || (s == 1);

    System.out.println();
    System.out.println("divide5(BigInteger,BigInteger,int,int,BigInteger,BigInteger)");
    System.out.println(description("n",n));
    System.out.println(description("d",d));
    System.out.println("s= " + s + ", e= " + e);
    System.out.println(description("q",q));
    System.out.println(description("r",r));

    final BigInteger q1 = q.add(BigInteger.ONE);
    System.out.println(description("q1",q1));
    //    final BigInteger q1dmn = q1.multiply(d).subtract(n);
    //    System.out.println(description("q1*d-n",q1dmn));
    //    System.out.println(r.compareTo(q1dmn));

    //final BigInteger r2 = r.shiftLeft(1);
    final int c = 
      q.multiply(d).subtract(n).abs()
      .compareTo(
        q1.multiply(d).subtract(n).abs());

    //    System.out.println(description("r2",r2));
    //    System.out.println("r2<=d: " + c);

    if (c < 0) { System.out.println("down"); }
    if (c > 0) { System.out.println("up"); }
    if (c == 0) { System.out.println("tie"); }

    final BigInteger qq = (c <= 0) ? q : q.add(BigInteger.ONE);

    System.out.println(description("qq",qq));

    final double z = divide6(qq,s,e);

    final BigInteger n1 = q.multiply(d).add(r);
    final double ze0 = ToDouble(n1,d,s,e);
    final double ze1 = ToDouble(qq,s,e);
    assert ze0 == z :
      "\n" 
      + Double.toHexString(ze0) + " :E0\n"
      + Double.toHexString(ze1) + " :E1\n"
      + Double.toHexString(z); 

    return z; }

  //--------------------------------------------------------------
  /** Divide numerator by denominator to get quotient and
   * remainder.
   * 
   * @param n positive numerator
   * @param d positive denominator
   * @param s sign bit: 0 positive, 1 negative.
   * @param e initial exponent
   * @return closest half-even rounded double to 
   * (-1)^s * (2^e) * n / d.
   */

  private static final double divide4 (final BigInteger n,
                                       final BigInteger d,
                                       final int s,
                                       final int e) {
    assert n.signum() == 1;
    assert d.signum() == 1;
    assert (s == 0) || (s == 1);

    System.out.println();
    System.out.println("divide4(BigInteger,BigInteger,int,int)");
    System.out.println(description("n",n));
    System.out.println(description("d",d));
    System.out.println("s= " + s + ", e= " + e);

    final BigInteger[] qr = n.divideAndRemainder(d);
    final BigInteger q = qr[0];
    final BigInteger r = qr[1];

    final double z = divide5(n,d,s,e,q,r); 

    final BigInteger n1 = q.multiply(d).add(r);
    final double ze = ToDouble(n1,d,s,e);
    assert ze == z :
      "\n" 
      + Double.toHexString(ze) + " :E\n"
      + Double.toHexString(z); 

    return z; }

  //--------------------------------------------------------------
  /** Shift numerator right so that it is at least 
   * 2^({@link Doubles#SIGNIFICAND_BITS} + 1) times the 
   * denominator, 
   * up to the point that the exponent is 
   * &ge; {@link Doubles#MINIMUM_EXPONENT}
   * (where subnormal numbers start).
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
                                       final int s,
                                       final int e) {
    assert n.signum() == 1;
    assert d.signum() == 1;
    assert (s == 0) || (s == 1);

    //    System.out.println();
    //    System.out.println("divide3(BigInteger,BigInteger,int,int)");
    //    System.out.println(description("n",n));
    //    System.out.println(description("d",d));
    //    System.out.println("s= " + s + ", e= " + e);

    final int shift0 = 53;
//      Math.max(0,NUMERATOR_BITS + hiBit(d) - hiBit(n));

        System.out.println("shift0= " + shift0);

    final int shift1 = Math.min(
      shift0,
      e - MINIMUM_EXPONENT + SIGNIFICAND_BITS - 1);
    final int e1 = e - shift1;
    //assert e1 >= MINIMUM_EXPONENT + SIGNIFICAND_BITS;
    final BigInteger n1 = n.shiftLeft(shift1);

        System.out.println("e1=" + e1 + "\nshift1=" + shift1);
        System.out.println(description("n",n));

    if (n1.signum() == 0) { 
      if (s == 1) { return -0.0; }
      return 0.0; }

    final double z = divide4(n1,d,s,e1); 

    final double ze = ToDouble(n1,d,s,e1);
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
                                       int s) {
    System.out.println();
    System.out.println("divide2(BigInteger,BigInteger,int)");
    System.out.println(description("n",n));
    System.out.println(description("d",d));
    System.out.println("s= " + s);

    assert n.signum() == 1;
    assert d.signum() == 1;
    assert (s == 0) || (s == 1);

    final int en = loBit(n);
    final int ed = loBit(d);
    final int e = en-ed;

    //    System.out.println("ed-en= " + en + "-" + ed + "=" + e);

    final BigInteger n1 = n.shiftRight(en);
    final BigInteger d1 = d.shiftRight(ed);
    final double z = divide3(n1,d1,s,e); 

    final double ze = ToDouble(n1,d1,s,e);
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
                                       int s) {
    //    System.out.println();
    //    System.out.println("divide1(BigInteger,BigInteger,int)");
    //    System.out.println(description("n",n));
    //    System.out.println(description("d",d));
    //    System.out.println("s= " + s);

    assert n.signum() == 1;
    assert d.signum() == 1;
    assert (s == 0) || (s == 1);

    final BigInteger gcd = n.gcd(d);
    final BigInteger n0,d0;
    if (BigInteger.ONE.equals(gcd)) { n0 = n; d0 = d; }
    else { n0 = n.divide(gcd); d0 = d.divide(gcd); }

    System.out.println("gcd=" + gcd);

    final double z = divide2(n0,d0,s);

    final double ze0 = ToDouble(n,d,s);
    final double ze1 = ToDouble(n0,d0,s);
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
    System.out.println();
    System.out.println("divide0(BigInteger,BigInteger)");
    System.out.println(description("n",n));
    System.out.println(description("d",d));

    assert d.signum() == 1;
    final int c = n.signum();
    if (c == 0) { return 0.0; }
    if (c == -1) { return divide1(n.negate(),d,1); }
    if (c == 1) { return divide1(n,d,0); }
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
      final double z = divide(n,d);
      final double ze = ToDouble(n,d);
      assert z == ze : i; } }

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
      final double z = divide(n,d);
      final double ze = ToDouble(n,d);
      assert z == ze : 
        i + "\n"
        + description("n",n) + "\n"
        + description("d",d) + "\n"
        + Double.toHexString(z)  + "\n"
        + Double.toHexString(ze)  + " :E\n";
    } }

  public static final void finiteDoubleRoundingTest () {
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

  public static final void normalDoubleRoundingTest () {
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

  public static final void subnormalDoubleRoundingTest () {
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

  //--------------------------------------------------------------

  public static final double roundingTest (final BigInteger n,
                                           final BigInteger d) {
    final double ze = ToDouble(n,d);
    System.out.println(Double.toHexString(ze) + " :E");
    final double z = divide(n,d);
    System.out.println(Double.toHexString(z) + " :D");
    assert ze == z : 
      "\n" 
      + Double.toHexString(ze) + " :E\n"
      + Double.toHexString(z); 
    return z; } 

  public static final double roundingTest (final long n,
                                           final long d) {
    final double z = roundingTest(
      BigInteger.valueOf(n),
      BigInteger.valueOf(d));
    return z; } 

  public static final double roundingTest (final double x) {
    //    System.out.println();
    //    System.out.println("roundingTest(" 
    //      + Double.toHexString(x) + ")");
    //    System.out.println("signBit=" + signBit(x));
    //    System.out.println("significand=" 
    //      + Long.toHexString(fullSignificand(x)));
    //    System.out.println("significand=" 
    //      + Long.toBinaryString(fullSignificand(x)));
    //    System.out.println("significand=" 
    //      + Long.toBinaryString(SIGNIFICAND_MASK));
    //    System.out.println("unbiasedExp=" 
    //      + Doubles.unbiasedExponent(x));
    final BigInteger[] nd = RationalSum.toRatio(x);
    final BigInteger n = nd[0];
    final BigInteger d = nd[1];
    final double z = roundingTest(n,d);
    assert z == x : 
      "E:\n" 
      + Double.toHexString(x) + "\n"
      + Double.toHexString(z); 
    return z; } 

  public static final void main (final String[] args) {
    final BigInteger q6 = BigInteger.valueOf(0x130eb6938c0156L);
    final BigInteger q7 = BigInteger.valueOf(0x130eb6938c0157L);
    final BigInteger q8 = BigInteger.valueOf(0x130eb6938c0158L);
    final BigInteger n = BigInteger.valueOf(0x789f09858446ad92L).shiftLeft(52);
    final BigInteger d = BigInteger.valueOf(0x19513ea5d70c32eL).shiftLeft(6);
    final BigInteger dr6 = d.multiply(q6);
    final BigInteger dr7 = d.multiply(q7);
    final BigInteger dr8 = d.multiply(q8);
    final BigInteger nmdr6 = n.subtract(dr6);
    final BigInteger nmdr7 = n.subtract(dr7);
    final BigInteger nmdr8 = n.subtract(dr8);
    System.out.println("n: " + n.toString(0x10));
    System.out.println("d: " + d.toString(0x10));
    System.out.println("6: " + nmdr6.toString(0x10));
    System.out.println("7: " + nmdr7.toString(0x10));
    System.out.println("8: " + nmdr8.toString(0x10));
    System.out.println(nmdr6.abs().compareTo(nmdr7.abs()));
    System.out.println();
    roundingTest(n.shiftRight(52),d);
    //    roundingTest(13L,3L);
    //    roundingTest(0x1.30eb6938c0156p6);
    //    roundingTest(0x1.30eb6938c0157p6);
    //    roundingTest(
    //      (0x789f09858446ad92L >>> 1) + 100L,
    //      (0x19513ea5d70c32eL << 5));
    //    roundingTest(
    //      (0x789f09858446ad92L >>> 1) + 10L,
    //      (0x19513ea5d70c32eL >>> 1));
    //roundingTest(0x789f09858446ad92L,0x19513ea5d70c32eL);
    //        roundingTest(0x0.0000000000001p-1022);
    //        roundingTest(0x0.1000000000001p-1022);
    //        roundingTest(0x0.1000000000000p-1022);
    //        roundingTest(0x1.0000000000001p-1022);
    //        roundingTest(0x1.0000000000000p-1022);
    //        roundingTest(0x0.0000000000001p-1022);
    //        roundingTest(0x0.033878c4999b7p-1022);
    //        roundingTest(0x1.33878c4999b6ap-1022);
    //        roundingTest(-0x1.76c4ebe6d57c8p-924);
    //        roundingTest(0x1.76c4ebe6d57c8p-924);
    //        roundingTest(-0x1.76c4ebe6d57c8p924);
    //        roundingTest(0x1.76c4ebe6d57c8p924);
    //        roundingTest(1L,3L);
    //        subnormalDoubleRoundingTest();
    //        normalDoubleRoundingTest();
    //        finiteDoubleRoundingTest(); 
    //    fromLongsRoundingTest();
    //    fromBigIntegersRoundingTest(); 
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
