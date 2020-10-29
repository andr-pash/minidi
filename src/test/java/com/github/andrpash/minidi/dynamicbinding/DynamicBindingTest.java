package com.github.andrpash.minidi.dynamicbinding;

import com.github.andrpash.minidi.MiniDI;
import com.github.andrpash.minidi.dynamicbinding.testclasses.DynamicRootClass;
import com.github.andrpash.minidi.dynamicbinding.testclasses.DynamicRootWithMissingDependency;
import com.github.andrpash.minidi.dynamicbinding.testclasses.LeafClass;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class DynamicBindingTest
{
	@Test
	public void test_allowInitIfMissingBindingIsMarkedAsDynamic( )
	{
		MiniDI.create( )
			.bind( LeafClass.class ).toClass( LeafClass.class )
			.dynamic( DynamicRootClass.class )
			.initialize( );
	}

	@Test( expected = MiniDI.InstantiationException.class )
	public void test_dynamicBindingDeclaredButNotProvided_throws( )
	{
		final MiniDI.Injector injector = MiniDI.create( )
			.bind( LeafClass.class ).toClass( LeafClass.class )
			.dynamic( DynamicRootClass.class )
			.initialize( );

		injector.get( LeafClass.class );
	}

	@Test
	public void test_dynamicBindingDeclaredAndProvided( )
	{
		final MiniDI.Injector injector = MiniDI.create( )
			.bind( LeafClass.class ).toClass( LeafClass.class )
			.dynamic( DynamicRootClass.class )
			.initialize( );

		final DynamicRootClass dynamicRootClassInstance = new DynamicRootClass( );
		injector.bindDynamic( DynamicRootClass.class ).toInstance( dynamicRootClassInstance );

		injector.get( LeafClass.class );
	}

	@Test( expected = MiniDI.MissingBindingException.class )
	public void test_dynamicBindingDeclaredWithMissingDependency_throws( )
	{
		final MiniDI.Injector injector = MiniDI.create( )
			.bind( LeafClass.class ).toClass( LeafClass.class )
			.dynamic( DynamicRootClass.class )
			.initialize( );

		injector.bindDynamic( DynamicRootClass.class ).toClass( DynamicRootWithMissingDependency.class );
	}

	@Test
	public void test_dynamicBindingResolvedFromParentUntilPresentInChildInjector( )
	{
		final DynamicRootClass rootInstance = new DynamicRootClass( );
		final DynamicRootClass childInstance = new DynamicRootClass( );

		final MiniDI.Injector childInjector = MiniDI.create( )
			.bind( DynamicRootClass.class ).toInstance( rootInstance )
			.initialize( )
			.createChild( )
			.bind( LeafClass.class ).toClass( LeafClass.class )
			.dynamic( DynamicRootClass.class )
			.initialize( );

		final DynamicRootClass instanceFromInjectorBeforeProviding = childInjector.get( DynamicRootClass.class );

		childInjector.bindDynamic( DynamicRootClass.class ).toInstance( childInstance );
		final DynamicRootClass instanceFromInjectorAfterProviding = childInjector.get( DynamicRootClass.class );

		assertThat( instanceFromInjectorBeforeProviding ).isEqualTo( rootInstance );
		assertThat( instanceFromInjectorAfterProviding ).isEqualTo( childInstance );
	}
}
