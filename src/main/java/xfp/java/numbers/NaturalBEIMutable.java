package xfp.java.numbers;

import static xfp.java.numbers.Numbers.loWord;
import static xfp.java.numbers.Numbers.unsigned;

import java.util.Arrays;

/**
 * Don't implement Comparable, because of mutability!
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-01
 */

public final class NaturalBEIMutable implements Natural {

  //--------------------------------------------------------------
  // mutable state
  //--------------------------------------------------------------
  /** The number of ints of the words array that are currently
   * used to hold the magnitude of this NaturalBEIMutable. The
   * magnitude starts at an start and start + nWords may be less
   * than words.length.
   */

  private int nWords;

  @Override
  public final int startWord () { return 0; }

  @Override
  public final int endWord () { return nWords; }

  /** The start into the words array where the magnitude of this
   * NaturalBEIMutable begins.
   *
   * <code>0&lt;=start</code>.
   */

  private int start = 0;

  /** big endian order. only a subsection used...
   */

  private int[] words;

  private final int[] getWords () {
    if ((start > 0) || (words.length != nWords)) {
      return Arrays.copyOfRange(words, start, start + nWords); }
    // UNSAFE!!!
    return words; }

  private final int[] copyWords () {
    return Arrays.copyOfRange(words, start, start + nWords); }

  // TODO: useful to oversize array? or as an option?
  private final void setWords (final int[] v,
                               final int length) {
    assert length<=v.length;
    words = v; nWords = length; start = 0; }

  private final int beIndex (final int i) {
    return (start+nWords)-1-i; }

  private final void clearUnused () {
    final int n = words.length;
    for (int i=0;i<start;i++) { words[i]=0; }
    for (int i=start+nWords;i<n;i++) { words[i]=0; } }

  private final void compact () {
    if (0!=start) {
      System.arraycopy(words,start,words,0,nWords);
      start = 0; }
    clearUnused(); }

  private final void expandTo (final int i) {
    final int i1 = i+1;
    if (nWords<i1) {
      if (i1<words.length) {
        compact();
        nWords = i1; }
      else {
        // TODO: more eager growth?
        final int[] tmp = new int[i1];
        System.arraycopy(words,start,tmp,i1-nWords,nWords);
        words = tmp;
        start = 0;
        nWords = i1; } }
    clearUnused(); }

  private final void setWords (final int[] v) {
    setWords(v,v.length); }

  @Override
  public final int word (final int i) {
    assert 0<=i;
    if (i>=nWords) { return 0; }
    return words[beIndex(i)]; }

  @Override
  public final Natural setWord (final int i,
                                final int w) {
    assert 0<=i;
    expandTo(i);
    words[beIndex(i)] = w;
    return this; }

  //--------------------------------------------------------------
  /** Returns an <code>int[]</code>containing the {@code n}
   * low ints of this number.
   * TODO: wrap with a view object, no copying?
   */

  private final int[] getLower (final int n) {
    if (isZero()) { return NaturalBEI.EMPTY; }
    // TODO: copy here? DANGER!!!
    else if (nWords < n) { return getWords(); }
    else {
      // strip zeros
      int len = n;
      while ((len > 0) && (words[(start+nWords)-len] == 0)) {
        len--; }
      return
        Arrays.copyOfRange(
          words, (start+nWords)-len, start+nWords); } }

  //--------------------------------------------------------------
  /** Makes this number an {@code n}-int number all of whose bits
   * are ones. Used by Burnikel-Ziegler division.
   * @param n number of ints in the {@code words} array
   * @return a number equal to {@code ((1<<(32*n)))-1}
   */

  private final void ones (final int n) {
    if (n > words.length) { words = new int[n]; }
    Arrays.fill(words, -1);
    start = 0;
    nWords = n; }

  @Override
  public final Natural zero () {
    start = 0;
    nWords = 0;
    Arrays.fill(words,0);
    return this; }

  @Override
  public final Natural clear () { return zero(); }

  public final int getLowestSetBit () {
    if (nWords == 0) { return -1; }
    int j, b;
    for (j=nWords-1; (j > 0) && (words[j+start] == 0); j--) { }
    b = words[j+start];
    if (b == 0) { return -1; }
    return ((nWords-1-j)<<5) + Integer.numberOfTrailingZeros(b); }

  private static final int loBit (final NaturalBEIMutable m) {
    return m.getLowestSetBit(); }

  private final void normalize () {
    if (nWords == 0) { start = 0; return; }
    int index = start;
    if (words[index] != 0) { return; }
    final int indexBound = index+nWords;
    do { index++; }
    while((index < indexBound) && (words[index] == 0));
    final int numZeros = index - start;
    nWords -= numZeros;
    start = (nWords == 0 ?  0 : start+numZeros); }

  /** Discards all ints whose index is greater than {@code n}.
   */
  private final void keepLower (final int n) {
    if (nWords >= n) { start += nWords - n; nWords = n; } }

  //--------------------------------------------------------------
  // bit operations
  //--------------------------------------------------------------

  public final long bitLength () {
    if (nWords == 0) { return 0; }
    return
      (nWords*32L)
      - Integer.numberOfLeadingZeros(words[start]); }

  //--------------------------------------------------------------
  /** Down shift this NaturalBEIMutable n bits, where n is
   * less than 32. Assumes that nWords > 0, n > 0 for speed
   */

  private final void primitiveDownShift (final int n) {
    final int[] val = words;
    final int n2 = 32 - n;
    for (int i=(start+nWords)-1, c=val[i]; i > start; i--) {
      final int b = c;
      c = val[i-1];
      val[i] = (c << n2) | (b >>> n); }
    val[start] >>>= n; }

  /** The NaturalBEIMutable is left in normal form.
   */

  private final void downShift (final int n) {
    if (nWords == 0) { return; }
    final int nInts = n >>> 5;
    final int nBits = n & 0x1F;
    this.nWords -= nInts;
    if (nBits == 0) { return; }
    final int bitsInHighWord = Numbers.bitLength(words[start]);
    if (nBits >= bitsInHighWord) {
      this.primitiveUpShift(32 - nBits);
      this.nWords--; }
    else { primitiveDownShift(nBits); } }

  /** {@code n} can be greater than the length of the number.
   */

  private final void safeDownShift (final int n) {
    if ((n/32) >= nWords) { clear(); }
    else { downShift(n); } }

  @Override
  public final NaturalBEIMutable shiftDown (final int shift) {
    safeDownShift(shift);
    return this; }

  //--------------------------------------------------------------
  /** Left shift this NaturalBEIMutable n bits, where n is
   * less than 32. Assumes that nWords > 0, n > 0 for speed
   */

  private final void primitiveUpShift (final int shift) {
    final int[] val = words;
    final int n2 = 32 - shift;
    for (int i=start, c=val[i], m=(i+nWords)-1; i < m; i++) {
      final int b = c;
      c = val[i+1];
      val[i] = (b << shift) | (c >>> n2); }
    val[(start+nWords)-1] <<= shift; }

  private final void upShift (final int shift) {
    // If there is enough storage space in this NaturalBEIMutable
    // already the available space will be used. Space to the
    // right of the used ints in the words array is faster to
    // utilize, so the extra space will be taken from the right if
    // possible.
    if (nWords == 0) { return; }
    final int iShift = (shift>>>5);
    final int rShift = (shift&0x1F);
    final int bitsInHighWord = Numbers.bitLength(words[start]);

    // If shift can be done without moving words, do so
    if (shift <= (32-bitsInHighWord)) {
      primitiveUpShift(rShift); return; }

    int newLen = nWords + iShift +1;
    if (rShift <= (32-bitsInHighWord)) { newLen--; }
    if (words.length < newLen) {
      // The array must grow
      final int[] result = new int[newLen];
      for (int i=0; i < nWords; i++) {
        result[i] = words[start+i]; }
      setWords(result, newLen); }
    else if ((words.length - start) >= newLen) {
      // Use space on right
      for(int i=0; i < (newLen - nWords); i++) {
        words[start+nWords+i] = 0; } }
    else {
      // Must use space on left
      for (int i=0;i<nWords;i++) { words[i] = words[start+i]; }
      for (int i=nWords; i<newLen; i++) { words[i] = 0; }
      start = 0; }
    nWords = newLen;
    if (rShift == 0) { return; }
    if (rShift <= (32-bitsInHighWord)) {
      primitiveUpShift(rShift); }
    else { primitiveDownShift(32 -rShift); } }

  /** {@code n} can be zero.
   */

  private final void safeUpShift (final int shift) {
    if (shift > 0) { upShift(shift); } }

  @Override
  public final NaturalBEIMutable shiftUp (final int shift) {
    assert 0<=shift;
    safeUpShift(shift);
    return this; }

  //--------------------------------------------------------------

  private static final void copyAndShift (final int[] src,
                                          int srcFrom,
                                          final int srcLen,
                                          final int[] dst,
                                          final int dstFrom,
                                          final int shift) {
    final int n2 = 32 - shift;
    int c=src[srcFrom];
    for (int i=0; i < (srcLen-1); i++) {
      final int b = c;
      c = src[++srcFrom];
      dst[dstFrom+i] = (b << shift) | (c >>> n2); }
    dst[(dstFrom+srcLen)-1] = c << shift; }

  //--------------------------------------------------------------
  // addition
  //--------------------------------------------------------------
  /** Adds the contents of two NaturalBEIMutable objects.The result
   * is placed within this NaturalBEIMutable. The contents of the
   * addend are not changed.
   */

