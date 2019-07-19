package xfp.java.numbers;

import static xfp.java.numbers.Numbers.hiWord;
import static xfp.java.numbers.Numbers.loWord;
import static xfp.java.numbers.Numbers.unsigned;

import java.util.List;

/** Division, gcd, etc., of natural numbers.
 * 
 * Non-instantiable.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-19
 */

@SuppressWarnings("unchecked")
public final class NaturalDivide {

  //--------------------------------------------------------------
  // division
  //--------------------------------------------------------------

  private static final int BURNIKEL_ZIEGLER_THRESHOLD = 80;
  private static final int BURNIKEL_ZIEGLER_OFFSET = 40;

  //--------------------------------------------------------------

  private static final boolean useKnuthDivision (final Natural u,
                                                 final Natural v) {
    final int nn = u.endWord();
    final int nd = v.endWord();
    return
      (nd < BURNIKEL_ZIEGLER_THRESHOLD)
      ||
      ((nn-nd) < BURNIKEL_ZIEGLER_OFFSET); }

  //--------------------------------------------------------------
  /** Interpret {@code d} as unsigned. */

  private static final List<Natural> 
  divideAndRemainder (final Natural u,
                      final int d) {
    if (1==d) {
      return List.of(u.immutable(),u.from(0)); }

    final long dd = unsigned(d);
    if (1==u.endWord()) {
      final long nn = u.uword(0);
      final int q = (int) (nn/dd);
      final int r = (int) (nn-(q*dd));
      return List.of(u.from(q),u.from(r)); }

    Natural qq = u.recyclable(u.endWord());

    final int shift = Integer.numberOfLeadingZeros(d);
    int r = u.word(u.endWord()-1);
    long rr = unsigned(r);
    if (rr < dd) { 
      qq = qq.setWord(qq.endWord()-1,0); }
    else {
      final int rrdd = (int) (rr / dd);
      qq = qq.setWord(u.endWord()-1,rrdd);
      r = (int) (rr-(rrdd*dd));
      rr = unsigned(r); }

    int xlen = u.endWord();
    while (--xlen > 0) {
      final long nEst = (rr << 32) | u.uword(xlen-1);
      final int q;
      if (nEst >= 0) {
        q = (int) (nEst / dd);
        r = (int) (nEst - (q * dd)); }
      else {
        final long tmp = Ints.divWord(nEst,dd);
        q = (int) Numbers.loWord(tmp);
        r = (int) Numbers.hiWord(tmp); }
      qq = qq.setWord(xlen-1,q);
      rr = unsigned(r); }
    //qq.compact();
    // decompact
    if (shift > 0) { return List.of(qq,u.from(r % d)); }
    return List.of(qq,u.from(r)); }

  //--------------------------------------------------------------
  /** Special shifted fused multiply-subtract 
   */

  private static List fms (final Natural u0,
                           final long x,
                           final Natural v,
                           final int i0) {
    assert 0L<=x;
    assert 0<=i0;
    Natural u = u0.copy();
    final int n0 = u.endWord();
    final int n1 = v.endWord();
    long carry = 0;
    int i = n0 - 1 - n1 - i0;
    assert 0<=i :
      "\nu=\n" + u + "\nv=\n" + v
      + "\nx= " + x 
      + "\nn0= " + n0 + "\nn1= " + n1 + "\ni0= " + i0; 
    for (int j=0;j<n1;j++,i++) {
      final long prod = (v.uword(j)*x) + carry;
      final long diff = u.uword(i)-prod;
      u = u.setWord(i, (int) diff);
      carry = hiWord(prod);
      // TODO: is this related to possibility x*u > this,
      // so difference is negative?
      if (loWord(diff) > unsigned(~(int)prod)) { carry++; } }
    return List.of(u, Long.valueOf(loWord(carry))); }

  //--------------------------------------------------------------
  /** A primitive used for division. This method adds in one
   * multiple of the divisor a back to the dividend result at a
   * specified start. It is used when qhat was estimated too
   * large, and must be adjusted.
   * <p>
   * Never called in testing, (prob less than 2^32),
   * so DANGER that changes have made it incorrect...
   */

