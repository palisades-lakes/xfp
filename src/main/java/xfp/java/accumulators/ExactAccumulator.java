package xfp.java.accumulators;

/** Base class for some exact accumulators.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-28
 */
@SuppressWarnings("unchecked")
public abstract class ExactAccumulator<T extends ExactAccumulator>
implements Accumulator<T> {

  //--------------------------------------------------------------

  @Override
  public final boolean isExact () { return true; }

  @Override
  public T add2 (final double z) {
    //assert Double.isFinite(z);
    // preserve exactness using twoMul to convert to 2 adds.
    final double z2 = z*z;
    final double e = Math.fma(z,z,-z2);
    add(z2);
    add(e);
    return (T) this; }

  @Override
  public T addProduct (final double z0,
                       final double z1) {
    //assert Double.isFinite(z0);
    //assert Double.isFinite(z1);
    // preserve exactness using twoMul to convert to 2 adds.
    final double z01 = z0*z1;
    final double e = Math.fma(z0,z1,-z01);
    add(z01);
    add(e);
    return (T) this; }

  @Override
  public T addL1 (final double z0,
                  final double z1) {
    //assert Double.isFinite(z0);
    //assert Double.isFinite(z1);
    // preserve exactness using twoAdd and twoMul to convert to 2
    // adds.
    final double s = z0 - z1;
    final double z = s - z0;
    final double e = (z0 - (s - z)) + ((-z1) - z);
    if (0<=s) {
      if (0<=e) { add(s); add(e); }
      else if (Math.abs(e)<=Math.abs(s)) { add(s); add(e); }
      else { add(-s); add(-e); } }
    else {
      if (0>e) { add(-s); add(-e); }
      else if (Math.abs(e)<=Math.abs(s)) { add(-s); add(-e); }
      else { add(s); add(e); } }
    return (T) this; }

  @Override
  public T addL2 (final double z0,
                  final double z1) {
    //assert Double.isFinite(z0);
    //assert Double.isFinite(z1);
    // preserve exactness using twoAdd and twoMul to convert to 8
    // adds.
    // twoAdd (twoSub):
    final double s = z0-z1;
    final double z = s-z0;
    final double e = (z0-(s-z)) + ((-z1)-z);
    // twoMul:
    final double ss = s*s;
    final double ess = Math.fma(s,s,-ss);
    add(ss);
    add(ess);
    // twoMul:
    final double es = e*s;
    final double ees = Math.fma(e,s,-es);
    add(es); add(es);
    add(ees); add(ees);
    // twoMul:
    final double ee = e*e;
    final double eee = Math.fma(e,e,-ee);
    add(ee);
    add(eee);
    return (T) this; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  ExactAccumulator () { super(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
