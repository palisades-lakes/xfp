package xfp.java.linear;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.rng.UniformRandomProvider;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;
import com.upokecenter.numbers.ERational;

import xfp.java.algebra.OneSetOneOperation;
import xfp.java.algebra.Set;
import xfp.java.algebra.TwoSetsOneOperation;
import xfp.java.numbers.ERationals;
import xfp.java.numbers.Q;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;

/** The set of arrays of some fixed length <code>n</code>,
 *  of primitive numbers, or 
 * of certain subclasses of <code>Number</code>,
 * interpreted as tuples of rational numbers.
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
public final class Qn extends LinearSpaceLike {

  //--------------------------------------------------------------
  // operations for algebraic structures over rational arrays.
  //--------------------------------------------------------------
  /** A <code>BinaryOperator</code> that adds elementwise
   * <code>ERational[]</code> instances of length 
   * <code>dimension</code>.
   */

  @Override
  public final Object add (final Object x0, 
                           final Object x1) {
    assert contains(x0);
    assert contains(x1);
    final ERational[] q0 = ERationals.toERationalArray(x0);
    final ERational[] q1 = ERationals.toERationalArray(x1);
    final ERational[] qq = new ERational[dimension()];
    for (int i=0;i<dimension();i++) { qq[i] = q0[i].Add(q1[i]); }
    return qq; }

  //--------------------------------------------------------------

  @Override
  public final ERational[] zero (final int n) {
    final ERational[] z = new ERational[n];
    Arrays.fill(z,ERational.Zero);
    return z; }

  //--------------------------------------------------------------

  @Override
  public final ERational[] negate (final Object x) {
    assert contains(x);
    final ERational[] q = 
      ERationals.toERationalArray(x);
    final ERational[] qq = new ERational[dimension()];
    for (int i=0;i<dimension();i++) { qq[i] = q[i].Negate(); }
    return qq; } 

  //--------------------------------------------------------------

  @Override
  public final ERational[] scale (final Object a, 
                                  final Object x) {
    assert contains(x);
    final ERational b = 
      ERationals.toERational(a);
    final ERational[] q = ERationals.toERationalArray(x);
    final ERational[] qq = new ERational[dimension()];
    for (int i=0;i<dimension();i++) { qq[i] = q[i].Multiply(b); }
    return qq; } 

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean equals (final Object x0, 
                               final Object x1) {
    assert contains(x0);
    assert contains(x1);
    final ERational[] q0 = ERationals.toERationalArray(x0);
    final ERational[] q1 = ERationals.toERationalArray(x1);
    for (int i=0;i<dimension();i++) {
      if (! ERationals.get().equals(q0[i],q1[i])) {
        return false; } }
    return true; }

  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    if (null == element) { return false; }
    final Class c = element.getClass();
    if (! c.isArray()) { return false; }
    final int n = Array.getLength(element);
    if (n != dimension()) { return false; }
    if (Q.knownRational(c.getComponentType())) { return true; }
    for (int i=0;i<n;i++) {
      if (! Q.get().contains(Array.get(element,i))) {
        return false; } }
    return true; }

  //--------------------------------------------------------------
  /** Intended primarily for testing. 
   */

  @Override
  public final Supplier generator (final Map options) {
    final UniformRandomProvider urp = Set.urp(options);
    return 
      new Supplier () {
      final Generator g =
        Generators.eRationalFromDoubleGenerator(dimension(),urp);
      //Generators.qnGenerator(dimension(),urp);
      @Override
      public final Object get () { return g.next(); } }; }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final String toString () { return "Q^" + dimension(); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  // TODO: support zero-dimensional space?

  private Qn (final int dimension) { super(dimension); }

  private static final IntObjectMap<Qn> _cache = 
    new IntObjectHashMap();

  public static final Qn get (final int dimension) {
    final Qn s0 = _cache.get(dimension);
    if (null != s0) { return s0; }
    final Qn s1 = new Qn(dimension); 
    _cache.put(dimension,s1);
    return s1; }

  //--------------------------------------------------------------

  public static final OneSetOneOperation group (final int n) {
    final Qn g = get(n);
    return OneSetOneOperation.commutativeGroup(
      g.adder(),
      g,
      g.additiveIdentity(),
      g.additiveInverse()); }

  //--------------------------------------------------------------
  /** n-dimensional rational vector space, implemented with
   * any known rational array.
   */

  private static final TwoSetsOneOperation 
  makeSpace (final int n) { 
    return
      TwoSetsOneOperation.linearSpaceLike(
        get(n).scaler(),
        group(n),
        Q.FIELD); }

  private static final IntObjectMap<TwoSetsOneOperation> 
  _spaceCache = new IntObjectHashMap();

  /** n-dimensional rational vector space, implemented with
   * <code>ERational[]</code>.
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

