package net.catenax.edc.tests.data;

import lombok.NonNull;
import lombok.Value;

@Value
public class Permission {
  @NonNull String action;
  String target;
}
