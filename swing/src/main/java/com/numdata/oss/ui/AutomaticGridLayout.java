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

/**
 * This class implements an automatic grid layout. It lays out a container's
 * components in a rectangular grid. The container is divided into equal-sized
 * rectangles, and one component is placed in each rectangle.
 *
 * It is similar to the {@link GridLayout}, except for the fact that the number
 * of columns and rows are dynamically adjusted to the available space in the
 * parent container.
 *
 * @author Peter S. Heijnen
 */
public final class AutomaticGridLayout
implements LayoutManager2
{
	/**
	 * Parent container of this layout.
	 */
	private final Container _parent;

	/**
	 * Horizontal gap (in pixels) which specifies the space between columns.
	 */
	private int _hgap;

	/**
	 * Vertical gap (in pixels) which specifies the space between rows.
	 */
	private int _vgap;

	/**
	 * Cached insets of parent container.
	 */
	private Insets _insets;

	/**
	 * Cached preferred size of a cell in pixels (excluding gaps).
	 */
	private Dimension _cellSize;

	/**
	 * Cached size of grid in columns and rows.
	 */
	private Dimension _gridSize;

	/**
	 * Creates an automatic grid layout with the both the horizontal and
	 * vertical gaps set to 1 unit.
	 *
	 * @param parent Parent container of this layout.
	 */
	public AutomaticGridLayout( final Container parent )
	{
		this( parent, 5, 5 );
	}

	/**
	 * Creates an automatic grid layout with the specified horizontal and
	 * vertical gaps. Horizontal gaps are placed between each of the columns.
	 * Vertical gaps are placed between each of the rows.
	 *
	 * @param parent Parent container of this layout.
	 * @param hgap   Horizontal gap.
	 * @param vgap   Vertical gap.
	 */
	public AutomaticGridLayout( final Container parent, final int hgap, final int vgap )
	{
		_parent = parent;
		_hgap = hgap;
		_vgap = vgap;

		_insets = null;
		_cellSize = null;
		_gridSize = null;
	}

	/**
	 * Gets the horizontal gap between components.
	 *
	 * @return Horizontal gap between components.
	 */
	public int getHgap()
	{
		return _hgap;
	}

	/**
	 * Sets the horizontal gap between components.
	 *
	 * @param hgap Horizontal gap between components.
	 */
	public void setHgap( final int hgap )
	{
		_hgap = hgap;
	}

	/**
	 * Gets the vertical gap between components.
	 *
	 * @return Vertical gap between components.
	 */
	public int getVgap()
	{
		return _vgap;
	}

	/**
	 * Sets the vertical gap between components.
	 *
	 * @param vgap Vertical gap between components.
	 */
	public void setVgap( final int vgap )
	{
		_vgap = vgap;
	}

	/**
	 * Get insets of parent container.
	 *
	 * This method caches the result, so please keep its contents intact.
	 *
	 * @return {@link Insets} of parent container.
	 */
	public Insets getInsets()
	{
		Insets result = _insets;

		if ( result == null )
		{
			result = _parent.getInsets();
			_insets = result;
		}

		return result;
	}

	/**
	 * Get preferred size of a cell in pixels (excluding gaps).
	 *
	 * This method caches the result, so please keep its contents intact.
	 *
	 * @return {@link Dimension} object with preferred size of a cell.
	 */
	public Dimension getCellSize()
	{
		Dimension result = _cellSize;

		if ( result == null )
		{
			int w = 0;
			int h = 0;

			final Container parent = _parent;
			synchronized ( parent.getTreeLock() )
			{
				final int componentCount = parent.getComponentCount();
				for ( int i = 0; i < componentCount; i++ )
				{
					final Component component = parent.getComponent( i );
					if ( component.isVisible() )
					{
						final Dimension dimension = component.getPreferredSize();

						w = Math.max( w, dimension.width );
						h = Math.max( h, dimension.height );
					}
				}
			}

			result = new Dimension( w, h );
			_cellSize = result;
		}

		return result;
	}

	/**
	 * Get size of grid in columns and rows.
	 *
	 * This method caches the result, so please keep its contents intact.
	 *
	 * @return {@link Dimension} object with size of grid in columns and rows.
	 */
	public Dimension getGridSize()
	{
		Dimension result = _gridSize;

		if ( result == null )
		{
			final int hgap = getHgap();

			final Container parent = _parent;
			synchronized ( parent.getTreeLock() )
			{
				final Insets insets = getInsets();
				final Dimension cellSize = getCellSize();
				final int availableWidth = parent.getWidth() - insets.left - insets.right;

				final int cols;
				final int rows;

				if ( availableWidth > cellSize.width )
				{
					cols = Math.max( 1, ( availableWidth + hgap ) / ( cellSize.width + hgap ) );
					rows = ( parent.getComponentCount() + cols - 1 ) / cols;
				}
				else
				{
					cols = Math.max( 1, (int)( Math.sqrt( (double)parent.getComponentCount() ) * 1.618034 ) );
					rows = ( parent.getComponentCount() + cols - 1 ) / cols;
				}

				result = new Dimension( cols, rows );
			}

			_gridSize = result;
		}

		return result;
	}

	@Override
	public float getLayoutAlignmentX( final Container target )
	{
		return 0.0f;
	}

	@Override
	public float getLayoutAlignmentY( final Container target )
	{
		return 0.0f;
	}

	@Override
	public void addLayoutComponent( final String name, final Component comp )
	{
	}

	@Override
	public void addLayoutComponent( final Component comp, final Object constraints )
	{
	}

	@Override
	public void removeLayoutComponent( final Component comp )
	{
	}

	@Override
	public void invalidateLayout( final Container target )
	{
		_insets = null;
		_cellSize = null;
		_gridSize = null;
	}

	@Override
	public Dimension preferredLayoutSize( final Container parent )
	{
		final int hgap = getHgap();
		final int vgap = getVgap();
		final Dimension gridSize = getGridSize();
		final Dimension cellSize = getCellSize();
		final Insets insets = getInsets();

		return new Dimension( insets.left + gridSize.width * cellSize.width + ( gridSize.width - 1 ) * hgap + insets.right,
		                      insets.top + gridSize.height * cellSize.height + ( gridSize.height - 1 ) * vgap + insets.bottom );
	}

	@Override
	public Dimension minimumLayoutSize( final Container parent )
	{
		return preferredLayoutSize( parent );
	}

	@Override
	public Dimension maximumLayoutSize( final Container parent )
	{
		return preferredLayoutSize( parent );
	}

	@Override
	public void layoutContainer( final Container parent )
	{
		synchronized ( parent.getTreeLock() )
		{
			final Dimension gridSize = getGridSize();
			final Dimension cellSize = getCellSize();
			final Insets insets = getInsets();
			final int hgap = getHgap();
			final int vgap = getVgap();

			int col = 0;
			int x = 0;
			int y = 0;

			final int componentCount = parent.getComponentCount();
			for ( int i = 0; i < componentCount; i++ )
			{
				final Component component = parent.getComponent( i );
				if ( component.isVisible() )
				{
					final Dimension dimension = component.getPreferredSize();
					final int cw = Math.min( cellSize.width, dimension.width );
					final int ch = Math.min( cellSize.height, dimension.height );

					component.setBounds( insets.left + x + ( cellSize.width - cw ) / 2, insets.top + y + ( cellSize.height - ch ) / 2, cw, ch );

					if ( ++col < gridSize.width )
					{
						x += cellSize.width + hgap;
					}
					else
					{
						col = 0;
						x = 0;
						y += cellSize.height + vgap;
					}
				}
			}
		}
	}

	public String toString()
	{
		return getClass().getName() + "[hgap=" + _hgap + ",vgap=" + _vgap + ']';
	}
}
