package com.avpines.dynamic.meters.counter;

import com.avpines.dynamic.meters.ParameterlessDynamicMeter;
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

/**
 * A dynamic counter, registers underlying {@link Counter} with dynamic tag values.
 */
public class DynamicCounter extends ParameterlessDynamicMeter<Counter, Builder> {

  public static @NotNull DynamicCounterBuilder builder(MeterRegistry registry, String name) {
    return new DynamicCounterBuilder(registry, name);
  }

  /**
   * Construct a new DynamicCounter.
   *
   * @param registry        To register the underlying meters.
   * @param name            Meter name, all underlying meters will share that name.
   * @param newInnerBuilder A function to construct the underlying meter builder.
   * @param tagger          A function to dynamically add the tags.
   * @param customizers     For any additional customization to the underlying meter.
   * @param registrar       Function to register the underlying meters.
   * @param tagKeys         The keys that this meter will have, and allow their values to be added
   *                        dynamically.
   */
  DynamicCounter(
      @NotNull MeterRegistry registry,
      @NotNull String name,
      @NotNull Function<String, Builder> newInnerBuilder,
      @NotNull BiFunction<Builder, Collection<Tag>, Counter.Builder> tagger,
      @Nullable Collection<UnaryOperator<Builder>> customizers,
      @NotNull Function<Builder, Counter> registrar,
      String @NotNull... tagKeys) {
    super(registry, name, newInnerBuilder, tagger, customizers, registrar, tagKeys);
  }

}