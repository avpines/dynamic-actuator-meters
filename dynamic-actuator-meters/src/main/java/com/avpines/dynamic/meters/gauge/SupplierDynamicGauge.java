package com.avpines.dynamic.meters.gauge;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Gauge.Builder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SupplierDynamicGauge extends AbstractDynamicGauge<Supplier<Number>> {

  SupplierDynamicGauge(
      @NotNull MeterRegistry registry,
      @NotNull String name,
      @NotNull BiFunction<String, GaugeParams<Supplier<Number>>, Builder<Supplier<Number>>> newInnerBuilder,
      @NotNull BiFunction<Builder<Supplier<Number>>, Collection<Tag>, Builder<Supplier<Number>>> tagger,
      @Nullable UnaryOperator<Builder<Supplier<Number>>> customizer,
      @NotNull Function<Builder<Supplier<Number>>, Gauge> registrar,
      String... tagKeys) {
    super(registry, name, newInnerBuilder, tagger, customizer, registrar, tagKeys);
  }

  public Gauge getOrCreate(Supplier<Number> f, String @NotNull ... tagValues) {
    return super.getOrCreate(new GaugeParams<>(f, null), tagValues);
  }

  @Override
  public Optional<Gauge> get(String @NotNull ... tagValues) {
    return super.get(tagValues);
  }
}