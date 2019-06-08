package xfp.java.numbers;

import java.math.BigDecimal;
import java.util.Objects;

import xfp.java.exceptions.Exceptions;

/** A sign times a {@link UnNatural} significand times 2 to a
 * <code>int</code> exponent.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-06-07
 */

public final class BigFloat
extends Number
implements Ringlike<BigFloat> {

  private static final long serialVersionUID = 1L;

  //--------------------------------------------------------------
  // instance fields and methods
  //--------------------------------------------------------------

  private final boolean _nonNegative;
  public final boolean nonNegative () { return _nonNegative; }

  private final int _exponent;
  public final int exponent () { return _exponent; }

  // must always be non-negative
  private final UnNatural _significand;
  public final UnNatural significand () { return _significand; }

  //--------------------------------------------------------------

  public final boolean isZero () {
    return significand().isZero(); }

  public final boolean isOne () {
    return BigFloat.ONE.equals(this); }

  //--------------------------------------------------------------

  @Override
  public final BigFloat negate () {
    if (isZero()) { return this; }
    return valueOf(! nonNegative(),significand(),exponent()); }

  //--------------------------------------------------------------
  // 0 leftShift

  private static final BigFloat addSameExponent (final boolean p0,
                                                 final UnNatural t0,
                                                 final int e,
                                                 final boolean p1,
                                                 final long t1) {
    assert 0L<=t1;
    // as long as only called from add(boolean,long,int)
    // we don't need to check these
    //if (0L==t1) { return valueOf(p0,t0,e); }
    if (p0 ^ p1) { // different signs
      final int c = t0.compareTo(t1);
      if (0==c) { return ZERO; }
      // t1 > t0
      if (0 > c) {
        final UnNatural t = t0.subtractFrom(t1);
        return valueOf(p1,t,e); }
      // t0 > t1
      final UnNatural t = t0.subtract(t1);
      return valueOf(p0,t,e); }
    final UnNatural t = t0.add(t1);
    return valueOf(p0,t,e); }

  private static final BigFloat add (final boolean n0,
                                     final UnNatural t0,
                                     final int e,
                                     final boolean n1,
                                     final long t1,
                                     final int leftShift) {
    // as long as only called from add(boolean,long,int)
    // we don't need to check these
    //if (t0.isZero()) { return valueOf(n1,t1,leftShift); }
    //if (0L==t1) { return valueOf(n0,t0,e); }
    //assert 0L < t1;
    //if (0==leftShift) { return add(n0,t0,e,n1,t1); }
    //assert 0<leftShift;
    if (n0 ^ n1) { // different signs
      final int c = t0.compareTo(t1,leftShift);
      if (0==c) { return ZERO; }
      // t1 > t0
      if (0 > c) {
        final UnNatural t = t0.subtractFrom(t1,leftShift);
        return valueOf(n1,t,e); }
      // t0 > t1
      final UnNatural t = t0.subtract(t1,leftShift);
      return valueOf(n0,t,e); }
    final UnNatural t = t0.add(t1,leftShift);
    return valueOf(n0,t,e); }

//  private static final BigFloat add (final boolean n0,
//                                     final long t0,
//                                     final int e0,
//                                     final boolean n1,
//                                     final long t1,
//                                     final int e1) {
//    if (n0 ^ n1) { // different signs
//      final int c = Bei.compare(t0,e0,t1,e1);
//      if (0==c) { return ZERO; }
//      // t1 > t0
//      if (0 > c) {
//        final UnNatural t = UnNatural.subtract(t1,e1,t0);
//        return valueOf(n1,t,e0); }
//      // t0 > t1
//      final UnNatural t = UnNatural.subtract(t0,t1,e1);
//      return valueOf(n0,t,e0); }
//    final UnNatural t = UnNatural.add(t0,t1,e1);
//    return valueOf(n0,t,e0); }

  //--------------------------------------------------------------

  private final BigFloat add (final boolean p1,
                              final long t11,
                              final int e11) {
    assert 0L<=t11;
    //if (0==t11) { return this; }
    //if (isZero()) { return valueOf(p1,t11,e11); }

    final boolean p0 = nonNegative();
    final UnNatural t0 = significand();
    final int e0 = exponent();

    final int shift = Numbers.loBit(t11);
    final long t1;
    final int e1;
    
    // 64==shift if t11 is zero
    if (0==shift) { t1=t11; e1=e11; }
    else if (64==shift) { t1=0L; e1=e0; }
    else { t1=(t11>>>shift); e1=e11+shift; }

    // adjust significands to the same exponent
    final int de = e1-e0;
    if (0<de) { return add(p0,t0,e0,p1,t1,de); }
    if (0==de) { return addSameExponent(p0,t0,e1,p1,t1); }
    final UnNatural ts = t0.shiftLeft(-de);
    return addSameExponent(p0,ts,e1,p1,t1); }

  //--------------------------------------------------------------

  public final BigFloat add (final double z) {
    assert Double.isFinite(z);
    return add(
      Doubles.nonNegative(z),
      Doubles.significand(z),
      Doubles.exponent(z)); }

  //--------------------------------------------------------------

  private final BigFloat add (final boolean n1,
                              final UnNatural t1,
                              final int e1) {
    //if (isZero()) { return valueOf(n1,t1,e1); }
    //if (t1.isZero()) { return this; }

    final boolean n0 = nonNegative();
    final UnNatural t0 = significand();
    final int e0 = exponent();

    final UnNatural t02,t12;
    final int e2;
    final int de = e1-e0;
    if (de > 0) {
      t02 = t0; t12 = t1.shiftLeft(de); e2 = e0; }
    else if (de < 0) {
      t02 = t0.shiftLeft(-de); t12 = t1; e2 = e1; }
    else {
      t02 = t0; t12 = t1; e2 = e1; }

    if (n0 ^ n1) { // different signs
      final int c01 = t02.compareTo(t12);
      if (0==c01) { return ZERO; }
      // t12 > t02
      if (0 > c01) { return valueOf(n1,t12.subtract(t02),e2); }
      // t02 > t12
      return valueOf(n0,t02.subtract(t12),e2); }

    return valueOf(n0,t02.add(t12),e2); }

  //--------------------------------------------------------------

  @Override
  public final BigFloat add (final BigFloat q) {
    //if (isZero()) { return q; }
    //if (q.isZero()) { return this; }
    return add(q.nonNegative(),q.significand(),q.exponent()); }

  //--------------------------------------------------------------

  @Override
  public final BigFloat subtract (final BigFloat q) {
    //if (isZero()) { return q.negate(); }
    //if (q.isZero()) { return this; }
    return add(
      ! q.nonNegative(),
      q.significand(),
      q.exponent()); }

  public final BigFloat subtract (final double z) {
    return add(-z); }

  public static final BigFloat subtract (final double z0,
                                         final double z1) {
    return valueOf(z0).add(
      ! Doubles.nonNegative(z1),
      Doubles.significand(z1),
      Doubles.exponent(z1)); }

  //--------------------------------------------------------------

  @Override
  public final BigFloat abs () {
    if (nonNegative()) { return this; }
    return negate(); }

  //--------------------------------------------------------------
  // used in Rational.addWithDenom()?

  public static final BigFloat multiply (final UnNatural x0,
                                         final boolean p1,
                                         final long x1) {
    assert 0L<=x1;
    //if (x0.isZero()) { return ZERO; }
    //if (UnNatural.ONE.equals(x0)) { return valueOf(p1,x1,0); }
    //if (1L==x1) { return p1 ? ONE : MINUS_ONE; }

    final int e0 = Numbers.loBit(x0);
    final int e1 = Numbers.loBit(x1);
    final UnNatural y0 = ((0==e0) ? x0 : x0.shiftRight(e0));
    final long y1 = ((0==e1) ? x1 : (x1 >>> e1));
    return valueOf(p1,y0.multiply(y1),e0+e1); }

  private final BigFloat multiply (final boolean p,
                                   final UnNatural t,
                                   final int e) {
    //if (isZero()) { return ZERO; }
    //if (t.isZero()) { return ZERO; }
    //if (isOne()) { return valueOf(p,t,e); }
    return valueOf(
      (! (nonNegative() ^ p)),
      significand().multiply(t),
      Math.addExact(exponent(),e)); }

  @Override
  public final BigFloat multiply (final BigFloat q) {
    //if (isZero() ) { return ZERO; }
    //if (q.isZero()) { return ZERO; }
    //if (q.isOne()) { return this; }
    //if (isOne()) { return q; }
    return
      multiply(q.nonNegative(),q.significand(),q.exponent()); }

  public final BigFloat multiply (final double z) {
    assert Double.isFinite(z);
    return
      multiply(
        Doubles.nonNegative(z),
        UnNatural.valueOf(Doubles.significand(z)),
        Doubles.exponent(z)); }

  //--------------------------------------------------------------

  public final BigFloat square () {
    //if (isZero() ) { return ZERO; }
    //if (isOne()) { return ONE; }
    return valueOf(true,significand().square(),2*exponent()); }

  //--------------------------------------------------------------

  private final BigFloat add2 (final long t0,
                               final int e0) {
    final int shift = Numbers.loBit(t0);
    final long t1;
    final int e1;
    if (0==shift) { t1=t0; e1=e0; }
    else { t1=(t0>>>shift); e1=e0+shift; }
    return add(true,UnNatural.square(t1),2*e1); }

  public final BigFloat add2 (final double z) {
    assert Double.isFinite(z);
    return add2(Doubles.significand(z),Doubles.exponent(z)); }

//--------------------------------------------------------------

  public final BigFloat addL2 (final double z0,
                               final double z1) {
    assert Double.isFinite(z0);
    assert Double.isFinite(z1);
    final BigFloat dz = subtract(z0,z1);
    final BigFloat dz2 = dz.square();
    final BigFloat after = add(dz2);
    return after; }

  //--------------------------------------------------------------

  private final BigFloat addProduct (final boolean p0,
                                     final long t0,
                                     final int e0,
                                     final boolean p1,
                                     final long t1,
                                     final int e1) {
    final int shift0 = Numbers.loBit(t0);
    final long t00;
    final int e00;
    if (0==shift0) { t00=t0; e00=e0; }
    else { t00=(t0>>>shift0); e00=e0+shift0; }
    final int shift1 = Numbers.loBit(t1);
    final long t11;
    final int e11;
    if (0==shift1) { t11=t1; e11=e1; }
    else { t11=(t1>>>shift1); e11=e1+shift1; }
    return 
      add(! (p0 ^ p1), UnNatural.multiply(t00,t11), e00+e11); }

  public final BigFloat addProduct (final double z0,
                                    final double z1) {
    assert Double.isFinite(z0);
    assert Double.isFinite(z1);
    final boolean p0 = Doubles.nonNegative(z0);
    final long t0 = Doubles.significand(z0);
    final int e0 = Doubles.exponent(z0);
    final boolean p1 = Doubles.nonNegative(z1);
    final long t1 = Doubles.significand(z1);
    final int e1 = Doubles.exponent(z1);
    return addProduct(p0,t0,e0,p1,t1,e1); }

  //--------------------------------------------------------------
  // Number methods
  //--------------------------------------------------------------
  /** Unsupported.
   *
   * TODO: should it really truncate or round instead? Or
   * should there be more explicit round, floor, ceil, etc.?
   */
  @Override
  public final int intValue () {
    throw Exceptions.unsupportedOperation(this,"longValue"); }

  /** Unsupported.
   *
   * TODO: should it really truncate or round instead? Or
   * should there be more explicit round, floor, ceil, etc.?
   */
  @Override
  public final long longValue () {
    throw Exceptions.unsupportedOperation(this,"longValue"); }

  //--------------------------------------------------------------

  private static final boolean roundUp (final UnNatural s,
                                        final int e) {
    if (! s.testBit(e-1)) { return false; }
    for (int i=e-2;i>=0;i--) {
      if (s.testBit(i)) { return true; } }
    return s.testBit(e); }

  //--------------------------------------------------------------
  /** Adjust exponent from viewing signifcand as an integer
   * to significand as a binary 'decimal'.
   * <p>
   * TODO: make integer/fractional significand consistent
   * across all classes. Probably convert to integer
   * interpretation everywhere.
   */

  private static final float floatMergeBits (final boolean nonNegative,
                                             final int significand,
                                             final int exponent) {
    final int sh = Numbers.hiBit(significand);
    if (sh > Doubles.SIGNIFICAND_BITS) {
      return (nonNegative
        ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY); }

    final int e =
      ((sh==Floats.SIGNIFICAND_BITS)
        ? exponent + Floats.STORED_SIGNIFICAND_BITS
          : Floats.SUBNORMAL_EXPONENT);

    return Floats.unsafeBits(nonNegative,e,significand); }

  /** @return closest half-even rounded <code>float</code>
   */

  @Override
  public final float floatValue () {
    final boolean nn = nonNegative();
    final UnNatural s0 = significand();
    final int e0 = exponent();
    if (s0.isZero()) { return (nn ? 0.0F : -0.0F); }

    final int eh = Numbers.hiBit(s0);
    final int es =
      Math.max(Floats.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND-e0,
        Math.min(
          Floats.MAXIMUM_EXPONENT_INTEGRAL_SIGNIFICAND-e0 -1,
          eh-Floats.SIGNIFICAND_BITS));
    if (0==es) {
      return floatMergeBits(nn,s0.intValue(),e0); }
    if (0 > es) {
      final int e1 = e0 + es;
      final int s1 = (s0.intValue() << -es);
      return floatMergeBits(nn,s1,e1); }
    if (eh <= es) { return (nn ? 0.0F : -0.0F); }
    // eh > es > 0
    final boolean up = roundUp(s0,es);
    // TODO: faster way to select the right bits as a int?
    //final int s1 = s0.shiftRight(es).intValue();
    final int s1 = s0.getShiftedInt(es);
    final int e1 = e0 + es;
    if (up) {
      final int s2 = s1 + 1;
      if (Numbers.hiBit(s2) > Floats.SIGNIFICAND_BITS) { // carry
        // lost bit has to be zero, since there was just a carry
        final int s3 = (s2 >> 1);
        final int e3 = e1 + 1;
        return floatMergeBits(nn,s3,e3); }
      // no carry
      return floatMergeBits(nn,s2,e1); }
    // round down
    return floatMergeBits(nn,s1,e1); }

  //--------------------------------------------------------------
  /** Adjust exponent from viewing signifcand as an integer
   * to significand as a binary 'decimal'.
   * <p>
   * TODO: make integer/fractional significand consistent
   * across all classes. Probably convert to integer
   * interpretation everywhere.
   */

  private static final double doubleMergeBits (final boolean nonNegative,
                                               final long significand,
                                               final int exponent) {
    //assert 0L < significand;
    final int sh = Numbers.hiBit(significand);
    if (sh > Doubles.SIGNIFICAND_BITS) {
      return (nonNegative
        ? Double.POSITIVE_INFINITY 
          : Double.NEGATIVE_INFINITY); }

    final int e =
      ((sh==Doubles.SIGNIFICAND_BITS)
        ? exponent + Doubles.STORED_SIGNIFICAND_BITS
          : Doubles.SUBNORMAL_EXPONENT);

    return Doubles.unsafeBits(nonNegative,e,significand); }

  //--------------------------------------------------------------
  /** @return closest half-even rounded <code>double</code>
   */

  @Override
  public final double doubleValue () {
    final boolean nn = nonNegative();
    final UnNatural s0 = significand();
    final int e0 = exponent();
    if (s0.isZero()) { return (nn ? 0.0 : -0.0); }
    final int eh = Numbers.hiBit(s0);
    final int es =
      Math.max(Doubles.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND-e0,
        Math.min(
          Doubles.MAXIMUM_EXPONENT_INTEGRAL_SIGNIFICAND-e0-1,
          eh-Doubles.SIGNIFICAND_BITS));
    if (0==es) {
      return doubleMergeBits(nn,s0.longValue(),e0); }
    if (0 > es) {
      final int e1 = e0 + es;
      final long s1 = (s0.longValue() << -es);
      return doubleMergeBits(nn,s1,e1); }
    if (eh <= es) { return (nn ? 0.0 : -0.0); }
    // eh > es > 0
    final boolean up = roundUp(s0,es);
    final long s1 = s0.getShiftedLong(es);
    final int e1 = e0 + es;
    if (up) {
      final long s2 = s1 + 1L;
      if (Numbers.hiBit(s2) > Doubles.SIGNIFICAND_BITS) { // carry
        // lost bit has to be zero, since there was just a carry
        final long s3 = (s2 >> 1);
        final int e3 = e1 + 1;
        return doubleMergeBits(nn,s3,e3); }
      // no carry
      return doubleMergeBits(nn,s2,e1); }
    // round down
    return doubleMergeBits(nn,s1,e1); }

  //--------------------------------------------------------------
  // Comparable methods
  //--------------------------------------------------------------

  @Override
  public final int compareTo (final BigFloat q) {

    if (nonNegative() && (! q.nonNegative())) { return 1; }
    if ((! nonNegative()) && q.nonNegative()) { return -1; }
    // same signs
    final UnNatural t0 = significand();
    final UnNatural t1 = q.significand();
    final int e0 = exponent();
    final int e1 = q.exponent();
    final int c;
    if (e0 <= e1) { c = t0.compareTo(t1.shiftLeft(e1-e0)); }
    else { c = t0.shiftLeft(e0-e1).compareTo(t1); }
    return (nonNegative() ? c : -c); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  public final boolean equals (final BigFloat q) {
    if (this==q) { return true; }
    if (null==q) { return false; }
    // assuming reduced
    return
      (nonNegative()==q.nonNegative())
      &&
      (exponent()==q.exponent())
      &&
      significand().equals(q._significand); }

  @Override
  public boolean equals (final Object o) {
    if (!(o instanceof BigFloat)) { return false; }
    return equals((BigFloat) o); }

  @Override
  public int hashCode () {
    int h = 17;
    h = (31*h) + (nonNegative() ? 0 : 1);
    h = (31*h) + exponent();
    h = (31*h) + Objects.hash(significand());
    return h; }

  @Override
  public final String toString () {
    assert (0==Numbers.loBit(significand()))
    || significand().isZero() :
      significand().toString()
      + "\nlo= " + Numbers.loBit(significand());
    return
      (nonNegative() ? "" : "-")
      + "0x" + significand().toString()
      + "p" + exponent(); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private BigFloat (final boolean nonNegative,
                    final UnNatural t0,
                    final int e0) {
    final int e1 = Math.max(0,Numbers.loBit(t0));
    _nonNegative = nonNegative;
    if (e1==0) {
      _significand = t0;
      _exponent = e0; }
    else {
      _significand = t0.shiftRight(e1);
      _exponent = Math.addExact(e0,e1); } }

  //--------------------------------------------------------------

  public static final BigFloat valueOf (final boolean nonNegative,
                                        final UnNatural t,
                                        final int e) {
    if (t.isZero()) { return ZERO; }
    return new BigFloat(nonNegative,t,e); }

  public static final BigFloat valueOf (final long t,
                                        final int e) {
    if (0L==t) { return ZERO; }
    if (0L < t) { 
      return valueOf(true,UnNatural.valueOf(t),e); }
    return valueOf(false,UnNatural.valueOf(-t),e); }

  public static final BigFloat valueOf (final int t,
                                        final int e) {
    if (0==t) { return ZERO; }
    if (0<t) { return valueOf(true,UnNatural.valueOf(t),e); }
    return valueOf(false,UnNatural.valueOf(-t),e); }

  //--------------------------------------------------------------

  private static final BigFloat valueOf (final boolean nonNegative,
                                         final long t0,
                                         final int e0)  {
    if (0L==t0) { return ZERO; }
    assert 0L<t0;
    final int shift = Numbers.loBit(t0);
    final long t1;
    final int e1;
    if (0==shift) { t1=t0; e1=e0; }
    else { t1 = (t0 >>> shift); e1 = e0 + shift; }
    return valueOf(nonNegative,UnNatural.valueOf(t1),e1); }

  public static final BigFloat valueOf (final double x)  {
    return valueOf(
      Doubles.nonNegative(x),
      Doubles.significand(x),
      Doubles.exponent(x)); }

  //--------------------------------------------------------------

  private static final BigFloat valueOf (final boolean nonNegative,
                                         final int t0,
                                         final int e0)  {
    if (0==t0) { return ZERO; }
    return valueOf(nonNegative,UnNatural.valueOf(t0),e0); }

  public static final BigFloat valueOf (final float x)  {
    return valueOf(
      Floats.nonNegative(x),
      Floats.significand(x),
      Floats.exponent(x)); }

  //--------------------------------------------------------------

  public static final BigFloat valueOf (final byte t)  {
    if (0 < t) {
      return valueOf(true,UnNatural.valueOf(t),0); }
    return valueOf(false,UnNatural.valueOf(-t),0); }

  public static final BigFloat valueOf (final short t)  {
    if (0 < t) {
      return valueOf(true,UnNatural.valueOf(t),0); }
    return valueOf(false,UnNatural.valueOf(-t),0); }

  public static final BigFloat valueOf (final int t)  {
    if (0 < t) {
      return valueOf(true,UnNatural.valueOf(t),0); }
    return valueOf(false,UnNatural.valueOf(-t),0); }

  public static final BigFloat valueOf (final long t)  {
    if (0 < t) {
      return valueOf(true,UnNatural.valueOf(t),0); }
    return valueOf(false,UnNatural.valueOf(-t),0); }

  //--------------------------------------------------------------

  public static final BigFloat valueOf (final Double x)  {
    return valueOf(x.doubleValue()); }

  public static final BigFloat valueOf (final Float x)  {
    return valueOf(x.floatValue()); }

  public static final BigFloat valueOf (final Byte x)  {
    return valueOf(x.byteValue()); }

  public static final BigFloat valueOf (final Short x)  {
    return valueOf(x.shortValue()); }

  public static final BigFloat valueOf (final Integer x)  {
    return valueOf(x.intValue()); }

  public static final BigFloat valueOf (final Long x)  {
    return valueOf(x.longValue()); }

  public static final BigFloat valueOf (final BigDecimal x)  {
    throw Exceptions.unsupportedOperation(null,"valueOf",x); }
  //    return valueOf(x, UnNatural.ONE); }

  public static final BigFloat valueOf (final UnNatural x)  {
    return valueOf(true,x,0); }

  public static final BigFloat valueOf (final Number x)  {
    if (x instanceof BigFloat) { return (BigFloat) x; }
    if (x instanceof Double) { return valueOf((Double) x); }
    if (x instanceof Float) { return valueOf((Float) x); }
    if (x instanceof Byte) { return valueOf((Byte) x); }
    if (x instanceof Short) { return valueOf((Short) x); }
    if (x instanceof Integer) { return valueOf((Integer) x); }
    if (x instanceof Long) { return valueOf((Long) x); }
    if (x instanceof UnNatural) { return valueOf((UnNatural) x); }
    if (x instanceof BigDecimal) { return valueOf((BigDecimal) x); }
    throw Exceptions.unsupportedOperation(null,"valueOf",x); }

  public static final BigFloat valueOf (final Object x)  {
    return valueOf((Number) x); }

  //--------------------------------------------------------------

  public static final BigFloat ZERO =
    new BigFloat(true,UnNatural.ZERO,0);

  public static final BigFloat ONE =
    new BigFloat(true,UnNatural.ONE,0);

  public static final BigFloat TWO =
    new BigFloat(true,UnNatural.ONE,1);

  public static final BigFloat TEN =
    new BigFloat(true,UnNatural.valueOf(5),1);

  public static final BigFloat MINUS_ONE =
    new BigFloat(false,UnNatural.ONE,0);

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
