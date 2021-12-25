package com.avpines.dynamic.meters.gauge;

import com.avpines.dynamic.meters.OfType;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Gauge.Builder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.NotNull;

/**
 * Common parent class for all Gauge builders.
 *
 * @param <E> The Guage type that this builder will build.
 * @param <A> The builder type (For Builder pattern inheritance).
 */
public abstract class AbstractDynamicGaugeBuilder<E, A extends AbstractDynamicGaugeBuilder<E, A>> {

  MeterRegistry registry;
  String name;
  Collection<UnaryOperator<Builder<E>>> customizers;
  Collection<String> tagKeys;

  /**
   * Construct a new AbstractDynamicGaugeBuilder.
   *
   * @param registry To register generated meters.
   * @param name The meter name, all underlying meters that will be created will share this name.
   * @param ignored To infer the type of the Gauge.
   */
  public AbstractDynamicGaugeBuilder(MeterRegistry registry, String name, OfType<E> ignored) {
    this(registry, name);
  }

  /**
   * Construct a new AbstractDynamicGaugeBuilder.
   *
   * @param registry To register generated meters.
   * @param name The meter name, all underlying meters that will be created will share this name.
   * @param ignored To infer the type of the Gauge.
   */
  public AbstractDynamicGaugeBuilder(MeterRegistry registry, String name, Class<E> ignored) {
    // Class used to infer the type reference
    this(registry, name);
  }

  protected AbstractDynamicGaugeBuilder(MeterRegistry registry, String name) {
    this.registry = registry;
    this.name = name;
    this.customizers = new ArrayList<>();
    this.tagKeys = new ArrayList<>();
  }

  public A customizers(
      @NotNull Collection<UnaryOperator<Gauge.Builder<E>>> customizers) {
    this.customizers.addAll(customizers);
    return self();
  }

  public A customizer(
      @NotNull UnaryOperator<Gauge.Builder<E>> customizer) {
    this.customizers.add(customizer);
    return self();
  }

  public A tagKeys(@NotNull Collection<String> tagKeys) {
    this.tagKeys.addAll(tagKeys);
    return self();
  }

  public A tagKeys(String @NotNull... tagKeys) {
    return tagKeys(Arrays.asList(tagKeys));
  }

  public A tagKey(@NotNull String tagKey) {
    this.tagKeys.add(tagKey);
    return self();
  }

  abstract A self();

  /**
   * Build a new Dynamic Gauge.
   *
   * @return a new Dynamic Gauge.
   */
  abstract AbstractDynamicGauge<E> build();

  protected Function<Builder<E>, Gauge> registrar() {
    return b -> b.register(registry);
  }

  protected BiFunction<Builder<E>, Collection<Tag>, Builder<E>> tagger() {
    return (b, t) -> t != null ? b.tags(t) : b;
  }

  protected BiFunction<String, GaugeParams<E>, Builder<E>> innerBuilderCreator() {
    return (s, p) -> {
      assert p.getToDouble() != null;
      return Gauge.builder(name, p.getObj(), p.getToDouble());
    };
  }

}