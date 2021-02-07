package com.github.andrpash.minidi.provider.testclasses.simple;

import com.github.andrpash.minidi.MiniDI;

public class NoDependenciesProvider implements MiniDI.Provider<NoDependenciesClass>
{
	@Override public NoDependenciesClass get( )
	{
		return new NoDependenciesClass( );
	}
}
