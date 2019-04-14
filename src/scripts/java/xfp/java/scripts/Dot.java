package xfp.java.scripts;

import static java.lang.Double.toHexString;

import java.util.Arrays;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.ListSampler;

import xfp.java.Classes;
import xfp.java.accumulators.Accumulator;
import xfp.java.accumulators.DoubleAccumulator;
import xfp.java.accumulators.RBFAccumulator;
import xfp.java.linear.Dn;
import xfp.java.numbers.Doubles;
import xfp.java.prng.Generator;
import xfp.java.prng.PRNG;
import xfp.java.test.Common;

/** Benchmark double dot products.
 * 
 * <pre>
 * java -ea -jar target\benchmarks.jar Dot
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-14
 */
@SuppressWarnings("unchecked")
public final class Dot {

  //--------------------------------------------------------------
   // TODO: more efficient via bits?
  private static final boolean isEven (final int k) {
    return k == 2*(k/2); }

  // exact sum is 0.0
  private static double[] sampleDoubles (final Generator g,
                                         final UniformRandomProvider urp) {
    double[] x = (double[]) g.next();
    x = Dn.concatenate(x,Dn.minus(x));
    ListSampler.shuffle(urp,Arrays.asList(x));
    return x; }


  private static double[][] sampleDoubles (final int dim,
                                           final int n) {
    assert isEven(dim);
    final UniformRandomProvider urp = 
      PRNG.well44497b("seeds/Well44497b-2019-01-05.txt");
    final Generator g = 
      Doubles.finiteGenerator(dim/2,urp,Common.deMax(dim));

    final double[][] x = new double[n][];
    for (int i=0;i<n;i++) { x[i] = sampleDoubles(g,urp); }
    return x; }

  private static final int DIM = 2 * 1024;
  private static final int N = 16;

  //--------------------------------------------------------------

  public static final void main (final String[] args) 
    throws InterruptedException {

    final double[][] x0 = sampleDoubles(DIM,N);
    final double[][] x1 = sampleDoubles(DIM,N);

    // should be zero with current construction
    final double[] truth = new double[N];
    final double[] pred = new double[N];
    // assuming ERational is correct!!!
    for (int i=0;i<N;i++) { 
      truth[i] = 
        RBFAccumulator
        .make()
        .addProducts(x0[i],x1[i])
        .doubleValue(); }

    for (int i=0;i<N;i++) { 
      System.out.println(
        i + " : " 
          + Double.toHexString(truth[i]) 
          + ", " 
          + Double.toHexString(Dn.maxAbs(x0[i])) 
          + ", " 
          + Double.toHexString(Dn.maxAbs(x1[i]))); }
    System.out.println();
    final Accumulator[] accumulators = 
    {
     DoubleAccumulator.make(),
     RBFAccumulator.make(),
    };

    Thread.sleep(16*1024);
    for (final Accumulator a : accumulators) {
      long t;
      t = System.nanoTime();
      for (int i=0;i<N;i++) { 
        pred[i] = 
          a.clear().addProducts(x0[i],x1[i]).doubleValue(); }
      t = (System.nanoTime()-t);
      System.out.println(toHexString(Dn.l1Dist(truth,pred)) + 
        " in " + (t*1.0e-9) 
        + " secs " + Classes.className(a)); } 

    Thread.sleep(16*1024); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
