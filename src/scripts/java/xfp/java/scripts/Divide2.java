package xfp.java.scripts;

import static xfp.java.numbers.Doubles.MINIMUM_EXPONENT;
import static xfp.java.numbers.Doubles.SIGNIFICAND_BITS;
import static xfp.java.numbers.Doubles.SIGNIFICAND_MASK;

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
public final class Divide2 {

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
    System.out.println("s=" + Integer.toHexString(s));
    System.out.println("t=" + Long.toHexString(t));
    System.out.println("e=" + Integer.toHexString(e));
    // TODO: NaN and Infinity
    final String ss = (s < 0) ? "-" : "";
    final String b = (MINIMUM_EXPONENT == e) ? "0" : "1";
    return 
      ss + "0x" 
      + b + "." 
      + Long.toHexString(t & SIGNIFICAND_MASK) + "p" 
      + Integer.toString(e); }

  //--------------------------------------------------------------

  private static final double divide (final BigInteger nn,
                                      final BigInteger d0) {

    final BigInteger n0;
    final int s;
    final int ns = nn.signum();
    if (ns == 0) { return 0.0; }
    else if (ns < 0) { s = 1; n0 = nn.negate(); }
    else { s = 0; n0 = nn; }
//    System.out.println();
//    System.out.println("-1^" + s + "*" 
//    + n0.toString(0x10) + "/" + d0.toString(0x10));
    
    final long nb = n0.bitLength();
    final long db = d0.bitLength();
    final int shift = 0;//(int) (nb - db + 1L);
    System.out.println();
    System.out.println("nb,db=" + nb + ", " + db + " : " + shift);
    final BigInteger n1 = n0.shiftLeft(shift+SIGNIFICAND_BITS);
    final BigInteger d1 = d0;//.shiftLeft(SIGNIFICAND_BITS);
//    System.out.println("-1^" + s + "*" 
//    + n1.toString(0x10) + "/" + d1.toString(0x10));

    final BigInteger[] qr = n1.divideAndRemainder(d1);
    final BigInteger q = qr[0];
    final long ql = q.longValue();
    System.out.println("ql=" + Long.toHexString(ql));
    System.out.println("zeros:" + Long.numberOfLeadingZeros(ql));
    final int de = Doubles.EXPONENT_BITS - Long.numberOfLeadingZeros(ql);
    final int e = (0L==ql) ? MINIMUM_EXPONENT : de;
    final long qq = ql << -de;
//    final BigInteger r = qr[1];
//    System.out.println(q.toString(0x10) + " + " 
//      + r.toString(0x10) + "/" + d1.toString(0x10));
    final long t = qq & SIGNIFICAND_MASK;
    //System.out.println("t=" + Double.toHexString(t));
    //System.out.println(toHexString(s,t,e));
    System.out.println(Double.toHexString(ToDouble(n0,d0)) + " :E");
    final double z = Doubles.makeDouble(s,t,e);
    System.out.println(Double.toHexString(z));
    return z; }

  private static final double divide (final long n,
                                      final long d) {
    return divide(BigInteger.valueOf(n),BigInteger.valueOf(d));  }

  //--------------------------------------------------------------

  public static final void main (final String[] args) {
    divide(1L,3L);
    divide(1L,1023L*1023L);
//    divide(4L,3L);
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
