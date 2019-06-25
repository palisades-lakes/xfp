package xfp.java.numbers;

/** {@link Natural} number builders. Mutable. Not thread safe.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-06-24
 */

public interface NaturalBuilder<T extends Natural> {

  //--------------------------------------------------------------
  /** return an immutable natural number whose value is the same
   * as this.
   */

  T build ();

  //--------------------------------------------------------------
  /** Return a builder whose value is the same as <code>u</code>. 
   * Usually the same object as <code>this</code>.
   */

  NaturalBuilder set (final T u); 

  //--------------------------------------------------------------
  /** Words from endWord on guaranteed to be zero
   * (exclusive bound). */

  int endWord ();

  int word (final int i);

  public default long uword (final int i) {
    return Numbers.unsigned(word(i)); }

  NaturalBuilder setWord (final int i,
                          final int w);

  //--------------------------------------------------------------
  // TODO: cache bit selectors?

  public default NaturalBuilder setBit (final int n) {
    assert 0<=n;
    final int iw = (n>>>5);
    final int w = word(iw);
    final int ib = (n&0x1F);
    setWord(iw,(w|(1<<ib)));
    return this; }

  public default NaturalBuilder clearBit (final int n) {
    assert 0<=n;
    final int iw = (n>>>5);
    final int w = word(iw);
    final int ib = (n&0x1F);
    setWord(iw,(w&(~(1<<ib))));
    return this; }

  public default NaturalBuilder flipBit (final int n) {
    assert 0<=n;
    final int iw = (n>>>5);
    final int w = word(iw);
    final int ib = (n&0x1F);
    setWord(iw,(w^(1<<ib)));
    return this; }

  //--------------------------------------------------------------
  /** Return a builder whose value is
   * <code>this * 2<sup>-shift</sup></code>.
   * Usually the same object as <code>this</code>.
   * 
   * <code>0&lt;=shift</code>
   */

  NaturalBuilder shiftDown (final int shift);

  /** Return a builder whose value is
   * <code>this * 2<sup>shift</sup></code>.
   * Usually the same object as <code>this</code>.
   * 
   * <code>0&lt;=shift</code>
   */

  NaturalBuilder shiftUp (final int shift);

  //--------------------------------------------------------------
  /** Return a builder whose value is
   * <code>this + u</code>.
   * Usually the same object as <code>this</code>.
   */

  public default NaturalBuilder increment (final T u) {
    // TODO: optimize by summing over joint range 
    // and just carrying after that
//    //Debug.println();
//    //Debug.println("this=" + toHexString());
//    //Debug.println("this.end=" + endWord());
//    //Debug.println("u=" + u.toHexString());
//    //Debug.println("u.end=" + u.endWord());
    final int end = Math.max(endWord(),u.endWord());
    long sum = 0L;
    long carry = 0L;
    int i=0;
    for (;i<end;i++) {
      sum = uword(i) + u.uword(i) + carry;
      carry = (sum>>>32);
      setWord(i,(int) sum); 
//      //Debug.println("carry="+carry);
//      //Debug.println("i="+i);
//      //Debug.println("this=" + toHexString());
    }
    if (0L!=carry) { 
      setWord(i,(int) carry); 
//      //Debug.println("carried");
//      //Debug.println("carry="+carry);
//      //Debug.println("i="+i);
    }
//    //Debug.println("this=" + toHexString());
//    //Debug.println("this.end=" + endWord());
    return this; }

  //--------------------------------------------------------------

  public default String toHexString () {
    final StringBuilder b = new StringBuilder("");
    final int n = endWord();
    if (0 == n) { b.append('0'); }
    else {
      b.append("[");
      b.append(String.format("%08x",Long.valueOf(uword(0))));
      for (int i=1;i<n;i++) {
        b.append(" ");
        b.append(
          String.format("%08x",Long.valueOf(uword(i)))); } 
      b.append("]"); }
    return b.toString(); }

}
//--------------------------------------------------------------

