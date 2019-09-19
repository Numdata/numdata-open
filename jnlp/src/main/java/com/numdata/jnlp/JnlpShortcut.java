/*
 * Copyright (c) 2011-2017, Numdata BV, The Netherlands.
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
package com.numdata.jnlp;

import javax.xml.stream.*;

/**
 * Indicates an application's preferences for desktop integration.
 *
 * @author  Peter S. Heijnen
 */
public class JnlpShortcut
	extends JnlpElement
{
	/**
	 * If set, prefer to create a shortcut that will launch the application
	 * online; if not set, prefer to create a shortcut that will launch the
	 * application offline.
	 */
	private Boolean _online = null;

	/**
	 * Prefer putting a shortcut on the user's desktop.
	 */
	private Boolean _desktop = null;

	/**
	 * Prefer putting a menu item in the user's start menu.
	 */
	private boolean _menu = false;

	/**
	 * The preferred place for the menu item. It can be any string.
	 */
	private String _submenu = null;

	/**
	 * Get preference for creating a shortcut that will launch the application
	 * online as opposed to offline.
	 *
	 * @return  {@link Boolean#TRUE} if online launch is preferred;
	 *          {@link Boolean#FALSE} if offline launch is preferred;
	 *          {@code null} if default behavior is used.
	 */
	public Boolean getOnline()
	{
		return _online;
	}

	/**
	 * Set preference for creating a shortcut that will launch the application
	 * online as opposed to offline.
	 *
	 * @param   online  {@link Boolean#TRUE} to prefer online launch;
	 *                  {@link Boolean#FALSE} to prefer offline launch;
	 *                  {@code null} to use default behavior.
	 */
	public void setOnline( final Boolean online )
	{
		_online = online;
	}

	/**
	 * Get preference for putting a shortcut on the user's desktop.
	 *
	 * @return  {@link Boolean#TRUE} if a desktop shortcut is preferred;
	 *          {@link Boolean#FALSE} is this is not preferred;
	 *          {@code null} if undetermined.

	 */
	public Boolean getDesktop()
	{
		return _desktop;
	}

	/**
	 * Set preference for putting a shortcut on the user's desktop.
	 *
	 * @param   desktop     {@link Boolean#TRUE} if a desktop shortcut is
	 *                      preferred; {@link Boolean#FALSE} is this is not
	 *                      preferred; {@code null} if undetermined.
	 */
	public void setDesktop( final Boolean desktop )
	{
		_desktop = desktop;
	}

	/**
	 * Get preference for putting a menu item in the user's start menu.
	 *
	 * @return  {@code true} if a start menu item is preferred.
	 */
	public boolean isMenu()
	{
		return _menu;
	}

	/**
	 * Set preference for putting a menu item in the user's start menu.
	 *
	 * @param   menu    {@code true} if a start menu item is preferred.
	 */
	public void setMenu( final boolean menu )
	{
		_menu = menu;
	}

	/**
	 * Get preferred place for the menu item. It can be any string.
	 *
	 * @return  Preferred place for the menu item.
	 */
	public String getSubmenu()
	{
		return _submenu;
	}

	/**
	 * Set preferred place for the menu item. It can be any string.
	 *
	 * @param   subMenu     Preferred place for the menu item.
	 */
	public void setSubmenu( final String subMenu )
	{
		_submenu = subMenu;
	}

	@Override
	public void write( final XMLStreamWriter out )
		throws XMLStreamException
	{
		final boolean menu = isMenu();
		final Boolean online = getOnline();
		final Boolean desktop = getDesktop();
		final String submenu = getSubmenu();

		final boolean emptyShortcutElement = !menu && !desktop;

		if ( emptyShortcutElement )
		{
			out.writeEmptyElement( "shortcut" );
		}
		else
		{
			out.writeStartElement( "shortcut" );
		}

		writeOptionalAttribute( out, "online", online );

		writeOptionalBooleanElement( out, "desktop", desktop );

		if ( menu )
		{
			out.writeEmptyElement( "menu" );
			writeOptionalAttribute( out, "submenu", submenu );
		}

		if ( !emptyShortcutElement )
		{
			out.writeEndElement();
		}
	}

	@Override
	public void read( final XMLStreamReader in )
		throws XMLStreamException
	{
		throw new UnsupportedOperationException( "Not implemented yet." );
	}
}
