package xfp.java.numbers;

/** Little endian natural number builder.
 * <p>
 * Don't implement Comparable, because of mutability!
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-05
 */

public final class NaturalLEMutable
extends NaturalLEBase
implements Natural {

  private static final long serialVersionUID = 1L;

  //--------------------------------------------------------------
  // state
  //--------------------------------------------------------------

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private NaturalLEMutable (final int[] words,
                            final int i0,
                            final int i1,
                            final int startWord) {
    super(words,i0,i1,startWord); }

  private NaturalLEMutable () { super(new int[0],-1,0,-1); }

  @Override
  public final Natural empty () { 
    return new NaturalLEMutable(); }
   
  //--------------------------------------------------------------

  public static final NaturalLEMutable make () {
    return new NaturalLEMutable(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