  private final void add (final NaturalBEIMutable addend) {
    int x = nWords;
    int y = addend.nWords;
    int resultLen =
      (nWords > addend.nWords ? nWords : addend.nWords);
    int[] result =
      (words.length < resultLen ? new int[resultLen] : words);

    int rstart = result.length-1;
    long sum;
    long carry = 0;

    // Add common parts of both numbers
    while((x > 0) && (y > 0)) {
      x--; y--;
      sum = unsigned(words[x+start])
        + unsigned(addend.words[y+addend.start])
        + carry;
      result[rstart--] = (int)sum;
      carry = sum >>> 32; }

    // Add remainder of the longer number
    while(x > 0) {
      x--;
      if ((carry == 0)
        && (result == words)
        && (rstart == (x + start))) {
        return; }
      sum = unsigned(words[x+start]) + carry;
      result[rstart--] = (int)sum;
      carry = sum >>> 32; }
    while(y > 0) {
      y--;
      sum = unsigned(addend.words[y+addend.start]) + carry;
      result[rstart--] = (int)sum;
      carry = sum >>> 32; }

    if (carry > 0) { // Result must grow in length
      resultLen++;
      if (result.length < resultLen) {
        final int temp[] = new int[resultLen];
        // Result one word longer from carry-out; copy low-order
        // bits into new result.
        System.arraycopy(result, 0, temp, 1, result.length);
        temp[0] = 1;
        result = temp; }
      else { result[rstart--] = 1; } }

    words = result;
    nWords = resultLen;
    start = result.length - resultLen; }

  /** Adds the words of {@code addend} shifted {@code n} ints to
   * the left. Has the same effect as
   * {@code addend.upShift(32*ints); add(addend);}
   * but doesn't change the words of {@code addend}.
   */

  private final void addShifted (final NaturalBEIMutable addend,
                                 final int shift) {
    if (addend.isZero()) { return; }
    int x = nWords;
    int y = addend.nWords + shift;
    int resultLen = (nWords > y ? nWords : y);
    int[] result =
      (words.length < resultLen ? new int[resultLen] : words);

    int rstart = result.length-1;
    long sum;
    long carry = 0;

    // Add common parts of both numbers
    while ((x > 0) && (y > 0)) {
      x--; y--;
      final int bval =
        (y+addend.start) < addend.words.length
        ? addend.words[y+addend.start]
          : 0;
        sum = unsigned(words[x+start]) +
          (unsigned(bval)) + carry;
        result[rstart--] = (int)sum;
        carry = sum >>> 32; }

    // Add remainder of the longer number
    while (x > 0) {
      x--;
      if ((carry == 0)
        && (result == words)
        && (rstart == (x + start))) {
        return; }
      sum = unsigned(words[x+start]) + carry;
      result[rstart--] = (int)sum;
      carry = sum >>> 32; }
    while (y > 0) {
      y--;
      final int bval = ((y+addend.start) < addend.words.length
        ? addend.words[y+addend.start]
          : 0);
      sum = (unsigned(bval)) + carry;
      result[rstart--] = (int)sum;
      carry = sum >>> 32; }

    if (carry > 0) { // Result must grow in length
      resultLen++;
      if (result.length < resultLen) {
        final int temp[] = new int[resultLen];
        // Result one word longer from carry-out; copy low-order
        // bits into new result.
        System.arraycopy(result, 0, temp, 1, result.length);
        temp[0] = 1;
        result = temp; }
      else { result[rstart--] = 1; } }

    words = result;
    nWords = resultLen;
    start = result.length - resultLen; }

  /** Like {@link #addShifted(NaturalBEIMutable, int)} but
   * {@code this.intLen} must not be greater than {@code n}. In
   * other words, concatenates {@code this} and {@code addend}.
   */

  private final void addDisjoint (final NaturalBEIMutable addend,
                                  final int n) {
    if (addend.isZero()) { return; }
    final int x = nWords;
    int y = addend.nWords + n;
    final int resultLen = (nWords > y ? nWords : y);
    int[] result;
    if (words.length < resultLen) { result = new int[resultLen]; }
    else {
      result = words;
      Arrays.fill(words, start+nWords, words.length, 0); }
    int rstart = result.length-1;
    // copy from this if needed
    System.arraycopy(words, start, result, (rstart+1)-x, x);
    y -= x;
    rstart -= x;
    final int len = Math.min(y, addend.words.length-addend.start);
    System.arraycopy(
      addend.words, addend.start,
      result, (rstart+1)-y, len);
    // zero the gap
    for (int i=((rstart+1)-y)+len; i < (rstart+1); i++) {
      result[i] = 0; }
    words = result;
    nWords = resultLen;
    start = result.length - resultLen; }

  /** Adds the low {@code n} ints of {@code addend}.
   */
  private final void addLower (final NaturalBEIMutable addend,
                               final int n) {
    final NaturalBEIMutable a = new NaturalBEIMutable(addend);
    if ((a.start + a.nWords) >= n) {
      a.start = (a.start + a.nWords) - n;
      a.nWords = n; }
    a.normalize();
    add(a); }

  //--------------------------------------------------------------
  // subtraction
  //--------------------------------------------------------------
  /** Subtracts the smaller of this and b from the larger and
   * places the result into this NaturalBEIMutable.
   */

  private final int subtract (NaturalBEIMutable b) {
    NaturalBEIMutable a = this;
    int[] result = words;
    final int sign = a.compareTo(b);
    if (sign == 0) { clear(); return 0; }
    if (sign < 0) {
      final NaturalBEIMutable tmp = a; a = b; b = tmp; }
    final int resultLen = a.nWords;
    if (result.length < resultLen) { result = new int[resultLen]; }
    long diff = 0;
    int x = a.nWords;
    int y = b.nWords;
    int rstart = result.length - 1;
    // Subtract common parts of both numbers
    while (y > 0) {
      x--; y--;
      diff = unsigned(a.words[x+a.start])
        - unsigned(b.words[y+b.start])
        - ((int)-(diff>>32));
      result[rstart--] = (int)diff; }
    // Subtract remainder of longer number
    while (x > 0) {
      x--;
      diff = unsigned(a.words[x+a.start])
        - ((int)-(diff>>32));
      result[rstart--] = (int) diff; }

    words = result;
    nWords = resultLen;
    start = words.length - resultLen;
    normalize();
    return sign; }

  //--------------------------------------------------------------
  // square
  //--------------------------------------------------------------

  private static final int[] stripLeadingZeros (final int[] m) {
    final int n = m.length;
    int start = 0;
    while ((start < n) && (m[start] == 0)) { start++; }
    return (0==start) ? m : Arrays.copyOfRange(m,start,n); }

  private static final int[] EMPTY = new int[0];
  private static final boolean isZero (final int[] z) {
    for (final int element : z) {
      if (0!=element) { return false; } }
    return true; }


  private static final int KARATSUBA_SQUARE_THRESHOLD = 128;
  private static final int TOOM_COOK_SQUARE_THRESHOLD = 216;

  private static void implSquareToLenChecks (final int[] x,
                                             final int len,
                                             final int[] z,
                                             final int zlen) {
    if (len < 1) {
      throw new IllegalArgumentException(
        "invalid input length: " + len); }
    if (len > x.length) {
      throw new IllegalArgumentException(
        "input to long: " + len + " > " + x.length); }
    if ((len * 2) > z.length) {
      throw new IllegalArgumentException(
        "input too long: " + (len * 2) + " > "
          + z.length); }
    if (zlen < 1) {
      throw new IllegalArgumentException(
        "invalid input length: " + zlen); }
    if (zlen > z.length) {
      throw new IllegalArgumentException(
        "input to long: " + len + " > " + z.length); } }

  // shifts a up to len left n bits assumes no leading zeros,
  // 0<=n<32
  private static void primitiveLeftShift (final int[] a,
                                          final int len,
                                          final int n) {
    if ((len == 0) || (n == 0)) { return; }
    final int n2 = 32 - n;
    for (int i = 0, c = a[i], m = (i + len) - 1; i < m; i++) {
      final int b = c;
      c = a[i + 1];
      a[i] = (b << n) | (c >>> n2); }
    a[len - 1] <<= n; }

  private static final int addOne (final int[] a,
                                   int offset,
                                   int mlen,
                                   final int carry) {
    offset = a.length - 1 - mlen - offset;
    final long t = unsigned(a[offset]) + unsigned(carry);
    a[offset] = (int) t;
    if ((t >>> 32) == 0) { return 0; }
    while (--mlen >= 0) {
      if (--offset < 0) { // Carry out of number
        return 1; }
      a[offset]++;
      if (a[offset] != 0) { return 0; } }
    return 1; }

  private static final int[] getLower (final int[] m,
                                       final int n) {
    //assert (! leadingZero(m));
    final int len = m.length;
    if (len <= n) { return m; }
    final int lowerInts[] = new int[n];
    System.arraycopy(m,len-n,lowerInts,0,n);
    return stripLeadingZeros(lowerInts); }

  private static final int[] getUpper (final int[] m,
                                       final int n) {
    //assert (! leadingZero(m));
    final int len = m.length;
    if (len <= n) { return EMPTY; }
    final int upperLen = len - n;
    final int upperInts[] = new int[upperLen];
    System.arraycopy(m,0,upperInts,0,upperLen);
    return stripLeadingZeros(upperInts); }

