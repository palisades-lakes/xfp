package xfp.java.accumulators;

//----------------------------------------------------------------
/** Compensated summation for lots of numbers.
 * Only makes sense for floating point numbers of various kinds.
 * <p>
 * Mutable! Not thread safe!
 * <p>
 * @see <a
 *      href="https://en.wikipedia.org/wiki/Kahan_summation_algorithm">
 *      Wikipedia:Kahan_summation_algorithm</a>
 *
 *@see  <a
 *      href="https://www-pequan.lip6.fr/~graillat/papers/posterRNC7.pdf">
 *      Graillat, Langlois, and Louvet, Accurate dot products with FMA"</a>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-06-05
 */

public final class KahanAccumulator

implements Accumulator<KahanAccumulator> {

  private double s = 0.0;
  private double c = 0.0;

  //--------------------------------------------------------------

  @Override
  public final boolean isExact () { return false; }

  @Override
  public final boolean noOverflow () { return false; }

  @Override
  public final Object value () {
    return Double.valueOf(doubleValue()); }

  @Override
  public final double doubleValue () { return s; }

  @Override
  public final KahanAccumulator clear () {
    s = 0.0; c = 0.0; return this; }

  @Override
  public final KahanAccumulator add (final double z) {
    assert Double.isFinite(z);
    final double zz = z - c;
    final double ss = s + zz;
    c = (ss - s) - zz;
    s = ss;
    return this; }

  @Override
  public final KahanAccumulator addAll (final double[] z) {
    final int n = z.length;
    for (int i=0;i<n;i++) {
      final double zi = z[i];
      assert Double.isFinite(zi);
      final double zz = zi - c;
      final double ss = s + zz;
      c = (ss - s) - zz;
      s = ss; }
    return this; }

  @Override
  public final KahanAccumulator add2 (final double x) {
    assert Double.isFinite(x);
    // preserve exactness using twoMul to convert to 2 adds.
    final double x2 = x*x;
    final double e = Math.fma(x,x,-x2);
    add(x2);
    add(e);
    return this; }

  @Override
  public final KahanAccumulator add2All (final double[] z) {
    final int n = z.length;
    for (int i=0;i<n;i++) {
      final double zi = z[i];
      assert Double.isFinite(zi);
      final double zz = (zi*zi) -c;
      final double ss = s + zz;
      c = (ss - s) - zz;
      s = ss; }
    return this; }

  @Override
  public final KahanAccumulator addProduct (final double x0,
                                            final double x1) {
    assert Double.isFinite(x0);
    assert Double.isFinite(x1);
    // preserve exactness using twoMul to convert to 2 adds.
    final double x01 = x0*x1;
    final double e = Math.fma(x0,x1,-x01);
    add(x01);
    add(e);
    return this; }

  @Override
  public final KahanAccumulator addProducts (final double[] z0,
                                             final double[] z1) {
    final int n = z0.length;
    assert n == z1.length;
    for (int i=0;i<n;i++) {
      assert Double.isFinite(z0[i]);
      assert Double.isFinite(z1[i]);
      final double zz = Math.fma(z0[i],z1[i],-c);
      final double ss = s + zz;
      c = (ss - s) - zz;
      s = ss;
    }
    return this; }

  @Override
  public KahanAccumulator addL1 (final double x0,
                                 final double x1) {
    assert Double.isFinite(x0);
    assert Double.isFinite(x1);
    // preserve exactness using twoAdd and twoMul to convert to 2
    // adds.
    final double s01 = x0 - x1;
    final double z = s01 - x0;
    final double e = (x0 - (s01 - z)) + ((-x1) - z);
    if (0<=s01) {
      if (0<=e) { add(s01); add(e); }
      else if (Math.abs(e)<=Math.abs(s01)) { add(s01); add(e); }
      else { add(-s01); add(-e); } }
    else {
      if (0>e) { add(-s01); add(-e); }
      else if (Math.abs(e)<=Math.abs(s01)) { add(-s01); add(-e); }
      else { add(s01); add(e); } }
    return this; }

  @Override
  public KahanAccumulator addL2 (final double x0,
                                 final double x1) {
    //Debug.println(Classes.className(this)+".addL2");
    //Debug.println("before=" + this);
    //Debug.println("before=" + doubleValue());
    assert Double.isFinite(x0);
    assert Double.isFinite(x1);
    // preserve exactness using twoAdd and twoMul to convert to 8
    // adds.
    // twoAdd (twoSub):
    final double s01 = x0-x1;
    final double z = s01-x0;
    final double e = (x0-(s01-z)) + ((-x1)-z);
    // twoMul:
    final double ss = s01*s01;
    final double ess = Math.fma(s01,s01,-ss);
    add(ss);
    add(ess);
    // twoMul:
    final double es = e*s01;
    final double ees = Math.fma(e,s01,-es);
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
    return this; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private KahanAccumulator () { }

  public static final KahanAccumulator make () {
    return new KahanAccumulator(); }

  //--------------------------------------------------------------
} // end of class
//--------------------------------------------------------------
