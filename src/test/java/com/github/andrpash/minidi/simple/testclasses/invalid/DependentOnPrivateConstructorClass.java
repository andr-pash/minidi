package com.github.andrpash.minidi.simple.testclasses.invalid;

import com.github.andrpash.minidi.MiniDI;

public class DependentOnPrivateConstructorClass {

    @MiniDI.Inject
    private PrivateConstructorClass child;
}
