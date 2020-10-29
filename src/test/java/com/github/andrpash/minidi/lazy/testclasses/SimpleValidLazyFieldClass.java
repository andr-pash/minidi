package com.github.andrpash.minidi.lazy.testclasses;

import com.github.andrpash.minidi.MiniDI;

public class SimpleValidLazyFieldClass
{
	@MiniDI.Inject
	@MiniDI.Lazy
	ClassNameReturner child;

	public ClassNameReturner getChild( )
	{
		return this.child;
	}
}
