/*
 * Copyright (c) 2008-2020, Numdata BV, The Netherlands.
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

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

import com.numdata.oss.*;
import com.numdata.oss.web.*;
import com.numdata.oss.web.HTMLTable.*;
import org.jetbrains.annotations.*;

/**
 * This class is used to generate and process HTML forms.
 *
 * @author S. Bouwman
 * @author Peter S. Heijnen
 */
public class Form
extends FormContainer
{
	/**
	 * Locale for localization of values shown on forms.
	 */
	private final @NotNull Locale _locale;

	/**
	 * Visible title of form (maybe null).
	 */
	private @Nullable String _title;

	/**
	 * Space-separated list of character encodings for input data that is
	 * accepted by the server processing this form.
	 */
	private @Nullable String _acceptCharset = "UTF-8";

	/**
	 * Form buttons.
	 */
	private final List<FormButton> _buttons = new ArrayList<>();

	/**
	 * FormActions to execute before the submitValues() method starts to check
	 * all form fields. This may be used to do add global tests to the form
	 * before any fields are tested or submitted.
	 */
	private final List<FormAction> _preSubmitActions = new ArrayList<>();

	/**
	 * FormActions to execute after the submitValues() method has submitted all
	 * fields successfully. This may be used to write field values to their
	 * final destination if needed (e.g. user questionnaire or derived fields).
	 */
	private final List<FormAction> _postSubmitActions = new ArrayList<>();

	/**
	 * Construct form with title bar.
	 *
	 * @param locale   Locale for localization of values shown on forms.
	 * @param name     Name of form.
	 * @param title    Title to display in the title bar
	 * @param editable Form is editable or not.
	 */
	public Form( @NotNull final Locale locale, @Nullable final String name, @Nullable final String title, final boolean editable )
	{
		super( name );
		_locale = locale;
		_title = title;

		setEditable( editable );
	}

	/**
	 * Construct form with title bar.
	 *
	 * @param locale         Locale for localization of values shown on forms.
	 * @param name           Name of form.
	 * @param title          Title to display in the title bar
	 * @param editable       Form is editable or not.
	 * @param qualifiedNames {@code true} to use qualified names; {@code false} otherwise.
	 */
	public Form( @NotNull final Locale locale, @Nullable final String name, @Nullable final String title, final boolean editable, final boolean qualifiedNames )
	{
		super( name, qualifiedNames );
		_locale = locale;
		_title = title;

		setEditable( editable );
	}

	/**
	 * Get title of form.
	 *
	 * @return Title of form; {@code null} if form has no title.
	 */
	@Nullable
	public String getTitle()
	{
		return _title;
	}

	/**
	 * Set title of form.
	 *
	 * @param title Title of form ({@code null} =&gt; no title).
	 */
	public void setTitle( @Nullable final String title )
	{
		_title = title;
	}

	/**
	 * Returns the character encodings accepted by the server processing this
	 * form.
	 *
	 * @return Space-separated list of character encodings.
	 */
	@Nullable
	public String getAcceptCharset()
	{
		return _acceptCharset;
	}

	/**
	 * Sets the character encodings accepted by the server processing this
	 * form.
	 *
	 * @param acceptCharset Space-separated list of character encodings.
	 */
	public void setAcceptCharset( @Nullable final String acceptCharset )
	{
		_acceptCharset = acceptCharset;
	}

	/**
	 * Add button to form.
	 *
	 * @param button Button to add.
	 */
	public void addButton( @NotNull final FormButton button )
	{
		_buttons.add( button );
	}

	/**
	 * Convenience method to add a normal (link) button to the form. The button
	 * action (link) and text must be specified.
	 *
	 * @param action Action when button pressed (link).
	 * @param text   Text to place on button.
	 */
	public void addButton( @NotNull final String action, @NotNull final String text )
	{
		addButton( new FormButton( action, text ) );
	}

	/**
	 * Convenience method to add a back button to the form. The button text must
	 * be specified.
	 *
	 * @param text Text to place on button.
	 */
	public void addBackButton( @NotNull final String text )
	{
		addButton( new FormButton( FormButton.BACK, text ) );
	}

	/**
	 * Convenience method to add a submit button to the form. The button text
	 * must be specified.
	 *
	 * @param text Text to place on button.
	 */
	public void addSubmitButton( @NotNull final String text )
	{
		addButton( new FormButton( FormButton.SUBMIT, text ) );
	}

	/**
	 * Returns the form buttons.
	 *
	 * @return Form buttons.
	 */
	@NotNull
	public List<FormButton> getButtons()
	{
		return Collections.unmodifiableList( _buttons );
	}

	/**
	 * Add FormAction to execute after the submitValues() method has submitted
	 * all fields successfully. This may be used to write field values to their
	 * final destination if needed (e.g. user questionnaire or derived fields).
	 *
	 * @param action FormAction to execute.
	 */
	public void addPostSubmitAction( final FormAction action )
	{
		if ( action != null )
		{
			_postSubmitActions.add( action );
		}
	}

	@NotNull
	public List<FormAction> getPostSubmitActions()
	{
		return Collections.unmodifiableList( _postSubmitActions );
	}

	/**
	 * Add FormAction to execute before the submitValues() method starts to
	 * check all form fields. This may be used to do add global tests to the
	 * form before any fields are tested or submitted.
	 *
	 * @param action FormAction to execute.
	 */
	public void addPreSubmitAction( final FormAction action )
	{
		if ( action != null )
		{
			_preSubmitActions.add( action );
		}
	}

	@NotNull
	public List<FormAction> getPreSubmitActions()
	{
		return Collections.unmodifiableList( _preSubmitActions );
	}

	/**
	 * Generates HTML for current form.
	 *
	 * @param contextPath    Context path associated with servlet (may be empty
	 *                       or {@code null}).
	 * @param formFactory    HTML form factory.
	 * @param out            Writer to use for output.
	 * @param action         Action for form.
	 * @param formAttributes Attributes of {@code &lt;form&gt;} element.
	 *
	 * @return Writer that was used for output (may be wrapped).
	 *
	 * @throws IOException when writing failed.
	 */
	public IndentingJspWriter generate( @NotNull final String contextPath, @NotNull final HTMLFormFactory formFactory, @NotNull final JspWriter out, @NotNull final String action, @Nullable final Properties formAttributes )
	throws IOException
	{
		final HTMLTable table = formFactory.getTableFactory().createTable( 2 );
		table.setColumnAlignment( HorizontalAlignment.LEFT, HorizontalAlignment.LEFT );
		return generate( contextPath, formFactory, table, out, action, formAttributes );
	}

	/**
	 * Generates HTML for current form.
	 *
	 * @param contextPath    Context path associated with servlet (may be empty
	 *                       or {@code null}).
	 * @param formFactory    HTML form factory.
	 * @param table          HTML table.
	 * @param out            Writer to use for output.
	 * @param action         Action for form.
	 * @param formAttributes Attributes of {@code &lt;form&gt;} element.
	 *
	 * @return Writer that was used for output (may be wrapped).
	 *
	 * @throws IOException when writing failed.
	 */
	public IndentingJspWriter generate( @NotNull final String contextPath, @NotNull final HTMLFormFactory formFactory, @Nullable final HTMLTable table, @NotNull final JspWriter out, @NotNull final String action, @Nullable final Properties formAttributes )
	throws IOException
	{
		final Map<String, String> actualFormAttributes = PropertyTools.toMap( formAttributes );
		actualFormAttributes.put( "action", action );

		if ( !actualFormAttributes.containsKey( "method" ) )
		{
			actualFormAttributes.put( "method", "POST" );
		}

		final String formName = _name;
		if ( !actualFormAttributes.containsKey( "name" ) && ( formName != null ) )
		{
			actualFormAttributes.put( "name", formName );
		}

		if ( !actualFormAttributes.containsKey( "accept-charset" ) && ( _acceptCharset != null ) )
		{
			actualFormAttributes.put( "accept-charset", _acceptCharset );
		}

		if ( !actualFormAttributes.containsKey( "onsubmit" ) )
		{
			// Use 'setTimeout' to disable the form *after* the submit occurs. Otherwise disabled fields (i.e. all fields) are excluded from the submitted data.
			actualFormAttributes.put( "onsubmit", "setTimeout( function() {" + getSubmitDisableScript( false, true ) + "}, 1 );" );
		}

		final IndentingJspWriter iw = IndentingJspWriter.create( out, 2, 1 );
		if ( isVisible() )
		{
			final String title = getTitle();
			final List<FormButton> buttons = getButtons();

			formFactory.writeFormPre( contextPath, iw, actualFormAttributes, title, table, buttons );
			generate( contextPath, this, table, iw, formFactory );
			formFactory.writeFormPost( contextPath, iw, table, buttons );
		}
		return iw;
	}

	/**
	 * Get locale for localization of values shown on forms.
	 *
	 * @return Locale for localization of values shown on forms.
	 */
	@Override
	public @NotNull Locale getLocale()
	{
		return _locale;
	}

	/**
	 * Handle tri-state operation of form. This does not produce any output, but
	 * may adjust the form to support the 3 states:
	 *
	 * <dl>
	 *
	 * <dt>1. Operator has not done anything yet.</dt><dd>If no {@code
	 * saveButton} is provided, or the form is not editable, make/leave form
	 * uneditable;</dd><dd>if no {@code editButton} is provided, just add the
	 * {@code saveButton} to the form;</dd><dd>if both buttons are provided,
	 * make the form uneditable and add the {@code editButton} it.</dd>
	 *
	 * <dt>2. Operator pressed 'edit' (flagged by 'edit' parameter); or
	 * operation has not done anything yet and no 'editButton' is provided.</dt>
	 * <dd>Add 'save' button to form.</dd>
	 *
	 * <dt>3. Operator pressed 'save'.</dt><dd>Submit data, then handle like
	 * (1)</dd>
	 *
	 * </dl>
	 *
	 * @param request   Page request.
	 * @param pageLink  Link to page containing form.
	 * @param editLabel Label for edit button (if editable).
	 * @param saveLabel Label for save button (if editable).
	 *
	 * @return {@code true} if the form was submitted (should save data); {@code
	 * false} otherwise.
	 *
	 * @throws InvalidFormDataException form submission failed.
	 */
	public boolean handleTristate( @NotNull final HttpServletRequest request, @Nullable final String pageLink, @Nullable final String editLabel, @Nullable final String saveLabel )
	throws InvalidFormDataException
	{
		final FormButton editButton;

		if ( ( pageLink != null ) && ( editLabel != null ) )
		{
			editButton = new FormButton( pageLink + ( ( pageLink.indexOf( '?' ) >= 0 ) ? "&edit=true" : "?edit=true" ), editLabel );
		}
		else
		{
			editButton = null;
		}

		final FormButton saveButton = ( saveLabel != null ) ? new FormButton( FormButton.SUBMIT, saveLabel ) : null;
		return handleTristate( request, editButton, saveButton );
	}

	/**
	 * Handle tri-state operation of form. This does not produce any output, but
	 * may adjust the form to support the 3 states: <dl>
	 *
	 * <dt>1. Operator has not done anything yet.</dt><dd>If no {@code
	 * saveButton} is provided, or the form is not editable, make/leave form
	 * uneditable;</dd><dd>if no {@code editButton} is provided, just add the
	 * {@code saveButton} to the form;</dd><dd>if both buttons are provided,
	 * make the form uneditable and add the {@code editButton} it.</dd>
	 *
	 * <dt>2. Operator pressed 'edit' (flagged by 'edit' parameter); or
	 * operation has not done anything yet and no 'editButton' is provided.</dt>
	 * <dd>Add 'save' button to form.</dd>
	 *
	 * <dt>3. Operator pressed 'save'.</dt><dd>Submit data, then handle like
	 * (1)</dd>
	 *
	 * </dl>
	 *
	 * @param request    Page request.
	 * @param editButton Edit button that may be added (if editable).
	 * @param saveButton Save button that may be added (if editable).
	 *
	 * @return {@code true} if the form was submitted (should save data); {@code
	 * false} otherwise.
	 *
	 * @throws InvalidFormDataException form submission failed.
	 */
	public boolean handleTristate( @NotNull final HttpServletRequest request, @Nullable final FormButton editButton, @Nullable final FormButton saveButton )
	throws InvalidFormDataException
	{
		boolean result = false;

		if ( ( saveButton == null ) || !isEditable() )
		{
			setEditable( false );
		}
		else
		{
			final SubmitStatus submitStatus = submitData( request );
			result = ( submitStatus == SubmitStatus.SUBMITTED );
			if ( ( submitStatus != SubmitStatus.SUBMITTED_WITH_ERRORS ) && ( editButton != null ) && ( request.getParameter( "edit" ) == null ) )
			{
				setEditable( false );
				addButton( editButton );
			}
			else
			{
				addButton( saveButton );
			}
		}

		return result;
	}

	/**
	 * Handle tri-state operation of form. This does not produce any output, but
	 * submits data and may make the form uneditable.
	 *
	 * This does the following:<dl>
	 *
	 * <dt>1. If the form is not editable (read-only mode).</dt><dd>Don't do
	 * anything, because there is nothing to edit.</dd>
	 *
	 * <dt>2. If the 'edit' parameter is <em>NOT</em> set (view mode).</dt>
	 * <dd>Make form uneditable, because the user must first activate edit mode.
	 * Edit mode can be activated by setting the 'edit' parameter, typically by
	 * adding a button to the form with a self-reference and an 'edit=true'
	 * parameter.</dd>
	 *
	 * <dt>3. If the 'edit' parameter is set (edit mode).</dt><dd>Process
	 * submitted data, if any. Leave the form editable. A button must be added
	 * to allow the user to save/submit the data.</dd>
	 *
	 * </dl>
	 *
	 * @param request Page request.
	 *
	 * @return {@code true} if the form was submitted (should save data); {@code
	 * false} otherwise.
	 *
	 * @throws InvalidFormDataException form submission failed.
	 */
	public boolean handleTristate( @NotNull final HttpServletRequest request )
	throws InvalidFormDataException
	{
		boolean result = false;

		if ( isEditable() )
		{
			final SubmitStatus submitStatus = submitData( request );
			if ( ( submitStatus != SubmitStatus.SUBMITTED_WITH_ERRORS ) && ( request.getParameter( "edit" ) == null ) )
			{
				setEditable( false );
			}

			result = ( submitStatus == SubmitStatus.SUBMITTED );
		}

		return result;
	}

	/**
	 * This is a convenience method to handle form submission. It checks if the
	 * form has been submitted. If so, the submitted values are retrieved and
	 * checked.
	 *
	 * NOTE: The editable flag of the form is set to {@code true} while this
	 * method executes. It is restored when this method exits. This is done,
	 * because the form must be editable to access any form fields.
	 *
	 * @param request Request that was used to process the form.
	 *
	 * @return {@code true} if the form has been submitted successfully; {@code
	 * false} if the form has not been submitted.
	 *
	 * @throws InvalidFormDataException if an error occurred during submission
	 * checks.
	 */
	public boolean submit( @NotNull final HttpServletRequest request )
	throws InvalidFormDataException
	{
		final boolean result;

		/*
		 * Save editable state of form and enable the editable flag.
		 */
		final boolean wasEditable = isEditable();
		try
		{
			setEditable( true );
			result = ( submitData( request ) == SubmitStatus.SUBMITTED );
		}
		finally
		{
			setEditable( wasEditable );
		}

		return result;
	}

	@NotNull
	@Override
	public SubmitStatus submitData( @NotNull final HttpServletRequest request )
	throws InvalidFormDataException
	{
		runPreSubmitActions();

		final SubmitStatus result = submitDataToComponents( request );

		if ( result == SubmitStatus.SUBMITTED )
		{
			runPostSubmitActions();
		}

		return result;
	}

	/**
	 * Delegate for {@link #submitData} method to run all pre-submit actions.
	 *
	 * @throws InvalidFormDataException if an error occurs.
	 */
	protected void runPreSubmitActions()
	throws InvalidFormDataException
	{
		for ( final FormAction preSubmitAction : getPreSubmitActions() )
		{
			preSubmitAction.run( this );
		}
	}

	/**
	 * Delegate for {@link #submitData} method to run all post-submit actions.
	 *
	 * @throws InvalidFormDataException if an error occurs.
	 */
	protected void runPostSubmitActions()
	throws InvalidFormDataException
	{
		for ( final FormAction postSubmitAction : getPostSubmitActions() )
		{
			postSubmitAction.run( this );
		}
	}

	/**
	 * Method to guess/determine whether a button was pressed to submit this
	 * form. The assumption is made that if a request parameter is encountered
	 * that does not match a form component's name, it indicated that a button
	 * was pressed. This is typically not the case if the form is submitted by a
	 * script.
	 *
	 * @param request Servlet request.
	 *
	 * @return {@code true} if a button was probably pressed.
	 */
	public boolean isButtonPressed( @NotNull final ServletRequest request )
	{
		boolean result = false;

		final Map<String, String[]> parameterMap = request.getParameterMap();
		if ( !parameterMap.isEmpty() )
		{
			final Collection<String> parameterNames = new HashSet<>( parameterMap.keySet() );
			for ( final Deque<FormComponent> queue = new ArrayDeque<>( getComponents() ); !queue.isEmpty() && !parameterNames.isEmpty(); )
			{
				final FormComponent component = queue.removeLast();
				if ( component instanceof FormContainer )
				{
					queue.addAll( ( (FormContainer)component ).getComponents() );
				}

				if ( parameterNames.remove( component.getName() ) && ( component instanceof FormButton ) )
				{
					result = true;
					break;
				}
			}

			result = result || !parameterNames.isEmpty();
		}

		return result;
	}

	@Override
	public void writeAsText( @NotNull final Appendable out, @NotNull final String indent )
	throws IOException
	{
		final String title = getTitle();
		if ( title != null )
		{
			out.append( indent ).append( "[ " ).append( title ).append( " ]" );
		}
		super.writeAsText( out, indent );
		out.append( '\n' );
	}

	/**
	 * Returns Javascript to submit and/or disable the form.
	 *
	 * @param submit  {@code true} to submit the form.
	 * @param disable {@code true} to disable the form.
	 *
	 * @return Javascript to submit/disable the form.
	 *
	 * @throws IllegalArgumentException if both parameters are {@code false}.
	 */
	public @NotNull String getSubmitDisableScript( final boolean submit, final boolean disable )
	{
		if ( !submit && !disable )
		{
			throw new IllegalArgumentException( "At least one parameter must be true." );
		}

		final StringBuilder result = new StringBuilder();
		result.append( "var form = document.forms['" );
		result.append( getName() );
		result.append( "'];" );
		if ( submit )
		{
			result.append( "form.submit();" );
		}
		if ( disable )
		{
			result.append( "form.elements[ 0 ].disabled = true;" );
		}
		return result.toString();
	}
}
