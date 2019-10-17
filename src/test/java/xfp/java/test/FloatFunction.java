package xfp.java.test;

/** Like {@link java.util.function.DoubleFunction}.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-22
 */

@FunctionalInterface
public interface FloatFunction<R> {
  R apply (final float x); }
