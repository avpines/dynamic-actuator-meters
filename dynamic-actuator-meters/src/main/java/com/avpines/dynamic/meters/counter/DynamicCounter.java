package com.avpines.dynamic.meters.counter;

import com.avpines.dynamic.meters.DynamicMeter;
import com.avpines.dynamic.meters.VoidParams;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Counter.Builder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DynamicCounter extends DynamicMeter<Counter, Builder, VoidParams> {

  public static @NotNull DynamicCounterBuilder builder(MeterRegistry registry, String name) {
    return new DynamicCounterBuilder(registry, name);
  }

  /**
   * {@inheritDoc}
   */
  DynamicCounter(
      @NotNull MeterRegistry registry,
      @NotNull String name,
      @NotNull BiFunction<String, VoidParams, Builder> newInnerBuilder,
      @NotNull BiFunction<Counter.Builder, Collection<Tag>, Counter.Builder> tagger,
      @Nullable UnaryOperator<Builder> customizer,
      @NotNull Function<Builder, Counter> registrar,
      String @NotNull ... tagKeys) {
    super(registry, name, newInnerBuilder, tagger, customizer, registrar, tagKeys);
  }

  public Counter getOrCreate(String @NotNull ... tagValues) {
    return super.getOrCreate(null, tagValues);
  }

}