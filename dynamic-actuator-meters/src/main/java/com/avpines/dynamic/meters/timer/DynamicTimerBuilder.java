package com.avpines.dynamic.meters.timer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Builder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.NotNull;

/**
 * Builder for {@link DynamicTimer} meters.
 */
public class DynamicTimerBuilder {

  MeterRegistry registry;
  String name;
  Collection<UnaryOperator<Builder>> customizers;
  Collection<String> tagKeys;

  /**
   * Construct a new DynamicTimerBuilder.
   *
   * @param registry To register the underlying meters.
   * @param name     Meter name, all underlying meters will share that name.
   */
  public DynamicTimerBuilder(
      MeterRegistry registry,
      String name) {
    this.registry = registry;
    this.name = name;
    this.customizers = new ArrayList<>();
    this.tagKeys = new ArrayList<>();
  }

  public DynamicTimerBuilder customizers(
      @NotNull Collection<UnaryOperator<Timer.Builder>> customizers) {
    this.customizers.addAll(customizers);
    return this;
  }

  public DynamicTimerBuilder customizer(@NotNull UnaryOperator<Timer.Builder> customizer) {
    this.customizers.add(customizer);
    return this;
  }

  public DynamicTimerBuilder tagKeys(@NotNull Collection<String> tagKeys) {
    this.tagKeys.addAll(tagKeys);
    return this;
  }

  public DynamicTimerBuilder tagKeys(String @NotNull... tagKeys) {
    return tagKeys(Arrays.asList(tagKeys));
  }

  public DynamicTimerBuilder tagKey(@NotNull String tagKey) {
    this.tagKeys.add(tagKey);
    return this;
  }

  /**
   * Build a new {@link DynamicTimer}.
   *
   * @return a new DynamicTimer.
   */
  public DynamicTimer build() {
    return new DynamicTimer(
        registry,
        name,
        Timer::builder,
        (b, t) -> t != null ? b.tags(t) : b,
        customizers,
        b -> b.register(registry),
        tagKeys.toArray(new String[0])
    );
  }

}