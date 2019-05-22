package xfp.java.numbers;

import static xfp.java.numbers.UnNatural0.BURNIKEL_ZIEGLER_OFFSET;
import static xfp.java.numbers.UnNatural0.BURNIKEL_ZIEGLER_THRESHOLD;

import java.math.BigInteger;
import java.util.Arrays;

// Don't implement Comparable, because of mutability!

public final class MutableUnNatural {

  //--------------------------------------------------------------
  // mutable state
  //--------------------------------------------------------------
  /** big endian order.
   * may start at an offset.
   * may end early..
   */
  private int[] value;

  /** The number of ints of the value array that are currently used
   * to hold the magnitude of this MutableUnNatural. The magnitude starts
   * at an offset and offset + intLen may be less than value.length.
   */
  private int intLen;

  /**
   * The offset into the value array where the magnitude of this
   * MutableUnNatural begins.
   */
  private int offset = 0;

  public final int[] getMagnitudeArray() {
    if ((offset > 0) || (value.length != intLen)) {
      return Arrays.copyOfRange(value, offset, offset + intLen); }
    return value; }

  //--------------------------------------------------------------

  private static final MutableUnNatural ONE = new MutableUnNatural(1);

  private static final int KNUTH_POW2_THRESH_LEN = 6;

  private static final int KNUTH_POW2_THRESH_ZEROS = 3;

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  MutableUnNatural () { value = new int[1]; intLen = 0; }

  /**
   * Construct a new MutableUnNatural with a magnitude equal to the
   * specified MutableUnNatural.
   */
  private MutableUnNatural (final MutableUnNatural val) {
    intLen = val.intLen;
    value = 
      Arrays.copyOfRange(val.value, val.offset, val.offset + intLen); }

  private MutableUnNatural (final int val) {
    value = new int[1];
    intLen = 1;
    value[0] = val; }

  // DANGER!!
  private MutableUnNatural (final int[] val) {
    value = val;
    intLen = val.length; }

  // DANGER!!
  public static final MutableUnNatural unsafe (final int[] val) {
    return new MutableUnNatural(val); }

  public static final MutableUnNatural valueOf (final int[] val) {
    return unsafe(Arrays.copyOf(val,val.length)); }

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

  //-------------------------------------------------------------
  //
  public final int compareTo (final MutableUnNatural b) {
    final int blen = b.intLen;
    if (intLen < blen) {
      return -1;
    }
    if (intLen > blen) {
      return 1;
    }

    // Add Integer.MIN_VALUE to make the comparison act as unsigned integer
    // comparison.
    final int[] bval = b.value;
    for (int i = offset, j = b.offset; i < (intLen + offset); i++, j++) {
      final int b1 = value[i] + 0x80000000;
      final int b2 = bval[j]  + 0x80000000;
      if (b1 < b2) {
        return -1;
      }
      if (b1 > b2) {
        return 1;
      }
    }
    return 0;
  }

  /**
   * Returns a value equal to what {@code b.leftShift(32*ints); return compare(b);}
   * would return, but doesn't change the value of {@code b}.
   */
  private int compareShifted(final MutableUnNatural b, final int ints) {
    final int blen = b.intLen;
    final int alen = intLen - ints;
    if (alen < blen) {
      return -1;
    }
    if (alen > blen) {
      return 1;
    }

    // Add Integer.MIN_VALUE to make the comparison act as unsigned integer
    // comparison.
    final int[] bval = b.value;
    for (int i = offset, j = b.offset; i < (alen + offset); i++, j++) {
      final int b1 = value[i] + 0x80000000;
      final int b2 = bval[j]  + 0x80000000;
      if (b1 < b2) {
        return -1;
      }
      if (b1 > b2) {
        return 1;
      }
    }
    return 0;
  }

  /**
   * Compare this against half of a MutableUnNatural object (Needed for
   * remainder tests).
   * Assumes no leading unnecessary zeros, which holds for results
   * from divide().
   */
  final int compareHalf(final MutableUnNatural b) {
    final int blen = b.intLen;
    final int len = intLen;
    if (len <= 0) {
      return blen <= 0 ? 0 : -1;
    }
    if (len > blen) {
      return 1;
    }
    if (len < (blen - 1)) {
      return -1;
    }
    final int[] bval = b.value;
    int bstart = 0;
    int carry = 0;
    // Only 2 cases left:len == blen or len == blen - 1
    if (len != blen) { // len == blen - 1
      if (bval[bstart] == 1) {
        ++bstart;
        carry = 0x80000000;
      }
      else {
        return -1;
      }
    }
    // compare values with right-shifted values of b,
    // carrying shifted-out bits across words
    final int[] val = value;
    for (int i = offset, j = bstart; i < (len + offset);) {
      final int bv = bval[j++];
      final long hb = ((bv >>> 1) + carry) & Numbers.UNSIGNED_MASK;
      final long v = val[i++] & Numbers.UNSIGNED_MASK;
      if (v != hb) {
        return v < hb ? -1 : 1;
      }
      carry = (bv & 1) << 31; // carray will be either 0x80000000 or 0
    }
    return carry == 0 ? 0 : -1;
  }

  /**
   * Return the index of the lowest set bit in this MutableUnNatural. If the
   * magnitude of this MutableUnNatural is zero, -1 is returned.
   */
  private final int getLowestSetBit() {
    if (intLen == 0) {
      return -1;
    }
    int j, b;
    for (j=intLen-1; (j > 0) && (value[j+offset] == 0); j--) { }
    b = value[j+offset];
    if (b == 0) {
      return -1;
    }
    return ((intLen-1-j)<<5) + Integer.numberOfTrailingZeros(b);
  }

  /**
   * Ensure that the MutableUnNatural is in normal form, specifically
   * making sure that there are no leading zeros, and that if the
   * magnitude is zero, then intLen is zero.
   */
  final void normalize() {
    if (intLen == 0) {
      offset = 0;
      return;
    }

    int index = offset;
    if (value[index] != 0) {
      return;
    }

    final int indexBound = index+intLen;
    do {
      index++;
    } while((index < indexBound) && (value[index] == 0));

    final int numZeros = index - offset;
    intLen -= numZeros;
    offset = (intLen == 0 ?  0 : offset+numZeros);
  }

  /**
   * Convert this MutableUnNatural into an int array with no leading
   * zeros, of a length that is equal to this MutableUnNatural's intLen.
   */
  int[] toIntArray() {
    final int[] result = new int[intLen];
    for(int i=0; i < intLen; i++) {
      result[i] = value[offset+i];
    }
    return result;
  }

  /**
   * Sets the int at index+offset in this MutableUnNatural to val.
   * This does not get inlined on all platforms so it is not used
   * as often as originally intended.
   */
  void setInt(final int index, final int val) {
    value[offset + index] = val;
  }

  /**
   * Sets this MutableUnNatural's value array to the specified array.
   * The intLen is set to the specified length.
   */
  void setValue(final int[] val, final int length) {
    value = val;
    intLen = length;
    offset = 0;
  }

  /**
   * Sets this MutableUnNatural's value array to a copy of the specified
   * array. The intLen is set to the length of the new array.
   */
  void copyValue(final MutableUnNatural src) {
    final int len = src.intLen;
    if (value.length < len) {
      value = new int[len];
    }
    System.arraycopy(src.value, src.offset, value, 0, len);
    intLen = len;
    offset = 0;
  }

  /**
   * Sets this MutableUnNatural's value array to a copy of the specified
   * array. The intLen is set to the length of the specified array.
   */
  void copyValue(final int[] val) {
    final int len = val.length;
    if (value.length < len) {
      value = new int[len];
    }
    System.arraycopy(val, 0, value, 0, len);
    intLen = len;
    offset = 0;
  }

  /**
   * Returns true iff this MutableUnNatural has a value of one.
   */
  boolean isOne() {
    return (intLen == 1) && (value[offset] == 1);
  }

  /**
   * Returns true iff this MutableUnNatural has a value of zero.
   */
  boolean isZero() {
    return (intLen == 0);
  }

  /**
   * Returns true iff this MutableUnNatural is even.
   */
  boolean isEven() {
    return (intLen == 0) || ((value[(offset + intLen) - 1] & 1) == 0);
  }

