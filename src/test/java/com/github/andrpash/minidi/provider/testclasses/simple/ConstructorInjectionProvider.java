package com.github.andrpash.minidi.provider.testclasses.simple;

import com.github.andrpash.minidi.MiniDI;

public class ConstructorInjectionProvider implements MiniDI.Provider<OneDependencyClass> {

    private final NoDependenciesClass injectedInstance;

    @MiniDI.Inject
    public ConstructorInjectionProvider(final NoDependenciesClass injectedInstance) {
        this.injectedInstance = injectedInstance;
    }

    @Override
    public OneDependencyClass get() {
        return new OneDependencyClass(this.injectedInstance);
    }
}
