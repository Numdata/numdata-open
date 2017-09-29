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
package com.numdata.oss.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import org.jetbrains.annotations.*;

/**
 * This class extends {@link StandardDialog} with functionality to create
 * wizards.
 *
 * A wizard normally guides the users through a complex task through a
 * step-by-step process. Each step is implemented as a {@link WizardPage}. The
 * {@code Wizard} class provides a GUI that controls these pages.
 *
 * @author Peter S. Heijnen
 * @see WizardPage
 */
public class Wizard
extends StandardDialog
{
	/**
	 * Action command associated with the 'Previous' button.
	 */
	public static final String PREVIOUS_ACTION = "previous";

	/**
	 * Action command associated with the 'Next' button.
	 */
	public static final String NEXT_ACTION = "next";

	/**
	 * Action command associated with the 'Finish' button.
	 */
	public static final String FINISH_ACTION = "finish";

	/**
	 * Preferred size of image panel (used to speed up wizard dialog layout).
	 */
	private static final Dimension PREFERRED_IMAGE_PANEL_SIZE = new Dimension( 154 + 8, 325 + 8 );

	/**
	 * Title component of page.
	 */
	private JLabel _title;

	/**
	 * Wizard image on the left side of the content pane.
	 */
	private VerticalBanner _imagePanel;

	/**
	 * This panel contains all pages.
	 */
	private CardPanel _cardPanel;

	/**
	 * Progress indicator of wizard.
	 */
	private WizardProgressPanel _progress = null;

	/**
	 * Flag to indicate that the {@link #doFinish()} method was called.
	 *
	 * @see #doFinish
	 */
	private boolean _finished;

	/**
	 * Construct dialog.
	 *
	 * @param owner  Parent component for this wizard.
	 * @param locale Locale to use for internationalized messages.
	 */
	public Wizard( final Window owner, final Locale locale )
	{
		super( owner, ModalityType.APPLICATION_MODAL, locale, OK_CANCEL_BUTTONS );
		initGUI();
		_finished = false;
	}

	/**
	 * Create main content with image on left side, progress indicator at
	 * bottom, and content in the center.
	 * <pre>
	 * +---------+-------------------+
	 * |         | Title             |
	 * |         | - - - - - - - - - |
	 * |         |                   |
	 * |         |                   |
	 * | [image] |                   |
	 * |         |                   |
	 * |         |                   |
	 * |         |                   |
	 * +---------+-------------------+
	 * |             (1) (2) (3) (4) |
	 * +-----------------------------+
	 * </pre>
	 */
	private void initGUI()
	{
		final JLabel title = new JLabel( " " )
		{
			{
				setFont( new Font( "SansSerif", Font.BOLD, 16 ) );
				setBorder( new EmptyBorder( 4, 4, 8, 4 ) );
			}

			@Override
			protected void paintComponent( final Graphics g )
			{
				final Graphics2D g2d = (Graphics2D)g;

				final RenderingHints oldHints = g2d.getRenderingHints();
				g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

				super.paintComponent( g );

				g2d.setRenderingHints( oldHints );
			}

			@Override
			protected void paintBorder( final Graphics g )
			{
				super.paintBorder( g );
				DrawTools.drawEtchHorizontal( g, 2, getHeight() - 5, getWidth() - 6 );
			}
		};

		final CardPanel cardPanel = new CardPanel()
		{
			@Override
			public Dimension getPreferredSize()
			{
				final Dimension titleSize = title.getPreferredSize();

				final Dimension result = super.getPreferredSize();
				result.height = Math.max( result.height, PREFERRED_IMAGE_PANEL_SIZE.height - titleSize.height );
				return result;
			}
		};

		final JPanel pagePanel = new JPanel( new BorderLayout() );
		pagePanel.add( title, BorderLayout.NORTH );
		pagePanel.add( cardPanel, BorderLayout.CENTER );

		final StandardContentPane contentPane = getStandardContentPane();
		final JPanel panel = (JPanel)contentPane.getContent();
		panel.setBorder( BorderFactory.createEmptyBorder() );
		panel.add( pagePanel, BorderLayout.CENTER );

		_title = title;
		_imagePanel = null;
		_cardPanel = cardPanel;

		final ResourceBundle res = _res;

		contentPane.addButton( res, PREVIOUS_ACTION, this );
		contentPane.addButton( res, NEXT_ACTION, this );
		contentPane.addButton( res, FINISH_ACTION, this );

		contentPane.setButtonState( OK_ACTION, StandardContentPane.BUTTON_DISABLED );
		contentPane.setButtonState( CANCEL_ACTION, StandardContentPane.BUTTON_ENABLED );
		contentPane.setButtonState( PREVIOUS_ACTION, StandardContentPane.BUTTON_HIDDEN );
		contentPane.setButtonState( NEXT_ACTION, StandardContentPane.BUTTON_HIDDEN );
		contentPane.setButtonState( FINISH_ACTION, StandardContentPane.BUTTON_HIDDEN );
	}

	@Override
	public void actionPerformed( final ActionEvent event )
	{
		final String command = event.getActionCommand();

		if ( PREVIOUS_ACTION.equals( command ) )
		{
			doPrevious();
		}
		else if ( NEXT_ACTION.equals( command ) )
		{
			doNext();
		}
		else if ( FINISH_ACTION.equals( command ) )
		{
			doFinish();
		}
		else
		{
			super.actionPerformed( event );
		}
	}

	/**
	 * Add page to wizard.
	 *
	 * @param page Page to add to wizard.
	 */
	public void addPage( final WizardPage page )
	{
		_cardPanel.addPage( page );

		if ( ( getPageCount() > 1 ) && ( _progress == null ) )
		{
			final WizardProgressPanel progress = new WizardProgressPanel( getLocale(), _cardPanel );

			final JComponent content = getContent();
			content.add( progress, BorderLayout.SOUTH );

			_progress = progress;
		}
	}

	/**
	 * Get page with specified index from wizard.
	 *
	 * @param index Index of page.
	 *
	 * @return {@link WizardPage} matching the index; {@code null} if no page
	 * exists with the specified index.
	 */
	@Nullable
	public WizardPage getPage( final int index )
	{
		final CardPanel cardPanel = _cardPanel;
		return ( ( index >= 0 ) && ( index < cardPanel.getPageCount() ) ) ? (WizardPage)cardPanel.getPage( index ) : null;
	}

	/**
	 * Get currently active wizard page.
	 *
	 * @return Currently active wizard page.
	 */
	public WizardPage getActivePage()
	{
		return (WizardPage)_cardPanel.getActivePage();
	}

	/**
	 * Get number of wizard pages.
	 *
	 * @return Number of pages.
	 */
	public int getPageCount()
	{
		return _cardPanel.getPageCount();
	}

	/**
	 * Set wizard image on the left side of the content pane.
	 *
	 * @param path Path to image ({@code null} => none).
	 *
	 * @see StandardContentPane#setImage(String)
	 */
	public void setImage( final String path )
	{
		VerticalBanner imagePanel = _imagePanel;
		if ( path != null )
		{
			if ( imagePanel == null )
			{
				imagePanel = new VerticalBanner();
				imagePanel.setPreferredSize( PREFERRED_IMAGE_PANEL_SIZE );
				imagePanel.setScaleMode( ImageTools.ScaleMode.CONTAIN );
				imagePanel.setVerticalAlignment( 1 );
				imagePanel.setBackground( Color.BLACK );
				_imagePanel = imagePanel;

				final Container contentPane = getContentPane();
				contentPane.add( imagePanel, BorderLayout.WEST );
			}

			imagePanel.setImage( path );
		}

		if ( imagePanel != null )
		{
			final boolean visible = ( path != null );
			if ( visible != imagePanel.isVisible() )
			{
				imagePanel.setVisible( visible );

				invalidate();
				final Container contentPane = getContentPane();
				contentPane.validate();
			}
		}
	}

	@Override
	public void reset()
	{
		super.reset();
		_finished = false;
	}

	@Override
	public void setVisible( final boolean visible )
	{
		if ( visible && !isVisible() )
		{
			throw new IllegalStateException( "please use doStart() instead" );
		}
	}

	/**
	 * Get flag that indicates if the {@link #doFinish()} method was called.
	 *
	 * This flag is reset when the dialog is (re)shown.
	 *
	 * @return {@code true} if the {@link #doFinish()} method was called; {@code
	 * false} otherwise.
	 *
	 * @see #doFinish
	 */
	public boolean isFinished()
	{
		return _finished;
	}

	/**
	 * Start the wizard. Default implementation jumps to first page.
	 */
	public void doStart()
	{
		if ( _cardPanel.getPageCount() == 0 )
		{
			_finished = true;
		}
		else
		{
			jumpTo( 0 );
		}
	}

	/**
	 * Jump to previous page.
	 */
	public void doPrevious()
	{
		final WizardPage page = getActivePage();
		if ( page != null )
		{
			page.doPrevious();
		}
	}

	/**
	 * Jump to next page.
	 */
	public void doNext()
	{
		final WizardPage page = getActivePage();
		if ( page != null )
		{
			page.doNext();
		}
	}

	/**
	 * Handle 'Ok' action.
	 *
	 * In a wizard, this action is equal to the {@link #doFinish} action.
	 */
	@Override
	protected void doOk()
	{
		doFinish();
	}

	/**
	 * Handle 'Finish' action.
	 *
	 * The default implementation finishes all pages non-interactively, sets the
	 * '{@code finished}' flag, and calls {@link #doClose}.
	 */
	public void doFinish()
	{
		//noinspection ObjectEquality
		for ( WizardPage page = getActivePage(), last = null; page != null && page != last; last = page, page = getActivePage() )
		{
			page.doFinish();
		}

		_finished = true;
		doClose();
	}

	/**
	 * Jump to page relative to current.
	 *
	 * @param position Relative position.
	 */
	public void jumpRelative( final int position )
	{
		if ( position > 0 )
		{
			for ( int i = _cardPanel.getActivePageIndex() + position; i < getPageCount(); i++ )
			{
				jumpTo( i );

				final WizardPage activePage = getActivePage();
				if ( !activePage.shouldSkip() )
				{
					break;
				}
			}

			final WizardPage activePage = getActivePage();
			if ( activePage.shouldSkip() )
			{
				_finished = true;
				doClose();
			}
		}
		else if ( position < 0 )
		{
			for ( int i = _cardPanel.getActivePageIndex() + position; i >= 0; i-- )
			{
				jumpTo( i );

				final WizardPage activePage = getActivePage();
				if ( !activePage.shouldSkip() )
				{
					break;
				}
			}
		}
	}

	/**
	 * Jump to the specified page.
	 *
	 * @param pageIndex Index of page to jump to.
	 */
	public void jumpTo( final int pageIndex )
	{
		if ( ( pageIndex >= 0 ) && ( pageIndex < _cardPanel.getPageCount() ) )
		{
			final CardPanel cardPanel = _cardPanel;
			final CardPage page = cardPanel.getPage( pageIndex );
			final boolean isFirst = ( pageIndex == 0 );
			final boolean isLast = ( pageIndex == ( getPageCount() - 1 ) );

			setTitle( page.getTitle() );

			final StandardContentPane contentPane = getStandardContentPane();
			contentPane.setButtonState( OK_ACTION, isFirst && isLast ? StandardContentPane.BUTTON_ENABLED : StandardContentPane.BUTTON_HIDDEN );
			contentPane.setButtonState( PREVIOUS_ACTION, isFirst ? isLast ? StandardContentPane.BUTTON_HIDDEN : StandardContentPane.BUTTON_DISABLED : StandardContentPane.BUTTON_ENABLED );
			contentPane.setButtonState( NEXT_ACTION, isLast ? isFirst ? StandardContentPane.BUTTON_HIDDEN : StandardContentPane.BUTTON_DISABLED : StandardContentPane.BUTTON_ENABLED );
			contentPane.setButtonState( FINISH_ACTION, isFirst && isLast ? StandardContentPane.BUTTON_HIDDEN : StandardContentPane.BUTTON_ENABLED );

			cardPanel.setActivePage( pageIndex );

			if ( _progress != null )
			{
				_progress.repaint();
			}

			WindowTools.packAndCenter( this, getWidth(), getHeight() );

			super.setVisible( true );

			final Component defaultButton = contentPane.getButton( isLast ? isFirst ? OK_ACTION : FINISH_ACTION : NEXT_ACTION );
			if ( ( defaultButton != null ) && defaultButton.isShowing() && defaultButton.isEnabled() )
			{
				defaultButton.requestFocus();
			}
		}
	}

	/**
	 * Sets the title of the wizard.
	 *
	 * @param title Title to be set.
	 */
	@Override
	public void setTitle( final String title )
	{
		super.setTitle( title );

		final JLabel titleLabel = _title;
		if ( titleLabel != null )
		{
			titleLabel.setText( title );
		}
	}

	/**
	 * Returns whether the given page is the last one in this wizard.
	 *
	 * @param page Page to be checked.
	 *
	 * @return {@code true} if {@code page} is the last page; {@code false}
	 * otherwise.
	 */
	public boolean isLastPage( final WizardPage page )
	{
		return ( page != null ) && ( page == getPage( getPageCount() - 1 ) );
	}
}
