package com.github.andrpash.minidi.simple.testclasses.circular;

import com.github.andrpash.minidi.MiniDI;

public class ChildWithRefToRoot
{
	@MiniDI.Inject
	private Root root;
}