  /**
   * Returns true iff this MutableUnNatural is odd.
   */
  boolean isOdd() {
    return isZero() ? false : ((value[(offset + intLen) - 1] & 1) == 1);
  }

  /**
   * Returns true iff this MutableUnNatural is in normal form. A
   * MutableUnNatural is in normal form if it has no leading zeros
   * after the offset, and intLen + offset <= value.length.
   */
  boolean isNormal() {
    if ((intLen + offset) > value.length) {
      return false;
    }
    if (intLen == 0) {
      return true;
    }
    return (value[offset] != 0);
  }

  @Override
  public final String toString() { 
    return UnNatural.valueOf(getMagnitudeArray()).toString(); }

  /**
   * Like {@link #rightShift(int)} but {@code n} can be greater than the length of the number.
   */
  void safeRightShift(final int n) {
    if ((n/32) >= intLen) {
      reset();
    } else {
      rightShift(n);
    }
  }

  /**
   * Right shift this MutableUnNatural n bits. The MutableUnNatural is left
   * in normal form.
   */
  void rightShift(final int n) {
    if (intLen == 0) {
      return;
    }
    final int nInts = n >>> 5;
    final int nBits = n & 0x1F;
    this.intLen -= nInts;
    if (nBits == 0) {
      return;
    }
    final int bitsInHighWord = Numbers.bitLength(value[offset]);
    if (nBits >= bitsInHighWord) {
      this.primitiveLeftShift(32 - nBits);
      this.intLen--;
    } else {
      primitiveRightShift(nBits);
    }
  }

  /**
   * Like {@link #leftShift(int)} but {@code n} can be zero.
   */
  void safeLeftShift(final int n) {
    if (n > 0) {
      leftShift(n);
    }
  }

  /**
   * Left shift this MutableUnNatural n bits.
   */
  void leftShift(final int n) {
    /*
     * If there is enough storage space in this MutableUnNatural already
     * the available space will be used. Space to the right of the used
     * ints in the value array is faster to utilize, so the extra space
     * will be taken from the right if possible.
     */
    if (intLen == 0) {
      return;
    }
    final int nInts = n >>> 5;
    final int nBits = n&0x1F;
    final int bitsInHighWord = Numbers.bitLength(value[offset]);

    // If shift can be done without moving words, do so
    if (n <= (32-bitsInHighWord)) {
      primitiveLeftShift(nBits);
      return;
    }

    int newLen = intLen + nInts +1;
    if (nBits <= (32-bitsInHighWord)) {
      newLen--;
    }
    if (value.length < newLen) {
      // The array must grow
      final int[] result = new int[newLen];
      for (int i=0; i < intLen; i++) {
        result[i] = value[offset+i];
      }
      setValue(result, newLen);
    } else if ((value.length - offset) >= newLen) {
      // Use space on right
      for(int i=0; i < (newLen - intLen); i++) {
        value[offset+intLen+i] = 0;
      }
    } else {
      // Must use space on left
      for (int i=0; i < intLen; i++) {
        value[i] = value[offset+i];
      }
      for (int i=intLen; i < newLen; i++) {
        value[i] = 0;
      }
      offset = 0;
    }
    intLen = newLen;
    if (nBits == 0) {
      return;
    }
    if (nBits <= (32-bitsInHighWord)) {
      primitiveLeftShift(nBits);
    }
    else {
      primitiveRightShift(32 -nBits);
    }
  }

  /**
   * A primitive used for division. This method adds in one multiple of the
   * divisor a back to the dividend result at a specified offset. It is used
   * when qhat was estimated too large, and must be adjusted.
   */
  private static int divadd(final int[] a, final int[] result, final int offset) {
    long carry = 0;

    for (int j=a.length-1; j >= 0; j--) {
      final long sum = (a[j] & Numbers.UNSIGNED_MASK) +
        (result[j+offset] & Numbers.UNSIGNED_MASK) + carry;
      result[j+offset] = (int)sum;
      carry = sum >>> 32;
    }
    return (int)carry;
  }

  /**
   * This method is used for division. It multiplies an n word input a by one
   * word input x, and subtracts the n word product from q. This is needed
   * when subtracting qhat*divisor from dividend.
   */
  private static int mulsub(final int[] q, final int[] a, final int x, final int len, int offset) {
    final long xLong = x & Numbers.UNSIGNED_MASK;
    long carry = 0;
    offset += len;

    for (int j=len-1; j >= 0; j--) {
      final long product = ((a[j] & Numbers.UNSIGNED_MASK) * xLong) + carry;
      final long difference = q[offset] - product;
      q[offset--] = (int)difference;
      carry = (product >>> 32)
        + (((difference & Numbers.UNSIGNED_MASK) >
        (((~(int)product) & Numbers.UNSIGNED_MASK))) ? 1:0);
    }
    return (int)carry;
  }

  /**
   * The method is the same as mulsun, except the fact that q array is not
   * updated, the only result of the method is borrow flag.
   */
  private static int mulsubBorrow(final int[] q, final int[] a, final int x, final int len, int offset) {
    final long xLong = x & Numbers.UNSIGNED_MASK;
    long carry = 0;
    offset += len;
    for (int j=len-1; j >= 0; j--) {
      final long product = ((a[j] & Numbers.UNSIGNED_MASK) * xLong) + carry;
      final long difference = q[offset--] - product;
      carry = (product >>> 32)
        + (((difference & Numbers.UNSIGNED_MASK) >
        (((~(int)product) & Numbers.UNSIGNED_MASK))) ? 1:0);
    }
    return (int)carry;
  }

  /**
   * Right shift this MutableUnNatural n bits, where n is
   * less than 32.
   * Assumes that intLen > 0, n > 0 for speed
   */
  private final void primitiveRightShift(final int n) {
    final int[] val = value;
    final int n2 = 32 - n;
    for (int i=(offset+intLen)-1, c=val[i]; i > offset; i--) {
      final int b = c;
      c = val[i-1];
      val[i] = (c << n2) | (b >>> n);
    }
    val[offset] >>>= n;
  }

  /**
   * Left shift this MutableUnNatural n bits, where n is
   * less than 32.
   * Assumes that intLen > 0, n > 0 for speed
   */
  private final void primitiveLeftShift(final int n) {
    final int[] val = value;
    final int n2 = 32 - n;
    for (int i=offset, c=val[i], m=(i+intLen)-1; i < m; i++) {
      final int b = c;
      c = val[i+1];
      val[i] = (b << n) | (c >>> n2);
    }
    val[(offset+intLen)-1] <<= n;
  }

  /** Returns a {@code BigInteger} equal to the {@code n}
   * low ints of this number.
   */
  private final UnNatural0 getLower (final int n) {
    if (isZero()) { return UnNatural0.ZERO; } 
    else if (intLen < n) {
      return UnNatural0.valueOf(getMagnitudeArray()); } 
    else {
      // strip zeros
      int len = n;
      while ((len > 0) && (value[(offset+intLen)-len] == 0)) {
        len--; }
      return 
        UnNatural0.valueOf(
          Arrays.copyOfRange(
            value, (offset+intLen)-len, offset+intLen)); } } 

  /** Discards all ints whose index is greater than {@code n}.
   */
  private final void keepLower (final int n) {
    if (intLen >= n) { offset += intLen - n; intLen = n; } }

