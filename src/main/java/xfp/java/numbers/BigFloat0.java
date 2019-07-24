
package xfp.java.numbers;

import java.math.BigDecimal;
import java.util.Objects;

import xfp.java.exceptions.Exceptions;

/** A sign times a {@link NaturalBEI0} significand times 2 to a
 * <code>int</code> exponent.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-07-23
 */

public final class BigFloat0
extends Number
implements Ringlike<BigFloat0> {

  private static final long serialVersionUID = 1L;

  //--------------------------------------------------------------
  // instance fields and methods
  //--------------------------------------------------------------

  private final boolean _nonNegative;
  public final boolean nonNegative () { return _nonNegative; }

  private final int _exponent;
  public final int exponent () { return _exponent; }

  // must always be non-negative
  private final NaturalBEI0 _significand;
  public final NaturalBEI0 significand () { return _significand; }

  //--------------------------------------------------------------

  @Override
  public final boolean isZero () {
    return significand().isZero(); }

  @Override
  public final boolean isOne () {
    return BigFloat0.this.isOne(); }

  //--------------------------------------------------------------

  @Override
  public final BigFloat0 negate () {
    if (isZero()) { return this; }
    return valueOf(! nonNegative(),significand(),exponent()); }

  //--------------------------------------------------------------

  private static final BigFloat0
  add (final boolean p0,
       final NaturalBEI0 t0,
       final int e0,
       final boolean p1,
       final NaturalBEI0 t1,
       final int e1) {

    final NaturalBEI0 t02,t12;
    final int e2;
    final int de = e1-e0;
    if (de > 0) {
      t02 = t0; t12 = t1.shiftUp(de); e2 = e0; }
    else if (de < 0) {
      t02 = t0.shiftUp(-de); t12 = t1; e2 = e1; }
    else {
      t02 = t0; t12 = t1; e2 = e1; }

    if (p0 ^ p1) { // different signs
      final int c01 = t02.compareTo(t12);
      if (0==c01) { return valueOf(0L); }
      // t12 > t02
      if (0 > c01) {
        return valueOf(p1,(NaturalBEI0) t12.subtract(t02),e2); }
      // t02 > t12
      return valueOf(p0,(NaturalBEI0) t02.subtract(t12),e2); }

    return valueOf(p0,(NaturalBEI0) t02.add(t12),e2); }

  //--------------------------------------------------------------

  @Override
  public final BigFloat0
  add (final BigFloat0 q) {
    return add(
      nonNegative(),
      significand(),
      exponent(),
      q.nonNegative(),
      q.significand(),
      q.exponent()); }

  //--------------------------------------------------------------
  // 0 upShift on t1

  private static final BigFloat0
  addSameExponent (final boolean p0,
                   final NaturalBEI0 t0,
                   final boolean p1,
                   final long t1,
                   final int e) {
    assert 0L<=t1;
    if (p0 ^ p1) { // different signs
      final int c = t0.compareTo(t1);
      if (0==c) { return valueOf(0L); }
      // t1 > t0
      if (0 > c) {
        final NaturalBEI0 t = t0.subtractFrom(t1);
        return valueOf(p1,t,e); }
      // t0 > t1
      final NaturalBEI0 t = (NaturalBEI0) t0.subtract(t1);
      return valueOf(p0,t,e); }
    final NaturalBEI0 t = t0.add(t1);
    return valueOf(p0,t,e); }

  private static final BigFloat0
  addSameExponent (final boolean n0,
                   final NaturalBEI0 t0,
                   final boolean n1,
                   final long t1,
                   final int upShift,
                   final int e) {
    if (n0 ^ n1) { // different signs
      final int c = t0.compareTo(t1,upShift);
      if (0==c) { return valueOf(0L); }
      // t1 > t0
      if (0 > c) {
        final NaturalBEI0 t = t0.subtractFrom(t1,upShift);
        return valueOf(n1,t,e); }
      // t0 > t1
      final NaturalBEI0 t = t0.subtract(t1,upShift);
      return valueOf(n0,t,e); }
    final NaturalBEI0 t = t0.add(t1,upShift);
    return valueOf(n0,t,e); }

  private static final BigFloat0
  addSameExponent (final boolean n0,
                   final long t0,
                   final boolean n1,
                   final long t1,
                   final int upShift,
                   final int e) {
    if (n0 ^ n1) { // different signs
      final int c = Bei0.compare(t0,t1,upShift);
      if (0==c) { return valueOf(0L); }
      if (0>c) { // t1 > t0
        final NaturalBEI0 t = NaturalBEI0.subtractFrom(t0,t1,upShift);
        return valueOf(n1,t,e); }
      // t0 > t1
      final NaturalBEI0 t = NaturalBEI0.subtract(t0,t1,upShift);
      return valueOf(n0,t,e); }
    final NaturalBEI0 t = NaturalBEI0.add(t0,t1,upShift);
    return valueOf(n0,t,e); }

  //--------------------------------------------------------------

  private static final BigFloat0
  add (final boolean p0,
       final NaturalBEI0 t0,
       final int e0,
       final boolean p1,
       final long t11,
       final int e11) {

    assert 0L<=t11;

    // minimize long bits
    final int shift = Numbers.loBit(t11);
    final long t1;
    final int e1;
    // 64==shift if t11 is zero
    if (0==shift) { t1=t11; e1=e11; }
    else if (64==shift) { t1=0L; e1=e0; }
    else { t1=(t11>>>shift); e1=e11+shift; }

    if (e0<=e1) { return addSameExponent(p0,t0,p1,t1,e1-e0,e0); }
    return addSameExponent(p0,t0.shiftUp(e0-e1),p1,t1,e1); }

  //--------------------------------------------------------------

  private static final BigFloat0
  add (final boolean p0,
       final long t00,
       final int e00,
       final boolean p1,
       final long t11,
       final int e11) {

    assert 0L<=t00;
    assert 0L<=t11;

    // minimize long bits
    final int shift0 = Numbers.loBit(t00);
    final long t0;
    final int e0;
    // 64==shift0 if t00 is zero
    if (0==shift0) { t0=t00; e0=e00; }
    else if (64==shift0) { t0=0L; e0=0; }
    else { t0=(t00>>>shift0); e0=e00+shift0; }

    final int shift1 = Numbers.loBit(t11);
    final long t1;
    final int e1;
    // 64==shift1 if t11 is zero
    if (0==shift1) { t1=t11; e1=e11; }
    else if (64==shift1) { t1=0L; e1=0; }
    else { t1=(t11>>>shift1); e1=e11+shift1; }

    if (e0<=e1) { return addSameExponent(p0,t0,p1,t1,e1-e0,e0); }
    return addSameExponent(p1,t1,p0,t0,e0-e1,e1); }

  //--------------------------------------------------------------

  public final BigFloat0
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
  public final BigFloat0
  subtract (final BigFloat0 q) {
    return add(
      nonNegative(),
      significand(),
      exponent(),
      ! q.nonNegative(),
      q.significand(),
      q.exponent()); }

  public final BigFloat0
  subtract (final double z) {
    return add(
      nonNegative(),
      significand(),
      exponent(),
      ! Doubles.nonNegative(z),
      Doubles.significand(z),
      Doubles.exponent(z)); }

  public static final BigFloat0
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
  public final BigFloat0
  abs () {
    if (nonNegative()) { return this; }
    return negate(); }

  //--------------------------------------------------------------
  // used in Rational.addWithDenom()?

  public static final BigFloat0
  multiply (final NaturalBEI0 x0,
            final boolean p1,
            final long x1) {
    assert 0L<=x1;
    final int e0 = Numbers.loBit(x0);
    final int e1 = Numbers.loBit(x1);
    final NaturalBEI0 y0 = ((0==e0) ? x0 : x0.shiftDown(e0));
    final long y1 = (((0==e1)||(64==e1)) ? x1 : (x1 >>> e1));
    return valueOf(p1,y0.multiply(y1),e0+e1); }

  private final BigFloat0
  multiply (final boolean p,
            final NaturalBEI0 t,
            final int e) {
    return valueOf(
      (! (nonNegative() ^ p)),
      (NaturalBEI0) significand().multiply(t),
      Math.addExact(exponent(),e)); }

  @Override
  public final BigFloat0
  multiply (final BigFloat0 q) {
    return
      multiply(q.nonNegative(),q.significand(),q.exponent()); }

  public final BigFloat0
  multiply (final double z) {
    assert Double.isFinite(z);
    return
      multiply(
        Doubles.nonNegative(z),
        NaturalBEI0.valueOf(Doubles.significand(z)),
        Doubles.exponent(z)); }

  //--------------------------------------------------------------

  @Override
  public final BigFloat0
  square () {
    //if (isZero() ) { return EMPTY; }
    //if (isOne()) { return ONE; }
    return valueOf(true,significand().square(),2*exponent()); }

  //--------------------------------------------------------------

  public final BigFloat0
  add2 (final double z) {
    assert Double.isFinite(z);
    final long tz = Doubles.significand(z);
    final int ez = Doubles.exponent(z);
    final int s = Numbers.loBit(tz);
    final long t;
    final int e;
    if ((0==s) || (64==s)) { t=tz; e=ez; }
    else { t=(tz>>>s); e=ez+s; }
    final NaturalBEI0 t2 = significand().square(t);
    final int e2 = (e<<1);
    return add(
      nonNegative(),
      significand(),
      exponent(),
      true,
      t2,
      e2); }

  //--------------------------------------------------------------

  public final BigFloat0
  addL1 (final double z0,
         final double z1) {
    assert Double.isFinite(z0);
    assert Double.isFinite(z1);
    final BigFloat0 dz = subtract(z0,z1);
    return add(
      nonNegative(),
      significand(),
      exponent(),
      true,
      dz.significand(),
      dz.exponent()); }

  //--------------------------------------------------------------

  public final BigFloat0
  addL2 (final double z0,
         final double z1) {
    assert Double.isFinite(z0);
    assert Double.isFinite(z1);
    final BigFloat0 dz = subtract(z0,z1);
    final NaturalBEI0 t2 = dz.significand().square();
    final int e2 = 2*dz.exponent();
    return add(
      nonNegative(),
      significand(),
      exponent(),
      true,
      t2,
      e2); }

  //--------------------------------------------------------------

  private final BigFloat0
  addProduct (final boolean p0,
              final long t0,
              final int e0,
              final boolean p1,
              final long t1,
              final int e1) {

    final int shift0 = Numbers.loBit(t0);
    final long t00;
    final int e00;
    if ((0==shift0) || (64==shift0)) { t00=t0; e00=e0; }
    else { t00=(t0>>>shift0); e00=e0+shift0; }

    final int shift1 = Numbers.loBit(t1);
    final long t11;
    final int e11;
    if ((0==shift1) || (64==shift1)) { t11=t1; e11=e1; }
    else { t11=(t1>>>shift1); e11=e1+shift1; }

    return
      add(
        nonNegative(),
        significand(),
        exponent(),
        ! (p0 ^ p1),
        NaturalBEI0.multiply(t00,t11),
        e00+e11); }

  public final BigFloat0
  addProduct (final double z0,
              final double z1) {
    assert Double.isFinite(z0);
    assert Double.isFinite(z1);
    return addProduct(
      Doubles.nonNegative(z0),
      Doubles.significand(z0),
      Doubles.exponent(z0),
      Doubles.nonNegative(z1),
      Doubles.significand(z1),
      Doubles.exponent(z1)); }

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

  private static final boolean roundUp (final NaturalBEI0 s,
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
    final NaturalBEI0 s0 = significand();
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
    //final int s1 = s0.shiftDown(es).intValue();
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
    final NaturalBEI0 s0 = significand();
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
  public final int compareTo (final BigFloat0 q) {

    if (nonNegative() && (! q.nonNegative())) { return 1; }
    if ((! nonNegative()) && q.nonNegative()) { return -1; }
    // same signs
    final NaturalBEI0 t0 = significand();
    final NaturalBEI0 t1 = q.significand();
    final int e0 = exponent();
    final int e1 = q.exponent();
    final int c;
    if (e0 <= e1) { c = t0.compareTo(t1.shiftUp(e1-e0)); }
    else { c = t0.shiftUp(e0-e1).compareTo(t1); }
    return (nonNegative() ? c : -c); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  public final boolean equals (final BigFloat0 q) {
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
    if (!(o instanceof BigFloat0)) { return false; }
    return equals((BigFloat0) o); }

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

  private BigFloat0 (final boolean nonNegative,
                     final NaturalBEI0 t0,
                     final int e0) {
    final int e1 = Math.max(0,Numbers.loBit(t0));
    _nonNegative = nonNegative;
    if (e1==0) {
      _significand = t0;
      _exponent = e0; }
    else {
      _significand = t0.shiftDown(e1);
      _exponent = Math.addExact(e0,e1); } }

  //--------------------------------------------------------------

  public static final BigFloat0 valueOf (final boolean nonNegative,
                                         final NaturalBEI0 t,
                                         final int e) {
    if (t.isZero()) { return ZERO; }
    return new BigFloat0(nonNegative,t,e); }

  public static final BigFloat0 valueOf (final long t,
                                         final int e) {
    if (0L==t) { return valueOf(0L); }
    if (0L < t) {
      return valueOf(true,NaturalBEI0.valueOf(t),e); }
    return valueOf(false,NaturalBEI0.valueOf(-t),e); }

  public static final BigFloat0 valueOf (final int t,
                                         final int e) {
    if (0==t) { return valueOf(0L); }
    if (0<t) { return valueOf(true,NaturalBEI0.valueOf(t),e); }
    return valueOf(false,NaturalBEI0.valueOf(-t),e); }

  //--------------------------------------------------------------

  private static final BigFloat0 valueOf (final boolean nonNegative,
                                          final long t0,
                                          final int e0)  {
    if (0L==t0) { return valueOf(0L); }
    assert 0L<t0;
    final int shift = Numbers.loBit(t0);
    final long t1;
    final int e1;
    if ((0==shift)||(64==shift)) { t1=t0; e1=e0; }
    else { t1 = (t0 >>> shift); e1 = e0 + shift; }
    return valueOf(nonNegative,NaturalBEI0.valueOf(t1),e1); }

  public static final BigFloat0 valueOf (final double z)  {
    return valueOf(
      Doubles.nonNegative(z),
      Doubles.significand(z),
      Doubles.exponent(z)); }

  //--------------------------------------------------------------

  private static final BigFloat0 valueOf (final boolean nonNegative,
                                          final int t0,
                                          final int e0)  {
    if (0==t0) { return valueOf(0L); }
    return valueOf(nonNegative,NaturalBEI0.valueOf(t0),e0); }

  public static final BigFloat0 valueOf (final float x)  {
    return valueOf(
      Floats.nonNegative(x),
      Floats.significand(x),
      Floats.exponent(x)); }

  //--------------------------------------------------------------

  public static final BigFloat0 valueOf (final byte t)  {
    if (0<=t) { return valueOf(true,NaturalBEI0.valueOf(t),0); }
    return valueOf(false,NaturalBEI0.valueOf(-t),0); }

  public static final BigFloat0 valueOf (final short t)  {
    if (0<=t) { return valueOf(true,NaturalBEI0.valueOf(t),0); }
    return valueOf(false,NaturalBEI0.valueOf(-t),0); }

  public static final BigFloat0 valueOf (final int t)  {
    if (0<=t) { return valueOf(true,NaturalBEI0.valueOf(t),0); }
    return valueOf(false,NaturalBEI0.valueOf(-t),0); }

  public static final BigFloat0 valueOf (final long t)  {
    if (0<=t) { return valueOf(true,NaturalBEI0.valueOf(t),0); }
    return valueOf(false,NaturalBEI0.valueOf(-t),0); }

  //--------------------------------------------------------------

  public static final BigFloat0 valueOf (final Double x)  {
    return valueOf(x.doubleValue()); }

  public static final BigFloat0 valueOf (final Float x)  {
    return valueOf(x.floatValue()); }

  public static final BigFloat0 valueOf (final Byte x)  {
    return valueOf(x.byteValue()); }

  public static final BigFloat0 valueOf (final Short x)  {
    return valueOf(x.shortValue()); }

  public static final BigFloat0 valueOf (final Integer x)  {
    return valueOf(x.intValue()); }

  public static final BigFloat0 valueOf (final Long x)  {
    return valueOf(x.longValue()); }

  public static final BigFloat0 valueOf (final BigDecimal x)  {
    throw Exceptions.unsupportedOperation(null,"valueOf",x); }

  public static final BigFloat0 valueOf (final NaturalBEI0 x)  {
    return valueOf(true,x,0); }

  public static final BigFloat0 valueOf (final Number x)  {
    if (x instanceof BigFloat0) { return (BigFloat0) x; }
    if (x instanceof Double) { return valueOf((Double) x); }
    if (x instanceof Float) { return valueOf((Float) x); }
    if (x instanceof Byte) { return valueOf((Byte) x); }
    if (x instanceof Short) { return valueOf((Short) x); }
    if (x instanceof Integer) { return valueOf((Integer) x); }
    if (x instanceof Long) { return valueOf((Long) x); }
    if (x instanceof NaturalBEI0) { return valueOf((NaturalBEI0) x); }
    if (x instanceof BigDecimal) { return valueOf((BigDecimal) x); }
    throw Exceptions.unsupportedOperation(null,"valueOf",x); }

  public static final BigFloat0 valueOf (final Object x)  {
    return valueOf((Number) x); }

  //--------------------------------------------------------------

  public static final BigFloat0 ZERO =
    new BigFloat0(true,NaturalBEI0.valueOf(0L),0);

  public static final BigFloat0 ONE =
    new BigFloat0(true,NaturalBEI0.ONE,0);

  public static final BigFloat0 TWO =
    new BigFloat0(true,NaturalBEI0.ONE,1);

  public static final BigFloat0 TEN =
    new BigFloat0(true,NaturalBEI0.valueOf(5),1);

  public static final BigFloat0 MINUS_ONE =
    new BigFloat0(false,NaturalBEI0.ONE,0);

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
