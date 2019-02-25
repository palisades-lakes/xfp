package xfp.java.linear;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.rng.UniformRandomProvider;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;

import xfp.java.algebra.OneSetOneOperation;
import xfp.java.algebra.TwoSetsOneOperation;
import xfp.java.numbers.Floats;
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
 * @version 2019-02-25
 */
@SuppressWarnings("unchecked")
public final class Fn extends LinearSpaceLike {

  //--------------------------------------------------------------
  // operations for algebraic structures over float[] arrays.
  //--------------------------------------------------------------

  public final float[] add (final float[] x0, 
                           final float[] x1) {
    assert contains(x0);
    assert contains(x1);
    final float[] qq = new float[dimension()];
    for (int i=0;i<dimension();i++) { qq[i] = x0[i] + x1[i]; }
    return qq; }

  @Override
  public final float[] add (final Object x0, 
                           final Object x1) {
    return add((float[]) x0, (float[]) x1); }
  
  //--------------------------------------------------------------

  @Override
  public final float[] zero (final int n) {
    final float[] qq = new float[n];
    Arrays.fill(qq,0.0F);
    return qq; }

  //--------------------------------------------------------------

  public final float[] negate (final float[] x) {
    assert contains(x);
    final float[] qq = new float[dimension()];
    for (int i=0;i<dimension();i++) { qq[i] = - x[i]; }
    return qq; } 

  @Override
  public final float[] negate (final Object x) {
    return negate((float[]) x); } 

  //--------------------------------------------------------------

  public final float[] scale (final float a, 
                             final float[] x) {
    assert contains(x);
    final float[] qq = new float[dimension()];
    for (int i=0;i<dimension();i++) { 
      qq[i] = a * x[i]; }
    return qq; } 

  @Override
  public final float[] scale (final Object a, 
                             final Object x) {
    return scale(((Number) a).floatValue(), (float[]) x); } 

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean equals (final Object x0, 
                               final Object x1) {
    assert contains(x0);
    assert contains(x1);
    return Arrays.equals((float[]) x0, (float[]) x1); }

  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    if (null == element) { return false; }
    final Class c = element.getClass();
    if (! c.isArray()) { return false; }
    if (! Float.TYPE.equals((c.getComponentType()))) { return false; }
    return Array.getLength(element) == dimension(); }

  //--------------------------------------------------------------
  /** Intended primarily for testing. 
   */

  @Override
  public final Supplier generator (final UniformRandomProvider urp,
                                   final Map options) {
    return 
      new Supplier () {
      final Generator g = 
        Generators.finiteFloatGenerator(dimension(),urp);
      @Override
      public final Object get () { return g.next(); } }; }

  @Override
  public final Supplier generator (final UniformRandomProvider urp) {
    return generator(urp,Collections.emptyMap()); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final String toString () { return "F^" + dimension(); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  // TODO: support zero-dimensional space?

  private Fn (final int dimension) { super(dimension); }

  private static final IntObjectMap<Fn> _cache = 
    new IntObjectHashMap();

  public static final Fn get (final int dimension) {
    final Fn dn0 = _cache.get(dimension);
    if (null != dn0) { return dn0; }
    final Fn dn1 = new Fn(dimension); 
    _cache.put(dimension,dn1);
    return dn1; }

  //--------------------------------------------------------------

  public static final OneSetOneOperation magma (final int n) {
    final Fn g = get(n);
    return OneSetOneOperation.magma(g.adder(),g); }

  //--------------------------------------------------------------
  /** n-dimensional rational vector space, implemented with
   * any known rational array.
   */

  private static final TwoSetsOneOperation 
  makeSpace (final int n) { 
    return
      TwoSetsOneOperation.floatingPointSpace(
        Fn.get(n).scaler(),
        Fn.magma(n),
        Floats.FLOATING_POINT); }

  private static final IntObjectMap<TwoSetsOneOperation> 
  _spaceCache = new IntObjectHashMap();

  /** n-dimensional floating point space, implemented with
   * <code>float[]</code>.
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

