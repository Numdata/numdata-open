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

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.lang.reflect.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.synth.*;
import javax.swing.table.*;

import com.numdata.oss.ensemble.*;
import org.jetbrains.annotations.*;

/**
 * This class provides utility methods related to Swing-based user-interfaces.
 *
 * @author Peter S. Heijnen
 */
public class SwingTools
{
	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private SwingTools()
	{
	}

	/**
	 * Call the specified {@link Callable} on the EDT. If the current thread is the
	 * EDT, the {@link Callable} will be invoked directly.
	 *
	 * @param callable Task to execute that returns a result.
	 * @param <T>      result value type.
	 *
	 * @return Result value.
	 */
	public static <T> T callOnEDT( final Callable<T> callable )
	{
		final T result;

		if ( SwingUtilities.isEventDispatchThread() )
		{
			try
			{
				result = callable.call();
			}
			catch ( RuntimeException e )
			{
				throw e;
			}
			catch ( Exception e )
			{
				throw new RuntimeException( e );
			}
		}
		else
		{
			final BasicSolo<T> resultContainer = new BasicSolo<T>( null );
			final BasicSolo<RuntimeException> execeptionContainer = new BasicSolo<RuntimeException>( null );

			try
			{
				SwingUtilities.invokeAndWait( new Runnable()
				{
					public void run()
					{
						try
						{
							resultContainer.setValue( callable.call() );
						}
						catch ( RuntimeException e )
						{
							execeptionContainer.setValue( e );
						}
						catch ( Exception e )
						{
							execeptionContainer.setValue( new RuntimeException( e ) );
						}
					}
				} );
			}
			catch ( InvocationTargetException e )
			{
				e.printStackTrace();
			}
			catch ( InterruptedException e )
			{
				e.printStackTrace();
			}

			final RuntimeException runtimeException = execeptionContainer.getValue();
			if ( runtimeException != null )
			{
				throw runtimeException;
			}

			result = resultContainer.getValue();
		}

		return result;
	}

	/**
	 * Run the specified {@link Runnable} on the EDT. If the current thread is the
	 * EDT, the {@link Runnable} will be invoked directly.
	 *
	 * @param runnable Task to execute that returns a result.
	 */
	public static void runOnEDT( final Runnable runnable )
	{
		if ( SwingUtilities.isEventDispatchThread() )
		{
			runnable.run();
		}
		else
		{
			final BasicSolo<RuntimeException> execeptionContainer = new BasicSolo<RuntimeException>( null );

			try
			{
				SwingUtilities.invokeAndWait( new Runnable()
				{
					public void run()
					{
						try
						{
							runnable.run();
						}
						catch ( RuntimeException e )
						{
							execeptionContainer.setValue( e );
						}
						catch ( Exception e )
						{
							execeptionContainer.setValue( new RuntimeException( e ) );
						}
					}
				} );
			}
			catch ( Exception e )
			{
				final Throwable cause = e.getCause();
				if ( cause instanceof RuntimeException )
				{
					throw (RuntimeException)cause;
				}
				else if ( cause instanceof Error )
				{
					throw (Error)cause;
				}
				else
				{
					throw new RuntimeException( cause );
				}
			}

			final RuntimeException runtimeException = execeptionContainer.getValue();
			if ( runtimeException != null )
			{
				throw runtimeException;
			}
		}
	}

