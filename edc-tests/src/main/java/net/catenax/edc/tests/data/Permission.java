package net.catenax.edc.tests.data;

import lombok.NonNull;
import lombok.Value;

@Value
public class Permission {
  @NonNull private String action;
  private String target;
}
