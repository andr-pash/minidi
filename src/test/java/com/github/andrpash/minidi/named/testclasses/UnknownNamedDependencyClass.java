package com.github.andrpash.minidi.named.testclasses;

import com.github.andrpash.minidi.MiniDI;

public class UnknownNamedDependencyClass
{
	@MiniDI.Inject
	@MiniDI.Named( "unknown" )
	private NamedConcretion named;
}
