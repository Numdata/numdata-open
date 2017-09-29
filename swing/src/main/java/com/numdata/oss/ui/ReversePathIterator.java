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
import java.awt.geom.*;

/**
 * A path iterator that iterates a path in reverse direction.
 *
 * @author Peter S. Heijnen
 */
public class ReversePathIterator
implements PathIterator
{
	/**
	 * Winding rule.
	 */
	private final int _windingRule;

	/**
	 * Segment types.
	 */
	private final int[] _types;

	/**
	 * Segment coordinates.
	 */
	private final double[] _coordinates;

	/**
	 * The index into the segment types during iteration.
	 */
	private int _typeIndex;

	/**
	 * The index into the coordinates during iteration.
	 */
	private int _coordinateIndex;

	/**
	 * Create reverse path iterator for shape.
	 *
	 * @param shape Shape whose path to iterate.
	 */
	public ReversePathIterator( final Shape shape )
	{
		this( shape.getPathIterator( null ) );
	}

	/**
	 * Create reverse flattened path iterator for shape.
	 *
	 * @param shape    Shape whose path to iterate.
	 * @param flatness Maximum deviation from curved segments.
	 */
	public ReversePathIterator( final Shape shape, final double flatness )
	{
		this( shape.getPathIterator( null, flatness ) );
	}

	/**
	 * Create reverse transformed path iterator for shape.
	 *
	 * @param shape     Shape whose path to iterate.
	 * @param transform {@link AffineTransform} to apply (optional).
	 */
	public ReversePathIterator( final Shape shape, final AffineTransform transform )
	{
		this( shape.getPathIterator( transform ) );
	}

	/**
	 * Create reverse transformed flattened path iterator for shape.
	 *
	 * @param shape     Shape whose path to iterate.
	 * @param transform {@link AffineTransform} to apply (optional).
	 * @param flatness  Maximum deviation from curved segments.
	 */
	public ReversePathIterator( final Shape shape, final AffineTransform transform, final double flatness )
	{
		this( shape.getPathIterator( transform, flatness ) );
	}

	/**
	 * Create reverse path iterator for shape.
	 *
	 * @param shape       Shape whose path to iterate.
	 * @param windingRule Winding rule of newly created iterator
	 */
	public ReversePathIterator( final Shape shape, final int windingRule )
	{
		this( shape.getPathIterator( null ), windingRule );
	}

	/**
	 * Create reverse transformed flattened path iterator for shape.
	 *
	 * @param shape       Shape whose path to iterate.
	 * @param flatness    Maximum deviation from curved segments.
	 * @param windingRule Winding rule of newly created iterator
	 */
	public ReversePathIterator( final Shape shape, final double flatness, final int windingRule )
	{
		this( shape.getPathIterator( null, flatness ), windingRule );
	}

	/**
	 * Create reverse transformed path iterator for shape.
	 *
	 * @param shape       Shape whose path to iterate.
	 * @param transform   {@link AffineTransform} to apply (optional).
	 * @param windingRule Winding rule of newly created iterator
	 */
	public ReversePathIterator( final Shape shape, final AffineTransform transform, final int windingRule )
	{
		this( shape.getPathIterator( transform ), windingRule );
	}

	/**
	 * Create reverse transformed flattened path iterator for shape.
	 *
	 * @param shape       Shape whose path to iterate.
	 * @param transform   {@link AffineTransform} to apply (optional).
	 * @param flatness    Maximum deviation from curved segments.
	 * @param windingRule Winding rule of newly created iterator
	 */
	public ReversePathIterator( final Shape shape, final AffineTransform transform, final double flatness, final int windingRule )
	{
		this( shape.getPathIterator( transform, flatness ), windingRule );
	}

	/**
	 * Create an inverted path iterator from a standard one, keeping the winding
	 * rule.
	 *
	 * @param original original iterator
	 */
	public ReversePathIterator( final PathIterator original )
	{
		this( original, original.getWindingRule() );
	}

	/**
	 * Create an inverted path iterator from a standard one.
	 *
	 * @param original    original iterator
	 * @param windingRule winding rule of newly created iterator
	 */
	public ReversePathIterator( final PathIterator original, final int windingRule )
	{
		/*
		 * Copy data from original iterator into temporary buffers.
		 */
		int[] tempTypes = new int[ 16 ];
		int typeCount = 0;
		double[] tempCoordinates = new double[ 32 ];
		int coordCount = 0;

		final double[] segmentCoords = new double[ 6 ];

		for ( ; !original.isDone(); original.next() )
		{
			final int type = original.currentSegment( segmentCoords );
			final int segmentCoordCount = getCoordinateCountForSegmentType( type );

			if ( typeCount == tempTypes.length )
			{
				final int[] newTypes = new int[ 2 * typeCount ];
				System.arraycopy( tempTypes, 0, newTypes, 0, typeCount );
				tempTypes = newTypes;
			}

			tempTypes[ typeCount++ ] = type;

			if ( segmentCoordCount > 0 )
			{
				if ( ( coordCount + segmentCoordCount ) > tempCoordinates.length )
				{
					final double[] newCoords = new double[ tempCoordinates.length * 2 ];
					System.arraycopy( tempCoordinates, 0, newCoords, 0, coordCount );
					tempCoordinates = newCoords;
				}

				System.arraycopy( segmentCoords, 0, tempCoordinates, coordCount, segmentCoordCount );
				coordCount += segmentCoordCount;
			}
		}

		/*
		 * Reverse coordinates.
		 */
		final double[] coordinates = new double[ coordCount ];
		for ( int i = 0; i < coordCount; i += 2 )
		{
			coordinates[ i ] = tempCoordinates[ coordCount - i - 2 ];
			coordinates[ i + 1 ] = tempCoordinates[ coordCount - i - 1 ];
		}

		/*
		 * Reverse segment types
		 */
		final int[] types = new int[ typeCount ];

		if ( typeCount > 0 )
		{
			types[ 0 ] = SEG_MOVETO;

			int dst = 1;
			boolean closed = false;

			for ( int src = typeCount; --src > 0; )
			{
				final int type = tempTypes[ src ];
				switch ( type )
				{
					case SEG_MOVETO:
						if ( closed )
						{
							types[ dst++ ] = SEG_CLOSE;
							closed = false;
						}
						types[ dst++ ] = type;
						break;

					default:
						types[ dst++ ] = type;
						break;

					case SEG_CLOSE:
						closed = true;
						break;
				}
			}

			if ( closed )
			{
				types[ dst ] = SEG_CLOSE;
			}
		}

		_windingRule = windingRule;
		_types = types;
		_coordinates = coordinates;
		_typeIndex = 0;
		_coordinateIndex = 0;
	}

	public int currentSegment( final double[] coords )
	{
		final int segmentType = _types[ _typeIndex ];

		final int size = getCoordinateCountForSegmentType( segmentType );
		if ( size > 0 )
		{
			System.arraycopy( _coordinates, _coordinateIndex, coords, 0, size );
		}

		return segmentType;
	}

	public int currentSegment( final float[] coords )
	{
		final int segmentType = _types[ _typeIndex ];

		final int size = getCoordinateCountForSegmentType( segmentType );
		if ( size > 0 )
		{
			final double[] coordinates = _coordinates;
			int src = _coordinateIndex;
			int dst = 0;

			while ( dst < size )
			{
				coords[ dst++ ] = (float)coordinates[ src++ ];
			}
		}

		return segmentType;
	}

	public int getWindingRule()
	{
		return _windingRule;
	}

	public boolean isDone()
	{
		return ( _typeIndex >= _types.length );
	}

	public void next()
	{
		if ( isDone() )
		{
			throw new IllegalStateException( "done" );
		}

		_coordinateIndex += getCoordinateCountForSegmentType( _types[ _typeIndex++ ] );
	}

	/**
	 * Get the number of coordinates for segment type.
	 *
	 * @param segmentType Segment type.
	 *
	 * @return Number of coordinates (may be {@code 0}).
	 */
	private static int getCoordinateCountForSegmentType( final int segmentType )
	{
		final int result;

		switch ( segmentType )
		{
			case SEG_MOVETO:
			case SEG_LINETO:
				result = 2;
				break;
			case SEG_QUADTO:
				result = 4;
				break;
			case SEG_CUBICTO:
				result = 6;
				break;
			default:
				result = 0;  /* SEG_CLOSE */
		}

		return result;
	}

}
