package com.github.andrpash.minidi.named.testclasses;

import com.github.andrpash.minidi.MiniDI;

public class NamedDependecyWithoutNameQualifierClass
{
	@MiniDI.Inject
	private NamedConcretion noNameSpecified;
}
