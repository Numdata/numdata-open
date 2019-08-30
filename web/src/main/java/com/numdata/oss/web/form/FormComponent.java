/*
 * Copyright (c) 2017-2019, Numdata BV, The Netherlands.
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
import javax.servlet.http.*;

import com.numdata.oss.web.*;
import org.jetbrains.annotations.*;

/**
 * This class is the base class for form components.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( "ClassReferencesSubclass" )
public abstract class FormComponent
{
	/**
	 * Submit status.
	 */
	public enum SubmitStatus
	{
		/**
		 * Submit status is not yet determined or can not be determined.
		 */
		UNKNOWN,

		/**
		 * Data has been submitted.
		 */
		SUBMITTED,

		/**
		 * Data has been submitted, but it contains errors.
		 */
		SUBMITTED_WITH_ERRORS,

		/**
		 * Data has not been submitted.
		 */
		NOT_SUBMITTED;

		/**
		 * Combine this status with another status.
		 *
		 * @param other Status to combine with.
		 *
		 * @return Combined status.
		 */
		SubmitStatus combineWith( final SubmitStatus other )
		{
			final SubmitStatus result;

			if ( ( this == SUBMITTED_WITH_ERRORS ) || ( other == SUBMITTED_WITH_ERRORS ) )
			{
				result = SUBMITTED_WITH_ERRORS;
			}
			else if ( ( this == NOT_SUBMITTED ) || ( other == NOT_SUBMITTED ) )
			{
				result = NOT_SUBMITTED;
			}
			else if ( ( this == SUBMITTED ) || ( other == SUBMITTED ) )
			{
				result = SUBMITTED;
			}
			else
			{
				result = UNKNOWN;
			}

			return result;
		}
	}

	/**
	 * Container of this component.
	 */
	@Nullable
	private FormContainer _parent;

	/**
	 * Editable flag of this component.
	 *
	 * Note that a component is only editable, if its editable flag is set, and
	 * its parent is editable as well.
	 */
	private boolean _editable = true;

	/**
	 * Visible flag of this component.
	 *
	 * Note that a component is only visible, if its visible flag is set, and
	 * its parent is visible as well.
	 */
	private boolean _visible = true;

	/**
	 * HTML/java name of field.
	 */
	@Nullable
	protected String _name;

	/**
	 * Construct component.
	 */
	protected FormComponent()
	{
		this( null );
	}

	/**
	 * Construct component.
	 *
	 * @param name Name of the field.
	 */
	protected FormComponent( @Nullable final String name )
	{
		_parent = null;
		_name = name;
	}

	public boolean isEditable()
	{
		final boolean result;
		if ( !_editable )
		{
			result = false;
		}
		else
		{
			final FormContainer parent = getParent();
			result = ( ( parent == null ) || parent.isEditable() );
		}
		return result;
	}

	public void setEditable( final boolean editable )
	{
		_editable = editable;
	}

	public boolean isVisible()
	{
		final boolean result;

		if ( !_visible )
		{
			result = false;
		}
		else
		{
			final FormContainer parent = getParent();
			result = ( ( parent == null ) || parent.isVisible() );
		}

		return result;
	}

	public void setVisible( final boolean visible )
	{
		_visible = visible;
	}

	/**
	 * Get locale for localization of values shown on forms.
	 *
	 * @return Locale for localization of values shown on forms.
	 */
	public Locale getLocale()
	{
		final FormContainer parent = getParent();
		return ( parent == null ) ? Locale.getDefault() : parent.getLocale();
	}

	/**
	 * Get HTML/java name of field.
	 *
	 * @return HTML/java name of field.
	 */
	@Nullable
	public String getName()
	{
		final boolean qualifyName = ( _parent != null ) && _parent.isQualifiedNameUsed();
		return qualifyName ? getQualifiedName() : getComponentName();
	}

	/**
	 * Get name of component.
	 *
	 * @return HTML/java name of field.
	 */
	@Nullable
	public String getComponentName()
	{
		return _name;
	}

	/**
	 * Set name of component.
	 *
	 * @param name Name of component.
	 */
	public void setComponentName( final String name )
	{
		_name = name;
	}

	/**
	 * Get container of this component.
	 *
	 * @return Container of this component.
	 */
	@Nullable
	public FormContainer getParent()
	{
		return _parent;
	}

	/**
	 * Returns the form containing this component.
	 *
	 * @return Containing form; {@code null} if not contained in a form.
	 */
	@Nullable
	public Form getForm()
	{
		Form result = null;

		for ( FormComponent component = this; component != null; component = component.getParent() )
		{
			if ( component instanceof Form )
			{
				result = (Form)component;
				break;
			}
		}

		return result;
	}

	/**
	 * Get previous component in the parent container.
	 *
	 * @return Previous component in the parent container; {@code null} if no
	 * previous component is available.
	 */
	@Nullable
	public FormComponent getPrevious()
	{
		final FormContainer parent = getParent();
		final int i = parent == null ? 0 : parent.getComponentIndex( this );
		return ( i > 0 ) ? parent.getComponent( i - 1 ) : null;
	}

	/**
	 * Get next component in the parent container.
	 *
	 * @return Next component in the parent container; {@code null} if no next
	 * component is available.
	 */
	@Nullable
	public FormComponent getNext()
	{
		final FormContainer parent = getParent();
		final int i = parent == null ? -1 : parent.getComponentIndex( this );
		return ( ( i >= 0 ) && ( i < ( parent.getComponentCount() - 1 ) ) ) ? parent.getComponent( i + 1 ) : null;
	}

	/**
	 * Test whether this is the first component in the parent container.
	 *
	 * @return {@code true} if this is the first component in the parent; {@code
	 * false} otherwise.
	 */
	public boolean isFirst()
	{
		final FormContainer parent = getParent();
		return ( parent != null ) && ( parent.getComponentIndex( this ) == 0 );
	}

	/**
	 * Test whether this is the last component in the parent container.
	 *
	 * @return {@code true} if this is the last component in the parent; {@code
	 * false} otherwise.
	 */
	public boolean isLast()
	{
		final FormContainer parent = getParent();
		final int i = parent == null ? -1 : parent.getComponentIndex( this );
		return ( i >= 0 ) && ( i == ( parent.getComponentCount() - 1 ) );
	}

	/**
	 * Set container of this component. This should never be called directly.
	 *
	 * @param parent Container of this component.
	 */
	protected void setParent( @Nullable final FormContainer parent )
	{
		_parent = parent;
	}

	/**
	 * Returns the qualified name of the form component. The qualified name
	 * consists of the name of the component, prefixed with the parent's
	 * qualified name (if the component has a parent). Names are separated by a
	 * period ('.'). As such, qualified names match the following grammar:
	 * {@code ( name "." )* name}.
	 *
	 * @return Qualified name of the field.
	 */
	@Nullable
	protected String getQualifiedName()
	{
		final String name = getComponentName();
		final FormContainer parent = _parent;
		return ( ( name == null ) || ( parent == null ) || ( parent.getName() == null ) ) ? name : parent.getName() + '.' + name;
	}

	/**
	 * Generate output for this field using the specified table and writer.
	 *
	 * @param contextPath Context path associated with servlet (may be empty).
	 * @param form        Form for which output is generated.
	 * @param table       Table for which output is generated (if used).
	 * @param iw          Writer used for output.
	 * @param formFactory HTML form factory.
	 *
	 * @throws IOException if an error occurs while generating output.
	 */
	protected abstract void generate( @NotNull final String contextPath, @NotNull final Form form, @Nullable final HTMLTable table, @NotNull final IndentingJspWriter iw, @NotNull final HTMLFormFactory formFactory )
	throws IOException;

	/**
	 * Process data that was submitted by the specified request and assign it to
	 * any input components of a form.
	 *
	 * @param request Request that was used to process the form.
	 *
	 * @return {@link SubmitStatus} of this component.
	 *
	 * @throws InvalidFormDataException if there was problem with the submitted
	 * data.
	 */
	@NotNull
	public abstract SubmitStatus submitData( @NotNull HttpServletRequest request )
	throws InvalidFormDataException;

	/**
	 * Writes a text representation of the form component.
	 *
	 * @param out    Stream to write to.
	 * @param indent Current indentation.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public void writeAsText( @NotNull final Appendable out, @NotNull final String indent )
	throws IOException
	{
	}

	@Override
	public String toString()
	{
		return super.toString() + "[name=" + getName() + ']';
	}
}
