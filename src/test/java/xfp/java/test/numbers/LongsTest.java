package xfp.java.test.numbers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import xfp.java.prng.Generator;
import xfp.java.prng.Generators;
import xfp.java.prng.PRNG;

//----------------------------------------------------------------
/** Test desired properties of doubles.
 * <p>
 * <pre>
 * mvn -Dtest=xfp/java/test/numbers/LongsTest test > LongsTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-05
 */

public final class LongsTest {


  private static final int TRYS = 16 * 1024;

  //--------------------------------------------------------------
  /** Will fail randomly roughly 1/2^n trials. */

  private static final boolean hasNegativeSample (final Generator g,
                                                  final int n) {
    for (int i=0;i<n;i++) {
      if (g.nextLong() < 0L) { return true; } }
    return false; }

  @SuppressWarnings({ "static-method" })
  @Test
  public final void negativeSamplesTest () {
    final Generator g = Generators.longGenerator(
      PRNG.well44497b("seeds/Well44497b-2019-01-05.txt"));
    assertTrue(hasNegativeSample(g,TRYS)); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
