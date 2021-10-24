package com.github.andrpash.minidi.simple.testclasses.circular;

import com.github.andrpash.minidi.MiniDI;

public class Child {

    @MiniDI.Inject
    private ChildWithRefToRoot child;
}
