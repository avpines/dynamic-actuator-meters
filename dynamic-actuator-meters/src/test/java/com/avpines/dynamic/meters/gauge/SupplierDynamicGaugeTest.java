package com.avpines.dynamic.meters.gauge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.avpines.dynamic.Conditions;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class SupplierDynamicGaugeTest {

  SimpleMeterRegistry smr;

  @BeforeEach
  void setup() {
    smr = new SimpleMeterRegistry();
  }

  @Test
  void noOperatorsNoTags() {
    String name = "my.metric";
    AtomicLong num = new AtomicLong(233);
    SupplierDynamicGauge dg = SupplierDynamicGauge
        .builder(smr, name)
        .build();
    Gauge g = dg.getOrCreate(num::get);
    List<Meter> meters = smr.getMeters();
    assertThat(meters).hasSize(1);
    assertThat(meters).filteredOn(m -> m.getId().getName().equals(name)).hasSize(1);
    assertThat(meters).flatExtracting(m -> m.getId().getTags()).isEmpty();
    assertThat(meters).extractingResultOf("value").containsExactly(233.0);
    assertThat(g.value()).isEqualTo(233.0);
  }

  @Test
  public void noOperatorsWithTags() {
    String name = "your.metric";
    SupplierDynamicGauge dg = DynamicGauge.builder(smr, name).tagKeys("change").build();
    AtomicInteger ai = new AtomicInteger();
    dg.getOrCreate(() -> ai.get() + 1, "add-1");
    dg.getOrCreate(() -> ai.get() - 2, "min-2");
    // Will return the same gauge as before, with the old obj and function (MicroMeter behaviour)
    dg.getOrCreate(() -> ai.get() + 12, "add-1");
    dg.getOrCreate(() -> ai.get() * 32, "min-2");
    ai.set(10);
    List<Meter> meters = smr.getMeters();
    assertThat(meters).hasSize(2);
    assertThat(meters).filteredOn(m -> m.getId().getName().equals(name)).hasSize(2);
    assertThat(meters)
        .filteredOn(Conditions.onTags(Tag.of("change", "add-1"))).hasSize(1)
        .map(m -> ((Gauge) m).value()).containsExactly(11.0);
    assertThat(meters)
        .filteredOn(Conditions.onTags(Tag.of("change", "min-2"))).hasSize(1)
        .map(m -> ((Gauge) m).value()).containsExactly(8.0);
  }

  @Test
  void withOperatorNoTags() {
    String name = "our.metric";
    String desc = "my very informative and interesting description";
    SupplierDynamicGauge dg = SupplierDynamicGauge.builder(smr, name)
        .customizer(b -> b.description(desc))
        .build();
    AtomicLong al = new AtomicLong();
    dg.getOrCreate(al::get);
    al.set(112233);
    List<Meter> meters = smr.getMeters();
    assertThat(meters).hasSize(1);
    assertThat(meters).filteredOn(m -> m.getId().getName().equals(name)).hasSize(1);
    assertThat(meters).flatExtracting(m -> m.getId().getTags()).isEmpty();
    assertThat(meters).extracting(m -> m.getId().getDescription()).contains(desc);
    assertThat(meters).extractingResultOf("value").containsExactly(112233.0);
  }

  @Test
  void withOperatorWithTags() {
    String name = "our.metric";
    SupplierDynamicGauge dg = SupplierDynamicGauge.builder(smr, name)
        .tagKey("t1")
        .customizer(b -> b.tag("another", "one"))
        .build();
    Set<Double> set = new HashSet<>(
        Arrays.asList(1.0, 2.0, 9.9999, 10.0, 10.0, 10.001, 11.0, 12.0, 1.0, 2.0, 3.0));
    dg.getOrCreate(() -> set.stream().max(Double::compare).orElse(Double.NaN), "max");
    dg.getOrCreate(() -> set.stream().reduce(Double::sum).orElse(Double.NaN), "sum");
    List<Meter> meters = smr.getMeters();
    assertThat(meters).hasSize(2);
    assertThat(meters).filteredOn(m -> m.getId().getName().equals(name)).hasSize(2);
    // tags added from the customizer will come before the explicitly set tags.
    assertThat(meters)
        .filteredOn(Conditions.onTags(Tag.of("t1", "max"), Tag.of("another", "one"))).hasSize(1)
        .extractingResultOf("value").containsExactly(12.0);
    assertThat(meters)
        .filteredOn(Conditions.onTags(Tag.of("t1", "sum"), Tag.of("another", "one"))).hasSize(1)
        .extractingResultOf("value").containsExactly(59.0009);
  }

  @Test
  void wrongNumberOfValues() {
    AtomicLong al = new AtomicLong();
    assertThatThrownBy(() ->
        SupplierDynamicGauge.builder(smr, "my-name")
            .tagKey("t1")
            .customizer(b -> b.tag("another", "one"))
            .build()
            .getOrCreate(al::get, "one", "two", "three")
    ).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() ->
        SupplierDynamicGauge.builder(smr, "my-name")
            .tagKey("t1")
            .customizer(b -> b.tag("another", "one"))
            .build()
            .getOrCreate(al::get, "one", "two", "three")
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getGaugeExists() {
    AtomicLong al = new AtomicLong();
    SupplierDynamicGauge dg = SupplierDynamicGauge.builder(smr, "hello")
        .tagKey("t1")
        .customizer(b -> b.tag("another", "one"))
        .build();
    Gauge g = dg.getOrCreate(al::get, "gt10");
    Optional<Gauge> og = dg.get("gt10");
    assertThat(og).isPresent().contains(g);
  }

  @Test
  void getGaugeNotExist() {
    SupplierDynamicGauge dg = DynamicGauge.builder(smr, "hello")
        .tagKey("t1")
        .customizer(b -> b.tag("another", "one"))
        .build();
    Set<Double> set = new HashSet<>();
    Optional<Gauge> og = dg.get("gt10");
    assertThat(og).isEmpty();
  }

}