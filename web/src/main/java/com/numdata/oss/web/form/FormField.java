/*
 * Copyright (c) 2008-2017, Numdata BV, The Netherlands.
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

import com.numdata.oss.*;
import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * This is the base class for input fields on a form.
 *
 * @author S. Bouwman
 * @author Peter S. Heijnen
 */
public abstract class FormField
extends FormComponent
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = FormField.class.getName();

	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( FormField.class );

	/**
	 * Value target for form field.
	 */
	private final FieldTarget _target;

	/**
	 * Flag to indicate that this field requires (non-empty) input.
	 */
	private boolean _required;

	/**
	 * Specifies a problem with the field's current value, if any.
	 */
	@Nullable
	private Exception _exception;

	/**
	 * Provides additional information about the field to the user.
	 */
	@Nullable
	private String _description;

	/**
	 * Checks to perform for this field (element = FormCheck).
	 */
	private final Collection<FormCheck> _checks = new ArrayList<FormCheck>();

	/**
	 * Constructor.
	 *
	 * @param target Target object for this field.
	 */
	protected FormField( @NotNull final FieldTarget target )
	{
		super( target.getName() );
		_target = target;
		_required = false;
		_exception = null;
		_description = null;
	}

	@NotNull
	@Override
	public String getName()
	{
		// @NotNull because FieldTarget.getName() is @NotNull
		//noinspection ConstantConditions
		return super.getName();
	}

	/**
	 * Set field value.
	 *
	 * @param value Value to assign to the field.
	 */
	public void setValue( @Nullable final String value )
	{
		_target.setValue( value );
	}

	/**
	 * Get field value.
	 *
	 * @return Field value.
	 */
	@Nullable
	public String getValue()
	{
		return _target.getValue();
	}

	/**
	 * Set input requirement for this field.
	 *
	 * @param required {@code true} if this field requires input; {@code false}
	 *                 otherwise.
	 */
	public void setRequired( final boolean required )
	{
		LOG.trace( CLASS_NAME + "[ " + getName() + " ].setRequired( " + required + " )" );
		_required = required;
	}

	/**
	 * Check if the field requires input.
	 *
	 * @return {@code true} if the field requires input; {@code false}
	 * otherwise.
	 */
	public boolean isRequired()
	{
		return _required;
	}

	/**
	 * Returns an exception specifying a problem with this field's value.
	 * Possible causes are a missing required field and a failed form check.
	 *
	 * @return Exception specifying a problem with this field's value; {@code
	 * null} if the field's value is valid.
	 */
	@Nullable
	public Exception getException()
	{
		return _exception;
	}

	/**
	 * Sets an exception specifying a problem with this field's value. Possible
	 * causes are a missing required field and a failed form check.
	 *
	 * @param exception Exception to be set.
	 */
	public void setException( @Nullable final Exception exception )
	{
		_exception = exception;
	}

	/**
	 * Returns a description that provides additional information about this
	 * field to the user. For example, it could specify the required length of
	 * the field or which characters may be used.
	 *
	 * @return Note about the field, if any.
	 */
	@Nullable
	public String getDescription()
	{
		return _description;
	}

	/**
	 * Sets a description that provides additional information about this field
	 * to the user. For example, it could specify the required length of the
	 * field or which characters may be used.
	 *
	 * @param description Note about the field.
	 */
	public void setDescription( @Nullable final String description )
	{
		_description = description;
	}

	/**
	 * Add check object to this field. Checks will be executed when the {@link
	 * #check()} method is called.
	 *
	 * @param check Form check to add.
	 */
	public void addCheck( final FormCheck check )
	{
		LOG.trace( CLASS_NAME + "[ " + getName() + " ].addCheck()" );
		if ( check != null )
		{
			_checks.add( check );
		}
	}

	/**
	 * Performs the field's form checks, if any.
	 *
	 * @throws InvalidFormDataException if there was problem with the submitted
	 * data.
	 */
	public void check()
	throws InvalidFormDataException
	{
		for ( final FormCheck check : _checks )
		{
			check.check( this );
		}

		if ( isRequired() && ( ( getValue() == null ) || TextTools.isEmpty( getValue() ) ) )
		{
			LOG.debug( "Required field '" + getName() + "' has no value" );
			throw new InvalidFormDataException( getName(), InvalidFormDataException.Type.INFO_MISSING );
		}
	}

	/**
	 * Returns the localized error message for the field, if any.
	 *
	 * @return Error message for the field.
	 */
	@Nullable
	public String getErrorMessage()
	{
		String errorMessage = null;

		//noinspection ThrowableResultOfMethodCallIgnored
		final Exception exception = getException();
		if ( exception != null )
		{
			if ( exception instanceof InvalidFormDataException )
			{
				// TODO: Form-related exceptions could use some cleanup, e.g.:
				final InvalidFormDataException invalidFormDataException = (InvalidFormDataException)exception;
				if ( invalidFormDataException.getType() == InvalidFormDataException.Type.INFO_MISSING )
				{
					errorMessage = invalidFormDataException.getLocalizedTitle( getLocale() );
				}

				if ( TextTools.isEmpty( errorMessage ) )
				{
					errorMessage = invalidFormDataException.getLocalizedMessage( getLocale() );
				}
			}
			else
			{
				errorMessage = exception.getMessage();
			}
		}

		return errorMessage;
	}
}
