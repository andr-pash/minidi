package com.github.andrpash.minidi.jsr330;

import com.github.andrpash.minidi.MiniDI;
import com.github.andrpash.minidi.jsr330.testclasses.DummyDependency;
import com.github.andrpash.minidi.jsr330.testclasses.JsrAndMiniDIMixed;
import com.github.andrpash.minidi.jsr330.testclasses.OnlyConstructorInjection;
import com.github.andrpash.minidi.jsr330.testclasses.OnlyFieldInjection;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Jsr330Test
{
	@Test
	public void fieldInjection( )
	{
		final MiniDI.Injector injector = MiniDI.create( )
			.bind( DummyDependency.class ).toClass( DummyDependency.class )
			.bind( OnlyFieldInjection.class ).toClass( OnlyFieldInjection.class )
			.initialize( );

		final DummyDependency dummyDependency = injector.get( OnlyFieldInjection.class ).getDummyDependency( );

		assertThat( dummyDependency ).isNotNull( );
	}

	@Test
	public void constructorInjection( )
	{
		final MiniDI.Injector injector = MiniDI.create( )
			.bind( DummyDependency.class ).toClass( DummyDependency.class )
			.bind( OnlyConstructorInjection.class ).toClass( OnlyConstructorInjection.class )
			.initialize( );

		final DummyDependency dummyDependency = injector.get( OnlyConstructorInjection.class ).getDummyDependency( );

		assertThat( dummyDependency ).isNotNull( );
	}

	@Test
	public void allowMixedAnnotations( )
	{
		final MiniDI.Injector injector = MiniDI.create( )
			.bind( DummyDependency.class ).withScope( MiniDI.BindingScope.SINGLETON ).toClass( DummyDependency.class )
			.bind( JsrAndMiniDIMixed.class ).toClass( JsrAndMiniDIMixed.class )
			.initialize( );

		final DummyDependency dummyDependency1 = injector.get( JsrAndMiniDIMixed.class ).getDummyDependency1( );
		final DummyDependency dummyDependency2 = injector.get( JsrAndMiniDIMixed.class ).getDummyDependency2( );

		assertThat( dummyDependency1 ).isEqualTo( dummyDependency2 );
	}
}
