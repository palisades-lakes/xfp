package xfp.java.numbers;

import static xfp.java.numbers.Ints.stripLeadingZeros;
import static xfp.java.numbers.Numbers.loWord;
import static xfp.java.numbers.Numbers.unsigned;

import java.util.Arrays;
import java.util.List;

import xfp.java.exceptions.Exceptions;

/**
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-11
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
    if (nWords == 0) { start = 0; return; }
    int index = start;
    if (words[index] != 0) { return; }
    final int indexBound = index+nWords;
    do { index++; }
    while((index < indexBound) && (words[index] == 0));
    final int numZeros = index - start;
    nWords -= numZeros;
    start = (nWords == 0 ?  0 : start+numZeros); }

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
  // Uints<Natural> api
  //--------------------------------------------------------------

  @Override
  public final Natural empty () { return make(); }

  @Override
  public final Natural clear () {
    start = 0;
    nWords = 0;
    Arrays.fill(words,0);
    return this; }

  //--------------------------------------------------------------
  // bit operations
  //--------------------------------------------------------------
  /** Down shift this NaturalBEIMutable n bits, where n is
   * less than 32. Assumes that nWords > 0, n > 0 for speed
   */

  private final void primitiveDownShift (final int shift) {
    assert 0<=shift;
    assert shift<32;
    final int[] val = words;
    final int n2 = 32-shift;
    for (int i=(start+nWords)-1, c=val[i]; i > start; i--) {
      final int b = c;
      c = val[i-1];
      val[i] = (c << n2) | (b >>> shift); }
    val[start] >>>= shift; }

  @Override
  public final Natural shiftDown (final int shift) {
    assert 0<=shift;
    if ((shift>>>5) >= nWords) { return clear(); }
    if (nWords==0) { return this; }
    final int nInts = shift >>> 5;
    final int nBits = shift & 0x1F;
    nWords -= nInts;
    if (nBits == 0) { return this; }
    final int bitsInHighWord = Numbers.hiBit(word(endWord()-1));
    if (nBits >= bitsInHighWord) {
      primitiveUpShift(32-nBits);
      nWords--; }
    else { primitiveDownShift(nBits); } 
    return this; }

  //--------------------------------------------------------------

  private final void primitiveUpShift (final int shift) {
    assert 0<=shift;
    assert shift<32;
    final int[] val = words;
    final int n2 = 32 - shift;
    for (int i=start, c=val[i], m=(i+nWords)-1; i < m; i++) {
      final int b = c;
      c = val[i+1];
      val[i] = (b << shift) | (c >>> n2); }
    val[(start+nWords)-1] <<= shift; }

  @Override
  public final Natural shiftUp (final int shift) {
    assert 0<=shift;
    if (0==shift) { return this; }
    // If there is enough storage space in this NaturalBEIMutable
    // already the available space will be used. Space to the
    // right of the used ints in the words array is faster to
    // utilize, so the extra space will be taken from the right if
    // possible.
    if (nWords == 0) { return this; }
    final int iShift = (shift>>>5);
    final int rShift = (shift&0x1F);
    final int bitsInHighWord = Numbers.hiBit(word(endWord()-1));

    // If shift can be done without moving words, do so
    if (shift <= (32-bitsInHighWord)) {
      primitiveUpShift(rShift); return this; }

    int newLen = nWords + iShift +1;
    if (rShift <= (32-bitsInHighWord)) { newLen--; }
    if (words.length<newLen) {
      // The array must grow
      final int[] r = new int[newLen];
      for (int i=0; i<nWords; i++) { r[i] = words[start+i]; }
      setWords(r, newLen); }
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
    if (rShift == 0) { return this; }
    if (rShift <= (32-bitsInHighWord)) {
      primitiveUpShift(rShift); }
    else { primitiveDownShift(32 -rShift); }
    return this; }

  //--------------------------------------------------------------
  // subtraction
  //--------------------------------------------------------------
  // DANGER: overwrites this with result!

  @Override
  public final Natural subtract (final Natural u) {
    if (! (u instanceof NaturalBEIMutable)) {
      return Natural.super.subtract(u); }

    NaturalBEIMutable a = this;
    NaturalBEIMutable b = (NaturalBEIMutable) u;
    int[] result = words;
    final int sign = a.compareTo(b);
    assert 0<=sign;
    if (sign == 0) { clear(); return this; }
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
      diff = unsigned(a.words[x+a.start]) - ((int)-(diff>>32));
      result[rstart--] = (int) diff; }

    words = result;
    nWords = resultLen;
    start = words.length - resultLen;
    compact();
    return this; }

  //--------------------------------------------------------------
  // division
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

   //--------------------------------------------------------------

  @Override
  public final List<Natural> knuthDivision (final Natural u) {
    assert !u.isZero();
    final NaturalBEIMutable div = (NaturalBEIMutable) u;
    final NaturalBEIMutable quotient = 
      (NaturalBEIMutable) recyclable(endWord());
    // D1 compact the divisor
    final int shift =
      Integer.numberOfLeadingZeros(div.word(div.endWord()-1));
    // Copy divisor words to protect divisor
    final int dlen = div.endWord();
    int[] divisor;
    // Remainder starts as dividend with space for a leading zero
    NaturalBEIMutable rem;
    if (0<shift) {
      divisor = new int[dlen];
      Ints.copyAndShift(div.words,div.start,dlen,divisor,0,shift);
      //rem = (NaturalBEIMutable) shiftUp(shift);
      //rem.expandTo(nWords+2);
      if (shift<=Integer.numberOfLeadingZeros(word(endWord()-1))) {
        rem = make(nWords+1); rem.nWords = nWords; rem.start = 1;
        Ints.copyAndShift(words,start,nWords,rem.words,1,shift); }
      else {
        rem = make(nWords+2); rem.nWords = nWords+1; rem.start = 1;
        int rFrom = start;
        int c=0;
        final int n2 = 32 - shift;
        for (int i=1; i < (nWords+1); i++,rFrom++) {
          final int b = c;
          c = words[rFrom];
          rem.words[i] = (b << shift) | (c >>> n2); }
        rem.words[nWords+1] = c << shift; }
      }
    else {
      divisor = Arrays.copyOfRange(
        div.words, div.start, div.start + div.nWords);
      rem = make(nWords+1); rem.nWords = nWords; rem.start = 1;
      System.arraycopy(words, start, rem.words, 1, nWords); }

    final int nlen = rem.endWord();

    // Set the quotient size
    final int limit = (nlen-dlen) + 1;
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
          final long tmp = Ints.divWord(nChunk, dh);
          qhat = (int) (loWord(tmp));
          qrem = (int) (tmp >>> 32); } }
      if (qhat == 0) { continue; }
      if (!skipCorrection) { // Correct qhat
        final long nl = unsigned(rem.words[j+2+rem.start]);
        long rs = (unsigned(qrem) << 32) | nl;
        long estProduct = unsigned(dl) * (unsigned(qhat));
        if (unsignedGreaterThan(estProduct, rs)) {
          qhat--;
          qrem = (int)((unsigned(qrem)) + dhLong);
          if ((unsigned(qrem)) >=  dhLong) {
            estProduct -= (unsigned(dl));
            rs = ((unsigned(qrem)) << 32) | nl;
            if (unsignedGreaterThan(estProduct, rs)) {
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
        final long tmp = Ints.divWord(nChunk, dh);
        qhat = (int) (loWord(tmp));
        qrem = (int) (tmp >>> 32); } }
    // 2nd correction
    if (qhat != 0) {
      if (!skipCorrection) {
        final long nl =
          unsigned(rem.words[limit + 1 + rem.start]);
        long rs = ((unsigned(qrem)) << 32) | nl;
        long estProduct = (unsigned(dl)) * (unsigned(qhat));
        if (unsignedGreaterThan(estProduct, rs)) {
          qhat--;
          qrem = (int) ((unsigned(qrem)) + dhLong);
          if ((unsigned(qrem)) >= dhLong) {
            estProduct -= (unsigned(dl));
            rs = ((unsigned(qrem)) << 32) | nl;
            if (unsignedGreaterThan(estProduct, rs)) { qhat--; } } } }
      // D4 Multiply and subtract
      int borrow;
      rem.words[(limit - 1) + rem.start] = 0;
      borrow =
        mulsub(rem.words,divisor,qhat,dlen,(limit-1)+rem.start);
      // D5 Test remainder
      if ((borrow + 0x80000000) > nh2) {
        // D6 Add back
        divadd(divisor,rem.words,(limit-1)+1+rem.start);
        qhat--; }
      // Store the quotient digit
      q[(limit - 1)] = qhat; }

    // D8 decompact
    if (shift > 0) { 
      rem = (NaturalBEIMutable) rem.shiftDown(shift); }
    rem.compact(); 
    quotient.compact();
    return List.of(quotient,rem); }

  //--------------------------------------------------------------
  // Burnikel-Ziegler
  //--------------------------------------------------------------
  /** Like {@link #addShifted(NaturalBEIMutable, int)} but
   * {@code this.intLen} must not be greater than {@code n}. In
   * other words, concatenates {@code this} and {@code addend}.
   */

  private final NaturalBEIMutable
  addDisjoint (final NaturalBEIMutable addend,
               final int n) {
    if (addend.isZero()) { return this; }
    final int x = nWords;
    final int n1 = addend.endWord();
    int y = n1 + n;
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
    start = result.length - resultLen; 
    return this; }

  //--------------------------------------------------------------
  /** This method implements algorithm 1 from pg. 4 of the
   * Burnikel-Ziegler paper. It divides a 2n-digit number by an
   * n-digit number.<br/>
   * The parameter beta is 2<sup>32</sup> so all shifts are
   * multiples of 32 bits. <br/>
   * {@code this} must be a nonnegative number such that
   * {@code this.hiBit() <= 2*b.hiBit()}
   * @param b a positive number such that {@code b.hiBit()} is even
   * @param quotient output parameter for {@code this/b}
   * @return {@code this%b}
   */

  private final NaturalBEIMutable
  divide2n1n (final NaturalBEIMutable b,
              final NaturalBEIMutable quotient) {
    final int n = b.nWords;

    // step 1: base case
    if (((n%2) != 0) 
      || (n < NaturalBEI.BURNIKEL_ZIEGLER_THRESHOLD)) {
      final List<Natural> qr = divideAndRemainder(b);
      return (NaturalBEIMutable) qr.get(1); }

    // step 2: view this as [a1,a2,a3,a4] 
    // where each ai is n/2 ints or less
    NaturalBEIMutable aUpper = new NaturalBEIMutable(this);
    // aUpper = [a1,a2,a3]
    aUpper = (NaturalBEIMutable) aUpper.shiftDown(32*(n/2));   
    NaturalBEIMutable a4 = (NaturalBEIMutable) words(0,n/2);
    //keepLower(n/2);   // this = a4

    // step 3: q1=aUpper/b, r1=aUpper%b
    final NaturalBEIMutable q1 = new NaturalBEIMutable();
    final NaturalBEIMutable r1 =aUpper.divide3n2n(b, q1);

    // step 4: quotient=[r1,this]/b, r2=[r1,this]%b
    a4 = a4.addDisjoint(r1, n/2);   // this = [r1,this]
    final NaturalBEIMutable r2 = a4.divide3n2n(b, quotient);

    // step 5: let quotient=[q1,quotient] and return r2
    quotient.addDisjoint(q1, n/2);
    return r2; }

  //--------------------------------------------------------------
  /** Makes this number an {@code n}-int number all of whose bits
   * are ones. Used by Burnikel-Ziegler division.
   * @param n number of ints in the {@code words} array
   * @return a number equal to {@code ((1<<(32*n)))-1}
   */

  private final void ones (final int n) {
    if (n>words.length) { words = new int[n]; }
    Arrays.fill(words, 0xFFFFFFFF);
    start = 0;
    nWords = n; }

  /** This method implements algorithm 2 from pg. 5 of the
   * Burnikel-Ziegler paper. It divides a 3n-digit number by a
   * 2n-digit number.<br/>
   * The parameter beta is 2<sup>32</sup> so all shifts are
   * multiples of 32 bits.<br/>
   * <br/>
   * {@code this} must be a nonnegative number such that
   * {@code 2*this.hiBit() <= 3*b.hiBit()}
   * @param quotient output parameter for {@code this/b}
   * @return {@code this%b}
   */

  private final NaturalBEIMutable 
  divide3n2n (final NaturalBEIMutable b,
              final NaturalBEIMutable quotient) {
    final int n = b.nWords / 2;   // half the length of b in ints

    // step 1: view this as [a1,a2,a3] where each ai is n ints
    // or less; let a12=[a1,a2]
    NaturalBEIMutable a12 = new NaturalBEIMutable(this);
    a12 = (NaturalBEIMutable) a12.shiftDown(32*n);

    // step 2: view b as [b1,b2] where each bi is n ints or less
    NaturalBEIMutable b1 = new NaturalBEIMutable(b);
    b1 = (NaturalBEIMutable) b1.shiftDown(n * 32);
    final Natural b2 = b.words(0,n);
    Natural r;
    NaturalBEIMutable d;
    if (compareShifted(b, n) < 0) {
      // step 3a: if a1<b1, let quotient=a12/b1 and r=a12%b1
      r = a12.divide2n1n(b1, quotient);
      // step 4: d=quotient*b2
      final Natural qu = quotient.multiply(b2);
      d = (NaturalBEIMutable) qu.recyclable(qu); }
    else {
      // step 3b: if a1>=b1, let quotient=beta^n-1
      //and r=a12-b1*2^n+b1
      quotient.ones(n);
      a12 = (NaturalBEIMutable) a12.add(b1);
      b1 = (NaturalBEIMutable) b1.shiftUp(32*n);
      a12 = (NaturalBEIMutable) a12.subtract(b1);
      r = a12;
      // step 4: d=quotient*b2=(b2 << 32*n) - b2
      d = NaturalBEIMutable.valueOf(b2);
      d = (NaturalBEIMutable) d.shiftUp(32 * n);
      d = (NaturalBEIMutable) d.subtract(NaturalBEIMutable.valueOf(b2)); }
    // step 5: r = r*beta^n + a3 - d (paper says a4)
    // However, don't subtract d until after the while loop
    // so r doesn't become negative
    //r = (NaturalBEIMutable) r.shiftUp(32 * n);
    //r.addLower(this, n);
    r = r.shiftUp(n<<5).add(words(0,n));
    // step 6: add b until r>=d
    while (r.compareTo(d) < 0) {
      r = r.add(b);
      quotient.subtract(one()); }
    return (NaturalBEIMutable) r.subtract(d); }

  //--------------------------------------------------------------
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

  @Override
  public final List<Natural>
  divideAndRemainderBurnikelZiegler (final Natural u) {
    final NaturalBEIMutable b = (NaturalBEIMutable) u;
    final int r = nWords;
    final int s = b.nWords;

    NaturalBEIMutable q = make();
    if (r < s) { return List.of(NaturalBEI.ZERO,this); }
    // step 1: let m = min{2^k | (2^k)*BURNIKEL_ZIEGLER_THRESHOLD > s}
    final int s0 = s/NaturalBEI.BURNIKEL_ZIEGLER_THRESHOLD;
    final int m = 1 << (32-Integer.numberOfLeadingZeros(s0));

    final int j = ((s+m)-1) / m; // step 2a: j = ceil(s/m)
    final int n = j * m; // step 2b: block length in 32-bit units
    final long n32 = 32L * n; // block length in bits
    // step 3: sigma = max{T | (2^T)*B < beta^n}
    final int sigma = (int) Math.max(0, n32 - b.hiBit());
    NaturalBEIMutable bShifted = new NaturalBEIMutable(b);
    // step 4a: shift b so its length is a multiple of n
    bShifted = (NaturalBEIMutable) bShifted.shiftUp(sigma);
    NaturalBEIMutable aShifted = new NaturalBEIMutable(this);
    // step 4b: shift a by the same amount
    aShifted = (NaturalBEIMutable) aShifted.shiftUp(sigma);

    // step 5: t is the number of blocks needed to accommodate a
    // plus one additional bit
    int t = (int) ((aShifted.hiBit()+n32) / n32);
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
      q.addShifted(qi, i*n); }
    // final iteration of step 8: do the loop one more time
    // for i=0 but leave z unchanged
    ri = z.divide2n1n(bShifted, qi);
    q = (NaturalBEIMutable) q.add(qi);
    // step 9: a and b were shifted, so shift back
    ri = (NaturalBEIMutable) ri.shiftDown(sigma);
    return List.of(q,ri); }

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

  //-------------------------------------------------------------
  // Comparable, DANGER to mutability
  //-------------------------------------------------------------
  /** Compare two longs as if they were unsigned.
   * Returns true iff a is bigger than a.
   */

  private static final boolean
  unsignedGreaterThan (final long a,
                       final long b) {
    return (a+Long.MIN_VALUE) > (b+Long.MIN_VALUE); }

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

  @Override
  public final int compareTo (final Natural that) {
    if (that instanceof NaturalBEIMutable) {
      return compareTo((NaturalBEIMutable) that); }
    return Natural.super.compareTo(that); }

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
  public final Natural copy () { return valueOf(this); }

  @Override
  public final Natural recyclable (final Natural init) {
    if (this==init) { return this; }
    return valueOf(init); }

  @Override
  public final Natural recyclable (final int n) {
    assert 0<=n;
    return make(n); }
