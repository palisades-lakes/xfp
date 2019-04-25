package xfp.java.scripts;

import xfp.java.accumulators.Accumulator;
import xfp.java.accumulators.BigFloatAccumulator;
import xfp.java.numbers.BigFloat;
import xfp.java.numbers.Numbers;
import xfp.java.prng.Generator;
import xfp.java.test.Common;

/** Benchmark partial sums.
 * 
 * <pre>
 * jy --source 11 src/scripts/java/xfp/java/scripts/PartialSums.java
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-24
 */
@SuppressWarnings("unchecked")
public final class PartialSums {

  //--------------------------------------------------------------

  public static final double[] partialSums (final Accumulator acc,
                                            final double[] x,
                                            final double[] s) { 
    // must be that some split is better than no split?
    final int n = x.length;
    assert 1 < n;
    acc.clear(); 
    int longOverflows = 0;
    for (int i=0;i<n;i++) { 
      final BigFloat sum = (BigFloat) acc.add(x[i]).value();
      if (Numbers.hiBit(sum.significand()) > 64) { 
        longOverflows++; }
      s[i] = sum.doubleValue(); }
    System.out.print("overflows=" + longOverflows + "/" + n
      + " = " + ((double) longOverflows)/n);
    return s; }

  public static final void main (final String[] args) {
    final int n = (32*1024*1024) - 1;
    final Accumulator a = BigFloatAccumulator.make();
    //assert a.isExact();
    final double[] s = new double[2*n];  
    final long t = System.nanoTime();
    for (final Generator g : Common.generators(n)) {
      System.out.println();
      System.out.println(g.name());
      final double[] x = (double[]) g.next();
      final double[] z = partialSums(a,x,s);
      if (0.0 != z[z.length-1]) {
        System.out.println(Double.toHexString(0.0) 
          + " != " + Double.toHexString(z[n-1])); } }
    System.out.printf("total secs: %8.2f\n",
      Double.valueOf((System.nanoTime()-t)*1.0e-9)); } 

//--------------------------------------------------------------
}
//--------------------------------------------------------------
