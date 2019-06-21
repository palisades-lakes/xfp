package xfp.java.accumulators;

import java.util.Arrays;

/** Eager distillation.
 * <p>
 * Mutable! Not thread safe!
 * <p>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-06-05
 */
@SuppressWarnings("unchecked")
public final class DistilledAccumulator
extends ExactAccumulator<DistilledAccumulator> {

  //--------------------------------------------------------------

  private double[] _sums = new double[32];
  private int _end = -1; // inclusive index

  private final void addValue (final double z) {
    _end++;
    if (_end >= _sums.length) {
      final int newSize = Math.max(1,(int) (0.5*_sums.length));
      _sums = Arrays.copyOf(_sums,newSize+_sums.length); }
    _sums[_end] = z; }

  private final void compact () {
    int lastNonZero = _end;
    while ((0.0==_sums[lastNonZero]) && (0 < lastNonZero)) {
      lastNonZero--; }
    _end = lastNonZero; }

  //--------------------------------------------------------------

  private final boolean twoSum (final int i) {
    final double x1 = _sums[i];
    final double x0 = _sums[i-1];
    final double s = x0+x1;
    final double z = s-x0;
    final double e = (x0-(s-z)) + (x1-z);
    _sums[i-1] = s;
    _sums[i] = e;
    return (x0 != s) || (x1 != e); }

  private final boolean distill () {
    if (! Double.isFinite(_sums[0])) { return false; }
    boolean changed = false;
    for (int i=_end;i>0;i--) { changed = changed || twoSum(i); }
    return changed; }

  //--------------------------------------------------------------
  // Accumulator
  //--------------------------------------------------------------

  @Override
  public final boolean noOverflow () { return false; }

  @Override
  public final double doubleValue () {
    if (0 > _end) { return 0.0; }
    return _sums[0]; }

  @Override
  public final Object value () {
    return Double.valueOf(doubleValue()); }

  @Override
  public final float floatValue () {
    return (float) doubleValue(); }

  @Override
  public final DistilledAccumulator clear () {
    Arrays.fill(_sums,0.0);
    _end = -1;
    return this; }

  @Override
  public final DistilledAccumulator add (final double z) {
    if (Double.isFinite(_sums[0])) {
      addValue(z);
      while (distill()) { compact(); } }
    return this; }

  @Override
  public final DistilledAccumulator add2 (final double z) {
    if (Double.isFinite(_sums[0])) {
      final double z2 = z*z;
      final double e = Math.fma(z,z,-z2);
      addValue(z2);
      addValue(e);
      while (distill()) { compact(); } }
    return this; }

  @Override
  public final DistilledAccumulator addProduct (final double z0,
                                                final double z1) {
    if (Double.isFinite(_sums[0])) {
      final double z01 = z0*z1;
      final double e = Math.fma(z0,z1,-z01);
      addValue(z01);
      addValue(e);
      while (distill()) { compact(); } }
    return this; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private DistilledAccumulator () { super(); clear(); }

  public static final DistilledAccumulator make () {
    return new DistilledAccumulator(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
