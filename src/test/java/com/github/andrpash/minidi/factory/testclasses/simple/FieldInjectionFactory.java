package com.github.andrpash.minidi.factory.testclasses.simple;

import com.github.andrpash.minidi.MiniDI;

public class FieldInjectionFactory implements MiniDI.Factory<OneDependencyClass>
{
	@MiniDI.Inject
	NoDependenciesClass injectedInstance;

	@Override
	public OneDependencyClass create( )
	{
		return new OneDependencyClass( this.injectedInstance );
	}
}
