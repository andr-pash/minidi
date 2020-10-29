package com.github.andrpash.minidi.simple.testclasses.simple;

import com.github.andrpash.minidi.MiniDI;

public class OneFieldDependencyClass
{
	@MiniDI.Inject
	private NoDependencyClass firstInstance;

	@MiniDI.Inject
	private NoDependencyClass secondInstance;

	public NoDependencyClass getFirstInstance( )
	{
		return this.firstInstance;
	}

	public NoDependencyClass getSecondInstance( )
	{
		return this.secondInstance;
	}
}
