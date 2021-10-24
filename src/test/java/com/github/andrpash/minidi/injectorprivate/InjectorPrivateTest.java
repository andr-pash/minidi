package com.github.andrpash.minidi.injectorprivate;

import com.github.andrpash.minidi.MiniDI;
import com.github.andrpash.minidi.injectorprivate.testclasses.LeafClass;
import com.github.andrpash.minidi.injectorprivate.testclasses.RootClass;
import org.junit.Test;

public class InjectorPrivateTest {

    @Test(expected = MiniDI.MissingBindingException.class)
    public void test_injectorPrivateMarkedBindingsCannotBeResolvedByChildInjectors() {
        MiniDI.create()
                .bind(RootClass.class).toClass(RootClass.class)
                .injectorPrivate(RootClass.class)
                .initialize()
                .createChild()
                .bind(LeafClass.class).toClass(LeafClass.class)
                .initialize();
    }

    @Test
    public void test_injectorsPassthroughBindingRequestIfTheyHaveThemDefinedAsPrivate() {
        MiniDI.create()
                .bind(RootClass.class).toClass(RootClass.class)
                .initialize()
                .createChild()
                .bind(RootClass.class).toClass(RootClass.class)
                .injectorPrivate(RootClass.class)
                .initialize()
                .createChild()
                .bind(LeafClass.class).toClass(LeafClass.class)
                .initialize();
    }

    @Test
    public void test_injectorsCanResolveOwnPrivates() {
        MiniDI.create()
                .bind(RootClass.class).toClass(RootClass.class)
                .bind(LeafClass.class).toClass(LeafClass.class)
                .injectorPrivate(RootClass.class)
                .initialize();
    }
}
