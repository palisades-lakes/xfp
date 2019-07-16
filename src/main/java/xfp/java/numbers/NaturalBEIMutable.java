package xfp.java.numbers;

import static xfp.java.numbers.Ints.stripLeadingZeros;
import static xfp.java.numbers.Numbers.unsigned;

import java.util.Arrays;
import java.util.List;

import xfp.java.exceptions.Exceptions;

/**
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-16
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

  private final int[] copyWords () {
    return Arrays.copyOfRange(words,start,start+nWords); }

  public final void copyWords (final int[] dst,
                               final int dstStart) {
    final int n = Math.min(words.length,dst.length-dstStart);
    Arrays.fill(dst,0);
    System.arraycopy(words,0,dst,dstStart,n); }

  // TODO: useful to oversize array? or as an option?
  private final void setWords (final int[] v,
                               final int length) {
    assert length<=v.length;
    words = v; nWords = length; start = 0; }

  private final int bei (final int i) {
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

  private final NaturalBEIMutable expandTo (final int i) {
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
    clearUnused();
    return this; }

  @Override
  public final int word (final int i) {
    assert 0<=i;
    if (i>=nWords) { return 0; }
    return words[bei(i)]; }

  @Override
  public final Natural setWord (final int i,
                                final int w) {
    assert 0<=i;
    expandTo(i);
    words[bei(i)] = w;
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

  private final void smallDownShift (final int shift) {
    assert 0<=shift;
    assert shift<32;
    final int[] val = words;
    final int lShift = 32-shift;
    for (int i=(start+nWords)-1, c=val[i]; i > start; i--) {
      final int b = c;
      c = val[i-1];
      val[i] = (c << lShift) | (b >>> shift); }
    val[start] >>>= shift; }

  @Override
  public final Natural shiftDown (final int shift) {
    assert 0<=shift;
    if ((shift>>>5) >= nWords) { return clear(); }
    if (nWords==0) { return this; }
    final int iShift = (shift>>>5);
    final int bShift = (shift&0x1F);
    nWords -= iShift;
    if (bShift == 0) { return this; }
    final int bitsInHighWord = Numbers.hiBit(word(endWord()-1));
    if (bShift >= bitsInHighWord) {
      smallUpShift(32-bShift);
      nWords--; }
    else { smallDownShift(bShift); }
    return this; }

  //--------------------------------------------------------------

  private final void smallUpShift (final int shift) {
    assert 0<=shift;
    assert shift<32;
    final int[] val = words;
    final int rShift = 32-shift;
    for (int i=start, c=val[i], m=(i+nWords)-1; i < m; i++) {
      final int b = c;
      c = val[i+1];
      val[i] = (b << shift) | (c >>> rShift); }
    val[(start+nWords)-1] <<= shift; }

  @Override
  public final Natural shiftUp (final int shift) {
    assert 0<=shift;
    if (0==shift) { return this; }
    // If there is enough storage space in this NaturalBEIMutable
    // already the available space will be used. Space to the
    // right of the used ints in the words array is faster to
    // use, so the extra space will be taken from the right if
    // possible.
    if (nWords == 0) { return this; }
    final int iShift = (shift>>>5);
    final int bShift = (shift&0x1F);
    final int bitsInHighWord = Numbers.hiBit(word(endWord()-1));

    // If shift can be done without moving words, do so
    if (shift<=(32-bitsInHighWord)) {
      smallUpShift(bShift); return this; }

    int newLen = nWords + iShift +1;
    if (bShift <= (32-bitsInHighWord)) { newLen--; }
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
    if (bShift == 0) { return this; }
    if (bShift <= (32-bitsInHighWord)) { smallUpShift(bShift); }
    else { smallDownShift(32-bShift); }
    return this; }

  //--------------------------------------------------------------
  // subtraction
  //--------------------------------------------------------------
  // DANGER: overwrites this with result!

  @Override
  public final Natural subtract (final Natural u) {
    if (! (u instanceof NaturalBEIMutable)) {
      return Natural.super.subtract(u); }

    final NaturalBEIMutable a = this;
    final NaturalBEIMutable b = (NaturalBEIMutable) u;
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
  // Burnikel-Ziegler
  //--------------------------------------------------------------
  /** Like {@link #addShifted(NaturalBEIMutable, int)} but
   * {@code this.intLen} must not be greater than {@code n}. In
   * other words, concatenates {@code this} and {@code addend}.
   */

  private final NaturalBEIMutable
  addDisjoint (final Natural u,
               final int n) {
    assert endWord()<=n;
    final NaturalBEIMutable addend = valueOf(u);
    if (addend.isZero()) { return this; }
    final int n0 = endWord();
    final int n1 = addend.endWord();
    int n1n = n1 + n;
    final int resultLen = Math.max(endWord(),n1n);
    int[] result;
    if (words.length < resultLen) { result = new int[resultLen]; }
    else {
      result = words;
      Arrays.fill(words, start+nWords, words.length, 0); }
    int rstart = result.length-1;
    // copy from this if needed
    System.arraycopy(words, start, result, (rstart+1)-n0, n0);
    n1n -= n0;
    rstart -= n0;
    final int len = Math.min(n1n, addend.endWord());
    //    for (int i=0;i<len;i++) {
    //      result[(rstart+1)-n1n+bei(i)] = addend.word(i); }
    System.arraycopy(
      addend.words, addend.start,
      result, (rstart+1)-n1n, len);
    // zero the gap
    for (int i=((rstart+1)-n1n)+len; i < (rstart+1); i++) {
      result[i] = 0; }
    words = result;
    nWords = resultLen;
    start = result.length - resultLen;
    return this; }

  //--------------------------------------------------------------
  /** Discards all words whose index is greater than {@code n}.
   */

  private final void keepLower (final int n) {
    if (nWords >= n) { start += nWords - n; nWords = n; } }

  //--------------------------------------------------------------
  /** This method implements algorithm 1 from pg. 4 of the
   * Burnikel-Ziegler paper. It divides a 2n-digit number by an
   * n-digit number.<br/>
   * The parameter beta is 2<sup>32</sup> so all shifts are
   * multiples of 32 bits. <br/>
   * {@code this} must be a nonnegative number such that
   * {@code this.hiBit() <= 2*b.hiBit()}
   * @param b a positive number such that {@code b.hiBit()} is even
   */

  private final List<Natural>
  divide2n1n (final Natural b) {
    final int n = b.endWord();

    // step 1: base case
    if (((n%2) != 0) || (n < BURNIKEL_ZIEGLER_THRESHOLD)) {
      final List<Natural> qr = divideAndRemainderKnuth(b);
      return List.of(qr.get(0),qr.get(1)); }

    // step 2: view this as [a1,a2,a3,a4]
    // where each ai is n/2 ints or less
    // aUpper = [a1,a2,a3]
    final NaturalBEIMutable aUpper =
      (NaturalBEIMutable) this.copy().shiftDown(32*(n/2));
    keepLower(n/2); // this = a4

    // step 3: q1=aUpper/b, r1=aUpper%b
    final List<Natural> qr1 = aUpper.divide3n2n(b);

    // step 4: quotient=[r1,this]/b, r2=[r1,this]%b
    addDisjoint(qr1.get(1), n/2);   // this = [r1,this]
    final List<Natural> qr2 = divide3n2n(b);
    // step 5: let quotient=[q1,quotient] and return r2
    final Natural q2 = 
      ((NaturalBEIMutable) qr2.get(0)).addDisjoint(qr1.get(0), n/2);
    return List.of(q2,qr2.get(1)); }

  //--------------------------------------------------------------
  /** Makes this number an {@code n}-int number all of whose bits
   * are ones. Used by Burnikel-Ziegler division.
   * @param n number of ints in the {@code words} array
   * @return a number equal to {@code ((1<<(32*n)))-1}
   */

  //  private final void ones (final int n) {
  //    if (n>words.length) { words = new int[n]; }
  //    Arrays.fill(words, 0xFFFFFFFF);
  //    start = 0;
  //    nWords = n; }

  private static final NaturalBEIMutable ones (final int n) {
    final int[] w = new int[n];
    Arrays.fill(w, 0xFFFFFFFF);
    return unsafe(w); }

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

  private final List<Natural>
  divide3n2n (final Natural b) {
    final int n = b.endWord() / 2;   // half the length of b in ints

    // step 1: view this as [a1,a2,a3] where each ai is n ints
    // or less; let a12=[a1,a2]
    NaturalBEIMutable a12 =
      (NaturalBEIMutable) copy().shiftDown(32*n);

    // step 2: view b as [b1,b2] where each bi is n ints or less
    Natural b1 = b.copy().shiftDown(n*32);
    final Natural b2 = b.words(0,n);
    Natural r;
    Natural d;
    Natural q;
    if (compareTo(b, 32*n) < 0) {
      // step 3a: if a1<b1, let quotient=a12/b1 and r=a12%b1
      final List<Natural> qr = a12.divide2n1n(b1);
      q = qr.get(0);
      r = qr.get(1);
      // step 4: d=quotient*b2
      d = valueOf(q.multiply(b2)); }
    else {
      // step 3b: if a1>=b1, let quotient=beta^n-1
      //and r=a12-b1*2^n+b1
      q = ones(n);
      a12 = (NaturalBEIMutable) a12.add(b1);
      b1 = b1.shiftUp(32*n);
      r = a12.subtract(b1);
      // step 4: d=quotient*b2=(b2 << 32*n) - b2
      d = valueOf(b2).shiftUp(32*n).subtract(b2); }
    // step 5: r = r*beta^n + a3 - d (paper says a4)
    // However, don't subtract d until after the while loop
    // so r doesn't become negative
    r = r.shiftUp(n<<5).add(words(0,n));
    // step 6: add b until r>=d
    while (r.compareTo(d) < 0) {
      r = r.add(b);
      q = q.subtract(one()); }
    return List.of(q,r.subtract(d)); }

  //--------------------------------------------------------------
  /** Computes {@code this/b} and {@code this%b} using the
   * <a href="http://cr.yp.to/bib/1998/burnikel.ps">
   * Burnikel-Ziegler algorithm</a>. This method implements
   * algorithm 3 from pg. 9 of the Burnikel-Ziegler paper.
   * The parameter beta was chosen to b 2<sup>32</sup> so almost
   * all shifts are multiples of 32 bits.<br/>
   */

  @Override
  public final List<Natural>
  divideAndRemainderBurnikelZiegler (final Natural u) {
    final int c = compareTo(u);
    if (0==c) { return List.of(NaturalBEI.ONE,NaturalBEI.ZERO); }
    if (0>c) { return List.of(NaturalBEI.ZERO,this); }
    final int s = u.endWord();
    //final int r = endWord();
    //if (r < s) { return List.of(NaturalBEI.ZERO,this); }

    // step 1: let m = min{2^k | (2^k)*BURNIKEL_ZIEGLER_THRESHOLD > s}
    final int s0 = s/BURNIKEL_ZIEGLER_THRESHOLD;
    final int m = 1 << (32-Integer.numberOfLeadingZeros(s0));

    final int j = ((s+m)-1) / m; // step 2a: j = ceil(s/m)
    final int n = j * m; // step 2b: block length in 32-bit units
    final long n32 = 32L * n; // block length in bits
    // step 3: sigma = max{T | (2^T)*B < beta^n}
    final int sigma = (int) Math.max(0, n32 - u.hiBit());

    // step 4a: shift b so its length is a multiple of n
    final Natural bShifted = u.shiftUp(sigma);
    // step 4b: shift a by the same amount
    final NaturalBEIMutable aShifted = (NaturalBEIMutable) shiftUp(sigma);

    // step 5: t is the number of blocks needed to accommodate a
    // plus one additional bit
    int t = (int) ((aShifted.hiBit()+n32) / n32);
    if (t < 2) { t = 2; }

    // step 6: conceptually split a into blocks a[t-1], ..., a[0]
    // the most significant block of a
    final Natural a1 = aShifted.getBlock(t-1, t, n);

    // step 7: z[t-2] = [a[t-1], a[t-2]]
    // the second to most significant block
    NaturalBEIMutable z = aShifted.getBlock(t-2, t, n);
    z = z.addDisjoint(a1, n);   // z[t-2]

    // schoolbook division on blocks, dividing 2-block by 1-block
    Natural q = zero();
    for (int i=t-2; i > 0; i--) {
      // step 8a: compute (qi,ri) such that z=b*qi+ri
      final List<Natural> qri = z.divide2n1n(bShifted);
      // step 8b: z = [ri, a[i-1]]
      z = aShifted.getBlock(i-1, t, n);   // a[i-1]
      z = z.addDisjoint(qri.get(1), n);
      // update q (part of step 9)
      q = q.add(qri.get(0).immutable(),(i*n)<<5); }
    // final iteration of step 8: do the loop one more time
    // for i=0 but leave z unchanged
    final List<Natural> qri = z.divide2n1n(bShifted);
    // step 9: a and b were shifted, so shift back
    return List.of(
      q.add(qri.get(0)),
      qri.get(1).shiftDown(sigma)); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final String toString () {
    return NaturalBEI.valueOf(words).toString(); }

  // DNAGER: mutable!!!
  @Override
  public final int hashCode () { return uintsHashCode(); }

  // DNAGER: mutable!!!
  @Override
  public final boolean equals (final Object x) {
    if (x==this) { return true; }
    if (!(x instanceof NaturalBEIMutable)) { return false; }
    return uintsEquals((NaturalBEIMutable) x); }

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
  public final Natural recyclable (final Natural init,
                                   final int n) {
    return make(init,n); }

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
        val.words,
        val.start,
        val.start+nWords); }

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

  public static final NaturalBEIMutable make (final NaturalBEI init,
                                              final int n) {
    assert 0<=n;
    final int[] words = new int[n];
    final int start = Math.max(0,n-init.endWord());
    init.copyWords(words,start);
    return new NaturalBEIMutable(words); }

  public static final NaturalBEIMutable make (final NaturalBEIMutable init,
                                              final int n) {
    assert 0<=n;
    final int[] wrods = new int[n];
    final int start = Math.max(0,n-init.endWord());
    init.copyWords(wrods,start);
    return new NaturalBEIMutable(wrods); }

  public static final NaturalBEIMutable make (final Natural u,
                                              final int n) {
    if (u instanceof NaturalBEI) {
      return make((NaturalBEI) u,n); }
    if (u instanceof NaturalBEIMutable) {
      return make((NaturalBEIMutable) u,n); }
    throw Exceptions.unsupportedOperation(
      null,"make",u,Integer.valueOf(n)); }

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

