package xfp.java.scripts;

import static xfp.java.numbers.Doubles.MINIMUM_EXPONENT;
import static xfp.java.numbers.Doubles.SIGNIFICAND_BITS;
import static xfp.java.numbers.Doubles.SIGNIFICAND_MASK;
import static xfp.java.numbers.Doubles.makeDouble;

import java.math.BigInteger;

import com.upokecenter.numbers.EContext;
import com.upokecenter.numbers.EFloat;
import com.upokecenter.numbers.EInteger;

import xfp.java.numbers.Doubles;

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
public final class Divide3 {

  //--------------------------------------------------------------

  private static final double ToDouble (final BigInteger n,
                                        final BigInteger d) {
    final EInteger ni = EInteger.FromBytes(n.toByteArray(), false);
    final EInteger di = EInteger.FromBytes(d.toByteArray(), false);
    final EFloat nf = EFloat.FromEInteger(ni); 
    final EFloat df = EFloat.FromEInteger(di); 
    final EFloat f = nf.Divide(df, EContext.Binary64);
    return f.ToDouble(); }

  //--------------------------------------------------------------

  private static final String toHexString (final int s,
                                           final long t,
                                           final int e) {
    System.out.println("s= " + Integer.toHexString(s));
    System.out.println("t= " + Long.toHexString(t));
    System.out.println("e= " + Integer.toHexString(e));
    // TODO: NaN and Infinity
    final String ss = (s < 0) ? "-" : "";
    final String b = (MINIMUM_EXPONENT == e) ? "0" : "1";
    return 
      ss + "0x" 
      + b + "." 
      + Long.toHexString(t & SIGNIFICAND_MASK) + "p" 
      + Integer.toString(e); }

  //--------------------------------------------------------------

  private static final int iabsmin (final BigInteger a,
                                    final BigInteger b,
                                    final BigInteger c) {
    final int ab = a.compareTo(b);
    if (ab <= 0) {
      final int ac = a.compareTo(c);
      if (ac <= 0) { return 0; }
      return 2; }
    final int bc = b.compareTo(c);
    if (bc <= 0) { return 1; }
    return 2; }

  //--------------------------------------------------------------

  private static final double divide (final BigInteger n,
                                      final BigInteger d) {

    final BigInteger np; // positive numerator
    final int s;
    final int c = n.signum();
    if (c == 0) { return 0.0; }
    else if (c < 0) { s = 1; np = n.negate(); }
    else { s = 0; np = n; }
    //    System.out.println();
    //    System.out.println("-1^" + s + "*" 
    //    + n0.toString(0x10) + "/" + d0.toString(0x10));

    final long nb = np.bitLength();
    final long db = d.bitLength();
    final int shift = (int) (db - nb + SIGNIFICAND_BITS + 1);
    final BigInteger ns = np.shiftLeft(shift); 

    final BigInteger[] qr = ns.divideAndRemainder(d);
    final BigInteger q = qr[0];
    final BigInteger qd = q.multiply(d);
    final BigInteger nmqd = ns.subtract(qd);
    final BigInteger nmqdpd = nmqd.add(d);
    final BigInteger nmqdmd = nmqd.subtract(d);
    final int iround = iabsmin(nmqdmd.abs(),nmqd,nmqdpd);
    final long ql = q.longValue() & SIGNIFICAND_MASK;
    final long t; 
    switch (iround) {
    case 0 : t = ql + 1L; break; 
    case 1 : t = ql; break; 
    case 2 : t = ql -1L; break;
    default : throw new RuntimeException("invalid iround= " + iround); }
    final double z = makeDouble(s,t,SIGNIFICAND_BITS-shift);
    final double ze = ToDouble(np,d);
    if ((iround == 2)
      || (nmqd.compareTo(BigInteger.ZERO) < 0)
      ||
      (nmqdmd.compareTo(BigInteger.ZERO) > 0)
      || 
      (z != ze)) {
      System.out.println();
      System.out.println("np = " + np.toString(0x10));
      System.out.println("d  = " + d.toString(0x10));
      System.out.println("nb,db= " + nb + ", " + db + " : " + shift);
      System.out.println("ns = " + ns.toString(0x10));
      System.out.println("q  = " + q.toString(0x10));
      System.out.println("qd = " + qd.toString(0x10));
      System.out.println("nmqdm1 = " + nmqdmd.toString(0x10));
      System.out.println("nmqd   = " + nmqd.toString(0x10));
      System.out.println("nmqdp1 = " + nmqdpd.toString(0x10));
      System.out.println("iround = " + iround);
      System.out.println("ql = " + ql);
      System.out.println("t  = " + t);
      System.out.println(Double.toHexString(ze) + " :E");
      System.out.println(Double.toHexString(z));
    }
    return z; }

  private static final double divide (final long n,
                                      final long d) {
    return divide(BigInteger.valueOf(n),BigInteger.valueOf(d));  }

  //--------------------------------------------------------------

  public static final void main (final String[] args) {
    for (long i=3L;i<23L;i++) { divide(1L,i); }
    //    divide(1L,1023L*1023L);
    //    divide(4L,3L);
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
