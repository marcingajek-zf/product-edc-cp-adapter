package net.catenax.edc.tests.data;

import lombok.NonNull;
import lombok.Value;

@Value
public class Asset {
  @NonNull private String Id;

  @NonNull private String description;
}