  private static final int[] shiftUp (final int[] m,
                                      final int bitShift) {
    assert 0<=bitShift;
    if (bitShift==0) { return m; }
    if (isZero(m)) { return EMPTY; }
    final int intShift = (bitShift >>> 5);
    final int remShift = (bitShift & 0x1f);
    final int n = m.length;
    if (remShift==0) {
      return Arrays.copyOfRange(m,0,n+intShift); }
    int m1[] = null;
    int i = 0;
    final int downShift = 32 - remShift;
    final int highBits = (m[0] >>> downShift);
    if (highBits != 0) {
      m1 = new int[n + intShift + 1];
      m1[i++] = highBits; }
    else { m1 = new int[n + intShift]; }
    int j = 0;
    while (j < (n - 1)) {
      m1[i++] = (m[j++] << remShift) | (m[j] >>> downShift); }
    m1[i] = m[j] << remShift;
    return stripLeadingZeros(m1); }

  //--------------------------------------------------------------

  private static final int[] add (final int[] m0,
                                  final int[] m1) {
    // TODO: assert necessary?
    //assert (! leadingZero(m0));
    //assert (! leadingZero(m1));
    // If m0 is shorter, swap the two arrays
    if (m0.length < m1.length) { return add(m1,m0); }
    int i0 = m0.length;
    int i1 = m1.length;
    final int[] r0 = new int[i0];
    long sum = 0;
    if (i1 == 1) {
      sum = unsigned(m0[--i0]) + unsigned(m1[0]);
      r0[i0] = (int) sum; }
    else {
      while (i1 > 0) {
        sum =
          unsigned(m0[--i0])
          + unsigned(m1[--i1])
          + (sum >>> 32);
        r0[i0] = (int) sum; } }
    boolean carry = ((sum >>> 32) != 0);
    while ((i0 > 0) && carry) {
      carry = ((r0[--i0] = m0[i0] + 1) == 0); }
    while (i0 > 0) { r0[--i0] = m0[i0]; }
    if (carry) {
      final int[] r1 = new int[r0.length + 1];
      System.arraycopy(r0,0,r1,1,r0.length);
      r1[0] = 0x01;
      return stripLeadingZeros(r1); }
    return stripLeadingZeros(r0); }

  private static final int[] subtract (final int[] m0,
                                       final int[] m1) {

    // TODO: assert necessary?
    //assert (! leadingZero(m0));
    //assert (! leadingZero(m1));
    if (isZero(m1)) { return m0; }

    //final int c = compare(m0,m1);
    //assert 0L <= c;
    //if (c == 0) { return EMPTY; }

    int i0 = m0.length;
    final int result[] = new int[i0];
    int i1 = m1.length;
    long dif = 0;

    while (i1 > 0) {
      dif =
        (unsigned(m0[--i0])
          - unsigned(m1[--i1]))
        + (dif >> 32);
      result[i0] = (int) dif; }

    boolean borrow = ((dif >> 32) != 0);
    while ((i0 > 0) && borrow) {
      borrow = ((result[--i0] = m0[i0] - 1) == -1); }

    while (i0 > 0) { result[--i0] = m0[i0]; }

    return stripLeadingZeros(result); }

  private static final int[] squareKaratsuba (final int[] m) {
    //assert (! leadingZero(m));
    final int half = (m.length + 1) / 2;
    final int[] xl = getLower(m,half);
    final int[] xh = getUpper(m,half);
    final int[] xhs = square(xh);  // xhs = xh^2
    final int[] xls = square(xl);  // xls = xl^2
    // xh^2 << 64 + (((xl+xh)^2 - (xh^2 + xl^2)) << 32) + xl^2
    final int h32 = half*32;
    return
      add(
        shiftUp(
          add(
            shiftUp(xhs,h32),
            subtract(square(add(xl,xh)),add(xhs,xls))),
          h32),
        xls); }

  //--------------------------------------------------------------
  // Modular Arithmetic
  //--------------------------------------------------------------

  private static final void implMulAddCheck (final int[] out,
                                             final int[] in,
                                             final int offset,
                                             final int len) {
    //assert (! leadingZero(in));
    if (len > in.length) {
      throw new IllegalArgumentException(
        "input length is out of bound: " + len + " > "
          + in.length); }
    if (offset < 0) {
      throw new IllegalArgumentException(
        "input offset is invalid: " + offset); }
    if (offset > (out.length - 1)) {
      throw new IllegalArgumentException(
        "input offset is out of bound: " + offset + " > "
          + (out.length - 1)); }
    if (len > (out.length - offset)) {
      throw new IllegalArgumentException(
        "input len is out of bound: " + len + " > "
          + (out.length - offset)); } }

  private static final int implMulAdd (final int[] out,
                                       final int[] in,
                                       int offset,
                                       final int len,
                                       final int k) {
    final long kLong = loWord(k);
    long carry = 0;

    offset = out.length - offset - 1;
    for (int j = len - 1; j >= 0; j--) {
      final long product =
        (unsigned(in[j]) * kLong) + unsigned(out[offset])
        + carry;
      out[offset--] = (int) product;
      carry = product >>> 32; }
    return (int) carry; }

  private static final int mulAdd (final int[] out,
                                   final int[] in,
                                   final int offset,
                                   final int len,
                                   final int k) {
    //assert (! leadingZero(in));
    implMulAddCheck(out,in,offset,len);
    return implMulAdd(out,in,offset,len,k); }

  private static final int[] implSquareToLen (final int[] x,
                                              final int len,
                                              final int[] z,
                                              final int zlen) {
    //assert (! leadingZero(x));

    // The algorithm used here is adapted from Colin Plumb's C
    // library.
    // Technique: Consider the partial products in the
    // multiplication
    // of "abcde" by itself:
    // a b c d e
    // * a b c d e
    // ==================
    // ae be ce de ee
    // ad bd cd dd de
    // ac bc cc cd ce
    // ab bb bc bd be
    // aa ab ac ad ae
    // Note that everything above the main diagonal:
    // ae be ce de = (abcd) * e
    // ad bd cd = (abc) * d
    // ac bc = (ab) * c
    // ab = (a) * b
    // is a copy of everything below the main diagonal:
    // de
    // cd ce
    // bc bd be
    // ab ac ad ae
    // Thus, the sum is 2 * (off the diagonal) + diagonal.
    // This is accumulated beginning with the diagonal (which
    // consist of the squares of the digits of the input), which
    // is then divided by two, the off-diagonal added, and
    // multiplied by two again. The low bit is simply a copy of
    // the low bit of the input, so it doesn't need special care.

    // Store the squares, right shifted one bit (i.e., divided by
    // 2)
    int lastProductLowWord = 0;
    for (int j = 0, i = 0; j < len; j++) {
      final long piece = unsigned(x[j]);
      final long product = piece * piece;
      z[i++] =
        (lastProductLowWord << 31) | (int) (product >>> 33);
      z[i++] = (int) (product >>> 1);
      lastProductLowWord = (int) product;
    }

    // Add in off-diagonal sums
    for (int i = len, offset = 1; i > 0; i--, offset += 2) {
      int t = x[i - 1];
      t = mulAdd(z,x,offset,i - 1,t);
      addOne(z,offset - 1,i,t); }

    // Shift back up and set low bit
    primitiveLeftShift(z,zlen,1);
    z[zlen - 1] |= x[len - 1] & 1;

    return stripLeadingZeros(z); }

  private static final int[] squareToLen (final int[] m,
                                          final int len,
                                          int[] z) {
    //assert (! leadingZero(m));
    assert (0<=len);
    final int zlen = len << 1;
    if ((z == null) || (z.length < zlen)) {
      z = new int[zlen]; }

    // Execute checks before calling intrinsic method.
    implSquareToLenChecks(m,len,z,zlen);
    return implSquareToLen(m,len,z,zlen); }

  private static final int[] getToomSlice (final int[] m,
                                           final int lowerSize,
                                           final int upperSize,
                                           final int slice,
                                           final int fullsize) {
    //assert (! leadingZero(m));
    final int len = m.length;
    final int offset = fullsize - len;
    int start;
    final int end;
    if (slice == 0) {
      start = 0 - offset;
      end = upperSize - 1 - offset; }
    else {
      start = (upperSize + ((slice - 1) * lowerSize)) - offset;
      end = (start + lowerSize) - 1; }
    if (start < 0) { start = 0; }
    if (end < 0) { return EMPTY; }
    final int sliceSize = (end - start) + 1;
    if (sliceSize <= 0) { return EMPTY; }
    // While performing Toom-Cook, all slices are positive and
    // the sign is adjusted when the final number is composed.
    if ((start == 0) && (sliceSize >= len)) { return stripLeadingZeros(m); }
    final int intSlice[] = new int[sliceSize];
    System.arraycopy(m,start,intSlice,0,sliceSize);
    return stripLeadingZeros(intSlice); }

  private static final int[] exactDivideBy3 (final int[] m) {
    //assert (! leadingZero(m));
    final int len = m.length;
    final int[] result = new int[len];
    long x, w, q, borrow;
    borrow = 0L;
    for (int i = len - 1; i >= 0; i--) {
      x = unsigned(m[i]);
      w = x - borrow;
      if (borrow > x) { // Did we make the number go negative?
        borrow = 1L; }
      else { borrow = 0L; }
      // 0xAAAAAAAB is the modular inverse of 3 (mod 2^32). Thus,
      // the effect of this is to divide by 3 (mod 2^32).
      // This is much faster than division on most architectures.
      q = loWord(w * 0xAAAAAAABL);
      result[i] = (int) q;
      // Now check the borrow. The second check can of course be
      // eliminated if the first fails.
      if (q >= 0x55555556L) {
        borrow++;
        if (q >= 0xAAAAAAABL) { borrow++; } } }
    return stripLeadingZeros(result); }

