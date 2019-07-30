package xfp.java.accumulators;

import com.upokecenter.numbers.EFloat;
import com.upokecenter.numbers.EInteger;

/** Naive sum of <code>double</code> values with EFloat
 * accumulator (for testing).
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-29
 */
public final class EFloatAccumulator
extends ExactAccumulator<EFloatAccumulator> {

  private EFloat _sum;

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------

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

  public final String toHexString () { return toHexString(_sum); }

  //--------------------------------------------------------------
  // Accumulator methods
  //--------------------------------------------------------------

  @Override
  public final boolean noOverflow () { return true; }

  @Override
  public final Object value () { return _sum; }

  @Override
  public final double doubleValue () {
    return _sum.ToDouble(); }

  @Override
  public final EFloatAccumulator clear () {
    _sum = EFloat.Zero;
    return this; }

  @Override
  public final EFloatAccumulator add (final double z) {
    //assert Double.isFinite(z);
    _sum = _sum.Add(EFloat.FromDouble(z));
    return this; }

  @Override
  public final EFloatAccumulator add2 (final double z) {
    //assert Double.isFinite(z);
    final EFloat zz = EFloat.FromDouble(z);
    _sum = _sum.Add(zz.Multiply(zz));
    return this; }

  @Override
  public final EFloatAccumulator addProduct (final double z0,
                                             final double z1) {
    //assert Double.isFinite(z0);
    //assert Double.isFinite(z1);
    _sum = _sum.Add(
      EFloat.FromDouble(z0)
      .Multiply(
        EFloat.FromDouble(z1)));
    return this; }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final String toString () {
    return _sum.toString(); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private EFloatAccumulator () { super(); clear(); }

  public static final EFloatAccumulator make () {
    return new EFloatAccumulator(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
