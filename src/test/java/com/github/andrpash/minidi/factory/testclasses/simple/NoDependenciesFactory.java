package com.github.andrpash.minidi.factory.testclasses.simple;

import com.github.andrpash.minidi.MiniDI;

public class NoDependenciesFactory implements MiniDI.Factory<NoDependenciesClass>
{
	@Override public NoDependenciesClass create( )
	{
		return new NoDependenciesClass( );
	}
}
