package com.avpines.dynamic.meters.gauge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.avpines.dynamic.meters.OfType;
import com.avpines.dynamic.meters.gauge.DynamicGauge;
import com.avpines.dynamic.Conditions;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class DynamicGaugeTest {

  SimpleMeterRegistry smr;

  @BeforeEach
  void setup() {
    smr = new SimpleMeterRegistry();
  }

    @Test
  void noOperatorsNoTags() {
    String name = "my.metric";
    DynamicGauge<List<String>> dg = DynamicGauge
        .builder(smr, name, new OfType<List<String>>() {})
        .build();
    List<String> list = new ArrayList<>();
    dg.getOrCreate(list, List::size);
    list.addAll(Arrays.asList("once", "upon", "a", "time"));
    List<Meter> meters = smr.getMeters();
    assertThat(meters).hasSize(1);
    assertThat(meters).filteredOn(m -> m.getId().getName().equals(name)).hasSize(1);
    assertThat(meters).flatExtracting(m -> m.getId().getTags()).isEmpty();
    assertThat(meters).extractingResultOf("value").containsExactly(4.0);
  }

  @Test
  public void noOperatorsWithTags() {
    String name = "your.metric";
    DynamicGauge<List<String>> dg = DynamicGauge
        .builder(smr, name, new OfType<List<String>>() {})
        .tagKeys("min", "max")
        .build();
    List<String> list = new ArrayList<>();
    dg.getOrCreate(list, l -> l.stream().filter(s -> between(s, 1, 3)).count(), "min-1", "max-3");
    dg.getOrCreate(list, l -> l.stream().filter(s -> between(s, 2, 4)).count(), "min-2", "max-4");
    dg.getOrCreate(list, l -> l.stream().filter(s -> between(s, 3, 5)).count(), "min-3", "max-5");
    // Will return the same gauge as before, with the old obj and function (MicroMeter behaviour)
    dg.getOrCreate(list, l -> l.stream().filter(s -> between(s, 4, 6)).count(), "min-1", "max-3");
    dg.getOrCreate(list, l -> l.stream().filter(s -> between(s, 5, 7)).count(), "min-2", "max-4");
    dg.getOrCreate(list, l -> l.stream().filter(s -> between(s, 6, 8)).count(), "min-3", "max-5");
    list.addAll(Arrays.asList("a", "as", "who", "why", "why?", "what", "when", "where", "a thesaurus"));
    List<Meter> meters = smr.getMeters();
    assertThat(meters).hasSize(3);
    assertThat(meters).filteredOn(m -> m.getId().getName().equals(name)).hasSize(3);
    assertThat(meters)
        .filteredOn(Conditions.onTags(Tag.of("min", "min-1"), Tag.of("max", "max-3"))).hasSize(1)
        .map(m -> ((Gauge) m).value()).containsExactly(4.0);
    assertThat(meters)
        .filteredOn(Conditions.onTags(Tag.of("min", "min-2"), Tag.of("max", "max-4"))).hasSize(1)
        .map(m -> ((Gauge) m).value()).containsExactly(6.0);
    assertThat(meters)
        .filteredOn(Conditions.onTags(Tag.of("min", "min-3"), Tag.of("max", "max-5"))).hasSize(1)
        .map(m -> ((Gauge) m).value()).containsExactly(6.0);
  }

  @Test
  void withOperatorNoTags() {
    String name = "our.metric";
    String desc = "my very informative and interesting description";
    DynamicGauge<AtomicLong> dg = DynamicGauge.builder(smr, name, AtomicLong.class)
        .customizer(b -> b.description(desc)).build();
    AtomicLong al = new AtomicLong();
    dg.getOrCreate(al, AtomicLong::get);
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
    DynamicGauge<Set<Double>> dg = DynamicGauge.builder(smr, name, new OfType<Set<Double>>() {})
        .tagKey("t1")
        .customizer(b -> b.tag("another", "one"))
        .build();
    Set<Double> set = new HashSet<>();
    dg.getOrCreate(set,s -> s.stream().filter(d -> d > 10.0).count(), "gt10");
    dg.getOrCreate(set,s -> s.stream().filter(d -> d < 10.0).count(), "lt10");
    set.addAll(Arrays.asList(1.0, 2.0, 9.9999, 10.0 ,10.0, 10.001, 11.0, 12.0, 1.0, 2.0, 3.0));
    List<Meter> meters = smr.getMeters();
    assertThat(meters).hasSize(2);
    assertThat(meters).filteredOn(m -> m.getId().getName().equals(name)).hasSize(2);
    // tags added from the customizer will come before the explicitly set tags.
    assertThat(meters)
        .filteredOn(Conditions.onTags(Tag.of("t1", "gt10"), Tag.of("another", "one"))).hasSize(1)
        .extractingResultOf("value").containsExactly(3.0);
    assertThat(meters)
        .filteredOn(Conditions.onTags(Tag.of("t1", "lt10"), Tag.of("another", "one"))).hasSize(1)
        .extractingResultOf("value").containsExactly(4.0);
  }

  @Test
  void wrongNumberOfValues() {
    // we expect values only for the dynamic tags given in the 'tagKeys'.
    assertThatThrownBy(() ->
        DynamicGauge.builder(smr, "my-name", AtomicLong.class)
            .tagKey("t1")
            .customizer(b -> b.tag("another", "one"))
            .build()
            .getOrCreate(new AtomicLong(), AtomicLong::get, "one", "two", "three")
    ).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() ->
        DynamicGauge.builder(smr, "my-name", AtomicLong.class)
            .tagKey("t1")
            .customizer(b -> b.tag("another", "one"))
            .build()
            .getOrCreate(new AtomicLong(), AtomicLong::get, "one", "two", "three")
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getGaugeExists() {
    DynamicGauge<Set<Double>> dg = DynamicGauge.builder(smr, "hello", new OfType<Set<Double>>() {})
        .tagKey("t1")
        .customizer(b -> b.tag("another", "one"))
        .build();
    Set<Double> set = new HashSet<>();
    Gauge g = dg.getOrCreate(set, s -> s.stream().filter(d -> d > 10.0).count(), "gt10");
    Optional<Gauge> og = dg.get("gt10");
    assertThat(og).isPresent().contains(g);
  }

  @Test
  void getGaugeNotExist() {
    DynamicGauge<Set<Double>> dg = DynamicGauge.builder(smr, "hello", new OfType<Set<Double>>() {})
        .tagKey("t1")
        .customizer(b -> b.tag("another", "one"))
        .build();
    Set<Double> set = new HashSet<>();
    Optional<Gauge> og = dg.get("gt10");
    assertThat(og).isEmpty();
  }

  private boolean between(@NotNull String s, int min, int max) {
    return s.length() >= min && s.length() <= max;
  }

}