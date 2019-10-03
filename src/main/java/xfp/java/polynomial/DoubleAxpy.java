package xfp.java.polynomial;

/** Inexact {@link Axpy} using <code>double</code>
 * and {@link Math#fma(double,double,double)}.
 * Just calls default methods from {@link Axpy}.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2019-10-03
 */

@SuppressWarnings("unchecked")
public final class DoubleAxpy implements Axpy<Double> {

  public static final DoubleAxpy make () {
    return new DoubleAxpy(); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------