  /**
   * Adds the contents of two MutableUnNatural objects.The result
   * is placed within this MutableUnNatural.
   * The contents of the addend are not changed.
   */
  void add(final MutableUnNatural addend) {
    int x = intLen;
    int y = addend.intLen;
    int resultLen = (intLen > addend.intLen ? intLen : addend.intLen);
    int[] result = (value.length < resultLen ? new int[resultLen] : value);

    int rstart = result.length-1;
    long sum;
    long carry = 0;

    // Add common parts of both numbers
    while((x > 0) && (y > 0)) {
      x--; y--;
      sum = (value[x+offset] & Numbers.UNSIGNED_MASK) +
        (addend.value[y+addend.offset] & Numbers.UNSIGNED_MASK) + carry;
      result[rstart--] = (int)sum;
      carry = sum >>> 32;
    }

    // Add remainder of the longer number
    while(x > 0) {
      x--;
      if ((carry == 0) && (result == value) && (rstart == (x + offset))) {
        return;
      }
      sum = (value[x+offset] & Numbers.UNSIGNED_MASK) + carry;
      result[rstart--] = (int)sum;
      carry = sum >>> 32;
    }
    while(y > 0) {
      y--;
      sum = (addend.value[y+addend.offset] & Numbers.UNSIGNED_MASK) + carry;
      result[rstart--] = (int)sum;
      carry = sum >>> 32;
    }

    if (carry > 0) { // Result must grow in length
      resultLen++;
      if (result.length < resultLen) {
        final int temp[] = new int[resultLen];
        // Result one word longer from carry-out; copy low-order
        // bits into new result.
        System.arraycopy(result, 0, temp, 1, result.length);
        temp[0] = 1;
        result = temp;
      } else {
        result[rstart--] = 1;
      }
    }

    value = result;
    intLen = resultLen;
    offset = result.length - resultLen;
  }

  /**
   * Adds the value of {@code addend} shifted {@code n} ints to the left.
   * Has the same effect as {@code addend.leftShift(32*ints); add(addend);}
   * but doesn't change the value of {@code addend}.
   */
  void addShifted(final MutableUnNatural addend, final int n) {
    if (addend.isZero()) {
      return;
    }

    int x = intLen;
    int y = addend.intLen + n;
    int resultLen = (intLen > y ? intLen : y);
    int[] result = (value.length < resultLen ? new int[resultLen] : value);

    int rstart = result.length-1;
    long sum;
    long carry = 0;

    // Add common parts of both numbers
    while ((x > 0) && (y > 0)) {
      x--; y--;
      final int bval = (y+addend.offset) < addend.value.length ? addend.value[y+addend.offset] : 0;
      sum = (value[x+offset] & Numbers.UNSIGNED_MASK) +
        (bval & Numbers.UNSIGNED_MASK) + carry;
      result[rstart--] = (int)sum;
      carry = sum >>> 32;
    }

    // Add remainder of the longer number
    while (x > 0) {
      x--;
      if ((carry == 0) && (result == value) && (rstart == (x + offset))) {
        return;
      }
      sum = (value[x+offset] & Numbers.UNSIGNED_MASK) + carry;
      result[rstart--] = (int)sum;
      carry = sum >>> 32;
    }
    while (y > 0) {
      y--;
      final int bval = (y+addend.offset) < addend.value.length ? addend.value[y+addend.offset] : 0;
      sum = (bval & Numbers.UNSIGNED_MASK) + carry;
      result[rstart--] = (int)sum;
      carry = sum >>> 32;
    }

    if (carry > 0) { // Result must grow in length
      resultLen++;
      if (result.length < resultLen) {
        final int temp[] = new int[resultLen];
        // Result one word longer from carry-out; copy low-order
        // bits into new result.
        System.arraycopy(result, 0, temp, 1, result.length);
        temp[0] = 1;
        result = temp;
      } else {
        result[rstart--] = 1;
      }
    }

    value = result;
    intLen = resultLen;
    offset = result.length - resultLen;
  }

  /**
   * Like {@link #addShifted(MutableUnNatural, int)} but {@code this.intLen} must
   * not be greater than {@code n}. In other words, concatenates {@code this}
   * and {@code addend}.
   */
  void addDisjoint(final MutableUnNatural addend, final int n) {
    if (addend.isZero()) {
      return;
    }

    final int x = intLen;
    int y = addend.intLen + n;
    final int resultLen = (intLen > y ? intLen : y);
    int[] result;
    if (value.length < resultLen) {
      result = new int[resultLen];
    }
    else {
      result = value;
      Arrays.fill(value, offset+intLen, value.length, 0);
    }

    int rstart = result.length-1;

    // copy from this if needed
    System.arraycopy(value, offset, result, (rstart+1)-x, x);
    y -= x;
    rstart -= x;

    final int len = Math.min(y, addend.value.length-addend.offset);
    System.arraycopy(addend.value, addend.offset, result, (rstart+1)-y, len);

    // zero the gap
    for (int i=((rstart+1)-y)+len; i < (rstart+1); i++) {
      result[i] = 0;
    }

    value = result;
    intLen = resultLen;
    offset = result.length - resultLen;
  }

  /**
   * Adds the low {@code n} ints of {@code addend}.
   */
  void addLower(final MutableUnNatural addend, final int n) {
    final MutableUnNatural a = new MutableUnNatural(addend);
    if ((a.offset + a.intLen) >= n) {
      a.offset = (a.offset + a.intLen) - n;
      a.intLen = n;
    }
    a.normalize();
    add(a);
  }

  /**
   * Subtracts the smaller of this and b from the larger and places the
   * result into this MutableUnNatural.
   */
  int subtract(MutableUnNatural b) {
    MutableUnNatural a = this;

    int[] result = value;
    final int sign = a.compareTo(b);

    if (sign == 0) {
      reset();
      return 0;
    }
    if (sign < 0) {
      final MutableUnNatural tmp = a;
      a = b;
      b = tmp;
    }

    final int resultLen = a.intLen;
    if (result.length < resultLen) {
      result = new int[resultLen];
    }

    long diff = 0;
    int x = a.intLen;
    int y = b.intLen;
    int rstart = result.length - 1;

    // Subtract common parts of both numbers
    while (y > 0) {
      x--; y--;

      diff = (a.value[x+a.offset] & Numbers.UNSIGNED_MASK) -
        (b.value[y+b.offset] & Numbers.UNSIGNED_MASK) - ((int)-(diff>>32));
      result[rstart--] = (int)diff;
    }
    // Subtract remainder of longer number
    while (x > 0) {
      x--;
      diff = (a.value[x+a.offset] & Numbers.UNSIGNED_MASK) - ((int)-(diff>>32));
      result[rstart--] = (int)diff;
    }

    value = result;
    intLen = resultLen;
    offset = value.length - resultLen;
    normalize();
    return sign;
  }

  /** Subtracts the smaller of a and b from the larger and places 
   * the result into the larger. Returns 1 if the answer is in a, 
   * -1 if in b, 0 if no operation was performed.
   */
  private final int difference (MutableUnNatural b) {
    MutableUnNatural a = this;
    final int sign = a.compareTo(b);
    if (sign == 0) { return 0; }
    if (sign < 0) { 
      final MutableUnNatural tmp = a; a = b; b = tmp; }

    long diff = 0;
    int x = a.intLen;
    int y = b.intLen;

    // Subtract common parts of both numbers
    while (y > 0) {
      x--; y--;
      diff = (a.value[a.offset+ x] & Numbers.UNSIGNED_MASK) -
        (b.value[b.offset+ y] & Numbers.UNSIGNED_MASK) - ((int)-(diff>>32));
      a.value[a.offset+x] = (int)diff; }
    // Subtract remainder of longer number
    while (x > 0) {
      x--;
      diff = (a.value[a.offset+ x] & Numbers.UNSIGNED_MASK) - ((int)-(diff>>32));
      a.value[a.offset+x] = (int)diff; }

    a.normalize();
    return sign; }

  /**
   * Multiply the contents of two MutableUnNatural objects. The result is
   * placed into MutableUnNatural z. The contents of y are not changed.
   */
  void multiply(final MutableUnNatural y, final MutableUnNatural z) {
    final int xLen = intLen;
    final int yLen = y.intLen;
    final int newLen = xLen + yLen;

    // Put z into an appropriate state to receive product
    if (z.value.length < newLen) {
      z.value = new int[newLen];
    }
    z.offset = 0;
    z.intLen = newLen;

    // The first iteration is hoisted out of the loop to avoid extra add
    long carry = 0;
    for (int j=yLen-1, k=(yLen+xLen)-1; j >= 0; j--, k--) {
      final long product = ((y.value[j+y.offset] & Numbers.UNSIGNED_MASK) *
        (value[(xLen-1)+offset] & Numbers.UNSIGNED_MASK)) + carry;
      z.value[k] = (int)product;
      carry = product >>> 32;
    }
    z.value[xLen-1] = (int)carry;

    // Perform the multiplication word by word
    for (int i = xLen-2; i >= 0; i--) {
      carry = 0;
      for (int j=yLen-1, k=yLen+i; j >= 0; j--, k--) {
        final long product = ((y.value[j+y.offset] & Numbers.UNSIGNED_MASK) *
          (value[i+offset] & Numbers.UNSIGNED_MASK)) +
          (z.value[k] & Numbers.UNSIGNED_MASK) + carry;
        z.value[k] = (int)product;
        carry = product >>> 32;
      }
      z.value[i] = (int)carry;
    }

    // Remove leading zeros from product
    z.normalize();
  }

