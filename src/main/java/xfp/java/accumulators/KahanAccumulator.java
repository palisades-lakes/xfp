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
 * @version 2019-08-05
 */

public final class KahanAccumulator

implements Accumulator<KahanAccumulator> {

  private double value = 0.0;
  private double correction = 0.0;

  //--------------------------------------------------------------

  @Override
  public final boolean isExact () { return false; }

  @Override
  public final boolean noOverflow () { return false; }

  @Override
  public final Object value () {
    return Double.valueOf(doubleValue()); }

  @Override
  public final double doubleValue () { return value; }

  @Override
  public final KahanAccumulator clear () {
    value = 0.0; correction = 0.0; return this; }

  //--------------------------------------------------------------

  @Override
  public final KahanAccumulator add (final double z) {
    //assert Double.isFinite(z);
    final double zc = z - correction;
    final double szc = value + zc;
    correction = (szc - value) - zc;
    value = szc;
    return this; }

  @Override
  public final KahanAccumulator addAll (final double[] z) {
    for (final double zi : z) { 
      //assert Double.isFinite(z);
      final double zc = zi - correction;
      final double szc = value + zc;
      correction = (szc - value) - zc;
      value = szc; }
    return this; }

  //--------------------------------------------------------------

  @Override
  public final KahanAccumulator addAbs (final double z) {
    //assert Double.isFinite(z);
    final double zc = Math.abs(z) - correction;
    final double szc = value + zc;
    correction = (szc - value) - zc;
    value = szc;
    return this; }

  @Override
  public final KahanAccumulator addAbsAll (final double[] z) {
    for (final double zi : z) { 
      //assert Double.isFinite(z);
      final double zc = Math.abs(zi) - correction;
      final double szc = value + zc;
      correction = (szc - value) - zc;
      value = szc; }
    return this; }

  //--------------------------------------------------------------

  @Override
  public final KahanAccumulator add2 (final double z) {
    //assert Double.isFinite(z);
    // twoMul -> 2 adds.
    final double zz = z*z;
    add(zz);
    final double e = Math.fma(z,z,-zz);
    add(e);
    return this; }

  @Override
  public final KahanAccumulator add2All (final double[] z) {
    for (final double zi : z) { 
      //assert Double.isFinite(zi);
      final double zz = zi*zi;
      add(zz);
      final double e = Math.fma(zi,zi,-zz);
      add(e); }
    return this; }

  //--------------------------------------------------------------

  @Override
  public final KahanAccumulator addProduct (final double z0,
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
  public final KahanAccumulator addProducts (final double[] z0,
                                             final double[] z1) {
    final int n= z0.length;
    //assert n==z1.length;
    for (int i=0;i<n;i++) { 
      final double z0i = z0[i];
      //assert Double.isFinite(z0i);
      final double z1i = z1[i];
      //assert Double.isFinite(z1i);
      final double zz = z0i*z1i;
      add(zz);
      final double e = Math.fma(z0i,z1i,-zz);
      add(e); }
    return this; }

  //--------------------------------------------------------------

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
      if (0<=e) { add(zz); add(e); }
      else if (Math.abs(e)<=Math.abs(zz)) { add(zz); add(e); }
      else { add(-zz); add(-e); } }
    else {
      if (0>e) { add(-zz); add(-e); }
      else if (Math.abs(e)<=Math.abs(zz)) { add(-zz); add(-e); }
      else { add(zz); add(e); } }
    return this; }

  @Override
  public final KahanAccumulator addL1Distance (final double[] z0,
                                               final double[] z1) {
    final int n= z0.length;
    //assert n==z1.length;
    for (int i=0;i<n;i++) { 
      final double z0i = z0[i];
      //assert Double.isFinite(z0i);
      final double z1i = z1[i];
      //assert Double.isFinite(z1i);
      addL1(z0i,z1i); }
    return this; }

  //--------------------------------------------------------------

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

  @Override
  public final KahanAccumulator addL2Distance (final double[] z0,
                                               final double[] z1) {
    final int n= z0.length;
    //assert n==z1.length;
    for (int i=0;i<n;i++) { 
      final double z0i = z0[i];
      //assert Double.isFinite(z0i);
      final double z1i = z1[i];
      //assert Double.isFinite(z1i);
      addL2(z0i,z1i); }
    return this; }

  //--------------------------------------------------------------

  @Override
  public final void partialSums (final double[] x,
                                 final double[] s) {
    final int n = x.length;
    //assert value.length==n;
    clear();
    for (int i=0;i<n;i++) { s[i] = add(x[i]).doubleValue(); } }

  @Override
  public final  double[] partialSums (final double[] x) {
    final int n = x.length;
    final double[] s = new double[n];
    clear();
    for (int i=0;i<n;i++) { s[i] = add(x[i]).doubleValue(); }
    return s; }

  @Override
  public final  double[] partialL1s (final double[] x) {
    final int n = x.length;
    final double[] s = new double[n];
    clear();
    for (int i=0;i<n;i++) { s[i] = addAbs(x[i]).doubleValue(); }
    return s; }

  @Override
  public final  double[] partialL2s (final double[] x) {
    final int n = x.length;
    final double[] s = new double[n];
    clear();
    for (int i=0;i<n;i++) { s[i] = add2(x[i]).doubleValue(); }
    return s; }

  @Override
  public final  double[] partialDots (final double[] x0,
                                      final double[] x1) {
    final int n = x0.length;
    final double[] s = new double[n];
    clear();
    for (int i=0;i<n;i++) {
      s[i] = addProduct(x0[i],x1[i]).doubleValue(); }
    return s; }

  @Override
  public final  double[] partialL1Distances (final double[] x0,
                                             final double[] x1) {
    final int n = x0.length;
    final double[] s = new double[n];
    clear();
    for (int i=0;i<n;i++) {
      s[i] = addL1(x0[i],x1[i]).doubleValue(); }
    return s; }

  @Override
  public final  double[] partialL2Distances (final double[] x0,
                                             final double[] x1) {
    final int n = x0.length;
    final double[] s = new double[n];
    clear();
    for (int i=0;i<n;i++) {
      s[i] = addL2(x0[i],x1[i]).doubleValue(); }
    return s; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private KahanAccumulator () { }

  public static final KahanAccumulator make () {
    return new KahanAccumulator(); }

  //--------------------------------------------------------------
} // end of class
//--------------------------------------------------------------
