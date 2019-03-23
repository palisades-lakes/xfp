package xfp.java.scripts;

import static java.lang.Float.MAX_EXPONENT;
import static java.lang.Float.MIN_EXPONENT;
import static java.lang.Float.NEGATIVE_INFINITY;
import static java.lang.Float.POSITIVE_INFINITY;
import static xfp.java.numbers.Floats.MINIMUM_SUBNORMAL_EXPONENT;
import static xfp.java.numbers.Floats.STORED_SIGNIFICAND_BITS;

import java.math.BigInteger;

import com.upokecenter.numbers.EContext;
import com.upokecenter.numbers.EFloat;
import com.upokecenter.numbers.EInteger;

import xfp.java.numbers.Floats;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;
import xfp.java.prng.PRNG;

/** BigInteger divide and round to float.
 * 
 * <pre>
 * j --source 11 src/scripts/java/xfp/java/scripts/DivideFloat.java > divide.txt 2>&1
 * </pre>
 * Profiling:
 * <pre>
 * jy --source 11 src/scripts/java/xfp/java/scripts/DivideFloat.java
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-22
 */

@SuppressWarnings("unchecked")
public final class DivideFloat {

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

  private static final float ToFloat (final BigInteger n,
                                      final BigInteger d) {

    final EInteger ni = EInteger.FromBytes(n.toByteArray(), false);
    final EInteger di = EInteger.FromBytes(d.toByteArray(), false);
    final EFloat nf = EFloat.FromEInteger(ni); 
    final EFloat df = EFloat.FromEInteger(di); 
    final EFloat f = nf.Divide(df, EContext.Binary32);
    final float ze = f.ToSingle(); 
    //    debug();
    //    debug("ToFloat(BigInteger,BigInteger,int,int)");
    //    debug(description("n",n));
    //    debug(description("d",d));
    //    debug("-> " + Float.toHexString(ze));
    return ze;}

  private static final float ToFloat (final boolean negative,
                                      final BigInteger n,
                                      final BigInteger d) {
    return negative ? ToFloat(n.negate(),d) : ToFloat(n,d) ; }

  private static final float ToFloat (final boolean negative,
                                      final int e,
                                      final BigInteger n,
                                      final BigInteger d) {
    if (e < 0) { return ToFloat(negative,n,d.shiftLeft(-e)); }
    if (e > 0) { return ToFloat(negative,n.shiftLeft(e),d); }
    return ToFloat(negative,n,d); }

  private static final float ToFloat (final boolean negative,
                                      final int e,
                                      final BigInteger q) {
    return ToFloat(negative,e,q,BigInteger.ONE); }

  public static final float ToFloat (final boolean negative,
                                     final int e,
                                     final long q) {
    return ToFloat(negative,e,BigInteger.valueOf(q)); }

  public static final float ToFloat (final long n,
                                     final long d) {
    return ToFloat(
      BigInteger.valueOf(n),
      BigInteger.valueOf(d)); }

  //--------------------------------------------------------------

  private static final int NUMERATOR_BITS = STORED_SIGNIFICAND_BITS + 1;

  /** Inclusive lower bound on exponents for rounding to float.
   */
  private static final int MINIMUM_EXPONENT =
    Float.MIN_EXPONENT - Floats.STORED_SIGNIFICAND_BITS;

  /** Exclusive upper bound on exponents for rounding to float.
   */
  private static final int MAXIMUM_EXPONENT =
    Float.MAX_EXPONENT - Floats.STORED_SIGNIFICAND_BITS + 1;

  //--------------------------------------------------------------
  /** Handle over and under flow (too many/few bits in q).
   * @param subnormal 
   * @param e7 exponent
   * @param q7 rounded(n/d)
   * @param s sign bit: 0 positive, 1 negative.
   * @return (-1)^s * (2^e) * q.
   */

