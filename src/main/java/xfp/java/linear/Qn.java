package xfp.java.linear;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.rng.UniformRandomProvider;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;

import xfp.java.algebra.Set;
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
 * @version 2019-02-21
 */
@SuppressWarnings("unchecked")
public final class Qn implements Set {

  //--------------------------------------------------------------

  private final int _dimension;
  public final int dimension () { return _dimension; }

  //--------------------------------------------------------------
  // operations for algebraic structures over BigFraction arrays.
  //--------------------------------------------------------------
  /** A <code>BinaryOperator</code> that adds elementwise
   * <code>BigFraction[]</code> instances of length 
   * <code>dimension</code>.
   */

  private final BigFraction[] add (final Object x0, 
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

  public final BinaryOperator adder () {
    return
      new BinaryOperator() {
      @Override
      public final Object apply (final Object q0, 
                                 final Object q1) {
        return Qn.this.add(q0,q1); } }; }

  //--------------------------------------------------------------

  public final BigFraction[] additiveIdentity () {
    final BigFraction[] qq = new BigFraction[dimension()];
    Arrays.fill(qq,BigFraction.ZERO);
    return qq; }

  //--------------------------------------------------------------

  private final BigFraction[] negate (final Object x) {
    assert contains(x);
    final BigFraction[] q = 
      (BigFraction[]) BigFractions.toBigFraction(x);
    final BigFraction[] qq = new BigFraction[dimension()];
    for (int i=0;i<dimension();i++) { qq[i] = q[i].negate(); }
    return qq; } 

  public final UnaryOperator additiveInverse () {
    return 
      new UnaryOperator () {
      @Override
      public final Object apply (final Object q) {
        return Qn.this.negate(q); } }; }

  //--------------------------------------------------------------

  private final BigFraction[] scale (final Object a, 
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

  public final BiFunction 
  scaler () {
    return
      new BiFunction() {
      @Override
      public final Object apply (final Object a, 
                                 final Object q) {
        return Qn.this.scale(a,q); } }; }

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  private final boolean equals (final Object x0, 
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

  @Override
  public final BiPredicate equivalence () { 
    return new BiPredicate<BigFraction[],BigFraction[]>() {

      @Override
      public final boolean test (final BigFraction[] q0, 
                                 final BigFraction[] q1) {
        return Qn.this.equals(q0,q1); } }; }

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
  public final int hashCode () { return dimension(); }

  // singleton
  @Override
  public final boolean equals (final Object that) {
    return 
      (that instanceof Qn)
      &&
      (dimension() == ((Qn) that).dimension()); }

  @Override
  public final String toString () { return "Q^" + dimension(); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  // TODO: support zero-dimensional space?

  private Qn (final int dimension) { 
    assert dimension > 0;
    _dimension = dimension; }

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

