/*
 * Copyright (c) 2017, Numdata BV, The Netherlands.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Numdata nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NUMDATA BV BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.numdata.oss.ui;

import java.awt.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;

import com.numdata.oss.junit.*;
import com.numdata.oss.ui.test.*;
import org.junit.*;

/**
 * This test verifies the {@link Wizard} class.
 *
 * @author Peter S. Heijnen
 */
public class TestWizard
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestWizard.class.getName();

	/**
	 * Test {@link Wizard#Wizard constructor}.
	 */
	@Test
	public void testConstructor()
	{
		System.out.println( CLASS_NAME + ".testConstructor()" );

		final Frame frame = new Frame();
		final Locale locale = new Locale( "nl", "NL" );

		final Wizard wizard = new Wizard( frame, locale );

		wizard.addPage( new WizardPage( wizard, null, null, null )
		{
			@Override
			public String getDescription()
			{
				return "TestDescription";
			}

			@Override
			public String getTitle()
			{
				return "TestTitle";
			}
		} );

		WizardTester.assertFunctionalWizard( wizard );
	}

	/**
	 * Test resource bundles for class.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testResources()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testResources()" );

		final Locale[] locales = { new Locale( "nl", "NL" ), Locale.US, Locale.GERMANY };

		final List<String> expectedKeys = new ArrayList<String>();

		for ( final Field field : Wizard.class.getFields() )
		{
			final int modifiers = field.getModifiers();
			final Class<?> type = field.getType();

			try
			{
				if ( Modifier.isStatic( modifiers ) && ( type == String.class ) )
				{
					final String value = (String)field.get( null );
					expectedKeys.add( value );
				}
			}
			catch ( Exception e )
			{
				e.printStackTrace();/* ignore invalid fields */
			}
		}

		ResourceBundleTester.testBundles( Wizard.class, true, locales, false, expectedKeys, false, true, false );
	}
}
