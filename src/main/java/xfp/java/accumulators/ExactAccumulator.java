package xfp.java.accumulators;

import xfp.java.Classes;
import xfp.java.Debug;

/** Base class for some exact accumulators.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-06-05
 */
public abstract class ExactAccumulator<T extends ExactAccumulator>
implements Accumulator<T> {

  //--------------------------------------------------------------

  @Override
  public final boolean isExact () { return true; }

  @Override
  public T add2 (final double x) {
    assert Double.isFinite(x);
    // preserve exactness using twoMul to convert to 2 adds.
    final double x2 = x*x;
    final double e = Math.fma(x,x,-x2);
    add(x2);
    add(e);
    return (T) this; }

  @Override
  public T addProduct (final double x0,
                       final double x1) {
    assert Double.isFinite(x0);
    assert Double.isFinite(x1);
    // preserve exactness using twoMul to convert to 2 adds.
    final double x01 = x0*x1;
    final double e = Math.fma(x0,x1,-x01);
    add(x01);
    add(e);
    return (T) this; }

  @Override
  public T addL1 (final double x0,
                  final double x1) {
    assert Double.isFinite(x0);
    assert Double.isFinite(x1);
    // preserve exactness using twoAdd and twoMul to convert to 2
    // adds.
    final double s = x0 - x1;
    final double z = s - x0;
    final double e = (x0 - (s - z)) + ((-x1) - z);
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
  public T addL2 (final double x0,
                  final double x1) {
    assert Double.isFinite(x0);
    assert Double.isFinite(x1);
    // preserve exactness using twoAdd and twoMul to convert to 8
    // adds.
    final double s = x0-x1;
    final double z = s-x0;
    final double e = (x0-(s-z)) + ((-x1)-z);
    final double ss = s*s;
    add(ss);
    final double ess = Math.fma(s,s,-s);
    add(ess);
    final double es = e*s;
    add(es);
    add(es);
    final double ees = Math.fma(e,s,-es);
    add(ees);
    add(ees);
    final double ee = e*e;
    add(ee);
    final double eee = Math.fma(e,e,-ee);
    add(eee);
    //Debug.println("x0,x1=" + x0+ ", " + x1);
    //Debug.println("x0-x1=" + (x0-x1));
    //Debug.println("(x0-x1)^2=" + (x0-x1)*(x0-x1));
    //Debug.println("s=" + doubleValue());
    return (T) this; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  ExactAccumulator () { super(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
