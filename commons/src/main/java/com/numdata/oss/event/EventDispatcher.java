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
package com.numdata.oss.event;

import java.util.*;

import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * This class and its supporting interfaces provides a simple filterable event
 * framework for applications.
 *
 * @see EventFilter
 * @see EventListener
 *
 * @author Sjoerd Bouwman
 * @author Peter S. Heijnen
 */
public class EventDispatcher
{
	/**
	 * This is a special event dispatcher that ignores all events and does not
	 * register any listeners or filers.
	 */
	public static final EventDispatcher NULL = new EventDispatcher()
	{
		@Override
		public void addListener( final EventListener listener )
		{
			throw new UnsupportedOperationException( "NULL dispatcher does not accept listeners" );
		}

		@Override
		public void appendFilter( final EventFilter filter )
		{
			throw new UnsupportedOperationException( "NULL dispatcher does not accept filters" );
		}

		@Override
		public void insertFilter( final EventFilter filter )
		{
			throw new UnsupportedOperationException( "NULL dispatcher does not accept filters" );
		}

		@Nullable
		@Override
		public EventObject dispatch( final EventObject event )
		{
			/* straight to /dev/null */
			return null;
		}

		@Override
		public void requestFocus( @NotNull final EventFilter filter )
		{
			/* don't accept focus */
		}
	};

	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( EventDispatcher.class );

	/**
	 * Chain of filters an incoming event must pass through before being passed
	 * on to the registered listeners.
	 *
	 * NOTE: The entry with the lowest index is executed last!
	 */
	private final List<EventFilter> _filters = new ArrayList<EventFilter>();

	/**
	 * Filter that is given first pick when filtering events. The rest of the
	 * filter ordering is retained. When the focus is released, the original
	 * filter ordering is restored.
	 */
	private EventFilter _focusFilter = null;

	/**
	 * List of registered listeners.
	 */
	private final List<EventListener> _listeners = new ArrayList<EventListener>();

	/**
	 * Append event filter to the end of the filter chain. This filter will be
	 * applied after all previously registered filters. Note that a filter may
	 * be added multiple times to the filter chain (unlike a listener).
	 *
	 * @param   filter      Event filter to add to the filter chain.
	 */
	public void appendFilter( final EventFilter filter )
	{
		_filters.add( filter );
	}

	/**
	 * Insert event filter at the start of the filter chain. This filter will
	 * be applied before all previously registered filters. Note that a filter
	 * may be added multiple times to the filter chain (unlike a listener).
	 *
	 * @param   filter      Event filter to add to the filter chain.
	 */
	public void insertFilter( final EventFilter filter )
	{
		_filters.add( 0 , filter );
	}

	/**
	 * Removes an event filter from the filter chain. If the filter is added
	 * multiple times, the first entry in the filter chain is removed.
	 *
	 * @param   filter      Event filter to remove from the filter chain.
	 */
	public void removeFilter( final EventFilter filter )
	{
		_filters.remove( filter );
	}

	/**
	 * Tests whether the specified filter currently has this dispatcher's focus.
	 *
	 * @param   filter  Filter to test.
	 *
	 * @return  {@code true} if the specified filter has focus;
	 *          {@code false} otherwise.
	 *
	 * @throws  NullPointerException if {@code filter} is {@code null}.
	 */
	public boolean hasFocus( @NotNull final EventFilter filter )
	{
		return ( filter == _focusFilter );
	}

	/**
	 * Requests that focus is transferred to the specified filter.
	 *
	 * Until the focus is released or transferred to another filter, the
	 * specified filter will have first pick in filtering events (its
	 * {@link EventFilter#filterEvent} method is called before all others). All
	 * other filters will be processed as usual (the filter having focus will
	 * not be called twice).
	 *
	 * @param   filter  Filter to be given focus.
	 */
	public void requestFocus( @NotNull final EventFilter filter )
	{
		_focusFilter = filter;
	}

	/**
	 * Requests that focus is released from any filter set as such. This will
	 * restore the original filter ordering.
	 */
	public void releaseFocus()
	{
		_focusFilter = null;
	}

	/**
	 * Send event through the filter chain. The filter chain may cause the event
	 * to change or dissipate completely.
	 *
	 * @param   event       Event to filter.
	 *
	 * @return  Filtered event (may be different or same as argument);
	 *          {@code null} if event dissipated (event was filtered out).
	 */
	@Nullable
	private EventObject filterEvent( final EventObject event )
	{
		EventObject filteredEvent = event;
		if ( filteredEvent != null )
		{
			final List<EventFilter> filters     = _filters;
			final EventFilter       focusFilter = _focusFilter;

			if ( focusFilter != null )
			{
				filteredEvent = focusFilter.filterEvent( filteredEvent );
			}

			for ( int i = 0 ; ( filteredEvent != null ) && ( i < filters.size() ) ; i++ )
			{
				final EventFilter filter = filters.get( i );
				if ( filter != focusFilter )
				{
					filteredEvent = filter.filterEvent( filteredEvent );
				}
			}
		}

		return filteredEvent;
	}

	/**
	 * Register a listener. Registering a listener twice or specifying
	 * {@code null} as listener has no effect.
	 *
	 * @param   listener    Listener to register.
	 */
	public void addListener( final EventListener listener )
	{
		if ( ( listener != null ) && !_listeners.contains( listener ) )
		{
			_listeners.add( listener );
		}
	}

	/**
	 * Unregisters a listener. Unregistering a listener twice or specifying
	 * {@code null} as listener has no effect.
	 *
	 * @param   listener    Listener to unregister.
	 */
	public void removeListener( final EventListener listener )
	{
		if ( listener != null )
		{
			_listeners.remove( listener );
		}
	}

	/**
	 * Dispatches an event to all registered listeners after sending it through
	 * the filter chain. The filter chain may cause the event to change or
	 * dry out all together (no listener is invoked).
	 *
	 * @param   event       Event to be filtered and dispatched.
	 *
	 * @return  Event that was passed to listeners (may be changed);
	 *          {@code null} if no event was sent (event was filtered out).
	 */
	@Nullable
	public EventObject dispatch( final EventObject event )
	{
		final EventObject filteredEvent = filterEvent( event );
		if ( filteredEvent != null )
		{
			final List<EventListener> listeners = _listeners;
			if ( listeners.isEmpty() )
			{
				LOG.trace( "LOST EVENT: " + event );
			}
			else
			{
				/*
				 * Dispatch event to all registered listeners.
				 */
				/* NOTE: use indexed loop here to prevent ConcurrentModificationException's when listeners change the listener list */
				//noinspection ForLoopReplaceableByForEach
				for ( int i = 0 ; i < listeners.size() ; i++ )
				{
					final EventListener listener = listeners.get( i );
					listener.handleEvent( filteredEvent );
				}
			}
		}
		return filteredEvent;
	}
}
