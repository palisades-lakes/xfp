package xfp.java.scripts;

import java.util.Map;
import java.util.function.IntFunction;

import org.apache.commons.rng.UniformRandomProvider;

import xfp.java.accumulators.Accumulator;
import xfp.java.accumulators.BigFloatAccumulator;
import xfp.java.accumulators.RationalFloatAccumulator;
import xfp.java.accumulators.ZhuHayesAccumulator;
import xfp.java.linear.Dn;
import xfp.java.numbers.BigFloat;
import xfp.java.numbers.Doubles;
import xfp.java.prng.Generator;
import xfp.java.prng.PRNG;
import xfp.java.test.Common;

/** Benchmark accumulators tests.
 * 
 * <pre>
 * j --source 11 -ea src/scripts/java/xfp/java/scripts/Distill.java > distilled.txt
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-29
 */
@SuppressWarnings("unchecked")
public final class Distill0 {

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

  private static final String[] generators = 
    factories.keySet().toArray(new String[0]);

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

  private static final boolean twoSum (final double[] x,
                                       final int i) {
    // might get +/- Infinity due to overflow
    final double x0 = x[i-1];
    final double x1 = x[i];
    final double s = x0 + x1;
    final double z = s - x0;
    final double e = (x0 - (s - z)) + (x1 - z); 
    x[i-1] = s;
    x[i] = e; 
    return (x0 != x[i-1]) || (x1 != x[i]); }

  private static final boolean distill (final double[] x) {
    boolean changed = false;
    for (int i=x.length-1;i>0;i--) { 
      changed = changed || twoSum(x,i); } 
    return changed; }

//  private static final boolean notDistilled (final double[] x) {
//    for (int i=2;i<x.length;i++) { 
//      if (0.0 != x[i]) { 
//        //System.out.println(i + " : " + Double.toHexString(x[i]));
//        //System.out.println(Dn.toHexString(x));
//        return true; } }
//    return false; }

  //--------------------------------------------------------------

  public static final void main (final String[] args) {
    final Accumulator a = BigFloatAccumulator.make();
    final Accumulator r = RationalFloatAccumulator.make();
    final Accumulator z = ZhuHayesAccumulator.make();
    final int dim = 1023;
    for (final String k : generators) {
      final Generator g = factories.get(k).apply(dim);
      System.out.println();
      System.out.println("generator= " + k);
      final double[] x = (double[]) g.next();
      System.out.println("condition= " + Dn.conditionSum(x));
      final double ztruth = z.clear().addAll(x).doubleValue();
      final double rtruth = r.clear().addAll(x).doubleValue();
      a.clear().addAll(x);
      final BigFloat v = (BigFloat) a.value();
      final double vtruth = v.doubleValue();
      final double truth = a.doubleValue();
      assert ztruth == truth : "z != a"
        + "\nz= " + Double.toHexString(ztruth) 
        + "\nr= " + Double.toHexString(rtruth) 
        + "\nr= " + r.value() 
        + "\na= " + Double.toHexString(truth)
        + "\nv= " + Double.toHexString(vtruth)
        + "\nv= " + v;
      System.out.println("sum= " + Double.toHexString(truth));
      //System.out.println(Dn.toHexString(x));
      int j = 0;
      //while (truth != x[0]) { j++; distill(x); }
      //System.out.println("condition= " + Dn.conditionSum(x));
      System.out.println("bound=" + dim*dim);
      while ((j < dim*dim) && distill(x)) { j++; }
      System.out.println("distillations: " + (j+1));
      assert truth == a.clear().addAll(x).doubleValue();
      assert truth == x[0] : 
        "\n" + Dn.toHexString(x)
        + "\n" + a.value();
      //System.out.println(Dn.toHexString(x)); 
    } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
