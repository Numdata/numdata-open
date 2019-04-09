/*
 * (C) Copyright Numdata BV 2017-2019 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package com.numdata.oss.junit;

import org.jetbrains.annotations.*;
import org.junit.*;

/**
 * Helper for {@link ArrayTester#assertEquals} methods to determine whether
 * two elements of an array are equal.
 *
 * @author Peter S. Heijnen
 */
public class AssertEquals
{
	/**
	 * Assert equality of {@code actualValue} to {@code expectedValue}.
	 *
	 * @param message       Message to display if assertion fails.
	 * @param expectedValue Expected value.
	 * @param actualValue   Actual value.
	 */
	public void assertEquals( @Nullable final String message, @Nullable final Object expectedValue, @Nullable final Object actualValue )
	{
		if ( ( expectedValue instanceof Double ) && ( actualValue instanceof Double ) )
		{
			final double expectedDouble = (Double)expectedValue;
			final double actualDouble = (Double)actualValue;

			Assert.assertEquals( message, expectedDouble, actualDouble, 0.0001 );
		}
		else if ( ( expectedValue instanceof Float ) && ( actualValue instanceof Float ) )
		{
			final float expectedFloat = (Float)expectedValue;
			final float actualFloat = (Float)actualValue;

			Assert.assertEquals( message, expectedFloat, actualFloat, 0.0001f );
		}
		else
		{
			Assert.assertEquals( message, expectedValue, actualValue );
		}
	}
}
