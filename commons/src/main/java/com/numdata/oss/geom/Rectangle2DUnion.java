/*
 * Copyright (c) 2007-2017, Numdata BV, The Netherlands.
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
package com.numdata.oss.geom;

import java.util.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * This class represents a union of 2D rectangles.
 *
 * @author  Peter S. Heijnen
 */
public class Rectangle2DUnion
{
	/**
	 * Non-overlapping rectangles that make up the union.
	 */
	private final List<Rectangle> _rectangles = new ArrayList<Rectangle>();

	/**
	 * Clear union.
	 */
	public void clear()
	{
		_rectangles.clear();
	}

	/**
	 * Add rectangle to union.
	 *
	 * @param x      X coordinate of rectangle.
	 * @param y      Y coordinate of rectangle.
	 * @param width  Width of rectangle.
	 * @param height Height of rectangle.
	 */
	public void add( final double x, final double y, final double width, final double height )
	{
		add( new Rectangle( x, y, width, height ) );
	}

	/**
	 * Add rectangle to union.
	 *
	 * @param   rectangle   Rectangle to be added.
	 */
	public void add( @NotNull final Rectangle rectangle )
	{
		final List<Rectangle> rectangles = _rectangles;

		final List<Rectangle> toAdd = new LinkedList<Rectangle>();
		toAdd.add( rectangle );

		for ( int i = 0 ; i < toAdd.size() ; i++ )
		{
			Rectangle current = toAdd.get( i );

			final Set<Rectangle> toRemove = new HashSet<Rectangle>();

			for ( final Rectangle other : rectangles )
			{
				final double x1 = current.getX();
				final double y1 = current.getY();
				final double w1 = current.getWidth();
				final double h1 = current.getHeight();

				final double x2 = other.getX();
				final double y2 = other.getY();
				final double w2 = other.getWidth();
				final double h2 = other.getHeight();

				/*
				 * Allow for very small overlap to avoid an infinite loop due
				 * to rounding errors.
				 */
				final double epsilon = 1.0e-8;
				final boolean intersects = MathTools.significantlyGreaterThan( x2 + w2, x1, epsilon ) &&
				                           MathTools.significantlyGreaterThan( y2 + h2, y1, epsilon ) &&
				                           MathTools.significantlyLessThan( x2, x1 + w1, epsilon ) &&
				                           MathTools.significantlyLessThan( y2, y1 + h1, epsilon );

				if ( intersects )
				{
					final boolean insideX  = ( x1 >= x2 ) && ( x1 + w1 <= x2 + w2 );
					final boolean outsideX = ( x1 <= x2 ) && ( x1 + w1 >= x2 + w2 );
					final boolean insideY  = ( y1 >= y2 ) && ( y1 + h1 <= y2 + h2 );
					final boolean outsideY = ( y1 <= y2 ) && ( y1 + h1 >= y2 + h2 );

					if ( insideX && insideY )
					{
						// completely inside other
						current = null;
						break;
					}
					else if ( outsideX && outsideY )
					{
						// completely surrounds other
						toRemove.add( other );
					}
					else if ( outsideX && insideY )
					{
						// intersection
						current.setRect( x1 , y1 , x2 - x1 , h1 );
						toAdd.add( new Rectangle( x2 + w2 , y1 , ( x1 + w1 ) - ( x2 + w2 ) , h1 ) );
					}
					else if ( insideX && outsideY )
					{
						// intersection
						current.setRect( x1 , y1 , w1 , y2 - y1 );
						toAdd.add( new Rectangle( x1 , y2 + h2 , w1 , ( y1 + h1 ) - ( y2 + h2 ) ) );
					}
					else
					{
						final boolean left   = ( x1 < x2 ) && ( x1 + w1 < x2 + w2 );
						final boolean bottom = ( y1 < y2 ) && ( y1 + h1 < y2 + h2 );

						if ( left )
						{
							if ( outsideY )
							{
								// shrink other to prevent overlap
								other.setRect( x1 + w1 , y2 , ( x2 + w2 ) - ( x1 + w1 ) , h2 );
							}
							else
							{
								// shrink to prevent overlap
								current.setRect( x1 , y1 , x2 - x1 , h1 );

								// additional rectangle for corners
								if ( bottom )
								{
									toAdd.add( new Rectangle( x2 , y1 , x1 + w1 - x2 , y2 - y1 ) );
								}
								else if ( !insideY )
								{
									toAdd.add( new Rectangle( x2 , y2 + h2 , x1 + w1 - x2 , ( y1 + h1 ) - ( y2 + h2 ) ) );
								}
							}
						}
						else if ( outsideX )
						{
							// shrink other to prevent overlap
							if ( bottom )
							{
								other.setRect( x2 , y1 + h1 , w2 , ( y2 + h2 ) - ( y1 + h1 ) );
							}
							else
							{
								other.setRect( x2 , y2 , w2 , y1 - y2 );
							}
						}
						else if ( insideX )
						{
							// shrink to prevent overlap
							if ( bottom )
							{
								current.setRect( x1 , y1 , w1 , y2 - y1 );
							}
							else
							{
								current.setRect( x1 , y2 + h2 , w1 , ( y1 + h1 ) - ( y2 + h2 ) );
							}
						}
						else
						{
							if ( outsideY )
							{
								// shrink other to prevent overlap
								other.setRect( x2 , y2 , x1 - x2 , h2 );
							}
							else
							{
								// shrink to prevent overlap
								current.setRect( x2 + w2, y1, ( x1 + w1 ) - ( x2 + w2 ), h1 );

								// additional rectangle for corners
								if ( bottom )
								{
									toAdd.add( new Rectangle( x1 , y1 , x2 + w2 - x1 , y2 - y1 ) );
								}
								else if ( !insideY )
								{
									toAdd.add( new Rectangle( x1 , y2 + h2 , x2 + w2 - x1 , ( y1 + h1 ) - ( y2 + h2 ) ) );
								}
							}
						}
					}
				}
			}

			rectangles.removeAll( toRemove );
			if ( current != null )
			{
				rectangles.add( current );
			}
		}
	}

