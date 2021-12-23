package com.avpines.dynamic.meters.timer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.avpines.dynamic.Conditions;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DynamicTimerTest {

  SimpleMeterRegistry smr;

  @BeforeEach
  void setup() {
    smr = new SimpleMeterRegistry();
  }

  @Test
  void noOperatorsNoTags() {
    String name = "my.metric";
    DynamicTimer dt = DynamicTimer.builder(smr, name).build();
    dt.getOrCreate().record(5, TimeUnit.SECONDS);
    List<Meter> meters = smr.getMeters();
    assertThat(meters).hasSize(1);
    assertThat(meters).filteredOn(m -> m.getId().getName().equals(name)).hasSize(1);
    assertThat(meters).flatExtracting(m -> m.getId().getTags()).isEmpty();
  }

  @Test
  public void noOperatorsWithTags() {
    String name = "your.metric";
    DynamicTimer dt = DynamicTimer.builder(smr, name).tagKeys("tag-1", "tag-2").build();
    dt.getOrCreate("tag-1-v-1", "tag-2-v-1").record(5, TimeUnit.SECONDS);
    dt.getOrCreate("tag-1-v-1", "tag-2-v-2").record(10, TimeUnit.SECONDS);
    dt.getOrCreate("tag-1-v-2", "tag-2-v-1").record(20, TimeUnit.SECONDS);
    dt.getOrCreate("tag-1-v-2", "tag-2-v-2").record(120, TimeUnit.SECONDS);
    dt.getOrCreate("tag-1-v-1", "tag-2-v-1").record(60, TimeUnit.SECONDS);
    dt.getOrCreate("tag-1-v-1", "tag-2-v-2").record(5, TimeUnit.SECONDS);
    dt.getOrCreate("tag-1-v-2", "tag-2-v-1").record(15, TimeUnit.SECONDS);
    List<Meter> meters = smr.getMeters();
    assertThat(meters).hasSize(4);
    assertThat(meters).filteredOn(m -> m.getId().getName().equals(name)).hasSize(4);
    assertUniqueTagCombination(meters, Tag.of("tag-1", "tag-1-v-1"), Tag.of("tag-2", "tag-2-v-1"));
    assertUniqueTagCombination(meters, Tag.of("tag-1", "tag-1-v-1"), Tag.of("tag-2", "tag-2-v-2"));
    assertUniqueTagCombination(meters, Tag.of("tag-1", "tag-1-v-2"), Tag.of("tag-2", "tag-2-v-1"));
    assertUniqueTagCombination(meters, Tag.of("tag-1", "tag-1-v-2"), Tag.of("tag-2", "tag-2-v-2"));
  }

  @Test
  void withOperatorNoTags() {
    String name = "our.metric";
    String desc = "my very informative and interesting description";
    DynamicTimer dt = DynamicTimer.builder(smr, name).customizer(b -> b.description(desc)).build();
    dt.getOrCreate().record(5, TimeUnit.SECONDS);
    List<Meter> meters = smr.getMeters();
    assertThat(meters).hasSize(1);
    assertThat(meters).filteredOn(m -> m.getId().getName().equals(name)).hasSize(1);
    assertThat(meters).flatExtracting(m -> m.getId().getTags()).isEmpty();
    assertThat(meters).extracting(m -> m.getId().getDescription()).contains(desc);
  }

  @Test
  void withOperatorWithTags() {
    String name = "our.metric";
    DynamicTimer dt = DynamicTimer.builder(smr, name)
        .customizer(b -> b.tag("another", "one"))
        .tagKeys("tag-1")
        .build();
    dt.getOrCreate("tag-1-val-1").record(2, TimeUnit.DAYS);
    dt.getOrCreate("tag-1-val-2").record(1, TimeUnit.HOURS);
    List<Meter> meters = smr.getMeters();
    assertThat(meters).hasSize(2);
    assertThat(meters).filteredOn(m -> m.getId().getName().equals(name)).hasSize(2);
    // tags added from the customizer will come before the explicitly set tags.
    assertUniqueTagCombination(meters, Tag.of("tag-1", "tag-1-val-1"), Tag.of("another", "one"));
    assertUniqueTagCombination(meters, Tag.of("tag-1", "tag-1-val-2"), Tag.of("another", "one"));
  }

  @Test
  void wrongNumberOfValues() {
    // we expect values only for the dynamic tags given in the 'tagKeys'.
    assertThatThrownBy(() ->
        DynamicTimer.builder(smr, "hello")
            .customizer(b -> b.tag("another", "one"))
            .tagKeys("tag-1", "tag-2")
            .build().getOrCreate("one", "two", "three")
    ).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() ->
        DynamicTimer.builder(smr, "world")
            .customizer(b -> b.tag("another", "one"))
            .tagKeys("tag-1", "tag-2")
            .build().getOrCreate()
    ).isInstanceOf(IllegalArgumentException.class);
  }

  private void assertUniqueTagCombination(List<Meter> meters, Tag... tags) {
    assertThat(meters).filteredOn(Conditions.onTags(tags)).hasSize(1);
  }

}