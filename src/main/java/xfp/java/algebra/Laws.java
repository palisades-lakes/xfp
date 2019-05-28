package xfp.java.algebra;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.google.common.collect.ImmutableList;

import xfp.java.Debug;

/** Constructor methods for Predicates/BiPredicate closures on
 * sets and operations.
 *
 * Universal algebra approach: binary, unary, nullary ops
 * plus 'laws' involving universal quantifiers impose
 * constraint on ops. No existential quantifiers as in traditional
 * definitions makes testing easier --- can check universal
 * quantified predicate approximately, using generator, but no easy
 * way to even approximately determine the truth of 'there exists'
 * statements.
 *
 * See https://en.wikipedia.org/wiki/Universal_algebra
 *
 * https://en.wikipedia.org/wiki/Outline_of_algebraic_structures
 *
 * (TODO: will need a 'TriPredicate' for affine spaces, etc.).
 *
 * Constants and class (static) methods only;
 * no instance state or methods.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-05-28
 */

@SuppressWarnings("unchecked")
public final class Laws {

  //--------------------------------------------------------------
  // one set, one operation
  //--------------------------------------------------------------
  /** Is the value of the operation an element of the structure?
   */
  public final static Predicate<Map<Set,Supplier>>
  closed (final Set elements,
          final BinaryOperator operation) {
    class Closed implements Predicate<Map<Set,Supplier>> {
      @Override
      public final String toString () { return elements + " closed binary"; }
      @Override
      public final boolean test (final Map<Set,Supplier> generators) {
        final Supplier generator = generators.get(elements);
        assert null != generator :
          "generators: " + generators + "\n" +
          "elements: " + elements;
        final Object a = generator.get();
        assert elements.contains(a) :
          a + " is not an element of " + elements;
        final Object b = generator.get();
        assert elements.contains(b):
          b + " is not an element of " + elements;
        return elements.contains(operation.apply(a,b)); } }
    return new Closed(); }

  //--------------------------------------------------------------
  /** Is the operation associative?
   */

  public final static Predicate<Map<Set,Supplier>>
  associative (final Set elements,
               final BinaryOperator operation) {
    class Associative implements Predicate<Map<Set,Supplier>> {
      @Override
      public final String toString () { return elements + " associative binary"; }
      @Override
      public final boolean test (final Map<Set,Supplier> generators) {
        final Supplier generator = generators.get(elements);
        final Object a = generator.get();
        assert elements.contains(a);
        final Object b = generator.get();
        assert elements.contains(b);
        final Object c = generator.get();
        assert elements.contains(c);
        final BiPredicate equal = elements.equivalence();
        final Object right = operation.apply(a,operation.apply(b,c));
        final Object left = operation.apply(operation.apply(a,b),c);
        final boolean pass = equal.test(right,left);
        if (! pass) {
          System.out.println();
          System.out.println(operation);
          System.out.println(a);
          System.out.println(b);
          System.out.println(c);
          System.out.println(right);
          System.out.println(left); }
        return pass; } }
    return new Associative(); }

  //--------------------------------------------------------------
  // TODO: right identity vs left identity?

  /** Does <code>(operation a identity) ==
   * (operation identity a) = a</code>?
   * ...except for excluded elements, as with the additive
   * identity (zero) breaking multiplicative identity in a
   * ring-like structure.
   */
  public final static Predicate<Map<Set,Supplier>>
  identity (final Set elements,
            final BinaryOperator operation,
            final Object identity,
            final java.util.Set excluded) {
    class Identity implements Predicate<Map<Set,Supplier>> {
      @Override
      public final String toString () {
        return elements + " identity: " + identity.toString()
        + "\nis not an identity for " + operation; }
      //      public final String toString () {
      //        return identity + " is not an identity for " +
      //          operation + " on " + elements + "\n" +
      //          "excluding " + excluded; }
      @Override
      public final boolean test (final Map<Set,Supplier> generators) {
        // TODO: what if we want <code>null</code>
        // to be the identity? IS there an example where null is
        // better than empty list, empty string, ...
        final Supplier generator = generators.get(elements);
        if (null == identity) { return false; }
        final Object a = generator.get();
        // contains doesn't work because test needs to obey
        // structure's definition of equivalence
        //if (Sets.contains(excluded,a)) { return true; }
        final BiPredicate eq = elements.equivalence();
        for (final Object x : excluded) {
          if (eq.test(x,a)) { return true; } }
        assert elements.contains(a) : a + " not in " + elements;
        assert elements.contains(identity) :
          "identity:" + identity + " not in " + elements;
        final Object l = operation.apply(identity,a);
        final boolean ll = eq.test(a,l);
        assert ll :
          "(" + operation + " " + identity + " " + a + ")"
          + " -> " + l;
        final Object r = operation.apply(a,identity);
        final boolean rr = eq.test(a,r);
        assert ll :
          "(" + operation + " " + identity + " " + a + ")"
          + " -> " + l;
        return ll && rr; } }
    return new Identity(); }

