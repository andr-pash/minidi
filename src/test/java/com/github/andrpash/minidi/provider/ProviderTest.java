package com.github.andrpash.minidi.provider;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.github.andrpash.minidi.MiniDI;
import com.github.andrpash.minidi.provider.testclasses.simple.ConstructorInjectionProvider;
import com.github.andrpash.minidi.provider.testclasses.simple.FieldInjectionProvider;
import com.github.andrpash.minidi.provider.testclasses.simple.NoDependenciesClass;
import com.github.andrpash.minidi.provider.testclasses.simple.NoDependenciesProvider;
import com.github.andrpash.minidi.provider.testclasses.simple.OneDependencyClass;
import org.junit.Test;

public class ProviderTest {

    @Test
    public void simpleFactory_noDependencies() {
        final MiniDI.Injector container = MiniDI.create()
                .bind(NoDependenciesClass.class).toProvider(NoDependenciesProvider.class)
                .initialize();

        final NoDependenciesClass instance = container.get(NoDependenciesClass.class);

        assertThat(instance).isNotNull();
    }

    @Test
    public void constructorInjection_pass() {
        final MiniDI.Injector container = MiniDI.create()
                .bind(OneDependencyClass.class).toProvider(ConstructorInjectionProvider.class)
                .bind(NoDependenciesClass.class).toClass(NoDependenciesClass.class)
                .initialize();

        final OneDependencyClass instance = container.get(OneDependencyClass.class);

        assertThat(instance).isNotNull();
    }

    @Test
    public void fieldInjection_pass() {
        final MiniDI.Injector container = MiniDI.create()
                .bind(OneDependencyClass.class).toProvider(FieldInjectionProvider.class)
                .bind(NoDependenciesClass.class).toClass(NoDependenciesClass.class)
                .initialize();

        final OneDependencyClass instance = container.get(OneDependencyClass.class);

        assertThat(instance).isNotNull();
    }
}