  private static final float divide7 (final boolean negative,
                                      final boolean subnormal,
                                      final int e7, 
                                      final int q7) {
    debug();
    debug("divide7(long,int,int)");
    debug(description("q7",q7));
    debug("negative= " + negative + ", e7= " + e7
      + ", subnormal=" + subnormal);
    if (subnormal) {
      assert e7 == MINIMUM_EXPONENT;
      assert 0L < q7;
      assert q7 < (1L << STORED_SIGNIFICAND_BITS); }
    else {
      assert MINIMUM_EXPONENT <= e7 : Integer.toString(e7);
      assert e7 < MAXIMUM_EXPONENT : Integer.toString(e7);
      assert (1L << STORED_SIGNIFICAND_BITS) <= q7 
        : description("q7",q7); 
      assert q7 <= (1L << (STORED_SIGNIFICAND_BITS+1)); }

    final int e70 = subnormal ? (e7 - 1) : e7;

    if (subnormal) { assert e70 == MINIMUM_EXPONENT - 1 : e70; }
    else {
      assert MINIMUM_EXPONENT <= e70 : Integer.toString(e70);
      assert e70 < MAXIMUM_EXPONENT : Integer.toString(e70); }

    final float z = Floats.makeFloat(negative,e70,q7);

    final float ze = ToFloat(negative,e7,q7);
    assert ze == z :
      "\n" 
      + Float.toHexString(ze) + " :E\n"
      + Float.toHexString(z); 

    return z; }

  //--------------------------------------------------------------
  /** Handle carry (too many bits in q).
   * @param subnormal TODO
   * @param e6 exponent
   * @param q6 rounded(n/d)
   * @param s sign bit: 0 positive, 1 negative.
   * @return (-1)^s * (2^e) * q.
   */

  private static final float divide6 (final boolean negative,
                                      final boolean subnormal,
                                      final int e6, 
                                      final BigInteger q6) {
    debug();
    debug("divide6(BigInteger,boolean,int,boolean)");
    debug("negative= " + negative + ", e= " + e6
      + ", subnormal=" + subnormal);
    debug(description("q6",q6));

    assert q6.signum() == 1;
    if (subnormal) {
      assert e6 == MINIMUM_EXPONENT;
      //      assert hiBit(q) <= NUMERATOR_BITS 
      //        : "too many bits:" + description("q",q);
      assert q6.compareTo(TWO_52) <= 0; }
    else {
      assert MINIMUM_EXPONENT <= e6 : Integer.toString(e6);
      assert e6 < MAXIMUM_EXPONENT : Integer.toString(e6);
      assert TWO_52.compareTo(q6) <= 0;
      assert q6.compareTo(TWO_53) < 0; }

    final BigInteger q60;
    final int e60;
    if (hiBit(q6) <= NUMERATOR_BITS) { q60 = q6; e60 = e6; }
    // handle carry
    else { q60 = q6.shiftRight(1); e60 = e6+1; }
    final float z = 
      divide7(negative,subnormal,e60, q60.intValueExact()); 

    final float ze = ToFloat(negative,e60,q60);
    assert ze == z :
      "\n" 
      + Float.toHexString(ze) + " :E\n"
      + Float.toHexString(z); 

    return z; }

  //--------------------------------------------------------------

  private static final BigInteger TWO_52 = 
    BigInteger.ONE.shiftLeft(STORED_SIGNIFICAND_BITS);

  private static final BigInteger TWO_53 = TWO_52.shiftLeft(1);

  /** Is q or (q+1) closer to n/d?.
   * @param negative extracted sign info.
   * @param subnormal is this a subnormal value?
   * @param e5 exponent
   * @param q5 floor(n5/d5)
   * @param r5 d5*((n5/d5) - floor(n5/d5))
   * @param d5 positive denominator
   * @return closest half-even rounded float to 
   * (-1)^s * (2^e) * (q + (r / d).
   */

