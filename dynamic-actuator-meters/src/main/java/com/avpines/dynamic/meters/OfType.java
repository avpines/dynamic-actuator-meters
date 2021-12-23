package com.avpines.dynamic.meters;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.jetbrains.annotations.NotNull;

/**
 * A super type token as described in <a href="http://gafter.blogspot.com/2006/12/super-type-tokens.html">
 * http://gafter.blogspot.com/2006/12/super-type-tokens.html</a>, and improved by Jackson's
 * com.fasterxml.jackson.core.type.TypeReference.
 *<p>
 * Example usage:
 *<pre>
 *  OfType ref = new OfType&lt;List&lt;Integer&gt;&gt;() { };
 *</pre>
 */
public abstract class OfType<T> implements Comparable<OfType<T>> {

  protected final Type _type;

  protected OfType() {
    Type superClass = getClass().getGenericSuperclass();
    if (superClass instanceof Class<?>) {
      throw new IllegalArgumentException("Missing type parameter");
    }
    _type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
  }

  public Type getType() {
    return _type;
  }

  @Override
  public int compareTo(@NotNull OfType<T> o) {
    return 0;
  }
}