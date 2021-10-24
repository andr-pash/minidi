package com.github.andrpash.minidi.examples.factory.testclasses;

/* Instances of this class represent some kind of configuration you may have in your system */
public class PaymentServiceConfig {

    private boolean isConfigured = false;

    public boolean isConfigured() {
        return this.isConfigured;
    }

    public void setConfigured(final boolean configured) {
        this.isConfigured = configured;
    }
}
