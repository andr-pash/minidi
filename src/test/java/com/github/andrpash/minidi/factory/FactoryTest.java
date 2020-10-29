package com.github.andrpash.minidi.factory;

import com.github.andrpash.minidi.MiniDI;
import com.github.andrpash.minidi.factory.testclasses.simple.*;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class FactoryTest
{
	@Test
	public void simpleFactory_noDependencies( )
	{
		final MiniDI.Injector container = MiniDI.create( )
			.bind( NoDependenciesClass.class ).toFactory( NoDependenciesFactory.class )
			.initialize( );

		final NoDependenciesClass instance = container.get( NoDependenciesClass.class );

		assertThat( instance ).isNotNull( );
	}

	@Test
	public void constructorInjection_pass( )
	{
		final MiniDI.Injector container = MiniDI.create( )
			.bind( OneDependencyClass.class ).toFactory( ConstructorInjectionFactory.class )
			.bind( NoDependenciesClass.class ).toClass( NoDependenciesClass.class )
			.initialize( );

		final OneDependencyClass instance = container.get( OneDependencyClass.class );

		assertThat( instance ).isNotNull( );
	}

	@Test
	public void fieldInjection_pass( )
	{
		final MiniDI.Injector container = MiniDI.create( )
			.bind( OneDependencyClass.class ).toFactory( FieldInjectionFactory.class )
			.bind( NoDependenciesClass.class ).toClass( NoDependenciesClass.class )
			.initialize( );

		final OneDependencyClass instance = container.get( OneDependencyClass.class );

		assertThat( instance ).isNotNull( );
	}
}
