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
package com.numdata.oss.ui.explorer;

import java.util.*;

import com.numdata.oss.junit.*;
import org.junit.*;

/**
 * This class tests the {@link FileDialog} class.
 *
 * @author Peter S. Heijnen
 */
public class TestFileDialog
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestFileDialog.class.getName();

	/**
	 * Locales to use for tests.
	 */
	private static final Locale[] LOCALES = { new Locale( "nl", "NL" ), Locale.US, Locale.GERMANY };

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

		ResourceBundleTester.testBundles( FileDialog.class, true, LOCALES, false, null, true, true, false );
	}
}
