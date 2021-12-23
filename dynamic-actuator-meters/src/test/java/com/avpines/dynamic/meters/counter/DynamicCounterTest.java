package com.avpines.dynamic.meters.counter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.avpines.dynamic.Conditions;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DynamicCounterTest {

  SimpleMeterRegistry smr;

  @BeforeEach
  void setup() {
    smr = new SimpleMeterRegistry();
  }

  @Test
  void noOperatorsNoTags() {
    String name = "my.metric";
    DynamicCounter c = DynamicCounter.builder(smr, name).build();
    c.getOrCreate().increment(122.7);
    List<Meter> meters = smr.getMeters();
    assertThat(meters).hasSize(1);
    assertThat(meters).filteredOn(m -> m.getId().getName().equals(name)).hasSize(1);
    assertThat(meters).flatExtracting(m -> m.getId().getTags()).isEmpty();
  }

  @Test
  public void noOperatorsWithTags() {
    String name = "your.metric";
    DynamicCounter c = DynamicCounter.builder(smr, name).tagKeys("t1", "t2").build();
    c.getOrCreate("t1-v1", "t2-v1").increment(5);
    c.getOrCreate("t1-v1", "t2-v2").increment(10);
    c.getOrCreate("t1-v2", "t2-v1").increment(20);
    c.getOrCreate("t1-v2", "t2-v2").increment(120);
    c.getOrCreate("t1-v1", "t2-v1").increment(60);
    c.getOrCreate("t1-v1", "t2-v2").increment(5);
    c.getOrCreate("t1-v2", "t2-v1").increment(15);
    List<Meter> meters = smr.getMeters();
    assertThat(meters).hasSize(4);
    assertThat(meters).filteredOn(m -> m.getId().getName().equals(name)).hasSize(4);
    assertThat(meters)
        .filteredOn(Conditions.onTags(Tag.of("t1", "t1-v1"), Tag.of("t2", "t2-v1"))).hasSize(1)
        .map(m -> ((Counter) m).count()).containsExactly(65.0);
    assertThat(meters)
        .filteredOn(Conditions.onTags(Tag.of("t1", "t1-v1"), Tag.of("t2", "t2-v2"))).hasSize(1)
        .map(m -> ((Counter) m).count()).containsExactly(15.0);
    assertThat(meters)
        .filteredOn(Conditions.onTags(Tag.of("t1", "t1-v2"), Tag.of("t2", "t2-v1"))).hasSize(1)
        .map(m -> ((Counter) m).count()).containsExactly(35.0);
    assertThat(meters)
        .filteredOn(Conditions.onTags(Tag.of("t1", "t1-v2"), Tag.of("t2", "t2-v2"))).hasSize(1)
        .map(m -> ((Counter) m).count()).containsExactly(120.0);

  }

  @Test
  void withOperatorNoTags() {
    String name = "our.metric";
    String desc = "my very informative and interesting description";
    DynamicCounter c = DynamicCounter.builder(smr, name).customizer(b -> b.baseUnit("boatloads").description(desc)).build();
    c.getOrCreate().increment(5);
    List<Meter> meters = smr.getMeters();
    assertThat(meters).hasSize(1);
    assertThat(meters).filteredOn(m -> m.getId().getName().equals(name)).hasSize(1);
    assertThat(meters).flatExtracting(m -> m.getId().getTags()).isEmpty();
    assertThat(meters).extracting(m -> m.getId().getDescription()).contains(desc);
  }

  @Test
  void withOperatorWithTags() {
    String name = "our.metric";
    DynamicCounter c = DynamicCounter.builder(smr, name)
        .tagKey("t1")
        .customizer(b -> b.tag("another", "one"))
        .build();
    c.getOrCreate("t1-v1").increment(2);
    c.getOrCreate("t1-v2").increment(1);
    List<Meter> meters = smr.getMeters();
    assertThat(meters).hasSize(2);
    assertThat(meters).filteredOn(m -> m.getId().getName().equals(name)).hasSize(2);
    // tags added from the customizer will come before the explicitly set tags.
    assertThat(meters)
        .filteredOn(Conditions.onTags(Tag.of("t1", "t1-v1"), Tag.of("another", "one"))).hasSize(1)
            .map(m -> ((Counter) m).count()).containsExactly(2.0);
    assertThat(meters)
        .filteredOn(Conditions.onTags(Tag.of("t1", "t1-v2"), Tag.of("another", "one"))).hasSize(1)
        .map(m -> ((Counter) m).count()).containsExactly(1.0);
  }

  @Test
  void wrongNumberOfValues() {
    // we expect values only for the dynamic tags given in the 'tagKeys'.
    assertThatThrownBy(() ->
        DynamicCounter.builder(smr, "my-name")
        .tagKey("t1")
        .customizer(b -> b.tag("another", "one"))
        .build().getOrCreate("one", "two", "three")
    ).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() ->
        DynamicCounter.builder(smr, "your-name")
            .tagKey("t1")
            .customizer(b -> b.tag("another", "one"))
            .build().getOrCreate()
    ).isInstanceOf(IllegalArgumentException.class);
  }

}