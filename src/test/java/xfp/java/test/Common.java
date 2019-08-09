package xfp.java.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;

import xfp.java.Classes;
import xfp.java.Debug;
import xfp.java.accumulators.Accumulator;
import xfp.java.function.FloatFunction;
import xfp.java.function.ToFloatFunction;
import xfp.java.numbers.Doubles;
import xfp.java.numbers.Floats;
import xfp.java.numbers.Natural;
import xfp.java.numbers.NaturalDivide;
import xfp.java.numbers.Ringlike;
import xfp.java.numbers.Uints;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;
import xfp.java.prng.PRNG;

/** Test utilities
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-08-01
 */
@SuppressWarnings("unchecked")
public final class Common {

  public static final int TRYS = 131; 

  //--------------------------------------------------------------

  public static final List<String> inexactAccumulators () {
    return
      Arrays.asList(
        new String[]
          { "xfp.java.accumulators.DoubleAccumulator",
            "xfp.java.accumulators.KahanAccumulator",
          }); }

  public static final List<String> accumulators () {
    return
      Arrays.asList(
        new String[]
          { //"xfp.java.accumulators.ERationalAccumulator",
            //"xfp.java.accumulators.EFloatAccumulator",
            "xfp.java.accumulators.DistilledAccumulator",
            "xfp.java.accumulators.ZhuHayesAccumulator",
            //"xfp.java.accumulators.BigFloatAccumulator0",
            "xfp.java.accumulators.BigFloatAccumulator",
            "xfp.java.accumulators.RationalFloatAccumulator",
            "xfp.java.accumulators.RationalAccumulator",
          }); }

  //--------------------------------------------------------------

  public static final Accumulator
  makeAccumulator (final String className) {
    try {

      final Class c = Class.forName(className);
      final Method m = c.getMethod("make");
      return (Accumulator) m.invoke(null); }

    catch (final
      ClassNotFoundException
      | NoSuchMethodException
      | SecurityException
      | IllegalAccessException
      | IllegalArgumentException
      | InvocationTargetException e) {
      // e.printStackTrace();
      throw new RuntimeException(className,e); } }

  //--------------------------------------------------------------

  public static final List<Accumulator>
  makeAccumulators (final List<String> classNames) {
    return
      classNames
      .stream()
      .map(Common::makeAccumulator)
      .collect(Collectors.toUnmodifiableList()); }

  //--------------------------------------------------------------
  // natural number/ integer tests

  public static final <T extends Ringlike<T>> void
  hiBit (final Function<BigInteger,T> fromBI,
         final BigInteger z0) {
    final int bl = z0.bitLength();
    final Uints r0 = (Uints) fromBI.apply(z0);
    final int hi = r0.hiBit();
    Assertions.assertEquals(bl,hi,() ->
    "\nbitLength=" + bl + "\nhiBit=" + hi); }

  public static final <T extends Ringlike<T>> void
  hexRoundTrip (final Function<BigInteger,T> fromBI,
                final Function<String,T> valueOf,
                final BigInteger z0) {

    // TODO: ignore formatting characters of all kinds
    // ignore spaces
    final String zs = z0.toString(0x10).replace(" ","");
    //Debug.println(zs);

    final T r0 = fromBI.apply(z0);
    final String rs = r0.toString().replace(" ","");

    Assertions.assertEquals(zs,rs,() ->
    "\nzs=" + zs + "\nrs=" + rs);

    //Debug.println("rs=" + rs);

    final T r1 = valueOf.apply(rs);
    Assertions.assertEquals(r0,r1,() ->
    "\n" + rs + "\n" 
    + r0.toString() 
    + "\n" + r1.toString() + "\n");  }

  public static final <T extends Ringlike<T>> void
  biRoundTrip (final Function<BigInteger,T> fromBI,
               final Function<T,BigInteger> toBI,
               final BigInteger x0) {
    final T y = fromBI.apply(x0);
    final BigInteger x1 = toBI.apply(y);
    Assertions.assertEquals(x0,x1,() ->
    "\n" + y.getClass()
    + "\n" + x0.toString(0x10)
    + "\n" + y.toString()
    + "\n" + x1.toString(0x10)
    + "\n"); }

  protected static final <T extends Ringlike<T>> void
  compare (final Function<BigInteger,T> fromBI,
           final BigInteger x0,
           final BigInteger x1) {
    final int c0 = x0.compareTo(x1);
    final Natural y0 = (Natural) fromBI.apply(x0);
    final Natural y1 = (Natural) fromBI.apply(x1);
    final int c1 = y0.compareTo(y1);
    Assertions.assertEquals(c0,c1, ()->
    "\n" + x0.toString(0x10)
    + "\n compareTo "
    + "\n" +  x1.toString(0x10) 
    + "\n -> " + c0
    + "\n\n" + y0.toString()
    + "\n compareTo "
    + "\n" +  y1.toString()
    + "\n -> " + c1
    + "\n\n"); 
    final int shift = 3*32 + 17;
    final int c2 = x0.compareTo(x1.shiftLeft(shift));
    final int c3 = y0.compareTo(y1.shiftUp(shift));
    Assertions.assertEquals(c2,c3, ()->
    "\n" + x0.toString(0x10)
    + "\n compareTo "
    + "\n" +  x1.toString(0x10)  
    + "\nshiftUp " + shift
    + "\n -> " + c2
    + "\n\n" + y0.toString()
    + "\n compareTo "
    + "\n" +  y1.toString()
    + "\nshiftUp " + shift
    + "\n -> " + c3
    + "\n\n"); 
    final int c4 = y0.compareTo(y1,shift);
    Assertions.assertEquals(c3,c4, ()->
    "\n\n" + y0.toString()
    + "\n compareTo "
    + "\n" +  y1.toString()
    + "\nshiftUp " + shift
    + "\n -> " + c3
    + "\n\n" + y0.toString()
    + "\n compareTo "
    + "\n" +  y1.toString() 
    + "\n, " + shift
    + "\n -> " + c4
    + "\n\n"); 


  }

  public static final <T extends Ringlike<T>> void
  add (final Function<BigInteger,T> fromBI,
       final Function<T,BigInteger> toBI,
       final BigInteger x0,
       final BigInteger x1) {
    final T y0 = fromBI.apply(x0);
    final T y1 = fromBI.apply(x1);
    final BigInteger x2 = x0.add(x1);
    final T y2 = y0.add(y1);
    final BigInteger x3 = toBI.apply( y2);
    Assertions.assertEquals(x2,x3,() ->
    "\n" + x0.toString(0x10) + "(" + x0.toString() + ")"
    + "\n + "
    + "\n" +  x1.toString(0x10) + "(" + x1.toString() + ")"
    + "\n -> "
    + "\n" + x2.toString(0x10) + "(" + x2.toString() + ")"
    + "\n\n" + y0.toString()
    + "\n + "
    + "\n" +  y1.toString()
    + "\n -> "
    + "\n" + y2.toString()
    + "\n\n" + x3.toString(0x10) + "(" + x3.toString() + ")"
    + "\n\n"); }

  public static final <T extends Ringlike<T>> void
  absDiff (final Function<BigInteger,T> fromBI,
           final Function<T,BigInteger> toBI,
           final BigInteger z0,
           final BigInteger z1) {
    final BigInteger x0 = z0.max(z1);
    final BigInteger x1 = z0.min(z1);
    final T y0 = fromBI.apply(x0);
    final T y1 = fromBI.apply(x1);
    final BigInteger x2 = x0.subtract(x1);
    final T y2 = y0.subtract(y1);
    final BigInteger x3 = toBI.apply( y2);
    Assertions.assertEquals(x2,x3,() ->
    x0.toString(0x10)
    + "\n - "
    + "\n" +  x1.toString(0x10)
    + "\n -> "
    + "\n" + x2.toString(0x10)
    + "\n" + y0.toString()
    + "\n - "
    + "\n" +  y1.toString()
    + "\n -> "
    + "\n" + y2.toString()
    + "\n" + x3.toString(0x10)); }

