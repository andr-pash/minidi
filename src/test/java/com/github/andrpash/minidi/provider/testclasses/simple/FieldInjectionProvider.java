package com.github.andrpash.minidi.provider.testclasses.simple;

import com.github.andrpash.minidi.MiniDI;

public class FieldInjectionProvider implements MiniDI.Provider<OneDependencyClass> {

    @MiniDI.Inject
    NoDependenciesClass injectedInstance;

    @Override
    public OneDependencyClass get() {
        return new OneDependencyClass(this.injectedInstance);
    }
}