  private static final float divide5 (final boolean negative,
                                      final boolean subnormal,
                                      final int e5,
                                      final BigInteger q5,
                                      final BigInteger r5,
                                      final BigInteger d5) {
    debug();
    debug("divide6(BigInteger,BigInteger,boolean,int,boolean,BigInteger,BigInteger)");
    debug(description("d5",d5));
    debug("negative= " + negative + ", e= " + e5
      + ", subnormal=" + subnormal);
    debug(description("q5",q5));
    debug(description("r5",r5));

    assert d5.signum() == 1;
    assert q5.signum() == 1;
    assert r5.signum() >= 0;
    assert r5.compareTo(d5) < 0;
    if (subnormal) {
      assert e5 == MINIMUM_EXPONENT;
      assert q5.compareTo(TWO_52) < 0; }
    else {
      assert MINIMUM_EXPONENT <= e5 : Integer.toString(e5);
      assert e5 < MAXIMUM_EXPONENT : Integer.toString(e5);
      // TODO: faster test!!!
      assert TWO_52.compareTo(q5) <= 0;
      assert q5.compareTo(TWO_53) < 0; }

    final BigInteger q50;
    final BigInteger r50 = r5.shiftLeft(1);
    if (r50.compareTo(d5) <= 0) { q50 = q5;  }
    else { q50 = q5.add(BigInteger.ONE); }
    debug(description("q50",q50));

    debug(description("q50",q50));

    final float z = divide6(negative,subnormal,e5,q50);

    final float ze = ToFloat(negative,e5,q50);
    assert ze == z :
      "\n" 
      + Float.toHexString(ze) + " :E0\n"
      + Float.toHexString(z); 

    return z; }

  //--------------------------------------------------------------

  /** Divide numerator by denominator to get quotient and
   * remainder.
   * @param e4 initial exponent
   * @param n4 positive numerator
   * @param d4 positive denominator
   * @param s sign bit: 0 positive, 1 negative.
   * @return closest half-even rounded float to 
   * (-1)^s * (2^e4) * n4 / d4.
   */

  private static final float divide4 (final boolean negative,
                                      final boolean subnormal,
                                      final int e4,
                                      final BigInteger n4,
                                      final BigInteger d4) {
    debug();
    debug("divide5(BigInteger,BigInteger,boolean,int,boolean)");
    debug(description("n4",n4));
    debug(description("d4",d4));
    debug("negative= " + negative + ", e= " + e4
      + ", subnormal=" + subnormal);

    assert n4.signum() == 1;
    assert d4.signum() == 1;
    if (subnormal) {
      assert e4 == MINIMUM_EXPONENT;
      assert TWO_52.compareTo(n4) <= 0; 
      assert n4.compareTo(d4.shiftLeft(STORED_SIGNIFICAND_BITS)) < 0; }
    else {
      assert MINIMUM_EXPONENT <= e4 : Integer.toString(e4);
      assert e4 < MAXIMUM_EXPONENT : Integer.toString(e4);
      assert d4.shiftLeft(STORED_SIGNIFICAND_BITS).compareTo(n4) <= 0;
      // TODO: faster test!!!
      assert n4.compareTo(d4.shiftLeft(STORED_SIGNIFICAND_BITS+1)) < 0; }

    final BigInteger[] qr = n4.divideAndRemainder(d4);
    final BigInteger q4 = qr[0];
    final BigInteger r4 = qr[1];

    final float z = divide5(negative,subnormal,e4,q4,r4,d4); 

    final BigInteger n41 = q4.multiply(d4).add(r4);
    final float ze = ToFloat(negative,e4,n41,d4);
    assert ze == z :
      "\n" 
      + Float.toHexString(ze) + " :E\n"
      + Float.toHexString(z); 

    return z; }

  //--------------------------------------------------------------
  /** Shift <code>n3</code> {@link Floats#STORED_SIGNIFICAND_BITS}
   * left, so the quotient will have enough bits (in the normal
   * case) for the significand ( as as many bits as possible in
   * thje subnormal case).
   * @param e3 exponent
   * @param n3 positive numerator
   * @param d3 positive denominator
   * @param s sign bit: 0 positive, 1 negative.
   * @return closest half-even rounded float to 
   * (-1)^s * (2^e) * n / d.
   */

