package xfp.java.prng;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;

/** Providers for pseduo-random number generators.
 *
 * Static methods only; no state.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-01-15
 */

public final class PRNG {

  //--------------------------------------------------------------
  // providers
  //--------------------------------------------------------------
  /** http://www.iro.umontreal.ca/~panneton/WELLRNG.html
   */

  public static final UniformRandomProvider 
  well44497b (final int[] seed) {
    assert 1391 == seed.length;
    return RandomSource.create(RandomSource.WELL_44497_B,seed); }

  //--------------------------------------------------------------

  public static final UniformRandomProvider 
  mersenneTwister (final int[] seed) {
    assert 624 == seed.length;
    return RandomSource.create(RandomSource.MT,seed); }

  //--------------------------------------------------------------
  // disable constructor
  //--------------------------------------------------------------

  private PRNG () {
    throw new UnsupportedOperationException(
      "can't instantiate " + getClass()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
