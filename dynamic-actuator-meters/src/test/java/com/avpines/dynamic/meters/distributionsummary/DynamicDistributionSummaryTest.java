package com.avpines.dynamic.meters.distributionsummary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.avpines.dynamic.Conditions;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DynamicDistributionSummaryTest {

  SimpleMeterRegistry smr;

  @BeforeEach
  void setup() {
    smr = new SimpleMeterRegistry();
  }

  @Test
  void noOperatorsNoTags() {
    String name = "my.metric";
    DynamicDistributionSummary dds = DynamicDistributionSummary.builder(smr, name).build();
    dds.getOrCreate().record(5);
    List<Meter> meters = smr.getMeters();
    assertThat(meters).hasSize(1);
    assertThat(meters).filteredOn(m -> m.getId().getName().equals(name)).hasSize(1);
    assertThat(meters).flatExtracting(m -> m.getId().getTags()).isEmpty();
    assertThat(meters).extractingResultOf("count").containsExactly(1L);
    assertThat(meters).extractingResultOf("totalAmount").containsExactly(5.0);
  }

  @Test
  public void noOperatorsWithTags() {
    String name = "your.metric";
    DynamicDistributionSummary dds = DynamicDistributionSummary.builder(smr, name)
        .tagKeys("t1", "t2")
        .build();
    dds.getOrCreate("t1-v1", "t2-v1").record(5);
    dds.getOrCreate("t1-v1", "t2-v2").record(10);
    dds.getOrCreate("t1-v2", "t2-v1").record(20);
    dds.getOrCreate("t1-v2", "t2-v2").record(120);
    dds.getOrCreate("t1-v1", "t2-v1").record(60);
    dds.getOrCreate("t1-v1", "t2-v2").record(5);
    dds.getOrCreate("t1-v2", "t2-v1").record(15);
    List<Meter> meters = smr.getMeters();
    assertThat(meters).hasSize(4);
    assertThat(meters).filteredOn(m -> m.getId().getName().equals(name)).hasSize(4);
    assertThat(meters)
        .filteredOn(Conditions.onTags(Tag.of("t1", "t1-v1"), Tag.of("t2", "t2-v1"))).hasSize(1)
        .extractingResultOf("totalAmount").containsExactly(65.0);
    assertThat(meters)
        .filteredOn(Conditions.onTags(Tag.of("t1", "t1-v1"), Tag.of("t2", "t2-v2"))).hasSize(1)
        .extractingResultOf("totalAmount").containsExactly(15.0);
    assertThat(meters)
        .filteredOn(Conditions.onTags(Tag.of("t1", "t1-v2"), Tag.of("t2", "t2-v1"))).hasSize(1)
        .extractingResultOf("totalAmount").containsExactly(35.0);
    assertThat(meters)
        .filteredOn(Conditions.onTags(Tag.of("t1", "t1-v2"), Tag.of("t2", "t2-v2"))).hasSize(1)
        .extractingResultOf("totalAmount").containsExactly(120.0);
  }

  @Test
  void withOperatorNoTags() {
    String name = "our.metric";
    String desc = "my very informative and interesting description";
    DynamicDistributionSummary dds = DynamicDistributionSummary
        .builder(smr, name).customizer(b -> b.description(desc)).build();
    dds.getOrCreate().record(5);
    dds.getOrCreate().record(1);
    List<Meter> meters = smr.getMeters();
    assertThat(meters).hasSize(1);
    assertThat(meters).filteredOn(m -> m.getId().getName().equals(name)).hasSize(1);
    assertThat(meters).flatExtracting(m -> m.getId().getTags()).isEmpty();
    assertThat(meters).extracting(m -> m.getId().getDescription()).contains(desc);
    assertThat(meters).extractingResultOf("count").containsExactly(2L);
    assertThat(meters).extractingResultOf("totalAmount").containsExactly(6.0);
  }

  @Test
  void withOperatorWithTags() {
    String name = "our.metric";
    DynamicDistributionSummary dds = DynamicDistributionSummary.builder(smr, name)
        .customizer(b -> b.tag("another", "one"))
        .tagKeys("t1")
        .build();
    dds.getOrCreate("t1-v1").record(2);
    dds.getOrCreate("t1-v2").record(1);
    List<Meter> meters = smr.getMeters();
    assertThat(meters).hasSize(2);
    assertThat(meters).filteredOn(m -> m.getId().getName().equals(name)).hasSize(2);
    // tags added from the customizer will come before the explicitly set tags.
    assertThat(meters)
        .filteredOn(Conditions.onTags(Tag.of("t1", "t1-v1"), Tag.of("another", "one"))).hasSize(1)
        .extractingResultOf("totalAmount").containsExactly(2.0);
    assertThat(meters)
        .filteredOn(Conditions.onTags(Tag.of("t1", "t1-v2"), Tag.of("another", "one"))).hasSize(1)
        .extractingResultOf("totalAmount").containsExactly(1.0);
  }

  @Test
  void wrongNumberOfValues() {
    assertThatThrownBy(() ->
        DynamicDistributionSummary.builder(smr, "hello")
            .customizer(b -> b.tag("another", "one"))
            .tagKeys("t1", "t2")
            .build().getOrCreate("one", "two", "three")
    ).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() ->
        DynamicDistributionSummary.builder(smr, "world")
            .customizer(b -> b.tag("another", "one"))
            .tagKeys("t1", "t2")
            .build().getOrCreate()
    ).isInstanceOf(IllegalArgumentException.class);
  }

}