  private static final int[] shiftDown0 (final int[] m0,
                                         final int n) {
    final int iShift = (n>>>5);
    final int rShift = (n & 0x1f);
    final int n0 = m0.length;
    int m1[] = null;

    // Special case: entire contents shifted off the end
    if (iShift >= n0) { return EMPTY; }

    if (rShift == 0) {
      final int newMagLen = n0 - iShift;
      m1 = Arrays.copyOf(m0,newMagLen); }
    else {
      int i = 0;
      final int highBits = m0[0] >>> rShift;
      if (highBits != 0) {
        m1 = new int[n0 - iShift];
        m1[i++] = highBits; }
      else {
        m1 = new int[n0 - iShift - 1]; }

      final int nBits2 = 32 - rShift;
      int j = 0;
      while (j < (n0 - iShift - 1)) {
        m1[i++] = (m0[j++] << nBits2) | (m0[j] >>> rShift); } }
    return m1; }

  private static final int[] shiftDown (final int[] m,
                                        final int n) {
    assert 0<=n;
    if (isZero(m)) { return EMPTY; }
    if (0==n) { return stripLeadingZeros(m); }
    return shiftDown0(m,n); }

  private static final int[] squareToomCook3 (final int[] m) {
    //assert (! leadingZero(m));
    final int len = m.length;
    // k is the size (in ints) of the lower-order slices.
    final int k = (len + 2) / 3;   // Equal to ceil(largest/3)

    // r is the size (in ints) of the highest-order slice.
    final int r = len - (2 * k);

    // Obtain slices of the numbers. a2 is the most significant
    // bits of the number, and a0 the least significant.
    final int[] a2 = getToomSlice(m,k,r,0,len);
    final int[] a1 = getToomSlice(m,k,r,1,len);
    final int[] a0 = getToomSlice(m,k,r,2,len);
    final int[] v0 = square(a0);
    int[] da1 = add(a2,a0);
    final int[] vm1 = square(subtract(da1,a1));
    da1 = add(da1,a1);
    final int[] v1 = square(da1);
    final int[] vinf = square(a2);
    final int[] v2 = square(subtract(shiftUp(add(da1,a2),1),a0));

    // The algorithm requires two divisions by 2 and one by 3.
    // All divisions are known to be exact, that is, they do not
    // produce remainders, and all results are positive. The
    // divisions by 2 are implemented as right shifts which are
    // relatively efficient, leaving only a division by 3.
    // The division by 3 is done by an optimized algorithm for
    // this case.
    int[] t2 = exactDivideBy3(subtract(v2,vm1));
    int[] tm1 = shiftDown(subtract(v1,vm1),1);
    int[] t1 = subtract(v1,v0);
    t2 = shiftDown(subtract(t2,t1),1);
    t1 = subtract(subtract(t1,tm1),vinf);
    t2 = shiftUp(subtract(t2,vinf),1);
    tm1 = subtract(tm1,t2);

    // Number of bits to shift left.
    final int ss = k * 32;

    return
      stripLeadingZeros(
        add(
          shiftUp(add(
            shiftUp(add(
              shiftUp(add(
                shiftUp(vinf,ss),
                t2),ss),
              t1),ss),
            tm1),ss),
          v0)); }

  private static final int[] square (final int[] m) {
    //assert (! leadingZero(m));
    if (isZero(m)) { return EMPTY; }
    final int len = m.length;
    if (len < KARATSUBA_SQUARE_THRESHOLD) {
      final int[] z = squareToLen(m,len,null);
      return stripLeadingZeros(z); }
    if (len < TOOM_COOK_SQUARE_THRESHOLD) {
      return squareKaratsuba(m); }
    // For a discussion of overflow detection see multiply()
    return squareToomCook3(m); }

  //--------------------------------------------------------------

  @Override
  public final NaturalBEIMutable square () {
    setWords(square(copyWords()));
    return this; }

  //--------------------------------------------------------------
  // multiplication
  //--------------------------------------------------------------

  private static final void multiplyToLenCheck (final int[] array,
                                                final int length) {
    //assert (! leadingZero(array));
    // not an error because multiplyToLen won't execute if len<=0
    if (length <= 0) { return; }
    //Objects.requireNonNull(array);
    if (length > array.length) {
      throw new ArrayIndexOutOfBoundsException(length-1); } }

  private static final int[] implMultiplyToLen (final int[] m0,
                                                final int n0,
                                                final int[] m1,
                                                final int n1,
                                                int[] z) {
    //assert (! leadingZero(m0));
    //assert (! leadingZero(m1));

    final int xstart = n0 - 1;
    final int ystart = n1 - 1;
    if ((z == null) || (z.length < (n0 + n1))) {
      z = new int[n0 + n1]; }
    long carry = 0;
    for (int j = ystart, k = ystart + 1 + xstart;
      j >= 0;
      j--, k--) {
      final long product =
        (unsigned(m1[j]) * unsigned(m0[xstart])) + carry;
      z[k] = (int) product;
      carry = product >>> 32; }
    z[xstart] = (int) carry;
    for (int i = xstart - 1; i >= 0; i--) {
      carry = 0;
      for (int j = ystart, k = ystart + 1 + i; j >= 0; j--, k--) {
        final long product =
          (unsigned(m1[j]) * unsigned(m0[i]))
          + unsigned(z[k]) + carry;
        z[k] = (int) product;
        carry = product >>> 32; }
      z[i] = (int) carry; }
    return z; }

  private static final int[] multiplyToLen (final int[] x,
                                            final int xlen,
                                            final int[] y,
                                            final int ylen,
                                            final int[] z) {
    multiplyToLenCheck(x,xlen);
    multiplyToLenCheck(y,ylen);
    return implMultiplyToLen(x,xlen,y,ylen,z); }

  //--------------------------------------------------------------

  private static final int[] multiplyKaratsuba (final int[] x,
                                                final int[] y) {
    //assert (! leadingZero(x));
    //assert (! leadingZero(y));
    final int xlen = x.length;
    final int ylen = y.length;

    // The number of ints in each half of the number.
    final int half = (Math.max(xlen,ylen) + 1) / 2;

    // xl and yl are the lower halves of x and y respectively,
    // xh and yh are the upper halves.
    final int[] xl = getLower(x,half);
    final int[] xh = getUpper(x,half);
    final int[] yl = getLower(y,half);
    final int[] yh = getUpper(y,half);

    final int[] p1 = multiply(xh,yh);  // p1 = xh*yh
    final int[] p2 = multiply(xl,yl);  // p2 = xl*yl

    // p3=(xh+xl)*(yh+yl)
    final int[] p3 = multiply(add(xh,xl),add(yh,yl));

    // result = p1 * 2^(32*2*half) + (p3 - p1 - p2) * 2^(32*half)
    // + p2
    final int h32 = half*32;
    final int[] result =
      add(
        shiftUp(add(
          shiftUp(p1,h32),
          subtract(subtract(p3,p1),p2)),
          h32),
        p2);

    return result; }

  //--------------------------------------------------------------

  private static final int compare (final int[] m0,
                                    final int[] m1) {
    // TODO: assert necessary?
    //assert (! leadingZero(m0));
    //assert (! leadingZero(m1));
    final int n0 = m0.length;
    final int n1 = m1.length;
    if (n0<n1) { return -1; }
    if (n0>n1) { return 1; }
    for (int i=0;i<n0;i++) {
      final long m0i = unsigned(m0[i]);
      final long m1i = unsigned(m1[i]);
      if (m0i<m1i) { return -1; }
      if (m0i>m1i) { return 1; } }
    return 0; }

  //--------------------------------------------------------------