  /**
   * Multiply the contents of this MutableUnNatural by the word y. The
   * result is placed into z.
   */
  void mul(final int y, final MutableUnNatural z) {
    if (y == 1) {
      z.copyValue(this);
      return;
    }

    if (y == 0) {
      z.clear();
      return;
    }

    // Perform the multiplication word by word
    final long ylong = y & Numbers.UNSIGNED_MASK;
    final int[] zval = (z.value.length < (intLen+1) ? new int[intLen + 1]
      : z.value);
    long carry = 0;
    for (int i = intLen-1; i >= 0; i--) {
      final long product = (ylong * (value[i+offset] & Numbers.UNSIGNED_MASK)) + carry;
      zval[i+1] = (int)product;
      carry = product >>> 32;
    }

    if (carry == 0) {
      z.offset = 1;
      z.intLen = intLen;
    } else {
      z.offset = 0;
      z.intLen = intLen + 1;
      zval[0] = (int)carry;
    }
    z.value = zval;
  }

  /**
   * This method is used for division of an n word dividend by a one word
   * divisor. The quotient is placed into quotient. The one word divisor is
   * specified by divisor.
   *
   * @return the remainder of the division is returned.
   *
   */
  int divideOneWord(final int divisor, final MutableUnNatural quotient) {
    final long divisorLong = divisor & Numbers.UNSIGNED_MASK;

    // Special case of one word dividend
    if (intLen == 1) {
      final long dividendValue = value[offset] & Numbers.UNSIGNED_MASK;
      final int q = (int) (dividendValue / divisorLong);
      final int r = (int) (dividendValue - (q * divisorLong));
      quotient.value[0] = q;
      quotient.intLen = (q == 0) ? 0 : 1;
      quotient.offset = 0;
      return r;
    }

    if (quotient.value.length < intLen) {
      quotient.value = new int[intLen];
    }
    quotient.offset = 0;
    quotient.intLen = intLen;

    // Normalize the divisor
    final int shift = Integer.numberOfLeadingZeros(divisor);

    int rem = value[offset];
    long remLong = rem & Numbers.UNSIGNED_MASK;
    if (remLong < divisorLong) {
      quotient.value[0] = 0;
    } else {
      quotient.value[0] = (int)(remLong / divisorLong);
      rem = (int) (remLong - (quotient.value[0] * divisorLong));
      remLong = rem & Numbers.UNSIGNED_MASK;
    }
    int xlen = intLen;
    while (--xlen > 0) {
      final long dividendEstimate = (remLong << 32) |
        (value[(offset + intLen) - xlen] & Numbers.UNSIGNED_MASK);
      int q;
      if (dividendEstimate >= 0) {
        q = (int) (dividendEstimate / divisorLong);
        rem = (int) (dividendEstimate - (q * divisorLong));
      } else {
        final long tmp = divWord(dividendEstimate, divisor);
        q = (int) (tmp & Numbers.UNSIGNED_MASK);
        rem = (int) (tmp >>> 32);
      }
      quotient.value[intLen - xlen] = q;
      remLong = rem & Numbers.UNSIGNED_MASK;
    }

    quotient.normalize();
    // Unnormalize
    if (shift > 0) {
      return rem % divisor;
    }
    return rem;
  }

  /**
   * Calculates the quotient of this div b and places the quotient in the
   * provided MutableUnNatural objects and the remainder object is returned.
   *
   */
  MutableUnNatural divide (final MutableUnNatural b, 
                           final MutableUnNatural quotient) {
    return divide(b,quotient,true);
  }

  MutableUnNatural divide (final MutableUnNatural b, 
                           final MutableUnNatural quotient, 
                           final boolean needRemainder) {
    if ((b.intLen < BURNIKEL_ZIEGLER_THRESHOLD) ||
      ((intLen - b.intLen) < BURNIKEL_ZIEGLER_OFFSET)) {
      return divideKnuth(b, quotient, needRemainder);
    }
    return divideAndRemainderBurnikelZiegler(b, quotient);
  }

  /**
   * @see #divideKnuth(MutableUnNatural, MutableUnNatural, boolean)
   */
  MutableUnNatural divideKnuth(final MutableUnNatural b, final MutableUnNatural quotient) {
    return divideKnuth(b,quotient,true);
  }

  /** Calculates the quotient of this div b and places the 
   * quotient in the provided MutableUnNatural objects and the 
   * remainder object is returned.
   *
   * Uses Algorithm D in Knuth section 4.3.1.
   * Many optimizations to that algorithm have been adapted from 
   * the Colin Plumb C library.
   * It special cases one word divisors for speed. The content of 
   * b is not changed.
   */
  public final MutableUnNatural 
  divideKnuth (final MutableUnNatural b, 
               final MutableUnNatural quotient, 
               final boolean needRemainder) {
    assert 0 != b.intLen;
    // Dividend is zero
    if (intLen == 0) {
      quotient.intLen = 0;
      quotient.offset = 0;
      return needRemainder ? new MutableUnNatural() : null; }

    final int cmp = compareTo(b);
    // Dividend less than divisor
    if (cmp < 0) {
      quotient.intLen = 0;
      quotient.offset = 0;
      return needRemainder ? new MutableUnNatural(this) : null; }
    // Dividend equal to divisor
    if (cmp == 0) {
      quotient.value[0] = 1;
      quotient.intLen = 1;
      quotient.offset = 0;
      return needRemainder ? new MutableUnNatural() : null; }

    quotient.clear();
    // Special case one word divisor
    if (b.intLen == 1) {
      final int r = divideOneWord(b.value[b.offset], quotient);
      if(needRemainder) {
        if (r == 0) { return new MutableUnNatural(); }
        return new MutableUnNatural(r); }
      return null; }

    // Cancel common powers of two if we're above the 
    // KNUTH_POW2_* thresholds
    if (intLen >= KNUTH_POW2_THRESH_LEN) {
      final int trailingZeroBits = 
        Math.min(getLowestSetBit(), b.getLowestSetBit());
      if (trailingZeroBits >= (KNUTH_POW2_THRESH_ZEROS*32)) {
        final MutableUnNatural aa = new MutableUnNatural(this);
        final MutableUnNatural bb = new MutableUnNatural(b);
        aa.rightShift(trailingZeroBits);
        bb.rightShift(trailingZeroBits);
        final MutableUnNatural r = aa.divideKnuth(bb, quotient);
        r.leftShift(trailingZeroBits);
        return r; } }

    return divideMagnitude(b, quotient, needRemainder); }

