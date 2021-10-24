package com.github.andrpash.minidi.examples.factory.testclasses;

import com.github.andrpash.minidi.MiniDI;

public class PaymentServiceProvider implements MiniDI.Provider<PaymentService> {

    private final PaymentServiceConfig config;

    @MiniDI.Inject
    public PaymentServiceProvider(final PaymentServiceConfig config) {
        this.config = config;
    }

    @Override
    public PaymentService get() {
        return this.config.isConfigured() ? new RealPaymentService() : new DummyPaymentService();
    }
}
