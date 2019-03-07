package xfp.java.scripts;

import java.util.Arrays;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.ListSampler;

import xfp.java.linear.BigFractionsN;
import xfp.java.linear.Dn;
import xfp.java.linear.Qn;
import xfp.java.numbers.Doubles;
import xfp.java.prng.Generator;
import xfp.java.prng.Generators;
import xfp.java.prng.PRNG;

/** Benchmark double dot products.
 * 
 * <pre>
 * java -ea -jar target\benchmarks.jar Dot
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-05
 */
@SuppressWarnings("unchecked")
public final class Dot {

  private static final int DIM = 1 * 1024;
  private static final int DELTA = 2 +
    ((Doubles.MAXIMUM_BIASED_EXPONENT  
      - 30 
      + Integer.numberOfLeadingZeros(2*DIM)) / 2);

  private static final UniformRandomProvider urp = 
    PRNG.well44497b("seeds/Well44497b-2019-01-05.txt");
  private static final Generator g = 
    Generators.finiteDoubleGenerator(DIM,urp,DELTA);

  private static final double[] x00 = (double[]) g.next();
  private static final double[] x0 = Dn.concatenate(x00,Dn.get(DIM).negate(x00));
  private static final double[] x11 = (double[]) g.next();
  private static final double[] x1 = Dn.concatenate(x11,x11);

  private static final double trueDot = Qn.naiveDot(x0,x1);

  //--------------------------------------------------------------

  private static final double dnNaiveDot (final double[] x,
                                          final double[] y) { 
    return  Math.abs(trueDot - Dn.naiveDot(x,y)) 
      / (1.0 + trueDot); }

  private static final double dnFmaDot (final double[] x,
                                        final double[] y) { 
    return  Math.abs(trueDot - Dn.fmaDot(x,y)) 
      / (1.0 + trueDot); }

  private static final double bfNaiveDot (final double[] x,
                                          final double[] y) { 
    return  Math.abs(trueDot - BigFractionsN.naiveDot(x,y)) 
      / (1.0 + trueDot); }

  private static final double qnNaiveDot (final double[] x,
                                          final double[] y) { 
    return  Math.abs(trueDot - Qn.naiveDot(x,y)) 
      / (1.0 + trueDot); }

  //--------------------------------------------------------------

  private static final int TRYS = 1024 * 1024;

  public static final void main (final String[] args) 
    throws InterruptedException {
    ListSampler.shuffle(urp,Arrays.asList(x0));

    Thread.sleep(16*1024);
    final long t = System.nanoTime();
    for (int i=0;i<TRYS;i++) {
      if (2.0*Math.ulp(1.0) > qnNaiveDot(x0,x1)) {
        System.out.println("false"); } } 
    System.out.printf("total secs: %8.2f\n",
      Double.valueOf((System.nanoTime()-t)*1.0e-9)); 
    Thread.sleep(16*1024); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------