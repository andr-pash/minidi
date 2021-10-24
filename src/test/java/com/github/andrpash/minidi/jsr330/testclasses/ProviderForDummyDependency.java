package com.github.andrpash.minidi.jsr330.testclasses;

import javax.inject.Provider;

public class ProviderForDummyDependency implements Provider<DummyDependency> {

  @Override
  public DummyDependency get() {
    return new DummyDependency();
  }
}
