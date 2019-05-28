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
 * @version 2019-04-21
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
    for (final double zi : z) { 
      assert Double.isFinite(zi);
      final double zz = zi - c;
      final double ss = s + zz;
      c = (ss - s) - zz;
      s = ss; } 
    return this; }

  @Override
  public final KahanAccumulator add2 (final double z) {
    assert Double.isFinite(z);
    final double zz = (z*z) - c;
    final double ss = s + zz;
    c = (ss - s) - zz;
    s = ss; 
    return this; }

  @Override
  public final KahanAccumulator add2All (final double[] z) { 
    for (final double zi : z) { 
      assert Double.isFinite(zi);
      final double zz = (zi*zi) -c;
      final double ss = s + zz;
      c = (ss - s) - zz;
      s = ss; } 
    return this; }

  @Override
  public final KahanAccumulator addProduct (final double z0,
                                            final double z1) {
    assert Double.isFinite(z0);
    assert Double.isFinite(z1);
    final double zz = (z0*z1) - c;
    final double ss = s + zz;
    c = (ss - s) - zz;
    s = ss; 
    return this; }

  @Override
  public final KahanAccumulator addProducts (final double[] z0,
                                             final double[] z1) { 
    final int n = z0.length;
    assert n == z1.length;
    for (int i=0;i<n;i++) {     
      assert Double.isFinite(z0[i]);
      assert Double.isFinite(z1[i]);
      final double zz = (z0[i]*z1[i]) - c;
      final double ss = s + zz;
      c = (ss - s) - zz;
      s = ss; 
    }
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
