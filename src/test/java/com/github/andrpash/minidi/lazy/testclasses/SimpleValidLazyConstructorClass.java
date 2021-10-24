package com.github.andrpash.minidi.lazy.testclasses;

import com.github.andrpash.minidi.MiniDI;

public class SimpleValidLazyConstructorClass {

    ClassNameReturner child;

    @MiniDI.Inject
    public SimpleValidLazyConstructorClass(@MiniDI.Lazy final ClassNameReturner child) {
        this.child = child;
    }

    public ClassNameReturner getChild() {
        return this.child;
    }
}