  private static final float divide3 (final boolean negative,
                                      final boolean subnormal,
                                      final int e3,
                                      final BigInteger n3,
                                      final BigInteger d3) {
    debug();
    debug("divide3(BigInteger,BigInteger,boolean,int,boolean)");
    debug(description("n",n3));
    debug(description("d",d3));
    debug("negative= " + negative + ", e= " + e3
      + ", subnormal=" + subnormal);

    assert n3.signum() == 1;
    assert d3.signum() == 1;
    if (subnormal) {
      assert e3 == MIN_EXPONENT;
      assert n3.compareTo(d3) < 0; }
    else {
      assert MIN_EXPONENT <= e3 : Integer.toString(e3);
      assert e3 <= MAX_EXPONENT : Integer.toString(e3);
      assert d3.compareTo(n3) <= 0;
      // TODO: faster test!!!
      assert n3.compareTo(d3.shiftLeft(1)) < 0; }


    final BigInteger n30 = n3.shiftLeft(STORED_SIGNIFICAND_BITS);
    final int e30 = e3 - STORED_SIGNIFICAND_BITS;

    debug(description("n30",n30));
    debug("e30=" + e30);

    final float z = divide4(negative,subnormal,e30,n30,d3);

    final float ze = ToFloat(negative,e30,n30,d3);
    assert ze == z :
      "\n" 
      + Float.toHexString(ze) + " :E\n"
      + Float.toHexString(z); 

    return z; }

  //--------------------------------------------------------------
  /** Detect zero, subnormal, normal, and infinite values.
   * .
   * @param n2 positive numerator
   * @param d2 positive denominator
   * @param s sign bit: 0 positive, 1 negative.
   * @return closest half-even rounded float to 
   * -1^s * 2<sup>e2</sup> * n / d.
   */

  private static final float divide2 (final boolean negative,
                                      final int e2,
                                      final BigInteger n2,
                                      final BigInteger d2) {
    debug();
    debug("divide2(BigInteger,BigInteger,boolean,int)");
    debug(description("n2",n2));
    debug(description("d2",d2));
    debug("e2=" + e2);
    debug("neg=" + negative);

    assert n2.signum() == 1;
    assert d2.signum() == 1;
    assert d2.compareTo(n2) <= 0;
    // TODO: faster test!!!
    assert n2.compareTo(d2.shiftLeft(1)) < 0;

    final float z;
    if (e2 > MAX_EXPONENT) {
      z = negative ? NEGATIVE_INFINITY : POSITIVE_INFINITY; }
    else if (e2 < MINIMUM_SUBNORMAL_EXPONENT) {
      z = negative ? -0.0F : 0.0F; }
    else if (e2 < MIN_EXPONENT) {
      z = divide3(
        negative,true,
        MIN_EXPONENT,n2,d2.shiftLeft(MIN_EXPONENT-e2)); }
    else {
      z = divide3(negative,false,e2,n2,d2); }


    final float ze = ToFloat(negative,e2,n2,d2);
    assert ze == z :
      "\n" 
      + Float.toHexString(ze) + " :E\n"
      + Float.toHexString(z); 

    return z; }

  //--------------------------------------------------------------
  /** Extract exponent <code>e</code> so that
   * <code> n1 / d1 == 2<sup>e11</sup> * n11 / d12</code>
   * and <code>d12 &le; n11 &lt; 2*d12</code>.
   * @param negative flag
   * @param n1 positive numerator
   * @param d1 positive denominator
   * 
   * @return closest half-even rounded float to -1^s * n / d.
   */

