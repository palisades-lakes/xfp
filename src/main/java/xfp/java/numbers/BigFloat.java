package xfp.java.numbers;

import static xfp.java.numbers.Doubles.doubleMergeBits;
import static xfp.java.numbers.Floats.floatMergeBits;
import static xfp.java.numbers.Numbers.loBit;

import java.util.Objects;

import xfp.java.exceptions.Exceptions;

/** A sign times a {@link Natural} significand times 2 to a
 * <code>int</code> exponent.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-04
 */

@SuppressWarnings("unchecked")
public final class BigFloat implements Ringlike<BigFloat> {

  //--------------------------------------------------------------
  // instance fields and methods
  //--------------------------------------------------------------

  private final boolean _nonNegative;
  public final boolean nonNegative () { return _nonNegative; }

  private final int _exponent;
  public final int exponent () { return _exponent; }

  // must always be non-negative
  private final Natural _significand;
  public final Natural significand () { return _significand; }

  //--------------------------------------------------------------
  // Ringlike
  //--------------------------------------------------------------

  @Override
  public final boolean isZero () {
    return significand().isZero(); }

  @Override
  public final boolean isOne () {
    return this.isOne(); }

  //--------------------------------------------------------------

  @Override
  public final BigFloat negate () {
    if (isZero()) { return this; }
    return valueOf(! nonNegative(),significand(),exponent()); }

  //--------------------------------------------------------------

  private static final BigFloat add (final boolean p0,
                                     final Natural t0,
                                     final int e0,
                                     final boolean p1,
                                     final Natural t1,
                                     final int e1) {
    if (e0<e1) { return add(p1,t1,e1,p0,t0,e0); }
    final int de = e0-e1;
    if (p0^p1) { 
      // different signs
      final Natural t0s = (de>0) ? t0.shiftUp(de) : t0; 
      final int c01 = t0s.compareTo(t1);
      // t1 > t0s
      if (0>c01) { return valueOf(p1,t1.subtract(t0s),e1); }
      // t0s > t1
      if (0<c01) { return valueOf(p0,t0s.subtract(t1),e1); }
      return ZERO; }
    // same signs
    if (0<de) { return valueOf(p0,t1.add(t0,de),e1);}
    return valueOf(p0,t0.add(t1),e1); }

  //--------------------------------------------------------------

  @Override
  public final BigFloat add (final BigFloat q) {
    return add(
      nonNegative(),
      significand(),
      exponent(),
      q.nonNegative(),
      q.significand(),
      q.exponent()); }

  //--------------------------------------------------------------

  private static final BigFloat
  add6 (final boolean p0,
        final Natural t0,
        final boolean p1,
        final long t1,
        final int upShift,
        final int e) {
    //assert 0L<t1;
    //assert 0<=upShift
    if (p0==p1) { return valueOf(p0,t0.add(t1,upShift),e); }
    final int c = t0.compareTo(t1,upShift);
    if (0<c) { return valueOf(p0,t0.subtract(t1,upShift),e); }
    if (0>c) { return valueOf(p1,t0.subtractFrom(t1,upShift),e); }
    return ZERO; }

  //--------------------------------------------------------------

  private static final BigFloat
  add5a (final boolean p0,
         final Natural t0,
         final boolean p1,
         final long t1,
         final int e) {
    //assert 0L<=t1;
    if (p0==p1) { return valueOf(p0,t0.add(t1),e); }
    // different signs
    final int c = t0.compareTo(t1);
    // t0>t1
    if (0<c) { return valueOf(p0,t0.subtract(t1),e); }
    // t1>t0
    if (0>c) { return valueOf(p1,t0.subtractFrom(t1),e); }
    return ZERO; }

  //--------------------------------------------------------------

  private final BigFloat
  add (final boolean p1,
       final long t11,
       final int e11) {
    //assert 0L<=t11;
    //if (0L==t11) { return this; }
    final boolean p0 = nonNegative();
    final Natural t0 = significand();
    final int e0 = exponent();
    // minimize long bits
    final int shift = Numbers.loBit(t11);
    final long t1 = (t11>>>shift);
    final int e1 = e11+shift;
    if (e0<e1) { return add6(p0,t0,p1,t1,e1-e0,e0); }
    if (e0==e1) { return add5a(p0,t0,p1,t1,e0); } 
    return add5a(p0,t0.shiftUp(e0-e1),p1,t1,e1); }

  //--------------------------------------------------------------

  public final BigFloat
  add (final double z) {
    //assert Double.isFinite(z);
    // escape on zero needed for add() 
    if (0.0==z) { return this; }
    return add(
      Doubles.nonNegative(z),
      Doubles.significand(z),
      Doubles.exponent(z)); }

