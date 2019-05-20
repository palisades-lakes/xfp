package xfp.java.linear;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.rng.UniformRandomProvider;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;

import xfp.java.algebra.OneSetOneOperation;
import xfp.java.algebra.Set;
import xfp.java.algebra.TwoSetsOneOperation;
import xfp.java.numbers.BigDecimals;
import xfp.java.prng.Generator;

/** The set of instances of <code>BigDecimal[dimension]</code>).
 *
 * TODO: generalize to tuples of <code>BigDecimal</code>
 * implemented with lists, <code>int</code> indexed maps
 * for sparse vectors, etc.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-07
 */
@SuppressWarnings("unchecked")
public final class BigDecimalsN extends LinearSpaceLike {

  //--------------------------------------------------------------
  // operations for algebraic structures over BigDecimal arrays.
  //--------------------------------------------------------------
  /** A <code>BinaryOperator</code> that adds elementwise
   * <code>BigDecimal[]</code> instances of length
   * <code>dimension</code>.
   */

  private final BigDecimal[] add (final BigDecimal[] q0,
                                  final BigDecimal[] q1) {
    assert contains(q0);
    assert contains(q1);
    final BigDecimal[] qq = new BigDecimal[dimension()];
    for (int i=0;i<dimension();i++) { qq[i] = q0[i].add(q1[i]); }
    return qq; }

  @Override
  public final Object add (final Object q0,
                           final Object q1) {
    return add((BigDecimal[]) q0, (BigDecimal[]) q1); }

  //--------------------------------------------------------------

  @Override
  public final Object zero (final int n) {
    final BigDecimal[] z = new BigDecimal[n];
    Arrays.fill(z,BigDecimal.ZERO);
    return z; }

  //--------------------------------------------------------------

  private final BigDecimal[] negate (final BigDecimal[] q) {
    assert contains(q);
    final BigDecimal[] qq = new BigDecimal[dimension()];
    for (int i=0;i<dimension();i++) { qq[i] = q[i].negate(); }
    return qq; }

  @Override
  public final Object negate (final Object q) {
    return negate((BigDecimal[]) q); }

  //--------------------------------------------------------------

  private final BigDecimal[] scale (final BigDecimal a,
                                    final BigDecimal[] q) {
    assert contains(q);
    final BigDecimal[] qq = new BigDecimal[dimension()];
    for (int i=0;i<dimension();i++) {
      qq[i] = q[i].multiply(a); }
    return qq; }

  @Override
  public final Object scale (final Object a,
                             final Object q) {
    return scale((BigDecimal) a, (BigDecimal[]) q); }

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  private final boolean equals (final BigDecimal[] q0,
                                final BigDecimal[] q1) {
    assert contains(q0);
    assert contains(q1);
    for (int i=0;i<dimension();i++) {
      if (! BigDecimals.get().equals(q0[i],q1[i])) {
        return false; } }
    return true; }

  @Override
  public final boolean equals (final Object q0,
                               final Object q1) {
    return equals((BigDecimal[]) q0, (BigDecimal[]) q1); }

  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    return
      (element instanceof BigDecimal[])
      &&
      (((BigDecimal[]) element).length == dimension()); }

  //--------------------------------------------------------------
  /** Intended primarily for testing.
   */

  @Override
  public final Supplier generator (final Map options) {
    final UniformRandomProvider urp = Set.urp(options);
    return
      new Supplier () {
      final Generator bf =
        BigDecimals.bigDecimalGenerator(dimension(),urp);
      @Override
      public final Object get () { return bf.next(); } }; }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final String toString () { return "BD^" + dimension(); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  // TODO: support zero-dimensional space?

  private BigDecimalsN (final int dimension) {
    super(dimension);  }

  private static final IntObjectMap<BigDecimalsN> _cache =
    new IntObjectHashMap();

  public static final BigDecimalsN get (final int dimension) {
    final BigDecimalsN dn0 = _cache.get(dimension);
    if (null != dn0) { return dn0; }
    final BigDecimalsN dn1 = new BigDecimalsN(dimension);
    _cache.put(dimension,dn1);
    return dn1; }

  //--------------------------------------------------------------

  public static final OneSetOneOperation group (final int n) {
    final BigDecimalsN bdn = get(n);
    return OneSetOneOperation.commutativeGroup(
      bdn.adder(),
      bdn,
      bdn.additiveIdentity(),
      bdn.additiveInverse()); }

  //--------------------------------------------------------------

  private static final TwoSetsOneOperation
  makeSpace (final int n) {
    return
      TwoSetsOneOperation.linearSpaceLike(
        BigDecimalsN.get(n).scaler(),
        BigDecimalsN.group(n),
        BigDecimals.RING); }

  private static final IntObjectMap<TwoSetsOneOperation>
  _spaceCache = new IntObjectHashMap();

  /** n-dimensional rational module, implemented with
   * <code>BigDecimal[]</code>.
   * (not a vector space because BigDecimal doesn't have a
   * multiplicative inverse.)
   */
  public static final TwoSetsOneOperation
  space (final int dimension) {
    final TwoSetsOneOperation space0 = _spaceCache.get(dimension);
    if (null != space0) { return space0; }
    final TwoSetsOneOperation space1 = makeSpace(dimension);
    _spaceCache.put(dimension,space1);
    return space1; }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