  public static final <T extends Ringlike<T>> void
  subtract (final Function<BigInteger,T> fromBI,
            final Function<T,BigInteger> toBI,
            final BigInteger x0,
            final BigInteger x1) {
    final T y0 = fromBI.apply(x0);
    final T y1 = fromBI.apply(x1);
    final BigInteger x2 = x0.subtract(x1);
    final T y2 = y0.subtract(y1);
    final BigInteger x3 = toBI.apply( y2);
    Assertions.assertEquals(x2,x3,() ->
    x0.toString(0x10)
    + "\n - "
    + "\n" +  x1.toString(0x10)
    + "\n -> "
    + "\n" + x2.toString(0x10)
    + "\n" + y0.toString()
    + "\n - "
    + "\n" +  y1.toString()
    + "\n -> "
    + "\n" + y2.toString()
    + "\n" + x3.toString(0x10)); }

  public static final <T extends Ringlike<T>> void
  square (final Function<BigInteger,T> fromBI,
          final Function<T,BigInteger> toBI,
          final BigInteger x0) {
    final BigInteger x2 = x0.multiply(x0);
    final T y0 = fromBI.apply(x0);
    final T y2 = y0.square();
    final BigInteger x3 = toBI.apply(y2);
    final BigInteger dx = x2.subtract(x3);
    Assertions.assertEquals(x2,x3,()->
    "\n" + x0.toString(0x10)
    + "\n square -> "
    + "\n" + x2.toString(0x10)
    + "\n\n" + y0.toString()
    + "\n square -> "
    + "\n" + y2.toString()
    + "\n\n" + x3.toString(0x10)
    + "\n" + x2.toString(0x10)
    + "\n\n" + dx.toString(0x10)); }

  public static final <T extends Ringlike<T>> void
  multiply (final Function<BigInteger,T> fromBI,
            final Function<T,BigInteger> toBI,
            final BigInteger x0,
            final BigInteger x1) {
    final BigInteger x2 = x0.multiply(x1);
    final T y0 = fromBI.apply(x0);
    final T y1 = fromBI.apply(x1);
    final T y2 = y0.multiply(y1);
    final BigInteger x3 = toBI.apply(y2);
    final BigInteger dx = x2.subtract(x3);
    Assertions.assertEquals(x2,x3,
      () ->
    "\n" + x0.toString(0x10)
    + "\n * "
    + "\n" +  x1.toString(0x10)
    + "\n -> "
    + "\n" + x2.toString(0x10)
    + "\n\n" + y0.toString()
    + "\n * "
    + "\n" +  y1.toString()
    + "\n -> "
    + "\n" + y2.toString()
    + "\n\n" + x3.toString(0x10)
    + "\n\n" + dx.toString(0x10)); }

  public static final <T extends Ringlike<T>> void
  divide (final Function<BigInteger,T> fromBI,
          final Function<T,BigInteger> toBI,
          final BigInteger x0,
          final BigInteger x1) {
    if (0 != x1.signum()) {
      final T y0 = fromBI.apply(x0);
      final T y1 = fromBI.apply(x1);
      final BigInteger x2 = x0.divide(x1);
      final T y2 = y0.divide(y1);
      final BigInteger x3 = toBI.apply( y2);
      Assertions.assertEquals(x2,x3,() ->
      x0.toString(0x10)
      + "\n / "
      + "\n" +  x1.toString(0x10)
      + "\n -> "
      + "\n" + x2.toString(0x10)
      + "\n" + y0.toString()
      + "\n / "
      + "\n" +  y1.toString()
      + "\n -> "
      + "\n" + y2.toString()
      + "\n" + x3.toString(0x10)); } }

  public static final <T extends Ringlike<T>> void
  divideAndRemainder (final Function<BigInteger,T> fromBI,
                      final Function<T,BigInteger> toBI,
                      final BigInteger x0,
                      final BigInteger x1) {
    if (0 != x1.signum()) {
      final Ringlike y0 = fromBI.apply(x0);
      final Ringlike y1 = fromBI.apply(x1);
      final BigInteger[] x2 = x0.divideAndRemainder(x1);
      final List<T> y2 = y0.divideAndRemainder(y1);
      final BigInteger[] x3 = { toBI.apply(y2.get(0)), 
                                toBI.apply(y2.get(1)),};

      Assertions.assertEquals(x2[0],x3[0],() ->
      x0.toString(0x10)
      + "\n / "
      + "\n" +  x1.toString(0x10)
      + "\n -> "
      + "\n" + x2[0].toString(0x10)
      + "\n" + y0.toString()
      + "\n / "
      + "\n" +  y1.toString()
      + "\n -> "
      + "\n" + y2.get(0).toString()
      + "\n" + x3[0].toString(0x10));

      Assertions.assertEquals(x2[1],x3[1],() ->
      x0.toString(0x10)
      + "\n rem "
      + "\n" +  x1.toString(0x10)
      + "\n -> "
      + "\n" + x2[1].toString(0x10)
      + "\n" + y0.toString()
      + "\n rem "
      + "\n" +  y1.toString()
      + "\n -> "
      + "\n" + y2.get(1).toString()
      + "\n" + x3[1].toString(0x10)); } }

  public static final void 
  divideAndRemainderKnuth (final Function<BigInteger,Natural> fromBI,
                           final Function<Natural,BigInteger> toBI,
                           final BigInteger x0,
                           final BigInteger x1) {
    if (0 != x1.signum()) {
      final Natural y0 = fromBI.apply(x0);
      final Natural y1 = fromBI.apply(x1);
      final BigInteger[] x2 = x0.divideAndRemainder(x1);
      final List<Natural> y2 = y0.divideAndRemainderKnuth(y1);
      final BigInteger[] x3 = { toBI.apply(y2.get(0)), 
                                toBI.apply(y2.get(1)),};

      Assertions.assertEquals(x2[0],x3[0],() ->
      x0.toString(0x10)
      + "\n / "
      + "\n" +  x1.toString(0x10)
      + "\n -> "
      + "\n" + x2[0].toString(0x10)
      + "\n" + y0.getClass()
      + "\n" + y0.toString()
      + "\n / "
      + "\n" +  y1.toString()
      + "\n -> "
      + "\n" + y2.get(0).getClass()
      + "\n" + y2.get(0).toString()
      + "\n" + x3[0].toString(0x10));

      Assertions.assertEquals(x2[1],x3[1],() ->
      x0.toString(0x10)
      + "\n rem "
      + "\n" +  x1.toString(0x10)
      + "\n -> "
      + "\n" + x2[1].toString(0x10)
      + "\n" + y0.toString()
      + "\n rem "
      + "\n" +  y1.toString()
      + "\n -> "
      + "\n" + y2.get(1).toString()
      + "\n" + x3[1].toString(0x10)); } }