//    expandTo(n); return this; }

  @Override
  public final boolean isImmutable () { return false; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  // DANGER!! no copying
  private NaturalBEIMutable (final int[] val) {
    words = val;
    nWords = val.length; }

  private NaturalBEIMutable () { 
    words = new int[1]; nWords = 0; }

  private NaturalBEIMutable (final NaturalBEIMutable val) {
    nWords = val.nWords;
    words =
      Arrays.copyOfRange(
        val.words,val.start,val.start+nWords); }

  private NaturalBEIMutable (final int val) {
    words = new int[1];
    nWords = 1;
    words[0] = val; }

  @Override
  public final Natural from (final int x) {
    return new NaturalBEIMutable(x); }

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
    assert 0<=n;
    return new NaturalBEIMutable(new int[n]); }

  public static final NaturalBEIMutable
  valueOf (final int[] val) {
    return unsafe(Arrays.copyOf(val,val.length)); }

  public static final NaturalBEIMutable
  valueOf (final NaturalBEI u) {
    return unsafe(u.copyWords()); }

  public static final NaturalBEIMutable
  valueOf (final NaturalBEIMutable u) {
    return unsafe(u.copyWords()); }

  public static final NaturalBEIMutable
  valueOf (final Natural u) {
    if (u instanceof NaturalBEI) { 
      return valueOf((NaturalBEI) u); }
    if (u instanceof NaturalBEIMutable) { 
      return valueOf((NaturalBEIMutable) u); }
    throw Exceptions.unsupportedOperation(null,"valueOf",u); }

  public static final NaturalBEIMutable valueOf (final long u) {
    if (0L==u) { return make(); }
    assert 0L<u;
    return unsafe(Ints.bigEndian(u)); }

  public static final NaturalBEIMutable valueOf (final long u,
                                                 final int upShift) {
    assert 0L<=u;
    assert 0<=upShift;
    if (0==upShift) { return valueOf(u); }
    if (0L==u) { return make(); }
    return (NaturalBEIMutable) valueOf(u).shiftUp(upShift); }

  //--------------------------------------------------------------

  @Override
  public final Natural one () { return new NaturalBEIMutable(1); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

