package xfp.java.algebra;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.apache.commons.rng.UniformRandomProvider;

import xfp.java.linear.BigDecimalsN;
import xfp.java.linear.BigFractionsN;
import xfp.java.linear.Qn;
import xfp.java.linear.RatiosN;
import xfp.java.numbers.BigDecimals;
import xfp.java.numbers.BigFractions;
import xfp.java.numbers.Ratios;

/** Group-like structures: One set plus closed binary operation.
 * 
 * Not that useful (?), but a simple case for working out testing,
 * etc.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-21
 */
@SuppressWarnings("unchecked")
public final class OneSetOneOperation implements Set {

  private final BinaryOperator _operation;
  // may be null
  private final Object _identity;
  // may be null
  private final UnaryOperator _inverse;

  private final Set _elements;

  //--------------------------------------------------------------
  // methods 
  //--------------------------------------------------------------

  public final BinaryOperator operation () { return _operation; }
  public final Set elements () { return _elements; }
  /** may be null. */
  public final Object identity () { return _identity; }
  /** may be null. */
  public final UnaryOperator inverse () { return _inverse; }

  //--------------------------------------------------------------
  // laws for some specific algebraic structures, for testing

  public final List<Predicate> magmaLaws () { 
    return Laws.magma(elements(),operation());}

  public final List<Predicate>  semigroupLaws  () {
    return Laws.semigroup(elements(),operation());}

  public final List<Predicate>  monoidLaws () {
    assert Objects.nonNull(identity());
    return Laws.monoid(elements(),operation(),identity());}

  public final List<Predicate> 
  groupLaws  () {
    assert Objects.nonNull(identity());
    assert Objects.nonNull(inverse());
    return 
      Laws.group(elements(),operation(),identity(),inverse());}

  public final List<Predicate> 
  commutativeGroupLaws  () {
    assert Objects.nonNull(identity());
    assert Objects.nonNull(inverse());
    return 
      Laws.commutativegroup(
        elements(),operation(),identity(),inverse());}

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  @Override
  public final boolean contains (final Object x) {
    return _elements.contains(x); }

  // TODO: should there be an _equivalence slot?
  // instead of inheriting from _elements?
  // Would it be a good idea to allow an equivalence relation 
  // different from the element set?
  // ---probably not. could always have a wrapper set that changes
  // the equivalence relation.
  @Override
  public final BiPredicate equivalence () {
    return _elements.equivalence(); }

  @Override
  public final Supplier generator (final UniformRandomProvider prng,
                                   final Map options) { 
    return _elements.generator(prng,options); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------
  // DANGER: relying on equivalence(), etc., returning equivalent 
  // objects each time

  @Override
  public final int hashCode () { 
    return Objects.hash(
      operation(),
      identity(),
      inverse(),
      equivalence(),
      elements()); } 

  @Override
  public final boolean equals (final Object obj) {
    if (this == obj) { return true; }
    if (obj == null) { return false; }
    if (!(obj instanceof OneSetOneOperation)) { return false; }
    OneSetOneOperation other = (OneSetOneOperation) obj;
    // WARNING: hard to tell if 2 operations are the same,
    // unless the implementing class has some kind of singleton
    // constraint.
    return 
      Objects.equals(operation(),other.operation())
      &&
      Objects.equals(identity(),other.identity())
      &&
      Objects.equals(inverse(),other.inverse())
      &&
      Objects.equals(equivalence(),other.equivalence())
      &&
      Objects.equals(elements(),other.elements()); }

  @Override
  public final String toString () { 
    return 
      "S1O1[" +
      //operation() +
      //"," + identity() +
      //"," + inverse() + "," + 
      elements()
      + "]"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private OneSetOneOperation (final BinaryOperator operation,
                              final Set elements,
                              final Object identity,
                              final UnaryOperator inverse) { 
    assert Objects.nonNull(operation);
    _operation = operation;
    assert Objects.nonNull(elements);
    _elements= elements; 
    _identity = identity;
    _inverse = inverse; }

  //--------------------------------------------------------------

  public static final OneSetOneOperation 
  make (final BinaryOperator operation,
        final Set elements,
        final Object identity,
        final UnaryOperator inverse) { 
    return new OneSetOneOperation(
      operation,elements,identity,inverse); }

  public static final OneSetOneOperation 
  make (final BinaryOperator operation,
        final Set elements) {
    return make(operation,elements,null,null); }

  //--------------------------------------------------------------
  // pre-define some standard magmas

  public static final OneSetOneOperation BIGDECIMALS_ADD = 
    OneSetOneOperation.make(
      BigDecimals.get().adder(),
      BigDecimals.get());

  public static final OneSetOneOperation BIGDECIMALS_MULTIPLY = 
    OneSetOneOperation.make(
      BigDecimals.get().multiplier(),
      BigDecimals.get());

  //--------------------------------------------------------------

  public static final OneSetOneOperation BIGFRACTIONS_ADD = 
    OneSetOneOperation.make(
      BigFractions.get().adder(),
      BigFractions.get());

  public static final OneSetOneOperation BIGFRACTIONS_MULTIPLY = 
    OneSetOneOperation.make(
      BigFractions.get().multiplier(),
      BigFractions.get());

  //--------------------------------------------------------------

  public static final OneSetOneOperation RATIOS_ADD = 
    OneSetOneOperation.make(
      Ratios.get().adder(),
      Ratios.get());

  public static final OneSetOneOperation RATIOS_MULTIPLY = 
    OneSetOneOperation.make(
      Ratios.get().multiplier(),
      Ratios.get());

  //--------------------------------------------------------------
  // TODO: cache by n?
  
  public static final OneSetOneOperation 
  bigDecimalsNGroup (final int n) {
    final BigDecimalsN bdn = BigDecimalsN.get(n);
    return
      OneSetOneOperation.make(
        bdn.adder(),
        bdn,
        bdn.additiveIdentity(),
        bdn.additiveInverse()); }

  //--------------------------------------------------------------
  // TODO: cache by n?
  
  public static final OneSetOneOperation 
  bigFractionsNGroup (final int n) {
    final BigFractionsN bfn = BigFractionsN.get(n);
    return
      OneSetOneOperation.make(
        bfn.adder(),
        bfn,
        bfn.additiveIdentity(),
        bfn.additiveInverse()); }

  //--------------------------------------------------------------
  // TODO: cache by n?
  
  public static final OneSetOneOperation 
  ratiosNGroup (final int n) {
    final RatiosN ratioN = RatiosN.get(n);
    return
      OneSetOneOperation.make(
        ratioN.adder(),
        ratioN,
        ratioN.additiveIdentity(),
        ratioN.additiveInverse()); }

  //--------------------------------------------------------------
  // TODO: cache by n?
  
  public static final OneSetOneOperation 
  qnGroup (final int n) {
    final Qn qn = Qn.get(n);
    return
      OneSetOneOperation.make(
        qn.adder(),
        qn,
        qn.additiveIdentity(),
        qn.additiveInverse()); }

  //--------------------------------------------------------------
}
