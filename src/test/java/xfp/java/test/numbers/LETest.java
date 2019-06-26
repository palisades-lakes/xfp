package xfp.java.test.numbers;

//----------------------------------------------------------------
/** Testing little-endian numbers.
 * <p>
 * <pre>
 * mvn -q -Dtest=xfp/java/test/numbers/BeiTest test > BeiTest.txt
 * </pre>
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-06-26
 */

public final class LETest {

  //  /** How big an array do we need to hold m shifted up (left)
  //   * bitShift bits?
  //   */
  //  private static final int length (final int[] m,
  //                                  final int bitShift) {
  //    // other wise we would need to find the first nonzero element
  //    // wouldn't little endian be easier?
  //    assert !Bei.leadingZero(m);
  //    final int n = m.length + (bitShift >>> 5);
  //    final int remShift = (bitShift & 0x1f);
  //    if (0==remShift) { return n; }
  //    final int rShift = 32 - remShift;
  //    final int hi = (m[0] >>> rShift);
  //    return n + ((0!=hi) ? 1 : 0); }
  //
  //  @SuppressWarnings({ "static-method" })
  //  @Test
  //  public final void testShiftInto () {
  //    //Debug.DEBUG=false;
  //
  //    final int[] shifts = { 0, 32, 15, 31, 33, 67, };
  //
  //    for (final int shift : shifts) {
  //      final int[] a0 = new int[] { 0xf0f0f0f0, 0xd0d0e0e0, };
  //      final int[] b0 = Bei.shiftUp(a0, shift);
  //      final int[] b1 = new int[length(a0,shift) + 3];
  //      final int[] b2 = Bei.shiftUpInto(b1,a0,shift);
  //      final int[] b3 = Arrays.copyOfRange(b2,3,b2.length);
  //      Assertions.assertArrayEquals(
  //        b0, b3,
  //        "shift=" + shift
  //        + "\na0=" + Arrays.toString(a0)
  //        + "\nb0=" + Arrays.toString(b0)
  //        + "\nb2=" + Arrays.toString(b2)
  //        + "\nb3=" + Arrays.toString(b3)); }
  //
  //    for (final int shift : shifts) {
  //      final int[] a0 = new int[] { 100, 100, };
  //      final int[] b0 = Bei.shiftUp(a0, shift);
  //      final int[] b1 = new int[length(a0,shift) + 3];
  //      final int[] b2 = Bei.shiftUpInto(b1,a0,shift);
  //      final int[] b3 = Arrays.copyOfRange(b2,3,b2.length);
  //      Assertions.assertArrayEquals(
  //        b0, b3,
  //        "shift=" + shift
  //        + "\na0=" + Arrays.toString(a0)
  //        + "\nb0=" + Arrays.toString(b0)
  //        + "\nb2=" + Arrays.toString(b2)
  //        + "\nb3=" + Arrays.toString(b3)); }
  //
  //    for (final int shift : shifts) {
  //      final int[] a0 = new int[] { 0xf0f0f0f0, 0xd0d0e0e0, };
  //      final int[] b0 = Bei.shiftUp(a0, shift);
  //      final int[] b1 = new int[length(a0,shift)];
  //      final int[] b2 = Bei.shiftUpInto(b1,a0,shift);
  //      Assertions.assertArrayEquals(
  //        b0, b2,
  //        "shift=" + shift
  //        + "\na0=" + Arrays.toString(a0)
  //        + "\nb0=" + Arrays.toString(b0)
  //        + "\nb2=" + Arrays.toString(b2)); }
  //
  //    for (final int shift : shifts) {
  //      final int[] a0 = new int[] { 100, 100, };
  //      final int[] b0 = Bei.shiftUp(a0, shift);
  //      final int[] b1 = new int[length(a0,shift)];
  //      final int[] b2 = Bei.shiftUpInto(b1,a0,shift);
  //      Assertions.assertArrayEquals(
  //        b0, b2,
  //        "shift=" + shift
  //        + "\na0=" + Arrays.toString(a0)
  //        + "\nb0=" + Arrays.toString(b0)
  //        + "\nb2=" + Arrays.toString(b2)); }
  //
  //    //Debug.DEBUG=false; }
  //
  //  @SuppressWarnings({ "static-method" })
  //  @Test
  //  public final void testSubtractFrom () {
  //    //Debug.DEBUG=false;
  //
  //    final int[] dif = new int[] { 300, };
  //    final int[] a0 = new int[] { 100, };
  //
  //    final int[] c0 =  Bei.subtract(Bei.toInts(0x19L, 4), a0);
  //    Assertions.assertArrayEquals(
  //      dif, c0,
  //      "\ndif=" + Arrays.toString(dif)
  //      + "\nc0=" + Arrays.toString(c0));
  //
  //    final int[] c1 = Bei.subtract(0x19L, 4, a0);
  //    Assertions.assertArrayEquals(
  //      dif, c1,
  //      "\ndif=" + Arrays.toString(dif)
  //      + "\nc1=" + Arrays.toString(c1));
  //
  //    final int[] c2 = Bei.subtract(25L, 4, a0 );
  //    Assertions.assertArrayEquals(
  //      dif, c2,
  //      "\ndif=" + Arrays.toString(dif)
  //      + "\nc2=" + Arrays.toString(c2));
  //
  //    //Debug.DEBUG=false; }
  //
  //  @SuppressWarnings({ "static-method" })
  //  @Test
  //  public final void testAdd () {
  //    //Debug.DEBUG=false;
  //
  //    final int[] sum = new int[] { 301, };
  //    final int[] a0 = new int[] {0xc9,};
  //
  //    final int[] c0 =  Bei.add(a0, Bei.toInts(0x19L, 2));
  //    Assertions.assertArrayEquals(
  //      sum, c0,
  //      "\nsum=" + Arrays.toString(sum)
  //      + "\nc0=" + Arrays.toString(c0));
  //
  //    final int[] c1 = Bei.add(a0, 0x19L, 2);
  //    Assertions.assertArrayEquals(
  //      sum, c1,
  //      "\nsum=" + Arrays.toString(sum)
  //      + "\nc1=" + Arrays.toString(c1));
  //
  //    final int[] c2 = Bei.add(a0, 25L, 2);
  //    Assertions.assertArrayEquals(
  //      sum,c2,
  //      "\nsum=" + Arrays.toString(sum)
  //      + "\nc2=" + Arrays.toString(c2));
  //
  //    //Debug.DEBUG=false; }
  //
  //  @SuppressWarnings({ "static-method" })
  //  @Test
  //  public final void testAddLong () {
  //    //Debug.DEBUG=false;
  //
  //    final int[] sum = new int[] { 301, };
  //    final long a0 = 0xc9L;
  //
  //    final int[] c0 =
  //      Bei.add(Bei.toInts(a0), Bei.toInts(0x19L, 2));
  //    Assertions.assertArrayEquals(
  //      sum, c0,
  //      "\nsum=" + Arrays.toString(sum)
  //      + "\nc0=" + Arrays.toString(c0));
  //
  //    final int[] c1 = Bei.add(a0, 0x19L, 2);
  //    Assertions.assertArrayEquals(
  //      sum, c1,
  //      "\nsum=" + Arrays.toString(sum)
  //      + "\nc1=" + Arrays.toString(c1));
  //
  //    final int[] c2 = Bei.add(a0, 25L, 2);
  //    Assertions.assertArrayEquals(
  //      sum,c2,
  //      "\nsum=" + Arrays.toString(sum)
  //      + "\nc2=" + Arrays.toString(c2));
  //
  //    //Debug.DEBUG=false; }
  //
  //  @SuppressWarnings({ "static-method" })
  //  @Test
  //  public final void testSubtract() {
  //    //Debug.DEBUG=false;
  //
  //    final int[] dif = new int[] { 101, };
  //    final int[] a0 = new int[] { 0xc9, };
  //
  //    final int[] c0 =  Bei.subtract(a0, Bei.toInts(0x19L, 2));
  //    Assertions.assertArrayEquals(
  //      dif, c0,
  //      "\ndif=" + Arrays.toString(dif)
  //      + "\nc0=" + Arrays.toString(c0));
  //
  //    final int[] c1 = Bei.subtract(a0, 0x19L, 2);
  //    Assertions.assertArrayEquals(
  //      dif, c1,
  //      "\ndif=" + Arrays.toString(dif)
  //      + "\nc1=" + Arrays.toString(c1));
  //
  //    final int[] c2 = Bei.subtract(a0, 25L, 2);
  //    Assertions.assertArrayEquals(
  //      dif, c2,
  //      "\ndif=" + Arrays.toString(dif)
  //      + "\nc2=" + Arrays.toString(c2));
  //
  //    //Debug.DEBUG=false; }
  //
  //  // failing, need to handle long arg w < 52 bits
  //  @SuppressWarnings({ "static-method" })
  //  @Test
  //  public final void testCompare () {
  //    //Debug.DEBUG=false;
  //
  //    Assertions.assertEquals(
  //      1,
  //      Bei.compare(new int[] {0xc9,}, Bei.toInts(0x19L, 2)));
  //
  //    Assertions.assertEquals(
  //      1,
  //      Bei.compare(new int[] {0xc9,}, 0x19L, 2));
  //
  //    Assertions.assertEquals(
  //      1,
  //      Bei.compare(new int[] {201,}, 25L, 2));
  //
  //    //Debug.DEBUG=false;
  //  }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
