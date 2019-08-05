package xfp.java.accumulators;

import xfp.java.numbers.BigFloat0;

/** Naive sum of <code>double</code> values with a BigFloat0-valued
 * accumulator.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-08-05
 */
public final class BigFloatAccumulator0
extends ExactAccumulator<BigFloatAccumulator0> {

  private BigFloat0 _sum;

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
  public final BigFloatAccumulator0 clear () {
    _sum = BigFloat0.valueOf(0L);
    return this; }

  @Override
  public final BigFloatAccumulator0 add (final double z) {
    _sum = _sum.add(z);
    return this; }

  @Override
  public final BigFloatAccumulator0 addAll (final double[] z) {
    //for (final double zi : z) { _sum = _sum.add(zi); }
    _sum = _sum.addAll(z); 
    return this; }

  @Override
  public final BigFloatAccumulator0 addAbs (final double z) {
    _sum = _sum.addAbs(z);
    return this; }

  @Override
  public final BigFloatAccumulator0 addAbsAll (final double[] z) {
    //for (final double zi : z) { _sum = _sum.addAbs(zi); }
    _sum = _sum.addAbsAll(z); 
    return this; }

  @Override
  public final BigFloatAccumulator0 add2 (final double z) {
    _sum = _sum.add2(z);
    return this; }

  @Override
  public final BigFloatAccumulator0 add2All (final double[] z)  {
    _sum = _sum.add2All(z);
    return this; }

  @Override
  public final BigFloatAccumulator0 addProduct (final double z0,
                                               final double z1) {
    _sum = _sum.addProduct(z0,z1);
    return this; }

  @Override
  public final BigFloatAccumulator0 addProducts (final double[] z0,
                                                final double[] z1)  {
    _sum = _sum.addProducts(z0,z1); 
    return this; }

  @Override
  public final BigFloatAccumulator0 addL1 (final double z0,
                                          final double z1) {

    _sum = _sum.addL1(z0,z1);
    return this; }

  @Override
  public final BigFloatAccumulator0 addL1Distance (final double[] z0,
                                                  final double[] z1)  {
    _sum = _sum.addL1Distance(z0,z1); 
    return this; }

  @Override
  public final BigFloatAccumulator0 addL2 (final double z0,
                                          final double z1) {
    _sum = _sum.addL2(z0,z1);
    return this; }

  @Override
  public final BigFloatAccumulator0 addL2Distance (final double[] z0,
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
    BigFloat0 sum = BigFloat0. valueOf(0L);
    for (int i=0;i<n;i++) { 
      sum = sum.add(x[i]);
      s[i] = sum.doubleValue(); } 
    _sum = sum;
  }

  @Override
  public final double[] partialSums (final double[] x) {
    final int n = x.length;
    final double[] s = new double[n];
    BigFloat0 sum = BigFloat0. valueOf(0L);
    for (int i=0;i<n;i++) { 
      sum = sum.add(x[i]);
      s[i] = sum.doubleValue(); } 
    _sum = sum;
    return s; }

  @Override
  public final  double[] partialL1s (final double[] x) {
    final int n = x.length;
    final double[] s = new double[n];
    BigFloat0 sum = BigFloat0. valueOf(0L);
    for (int i=0;i<n;i++) { 
      sum = sum.addAbs(x[i]);
      s[i] = sum.doubleValue(); } 
    _sum = sum;
    return s; }

  @Override
  public final  double[] partialL2s (final double[] x) {
    final int n = x.length;
    final double[] s = new double[n];
    BigFloat0 sum = BigFloat0. valueOf(0L);
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
    BigFloat0 sum = BigFloat0. valueOf(0L);
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
    BigFloat0 sum = BigFloat0. valueOf(0L);
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
    BigFloat0 sum = BigFloat0. valueOf(0L);
    for (int i=0;i<n;i++) {
      sum = sum.addL2(x0[i],x1[i]);
      s[i] = sum.doubleValue(); } 
    _sum = sum;
    return s; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private BigFloatAccumulator0 () { super(); clear(); }

  public static final BigFloatAccumulator0 make () {
    return new BigFloatAccumulator0(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
