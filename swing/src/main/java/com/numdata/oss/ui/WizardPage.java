/*
 * Copyright (c) 2005-2017, Numdata BV, The Netherlands.
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
import javax.swing.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * This interface defines a page that is part of a {@link Wizard}.
 *
 * @author Peter S. Heijnen
 */
public abstract class WizardPage
extends JPanel
implements CardPage
{
	/**
	 * This is the Wizard that controls this page.
	 */
	private final Wizard _wizard;

	/**
	 * Resource bundle for this page.
	 */
	protected final ResourceBundle _res;

	/**
	 * Construct page.
	 *
	 * @param wizard Wizard that controls this page.
	 * @param res    Resource bundle for this page (optional).
	 * @param name   Name and resource key of page (optional).
	 * @param layout Layout manager for this page.
	 */
	protected WizardPage( final Wizard wizard, final ResourceBundle res, final String name, final LayoutManager layout )
	{
		super( layout );
		setLocale( wizard.getLocale() );
		setName( name );

		_wizard = wizard;
		_res = res;
	}

	/**
	 * Get the Wizard that controls this page.
	 *
	 * @return Wizard that controls this page.
	 */
	public Wizard getWizard()
	{
		return _wizard;
	}

	/**
	 * Get translated string to be used on this page (see {@link
	 * #getTranslatedValue} for details about the translation process).
	 *
	 * @param value Value to translate.
	 *
	 * @return Translated value (may be same as argument if no translation is
	 * available).
	 *
	 * @see #getTranslatedValue
	 */
	protected String getTranslatedString( final String value )
	{
		return (String)getTranslatedValue( value );
	}

	/**
	 * Get translated value to be used on this page. The value is translated
	 * using the wizard's resource bundle. If a resource named {@code
	 * {resourceKey}.{value}} is available, it will be used; otherwise, if a
	 * resource named {@code {resourceKey}.{value}} is available, it will be
	 * used; otherwise, the {@code value} is returned as-is.
	 *
	 * <strong>IMPORTANT</strong>: To make translations work, a resource bundle
	 * must be passed to the constructor; otherwise, all values will be returned
	 * as-is.
	 *
	 * @param value Value to translate.
	 *
	 * @return Translated value (may be same as argument if no translation is
	 * available).
	 */
	protected Object getTranslatedValue( final Object value )
	{
		Object result = value;

		final ResourceBundle res = _res;
		if ( ( res != null ) && ( value instanceof String ) )
		{
			final String s = (String)value;

			final String name = getName();
			if ( name != null )
			{
				result = ResourceBundleTools.getString( res, name + '.' + s, null );
				if ( result == null )
				{
					result = ResourceBundleTools.getString( res, s, s );
				}
			}
			else
			{
				result = ResourceBundleTools.getString( res, s, s );
			}
		}

		return result;
	}

	@Nullable
	@Override
	public String getTitle()
	{
		final String name = getName();
		return ( name != null ) ? ResourceBundleTools.getString( _res, name + ".title", null ) : null;
	}

	@Nullable
	@Override
	public String getDescription()
	{
		final String name = getName();
		return ( name != null ) ? ResourceBundleTools.getString( _res, name + ".description", null ) : null;
	}

	@Override
	public void pageAction( final CardPanel parent, final int action )
	{
		switch ( action )
		{
			case INIT_GUI:
				initGui();
				break;

			case SHOWN:
				shown();
				break;

			case SHOWING:
				showing();
				break;

			case HIDDEN:
				hidden();
				break;
		}
	}

	/**
	 * This method is called once before the page is {@link #SHOWN} for the
	 * first time. Its main purpose is initializing the GUI of the page if
	 * necessary.
	 *
	 * @see #INIT_GUI
	 * @see #shown
	 */
	protected void initGui()
	{
	}

	/**
	 * This method is called when the page is about to be shown, just before
	 * {@link #SHOWING} the page.
	 *
	 * @see #SHOWN
	 * @see #showing
	 */
	protected void shown()
	{
	}

	/**
	 * This method is called when the page has become visible. It may be {@link
	 * #HIDDEN} at some later time.
	 *
	 * This may be a suitable place to setup focus related things.
	 *
	 * @see #SHOWING
	 * @see #hidden
	 */
	protected void showing()
	{
	}

	/**
	 * This method is called just after the page has become invisible. The page
	 * may be {@link #SHOWN} again when is needed again.
	 *
	 * This is suitable place to perform cleanups and other page finalization
	 * tasks.
	 *
	 * @see #HIDDEN
	 * @see #shown
	 */
	protected void hidden()
	{
	}

	/**
	 * This method is called when the wizard should jump to the previous page.
	 * This may be overridden to change the default behavior (jumping to the
	 * previous available page).
	 */
	public void doPrevious()
	{
		final Wizard wizard = getWizard();
		wizard.jumpRelative( -1 );
	}

	/**
	 * This method is called when the wizard should jump to the next page. This
	 * may be overridden to change the default behavior (jumpting to the next
	 * available page).
	 */
	public void doNext()
	{
		final Wizard wizard = getWizard();
		wizard.jumpRelative( 1 );
	}

	/**
	 * This method is called when the wizard should finish. This may be
	 * overridden to change the default behavior (jumpting to next page until
	 * the wizard finished).
	 */
	public void doFinish()
	{
		doNext();
	}

	/**
	 * Returns whether the wizard page should be skipped.
	 *
	 * @return {@code true} if the page should be skipped.
	 */
	public boolean shouldSkip()
	{
		return false;
	}
}
