package com.avpines.dynamic.meters.gauge;

import com.avpines.dynamic.meters.MeterParams;
import java.util.function.ToDoubleFunction;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Parameters needed for constructing new Gauges.
 *
 * @param <T> The Gauge type.
 * @see DynamicGauge
 * @see SupplierDynamicGauge
 */
@Value
public class GaugeParams<T> implements MeterParams {

  @NotNull T obj;
  @Nullable ToDoubleFunction<T> toDouble;

  /**
   * Constrcut a new GaugeParams.
   *
   * @param obj      An object with some state.
   * @param toDouble A function that yields a double value for the gauge, based on the state of
   *                 {@code obj}.
   */
  public GaugeParams(
      @NotNull T obj,
      @Nullable ToDoubleFunction<T> toDouble) {
    this.obj = obj;
    this.toDouble = toDouble;
  }

}