package com.avpines.dynamic.meters.timer;

import com.avpines.dynamic.meters.DynamicMeter;
import com.avpines.dynamic.meters.VoidParams;
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

public class DynamicTimer extends DynamicMeter<Timer, Builder, VoidParams> {

  public static @NotNull DynamicTimerBuilder builder(MeterRegistry registry, String name) {
    return new DynamicTimerBuilder(registry, name);
  }

  /**
   * {@inheritDoc}
   */
  DynamicTimer(
      @NotNull MeterRegistry registry,
      @NotNull String name,
      @NotNull BiFunction<String, VoidParams, Timer.Builder> newInnerBuilder,
      @NotNull BiFunction<Timer.Builder, Collection<Tag>, Timer.Builder> tagger,
      @Nullable UnaryOperator<Builder> customizer,
      @NotNull Function<Builder, Timer> registrar,
      String @NotNull ... tagKeys) {
    super(registry, name, newInnerBuilder, tagger, customizer, registrar, tagKeys);
  }

  public Timer getOrCreate(String @NotNull ... tagValues) {
    return super.getOrCreate(null, tagValues);
  }

}