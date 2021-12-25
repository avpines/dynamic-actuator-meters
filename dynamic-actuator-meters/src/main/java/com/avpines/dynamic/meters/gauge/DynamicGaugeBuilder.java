package com.avpines.dynamic.meters.gauge;

import com.avpines.dynamic.meters.OfType;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Builder for {@link DynamicGauge} meters.
 */
public class DynamicGaugeBuilder<E> extends
    AbstractDynamicGaugeBuilder<E, DynamicGaugeBuilder<E>> {

  public DynamicGaugeBuilder(MeterRegistry registry, String name, OfType<E> ignored) {
    super(registry, name, ignored);
  }

  public DynamicGaugeBuilder(MeterRegistry registry, String name, Class<E> ignored) {
    super(registry, name, ignored);
  }

  @Override
  DynamicGaugeBuilder<E> self() {
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DynamicGauge<E> build() {
    return new DynamicGauge<>(
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