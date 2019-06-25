package xfp.java.numbers;

/** {@link Natural} number builders. Mutable. Not thread safe.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-06-25
 */

public interface NaturalBuilder<T extends Natural> {

  //--------------------------------------------------------------
  /** return an immutable natural number whose value is the same
   * as this.
   */

  T build ();

  //--------------------------------------------------------------

  public default boolean isZero () { 
    for (int i=0;i<endWord();i++) {
      if (0!=word(i)) { return false; } }
    return true; }

  //--------------------------------------------------------------
  /** Return a builder whose value is zero. 
   * Usually the same object as <code>this</code>, preserving 
   * allocated arrays if possible,
   */

  NaturalBuilder zero (); 

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
  /** Return a builder whose value is the same as <code>u</code>. 
   * Usually the same object as <code>this</code>.
   */

  public default NaturalBuilder set (final long u) {
    assert 0<=u;
    // TODO: optimize zeroing internal array?
    zero();
    final long lo = Numbers.loWord(u);
    if (0!=lo) { setWord(0,(int) lo); }
    final long hi = Numbers.hiWord(u);
    if (0!=hi) { setWord(1,(int) hi); }
    return this; }

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

  /** Return a builder whose value is
   * <code>u * 2<sup>shift</sup></code>.
   * Usually the same object as <code>this</code>.
   * 
   * <code>0&lt;=shift</code>
   */

  public default NaturalBuilder shiftUp (final T u,
                                         final int shift) {
    // TODO: optimize as single op
    return set(u).shiftUp(shift); }

  /** Return a builder whose value is
   * <code>u * 2<sup>shift</sup></code>.
   * Usually the same object as <code>this</code>.
   * 
   * <code>0&lt;=shift</code>
   */

  public default NaturalBuilder shiftUp (final long u,
                                         final int shift) {
    // TODO: optimize as single op
    return set(u).shiftUp(shift); }

  //--------------------------------------------------------------
  /** Return a builder whose value is
   * <code>this + u</code>.
   * Usually the same object as <code>this</code>.
   */

  public default NaturalBuilder increment (final T u) {
    // TODO: optimize by summing over joint range 
    // and just carrying after that
    final int end = Math.max(endWord(),u.endWord());
    long sum = 0L;
    long carry = 0L;
    int i=0;
    for (;i<end;i++) {
      sum = uword(i) + u.uword(i) + carry;
      carry = (sum>>>32);
      setWord(i,(int) sum); }
    if (0L!=carry) { setWord(i,(int) carry); }
    return this; }

  public default NaturalBuilder increment (final long u) {
    assert 0L<=u;
    if (0L==u) { return this; }
    if (isZero()) { return set(u); }
    long sum = uword(0) + Numbers.loWord(u);
    setWord(0,(int) sum);
    long carry = (sum>>>32);
    sum = uword(1) + Numbers.hiWord(u) + carry;
    setWord(1,(int) sum);
    carry = (sum>>>32);
    int i=2;
    final int n = endWord();
    for (;(0L!=carry)&&(i<n);i++) {
      sum = uword(i) + carry;
      setWord(i,(int) sum);
      carry = (sum>>>32); }
    if (0L!=carry) { setWord(i,(int) carry); }
    return this; }

  //--------------------------------------------------------------
  /** Return a builder whose value is
   * <code>this  u</code>.
   * Usually the same object as <code>this</code>.
   * <code>u<=this</code>
   */

  public default NaturalBuilder decrement (final T u) {
    // TODO: fast correct check of u<=this
    if (u.isZero()) { return this; }
    assert ! isZero();
    long dif = 0L;
    long borrow = 0L;
    final int n = Math.max(endWord(),u.endWord());
    int i=0;
    // TODO: optimize by differencing over shared range
    // and then just borrowing
    for (;i<n;i++) {
      dif = uword(i) - u.uword(i) + borrow;
      borrow = (dif>>32);
      setWord(i,(int) dif); }
    assert 0L==borrow;
    return this; }

  public default NaturalBuilder decrement (final long u) {
    assert 0L<=u;
    if (0L==u) { return this; }
    assert ! isZero();
    final long lo = Numbers.loWord(u);
    final long hi = Numbers.hiWord(u);
    if (0L!=hi) { assert 2<=endWord(); }
    if (0L!=lo) { assert 1<=endWord(); }
    long dif = uword(0)-lo;
    setWord(0,(int) dif);
    long borrow = (dif>>32);
    dif = uword(1)-hi+borrow;
    setWord(1,(int) dif);
    borrow = (dif>>32);
    int i=2;
    final int n = endWord();
    for (;(0L!=borrow)&&(i<n);i++) {
      dif = uword(i)+borrow;
      setWord(i,(int) dif);
      borrow = (dif>>32); }
    assert 0L==borrow : borrow;
    return this; }

  //--------------------------------------------------------------

  NaturalBuilder square ();
  
  NaturalBuilder multiply (T u);
  
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

