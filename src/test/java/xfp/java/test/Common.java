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
 * @version 2019-04-01
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
  public static final int feMax (final int dim) { 
    final int d = Float.MAX_EXPONENT - ceilLog2(dim);
    return d; }

  /** Maximum exponent for double generation such that a double 
   * sum of <code>dim</code> <code>double</code>s will be finite
   * (with high enough probability).
   */
  public static final int deMax (final int dim) { 
    final int d = Double.MAX_EXPONENT - ceilLog2(dim);
    return d; }

  //--------------------------------------------------------------
  /** Generate <code>doubl[dim]</code> such that the sum of the
   * squares (and the sum of the elements) is very likely to be 
   * finite.
   */

  public static final List<Generator> generators (final int dim) {
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
    final int emax = feMax(dim)/2;
    final double dmax = (1<<emax);
    final List<Generator> gs0 =
      List.of(
        Doubles.finiteGenerator(dim,urp0,emax),
        Doubles.gaussianGenerator(dim,urp1,0.0,dmax),
        Doubles.exponentialGenerator(dim,urp2,0.0,dmax),
        Doubles.laplaceGenerator(dim,urp2,0.0,dmax),
        Doubles.uniformGenerator(dim,urp3,-dmax,dmax));
    final List<Generator> gs1 =
      gs0.stream().map(Doubles::zeroSumGenerator)
      .collect(Collectors.toUnmodifiableList());
    final List<Generator> gs2 =
      gs1.stream().map((g) -> Doubles.shuffledGenerator(g,urp4))
      .collect(Collectors.toUnmodifiableList());
    return
      Streams.concat(
        gs0.stream(),
        gs1.stream(), 
        gs2.stream())
      .collect(Collectors.toUnmodifiableList()); }

  //--------------------------------------------------------------

  public static final List<String> accumulators () { 
    return List.of(
      //"xfp.java.accumulators.DoubleAccumulator",
      "xfp.java.accumulators.DoubleFmaAccumulator",
      //"xfp.java.accumulators.FloatAccumulator",
      //"xfp.java.accumulators.FloatFmaAccumulator",
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
      if (a.isExact()) { Assertions.assertEquals(truth,pred); }
      final double l1d = Math.abs(truth-pred);
      final double l1n = Math.max(1.0,Math.abs(truth));
      Debug.println(
        String.format("%32s %8.2fms ",Classes.className(a),Double.valueOf(t1*1.0e-6)) 
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
