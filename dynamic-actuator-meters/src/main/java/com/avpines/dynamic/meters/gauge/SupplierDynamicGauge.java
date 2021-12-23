package com.avpines.dynamic.meters.gauge;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Gauge.Builder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A dynamic gauge which uses a {@link Number} {@link Supplier} as the gauge type, registers
 * underlying {@link Gauge} with dynamic tag values.
 *
 * @see Gauge#builder(String, Supplier)
 */
public class SupplierDynamicGauge extends AbstractDynamicGauge<Supplier<Number>> {

  SupplierDynamicGauge(
      @NotNull MeterRegistry registry,
      @NotNull String name,
      @NotNull BiFunction<
          String,
          GaugeParams<Supplier<Number>>,
          Builder<Supplier<Number>>> newInnerBuilder,
      @NotNull BiFunction<Builder<
          Supplier<Number>>,
          Collection<Tag>,
          Builder<Supplier<Number>>> tagger,
      @Nullable UnaryOperator<Builder<Supplier<Number>>> customizer,
      @NotNull Function<Builder<Supplier<Number>>, Gauge> registrar,
      String... tagKeys) {
    super(registry, name, newInnerBuilder, tagger, customizer, registrar, tagKeys);
  }

  /**
   * Gets the requested meter with the given tag values. If such a meter did not previously exist, a
   * new one will be registered and returned. If a meter already existed, <b>the
   * Supplier&lt;Number&gt; given at the first time will be used.</b>
   *
   * @param f         A function that yields a number value for the gauge.
   * @param tagValues the tag values, should correspond to the given tag keys when this dynamic
   *                  meter was constructed.
   * @return A new {@link Gauge}.
   */
  public Gauge getOrCreate(Supplier<Number> f, String @NotNull... tagValues) {
    return super.getOrCreate(new GaugeParams<>(f, null), tagValues);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<Gauge> get(String @NotNull... tagValues) {
    return super.get(tagValues);
  }
}