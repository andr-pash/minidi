package com.github.andrpash.minidi.simple.testclasses.circular;

import com.github.andrpash.minidi.MiniDI;

public class InjectConstructorRefToSelfClass {

    @MiniDI.Inject
    public InjectConstructorRefToSelfClass(final InjectConstructorRefToSelfClass child) {
    }
}
