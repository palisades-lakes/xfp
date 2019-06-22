package xfp.java.numbers;

import static xfp.java.numbers.Numbers.hiBit;

import java.math.BigDecimal;
import java.util.Objects;

import xfp.java.exceptions.Exceptions;

/** A sign times a {@link NaturalBEI} significand times 2 to a
 * <code>int</code> exponent.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-06-20
 */

public final class BigFloatNBEI
extends Number
implements Ringlike<BigFloatNBEI> {

  private static final long serialVersionUID = 1L;

  //--------------------------------------------------------------
  // instance fields and methods
  //--------------------------------------------------------------

  private final boolean _nonNegative;
  public final boolean nonNegative () { return _nonNegative; }

  private final int _exponent;
  public final int exponent () { return _exponent; }

  // must always be non-negative
  private final NaturalBEI _significand;
  public final NaturalBEI significand () { return _significand; }

  //--------------------------------------------------------------

  @Override
  public final boolean isZero () {
    return significand().isZero(); }

  @Override
  public final boolean isOne () {
    return BigFloatNBEI.this.isOne(); }

  //--------------------------------------------------------------

  @Override
  public final BigFloatNBEI negate () {
    if (isZero()) { return this; }
    return valueOf(! nonNegative(),significand(),exponent()); }

  //--------------------------------------------------------------

  private static final BigFloatNBEI
  add (final boolean p0,
       final NaturalBEI t0,
       final int e0,
       final boolean p1,
       final NaturalBEI t1,
       final int e1) {

    if (p0 ^ p1) { // different signs
      final NaturalBEI t02,t12;
      final int e2;
      if (e0<e1) {
        t02 = t0; t12 = t1.shiftLeft(e1-e0); e2 = e0; }
      else if (e0>e1) {
        t02 = t0.shiftLeft(e0-e1); t12 = t1; e2 = e1; }
      else {
        t02 = t0; t12 = t1; e2 = e1; }
      final int c01 = t02.compareTo(t12);
      if (0==c01) { return ZERO; }
      // t12 > t02
      if (0 > c01) { return valueOf(p1,t12.subtract(t02),e2); }
      // t02 > t12
      return valueOf(p0,t02.subtract(t12),e2); }

    // same signs
    if (e0<e1) { return valueOf(p0,t0.add(t1,e1-e0),e0);}
    if (e0>e1) { return valueOf(p0,t1.add(t0,e0-e1),e1);}
    return valueOf(p0,t0.add(t1),e0);}

  //--------------------------------------------------------------

  @Override
  public final BigFloatNBEI
  add (final BigFloatNBEI q) {
    return add(
      nonNegative(),
      significand(),
      exponent(),
      q.nonNegative(),
      q.significand(),
      q.exponent()); }

  //--------------------------------------------------------------
  // 0 leftShift on t1

  private static final BigFloatNBEI
  addSameExponent (final boolean p0,
                   final NaturalBEI t0,
                   final boolean p1,
                   final long t1,
                   final int e) {
    assert 0L<=t1;
    if (p0 ^ p1) { // different signs
      final int c = t0.compareTo(t1);
      if (0==c) { return ZERO; }
      // t1 > t0
      if (0 > c) {
        final NaturalBEI t = t0.subtractFrom(t1);
        return valueOf(p1,t,e); }
      // t0 > t1
      final NaturalBEI t = t0.subtract(t1);
      return valueOf(p0,t,e); }
    final NaturalBEI t = t0.add(t1);
    return valueOf(p0,t,e); }

  private static final BigFloatNBEI
  addSameExponent (final boolean n0,
                   final NaturalBEI t0,
                   final boolean n1,
                   final long t1,
                   final int leftShift,
                   final int e) {
    if (n0 ^ n1) { // different signs
      final int c = t0.compareTo(t1,leftShift);
      if (0==c) { return ZERO; }
      // t1 > t0
      if (0 > c) {
        final NaturalBEI t = t0.subtractFrom(t1,leftShift);
        return valueOf(n1,t,e); }
      // t0 > t1
      final NaturalBEI t = t0.subtract(t1,leftShift);
      return valueOf(n0,t,e); }
    final NaturalBEI t = t0.add(t1,leftShift);
    return valueOf(n0,t,e); }

  private static final int compare (final long m0,
                                    final long m1,
                                    final int bitShift) {
    assert 0L<=m0;
    assert 0L<=m1;
    assert 0<=bitShift : "bitShift=" + bitShift;
    if (0==bitShift) { return Long.compare(m0,m1); }
    if (0L==m1) {
      if (0L==m0) { return 0; }
      return 1; }
    if (0L==m0) { return -1; }
    final int hiBit0 = hiBit(m0);
    final int hiBit1 = hiBit(m1) + bitShift;
    if (hiBit0<hiBit1) { return -1; }
    if (hiBit0>hiBit1) { return 1; }
    // shifted m1 must fit in one long, since hiBit0 < 64
    final long m11 = (m1<<bitShift);
    return Long.compare(m0,m11); }

  private static final BigFloatNBEI
  addSameExponent (final boolean n0,
                   final long t0,
                   final boolean n1,
                   final long t1,
                   final int lShift,
                   final int e) {
    if (n0 ^ n1) { // different signs
      final int c = compare(t0,t1,lShift);
      if (0==c) { return ZERO; }
      if (0>c) { // t1 > t0
        final NaturalBEI t = NaturalBEI.subtract(t1,lShift,t0);
        return valueOf(n1,t,e); }
      // t0 > t1
      final NaturalBEI t = NaturalBEI.subtract(t0,t1,lShift);
      return valueOf(n0,t,e); }
    final NaturalBEI t = NaturalBEI.add(t0,t1,lShift);
    return valueOf(n0,t,e); }

  //--------------------------------------------------------------

  private static final BigFloatNBEI
  add (final boolean p0,
       final NaturalBEI t0,
       final int e0,
       final boolean p1,
       final long t11,
       final int e11) {
    //assert 0L<=t11;
    if (0L==t11) { return valueOf(p0,t0,e0); }
    // minimize long bits
    final int shift = Numbers.loBit(t11);
    final long t1 = (t11>>>shift);
    final int e1 = e11+shift;

    if (e0<=e1) { return addSameExponent(p0,t0,p1,t1,e1-e0,e0); }
    return addSameExponent(p0,t0.shiftLeft(e0-e1),p1,t1,e1); }

  //--------------------------------------------------------------

  private static final BigFloatNBEI
  add (final boolean p0,
       final long t00,
       final int e00,
       final boolean p1,
       final long t11,
       final int e11) {

    //assert 0L<=t00;
    //assert 0L<=t11;

    // minimize long bits
    final int shift0 = Numbers.loBit(t00);
    // 64==shift0 if t00 is zero
    if (64==shift0) { return valueOf(p1,t11,e11); }
    final long t0=(t00>>>shift0);
    final int e0=e00+shift0;

    final int shift1 = Numbers.loBit(t11);
    // 64==shift1 if t11 is zero
    if (64==shift1) { return valueOf(p0,t0,e0); }
    final long t1=(t11>>>shift1);
    final int e1=e11+shift1;

    final int de = e1-e0;
    if (0<=de) { return addSameExponent(p0,t0,p1,t1,de,e0); }
    return addSameExponent(p1,t1,p0,t0,-de,e1); }

  //--------------------------------------------------------------

  public final BigFloatNBEI
  add (final double z) {
    assert Double.isFinite(z);
    return add(
      nonNegative(),
      significand(),
      exponent(),
      Doubles.nonNegative(z),
      Doubles.significand(z),
      Doubles.exponent(z)); }

  //--------------------------------------------------------------

  @Override
  public final BigFloatNBEI
  subtract (final BigFloatNBEI q) {
    return add(
      nonNegative(),
      significand(),
      exponent(),
      ! q.nonNegative(),
      q.significand(),
      q.exponent()); }

  public final BigFloatNBEI
  subtract (final double z) {
    return add(
      nonNegative(),
      significand(),
      exponent(),
      ! Doubles.nonNegative(z),
      Doubles.significand(z),
      Doubles.exponent(z)); }

  public static final BigFloatNBEI
  subtract (final double z0,
            final double z1) {
    return
      add(
        Doubles.nonNegative(z0),
        Doubles.significand(z0),
        Doubles.exponent(z0),
        ! Doubles.nonNegative(z1),
        Doubles.significand(z1),
        Doubles.exponent(z1)); }

  //--------------------------------------------------------------

  @Override
  public final BigFloatNBEI
  abs () {
    if (nonNegative()) { return this; }
    return negate(); }

  //--------------------------------------------------------------
  // used in Rational.addWithDenom()?

  public static final BigFloatNBEI
  multiply (final NaturalBEI x0,
            final boolean p1,
            final long x1) {
    assert 0L<=x1;
    final int e0 = x0.loBit();
    final int e1 = Numbers.loBit(x1);
    final NaturalBEI y0 = ((0==e0) ? x0 : x0.shiftRight(e0));
    final long y1 = (((0==e1)||(64==e1)) ? x1 : (x1 >>> e1));
    return valueOf(p1,y0.multiply(y1),e0+e1); }

  private final BigFloatNBEI
  multiply (final boolean p,
            final NaturalBEI t,
            final int e) {
    return valueOf(
      (! (nonNegative() ^ p)),
      significand().multiply(t),
      Math.addExact(exponent(),e)); }

  @Override
  public final BigFloatNBEI
  multiply (final BigFloatNBEI q) {
    return
      multiply(q.nonNegative(),q.significand(),q.exponent()); }

  public final BigFloatNBEI
  multiply (final double z) {
    assert Double.isFinite(z);
    return
      multiply(
        Doubles.nonNegative(z),
        NaturalBEI.valueOf(Doubles.significand(z)),
        Doubles.exponent(z)); }

  //--------------------------------------------------------------

  @Override
  public final BigFloatNBEI
  square () {
    return valueOf(true,significand().square(),(exponent()<<1)); }

  //--------------------------------------------------------------

  public final BigFloatNBEI
  add2 (final double z) {
    assert Double.isFinite(z);
    final long tz = Doubles.significand(z);
    final int ez = Doubles.exponent(z);
    final int s = Numbers.loBit(tz);
    final long t;
    final int e;
    if ((0==s) || (64==s)) { t=tz; e=ez; }
    else { t=(tz>>>s); e=ez+s; }
    final NaturalBEI t2 = NaturalBEI.square(t);
    final int e2 = (e<<1);
    return add(
      nonNegative(),
      significand(),
      exponent(),
      true,
      t2,
      e2); }

  //--------------------------------------------------------------

  //  public final BigFloat
  //  addL1 (final double z0,
  //         final double z1) {
  //    assert Double.isFinite(z0);
  //    assert Double.isFinite(z1);
  //    final BigFloat dz = subtract(z0,z1);
  //    return add(
  //      nonNegative(),
  //      significand(),
  //      exponent(),
  //      true,
  //      dz.significand(),
  //      dz.exponent()); }

  public BigFloatNBEI addL1 (final double z0,
                             final double z1) {
    // later adds should catch non-finite inputs
    //assert Double.isFinite(z0);
    //assert Double.isFinite(z1);
    // preserve exactness using twoAdd to convert to 2 adds.
    final double dz = z0 - z1;
    final double ddz = dz - z0;
    final double e = (z0 - (dz - ddz)) + ((-z1) - ddz);
    if (0<=dz) {
      if (0<=e) { return add(dz).add(e); }
      if (Math.abs(e)<=Math.abs(dz)) { return add(dz).add(e); }
      return add(-dz).add(-e); }
    // 0>dz
    if (0>e) { return add(-dz).add(-e); }
    if (Math.abs(e)<=Math.abs(dz)) { return add(-dz).add(-e); }
    return add(dz).add(e); }

  //--------------------------------------------------------------

  public final BigFloatNBEI
  addL2 (final double z0,
         final double z1) {
    assert Double.isFinite(z0);
    assert Double.isFinite(z1);
    final BigFloatNBEI dz = subtract(z0,z1);
    final NaturalBEI t2 = dz.significand().square();
    final int e2 = 2*dz.exponent();
    return add(
      nonNegative(),
      significand(),
      exponent(),
      true,
      t2,
      e2); }

  //--------------------------------------------------------------

  public final BigFloatNBEI
  addProduct (final double z0,
              final double z1) {
    if ((0.0==z0) || (0.0==z1)) { return this; }
    assert Double.isFinite(z0);
    assert Double.isFinite(z1);
    final boolean p0 = Doubles.nonNegative(z0);
    final long t0 = Doubles.significand(z0);
    final int e0 = Doubles.exponent(z0);
    final boolean p1 = Doubles.nonNegative(z1);
    final long t1 = Doubles.significand(z1);
    final int e1 = Doubles.exponent(z1);
    final int shift0 = Numbers.loBit(t0);
    final long t00 = (t0>>>shift0);
    final int e00 = e0+shift0;
    final int shift1 = Numbers.loBit(t1);
    final long t11 = (t1>>>shift1);
    final int e11=e1+shift1;
    return
      add(
        nonNegative(),
        significand(),
        exponent(),
        ! (p0 ^ p1),
        NaturalBEI.multiply(t00,t11),
        e00+e11); }

  //  public final BigFloat
  //  addProduct (final double z0,
  //              final double z1) {
  //    // use twoMul to convert exactly to 2 adds.
  //    final double z01 = z0*z1;
  //    assert Double.isFinite(z01);
  //    final double e = Math.fma(z0,z1,-z01);
  //    return add(z01).add(e); }

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

  private static final boolean roundUp (final NaturalBEI s,
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
    final NaturalBEI s0 = significand();
    final int e0 = exponent();
    if (s0.isZero()) { return (nn ? 0.0F : -0.0F); }

    final int eh = s0.hiBit();
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
    final NaturalBEI s0 = significand();
    final int e0 = exponent();
    if (s0.isZero()) { return (nn ? 0.0 : -0.0); }
    final int eh = s0.hiBit();
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
  public final int compareTo (final BigFloatNBEI q) {

    if (nonNegative() && (! q.nonNegative())) { return 1; }
    if ((! nonNegative()) && q.nonNegative()) { return -1; }
    // same signs
    final NaturalBEI t0 = significand();
    final NaturalBEI t1 = q.significand();
    final int e0 = exponent();
    final int e1 = q.exponent();
    final int c;
    if (e0 <= e1) { c = t0.compareTo(t1.shiftLeft(e1-e0)); }
    else { c = t0.shiftLeft(e0-e1).compareTo(t1); }
    return (nonNegative() ? c : -c); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  public final boolean equals (final BigFloatNBEI q) {
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
    if (!(o instanceof BigFloatNBEI)) { return false; }
    return equals((BigFloatNBEI) o); }

  @Override
  public int hashCode () {
    int h = 17;
    h = (31*h) + (nonNegative() ? 0 : 1);
    h = (31*h) + exponent();
    h = (31*h) + Objects.hash(significand());
    return h; }

  @Override
  public final String toString () {
    assert (0==significand().loBit())
    || significand().isZero() :
      significand().toString()
      + "\nlo= " + significand().loBit();
    return
      (nonNegative() ? "" : "-")
      + "0x" + significand().toString()
      + "p" + exponent(); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private BigFloatNBEI (final boolean nonNegative,
                        final NaturalBEI t0,
                        final int e0) {
    final int e1 = Math.max(0,t0.loBit());
    _nonNegative = nonNegative;
    if (e1==0) {
      _significand = t0;
      _exponent = e0; }
    else {
      _significand = t0.shiftRight(e1);
      _exponent = Math.addExact(e0,e1); } }

  //--------------------------------------------------------------

  public static final BigFloatNBEI valueOf (final boolean nonNegative,
                                            final NaturalBEI t,
                                            final int e) {
    if (t.isZero()) { return ZERO; }
    return new BigFloatNBEI(nonNegative,t,e); }

  public static final BigFloatNBEI valueOf (final long t,
                                            final int e) {
    if (0L==t) { return ZERO; }
    if (0L < t) {
      return valueOf(true,NaturalBEI.valueOf(t),e); }
    return valueOf(false,NaturalBEI.valueOf(-t),e); }

  public static final BigFloatNBEI valueOf (final int t,
                                            final int e) {
    if (0==t) { return ZERO; }
    if (0<t) { return valueOf(true,NaturalBEI.valueOf(t),e); }
    return valueOf(false,NaturalBEI.valueOf(-t),e); }

  //--------------------------------------------------------------

  private static final BigFloatNBEI valueOf (final boolean nonNegative,
                                             final long t0,
                                             final int e0)  {
    if (0L==t0) { return ZERO; }
    assert 0L<t0;
    final int shift = Numbers.loBit(t0);
    final long t1;
    final int e1;
    if ((0==shift)||(64==shift)) { t1=t0; e1=e0; }
    else { t1 = (t0 >>> shift); e1 = e0 + shift; }
    return valueOf(nonNegative,NaturalBEI.valueOf(t1),e1); }

  public static final BigFloatNBEI valueOf (final double z)  {
    return valueOf(
      Doubles.nonNegative(z),
      Doubles.significand(z),
      Doubles.exponent(z)); }

  //--------------------------------------------------------------

  private static final BigFloatNBEI valueOf (final boolean nonNegative,
                                             final int t0,
                                             final int e0)  {
    if (0==t0) { return ZERO; }
    return valueOf(nonNegative,NaturalBEI.valueOf(t0),e0); }

  public static final BigFloatNBEI valueOf (final float x)  {
    return valueOf(
      Floats.nonNegative(x),
      Floats.significand(x),
      Floats.exponent(x)); }

  //--------------------------------------------------------------

  public static final BigFloatNBEI valueOf (final byte t)  {
    if (0<=t) { return valueOf(true,NaturalBEI.valueOf(t),0); }
    return valueOf(false,NaturalBEI.valueOf(-t),0); }

  public static final BigFloatNBEI valueOf (final short t)  {
    if (0<=t) { return valueOf(true,NaturalBEI.valueOf(t),0); }
    return valueOf(false,NaturalBEI.valueOf(-t),0); }

  public static final BigFloatNBEI valueOf (final int t)  {
    if (0<=t) { return valueOf(true,NaturalBEI.valueOf(t),0); }
    return valueOf(false,NaturalBEI.valueOf(-t),0); }

  public static final BigFloatNBEI valueOf (final long t)  {
    if (0<=t) { return valueOf(true,NaturalBEI.valueOf(t),0); }
    return valueOf(false,NaturalBEI.valueOf(-t),0); }

  //--------------------------------------------------------------

  public static final BigFloatNBEI valueOf (final Double x)  {
    return valueOf(x.doubleValue()); }

  public static final BigFloatNBEI valueOf (final Float x)  {
    return valueOf(x.floatValue()); }

  public static final BigFloatNBEI valueOf (final Byte x)  {
    return valueOf(x.byteValue()); }

  public static final BigFloatNBEI valueOf (final Short x)  {
    return valueOf(x.shortValue()); }

  public static final BigFloatNBEI valueOf (final Integer x)  {
    return valueOf(x.intValue()); }

  public static final BigFloatNBEI valueOf (final Long x)  {
    return valueOf(x.longValue()); }

  public static final BigFloatNBEI valueOf (final BigDecimal x)  {
    throw Exceptions.unsupportedOperation(null,"valueOf",x); }

  public static final BigFloatNBEI valueOf (final NaturalBEI x)  {
    return valueOf(true,x,0); }

  public static final BigFloatNBEI valueOf (final Number x)  {
    if (x instanceof BigFloatNBEI) { return (BigFloatNBEI) x; }
    if (x instanceof Double) { return valueOf((Double) x); }
    if (x instanceof Float) { return valueOf((Float) x); }
    if (x instanceof Byte) { return valueOf((Byte) x); }
    if (x instanceof Short) { return valueOf((Short) x); }
    if (x instanceof Integer) { return valueOf((Integer) x); }
    if (x instanceof Long) { return valueOf((Long) x); }
    if (x instanceof NaturalBEI) { return valueOf((NaturalBEI) x); }
    if (x instanceof BigDecimal) { return valueOf((BigDecimal) x); }
    throw Exceptions.unsupportedOperation(null,"valueOf",x); }

  public static final BigFloatNBEI valueOf (final Object x)  {
    return valueOf((Number) x); }

  //--------------------------------------------------------------

  public static final BigFloatNBEI ZERO =
    new BigFloatNBEI(true,NaturalBEI.ZERO,0);

  public static final BigFloatNBEI ONE =
    new BigFloatNBEI(true,NaturalBEI.ONE,0);

  public static final BigFloatNBEI TWO =
    new BigFloatNBEI(true,NaturalBEI.ONE,1);

  public static final BigFloatNBEI TEN =
    new BigFloatNBEI(true,NaturalBEI.valueOf(5),1);

  public static final BigFloatNBEI MINUS_ONE =
    new BigFloatNBEI(false,NaturalBEI.ONE,0);

  //--------------------------------------------------------------
}
//--------------------------------------------------------------