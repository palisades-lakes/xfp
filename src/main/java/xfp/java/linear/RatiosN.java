package xfp.java.linear;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.apache.commons.rng.UniformRandomProvider;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;

import clojure.lang.Ratio;
import xfp.java.algebra.Set;
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
 * @version 2019-02-19
 */
@SuppressWarnings("unchecked")
public final class RatiosN implements Set {

  private final int _dimension;
  public final int dimension () { return _dimension; }

  private final BiPredicate<Ratio[],Ratio[]> 
  _equivalence =
  new BiPredicate<Ratio[],Ratio[]>() {
    @Override
    public final String toString () { return "RatioN.equals"; }

    // Ratio.equals reduces both arguments before checking
    // numerator and denominators are equal.
    // Guessing our Ratios are usually already reduced.
    //     @Override
    //    public final boolean test (final Ratio[] q0, 
    //                               final Ratio[] q1) {
    //      assert null != q0;
    //      assert null != q1;
    //      assert _dimension == q0.length;
    //      assert _dimension == q1.length;
    //      return Arrays.deepEquals(q0,q1); }
    @Override
    public final boolean test (final Ratio[] q0, 
                               final Ratio[] q1) {
      assert null != q0;
      assert null != q1;
      assert _dimension == q0.length;
      assert _dimension == q1.length;
      for (int i=0;i<_dimension;i++) {
        if (! Ratios.equalRatios(q0[i],q1[i])) {
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
      (element instanceof Ratio[])
      &&
      ((Ratio[]) element).length == _dimension; }

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
      final Generator g = Generators.ratioGenerator(_dimension,urp);
      @Override
      public final Object get () { return g.next(); } }; }

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
    return that instanceof RatiosN; }

  @Override
  public final String toString () { return "Ratio^" + _dimension; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  // TODO: support zero-dimensional space?

  private RatiosN (final int dimension) { 
    assert dimension > 0;
    _dimension = dimension; }

  private static final IntObjectMap<RatiosN> _cache = 
    new IntObjectHashMap();

  public static final RatiosN get (final int dimension) {
    final RatiosN dn0 = _cache.get(dimension);
    if (null != dn0) { return dn0; }
    final RatiosN dn1 = new RatiosN(dimension); 
    _cache.put(dimension,dn1);
    return dn1; }

  //--------------------------------------------------------------
  // operations for algebraic structures over Ratio tuples.
  //--------------------------------------------------------------
  /** A <code>BinaryOperator</code> that adds elementwise
   * <code>Ratio[]</code> instances of length 
   * <code>dimension</code>.
   */

  public static final BinaryOperator<Ratio[]> 
  adder (final int dimension) {
    assert dimension > 0;
    return
      new BinaryOperator<Ratio[]>() {
        @Override
        public final String toString () { return "RatioN.add"; }
        @Override
        public final Ratio[] apply (final Ratio[] q0, 
                                    final Ratio[] q1) {
          assert null != q0;
          assert null != q1;
          assert dimension == q0.length;
          assert dimension == q1.length;
          final Ratio[] qq = new Ratio[dimension];
          for (int i=0;i<dimension;i++) { 
            qq[i] = Ratios.add(q0[i],q1[i]); }
          return qq; } }; }

  // TODO: special sparse representation for zero vector?

  public static final Ratio[] 
    additiveIdentity (final int dimension) {
    assert dimension > 0;
    final Ratio[] qq = new Ratio[dimension];
    Arrays.fill(qq,Ratios.ZERO);
    return qq; }

  public static final UnaryOperator<Ratio[]>
  additiveInverse (final int dimension) {
    assert dimension > 0;
    return 
      new UnaryOperator<Ratio[]>() {
        @Override
        public final Ratio[] apply (final Ratio[] q) {
          assert null != q;
          assert dimension == q.length;
          final Ratio[] qq = new Ratio[dimension];
          for (int i=0;i<dimension;i++) { 
            qq[i] = Ratios.negate(q[i]); }
          return qq; } }; }

  public static final 
  BiFunction<Ratio,Ratio[],Ratio[]> 
  scaler (final int dimension) {
    assert dimension > 0;
    return
      new BiFunction<Ratio,Ratio[],Ratio[]>() {
        @Override
        public final String toString () { return "RatioN.scale"; }
        @Override
        public final Ratio[] apply (final Ratio a, 
                                    final Ratio[] q) {
          assert null != q;
          assert dimension == q.length;
          final Ratio[] qq = new Ratio[dimension];
          for (int i=0;i<dimension;i++) { 
            qq[i] = Ratios.multiply(q[i],a); }
          return qq; } }; }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

