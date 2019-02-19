package xfp.java.numbers;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.apache.commons.rng.UniformRandomProvider;

import xfp.java.algebra.Set;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;

/** The set of rational numbers represented by 
 * <code>BigDecimal</code>.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-19
 */
public final class BigDecimals implements Set {

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    return element instanceof BigDecimal; }

  public static final boolean equalBigDecimals (final BigDecimal q0, 
                                                 final BigDecimal q1) {
    if (q0 == q1) { return true; }
    if (null == q0) {
      if (null == q1) { return true; }
      return false; }
    if (null == q1) { return false; }
    final int c = q0.compareTo(q1);
    return 0 == c; }

  private static final BiPredicate<BigDecimal,BigDecimal> 
  EQUALS = 
  new BiPredicate<BigDecimal,BigDecimal>() {
    @Override
    public final boolean test (final BigDecimal q0, 
                               final BigDecimal q1) {
      return equalBigDecimals(q0,q1); }
  };

  @Override
  public final BiPredicate equivalence () { return EQUALS; }

  //--------------------------------------------------------------

  @Override
  public final Supplier generator (final UniformRandomProvider urp,
                                   final Map options) {
    final Generator bfs = Generators.bigDecimalGenerator(urp);
    return 
      new Supplier () {
      @Override
      public final Object get () { return bfs.next(); } }; }

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
    return that instanceof BigDecimals; }

  @Override
  public final String toString () { return "BD"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private BigDecimals () { }

  private static final BigDecimals SINGLETON = 
    new BigDecimals();

  public static final BigDecimals get () { return SINGLETON; } 

  public static final BinaryOperator<BigDecimal> ADD =
    new BinaryOperator<BigDecimal> () {
    @Override
    public final String toString () { 
      return "BigDecimal.add()"; }
    @Override
    public final BigDecimal apply (final BigDecimal q0, 
                                   final BigDecimal q1) {
      return q0.add(q1); } 
  };

  public static final UnaryOperator<BigDecimal>
  ADDITIVE_INVERSE =
  new UnaryOperator<BigDecimal> () {
    @Override
    public final String toString () { 
      return "BigDecimal.negate()"; }
    @Override
    public final BigDecimal apply (final BigDecimal q) {
      return q.negate(); } 
  };

  public static final BinaryOperator<BigDecimal> MULTIPLY =
    new BinaryOperator<BigDecimal>() {
    @Override
    public final String toString () { 
      return "BigDecimal.multiply()"; }
    @Override
    public final BigDecimal apply (final BigDecimal q0, 
                                   final BigDecimal q1) {
      return q0.multiply(q1); } 
  };

  public static final UnaryOperator<BigDecimal>
  MULTIPLICATIVE_INVERSE =
  new UnaryOperator<BigDecimal> () {
    @Override
    public final String toString () { 
      return "BigDecimal.inverse()"; }
    @Override
    public final BigDecimal apply (final BigDecimal q) {
      // only a partial inverse
      if (BigDecimal.ZERO.equals(q)) { return null; }
      return BigDecimal.ONE.divide(q); } 
  };

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