  /** Does <code>(operation a identity) ==
   * (operation identity a) = a</code>?
   */
  public final static Predicate<Map<Set,Supplier>>
  identity (final Set elements,
            final BinaryOperator operation,
            final Object identity) {
    return
      identity(elements,operation,identity,java.util.Set.of()); }

  //--------------------------------------------------------------
  // TODO: right inverses vs left inverses?

  /** Does <code>(operation a (inverse a)) ==
   * (operation (inverse a) a) = identity</code>?
   * ...except for excluded elements, as with the additive
   * identity (zero) having no inverse element for the
   * multiplicative operation in a ring-like structure.
   */
  public final static Predicate<Map<Set,Supplier>>
  inverse (final Set elements,
           final BinaryOperator operation,
           final Object identity,
           final UnaryOperator inverse,
           final java.util.Set excluded) {
    class Inverse implements Predicate<Map<Set,Supplier>> {
      @Override
      public final String toString () {
        return elements + " inverse:" + inverse.toString(); }
      @Override
      public final boolean test (final Map<Set,Supplier> generators) {
        final Supplier generator = generators.get(elements);
        final Object a = generator.get();
        // contains doesn't work because test needs to obey
        // structure's definition of equivalence
        // need excluded to be subset of Structure's elements
        //if (Sets.contains(excluded,a)) { return true; }
        final BiPredicate eq = elements.equivalence();
        for (final Object x : (excluded)) {
          if (eq.test(x,a)) { return true; } }
        assert elements.contains(a);
        assert elements.contains(identity);
        final Object ainv = inverse.apply(a);
        final boolean result =
          eq.test(identity,operation.apply(a,ainv))
          &&
          eq.test(identity,operation.apply(ainv,a));
        if (! result) {
          System.out.println("op=" + operation);
          System.out.println("invert=" + inverse);
          System.out.println("a=" + a);
          System.out.println("ainv=" + ainv);
          System.out.println("identity=" + identity);
          System.out.println("(op a ainv)=" + operation.apply(a,ainv));
          System.out.println("(op ainv a)=" + operation.apply(ainv,a));
        }
        return result; } }
    return new Inverse(); }

  /** Does <code>(operation a (inverse a)) ==
   * (operation (inverse a) a) = identity</code>
   * for all <code>a</code> in <code>elements</code>?
   */
  public final static Predicate<Map<Set,Supplier>>
  inverse (final Set elements,
           final BinaryOperator operation,
           final Object identity,
           final UnaryOperator inverse) {
    return inverse(
      elements,operation,identity,inverse,java.util.Set.of()); }

  //--------------------------------------------------------------
  /** Is the operation commutative (aka symmetric)?
   */

  public final static Predicate<Map<Set,Supplier>>
  commutative (final Set elements,
               final BinaryOperator operation) {
    class Commutative implements Predicate<Map<Set,Supplier>> {
      @Override
      public final String toString () { return elements + " commutative"; }
      @Override
      public final boolean test (final Map<Set,Supplier> generators) {
        final Supplier generator = generators.get(elements);
        final Object a = generator.get();
        assert elements.contains(a);
        final Object b = generator.get();
        assert elements.contains(b);
        final BiPredicate equal = elements.equivalence();
        return
          equal.test(
            operation.apply(a,b),
            operation.apply(b,a)); } }
    return new Commutative(); }

  //--------------------------------------------------------------
  // by algebraic structure
  // TODO: reuse code by adding elements to (immutable) lists

  public static final ImmutableList<Predicate<Map<Set,Supplier>>>
  magma  (final Set elements,
          final BinaryOperator operation) {
    return ImmutableList.of(closed(elements,operation));}

