package com.avpines.dynamic.meters.gauge;

import com.avpines.dynamic.meters.DynamicMeter;
import com.avpines.dynamic.meters.OfType;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Gauge.Builder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Parent class for the different dynamic gauges.
 *
 * @param <T> The gauge type.
 */
abstract class AbstractDynamicGauge<T> extends
    DynamicMeter<Gauge, Builder<T>, GaugeParams<T>> {

  /**
   * Builder for dynamic gauges.
   *
   * @param registry To register generated meters.
   * @param name     The meter name that will be shared among all the underlying meters.
   * @param type     The type of the gauge, used for type inference.
   * @param <E>      The gauge type.
   * @return a new gauge builder.
   */
  public static <E> @NotNull DynamicGaugeBuilder<E> builder(
      MeterRegistry registry, String name, OfType<E> type) {
    return new DynamicGaugeBuilder<>(registry, name, type);
  }

  /**
   * Builder for dynamic gauges.
   *
   * @param registry To register generated meters.
   * @param name     The meter name that will be shared among all the underlying meters.
   * @param type     The type of the gauge, used for type inference.
   * @param <E>      The gauge type.
   * @return a new gauge builder.
   */
  public static <E> @NotNull DynamicGaugeBuilder<E> builder(
      MeterRegistry registry, String name, Class<E> type) {
    return new DynamicGaugeBuilder<>(registry, name, type);
  }

  /**
   * Builder for a {@link SupplierDynamicGauge}.
   *
   * @param registry To register generated meters.
   * @param name     The meter name that will be shared among all the underlying meters.
   * @return a new gauge builder.
   */
  public static @NotNull SupplierDynamicGaugeBuilder builder(
      MeterRegistry registry, String name) {
    return new SupplierDynamicGaugeBuilder(registry, name);
  }

  /**
   * Construct a new AbstractDynamicGauge.
   *
   * @param registry        To register the underlying meters.
   * @param name            Meter name, all underlying meters will share that name.
   * @param newInnerBuilder A function to construct the underlying meter builder.
   * @param tagger          A function to dynamically add the tags.
   * @param customizer      For any additional customization to the underlying meter.
   * @param registrar       Function to register the underlying meters.
   * @param tagKeys         The keys that this meter will have, and allow their values to be added
   *                        dynamically.
   */
  AbstractDynamicGauge(
      @NotNull MeterRegistry registry,
      @NotNull String name,
      @NotNull BiFunction<String, GaugeParams<T>, Builder<T>> newInnerBuilder,
      @NotNull BiFunction<Builder<T>, Collection<Tag>, Builder<T>> tagger,
      @Nullable UnaryOperator<Builder<T>> customizer,
      @NotNull Function<Builder<T>, Gauge> registrar,
      String... tagKeys) {
    super(registry, name, newInnerBuilder, tagger, customizer, registrar, tagKeys);
  }

}