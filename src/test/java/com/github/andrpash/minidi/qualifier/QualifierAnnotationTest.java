package com.github.andrpash.minidi.qualifier;

import com.github.andrpash.minidi.MiniDI;
import com.github.andrpash.minidi.qualifier.testclasses.QualifiedClass;
import com.github.andrpash.minidi.qualifier.testclasses.QualifiedDependencyClass;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class QualifierAnnotationTest
{
	@Test
	public void qualifiedFields_injectCorrectDependencies( )
	{
		final MiniDI.Injector injector = MiniDI.create( )
			.bind( QualifiedClass.class ).toClass( QualifiedClass.QualifiedClass1.class )
			.bind( QualifiedClass.class ).toClass( QualifiedClass.QualifiedClass2.class )
			.bind( QualifiedDependencyClass.class ).toClass( QualifiedDependencyClass.class )
			.initialize( );

		final QualifiedDependencyClass dependencyClass = injector.get( QualifiedDependencyClass.class );

		assertThat( dependencyClass.getQualified1( ) ).isExactlyInstanceOf( QualifiedClass.QualifiedClass1.class );
		assertThat( dependencyClass.getQualified2( ) ).isExactlyInstanceOf( QualifiedClass.QualifiedClass2.class );
	}
}
