package xfp.java.test;

import static java.lang.Double.toHexString;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import xfp.java.Classes;
import xfp.java.accumulators.Accumulator;
import xfp.java.prng.Generator;

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
    //System.out.println("emax=" + d);
    return d; }

  /** Maximum exponent for double generation such that a double 
   * sum of <code>dim</code> <code>double</code>s will be finite
   * (with high enough probability).
   */
  public static final int deMax (final int dim) { 
    final int d = Double.MAX_EXPONENT - ceilLog2(dim);
    return d; }

  //--------------------------------------------------------------
  /** Maximum value for double generation such that a float sum 
   * of <code>dim</code> <code>double</code>s will be finite
   * (with high enough probability).
   */
  public static final double fMax (final int dim) { 
    return 1024*(1 << feMax(dim)); }

  /** Maximum value for double generation such that a double sum 
   * of <code>dim</code> <code>double</code>s will be finite
   * (with high enough probability).
   */
  public static final double dMax (final int dim) { 
    return 1024*(1 << deMax(dim)); }

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
  makeAccumulators (final String[] classNames) {
    return 
      Arrays
      .stream(classNames)
      .map(Common::makeAccumulator)
      .collect(Collectors.toUnmodifiableList()); }

  //--------------------------------------------------------------

  private static final void sumTest (final Generator g,
                                     final List<Accumulator> accumulators,
                                     final Accumulator exact) {

    final double[] x = (double[]) g.next();
    final double truth = exact.clear().addAll(x).doubleValue(); 
    System.out.println(g.name());
    for (final Accumulator a : accumulators) {
      final long t0 = System.nanoTime();
      final double pred = a.clear().addAll(x).doubleValue(); 
      final long t1 = (System.nanoTime()-t0);
      final double l1d = Math.abs(truth-pred);
      final double l1n = Math.max(1.0,Math.abs(truth));
      System.out.println(
        String.format("%32s %8.2fms ",Classes.className(a),Double.valueOf(t1*1.0e-6)) 
        + toHexString(l1d) 
        + " / " + toHexString(l1n) + " = " 
        + String.format("%8.2e",Double.valueOf(l1d/l1n))); } }

  public static final void sumTests (final List<Generator> generators,
                                     final List<Accumulator> accumulators,
                                     final Accumulator exact) {

    for (final Generator g : generators) { 
      Common.sumTest(g,accumulators,exact); } }

  //--------------------------------------------------------------

  private static final void dotTest (final Generator g,
                                     final List<Accumulator> accumulators,
                                     final Accumulator exact) {

    final double[] x0 = (double[]) g.next();
    final double[] x1 = (double[]) g.next();
    final double truth = exact.clear().addProducts(x0,x1).doubleValue(); 
    System.out.println(g.name());
    for (final Accumulator a : accumulators) {
      final long t0 = System.nanoTime();
      final double pred = 
        a.clear().addProducts(x0,x1).doubleValue(); 
      final long t1 = (System.nanoTime()-t0);
      final double l1d = Math.abs(truth - pred);
      final double l1n = Math.max(1.0,Math.abs(truth));
      System.out.println(
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
