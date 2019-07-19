package xfp.java.numbers;

import java.util.Arrays;

/** Little endian natural number builder.
 * <p>
 * Don't implement Comparable, because of mutability!
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-05
 */

public final class NaturalLEMutable0
extends NaturalLEBase
implements Natural {

  //--------------------------------------------------------------
  // Uints
  //--------------------------------------------------------------

  @Override
  public final Natural setWord (final int i,
                                final int w) {
    assert 0<=i;
    Natural u = recyclable(this);
    u = u.setWord(i,w);
    return u.immutable(); }

   @Override
  public final Natural empty () { return new NaturalLEMutable0(); }
   
  //--------------------------------------------------------------
  // Transience
  //--------------------------------------------------------------
  
  @Override
  public boolean isImmutable () { return false; }
  
  @Override
  public final Natural immutable () { return NaturalLE.copy(this); }

  @Override
  public final Natural recyclable (final Natural init) { 
    return copy((NaturalLEBase) init); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private NaturalLEMutable0 (final int[] words,
                            final int i0,
                            final int i1,
                            final int startWord) {
    super(words,i0,i1,startWord); }

  private NaturalLEMutable0 () { super(new int[0],-1,0,-1); }

  //--------------------------------------------------------------

  public static final NaturalLEMutable0 
  copy (final NaturalLEBase u) {
    return new NaturalLEMutable0(
      Arrays.copyOf(u.words(),u.words().length),
      u.i0(),
      u.i1(),
      u.startWord()); }
  
  //--------------------------------------------------------------
}
//--------------------------------------------------------------

