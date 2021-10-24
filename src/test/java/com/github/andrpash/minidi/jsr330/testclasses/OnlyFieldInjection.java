package com.github.andrpash.minidi.jsr330.testclasses;

import javax.inject.Inject;

public class OnlyFieldInjection {

    @Inject
    private DummyDependency dummyDependency;

    public DummyDependency getDummyDependency() {
        return this.dummyDependency;
    }
}
