package com.github.andrpash.minidi.qualifier.testclasses;

import com.github.andrpash.minidi.MiniDI;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class QualifiedClass
{
	@MiniDI.Qualifier
	@Target( { FIELD, PARAMETER, METHOD, TYPE } )
	@Retention( RUNTIME ) @interface Qualified1
	{

	}

	@MiniDI.Qualifier
	@Target( { FIELD, PARAMETER, METHOD, TYPE } )
	@Retention( RUNTIME ) @interface Qualified2
	{

	}

	@Qualified1
	public static class QualifiedClass1 extends QualifiedClass
	{
	}

	@Qualified2
	public static class QualifiedClass2 extends QualifiedClass
	{
	}
}
