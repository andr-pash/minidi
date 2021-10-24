package com.github.andrpash.minidi.jsr330;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.andrpash.minidi.MiniDI;
import com.github.andrpash.minidi.jsr330.testclasses.DummyDependency;
import com.github.andrpash.minidi.jsr330.testclasses.JsrAndMiniDIMixed;
import com.github.andrpash.minidi.jsr330.testclasses.NamedStringFieldDependencyClass;
import com.github.andrpash.minidi.jsr330.testclasses.NamedStringProvider;
import com.github.andrpash.minidi.jsr330.testclasses.OnlyConstructorInjection;
import com.github.andrpash.minidi.jsr330.testclasses.OnlyFieldInjection;
import com.github.andrpash.minidi.jsr330.testclasses.ProviderForDummyDependency;
import com.github.andrpash.minidi.jsr330.testclasses.QualifiedClass;
import com.github.andrpash.minidi.jsr330.testclasses.QualifiedDependencyClass;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.Test;

public class Jsr330Test {

  @Test
  public void fieldInjection() {
    final MiniDI.Injector injector = MiniDI.create()
        .bind(DummyDependency.class).toClass(DummyDependency.class)
        .bind(OnlyFieldInjection.class).toClass(OnlyFieldInjection.class)
        .initialize();

    final DummyDependency dummyDependency = injector.get(OnlyFieldInjection.class)
        .getDummyDependency();

    assertThat(dummyDependency).isNotNull();
  }

  @Test
  public void constructorInjection() {
    final MiniDI.Injector injector = MiniDI.create()
        .bind(DummyDependency.class).toClass(DummyDependency.class)
        .bind(OnlyConstructorInjection.class).toClass(OnlyConstructorInjection.class)
        .initialize();

    final DummyDependency dummyDependency = injector.get(OnlyConstructorInjection.class)
        .getDummyDependency();

    assertThat(dummyDependency).isNotNull();
  }

  @Test
  public void allowMixedAnnotations() {
    final MiniDI.Injector injector = MiniDI.create()
        .bind(DummyDependency.class).withScope(MiniDI.BindingScope.SINGLETON)
        .toClass(DummyDependency.class)
        .bind(JsrAndMiniDIMixed.class).toClass(JsrAndMiniDIMixed.class)
        .initialize();

    final DummyDependency dummyDependency1 = injector.get(JsrAndMiniDIMixed.class)
        .getDummyDependency1();
    final DummyDependency dummyDependency2 = injector.get(JsrAndMiniDIMixed.class)
        .getDummyDependency2();

    assertThat(dummyDependency1).isEqualTo(dummyDependency2);
  }

  @Test
  public void respectsSingletonAnnotation() {
    final MiniDI.Injector injector = MiniDI.create()
        .bind(DummyDependency.class).toClass(DummyDependency.class)
        .bind(JsrAndMiniDIMixed.class).toClass(JsrAndMiniDIMixed.class)
        .initialize();

    final DummyDependency dummyDependency1 = injector.get(JsrAndMiniDIMixed.class)
        .getDummyDependency1();
    final DummyDependency dummyDependency2 = injector.get(JsrAndMiniDIMixed.class)
        .getDummyDependency2();

    assertThat(dummyDependency1).isEqualTo(dummyDependency2);
  }

  @Test
  public void canUseJsr330ProviderClass() {
    final MiniDI.Injector injector = MiniDI.create()
        .bind(DummyDependency.class).toProvider(ProviderForDummyDependency.class)
        .bind(OnlyConstructorInjection.class).toClass(OnlyConstructorInjection.class)
        .initialize();

    final DummyDependency dummyDependency = injector.get(OnlyConstructorInjection.class)
        .getDummyDependency();

    assertThat(dummyDependency).isNotNull();
  }

  @Test
  public void respectsNamedAnnotation() {
    final MiniDI.Injector injector = MiniDI.create()
        .bind(String.class).toProvider(NamedStringProvider.class)
        .bind(NamedStringFieldDependencyClass.class)
        .toClass(NamedStringFieldDependencyClass.class)
        .initialize();

    NamedStringFieldDependencyClass instance = injector.get(
        NamedStringFieldDependencyClass.class);

    assertThat(instance.getStringField()).isEqualTo("test");
  }

  @Test
  public void respectsQualifierAnnotation() {
    final MiniDI.Injector injector = MiniDI.create()
        .bind(QualifiedClass.class).toClass(QualifiedClass.QualifiedClass1.class)
        .bind(QualifiedClass.class).toClass(QualifiedClass.QualifiedClass2.class)
        .bind(QualifiedDependencyClass.class).toClass(QualifiedDependencyClass.class)
        .initialize();

    final QualifiedDependencyClass dependencyClass = injector.get(
        QualifiedDependencyClass.class);

    AssertionsForClassTypes.assertThat(dependencyClass.getQualified1())
        .isExactlyInstanceOf(QualifiedClass.QualifiedClass1.class);
    AssertionsForClassTypes.assertThat(dependencyClass.getQualified2())
        .isExactlyInstanceOf(QualifiedClass.QualifiedClass2.class);
  }
}
