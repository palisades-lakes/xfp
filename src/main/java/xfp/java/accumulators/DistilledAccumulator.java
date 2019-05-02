package xfp.java.accumulators;

import java.util.Arrays;

/** Distillation.
 * <p>
 * Mutable! Not thread safe!
 * <p>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-05-01
 */
@SuppressWarnings("unchecked")
public final class DistilledAccumulator 
implements Accumulator<DistilledAccumulator> {

  //--------------------------------------------------------------

  private double[] _sums = new double[32];
  private int _end = -1; // inclusive index

  private final void addValue (final double z) {
    _end++;
    if (_end >= _sums.length) { 
      final int newSize = (int) 1.5*_sums.length;
      _sums = Arrays.copyOf(_sums,newSize); }
    _sums[_end] = z; }

  private final void compact () {
    int lastNonZero = _end;
    while ((0.0 == _sums[lastNonZero]) && (0 < lastNonZero)) { 
      lastNonZero--; }
    _end = lastNonZero; }

  //--------------------------------------------------------------

  private final boolean twoSum (final int i) {

    final double x1 = _sums[i];
    //if (0.0 == x1) { return false; }

    final double x0 = _sums[i-1];

    //if (0.0 == x0) {
    //  _sums[i] = 0.0; _sums[i-1] = x1; return true; }

    final double s = x0 + x1;
    final double z = s - x0;
    final double e = (x0 - (s - z)) + (x1 - z); 
    _sums[i-1] = s;
    _sums[i] = e; 
    return (x0 != s) || (x1 != e); }

  private final boolean distill () {
    boolean changed = false;
    for (int i=_end;i>0;i--) { 
      changed = changed || twoSum(i); } 
    return changed; }

  //--------------------------------------------------------------
  // Accumulator
  //--------------------------------------------------------------
  @Override
  public final boolean isExact () { return true; }

  @Override
  public final boolean noOverflow () { return true; }

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
    _end = -1;
    return this; }

  @Override
  public final DistilledAccumulator add (final double z) { 
    assert Double.isFinite(z);
    addValue(z);
    while (distill()) { compact(); }
    return this; }

  @Override
  public final DistilledAccumulator add2 (final double z) { 
    assert Double.isFinite(z);
    final double z2 = z*z;
    final double e = Math.fma(z,z,-z2);
    add(z2);
    add(e); 
    return this; }

  @Override
  public final DistilledAccumulator addProduct (final double z0,
                                                 final double z1) { 
    assert Double.isFinite(z0);
    assert Double.isFinite(z1);
    final double z01 = z0*z1;
    final double e = Math.fma(z0,z1,-z01);
    add(z01);
    add(e); 
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