  private static final int[] multiplyToomCook3 (final int[] m0,
                                                final int[] m1) {
    //assert (! leadingZero(m0));
    //assert (! leadingZero(m1));

    final int n0 = m0.length;
    final int n1 = m1.length;

    final int largest = Math.max(n0,n1);

    // k is the size (in ints) of the lower-order slices.
    final int k = (largest + 2) / 3;   // Equal to ceil(largest/3)

    // r is the size (in ints) of the highest-order slice.
    final int r = largest - (2 * k);

    // Obtain slices of the numbers. a2 and b2 are the most
    // significant bits of the numbers a and b, and a0 and b0 the
    // least significant.
    final int[] a2 = getToomSlice(m0,k,r,0,largest);
    final int[] a1 = getToomSlice(m0,k,r,1,largest);
    final int[] a0 = getToomSlice(m0,k,r,2,largest);
    final int[] b2 = getToomSlice(m1,k,r,0,largest);
    final int[] b1 = getToomSlice(m1,k,r,1,largest);
    final int[] b0 = getToomSlice(m1,k,r,2,largest);
    final int[] v0 = multiply(a0,b0);
    int[] da1 = add(a2,a0);
    int[] db1 = add(b2,b0);

    // might be negative
    final int[] da1_a1;
    final int ca = compare(da1,a1);
    if (0 < ca) { da1_a1 = subtract(da1,a1); }
    else { da1_a1 = subtract(a1,da1); }
    // might be negative
    final int[] db1_b1;
    final int cb = compare(db1,b1);
    if (0 < cb) { db1_b1 = subtract(db1,b1); }
    else { db1_b1 = subtract(b1,db1); }
    final int cv = ca * cb;
    final int[] vm1 = multiply(da1_a1,db1_b1);

    da1 = add(da1,a1);
    db1 = add(db1,b1);
    final int[] v1 = multiply(da1,db1);
    final int[] v2 =
      multiply(
        subtract(shiftUp(add(da1,a2),1),a0),
        subtract(shiftUp(add(db1,b2),1),b0));

    final int[] vinf = multiply(a2,b2);

    // The algorithm requires two divisions by 2 and one by 3.
    // All divisions are known to be exact, that is, they do not
    // produce remainders, and all results are positive. The
    // divisions by 2 are implemented as right shifts which are
    // relatively efficient, leaving only an exact division by 3,
    // which is done by a specialized linear-time algorithm.
    int[] t2;
    // handle missing sign of vm1
    if (0 < cv) { t2 = exactDivideBy3(subtract(v2,vm1)); }
    else { t2 = exactDivideBy3(add(v2,vm1));}

    int[] tm1;
    // handle missing sign of vm1
    if (0 < cv) { tm1 = shiftDown(subtract(v1,vm1),1); }
    else { tm1 = shiftDown(add(v1,vm1),1); }

    int[] t1 = subtract(v1,v0);
    t2 = shiftDown(subtract(t2,t1),1);
    t1 = subtract(subtract(t1,tm1),vinf);
    t2 = subtract(t2,shiftUp(vinf,1));
    tm1 = subtract(tm1,t2);

    // Number of bits to shift left.
    final int ss = k * 32;

    final int[] result =
      add(shiftUp(
        add(shiftUp(
          add(shiftUp(
            add(shiftUp(vinf,ss),t2),
            ss),t1),
          ss),tm1),
        ss),v0);
    return stripLeadingZeros(result); }

  //--------------------------------------------------------------

  private static final int[] multiply (final int[] m0,
                                       final long m1) {
    //assert (! leadingZero(m0));
    if (0L==m1) { return EMPTY; }
    assert 0L < m1;

    final long dh = m1 >>> 32;      // higher order bits
    final long dl = loWord(m1); // lower order bits
    final int xlen = m0.length;
    final int[] value = m0;
    int[] rm =
      (dh == 0L) ? (new int[xlen + 1]) : (new int[xlen + 2]);
      long carry = 0;
      int rstart = rm.length - 1;
      for (int i = xlen - 1; i >= 0; i--) {
        final long product = (unsigned(value[i]) * dl) + carry;
        rm[rstart--] = (int) product;
        carry = product >>> 32; }
      rm[rstart] = (int) carry;
      if (dh != 0L) {
        carry = 0;
        rstart = rm.length - 2;
        for (int i = xlen - 1; i >= 0; i--) {
          final long product =
            (unsigned(value[i]) * dh)
            + unsigned(rm[rstart]) + carry;
          rm[rstart--] = (int) product;
          carry = product >>> 32; }
        rm[0] = (int) carry; }
      if (carry == 0L) {
        rm = Arrays.copyOfRange(rm,1,rm.length); }
      return stripLeadingZeros(rm); }

  //--------------------------------------------------------------

  private static final int[] multiply (final int[] x,
                                       final int[] y) {
    //assert (! leadingZero(x));
    //assert (! leadingZero(y));

    if ((isZero(y)) || (isZero(x))) { return EMPTY; }
    final int xlen = x.length;
    if ((y == x)
      &&
      (xlen > MULTIPLY_SQUARE_THRESHOLD)) {
      return square(x); }

    final int ylen = y.length;

    if ((xlen < KARATSUBA_THRESHOLD)
      || (ylen < KARATSUBA_THRESHOLD)) {
      if (y.length == 1) { return multiply(x,unsigned(y[0])); }
      if (x.length == 1) { return multiply(y,unsigned(x[0])); }
      int[] result = multiplyToLen(x,xlen,y,ylen,null);
      result = stripLeadingZeros(result);
      return result; }
    if ((xlen < TOOM_COOK_THRESHOLD)
      && (ylen < TOOM_COOK_THRESHOLD)) {
      return multiplyKaratsuba(x,y); }
    return multiplyToomCook3(x,y); }

  @Override
  public final NaturalBEIMutable multiply (final Natural u) {
    setWords(
      multiply(copyWords(),((NaturalBEIMutable) u).copyWords()));
    return this; }

  //--------------------------------------------------------------
  // division
  //--------------------------------------------------------------
  /** This method is used for division of an n word dividend by a
   * one word divisor. The quotient is placed into quotient. The
   * one word divisor is specified by divisor.
   * @return the remainder of the division.
   */

  private final int divideOneWord (final int divisor,
                                   final NaturalBEIMutable quotient) {
    final long divisorLong = unsigned(divisor);
    // Special case of one word dividend
    if (nWords == 1) {
      final long dividendValue = unsigned(words[start]);
      final int q = (int) (dividendValue / divisorLong);
      final int r = (int) (dividendValue - (q * divisorLong));
      quotient.words[0] = q;
      quotient.nWords = (q == 0) ? 0 : 1;
      quotient.start = 0;
      return r; }

    if (quotient.words.length < nWords) {
      quotient.words = new int[nWords]; }
    quotient.start = 0;
    quotient.nWords = nWords;

    // Normalize the divisor
    final int shift = Integer.numberOfLeadingZeros(divisor);
    int rem = words[start];
    long remLong = unsigned(rem);
    if (remLong < divisorLong) { quotient.words[0] = 0; }
    else {
      quotient.words[0] = (int)(remLong / divisorLong);
      rem = (int) (remLong - (quotient.words[0] * divisorLong));
      remLong = unsigned(rem); }
    int xlen = nWords;
    while (--xlen > 0) {
      final long dividendEstimate = (remLong << 32) |
        unsigned(words[(start + nWords) - xlen]);
      int q;
      if (dividendEstimate >= 0) {
        q = (int) (dividendEstimate / divisorLong);
        rem = (int) (dividendEstimate - (q * divisorLong)); }
      else {
        final long tmp = divWord(dividendEstimate, divisor);
        q = (int) Numbers.loWord(tmp);
        rem = (int) (tmp >>> 32); }
      quotient.words[nWords - xlen] = q;
      remLong = unsigned(rem); }
    quotient.normalize();
    // denormalize
    if (shift > 0) { return rem % divisor; }
    return rem; }

  //--------------------------------------------------------------
  /** A primitive used for division. This method adds in one
   * multiple of the divisor a back to the dividend result at a
   * specified start. It is used when qhat was estimated too
   * large, and must be adjusted.
   */

  private static final int divadd (final int[] a,
                                   final int[] result,
                                   final int start) {
    long carry = 0;
    for (int j=a.length-1; j >= 0; j--) {
      final long sum =
        (unsigned(a[j])) + unsigned(result[j+start]) + carry;
      result[j+start] = (int)sum;
      carry = sum >>> 32; }
    return (int) carry; }

  /** Divide this NaturalBEIMutable by the divisor.
   * The quotient will be placed into the provided quotient object
   * and the remainder object is returned.
   */