  private static final Natural divadd (final Natural u0,
                                       final Natural v,
                                       final int i0) {
    assert 0<=i0;
    Natural u = u0;
    final int n0 = u.endWord();
    final int n1 = v.endWord();
    final int off = n0 - n1 - 1 - i0;
    long carry = 0;   
    for (int j=0;j<n1;j++) {
      final int i = off + j;
      final long sum = v.uword(j) + u.uword(i) + carry;
      u = u.setWord(i,(int)sum);
      carry = sum >>> 32; }
    return u; }

  //--------------------------------------------------------------

  private static final List<Natural> 
  knuthDivision (final Natural u,
                 final Natural v) {
    assert !v.isZero();
    // D1 compact the divisor
    final int nu = v.endWord();
    final int lShift = Integer.numberOfLeadingZeros(v.word(nu-1));
    Natural d = u.recyclable(v,nu).shiftUp(lShift);
    final int nd = d.endWord();
    Natural r = u.copy().shiftUp(lShift);
    final int nr = r.endWord();
    r = r.setWord(nr,0);
    final int limit = nr-nu+1;
    Natural q = u.recyclable(limit);
    final long dh = d.uword(nd-1);
    final long dl = d.uword(nd-2);
    // D2 Initialize j
    for (int j=0;j<(limit-1);j++) {
      // D3 Calculate qhat
      boolean skipCorrection = false;
      final int i = nr-j;
      final long nh = r.uword(i);
      final long nm = r.uword(i-1);
      long qhat;
      long qrem;
      if (nh==dh) {
        qhat = 0xFFFFFFFFL;
        qrem = nh+nm;
        skipCorrection = (qrem < nh); }
      else {
        final long nChunk = (nh<<32) | nm;
        if (nChunk >= 0) {
          qhat = loWord(nChunk/dh);
          qrem = loWord(nChunk-(qhat*dh)); }
        else {
          final long tmp = Ints.divWord(nChunk,dh);
          qhat = loWord(tmp);
          qrem = hiWord(tmp); } }
      if (qhat == 0L) { continue; }
      if (!skipCorrection) { // Correct qhat
        final long nl = r.uword(i-2);
        long rs = (qrem << 32) | nl;
        long estProduct = dl*qhat;
        if (Long.compareUnsigned(estProduct, rs)>0) {
          qhat--;
          qrem = loWord(qrem+dh); 
          if (qrem>=dh) {
            estProduct -= (dl);
            rs = (qrem << 32) | nl;
            if (Long.compareUnsigned(estProduct, rs)>0) { qhat--; } } } }
      // D4 Multiply and subtract
      r.setWord(i,0);
      final List rc = fms(r,qhat,d,j);
      r = (Natural) rc.get(0);
      final long borrow = ((Long) rc.get(1)).longValue();
      // D5 Test remainder
      //divaddCheck(d,r,j);
      if (borrow > nh) { // D6 Add back
        r = divadd(r,d,j); qhat--; }
      // Store the quotient digit
      q = q.setWord(q.endWord()-1-j,(int)qhat); } // D7 loop on j

    // D3 Calculate qhat
    // 1st estimate
    long qhat;
    long qrem;
    boolean skipCorrection = false;
    final int i = r.endWord() - limit;
    final long nh = r.uword(i);
    final long nm =  r.uword(i-1);
    if (nh == dh) {
      qhat = 0xFFFFFFFFL;
      qrem = nh+nm;
      skipCorrection = (qrem < nh); }
    else {
      final long nChunk = (nh << 32) | nm;
      if (nChunk >= 0) {
        qhat = loWord(nChunk/dh);
        qrem = loWord(nChunk-(qhat*dh)); }
      else {
        final long tmp = Ints.divWord(nChunk,dh);
        qhat = loWord(tmp);
        qrem = hiWord(tmp); } }
    // 2nd correction
    if (qhat != 0L) {
      if (!skipCorrection) {
        final long nl = r.uword(i-2);
        long rs = (qrem << 32) | nl;
        long estProduct = dl*qhat;
        if (Long.compareUnsigned(estProduct, rs)>0) {
          qhat--;
          qrem = loWord(qrem + dh);
          if (qrem >= dh) {
            estProduct -= (dl);
            rs = (qrem << 32) | nl;
            if (Long.compareUnsigned(estProduct, rs)>0) { qhat--; } } } }
      // D4 Multiply and subtract
      r = r.setWord(i,0);
      final List rc = fms(r,qhat,d,limit-1);
      r = (Natural) rc.get(0);
      final long borrow = ((Long) rc.get(1)).longValue();
      // D5 Test remainder
      if (borrow > nh) { // D6 Add back
        r = divadd(r,d,limit-1); qhat--; }
      // Store the quotient digit
      q = q.setWord(q.endWord()-limit,(int)qhat); }

    // D8 decompact
    if (lShift > 0) { r = r.shiftDown(lShift); }
    //r.compact(); 
    //q.compact();
    return List.of(q,r); }

