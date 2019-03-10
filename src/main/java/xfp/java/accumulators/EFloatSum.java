package xfp.java.accumulators;

import com.upokecenter.numbers.EFloat;
import com.upokecenter.numbers.EInteger;

/** Naive sum of <code>double</code> values with EFloat 
 * accumulator (for testing).
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-09
 */
public final class EFloatSum implements Accumulator<EFloatSum> {

  private EFloat _sum;

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------

  /** @see Double#toHextString(double)
   */
  public static final String toHexString (final EFloat x) {
    if (x.IsNaN()) { return "NaN"; }
    final int s = x.signum();
    final String ss = (s < 0) ? "-" : "";
    if (x.IsInfinity()) { return ss + "Infinity"; }
    if (x.isZero()) { return ss + "0x0.0p0"; }
    final EInteger e = x.getExponent();
    final EInteger t = x.getUnsignedMantissa();
    return 
      ss + "0x" + t.ToRadixString(0x10) + "p" +
      e.toString(); }

  /** @see Double#toHextString(double)
   */
  public final String toHexString () {
    return toHexString(_sum); }

  //--------------------------------------------------------------
  // Accumulator methods
  //--------------------------------------------------------------
  // start with only immediate needs

  @Override
  public final double doubleValue () { 
    return _sum.ToDouble(); }

  @Override
  public final EFloatSum clear () { 
    _sum = EFloat.Zero;
    return this; }

  @Override
  public final EFloatSum add (final double z) { 
    _sum = _sum.Add(EFloat.FromDouble(z));
    return this; }

  //  @Override
  //  public final EFloatSum addAll (final double[] z)  {
  //    for (final double zi : z) { 
  //      _sum = _sum.Add(EFloat.FromDouble(zi)); }
  //    return this; }


  @Override
  public final EFloatSum addProduct (final double z0,
                                     final double z1) { 
    _sum = _sum.Add(
      EFloat.FromDouble(z0)
      .Multiply(
        EFloat.FromDouble(z1)));
    return this; }

  //@Override
  //public final EFloatSum addProducts (final double[] z0,
  //                                        final double[] z1)  {
  //    final int n = z0.length;
  //    assert n == z1.length;
  //    for (int i=0;i<n;i++) { 
  //  sum = _sum.Add(
  //    EFloat.FromDouble(z0[i])
  //    .Multiply(
  //      EFloat.FromDouble(z1[i])));}
  //    return this; }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final String toString () {
    return _sum.toString(); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private EFloatSum () { super(); clear(); }

  public static final EFloatSum make () {
    return new EFloatSum(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
