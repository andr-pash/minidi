package com.github.andrpash.minidi.provider.testclasses.simple;

import com.github.andrpash.minidi.MiniDI;

public class ProviderForSubType implements MiniDI.Provider<NoDependenciesClass> {

  public NoDependenciesClassSubType get() {
    return new NoDependenciesClassSubType();
  }
}
