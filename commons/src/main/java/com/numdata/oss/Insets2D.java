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
package com.numdata.oss;

import java.io.*;

/**
 * Represents the border of a rectangle, by specifying the width of the border
 * for each side. This is a 2D floating-point version of the {@code
 * java.awt.Insets} class. It is similar to how {@code java.awt.geom.Rectangle2D}
 * relates to {@code java.awt.Rectangle}.
 *
 * This class defines an abstract interface. Actual storage is left to
 * implementing classes.
 *
 * @author Peter S. Heijnen
 */
public abstract class Insets2D
{
	/**
	 * The {@code Float} class defines insets specified in float values.
	 */
	@SuppressWarnings( { "PublicField", "InstanceVariableNamingConvention", "FinalClass" } )
	public static final class Float
	extends Insets2D
	implements Serializable
	{
		@SuppressWarnings( "JavaDoc" )
		private static final long serialVersionUID = 5593946130563940422L;

		/**
		 * The top inset of this {@code Insets2D}.
		 */
		public float top;

		/**
		 * The left inset of this {@code Insets2D}.
		 */
		public float left;

		/**
		 * The bottom inset of this {@code Insets2D}.
		 */
		public float bottom;

		/**
		 * The right inset of this {@code Insets2D}.
		 */
		public float right;

		/**
		 * Constructs a new {@code Insets2D}, with all component set to 0.
		 */
		public Float()
		{
			top = 0.0f;
			left = 0.0f;
			bottom = 0.0f;
			right = 0.0f;
		}

		/**
		 * Constructs and initializes a {@code Insets2D} from the specified
		 * float values.
		 *
		 * @param top    Top inset value.
		 * @param left   Left inset value.
		 * @param bottom Bottom inset value.
		 * @param right  Right inset value.
		 */
		public Float( final float top, final float left, final float bottom, final float right )
		{
			this.top = top;
			this.left = left;
			this.bottom = bottom;
			this.right = right;
		}

		/**
		 * Constructs insets based on the given insets.
		 *
		 * @param other Specifies the insets to be set.
		 */
		public Float( final Insets2D other )
		{
			set( other );
		}

		@Override
		public double getTop()
		{
			return (double)top;
		}

		@Override
		public double getLeft()
		{
			return (double)left;
		}

		@Override
		public double getBottom()
		{
			return (double)bottom;
		}

		@Override
		public double getRight()
		{
			return (double)right;
		}

		/**
		 * Sets this {@code Insets2D} to the specified float values.
		 *
		 * @param top    Top inset value.
		 * @param left   Left inset value.
		 * @param bottom Bottom inset value.
		 * @param right  Right inset value.
		 */
		public void set( final float top, final float left, final float bottom, final float right )
		{
			this.top = top;
			this.left = left;
			this.bottom = bottom;
			this.right = right;
		}

		@Override
		public void set( final double top, final double left, final double bottom, final double right )
		{
			this.top = (float)top;
			this.left = (float)left;
			this.bottom = (float)bottom;
			this.right = (float)right;
		}

		@Override
		public void set( final Insets2D insets )
		{
			top = (float)insets.getTop();
			left = (float)insets.getLeft();
			bottom = (float)insets.getBottom();
			right = (float)insets.getRight();
		}

		public String toString()
		{
			return getClass().getName()
			       + "[top=" + top
			       + ",left=" + left
			       + ",bottom=" + bottom
			       + ",right=" + right
			       + ']';
		}
	}