  public static final ImmutableList<Predicate<Map<Set,Supplier>>>
  semigroup  (final Set elements,
              final BinaryOperator operation) {
    return ImmutableList.of(
      closed(elements,operation),
      associative(elements,operation));}

  public static final ImmutableList<Predicate<Map<Set,Supplier>>>
  monoid  (final Set elements,
           final BinaryOperator operation,
           final Object identity) {
    return ImmutableList.of(
      closed(elements,operation),
      associative(elements,operation),
      identity(elements,operation,identity));}

  public static final ImmutableList<Predicate<Map<Set,Supplier>>>
  group  (final Set elements,
          final BinaryOperator operation,
          final Object identity,
          final UnaryOperator inverse) {
    return ImmutableList.of(
      closed(elements,operation),
      associative(elements,operation),
      identity(elements,operation,identity),
      inverse(elements,operation,identity,inverse));}

  public static final ImmutableList<Predicate<Map<Set,Supplier>>>
  commutativegroup  (final Set elements,
                     final BinaryOperator operation,
                     final Object identity,
                     final UnaryOperator inverse) {
    return ImmutableList.of(
      closed(elements,operation),
      associative(elements,operation),
      identity(elements,operation,identity),
      inverse(elements,operation,identity,inverse),
      commutative(elements,operation));}

  //--------------------------------------------------------------
  // one set, two operations
  //--------------------------------------------------------------
  /** Does <code>multiply</code> distribute over <code>add</code>?
   * <code>a*(b+c) == (a*b) + (a*c)</code>?
   * (Ring-like version)
   */

  public final static Predicate<Map<Set,Supplier>>
  distributive (final Set elements,
                final BinaryOperator add,
                final BinaryOperator multiply) {
    class Distributive implements Predicate<Map<Set,Supplier>> {
      @Override
      public final String toString () { return elements + " distributive"; }
      @Override
      public final boolean test (final Map<Set,Supplier> generators) {
        final Supplier generator = generators.get(elements);
        final Object a = generator.get();
        assert elements.contains(a);
        final Object b = generator.get();
        assert elements.contains(b);
        final Object c = generator.get();
        assert elements.contains(c);
        final BiPredicate equal = elements.equivalence();
        final Object left = multiply.apply(a,add.apply(b,c));
        final Object right = add.apply(
          multiply.apply(a,b),
          multiply.apply(a,c));
        final boolean pass = equal.test(left,right);
        if (! pass) {
          if (! pass) {
            System.out.println();
            System.out.println("mul=" + multiply);
            System.out.println("add=" + add);
            System.out.println("a=" + a);
            System.out.println("b=" + b);
            System.out.println("c=" + c);
            System.out.println("a*(b+c)    =" + left); 
            System.out.println("(a*b)+(a*c)=" + right);}
    } 
        return pass; } }
    return new Distributive(); }

  //--------------------------------------------------------------
  // by algebraic structure

  public static final ImmutableList<Predicate<Map<Set,Supplier>>>
  semiring (final BinaryOperator add,
            final Object additiveIdentity,
            final BinaryOperator multiply,
            final Object multiplicativeIdentity,
            final Set elements){
    return ImmutableList.of(
      closed(elements,add),
      associative(elements,add),
      identity(elements,add,additiveIdentity),
      commutative(elements,add),
      closed(elements,multiply),
      associative(elements,multiply),
      identity(elements,multiply,multiplicativeIdentity,
        java.util.Set.of(additiveIdentity)),
      distributive(elements,add,multiply));}

  public static final ImmutableList<Predicate<Map<Set,Supplier>>>
  ring (final BinaryOperator add,
        final Object additiveIdentity,
        final UnaryOperator additiveInverse,
        final BinaryOperator multiply,
        final Object multiplicativeIdentity,
        final Set elements){
    return ImmutableList.of(
      closed(elements,add),
      associative(elements,add),
      identity(elements,add,additiveIdentity),
      inverse(elements,add,additiveIdentity,additiveInverse),
      commutative(elements,add),
      closed(elements,multiply),
      associative(elements,multiply),
      identity(elements,multiply,multiplicativeIdentity,
        java.util.Set.of(additiveIdentity)),
      distributive(elements,add,multiply));}

