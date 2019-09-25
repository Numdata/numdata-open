/*
 * Copyright (c) 2005-2019, Numdata BV, The Netherlands.
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
import java.util.*;

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
	@Test
	public void testConstructor()
	{
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

	@Test
	public void testResources()
	{
		final Class<?> clazz = Wizard.class;
		final ResourceBundleTester tester = ResourceBundleTester.forClass( clazz );
		tester.addExpectedKeys( FieldTester.getConstants( clazz, false, true, String.class ) );
		tester.run();
	}
}
