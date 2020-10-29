package com.github.andrpash.minidi.lazy;

import com.github.andrpash.minidi.MiniDI;
import com.github.andrpash.minidi.lazy.testclasses.*;
import org.junit.Test;

import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThat;

public class LazyInitializationTest
{
	@Test( expected = MiniDI.InvalidLazyAnnotation.class )
	public void test_lazyAnnotationOnNonInterface_throws( )
	{
		MiniDI.create( )
			.bind( NoDependencyClass.class ).toClass( NoDependencyClass.class )
			.bind( InvalidLazyAnnotationClass.class ).toClass( InvalidLazyAnnotationClass.class );
	}

	@Test
	public void test_lazyFieldAnnotation_pass( )
	{
		final MiniDI.Injector container = MiniDI.create( )
			.bind( ClassNameReturner.class ).toClass( ClassNameReturnerImpl.class )
			.bind( SimpleValidLazyFieldClass.class ).toClass( SimpleValidLazyFieldClass.class )
			.initialize( );

		final SimpleValidLazyFieldClass instance = container.get( SimpleValidLazyFieldClass.class );

		assertThat( instance ).isNotNull( );
		assertThat( instance.getChild( ) ).isInstanceOf( Proxy.class );
		assertThat( instance.getChild( ).returnClassName( ) ).isEqualTo( ClassNameReturnerImpl.class.getSimpleName( ) );
	}

	@Test
	public void test_lazyConstructorParameterAnnotation_pass( )
	{
		final MiniDI.Injector container = MiniDI.create( )
			.bind( ClassNameReturner.class ).toClass( ClassNameReturnerImpl.class )
			.bind( SimpleValidLazyConstructorClass.class ).toClass( SimpleValidLazyConstructorClass.class )
			.initialize( );

		final SimpleValidLazyConstructorClass instance = container.get( SimpleValidLazyConstructorClass.class );

		assertThat( instance ).isNotNull( );
		assertThat( instance.getChild( ) ).isInstanceOf( Proxy.class );
		assertThat( instance.getChild( ).returnClassName( ) ).isEqualTo( ClassNameReturnerImpl.class.getSimpleName( ) );
	}

	@Test
	public void test_lazilyConstructedInstancesAreReusedSingleton_pass( )
	{
		/* Given: two classes that lazily require the same interface which is marked as scope singleton */
		final MiniDI.Injector container = MiniDI.create( )
			.bind( ClassNameReturner.class ).toClass( ClassNameReturnerImpl.class )
			.bind( SimpleValidLazyFieldClass.class ).toClass( SimpleValidLazyFieldClass.class )
			.bind( SimpleValidLazyConstructorClass.class ).toClass( SimpleValidLazyConstructorClass.class )
			.initialize( );

		/* When: getting getting instances for the two classes, without triggering the lazy init */
		final SimpleValidLazyConstructorClass instance1 = container.get( SimpleValidLazyConstructorClass.class );
		final SimpleValidLazyFieldClass instance2 = container.get( SimpleValidLazyFieldClass.class );

		/* Then: the created proxies should point to the same real instance, once it has been created */
		final ClassNameReturner child1 = instance1.getChild( );
		final ClassNameReturner child2 = instance2.getChild( );

		final ClassNameReturner realInstance1 = child1.returnSelf( );
		final ClassNameReturner realInstance2 = child2.returnSelf( );

		assertThat( realInstance1 ).isEqualTo( realInstance2 );
	}

	@Test
	public void test_lazilyConstructedInstancesAreNotReusedTransient_pass( )
	{
		/* Given: two classes that lazily require the same interface which is marked as scope singleton */
		final MiniDI.Injector container = MiniDI.create( )
			.bind( ClassNameReturner.class ).withScope( MiniDI.BindingScope.TRANSIENT )
			.toClass( ClassNameReturnerImpl.class )
			.bind( SimpleValidLazyFieldClass.class ).toClass( SimpleValidLazyFieldClass.class )
			.bind( SimpleValidLazyConstructorClass.class ).toClass( SimpleValidLazyConstructorClass.class )
			.initialize( );

		/* When: getting getting instances for the two classes, without triggering the lazy init */
		final SimpleValidLazyConstructorClass instance1 = container.get( SimpleValidLazyConstructorClass.class );
		final SimpleValidLazyFieldClass instance2 = container.get( SimpleValidLazyFieldClass.class );

		/* Then: the created proxies should point to the same real instance, once it has been created */
		final ClassNameReturner child1 = instance1.getChild( );
		final ClassNameReturner child2 = instance2.getChild( );

		final ClassNameReturner realInstance1 = child1.returnSelf( );
		final ClassNameReturner realInstance2 = child2.returnSelf( );

		assertThat( realInstance1 ).isNotEqualTo( realInstance2 );
	}
}
