package com.github.andrpash.minidi.qualifier.testclasses;

import com.github.andrpash.minidi.MiniDI;

public class QualifiedDependencyClass {

    @QualifiedClass.Qualified1
    @MiniDI.Inject
    QualifiedClass qualified1;

    @QualifiedClass.Qualified2
    @MiniDI.Inject
    QualifiedClass qualified2;

    public QualifiedClass getQualified1() {
        return this.qualified1;
    }

    public QualifiedClass getQualified2() {
        return this.qualified2;
    }
}
