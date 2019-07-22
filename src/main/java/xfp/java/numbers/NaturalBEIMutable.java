package xfp.java.numbers;

import static xfp.java.numbers.Ints.stripLeadingZeros;

import java.util.Arrays;

import xfp.java.exceptions.Exceptions;

/**
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-22
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

  final NaturalBEIMutable expandTo (final int i) {
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
  // DANGER!!!

  private final Natural smallDownShift (final int rShift) {
    assert 0<=rShift;
    assert rShift<32;
    if (0<rShift) {
      final int lShift = 32-rShift;
      int c = words[(start+nWords)-1];
      for (int i=(start+nWords)-1; i > start; i--) {
        final int b = c;
        c = words[i-1];
        words[i] = (c << lShift) | (b >>> rShift); }
      words[start] >>>= rShift; }
    return this; }

  // DANGER!!!
  @Override
  public final Natural shiftDown (final int shift) {
    assert 0<=shift;
    if ((shift>>>5) >= nWords) { return clear(); }
    if (nWords==0) { return this; }
    final int iShift = (shift>>>5);
    final int bShift = (shift&0x1F);
    nWords -= iShift;
    if (bShift == 0) { return this; }
    final int bitsInHighWord = Numbers.hiBit(word(hiInt()-1));
    if (bShift >= bitsInHighWord) {
      smallUpShift(32-bShift);
      nWords--; }
    else { smallDownShift(bShift); }
    return this; }

  //--------------------------------------------------------------
  // DANGER!!!

  private final Natural smallUpShift (final int lShift) {
    assert 0<=lShift;
    assert lShift<32;
    if (0<lShift) {
      final int rShift = 32-lShift;
      final int m=start+nWords-1;
      int c = words[start];
      for (int i=start;i<m;i++) {
        final int b = c;
        c = words[i+1];
        words[i] = (b<<lShift) | (c>>>rShift); }
      words[m] <<= lShift; }
    return this; }

  // DANGER!!!

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
    final int maxShift =32- Numbers.hiBit(word(hiInt()-1));

    // If shift can be done without moving words, do so
    if (shift<=maxShift) { return smallUpShift(bShift); }

    int newLen = nWords + iShift +1;
    if (bShift<=maxShift) { newLen--; }
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
    if (bShift==0) { return this; }
    if (bShift<=maxShift) { return smallUpShift(bShift); }
    return smallDownShift(32-bShift); }

  //--------------------------------------------------------------
  // subtraction
  //--------------------------------------------------------------
  // DANGER: overwrites this with result!

  //  @Override
  //  public final Natural subtract (final Natural u) {
  //    if (! (u instanceof NaturalBEIMutable)) {
  //      return Natural.super.subtract(u); }
  //
  //    final NaturalBEIMutable a = this;
  //    final NaturalBEIMutable b = (NaturalBEIMutable) u;
  //    int[] result = words;
  //    final int sign = a.compareTo(b);
  //    assert 0<=sign;
  //    if (sign == 0) { clear(); return this; }
  //    final int resultLen = a.nWords;
  //    if (result.length < resultLen) { result = new int[resultLen]; }
  //    long diff = 0;
  //    int x = a.nWords;
  //    int y = b.nWords;
  //    int rstart = result.length - 1;
  //    // Subtract common parts of both numbers
  //    while (y > 0) {
  //      x--; y--;
  //      diff = unsigned(a.words[x+a.start])
  //        - unsigned(b.words[y+b.start])
  //        - ((int)-(diff>>32));
  //      result[rstart--] = (int)diff; }
  //    // Subtract remainder of longer number
  //    while (x > 0) {
  //      x--;
  //      diff = unsigned(a.words[x+a.start]) - ((int)-(diff>>32));
  //      result[rstart--] = (int) diff; }
  //
  //    words = result;
  //    nWords = resultLen;
  //    start = words.length - resultLen;
  //    compact();
  //    return this; }

  //--------------------------------------------------------------

  //--------------------------------------------------------------

  @Override
  public final Natural one () { return new NaturalBEIMutable(1); }

  @Override
  public final Natural ones (final int n) {
    final int[] w = new int[n];
    Arrays.fill(w, 0xFFFFFFFF);
    return unsafe(w); }

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
    final int start = Math.max(0,n-init.hiInt());
    init.copyWords(words,start);
    return new NaturalBEIMutable(words); }

  public static final NaturalBEIMutable make (final NaturalBEIMutable init,
                                              final int n) {
    assert 0<=n;
    final int[] wrods = new int[n];
    final int start = Math.max(0,n-init.hiInt());
    init.copyWords(wrods,start);
    return new NaturalBEIMutable(wrods); }

  public static final NaturalBEIMutable make (final Natural u,
                                              final int n) {
    if (u instanceof NaturalBEI) {
      return make((NaturalBEI) u,n); }
    if (u instanceof NaturalBEIMutable) {
      return make((NaturalBEIMutable) u,n); }
    throw Exceptions.unsupportedOperation(null,"make",u,n); }

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
}
//--------------------------------------------------------------

