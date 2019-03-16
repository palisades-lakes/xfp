package xfp.java.scripts;

import static xfp.java.numbers.Doubles.SIGNIFICAND_BITS;
import static xfp.java.numbers.Doubles.SIGNIFICAND_MASK;
import static xfp.java.numbers.Doubles.fullSignificand;
import static xfp.java.numbers.Doubles.signBit;

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
 * @version 2019-03-15
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
    + "\n"
    + "lo,hi bits= [" + 
    loBit(i) + "," + hiBit(i) + ")"; }

  private static final String description (final String name,
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
    System.out.println();
    System.out.println("ToDouble(BigInteger,BigInteger,int,int)");
    System.out.println(description("n",n));
    System.out.println(description("d",d));
    System.out.println("-> " + Double.toHexString(ze));
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

  private static final double ToDouble (final long q,
                                        final int s,
                                        final int e) {
    return ToDouble(BigInteger.valueOf(q),s,e); }

  private static final double ToDouble (final long n,
                                        final long d) {
    return ToDouble(
      BigInteger.valueOf(n),
      BigInteger.valueOf(d)); }

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
    assert q > 1;
    assert (s == 0) || (s == 1);

    System.out.println();
    System.out.println("divide7(long,int,int)");
    System.out.println(description("q",q));
    System.out.println("s= " + s);
    System.out.println("e= " + e);

    assert hiBit(q) <= SIGNIFICAND_BITS + 1;
    final double z = // losing the high bit, if set
      Doubles.makeDouble(s,q & SIGNIFICAND_MASK,e+SIGNIFICAND_BITS);
    final double ze = ToDouble(q,s,e);
    assert ze == z :
      "\n" 
      + Double.toHexString(ze) + " :E\n"
      + Double.toHexString(z); 
    return z; }

  //--------------------------------------------------------------
  /** Handle over and under flow (too many/few bits in q).
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

    System.out.println();
    System.out.println("divide6(BigInteger,int,int)");
    System.out.println(description("q",q));
    System.out.println("s= " + s);
    System.out.println("e= " + e);

    final BigInteger qq;
    final int ee;
    if (hiBit(q) <= SIGNIFICAND_BITS) { qq = q; ee =e; }
    else { qq = q.shiftRight(1); ee = e+1; }
    final double z = divide7(qq.longValue(),s,ee); 
    final double ze = ToDouble(q,s,e);
    assert ze == z :
      "\n" 
      + Double.toHexString(ze) + " :E\n"
      + Double.toHexString(z); 
    return z; }

  //--------------------------------------------------------------
  /** Is q or q+1 closer to n/d?.
   * 
   * TODO: Ought to be able to determine this without
   * forming all the intermediate BigIntegers.
   * 
   * @param n positive numerator
   * @param d positive denominator
   * @param s sign bit: 0 positive, 1 negative.
   * @param e initial exponent
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
    assert 
    hiBit(n) - hiBit(d) > SIGNIFICAND_BITS :
      hiBit(n) + " - " + hiBit(d) 
      + " = " + (hiBit(n) - hiBit(d))
      + " <= " + SIGNIFICAND_BITS;

    System.out.println();
    System.out.println("divide5(BigInteger,BigInteger,int,int)");
    System.out.println(description("n",n));
    System.out.println(description("d",d));
    System.out.println("s= " + s);
    System.out.println("e= " + e);
    System.out.println(description("q",q));
    System.out.println(description("r",r));

    final BigInteger down = n.subtract(q.multiply(d));
    final BigInteger up = d.subtract(down);
    System.out.println(description("down",down));
    System.out.println(description("up",up));
    assert down.signum() >= 0;
    assert up.signum() >= 0;

    final BigInteger qq = 
      (down.compareTo(up) <= 0) ? q : q.add(BigInteger.ONE);
    final double z = divide6(qq,s,e);
    final double ze = ToDouble(qq,s,e);
    assert ze == z :
      "\n" 
      + Double.toHexString(ze) + " :E\n"
      + Double.toHexString(z); 
    return z; }

  //--------------------------------------------------------------
  /** Divide numerator by denominator to get quotient and
   * remainder..
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
    assert 
    hiBit(n) - hiBit(d) > SIGNIFICAND_BITS :
      hiBit(n) + " - " + hiBit(d) 
      + " = " + (hiBit(n) - hiBit(d))
      + " <= " + SIGNIFICAND_BITS;

    System.out.println();
    System.out.println("divide4(BigInteger,BigInteger,int,int)");
    System.out.println(description("n",n));
    System.out.println(description("d",d));
    System.out.println("s= " + s);
    System.out.println("e= " + e);

    final BigInteger[] qr = n.divideAndRemainder(d);
    final BigInteger q = qr[0];
    final BigInteger r = qr[1];

    final double z = divide5(n,d,s,e,q,r); 
    //final double ze = ToDouble(n,d,s,e);
    //assert ze == z :
    //  "\n" 
    //  + Double.toHexString(ze) + " :E\n"
    //  + Double.toHexString(z); 
    return z; }

  /** Shift numerator right so that it is at least 
   * 2^({@link Doubles#SIGNIFICAND_BITS} + 1) times the 
   * denominator.
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

    System.out.println();
    System.out.println("divide3(BigInteger,BigInteger,int,int)");
    System.out.println(description("n",n));
    System.out.println(description("d",d));
    System.out.println("s= " + s);
    System.out.println("e= " + e);

    final int shift = Math.max(
      0,
      SIGNIFICAND_BITS + 1 + hiBit(d) - hiBit(n));
    System.out.println("shift= " + shift);

    final double z = divide4(n.shiftLeft(shift),d,s,e-shift); 
    //    final double ze = ToDouble(n,d,s,e);
    //    assert ze == z :
    //      "\n" 
    //      + Double.toHexString(ze) + " :E\n"
    //      + Double.toHexString(z); 
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
    System.out.println("ed-en= " + e);

    final double z = divide3(n.shiftRight(en),d.shiftRight(ed),s,e); 
    //    final double ze = ToDouble(n,d,s);
    //    assert ze == z :
    //      "\n" 
    //      + Double.toHexString(ze) + " :E\n"
    //      + Double.toHexString(z); 
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
    System.out.println();
    System.out.println("divide1(BigInteger,BigInteger,int)");
    System.out.println(description("n",n));
    System.out.println(description("d",d));
    System.out.println("s= " + s);

    assert n.signum() == 1;
    assert d.signum() == 1;
    assert (s == 0) || (s == 1);

    final BigInteger gcd = n.gcd(d);
    //    System.out.println("gcd=" + gcd);
    final BigInteger n0,d0;
    if (BigInteger.ONE.equals(gcd)) { n0 = n; d0 = d; }
    else { n0 = n.divide(gcd); d0 = d.divide(gcd); }

    final double z = divide2(n0,d0,s);
    //    final double ze = ToDouble(n,d,s);
    //    assert ze == z :
    //      "\n" 
    //      + Double.toHexString(ze) + " :E\n"
    //      + Double.toHexString(z); 
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
      assert z == ze : i; } }

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
    final double z = divide(n,d);
    final double ze = ToDouble(n,d);
    assert ze == z : 
      "\n" 
      + Double.toHexString(ze) + "\n"
      + Double.toHexString(z); 
    return z; } 

  public static final double roundingTest (final long n,
                                           final long d) {
    return roundingTest(
      BigInteger.valueOf(n),
      BigInteger.valueOf(d)); } 

  public static final double roundingTest (final double x) {
    System.out.println();
    System.out.println("roundingTest(" 
      + Double.toHexString(x) + ")");
    System.out.println("signBit=" + signBit(x));
    System.out.println("significand=" 
      + Long.toHexString(fullSignificand(x)));
    System.out.println("significand=" 
      + Long.toBinaryString(fullSignificand(x)));
    System.out.println("significand=" 
      + Long.toBinaryString(SIGNIFICAND_MASK));
    System.out.println("unbiasedExp=" 
      + Doubles.unbiasedExponent(x));
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
    roundingTest(0x0.033878c4999b6ap-1022);
    //roundingTest(0x1.33878c4999b6ap-1022);
    //roundingTest(-0x1.76c4ebe6d57c8p-924);
    //roundingTest(0x1.76c4ebe6d57c8p-924);
    //roundingTest(-0x1.76c4ebe6d57c8p924);
    //roundingTest(0x1.76c4ebe6d57c8p924);
    //roundingTest(1L,3L);
    //subnormalDoubleRoundingTest();
    //    normalDoubleRoundingTest();
    //    finiteDoubleRoundingTest(); 
    //    fromLongsRoundingTest();
    //    fromBigIntegersRoundingTest(); 
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
