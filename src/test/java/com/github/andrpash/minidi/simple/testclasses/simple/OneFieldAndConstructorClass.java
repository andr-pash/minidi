package com.github.andrpash.minidi.simple.testclasses.simple;

import com.github.andrpash.minidi.MiniDI;

public class OneFieldAndConstructorClass {

    @MiniDI.Inject
    private NoDependencyClass child1;
    private final NoDependencyClass child2;

    @MiniDI.Inject
    public OneFieldAndConstructorClass(final NoDependencyClass child2) {
        this.child2 = child2;
    }

    public NoDependencyClass getChild1() {
        return this.child1;
    }

    public NoDependencyClass getChild2() {
        return this.child2;
    }
}
