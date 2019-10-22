/*
 * Copyright (c) 2007-2019, Numdata BV, The Netherlands.
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
package com.numdata.printer;

import javax.print.attribute.standard.*;

import com.numdata.oss.*;
import com.numdata.oss.junit.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * This class tests the {@link PrinterSettings} class.
 *
 * @author Peter S. Heijnen
 */
public class TestPrinterSettings
{
	@Test
	public void testResources()
	{
		final Class<?> clazz = PrinterSettings.class;
		final ResourceBundleTester tester = ResourceBundleTester.forClass( clazz );
		tester.addExpectedKeysWithSuffix( FieldTester.getConstants( clazz, false, true, String.class ), "Tip", "Type" );
		tester.removeExpectedKey( PrinterSettings.PRINTER_SETTINGS + "Tip" );
		tester.removeExpectedKey( PrinterSettings.PRINTER_SETTINGS + "Type" );
		tester.addExpectedKeyWithSuffix( PrinterSettings.HOSTNAME, "Condition" );
		tester.addExpectedKeyWithSuffix( PrinterSettings.METHOD, "Values", "Default" );
		tester.addEnumNames( PrinterSettings.METHOD + '.', PrinterSettings.Method.class );
		tester.addExpectedKeyWithSuffix( PrinterSettings.NETWORK_PORT, "Condition", "Default" );
		tester.addExpectedKeyWithSuffix( PrinterSettings.PAGE_ORIENTATION, "Values", "Default" );
		tester.addEnumNames( PrinterSettings.PAGE_ORIENTATION + '.', PrinterSettings.PageOrientation.class );
		tester.addExpectedKeyWithSuffix( PrinterSettings.QUEUE_NAME, "Condition" );
		tester.addExpectedKey( "settings" );
		tester.run();
	}

	@Test
	public void testImageableBounds()
	{
		final PrinterSettings settings = new PrinterSettings();
		settings.setPageOrientation( PrinterSettings.PageOrientation.PORTRAIT );
		settings.setImageablePageWidth( 180.0 );
		settings.setImageablePageHeight( 267.0 );
		settings.setImageableX( 14.0 );
		settings.setPageOrientation( PrinterSettings.PageOrientation.LANDSCAPE );
		settings.setImageableY( 13.0 );
		settings.setPageHeight( 297.0 );
		settings.setPageWidth( 210.0 );
		settings.setResolution( 600.0 );

		final double delta = 1.0e-8;
		assertEquals( "Imageable page width", 180.0, settings.getImageablePageWidth(), delta );
		assertEquals( "Imageable page height", 267.0, settings.getImageablePageHeight(), delta );
		assertEquals( "Imageable x", 14.0, settings.getImageableX(), delta );
		assertEquals( "Imageable y", 13.0, settings.getImageableY(), delta );
		assertEquals( "Page width", 210.0, settings.getPageWidth(), delta );
		assertEquals( "Page height", 297.0, settings.getPageHeight(), delta );
		assertEquals( "Resolution", 600.0, settings.getResolution(), delta );
		assertEquals( "Page orientation", PrinterSettings.PageOrientation.LANDSCAPE, settings.getPageOrientation() );
	}

	@Test
	public void testSetMediaSize()
	{
		final PrinterSettings settings = new PrinterSettings();
		settings.setPageSize( 100.0, 100.0 );
		settings.setMargins( new Insets2D.Double( 13.0, 14.0, 15.0, 16.0 ) );
		settings.setPageOrientation( PrinterSettings.PageOrientation.PORTRAIT );
		settings.setMediaSize( MediaSize.ISO.A4 );

		final double delta = 1.0e-8;
		assertEquals( "Page width", 210.0, settings.getPageWidth(), delta );
		assertEquals( "Page height", 297.0, settings.getPageHeight(), delta );
		assertEquals( "Imageable page width", 180.0, settings.getImageablePageWidth(), delta );
		assertEquals( "Imageable page height", 269.0, settings.getImageablePageHeight(), delta );
		assertEquals( "Imageable x", 14.0, settings.getImageableX(), delta );
		assertEquals( "Imageable y", 13.0, settings.getImageableY(), delta );

		settings.setPageSize( 100.0, 100.0 );
		settings.setMargins( new Insets2D.Double( 13.0, 14.0, 15.0, 16.0 ) );
		settings.setPageOrientation( PrinterSettings.PageOrientation.LANDSCAPE );
		settings.setMediaSize( MediaSize.ISO.A4 );
		assertEquals( "Page width", 210.0, settings.getPageWidth(), delta );
		assertEquals( "Page height", 297.0, settings.getPageHeight(), delta );
		assertEquals( "Imageable page width", 180.0, settings.getImageablePageWidth(), delta );
		assertEquals( "Imageable page height", 269.0, settings.getImageablePageHeight(), delta );
		assertEquals( "Imageable x", 14.0, settings.getImageableX(), delta );
		assertEquals( "Imageable y", 13.0, settings.getImageableY(), delta );
	}

	@Test
	public void testMargins()
	{
		final PrinterSettings settings = new PrinterSettings();
		settings.setPageOrientation( PrinterSettings.PageOrientation.PORTRAIT );

		final Insets2D.Double margins = new Insets2D.Double( 13.0, 14.0, 15.0, 16.0 );
		final double delta = 1.0e-8;

		settings.setMargins( margins );
		assertEquals( "Imageable page width", 180.0, settings.getImageablePageWidth(), delta );
		assertEquals( "Imageable page height", 269.0, settings.getImageablePageHeight(), delta );
		assertEquals( "Imageable x", 14.0, settings.getImageableX(), delta );
		assertEquals( "Imageable y", 13.0, settings.getImageableY(), delta );
		assertInsetEquals( "Margins", margins, settings.getMargins(), delta );

		settings.setPageOrientation( PrinterSettings.PageOrientation.LANDSCAPE );
		settings.setMargins( margins );
		assertEquals( "Imageable page width", 180.0, settings.getImageablePageWidth(), delta );
		assertEquals( "Imageable page height", 269.0, settings.getImageablePageHeight(), delta );
		assertEquals( "Imageable x", 14.0, settings.getImageableX(), delta );
		assertEquals( "Imageable y", 13.0, settings.getImageableY(), delta );
		assertInsetEquals( "Margins", margins, settings.getMargins(), delta );

		settings.setPageOrientation( PrinterSettings.PageOrientation.REVERSE_LANDSCAPE );
		settings.setMargins( margins );
		assertEquals( "Imageable page width", 180.0, settings.getImageablePageWidth(), delta );
		assertEquals( "Imageable page height", 269.0, settings.getImageablePageHeight(), delta );
		assertEquals( "Imageable x", 14.0, settings.getImageableX(), delta );
		assertEquals( "Imageable y", 13.0, settings.getImageableY(), delta );
		assertInsetEquals( "Margins", margins, settings.getMargins(), delta );
	}

	private void assertInsetEquals( final String message, final Insets2D expected, final Insets2D actual, final double delta )
	{
		assertEquals( message + ": top", expected.getTop(), actual.getTop(), delta );
		assertEquals( message + ": left", expected.getLeft(), actual.getLeft(), delta );
		assertEquals( message + ": bottom", expected.getBottom(), actual.getBottom(), delta );
		assertEquals( message + ": right", expected.getRight(), actual.getRight(), delta );
	}
}
