package com.github.andrpash.minidi.lazy.testclasses;

import com.github.andrpash.minidi.MiniDI;

public class InvalidLazyAnnotationClass
{
	@MiniDI.Inject
	@MiniDI.Lazy
	private NoDependencyClass child;
}
