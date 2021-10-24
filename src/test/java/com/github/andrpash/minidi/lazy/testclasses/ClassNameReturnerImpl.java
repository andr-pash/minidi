package com.github.andrpash.minidi.lazy.testclasses;

public class ClassNameReturnerImpl implements ClassNameReturner {

    @Override
    public String returnClassName() {
        return getClass().getSimpleName();
    }

    @Override
    public ClassNameReturner returnSelf() {
        return this;
    }
}