  public final BigFloat
  addAll (final double[] z) {
    //assert Double.isFinite(z);
    BigFloat s = this;
    for (final double zi : z) { s = s.add(zi); }
    return s; }

  //--------------------------------------------------------------

  public final BigFloat
  addAbs (final double z) {
    //assert Double.isFinite(z);
    // escape on zero needed for add() 
    if (0.0==z) { return this; }
    return add(
      true,
      Doubles.significand(z),
      Doubles.exponent(z)); }

  public final BigFloat
  addAbsAll (final double[] z) {
    //assert Double.isFinite(z);
    BigFloat s = this;
    for (final double zi : z) { s = s.addAbs(zi); }
    return s; }

  //--------------------------------------------------------------

  @Override
  public final BigFloat
  subtract (final BigFloat q) {
    return add(
      nonNegative(),
      significand(),
      exponent(),
      ! q.nonNegative(),
      q.significand(),
      q.exponent()); }

  //--------------------------------------------------------------

  @Override
  public final BigFloat abs () {
    if (nonNegative()) { return this; }
    return negate(); }

  //--------------------------------------------------------------
  // used in Rational.addWithDenom()?

  public static final BigFloat
  product (final Natural x0,
           final boolean p1,
           final long x1) {
    //assert 0L<=x1;
    final int e0 = x0.loBit();
    final int e1 = loBit(x1);
    final Natural y0 =  ((0==e0) ? x0 : x0.shiftDown(e0));
    final long y1 = (((0==e1)||(64==e1)) ? x1 : (x1 >>> e1));
    return valueOf(p1,y0.multiply(y1),e0+e1); }

  private final BigFloat
  multiply (final boolean p,
            final Natural t,
            final int e) {
    return valueOf(
      (! (nonNegative() ^ p)),
      significand().multiply(t),
      Math.addExact(exponent(),e)); }

  @Override
  public final BigFloat
  multiply (final BigFloat q) {
    return
      multiply(q.nonNegative(),q.significand(),q.exponent()); }

  //--------------------------------------------------------------

  private final BigFloat
  multiply (final boolean p,
            final long t,
            final int e) {
    return valueOf(
      (! (nonNegative() ^ p)),
      significand().multiply(t),
      Math.addExact(exponent(),e)); }

  public final BigFloat
  multiply (final double z) {
    //assert Double.isFinite(z);
    // escape on zero needed for add() 
    if (0.0==z) { return this; }
    return multiply(
      Doubles.nonNegative(z),
      Doubles.significand(z),
      Doubles.exponent(z)); }

  //--------------------------------------------------------------

  @Override
  public final BigFloat
  square () {
    //if (isZero() ) { return EMPTY; }
    //if (isOne()) { return ONE; }
    return valueOf(true,significand().square(),2*exponent()); }

  //--------------------------------------------------------------

  public final BigFloat
  add2 (final double z) {
    //assert Double.isFinite(z);
    if (0.0==z) { return this; }
    final long tz = Doubles.significand(z);
    final int ez = Doubles.exponent(z);
    final int s = Numbers.loBit(tz);
    final long t;
    final int e;
    if ((0==s) || (64==s)) { t=tz; e=ez; }
    else { t=(tz>>>s); e=ez+s; }
    final Natural t2 = Natural.fromSquare(t);
    final int e2 = (e<<1);
    return add( 
      nonNegative(),
      significand(),
      exponent(),
      true,
      t2,
      e2); }

  public final BigFloat
  add2All (final double[] z) {
    BigFloat s = this;
    for (final double zi : z) { s = s.add2(zi); }
    return s; }

  //--------------------------------------------------------------

  public final BigFloat
  addProduct (final double z0,
              final double z1) {
    //assert Double.isFinite(z0);
    //assert Double.isFinite(z1);
    if ((0.0==z0) || (0.0==z1)) { return this; }

    final long t01 = Doubles.significand(z0);
    final int e01 = Doubles.exponent(z0);
    final int shift0 = Numbers.loBit(t01);
    final long t0 = (t01>>>shift0);
    final int e0 = e01+shift0;

    final long t11 = Doubles.significand(z1);
    final int e11 = Doubles.exponent(z1);
    final int shift1 = Numbers.loBit(t11);
    final long t1 = (t11>>>shift1);
    final int e1 = e11+shift1;

    return
      add(
        Doubles.nonNegative(z0)==Doubles.nonNegative(z1),
        Natural.product(t0,t1),
        e0+e1,
        nonNegative(),
        significand(),
        exponent()); }

