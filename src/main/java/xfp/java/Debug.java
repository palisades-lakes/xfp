package xfp.java;

import java.io.PrintStream;
import java.math.BigInteger;

import com.upokecenter.numbers.EContext;
import com.upokecenter.numbers.EFloat;
import com.upokecenter.numbers.EInteger;

/** Debugging output.
 * Hacky substitute for mess of dependencies and
 * configuration needed by java logging libraries.
 * Intended only for use during development;
 * no Debug.* references should persist in 'production' code.
 *
 * Static methods only; no state.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-25
 */

public final class Debug {

  public static boolean DEBUG = false;

  public static PrintStream OUT = System.out;

   //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------
  /** Hex string representation of the bits 
   * implementing the double. Not at all the same as 
   * {@link Double#toHexString}.
   */

  public static final String hexBits (final double x) {
    return 
      Long.toHexString(Double.doubleToLongBits(x))
      .toUpperCase(); }

  //--------------------------------------------------------------

  public static final void println () {
    if (DEBUG) { OUT.println(); } }

  public static final void println (final String msg) {
    if (DEBUG) { OUT.println(msg); } }

  //--------------------------------------------------------------

  public static final void printf (final String format,
                                   final Object... args) {
    if (DEBUG) { OUT.printf(format,args); } }

  //--------------------------------------------------------------

  public static final void printf (final String format,
                                   final boolean arg) {
    if (DEBUG) { OUT.printf(format,Boolean.valueOf(arg)); } }

  //--------------------------------------------------------------

  public static final void printf (final String format,
                                   final int arg) {
    if (DEBUG) { OUT.printf(format,Integer.valueOf(arg)); } }

  //--------------------------------------------------------------

  public static final void printf (final String format,
                                   final double arg) {
    if (DEBUG) { OUT.printf(format,Double.valueOf(arg)); } }

  //--------------------------------------------------------------

  public static final void printf (final String format,
                                   final double arg0,
                                   final double arg1) {
    if (DEBUG) { 
      OUT.printf(format, 
        Double.valueOf(arg0), Double.valueOf(arg1)); } }

  //--------------------------------------------------------------
  // for comparing to numbers-java results
  //--------------------------------------------------------------

  public static final double ToDouble (final long n,
                                       final long d) {
    return ToDouble(
      BigInteger.valueOf(n),
      BigInteger.valueOf(d)); }

    public static final double ToDouble (final boolean negative,
                                       final int e,
                                       final long q) {
    return ToDouble(negative,e,BigInteger.valueOf(q)); }

    public static final double ToDouble (final boolean negative,
                                       final int e,
                                       final BigInteger q) {
    return ToDouble(negative,e,q,BigInteger.ONE); }

    public static final double ToDouble (final boolean negative,
                                       final int e,
                                       final BigInteger n,
                                       final BigInteger d) {
    if (e < 0) { return ToDouble(negative,n,d.shiftLeft(-e)); }
    if (e > 0) { return ToDouble(negative,n.shiftLeft(e),d); }
    return ToDouble(negative,n,d); }

    public static final double ToDouble (final boolean negative,
                                       final BigInteger n,
                                       final BigInteger d) {
    return negative ? ToDouble(n.negate(),d) : ToDouble(n,d) ; }

    public static final double ToDouble (final BigInteger n,
                                       final BigInteger d) {
    
    final EInteger ni = EInteger.FromBytes(n.toByteArray(), false);
    final EInteger di = EInteger.FromBytes(d.toByteArray(), false);
    final EFloat nf = EFloat.FromEInteger(ni); 
    final EFloat df = EFloat.FromEInteger(di); 
    final EFloat f = nf.Divide(df, EContext.Binary64);
    final double ze = f.ToDouble(); 
    //    Debug.println();
    //    Debug.println("ToDouble(BigInteger,BigInteger,int,int)");
    //    Debug.println(description("n",n));
    //    Debug.println(description("d",d));
    //    Debug.println("-> " + Double.toHexString(ze));
    return ze;}

    public static final float ToFloat (final long n,
                                       final long d) {
      return ToFloat(
        BigInteger.valueOf(n),
        BigInteger.valueOf(d)); }

      public static final float ToFloat (final boolean negative,
                                       final int e,
                                       final long q) {
      return ToFloat(negative,e,BigInteger.valueOf(q)); }

      public static final float ToFloat (final boolean negative,
                                        final int e,
                                        final BigInteger q) {
      return ToFloat(negative,e,q,BigInteger.ONE); }

      public static final float ToFloat (final boolean negative,
                                        final int e,
                                        final BigInteger n,
                                        final BigInteger d) {
      if (e < 0) { return ToFloat(negative,n,d.shiftLeft(-e)); }
      if (e > 0) { return ToFloat(negative,n.shiftLeft(e),d); }
      return ToFloat(negative,n,d); }

      public static final float ToFloat (final boolean negative,
                                        final BigInteger n,
                                        final BigInteger d) {
      return negative ? ToFloat(n.negate(),d) : ToFloat(n,d) ; }

      public static final float ToFloat (final BigInteger n,
                                        final BigInteger d) {
      
      final EInteger ni = EInteger.FromBytes(n.toByteArray(), false);
      final EInteger di = EInteger.FromBytes(d.toByteArray(), false);
      final EFloat nf = EFloat.FromEInteger(ni); 
      final EFloat df = EFloat.FromEInteger(di); 
      final EFloat f = nf.Divide(df, EContext.Binary32);
      final float ze = f.ToSingle(); 
      //    Debug.println();
      //    Debug.println("ToFloat(BigInteger,BigInteger,int,int)");
      //    Debug.println(description("n",n));
      //    Debug.println(description("d",d));
      //    Debug.println("-> " + Float.toHexString(ze));
      return ze;}

  //--------------------------------------------------------------
  // disable constructor
  //--------------------------------------------------------------

  private Debug () {
    throw new UnsupportedOperationException(
      "can't instantiate " + getClass()); }


  //--------------------------------------------------------------
}
//--------------------------------------------------------------
