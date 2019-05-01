package xfp.java.accumulators;

import com.carrotsearch.hppc.DoubleArrayList;

/** Distillation.
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-30
 */
@SuppressWarnings("unchecked")
public final class DistilledAccumulator 
implements Accumulator<DistilledAccumulator> {

  //--------------------------------------------------------------

  private final DoubleArrayList _sums = new DoubleArrayList();

  private final boolean twoSum (final int i) {

    final double x1 = _sums.get(i);
    if (0.0 == x1) { return false; }

    final double x0 = _sums.get(i-1);
    
    if (0.0 == x0) {
      _sums.set(i,0.0); _sums.set(i-1,x1); return true; }
    
    final double s = x0 + x1;
    final double z = s - x0;
    final double e = (x0 - (s - z)) + (x1 - z); 
    _sums.set(i-1,s);
    _sums.set(i,e); 
    return (x0 != s) || (x1 != e); }

  private final boolean distill () {
    boolean changed = false;
    for (int i=_sums.size()-1;i>0;i--) { 
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
    if (0 == _sums.size()) { return 0.0; }
    return _sums.get(0); }

  @Override
  public final Object value () { 
    return Double.valueOf(doubleValue()); }

  @Override
  public final float floatValue () { 
    return (float) doubleValue(); }

  @Override
  public final DistilledAccumulator clear () { 
    _sums.clear();
    return this; }

  @Override
  public final DistilledAccumulator add (final double z) { 
    assert Double.isFinite(z);
    _sums.add(z);
    while (distill()) { }
    _sums.removeAll(0.0);
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