  //--------------------------------------------------------------

  private static final int KNUTH_POW2_THRESH_LEN = 6;
  private static final int KNUTH_POW2_THRESH_ZEROS = 3*32;

  //--------------------------------------------------------------

  public static final List<Natural> 
  divideAndRemainderKnuth (final Natural u,
                           final Natural v) {
    assert ! v.isZero();
    if (v.isOne()) { return List.of(u.immutable(),u.zero()); }
    if (u.isZero()) { return List.of(u.zero(),u.zero()); }

    final int cmp = u.compareTo(v);
    if (0==cmp) { return List.of(u.one(),u.zero()); }
    if (0>cmp) { return List.of(u.zero(),u.immutable()); }

    if (1==v.endWord()) { 
      return divideAndRemainder(u,v.word(0)); } 

    // Cancel common powers of 2 if above KNUTH_POW2_* thresholds
    if (u.endWord() >= KNUTH_POW2_THRESH_LEN) {
      final int shift = Math.min(u.loBit(),v.loBit());
      if (shift >= KNUTH_POW2_THRESH_ZEROS) {
        final Natural a = u.recyclable(u).shiftDown(shift);
        final Natural b = v.recyclable(v).shiftDown(shift);
        final List<Natural> qr = divideAndRemainderKnuth(a,b);
        final Natural r = qr.get(1).shiftUp(shift);
        return List.of(qr.get(0),r); } }

    final Natural a = u.recyclable(u);
    final Natural b = v.recyclable(v);
    return knuthDivision(a,b); }

  //--------------------------------------------------------------
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

  private static final List<Natural> 
  divide3n2n (final Natural a,
              final Natural b) {
    final int n = b.endWord() / 2;   // half the length of b in ints

    // step 1: view this as [a1,a2,a3] where each ai is n ints
    // or less; let a12=[a1,a2]
    Natural a12 = a.copy().shiftDown(32*n);

    // step 2: view b as [b1,b2] where each bi is n ints or less
    Natural b1 = b.copy().shiftDown(n*32);
    final Natural b2 = b.words(0,n);
    Natural r;
    Natural d;
    Natural q;
    if (a.compareTo(b, 32*n) < 0) {
      // step 3a: if a1<b1, let quotient=a12/b1 and r=a12%b1
      // Doesn't need modified a12
      final List<Natural> qr = divide2n1n(a12,b1);
      q = qr.get(0);
      r = qr.get(1);
      // step 4: d=quotient*b2
      d = q.multiply(b2); }
    else {
      // step 3b: if a1>=b1, let quotient=beta^n-1
      //and r=a12-b1*2^n+b1
      q = a.ones(n);
      a12 = a12.add(b1);
      b1 = b1.shiftUp(32*n);
      r = a12.subtract(b1);
      // step 4: d=quotient*b2=(b2 << 32*n) - b2
      d = b2.copy().shiftUp(32*n).subtract(b2); }
    // step 5: r = r*beta^n + a3 - d (paper says a4)
    // However, don't subtract d until after the while loop
    // so r doesn't become negative
    r = r.shiftUp(n<<5).add(a.words(0,n));
    // step 6: add b until r>=d
    while (r.compareTo(d) < 0) {
      r = r.add(b);
      q = q.subtract(a.one()); }
    return List.of(q,r.subtract(d)); }

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

