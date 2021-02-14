package com.github.andrpash.minidi.named;

import com.github.andrpash.minidi.MiniDI;
import com.github.andrpash.minidi.named.testclasses.*;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class NamedAnnotationTest
{
	@Test
	public void namedField_resolvedCorrectDependencies( )
	{
		final MiniDI.Injector injector = MiniDI.create( )
			.bind( NamedConcretion.class ).toClass( NamedConcretion.NamedConcretion1.class )
			.bind( NamedConcretion.class ).toClass( NamedConcretion.NamedConcretion2.class )
			.bind( NamedDependencyClass.class ).toClass( NamedDependencyClass.class )
			.initialize( );

		final NamedDependencyClass namedDependencyClass = injector.get( NamedDependencyClass.class );

		assertThat( namedDependencyClass.getNamed1( ) ).isExactlyInstanceOf( NamedConcretion.NamedConcretion1.class );
		assertThat( namedDependencyClass.getNamed2( ) ).isExactlyInstanceOf( NamedConcretion.NamedConcretion2.class );
	}

	@Test
	public void missingNamedBinding_throwsMissingBindingException( )
	{
		final MiniDI.InjectorBuilder builder = MiniDI.create( )
			.bind( NamedConcretion.class ).toClass( NamedConcretion.NamedConcretion1.class )
			.bind( NamedConcretion.class ).toClass( NamedConcretion.NamedConcretion2.class )
			.bind( UnknownNamedDependencyClass.class ).toClass( UnknownNamedDependencyClass.class );

		assertThatCode( builder::initialize ).isInstanceOf( MiniDI.MissingBindingException.class );
	}

	@Test
	public void onlyNamedBindingsRegistered_requestFieldWithoutName_throwsMissingBindingException( )
	{
		final MiniDI.InjectorBuilder builder = MiniDI.create( )
			.bind( NamedConcretion.class ).toClass( NamedConcretion.NamedConcretion1.class )
			.bind( NamedConcretion.class ).toClass( NamedConcretion.NamedConcretion2.class )
			.bind( NamedDependecyWithoutNameQualifierClass.class )
			.toClass( NamedDependecyWithoutNameQualifierClass.class );

		assertThatCode( builder::initialize ).isInstanceOf( MiniDI.MissingBindingException.class );
	}

	@Test
	public void injectNamedStringField_valueByProvider( )
	{
		final MiniDI.Injector injector = MiniDI.create( )
			.bind( String.class ).toProvider( StringProvider.class )
			.bind( NamedStringFieldClass.class ).toClass( NamedStringFieldClass.class )
			.initialize( );

		final NamedStringFieldClass namedStringFieldClass = injector.get( NamedStringFieldClass.class );

		assertThat( namedStringFieldClass.getStringField( ) ).isEqualTo( "this is a string" );
	}
}
