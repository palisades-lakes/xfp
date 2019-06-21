package xfp.java.numbers;

import static xfp.java.numbers.Numbers.loWord;
import static xfp.java.numbers.Numbers.unsigned;

import java.util.Arrays;

/**
 * Don't implement Comparable, because of mutability!
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-06-19
 */

public final class NaturalBEIBuilder {

  //--------------------------------------------------------------
  // mutable state
  //--------------------------------------------------------------
  /** The number of ints of the value array that are currently
   * used to hold the magnitude of this NaturalBEIBuilder. The
   * magnitude starts at an offset and offset + intLen may be less
   * than value.length.
   */

  private int intLen;

  /** The offset into the value array where the magnitude of this
   * NaturalBEIBuilder begins.
   */

  private int offset = 0;

  /** big endian order. may start at an offset. may end early...
   */

  private int[] value;

  /** Unsafe! Returns internal array. */

  private final int[] getValue () {
    if ((offset > 0) || (value.length != intLen)) {
      return Arrays.copyOfRange(value, offset, offset + intLen); }
    return value; }

  private final void setValue (final int[] val,
                               final int length) {
    value = val; intLen = length; offset = 0; }

  public final NaturalBEI build () {
    final NaturalBEI u;
    if (isZero()) { u = NaturalBEI.ZERO; }
    else { u = NaturalBEI.unsafe(getValue()); }
    setValue(null,0);
    return u; }

  /** Returns an <code>int[]</code>containing the {@code n}
   * low ints of this number.
   * TODO: wrap with a view object, no copying?
   */
  private final int[] getLower (final int n) {
    if (isZero()) { return NaturalBEI.EMPTY; }
    // TODO: copy here? DANGER!!!
    else if (intLen < n) { return getValue(); }
    else {
      // strip zeros
      int len = n;
      while ((len > 0) && (value[(offset+intLen)-len] == 0)) {
        len--; }
      return
        Arrays.copyOfRange(
          value, (offset+intLen)-len, offset+intLen); } }

  //--------------------------------------------------------------
  /** Makes this number an {@code n}-int number all of whose bits
   * are ones. Used by Burnikel-Ziegler division.
   * @param n number of ints in the {@code value} array
   * @return a number equal to {@code ((1<<(32*n)))-1}
   */

  private final void ones (final int n) {
    if (n > value.length) { value = new int[n]; }
    Arrays.fill(value, -1);
    offset = 0;
    intLen = n; }

  private final void clear () {
    offset = intLen = 0;
    for (int index=0, n=value.length; index < n; index++) {
      value[index] = 0; } }

  private final void reset () { offset = intLen = 0; }

  public final int getLowestSetBit () {
    if (intLen == 0) { return -1; }
    int j, b;
    for (j=intLen-1; (j > 0) && (value[j+offset] == 0); j--) { }
    b = value[j+offset];
    if (b == 0) { return -1; }
    return ((intLen-1-j)<<5) + Integer.numberOfTrailingZeros(b); }

  private static final int loBit (final NaturalBEIBuilder m) {
    return m.getLowestSetBit(); }

  private final void normalize () {
    if (intLen == 0) { offset = 0; return; }
    int index = offset;
    if (value[index] != 0) { return; }
    final int indexBound = index+intLen;
    do { index++; }
    while((index < indexBound) && (value[index] == 0));
    final int numZeros = index - offset;
    intLen -= numZeros;
    offset = (intLen == 0 ?  0 : offset+numZeros); }

  /** Discards all ints whose index is greater than {@code n}.
   */
  private final void keepLower (final int n) {
    if (intLen >= n) { offset += intLen - n; intLen = n; } }

  public final boolean isZero () { return (intLen == 0); }

  //--------------------------------------------------------------
  // bit operations
  //--------------------------------------------------------------

  public final long bitLength () {
    if (intLen == 0) { return 0; }
    return
      (intLen*32L)
      - Integer.numberOfLeadingZeros(value[offset]); }

  //--------------------------------------------------------------
  /** Right shift this NaturalBEIBuilder n bits, where n is
   * less than 32. Assumes that intLen > 0, n > 0 for speed
   */

  private final void primitiveRightShift (final int n) {
    final int[] val = value;
    final int n2 = 32 - n;
    for (int i=(offset+intLen)-1, c=val[i]; i > offset; i--) {
      final int b = c;
      c = val[i-1];
      val[i] = (c << n2) | (b >>> n); }
    val[offset] >>>= n; }

  /** The NaturalBEIBuilder is left in normal form.
   */

