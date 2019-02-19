package xfp.java.sets;

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

import xfp.java.sets.BigFractions;
import xfp.java.sets.BigFractionsN;
import xfp.java.sets.Set;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;

/** The set of instances of <code>BigFraction[dimension]</code>).
 * 
 * TODO: generalize to tuples of <code>BigFraction</code>
 * implemented with lists, <code>int</code> indexed maps
 * for sparse vectors, etc.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-01-29
 */
@SuppressWarnings("unchecked")
public final class BigFractionsN implements Set {

  private final int _dimension;
  public final int dimension () { return _dimension; }

  private final BiPredicate<BigFraction[],BigFraction[]> 
  _equivalence =
  new BiPredicate<BigFraction[],BigFraction[]>() {

    // BigFraction.equals reduces both arguments before checking
    // numerator and denominators are equal.
    // Guessing our BigFractions are usually already reduced.
//     @Override
//    public final boolean test (final BigFraction[] q0, 
//                               final BigFraction[] q1) {
//      assert null != q0;
//      assert null != q1;
//      assert _dimension == q0.length;
//      assert _dimension == q1.length;
//      return Arrays.deepEquals(q0,q1); }
    @Override
    public final boolean test (final BigFraction[] q0, 
                               final BigFraction[] q1) {
      assert null != q0;
      assert null != q1;
      assert _dimension == q0.length;
      assert _dimension == q1.length;
      for (int i=0;i<_dimension;i++) {
        if (! BigFractions.equalBigFractions(q0[i],q1[i])) {
          return false; } }
      return true;
      }
  };

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    return 
      (element instanceof BigFraction[])
      &&
      ((BigFraction[]) element).length == _dimension; }


  @Override
  public final BiPredicate equivalence () { return _equivalence; }

  //--------------------------------------------------------------
  /** Intended primarily for testing. 
   */
  @Override
  public final Supplier generator (final UniformRandomProvider urp,
                                   final Map options) {
    return 
      new Supplier () {
      final Generator bf = 
        Generators.bigFractionGenerator(_dimension,urp);
        @Override
      public final Object get () { return bf.next(); } }; }

  @Override
  public final Supplier generator (final UniformRandomProvider urp) {
    return generator(urp,Collections.emptyMap()); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { return 0; }

  // singleton
  @Override
  public final boolean equals (final Object that) {
    return that instanceof BigFractionsN; }

  @Override
  public final String toString () { return "BF^" + _dimension; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  // TODO: support zero-dimensional space?
  
  private BigFractionsN (final int dimension) { 
    assert dimension > 0;
    _dimension = dimension; }

  private static final IntObjectMap<BigFractionsN> _cache = 
    new IntObjectHashMap();

  public static final BigFractionsN get (final int dimension) {
    final BigFractionsN dn0 = _cache.get(dimension);
    if (null != dn0) { return dn0; }
    final BigFractionsN dn1 = new BigFractionsN(dimension); 
    _cache.put(dimension,dn1);
    return dn1; }

  //--------------------------------------------------------------
  // operations for algebraic structures over BigFraction tuples.
  //--------------------------------------------------------------
  /** A <code>BinaryOperator</code> that adds elementwise
   * <code>BigFraction[]</code> instances of length 
   * <code>dimension</code>.
   */

  public static final BinaryOperator<BigFraction[]> 
  adder (final int dimension) {
    assert dimension > 0;
    return
      new BinaryOperator<BigFraction[]>() {
        @Override
        public final BigFraction[] apply (final BigFraction[] q0, 
                                          final BigFraction[] q1) {
          assert null != q0;
          assert null != q1;
          assert dimension == q0.length;
          assert dimension == q1.length;
          final BigFraction[] qq = new BigFraction[dimension];
          for (int i=0;i<dimension;i++) { qq[i] = q0[i].add(q1[i]); }
          return qq; } }; }

  // TODO: special sparse representation for zero vector?

  public static final BigFraction[] 
    additiveIdentity (final int dimension) {
    assert dimension > 0;
    final BigFraction[] qq = new BigFraction[dimension];
    Arrays.fill(qq,BigFraction.ZERO);
    return qq; }

  public static final UnaryOperator<BigFraction[]>
  additiveInverse (final int dimension) {
    assert dimension > 0;
    return 
      new UnaryOperator<BigFraction[]>() {
        @Override
        public final BigFraction[] apply (final BigFraction[] q) {
          assert null != q;
          assert dimension == q.length;
          final BigFraction[] qq = new BigFraction[dimension];
          for (int i=0;i<dimension;i++) { qq[i] = q[i].negate(); }
          return qq; } }; }

  public static final 
  BiFunction<BigFraction,BigFraction[],BigFraction[]> 
  scaler (final int dimension) {
    assert dimension > 0;
    return
      new BiFunction<BigFraction,BigFraction[],BigFraction[]>() {
        @Override
        public final BigFraction[] apply (final BigFraction a, 
                                          final BigFraction[] q) {
          assert null != q;
          assert dimension == q.length;
          final BigFraction[] qq = new BigFraction[dimension];
          for (int i=0;i<dimension;i++) { qq[i] = q[i].multiply(a); }
          return qq; } }; }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

