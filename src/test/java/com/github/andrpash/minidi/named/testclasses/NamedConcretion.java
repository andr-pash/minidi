package com.github.andrpash.minidi.named.testclasses;

import com.github.andrpash.minidi.MiniDI;

abstract public class NamedConcretion
{
	@MiniDI.Named( "named1" )
	public static class NamedConcretion1 extends NamedConcretion
	{

	}

	@MiniDI.Named( "named2" )
	public static class NamedConcretion2 extends NamedConcretion
	{

	}
}
