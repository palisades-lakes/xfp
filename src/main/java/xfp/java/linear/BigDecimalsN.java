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
 * @version 2019-02-21
 */
@SuppressWarnings("unchecked")
public final class BigDecimalsN implements Set {

  private final int _dimension;
  public final int dimension () { return _dimension; }

  //--------------------------------------------------------------
  // operations for algebraic structures over BigDecimal arrays.
  //--------------------------------------------------------------
  /** A <code>BinaryOperator</code> that adds elementwise
   * <code>BigDecimal[]</code> instances of length 
   * <code>dimension</code>.
   */

  private final BigDecimal[] add (final BigDecimal[] q0, 
                                  final BigDecimal[] q1) {
    assert contains(q0);
    assert contains(q1);
    final BigDecimal[] qq = new BigDecimal[dimension()];
    for (int i=0;i<dimension();i++) { qq[i] = q0[i].add(q1[i]); }
    return qq; }

  public final BinaryOperator<BigDecimal[]> adder () {
    return
      new BinaryOperator<BigDecimal[]>() {
        @Override
        public final BigDecimal[] apply (final BigDecimal[] q0, 
                                         final BigDecimal[] q1) {
          return BigDecimalsN.this.add(q0,q1); } }; }

  //--------------------------------------------------------------

  public final BigDecimal[] additiveIdentity () {
    final BigDecimal[] qq = new BigDecimal[dimension()];
    Arrays.fill(qq,BigDecimal.ZERO);
    return qq; }

  //--------------------------------------------------------------

  private final BigDecimal[] invert (final BigDecimal[] q) {
    assert contains(q);
    final BigDecimal[] qq = new BigDecimal[dimension()];
    for (int i=0;i<dimension();i++) { qq[i] = q[i].negate(); }
    return qq; } 

  public final UnaryOperator<BigDecimal[]> additiveInverse () {
    return 
      new UnaryOperator<BigDecimal[]>() {
        @Override
        public final BigDecimal[] apply (final BigDecimal[] q) {
          return BigDecimalsN.this.invert(q); } }; }

  //--------------------------------------------------------------

  private final BigDecimal[] scale (final BigDecimal a, 
                                    final BigDecimal[] q) {
    assert contains(q);
    final BigDecimal[] qq = new BigDecimal[dimension()];
    for (int i=0;i<dimension();i++) { 
      qq[i] = q[i].multiply(a); }
    return qq; } 

  public final BiFunction<BigDecimal,BigDecimal[],BigDecimal[]> 
  scaler () {
    return
      new BiFunction<BigDecimal,BigDecimal[],BigDecimal[]>() {
        @Override
        public final BigDecimal[] apply (final BigDecimal a, 
                                         final BigDecimal[] q) {
          return BigDecimalsN.this.scale(a,q); } }; }

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  private final boolean equals (final BigDecimal[] q0, 
                                final BigDecimal[] q1) {
    assert null != q0;
    assert null != q1;
    assert dimension() == q0.length;
    assert dimension() == q1.length;
    for (int i=0;i<dimension();i++) {
      if (! BigDecimals.get().equals(q0[i],q1[i])) {
        return false; } }
    return true; }

  @Override
  public final BiPredicate equivalence () { 
    return new BiPredicate<BigDecimal[],BigDecimal[]>() {

      @Override
      public final boolean test (final BigDecimal[] q0, 
                                 final BigDecimal[] q1) {
        return BigDecimalsN.this.equals(q0,q1); } }; }

  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    return 
      (element instanceof BigDecimal[])
      &&
      ((BigDecimal[]) element).length == dimension(); }

  //--------------------------------------------------------------
  /** Intended primarily for testing. 
   */
  @Override
  public final Supplier generator (final UniformRandomProvider urp,
                                   final Map options) {
    return 
      new Supplier () {
      final Generator bf = 
        Generators.bigDecimalGenerator(dimension(),urp);
      @Override
      public final Object get () { return bf.next(); } }; }

  @Override
  public final Supplier generator (final UniformRandomProvider urp) {
    return generator(urp,Collections.emptyMap()); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { return dimension(); }

  @Override
  public final boolean equals (final Object that) {
    if (this == that) { return true; }
    return 
      (that instanceof BigDecimalsN)
      &&
      dimension() == ((BigDecimalsN) that).dimension(); }

  @Override
  public final String toString () { return "BD^" + dimension(); }

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
}
//--------------------------------------------------------------

