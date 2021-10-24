package com.github.andrpash.minidi.simple.testclasses.subtype;

import com.github.andrpash.minidi.MiniDI;

public class SuperTypeImpl extends SuperType {

    @MiniDI.Inject
    private SuperTypeImplDependency child;
}
