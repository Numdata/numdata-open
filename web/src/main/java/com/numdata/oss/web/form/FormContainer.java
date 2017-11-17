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
package com.numdata.oss.web.form;

import java.io.*;
import java.math.*;
import java.util.*;
import javax.servlet.http.*;

import com.numdata.oss.*;
import com.numdata.oss.web.*;
import org.jetbrains.annotations.*;

/**
 * This class defines a container for form components.
 *
 * @author Peter S. Heijnen
 */
public class FormContainer
extends FormComponent
{
	/**
	 * Components contained within this container.
	 */
	private final List<FormComponent> _components = new ArrayList<FormComponent>();

	/**
	 * Whether qualified names are used by the components in the container.
	 */
	private final boolean _qualifiedNameUsed;

	/**
	 * Whether this container should be hidden if the form is not editable.
	 */
	private boolean _hideIfReadOnly = false;

	/**
	 * Add a component to this container. The component's parent is updated
	 * accordingly.
	 *
	 * @param component Component to add.
	 */
	public void addComponent( @NotNull final FormComponent component )
	{
		final FormContainer oldContainer = component.getParent();
		//noinspection ObjectEquality
		if ( oldContainer != this )
		{
			if ( oldContainer != null )
			{
				oldContainer._components.remove( component );
			}

			component.setParent( this );
			_components.add( component );
		}
	}

	/**
	 * Construct container.
	 */
	public FormContainer()
	{
		this( null, false );
	}

	/**
	 * Construct container.
	 *
	 * @param name Name of the field.
	 */
	public FormContainer( @Nullable final String name )
	{
		this( name, false );
	}

	/**
	 * Constructs a container with the specified name, using either unqualified
	 * or qualified names (see {@link FormComponent#getQualifiedName()}).
	 *
	 * @param name           Name of the container.
	 * @param qualifiedNames {@code true} to use qualified names; {@code false}
	 *                       otherwise.
	 */
	public FormContainer( @Nullable final String name, final boolean qualifiedNames )
	{
		super( name );
		_qualifiedNameUsed = qualifiedNames;
	}

	public boolean isQualifiedNameUsed()
	{
		return _qualifiedNameUsed;
	}

	public boolean isHideIfReadOnly()
	{
		return _hideIfReadOnly;
	}

	public void setHideIfReadOnly( final boolean hideIfReadOnly )
	{
		_hideIfReadOnly = hideIfReadOnly;
	}

	/**
	 * Add line-break to this container. This can be used to force a line-break
	 * between fields. This is especially useful for multi-line input fields.
	 */
	public void addLineBreak()
	{
		addComponent( new FormStaticText( "<br />\n" ) );
	}

	/**
	 * Add field spacer to this container. This can be used to create some space
	 * between two fields to get a more pleasing output.
	 */
	public void addFieldSpacer()
	{
		addComponent( new FormStaticText( "&nbsp;&nbsp;" ) );
	}

	/**
	 * Convenience method to quickly add a checkbox field to this container.
	 *
	 * @param bundle Bundle containing the field label (key=name).
	 * @param target Target object.
	 *
	 * @return Checkbox field that was created.
	 */
	public FormCheckbox addCheckbox( @Nullable final ResourceBundle bundle, @NotNull final FieldTarget target )
	{
		return addCheckbox( bundle, target.getName(), target );
	}

	/**
	 * Convenience method to quickly add a checkbox field to this container.
	 *
	 * @param bundle      Bundle containing the field label.
	 * @param resourceKey Key to use in resource bundle.
	 * @param target      Target object.
	 *
	 * @return Checkbox field that was created.
	 */
	public FormCheckbox addCheckbox( @Nullable final ResourceBundle bundle, @NotNull final String resourceKey, @NotNull final FieldTarget target )
	{
		final FormCheckbox checkbox = new FormCheckbox( target );
		addComponent( new FormLabel( ResourceBundleTools.getString( bundle, resourceKey, resourceKey ), checkbox ) );
		addComponent( checkbox );
		return checkbox;
	}

	/**
	 * Convenience method to quickly add a choice field to this container. The
	 * available value choices must be contained within the specified resource
	 * bundle.
	 *
	 * @param bundle Bundle containing the field label and value choices
	 *               (key=name).
	 * @param target Target object.
	 *
	 * @return Choice field that was created.
	 *
	 * @see ResourceBundleTools#getChoices
	 */
	public FormChoice addChoice( @NotNull final ResourceBundle bundle, @NotNull final FieldTarget target )
	{
		return addChoice( bundle, target.getName(), target );
	}

	/**
	 * Convenience method to quickly add a choice field to this container. The
	 * available value choices must be contained within the specified resource
	 * bundle.
	 *
	 * @param bundle      Bundle containing the field label and value choices.
	 * @param resourceKey Key to use in resource bundle.
	 * @param target      Target object.
	 *
	 * @return Choice field that was created.
	 *
	 * @see ResourceBundleTools#getChoices
	 */
	public FormChoice addChoice( @NotNull final ResourceBundle bundle, @NotNull final String resourceKey, @NotNull final FieldTarget target )
	{
		final List<String> optionValues = Arrays.asList( ResourceBundleTools.getStringList( bundle, resourceKey + "Values" ) );
		final List<String> optionLabels = new ArrayList<String>( optionValues.size() );

		for ( final String value : optionValues )
		{
			String label = TextTools.isNonEmpty( resourceKey ) ? ResourceBundleTools.getString( bundle, resourceKey + '.' + value, null ) : null;
			if ( label == null )
			{
				label = ResourceBundleTools.getString( bundle, value, value );
			}

			optionLabels.add( label );
		}

		final FormChoice choice = new FormChoice( target, optionValues, optionLabels );
		addComponent( new FormLabel( bundle.getString( resourceKey ), choice ) );
		addComponent( choice );
		return choice;
	}

	/**
	 * Convenience method to quickly add a choice field to this container. The
	 * available value choices are defined by the specified enumeration class.
	 *
	 * @param bundle   Bundle containing the field label and value choices
	 *                 (key=name).
	 * @param target   Target object.
	 * @param locale   Locale for enumeration value translations.
	 * @param enumType Enumeration type to determine choices.
	 * @param <T>      Enumeration type.
	 *
	 * @return Choice field that was created.
	 *
	 * @see ResourceBundleTools#getChoices
	 */
	public <T extends Enum<T>> FormChoice addChoice( @Nullable final ResourceBundle bundle, @NotNull final FieldTarget target, @NotNull final Locale locale, @NotNull final Class<T> enumType )
	{
		return addChoice( bundle, target.getName(), target, locale, enumType );
	}

	/**
	 * Convenience method to quickly add a choice field to this container. The
	 * available value choices are defined by the specified enumeration class.
	 *
	 * @param bundle      Bundle containing the field label and value choices.
	 * @param resourceKey Key to use in resource bundle.
	 * @param target      Target object.
	 * @param locale      Locale for enumeration value translations.
	 * @param enumType    Enumeration type to determine choices.
	 * @param <T>         Enumeration type.
	 *
	 * @return Choice field that was created.
	 *
	 * @see ResourceBundleTools#getChoices
	 */
	public <T extends Enum<T>> FormChoice addChoice( @Nullable final ResourceBundle bundle, @NotNull final String resourceKey, @NotNull final FieldTarget target, @NotNull final Locale locale, @NotNull final Class<T> enumType )
	{
		final FormChoice choice = createChoice( bundle, target, locale, enumType );
		addComponent( new FormLabel( ResourceBundleTools.getString( bundle, resourceKey, resourceKey ), choice ) );
		addComponent( choice );
		return choice;
	}

	/**
	 * Convenience method to quickly create a choice field. The available value
	 * choices are defined by the specified enumeration class.
	 *
	 * @param bundle   Bundle containing the field label and value choices
	 *                 (key=name).
	 * @param target   Target object.
	 * @param locale   Locale for enumeration value translations.
	 * @param enumType Enumeration type to determine choices.
	 * @param <T>      Enumeration type.
	 *
	 * @return Choice field that was created.
	 *
	 * @see ResourceBundleTools#getChoices
	 */
	@NotNull
	public <T extends Enum<T>> FormChoice createChoice( @Nullable final ResourceBundle bundle, @NotNull final FieldTarget target, @NotNull final Locale locale, @NotNull final Class<T> enumType )
	{
		return createChoice( bundle, target.getName(), target, locale, enumType );
	}

	/**
	 * Convenience method to quickly create a choice field. The available value
	 * choices are defined by the specified enumeration class.
	 *
	 * @param bundle      Bundle containing the field label and value choices.
	 * @param resourceKey Key to use in resource bundle.
	 * @param target      Target object.
	 * @param locale      Locale for enumeration value translations.
	 * @param enumType    Enumeration type to determine choices.
	 * @param <T>         Enumeration type.
	 *
	 * @return Choice field that was created.
	 *
	 * @see ResourceBundleTools#getChoices
	 */
	@NotNull
	public <T extends Enum<T>> FormChoice createChoice( @Nullable final ResourceBundle bundle, @NotNull final String resourceKey, @NotNull final FieldTarget target, @NotNull final Locale locale, @NotNull final Class<T> enumType )
	{
		final T[] constants = enumType.getEnumConstants();

		final List<String> optionValues = new ArrayList<String>( constants.length );
		final List<String> optionLabels = new ArrayList<String>( constants.length );

		for ( final T constant : constants )
		{
			String label = ResourceBundleTools.getString( bundle, resourceKey + '.' + constant.name(), null );
			if ( label == null )
			{
				label = ResourceBundleTools.getString( locale, constant );
			}

			optionValues.add( constant.name() );
			optionLabels.add( label );
		}

		return new FormChoice( target, optionValues, optionLabels );
	}

	/**
	 * Convenience method to quickly add a choice field to this container. The
	 * available value choices are defined by the given collection.
	 *
	 * @param bundle    Bundle containing the field label and value choices
	 *                  (key=name).
	 * @param target    Target object.
	 * @param optionMap Map option values to option labels.
	 *
	 * @return Choice field that was created.
	 *
	 * @see ResourceBundleTools#getChoices
	 */
	public FormChoice addChoice( @Nullable final ResourceBundle bundle, @NotNull final FieldTarget target, @NotNull final Map<String, String> optionMap )
	{
		return addChoice( bundle, target.getName(), target, optionMap );
	}

	/**
	 * Convenience method to quickly add a choice field to this container. The
	 * available value choices are defined by the given collection.
	 *
	 * @param bundle      Bundle containing the field label and value choices.
	 * @param resourceKey Key to use in resource bundle.
	 * @param target      Target object.
	 * @param optionMap   Map option values to option labels.
	 *
	 * @return Choice field that was created.
	 *
	 * @see ResourceBundleTools#getChoices
	 */
	public FormChoice addChoice( @Nullable final ResourceBundle bundle, @NotNull final String resourceKey, @NotNull final FieldTarget target, @NotNull final Map<String, String> optionMap )
	{
		final Collection<String> optionValues = new ArrayList<String>( optionMap.size() );
		final Collection<String> optionLabels = new ArrayList<String>( optionMap.size() );

		for ( final Map.Entry<String, String> entry : optionMap.entrySet() )
		{
			optionValues.add( entry.getKey() );
			optionLabels.add( entry.getValue() );
		}

		return addChoice( bundle, resourceKey, target, optionValues, optionLabels );
	}

	/**
	 * Convenience method to quickly add a choice field to this container. The
	 * available value choices are defined by the given collection.
	 *
	 * @param bundle       Bundle containing the field label and value choices
	 *                     (key=name).
	 * @param target       Target object.
	 * @param optionValues Available options for choice.
	 * @param optionLabels Descriptions of options for choice.
	 *
	 * @return Choice field that was created.
	 *
	 * @see ResourceBundleTools#getChoices
	 */
	public FormChoice addChoice( @Nullable final ResourceBundle bundle, @NotNull final FieldTarget target, @NotNull final Collection<String> optionValues, @NotNull final Collection<String> optionLabels )
	{
		return addChoice( bundle, target.getName(), target, optionValues, optionLabels );
	}

	/**
	 * Convenience method to quickly add a choice field to this container. The
	 * available value choices are defined by the given collection.
	 *
	 * @param bundle       Bundle containing the field label and value choices.
	 * @param resourceKey  Key to use in resource bundle.
	 * @param target       Target object.
	 * @param optionValues Available options for choice.
	 * @param optionLabels Descriptions of options for choice.
	 *
	 * @return Choice field that was created.
	 *
	 * @see ResourceBundleTools#getChoices
	 */
	public FormChoice addChoice( @Nullable final ResourceBundle bundle, @NotNull final String resourceKey, @NotNull final FieldTarget target, @NotNull final Collection<String> optionValues, @NotNull final Collection<String> optionLabels )
	{
		final FormChoice choice = new FormChoice( target, optionValues, optionLabels );
		addComponent( new FormLabel( ResourceBundleTools.getString( bundle, resourceKey, resourceKey ), choice ) );
		addComponent( choice );
		return choice;
	}

	/**
	 * Convenience method to quickly add a column to this container for
	 * table-based forms.
	 *
	 * @param bundle Bundle containing the field label (key=name).
	 * @param head   Column defines a header vs. a data column.
	 * @param text   Text content of column.
	 *
	 * @return {@link FormColumn} that was created.
	 */
	public FormColumn addColumn( final boolean head, @Nullable final ResourceBundle bundle, @Nullable final String text )
	{
		final FormColumn column = new FormColumn();
		column.setHead( head );
		if ( ( text != null ) && TextTools.isNonEmpty( text ) )
		{
			column.setText( ResourceBundleTools.getString( bundle, text, text ) );
		}
		addComponent( column );
		return column;
	}

	/**
	 * Convenience method to quickly add labeled static text to this container.
	 *
	 * @param label Label text.
	 * @param text  Static text.
	 *
	 * @return Text field that was created.
	 */
	public FormStaticText addStaticText( @NotNull final String label, @Nullable final String text )
	{
		final FormStaticText result = new FormStaticText( text );
		addComponent( new FormLabel( label ) );
		addComponent( result );
		return result;
	}

	/**
	 * Convenience method to quickly add a text field to this container.
	 *
	 * @param bundle    Bundle containing the field label (key=name).
	 * @param target    Target object.
	 * @param size      Visible size of field (-1 = unspecified).
	 * @param maxLength Maximum number of characters in field (-1 =
	 *                  unspecified).
	 *
	 * @return Text field that was created.
	 */
	public FormTextField addTextField( @Nullable final ResourceBundle bundle, @NotNull final FieldTarget target, final int size, final int maxLength )
	{
		return addTextField( bundle, target.getName(), target, size, maxLength );
	}

	/**
	 * Convenience method to quickly add a text field to this container.
	 *
	 * @param bundle      Bundle containing the field label.
	 * @param resourceKey Key to use in resource bundle.
	 * @param target      Target object.
	 * @param size        Visible size of field (-1 = unspecified).
	 * @param maxLength   Maximum number of characters in field (-1 =
	 *                    unspecified).
	 *
	 * @return Text field that was created.
	 */
	public FormTextField addTextField( @Nullable final ResourceBundle bundle, @NotNull final String resourceKey, @NotNull final FieldTarget target, final int size, final int maxLength )
	{
		final FormTextField field = new FormTextField( target, size, maxLength );
		addComponent( new FormLabel( ResourceBundleTools.getString( bundle, resourceKey, resourceKey ), field ) );
		addComponent( field );
		return field;
	}

	/**
	 * Convenience method to quickly add a text field to this container.
	 *
	 * @param bundle    Bundle containing the field label (key=name).
	 * @param target    Target object.
	 * @param size      Visible size of field (-1 = unspecified).
	 * @param maxLength Maximum number of characters in field (-1 =
	 *                  unspecified).
	 * @param value     Initial value to assign to text field.
	 *
	 * @return Text field that was created.
	 */
	public FormTextField addTextField( @Nullable final ResourceBundle bundle, @NotNull final FieldTarget target, final int size, final int maxLength, @Nullable final String value )
	{
		return addTextField( bundle, target.getName(), target, size, maxLength, value );
	}

	/**
	 * Convenience method to quickly add a text field to this container.
	 *
	 * @param bundle      Bundle containing the field label.
	 * @param resourceKey Key to use in resource bundle.
	 * @param target      Target object.
	 * @param size        Visible size of field (-1 = unspecified).
	 * @param maxLength   Maximum number of characters in field (-1 =
	 *                    unspecified).
	 * @param value       Initial value to assign to text field.
	 *
	 * @return Text field that was created.
	 */
	public FormTextField addTextField( @Nullable final ResourceBundle bundle, @NotNull final String resourceKey, @NotNull final FieldTarget target, final int size, final int maxLength, @Nullable final String value )
	{
		final FormTextField result = addTextField( bundle, resourceKey, target, size, maxLength );
		result.setValue( value );
		return result;
	}

	/**
	 * Convenience method to quickly add a text area to this container.
	 *
	 * @param bundle  Bundle containing the field label (key=name).
	 * @param target  Target object.
	 * @param rows    Number of rows (-1 = unspecified).
	 * @param columns Number of columns (-1 = unspecified).
	 *
	 * @return Text area that was created.
	 */
	public FormTextArea addTextArea( @Nullable final ResourceBundle bundle, @NotNull final FieldTarget target, final int rows, final int columns )
	{
		return addTextArea( bundle, target.getName(), target, rows, columns );
	}

	/**
	 * Convenience method to quickly add a text area to this container.
	 *
	 * @param bundle      Bundle containing the field label.
	 * @param resourceKey Key to use in resource bundle.
	 * @param target      Target object.
	 * @param rows        Number of rows (-1 = unspecified).
	 * @param columns     Number of columns (-1 = unspecified).
	 *
	 * @return Text area that was created.
	 */
	public FormTextArea addTextArea( @Nullable final ResourceBundle bundle, @NotNull final String resourceKey, @NotNull final FieldTarget target, final int rows, final int columns )
	{
		final FormTextArea area = new FormTextArea( target, rows, columns );
		addComponent( new FormLabel( ResourceBundleTools.getString( bundle, resourceKey, resourceKey ), area ) );
		addComponent( area );
		return area;
	}

	/**
	 * Convenience method to quickly add a number field to this container.
	 *
	 * @param bundle                Bundle containing the field label
	 *                              (key=name).
	 * @param target                Target object.
	 * @param allowNegative         Allow negative numbers.
	 * @param maximumFractionDigits Maximum number of fraction digits (0 &#8658;
	 *                              integer only).
	 *
	 * @return Number field that was created.
	 */
	public FormNumberField addNumberField( @Nullable final ResourceBundle bundle, @NotNull final FieldTarget target, final boolean allowNegative, final int maximumFractionDigits )
	{
		return addNumberField( bundle, target.getName(), target, allowNegative, maximumFractionDigits );
	}

	/**
	 * Convenience method to quickly add a number field to this container.
	 *
	 * @param bundle                Bundle containing the field label.
	 * @param resourceKey           Key to use in resource bundle.
	 * @param target                Target object.
	 * @param allowNegative         Allow negative numbers.
	 * @param maximumFractionDigits Maximum number of fraction digits (0 &#8658;
	 *                              integer only).
	 *
	 * @return Number field that was created.
	 */
	public FormNumberField addNumberField( @Nullable final ResourceBundle bundle, @NotNull final String resourceKey, @NotNull final FieldTarget target, final boolean allowNegative, final int maximumFractionDigits )
	{
		final FormNumberField field = new FormNumberField( target, maximumFractionDigits, allowNegative ? null : BigDecimal.ZERO, null );
		addComponent( new FormLabel( ResourceBundleTools.getString( bundle, resourceKey, resourceKey ), field ) );
		addComponent( field );
		return field;
	}

	/**
	 * Convenience method to add a hidden field (to define a variable). This
	 * field is not visible nor editable.
	 *
	 * @param target Target object of field.
	 */
	public void addHiddenField( @NotNull final FieldTarget target )
	{
		addComponent( new FormHiddenField( target ) );
	}

	/**
	 * Convenience method to add a hidden field (to define a variable). This
	 * field is not visible nor editable.
	 *
	 * @param name  Name of field.
	 * @param value Value to set the field to.
	 */
	public void addHiddenField( @NotNull final String name, @NotNull final String value )
	{
		addComponent( new FormHiddenField( new VariableFieldTarget( name ), value ) );
	}

	/**
	 * Remove a component from this container. The component's parent is updated
	 * accordingly.
	 *
	 * @param component Component to remove.
	 */
	public void removeComponent( @NotNull final FormComponent component )
	{
		//noinspection ObjectEquality
		if ( component.getParent() == this )
		{
			component.setParent( null );
		}

		_components.remove( component );
	}

	/**
	 * Get component with specified index from this container.
	 *
	 * @param index Index of component in container.
	 *
	 * @return The requested component.
	 *
	 * @throws IndexOutOfBoundsException if the index is out of range.
	 */
	public FormComponent getComponent( final int index )
	{
		return _components.get( index );
	}

	/**
	 * Get component with specified name from this container.
	 *
	 * @param name Name of component in container.
	 *
	 * @return The requested component; {@code null} if no matching component
	 * was found.
	 */
	@Nullable
	public FormComponent getComponent( @NotNull final String name )
	{
		FormComponent result = null;

		for ( final FormComponent component : _components )
		{
			if ( name.equals( component.getName() ) )
			{
				result = component;
				break;
			}
		}

		return result;
	}

	/**
	 * Get number of components in this container.
	 *
	 * @return Number of components in this container.
	 */
	public int getComponentCount()
	{
		return _components.size();
	}

	/**
	 * Get number of components in this container.
	 *
	 * @return Number of components in this container.
	 */
	public int getVisibleComponentCount()
	{
		int result = 0;

		for ( final FormComponent component : _components )
		{
			if ( component.isVisible() )
			{
				result++;
			}
		}

		return result;
	}

	/**
	 * Get index of component in this container.
	 *
	 * @param component Component to get index of.
	 *
	 * @return Index of the specified component in this container; {@code -1} if
	 * the component is not in this container.
	 */
	public int getComponentIndex( @NotNull final FormComponent component )
	{
		return _components.indexOf( component );
	}

	public List<FormComponent> getComponents()
	{
		return Collections.unmodifiableList( _components );
	}

	@SuppressWarnings( "ClassReferencesSubclass" )
	@Override
	protected void generate( @NotNull final String contextPath, @NotNull final Form form, @Nullable final HTMLTable table, @NotNull final IndentingJspWriter iw, @NotNull final HTMLFormFactory formFactory )
	throws IOException
	{
		if ( !_hideIfReadOnly || isEditable() )
		{
			for ( int i = 0; i < getComponentCount(); i++ )
			{
				final FormComponent component = getComponent( i );
				if ( component.isVisible() )
				{
					component.generate( contextPath, form, table, iw, formFactory );

					if ( component.isEditable() && ( component instanceof FormField ) )
					{
						final FormField field = (FormField)component;
						final String note = field.getDescription();
						if ( !TextTools.isEmpty( note ) )
						{
							if ( table != null )
							{
								table.newRow( iw );
								table.newColumn( iw );
								table.newColumn( iw );
							}

							iw.append( "<div class=\"fieldNote\">" );
							iw.append( note );
							iw.append( "</div>" );
						}
					}
				}
			}
		}
	}

	@NotNull
	@Override
	public SubmitStatus submitData( @NotNull final HttpServletRequest request )
	throws InvalidFormDataException
	{
		return submitDataToComponents( request );
	}

	/**
	 * Delegate for {@link #submitData} method to let all components in this
	 * container process data that was submitted by the specified request.
	 *
	 * @param request Request that was used to process the form.
	 *
	 * @return Combined {@link SubmitStatus} of components in this container.
	 *
	 * @throws InvalidFormDataException if there was problem with the submitted
	 * data.
	 */
	@NotNull
	protected SubmitStatus submitDataToComponents( @NotNull final HttpServletRequest request )
	throws InvalidFormDataException
	{
		SubmitStatus result = SubmitStatus.UNKNOWN;

		for ( int i = getComponentCount(); --i >= 0; )
		{
			final FormComponent component = getComponent( i );
			if ( component.isVisible() )
			{
				try
				{
					try
					{
						result = result.combineWith( component.submitData( request ) );
					}
					catch ( final NumberFormatException e )
					{
						//noinspection ThrowCaughtLocally
						throw new InvalidFormDataException( e.getMessage(), InvalidFormDataException.Type.INVALID_NUMBER, e );
					}
					catch ( final RuntimeException e )
					{
						//noinspection ThrowCaughtLocally
						throw new InvalidFormDataException( e.getMessage(), InvalidFormDataException.Type.INVALID_DATA, e );
					}
				}
				catch ( final InvalidFormDataException e )
				{
					if ( component.isEditable() && ( component instanceof FormField ) )
					{
						final FormField field = (FormField)component;
						field.setException( e );
						result = SubmitStatus.SUBMITTED_WITH_ERRORS;
					}
					else
					{
						throw e;
					}
				}
			}
		}

		if ( result != SubmitStatus.NOT_SUBMITTED )
		{
			for ( int i = 0; i < getComponentCount(); i++ )
			{
				final FormComponent component = getComponent( i );
				if ( component.isVisible() && component.isEditable() && ( component instanceof FormField ) )
				{
					final FormField field = (FormField)component;
					if ( field.getException() == null )
					{
						try
						{
							field.check();
						}
						catch ( final InvalidFormDataException e )
						{
							field.setException( e );
							result = SubmitStatus.SUBMITTED_WITH_ERRORS;
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Returns the component with the specified name. This component and all
	 * nested components are considered. Components names depend on the value of
	 * {@link #isQualifiedNameUsed()}.
	 *
	 * @param name Name to find.
	 *
	 * @return Found form component; {@code null} if not found.
	 */
	@Nullable
	public FormComponent getNestedComponent( @NotNull final String name )
	{
		FormComponent result = null;

		if ( name.equals( getName() ) )
		{
			result = this;
		}
		else
		{
			for ( final FormComponent component : _components )
			{
				if ( component instanceof FormContainer )
				{
					result = ( (FormContainer)component ).getNestedComponent( name );
					if ( result != null )
					{
						break;
					}
				}
				else if ( name.equals( component.getName() ) )
				{
					result = component;
					break;
				}
			}
		}

		return result;
	}

	@Override
	public void writeAsText( @NotNull final Appendable out, @NotNull final String indent )
	throws IOException
	{
		final String nextIndent = indent + "    ";
		for ( final FormComponent component : getComponents() )
		{
			component.writeAsText( out, nextIndent );
		}
	}
}
