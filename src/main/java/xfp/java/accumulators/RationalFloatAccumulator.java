package xfp.java.accumulators;

import xfp.java.numbers.RationalFloat;

/** Naive sum of <code>double</code> values with a RationalFloat
 * accumulator (for testing).
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-09
 */
public final class RationalFloatAccumulator
extends ExactAccumulator<RationalFloatAccumulator> {

  private RationalFloat _sum;

  //--------------------------------------------------------------

  @Override
  public final boolean noOverflow () { return true; }

  @Override
  public final Object value () { return _sum; }

  @Override
  public final double doubleValue () {
    return _sum.doubleValue(); }

  @Override
  public final float floatValue () {
    return _sum.floatValue(); }

  @Override
  public final RationalFloatAccumulator clear () {
    _sum = RationalFloat.ZERO;
    return this; }

  @Override
  public final RationalFloatAccumulator add (final double z) {
    _sum = _sum.add(z);
    return this; }

  @Override
  public final RationalFloatAccumulator addAll (final double[] z) {
    _sum = _sum.addAll(z); 
    return this; }

  @Override
  public final RationalFloatAccumulator addAbs (final double z) {
    _sum = _sum.addAbs(z);
    return this; }

  @Override
  public final RationalFloatAccumulator addAbsAll (final double[] z) {
    _sum = _sum.addAbsAll(z); 
    return this; }

  @Override
  public final RationalFloatAccumulator add2 (final double z) {
    _sum = _sum.add2(z);
    return this; }

  @Override
  public final RationalFloatAccumulator add2All (final double[] z)  {
    _sum = _sum.add2All(z);
    return this; }

  @Override
  public final RationalFloatAccumulator addProduct (final double z0,
                                                    final double z1) {
    _sum = _sum.addProduct(z0,z1);
    return this; }

  @Override
  public final RationalFloatAccumulator 
  addProducts (final double[] z0,
               final double[] z1)  {
    _sum = _sum.addProducts(z0,z1); 
    return this; }

  @Override
  public final RationalFloatAccumulator addL1 (final double z0,
                                               final double z1) {

    _sum = _sum.addL1(z0,z1);
    return this; }

  @Override
  public final RationalFloatAccumulator 
  addL1Distance (final double[] z0,
                 final double[] z1)  {
    _sum = _sum.addL1Distance(z0,z1); 
    return this; }

  @Override
  public final RationalFloatAccumulator addL2 (final double z0,
                                               final double z1) {
    _sum = _sum.addL2(z0,z1);
    return this; }

  @Override
  public final RationalFloatAccumulator 
  addL2Distance (final double[] z0,
                 final double[] z1)  {
    _sum = _sum.addL2Distance(z0,z1); 
    return this; }

  //--------------------------------------------------------------
  // TODO: these don't make sense for a = a.op paradigm

  @Override
  public final void partialSums (final double[] x,
                                 final double[] s) {
    final int n = x.length;
    //assert s.length==n;
    RationalFloat sum = RationalFloat.ZERO;
    for (int i=0;i<n;i++) { 
      sum = sum.add(x[i]);
      s[i] = sum.doubleValue(); } 
    _sum = sum; }

  @Override
  public final double[] partialSums (final double[] x) {
    final int n = x.length;
    final double[] s = new double[n];
    RationalFloat sum = RationalFloat.ZERO;
    for (int i=0;i<n;i++) { 
      sum = sum.add(x[i]);
      s[i] = sum.doubleValue(); } 
    _sum = sum;
    return s; }

  @Override
  public final  double[] partialL1s (final double[] x) {
    final int n = x.length;
    final double[] s = new double[n];
    RationalFloat sum = RationalFloat.ZERO;
    for (int i=0;i<n;i++) { 
      sum = sum.addAbs(x[i]);
      s[i] = sum.doubleValue(); } 
    _sum = sum;
    return s; }

  @Override
  public final  double[] partialL2s (final double[] x) {
    final int n = x.length;
    final double[] s = new double[n];
    RationalFloat sum = RationalFloat.ZERO;
    for (int i=0;i<n;i++) { 
      sum = sum.add2(x[i]);
      s[i] = sum.doubleValue(); } 
    _sum = sum;
    return s; }

  @Override
  public final  double[] partialDots (final double[] x0,
                                      final double[] x1) {
    final int n = x0.length;
    final double[] s = new double[n];
    RationalFloat sum = RationalFloat.ZERO;
    for (int i=0;i<n;i++) {
      sum = sum.addProduct(x0[i],x1[i]);
      s[i] = sum.doubleValue(); } 
    _sum = sum;
    return s; }

  @Override
  public final  double[] partialL1Distances (final double[] x0,
                                             final double[] x1) {
    final int n = x0.length;
    final double[] s = new double[n];
    RationalFloat sum = RationalFloat.ZERO;
    for (int i=0;i<n;i++) {
      sum = sum.addL1(x0[i],x1[i]);
      s[i] = sum.doubleValue(); } 
    _sum = sum;
    return s; }

  @Override
  public final  double[] partialL2Distances (final double[] x0,
                                             final double[] x1) {
    final int n = x0.length;
    final double[] s = new double[n];
    RationalFloat sum = RationalFloat.ZERO;
    for (int i=0;i<n;i++) {
      sum = sum.addL2(x0[i],x1[i]);
      s[i] = sum.doubleValue(); } 
    _sum = sum;
    return s; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private RationalFloatAccumulator () { super(); clear(); }

  public static final RationalFloatAccumulator make () {
    return new RationalFloatAccumulator(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
