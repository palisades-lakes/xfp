package xfp.java.test.polynomial;

import org.junit.jupiter.api.Test;

import xfp.java.polynomial.MonomialBigFloat;
import xfp.java.polynomial.MonomialDouble;
import xfp.java.polynomial.MonomialDoubleBF;
import xfp.java.polynomial.MonomialDoubleRF;
import xfp.java.polynomial.MonomialRationalFloat;
import xfp.java.test.Common;

//----------------------------------------------------------------
/** Test desired properties of monimial calculators.
 * <p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/polynomial/MonomialTest test > MT.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-08
 */

public final class MonomialTest {

  @SuppressWarnings({ "static-method" })
  @Test
  public final void dbf () { 
    Common.monomial(MonomialDoubleBF.class); } 

  @SuppressWarnings({ "static-method" })
  @Test
  public final void d () { 
    Common.monomial(MonomialDouble.class); } 

  @SuppressWarnings({ "static-method" })
  @Test
  public final void drf () { 
    Common.monomial(MonomialDoubleRF.class); } 

  @SuppressWarnings({ "static-method" })
  @Test
  public final void rf () { 
    Common.monomial(MonomialRationalFloat.class); } 

  @SuppressWarnings({ "static-method" })
  @Test
  public final void bf () { 
    Common.monomial(MonomialBigFloat.class); } 

  @SuppressWarnings({ "static-method" })
  @Test
  public final void er () { 
    Common.monomial(MonomialERational.class); } 

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