  private static final List<Natural> 
  divide2n1n (final Natural a,
              final Natural b) {
    final int n = b.endWord();

    // step 1: base case
    if (((n%2) != 0) || (n < BURNIKEL_ZIEGLER_THRESHOLD)) {
      final List<Natural> qr = divideAndRemainderKnuth(a,b);
      return List.of(qr.get(0),qr.get(1)); }

    // step 2: view this as [a1,a2,a3,a4]
    // where each ai is n/2 ints or less
    // aUpper = [a1,a2,a3]
    final Natural aUpper = a.copy().shiftDown(32*(n/2));

    Natural aa = a.words(0,n/2); // this = a4

    // step 3: q1=aUpper/b, r1=aUpper%b
    final List<Natural> qr1 = divide3n2n(aUpper,b);

    // step 4: quotient=[r1,this]/b, r2=[r1,this]%b
    aa = aa.add(qr1.get(1),32*(n/2));   // this = [r1,this]

    final List<Natural> qr2 = divide3n2n(aa,b);
    // step 5: let quotient=[q1,quotient] and return r2
    final Natural q2 = qr2.get(0).add(qr1.get(0), 32*(n/2));
    return List.of(q2,qr2.get(1)); }

  //--------------------------------------------------------------
  /** Returns a {@code Natural} containing
   * {@code blockLength} ints from {@code this} number, starting
   * at {@code index*blockLength}.<br/>
   * Used by Burnikel-Ziegler division.
   * @param index the block index
   * @param numBlocks the total number of blocks in {@code this}
   * @param blockLength length of one block in units of 32 bits
   */

  private static final Natural getBlock (final Natural u,
                                         final int index,
                                         final int numBlocks,
                                         final int blockLength) {
    final int blockStart = index * blockLength;
    if (blockStart >= u.endWord()) { return u.zero(); }
    final int blockEnd;
    if (index == (numBlocks-1)) { blockEnd = u.endWord(); }
    else { blockEnd = (index+1) * blockLength; }
    if (blockEnd > u.endWord()) { return u.zero(); }
    return u.words(blockStart,blockEnd); }

  //--------------------------------------------------------------

  public static final List<Natural>
  divideAndRemainderBurnikelZiegler (final Natural u,
                                     final Natural v) {
    if (u.isImmutable()) {
      return
        u.recyclable(u).divideAndRemainderBurnikelZiegler(v); }
    final int c = u.compareTo(v);
    if (0==c) { return List.of(u.one(),u.zero()); }
    if (0>c) { return List.of(u.zero(),u); }
    final int s = v.endWord();

    // step 1: let m = min{2^k | (2^k)*BURNIKEL_ZIEGLER_THRESHOLD > s}
    final int s0 = s/BURNIKEL_ZIEGLER_THRESHOLD;
    final int m = 1 << (32-Integer.numberOfLeadingZeros(s0));

    final int j = ((s+m)-1) / m; // step 2a: j = ceil(s/m)
    final int n = j * m; // step 2b: block length in 32-bit units
    final long n32 = 32L * n; // block length in bits
    // step 3: sigma = max{T | (2^T)*B < beta^n}
    final int sigma = (int) Math.max(0, n32 - v.hiBit());

    // step 4a: shift b so its length is a multiple of n
    assert 0<=sigma;
    final Natural bShifted = v.copy().shiftUp(sigma);
    // step 4b: shift a by the same amount
    final Natural aShifted = u.copy().shiftUp(sigma);

    // step 5: t is the number of blocks needed to accommodate a
    // plus one additional bit
    final int t = Math.max(2,(int) ((aShifted.hiBit()+n32) / n32));

    // step 6: conceptually split a into blocks a[t-1], ..., a[0]
    // the most significant block of a
    final Natural a1 = getBlock(aShifted,t-1, t, n);

    // step 7: z[t-2] = [a[t-1], a[t-2]]
    // the second to most significant block
    Natural z = getBlock(aShifted,t-2, t, n);
    z = z.add(a1,32*n);   // z[t-2]

    // schoolbook division on blocks, dividing 2-block by 1-block
    Natural q = u.zero();
    for (int i=t-2; i > 0; i--) {
      // step 8a: compute (qi,ri) such that z=b*qi+ri
      // Doesn't need modified z
      final List<Natural> qri = divide2n1n(z,bShifted);
      // step 8b: z = [ri, a[i-1]]
      z = getBlock(aShifted,i-1, t, n);   // a[i-1]
      z = z.add(qri.get(1), 32*n);
      // update q (part of step 9)
      q = q.add(qri.get(0).immutable(),(i*n)<<5); }
    //System.out.println("q=\n"+q);
    // final iteration of step 8: do the loop one more time
    // for i=0 but leave z unchanged
    // Doesn't need modified z
    final List<Natural> qri = divide2n1n(z,bShifted);
    //System.out.println("z=\n"+z);
    //System.out.println("b=\n"+bShifted);
    //System.out.println("q=\n"+qri.get(0));
    //System.out.println("r=\n"+qri.get(1));
    
    // step 9: a and b were shifted, so shift back
    return List.of(
      q.add(qri.get(0)),
      qri.get(1).shiftDown(sigma)); }

