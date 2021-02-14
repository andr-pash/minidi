package com.github.andrpash.minidi.named.testclasses;

import com.github.andrpash.minidi.MiniDI;

public class NamedDependencyClass
{
	@MiniDI.Inject
	@MiniDI.Named( "named1" )
	private NamedConcretion named1;

	@MiniDI.Inject
	@MiniDI.Named( "named2" )
	private NamedConcretion named2;

	public NamedConcretion getNamed1( )
	{
		return this.named1;
	}

	public NamedConcretion getNamed2( )
	{
		return this.named2;
	}
}
