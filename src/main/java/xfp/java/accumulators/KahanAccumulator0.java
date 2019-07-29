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

public final class KahanAccumulator0

implements Accumulator<KahanAccumulator0> {

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
  public final KahanAccumulator0 clear () {
    s = 0.0; c = 0.0; return this; }

  @Override
  public final KahanAccumulator0 add (final double z) {
    //assert Double.isFinite(z);
    final double zz = z - c;
    final double ss = s + zz;
    c = (ss - s) - zz;
    s = ss;
    return this; }

  @Override
  public final KahanAccumulator0 add2 (final double z) {
    //assert Double.isFinite(z);
    // twoMul -> 2 adds.
    final double zz = z*z;
    final double e = Math.fma(z,z,-zz);
    add(zz);
    add(e);
    return this; }

  @Override
  public final KahanAccumulator0 addProduct (final double z0,
                                             final double z1) {
    //assert Double.isFinite(z0);
    //assert Double.isFinite(z1);
    // twoMul -> 2 adds.
    final double zz = z0*z1;
    add(zz);
    final double e = Math.fma(z0,z1,-zz);
    add(e);
    return this; }

  @Override
  public KahanAccumulator0 addL1 (final double z0,
                                  final double z1) {
    //assert Double.isFinite(z0);
    //assert Double.isFinite(z1);
    // twoAdd -> 2 adds.
    final double zz = z0 - z1;
    final double dz = zz - z0;
    final double e = (z0 - (zz - dz)) + ((-z1) - dz);
    if (0<=zz) {
      if (0<=e) { add(zz); add(e); }
      else if (Math.abs(e)<=Math.abs(zz)) { add(zz); add(e); }
      else { add(-zz); add(-e); } }
    else {
      if (0>e) { add(-zz); add(-e); }
      else if (Math.abs(e)<=Math.abs(zz)) { add(-zz); add(-e); }
      else { add(zz); add(e); } }
    return this; }

  @Override
  public KahanAccumulator0 addL2 (final double z0,
                                  final double z1) {
    //assert Double.isFinite(z0);
    //assert Double.isFinite(z1);
    // twoAdd and twoMul -> 8 adds.
    // twoAdd (twoSub):
    final double zz = z0-z1;
    final double dz = zz-z0;
    final double e = (z0-(zz-dz)) + ((-z1)-dz);
    // twoMul:
    final double ss = zz*zz;
    final double ess = Math.fma(zz,zz,-ss);
    add(ss);
    add(ess);
    // twoMul:
    final double es = e*zz;
    final double ees = Math.fma(e,zz,-es);
    add(es); add(es);
    add(ees); add(ees);
    // twoMul:
    final double ee = e*e;
    final double eee = Math.fma(e,e,-ee);
    add(ee);
    add(eee);
    return this; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private KahanAccumulator0 () { }

  public static final KahanAccumulator0 make () {
    return new KahanAccumulator0(); }

  //--------------------------------------------------------------
} // end of class
//--------------------------------------------------------------
