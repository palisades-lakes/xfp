package xfp.java.linear;

import java.math.BigDecimal;
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

import xfp.java.algebra.Set;
import xfp.java.numbers.BigDecimals;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;

/** The set of instances of <code>BigDecimal[dimension]</code>).
 * 
 * TODO: generalize to tuples of <code>BigDecimal</code>
 * implemented with lists, <code>int</code> indexed maps
 * for sparse vectors, etc.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-19
 */
@SuppressWarnings("unchecked")
public final class BigDecimalsN implements Set {

  private final int _dimension;
  public final int dimension () { return _dimension; }

  private final BiPredicate<BigDecimal[],BigDecimal[]> 
  _equivalence =
  new BiPredicate<BigDecimal[],BigDecimal[]>() {

    // BigDecimal.equals reduces both arguments before checking
    // numerator and denominators are equal.
    // Guessing our BigDecimals are usually already reduced.
    //     @Override
    //    public final boolean test (final BigDecimal[] q0, 
    //                               final BigDecimal[] q1) {
    //      assert null != q0;
    //      assert null != q1;
    //      assert _dimension == q0.length;
    //      assert _dimension == q1.length;
    //      return Arrays.deepEquals(q0,q1); }
    @Override
    public final boolean test (final BigDecimal[] q0, 
                               final BigDecimal[] q1) {
      assert null != q0;
      assert null != q1;
      assert _dimension == q0.length;
      assert _dimension == q1.length;
      for (int i=0;i<_dimension;i++) {
        if (! BigDecimals.equalBigDecimals(q0[i],q1[i])) {
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
      (element instanceof BigDecimal[])
      &&
      ((BigDecimal[]) element).length == _dimension; }


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
        Generators.bigDecimalGenerator(_dimension,urp);
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
    return that instanceof BigDecimalsN; }

  @Override
  public final String toString () { return "BF^" + _dimension; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  // TODO: support zero-dimensional space?

  private BigDecimalsN (final int dimension) { 
    assert dimension > 0;
    _dimension = dimension; }

  private static final IntObjectMap<BigDecimalsN> _cache = 
    new IntObjectHashMap();

  public static final BigDecimalsN get (final int dimension) {
    final BigDecimalsN dn0 = _cache.get(dimension);
    if (null != dn0) { return dn0; }
    final BigDecimalsN dn1 = new BigDecimalsN(dimension); 
    _cache.put(dimension,dn1);
    return dn1; }

  //--------------------------------------------------------------
  // operations for algebraic structures over BigDecimal tuples.
  //--------------------------------------------------------------
  /** A <code>BinaryOperator</code> that adds elementwise
   * <code>BigDecimal[]</code> instances of length 
   * <code>dimension</code>.
   */

  public static final BinaryOperator<BigDecimal[]> 
  adder (final int dimension) {
    assert dimension > 0;
    return
      new BinaryOperator<BigDecimal[]>() {
        @Override
        public final BigDecimal[] apply (final BigDecimal[] q0, 
                                         final BigDecimal[] q1) {
          assert null != q0;
          assert null != q1;
          assert dimension == q0.length;
          assert dimension == q1.length;
          final BigDecimal[] qq = new BigDecimal[dimension];
          for (int i=0;i<dimension;i++) { qq[i] = q0[i].add(q1[i]); }
          return qq; } }; }

  // TODO: special sparse representation for zero vector?

  public static final BigDecimal[] 
    additiveIdentity (final int dimension) {
    assert dimension > 0;
    final BigDecimal[] qq = new BigDecimal[dimension];
    Arrays.fill(qq,BigDecimal.ZERO);
    return qq; }

  public static final UnaryOperator<BigDecimal[]>
  additiveInverse (final int dimension) {
    assert dimension > 0;
    return 
      new UnaryOperator<BigDecimal[]>() {
        @Override
        public final BigDecimal[] apply (final BigDecimal[] q) {
          assert null != q;
          assert dimension == q.length;
          final BigDecimal[] qq = new BigDecimal[dimension];
          for (int i=0;i<dimension;i++) { qq[i] = q[i].negate(); }
          return qq; } }; }

  public static final 
  BiFunction<BigDecimal,BigDecimal[],BigDecimal[]> 
  scaler (final int dimension) {
    assert dimension > 0;
    return
      new BiFunction<BigDecimal,BigDecimal[],BigDecimal[]>() {
        @Override
        public final BigDecimal[] apply (final BigDecimal a, 
                                         final BigDecimal[] q) {
          assert null != q;
          assert dimension == q.length;
          final BigDecimal[] qq = new BigDecimal[dimension];
          for (int i=0;i<dimension;i++) { qq[i] = q[i].multiply(a); }
          return qq; } }; }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

