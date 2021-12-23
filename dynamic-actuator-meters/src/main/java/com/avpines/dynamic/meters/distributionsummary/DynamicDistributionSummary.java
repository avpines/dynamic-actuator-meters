package com.avpines.dynamic.meters.distributionsummary;

import com.avpines.dynamic.meters.DynamicMeter;
import com.avpines.dynamic.meters.VoidParams;
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

public class DynamicDistributionSummary
    extends DynamicMeter<DistributionSummary, DistributionSummary.Builder, VoidParams> {

  public static @NotNull DynamicDistributionSummaryBuilder builder(
      MeterRegistry registry, String name) {
    return new DynamicDistributionSummaryBuilder(registry, name);
  }

  /**
   * {@inheritDoc}
   */
  DynamicDistributionSummary(
      @NotNull MeterRegistry registry, @NotNull String name,
      @NotNull BiFunction<String, VoidParams, Builder> newInnerBuilder,
      @NotNull BiFunction<Builder, Collection<Tag>, Builder> tagger,
      @Nullable UnaryOperator<Builder> customizer,
      @NotNull Function<Builder, DistributionSummary> registrar,
      String @NotNull ... tagKeys) {
    super(registry, name, newInnerBuilder, tagger, customizer, registrar, tagKeys);
  }

  public DistributionSummary getOrCreate(String @NotNull ... tagValues) {
    return super.getOrCreate(null, tagValues);
  }

}
