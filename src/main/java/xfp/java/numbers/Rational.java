package xfp.java.numbers;

import java.math.BigInteger;
import java.util.Objects;

import com.upokecenter.numbers.EContext;
import com.upokecenter.numbers.EFloat;
import com.upokecenter.numbers.EInteger;

/** Ratios of {@link BigInteger}.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-03-22
 */

public final class Rational 
extends Number
implements Comparable<Rational> {

  private static final long serialVersionUID = 1L;

  //--------------------------------------------------------------
  // instance fields and methods
  //--------------------------------------------------------------

  private final BigInteger _numerator;
  public final BigInteger numerator () { return _numerator; }
  private final BigInteger _denominator;
  public final BigInteger denominator () { return _denominator; }

  //--------------------------------------------------------------

  private static final boolean isZero (final BigInteger i) {
    return 0 == i.signum(); }

  public final boolean isZero () { return isZero(numerator()); }

//  private static final boolean isOne (final BigInteger i) {
//    return BigInteger.ONE.equals(i); }

  private static final boolean isOne (final BigInteger n,
                                      final BigInteger d) {
    return n.equals(d); }

  public final boolean isOne () { 
    return isOne(numerator(),denominator()); }

  //--------------------------------------------------------------
  // TODO: does BigInteger optimize multiply by ONE?
  
  private final Rational add (final BigInteger n,
                              final BigInteger d) {
    assert !isZero(d);
    if (0 == n.signum()) { return this; }
    if (isZero()) { return valueOf(n,d); }
    return valueOf(
      numerator().multiply(d).add(n.multiply(denominator())),
      denominator().multiply(d)); }

  private final Rational multiply (final BigInteger n,
                                   final BigInteger d) {
    assert !isZero(d);
    if (isZero() ) { return ZERO; }
    if (isZero(n)) { return ZERO; }
    if (isOne(n,d)) { return this; }
    if (isOne()) { return valueOf(n,d); }
    return 
      valueOf(
        numerator().multiply(n), 
        denominator().multiply(d)); }

  //--------------------------------------------------------------

  public final Rational negate () {
    if (isZero()) { return this; }
    return valueOf(numerator().negate(),denominator()); }

  public final Rational reciprocal () {
    assert !isZero(numerator());
    return valueOf(denominator(),numerator()); }

  public final Rational add (final Rational q) {
    return add(q.numerator(),q.denominator()); }

  public final Rational multiply (final Rational q) {
    return multiply(q.numerator(),q.denominator()); }

  //--------------------------------------------------------------

  public final Rational add (final double q) {
    final BigInteger[] nd = Doubles.toRatio(q);
    return add(nd[0],nd[1]); }

  public final Rational addProduct (final double z0,
                                    final double z1) { 
    final BigInteger[] nd0 = Doubles.toRatio(z0);
    final BigInteger[] nd1 = Doubles.toRatio(z1);
    return add(
      nd0[0].multiply(nd1[0]),
      nd0[1].multiply(nd1[1])); }

  //--------------------------------------------------------------
  // Number methods
  //--------------------------------------------------------------

  @Override
  public final int intValue () {
    return numerator().divide(denominator()).intValue(); }

  @Override
  public final long longValue () {
    return numerator().divide(denominator()).longValue(); }

  // TODO: replace with explicit division
  @Override
  public final float floatValue () {
    final EInteger ni = 
      EInteger.FromBytes(numerator().toByteArray(), false);
    final EInteger di = 
      EInteger.FromBytes(denominator().toByteArray(), false);
    final EFloat nf = EFloat.FromEInteger(ni); 
    final EFloat df = EFloat.FromEInteger(di); 
    final EFloat f = nf.Divide(df, EContext.Binary32);
    final float ze = f.ToSingle(); 
    return ze;}

  // TODO: replace with explicit division
  @Override
  public final double doubleValue () { 
    final EInteger ni = 
      EInteger.FromBytes(numerator().toByteArray(), false);
    final EInteger di = 
      EInteger.FromBytes(denominator().toByteArray(), false);
    final EFloat nf = EFloat.FromEInteger(ni); 
    final EFloat df = EFloat.FromEInteger(di); 
    final EFloat f = nf.Divide(df, EContext.Binary64);
    return f.ToDouble(); }

  //--------------------------------------------------------------
  // Comparable methods
  //--------------------------------------------------------------

  @Override
  public final int compareTo (final Rational o) {
    final BigInteger n0d1 = numerator().multiply(o.denominator());
    final BigInteger n1d0 = o.numerator().multiply(denominator());
    return n0d1.compareTo(n1d0); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  public final boolean equals (final Rational q) {
    if (this == q) { return true; }
    if (null == q) { return false; }
    final BigInteger n0 = numerator(); 
    final BigInteger d0 = denominator(); 
    final BigInteger n1 = q.numerator(); 
    final BigInteger d1 = q.denominator(); 
    return n0.multiply(d1).equals(n1.multiply(d0)); }

  @Override
  public boolean equals (final Object o) {
    if (!(o instanceof Rational)) { return false; }
    return equals((Rational) o); }
  
  @Override
  public int hashCode () {
    return Objects.hash(numerator(),denominator()); }

  @Override
  public final String toString () {
    return 
      "(" + numerator().toString(0x10) 
      + " / " + denominator().toString(0x10) 
      + ")"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private Rational (final BigInteger numerator,
                    final BigInteger denominator) {
    super();
    assert 1 == denominator.signum();
    _numerator = numerator;
    _denominator = denominator; }

  //--------------------------------------------------------------

  private static final Rational reduced (final BigInteger n,
                                         final BigInteger d) {
    
    if (d.signum() < 0) { return reduced(n.negate(),d.negate()); }
    
    if (n == BigInteger.ZERO) { 
      return new Rational(n,BigInteger.ONE); }

    // TODO: any value in this test?
    if ((n == BigInteger.ZERO) || (d == BigInteger.ZERO)) {
      return new Rational(n,d); }

    final BigInteger gcd = n.gcd(d);
    // TODO: any value in this test?
    if (gcd.compareTo(BigInteger.ONE) > 0) {
      return new Rational(n.divide(gcd),d.divide(gcd)); } 

    return new Rational(n,d); }

  //--------------------------------------------------------------

  public static final Rational valueOf (final BigInteger n,
                                        final BigInteger d) {
    // TODO: is it better to keep ratio in reduced form or not?
    // return new Rational(n,d); }
    return reduced(n,d); }

  public static final Rational valueOf (final long n,
                                        final long d) {
    return valueOf(BigInteger.valueOf(n),BigInteger.valueOf(d)); }

  public static final Rational valueOf (final int n,
                                        final int d) {
    return valueOf(BigInteger.valueOf(n),BigInteger.valueOf(d)); }

  //--------------------------------------------------------------

  public static final Rational valueOf (final double x)  {
    final BigInteger[] nd = Doubles.toRatio(x);
    return valueOf(nd[0], nd[1]); }

  public static final Rational valueOf (final float x)  {
    final BigInteger[] nd = Floats.toRatio(x);
    return valueOf(nd[0], nd[1]); }

  public static final Rational valueOf (final byte x)  {
    return valueOf(BigInteger.valueOf(x), BigInteger.ONE); }

  public static final Rational valueOf (final short x)  {
    return valueOf(BigInteger.valueOf(x), BigInteger.ONE); }

  public static final Rational valueOf (final int x)  {
    return valueOf(BigInteger.valueOf(x), BigInteger.ONE); }

  public static final Rational valueOf (final long x)  {
    return valueOf(BigInteger.valueOf(x), BigInteger.ONE); }

  public static final Rational valueOf (final BigInteger x)  {
    return valueOf(x, BigInteger.ONE); }

  //--------------------------------------------------------------

  public static final Rational ZERO = 
    Rational.valueOf(BigInteger.ZERO,BigInteger.ONE);

  public static final Rational ONE = 
    Rational.valueOf(BigInteger.ONE,BigInteger.ONE);

  public static final Rational TWO = 
    Rational.valueOf(BigInteger.TWO,BigInteger.ONE);

  public static final Rational TEN = 
    Rational.valueOf(BigInteger.TEN,BigInteger.ONE);

  public static final Rational MINUS_ONE = 
    Rational.valueOf(BigInteger.ONE.negate(),BigInteger.ONE);

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
