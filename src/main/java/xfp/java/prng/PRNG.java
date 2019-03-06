package xfp.java.prng;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;

import xfp.java.exceptions.Exceptions;

/** Providers for pseduo-random number generators.
 *
 * Static methods only; no state.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-05
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

  public static final UniformRandomProvider 
  well44497b (final String seed) {
    return well44497b(Seeds.seed(seed)); }

  public static final UniformRandomProvider 
  well44497b (final Object seed) {
    if (seed instanceof int[]) { 
      return well44497b((int[]) seed); }
    if (seed instanceof String)  { 
      return well44497b((String) seed); }
    throw Exceptions.unsupportedOperation(
      null,"well44497b",seed); }

  //--------------------------------------------------------------

  public static final UniformRandomProvider 
  mersenneTwister (final int[] seed) {
    assert 624 == seed.length;
    return RandomSource.create(RandomSource.MT,seed); }

  public static final UniformRandomProvider 
  mersenneTwister (final String resource) {
    return mersenneTwister(Seeds.seed(resource)); }

  public static final UniformRandomProvider 
  mersenneTwister (final Object seed) {
    if (seed instanceof int[]) { 
      return mersenneTwister((int[]) seed); }
    if (seed instanceof String)  { 
      return mersenneTwister((String) seed); }
    throw Exceptions.unsupportedOperation(
      null,"mersenneTwister",seed); }

  //--------------------------------------------------------------
  // disable constructor
  //--------------------------------------------------------------

  private PRNG () {
    throw new UnsupportedOperationException(
      "can't instantiate " + getClass()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
