package com.avpines.dynamic.meters.counter;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Counter.Builder;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Builder for {@link DynamicCounter} meters.
 */
public class DynamicCounterBuilder {

  MeterRegistry registry;
  String name;
  Collection<UnaryOperator<Builder>> customizers;
  Collection<String> tagKeys;

  /**
   * Construct a new DynamicCounterBuilder.
   *
   * @param registry To register generated meters.
   * @param name     The meter name that will be shared among all the underlying meters.
   */
  public DynamicCounterBuilder(
      MeterRegistry registry,
      String name) {
    this.registry = registry;
    this.name = name;
    this.customizers = new ArrayList<>();
    this.tagKeys = new ArrayList<>();

  }

  public DynamicCounterBuilder customizers(
      @NotNull Collection<UnaryOperator<Counter.Builder>> customizers) {
    this.customizers.addAll(customizers);
    return this;
  }

  public DynamicCounterBuilder customizer(@NotNull UnaryOperator<Counter.Builder> customizer) {
    this.customizers.add(customizer);
    return this;
  }

  public DynamicCounterBuilder tagKeys(@NotNull Collection<String> tagKeys) {
    this.tagKeys.addAll(tagKeys);
    return this;
  }

  public DynamicCounterBuilder tagKeys(String @NotNull... tagKeys) {
    return tagKeys(Arrays.asList(tagKeys));
  }

  public DynamicCounterBuilder tagKey(@NotNull String tagKey) {
    this.tagKeys.add(tagKey);
    return this;
  }

  /**
   * Build a new {@link DynamicCounter}.
   *
   * @return a new DynamicCounter.
   */
  public DynamicCounter build() {
    return new DynamicCounter(
        registry,
        name,
        Counter::builder,
        (b, t) -> t != null ? b.tags(t) : b,
        reduceCustomizers(customizers),
        b -> b.register(registry),
        tagKeys.toArray(new String[0])
    );
  }

  private @Nullable UnaryOperator<Counter.Builder> reduceCustomizers(
      @NotNull Collection<UnaryOperator<Counter.Builder>> customizers) {
    return customizers.isEmpty()
        ? null
        : customizers.stream().reduce((l, r) -> b -> l.andThen(r).apply(b)).orElse(null);
  }

}