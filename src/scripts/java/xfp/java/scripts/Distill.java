package xfp.java.scripts;

import java.util.Arrays;
import java.util.Map;
import java.util.function.IntFunction;

import org.apache.commons.rng.UniformRandomProvider;

import xfp.java.accumulators.Accumulator;
import xfp.java.accumulators.DistilledAccumulator;
import xfp.java.accumulators.EFloatAccumulator;
import xfp.java.numbers.Doubles;
import xfp.java.prng.Generator;
import xfp.java.prng.PRNG;
import xfp.java.test.Common;

/** Distillation experiments.
 *
 * <pre>
 * j --source 11 -ea src/scripts/java/xfp/java/scripts/Distill.java > distilled.txt
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-23
 */
@SuppressWarnings("unchecked")
public final class Distill {

  //--------------------------------------------------------------
  // experiment script
  //--------------------------------------------------------------

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
        //Debug.println("emax=" + emax);
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

  //  private static final double uRatio (final double[] x) {
  //    double umin = Double.POSITIVE_INFINITY;
  //    double umax = 0.0;
  //    for (final double xi : x) {
  //      final double u = Math.ulp(xi);
  //      if (u < umin) { umin = u; }
  //      if (umax < u) { umax = u; } }
  //    return umax / umin; }
  //
  //  private static final int eRange (final double[] x) {
  //    int emin = Integer.MAX_VALUE;
  //    int emax = Integer.MIN_VALUE;
  //    for (final double xi : x) {
  //      final int e = Doubles.exponent(xi);
  //      if (e < emin) { emin = e; }
  //      if (emax < e) { emax = e; } }
  //    return emax - emin; }
  //
  //  private static final int used (final double[] x) {
  //    for (int i=x.length;i>0;i--) {
  //      if (0 != x[i-1]) { return i; } }
  //    return 0; }

  //--------------------------------------------------------------

  private static final double[] partials (final Accumulator a,
                                          final double[] x) {
    final int n = x.length;
    final double[] sums = new double[n];
    a.clear();
    for (int i=0;i<n;i++) {
      sums[i]= a.add(x[i]).doubleValue(); }
    return sums; }

  //--------------------------------------------------------------

  public static final void main (final String[] args) {
    //Debug.DEBUG=false;
    final Accumulator bfa = EFloatAccumulator.make();
    final Accumulator da = DistilledAccumulator.make();
    final int dim = (32 * 1024) - 1;
    //Debug.println("dim=" + dim);
    //Debug.println("bound=" + (64*dim));
    Arrays.sort(generators);
    for (final String k : generators) {
      final Generator g = factories.get(k).apply(dim);
      //Debug.println();
      //Debug.println("generator= " + k);
      final double[] x = (double[]) g.next();
      //Debug.println("used= " + used(x));
      //Debug.println("condition= " + Dn.conditionSum(x));
      //Debug.println("uRatio= " + uRatio(x));
      //Debug.println("eRange= " + eRange(x));
      final double[] bfp = partials(bfa,x);
      final double[] dp = partials(da,x);
      assert Arrays.equals(bfp,dp); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