  /**
   * Computes {@code this/b} and {@code this%b} using the
   * <a href="http://cr.yp.to/bib/1998/burnikel.ps"> Burnikel-Ziegler algorithm</a>.
   * This method implements algorithm 3 from pg. 9 of the Burnikel-Ziegler paper.
   * The parameter beta was chosen to b 2<sup>32</sup> so almost all shifts are
   * multiples of 32 bits.<br/>
   * {@code this} and {@code b} must be nonnegative.
   * @param b the divisor
   * @param quotient output parameter for {@code this/b}
   * @return the remainder
   */
  public final MutableUnNatural 
  divideAndRemainderBurnikelZiegler (final MutableUnNatural b, 
                                     final MutableUnNatural quotient) {
    final int r = intLen;
    final int s = b.intLen;

    // Clear the quotient
    quotient.offset = quotient.intLen = 0;

    if (r < s) {
      return this;
    }
    // step 1: let m = min{2^k | (2^k)*BURNIKEL_ZIEGLER_THRESHOLD > s}
    final int m = 1 << (32-Integer.numberOfLeadingZeros(s/BURNIKEL_ZIEGLER_THRESHOLD));

    final int j = ((s+m)-1) / m;      // step 2a: j = ceil(s/m)
    final int n = j * m;            // step 2b: block length in 32-bit units
    final long n32 = 32L * n;         // block length in bits
    final int sigma = (int) Math.max(0, n32 - b.bitLength());   // step 3: sigma = max{T | (2^T)*B < beta^n}
    final MutableUnNatural bShifted = new MutableUnNatural(b);
    bShifted.safeLeftShift(sigma);   // step 4a: shift b so its length is a multiple of n
    final MutableUnNatural aShifted = new MutableUnNatural (this);
    aShifted.safeLeftShift(sigma);     // step 4b: shift a by the same amount

    // step 5: t is the number of blocks needed to accommodate a plus one additional bit
    int t = (int) ((aShifted.bitLength()+n32) / n32);
    if (t < 2) {
      t = 2;
    }

    // step 6: conceptually split a into blocks a[t-1], ..., a[0]
    final MutableUnNatural a1 = aShifted.getBlock(t-1, t, n);   // the most significant block of a

    // step 7: z[t-2] = [a[t-1], a[t-2]]
    MutableUnNatural z = aShifted.getBlock(t-2, t, n);    // the second to most significant block
    z.addDisjoint(a1, n);   // z[t-2]

    // do schoolbook division on blocks, dividing 2-block numbers by 1-block numbers
    final MutableUnNatural qi = new MutableUnNatural();
    MutableUnNatural ri;
    for (int i=t-2; i > 0; i--) {
      // step 8a: compute (qi,ri) such that z=b*qi+ri
      ri = z.divide2n1n(bShifted, qi);

      // step 8b: z = [ri, a[i-1]]
      z = aShifted.getBlock(i-1, t, n);   // a[i-1]
      z.addDisjoint(ri, n);
      quotient.addShifted(qi, i*n);   // update q (part of step 9)
    }
    // final iteration of step 8: do the loop one more time for i=0 but leave z unchanged
    ri = z.divide2n1n(bShifted, qi);
    quotient.add(qi);

    ri.rightShift(sigma);   // step 9: a and b were shifted, so shift back
    return ri;
  }

  /**
   * This method implements algorithm 1 from pg. 4 of the Burnikel-Ziegler paper.
   * It divides a 2n-digit number by a n-digit number.<br/>
   * The parameter beta is 2<sup>32</sup> so all shifts are multiples of 32 bits.
   * <br/>
   * {@code this} must be a nonnegative number such that {@code this.bitLength() <= 2*b.bitLength()}
   * @param b a positive number such that {@code b.bitLength()} is even
   * @param quotient output parameter for {@code this/b}
   * @return {@code this%b}
   */
  private MutableUnNatural divide2n1n(final MutableUnNatural b, final MutableUnNatural quotient) {
    final int n = b.intLen;

    // step 1: base case
    if (((n%2) != 0) || (n < BURNIKEL_ZIEGLER_THRESHOLD)) {
      return divideKnuth(b, quotient);
    }

    // step 2: view this as [a1,a2,a3,a4] where each ai is n/2 ints or less
    final MutableUnNatural aUpper = new MutableUnNatural(this);
    aUpper.safeRightShift(32*(n/2));   // aUpper = [a1,a2,a3]
    keepLower(n/2);   // this = a4

    // step 3: q1=aUpper/b, r1=aUpper%b
    final MutableUnNatural q1 = new MutableUnNatural();
    final MutableUnNatural r1 = aUpper.divide3n2n(b, q1);

    // step 4: quotient=[r1,this]/b, r2=[r1,this]%b
    addDisjoint(r1, n/2);   // this = [r1,this]
    final MutableUnNatural r2 = divide3n2n(b, quotient);

    // step 5: let quotient=[q1,quotient] and return r2
    quotient.addDisjoint(q1, n/2);
    return r2;
  }

  /**
   * This method implements algorithm 2 from pg. 5 of the Burnikel-Ziegler paper.
   * It divides a 3n-digit number by a 2n-digit number.<br/>
   * The parameter beta is 2<sup>32</sup> so all shifts are multiples of 32 bits.<br/>
   * <br/>
   * {@code this} must be a nonnegative number such that {@code 2*this.bitLength() <= 3*b.bitLength()}
   * @param quotient output parameter for {@code this/b}
   * @return {@code this%b}
   */
  private MutableUnNatural divide3n2n (final MutableUnNatural b, 
                                       final MutableUnNatural quotient) {
    final int n = b.intLen / 2;   // half the length of b in ints

    // step 1: view this as [a1,a2,a3] where each ai is n ints or less; let a12=[a1,a2]
    final MutableUnNatural a12 = new MutableUnNatural(this);
    a12.safeRightShift(32*n);

    // step 2: view b as [b1,b2] where each bi is n ints or less
    final MutableUnNatural b1 = new MutableUnNatural(b);
    b1.safeRightShift(n * 32);
    final UnNatural0 b2 = b.getLower(n);

    MutableUnNatural r;
    MutableUnNatural d;
    if (compareShifted(b, n) < 0) {
      // step 3a: if a1<b1, let quotient=a12/b1 and r=a12%b1
      r = a12.divide2n1n(b1, quotient);

      // step 4: d=quotient*b2
      final UnNatural0 qu = UnNatural0.valueOf(quotient.getMagnitudeArray());
      d = MutableUnNatural.valueOf(qu.multiply(b2)._mag);
    } else {
      // step 3b: if a1>=b1, let quotient=beta^n-1 and r=a12-b1*2^n+b1
      quotient.ones(n);
      a12.add(b1);
      b1.leftShift(32*n);
      a12.subtract(b1);
      r = a12;

      // step 4: d=quotient*b2=(b2 << 32*n) - b2
      d = MutableUnNatural.valueOf(b2._mag);
      d.leftShift(32 * n);
      d.subtract(MutableUnNatural.valueOf(b2._mag)); }

    // step 5: r = r*beta^n + a3 - d (paper says a4)
    // However, don't subtract d until after the while loop so r doesn't become negative
    r.leftShift(32 * n);
    r.addLower(this, n);

    // step 6: add b until r>=d
    while (r.compareTo(d) < 0) {
      r.add(b);
      quotient.subtract(MutableUnNatural.ONE); }
    r.subtract(d);
    return r; }

  /**
   * Returns a {@code MutableUnNatural} containing {@code blockLength} ints from
   * {@code this} number, starting at {@code index*blockLength}.<br/>
   * Used by Burnikel-Ziegler division.
   * @param index the block index
   * @param numBlocks the total number of blocks in {@code this} number
   * @param blockLength length of one block in units of 32 bits
   * @return
   */
  private MutableUnNatural getBlock(final int index, final int numBlocks, final int blockLength) {
    final int blockStart = index * blockLength;
    if (blockStart >= intLen) {
      return new MutableUnNatural();
    }

    int blockEnd;
    if (index == (numBlocks-1)) {
      blockEnd = intLen;
    } else {
      blockEnd = (index+1) * blockLength;
    }
    if (blockEnd > intLen) {
      return new MutableUnNatural();
    }

    final int[] newVal = 
      Arrays.copyOfRange(
        value, 
        (offset+intLen)-blockEnd, 
        (offset+intLen)-blockStart);
    return new MutableUnNatural(newVal);
  }

  /** @see BigInteger#bitLength() */
  private final long bitLength() {
    if (intLen == 0) { return 0; }
    return (intLen*32L) - Integer.numberOfLeadingZeros(value[offset]); }

  //  /** Internally used  to calculate the quotient of this div v and
  //   * places the quotient in the provided MutableUnNatural object 
  //   * and the remainder is returned.
  //   *
  //   * @return the remainder of the division will be returned.
  //   */
  //  private final long divide (long v, final MutableUnNatural quotient) {
  //    assert 0 != v;
  //    if (intLen == 0) {
  //      quotient.intLen = quotient.offset = 0;
  //      return 0; }
  //    if (v < 0) { v = -v; }
  //
  //    final int d = (int) (v >>> 32);
  //    quotient.clear();
  //    // Special case on word divisor
  //    if (d == 0) {
  //      return 
  //        divideOneWord((int)v, quotient) & Numbers.UNSIGNED_MASK; }
  //    return divideLongMagnitude(v, quotient).toLong(); }

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

