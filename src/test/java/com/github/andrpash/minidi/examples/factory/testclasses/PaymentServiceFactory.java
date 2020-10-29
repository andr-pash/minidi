package com.github.andrpash.minidi.examples.factory.testclasses;

import com.github.andrpash.minidi.MiniDI;

public class PaymentServiceFactory implements MiniDI.Factory<PaymentService>
{
	private final PaymentServiceConfig config;

	@MiniDI.Inject
	public PaymentServiceFactory( final PaymentServiceConfig config )
	{
		this.config = config;
	}

	@Override
	public PaymentService create( )
	{
		return this.config.isConfigured( ) ? new RealPaymentService( ) : new DummyPaymentService( );
	}
}