  private final NaturalBEIMutable
  divideMagnitude (final NaturalBEIMutable div,
                   final NaturalBEIMutable quotient,
                   final boolean needRemainder ) {
    assert div.nWords > 1;
    // D1 normalize the divisor
    final int shift =
      Integer.numberOfLeadingZeros(div.words[div.start]);
    // Copy divisor words to protect divisor
    final int dlen = div.nWords;
    int[] divisor;
    // Remainder starts as dividend with space for a leading zero
    NaturalBEIMutable rem;
    if (shift > 0) {
      divisor = new int[dlen];
      copyAndShift(div.words,div.start,dlen,divisor,0,shift);
      if (Integer.numberOfLeadingZeros(words[start]) >= shift) {
        final int[] remarr = new int[nWords + 1];
        rem = new NaturalBEIMutable(remarr);
        rem.nWords = nWords;
        rem.start = 1;
        copyAndShift(words,start,nWords,remarr,1,shift); }
      else {
        final int[] remarr = new int[nWords + 2];
        rem = new NaturalBEIMutable(remarr);
        rem.nWords = nWords+1;
        rem.start = 1;
        int rFrom = start;
        int c=0;
        final int n2 = 32 - shift;
        for (int i=1; i < (nWords+1); i++,rFrom++) {
          final int b = c;
          c = words[rFrom];
          remarr[i] = (b << shift) | (c >>> n2); }
        remarr[nWords+1] = c << shift; } }
    else {
      divisor = Arrays.copyOfRange(
        div.words, div.start, div.start + div.nWords);
      rem = new NaturalBEIMutable(new int[nWords + 1]);
      System.arraycopy(words, start, rem.words, 1, nWords);
      rem.nWords = nWords;
      rem.start = 1; }

    final int nlen = rem.nWords;

    // Set the quotient size
    final int limit = (nlen - dlen) + 1;
    if (quotient.words.length < limit) {
      quotient.words = new int[limit];
      quotient.start = 0; }
    quotient.nWords = limit;
    final int[] q = quotient.words;

    // Must insert leading 0 in rem if its length did not change
    if (rem.nWords == nlen) {
      rem.start = 0;
      rem.words[0] = 0;
      rem.nWords++; }

    final int dh = divisor[0];
    final long dhLong = unsigned(dh);
    final int dl = divisor[1];
    // D2 Initialize j
    for (int j=0; j < (limit-1); j++) {
      // D3 Calculate qhat
      // estimate qhat
      int qhat = 0;
      int qrem = 0;
      boolean skipCorrection = false;
      final int nh = rem.words[j+rem.start];
      final int nh2 = nh + 0x80000000;
      final int nm = rem.words[j+1+rem.start];
      if (nh == dh) {
        qhat = ~0;
        qrem = nh + nm;
        skipCorrection = (qrem + 0x80000000) < nh2; }
      else {
        final long nChunk =
          (((long)nh) << 32) | (unsigned(nm));
        if (nChunk >= 0) {
          qhat = (int) (nChunk / dhLong);
          qrem = (int) (nChunk - (qhat * dhLong)); }
        else {
          final long tmp = divWord(nChunk, dh);
          qhat = (int) (loWord(tmp));
          qrem = (int) (tmp >>> 32); } }
      if (qhat == 0) { continue; }
      if (!skipCorrection) { // Correct qhat
        final long nl = unsigned(rem.words[j+2+rem.start]);
        long rs = (unsigned(qrem) << 32) | nl;
        long estProduct = unsigned(dl) * (unsigned(qhat));
        if (unsignedLongCompare(estProduct, rs)) {
          qhat--;
          qrem = (int)((unsigned(qrem)) + dhLong);
          if ((unsigned(qrem)) >=  dhLong) {
            estProduct -= (unsigned(dl));
            rs = ((unsigned(qrem)) << 32) | nl;
            if (unsignedLongCompare(estProduct, rs)) {
              qhat--; } } } }
      // D4 Multiply and subtract
      rem.words[j+rem.start] = 0;
      final int borrow =
        mulsub(rem.words, divisor, qhat, dlen, j+rem.start);
      // D5 Test remainder
      if ((borrow + 0x80000000) > nh2) {
        // D6 Add back
        divadd(divisor, rem.words, j+1+rem.start);
        qhat--; }
      // Store the quotient digit
      q[j] = qhat; } // D7 loop on j

    // D3 Calculate qhat
    // 1st estimate
    int qhat = 0;
    int qrem = 0;
    boolean skipCorrection = false;
    final int nh = rem.words[(limit - 1) + rem.start];
    final int nh2 = nh + 0x80000000;
    final int nm = rem.words[limit + rem.start];
    if (nh == dh) {
      qhat = ~0;
      qrem = nh + nm;
      skipCorrection = (qrem + 0x80000000) < nh2; }
    else {
      final long nChunk = (((long) nh) << 32) | (unsigned(nm));
      if (nChunk >= 0) {
        qhat = (int) (nChunk / dhLong);
        qrem = (int) (nChunk - (qhat * dhLong)); }
      else {
        final long tmp = divWord(nChunk, dh);
        qhat = (int) (loWord(tmp));
        qrem = (int) (tmp >>> 32); } }
    // 2nd correction
    if (qhat != 0) {
      if (!skipCorrection) {
        final long nl =
          unsigned(rem.words[limit + 1 + rem.start]);
        long rs = ((unsigned(qrem)) << 32) | nl;
        long estProduct = (unsigned(dl)) * (unsigned(qhat));
        if (unsignedLongCompare(estProduct, rs)) {
          qhat--;
          qrem = (int) ((unsigned(qrem)) + dhLong);
          if ((unsigned(qrem)) >= dhLong) {
            estProduct -= (unsigned(dl));
            rs = ((unsigned(qrem)) << 32) | nl;
            if (unsignedLongCompare(estProduct, rs)) { qhat--; } } } }
      // D4 Multiply and subtract
      int borrow;
      rem.words[(limit - 1) + rem.start] = 0;
      if(needRemainder) {
        borrow =
          mulsub(
            rem.words,divisor,qhat,dlen,(limit-1)+rem.start); }
      else {
        borrow =
          mulsubBorrow
          (rem.words,divisor,qhat,dlen,(limit-1)+rem.start); }
      // D5 Test remainder
      if ((borrow + 0x80000000) > nh2) {
        // D6 Add back
        if(needRemainder) {
          divadd(divisor,rem.words,(limit-1)+1+rem.start); }
        qhat--; }
      // Store the quotient digit
      q[(limit - 1)] = qhat; }

    if (needRemainder) {
      // D8 denormalize
      if (shift > 0) { rem.downShift(shift); }
      rem.normalize(); }
    quotient.normalize();
    return needRemainder ? rem : null; }

  //--------------------------------------------------------------

  private static final int KNUTH_POW2_THRESH_LEN = 6;
  private static final int KNUTH_POW2_THRESH_ZEROS = 3;

  /** Calculates the quotient of this div b and places the
   * quotient in the provided NaturalBEIMutable objects and the
   * remainder object is returned.
   *
   * Uses Algorithm D in Knuth section 4.3.1.
   * Many optimizations to that algorithm have been adapted from
   * the Colin Plumb C library.
   * It special cases one word divisors for speed. The content of
   * b is not changed.
   */

  public final NaturalBEIMutable
  divideKnuth (final NaturalBEIMutable b,
               final NaturalBEIMutable quotient,
               final boolean needRemainder) {
    assert 0 != b.nWords;
    // Dividend is zero
    if (nWords == 0) {
      quotient.nWords = 0;
      quotient.start = 0;
      return needRemainder ? new NaturalBEIMutable() : null; }

    final int cmp = compareTo(b);
    // Dividend less than divisor
    if (cmp < 0) {
      quotient.nWords = 0;
      quotient.start = 0;
      return needRemainder ? new NaturalBEIMutable(this) : null; }
    // Dividend equal to divisor
    if (cmp == 0) {
      quotient.words[0] = 1;
      quotient.nWords = 1;
      quotient.start = 0;
      return needRemainder ? new NaturalBEIMutable() : null; }

    quotient.zero();
    // Special case one word divisor
    if (b.nWords == 1) {
      final int r = divideOneWord(b.words[b.start], quotient);
      if(needRemainder) {
        if (r == 0) { return new NaturalBEIMutable(); }
        return new NaturalBEIMutable(r); }
      return null; }

    // Cancel common powers of two if we're above the
    // KNUTH_POW2_* thresholds
    if (nWords >= KNUTH_POW2_THRESH_LEN) {
      final int trailingZeroBits =
        Math.min(getLowestSetBit(), b.getLowestSetBit());
      if (trailingZeroBits >= (KNUTH_POW2_THRESH_ZEROS*32)) {
        final NaturalBEIMutable aa = new NaturalBEIMutable(this);
        final NaturalBEIMutable bb = new NaturalBEIMutable(b);
        aa.downShift(trailingZeroBits);
        bb.downShift(trailingZeroBits);
        final NaturalBEIMutable r = aa.divideKnuth(bb,quotient,true);
        r.upShift(trailingZeroBits);
        return r; } }

    return divideMagnitude(b, quotient, needRemainder); }

  //--------------------------------------------------------------
  // Burnikel-Ziegler
  //--------------------------------------------------------------
  /** This method implements algorithm 1 from pg. 4 of the
   * Burnikel-Ziegler paper. It divides a 2n-digit number by an
   * n-digit number.<br/>
   * The parameter beta is 2<sup>32</sup> so all shifts are
   * multiples of 32 bits. <br/>
   * {@code this} must be a nonnegative number such that
   * {@code this.bitLength() <= 2*b.bitLength()}
   * @param b a positive number such that {@code b.bitLength()} is even
   * @param quotient output parameter for {@code this/b}
   * @return {@code this%b}
   */

  private final NaturalBEIMutable
  divide2n1n (final NaturalBEIMutable b,
              final NaturalBEIMutable quotient) {
    final int n = b.nWords;

    // step 1: base case
    if (((n%2) != 0) || (n < NaturalBEI.BURNIKEL_ZIEGLER_THRESHOLD)) {
      return divideKnuth(b,quotient,true); }

    // step 2: view this as [a1,a2,a3,a4] where each ai is n/2 ints or less
    final NaturalBEIMutable aUpper = new NaturalBEIMutable(this);
    aUpper.safeDownShift(32*(n/2));   // aUpper = [a1,a2,a3]
    keepLower(n/2);   // this = a4

    // step 3: q1=aUpper/b, r1=aUpper%b
    final NaturalBEIMutable q1 = new NaturalBEIMutable();
    final NaturalBEIMutable r1 = aUpper.divide3n2n(b, q1);

    // step 4: quotient=[r1,this]/b, r2=[r1,this]%b
    addDisjoint(r1, n/2);   // this = [r1,this]
    final NaturalBEIMutable r2 = divide3n2n(b, quotient);

    // step 5: let quotient=[q1,quotient] and return r2
    quotient.addDisjoint(q1, n/2);
    return r2; }

  //--------------------------------------------------------------
  /** This method implements algorithm 2 from pg. 5 of the
   * Burnikel-Ziegler paper. It divides a 3n-digit number by a
   * 2n-digit number.<br/>
   * The parameter beta is 2<sup>32</sup> so all shifts are
   * multiples of 32 bits.<br/>
   * <br/>
   * {@code this} must be a nonnegative number such that
   * {@code 2*this.bitLength() <= 3*b.bitLength()}
   * @param quotient output parameter for {@code this/b}
   * @return {@code this%b}
   */