  public static final void
  divideAndRemainderBurnikelZiegler (final Function<BigInteger,Natural> fromBI,
                                     final Function<Natural,BigInteger> toBI,
                                     final BigInteger x0,
                                     final BigInteger x1) {
    if (0 != x1.signum()) {
      final Natural y0 = fromBI.apply(x0);
      final Natural y1 = fromBI.apply(x1);
      final BigInteger[] x2 = x0.divideAndRemainder(x1);
      final List<Natural> y2 = y0.divideAndRemainderBurnikelZiegler(y1);
      final Natural q = y2.get(0);
      final Natural r = y2.get(1);
      final BigInteger[] x3 = { toBI.apply(q), toBI.apply(r),};

      Assertions.assertEquals(x2[0],x3[0],() ->
      "\n" + x0.toString(0x10)
      + "\n / "
      + "\n" +  x1.toString(0x10)
      + "\n -> "
      + "\n" + x2[0].toString(0x10)
      + "\n" + y0.toString()
      + "\n / "
      + "\n" +  y1.toString()
      + "\n -> "
      + "\n" + y2.get(0).toString()
      + "\n" + x3[0].toString(0x10) + "\n");

      Assertions.assertEquals(x2[1],x3[1],() ->
      "\n" + x0.toString(0x10)
      + "\n rem "
      + "\n" +  x1.toString(0x10)
      + "\n -> "
      + "\n" + x2[1].toString(0x10)
      + "\n" + y0.toString()
      + "\n rem "
      + "\n" +  y1.toString()
      + "\n -> "
      + "\n" + y2.get(1).toString()
      + "\n" + x3[1].toString(0x10) + "\n"); } }

  public static final <T extends Ringlike<T>> void
  remainder (final Function<BigInteger,T> fromBI,
             final Function<T,BigInteger> toBI,
             final BigInteger x0,
             final BigInteger x1) {
    if (0 != x1.signum()) {
      final T y0 = fromBI.apply(x0);
      final T y1 = fromBI.apply(x1);
      final BigInteger x2 = x0.remainder(x1);
      final T y2 = y0.remainder(y1);
      final BigInteger x3 = toBI.apply(y2);

      Assertions.assertEquals(x2,x3,() ->
      x0.toString(0x10)
      + "\n rem "
      + "\n" +  x1.toString(0x10)
      + "\n -> "
      + "\n" + x2.toString(0x10)
      + "\n" + y0.toString()
      + "\n rem "
      + "\n" +  y1.toString()
      + "\n -> "
      + "\n" + y2.toString()
      + "\n" + x3.toString(0x10)); } }

  public static final <T extends Ringlike<T>> void
  gcd (final Function<BigInteger,T> fromBI,
       final Function<T,BigInteger> toBI,
       final BigInteger x0,
       final BigInteger x1) {
    if (0 != x1.signum()) {
      final T y0 = fromBI.apply(x0);
      final T y1 = fromBI.apply(x1);
      final BigInteger x2 = x0.gcd(x1);
      final T y2 = y0.gcd(y1);
      final BigInteger x3 = toBI.apply(y2);

      Assertions.assertEquals(x2,x3,() ->
      x0.toString(0x10)
      + "\n gcd "
      + "\n" +  x1.toString(0x10)
      + "\n -> "
      + "\n" + x2.toString(0x10)
      + "\n" + y0.toString()
      + "\n gcd "
      + "\n" +  y1.toString()
      + "\n -> "
      + "\n" + y2.toString()
      + "\n" + x3.toString(0x10)); } }

  public static final <T extends Ringlike<T>> void
  reduce (final Function<BigInteger,T> fromBI,
          final Function<T,BigInteger> toBI,
          final BigInteger x0,
          final BigInteger x1) {
    if (0 != x1.signum()) {
      final Ringlike y0 = fromBI.apply(x0);
      final Ringlike y1 = fromBI.apply(x1);
      final List<BigInteger> x2 = NaturalDivide.reduce(x0,x1);
      final List<T> y2 = y0.reduce(y1);
      final List<BigInteger> x3 = 
        List.of (
          toBI.apply(y2.get(0)), 
          toBI.apply(y2.get(1)));

      Assertions.assertEquals(x2.get(0),x3.get(0),() ->
      x0.toString(0x10)
      + "\n / "
      + "\n" +  x1.toString(0x10)
      + "\n -> "
      + "\n" + x2.get(0).toString(0x10)
      + "\n" + y0.toString()
      + "\n / "
      + "\n" +  y1.toString()
      + "\n -> "
      + "\n" + y2.get(0).toString()
      + "\n" + x3.get(0).toString(0x10));

      Assertions.assertEquals(x2.get(1),x3.get(1),() ->
      x0.toString(0x10)
      + "\n rem "
      + "\n" +  x1.toString(0x10)
      + "\n -> "
      + "\n" + x2.get(1).toString(0x10)
      + "\n" + y0.toString()
      + "\n rem "
      + "\n" +  y1.toString()
      + "\n -> "
      + "\n" + y2.get(1).toString()
      + "\n" + x3.get(1).toString(0x10)); } }

  //--------------------------------------------------------------

  public static final void
  naturalTest (final Function<String,Natural> valueOf,
               final Function<BigInteger,Natural> fromBI,
               final Function<Natural,BigInteger> toBI,
               final BigInteger z0,
               final BigInteger z1) {
    assert 0<=z0.signum();
    assert 0<=z1.signum();
    //Debug.println("z0=" + z0.toString(0x10));
    //Debug.println("z1=" + z1.toString(0x10));
    // TODO: test shifting and other bit ops. uintsTest?
    hiBit(fromBI,z0);
    hiBit(fromBI,z1);
    hexRoundTrip(fromBI,valueOf,z0);
    hexRoundTrip(fromBI,valueOf,z1);
    biRoundTrip(fromBI,toBI,z0);
    biRoundTrip(fromBI,toBI,z1);
    compare(fromBI,z0,z1);
    add(fromBI,toBI,z0,z1);
    add(fromBI,toBI,z0,z0);
    absDiff(fromBI,toBI,z0,z1);
    absDiff(fromBI,toBI,z0,z0);
    multiply(fromBI,toBI,z0,z0);
    square(fromBI,toBI,z0);
    multiply(fromBI,toBI,z1,z1);
    square(fromBI,toBI,z1);
    multiply(fromBI,toBI,z0,z0);
    multiply(fromBI,toBI,z0,z1);
    divide(fromBI,toBI,z0,z1);
    divide(fromBI,toBI,z0,z0);
    divideAndRemainder(fromBI,toBI,z0,z1);
    divideAndRemainder(fromBI,toBI,z0,z0);
    divideAndRemainderKnuth(fromBI,toBI,z0,z1);
    divideAndRemainderKnuth(fromBI,toBI,z0,z0);
    divideAndRemainderBurnikelZiegler(fromBI,toBI,z0,z1);
    divideAndRemainderBurnikelZiegler(fromBI,toBI,z0,z0);
    remainder(fromBI,toBI,z0,z1);
    remainder(fromBI,toBI,z0,z0);
    gcd(fromBI,toBI,z0,z1);
    gcd(fromBI,toBI,z0,z0);
    reduce(fromBI,toBI,z0,z1);
    reduce(fromBI,toBI,z0,z0);
  }


