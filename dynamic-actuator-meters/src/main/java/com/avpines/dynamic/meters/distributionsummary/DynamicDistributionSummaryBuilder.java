package com.avpines.dynamic.meters.distributionsummary;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.DistributionSummary.Builder;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.NotNull;

/**
 * Builder for {@link DynamicDistributionSummary} meters.
 */
public class DynamicDistributionSummaryBuilder {

  MeterRegistry registry;
  String name;
  Collection<UnaryOperator<Builder>> customizers;
  Collection<String> tagKeys;

  /**
   * Construct a new DynamicDistributionSummaryBuilder.
   *
   * @param registry To register the underlying meters.
   * @param name     Meter name, all underlying meters will share that name.
   */
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

  public DynamicDistributionSummaryBuilder tagKeys(String @NotNull... tagKeys) {
    return tagKeys(Arrays.asList(tagKeys));
  }

  public DynamicDistributionSummaryBuilder tagKey(@NotNull String tagKey) {
    this.tagKeys.add(tagKey);
    return this;
  }

  /**
   * Build a new {@link DynamicDistributionSummary}.
   *
   * @return a new DynamicDistributionSummary.
   */
  public DynamicDistributionSummary build() {
    return new DynamicDistributionSummary(
        registry,
        name,
        DistributionSummary::builder,
        (b, t) -> t != null ? b.tags(t) : b,
        customizers,
        b -> b.register(registry),
        tagKeys.toArray(new String[0])
    );
  }

}