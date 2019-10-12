package xfp.java.linear;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.rng.UniformRandomProvider;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;

import xfp.java.algebra.OneSetOneOperation;
import xfp.java.algebra.Set;
import xfp.java.algebra.TwoSetsOneOperation;
import xfp.java.numbers.RationalFloat;
import xfp.java.numbers.RationalFloats;
import xfp.java.prng.Generator;

/** The set of instances of <code>Rational[dimension]</code>).
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
 * @version 2019-10-12
 */
@SuppressWarnings("unchecked")
public final class RationalFloatsN extends LinearSpaceLike  {

  //--------------------------------------------------------------
  // operations for algebraic structures over Rational arrays.
  //--------------------------------------------------------------
  /** A <code>BinaryOperator</code> that adds elementwise
   * <code>RationalFloat[]</code> instances of length
   * <code>dimension</code>.
   */

  @Override
  public final RationalFloat[] add (final Object x0,
                               final Object x1) {
    assert contains(x0);
    assert contains(x1);
    final RationalFloat[] q0 = (RationalFloat[]) x0;
    final RationalFloat[] q1 = (RationalFloat[]) x1;
    final RationalFloat[] qq = new RationalFloat[dimension()];
    for (int i=0;i<dimension();i++) { qq[i] = q0[i].add(q1[i]); }
    return qq; }

  //--------------------------------------------------------------

  @Override
  public final RationalFloat[] zero (final int n) {
    final RationalFloat[] z = new RationalFloat[n];
    Arrays.fill(z,RationalFloat.ZERO);
    return z; }

  //--------------------------------------------------------------

  @Override
  public final RationalFloat[] negate (final Object x) {
    assert contains(x);
    final RationalFloat[] q = (RationalFloat[]) x;
    final RationalFloat[] qq = new RationalFloat[dimension()];
    for (int i=0;i<dimension();i++) { qq[i] = q[i].negate(); }
    return qq; }

  //--------------------------------------------------------------

  @Override
  public final Object scale (final Object a,
                             final Object x) {
    assert contains(x);
    final RationalFloat b = (RationalFloat) a;
    final RationalFloat[] q = (RationalFloat[]) x;
    final RationalFloat[] qq = new RationalFloat[dimension()];
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
    final RationalFloat[] q0 = (RationalFloat[]) x0;
    final RationalFloat[] q1 = (RationalFloat[]) x1;
    for (int i=0;i<dimension();i++) {
      if (! q0[i].equals(q1[i])) { return false; } }
    return true; }

  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    return
      (element instanceof RationalFloat[])
      &&
      (((RationalFloat[]) element).length == dimension()); }

  //--------------------------------------------------------------
  /** Intended primarily for testing.
   */

  @Override
  public final Supplier generator (final Map options) {
    final UniformRandomProvider urp = Set.urp(options);
    return
      new Supplier () {
      final Generator g = RationalFloats.generator(dimension(),urp);
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

  private RationalFloatsN (final int dimension) { super(dimension); }

  private static final IntObjectMap<RationalFloatsN> _cache =
    new IntObjectHashMap();

  public static final RationalFloatsN get (final int dimension) {
    final RationalFloatsN s0 = _cache.get(dimension);
    if (null != s0) { return s0; }
    final RationalFloatsN s1 = new RationalFloatsN(dimension);
    _cache.put(dimension,s1);
    return s1; }

  //--------------------------------------------------------------

  public static final OneSetOneOperation group (final int n) {
    final RationalFloatsN g = get(n);
    return OneSetOneOperation.commutativeGroup(
      g.adder(),
      g,
      g.additiveIdentity(),
      g.additiveInverse()); }

  //--------------------------------------------------------------
  /** n-dimensional rational vector space, implemented with
   * <code>Rational[n]</code>.
   */

  private static final TwoSetsOneOperation
  makeSpace (final int n) {
    return
      TwoSetsOneOperation.linearSpaceLike(
        get(n).scaler(),
        group(n),
        RationalFloats.FIELD); }

  private static final IntObjectMap<TwoSetsOneOperation>
  _spaceCache = new IntObjectHashMap();

  /** n-dimensional rational vector space, implemented with
   * <code>Rational[]</code>.
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

