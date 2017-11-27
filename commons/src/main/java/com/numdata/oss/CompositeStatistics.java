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

import java.util.*;

import org.jetbrains.annotations.*;

/**
 * Keeps track of multiple labeled statistics.
 *
 * @author Gerrit Meinders
 */
public class CompositeStatistics
{
	/**
	 * Label of each statistic.
	 */
	private List<String> _labels;

	/**
	 * Statistics.
	 */
	private List<Stats> _stats;

	/**
	 * Constructs a new instance.
	 */
	public CompositeStatistics()
	{
		_labels = new ArrayList<String>();
		_stats = new ArrayList<Stats>();
	}

	/**
	 * Adds or updates the specified statistic.
	 *
	 * @param label Label.
	 * @param stats Value(s) to add.
	 */
	public void add( final String label, final Stats stats )
	{
		final int index = _labels.indexOf( label );
		if ( index == -1 )
		{
			_labels.add( label );
			_stats.add( stats );
		}
		else
		{
			_stats.get( index ).add( stats );
		}
	}

	/**
	 * Adds or updates the specified statistic.
	 *
	 * @param label Label.
	 * @param value Value to add.
	 */
	public void add( final String label, final int value )
	{
		add( label, new IntStats( value ) );
	}

	/**
	 * Adds or updates the specified statistic.
	 *
	 * @param label Label.
	 * @param value Value to add.
	 */
	public void add( final String label, final double value )
	{
		add( label, new DoubleStats( value ) );
	}

	/**
	 * Adds all statistics from the given composite statistics.
	 *
	 * @param other Statistics to add.
	 */
	public void add( @NotNull final CompositeStatistics other )
	{
		add( other, null );
	}

	/**
	 * Adds all statistics from the given composite statistics, adding the
	 * specified prefix to each label.
	 *
	 * @param other       Statistics to add.
	 * @param labelPrefix Prefix to add to labels.
	 */
	public void add( @NotNull final CompositeStatistics other, @Nullable final String labelPrefix )
	{
		for ( int i = 0; i < other._labels.size(); i++ )
		{
			final String label = other._labels.get( i );
			final Object stats = other._stats.get( i );
			final String prefixedLabel = labelPrefix == null ? label : labelPrefix + ": " + label;
			if ( stats instanceof IntStats )
			{
				add( prefixedLabel, (IntStats)stats );
			}
			else
			{
				add( prefixedLabel, (DoubleStats)stats );
			}
		}
	}

	/**
	 * Returns the statistic with the given label.
	 *
	 * @param label Label.
	 *
	 * @return Statistic.
	 */
	@Nullable
	public Stats get( @NotNull final String label )
	{
		final int index = _labels.indexOf( label );
		return index == -1 ? null : _stats.get( index );
	}

	/**
	 * Returns the labels of all statistics.
	 *
	 * @return Labels.
	 */
	public List<String> getLabels()
	{
		return Collections.unmodifiableList( _labels );
	}

	/**
	 * Returns the individual statistics.
	 *
	 * @return Statistics.
	 */
	public List<Object> getStats()
	{
		return Collections.<Object>unmodifiableList( _stats );
	}

	@Override
	public String toString()
	{
		final StringBuilder result = new StringBuilder();
		final Iterator<String> labelIterator = _labels.iterator();
		final Iterator<Stats> statsIterator = _stats.iterator();
		while ( labelIterator.hasNext() )
		{
			result.append( labelIterator.next() );
			result.append( ": " );
			result.append( statsIterator.next() );
			if ( labelIterator.hasNext() )
			{
				result.append( "\n" );
			}
		}
		return result.toString();
	}
}