  private final void rightShift (final int n) {
    if (intLen == 0) { return; }
    final int nInts = n >>> 5;
    final int nBits = n & 0x1F;
    this.intLen -= nInts;
    if (nBits == 0) { return; }
    final int bitsInHighWord = Numbers.bitLength(value[offset]);
    if (nBits >= bitsInHighWord) {
      this.primitiveLeftShift(32 - nBits);
      this.intLen--; }
    else { primitiveRightShift(nBits); } }

  /** {@code n} can be greater than the length of the number.
   */

  private final void safeRightShift (final int n) {
    if ((n/32) >= intLen) { reset(); }
    else { rightShift(n); } }

  //--------------------------------------------------------------
  /** Left shift this NaturalBEIBuilder n bits, where n is
   * less than 32. Assumes that intLen > 0, n > 0 for speed
   */

  private final void primitiveLeftShift(final int n) {
    final int[] val = value;
    final int n2 = 32 - n;
    for (int i=offset, c=val[i], m=(i+intLen)-1; i < m; i++) {
      final int b = c;
      c = val[i+1];
      val[i] = (b << n) | (c >>> n2); }
    val[(offset+intLen)-1] <<= n; }

  private final void leftShift (final int n) {
    // If there is enough storage space in this NaturalBEIBuilder
    // already the available space will be used. Space to the
    // right of the used ints in the value array is faster to
    // utilize, so the extra space will be taken from the right if
    // possible.
    if (intLen == 0) { return; }
    final int nInts = n >>> 5;
    final int nBits = n&0x1F;
    final int bitsInHighWord = Numbers.bitLength(value[offset]);

    // If shift can be done without moving words, do so
    if (n <= (32-bitsInHighWord)) {
      primitiveLeftShift(nBits); return; }

    int newLen = intLen + nInts +1;
    if (nBits <= (32-bitsInHighWord)) { newLen--; }
    if (value.length < newLen) {
      // The array must grow
      final int[] result = new int[newLen];
      for (int i=0; i < intLen; i++) {
        result[i] = value[offset+i]; }
      setValue(result, newLen); }
    else if ((value.length - offset) >= newLen) {
      // Use space on right
      for(int i=0; i < (newLen - intLen); i++) {
        value[offset+intLen+i] = 0; } }
    else {
      // Must use space on left
      for (int i=0;i<intLen;i++) { value[i] = value[offset+i]; }
      for (int i=intLen; i<newLen; i++) { value[i] = 0; }
      offset = 0; }
    intLen = newLen;
    if (nBits == 0) { return; }
    if (nBits <= (32-bitsInHighWord)) {
      primitiveLeftShift(nBits); }
    else { primitiveRightShift(32 -nBits); } }

  /** {@code n} can be zero.
   */

  private final void safeLeftShift (final int n) {
    if (n > 0) { leftShift(n); } }

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
  /** Adds the contents of two NaturalBEIBuilder objects.The result
   * is placed within this NaturalBEIBuilder. The contents of the
   * addend are not changed.
   */

