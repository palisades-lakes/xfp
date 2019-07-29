package xfp.java.accumulators;

//----------------------------------------------------------------
/** Compensated summation for lots of numbers.
 * Only makes sense for floating point numbers of various kinds.
 * <p>
 * Use twoAdd and twoMul to convert, eg, dot products to 
 * multiple compensated additions. 
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
 * @version 2019-07-29
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
    //assert Double.isFinite(z);
    final double zc = z - c;
    final double szc = s + zc;
    c = (szc - s) - zc;
    s = szc;
    return this; }

  @Override
  public final KahanAccumulator addAll (final double[] z) {
    final int n = z.length;
    for (int i=0;i<n;i++) {
      final double zi = z[i];
      //assert Double.isFinite(zi);
      final double zz = zi - c;
      final double szz = s + zz;
      c = (szz - s) - zz;
      s = szz; }
    return this; }

  @Override
  public final KahanAccumulator add2 (final double z) {
    //assert Double.isFinite(z);
    // preserve exactness using twoMul to convert to 2 adds.
    final double zz = z*z;
    //add(zz);
    final double zc = zz - c;
    final double szc = s + zc;
    c = (szc - s) - zc;
    s = szc;
    final double e = Math.fma(z,z,-zz);
    //add(e);
    final double ec = e - c;
    final double sec = s + ec;
    c = (sec - s) - ec;
    s = sec;
    return this; }

//  @Override
//  public final KahanAccumulator add2All (final double[] z) {
//    final int n = z.length;
//    for (int i=0;i<n;i++) {
//      final double zi = z[i];
//      //assert Double.isFinite(zi);
//      final double zz = (zi*zi) -c;
//      final double ss = s + zz;
//      c = (ss - s) - zz;
//      s = ss; }
//    return this; }

  @Override
  public final KahanAccumulator addProduct (final double z0,
                                            final double z1) {
    //assert Double.isFinite(z0);
    //assert Double.isFinite(z1);
    // preserve exactness using twoMul to convert to 2 adds.
    final double zz = z0*z1;
    //add(zz);
    final double zc = zz - c;
    final double szc = s + zc;
    c = (szc - s) - zc;
    s = szc;
    final double e = Math.fma(z0,z1,-zz);
    //add(e);
    final double ec = e - c;
    final double sec = s + ec;
    c = (sec - s) - ec;
    s = sec;
    return this; }

//  @Override
//  public final KahanAccumulator addProducts (final double[] z0,
//                                             final double[] z1) {
//    final int n = z0.length;
//    //assert n == z1.length;
//    for (int i=0;i<n;i++) {
//      //assert Double.isFinite(z0[i]);
//      //assert Double.isFinite(z1[i]);
//      final double z0i = z0[i];
//      final double z1i = z1[i];
//      final double zz = z0i*z1i;
//      final double szz = s + zz;
//      c = (szz - s) - zz;
//      s = szz;
//      final double e = Math.fma(z0i,z1i,-zz);
//      final double se = s + e;
//      c = (se - s) - e;
//      s = se; }
//    return this; }

  @Override
  public KahanAccumulator addL1 (final double z0,
                                 final double z1) {
    //assert Double.isFinite(z0);
    //assert Double.isFinite(z1);
    // twoAdd -> 2 adds.
    final double zz = z0 - z1;
    final double dz = zz - z0;
    final double e = (z0 - (zz - dz)) + ((-z1) - dz);
    if (0<=zz) {
      if (0<=e) { 
        add(zz); 
        add(e); }
      else if (Math.abs(e)<=Math.abs(zz)) { 
        add(zz); 
        add(e); }
      else { 
        add(-zz); 
        add(-e); } }
    else {
      if (0>e) { 
        add(-zz); 
        add(-e); }
      else if (Math.abs(e)<=Math.abs(zz)) { 
        add(-zz); 
        add(-e); }
      else { 
        add(zz); 
        add(e); } }
    return this; }

  @Override
  public KahanAccumulator addL2 (final double z0,
                                 final double z1) {
    //assert Double.isFinite(z0);
    //assert Double.isFinite(z1);
    // twoAdd and twoMul -> 8 adds.
    // twoAdd (twoSub):
    final double zz = z0-z1;
    final double dz = zz-z0;
    final double e = (z0-(zz-dz)) + ((-z1)-dz);
    // twoMul:
    final double zzzz = zz*zz;
    final double ezzzz = Math.fma(zz,zz,-zzzz);
    add(zzzz);
    add(ezzzz);
    // twoMul:
    final double ezz = e*zz;
    final double eezz = Math.fma(e,zz,-ezz);
    add(ezz); add(ezz);
    add(eezz); add(eezz);
    // twoMul:
    final double ee = e*e;
    final double eee = Math.fma(e,e,-ee);
    add(ee);
    add(eee);
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
