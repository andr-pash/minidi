package com.github.andrpash.minidi.examples.factory;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.github.andrpash.minidi.MiniDI;
import com.github.andrpash.minidi.MiniDI.BindingScope;
import com.github.andrpash.minidi.examples.factory.testclasses.DummyPaymentService;
import com.github.andrpash.minidi.examples.factory.testclasses.PaymentService;
import com.github.andrpash.minidi.examples.factory.testclasses.PaymentServiceConfig;
import com.github.andrpash.minidi.examples.factory.testclasses.PaymentServiceProvider;
import com.github.andrpash.minidi.examples.factory.testclasses.RealPaymentService;
import org.junit.Test;

public class ProviderExampleTest {

    @Test
    public void factoryExample() {
        final PaymentServiceConfig config = new PaymentServiceConfig();
        config.setConfigured(true);

        /*
         * First, let's configure our container with a enabled config.
         *
         * Note: We define the scope of our PaymentService to be transient, so we always create a new instance.
         * 		This allows us to react to changes of the configuration at runtime
         */
        final MiniDI.Injector container = MiniDI.create()
                .bind(PaymentService.class).withScope(BindingScope.TRANSIENT)
                .toProvider(PaymentServiceProvider.class)
                .bind(PaymentServiceConfig.class).toInstance(config)
                .initialize();

        final PaymentService realPaymentService = container.get(PaymentService.class);

        assertThat(realPaymentService).isInstanceOf(RealPaymentService.class);

        /* Now let's disable the payment config and get a new instance of the payment service */
        config.setConfigured(false);

        final PaymentService dummyPaymentService = container.get(PaymentService.class);

        assertThat(dummyPaymentService).isInstanceOf(DummyPaymentService.class);
    }
}