  private static final float divide1 (final boolean negative,
                                      final BigInteger n1,
                                      final BigInteger d1) {
    debug();
    debug("divide1(BigInteger,BigInteger,boolean)");
    debug(description("n1",n1));
    debug(description("d1",d1));
    debug("neg=" + negative);

    assert n1.signum() == 1  : 
      "numernator not strictly positive: " 
      + n1.toString(0x10);
    assert d1.signum() == 1 : 
      "denominator not strictly positive: " 
      + d1.toString(0x10);

    final int n1h = hiBit(n1);
    final int d1h = hiBit(d1);
    final int e10 = n1h - d1h - 1;

    debug("n1h,d1h,e10=" + n1h + "," + d1h + "," + e10);

    final BigInteger n10, d10;
    if (e10 > 0) { n10 = n1; d10 = d1.shiftLeft(e10); }
    else if (e10 == 0) { n10 = n1; d10 = d1; }
    else if (e10 < 0) { n10 = n1.shiftLeft(-e10); d10 = d1; }
    else { throw new RuntimeException("can't get here"); }

    debug(description("n10",n10));
    debug(description("d10",d10));

    // TODO: more efficient test!!!
    final BigInteger d11 = d10.shiftLeft(1);
    final BigInteger d12;
    final int e11;
    if (n10.compareTo(d11) < 0) { d12 = d10; e11 = e10;}
    else { d12 = d11; e11 = e10 + 1; }

    debug(description("n10",n10));
    debug(description("d12",d12));
    debug("e11=" + e11);

    final float z = divide2(negative,e11,n10,d12);

    final float ze = ToFloat(negative,e11,n10,d12);
    assert ze == z :
      "\n" 
      + Float.toHexString(ze) + " :E\n"
      + Float.toHexString(z); 

    return z; }

  //--------------------------------------------------------------
  /** Extract sign bit, so numerator and denominator are both
   * positive. Also reduce by gcd.
   * <p>
   * <em>TODO:</em> measure performance effect of reducing/not
   * reducing?
   * 
   * @param n0 numerator
   * @param d0 positive denominator
   * @return closest half-even rounded <code>float</code> to 
   * exact value of <code>n0 / d0</code>.
   */

  private static final float divide0 (final BigInteger n0,
                                      final BigInteger d0) {
    debug();
    debug("divide0(BigInteger,BigInteger)");
    debug(description("n0",n0));
    debug(description("d0",d0));

    assert d0.signum() == 1 : 
      "denominator not strictly positive: " 
      + d0.toString(0x10);

    // extract sign
    final int n0s = n0.signum();

    debug("n0.signum= " + n0s);

    if (n0s == 0) { return 0.0F; }
    final boolean negative = (n0s < 0);
    final BigInteger n00 = negative ? n0.negate() : n0;

    debug(description("n00",n00));

    // reduce fraction (optional step)
    // TODO: test performance effect
    final BigInteger gcd = n00.gcd(d0);

    debug("gcd=" + gcd);

    final BigInteger n01,d01;
    if (BigInteger.ONE.equals(gcd)) { n01 = n00; d01 = d0; }
    else { n01 = n00.divide(gcd); d01 = d0.divide(gcd); }

    debug(description("n01",n01));
    debug(description("d01",d01));

    final float z = divide1(negative,n01,d01);

    final float ze = ToFloat(negative,n01,d01);

    assert ze == z :
      "\n" 
      + Float.toHexString(ze) + " :E\n"
      + Float.toHexString(z); 

    return z; }

  //--------------------------------------------------------------
  /** Half-even rounding from {@link BigInteger} ratio to 
   * <code>float</code>.
   * @param n numerator
   * @param d positive denominator
   * @return closest half-even rounded float to n / d.
   */

  public static final float divide (final BigInteger n,
                                    final BigInteger d) {
    assert d.signum() == 1 : 
      "denominator not strictly positive: " 
      + d.toString(0x10);

    final float z = divide0(n,d);

    final float ze = ToFloat(n,d);
    assert ze == z :
      "\n" 
      + Float.toHexString(ze) + " :E\n"
      + Float.toHexString(z); 

    return z; }

  /**
   * @param n numerator
   * @param d positive denominator
   * @return closest half-even rounded float n / d.
   */

  public static final float divide (final long n,
                                    final long d) {
    assert d > 0L;
    return divide(BigInteger.valueOf(n),BigInteger.valueOf(d));  }

  //--------------------------------------------------------------