  public static final ImmutableList<Predicate<Map<Set,Supplier>>>
  commutativeRing (final BinaryOperator add,
                   final Object additiveIdentity,
                   final UnaryOperator additiveInverse,
                   final BinaryOperator multiply,
                   final Object multiplicativeIdentity,
                   final Set elements){
    return ImmutableList.of(
      closed(elements,add),
      associative(elements,add),
      identity(elements,add,additiveIdentity),
      inverse(elements,add,additiveIdentity,additiveInverse),
      commutative(elements,add),
      closed(elements,multiply),
      associative(elements,multiply),
      identity(elements,multiply,multiplicativeIdentity,
        java.util.Set.of(additiveIdentity)),
      commutative(elements,multiply),
      distributive(elements,add,multiply));}

  public static final ImmutableList<Predicate<Map<Set,Supplier>>>
  divisionRing (final BinaryOperator add,
                final Object additiveIdentity,
                final UnaryOperator additiveInverse,
                final BinaryOperator multiply,
                final Object multiplicativeIdentity,
                final UnaryOperator multiplicativeInverse,
                final Set elements) {
    return ImmutableList.of(
      closed(elements,add),
      associative(elements,add),
      identity(elements,add,additiveIdentity),
      inverse(elements,add,additiveIdentity,additiveInverse),
      commutative(elements,add),
      closed(elements,multiply),
      associative(elements,multiply),
      identity(elements,multiply,multiplicativeIdentity,
        java.util.Set.of(additiveIdentity)),
      inverse(elements,multiply,multiplicativeIdentity,
        multiplicativeInverse,
        java.util.Set.of(additiveIdentity)),
      distributive(elements,add,multiply));}

  public static final ImmutableList<Predicate<Map<Set,Supplier>>>
  field (final BinaryOperator add,
         final Object additiveIdentity,
         final UnaryOperator additiveInverse,
         final BinaryOperator multiply,
         final Object multiplicativeIdentity,
         final UnaryOperator multiplicativeInverse,
         final Set elements) {
    return ImmutableList.of(
      closed(elements,add),
      associative(elements,add),
      identity(elements,add,additiveIdentity),
      inverse(elements,add,additiveIdentity,additiveInverse),
      commutative(elements,add),
      closed(elements,multiply),
      associative(elements,multiply),
      identity(elements,multiply,multiplicativeIdentity,
        java.util.Set.of(additiveIdentity)),
      inverse(elements,multiply,multiplicativeIdentity,
        multiplicativeInverse,
        java.util.Set.of(additiveIdentity)),
      commutative(elements,multiply),
      distributive(elements,add,multiply));}

  // NOT a standard algebraic structure
  // TODO: test for laws that are violated?
  @SuppressWarnings("unused")
  public static final ImmutableList<Predicate<Map<Set,Supplier>>>
  floatingPoint (final BinaryOperator add,
                 final Object additiveIdentity,
                 final UnaryOperator additiveInverse,
                 final BinaryOperator multiply,
                 final Object multiplicativeIdentity,
                 final UnaryOperator multiplicativeInverse,
                 final Set elements) {
    return ImmutableList.of(
      closed(elements,add),
      //associative(elements,add),
      identity(elements,add,additiveIdentity),
      //inverse(elements,add,additiveIdentity,additiveInverse),
      commutative(elements,add),
      closed(elements,multiply),
      //associative(elements,multiply),
      identity(elements,multiply,multiplicativeIdentity,
        java.util.Set.of(additiveIdentity)),
      //inverse(elements,multiply,multiplicativeIdentity,
      //  multiplicativeInverse,
      //  java.util.Set.of(additiveIdentity)),
      commutative(elements,multiply)
      //,distributive(elements,add,multiply)
      ); }

  //--------------------------------------------------------------
  // Two sets: scalars and elements/vectors
  //--------------------------------------------------------------
  /** Is a scaled element still an element of the structure?
   */

