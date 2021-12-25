package com.avpines.dynamic.meters.gauge;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Gauge.Builder;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Builder for {@link SupplierDynamicGauge} meters.
 */
public class SupplierDynamicGaugeBuilder
    extends AbstractDynamicGaugeBuilder<Supplier<Number>, SupplierDynamicGaugeBuilder> {

  /**
   * Constrcut a new SupplierDynamicGaugeBuilder.
   *
   * @param registry To register generated meters.
   * @param name The meter name that will be shared among all the underlying meters.
   */
  public SupplierDynamicGaugeBuilder(MeterRegistry registry, String name) {
    super(registry, name);
  }

  @Override
  SupplierDynamicGaugeBuilder self() {
    return this;
  }

  protected BiFunction<String, GaugeParams<Supplier<Number>>, Builder<Supplier<Number>>>
      innerBuilderCreator() {
    return (s, p) -> Gauge.builder(name, p.getObj());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SupplierDynamicGauge build() {
    return new SupplierDynamicGauge(
        registry,
        name,
        innerBuilderCreator(),
        tagger(),
        customizers,
        registrar(),
        tagKeys.toArray(new String[0])
    );
  }
}