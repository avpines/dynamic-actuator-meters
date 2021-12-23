package com.avpines.dynamic.meters.gauge;

import com.avpines.dynamic.meters.OfType;
import com.avpines.dynamic.meters.DynamicMeter;
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

abstract class AbstractDynamicGauge<T> extends
    DynamicMeter<Gauge, Builder<T>, GaugeParams<T>> {

  public static <E> @NotNull DynamicGaugeBuilder<E> builder(
      MeterRegistry registry, String name, OfType<E> type) {
    return new DynamicGaugeBuilder<>(registry, name, type);
  }

  public static <E> @NotNull DynamicGaugeBuilder<E> builder(
      MeterRegistry registry, String name, Class<E> type) {
    return new DynamicGaugeBuilder<>(registry, name, type);
  }

  public static @NotNull SupplierDynamicGaugeBuilder builder(
      MeterRegistry registry, String name) {
    return new SupplierDynamicGaugeBuilder(registry, name);
  }

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