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
import org.jetbrains.annotations.Nullable;

public abstract class AbstractDynamicGaugeBuilder<E, A extends AbstractDynamicGaugeBuilder<E, A>> {

  MeterRegistry registry;
  String name;
  Collection<UnaryOperator<Builder<E>>> customizers;
  Collection<String> tagKeys;

  public AbstractDynamicGaugeBuilder(MeterRegistry registry, String name, OfType<E> ignored) {
    // OfType used to infer the type reference
    this(registry, name);
  }

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

  public A tagKeys(String @NotNull ... tagKeys) {
    return tagKeys(Arrays.asList(tagKeys));
  }

  public A tagKey(@NotNull String tagKey) {
    this.tagKeys.add(tagKey);
    return self();
  }

  abstract A self();

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

  protected @Nullable UnaryOperator<Gauge.Builder<E>> reduceCustomizers(
      @NotNull Collection<UnaryOperator<Gauge.Builder<E>>> customizers) {
    return customizers.isEmpty()
        ? null
        : customizers.stream().reduce((l, r) -> b -> l.andThen(r).apply(b)).orElse(null);
  }

}