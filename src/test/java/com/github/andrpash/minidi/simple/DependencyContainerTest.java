package com.github.andrpash.minidi.simple;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.github.andrpash.minidi.MiniDI;
import com.github.andrpash.minidi.simple.testclasses.circular.Child;
import com.github.andrpash.minidi.simple.testclasses.circular.ChildWithRefToRoot;
import com.github.andrpash.minidi.simple.testclasses.circular.InjectConstructorRefToSelfClass;
import com.github.andrpash.minidi.simple.testclasses.circular.InjectFieldRefToSelfClass;
import com.github.andrpash.minidi.simple.testclasses.circular.Root;
import com.github.andrpash.minidi.simple.testclasses.invalid.DependentOnPrivateConstructorClass;
import com.github.andrpash.minidi.simple.testclasses.invalid.PrivateConstructorClass;
import com.github.andrpash.minidi.simple.testclasses.invalid.ThrowingConstructorClass;
import com.github.andrpash.minidi.simple.testclasses.simple.NoDependencyClass;
import com.github.andrpash.minidi.simple.testclasses.simple.OneConstructorDependencyClass;
import com.github.andrpash.minidi.simple.testclasses.simple.OneFieldAndConstructorClass;
import com.github.andrpash.minidi.simple.testclasses.simple.OneFieldDependencyClass;
import com.github.andrpash.minidi.simple.testclasses.simple.SingletonNoDependencyClass;
import com.github.andrpash.minidi.simple.testclasses.subtype.SuperType;
import com.github.andrpash.minidi.simple.testclasses.subtype.SuperTypeImpl;
import com.github.andrpash.minidi.simple.testclasses.subtype.SuperTypeImplDependency;
import org.junit.Test;

public class DependencyContainerTest {

    @Test
    public void test_creates() {
        MiniDI.create();
    }

    @Test
    public void test_fieldInjection_simpleDependency() {
        final MiniDI.Injector container = MiniDI.create()
                .bind(NoDependencyClass.class).toClass(NoDependencyClass.class)
                .bind(OneFieldDependencyClass.class).toClass(OneFieldDependencyClass.class)
                .initialize();

        final OneFieldDependencyClass class1 = container.get(OneFieldDependencyClass.class);

        assertThat(class1).isNotNull();
        assertThat(class1.getFirstInstance()).isNotNull();
    }

    @Test
    public void test_constructorInjection_simpleDependency() {
        final MiniDI.Injector container = MiniDI.create()
                .bind(NoDependencyClass.class).toClass(NoDependencyClass.class)
                .bind(OneConstructorDependencyClass.class)
                .toClass(OneConstructorDependencyClass.class)
                .initialize();

        final OneConstructorDependencyClass class1 = container.get(
                OneConstructorDependencyClass.class);

        assertThat(class1).isNotNull();
        assertThat(class1.getChild()).isNotNull();
    }

    @Test
    public void test_supportsConstructorAndFieldInjectionSimultaneously_simpleDependency() {
        final MiniDI.Injector container = MiniDI.create()
                .bind(NoDependencyClass.class).toClass(NoDependencyClass.class)
                .bind(OneFieldAndConstructorClass.class).toClass(OneFieldAndConstructorClass.class)
                .initialize();

        final OneFieldAndConstructorClass class1 = container.get(OneFieldAndConstructorClass.class);

        assertThat(class1).isNotNull();
        assertThat(class1.getChild1()).isNotNull();
        assertThat(class1.getChild2()).isNotNull();
    }

    @Test
    public void test_instancesAreReused() {
        final MiniDI.Injector container = MiniDI.create()
                .bind(NoDependencyClass.class)
                .withScope(MiniDI.BindingScope.SINGLETON)
                .toClass(NoDependencyClass.class)
                .bind(OneFieldDependencyClass.class).toClass(OneFieldDependencyClass.class)
                .initialize();

        final OneFieldDependencyClass class1 = container.get(OneFieldDependencyClass.class);

        assertThat(class1.getFirstInstance()).isEqualTo(class1.getSecondInstance());
    }

    @Test(expected = MiniDI.CircularDependencyException.class)
    public void test_circularDependency_fails() {
        final MiniDI.Injector container = MiniDI.create()
                .bind(Root.class).toClass(Root.class)
                .bind(Child.class).toClass(Child.class)
                .bind(ChildWithRefToRoot.class).toClass(ChildWithRefToRoot.class)
                .initialize();

        container.get(Root.class);
    }

    @Test
    public void test_instanceBinding_pass() {
        final NoDependencyClass noDependencyClassInstance = new NoDependencyClass();
        final MiniDI.Injector container = MiniDI.create()
                .bind(NoDependencyClass.class).toInstance(noDependencyClassInstance)
                .bind(OneFieldDependencyClass.class).toClass(OneFieldDependencyClass.class)
                .initialize();

        final OneFieldDependencyClass oneDependencyClassInstance = container.get(
                OneFieldDependencyClass.class);
        final NoDependencyClass noDependencyClassInstanceFromContainer = container.get(
                NoDependencyClass.class);

        assertThat(oneDependencyClassInstance).isNotNull();
        assertThat(noDependencyClassInstanceFromContainer).isEqualTo(noDependencyClassInstance);
        assertThat(oneDependencyClassInstance.getFirstInstance()).isNotNull();
        assertThat(oneDependencyClassInstance.getFirstInstance()).isEqualTo(
                noDependencyClassInstance);
    }

