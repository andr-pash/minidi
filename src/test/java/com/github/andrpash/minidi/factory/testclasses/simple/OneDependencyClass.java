package com.github.andrpash.minidi.factory.testclasses.simple;

import com.github.andrpash.minidi.MiniDI;

public class OneDependencyClass
{
	private final NoDependenciesClass dependency;

	@MiniDI.Inject
	public OneDependencyClass( final NoDependenciesClass dependency )
	{
		this.dependency = dependency;
	}
}