  private final NaturalBEIMutable
  divide3n2n (final NaturalBEIMutable b,
              final NaturalBEIMutable quotient) {
    final int n = b.nWords / 2;   // half the length of b in ints

    // step 1: view this as [a1,a2,a3] where each ai is n ints
    // or less; let a12=[a1,a2]
    final NaturalBEIMutable a12 = new NaturalBEIMutable(this);
    a12.safeDownShift(32*n);

    // step 2: view b as [b1,b2] where each bi is n ints or less
    final NaturalBEIMutable b1 = new NaturalBEIMutable(b);
    b1.safeDownShift(n * 32);
    final int[] b2 = b.getLower(n);
    NaturalBEIMutable r;
    NaturalBEIMutable d;
    if (compareShifted(b, n) < 0) {
      // step 3a: if a1<b1, let quotient=a12/b1 and r=a12%b1
      r = a12.divide2n1n(b1, quotient);
      // step 4: d=quotient*b2
      final int[] qu = NaturalBEI.multiply(quotient.getWords(),b2);
      d = NaturalBEIMutable.valueOf(qu); }
    else {
      // step 3b: if a1>=b1, let quotient=beta^n-1
      //and r=a12-b1*2^n+b1
      quotient.ones(n);
      a12.add(b1);
      b1.upShift(32*n);
      a12.subtract(b1);
      r = a12;
      // step 4: d=quotient*b2=(b2 << 32*n) - b2
      d = NaturalBEIMutable.valueOf(b2);
      d.upShift(32 * n);
      d.subtract(NaturalBEIMutable.valueOf(b2)); }
    // step 5: r = r*beta^n + a3 - d (paper says a4)
    // However, don't subtract d until after the while loop
    // so r doesn't become negative
    r.upShift(32 * n);
    r.addLower(this, n);
    // step 6: add b until r>=d
    while (r.compareTo(d) < 0) {
      r.add(b);
      quotient.subtract(NaturalBEIMutable.ONE); }
    r.subtract(d);
    return r; }

  //--------------------------------------------------------------
  /** Computes {@code this/b} and {@code this%b} using the
   * <a href="http://cr.yp.to/bib/1998/burnikel.ps">
   * Burnikel-Ziegler algorithm</a>. This method implements
   * algorithm 3 from pg. 9 of the Burnikel-Ziegler paper.
   * The parameter beta was chosen to b 2<sup>32</sup> so almost
   * all shifts are multiples of 32 bits.<br/>
   * {@code this} and {@code b} must be nonnegative.
   * @param b the divisor
   * @param quotient output parameter for {@code this/b}
   * @return the remainder
   */

  public final NaturalBEIMutable
  divideAndRemainderBurnikelZiegler (final NaturalBEIMutable b,
                                     final NaturalBEIMutable quotient) {
    final int r = nWords;
    final int s = b.nWords;

    // Clear the quotient
    quotient.start = quotient.nWords = 0;
    if (r < s) { return this; }
    // step 1: let m = min{2^k | (2^k)*BURNIKEL_ZIEGLER_THRESHOLD > s}
    final int s0 = s/NaturalBEI.BURNIKEL_ZIEGLER_THRESHOLD;
    final int m = 1 << (32-Integer.numberOfLeadingZeros(s0));

    final int j = ((s+m)-1) / m; // step 2a: j = ceil(s/m)
    final int n = j * m; // step 2b: block length in 32-bit units
    final long n32 = 32L * n; // block length in bits
    // step 3: sigma = max{T | (2^T)*B < beta^n}
    final int sigma = (int) Math.max(0, n32 - b.bitLength());
    final NaturalBEIMutable bShifted = new NaturalBEIMutable(b);
    // step 4a: shift b so its length is a multiple of n
    bShifted.safeUpShift(sigma);
    final NaturalBEIMutable aShifted = new NaturalBEIMutable(this);
    // step 4b: shift a by the same amount
    aShifted.safeUpShift(sigma);

    // step 5: t is the number of blocks needed to accommodate a
    // plus one additional bit
    int t = (int) ((aShifted.bitLength()+n32) / n32);
    if (t < 2) { t = 2; }

    // step 6: conceptually split a into blocks a[t-1], ..., a[0]
    // the most significant block of a
    final NaturalBEIMutable a1 = aShifted.getBlock(t-1, t, n);

    // step 7: z[t-2] = [a[t-1], a[t-2]]
    // the second to most significant block
    NaturalBEIMutable z = aShifted.getBlock(t-2, t, n);
    z.addDisjoint(a1, n);   // z[t-2]

    // do schoolbook division on blocks, dividing 2-block numbers
    // by 1-block numbers
    final NaturalBEIMutable qi = new NaturalBEIMutable();
    NaturalBEIMutable ri;
    for (int i=t-2; i > 0; i--) {
      // step 8a: compute (qi,ri) such that z=b*qi+ri
      ri = z.divide2n1n(bShifted, qi);

      // step 8b: z = [ri, a[i-1]]
      z = aShifted.getBlock(i-1, t, n);   // a[i-1]
      z.addDisjoint(ri, n);
      // update q (part of step 9)
      quotient.addShifted(qi, i*n); }
    // final iteration of step 8: do the loop one more time
    // for i=0 but leave z unchanged
    ri = z.divide2n1n(bShifted, qi);
    quotient.add(qi);
    // step 9: a and b were shifted, so shift back
    ri.downShift(sigma);
    return ri; }

  /** This method is used for division. It multiplies an n word
   * input a by one word input x, and subtracts the n word product
   * from q. This is needed when subtracting qhat*divisor from
   * the dividend.
   */
  private static final int mulsub (final int[] q,
                                   final int[] a,
                                   final int x,
                                   final int len,
                                   int start) {
    final long xLong = unsigned(x);
    long carry = 0;
    start += len;
    for (int j=len-1;j>=0;j--) {
      final long product = (unsigned(a[j])*xLong) + carry;
      final long difference = q[start] - product;
      q[start--] = (int)difference;
      carry = (product >>> 32)
        +
        (((loWord(difference))>(unsigned(~(int)product)))?1:0); }
    return (int) carry; }

  /** The method is the same as {@link #mulsub}, except the fact
   * that q array is not updated, the only result of the method is
   * borrow flag.
   */
  private static final int mulsubBorrow (final int[] q,
                                         final int[] a,
                                         final int x,
                                         final int len,
                                         int start) {
    final long xLong = unsigned(x);
    long carry = 0;
    start += len;
    for (int j=len-1; j >= 0; j--) {
      final long product =
        ((unsigned(a[j])) * xLong) + carry;
      final long difference = q[start--] - product;
      carry = (product >>> 32)
        + (((loWord(difference)) >
        (unsigned(~(int)product))) ? 1:0); }
    return (int) carry; }

  /** This method divides a long quantity by an int to estimate
   * qhat for two multi precision numbers. It is used when
   * the signed words of n is less than zero.
   * Returns long words where high 32 bits contain remainder words
   * and low 32 bits contain quotient words.
   */
  private static final long divWord(final long n, final int d) {
    final long dLong = unsigned(d);
    if (dLong == 1) {
      final long q = (int) n;
      final long r = 0;
      return (r << 32) | (loWord(q)); }
    // Approximate the quotient and remainder
    long q = (n >>> 1) / (dLong >>> 1);
    long r = n - (q*dLong);
    // Correct the approximation
    while (r < 0) { r += dLong; q--; }
    while (r >= dLong) { r -= dLong; q++; }
    // n - q*dlong == r && 0 <= r <dLong, hence we're done.
    return (r << 32) | (loWord(q)); }

  //-------------------------------------------------------------

  private final NaturalBEIMutable
  divide (final NaturalBEIMutable b,
          final NaturalBEIMutable quotient,
          final boolean needRemainder) {
    if ((b.nWords < NaturalBEI.BURNIKEL_ZIEGLER_THRESHOLD) ||
      ((nWords - b.nWords) < NaturalBEI.BURNIKEL_ZIEGLER_OFFSET)) {
      return divideKnuth(b, quotient, needRemainder); }
    return divideAndRemainderBurnikelZiegler(b, quotient); }

  //-------------------------------------------------------------
  // gcd
  //-------------------------------------------------------------
  /** a and b interpreted as unsigned integers.
   */

  private static final int binaryGcd (int a, int b) {
    if (b == 0) { return a; }
    if (a == 0) { return b; }

    // Down shift a & b till their last bits equal to 1.
    final int aZeros = Integer.numberOfTrailingZeros(a);
    final int bZeros = Integer.numberOfTrailingZeros(b);
    a >>>= aZeros;
    b >>>= bZeros;

    final int t = (aZeros < bZeros ? aZeros : bZeros);

    while (a != b) {
      if ((a+0x80000000) > (b+0x80000000)) {  // a > b as unsigned
        a -= b;
        a >>>= Integer.numberOfTrailingZeros(a); }
      else {
        b -= a;
        b >>>= Integer.numberOfTrailingZeros(b); } }
    return a<<t; }