  private final void add (final NaturalBEIBuilder addend) {
    int x = intLen;
    int y = addend.intLen;
    int resultLen =
      (intLen > addend.intLen ? intLen : addend.intLen);
    int[] result =
      (value.length < resultLen ? new int[resultLen] : value);

    int rstart = result.length-1;
    long sum;
    long carry = 0;

    // Add common parts of both numbers
    while((x > 0) && (y > 0)) {
      x--; y--;
      sum = unsigned(value[x+offset])
        + unsigned(addend.value[y+addend.offset])
        + carry;
      result[rstart--] = (int)sum;
      carry = sum >>> 32; }

    // Add remainder of the longer number
    while(x > 0) {
      x--;
      if ((carry == 0)
        && (result == value)
        && (rstart == (x + offset))) {
        return; }
      sum = unsigned(value[x+offset]) + carry;
      result[rstart--] = (int)sum;
      carry = sum >>> 32; }
    while(y > 0) {
      y--;
      sum = unsigned(addend.value[y+addend.offset]) + carry;
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

    value = result;
    intLen = resultLen;
    offset = result.length - resultLen; }

  /** Adds the value of {@code addend} shifted {@code n} ints to
   * the left. Has the same effect as
   * {@code addend.leftShift(32*ints); add(addend);}
   * but doesn't change the value of {@code addend}.
   */

  private final void addShifted (final NaturalBEIBuilder addend,
                                 final int shift) {
    if (addend.isZero()) { return; }
    int x = intLen;
    int y = addend.intLen + shift;
    int resultLen = (intLen > y ? intLen : y);
    int[] result =
      (value.length < resultLen ? new int[resultLen] : value);

    int rstart = result.length-1;
    long sum;
    long carry = 0;

    // Add common parts of both numbers
    while ((x > 0) && (y > 0)) {
      x--; y--;
      final int bval =
        (y+addend.offset) < addend.value.length
        ? addend.value[y+addend.offset]
          : 0;
        sum = unsigned(value[x+offset]) +
          (unsigned(bval)) + carry;
        result[rstart--] = (int)sum;
        carry = sum >>> 32; }

    // Add remainder of the longer number
    while (x > 0) {
      x--;
      if ((carry == 0)
        && (result == value)
        && (rstart == (x + offset))) {
        return; }
      sum = unsigned(value[x+offset]) + carry;
      result[rstart--] = (int)sum;
      carry = sum >>> 32; }
    while (y > 0) {
      y--;
      final int bval = ((y+addend.offset) < addend.value.length
        ? addend.value[y+addend.offset]
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

    value = result;
    intLen = resultLen;
    offset = result.length - resultLen; }

  /** Like {@link #addShifted(NaturalBEIBuilder, int)} but
   * {@code this.intLen} must not be greater than {@code n}. In
   * other words, concatenates {@code this} and {@code addend}.
   */

  private final void addDisjoint (final NaturalBEIBuilder addend,
                                  final int n) {
    if (addend.isZero()) { return; }
    final int x = intLen;
    int y = addend.intLen + n;
    final int resultLen = (intLen > y ? intLen : y);
    int[] result;
    if (value.length < resultLen) { result = new int[resultLen]; }
    else {
      result = value;
      Arrays.fill(value, offset+intLen, value.length, 0); }
    int rstart = result.length-1;
    // copy from this if needed
    System.arraycopy(value, offset, result, (rstart+1)-x, x);
    y -= x;
    rstart -= x;
    final int len = Math.min(y, addend.value.length-addend.offset);
    System.arraycopy(
      addend.value, addend.offset,
      result, (rstart+1)-y, len);
    // zero the gap
    for (int i=((rstart+1)-y)+len; i < (rstart+1); i++) {
      result[i] = 0; }
    value = result;
    intLen = resultLen;
    offset = result.length - resultLen; }

  /** Adds the low {@code n} ints of {@code addend}.
   */
  private final void addLower (final NaturalBEIBuilder addend,
                               final int n) {
    final NaturalBEIBuilder a = new NaturalBEIBuilder(addend);
    if ((a.offset + a.intLen) >= n) {
      a.offset = (a.offset + a.intLen) - n;
      a.intLen = n; }
    a.normalize();
    add(a); }

  //--------------------------------------------------------------
  // subtraction
  //--------------------------------------------------------------
  /** Subtracts the smaller of this and b from the larger and
   * places the result into this NaturalBEIBuilder.
   */

  private final int subtract (NaturalBEIBuilder b) {
    NaturalBEIBuilder a = this;
    int[] result = value;
    final int sign = a.compareTo(b);
    if (sign == 0) { reset(); return 0; }
    if (sign < 0) {
      final NaturalBEIBuilder tmp = a; a = b; b = tmp; }
    final int resultLen = a.intLen;
    if (result.length < resultLen) { result = new int[resultLen]; }
    long diff = 0;
    int x = a.intLen;
    int y = b.intLen;
    int rstart = result.length - 1;
    // Subtract common parts of both numbers
    while (y > 0) {
      x--; y--;
      diff = unsigned(a.value[x+a.offset])
        - unsigned(b.value[y+b.offset])
        - ((int)-(diff>>32));
      result[rstart--] = (int)diff; }
    // Subtract remainder of longer number
    while (x > 0) {
      x--;
      diff = unsigned(a.value[x+a.offset])
        - ((int)-(diff>>32));
      result[rstart--] = (int) diff; }

    value = result;
    intLen = resultLen;
    offset = value.length - resultLen;
    normalize();
    return sign; }

  //--------------------------------------------------------------
  // division
  //--------------------------------------------------------------
  /** This method is used for division of an n word dividend by a
   * one word divisor. The quotient is placed into quotient. The
   * one word divisor is specified by divisor.
   * @return the remainder of the division.
   */

  private final int divideOneWord (final int divisor,
                                   final NaturalBEIBuilder quotient) {
    final long divisorLong = unsigned(divisor);
    // Special case of one word dividend
    if (intLen == 1) {
      final long dividendValue = unsigned(value[offset]);
      final int q = (int) (dividendValue / divisorLong);
      final int r = (int) (dividendValue - (q * divisorLong));
      quotient.value[0] = q;
      quotient.intLen = (q == 0) ? 0 : 1;
      quotient.offset = 0;
      return r; }

    if (quotient.value.length < intLen) {
      quotient.value = new int[intLen]; }
    quotient.offset = 0;
    quotient.intLen = intLen;

    // Normalize the divisor
    final int shift = Integer.numberOfLeadingZeros(divisor);
    int rem = value[offset];
    long remLong = unsigned(rem);
    if (remLong < divisorLong) { quotient.value[0] = 0; }
    else {
      quotient.value[0] = (int)(remLong / divisorLong);
      rem = (int) (remLong - (quotient.value[0] * divisorLong));
      remLong = unsigned(rem); }
    int xlen = intLen;
    while (--xlen > 0) {
      final long dividendEstimate = (remLong << 32) |
        unsigned(value[(offset + intLen) - xlen]);
      int q;
      if (dividendEstimate >= 0) {
        q = (int) (dividendEstimate / divisorLong);
        rem = (int) (dividendEstimate - (q * divisorLong)); }
      else {
        final long tmp = divWord(dividendEstimate, divisor);
        q = (int) Numbers.loWord(tmp);
        rem = (int) (tmp >>> 32); }
      quotient.value[intLen - xlen] = q;
      remLong = unsigned(rem); }
    quotient.normalize();
    // denormalize
    if (shift > 0) { return rem % divisor; }
    return rem; }

  //--------------------------------------------------------------
  /** A primitive used for division. This method adds in one
   * multiple of the divisor a back to the dividend result at a
   * specified offset. It is used when qhat was estimated too
   * large, and must be adjusted.
   */

  private static final int divadd (final int[] a,
                                   final int[] result,
                                   final int offset) {
    long carry = 0;
    for (int j=a.length-1; j >= 0; j--) {
      final long sum =
        (unsigned(a[j])) + unsigned(result[j+offset]) + carry;
      result[j+offset] = (int)sum;
      carry = sum >>> 32; }
    return (int) carry; }

  /** Divide this NaturalBEIBuilder by the divisor.
   * The quotient will be placed into the provided quotient object
   * and the remainder object is returned.
   */

  private final NaturalBEIBuilder
  divideMagnitude (final NaturalBEIBuilder div,
                   final NaturalBEIBuilder quotient,
                   final boolean needRemainder ) {
    assert div.intLen > 1;
    // D1 normalize the divisor
    final int shift =
      Integer.numberOfLeadingZeros(div.value[div.offset]);
    // Copy divisor value to protect divisor
    final int dlen = div.intLen;
    int[] divisor;
    // Remainder starts as dividend with space for a leading zero
    NaturalBEIBuilder rem;
    if (shift > 0) {
      divisor = new int[dlen];
      copyAndShift(div.value,div.offset,dlen,divisor,0,shift);
      if (Integer.numberOfLeadingZeros(value[offset]) >= shift) {
        final int[] remarr = new int[intLen + 1];
        rem = new NaturalBEIBuilder(remarr);
        rem.intLen = intLen;
        rem.offset = 1;
        copyAndShift(value,offset,intLen,remarr,1,shift); }
      else {
        final int[] remarr = new int[intLen + 2];
        rem = new NaturalBEIBuilder(remarr);
        rem.intLen = intLen+1;
        rem.offset = 1;
        int rFrom = offset;
        int c=0;
        final int n2 = 32 - shift;
        for (int i=1; i < (intLen+1); i++,rFrom++) {
          final int b = c;
          c = value[rFrom];
          remarr[i] = (b << shift) | (c >>> n2); }
        remarr[intLen+1] = c << shift; } }
    else {
      divisor = Arrays.copyOfRange(
        div.value, div.offset, div.offset + div.intLen);
      rem = new NaturalBEIBuilder(new int[intLen + 1]);
      System.arraycopy(value, offset, rem.value, 1, intLen);
      rem.intLen = intLen;
      rem.offset = 1; }

    final int nlen = rem.intLen;

    // Set the quotient size
    final int limit = (nlen - dlen) + 1;
    if (quotient.value.length < limit) {
      quotient.value = new int[limit];
      quotient.offset = 0; }
    quotient.intLen = limit;
    final int[] q = quotient.value;

    // Must insert leading 0 in rem if its length did not change
    if (rem.intLen == nlen) {
      rem.offset = 0;
      rem.value[0] = 0;
      rem.intLen++; }

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
      final int nh = rem.value[j+rem.offset];
      final int nh2 = nh + 0x80000000;
      final int nm = rem.value[j+1+rem.offset];
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
        final long nl = unsigned(rem.value[j+2+rem.offset]);
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
      rem.value[j+rem.offset] = 0;
      final int borrow =
        mulsub(rem.value, divisor, qhat, dlen, j+rem.offset);
      // D5 Test remainder
      if ((borrow + 0x80000000) > nh2) {
        // D6 Add back
        divadd(divisor, rem.value, j+1+rem.offset);
        qhat--; }
      // Store the quotient digit
      q[j] = qhat; } // D7 loop on j

    // D3 Calculate qhat
    // 1st estimate
    int qhat = 0;
    int qrem = 0;
    boolean skipCorrection = false;
    final int nh = rem.value[(limit - 1) + rem.offset];
    final int nh2 = nh + 0x80000000;
    final int nm = rem.value[limit + rem.offset];
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
          unsigned(rem.value[limit + 1 + rem.offset]);
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
      rem.value[(limit - 1) + rem.offset] = 0;
      if(needRemainder) {
        borrow =
          mulsub(
            rem.value,divisor,qhat,dlen,(limit-1)+rem.offset); }
      else {
        borrow =
          mulsubBorrow
          (rem.value,divisor,qhat,dlen,(limit-1)+rem.offset); }
      // D5 Test remainder
      if ((borrow + 0x80000000) > nh2) {
        // D6 Add back
        if(needRemainder) {
          divadd(divisor,rem.value,(limit-1)+1+rem.offset); }
        qhat--; }
      // Store the quotient digit
      q[(limit - 1)] = qhat; }

    if (needRemainder) {
      // D8 denormalize
      if (shift > 0) { rem.rightShift(shift); }
      rem.normalize(); }
    quotient.normalize();
    return needRemainder ? rem : null; }

  //--------------------------------------------------------------

  private static final int KNUTH_POW2_THRESH_LEN = 6;
  private static final int KNUTH_POW2_THRESH_ZEROS = 3;

  /** Calculates the quotient of this div b and places the
   * quotient in the provided NaturalBEIBuilder objects and the
   * remainder object is returned.
   *
   * Uses Algorithm D in Knuth section 4.3.1.
   * Many optimizations to that algorithm have been adapted from
   * the Colin Plumb C library.
   * It special cases one word divisors for speed. The content of
   * b is not changed.
   */

  public final NaturalBEIBuilder
  divideKnuth (final NaturalBEIBuilder b,
               final NaturalBEIBuilder quotient,
               final boolean needRemainder) {
    assert 0 != b.intLen;
    // Dividend is zero
    if (intLen == 0) {
      quotient.intLen = 0;
      quotient.offset = 0;
      return needRemainder ? new NaturalBEIBuilder() : null; }

    final int cmp = compareTo(b);
    // Dividend less than divisor
    if (cmp < 0) {
      quotient.intLen = 0;
      quotient.offset = 0;
      return needRemainder ? new NaturalBEIBuilder(this) : null; }
    // Dividend equal to divisor
    if (cmp == 0) {
      quotient.value[0] = 1;
      quotient.intLen = 1;
      quotient.offset = 0;
      return needRemainder ? new NaturalBEIBuilder() : null; }

    quotient.clear();
    // Special case one word divisor
    if (b.intLen == 1) {
      final int r = divideOneWord(b.value[b.offset], quotient);
      if(needRemainder) {
        if (r == 0) { return new NaturalBEIBuilder(); }
        return new NaturalBEIBuilder(r); }
      return null; }

    // Cancel common powers of two if we're above the
    // KNUTH_POW2_* thresholds
    if (intLen >= KNUTH_POW2_THRESH_LEN) {
      final int trailingZeroBits =
        Math.min(getLowestSetBit(), b.getLowestSetBit());
      if (trailingZeroBits >= (KNUTH_POW2_THRESH_ZEROS*32)) {
        final NaturalBEIBuilder aa = new NaturalBEIBuilder(this);
        final NaturalBEIBuilder bb = new NaturalBEIBuilder(b);
        aa.rightShift(trailingZeroBits);
        bb.rightShift(trailingZeroBits);
        final NaturalBEIBuilder r = aa.divideKnuth(bb,quotient,true);
        r.leftShift(trailingZeroBits);
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

  private final NaturalBEIBuilder
  divide2n1n (final NaturalBEIBuilder b,
              final NaturalBEIBuilder quotient) {
    final int n = b.intLen;

    // step 1: base case
    if (((n%2) != 0) || (n < NaturalBEI.BURNIKEL_ZIEGLER_THRESHOLD)) {
      return divideKnuth(b,quotient,true); }

    // step 2: view this as [a1,a2,a3,a4] where each ai is n/2 ints or less
    final NaturalBEIBuilder aUpper = new NaturalBEIBuilder(this);
    aUpper.safeRightShift(32*(n/2));   // aUpper = [a1,a2,a3]
    keepLower(n/2);   // this = a4

    // step 3: q1=aUpper/b, r1=aUpper%b
    final NaturalBEIBuilder q1 = new NaturalBEIBuilder();
    final NaturalBEIBuilder r1 = aUpper.divide3n2n(b, q1);

    // step 4: quotient=[r1,this]/b, r2=[r1,this]%b
    addDisjoint(r1, n/2);   // this = [r1,this]
    final NaturalBEIBuilder r2 = divide3n2n(b, quotient);

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

  private final NaturalBEIBuilder
  divide3n2n (final NaturalBEIBuilder b,
              final NaturalBEIBuilder quotient) {
    final int n = b.intLen / 2;   // half the length of b in ints

    // step 1: view this as [a1,a2,a3] where each ai is n ints
    // or less; let a12=[a1,a2]
    final NaturalBEIBuilder a12 = new NaturalBEIBuilder(this);
    a12.safeRightShift(32*n);

    // step 2: view b as [b1,b2] where each bi is n ints or less
    final NaturalBEIBuilder b1 = new NaturalBEIBuilder(b);
    b1.safeRightShift(n * 32);
    final int[] b2 = b.getLower(n);
    NaturalBEIBuilder r;
    NaturalBEIBuilder d;
    if (compareShifted(b, n) < 0) {
      // step 3a: if a1<b1, let quotient=a12/b1 and r=a12%b1
      r = a12.divide2n1n(b1, quotient);
      // step 4: d=quotient*b2
      final int[] qu = NaturalBEI.multiply(quotient.getValue(),b2);
      d = NaturalBEIBuilder.valueOf(qu); }
    else {
      // step 3b: if a1>=b1, let quotient=beta^n-1
      //and r=a12-b1*2^n+b1
      quotient.ones(n);
      a12.add(b1);
      b1.leftShift(32*n);
      a12.subtract(b1);
      r = a12;
      // step 4: d=quotient*b2=(b2 << 32*n) - b2
      d = NaturalBEIBuilder.valueOf(b2);
      d.leftShift(32 * n);
      d.subtract(NaturalBEIBuilder.valueOf(b2)); }
    // step 5: r = r*beta^n + a3 - d (paper says a4)
    // However, don't subtract d until after the while loop
    // so r doesn't become negative
    r.leftShift(32 * n);
    r.addLower(this, n);
    // step 6: add b until r>=d
    while (r.compareTo(d) < 0) {
      r.add(b);
      quotient.subtract(NaturalBEIBuilder.ONE); }
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

  public final NaturalBEIBuilder
  divideAndRemainderBurnikelZiegler (final NaturalBEIBuilder b,
                                     final NaturalBEIBuilder quotient) {
    final int r = intLen;
    final int s = b.intLen;

    // Clear the quotient
    quotient.offset = quotient.intLen = 0;
    if (r < s) { return this; }
    // step 1: let m = min{2^k | (2^k)*BURNIKEL_ZIEGLER_THRESHOLD > s}
    final int s0 = s/NaturalBEI.BURNIKEL_ZIEGLER_THRESHOLD;
    final int m = 1 << (32-Integer.numberOfLeadingZeros(s0));

    final int j = ((s+m)-1) / m; // step 2a: j = ceil(s/m)
    final int n = j * m; // step 2b: block length in 32-bit units
    final long n32 = 32L * n; // block length in bits
    // step 3: sigma = max{T | (2^T)*B < beta^n}
    final int sigma = (int) Math.max(0, n32 - b.bitLength());
    final NaturalBEIBuilder bShifted = new NaturalBEIBuilder(b);
    // step 4a: shift b so its length is a multiple of n
    bShifted.safeLeftShift(sigma);
    final NaturalBEIBuilder aShifted = new NaturalBEIBuilder(this);
    // step 4b: shift a by the same amount
    aShifted.safeLeftShift(sigma);

    // step 5: t is the number of blocks needed to accommodate a
    // plus one additional bit
    int t = (int) ((aShifted.bitLength()+n32) / n32);
    if (t < 2) { t = 2; }

    // step 6: conceptually split a into blocks a[t-1], ..., a[0]
    // the most significant block of a
    final NaturalBEIBuilder a1 = aShifted.getBlock(t-1, t, n);

    // step 7: z[t-2] = [a[t-1], a[t-2]]
    // the second to most significant block
    NaturalBEIBuilder z = aShifted.getBlock(t-2, t, n);
    z.addDisjoint(a1, n);   // z[t-2]

    // do schoolbook division on blocks, dividing 2-block numbers
    // by 1-block numbers
    final NaturalBEIBuilder qi = new NaturalBEIBuilder();
    NaturalBEIBuilder ri;
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
    ri.rightShift(sigma);
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
                                   int offset) {
    final long xLong = unsigned(x);
    long carry = 0;
    offset += len;
    for (int j=len-1;j>=0;j--) {
      final long product = (unsigned(a[j])*xLong) + carry;
      final long difference = q[offset] - product;
      q[offset--] = (int)difference;
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
                                         int offset) {
    final long xLong = unsigned(x);
    long carry = 0;
    offset += len;
    for (int j=len-1; j >= 0; j--) {
      final long product =
        ((unsigned(a[j])) * xLong) + carry;
      final long difference = q[offset--] - product;
      carry = (product >>> 32)
        + (((loWord(difference)) >
        (unsigned(~(int)product))) ? 1:0); }
    return (int) carry; }

  /** This method divides a long quantity by an int to estimate
   * qhat for two multi precision numbers. It is used when
   * the signed value of n is less than zero.
   * Returns long value where high 32 bits contain remainder value
   * and low 32 bits contain quotient value.
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

  private final NaturalBEIBuilder
  divide (final NaturalBEIBuilder b,
          final NaturalBEIBuilder quotient,
          final boolean needRemainder) {
    if ((b.intLen < NaturalBEI.BURNIKEL_ZIEGLER_THRESHOLD) ||
      ((intLen - b.intLen) < NaturalBEI.BURNIKEL_ZIEGLER_OFFSET)) {
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

    // Right shift a & b till their last bits equal to 1.
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
  private final int difference (NaturalBEIBuilder b) {
    NaturalBEIBuilder a = this;
    final int sign = a.compareTo(b);
    if (sign == 0) { return 0; }
    if (sign < 0) {
      final NaturalBEIBuilder tmp = a; a = b; b = tmp; }

    long diff = 0;
    int x = a.intLen;
    int y = b.intLen;

    // Subtract common parts of both numbers
    while (y > 0) {
      x--; y--;
      diff =
        unsigned(a.value[a.offset+ x])
        - unsigned(b.value[b.offset+ y])
        - ((int)-(diff>>32));
      a.value[a.offset+x] = (int) diff; }
    // Subtract remainder of longer number
    while (x > 0) {
      x--;
      diff = unsigned(a.value[a.offset+ x]) - ((int)-(diff>>32));
      a.value[a.offset+x] = (int)diff; }
    a.normalize();
    return sign; }

  private final NaturalBEIBuilder binaryGCD(NaturalBEIBuilder v) {
    // Algorithm B from Knuth section 4.5.2
    NaturalBEIBuilder u = this;
    final NaturalBEIBuilder r = new NaturalBEIBuilder();

    // step B1
    final int s1 = u.getLowestSetBit();
    final int s2 = v.getLowestSetBit();
    final int k = (s1 < s2) ? s1 : s2;
    if (k != 0) { u.rightShift(k); v.rightShift(k); }

    // step B2
    final boolean uOdd = (k == s1);
    NaturalBEIBuilder t = uOdd ? v: u;
    int tsign = uOdd ? -1 : 1;

    int lb;
    while ((lb = t.getLowestSetBit()) >= 0) {
      // steps B3 and B4
      t.rightShift(lb);
      // step B5
      if (tsign > 0) { u = t; }
      else { v = t; }

      // Special case one word numbers
      if ((u.intLen < 2) && (v.intLen < 2)) {
        int x = u.value[u.offset];
        final int y = v.value[v.offset];
        x  = binaryGcd(x, y);
        r.value[0] = x;
        r.intLen = 1;
        r.offset = 0;
        if (k > 0) { r.leftShift(k); }
        return r; }

      // step B6
      if ((tsign = u.difference(v)) == 0) { break; }
      t = ((tsign >= 0) ? u : v); }

    if (k > 0) { u.leftShift(k); }
    return u; }

  //-------------------------------------------------------------
  // Use Euclid's algorithm until the numbers are approximately the
  // same length, then use the binary GCD algorithm to find the GCD.

  public final NaturalBEIBuilder hybridGCD (final NaturalBEIBuilder d) {
    NaturalBEIBuilder b = d;
    NaturalBEIBuilder a = this;
    final NaturalBEIBuilder q = new NaturalBEIBuilder();
    while (b.intLen != 0) {
      if (Math.abs(a.intLen - b.intLen) < 2) {
        return a.binaryGCD(b); }
      final NaturalBEIBuilder r = a.divide(b, q, true);
      a = b;
      b = r; }
    return a; }

  // remove common factors as if numerator and denominator
  public static final NaturalBEIBuilder[]
    reduce (final NaturalBEIBuilder n,
            final NaturalBEIBuilder d) {
    final int shift = Math.min(loBit(n),loBit(d));
    if (0 != shift) {
      n.rightShift(shift);
      d.rightShift(shift); }
    //    if (n.equals(d)) {
    //      return new NaturalBEIBuilder[] { ONE, ONE, }; }
    //    if (NaturalBEIBuilder.d.isOne()) {
    //      return new NaturalBEIBuilder[] { n, ONE, }; }
    //    if (NaturalBEI.n.isOne()) {
    //      return new NaturalBEIBuilder[] { ONE, d, }; }
    final NaturalBEIBuilder gcd = n.hybridGCD(d);
    if (gcd.compareTo(ONE) > 0) {
      final NaturalBEIBuilder[] nd = { new NaturalBEIBuilder(),
                                       new NaturalBEIBuilder(), };
      n.divide(gcd,nd[0],false);
      d.divide(gcd,nd[1],false);
      return nd; }
    return new NaturalBEIBuilder[] { n, d, }; }

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

  private final int compareTo (final NaturalBEIBuilder b) {
    final int blen = b.intLen;
    if (intLen < blen) { return -1; }
    if (intLen > blen) { return 1; }
    // TODO: is this faster than unsigned long conversion?
    final int[] bval = b.value;
    for (int i = offset, j = b.offset; i < (intLen + offset); i++, j++) {
      final int b1 = value[i] + 0x80000000;
      final int b2 = bval[j]  + 0x80000000;
      if (b1 < b2) { return -1; }
      if (b1 > b2) { return 1; } }
    return 0; }

  private final int compareShifted (final NaturalBEIBuilder b,
                                    final int ints) {
    final int blen = b.intLen;
    final int alen = intLen - ints;
    if (alen < blen) { return -1; }
    if (alen > blen) { return 1; }
    // TODO: is this faster than unsigned long conversion?
    final int[] bval = b.value;
    for (int i = offset, j = b.offset; i < (alen + offset); i++, j++) {
      final int b1 = value[i] + 0x80000000;
      final int b2 = bval[j]  + 0x80000000;
      if (b1 < b2) { return -1; }
      if (b1 > b2) { return 1; } }
    return 0; }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final String toString () {
    return NaturalBEI.valueOf(getValue()).toString(); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  // DANGER!! no copying
  private NaturalBEIBuilder (final int[] val) {
    value = val;
    intLen = val.length; }

  private NaturalBEIBuilder () { value = new int[1]; intLen = 0; }

  private NaturalBEIBuilder (final NaturalBEIBuilder val) {
    intLen = val.intLen;
    value =
      Arrays.copyOfRange(
        val.value,val.offset,val.offset+intLen); }

  private NaturalBEIBuilder (final int val) {
    value = new int[1];
    intLen = 1;
    value[0] = val; }

  //--------------------------------------------------------------
  /** Returns a {@code NaturalBEIBuilder} containing
   * {@code blockLength} ints from {@code this} number, starting
   * at {@code index*blockLength}.<br/>
   * Used by Burnikel-Ziegler division.
   * @param index the block index
   * @param numBlocks the total number of blocks in {@code this}
   * @param blockLength length of one block in units of 32 bits
   * @return
   */

  private final NaturalBEIBuilder getBlock (final int index,
                                            final int numBlocks,
                                            final int blockLength) {
    final int blockStart = index * blockLength;
    if (blockStart >= intLen) { return new NaturalBEIBuilder(); }
    int blockEnd;
    if (index == (numBlocks-1)) { blockEnd = intLen; }
    else { blockEnd = (index+1) * blockLength; }
    if (blockEnd > intLen) { return new NaturalBEIBuilder(); }
    final int[] newVal =
      Arrays.copyOfRange(
        value,
        (offset+intLen)-blockEnd,
        (offset+intLen)-blockStart);
    return new NaturalBEIBuilder(newVal); }

  //--------------------------------------------------------------

  public static final NaturalBEIBuilder make () {
    return new NaturalBEIBuilder(); }

  //DANGER!!
  public static final NaturalBEIBuilder unsafe (final int[] val) {
    return new NaturalBEIBuilder(val); }

  public static final NaturalBEIBuilder make (final int n) {
    return new NaturalBEIBuilder(new int[n]); }

  public static final NaturalBEIBuilder valueOf (final int[] val) {
    return unsafe(Arrays.copyOf(val,val.length)); }

  //--------------------------------------------------------------

  private static final NaturalBEIBuilder ONE =
    new NaturalBEIBuilder(1);

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