	/**
	 * The {@code Double} class defines insets specified in final double
	 * values.
	 */
	@SuppressWarnings( { "PublicField", "InstanceVariableNamingConvention", "FinalClass" } )
	public static final class Double
	extends Insets2D
	implements Serializable
	{
		@SuppressWarnings( "JavaDoc" )
		private static final long serialVersionUID = 1688813696653138167L;

		/**
		 * The top inset of this {@code Insets2D}.
		 */
		public double top;

		/**
		 * The left inset of this {@code Insets2D}.
		 */
		public double left;

		/**
		 * The bottom inset of this {@code Insets2D}.
		 */
		public double bottom;

		/**
		 * The right inset of this {@code Insets2D}.
		 */
		public double right;

		/**
		 * Constructs a new {@code Insets2D}, with all component set to 0.
		 */
		public Double()
		{
			top = 0.0;
			left = 0.0;
			bottom = 0.0;
			right = 0.0;
		}

		/**
		 * Constructs and initializes an {@code Insets2D} from the specified
		 * {@code double} values.
		 *
		 * @param top    Top inset value.
		 * @param left   Left inset value.
		 * @param bottom Bottom inset value.
		 * @param right  Right inset value.
		 */
		public Double( final double top, final double left, final double bottom, final double right )
		{
			this.top = top;
			this.left = left;
			this.bottom = bottom;
			this.right = right;
		}

		/**
		 * Constructs insets based on the given insets.
		 *
		 * @param other Specifies the insets to be set.
		 */
		public Double( final Insets2D other )
		{
			set( other );
		}

		@Override
		public double getTop()
		{
			return top;
		}

		@Override
		public double getLeft()
		{
			return left;
		}

		@Override
		public double getBottom()
		{
			return bottom;
		}

		@Override
		public double getRight()
		{
			return right;
		}

		/**
		 * Sets this {@code Insets2D} to the specified double values.
		 *
		 * @param top    Top inset value.
		 * @param left   Left inset value.
		 * @param bottom Bottom inset value.
		 * @param right  Right inset value.
		 */
		@Override
		public void set( final double top, final double left, final double bottom, final double right )
		{
			this.top = top;
			this.left = left;
			this.bottom = bottom;
			this.right = right;
		}

		@Override
		public void set( final Insets2D insets )
		{
			top = insets.getTop();
			left = insets.getLeft();
			bottom = insets.getBottom();
			right = insets.getRight();
		}

		public String toString()
		{
			return getClass().getName()
			       + "[top=" + top
			       + ",left=" + left
			       + ",bottom=" + bottom
			       + ",right=" + right
			       + ']';
		}
	}

	/**
	 * This is an abstract class that cannot be instantiated directly.
	 * Type-specific implementation subclasses are available for instantiation
	 * and provide a number of formats for storing the information necessary to
	 * satisfy the various accessor methods below.
	 *
	 * @see Insets2D.Float
	 * @see Insets2D.Double
	 * @see java.awt.Insets
	 */
	protected Insets2D()
	{
	}

	/**
	 * Get top inset value of this {@code Insets2D}.
	 *
	 * @return Top inset value.
	 */
	public abstract double getTop();

	/**
	 * Get left inset value of this {@code Insets2D}.
	 *
	 * @return Left inset value.
	 */
	public abstract double getLeft();

	/**
	 * Get bottom inset value of this {@code Insets2D}.
	 *
	 * @return Bottom inset value.
	 */
	public abstract double getBottom();

	/**
	 * Get right inset value of this {@code Insets2D}.
	 *
	 * @return Right inset value.
	 */
	public abstract double getRight();

	/**
	 * Set {@code Insets2D} values from the specified {@code Insets2D} object.
	 *
	 * @param insets Insets to set.
	 */
	public abstract void set( Insets2D insets );

	/**
	 * Set {@code Insets2D} values from the specified double values.
	 *
	 * @param top    Top inset value.
	 * @param left   Left inset value.
	 * @param bottom Bottom inset value.
	 * @param right  Right inset value.
	 */
	public abstract void set( final double top, final double left, final double bottom, final double right );

	public int hashCode()
	{
		final long bits = java.lang.Double.doubleToLongBits( getLeft() )
		                  + java.lang.Double.doubleToLongBits( getTop() ) * 37L
		                  + java.lang.Double.doubleToLongBits( getRight() ) * 43L
		                  + java.lang.Double.doubleToLongBits( getBottom() ) * 47L;

		return ( ( (int)bits ) ^ ( (int)( bits >> 32 ) ) );
	}

	public boolean equals( final Object obj )
	{
		final boolean result;

		if ( obj == this )
		{
			result = true;
		}
		else if ( obj instanceof Insets2D )
		{
			final Insets2D other = (Insets2D)obj;

			result = ( getLeft() == other.getLeft() )
			         && ( getTop() == other.getTop() )
			         && ( getRight() == other.getRight() )
			         && ( getBottom() == other.getBottom() );
		}
		else
		{
			result = false;
		}

		return result;
	}
}
