package com.github.andrpash.minidi.dynamicbinding.testclasses;

import com.github.andrpash.minidi.MiniDI;

public class DynamicRootWithMissingDependency extends DynamicRootClass
{
	@MiniDI.Inject
	private UnprovidedClass unprovided;
}
