package com.github.andrpash.minidi.factory.testclasses.simple;

import com.github.andrpash.minidi.MiniDI;

public class ConstructorInjectionFactory implements MiniDI.Factory<OneDependencyClass>
{
	private final NoDependenciesClass injectedInstance;

	@MiniDI.Inject
	public ConstructorInjectionFactory( final NoDependenciesClass injectedInstance )
	{
		this.injectedInstance = injectedInstance;
	}

	@Override
	public OneDependencyClass create( )
	{
		return new OneDependencyClass( this.injectedInstance );
	}
}
