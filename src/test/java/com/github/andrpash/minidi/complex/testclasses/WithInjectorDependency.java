package com.github.andrpash.minidi.complex.testclasses;

import com.github.andrpash.minidi.MiniDI;

public class WithInjectorDependency
{
	@MiniDI.Inject
	private MiniDI.Injector injector;

	public MiniDI.Injector getInjector( )
	{
		return this.injector;
	}
}