  public static final float roundingTest (final BigInteger n,
                                          final BigInteger d) {
    final float ze = ToFloat(n,d);
    debug(Float.toHexString(ze) + " :E");
    try {
      final float z = divide(n,d);
      debug(Float.toHexString(z) + " :D");
      assert ze == z : 
        "\n" 
        + Float.toHexString(ze) + " :E\n"
        + Float.toHexString(z); 
      return z; } 
    catch (final Throwable t) {
      System.err.println("failed on:");
      System.err.println(description("n",n)); 
      System.err.println(description("d",d)); 
      throw t; } }

  public static final float roundingTest (final long n,
                                          final long d) {
    final float z = roundingTest(
      BigInteger.valueOf(n),
      BigInteger.valueOf(d));
    return z; } 

  public static final float roundingTest (final float x) {
    //    debug();
    //    debug("roundingTest(" 
    //      + Float.toHexString(x) + ")");
    //    debug("signBit=" + signBit(x));
    //    debug("significand=" 
    //      + Long.toHexString(fullSignificand(x)));
    //    debug("significand=" 
    //      + Long.toBinaryString(fullSignificand(x)));
    //    debug("significand=" 
    //      + Long.toBinaryString(SIGNIFICAND_MASK));
    //    debug("unbiasedExp=" 
    //      + Floats.unbiasedExponent(x));
    final BigInteger[] nd = Floats.toRatio(x);
    final BigInteger n = nd[0];
    final BigInteger d = nd[1];
    try {
      final float z = roundingTest(n,d);
      assert z == x : 
        "E:\n" 
        + Float.toHexString(x) + "\n"
        + Float.toHexString(z); 
      return z; } 
    catch (final Throwable t) {
      System.err.println("failed on x= " + Float.toHexString(x)); 
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

  public static final void fromIntsRoundingTest () {
    final Generator gn = 
      Generators.intGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    final Generator gd = 
      Generators.positiveIntGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-07.txt"));
    for (int i=0;i<TRYS;i++) {
      // some ints will not be exactly representable as floats
      final int n = gn.nextInt();
      final int d = gd.nextInt();
      roundingTest(n,d); } }

  public static final void finiteFloatRoundingTest () {
    final Generator g = 
      Floats.finiteGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final float x = g.nextFloat();
      roundingTest(x); } }

  public static final void normalFloatRoundingTest () {
    final Generator g = 
      Floats.normalGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-07.txt"));
    for (int i=0;i<TRYS;i++) {
      final float x = g.nextFloat();
      roundingTest(x); } }

  public static final void subnormalFloatRoundingTest () {
    final Generator g = 
      Floats.subnormalGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-09.txt"));
    for (int i=0;i<TRYS;i++) {
      final float x = g.nextFloat();
      roundingTest(x); } }

  //--------------------------------------------------------------

  public static final void main (final String[] args) {
    final long t = System.nanoTime();
    DEBUG = false;
    // test numbers outside float range
    final BigInteger[] nd = 
      Floats.toRatio(Float.MAX_VALUE);
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
    //    roundingTest(0x0.0000000000001p-1022);
    //    roundingTest(0x0.1000000000001p-1022);
    //    roundingTest(0x0.1000000000000p-1022);
    //    roundingTest(0x1.0000000000001p-1022);
    //    roundingTest(0x1.0000000000000p-1022);
    //    roundingTest(0x0.0000000000001p-1022);
    //    roundingTest(0x0.033878c4999b7p-1022);
    //    roundingTest(0x1.33878c4999b6ap-1022);
    //    roundingTest(-0x1.76c4ebe6d57c8p-924);
    //    roundingTest(0x1.76c4ebe6d57c8p-924);
    //    roundingTest(-0x1.76c4ebe6d57c8p924);
    //    roundingTest(0x1.76c4ebe6d57c8p924);
    //    roundingTest(1L,3L);
    fromIntsRoundingTest();
    normalFloatRoundingTest();
    subnormalFloatRoundingTest();
    //finiteFloatRoundingTest(); 
    fromBigIntegersRoundingTest(); 
    System.out.printf("total secs: %8.2f\n",
      Double.valueOf((System.nanoTime()-t)*1.0e-9)); 
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
