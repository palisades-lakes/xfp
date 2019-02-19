package xfp.java.numbers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.apache.commons.rng.UniformRandomProvider;

import clojure.lang.Numbers;
import clojure.lang.Ratio;
import xfp.java.algebra.Set;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;

/** The set of rational numbers represented by 
 * <code>Ratio</code>.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-19
 */
public final class Ratios implements Set {

  //--------------------------------------------------------------
  // Ratio utils
  //--------------------------------------------------------------

  public static final Ratio toRatio (final double q) {
    return clojure.lang.Numbers.toRatio(new BigDecimal(q)); }

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object element) {
    return element instanceof Ratio; }

  // clojure.lang.Ratio doesn't equate 1/1 and 2/2!
  public static final boolean equalRatios (final Ratio q0, 
                                           final Ratio q1) {
    if (q0 == q1) { return true; }
    if (null == q0) {
      if (null == q1) { return true; }
      return false; }
    final BigInteger n0 = q0.numerator; 
    final BigInteger d0 = q0.denominator; 
    final BigInteger n1 = q1.numerator; 
    final BigInteger d1 = q1.denominator; 
    return n0.multiply(d1).equals(n1.multiply(d0)); }

  private static final BiPredicate<Ratio,Ratio> 
  EQUALS = new BiPredicate<Ratio,Ratio>() {
    @Override
    public final boolean test (final Ratio q0, 
                               final Ratio q1) {
      return equalRatios(q0,q1); }
  };

    @Override
    public final BiPredicate equivalence () { return EQUALS; }

    //--------------------------------------------------------------

    @Override
    public final Supplier generator (final UniformRandomProvider urp,
                                     final Map options) {
      final Generator bfs = Generators.ratioGenerator(urp);
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
      return that instanceof Ratios; }

    @Override
    public final String toString () { return "Ratios"; }

    //--------------------------------------------------------------
    // construction
    //--------------------------------------------------------------

    private Ratios () { }

    private static final Ratios SINGLETON = 
      new Ratios();

    public static final Ratios get () { return SINGLETON; } 

    public static final BinaryOperator<Ratio> ADD =
      new BinaryOperator<Ratio> () {
      @Override
      public final String toString () { return "Ratios.add"; }
      @Override
      public final Ratio apply (final Ratio q0, 
                                final Ratio q1) {
        return Numbers.toRatio(Numbers.add(q0,q1)); } 
    };

    public static final Ratio ZERO =
      new Ratio(BigInteger.ZERO,BigInteger.ONE);

    public static final UnaryOperator<Ratio>
    ADDITIVE_INVERSE =
    new UnaryOperator<Ratio> () {
      @Override
      public final Ratio apply (final Ratio q) {
        return Numbers.toRatio(Numbers.minus(q)); } 
    };

    public static final BinaryOperator<Ratio> MULTIPLY =
      new BinaryOperator<Ratio>() {
      @Override
      public final Ratio apply (final Ratio q0, 
                                final Ratio q1) {
        return Numbers.toRatio(Numbers.multiply(q0,q1)); } 
    };

    public static final Ratio ONE =
      new Ratio(BigInteger.ONE,BigInteger.ONE);

    public static final UnaryOperator<Ratio>
    MULTIPLICATIVE_INVERSE =
    new UnaryOperator<Ratio> () {
      @Override
      public final Ratio apply (final Ratio q) {
        final BigInteger n = q.numerator;
        final BigInteger d = q.denominator;
        // only a partial inverse
        if (BigInteger.ZERO.equals(n)) { return null; }
        return new Ratio(d,n); } 
    };

    //--------------------------------------------------------------
  }
  //--------------------------------------------------------------