    @Test
    public void test_registerSubType_pass() {
        final MiniDI.Injector container = MiniDI.create()
                .bind(SuperType.class).toClass(SuperTypeImpl.class)
                .bind(SuperTypeImplDependency.class).toClass(SuperTypeImplDependency.class)
                .initialize();

        final SuperType instance = container.get(SuperType.class);

        assertThat(instance).isInstanceOf(SuperTypeImpl.class);
    }

    @Test
    public void test_transientScope_createsNewInstanceEveryTime() {
        final MiniDI.Injector container = MiniDI.create()
                .bind(NoDependencyClass.class)
                .withScope(MiniDI.BindingScope.TRANSIENT)
                .toClass(NoDependencyClass.class)
                .initialize();

        final NoDependencyClass instance1 = container.get(NoDependencyClass.class);
        final NoDependencyClass instance2 = container.get(NoDependencyClass.class);

        assertThat(instance1).isNotEqualTo(instance2);
    }

    @Test
    public void test_singletonScope_reusesInstances() {
        final MiniDI.Injector container = MiniDI.create()
                .bind(NoDependencyClass.class)
                .withScope(MiniDI.BindingScope.SINGLETON)
                .toClass(NoDependencyClass.class)
                .initialize();

        final NoDependencyClass instance1 = container.get(NoDependencyClass.class);
        final NoDependencyClass instance2 = container.get(NoDependencyClass.class);

        assertThat(instance1).isEqualTo(instance2);
    }

    @Test
    public void test_defaultsToTransientScope() {
        final MiniDI.Injector container = MiniDI.create()
                .bind(NoDependencyClass.class)
                .toClass(NoDependencyClass.class)
                .initialize();

        final NoDependencyClass instance1 = container.get(NoDependencyClass.class);
        final NoDependencyClass instance2 = container.get(NoDependencyClass.class);

        assertThat(instance1).isNotEqualTo(instance2);
    }

    @Test
    public void test_useSingletonScopeIfAnnotationPresent() {
        final MiniDI.Injector container = MiniDI.create()
                .bind(SingletonNoDependencyClass.class).toClass(SingletonNoDependencyClass.class)
                .initialize();

        final SingletonNoDependencyClass instance1 = container.get(
                SingletonNoDependencyClass.class);
        final SingletonNoDependencyClass instance2 = container.get(
                SingletonNoDependencyClass.class);

        assertThat(instance1).isEqualTo(instance2);
    }

    @Test
    public void test_useTransientScopeIfExplicitlyDefinedEvenIfAnnotatedAsSingleton() {
        final MiniDI.Injector container = MiniDI.create()
                .bind(SingletonNoDependencyClass.class)
                .withScope(MiniDI.BindingScope.TRANSIENT)
                .toClass(SingletonNoDependencyClass.class)
                .initialize();

        final SingletonNoDependencyClass instance1 = container.get(
                SingletonNoDependencyClass.class);
        final SingletonNoDependencyClass instance2 = container.get(
                SingletonNoDependencyClass.class);

        assertThat(instance1).isNotEqualTo(instance2);
    }

    @Test(expected = MiniDI.MissingConstructorException.class)
    public void test_noAccessibleConstructor_throws() {
        MiniDI.create()
                .bind(DependentOnPrivateConstructorClass.class)
                .toClass(DependentOnPrivateConstructorClass.class)
                .bind(PrivateConstructorClass.class).toClass(PrivateConstructorClass.class);
    }

    @Test(expected = MiniDI.CircularDependencyException.class)
    public void test_classTriesToInjectFieldReferenceToItself_throws() {
        MiniDI.create()
                .bind(InjectFieldRefToSelfClass.class).toClass(InjectFieldRefToSelfClass.class)
                .initialize();
    }

    @Test(expected = MiniDI.CircularDependencyException.class)
    public void test_classTriesToInjectConstructorReferenceToItself_throws() {
        MiniDI.create()
                .bind(InjectConstructorRefToSelfClass.class)
                .toClass(InjectConstructorRefToSelfClass.class)
                .initialize();
    }

    @Test
    public void test_failedInstantiation_throwsWrappedExpection() {
        final MiniDI.Injector container = MiniDI.create()
                .bind(ThrowingConstructorClass.class).toClass(ThrowingConstructorClass.class)
                .initialize();

        Exception caughtException = null;
        try {
            container.get(ThrowingConstructorClass.class);
        } catch (final Exception ex) {
            caughtException = ex;
        }

        assertThat(caughtException).isNotNull();
        assertThat(caughtException).isInstanceOf(MiniDI.InstantiationException.class);
        assertThat(caughtException)
                .hasRootCauseExactlyInstanceOf(
                        ThrowingConstructorClass.ThrowingConstructorClassException.class);
    }
}