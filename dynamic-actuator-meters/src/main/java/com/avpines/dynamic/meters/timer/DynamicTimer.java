package com.avpines.dynamic.meters.timer;

import com.avpines.dynamic.meters.ParameterlessDynamicMeter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Builder;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A dynamic timer, registers underlying {@link Timer} with dynamic tag values.
 */
public class DynamicTimer extends ParameterlessDynamicMeter<Timer, Builder> {

  /**
   * Builder for a {@link DynamicTimerBuilder}.
   *
   * @param registry To register generated meters.
   * @param name     The meter name that will be shared among all the underlying meters.
   * @return a new dynamic timer builder.
   */
  public static @NotNull DynamicTimerBuilder builder(MeterRegistry registry, String name) {
    return new DynamicTimerBuilder(registry, name);
  }

  /**
   * Construct a new DynamicTimer.
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
  DynamicTimer(
      @NotNull MeterRegistry registry,
      @NotNull String name,
      @NotNull Function<String, Timer.Builder> newInnerBuilder,
      @NotNull BiFunction<Timer.Builder, Collection<Tag>, Timer.Builder> tagger,
      @Nullable Collection<UnaryOperator<Timer.Builder>> customizers,
      @NotNull Function<Builder, Timer> registrar,
      String @NotNull... tagKeys) {
    super(registry, name, newInnerBuilder, tagger, customizers, registrar, tagKeys);
  }

}