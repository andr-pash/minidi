package com.github.andrpash.minidi.jsr330.testclasses;

import com.github.andrpash.minidi.MiniDI;
import javax.inject.Inject;

public class JsrAndMiniDIMixed {

    @MiniDI.Inject
    private DummyDependency dummyDependency1;

    @Inject
    private DummyDependency dummyDependency2;

    public DummyDependency getDummyDependency1() {
        return this.dummyDependency1;
    }

    public DummyDependency getDummyDependency2() {
        return this.dummyDependency2;
    }
}
