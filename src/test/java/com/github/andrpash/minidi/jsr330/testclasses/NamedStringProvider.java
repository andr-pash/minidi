package com.github.andrpash.minidi.jsr330.testclasses;

import javax.inject.Named;
import javax.inject.Provider;

@Named("string_field")
public class NamedStringProvider implements Provider<String> {

  @Override
  public String get() {
    return "test";
  }
}
