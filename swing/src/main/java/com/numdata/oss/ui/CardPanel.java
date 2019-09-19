/*
 * Copyright (c) 2004-2017, Numdata BV, The Netherlands.
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
import java.util.List;
import javax.swing.*;

/**
 * This component is a simple panel that uses a card layout to manage its
 * children. It allows easy switching between a set of pages (components).
 *
 * @author Peter S. Heijnen
 */
public class CardPanel
extends JPanel
{
	/**
	 * This is a collection of all managed pages.
	 */
	private final List _pages = new ArrayList();

	/**
	 * This is the collection of pages that have been shown.
	 */
	private final List _shown = new ArrayList();

	/**
	 * Index of currently active page.
	 */
	private int _activePage;

	/**
	 * This CardLayout manages the visibility of the pages.
	 */
	private final CardLayout _cardLayout;

	/**
	 * Construct a new CardPanel.
	 */
	public CardPanel()
	{
		setLayout( _cardLayout = new CardLayout() );
		setOpaque( true );
		_activePage = 0;
	}

	/**
	 * Prevent usage of other methods than {@link #addPage} to add components.
	 *
	 * @param comp        Component to be added
	 * @param constraints Object expressing layout constraints.
	 * @param index       Insert position in container's list (-1 = end).
	 */
	protected final void addImpl( final Component comp, final Object constraints, final int index )
	{
		throw new RuntimeException( "must use addPage()!" );
	}

	/**
	 * Add page to this panel.
	 *
	 * @param page Page to add to this panel.
	 */
	public final void addPage( final CardPage page )
	{
		//if ( !(page instanceof Component) )
		//throw new RuntimeException( "wizard page must be a component" );

		if ( page != null )
		{
			( (Component)page ).setVisible( false );
			super.addImpl( (Component)page, String.valueOf( _pages.size() ), -1 );
			_pages.add( page );
		}
	}

	/**
	 * Get currently active page.
	 *
	 * @return CardPage object; {@code null} if none of the panel's pages are
	 * active.
	 */
	public final CardPage getActivePage()
	{
		final List pages = _pages;
		final int pageIndex = _activePage;

		return ( ( pageIndex >= 0 ) && ( pageIndex < pages.size() ) ) ? (CardPage)pages.get( pageIndex ) : null;
	}

	/**
	 * Get index of currently active page.
	 *
	 * @return Index of currently active page.
	 */
	public final int getActivePageIndex()
	{
		return ( _activePage );
	}

	/**
	 * Get a specific page from the panel.
	 *
	 * @param index Page index.
	 *
	 * @return {@link CardPage} matching the index; {@code null} if no page
	 * exists with the specified index.
	 */
	public final CardPage getPage( final int index )
	{
		final CardPage result;

		if ( index < 0 || index >= _pages.size() )
		{
			result = null;
		}
		else
		{
			result = (CardPage)_pages.get( index );
		}

		return result;
	}

	/**
	 * Get number of pages.
	 *
	 * @return Number of pages.
	 */
	public final int getPageCount()
	{
		return ( _pages.size() );
	}

	/**
	 * Get title of currently active page.
	 *
	 * @return Title of currently active page.
	 */
	public final String getTitle()
	{
		final CardPage page = getActivePage();
		return ( ( page != null ) ? page.getTitle() : "???" );
	}

	/**
	 * Jump to the specified page.
	 *
	 * @param index Index of page to jump to.
	 */
	public final void setActivePage( final int index )
	{
		if ( ( index >= 0 ) && ( index < getPageCount() ) )
		{
			_activePage = index;
			showPage();
		}
	}

	/**
	 * Jump to the specified page.
	 *
	 * @param page CardPage to jump to.
	 */
	public final void setActivePage( final CardPage page )
	{
		if ( page != null )
		{
			setActivePage( _pages.indexOf( page ) );
		}
	}

	/**
	 * Make the current panel's page appear.
	 */
	protected void showPage()
	{
		/*
		 * Find out what target page to display.
		 */
		final CardPage page = getActivePage();
		if ( page != null )
		{
			/*
			 * Send "hidden" event to old page.
			 */
			synchronized ( getTreeLock() )
			{
				for ( int i = getComponentCount(); --i >= 0; )
				{
					final Component c = getComponent( i );
					if ( c.isVisible() )
					{
						if ( c instanceof CardPage )
						{
							( (CardPage)c ).pageAction( this, CardPage.HIDDEN );
						}
					}
				}
			}

			/*
			 * Show new page
			 */
			if ( !_shown.contains( page ) )
			{
				_shown.add( page );
				page.pageAction( this, CardPage.INIT_GUI );
			}
			page.pageAction( this, CardPage.SHOWN );
			_cardLayout.show( this, String.valueOf( getActivePageIndex() ) );

			///*
			//* Trying something here.....
			//*/
			//for ( int i = 0 ; i < getComponentCount(); i++ )
			//{
			//Component c = getComponent( i );
			//if ( c == getActivePage() && !c.isShowing() )
			//JOptionPanel.showMessage( soda.rootFrame , "CardPanel.showPage() : new Page is not made visible" , "Cardpanel bug" , JOptionPanel.WARNING_MESSAGE );
			//if ( c != getActivePage() && c.isShowing() )
			//JOptionPanel.showMessage( soda.rootFrame , "CardPanel.showPage() : old Page is not made invisible" , "Cardpanel bug" , JOptionPanel.WARNING_MESSAGE );
			//if ( c != getActivePage() ) c.setVisible( false );
			//}

			page.pageAction( this, CardPage.SHOWING );
		}
	}
}