	/**
	 * Get surface area of union.
	 *
	 * @return  Surface area of union.
	  */
	public double getSurfaceArea()
	{
		double result = 0.0;

		for ( final Rectangle rectangle : _rectangles )
		{
			result += rectangle.getWidth() * rectangle.getHeight();
		}

		return result;
	}

	/**
	 * 2D rectangle.
	 */
	public static class Rectangle
	{
		/**
		 * X coordinate of rectangle.
		 */
		private double _x;

		/**
		 * Y coordinate of rectangle.
		 */
		private double _y;

		/**
		 * Width of rectangle.
		 */
		private double _width;

		/**
		 * Height of rectangle.
		 */
		private double _height;

		/**
		 * Create rectangle.
		 *
		 * @param x      X coordinate of rectangle.
		 * @param y      Y coordinate of rectangle.
		 * @param width  Width of rectangle.
		 * @param height Height of rectangle.
		 */
		public Rectangle( final double x, final double y, final double width, final double height )
		{
			_x = x;
			_y = y;
			_width = width;
			_height = height;
		}

		/**
		 * Set rectangle.
		 *
		 * @param x      X coordinate of rectangle.
		 * @param y      Y coordinate of rectangle.
		 * @param width  Width of rectangle.
		 * @param height Height of rectangle.
		 */
		public void setRect( final double x, final double y, final double width, final double height )
		{
			_x = x;
			_y = y;
			_width = width;
			_height = height;
		}

		public double getX()
		{
			return _x;
		}

		public double getY()
		{
			return _y;
		}

		public double getWidth()
		{
			return _width;
		}

		public double getHeight()
		{
			return _height;
		}
	}
}
