package xfp.java.accumulators;

/** Base class for some exact accumulators.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-06-06
 */
@SuppressWarnings("unchecked")
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
    //Debug.println(Classes.className(this)+".addL2");
    //Debug.println("before=" + this);
    //Debug.println("before=" + doubleValue());
    assert Double.isFinite(x0);
    assert Double.isFinite(x1);
    // preserve exactness using twoAdd and twoMul to convert to 8
    // adds.
    // twoAdd (twoSub):
    final double s = x0-x1;
    final double z = s-x0;
    final double e = (x0-(s-z)) + ((-x1)-z);
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
    //Debug.println("x0,x1=" + x0+ ", " + x1);
    //Debug.println("x0-x1=" + (x0-x1));
    //Debug.println("(x0-x1)^2=" + (x0-x1)*(x0-x1));
    //Debug.println("s=" + s);
    //Debug.println("z=" + z);
    //Debug.println("e=" + e);
    //Debug.println("ss=" + ss);
    //Debug.println("ess=" + ess);
    //Debug.println("es=" + es);
    //Debug.println("ees=" + ees);
    //Debug.println("ee=" + ee);
    //Debug.println("eee=" + eee);
    //Debug.println("after=" + doubleValue());
    return (T) this; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  ExactAccumulator () { super(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