  public static final void
  naturalTest (final Function<String,Natural> valueOf,
               final Function<BigInteger,Natural> fromBI,
               final Function<Natural,BigInteger> toBI) {
    final Generator gn =
      Generators.bigIntegerGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      naturalTest(valueOf,fromBI,toBI,
        ((BigInteger) gn.next()).abs(),
        ((BigInteger) gn.next()).abs()); } }

  //--------------------------------------------------------------

  public static final void
  integerTest (final Function<String,Ringlike> valueOf,
               final Function<BigInteger,Ringlike> fromBI,
               final Function<Ringlike,BigInteger> toBI,
               final BigInteger z0,
               final BigInteger z1) {
    hexRoundTrip(fromBI,valueOf,z0);
    hexRoundTrip(fromBI,valueOf,z1);
    biRoundTrip(fromBI,toBI,z0);
    biRoundTrip(fromBI,toBI,z1);
    add(fromBI,toBI,z0,z1);
    add(fromBI,toBI,z0,z0);
    absDiff(fromBI,toBI,z0,z1);
    absDiff(fromBI,toBI,z0,z0);
    subtract(fromBI,toBI,z0,z1);
    subtract(fromBI,toBI,z0,z0);
    square(fromBI,toBI,z0);
    square(fromBI,toBI,z1);
    multiply(fromBI,toBI,z0,z1);
    multiply(fromBI,toBI,z0,z0);
    divide(fromBI,toBI,z0,z1);
    divide(fromBI,toBI,z0,z0);
    divideAndRemainder(fromBI,toBI,z0,z1);
    divideAndRemainder(fromBI,toBI,z0,z0);
    gcd(fromBI,toBI,z0,z1);
    gcd(fromBI,toBI,z0,z0); }

  public static final void
  integerTest (final Function<String,Ringlike> valueOf,
               final Function<BigInteger,Ringlike> fromBI,
               final Function<Ringlike,BigInteger> toBI) {
    final Generator gn =
      Generators.bigIntegerGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      integerTest(valueOf,fromBI,toBI,
        (BigInteger) gn.next(), (BigInteger) gn.next()); } }

  //--------------------------------------------------------------

  public static final void
  ringlikeTest (final Function<String,Ringlike> valueOf,
                final Function<BigInteger,Ringlike> fromBI,
                final Function<Ringlike,BigInteger> toBI) {
    final Generator gn =
      Generators.bigIntegerGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      integerTest(valueOf,fromBI,toBI,
        (BigInteger) gn.next(), (BigInteger) gn.next()); } }

  public static final void
  ringlikeTest (final Function<String,Ringlike> valueOf,
                final Function<BigInteger,Ringlike> fromBI,
                final Function<Ringlike,BigInteger> toBI,
                final BigInteger z0,
                final BigInteger z1) {
    // TODO: more tests
    hexRoundTrip(fromBI,valueOf,z0);
    hexRoundTrip(fromBI,valueOf,z1);
    biRoundTrip(fromBI,toBI,z0);
    biRoundTrip(fromBI,toBI,z1);
    add(fromBI,toBI,z0,z1);
    add(fromBI,toBI,z0,z0);
    absDiff(fromBI,toBI,z0,z1);
    absDiff(fromBI,toBI,z0,z0);
    subtract(fromBI,toBI,z0,z1);
    subtract(fromBI,toBI,z0,z0);
    square(fromBI,toBI,z0);
    square(fromBI,toBI,z1);
    multiply(fromBI,toBI,z0,z1);
    multiply(fromBI,toBI,z0,z0);
    divide(fromBI,toBI,z0,z1);
    divide(fromBI,toBI,z0,z0); }

  //--------------------------------------------------------------
  // TODO: java missing corresponding FloatFunction, etc.

  public static final void
  floatRoundTripTest (final FloatFunction fromFloat,
                      final ToFloatFunction toFloat,
                      final float x0) {
    final Object f = fromFloat.apply(x0);
    final float x1 = toFloat.applyAsFloat(f);
    // differentiate -0.0, 0.0 and handle NaN
    Assertions.assertEquals(0,Float.compare(x0,x1),
      () ->
    Float.toString(x0)
    + "\n->" + f.toString()
    + "\n->" + Float.toString(x1)); }

  //--------------------------------------------------------------

  public static final void
  floatRoundingTest (final FloatFunction<Comparable> fromFloat,
                     final ToFloatFunction toFloat,
                     final BinaryOperator<Comparable> dist,
                     @SuppressWarnings("unused")
  final Function<Comparable,String> toString,
  final Comparable f) {

    //Debug.println("f=" + toString.apply(f));

    final float x = toFloat.applyAsFloat(f);
    //Debug.println("x=" + Float.toString(x));

    // only check finite numbers for now
    if (Float.isFinite(x)) {
      final Comparable fx = fromFloat.apply(x);
      //Debug.println("fx=" + toString.apply(fx));
      //final int r = f.compareTo(fx);

      final float x1o = Math.nextDown(x);
      final float xhi = Math.nextUp(x);
      //Debug.println("xlo=" + Float.toString(x1o));
      //Debug.println("xhi=" + Float.toString(xhi));

      final Comparable flo = fromFloat.apply(x1o);
      final Comparable fhi = fromFloat.apply(xhi);
      //Debug.println("flo=" + toString.apply(flo));
      //Debug.println("fhi=" + toString.apply(fhi));

      //Debug.println("r=" + r);
      //if (r < 0) { // f < fx
      Assertions.assertTrue(flo.compareTo(f) < 0);
      //}
      //if (r > 0) { // f > fx
      Assertions.assertTrue(f.compareTo(fhi) < 0);
      // }

      final Comparable dlo = dist.apply(f,flo);
      final Comparable dx = dist.apply(f,fx);
      final Comparable dhi = dist.apply(f,fhi);
      //Debug.println("|f-flo|= " + toString.apply(dlo));
      //Debug.println("|f-fx |= " + toString.apply(dx));
      //Debug.println("|f-fhi|= " + toString.apply(dhi));
      Assertions.assertTrue(dx.compareTo(dlo) <= 0);
      Assertions.assertTrue(dx.compareTo(dhi) <= 0);
      if (dx.equals(dlo) || dx.equals(dhi)) {
        Assertions.assertTrue(Floats.isEven(x)); } } }

  //--------------------------------------------------------------

  private static final void
  floatRoundingTest (final BiFunction<BigInteger,BigInteger,Comparable> fromBigIntegers,
                     final FloatFunction<Comparable> fromFloat,
                     final ToFloatFunction toFloat,
                     final BinaryOperator<Comparable> dist,
                     final Function<Comparable,String> string) {

    floatRoundingTest(fromFloat,toFloat,dist,string,
      fromBigIntegers.apply(
        BigInteger.valueOf(13),
        BigInteger.valueOf(11)));

    //Debug.DEBUG=false;

    floatRoundingTest(fromFloat,toFloat,dist,string,
      fromBigIntegers.apply(
        BigInteger.valueOf(-0x331c0c32d0072fL),
        BigInteger.valueOf(0x1000000L)));

    floatRoundingTest(fromFloat,toFloat,dist,string,
      fromBigIntegers.apply(
        BigInteger.valueOf(0x331c0c32d0072fL),
        BigInteger.valueOf(0x1000000L)));

    //Debug.DEBUG=false;

    floatRoundingTest(fromFloat,toFloat,dist,string,
      fromBigIntegers.apply(
        BigInteger.valueOf(0x789f09858446ad92L),
        BigInteger.valueOf(0x19513ea5d70c32eL))); }

  private static final void
  fromBigIntegersRoundingTest (final BiFunction<BigInteger,BigInteger,Comparable> fromBigIntegers,
                               final FloatFunction<Comparable> fromFloat,
                               final ToFloatFunction toFloat,
                               final BinaryOperator<Comparable> dist,
                               final Function<Comparable,String> string) {
    final Generator gn =
      Generators.bigIntegerGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    final Generator gd =
      Generators.positiveBigIntegerGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-07.txt"));
    //Debug.DEBUG=false;
    for (int i=0;i<TRYS;i++) {
      // some longs will not be exactly representable as floats
      final BigInteger n = (BigInteger) gn.next();
      final BigInteger d = (BigInteger) gd.next();
      floatRoundingTest(fromFloat,toFloat,dist,string,
        fromBigIntegers.apply(n,d)); }
    //Debug.DEBUG=false;
  }

  private static final void
  fromLongsRoundingTest (final BiFunction<BigInteger,BigInteger,Comparable> fromBigIntegers,
                         final FloatFunction<Comparable> fromFloat,
                         final ToFloatFunction toFloat,
                         final BinaryOperator<Comparable> dist,
                         final Function<Comparable,String> string) {
    final Generator g0 =
      Generators.longGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    final Generator g1 =
      Generators.positiveLongGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-07.txt"));
    for (int i=0;i<TRYS;i++) {
      // some longs will not be exactly representable as floats
      final long n = g0.nextLong();
      final long d = g1.nextLong();
      floatRoundingTest(fromFloat,toFloat,dist,string,
        fromBigIntegers.apply(
          BigInteger.valueOf(n),
          BigInteger.valueOf(d))); } }

  private static final void
  finiteFloatRoundingTest (final FloatFunction<Comparable> fromFloat,
                           final ToFloatFunction toFloat,
                           final BinaryOperator<Comparable> dist,
                           final Function<Comparable,String> string) {
    final Generator g =
      Floats.finiteGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final float x = g.nextFloat();
      floatRoundTripTest(fromFloat,toFloat,x);
      final Comparable f = fromFloat.apply(x);
      floatRoundingTest(fromFloat,toFloat,dist,string,f); } }

  private static final void
  subnormalFloatRoundingTest (final FloatFunction<Comparable> fromFloat,
                              final ToFloatFunction toFloat,
                              final BinaryOperator<Comparable> dist,
                              final Function<Comparable,String> string) {
    final Generator g =
      Floats.subnormalGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final float x = g.nextFloat();
      floatRoundTripTest(fromFloat,toFloat,x);
      final Comparable f = fromFloat.apply(x);
      floatRoundingTest(fromFloat,toFloat,dist,string,f); } }

  public static final void
  floatRoundingTests (final BiFunction<BigInteger,BigInteger,Comparable> fromBigIntegers,
                      final FloatFunction<Comparable> fromFloat,
                      final ToFloatFunction toFloat,
                      final BinaryOperator<Comparable> dist,
                      final Function<Comparable,String> string) {

    if (null != fromBigIntegers) {
      floatRoundingTest(
        fromBigIntegers,fromFloat,toFloat,dist,string);
      fromBigIntegersRoundingTest(
        fromBigIntegers,fromFloat,toFloat,dist,string);
      fromLongsRoundingTest(
        fromBigIntegers,fromFloat,toFloat,dist,string); }
    finiteFloatRoundingTest(fromFloat,toFloat,dist,string);
    subnormalFloatRoundingTest(fromFloat,toFloat,dist,string);
  }

  //--------------------------------------------------------------
  // TODO: java missing corresponding FloatFunction, etc.

  public static final void
  doubleRoundTripTest (final DoubleFunction fromDouble,
                       final ToDoubleFunction toDouble,
                       final double x0) {
    final Object f = fromDouble.apply(x0);
    final double x1 = toDouble.applyAsDouble(f);
    // differentiate -0.0, 0.0 and handle NaN
    Assertions.assertEquals(0,Double.compare(x0,x1),
      () ->
    Double.toString(x0)
    + "\n->" + f.toString()
    + "\n->" + Double.toString(x1)); }

  //--------------------------------------------------------------

  @SuppressWarnings("unused")
  public static final void
  doubleRoundingTest (final DoubleFunction<Comparable> fromDouble,
                      final ToDoubleFunction toDouble,
                      final BinaryOperator<Comparable> dist,
                      final Function<Comparable,String> toString,
                      final Comparable f) {

    //Debug.println("f=" + toString.apply(f));

    final double x = toDouble.applyAsDouble(f);
    //Debug.println("x=" + Double.toString(x));

    // only check finite numbers for now
    if (Double.isFinite(x)) {
      final Comparable fx = fromDouble.apply(x);

      final double x1o = Math.nextDown(x);
      final double xhi = Math.nextUp(x);
      //Debug.println("xlo=" + Double.toString(x1o));
      //Debug.println("x  =" + Double.toString(x));
      //Debug.println("xhi=" + Double.toString(xhi));

      final Comparable flo = fromDouble.apply(x1o);
      final Comparable fhi = fromDouble.apply(xhi);
      //Debug.println("flo=" + toString.apply(flo));
      //Debug.println("f  =" + toString.apply(f));
      //Debug.println("fx =" + toString.apply(fx));
      //Debug.println("fhi=" + toString.apply(fhi));

      //final int r = f.compareTo(fx);
      //Debug.println("r=" + r);

      final Comparable dlo = dist.apply(f,flo);
      final Comparable dx = dist.apply(f,fx);
      final Comparable dhi = dist.apply(f,fhi);
      //Debug.println("|f-flo|= " + toString.apply(dlo));
      //Debug.println("|f-fx |= " + toString.apply(dx));
      //Debug.println("|f-fhi|= " + toString.apply(dhi));
      Assertions.assertTrue(flo.compareTo(f) < 0,
        "\nf=" + f + " > flo=" + flo);
      Assertions.assertTrue(f.compareTo(fhi) < 0,
        "\nf=" + f + " < fhi=" + fhi);
      Assertions.assertTrue(flo.compareTo(fx) <= 0);
      Assertions.assertTrue(fx.compareTo(fhi) <= 0);
      Assertions.assertTrue(dx.compareTo(dlo) <= 0,
        "dx=" + dx + " > dlo=" + dlo);
      Assertions.assertTrue(dx.compareTo(dhi) <= 0);
      if (dx.equals(dlo) || dx.equals(dhi)) {
        Assertions.assertTrue(Doubles.isEven(x),
          () ->
        "not even!"
        + "\nxlo=" + Double.toString(x1o)
        + "\nx  =" + Double.toString(x)
        + "\nxhi=" + Double.toString(xhi)
        + "\ndlo=" + dlo
        + "\ndx =" + dx
        + "\ndhi=" + dhi); } } }

  //--------------------------------------------------------------

  private static final void
  doubleRoundingTest (final BiFunction<BigInteger,BigInteger,Comparable> fromBigIntegers,
                      final DoubleFunction<Comparable> fromDouble,
                      final ToDoubleFunction toDouble,
                      final BinaryOperator<Comparable> dist,
                      final Function<Comparable,String> string) {

    doubleRoundingTest(fromDouble,toDouble,dist,string,
      fromBigIntegers.apply(
        BigInteger.valueOf(13),
        BigInteger.valueOf(11)));

    //Debug.DEBUG=false;

    doubleRoundingTest(fromDouble,toDouble,dist,string,
      fromBigIntegers.apply(
        BigInteger.valueOf(-0x331c0c32d0072fL),
        BigInteger.valueOf(0x1000000L)));

    doubleRoundingTest(fromDouble,toDouble,dist,string,
      fromBigIntegers.apply(
        BigInteger.valueOf(0x331c0c32d0072fL),
        BigInteger.valueOf(0x1000000L)));

    //Debug.DEBUG=false;

    doubleRoundingTest(fromDouble,toDouble,dist,string,
      fromBigIntegers.apply(
        BigInteger.valueOf(0x789f09858446ad92L),
        BigInteger.valueOf(0x19513ea5d70c32eL))); }

  private static final void
  fromBigIntegersRoundingTest (final BiFunction<BigInteger,BigInteger,Comparable> fromBigIntegers,
                               final DoubleFunction<Comparable> fromDouble,
                               final ToDoubleFunction toDouble,
                               final BinaryOperator<Comparable> dist,
                               final Function<Comparable,String> string) {
    final Generator gn =
      Generators.bigIntegerGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    final Generator gd =
      Generators.positiveBigIntegerGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-07.txt"));
    for (int i=0;i<TRYS;i++) {
      // some longs will not be exactly representable as doubles
      final BigInteger n = (BigInteger) gn.next();
      final BigInteger d = (BigInteger) gd.next();
      doubleRoundingTest(fromDouble,toDouble,dist,string,
        fromBigIntegers.apply(n,d)); } }

  private static final void
  fromLongsRoundingTest (final BiFunction<BigInteger,BigInteger,Comparable> fromBigIntegers,
                         final DoubleFunction<Comparable> fromDouble,
                         final ToDoubleFunction toDouble,
                         final BinaryOperator<Comparable> dist,
                         final Function<Comparable,String> string) {
    final Generator g0 =
      Generators.longGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    final Generator g1 =
      Generators.positiveLongGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-07.txt"));
    for (int i=0;i<TRYS;i++) {
      // some longs will not be exactly representable as doubles
      final long n = g0.nextLong();
      final long d = g1.nextLong();
      doubleRoundingTest(fromDouble,toDouble,dist,string,
        fromBigIntegers.apply(
          BigInteger.valueOf(n),
          BigInteger.valueOf(d))); } }

  private static final void
  finiteDoubleRoundingTest (final DoubleFunction<Comparable> fromDouble,
                            final ToDoubleFunction toDouble,
                            final BinaryOperator<Comparable> dist,
                            final Function<Comparable,String> string) {
    final Generator g =
      Doubles.finiteGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      doubleRoundTripTest(fromDouble,toDouble,x);
      final Comparable f = fromDouble.apply(x);
      doubleRoundingTest(fromDouble,toDouble,dist,string,f); } }

  private static final void
  subnormalDoubleRoundingTest (final DoubleFunction<Comparable> fromDouble,
                               final ToDoubleFunction toDouble,
                               final BinaryOperator<Comparable> dist,
                               final Function<Comparable,String> string) {
    final Generator g =
      Doubles.subnormalGenerator(
        PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    for (int i=0;i<TRYS;i++) {
      final double x = g.nextDouble();
      doubleRoundTripTest(fromDouble,toDouble,x);
      final Comparable f = fromDouble.apply(x);
      doubleRoundingTest(fromDouble,toDouble,dist,string,f); } }

  public static final void
  doubleRoundingTests (final BiFunction<BigInteger,BigInteger,Comparable> fromBigIntegers,
                       final DoubleFunction<Comparable> fromDouble,
                       final ToDoubleFunction toDouble,
                       final BinaryOperator<Comparable> dist,
                       final Function<Comparable,String> string) {

    if (null != fromBigIntegers) {
      doubleRoundingTest(
        fromBigIntegers,fromDouble,toDouble,dist,string);
      fromBigIntegersRoundingTest(
        fromBigIntegers,fromDouble,toDouble,dist,string);
      fromLongsRoundingTest(
        fromBigIntegers,fromDouble,toDouble,dist,string); }
    finiteDoubleRoundingTest(fromDouble,toDouble,dist,string);
    subnormalDoubleRoundingTest(fromDouble,toDouble,dist,string);
  }

  //--------------------------------------------------------------
  /** See {@link Integer#numberOfLeadingZeros(int)}. */
  public static final int ceilLog2 (final int k) {
    return Integer.SIZE - Integer.numberOfLeadingZeros(k-1); }

  // TODO: more efficient via bits?
  public static final boolean isEven (final int k) {
    return k == (2*(k/2)); }

  //--------------------------------------------------------------
  /** Maximum exponent for double generation such that a float
   * sum of <code>dim</code> <code>double</code>s will be finite
   * (with high enough probability).
   */
  //  public static final int feMax (final int dim) {
  //    final int d = Float.MAX_EXPONENT - ceilLog2(dim);
  //    return d; }

  /** Maximum exponent for double generation such that a double
   * sum of <code>dim</code> <code>double</code>s will be finite
   * (with high enough probability).
   */
  public static final int deMax (final int dim) {
    final int d = Double.MAX_EXPONENT - ceilLog2(dim);
    return d; }

  //--------------------------------------------------------------

  private static final List<Generator> baseGenerators (final int dim) {
    final UniformRandomProvider urp0 =
      PRNG.well44497b("seeds/Well44497b-2019-01-05.txt");
    final UniformRandomProvider urp1 =
      PRNG.well44497b("seeds/Well44497b-2019-01-07.txt");
    final UniformRandomProvider urp2 =
      PRNG.well44497b("seeds/Well44497b-2019-01-09.txt");
    final UniformRandomProvider urp3 =
      PRNG.well44497b("seeds/Well44497b-2019-01-11.txt");
    final UniformRandomProvider urp4 =
      PRNG.well44497b("seeds/Well44497b-2019-04-01.txt");

    // as large as will still have finite float l2 norm squared
    final int emax = deMax(dim)/2;
    final double dmax = (1<<emax);
    return Arrays.asList(
      new Generator[]
        {
         Doubles.gaussianGenerator(dim,urp1,0.0,dmax),
         Doubles.exponentialGenerator(dim,urp2,0.0,dmax),
         Doubles.laplaceGenerator(dim,urp3,0.0,dmax),
         Doubles.uniformGenerator(dim,urp4,-dmax,dmax),
         Doubles.finiteGenerator(dim,urp0,emax),
        }); }

  private static final List<Generator>
  zeroSumGenerators (final List<Generator> gs0) {

    final List<Generator> gs1 =
      gs0.stream().map(Doubles::zeroSumGenerator)
      .collect(Collectors.toUnmodifiableList());
    final UniformRandomProvider urp =
      PRNG.well44497b("seeds/Well44497b-2019-04-09.txt");
    final List<Generator> gs2 =
      gs1.stream().map((g) -> Doubles.shuffledGenerator(g,urp))
      .collect(Collectors.toUnmodifiableList());
    return
      Stream
      .concat(gs1.stream(),gs2.stream())
      .collect(Collectors.toUnmodifiableList()); }

  //--------------------------------------------------------------
  /** Generate <code>double[dim]</code> such that the sum of the
   * squares (and the sum of the elements) is very likely to be
   * finite.
   */

  public static final List<Generator>
  zeroSumGenerators (final int dim) {
    return zeroSumGenerators(baseGenerators(dim)); }

  //--------------------------------------------------------------
  /** Generate <code>double[dim]</code> such that the sum of the
   * squares (and the sum of the elements) is very likely to be
   * finite.
   */

  public static final List<Generator> generators (final int dim) {
    final List<Generator> gs0 = baseGenerators(dim);
    final List<Generator> gs1 = zeroSumGenerators(gs0);
    return
      Stream
      .concat(gs0.stream(),gs1.stream())
      .collect(Collectors.toUnmodifiableList()); }

  //--------------------------------------------------------------

  public static final void
  overflowTest (final Accumulator aa) {

    Accumulator a = aa.clear();
    a = a.add(0x0.1p0);
    double s = a.doubleValue();
    if (a.noOverflow()) {
      Assertions.assertEquals(0x0.1p0,s,
        "\n" + Classes.className(aa)
        + "\n!= " + Double.toHexString(s)
        + "\n!= " + s
        + "\n"); }
    a = a.add(0x0.1p0);
    s = a.doubleValue();
    if (a.noOverflow()) {
      Assertions.assertEquals(0x0.2p0,s,
        "\n" + Classes.className(aa)
        + "\n!= " + Double.toHexString(s)
        + "\n"); }
    a = a.add(0x1.0p0);
    s = a.doubleValue();
    if (a.noOverflow()) {
      Assertions.assertEquals(0x1.2p0,s,
        "\n" + Classes.className(aa)
        + "\n!= " + Double.toHexString(s)
        + "\n"); }
    a = a.add(-0x0.1p0);
    s = a.doubleValue();
    if (a.noOverflow()) {
      Assertions.assertEquals(0x1.1p0,s,
        "\n" + Classes.className(aa)
        + "\n!= " + Double.toHexString(s)
        + "\n"); }
    a = a.add(-0x0.1p0);
    s = a.doubleValue();
    if (a.noOverflow()) {
      Assertions.assertEquals(0x1.0p0,s,
        "\n" + Classes.className(aa)
        + "\n1.0p0!= " + Double.toHexString(s)
        + "\n"); }

    Accumulator a0 = aa.clear();
    a0 = a0.addAll(new double[]
      { 100.0, 100.0, 1.0, -100.0, -100.0});
    final double s0 = a0.doubleValue();
    if (a0.noOverflow()) {
      Assertions.assertEquals(1.0,s0,()->
      "\n" + Classes.className(aa)
      + "\n1.0p0!= " + Double.toHexString(s0)
      + "\n1.0e0!= " + s0
      + "\n"); }

    //Debug.DEBUG=true;
    Accumulator a1 = aa.clear();
    a1 = a1.addAll(new double[]
      { 1.0e-1, 1.0e-1, 1.0, -1.0e-1, -1.0e-1});
    final double s1 = a1.doubleValue();
    if (a1.noOverflow()) {
      Assertions.assertEquals(1.0,s1,()->
      "\n" + Classes.className(aa)
      + "\n1.0p0!= " + Double.toHexString(s1)
      + "\n1.0e0!= " + s1
      + "\n"); }
    Debug.DEBUG=false;

    Accumulator a2 = aa.clear();
    a2 = a2.addAll(new double[]
      { Double.MAX_VALUE,
        Double.MAX_VALUE,
        1.0,
        -Double.MAX_VALUE,
        -Double.MAX_VALUE});
    final double s2 = a2.doubleValue();
    if (a2.noOverflow()) {
      Assertions.assertEquals(1.0,s2,()->
      "\n" + Classes.className(aa)
      + "\n1.0p0!= " + Double.toHexString(s2)
      + "\n1.0e0!= " + s2
      + "\n"); }

    Accumulator a3 = aa.clear();
    a3 = a3.addAll(new double[]
      { -Double.MAX_VALUE,
        -Double.MAX_VALUE,
        -1.0,
        Double.MAX_VALUE,
        Double.MAX_VALUE});
    final double s3 = a3.doubleValue();
    if (a3.noOverflow()) {
      Assertions.assertEquals(-1.0,s3,()->
      "\n" + Classes.className(aa)
      + "\n-1.0p0!= " + Double.toHexString(s3)
      + "\n-1.0e0!= " + s3
      + "\n"); } }

  public static final void
  overflowTests (final List<Accumulator> accumulators) {
    for (final Accumulator a : accumulators) {
      overflowTest(a); } }

  //--------------------------------------------------------------

  // TODO: determine how accumulators should behave when given
  // non-finite input

  //  public static final void
  //  nonFiniteTest (final Accumulator a) {
  //
  //    Assertions.assertThrows(
  //      AssertionError.class,
  //      () -> {
  //        final double s0 =
  //          a.clear()
  //          .addAll(new double[] {-1.0, Double.POSITIVE_INFINITY, })
  //          .doubleValue();
  //        Assertions.assertEquals(Double.POSITIVE_INFINITY,s0,
  //          Classes.className(a)); },
  //      Classes.className(a));
  //
  //    Assertions.assertThrows(
  //      AssertionError.class,
  //      () -> {
  //        final double s2 =
  //          a.clear()
  //          .addAll(new double[] {-1.0, Double.NaN, })
  //          .doubleValue();
  //        Assertions.assertEquals(
  //          Double.NaN,s2,Classes.className(a));},
  //      Classes.className(a));
  //  }
  //
  //  public static final void
  //  nonFiniteTests (final List<Accumulator> accumulators) {
  //    for (final Accumulator a : accumulators) {
  //      nonFiniteTest(a); } }

  //--------------------------------------------------------------

  public static final void
  infinityTest (final Accumulator a) {

    final double s0 =
      a.clear()
      .addAll(new double[] {Double.MAX_VALUE, Double.MAX_VALUE, })
      .doubleValue();
    Assertions.assertEquals(Double.POSITIVE_INFINITY,s0,
      Classes.className(a));

    final double s1 =
      a.clear()
      .addAll(new double[] {-Double.MAX_VALUE, -Double.MAX_VALUE, })
      .doubleValue();
    Assertions.assertEquals(Double.NEGATIVE_INFINITY,s1,
      Classes.className(a));

  }

  public static final void
  infinityTests (final List<Accumulator> accumulators) {
    for (final Accumulator a : accumulators) {
      infinityTest(a); } }

  //--------------------------------------------------------------
  /** Assumes the generator creates arrays whose exact sum is 0.0
   */

  private static final void
  zeroSumTest (final Generator g,
               final List<Accumulator> accumulators) {
    final double[] x = (double[]) g.next();
    for (final Accumulator a : accumulators) {
      //final long t0 = System.nanoTime();
      final double pred = a.clear().addAll(x).doubleValue();
      //final long t1 = (System.nanoTime()-t0);
      if (a.isExact()) {
        Assertions.assertEquals(0.0,pred,
          "sum not zero: " + Classes.className(a)
          + " = " + Double.toHexString(pred) + "\n");  }
      //final double l1d = Math.abs(pred);
      //Debug.println(
      //  String.format("%32s %8.2fms ",Classes.className(a),
      //    Double.valueOf(t1*1.0e-6))
      //  + toString(l1d) + " = "
      //  + String.format("%8.2e",Double.valueOf(l1d)));
    } }

  /** Assumes the generators create arrays whose exact sum is 0.0
   */

  public static final void
  zeroSumTests (final List<Generator> generators,
                final List<Accumulator> accumulators) {
    for (final Generator g : generators) {
      Common.zeroSumTest(g,accumulators); } }

  //--------------------------------------------------------------

  private static final void
  addTest (final Generator g,
           final List<Accumulator> accumulators,
           final Accumulator base) {
    //Debug.DEBUG=false;
    //Assertions.assertTrue(base.isExact());
    final double[] x = (double[]) g.next();
    //Debug.println(Classes.className(base));
    //Debug.println(base.toString());
    //Debug.println(g.name());
    for (final Accumulator a : accumulators) {
      Accumulator e = base.clear();
      Accumulator p = a.clear();
      //Debug.println(Classes.className(p));
      for (final double xi : x) {
        //Debug.println();
        //Debug.println("xi=" + Double.toString(xi));
        //Debug.println(Classes.className(e));
        e = e.add(xi);
        p = p.add(xi);
        final double truth = e.doubleValue();
        final double pred = p.doubleValue();
        //Debug.println("truth=" + Double.toString(truth));
        //Debug.println("pred=" + Double.toString(pred));
        Assertions.assertEquals(truth,pred,
          "\nbase: " + Classes.className(base)
          + "\n= " + Double.toHexString(truth)
          + "\n= " + base.value()
          + "\npred: " + Classes.className(a)
          + "\n= " + Double.toHexString(pred)
          + "\n= " + a.value()
          + "\n"); } } 
    //Debug.DEBUG=false;
  }

  private static final void
  addAllTest (final Generator g,
              final List<Accumulator> accumulators,
              final Accumulator base) {
    //Debug.DEBUG=false;
    //Assertions.assertTrue(base.isExact());
    final double[] x = (double[]) g.next();
    final Accumulator e = base.clear().addAll(x);
    //Debug.println(Classes.className(base));
    //Debug.println(base.toString());
    final double truth = e.doubleValue();
    //Debug.println(g.name());
    for (final Accumulator a : accumulators) {
      //final long t0 = System.nanoTime();
      final Accumulator pfinal = a.clear().addAll(x);
      //Debug.println(Classes.className(a));
      //Debug.println(pfinal.value().toString());
      final double pred = pfinal.doubleValue();
      //final long t1 = (System.nanoTime()-t0);
      Assertions.assertEquals(truth,pred,
        "\nbase: " + Classes.className(base)
        + " = " + Double.toHexString(truth)
        + "\n= " + base.value()
        + "\npred: " + Classes.className(a)
        + " = " + Double.toHexString(pred)
        + "\n= " + a.value()
        + "\n"); 
      //final double l1d = Math.abs(truth-pred);
      //final double l1n = Math.max(1.0,Math.abs(truth));
      //      //Debug.println(
      //        String.format("%32s %8.2fms ",Classes.className(a),
      //          Double.valueOf(t1*1.0e-6))
      //        + toString(l1d)
      //        + " / " + toString(l1n) + " = "
      //        + String.format("%8.2e",Double.valueOf(l1d/l1n)));
      //Debug.DEBUG=false;
    } }

  public static final void
  sumTests (final List<Generator> generators,
            final List<Accumulator> accumulators,
            final Accumulator base) {
    for (final Generator g : generators) {
      Common.addTest(g,accumulators,base);
      Common.addAllTest(g,accumulators,base); } }

  //--------------------------------------------------------------

  private static final void l2Test (final Generator g,
                                    final List<Accumulator> accumulators,
                                    final Accumulator base) {
    //Debug.println("generator=" +g.name());
    //Assertions.assertTrue(base.isExact());
    final double[] x = (double[]) g.next();
    for (final Accumulator aa : accumulators) {
      Accumulator e = base.clear();
      Accumulator a = aa.clear();
      for (int i=0; i<x.length;i++) {
        final int ii = i;
        final double xi = x[i];
        e = e.add2(xi); 
        final double truth = e.doubleValue();
        a = a.add2(xi);
        final double pred = a.doubleValue();
        Assertions.assertEquals(truth,pred,()->
        Classes.className(aa)
        + "\ni=" + ii + "\n" + Double.toHexString(truth)
        + "\n" +  Double.toHexString(pred) + "\n"); } }
    final double truth = base.clear().add2All(x).doubleValue();
    for (final Accumulator a : accumulators) {
      final double pred = a.clear().add2All(x).doubleValue();
      Assertions.assertEquals(truth,pred,
        () ->
      "\n" + Double.toHexString(truth)
      + "\n" +  Double.toHexString(pred) + "\n"); } } 

  public static final void l2Tests (final List<Generator> generators,
                                    final List<Accumulator> accumulators,
                                    final Accumulator base) {

    for (final Generator g : generators) {
      l2Test(g,accumulators,base); } }

  //--------------------------------------------------------------

  private static final void
  l2DistanceTest (final List<Accumulator> accumulators,
                  final Accumulator base) {
    //Assertions.assertTrue(base.isExact());
    final double[] x0 = new double[4];
    final double[] x1 = new double[4];
    Arrays.fill(x0,1.0/3.0);
    final double truth =
      base.clear().addL2Distance(x0,x1).doubleValue();
    Assertions.assertTrue(0.0<=truth,
      "\n" + Classes.className(base) + "\n");
    //    Assertions.assertEquals(4.0,truth,
    //      "\n" + Classes.className(base) + "\n");
    for (final Accumulator a : accumulators) {
      final double pred =
        a.clear().addL2Distance(x0,x1).doubleValue();
      Assertions.assertTrue(0.0<=pred,
        "\n" + Classes.className(a) + "\n");
      Assertions.assertEquals(truth,pred,
        () ->
      "\n" + Double.toHexString(truth)
      + "\n" +  Double.toHexString(pred) + "\n"); } } 

  private static final void
  l2DistanceTest (final Generator g,
                  final List<Accumulator> accumulators,
                  final Accumulator base) {
    //Assertions.assertTrue(base.isExact());
    final double[] x0 = (double[]) g.next();
    final double[] x1 = (double[]) g.next();
    final double truth =
      base.clear().addL2Distance(x0,x1).doubleValue();
    Assertions.assertTrue(0.0<=truth,
      "\n" + Classes.className(base) + "\n");
    for (final Accumulator a : accumulators) {
      final double pred =
        a.clear().addL2Distance(x0,x1).doubleValue();
      Assertions.assertTrue(0.0<=pred,
        "\n" + Classes.className(a) + "\n");
      Assertions.assertEquals(truth,pred,
        () ->
      "\n" + Double.toHexString(truth)
      + "\n" +  Double.toHexString(pred) + "\n"); } } 

  public static final void
  l2DistanceTests (final List<Generator> generators,
                   final List<Accumulator> accumulators,
                   final Accumulator base) {
    l2DistanceTest(accumulators,base);
    for (final Generator g : generators) {
      l2DistanceTest(g,accumulators,base); } }

  //--------------------------------------------------------------

  private static final void
  l1DistanceTest (final Generator g,
                  final List<Accumulator> accumulators,
                  final Accumulator base) {
    //Assertions.assertTrue(base.isExact());
    final double[] x0 = (double[]) g.next();
    final double[] x1 = (double[]) g.next();
    final double truth = base.clear().addL1Distance(x0,x1).doubleValue();
    //Debug.println(g.name());
    for (final Accumulator a : accumulators) {
      //final long t0 = System.nanoTime();
      final double pred = a.clear().addL1Distance(x0,x1).doubleValue();
      //final long t1 = (System.nanoTime()-t0);
      Assertions.assertEquals(truth,pred,Classes.className(a)); }
    //final double l1d = Math.abs(truth - pred);
    //final double l1n = Math.max(1.0,Math.abs(truth));
    //Debug.println(
    //  String.format("%32s %8.2fms ",
    //    Classes.className(a),Double.valueOf(t1*1.0e-6))
    //  + toString(l1d)
    //  + " / " + toString(l1n) + " = "
    //  + String.format("%8.2e",Double.valueOf(l1d/l1n)));
  }

  public static final void
  l1DistanceTests (final List<Generator> generators,
                   final List<Accumulator> accumulators,
                   final Accumulator base) {

    for (final Generator g : generators) {
      l1DistanceTest(g,accumulators,base); } }

  //--------------------------------------------------------------

  private static final void dotTest (final Generator g,
                                     final List<Accumulator> accumulators,
                                     final Accumulator base) {
    //Assertions.assertTrue(base.isExact());
    final double[] x0 = (double[]) g.next();
    final double[] x1 = (double[]) g.next();
    final double truth = base.clear().addProducts(x0,x1).doubleValue();
    //Debug.println(g.name());
    for (final Accumulator a : accumulators) {
      //final long t0 = System.nanoTime();
      final double pred =
        a.clear().addProducts(x0,x1).doubleValue();
      //final long t1 = (System.nanoTime()-t0);
      Assertions.assertEquals(truth,pred,
        "\ntrue=" + Double.toHexString(truth)
        + "\npred" + Double.toHexString(pred)); 
      //final double l1d = Math.abs(truth - pred);
      //final double l1n = Math.max(1.0,Math.abs(truth));
      //Debug.println(
      //  String.format("%32s %8.2fms ",
      //    Classes.className(a),Double.valueOf(t1*1.0e-6))
      //  + toString(l1d)
      //  + " / " + toString(l1n) + " = "
      //  + String.format("%8.2e",Double.valueOf(l1d/l1n)));
    } }

  public static final void dotTests (final List<Generator> generators,
                                     final List<Accumulator> accumulators,
                                     final Accumulator base) {

    for (final Generator g : generators) {
      dotTest(g,accumulators,base); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
