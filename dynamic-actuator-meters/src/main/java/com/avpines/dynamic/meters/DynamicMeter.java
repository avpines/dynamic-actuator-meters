package com.avpines.dynamic.meters;

import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @param <T> The meter type.
 * @param <E> The builder for the meter [T].
 * @param <R> The additional arguments type ({@link MeterParams}).
 */
@Value
@NonFinal
@Getter(value = AccessLevel.NONE)
public abstract class DynamicMeter<T extends Meter, E, R extends MeterParams> {

  MeterRegistry registry;

  /**
   * The name of this meter.
   */
  String name;

  /**
   * Function to create a new builder of the meter handled by this class.
   */
  BiFunction<String, R, E> newInnerBuilder;

  /**
   * Function to add requested tags.
   */
  BiFunction<E, Collection<Tag>, E> tagger;

  /**
   * Customizes the meter, e.g., add description or percentiles.
   */
  UnaryOperator<E> customizer;

  /**
   * Registers the meter in the {@link MeterRegistry}
   */
  Function<E, T> registrar;

  /**
   * The keys for the tags that this meter will produce, values are dynamic.
   */
  String[] tagKeys;

  Map<String, T> meters;

  /**
   * Construct a new DynamicMeter.
   *
   * @param name       The name of the counter.
   * @param registry   Meter registry to register this counter with.
   * @param customizer Operators to adjust the counter to your liking. Will be run for each name.
   */
  protected DynamicMeter(
      @NotNull MeterRegistry registry,
      @NotNull String name,
      @NotNull BiFunction<String, R, E> newInnerBuilder,
      @NotNull BiFunction<E, Collection<Tag>, E> tagger,
      @Nullable UnaryOperator<E> customizer,
      @NotNull Function<E, T> registrar,
      String @NotNull ... tagKeys) {
    this.registry = registry;
    this.name = name;
    this.newInnerBuilder = newInnerBuilder;
    this.tagger = tagger;
    this.customizer = customizer;
    this.registrar = registrar;
    this.tagKeys = tagKeys;
    this.meters = new HashMap<>();
  }

  protected T getOrCreate(R params, String @NotNull ... tagValues) {
    validate(tagValues);
    var key = key(tagValues);
    return meters.computeIfAbsent(key, k -> {
      var builder = newInnerBuilder.apply(name, params);
      if (customizer != null) {
        builder = customizer.apply(builder);
      }
      List<Tag> tags = IntStream.range(0, this.tagKeys.length)
          .mapToObj(i -> new ImmutableTag(tagKeys[i], tagValues[i]))
          .collect(Collectors.toList());
      tagger.apply(builder, tags);
      return registrar.apply(builder);
    });
  }

  protected Optional<T> get(String @NotNull ... tagValues) {
    validate(tagValues);
    return Optional.ofNullable(meters.get(key(tagValues)));
  }

  private void validate(String @NotNull ... tagValues) {
    if (tagValues.length != tagKeys.length) {
      throw new IllegalArgumentException(
          String.format("Expected '%d' values, got '%d'. Keys: '%s'",
              tagKeys.length, tagValues.length, Arrays.toString(tagKeys)));
    }
  }

  private String key(String ... tagValues) {
    return Arrays.toString(tagValues);
  }

}