  public final BigFloat 
  addProducts (final double[] z0,
               final double[] z1)  {
    final int n = z0.length;
    //assert n==z1.length;
    BigFloat s = this;
    for (int i=0;i<n;i++) { s = s.addProduct(z0[i],z1[i]); }
    return s; }

  //--------------------------------------------------------------
  /** Exact <code>a*x+y</code> (aka fma). */

  public static final BigFloat
  axpy (final double a,
        final double x,
        final double y) {
    return valueOf(y).addProduct(a,x); }

  /** Exact <code>a*x+y</code> (aka fma). */

  public static final BigFloat[]
    axpy (final double[] a,
          final double[] x,
          final double[] y) {
    final int n = a.length;
    //assert n==x.length;
    //assert n==y.length;
    final BigFloat[] bf = new BigFloat[n];
    for (int i=0;i<n;i++) { bf[i] = axpy(a[i],x[i],y[i]); }
    return bf; }

  /** Exact <code>this*x+y</code> (aka fma). */

  public static final BigFloat
  axpy (final double a,
        final BigFloat x,
        final double y) {
    return x.multiply(a).add(y); }

  /** Exact <code>a*this+y</code> (aka fma). */

  public static final BigFloat[] axpy (final double[] a,
                                       final BigFloat[] x,
                                       final double[] y) {
    final int n = x.length;
    //assert n==x.length;
    //assert n==y.length;
    final BigFloat[] bf = new BigFloat[n];
    for (int i=0;i<n;i++) { bf[i] = axpy(a[i],x[i],y[i]); }
    return bf; }

  //--------------------------------------------------------------

  public BigFloat addL1 (final double z0,
                         final double z1) {
    if (z0>z1) { return add(z0).add(-z1); }
    if (z0<z1) { return add(-z0).add(z1); }
    return this; }

  public final BigFloat
  addL1Distance (final double[] z0,
                 final double[] z1) {
    final int n = z0.length;
    //assert n==z1.length;
    BigFloat s = this;
    for (int i=0;i<n;i++) { s = s.addL1(z0[i],z1[i]); }
    return s; }

  //--------------------------------------------------------------
  // internal special case: add 2*z0*z1

  private final BigFloat
  addProductTwice (final double z0,
                   final double z1) {
    //assert Double.isFinite(z0);
    //assert Double.isFinite(z1);
    if ((0.0==z0) || (0.0==z1)) { return this; }

    final long t01 = Doubles.significand(z0);
    final int e01 = Doubles.exponent(z0);
    final int shift0 = Numbers.loBit(t01);
    final long t0 = (t01>>>shift0);
    final int e0 = e01+shift0;

    final long t11 = Doubles.significand(z1);
    final int e11 = Doubles.exponent(z1);
    final int shift1 = Numbers.loBit(t11);
    final long t1 = (t11>>>shift1);
    final int e1 = e11+shift1;

    return
      add(
        nonNegative(),
        significand(),
        exponent(),
        Doubles.nonNegative(z0)==Doubles.nonNegative(z1),
        Natural.product(t0,t1),
        e0+e1+1); }

  //--------------------------------------------------------------

  public final BigFloat
  addL2 (final double z0,
         final double z1) {
    final double mz1 = -z1;
    return
      add2(z0).add2(z1).addProductTwice(z0,mz1); }

