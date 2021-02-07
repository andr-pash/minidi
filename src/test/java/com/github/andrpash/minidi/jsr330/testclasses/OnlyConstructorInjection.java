package com.github.andrpash.minidi.jsr330.testclasses;

import javax.inject.Inject;

public class OnlyConstructorInjection
{
	private final DummyDependency dummyDependency;

	@Inject
	public OnlyConstructorInjection( final DummyDependency dummyDependency )
	{
		this.dummyDependency = dummyDependency;
	}

	public DummyDependency getDummyDependency( )
	{
		return this.dummyDependency;
	}
}
