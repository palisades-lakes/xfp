package xfp.java.linear;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.rng.UniformRandomProvider;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;

import xfp.java.numbers.BigFractions;
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
 * @version 2019-02-22
 */
@SuppressWarnings("unchecked")
public final class Qn extends LinearSpaceLike {

  //--------------------------------------------------------------
  // operations for algebraic structures over BigFraction arrays.
  //--------------------------------------------------------------
  /** A <code>BinaryOperator</code> that adds elementwise
   * <code>BigFraction[]</code> instances of length 
   * <code>dimension</code>.
   */

  @Override
  public final Object add (final Object x0, 
                           final Object x1) {
    assert contains(x0);
    assert contains(x1);
    final BigFraction[] q0 = 
      (BigFraction[]) BigFractions.toBigFraction(x0);
    final BigFraction[] q1 = 
      (BigFraction[]) BigFractions.toBigFraction(x1);
    final BigFraction[] qq = new BigFraction[dimension()];
    for (int i=0;i<dimension();i++) { qq[i] = q0[i].add(q1[i]); }
    return qq; }

  //--------------------------------------------------------------

  @Override
  public final Object zero (final int n) {
    final BigFraction[] qq = new BigFraction[n];
    Arrays.fill(qq,BigFraction.ZERO);
    return qq; }

  //--------------------------------------------------------------

  @Override
  public final BigFraction[] negate (final Object x) {
    assert contains(x);
    final BigFraction[] q = 
      (BigFraction[]) BigFractions.toBigFraction(x);
    final BigFraction[] qq = new BigFraction[dimension()];
    for (int i=0;i<dimension();i++) { qq[i] = q[i].negate(); }
    return qq; } 

  //--------------------------------------------------------------

  @Override
  public final Object scale (final Object a, 
                             final Object x) {
    assert contains(x);
    final BigFraction b = 
      (BigFraction) BigFractions.toBigFraction(a);
    final BigFraction[] q = 
      (BigFraction[]) BigFractions.toBigFraction(x);
    final BigFraction[] qq = new BigFraction[dimension()];
    for (int i=0;i<dimension();i++) { 
      qq[i] = q[i].multiply(b); }
    return qq; } 

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean equals (final Object x0, 
                               final Object x1) {
    assert contains(x0);
    assert contains(x1);
    final BigFraction[] q0 = 
      (BigFraction[]) BigFractions.toBigFraction(x0);
    final BigFraction[] q1 = 
      (BigFraction[]) BigFractions.toBigFraction(x1);
    for (int i=0;i<dimension();i++) {
      if (! BigFractions.get().equals(q0[i],q1[i])) {
        return false; } }
    return true; }

  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    if (null == element) { return false; }
    final Class c = element.getClass();
    if (! c.isArray()) { return false; }
    if (! Q.knownRational(c.getComponentType())) { return false; }
    return Array.getLength(element) == dimension(); }

  //--------------------------------------------------------------
  /** Intended primarily for testing. 
   */

  @Override
  public final Supplier generator (final UniformRandomProvider urp,
                                   final Map options) {
    return 
      new Supplier () {
      final Generator g = Generators.qnGenerator(dimension(),urp);
      @Override
      public final Object get () { return g.next(); } }; }

  @Override
  public final Supplier generator (final UniformRandomProvider urp) {
    return generator(urp,Collections.emptyMap()); }

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
    final Qn dn0 = _cache.get(dimension);
    if (null != dn0) { return dn0; }
    final Qn dn1 = new Qn(dimension); 
    _cache.put(dimension,dn1);
    return dn1; }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

