package xfp.java.linear;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.rng.UniformRandomProvider;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;

import xfp.java.algebra.OneSetOneOperation;
import xfp.java.algebra.Set;
import xfp.java.algebra.TwoSetsOneOperation;
import xfp.java.numbers.Doubles;
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
strictfp
public final class Dn extends LinearSpaceLike {

  //--------------------------------------------------------------
  // operations on arrays of double
  // TODO: better elsewhere?
  //--------------------------------------------------------------
  
  public static final double[] concatenate (final double[] x0,
                                            final double[] x1) {
    final double[] x = new double[x0.length + x1.length];
    for (int i=0;i<x0.length;i++) { x[i] = x0[i]; }
    for (int i=0;i<x1.length;i++) { x[i+x0.length] = x1[i]; }
    return x; }
  
  //--------------------------------------------------------------

  public static final double[] minus (final double[] x) {
    final double[] y = new double[x.length];
    for (int i=0;i<x.length;i++) { y[i] = -x[i]; }
    return y; }
  
  //--------------------------------------------------------------

  public static final double l1Dist (final double[] x0,
                                     final double[] x1) {
    final int n = x0.length;
    assert n == x1.length;
    double m = NEGATIVE_INFINITY;
    for (int i=0;i<n;i++) { 
      m = Math.max(m,Math.abs(x0[i]-x1[i])); }
    return m; }
  
  //--------------------------------------------------------------

  public static final double maxAbs (final double[] x) {
    double m = NEGATIVE_INFINITY;
    for (int i=0;i<x.length;i++) { 
      m = Math.max(m,Math.abs(x[i])); }
    return m; }
  
  //--------------------------------------------------------------

  public static final double max (final double[] x) {
    double m = NEGATIVE_INFINITY;
    for (int i=0;i<x.length;i++) { m = Math.max(m,x[i]); }
    return m; }
  
  //--------------------------------------------------------------

  public static final double min (final double[] x) {
    double m = POSITIVE_INFINITY;
    for (int i=0;i<x.length;i++) { m = Math.min(m,x[i]); }
    return m; }
  
  //--------------------------------------------------------------

  public static final double fmaDot (final double[] x0,
                                     final double[] x1) {
    final int n = x0.length;
    assert n == x1.length;
    if (0 == n) { return 0.0; }
    double sum = x0[0] * x1[0];
    for (int i=1;i<x0.length;i++) { 
      sum = Math.fma(x0[i],x1[i],sum); }
    return sum; }

  //--------------------------------------------------------------
  // operations for algebraic structures over double[] arrays.
  //--------------------------------------------------------------

  public final double[] add (final double[] x0, 
                             final double[] x1) {
    assert contains(x0);
    assert contains(x1);
    final double[] qq = new double[dimension()];
    for (int i=0;i<dimension();i++) { qq[i] = x0[i] + x1[i]; }
    return qq; }

  @Override
  public final double[] add (final Object x0, 
                             final Object x1) {
    return add((double[]) x0, (double[]) x1); }

  //--------------------------------------------------------------

  @Override
  public final double[] zero (final int n) {
    final double[] qq = new double[n];
    Arrays.fill(qq,0.0);
    return qq; }

  //--------------------------------------------------------------

  public final double[] negate (final double[] x) {
    assert contains(x);
    return minus(x); } 

  @Override
  public final double[] negate (final Object x) {
    return negate((double[]) x); } 

  //--------------------------------------------------------------

  public final double[] scale (final double a, 
                               final double[] x) {
    assert contains(x);
    final double[] qq = new double[dimension()];
    for (int i=0;i<dimension();i++) { 
      qq[i] = a * x[i]; }
    return qq; } 

  @Override
  public final double[] scale (final Object a, 
                               final Object x) {
    return scale(((Number) a).doubleValue(), (double[]) x); } 

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean equals (final Object x0, 
                               final Object x1) {
    assert contains(x0);
    assert contains(x1);
    return Arrays.equals((double[]) x0, (double[]) x1); }

  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    if (null == element) { return false; }
    final Class c = element.getClass();
    if (! c.isArray()) { return false; }
    if (! Double.TYPE.equals((c.getComponentType()))) { return false; }
    return Array.getLength(element) == dimension(); }

  //--------------------------------------------------------------
  /** Intended primarily for testing. 
   */

  @Override
  public final Supplier generator (final Map options) {
    final UniformRandomProvider urp = Set.urp(options);
    return 
      new Supplier () {
      final Generator g = 
        Generators.finiteDoubleGenerator(dimension(),urp);
      @Override
      public final Object get () { return g.next(); } }; }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final String toString () { return "D^" + dimension(); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  // TODO: support zero-dimensional space?

  private Dn (final int dimension) { super(dimension); }

  private static final IntObjectMap<Dn> _cache = 
    new IntObjectHashMap();

  public static final Dn get (final int dimension) {
    final Dn dn0 = _cache.get(dimension);
    if (null != dn0) { return dn0; }
    final Dn dn1 = new Dn(dimension); 
    _cache.put(dimension,dn1);
    return dn1; }

  //--------------------------------------------------------------

  public static final OneSetOneOperation magma (final int n) {
    final Dn g = get(n);
    return OneSetOneOperation.magma(g.adder(),g); }

  //--------------------------------------------------------------
  /** n-dimensional rational vector space, implemented with
   * any known rational array.
   */

  private static final TwoSetsOneOperation 
  makeSpace (final int n) { 
    return
      TwoSetsOneOperation.floatingPointSpace(
        Dn.get(n).scaler(),
        Dn.magma(n),
        Doubles.FLOATING_POINT); }

  private static final IntObjectMap<TwoSetsOneOperation> 
  _spaceCache = new IntObjectHashMap();

  /** n-dimensional floating point space, implemented with
   * <code>double[]</code>.
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

