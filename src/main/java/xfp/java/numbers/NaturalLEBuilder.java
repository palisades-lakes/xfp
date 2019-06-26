package xfp.java.numbers;

/** Little endian natural number builder.
 * <p>
 * Don't implement Comparable, because of mutability!
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-06-26
 */

public final class NaturalLEBuilder 
extends NaturalLEBase
implements NaturalBuilder<NaturalLE> {

  private static final long serialVersionUID = 1L;

  //--------------------------------------------------------------
  // state 
  //--------------------------------------------------------------

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private NaturalLEBuilder (final int[] words,
                            final int i0,
                            final int i1,
                            final int startWord) {
    super(words,i0,i1,startWord); }

  private NaturalLEBuilder () { super(new int[0],-1,0,-1); }

  //--------------------------------------------------------------

  public static final NaturalLEBuilder make () {
    return new NaturalLEBuilder(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

