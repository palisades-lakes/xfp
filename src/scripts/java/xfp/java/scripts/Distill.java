package xfp.java.scripts;

import java.util.Map;
import java.util.function.IntFunction;

import org.apache.commons.rng.UniformRandomProvider;

import xfp.java.accumulators.Accumulator;
import xfp.java.accumulators.BigFloatAccumulator;
import xfp.java.accumulators.ZhuHayesAccumulator;
import xfp.java.linear.Dn;
import xfp.java.numbers.BigFloat;
import xfp.java.numbers.Doubles;
import xfp.java.numbers.Numbers;
import xfp.java.prng.Generator;
import xfp.java.prng.PRNG;
import xfp.java.test.Common;

/** Benchmark accumulators tests.
 * 
 * <pre>
 * j --source 11 -ea src/scripts/java/xfp/java/scripts/Distill.java
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-27
 */
@SuppressWarnings("unchecked")
public final class Distill {

  private static final String SEED0 = 
    "seeds/Well44497b-2019-01-05.txt";
  //  "seeds/Well44497b-2019-01-07.txt";
  //  "seeds/Well44497b-2019-01-09.txt";

  private static final 
  Map<String,IntFunction<Generator>> 
  factories = 
  Map.of(
    "uniform",
    new IntFunction<Generator>() {
      @Override
      public final Generator apply (final int dim) {
        final UniformRandomProvider urp0 = PRNG.well44497b(SEED0);
        final int emax = Common.deMax(dim)/2;
        final double dmax = (1<<emax);
        return Doubles.uniformGenerator(dim,urp0,-dmax,dmax); } 
    },
    "finite",
    new IntFunction<Generator>() {
      @Override
      public final Generator apply (final int dim) {
        final UniformRandomProvider urp0 = PRNG.well44497b(SEED0);
        final int emax = Common.deMax(dim)/2;
        System.out.println("emax=" + emax);
        return Doubles.finiteGenerator(dim,urp0,emax); } 
    },
    "exponential",
    new IntFunction<Generator>() {
      @Override
      public final Generator apply (final int dim) {
        final UniformRandomProvider urp0 = PRNG.well44497b(SEED0);
        final int emax = Common.deMax(dim)/2;
        final double dmax = (1<<emax);
        return Doubles.exponentialGenerator(dim,urp0,0.0,dmax); } 
    },
    "gaussian",
    new IntFunction<Generator>() {
      @Override
      public final Generator apply (final int dim) {
        final UniformRandomProvider urp0 = PRNG.well44497b(SEED0);
        final int emax = Common.deMax(dim)/2;
        final double dmax = (1<<emax);
        return Doubles.gaussianGenerator(dim,urp0,0.0,dmax); } 
    },
    "laplace",
    new IntFunction<Generator>() {
      @Override
      public final Generator apply (final int dim) {
        final UniformRandomProvider urp0 = PRNG.well44497b(SEED0);
        final int emax = Common.deMax(dim)/2;
        final double dmax = (1<<emax);
        return Doubles.laplaceGenerator(dim,urp0,0.0,dmax); } 
    });

  //--------------------------------------------------------------

  //  private double sumTwo = Double.NaN;
  //  private double errTwo = Double.NaN;
  //
  //  /** Update {@link #sumTwo} and {@link #errTwo} so that
  //   * <code>{@link #sumTwo} == x0 + x1</code> 
  //   * (sum rounded to nearest double), and
  //   * <code>rationalSum({@link #sumTwo},{@link #errTwo}) 
  //   * == rationalSum(x0,x1)</code> 
  //   * (exact sums, implemented, for example, with arbitrary
  //   * precision rationals)
  //   */
  //
  //  private final void twoSum (final double x0, 
  //                             final double x1) {
  //    // might get +/- Infinity due to overflow
  //    sumTwo = x0 + x1;
  //    final double z = sumTwo - x0;
  //    errTwo = (x0 - (sumTwo - z)) + (x1 - z); }

  private static final void twoSum (final double[] x,
                                    final int i) {
    // might get +/- Infinity due to overflow
    final double x0 = x[i-1];
    final double x1 = x[i];
    final double s = x0 + x1;
    final double z = s - x0;
    final double e = (x0 - (s - z)) + (x1 - z); 
    x[i-1] = s;
    x[i] = e; }

  private static final void distill (final double[] x) {
    for (int i=x.length-1;i>0;i--) { twoSum(x,i); } }

  private static final boolean notDistilled (final double[] x) {
    for (int i=2;i<x.length;i++) { 
      if (0.0 != x[i]) { 
        //System.out.println(i + " : " + Double.toHexString(x[i]));
        //System.out.println(Dn.toHexString(x));
        return true; } }
    return false; }

  //--------------------------------------------------------------

  public static final void main (final String[] args) {
    final Accumulator a = BigFloatAccumulator.make();
    final Accumulator z = ZhuHayesAccumulator.make();
    final int dim = 5;
    final Generator g = factories.get("exponential").apply(dim);
    for (int k=0; k<16;k++) {
      System.out.println();
      System.out.println("k= " + k);
      final double[] x = (double[]) g.next();
      System.out.println("condition= " + Dn.conditionSum(x));
      final double ztruth = z.clear().addAll(x).doubleValue();
      a.clear().addAll(x);
      final BigFloat v = (BigFloat) a.value();
      final double vtruth = v.doubleValue();
      final double truth = a.doubleValue();
      assert ztruth == truth : "\n"
        + Double.toHexString(ztruth) 
        + "\n"
        + Double.toHexString(truth)
        + "\n"
        + Double.toHexString(vtruth)
        + "\n"
        + v
      + "\n"
      + Numbers.loBit(v.significand());
      System.out.println("sum= " + Double.toHexString(truth));
      //System.out.println(Dn.toHexString(x));
      int j = 0;
      //while (truth != x[0]) { j++; distill(x); }
      //System.out.println("condition= " + Dn.conditionSum(x));
      while (notDistilled(x)) { j++; distill(x); }
      System.out.println("distillations: " + j);
      assert truth == a.clear().addAll(x).doubleValue();
      assert truth == x[0] : 
        "\n" + Dn.toHexString(x)
        + "\n" + a.value();
      //System.out.println(Dn.toHexString(x)); 
    } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
