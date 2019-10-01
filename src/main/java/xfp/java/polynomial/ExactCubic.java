package xfp.java.polynomial;

/** Base class for some exact cubic polynomial evaluators.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-01
 */
@SuppressWarnings("unchecked")
public abstract class ExactCubic<T extends ExactCubic>
implements Cubic<T> {

  //--------------------------------------------------------------

  @Override
  public final boolean isExact () { return true; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  protected ExactCubic () { super(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
