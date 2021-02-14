package com.github.andrpash.minidi.named.testclasses;

import com.github.andrpash.minidi.MiniDI;

@MiniDI.Named( "string_field" )
public class StringProvider implements MiniDI.Provider<String>
{
	@Override public String get( )
	{
		return "this is a string";
	}
}
