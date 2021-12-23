package com.avpines.dynamic.meters.distributionsummary;

import com.avpines.dynamic.meters.ParameterlessDynamicMeter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.DistributionSummary.Builder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A dynamic distribution summary, registers underlying {@link DistributionSummary} with dynamic tag
 * values.
 */
public class DynamicDistributionSummary
    extends ParameterlessDynamicMeter<DistributionSummary, Builder> {

  public static @NotNull DynamicDistributionSummaryBuilder builder(
      MeterRegistry registry, String name) {
    return new DynamicDistributionSummaryBuilder(registry, name);
  }

  /**
   * Construct a new DynamicDistributionSummary.
   *
   * @param registry        To register the underlying meters.
   * @param name            Meter name, all underlying meters will share that name.
   * @param newInnerBuilder A function to construct the underlying meter builder.
   * @param tagger          A function to dynamically add the tags.
   * @param customizer      For any additional customization to the underlying meter.
   * @param registrar       Function to register the underlying meters.
   * @param tagKeys         The keys that this meter will have, and allow their values to be added
   *                        dynamically.
   */
  DynamicDistributionSummary(
      @NotNull MeterRegistry registry, @NotNull String name,
      @NotNull Function<String, Builder> newInnerBuilder,
      @NotNull BiFunction<Builder, Collection<Tag>, Builder> tagger,
      @Nullable UnaryOperator<Builder> customizer,
      @NotNull Function<Builder, DistributionSummary> registrar,
      String @NotNull... tagKeys) {
    super(registry, name, newInnerBuilder, tagger, customizer, registrar, tagKeys);
  }

  /**
   * {@inheritDoc}
   */
  public DistributionSummary getOrCreate(String @NotNull... tagValues) {
    return super.getOrCreate(null, tagValues);
  }

}
