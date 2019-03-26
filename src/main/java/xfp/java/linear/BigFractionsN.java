package xfp.java.linear;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.rng.UniformRandomProvider;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;

import xfp.java.algebra.OneSetOneOperation;
import xfp.java.algebra.Set;
import xfp.java.algebra.TwoSetsOneOperation;
import xfp.java.numbers.BigFractions;
import xfp.java.prng.Generator;

/** The set of instances of <code>BigFraction[dimension]</code>).
 * 
 * This is primarily intended to support implementing the standard
 * <em>rational</em> linear space <b>Q</b><sup>n</sup>.
 * (Essentially <b>R</b><sup>n</sup> except over the rational
 * numbers rather than the reals.)

 * TODO: generalize to tuples 
 * implemented with lists, <code>int</code> indexed maps
 * for sparse vectors, etc.
 * Long term goal is to support any useful data structures
 * that can be used to represent tuples of rational numbers.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-07
 */
@SuppressWarnings("unchecked")
public final class BigFractionsN extends LinearSpaceLike  {

  //--------------------------------------------------------------
  // operations for algebraic structures over BigFraction arrays.
  //--------------------------------------------------------------
  /** A <code>BinaryOperator</code> that adds elementwise
   * <code>BigFraction[]</code> instances of length 
   * <code>dimension</code>.
   */

  @Override
  public final BigFraction[] add (final Object x0, 
                                  final Object x1) {
    assert contains(x0);
    assert contains(x1);
    final BigFraction[] q0 = (BigFraction[]) x0;
    final BigFraction[] q1 = (BigFraction[]) x1;
    final BigFraction[] qq = new BigFraction[dimension()];
    for (int i=0;i<dimension();i++) { qq[i] = q0[i].add(q1[i]); }
    return qq; }

  //--------------------------------------------------------------

  @Override
  public final BigFraction[] zero (final int n) {
    final BigFraction[] z = new BigFraction[n];
    Arrays.fill(z,BigFraction.ZERO);
    return z; }

  //--------------------------------------------------------------

  @Override
  public final BigFraction[] negate (final Object x) {
    assert contains(x);
    final BigFraction[] q = (BigFraction[]) x;
    final BigFraction[] qq = new BigFraction[dimension()];
    for (int i=0;i<dimension();i++) { qq[i] = q[i].negate(); }
    return qq; } 

  //--------------------------------------------------------------

  @Override
  public final Object scale (final Object a, 
                             final Object x) {
    assert contains(x);
    final BigFraction b = (BigFraction) a;
    final BigFraction[] q = (BigFraction[]) x;
    final BigFraction[] qq = new BigFraction[dimension()];
    for (int i=0;i<dimension();i++) { qq[i] = q[i].multiply(b); }
    return qq; } 

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean equals (final Object x0, 
                               final Object x1) {
    assert contains(x0);
    assert contains(x1);
    final BigFraction[] q0 = (BigFraction[]) x0;
    final BigFraction[] q1 = (BigFraction[]) x1;
    for (int i=0;i<dimension();i++) {
      if (! BigFractions.get().equals(q0[i],q1[i])) {
        return false; } }
    return true; }

  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    return 
      (element instanceof BigFraction[])
      &&
      ((BigFraction[]) element).length == dimension(); }

  //--------------------------------------------------------------
  /** Intended primarily for testing. 
   */

  @Override
  public final Supplier generator (final Map options) {
    final UniformRandomProvider urp = Set.urp(options);
    return 
      new Supplier () {
      final Generator g =
        BigFractions.bigFractionGenerator(dimension(),urp);
      @Override
      public final Object get () { return g.next(); } }; }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final String toString () { return "BF^" + dimension(); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  // TODO: support zero-dimensional space?

  private BigFractionsN (final int dimension) { super(dimension); }

  private static final IntObjectMap<BigFractionsN> _cache = 
    new IntObjectHashMap();

  public static final BigFractionsN get (final int dimension) {
    final BigFractionsN s0 = _cache.get(dimension);
    if (null != s0) { return s0; }
    final BigFractionsN s1 = new BigFractionsN(dimension); 
    _cache.put(dimension,s1);
    return s1; }

  //--------------------------------------------------------------

  public static final OneSetOneOperation group (final int n) {
    final BigFractionsN g = get(n);
    return OneSetOneOperation.commutativeGroup(
      g.adder(),
      g,
      g.additiveIdentity(),
      g.additiveInverse()); }

  //--------------------------------------------------------------
  /** n-dimensional rational vector space, implemented with
   * <code>BigFraction[n]</code>.
   */

  private static final TwoSetsOneOperation 
  makeSpace (final int n) { 
    return
      TwoSetsOneOperation.linearSpaceLike(
        get(n).scaler(),
        group(n),
        BigFractions.FIELD); }

  private static final IntObjectMap<TwoSetsOneOperation> 
  _spaceCache = new IntObjectHashMap();

  /** n-dimensional rational vector space, implemented with
   * <code>BigFraction[]</code>.
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

