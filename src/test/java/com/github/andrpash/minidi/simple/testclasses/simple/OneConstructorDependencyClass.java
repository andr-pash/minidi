package com.github.andrpash.minidi.simple.testclasses.simple;

import com.github.andrpash.minidi.MiniDI;

public class OneConstructorDependencyClass {

    private final NoDependencyClass child;

    @MiniDI.Inject
    public OneConstructorDependencyClass(final NoDependencyClass child) {
        this.child = child;
    }

    public NoDependencyClass getChild() {
        return this.child;
    }
}