	/**
	 * Create a {@link JTable} that has {@link JList}-like behavior.
	 *
	 * @param tableModel Table model to use.
	 *
	 * @return {@link JTable}.
	 */
	@NotNull
	public static JTable createListLikeTable( @Nullable final TableModel tableModel )
	{
		final JTable table = new JTable( tableModel )
		{
			@Override
			public Component prepareRenderer( final TableCellRenderer renderer, final int row, final int column )
			{
				final Object value = getValueAt( row, column );
				final boolean isSelected = !isPaintingForPrint() && isCellSelected( row, column );
				return renderer.getTableCellRendererComponent( this, value, isSelected, false, row, column );
			}

			@Override
			public boolean getScrollableTracksViewportWidth()
			{
				final Container parent = getParent();
				return ( getAutoResizeMode() != AUTO_RESIZE_OFF ) && ( parent instanceof JViewport ) && ( parent.getWidth() > getPreferredSize().width );
			}
		};

		/*
		 * Set row selection.
		 */
		table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		table.setCellSelectionEnabled( false );
		table.setRowSelectionAllowed( true );

		/*
		 * Make [Home] and [End] select the first/last row instead of column.
		 */
		final InputMap inputMap = table.getInputMap();
		inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_HOME, 0 ), "selectFirstRow" );
		inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_END, 0 ), "selectLastRow" );

		return table;
	}

	/**
	 * Set JTable column widths with only the last column resizing.
	 *
	 * @param table JTable whose column widths to set.
	 */
	public static void setAutoResizeLastColumn( final JTable table )
	{
		table.setAutoResizeMode( JTable.AUTO_RESIZE_LAST_COLUMN );

		for ( int columnIndex = 0; columnIndex < table.getColumnCount() - 1; columnIndex++ )
		{
			setPreferredColumnWidth( table, columnIndex );
		}
	}

	/**
	 * Set column of table to its preferred width.
	 *
	 * @param table       Table to set column width of.
	 * @param columnIndex Index of column to set width of.
	 */
	public static void setPreferredColumnWidth( final JTable table, final int columnIndex )
	{
		final int preferredWidth = getPreferredColumnWidth( table, columnIndex );
		if ( preferredWidth > 0 )
		{
			final TableColumnModel columnModel = table.getColumnModel();
			final TableColumn tableColumn = columnModel.getColumn( columnIndex );
			tableColumn.setPreferredWidth( preferredWidth );
			tableColumn.setMinWidth( preferredWidth );
			tableColumn.setMaxWidth( preferredWidth );
		}
	}

	/**
	 * Get preferred width of table column.
	 *
	 * @param table       Table to get column width from.
	 * @param columnIndex Column index.
	 *
	 * @return Preferred width of table column; -1 if column has no preferred
	 *         width.
	 */
	public static int getPreferredColumnWidth( final JTable table, final int columnIndex )
	{
		final TableModel tableModel = table.getModel();
		final TableColumnModel columnModel = table.getColumnModel();
		final TableColumn tableColumn = columnModel.getColumn( columnIndex );

		final int columnSpacing = 2 * table.getIntercellSpacing().width;

		TableCellRenderer renderer = tableColumn.getHeaderRenderer();
		if ( renderer == null )
		{
			final JTableHeader tableHeader = table.getTableHeader();
			if ( tableHeader != null )
			{
				renderer = tableHeader.getDefaultRenderer();
			}
			else
			{
				renderer = table.getCellRenderer( 0, columnIndex );
			}
		}

		Component component = renderer.getTableCellRendererComponent( table, tableColumn.getHeaderValue(), false, false, -1, columnIndex );
		final int headerPadding = 8;
		int result = component.getPreferredSize().width + columnSpacing + headerPadding;

		final int rowCount = tableModel.getRowCount();
		if ( rowCount > 0 )
		{
			final int modelColumnIndex = tableColumn.getModelIndex();

			renderer = tableColumn.getCellRenderer();
			if ( renderer == null )
			{
				renderer = table.getDefaultRenderer( table.getColumnClass( modelColumnIndex ) );
			}

			for ( int rowIndex = 0; rowIndex < Math.min( rowCount, 100 ); rowIndex++ )
			{
				component = renderer.getTableCellRendererComponent( table, table.getValueAt( rowIndex, columnIndex ), false, false, rowIndex, columnIndex );
				result = Math.max( result, component.getPreferredSize().width + columnSpacing );
			}
		}

		return result;
	}

	/**
	 * Creates an empty cursor, i.e. a cursor that is completely invisible.
	 *
	 * @return Empty cursor.
	 */
	public static Cursor createEmptyCursor()
	{
		final BufferedImage image = new BufferedImage( 1, 1, BufferedImage.TYPE_INT_ARGB );
		image.setRGB( 0, 0, 0 );
		final Toolkit toolkit = Toolkit.getDefaultToolkit();
		return toolkit.createCustomCursor( image, new Point(), "empty" );
	}

	/**
	 * Create {@link JButton} which only displays an icon and no other
	 * decorations.
	 *
	 * @param icon Icon to show.
	 *
	 * @return {@link JButton} that was created.
	 */
	public static JButton createIconButton( @NotNull final Icon icon )
	{
		final JButton button = new JButton();
		makeIconButton( button, icon );
		return button;
	}

	/**
	 * Set {@link JButton} properties so that it only displays an icon and no other
	 * decorations.
	 *
	 * @param button {@link JButton} to change into an icon-only button.
	 * @param icon   Icon to show.
	 */
	public static void makeIconButton( @NotNull final JButton button, @NotNull final Icon icon )
	{
		button.setIcon( icon );
		button.setBorder( BorderFactory.createEmptyBorder( 4, 4, 4, 4 ) );
		button.setFocusPainted( false );
		button.setContentAreaFilled( false );
		button.setPressedIcon( new Icon()
		{
			public void paintIcon( final Component component, final Graphics g, final int x, final int y )
			{
				icon.paintIcon( component, g, x + 1, y + 1 );
			}

			public int getIconWidth()
			{
				return icon.getIconWidth();
			}

			public int getIconHeight()
			{
				return icon.getIconHeight();
			}
		} );
	}

	/**
	 * Create {@link JSplitPane} with invisible controls. This should help keeping
	 * the user interface clean.
	 *
	 * @param leftComponent  Component on left side.
	 * @param rightComponent Component on right side.
	 *
	 * @return {@link JSplitPane} that was created.
	 */
	public static JSplitPane createInvisibleSplitPane( final JComponent leftComponent, final JComponent rightComponent )
	{
		final JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, leftComponent, rightComponent );
		makeSplitPaneControlsInvisible( splitPane );
		return splitPane;
	}

	/**
	 * Make {@link JSplitPane} controls invisible. This should help keeping the
	 * user interface clean.
	 *
	 * @param splitPane Split pane whose controls to make invisible.
	 */
	public static void makeSplitPaneControlsInvisible( final JSplitPane splitPane )
	{
		makeSplitPaneControlsInvisible( splitPane, 4, 8 );
	}

	/**
	 * Make {@link JSplitPane} controls invisible. This should help keeping the
	 * user interface clean.
	 *
	 * @param splitPane   Split pane whose controls to make invisible.
	 * @param borderSize  Size of empty border around split pane (e.g. 4).
	 * @param dividerSize Size of split pane divider(e.g. 8).
	 */
	public static void makeSplitPaneControlsInvisible( final JSplitPane splitPane, final int borderSize, final int dividerSize )
	{
		splitPane.setBorder( BorderFactory.createEmptyBorder( borderSize, borderSize, borderSize, borderSize ) );
		splitPane.setDividerSize( dividerSize );
		splitPane.setUI( new InvisibleSplitPaneUI() );
	}

	/**
	 * Creates a label-like component that uses a {@link JTextArea} to simulate a
	 * wrappable label.
	 *
	 * Note that you may consider using a {@link JLabel} with HTML-content in other
	 * situations. This requires less trickery, but uses other wrapping/layout
	 * rules.
	 *
	 * @param text Label text content.
	 *
	 * @return {@link JTextArea} with label like appearance and word wrapping.
	 */
	public static JComponent createWrappableLabel( final String text )
	{
		return createWrappableLabel( text, 50 );
	}

	/**
	 * Creates a label-like component that uses a {@link JTextArea} to simulate a
	 * wrappable label.
	 *
	 * Note that you may consider using a {@link JLabel} with HTML-content in other
	 * situations. This requires less trickery, but uses other wrapping/layout
	 * rules.
	 *
	 * @param text           Label text content.
	 * @param preferredWidth Maximum preferred width of label; 0 to use default
	 *                       preferred width of component.
	 *
	 * @return {@link JTextArea} with label like appearance and word wrapping.
	 */
	public static JComponent createWrappableLabel( final String text, final int preferredWidth )
	{
		final JTextArea result;

		if ( preferredWidth > 0 )
		{
			result = new JTextArea( text )
			{
				@Override
				public Dimension getPreferredSize()
				{
					final Dimension result = super.getPreferredSize();
					if ( result.width > preferredWidth )
					{
						result.width = preferredWidth;
					}
					return result;
				}
			};
		}
		else
		{
			result = new JTextArea( text );
		}

		result.setEditable( false );
		result.setLineWrap( true );
		result.setWrapStyleWord( true );
		result.setFocusable( false );
		result.setBorder( BorderFactory.createEmptyBorder() );
		result.setFont( (Font)UIManager.get( "Label.font" ) );
		result.setForeground( (Color)UIManager.get( "Label.foreground" ) );
		makeTransparent( result, result.getUI() );

		return result;
	}

	/**
	 * Make Swing component with given UI transparent.
	 *
	 * Normally, simply calling {@link JComponent#setOpaque} with {@code false} is
	 * enough, but for Nimbus/Synth PLAF we need some extra trickery.
	 *
	 * @param component Component to make transparent.
	 * @param ui        Component UI that is used.
	 *
	 * @see <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug%5Fid=6687960">Bug
	 *      6687960: Background of component invisible with Nimbus</a>
	 */
	public static void makeTransparent( final JComponent component, final ComponentUI ui )
	{
		component.setOpaque( false );

		if ( ( ui != null ) && UIManager.getLookAndFeel() instanceof SynthLookAndFeel )
		{
			// Special handling to make Synth/Nimbus-component transparent:
			//
			// Setting the background to null does not work with SynthUI's because such UI's would then revert to the default background painter,
			// which is called regardless of the component's opacity setting (other PLAF's do honor this setting and will become transparent).
			//
			// We actually set a background color with alpha set 0 (fully transparent) to work around this.
			component.setBackground( new Color( 0, 0, 0, 0 ) );
		}
		else
		{
			component.setBackground( null );
		}
	}

	/**
	 * Request focus on the given component when it is shown. This is a delayed
	 * call to {@link Component#requestFocusInWindow()}.
	 *
	 * @param component Component to request focus on.
	 */
	public static void requestFocusWhenShown( final JComponent component )
	{
		component.addHierarchyListener( new RequestFocusListener() );
	}

	/**
	 * Re-validate the given component hierarchy. After invalidating the given
	 * component hierarchy, this triggers validation of the component's suitable
	 * 'root' container (some well-known containers are defines as validate root).
	 *
	 * Note that Java 7 has a {@link Component#revalidate()} method that does
	 * this.
	 *
	 * @param component Component to re-validate.
	 */
	public static void revalidate( final Component component )
	{
		component.invalidate();

		if ( component instanceof JComponent )
		{
			final JComponent jComponent = (JComponent)component;
			RepaintManager.currentManager( jComponent ).addInvalidComponent( jComponent );
		}
		else
		{
			Component root = component;
			while ( !( root instanceof Window ) &&
			        !( root instanceof JRootPane ) &&
			        !( root instanceof JScrollPane ) &&
			        !( root instanceof JSplitPane ) &&
			        !( root instanceof Applet ) )
			{
				final Container parent = root.getParent();
				if ( parent == null )
				{
					break;
				}
				root = parent;
			}

			root.validate();
		}
	}

	/**
	 * A {@link HierarchyListener} that requests focus on a component when it is
	 * shown.
	 */
	public static class RequestFocusListener
	implements HierarchyListener
	{
		public void hierarchyChanged( final HierarchyEvent hierarchyEvent )
		{
			final JComponent component = (JComponent)hierarchyEvent.getSource();

			if ( ( ( (long)HierarchyEvent.SHOWING_CHANGED & hierarchyEvent.getChangeFlags() ) != 0L ) && component.isShowing() )
			{
				component.removeHierarchyListener( this );
				component.requestFocusInWindow();

				/* on Linux the 'requestFocusInWindow()' does not seem to work, so we try again after a 100ms delay */
				final Thread delay = new Thread( new Runnable()
				{
					public void run()
					{
						try
						{
							Thread.sleep( 100L );

							SwingUtilities.invokeLater( new Runnable()
							{
								public void run()
								{
									component.requestFocusInWindow();
								}
							} );
						}
						catch ( InterruptedException e )
						{
							e.printStackTrace();
						}
					}
				} );
				delay.start();
			}
		}
	}

	/**
	 * {@link BasicSplitPaneUI} used by {@link SwingTools#createInvisibleSplitPane}.
	 */
	private static class InvisibleSplitPaneUI
	extends BasicSplitPaneUI
	{
		@Override
		public BasicSplitPaneDivider createDefaultDivider()
		{
			return new InvisibleSplitPaneDivider( this );
		}
	}

	/**
	 * {@link BasicSplitPaneDivider} used by {@link InvisibleSplitPaneUI}.
	 */
	private static class InvisibleSplitPaneDivider
	extends BasicSplitPaneDivider
	{
		/**
		 * Construct divier.
		 *
		 * @param ui {@link BasicSplitPaneUI}.
		 */
		private InvisibleSplitPaneDivider( final BasicSplitPaneUI ui )
		{
			super( ui );
		}

		@Override
		public void paint( final Graphics g )
		{
		}
	}
}
