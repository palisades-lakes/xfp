package xfp.java.test;

import static java.lang.Double.toHexString;

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
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;
import xfp.java.prng.PRNG;

/** Test utilities
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-30
 */
@SuppressWarnings("unchecked")
public final class Common {

  private static final int TRYS = 1 * 256;

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
    Float.toHexString(x0)
    + "\n->" + f.toString()
    + "\n->" + Float.toHexString(x1)); }

  //--------------------------------------------------------------

  public static final void 
  floatRoundingTest (final FloatFunction<Comparable> fromFloat,
                     final ToFloatFunction toFloat,
                     final BinaryOperator<Comparable> dist,
                     final Function<Comparable,String> toString,
                     final Comparable f) {

    Debug.println("f=" + toString.apply(f));

    final float x = toFloat.applyAsFloat(f);
    Debug.println("x=" + Float.toHexString(x));

    // only check finite numbers for now
    if (Float.isFinite(x)) {
      final Comparable fx = fromFloat.apply(x);
      Debug.println("fx=" + toString.apply(fx));
      final int r = f.compareTo(fx);

      final float x1o = Math.nextDown(x);
      final float xhi = Math.nextUp(x);
      Debug.println("xlo=" + Float.toHexString(x1o));
      Debug.println("xhi=" + Float.toHexString(xhi));

      final Comparable flo = fromFloat.apply(x1o);
      final Comparable fhi = fromFloat.apply(xhi);
      Debug.println("flo=" + toString.apply(flo));
      Debug.println("fhi=" + toString.apply(fhi));

      Debug.println("r=" + r);
      //if (r < 0) { // f < fx
      Assertions.assertTrue(flo.compareTo(f) < 0); 
      //}
      //if (r > 0) { // f > fx
      Assertions.assertTrue(f.compareTo(fhi) < 0); 
      // } 

      final Comparable dlo = dist.apply(f,flo);
      final Comparable dx = dist.apply(f,fx);
      final Comparable dhi = dist.apply(f,fhi);
      Debug.println("|f-flo|= " + toString.apply(dlo));
      Debug.println("|f-fx |= " + toString.apply(dx));
      Debug.println("|f-fhi|= " + toString.apply(dhi));
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

    // Debug.DEBUG = true;

    floatRoundingTest(fromFloat,toFloat,dist,string,
      fromBigIntegers.apply(
        BigInteger.valueOf(-0x331c0c32d0072fL),
        BigInteger.valueOf(0x1000000L))); 

    floatRoundingTest(fromFloat,toFloat,dist,string,
      fromBigIntegers.apply(
        BigInteger.valueOf(0x331c0c32d0072fL),
        BigInteger.valueOf(0x1000000L))); 

    //Debug.DEBUG = false; 

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
    //Debug.DEBUG = true;
    for (int i=0;i<TRYS;i++) {
      // some longs will not be exactly representable as floats
      final BigInteger n = (BigInteger) gn.next();
      final BigInteger d = (BigInteger) gd.next();
      floatRoundingTest(fromFloat,toFloat,dist,string,
        fromBigIntegers.apply(n,d)); } 
    //Debug.DEBUG = false;
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
    Double.toHexString(x0)
    + "\n->" + f.toString()
    + "\n->" + Double.toHexString(x1)); }

  //--------------------------------------------------------------

  public static final void 
  doubleRoundingTest (final DoubleFunction<Comparable> fromDouble,
                      final ToDoubleFunction toDouble,
                      final BinaryOperator<Comparable> dist,
                      final Function<Comparable,String> toString,
                      final Comparable f) {

    Debug.println("f=" + toString.apply(f));

    final double x = toDouble.applyAsDouble(f);
    Debug.println("x=" + Double.toHexString(x));

    // only check finite numbers for now
    if (Double.isFinite(x)) {
      final Comparable fx = fromDouble.apply(x);

      final double x1o = Math.nextDown(x);
      final double xhi = Math.nextUp(x);
      Debug.println("xlo=" + Double.toHexString(x1o));
      Debug.println("x  =" + Double.toHexString(x));
      Debug.println("xhi=" + Double.toHexString(xhi));

      final Comparable flo = fromDouble.apply(x1o);
      final Comparable fhi = fromDouble.apply(xhi);
      Debug.println("flo=" + toString.apply(flo));
      Debug.println("f  =" + toString.apply(f));
      Debug.println("fx =" + toString.apply(fx));
      Debug.println("fhi=" + toString.apply(fhi));

      final int r = f.compareTo(fx);
      Debug.println("r=" + r);

      final Comparable dlo = dist.apply(f,flo);
      final Comparable dx = dist.apply(f,fx);
      final Comparable dhi = dist.apply(f,fhi);
      Debug.println("|f-flo|= " + toString.apply(dlo));
      Debug.println("|f-fx |= " + toString.apply(dx));
      Debug.println("|f-fhi|= " + toString.apply(dhi));
      Assertions.assertTrue(flo.compareTo(f) < 0, 
        "f=" + f + " > flo=" + flo);
      Assertions.assertTrue(f.compareTo(fhi) < 0); 
      Assertions.assertTrue(flo.compareTo(fx) <= 0); 
      Assertions.assertTrue(fx.compareTo(fhi) <= 0); 
      Assertions.assertTrue(dx.compareTo(dlo) <= 0,
        "dx=" + dx + " > dlo=" + dlo);
      Assertions.assertTrue(dx.compareTo(dhi) <= 0);
      if (dx.equals(dlo) || dx.equals(dhi)) {
        Assertions.assertTrue(Doubles.isEven(x),
          () -> 
        "not even!"
        + "\nxlo=" + Double.toHexString(x1o)
        + "\nx  =" + Double.toHexString(x)
        + "\nxhi=" + Double.toHexString(xhi)
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

    // Debug.DEBUG = true;

    doubleRoundingTest(fromDouble,toDouble,dist,string,
      fromBigIntegers.apply(
        BigInteger.valueOf(-0x331c0c32d0072fL),
        BigInteger.valueOf(0x1000000L))); 

    doubleRoundingTest(fromDouble,toDouble,dist,string,
      fromBigIntegers.apply(
        BigInteger.valueOf(0x331c0c32d0072fL),
        BigInteger.valueOf(0x1000000L))); 

    //Debug.DEBUG = false; 

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
    return k == 2*(k/2); }

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

  public static final List<String> accumulators () { 
    return Arrays.asList(
      new String[] 
        {
         "xfp.java.accumulators.DoubleAccumulator",
         //"xfp.java.accumulators.DistilledAccumulator",
         "xfp.java.accumulators.ZhuHayesAccumulator",
         "xfp.java.accumulators.BigFloatAccumulator",
         "xfp.java.accumulators.RationalFloatAccumulator",
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
      throw new RuntimeException(e); } }

  //--------------------------------------------------------------

  public static final List<Accumulator> 
  makeAccumulators (final List<String> classNames) {
    return 
      classNames
      .stream()
      .map(Common::makeAccumulator)
      .collect(Collectors.toUnmodifiableList()); }

  //--------------------------------------------------------------

  public static final void 
  overflowTest (final Accumulator a) {

    final double s0 = 
      a.clear()
      .addAll(
        new double[] 
          { Double.MAX_VALUE, 
            Double.MAX_VALUE, 
            1.0,
            -Double.MAX_VALUE,
            -Double.MAX_VALUE})
      .doubleValue();
    if (a.noOverflow()) {
      Assertions.assertEquals(1.0,s0,Classes.className(a)); } 

    final double s1 = 
      a.clear()
      .addAll(new double[]
        { -Double.MAX_VALUE, 
          -Double.MAX_VALUE, 
          -1.0, 
          Double.MAX_VALUE, 
          Double.MAX_VALUE})
      .doubleValue();
    if (a.noOverflow()) {
      Assertions.assertEquals(-1.0,s1,Classes.className(a));  } }

  public static final void 
  overflowTests (final List<Accumulator> accumulators) {
    for (final Accumulator a : accumulators) {
      overflowTest(a); } }

  //--------------------------------------------------------------

  public static final void 
  nonFiniteTest (final Accumulator a) {

    Assertions.assertThrows(
      AssertionError.class, 
      () -> {
        final double s0 = 
          a.clear()
          .addAll(new double[] {-1.0, Double.POSITIVE_INFINITY, })
          .doubleValue();
        Assertions.assertEquals(Double.POSITIVE_INFINITY,s0,
          Classes.className(a)); },
      Classes.className(a)); 

    Assertions.assertThrows(
      AssertionError.class, 
      () -> {
        final double s2 = 
          a.clear()
          .addAll(new double[] {-1.0, Double.NaN, })
          .doubleValue();
        Assertions.assertEquals(
          Double.NaN,s2,Classes.className(a));},
      Classes.className(a)); 
  }

  public static final void 
  nonFiniteTests (final List<Accumulator> accumulators) {
    for (final Accumulator a : accumulators) {
      nonFiniteTest(a); } }

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
    Debug.println(g.name());
    for (final Accumulator a : accumulators) {
      final long t0 = System.nanoTime();
      final double pred = a.clear().addAll(x).doubleValue(); 
      final long t1 = (System.nanoTime()-t0);
      if (a.isExact()) { 
        Assertions.assertEquals(0.0,pred,
          "sum not zero: " + Classes.className(a) 
          + " = " + Double.toHexString(pred) + "\n");  }
      final double l1d = Math.abs(pred);
      Debug.println(
        String.format("%32s %8.2fms ",Classes.className(a),
          Double.valueOf(t1*1.0e-6)) 
        + toHexString(l1d) + " = " 
        + String.format("%8.2e",Double.valueOf(l1d))); } } 

  /** Assumes the generators create arrays whose exact sum is 0.0
   */

  public static final void
  zeroSumTests (final List<Generator> generators,
                final List<Accumulator> accumulators) {
    for (final Generator g : generators) { 
      Common.zeroSumTest(g,accumulators); } }

  //--------------------------------------------------------------

  private static final void 
  sumTest (final Generator g,
           final List<Accumulator> accumulators,
           final Accumulator exact) {
    Assertions.assertTrue(exact.isExact());
    final double[] x = (double[]) g.next();
    final Accumulator efinal = exact.clear().addAll(x);
    Debug.println(Classes.className(exact));
    Debug.println(exact.toString());
    final double truth = efinal.doubleValue();
    Debug.println(g.name());
    for (final Accumulator a : accumulators) {
      final long t0 = System.nanoTime();
      final Accumulator pfinal = a.clear().addAll(x);
      Debug.println(Classes.className(a));
      Debug.println(pfinal.value().toString());
      final double pred = pfinal.doubleValue(); 
      final long t1 = (System.nanoTime()-t0);
      if (a.isExact()) { 
        Assertions.assertEquals(truth,pred,
          "\nexact: " + Classes.className(exact) 
          + " = " + Double.toHexString(truth)
          + "\n= " + exact.value()
          + "\npred: " + Classes.className(a) 
          + " = " + Double.toHexString(pred) 
          + "\n= " + a.value()
          + "\n"); }
      final double l1d = Math.abs(truth-pred);
      final double l1n = Math.max(1.0,Math.abs(truth));
      Debug.println(
        String.format("%32s %8.2fms ",Classes.className(a),
          Double.valueOf(t1*1.0e-6)) 
        + toHexString(l1d) 
        + " / " + toHexString(l1n) + " = " 
        + String.format("%8.2e",Double.valueOf(l1d/l1n))); } }

  public static final void
  sumTests (final List<Generator> generators,
            final List<Accumulator> accumulators,
            final Accumulator exact) {
    for (final Generator g : generators) { 
      Common.sumTest(g,accumulators,exact); } }

  //--------------------------------------------------------------

  private static final void l2Test (final Generator g,
                                    final List<Accumulator> accumulators,
                                    final Accumulator exact) {
    Assertions.assertTrue(exact.isExact());
    final double[] x = (double[]) g.next();
    final double truth = exact.clear().add2All(x).doubleValue(); 
    Debug.println(g.name());
    for (final Accumulator a : accumulators) {
      final long t0 = System.nanoTime();
      final double pred = 
        a.clear().add2All(x).doubleValue(); 
      final long t1 = (System.nanoTime()-t0);
      if (a.isExact()) { Assertions.assertEquals(truth,pred); }
      final double l1d = Math.abs(truth - pred);
      final double l1n = Math.max(1.0,Math.abs(truth));
      Debug.println(
        String.format("%32s %8.2fms ",Classes.className(a),Double.valueOf(t1*1.0e-6)) 
        + toHexString(l1d) 
        + " / " + toHexString(l1n) + " = " 
        + String.format("%8.2e",Double.valueOf(l1d/l1n))); } }

  public static final void l2Tests (final List<Generator> generators,
                                    final List<Accumulator> accumulators,
                                    final Accumulator exact) {

    for (final Generator g : generators) { 
      l2Test(g,accumulators,exact); } }

  //--------------------------------------------------------------

  private static final void dotTest (final Generator g,
                                     final List<Accumulator> accumulators,
                                     final Accumulator exact) {
    Assertions.assertTrue(exact.isExact());
    final double[] x0 = (double[]) g.next();
    final double[] x1 = (double[]) g.next();
    final double truth = exact.clear().addProducts(x0,x1).doubleValue(); 
    Debug.println(g.name());
    for (final Accumulator a : accumulators) {
      final long t0 = System.nanoTime();
      final double pred = 
        a.clear().addProducts(x0,x1).doubleValue(); 
      final long t1 = (System.nanoTime()-t0);
      if (a.isExact()) { Assertions.assertEquals(truth,pred); }
      final double l1d = Math.abs(truth - pred);
      final double l1n = Math.max(1.0,Math.abs(truth));
      Debug.println(
        String.format("%32s %8.2fms ",Classes.className(a),Double.valueOf(t1*1.0e-6)) 
        + toHexString(l1d) 
        + " / " + toHexString(l1n) + " = " 
        + String.format("%8.2e",Double.valueOf(l1d/l1n))); } }

  public static final void dotTests (final List<Generator> generators,
                                     final List<Accumulator> accumulators,
                                     final Accumulator exact) {

    for (final Generator g : generators) { 
      dotTest(g,accumulators,exact); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
