package com.avpines.dynamic.meters;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A dynamic meter that its underlying meters don't need any additional parameters in construction.
 *
 * @param <T> The meter type.
 * @param <E> The builder for the meter [T].
 */
public class ParameterlessDynamicMeter<T extends Meter, E> extends DynamicMeter<T, E, VoidParams> {

  /**
   * Construct a new ParameterlessDynamicMeter.
   *
   * @param registry        To register the underlying meters.
   * @param name            Meter name, all underlying meters will share that name.
   * @param newInnerBuilder A function to construct the underlying meter builder.
   * @param tagger          A function to dynamically add the tags.
   * @param customizer      For any additional customization to the underlying meter.
   * @param registrar       Function to register the underlying meters.
   * @param tagKeys         The keys that this meter will have, and allow their values to be added
   *                        dynamically.
   */
  protected ParameterlessDynamicMeter(
      @NotNull MeterRegistry registry,
      @NotNull String name,
      @NotNull Function<String, E> newInnerBuilder,
      @NotNull BiFunction<E, Collection<Tag>, E> tagger,
      @Nullable UnaryOperator<E> customizer,
      @NotNull Function<E, T> registrar,
      String @NotNull... tagKeys) {
    super(
        registry, name, (s, p) -> newInnerBuilder.apply(s), tagger, customizer, registrar, tagKeys
    );
  }

  /**
   * Gets the requested meter with the given tag values. If such a meter did not previously exist, a
   * new one will be registered and returned.
   *
   * @param tagValues the tag values, should correspond to the given tag keys when this dynamic
   *                  meter was constructed.
   * @return A new underlying meter.
   */
  public T getOrCreate(String @NotNull... tagValues) {
    return super.getOrCreate(null, tagValues);
  }

}
