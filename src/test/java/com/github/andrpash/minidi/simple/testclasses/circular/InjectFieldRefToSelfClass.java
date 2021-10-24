package com.github.andrpash.minidi.simple.testclasses.circular;

import com.github.andrpash.minidi.MiniDI;

public class InjectFieldRefToSelfClass {

    @MiniDI.Inject
    private InjectFieldRefToSelfClass child;
}
