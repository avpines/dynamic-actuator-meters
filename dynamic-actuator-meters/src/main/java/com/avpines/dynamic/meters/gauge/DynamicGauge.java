package com.avpines.dynamic.meters.gauge;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Gauge.Builder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @param <T>
 */
public class DynamicGauge<T> extends AbstractDynamicGauge<T> {

  DynamicGauge(
      @NotNull MeterRegistry registry,
      @NotNull String name,
      @NotNull BiFunction<String, GaugeParams<T>, Builder<T>> newInnerBuilder,
      @NotNull BiFunction<Builder<T>, Collection<Tag>, Builder<T>> tagger,
      @Nullable UnaryOperator<Builder<T>> customizer,
      @NotNull Function<Builder<T>, Gauge> registrar,
      String... tagKeys) {
    super(registry, name, newInnerBuilder, tagger, customizer, registrar, tagKeys);
  }

  public Gauge getOrCreate(T obj, ToDoubleFunction<T> toDouble, String @NotNull ... tagValues) {
    return super.getOrCreate(new GaugeParams<>(obj, toDouble), tagValues);
  }

  @Override
  public Optional<Gauge> get(String @NotNull ... tagValues) {
    return super.get(tagValues);
  }

}
