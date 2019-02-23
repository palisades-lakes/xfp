package xfp.java.scripts;

import org.apache.commons.math3.fraction.BigFraction;

import xfp.java.numbers.BigFractions;

//----------------------------------------------------------------
/** BigFraction from Long bug? 
 *
 * jy --source 11 src/scripts/java/xfp/java/scripts/LBF.java
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-02-22
 */

@SuppressWarnings("unchecked")
public final class LBF {

  public static final void main (final String[] args) {
    
    final long l = -1321315252193142600L;
    System.out.println("long:" + l);
    final Object ll = Long.valueOf(l);
    System.out.println("Long:" + ll);
    final BigFraction bf0 = (BigFraction) BigFractions.toBigFraction(ll);
    System.out.println(" BigFractions.toBigFraction(Long):" + bf0);
    }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
