package com.avpines.dynamic.meters.gauge;

import com.avpines.dynamic.meters.MeterParams;
import java.util.function.ToDoubleFunction;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @param <T>
 */
@Value
public class GaugeParams<T> implements MeterParams {

  @NotNull T obj;
  @Nullable ToDoubleFunction<T> toDouble;

  public GaugeParams(
      @NotNull T obj,
      @Nullable ToDoubleFunction<T> toDouble) {
    this.obj = obj;
    this.toDouble = toDouble;
  }

}