package xfp.java.linear;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.rng.UniformRandomProvider;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;

import clojure.lang.Ratio;
import xfp.java.algebra.OneSetOneOperation;
import xfp.java.algebra.Set;
import xfp.java.algebra.TwoSetsOneOperation;
import xfp.java.numbers.Ratios;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;

/** The set of instances of <code>Ratio[dimension]</code>).
 * 
 * TODO: generalize to tuples of <code>Ratio</code>
 * implemented with lists, <code>int</code> indexed maps
 * for sparse vectors, etc.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-07
 */
@SuppressWarnings("unchecked")
public final class RatiosN extends LinearSpaceLike {

  //--------------------------------------------------------------
  // operations for algebraic structures over Ratio arrays.
  //--------------------------------------------------------------
  /** A <code>BinaryOperator</code> that adds elementwise
   * <code>Ratio[]</code> instances of length 
   * <code>dimension</code>.
   */

  private final Ratio[] add (final Ratio[] q0, 
                             final Ratio[] q1) {
    assert contains(q0);
    assert contains(q1);
    final Ratio[] qq = new Ratio[dimension()];
    for (int i=0;i<dimension();i++) { 
      qq[i] = Ratios.get().add(q0[i],q1[i]); }
    return qq; }

  @Override
  public final Object add (final Object q0, 
                           final Object q1) {
    return add((Ratio[]) q0, (Ratio[]) q1); }

  //--------------------------------------------------------------

  private static final Ratio ZERO = 
    new Ratio(BigInteger.ZERO,BigInteger.ONE);
  
  @Override
  public final Object zero (final int n) {
    final Ratio[] z = new Ratio[n];
    Arrays.fill(z,ZERO);
    return z; }

  //--------------------------------------------------------------

  private final Ratio[] negate (final Ratio[] q) {
    assert contains(q);
    final Ratio[] qq = new Ratio[dimension()];
    for (int i=0;i<dimension();i++) { 
      qq[i] = Ratios.get().negate(q[i]); }
    return qq; } 

  @Override
  public final Object negate (final Object q) {
    return negate((Ratio[]) q); } 

  //--------------------------------------------------------------

  private final Ratio[] scale (final Ratio a, 
                               final Ratio[] q) {
    assert contains(q);
    final Ratio[] qq = new Ratio[dimension()];
    for (int i=0;i<dimension();i++) { 
      qq[i] = Ratios.get().multiply(q[i],a); }
    return qq; } 

  @Override
  public final Object scale (final Object a, 
                             final Object q) {
    return scale((Ratio) a, (Ratio[]) q); } 

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  private final boolean equals (final Ratio[] q0, 
                                final Ratio[] q1) {
    assert null != q0;
    assert null != q1;
    assert dimension() == q0.length;
    assert dimension() == q1.length;
    for (int i=0;i<dimension();i++) {
      if (! Ratios.get().equals(q0[i],q1[i])) {
        return false; } }
    return true; }

  @Override
  public final boolean equals (final Object q0,
                               final Object q1) {
    return equals((Ratio[]) q0, (Ratio[]) q1); }
  
  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    return 
      (element instanceof Ratio[])
      &&
      ((Ratio[]) element).length == dimension(); }

  //--------------------------------------------------------------
  /** Intended primarily for testing. 
   */
  @Override
  public final Supplier generator (final Map options) {
    final UniformRandomProvider urp = Set.urp(options);
    return 
      new Supplier () {
      final Generator g = Generators.ratioGenerator(dimension(),urp);
      @Override
      public final Object get () { return g.next(); } }; }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final String toString () { return "Ratio^" + dimension(); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  // TODO: support zero-dimensional space?

  private RatiosN (final int dimension) { 
    super(dimension); }

  private static final IntObjectMap<RatiosN> _cache = 
    new IntObjectHashMap();

  public static final RatiosN get (final int dimension) {
    final RatiosN dn0 = _cache.get(dimension);
    if (null != dn0) { return dn0; }
    final RatiosN dn1 = new RatiosN(dimension); 
    _cache.put(dimension,dn1);
    return dn1; }

  //--------------------------------------------------------------

  public static final OneSetOneOperation 
  group (final int n) {
    final RatiosN ratioN = get(n);
    return OneSetOneOperation.commutativeGroup(
        ratioN.adder(),
        ratioN,
        ratioN.additiveIdentity(),
        ratioN.additiveInverse()); }

  //--------------------------------------------------------------
  /** n-dimensional rational vector space, implemented with
   * <code>Ratio[n]</code>.
   */

  private static final TwoSetsOneOperation 
  makeSpace (final int n) { 
    return
      TwoSetsOneOperation.linearSpaceLike(
        RatiosN.get(n).scaler(),
        RatiosN.group(n),
        Ratios.FIELD); }

  private static final IntObjectMap<TwoSetsOneOperation> 
  _spaceCache = new IntObjectHashMap();

  /** n-dimensional rational vector space, implemented with
   * <code>Ratio[]</code>.
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

