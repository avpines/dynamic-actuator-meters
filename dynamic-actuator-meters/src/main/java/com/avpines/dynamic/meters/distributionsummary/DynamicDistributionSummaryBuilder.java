package com.avpines.dynamic.meters.distributionsummary;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.DistributionSummary.Builder;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DynamicDistributionSummaryBuilder {
  MeterRegistry registry;
  String name;
  Collection<UnaryOperator<Builder>> customizers;
  Collection<String> tagKeys;

  public DynamicDistributionSummaryBuilder(
      MeterRegistry registry,
      String name) {
    this.registry = registry;
    this.name = name;
    this.customizers = new ArrayList<>();
    this.tagKeys = new ArrayList<>();
  }

  public DynamicDistributionSummaryBuilder customizers(
      @NotNull Collection<UnaryOperator<DistributionSummary.Builder>> customizers) {
    this.customizers.addAll(customizers);
    return this;
  }

  public DynamicDistributionSummaryBuilder customizer(
      @NotNull UnaryOperator<DistributionSummary.Builder> customizer) {
    this.customizers.add(customizer);
    return this;
  }

  public DynamicDistributionSummaryBuilder tagKeys(@NotNull Collection<String> tagKeys) {
    this.tagKeys.addAll(tagKeys);
    return this;
  }

  public DynamicDistributionSummaryBuilder tagKeys(String @NotNull ... tagKeys) {
    return tagKeys(Arrays.asList(tagKeys));
  }

  public DynamicDistributionSummaryBuilder tagKey(@NotNull String tagKey) {
    this.tagKeys.add(tagKey);
    return this;
  }

  public DynamicDistributionSummary build() {
    return new DynamicDistributionSummary(
        registry,
        name,
        (k, p) -> DistributionSummary.builder(k),
        (b, t) -> b != null ? b.tags(t) : b,
        reduceCustomizers(customizers),
        b -> b.register(registry),
        tagKeys.toArray(new String[0])
    );
  }

  private @Nullable UnaryOperator<DistributionSummary.Builder> reduceCustomizers(
      @NotNull Collection<UnaryOperator<DistributionSummary.Builder>> customizers) {
    return customizers.isEmpty()
        ? null
        : customizers.stream().reduce((l, r) -> b -> l.andThen(r).apply(b)).orElse(null);
  }
}