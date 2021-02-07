package com.github.andrpash.minidi.complex;

import com.github.andrpash.minidi.MiniDI;
import com.github.andrpash.minidi.complex.testclasses.Level1;
import com.github.andrpash.minidi.complex.testclasses.WithInjectorDependency;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class ComplexMiniDITest
{
	@Test
	public void reuseLeafDependencyOnMultipleNodes( )
	{
		final MiniDI.Injector container = MiniDI.create( )
			.bind( Level1.class ).toClass( Level1.class )
			.bind( Level1.Level2_1.class ).toClass( Level1.Level2_1.class )
			.bind( Level1.Level2_2.class ).toClass( Level1.Level2_2.class )
			.bind( Level1.Level3.class ).toClass( Level1.Level3.class )
			.initialize( );

		final Level1 instance = container.get( Level1.class );

		assertThat( instance ).isNotNull( );
	}

	@Test
	public void multiLevelContainer_childHasAccessToParent( )
	{
		final MiniDI.Injector rootContainer = MiniDI.create( )
			.bind( Level1.Level3.class ).toClass( Level1.Level3.class )
			.initialize( );

		final MiniDI.Injector childContainer = rootContainer.createChild( )
			.bind( Level1.Level2_1.class ).toClass( Level1.Level2_1.class )
			.bind( Level1.Level2_2.class ).toClass( Level1.Level2_2.class )
			.bind( Level1.class ).toClass( Level1.class )
			.initialize( );

		final Level1.Level3 level3 = childContainer.get( Level1.Level3.class );
		assertThat( level3 ).isNotNull( );
	}

	@Test( expected = MiniDI.MissingBindingException.class )
	public void multiLevelContainer_parentHasNoAccessToChild( )
	{
		final MiniDI.Injector rootContainer = MiniDI.create( )
			.bind( Level1.class ).toClass( Level1.class )
			.initialize( );

		final MiniDI.Injector childContainer = rootContainer.createChild( )
			.bind( Level1.Level2_1.class ).toClass( Level1.Level2_1.class )
			.bind( Level1.Level2_2.class ).toClass( Level1.Level2_2.class )
			.bind( Level1.Level3.class ).toClass( Level1.Level3.class )
			.initialize( );

		final Level1.Level2_1 level2 = rootContainer.get( Level1.Level2_1.class );
		assertThat( level2 ).isNotNull( );
	}

	@Test
	public void multiLevelContainer_rootLevelInstancesGetReused( )
	{
		/* Given: parent + child container, with leaf node binding defined in parent */
		final MiniDI.Injector rootContainer = MiniDI.create( )
			.bind( Level1.Level3.class ).withScope( MiniDI.BindingScope.SINGLETON ).toClass( Level1.Level3.class )
			.initialize( );

		final MiniDI.Injector childContainer = rootContainer.createChild( )
			.bind( Level1.Level2_1.class ).toClass( Level1.Level2_1.class )
			.bind( Level1.Level2_2.class ).toClass( Level1.Level2_2.class )
			.bind( Level1.class ).toClass( Level1.class )
			.initialize( );

		/*
		 * When: instance of leaf node is acquired from child and root
		 * NOTE: order is important here, child has to be called first otherwise root will already have an instance registered
		 */
		final Level1.Level3 childLevel1 = childContainer.get( Level1.Level3.class );
		final Level1.Level3 rootLevel1 = rootContainer.get( Level1.Level3.class );

		/* Then: both containers should return the same instance */
		assertThat( childLevel1 ).isNotNull( );
		assertThat( rootLevel1 ).isEqualTo( childLevel1 );
	}

	@Test
	public void multiLevelContainer_instancesGetReusedFromContainerThatDefinedBinding( )
	{
		/* Given: parent + child container, with leaf node binding defined in parent */
		final MiniDI.Injector rootContainer = MiniDI.create( )
			.bind( Level1.Level3.class ).withScope( MiniDI.BindingScope.SINGLETON ).toClass( Level1.Level3.class )
			.initialize( );

		final MiniDI.Injector childContainer1 = rootContainer.createChild( )
			.bind( Level1.Level2_1.class ).withScope( MiniDI.BindingScope.SINGLETON ).toClass( Level1.Level2_1.class )
			.bind( Level1.Level2_2.class ).withScope( MiniDI.BindingScope.SINGLETON ).toClass( Level1.Level2_2.class )
			.initialize( );

		final MiniDI.Injector childContainer2 = childContainer1.createChild( )
			.bind( Level1.class ).withScope( MiniDI.BindingScope.SINGLETON ).toClass( Level1.class )
			.initialize( );

		/*
		 * When: instance of leaf node is acquired from child and root
		 * NOTE: order is important here, child has to be called first otherwise root will already have an instance registered
		 */
		final Level1.Level3 childLevel1 = childContainer1.get( Level1.Level3.class );
		final Level1.Level3 rootLevel1 = rootContainer.get( Level1.Level3.class );

		final Level1.Level2_1 childLevel21 = childContainer2.get( Level1.Level2_1.class );
		final Level1.Level2_1 rootLevel21 = childContainer1.get( Level1.Level2_1.class );

		/* Then: both containers should return the same instance */
		assertThat( childLevel1 ).isNotNull( );
		assertThat( rootLevel1 ).isEqualTo( childLevel1 );

		assertThat( childLevel21 ).isEqualTo( rootLevel21 );
	}

	@Test
	public void injectInjector_canInjectInjectorAsDependency( )
	{
		final MiniDI.Injector injector = MiniDI.create( )
			.bind( WithInjectorDependency.class ).toClass( WithInjectorDependency.class )
			.initialize( );

		final WithInjectorDependency withInjectorDependency = injector.get( WithInjectorDependency.class );

		assertThat( withInjectorDependency.getInjector( ) ).isEqualTo( injector );
	}

	@Test
	public void multiLevelContainer_injectInjector_usedInjectorOfCorrectLevel( )
	{
		final MiniDI.Injector rootInjector = MiniDI.create( )
			.bind( Level1.Level3.class ).toClass( Level1.Level3.class )
			.initialize( );

		final MiniDI.Injector childInjector = rootInjector.createChild( )
			.bind( WithInjectorDependency.class ).toClass( WithInjectorDependency.class )
			.initialize( );

		final WithInjectorDependency withInjectorDependency = childInjector.get( WithInjectorDependency.class );

		assertThat( withInjectorDependency.getInjector( ) ).isEqualTo( childInjector );
	}

}
