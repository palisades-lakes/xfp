package xfp.java.scripts;

import org.apache.commons.rng.UniformRandomProvider;

import xfp.java.numbers.NaturalLE;
import xfp.java.prng.Generator;
import xfp.java.prng.PRNG;

/** Benchmark squares.
 *
 * <pre>
 * jy --source 12 src/scripts/java/xfp/java/scripts/Square.java
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-30
 */
@SuppressWarnings("unchecked")
public final class Square {

  public static final void main (final String[] args) {
    final int ntrys = (1024*1024*1024) - 1;
    final UniformRandomProvider urp =
      PRNG.well44497b("seeds/Well44497b-2019-01-09.txt");
    final Generator g = NaturalLE.generator(urp);
    for (int i=0;i<ntrys;i++) {
      final NaturalLE z = (NaturalLE) g.next();
      final double z2 = z.squareSimple().doubleValue();
      assert Double.isFinite(z2); } }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
