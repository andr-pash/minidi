package com.github.andrpash.minidi.complex.testclasses;

import com.github.andrpash.minidi.MiniDI;

public class Level1
{
	@MiniDI.Inject
	private Level2_1 child1;

	@MiniDI.Inject
	private Level2_2 child2;

	@MiniDI.Inject
	private Level3 child3;

	public static class Level2_1
	{
		@MiniDI.Inject
		private Level2_2 child1;

		@MiniDI.Inject
		private Level3 child2;
	}

	public static class Level2_2
	{
		@MiniDI.Inject
		private Level3 child;
	}

	public static class Level3
	{

	}

}
