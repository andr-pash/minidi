package com.github.andrpash.minidi.jsr330.testclasses;

import javax.inject.Inject;
import javax.inject.Named;

public class NamedStringFieldDependencyClass {

  @Inject
  @Named("string_field")
  private String stringField;

  public String getStringField() {
    return stringField;
  }
}
