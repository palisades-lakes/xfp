package xfp.java.function;

/** Like {@link java.util.function.ToDoubleFunction}.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2019-04-22
 */

@FunctionalInterface
public interface ToFloatFunction<T> {
  float applyAsFloat (final T q); }
