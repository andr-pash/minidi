package com.github.andrpash.minidi.simple.testclasses.invalid;

public class ThrowingConstructorClass
{
	public static class ThrowingConstructorClassException extends RuntimeException
	{

	}

	public ThrowingConstructorClass( )
	{
		throw new ThrowingConstructorClassException( );
	}
}