  /** Divide this MutableUnNatural by the divisor.
   * The quotient will be placed into the provided quotient object
   * and the remainder object is returned.
   */
  private final MutableUnNatural divideMagnitude (final MutableUnNatural div,
                                                  final MutableUnNatural quotient,
                                                  final boolean needRemainder ) {
    // assert div.intLen > 1
    // D1 normalize the divisor
    final int shift = Integer.numberOfLeadingZeros(div.value[div.offset]);
    // Copy divisor value to protect divisor
    final int dlen = div.intLen;
    int[] divisor;
    MutableUnNatural rem; // Remainder starts as dividend with space for a leading zero
    if (shift > 0) {
      divisor = new int[dlen];
      copyAndShift(div.value,div.offset,dlen,divisor,0,shift);
      if (Integer.numberOfLeadingZeros(value[offset]) >= shift) {
        final int[] remarr = new int[intLen + 1];
        rem = new MutableUnNatural(remarr);
        rem.intLen = intLen;
        rem.offset = 1;
        copyAndShift(value,offset,intLen,remarr,1,shift);
      } else {
        final int[] remarr = new int[intLen + 2];
        rem = new MutableUnNatural(remarr);
        rem.intLen = intLen+1;
        rem.offset = 1;
        int rFrom = offset;
        int c=0;
        final int n2 = 32 - shift;
        for (int i=1; i < (intLen+1); i++,rFrom++) {
          final int b = c;
          c = value[rFrom];
          remarr[i] = (b << shift) | (c >>> n2);
        }
        remarr[intLen+1] = c << shift;
      }
    } else {
      divisor = Arrays.copyOfRange(div.value, div.offset, div.offset + div.intLen);
      rem = new MutableUnNatural(new int[intLen + 1]);
      System.arraycopy(value, offset, rem.value, 1, intLen);
      rem.intLen = intLen;
      rem.offset = 1;
    }

    final int nlen = rem.intLen;

    // Set the quotient size
    final int limit = (nlen - dlen) + 1;
    if (quotient.value.length < limit) {
      quotient.value = new int[limit];
      quotient.offset = 0;
    }
    quotient.intLen = limit;
    final int[] q = quotient.value;


    // Must insert leading 0 in rem if its length did not change
    if (rem.intLen == nlen) {
      rem.offset = 0;
      rem.value[0] = 0;
      rem.intLen++;
    }

    final int dh = divisor[0];
    final long dhLong = dh & Numbers.UNSIGNED_MASK;
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
        skipCorrection = (qrem + 0x80000000) < nh2;
      } else {
        final long nChunk = (((long)nh) << 32) | (nm & Numbers.UNSIGNED_MASK);
        if (nChunk >= 0) {
          qhat = (int) (nChunk / dhLong);
          qrem = (int) (nChunk - (qhat * dhLong));
        } else {
          final long tmp = divWord(nChunk, dh);
          qhat = (int) (tmp & Numbers.UNSIGNED_MASK);
          qrem = (int) (tmp >>> 32);
        }
      }

      if (qhat == 0) {
        continue;
      }

      if (!skipCorrection) { // Correct qhat
        final long nl = rem.value[j+2+rem.offset] & Numbers.UNSIGNED_MASK;
        long rs = ((qrem & Numbers.UNSIGNED_MASK) << 32) | nl;
        long estProduct = (dl & Numbers.UNSIGNED_MASK) * (qhat & Numbers.UNSIGNED_MASK);

        if (unsignedLongCompare(estProduct, rs)) {
          qhat--;
          qrem = (int)((qrem & Numbers.UNSIGNED_MASK) + dhLong);
          if ((qrem & Numbers.UNSIGNED_MASK) >=  dhLong) {
            estProduct -= (dl & Numbers.UNSIGNED_MASK);
            rs = ((qrem & Numbers.UNSIGNED_MASK) << 32) | nl;
            if (unsignedLongCompare(estProduct, rs)) {
              qhat--;
            }
          }
        }
      }

      // D4 Multiply and subtract
      rem.value[j+rem.offset] = 0;
      final int borrow = mulsub(rem.value, divisor, qhat, dlen, j+rem.offset);

      // D5 Test remainder
      if ((borrow + 0x80000000) > nh2) {
        // D6 Add back
        divadd(divisor, rem.value, j+1+rem.offset);
        qhat--;
      }

