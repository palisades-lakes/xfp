package xfp.java.test;

import static java.lang.Double.toHexString;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;

import com.google.common.collect.Streams;

import xfp.java.Classes;
import xfp.java.Debug;
import xfp.java.accumulators.Accumulator;
import xfp.java.numbers.Doubles;
import xfp.java.prng.Generator;
import xfp.java.prng.PRNG;

/** Teat utilities
 * 
 * <pre>
 * java -ea -jar target\benchmarks.jar Dot
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-08
 */
@SuppressWarnings("unchecked")
public final class Common {

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
    return List.of(
      Doubles.gaussianGenerator(dim,urp1,0.0,dmax),
      Doubles.exponentialGenerator(dim,urp2,0.0,dmax),
      Doubles.laplaceGenerator(dim,urp3,0.0,dmax),
      Doubles.uniformGenerator(dim,urp4,-dmax,dmax),
      Doubles.finiteGenerator(dim,urp0,emax)); }

  private static final List<Generator> zeroSumGenerators (final List<Generator> gs0) {
    final List<Generator> gs1 =
      gs0.stream().map(Doubles::zeroSumGenerator)
      .collect(Collectors.toUnmodifiableList());
    final UniformRandomProvider urp = 
      PRNG.well44497b("seeds/Well44497b-2019-04-09.txt");
    final List<Generator> gs2 =
      gs1.stream().map((g) -> Doubles.shuffledGenerator(g,urp))
      .collect(Collectors.toUnmodifiableList());
    return
      Streams
      .concat(gs1.stream(),gs2.stream())
      .collect(Collectors.toUnmodifiableList()); }

  //--------------------------------------------------------------
  /** Generate <code>doubl[dim]</code> such that the sum of the
   * squares (and the sum of the elements) is very likely to be 
   * finite.
   */

  public static final List<Generator> zeroSumGenerators (final int dim) {
    return zeroSumGenerators(baseGenerators(dim)); }

  //--------------------------------------------------------------
  /** Generate <code>doubl[dim]</code> such that the sum of the
   * squares (and the sum of the elements) is very likely to be 
   * finite.
   */

  public static final List<Generator> generators (final int dim) {
    final List<Generator> gs0 = baseGenerators(dim);
    final List<Generator> gs1 = zeroSumGenerators(gs0);
    return
      Streams
      .concat(gs0.stream(),gs1.stream())
      .collect(Collectors.toUnmodifiableList()); }

  //--------------------------------------------------------------

  public static final List<String> accumulators () { 
    return List.of(
      "xfp.java.accumulators.DoubleAccumulator",
      //"xfp.java.accumulators.DoubleFmaAccumulator",
      //"xfp.java.accumulators.RationalAccumulator",
      "xfp.java.accumulators.RBFAccumulator"); }

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
    final double truth = exact.clear().addAll(x).doubleValue();
    Debug.println(g.name());
    for (final Accumulator a : accumulators) {
      final long t0 = System.nanoTime();
      final double pred = a.clear().addAll(x).doubleValue(); 
      final long t1 = (System.nanoTime()-t0);
      if (a.isExact()) { 
        Assertions.assertEquals(truth,pred,
          "\nexact: " + Classes.className(exact) 
          + " = " + Double.toHexString(truth)
          + "\npred: " + Classes.className(a) 
          + " = " + Double.toHexString(pred) + "\n"); }
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
