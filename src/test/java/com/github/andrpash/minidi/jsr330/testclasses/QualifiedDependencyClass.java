package com.github.andrpash.minidi.jsr330.testclasses;

import javax.inject.Inject;

public class QualifiedDependencyClass {

  @QualifiedClass.Qualified1
  @Inject
  QualifiedClass qualified1;

  @QualifiedClass.Qualified2
  @Inject
  QualifiedClass qualified2;

  public QualifiedClass getQualified1() {
    return this.qualified1;
  }

  public QualifiedClass getQualified2() {
    return this.qualified2;
  }
}