  public final BigFloat
  addL2Distance (final double[] z0,
                 final double[] z1) {
    final int n = z0.length;
    //assert n==z1.length;
    BigFloat s = this;
    for (int i=0;i<n;i++) { s = s.addL2(z0[i],z1[i]); }
    return s; }

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
    throw Exceptions.unsupportedOperation(this,"intValue"); }

  /** Unsupported.
   *
   * TODO: should it really truncate or round instead? Or
   * should there be more explicit round, floor, ceil, etc.?
   */
  @Override
  public final long longValue () {
    throw Exceptions.unsupportedOperation(this,"longValue"); }

  //--------------------------------------------------------------

  /** @return closest half-even rounded <code>float</code>
   */

  @Override
  public final float floatValue () {
    final boolean nn = nonNegative();
    final Natural s0 = significand();
    final int e0 = exponent();
    if (s0.isZero()) { return (nn ? 0.0F : -0.0F); }

    // DANGER: what if hiBit isn't in the int range?
    final int eh = s0.hiBit();
    final int es =
      Math.max(Floats.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND-e0,
        Math.min(
          Floats.MAXIMUM_EXPONENT_INTEGRAL_SIGNIFICAND-e0-1,
          eh-Floats.SIGNIFICAND_BITS));
    if (0==es) {
      return floatMergeBits(nn,s0.intValue(),e0); }
    if (0 > es) {
      final int e1 = e0 + es;
      final int s1 = (s0.intValue() << -es);
      return floatMergeBits(nn,s1,e1); }
    if (eh <= es) { return (nn ? 0.0F : -0.0F); }
    // eh > es > 0
    final boolean up = s0.roundUp(es);
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
  /** @return closest half-even rounded <code>double</code>
   */

  @Override
  public final double doubleValue () {
    final boolean nn = nonNegative();
    final Natural s0 = significand();
    final int e0 = exponent();
    if (s0.isZero()) { return (nn ? 0.0 : -0.0); }
    final int eh = s0.hiBit();
    final int es =
      Math.max(Doubles.MINIMUM_EXPONENT_INTEGRAL_SIGNIFICAND-e0,
        Math.min(
          Doubles.MAXIMUM_EXPONENT_INTEGRAL_SIGNIFICAND-e0-1,
          eh-Doubles.SIGNIFICAND_BITS));
    if (eh-es>Doubles.SIGNIFICAND_BITS) {
      return 
        (nn ? 
          Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY); }
    if (0==es) {
      return doubleMergeBits(nn,s0.longValue(),e0); }
    if (0 > es) {
      final int e1 = e0 + es;
      final long s1 = (s0.longValue() << -es);
      return doubleMergeBits(nn,s1,e1); }
    if (eh <= es) { return (nn ? 0.0 : -0.0); }
    // eh > es > 0
    final boolean up = s0.roundUp(es);
    final long s1 = s0.getShiftedLong(es);
    final int e1 = e0 + es;
    if (up) {
      final long s2 = s1 + 1L;
      if (Numbers.hiBit(s2) > Doubles.SIGNIFICAND_BITS) { // carry
        // lost bit has to be zero, since there was just a carry
        final long s3 = (s2>>1);
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
    final Natural t0 = significand();
    final Natural t1 = q.significand();
    final int e0 = exponent();
    final int e1 = q.exponent();
    final int c;
    //if (e0 <= e1) { c = t0.compareTo(t1.shiftUp(e1-e0)); }
    if (e0 <= e1) { c = t0.compareTo(t1.shiftUp(e1-e0)); }
    else { c = t0.shiftUp(e0-e1).compareTo(t1); }
    return (nonNegative() ? c : -c); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  private static final boolean reducedEquals (final BigFloat a,
                                              final BigFloat b) {
    // assuming a and b have minimum significand and maximum 
    // exponent
    if (a==b) { return true; }
    if (null==a) { return false; }
    // assuming reduced
    if (! a.significand().equals(b.significand())) { return false; }
    if (a.significand().isZero()) { return true; }
    if (a.nonNegative()!=b.nonNegative()) { return false; }
    if (a.exponent()!=b.exponent()) { return false; }
    return true; }

  public final boolean equals (final BigFloat q) {
    return reducedEquals(reduce(),q.reduce()); }

  @Override
  public boolean equals (final Object o) {
    if (!(o instanceof BigFloat)) { return false; }
    return equals((BigFloat) o); }

  @Override
  public int hashCode () {
    final BigFloat a = reduce();
    int h = 17;
    h = (31*h) + (a.nonNegative() ? 0 : 1);
    h = (31*h) + a.exponent();
    h = (31*h) + Objects.hash(a.significand());
    return h; }

  @Override
  public final String toString () {
    return
      (nonNegative() ? "" : "-")
      + "0x" + significand().toString()
      + "p" + exponent(); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private BigFloat (final boolean p0,
                    final Natural t0,
                    final int e0) {
    //assert null!=t0;
    _nonNegative = p0;
    _significand = t0;
    _exponent = e0; } 

  //--------------------------------------------------------------

  public static final BigFloat ZERO =
    new BigFloat(true,Natural.ZERO,0);

  //  private static final BigFloat ONE =
  //    new BigFloat(true,Natural.valueOf(1),0);
  //
  //  private static final BigFloat TWO =
  //    new BigFloat(true,Natural.valueOf(1),1);
  //
  //  private static final BigFloat TEN =
  //    new BigFloat(true,Natural.valueOf(5),1);
  //
  //  private static final BigFloat MINUS_ONE =
  //    new BigFloat(false,Natural.valueOf(1),0);

  //--------------------------------------------------------------

  //  private static final BigFloat reduce (final boolean p0,
  //                                        final Natural t0,
  //                                        final int e0) {
  //    //if (t0.isZero()) { return ZERO; }
  //    final int shift = t0.loBit();
  //    if (0>=shift) { return new BigFloat(p0,t0,e0); }
  //    return new BigFloat(p0, t0.shiftDown(shift),e0+shift); }

  private final BigFloat reduce () {
    final boolean p0 = nonNegative();
    final Natural t0 = significand();
    final int e0 = exponent();
    final int shift = t0.loBit();
    if (0>=shift) { return this; }
    return new BigFloat(p0, t0.shiftDown(shift),e0+shift); }

  public static final BigFloat valueOf (final boolean p0,
                                        final Natural t0,
                                        final int e0) {
    //return reduce(p0,t0,e0); }
    return new BigFloat(p0,t0,e0); }

  //--------------------------------------------------------------

  private static final BigFloat valueOf (final boolean nonNegative,
                                         final long t0,
                                         final int e0)  {
    //if (0L==t0) { return ZERO; }
    //assert 0L<t0;
    final int shift = Numbers.loBit(t0);
    final long t1;
    final int e1;
    if ((0==shift)||(64==shift)) { t1=t0; e1=e0; }
    else { t1 = (t0 >>> shift); e1 = e0 + shift; }
    return valueOf(nonNegative,Natural.valueOf(t1),e1); }

  public static final BigFloat valueOf (final double z)  {
    return valueOf(
      Doubles.nonNegative(z),
      Doubles.significand(z),
      Doubles.exponent(z)); }

  //--------------------------------------------------------------

  private static final BigFloat valueOf (final boolean nonNegative,
                                         final int t0,
                                         final int e0)  {
    //if (0==t0) { return ZERO; }
    return valueOf(nonNegative,Natural.valueOf(t0),e0); }

  public static final BigFloat valueOf (final float x)  {
    return valueOf(
      Floats.nonNegative(x),
      Floats.significand(x),
      Floats.exponent(x)); }

  //--------------------------------------------------------------

  //  public static final BigFloat valueOf (final byte t)  {
  //    if (0<=t) { return valueOf(true,Natural.valueOf(t),0); }
  //    return valueOf(false,Natural.valueOf(-t),0); }
  //
  //  public static final BigFloat valueOf (final short t)  {
  //    if (0<=t) { return valueOf(true,Natural.valueOf(t),0); }
  //    return valueOf(false,Natural.valueOf(-t),0); }
  //
  //  public static final BigFloat valueOf (final int t)  {
  //    if (0<=t) { return valueOf(true,Natural.valueOf(t),0); }
  //    return valueOf(false,Natural.valueOf(-t),0); }

  //  public static final BigFloat valueOf (final long t)  {
  //    if (0<=t) { return valueOf(true,Natural.valueOf(t),0); }
  //    return valueOf(false,Natural.valueOf(-t),0); }

  //--------------------------------------------------------------

  //  public static final BigFloat valueOf (final Double x)  {
  //    return valueOf(x.doubleValue()); }
  //
  //  public static final BigFloat valueOf (final Float x)  {
  //    return valueOf(x.floatValue()); }
  //
  //  public static final BigFloat valueOf (final Byte x)  {
  //    return valueOf(x.byteValue()); }
  //
  //  public static final BigFloat valueOf (final Short x)  {
  //    return valueOf(x.shortValue()); }
  //
  //  public static final BigFloat valueOf (final Integer x)  {
  //    return valueOf(x.intValue()); }
  //
  //  public static final BigFloat valueOf (final Long x)  {
  //    return valueOf(x.longValue()); }
  //
  //  public static final BigFloat valueOf (final BigDecimal x)  {
  //    throw Exceptions.unsupportedOperation(null,"valueOf",x); }
  //
  //  public static final BigFloat valueOf (final Natural x)  {
  //    return valueOf(true,x,0); }
  //
  //  public static final BigFloat valueOf (final Number x)  {
  //    if (x instanceof Double) { return valueOf((Double) x); }
  //    if (x instanceof Float) { return valueOf((Float) x); }
  //    if (x instanceof Byte) { return valueOf((Byte) x); }
  //    if (x instanceof Short) { return valueOf((Short) x); }
  //    if (x instanceof Integer) { return valueOf((Integer) x); }
  //    if (x instanceof Long) { return valueOf((Long) x); }
  //    if (x instanceof BigDecimal) { return valueOf((BigDecimal) x); }
  //    throw Exceptions.unsupportedOperation(null,"valueOf",x); }
  //
  //  public static final BigFloat valueOf (final Object x)  {
  //    if (x instanceof BigFloat) { return (BigFloat) x; }
  //    if (x instanceof Natural) { return valueOf((Natural) x); }
  //    return valueOf((Number) x); }
  //
  //--------------------------------------------------------------
}
//--------------------------------------------------------------
