package com.github.andrpash.minidi.named.testclasses;

import com.github.andrpash.minidi.MiniDI;

public class NamedStringFieldClass
{
	@MiniDI.Inject
	@MiniDI.Named( "string_field" )
	private String stringField;

	public String getStringField( )
	{
		return this.stringField;
	}
}
