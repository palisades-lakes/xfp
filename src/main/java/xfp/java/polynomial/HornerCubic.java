package xfp.java.polynomial;

/** Horner's algorithm over <code>double</code>.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-01
 */

public final class HornerCubic
implements Cubic<HornerCubic> {

  //--------------------------------------------------------------

  @Override
  public final boolean isExact () { return false; }

  // TODO: test for overflow and throw an exception?
  @Override
  public final boolean noOverflow () { return false; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private HornerCubic () { super(); }

  public static final HornerCubic make () {
    return new HornerCubic(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
