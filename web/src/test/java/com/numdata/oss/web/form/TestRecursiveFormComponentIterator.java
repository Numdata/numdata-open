/*
 * Copyright (c) 2016-2017, Numdata BV, The Netherlands.
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
package com.numdata.oss.web.form;

import java.util.*;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test for {@link RecursiveFormComponentIterator}.
 *
 * @author Peter S. Heijnen
 */
public class TestRecursiveFormComponentIterator
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestRecursiveFormComponentIterator.class.getName();

	@Test
	public void testWalk()
	{
		System.out.println( CLASS_NAME + ".testWalk" );

		final Locale locale = Locale.ENGLISH;
		final Form form = new Form( locale, "formName", "formTitle", true );

		final FormTextField textField1 = new FormTextField( new VariableFieldTarget( "textField1", "textField1" ), 10, -1 );
		final FormLabel textField1Label = new FormLabel( textField1.getName(), textField1 );
		form.addComponent( textField1Label );
		form.addComponent( textField1 );

		final FormSection formSection1 = new FormSection( "formSection1" );
		form.addComponent( formSection1 );

		final FormChoice choice1 = formSection1.createChoice( null, new VariableFieldTarget( "choice1", "value1" ), locale, FormComponent.SubmitStatus.class );
		final FormLabel choice1Label = new FormLabel( choice1.getName(), choice1 );
		formSection1.addComponent( choice1Label );
		formSection1.addComponent( choice1 );

		final FormTextArea textArea1 = new FormTextArea( new VariableFieldTarget( "textArea1", "textArea1" ), 5, 10 );
		final FormLabel textArea1Label = new FormLabel( textArea1.getName(), textArea1 );
		formSection1.addComponent( textArea1Label );
		formSection1.addComponent( textArea1 );

		final FormButton submitButton = new FormButton( FormButton.SUBMIT, "submitButton" );
		form.addButton( submitButton );

		final SubForm subForm1 = new SubForm( "subForm1" );
		form.addComponent( subForm1 );

		final FormCheckbox checkBox1 = new FormCheckbox( new VariableFieldTarget( "checkBox1", "true" ) );
		final FormLabel checkBox1Label = new FormLabel( checkBox1.getName(), checkBox1 );
		subForm1.addComponent( checkBox1Label );
		subForm1.addComponent( checkBox1 );

		final SubForm subForm2 = new SubForm( "subForm2" );
		form.addComponent( subForm2 );

		System.out.println( " - Test #1 - Iterate root" );
		final Iterator<FormComponent> iterator1 = new RecursiveFormComponentIterator( form );
		assertSame( "Test #1 - Iteration step #1", textField1Label, iterator1.next() );
		assertSame( "Test #1 - Iteration step #2", textField1, iterator1.next() );
		assertSame( "Test #1 - Iteration step #3", formSection1, iterator1.next() );
		assertSame( "Test #1 - Iteration step #4", choice1Label, iterator1.next() );
		assertSame( "Test #1 - Iteration step #5", choice1, iterator1.next() );
		assertSame( "Test #1 - Iteration step #6", textArea1Label, iterator1.next() );
		assertSame( "Test #1 - Iteration step #7", textArea1, iterator1.next() );
		assertSame( "Test #1 - Iteration step #8", subForm1, iterator1.next() );
		assertSame( "Test #1 - Iteration step #9", checkBox1Label, iterator1.next() );
		assertSame( "Test #1 - Iteration step #10", checkBox1, iterator1.next() );
		assertSame( "Test #1 - Iteration step #11", subForm2, iterator1.next() );
		assertEquals( "Test #1 - Finished", null, iterator1.hasNext() ? iterator1.next() : null );
		try
		{
			iterator1.next();
			fail( "Test #1 - Should have thrown NoSuchElementException" );
		}
		catch ( final NoSuchElementException e )
		{
			// expected
		}

		System.out.println( " - Test #2 - Iterate formSection1" );
		final Iterator<FormComponent> iterator2 = new RecursiveFormComponentIterator( formSection1 );
		assertSame( "Test #2 - Iteration step #1", choice1Label, iterator2.next() );
		assertSame( "Test #2 - Iteration step #2", choice1, iterator2.next() );
		assertSame( "Test #2 - Iteration step #3", textArea1Label, iterator2.next() );
		assertSame( "Test #2 - Iteration step #4", textArea1, iterator2.next() );
		assertEquals( "Test #2 - Finished", null, iterator2.hasNext() ? iterator2.next() : null );
		try
		{
			iterator2.next();
			fail( "Test #1 - Should have thrown NoSuchElementException" );
		}
		catch ( final NoSuchElementException e )
		{
			// expected
		}

		System.out.println( " - Test #3 - Iterate subForm1" );
		final Iterator<FormComponent> iterator3 = new RecursiveFormComponentIterator( subForm1 );
		assertSame( "Test #3 - Iteration step #1", checkBox1Label, iterator3.next() );
		assertSame( "Test #3 - Iteration step #2", checkBox1, iterator3.next() );
		assertEquals( "Test #3 - Finished", null, iterator3.hasNext() ? iterator3.next() : null );
		try
		{
			iterator3.next();
			fail( "Test #1 - Should have thrown NoSuchElementException" );
		}
		catch ( final NoSuchElementException e )
		{
			// expected
		}

		System.out.println( " - Test #4 - Iterate subForm1" );
		final Iterator<FormComponent> iterator4 = new RecursiveFormComponentIterator( subForm2 );
		assertEquals( "Test #4 - Finished", null, iterator4.hasNext() ? iterator4.next() : null );
		try
		{
			iterator4.next();
			fail( "Test #1 - Should have thrown NoSuchElementException" );
		}
		catch ( final NoSuchElementException e )
		{
			// expected
		}
	}
}