  public final static Predicate<Map<Set,Supplier>>
  closed (final Set elements,
          final Set scalars,
          final BiFunction scale) {
    class ClosedScaling implements Predicate<Map<Set,Supplier>> {
      @Override
      public final String toString () { return elements + " X " + scalars + " closed"; }
      @Override
      public final boolean test (final Map<Set,Supplier> generators) {
        final Supplier scalarSamples = generators.get(scalars);
        assert null != scalarSamples;
        final Supplier elementSamples = generators.get(elements);
        assert null != elementSamples;
        final Object a = scalarSamples.get();
        assert scalars.contains(a);
        final Object b = elementSamples.get();
        assert elements.contains(b);
        return elements.contains(scale.apply(a,b)); } }
    return new ClosedScaling(); }

  //--------------------------------------------------------------
  /** Is code>multiply</code> associative with <code>scale</code>?
   * <code>(scale (multiply a b) c) == (scale a (scale b c)</code>?
   * (Module-like version)
   */

  public final static Predicate<Map<Set,Supplier>>
  associative (final Set elements,
               final Set scalars,
               final BiFunction multiply,
               final BiFunction scale) {
    class AssociativeScaling implements Predicate<Map<Set,Supplier>> {
      @Override
      public final String toString () { return elements + " X " + scalars + " associative"; }
      @Override
      public final boolean test (final Map<Set,Supplier> generators) {
        final Supplier scalarSamples = generators.get(scalars);
        assert null != scalarSamples :
          generators.toString() + "\n" + scalars;
        final Supplier elementSamples = generators.get(elements);
        assert null != elementSamples;
        final Object a = scalarSamples.get();
        assert scalars.contains(a);
        final Object b = scalarSamples.get();
        assert scalars.contains(b);
        final Object c = elementSamples.get();
        assert elements.contains(c);
        final BiPredicate equal = elements.equivalence();
        final Object right = scale.apply(a,scale.apply(b,c));
        final Object left = scale.apply(multiply.apply(a,b),c);
        return equal.test(right,left); } }
    return new AssociativeScaling(); }

  //--------------------------------------------------------------
  /** Does <code>scale</code> distribute over <code>add</code>?
   * <code>a*(b+c) == (a*b) + (a*c)</code>?
   * (Module-like version)
   */

  public final static Predicate<Map<Set,Supplier>>
  distributive (final Set elements,
                final Set scalars,
                final BinaryOperator add,
                final BiFunction scale) {
    class DistributiveScaling implements Predicate<Map<Set,Supplier>> {
      @Override
      public final String toString () { return elements + " X " + scalars + " distributive"; }
      @Override
      public final boolean test (final Map<Set,Supplier> generators) {
        final Supplier scalarSamples = generators.get(scalars);
        assert null != scalarSamples;
        final Supplier elementSamples = generators.get(elements);
        assert null != elementSamples;
        final Object a = scalarSamples.get();
        assert scalars.contains(a);
        final Object b = elementSamples.get();
        assert elements.contains(b);
        final Object c = elementSamples.get();
        assert elements.contains(c);
        final BiPredicate equal = elements.equivalence();
        return
          equal.test(
            scale.apply(a,add.apply(b,c)),
            add.apply(
              scale.apply(a,b),
              scale.apply(a,c))); } }
    return new DistributiveScaling(); }

  //--------------------------------------------------------------
  /** Whether these are laws for linear spaces, or modules, or
   * something else, is determined by the laws in the
   * elements and scalars structures.
   */

  public static final ImmutableList
  linearSpaceLike (final BiFunction scale,
                   final OneSetOneOperation elements,
                   final OneSetTwoOperations scalars) {
    final ImmutableList.Builder b = ImmutableList.builder();
    final Set e = elements.elements();
    final Set s = scalars.elements();
    b.addAll(scalars.laws());
    b.addAll(elements.laws());
    b.add(
      closed(e,s,scale),
      associative(e,s,scalars.multiply(),scale),
      distributive(e,s,elements.operation(),scale));
    return b.build(); }

  public static final ImmutableList
  floatingPointSpace (final BiFunction scale,
                      final OneSetOneOperation elements,
                      final OneSetTwoOperations scalars) {
    final ImmutableList.Builder b = ImmutableList.builder();
    final Set e = elements.elements();
    final Set s = scalars.elements();
    b.add(
      closed(e,s,scale));
    b.addAll(elements.laws());
    b.addAll(scalars.laws());
    return b.build(); }

  //--------------------------------------------------------------
  // disable constructor
  //--------------------------------------------------------------

  private Laws () {
    throw new UnsupportedOperationException(
      "can't instantiate " + getClass()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
