package xfp.java.scripts;

/**<pre>
 * jy --source 11 src/scripts/java/xfp/java/scripts/Shift.java
 * </pre>
 * @author palisades dot lakes at gmail dot com
 * @version 2019-05-07
 */
@SuppressWarnings("unchecked")
public final class Shift {

  public static final void main (final String[] args) {
    final int iones = 0xFFFFFFFF;
    for (int i=0;i<=32;i++) {
      System.out.println(i + " : " + Integer.toHexString(iones >>> i)); }
    final long lones = 0xFFFFFFFFFFFFFFFFL;
    for (int i=0;i<=64;i++) {
      System.out.println(i + " : " + Long.toHexString(lones >>> i)); }
  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