  //--------------------------------------------------------------

  public static final List<Natural> 
  divideAndRemainder (final Natural u,
                      final Natural v) {
    assert (! v.isZero());
    final List<Natural> qr;
    if (useKnuthDivision(u,v)) { 
      qr = NaturalDivide.divideAndRemainderKnuth(u,v); }
    else { 
      qr = NaturalDivide.divideAndRemainderBurnikelZiegler(u,v); }
    return List.of(qr.get(0).immutable(),qr.get(1).immutable()); }

  //--------------------------------------------------------------
  // gcd
  //--------------------------------------------------------------
  /** Algorithm B from Knuth section 4.5.2 */

  private static final Natural gcdKnuth (final Natural u,
                                         final Natural v) {
    Natural a = u.recyclable(u);
    Natural b = v.recyclable(v);
    // B1
    final int sa = a.loBit();
    final int s = Math.min(sa,b.loBit());
    if (s!=0) { a = a.shiftDown(s); b = b.shiftDown(s); }
    // B2
    int tsign = (s==sa) ? -1 : 1;
    Natural t = (0<tsign) ? a : b;
    for (int lb=t.loBit();lb>=0;lb=t.loBit()) {
      // B3 and B4
      t = t.shiftDown(lb);
      // step B5
      if (0<tsign) { a = t; }
      else { b = t; }
      final int an = a.endWord();
      final int bn = b.endWord();
      if ((an<2) && (bn<2)) {
        final int x = a.word(an-1);
        final int y = b.word(bn-1);
        Natural r = u.from(Ints.unsignedGcd(x,y));
        if (s > 0) { r = r.shiftUp(s); }
        return r; }
      // B6
      tsign = a.compareTo(b);
      if (0==tsign) { break; }
      else if (0<tsign) { a = a.subtract(b); t = a;  }
      else { b = b.subtract(a); t = b; } }
    if (s > 0) { a = a.shiftUp(s); }
    return a; }

  //--------------------------------------------------------------
  /** Use Euclid until the numbers are approximately the
   * same length, then use the Knuth algorithm.
   */

  public static final Natural gcd (final Natural u,
                                   final Natural v) {
    Natural a = u.recyclable(u);
    Natural b = v.recyclable(v);
    while (b.endWord() != 0) {
      if (Math.abs(a.endWord()-b.endWord()) < 2) { 
        return gcdKnuth(a,b).immutable(); }
      final List<Natural> qr = divideAndRemainder(a,b);
      a = b;
      final Natural r = qr.get(1);
      b = r.recyclable(r); }
    return a.immutable(); }

  public static final List<Natural> reduce (final Natural n0,
                                            final Natural d0) {
    final int shift = Math.min(n0.loBit(),d0.loBit());
    final Natural n = ((shift != 0) ? n0.shiftDown(shift) : n0);
    final Natural d = ((shift != 0) ? d0.shiftDown(shift) : d0);
    if (n.equals(d)) { return List.of(n0.one(),n0.one()); }
    if (d.isOne()) { return List.of(n,n0.one()); }
    if (n.isOne()) { return List.of(n0.one(),d); }
    final Natural g = n.gcd(d);
    if (g.compareTo(n0.one()) > 0) {
      return List.of(n.divide(g),d.divide(g)); }
    return List.of(n,d); }

  //--------------------------------------------------------------
  // disable constructor
  //--------------------------------------------------------------

  private NaturalDivide () {
    throw new
    UnsupportedOperationException(
      "can't instantiate " + getClass().getCanonicalName()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
