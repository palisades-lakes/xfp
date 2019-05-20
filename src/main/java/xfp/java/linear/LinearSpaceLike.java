package xfp.java.linear;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import xfp.java.algebra.Set;

/** Base class for structures that are like linear spaces.
 * Main reason a structure might not be a true linear space
 * is that the scalars might not be a field.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-25
 */
@SuppressWarnings("unchecked")
public abstract class LinearSpaceLike implements Set {

  private final int _dimension;
  public final int dimension () { return _dimension; }

  //--------------------------------------------------------------
  // operations for algebraic structures over BigDecimal arrays.
  //--------------------------------------------------------------

  public abstract Object add (final Object v0, final Object v1);
  private final BinaryOperator _adder;
  public final BinaryOperator adder () { return _adder; }

  //--------------------------------------------------------------
  // TODO: special sparse zero vector?

  public abstract Object zero (final int dimension);
  private final Object _additiveIdentity;
  public final Object additiveIdentity () {
    return _additiveIdentity; }

  //--------------------------------------------------------------

  public abstract Object negate (final Object v);
  private final UnaryOperator _additiveInverse;
  public final UnaryOperator additiveInverse () {
    return _additiveInverse; }

  //--------------------------------------------------------------

  public abstract Object scale (final Object a, final Object v);
  private final BiFunction _scaler;
  public final BiFunction scaler () { return _scaler; }

  //--------------------------------------------------------------
  // Set methods
  //--------------------------------------------------------------

  public abstract boolean equals (final Object v0, final Object v1);
  private final BiPredicate _equivalence;
  @Override
  public final BiPredicate equivalence () { return _equivalence; }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () {
    int h = 17;
    h = (31*h) + dimension();
    h = (31*h) + Objects.hashCode(getClass());
    return h; }

  @Override
  public final boolean equals (final Object that) {
    if (this == that) { return true; }
    if (null == that) { return false; }
    if (! getClass().equals(that.getClass())) { return false; }
    return dimension() == ((LinearSpaceLike) that).dimension(); }

  //--------------------------------------------------------------

  private final class Equivalence implements BiPredicate {
    @Override
    public final String toString () {
      return LinearSpaceLike.this.toString() + ".equals"; }
    @Override
    public final boolean test (final Object q0,
                               final Object q1) {
      return LinearSpaceLike.this.equals(q0,q1); } }

  private final class Scaler implements BiFunction{
    @Override
    public final String toString () {
      return LinearSpaceLike.this.toString() + ".scale"; }
    @Override
    public final Object apply (final Object a,
                               final Object q) {
      return LinearSpaceLike.this.scale(a,q); }  }

  private final class AdditiveInverse implements UnaryOperator {
    @Override
    public final String toString () {
      return LinearSpaceLike.this.toString() + ".negate"; }
    @Override
    public final Object apply (final Object q) {
      return LinearSpaceLike.this.negate(q); } }

  private final class Adder implements BinaryOperator {
    @Override
    public final String toString () {
      return LinearSpaceLike.this.toString() + ".add"; }
    @Override
    public final Object apply (final Object q0,
                               final Object q1) {
      return LinearSpaceLike.this.add(q0,q1); } }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  // TODO: support zero-dimensional space?

  public LinearSpaceLike (final int dimension) {

    assert dimension > 0;
    _dimension = dimension;
    _equivalence = new Equivalence();
    _scaler = new Scaler();
    _additiveInverse  = new AdditiveInverse();
    _additiveIdentity = zero(dimension);
    _adder = new Adder(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

