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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
 * A dynamic meter, dynamically registers underlying meters with requested tag values.
 *
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
   * Registers the meter in the {@link MeterRegistry}.
   */
  Function<E, T> registrar;

  /**
   * The keys for the tags that this meter will produce, values are dynamic.
   */
  String[] tagKeys;

  ConcurrentMap<String, T> meters;

  /**
   * Construct a new DynamicMeter.
   *
   * @param registry        To register the underlying meters.
   * @param name            Meter name, all underlying meters will share that name.
   * @param newInnerBuilder A function to construct the underlying meter builder.
   * @param tagger          A function to dynamically add the tags.
   * @param customizers     For any additional customization to the underlying meter.
   * @param registrar       Function to register the underlying meters.
   * @param tagKeys         The keys that this meter will have, and allow their values to be added
   *                        dynamically.
   */
  protected DynamicMeter(
      @NotNull MeterRegistry registry,
      @NotNull String name,
      @NotNull BiFunction<String, R, E> newInnerBuilder,
      @NotNull BiFunction<E, Collection<Tag>, E> tagger,
      @Nullable Collection<UnaryOperator<E>> customizers,
      @NotNull Function<E, T> registrar,
      String @NotNull... tagKeys) {
    this.registry = registry;
    this.name = name;
    this.newInnerBuilder = newInnerBuilder;
    this.tagger = tagger;
    this.customizer = reduceCustomizers(customizers);
    this.registrar = registrar;
    this.tagKeys = tagKeys;
    this.meters = new ConcurrentHashMap<>();
  }

  protected T getOrCreate(R params, String @NotNull... tagValues) {
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

  /**
   * Get an existing underlying meter if one is already registered with the given tag values.
   *
   * @param tagValues The tag values to check if a meter exists for.
   * @return the meter, if one was found.
   */
  protected Optional<T> get(String @NotNull... tagValues) {
    validate(tagValues);
    return Optional.ofNullable(meters.get(key(tagValues)));
  }

  private void validate(String @NotNull... tagValues) {
    if (tagValues.length != tagKeys.length) {
      throw new IllegalArgumentException(
          String.format("Expected '%d' values, got '%d'. Keys: '%s'",
              tagKeys.length, tagValues.length, Arrays.toString(tagKeys)));
    }
  }

  private @NotNull String key(String... tagValues) {
    return Arrays.toString(tagValues);
  }

  private @Nullable UnaryOperator<E> reduceCustomizers(
      @Nullable Collection<UnaryOperator<E>> customizers) {
    return customizers == null || customizers.isEmpty()
        ? null
        : customizers.stream().reduce((l, r) -> b -> l.andThen(r).apply(b)).orElse(null);
  }

}