  /** Subtracts the smaller of a and b from the larger and places
   * the result into the larger. Returns 1 if the answer is in a,
   * -1 if in b, 0 if no operation was performed.
   */
  private final int difference (NaturalBEIMutable b) {
    NaturalBEIMutable a = this;
    final int sign = a.compareTo(b);
    if (sign == 0) { return 0; }
    if (sign < 0) {
      final NaturalBEIMutable tmp = a; a = b; b = tmp; }

    long diff = 0;
    int x = a.nWords;
    int y = b.nWords;

    // Subtract common parts of both numbers
    while (y > 0) {
      x--; y--;
      diff =
        unsigned(a.words[a.start+ x])
        - unsigned(b.words[b.start+ y])
        - ((int)-(diff>>32));
      a.words[a.start+x] = (int) diff; }
    // Subtract remainder of longer number
    while (x > 0) {
      x--;
      diff = unsigned(a.words[a.start+ x]) - ((int)-(diff>>32));
      a.words[a.start+x] = (int)diff; }
    a.normalize();
    return sign; }

  private final NaturalBEIMutable
  binaryGCD (NaturalBEIMutable v) {
    // Algorithm B from Knuth section 4.5.2
    NaturalBEIMutable u = this;
    final NaturalBEIMutable r = new NaturalBEIMutable();

    // step B1
    final int s1 = u.getLowestSetBit();
    final int s2 = v.getLowestSetBit();
    final int k = (s1 < s2) ? s1 : s2;
    if (k != 0) { u.downShift(k); v.downShift(k); }

    // step B2
    final boolean uOdd = (k == s1);
    NaturalBEIMutable t = uOdd ? v: u;
    int tsign = uOdd ? -1 : 1;

    int lb;
    while ((lb = t.getLowestSetBit()) >= 0) {
      // steps B3 and B4
      t.downShift(lb);
      // step B5
      if (tsign > 0) { u = t; }
      else { v = t; }

      // Special case one word numbers
      if ((u.nWords < 2) && (v.nWords < 2)) {
        int x = u.words[u.start];
        final int y = v.words[v.start];
        x  = binaryGcd(x, y);
        r.words[0] = x;
        r.nWords = 1;
        r.start = 0;
        if (k > 0) { r.upShift(k); }
        return r; }

      // step B6
      if ((tsign = u.difference(v)) == 0) { break; }
      t = ((tsign >= 0) ? u : v); }

    if (k > 0) { u.upShift(k); }
    return u; }

  //-------------------------------------------------------------
  // Use Euclid's algorithm until the numbers are approximately the
  // same length, then use the binary GCD algorithm to find the GCD.

  public final NaturalBEIMutable hybridGCD (final NaturalBEIMutable d) {
    NaturalBEIMutable b = d;
    NaturalBEIMutable a = this;
    final NaturalBEIMutable q = new NaturalBEIMutable();
    while (b.nWords != 0) {
      if (Math.abs(a.nWords - b.nWords) < 2) {
        return a.binaryGCD(b); }
      final NaturalBEIMutable r = a.divide(b, q, true);
      a = b;
      b = r; }
    return a; }

  // remove common factors as if numerator and denominator
  public static final NaturalBEIMutable[]
    reduce (final NaturalBEIMutable n,
            final NaturalBEIMutable d) {
    final int shift = Math.min(loBit(n),loBit(d));
    if (0 != shift) {
      n.downShift(shift);
      d.downShift(shift); }
    //    if (n.equals(d)) {
    //      return new NaturalBEIMutable[] { ONE, ONE, }; }
    //    if (NaturalBEIMutable.d.isOne()) {
    //      return new NaturalBEIMutable[] { n, ONE, }; }
    //    if (NaturalBEI.n.isOne()) {
    //      return new NaturalBEIMutable[] { ONE, d, }; }
    final NaturalBEIMutable gcd = n.hybridGCD(d);
    if (gcd.compareTo(ONE) > 0) {
      final NaturalBEIMutable[] nd = { new NaturalBEIMutable(),
                                       new NaturalBEIMutable(), };
      n.divide(gcd,nd[0],false);
      d.divide(gcd,nd[1],false);
      return nd; }
    return new NaturalBEIMutable[] { n, d, }; }

  //-------------------------------------------------------------
  // pseudo-Comparable, not Comparable due to mutability
  //-------------------------------------------------------------
  /** Compare two longs as if they were unsigned.
   * Returns true iff one is bigger than two.
   */

  private static final boolean
  unsignedLongCompare (final long one,
                       final long two) {
    return (one+Long.MIN_VALUE) > (two+Long.MIN_VALUE); }

  private final int compareTo (final NaturalBEIMutable b) {
    final int blen = b.nWords;
    if (nWords < blen) { return -1; }
    if (nWords > blen) { return 1; }
    // TODO: is this faster than unsigned long conversion?
    final int[] bval = b.words;
    for (int i = start, j = b.start; i < (nWords + start); i++, j++) {
      final int b1 = words[i] + 0x80000000;
      final int b2 = bval[j]  + 0x80000000;
      if (b1 < b2) { return -1; }
      if (b1 > b2) { return 1; } }
    return 0; }

  private final int compareShifted (final NaturalBEIMutable b,
                                    final int ints) {
    final int blen = b.nWords;
    final int alen = nWords - ints;
    if (alen < blen) { return -1; }
    if (alen > blen) { return 1; }
    // TODO: is this faster than unsigned long conversion?
    final int[] bval = b.words;
    for (int i = start, j = b.start; i < (alen + start); i++, j++) {
      final int b1 = words[i] + 0x80000000;
      final int b2 = bval[j]  + 0x80000000;
      if (b1 < b2) { return -1; }
      if (b1 > b2) { return 1; } }
    return 0; }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final String toString () {
    return NaturalBEI.valueOf(getWords()).toString(); }

  //--------------------------------------------------------------
  // Mutability
  //-------------------------------------------------------------
  /** safe but slow...
   * TODO: options are:
   * <ul>
   * <ui> return internal array and destroy references.
   * prevents incremental builds.
   * <ui> copy internal array and preserve internal state.
   * extra array allocation if only building once.
   * </ul>
   * maybe 2 'build' operations, 'build', 'buildAndClaer'?
   */

  @Override
  public final Natural immutable () {
    // TODO: other constants?
    if (isZero()) { return NaturalBEI.ZERO; }
    return
      NaturalBEI.unsafe(
        stripLeadingZeros(
          Arrays.copyOfRange(words,start,start+nWords))); }

  @Override
  public final Natural recyclable (final Natural init) {
    assert this==init;
    return this; }

  @Override
  public final Natural recyclable (final int n) {
    expandTo(n);
    return this; }

  @Override
  public final boolean isImmutable () { return false; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  // DANGER!! no copying
  private NaturalBEIMutable (final int[] val) {
    words = val;
    nWords = val.length; }

  private NaturalBEIMutable () { words = new int[1]; nWords = 0; }

  private NaturalBEIMutable (final NaturalBEIMutable val) {
    nWords = val.nWords;
    words =
      Arrays.copyOfRange(
        val.words,val.start,val.start+nWords); }

  private NaturalBEIMutable (final int val) {
    words = new int[1];
    nWords = 1;
    words[0] = val; }

  //--------------------------------------------------------------
  /** Returns a {@code NaturalBEIMutable} containing
   * {@code blockLength} ints from {@code this} number, starting
   * at {@code index*blockLength}.<br/>
   * Used by Burnikel-Ziegler division.
   * @param index the block index
   * @param numBlocks the total number of blocks in {@code this}
   * @param blockLength length of one block in units of 32 bits
   * @return
   */

  private final NaturalBEIMutable getBlock (final int index,
                                            final int numBlocks,
                                            final int blockLength) {
    final int blockStart = index * blockLength;
    if (blockStart >= nWords) { return new NaturalBEIMutable(); }
    int blockEnd;
    if (index == (numBlocks-1)) { blockEnd = nWords; }
    else { blockEnd = (index+1) * blockLength; }
    if (blockEnd > nWords) { return new NaturalBEIMutable(); }
    final int[] newVal =
      Arrays.copyOfRange(
        words,
        (start+nWords)-blockEnd,
        (start+nWords)-blockStart);
    return new NaturalBEIMutable(newVal); }

  //--------------------------------------------------------------

  public static final NaturalBEIMutable make () {
    return new NaturalBEIMutable(); }

  //DANGER!!
  public static final NaturalBEIMutable unsafe (final int[] u) {
    return new NaturalBEIMutable(u); }

  public static final NaturalBEIMutable make (final int n) {
    return new NaturalBEIMutable(new int[n]); }

  public static final NaturalBEIMutable
  valueOf (final int[] val) {
    return unsafe(Arrays.copyOf(val,val.length)); }

  public static final NaturalBEIMutable
  valueOf (final NaturalBEI u) {
    return unsafe(u.copyWords()); }

  public static final NaturalBEIMutable valueOf (final long u) {
    if (0L==u) { return make(); }
    assert 0L<u;
    return unsafe(NaturalBEI.toInts(u)); }

  public static final NaturalBEIMutable valueOf (final long u,
                                                 final int upShift) {
    assert 0L<=u;
    assert 0<=upShift;
    if (0==upShift) { return valueOf(u); }
    if (0L==u) { return make(); }
    return unsafe(NaturalBEI.shiftUpLong(u,upShift)); }

  @Override
  public final Natural from (final long u) {
    return NaturalBEI.valueOf(u); }

  @Override
  public final Natural from (final long u,
                             final int shift) {
    return valueOf(u,shift); }

  //--------------------------------------------------------------

  private static final NaturalBEIMutable ONE =
    new NaturalBEIMutable(1);

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

