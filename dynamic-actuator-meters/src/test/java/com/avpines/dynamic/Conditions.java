package com.avpines.dynamic;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import java.util.Arrays;
import java.util.List;
import lombok.EqualsAndHashCode;
import org.assertj.core.api.Condition;
import org.jetbrains.annotations.NotNull;

public class Conditions {

  public static MeterTagCondition onTags(Tag... tags) {
    return new MeterTagCondition(tags);
  }

  @EqualsAndHashCode(callSuper = true)
  static class MeterTagCondition extends Condition<Meter> {

    MeterTagCondition(Tag... tags) {
      super(
          meter -> {
            List<Tag> meterTags = meter.getId().getTags();
            return meterTags.size() == tags.length && meterTags.containsAll(Arrays.asList(tags));
          },
          String.format("has the exact tags: '%s'", Arrays.toString(tags))
      );
    }
  }

}
