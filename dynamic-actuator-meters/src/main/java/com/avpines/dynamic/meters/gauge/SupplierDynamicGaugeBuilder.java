package com.avpines.dynamic.meters.gauge;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Gauge.Builder;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 *
 */
public class SupplierDynamicGaugeBuilder
    extends AbstractDynamicGaugeBuilder<Supplier<Number>, SupplierDynamicGaugeBuilder> {

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

  @Override
  public SupplierDynamicGauge build() {
    return new SupplierDynamicGauge(
        registry,
        name,
        innerBuilderCreator(),
        tagger(),
        reduceCustomizers(customizers),
        registrar(),
        tagKeys.toArray(new String[0])
    );
  }
}