      // Store the quotient digit
      q[j] = qhat;
    } // D7 loop on j

    // D3 Calculate qhat
    // estimate qhat
    int qhat = 0;
    int qrem = 0;
    boolean skipCorrection = false;
    final int nh = rem.value[(limit - 1) + rem.offset];
    final int nh2 = nh + 0x80000000;
    final int nm = rem.value[limit + rem.offset];

    if (nh == dh) {
      qhat = ~0;
      qrem = nh + nm;
      skipCorrection = (qrem + 0x80000000) < nh2;
    } else {
      final long nChunk = (((long) nh) << 32) | (nm & Numbers.UNSIGNED_MASK);
      if (nChunk >= 0) {
        qhat = (int) (nChunk / dhLong);
        qrem = (int) (nChunk - (qhat * dhLong));
      } else {
        final long tmp = divWord(nChunk, dh);
        qhat = (int) (tmp & Numbers.UNSIGNED_MASK);
        qrem = (int) (tmp >>> 32);
      }
    }
    if (qhat != 0) {
      if (!skipCorrection) { // Correct qhat
        final long nl = rem.value[limit + 1 + rem.offset] & Numbers.UNSIGNED_MASK;
        long rs = ((qrem & Numbers.UNSIGNED_MASK) << 32) | nl;
        long estProduct = (dl & Numbers.UNSIGNED_MASK) * (qhat & Numbers.UNSIGNED_MASK);

        if (unsignedLongCompare(estProduct, rs)) {
          qhat--;
          qrem = (int) ((qrem & Numbers.UNSIGNED_MASK) + dhLong);
          if ((qrem & Numbers.UNSIGNED_MASK) >= dhLong) {
            estProduct -= (dl & Numbers.UNSIGNED_MASK);
            rs = ((qrem & Numbers.UNSIGNED_MASK) << 32) | nl;
            if (unsignedLongCompare(estProduct, rs)) {
              qhat--;
            }
          }
        }
      }


      // D4 Multiply and subtract
      int borrow;
      rem.value[(limit - 1) + rem.offset] = 0;
      if(needRemainder) {
        borrow = mulsub(rem.value, divisor, qhat, dlen, (limit - 1) + rem.offset);
      }
      else {
        borrow = mulsubBorrow(rem.value, divisor, qhat, dlen, (limit - 1) + rem.offset);
      }

      // D5 Test remainder
      if ((borrow + 0x80000000) > nh2) {
        // D6 Add back
        if(needRemainder) {
          divadd(divisor, rem.value, (limit - 1) + 1 + rem.offset);
        }
        qhat--;
      }

      // Store the quotient digit
      q[(limit - 1)] = qhat;
    }


    if (needRemainder) {
      // D8 Unnormalize
      if (shift > 0) {
        rem.rightShift(shift);
      }
      rem.normalize();
    }
    quotient.normalize();
    return needRemainder ? rem : null;
  }

  //  /** Divide this MutableUnNatural by the divisor represented by 
  //   * positive long value. The quotient will be placed into the 
  //   * provided quotient object & the remainder object is returned.
  //   */
  //  private final MutableUnNatural 
  //  divideLongMagnitude (long ldivisor, 
  //                       final MutableUnNatural quotient) {
  //    // Remainder starts as dividend with space for a leading zero
  //    final MutableUnNatural rem = new MutableUnNatural(new int[intLen + 1]);
  //    System.arraycopy(value, offset, rem.value, 1, intLen);
  //    rem.intLen = intLen;
  //    rem.offset = 1;
  //
  //    final int nlen = rem.intLen;
  //
  //    final int limit = (nlen - 2) + 1;
  //    if (quotient.value.length < limit) {
  //      quotient.value = new int[limit];
  //      quotient.offset = 0;
  //    }
  //    quotient.intLen = limit;
  //    final int[] q = quotient.value;
  //
  //    // D1 normalize the divisor
  //    final int shift = Long.numberOfLeadingZeros(ldivisor);
  //    if (shift > 0) {
  //      ldivisor<<=shift;
  //      rem.leftShift(shift);
  //    }
  //
  //    // Must insert leading 0 in rem if its length did not change
  //    if (rem.intLen == nlen) {
  //      rem.offset = 0;
  //      rem.value[0] = 0;
  //      rem.intLen++;
  //    }
  //
  //    final int dh = (int)(ldivisor >>> 32);
  //    final long dhLong = dh & Numbers.UNSIGNED_MASK;
  //    final int dl = (int)(ldivisor & Numbers.UNSIGNED_MASK);
  //
  //    // D2 Initialize j
  //    for (int j = 0; j < limit; j++) {
  //      // D3 Calculate qhat
  //      // estimate qhat
  //      int qhat = 0;
  //      int qrem = 0;
  //      boolean skipCorrection = false;
  //      final int nh = rem.value[j + rem.offset];
  //      final int nh2 = nh + 0x80000000;
  //      final int nm = rem.value[j + 1 + rem.offset];
  //
  //      if (nh == dh) {
  //        qhat = ~0;
  //        qrem = nh + nm;
  //        skipCorrection = (qrem + 0x80000000) < nh2;
  //      } else {
  //        final long nChunk = (((long) nh) << 32) | (nm & Numbers.UNSIGNED_MASK);
  //        if (nChunk >= 0) {
  //          qhat = (int) (nChunk / dhLong);
  //          qrem = (int) (nChunk - (qhat * dhLong));
  //        } else {
  //          final long tmp = divWord(nChunk, dh);
  //          qhat =(int)(tmp & Numbers.UNSIGNED_MASK);
  //          qrem = (int)(tmp>>>32);
  //        }
  //      }
  //
  //      if (qhat == 0) {
  //        continue;
  //      }
  //
  //      if (!skipCorrection) { // Correct qhat
  //        final long nl = rem.value[j + 2 + rem.offset] & Numbers.UNSIGNED_MASK;
  //        long rs = ((qrem & Numbers.UNSIGNED_MASK) << 32) | nl;
  //        long estProduct = (dl & Numbers.UNSIGNED_MASK) * (qhat & Numbers.UNSIGNED_MASK);
  //
  //        if (unsignedLongCompare(estProduct, rs)) {
  //          qhat--;
  //          qrem = (int) ((qrem & Numbers.UNSIGNED_MASK) + dhLong);
  //          if ((qrem & Numbers.UNSIGNED_MASK) >= dhLong) {
  //            estProduct -= (dl & Numbers.UNSIGNED_MASK);
  //            rs = ((qrem & Numbers.UNSIGNED_MASK) << 32) | nl;
  //            if (unsignedLongCompare(estProduct, rs)) {
  //              qhat--;
  //            }
  //          }
  //        }
  //      }
  //
  //      // D4 Multiply and subtract
  //      rem.value[j + rem.offset] = 0;
  //      final int borrow = mulsubLong(rem.value, dh, dl, qhat,  j + rem.offset);
  //
  //      // D5 Test remainder
  //      if ((borrow + 0x80000000) > nh2) {
  //        // D6 Add back
  //        divaddLong(dh,dl, rem.value, j + 1 + rem.offset);
  //        qhat--;
  //      }
  //
  //      // Store the quotient digit
  //      q[j] = qhat;
  //    } // D7 loop on j
  //
  //    // D8 Unnormalize
  //    if (shift > 0) {
  //      rem.rightShift(shift);
  //    }
  //
  //    quotient.normalize();
  //    rem.normalize();
  //    return rem;
  //  }

  //  /** A primitive used for division by long.
  //   * Specialized version of the method divadd.
  //   * dh is a high part of the divisor, dl is a low part
  //   */
  //  private static final int divaddLong(final int dh, 
  //                                      final int dl, 
  //                                      final int[] result, 
  //                                      final int offset) {
  //    long carry = 0;
  //
  //    long sum = (dl & Numbers.UNSIGNED_MASK) + (result[1+offset] & Numbers.UNSIGNED_MASK);
  //    result[1+offset] = (int)sum;
  //
  //    sum = (dh & Numbers.UNSIGNED_MASK) + (result[offset] & Numbers.UNSIGNED_MASK) + carry;
  //    result[offset] = (int)sum;
  //    carry = (sum >>> 32);
  //    return (int)carry; }

  //  /** This method is used for division by long.
  //   * Specialized version of the method sulsub.
  //   * dh is a high part of the divisor, dl is a low part
  //   */
  //  private static final int mulsubLong(final int[] q, 
  //                                      final int dh, 
  //                                      final int dl, 
  //                                      final int x, 
  //                                      int offset1) {
  //    final long xLong = x & Numbers.UNSIGNED_MASK;
  //    offset1 += 2;
  //    long product = (dl & Numbers.UNSIGNED_MASK) * xLong;
  //    long difference = q[offset1] - product;
  //    q[offset1--] = (int)difference;
  //    long carry = (product >>> 32)
  //      + (((difference & Numbers.UNSIGNED_MASK) >
  //      (((~(int)product) & Numbers.UNSIGNED_MASK))) ? 1:0);
  //    product = ((dh & Numbers.UNSIGNED_MASK) * xLong) + carry;
  //    difference = q[offset1] - product;
  //    q[offset1--] = (int)difference;
  //    carry = (product >>> 32)
  //      + (((difference & Numbers.UNSIGNED_MASK) >
  //      (((~(int)product) & Numbers.UNSIGNED_MASK))) ? 1:0);
  //    return (int)carry; }

  /** Compare two longs as if they were unsigned.
   * Returns true iff one is bigger than two.
   */
  private static final boolean unsignedLongCompare (final long one, 
                                                    final long two) {
    return (one+Long.MIN_VALUE) > (two+Long.MIN_VALUE); }

  /**
   * This method divides a long quantity by an int to estimate
   * qhat for two multi precision numbers. It is used when
   * the signed value of n is less than zero.
   * Returns long value where high 32 bits contain remainder value and
   * low 32 bits contain quotient value.
   */
  private static final long divWord(final long n, final int d) {
    final long dLong = d & Numbers.UNSIGNED_MASK;
    long r;
    long q;
    if (dLong == 1) {
      q = (int)n;
      r = 0;
      return (r << 32) | (q & Numbers.UNSIGNED_MASK);
    }

    // Approximate the quotient and remainder
    q = (n >>> 1) / (dLong >>> 1);
    r = n - (q*dLong);

    // Correct the approximation
    while (r < 0) {
      r += dLong;
      q--;
    }
    while (r >= dLong) {
      r -= dLong;
      q++;
    }
    // n - q*dlong == r && 0 <= r <dLong, hence we're done.
    return (r << 32) | (q & Numbers.UNSIGNED_MASK);
  }

  /**
   * Calculate the integer square root {@code floor(sqrt(this))} where
   * {@code sqrt(.)} denotes the mathematical square root. The contents of
   * {@code this} are <b>not</b> changed. The value of {@code this} is assumed
   * to be non-negative.
   *
   * @implNote The implementation is based on the material in Henry S. Warren,
   * Jr., <i>Hacker's Delight (2nd ed.)</i> (Addison Wesley, 2013), 279-282.
   *
   * @throws ArithmeticException if the value returned by {@code bitLength()}
   * overflows the range of {@code int}.
   * @return the integer square root of {@code this}
   * @since 9
   */
  MutableUnNatural sqrt() {
    // Special cases.
    if (this.isZero()) {
      return new MutableUnNatural(0);
    } else if ((this.value.length == 1)
      && ((this.value[0] & Numbers.UNSIGNED_MASK) < 4)) { // result is unity
      return ONE;
    }

    if (bitLength() <= 63) {
      // Initial estimate is the square root of the positive long value.
      final long v = UnNatural0.valueOf(this.value).longValue();
      long xk = (long)Math.floor(Math.sqrt(v));

      // Refine the estimate.
      do {
        final long xk1 = (xk + (v/xk))/2;

        // Terminate when non-decreasing.
        if (xk1 >= xk) {
          return new MutableUnNatural(new int[] {
                                                 (int)(xk >>> 32), (int)(xk & Numbers.UNSIGNED_MASK)
          });
        }

        xk = xk1;
      } while (true);
    }
    // Obtain the bitLength > 63.
    final int bitLength = (int) this.bitLength();
    if (bitLength != this.bitLength()) {
      throw new ArithmeticException("bitLength() integer overflow");
    }

    // Determine an even valued right shift into positive long range.
    int shift = bitLength - 63;
    if ((shift % 2) == 1) {
      shift++;
    }

    // Shift the value into positive long range.
    MutableUnNatural xk = new MutableUnNatural(this);
    xk.rightShift(shift);
    xk.normalize();

    // Use the square root of the shifted value as an approximation.
    final double d = UnNatural0.valueOf(xk.value).doubleValue();
    final UnNatural0 bi = UnNatural0.valueOf((long)Math.ceil(Math.sqrt(d)));
    xk = new MutableUnNatural(bi._mag);

    // Shift the approximate square root back into the original range.
    xk.leftShift(shift / 2);

    // Refine the estimate.
    final MutableUnNatural xk1 = new MutableUnNatural();
    do {
      // xk1 = (xk + n/xk)/2
      this.divide(xk, xk1, false);
      xk1.add(xk);
      xk1.rightShift(1);

      // Terminate when non-decreasing.
      if (xk1.compareTo(xk) >= 0) { return xk; }

      // xk = xk1
      xk.copyValue(xk1);

      xk1.reset();
    } while (true); }

  /**
   * Calculate GCD of this and b. This and b are changed by the computation.
   */
  MutableUnNatural hybridGCD(MutableUnNatural b) {
    // Use Euclid's algorithm until the numbers are approximately the
    // same length, then use the binary GCD algorithm to find the GCD.
    MutableUnNatural a = this;
    final MutableUnNatural q = new MutableUnNatural();

    while (b.intLen != 0) {
      if (Math.abs(a.intLen - b.intLen) < 2) {
        return a.binaryGCD(b);
      }

      final MutableUnNatural r = a.divide(b, q);
      a = b;
      b = r;
    }
    return a;
  }

  /**
   * Calculate GCD of this and v.
   * Assumes that this and v are not zero.
   */
  private MutableUnNatural binaryGCD(MutableUnNatural v) {
    // Algorithm B from Knuth section 4.5.2
    MutableUnNatural u = this;
    final MutableUnNatural r = new MutableUnNatural();

    // step B1
    final int s1 = u.getLowestSetBit();
    final int s2 = v.getLowestSetBit();
    final int k = (s1 < s2) ? s1 : s2;
    if (k != 0) {
      u.rightShift(k);
      v.rightShift(k);
    }

    // step B2
    final boolean uOdd = (k == s1);
    MutableUnNatural t = uOdd ? v: u;
    int tsign = uOdd ? -1 : 1;

    int lb;
    while ((lb = t.getLowestSetBit()) >= 0) {
      // steps B3 and B4
      t.rightShift(lb);
      // step B5
      if (tsign > 0) {
        u = t;
      }
      else {
        v = t;
      }

      // Special case one word numbers
      if ((u.intLen < 2) && (v.intLen < 2)) {
        int x = u.value[u.offset];
        final int y = v.value[v.offset];
        x  = binaryGcd(x, y);
        r.value[0] = x;
        r.intLen = 1;
        r.offset = 0;
        if (k > 0) {
          r.leftShift(k);
        }
        return r;
      }

      // step B6
      if ((tsign = u.difference(v)) == 0) {
        break;
      }
      t = (tsign >= 0) ? u : v;
    }

    if (k > 0) {
      u.leftShift(k);
    }
    return u;
  }

  /**
   * Calculate GCD of a and b interpreted as unsigned integers.
   */
  static int binaryGcd(int a, int b) {
    if (b == 0) {
      return a;
    }
    if (a == 0) {
      return b;
    }

    // Right shift a & b till their last bits equal to 1.
    final int aZeros = Integer.numberOfTrailingZeros(a);
    final int bZeros = Integer.numberOfTrailingZeros(b);
    a >>>= aZeros;
    b >>>= bZeros;

    final int t = (aZeros < bZeros ? aZeros : bZeros);

    while (a != b) {
      if ((a+0x80000000) > (b+0x80000000)) {  // a > b as unsigned
        a -= b;
        a >>>= Integer.numberOfTrailingZeros(a);
      } else {
        b -= a;
        b >>>= Integer.numberOfTrailingZeros(b);
      }
    }
    return a<<t;
  }



  /*
   * Calculate the multiplicative inverse of this mod 2^k.
   */
  MutableUnNatural modInverseMP2(final int k) {
    if (isEven()) {
      throw new ArithmeticException("Non-invertible. (GCD != 1)");
    }

    if (k > 64) {
      return euclidModInverse(k);
    }

    int t = inverseMod32(value[(offset+intLen)-1]);

    if (k < 33) {
      t = (k == 32 ? t : t & ((1 << k) - 1));
      return new MutableUnNatural(t);
    }

    long pLong = (value[(offset+intLen)-1] & Numbers.UNSIGNED_MASK);
    if (intLen > 1) {
      pLong |=  ((long)value[(offset+intLen)-2] << 32);
    }
    long tLong = t & Numbers.UNSIGNED_MASK;
    tLong = tLong * (2 - (pLong * tLong));  // 1 more Newton iter step
    tLong = (k == 64 ? tLong : tLong & ((1L << k) - 1));

    final MutableUnNatural result = new MutableUnNatural(new int[2]);
    result.value[0] = (int)(tLong >>> 32);
    result.value[1] = (int)tLong;
    result.intLen = 2;
    result.normalize();
    return result;
  }

  /**
   * Returns the multiplicative inverse of val mod 2^32.  Assumes val is odd.
   */
  private static final int inverseMod32(final int val) {
    // Newton's iteration!
    int t = val;
    t *= 2 - (val*t);
    t *= 2 - (val*t);
    t *= 2 - (val*t);
    t *= 2 - (val*t);
    return t;
  }


  /** Uses the extended Euclidean algorithm to compute the modInverse of base
   * mod a modulus that is a power of 2. The modulus is 2^k.
   */
  private final MutableUnNatural euclidModInverse(final int k) {
    MutableUnNatural b = new MutableUnNatural(1);
    b.leftShift(k);
    final MutableUnNatural mod = new MutableUnNatural(b);

    MutableUnNatural a = new MutableUnNatural(this);
    MutableUnNatural q = new MutableUnNatural();
    MutableUnNatural r = b.divide(a, q);

    MutableUnNatural swapper = b;
    // swap b & r
    b = r;
    r = swapper;

    final MutableUnNatural t1 = new MutableUnNatural(q);
    final MutableUnNatural t0 = new MutableUnNatural(1);
    MutableUnNatural temp = new MutableUnNatural();

    while (!b.isOne()) {
      r = a.divide(b, q);

      if (r.intLen == 0) {
        throw new ArithmeticException("BigInteger not invertible.");
      }

      swapper = r;
      a = swapper;

      if (q.intLen == 1) {
        t1.mul(q.value[q.offset], temp);
      }
      else {
        q.multiply(t1, temp);
      }
      swapper = q;
      q = temp;
      temp = swapper;
      t0.add(q);

      if (a.isOne()) {
        return t0;
      }

      r = b.divide(a, q);

      if (r.intLen == 0) {
        throw new ArithmeticException("BigInteger not invertible.");
      }

      swapper = b;
      b =  r;

      if (q.intLen == 1) {
        t0.mul(q.value[q.offset], temp);
      }
      else {
        q.multiply(t0, temp);
      }
      swapper = q; q = temp; temp = swapper;

      t1.add(q);
    }
    mod.subtract(t1);
    return mod;
  }
}
