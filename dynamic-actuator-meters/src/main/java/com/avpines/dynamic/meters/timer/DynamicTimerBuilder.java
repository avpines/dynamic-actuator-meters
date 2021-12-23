package com.avpines.dynamic.meters.timer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Builder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public class DynamicTimerBuilder {

  MeterRegistry registry;
  String name;
  Collection<UnaryOperator<Builder>> customizers;
  Collection<String> tagKeys;

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

  public DynamicTimerBuilder tagKeys(String @NotNull ... tagKeys) {
    return tagKeys(Arrays.asList(tagKeys));
  }

  public DynamicTimerBuilder tagKey(@NotNull String tagKey) {
    this.tagKeys.add(tagKey);
    return this;
  }

  public DynamicTimer build() {
    return new DynamicTimer(
        registry,
        name,
        (k, p) -> Timer.builder(k),
        (b, t) -> b != null ? b.tags(t) : b,
        reduceCustomizers(customizers),
        b -> b.register(registry),
        tagKeys.toArray(new String[0])
    );
  }

  private @Nullable UnaryOperator<Timer.Builder> reduceCustomizers(
      @NotNull Collection<UnaryOperator<Timer.Builder>> customizers) {
    return customizers.isEmpty()
        ? null
        : customizers.stream().reduce((l, r) -> b -> l.andThen(r).apply(b)).orElse(null);
  }

}