/*
 * Copyright (c) 2017-2021, Unicon Creation BV, The Netherlands.
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

import type EventFilter from './EventFilter';
import type EventListener from './EventListener';

/**
 * Provides information about an event.
 */
export type EventObject = any;

/**
 * This class and its supporting interfaces provides a simple filterable event
 * framework for applications.
 *
 * @author Sjoerd Bouwman
 * @author Peter S. Heijnen
 */
export default class EventDispatcher
{
	/**
	 * This is a special event dispatcher that ignores all events and does not
	 * register any listeners or filers.
	 */
	static NULL: NullEventDispatcher;

	/**
	 * Chain of filters an incoming event must pass through before being passed
	 * on to the registered listeners.
	 *
	 * NOTE: The entry with the lowest index is executed last!
	 */
	private readonly _filters: EventFilter[] = [];

	/**
	 * Filter that is given first pick when filtering events. The rest of the
	 * filter ordering is retained. When the focus is released, the original
	 * filter ordering is restored.
	 */
	private _focusFilter: EventFilter = null;

	/**
	 * List of registered listeners.
	 */
	private readonly _listeners: EventListener[] = [];

	/**
	 * Append event filter to the end of the filter chain. This filter will be
	 * applied after all previously registered filters. Note that a filter may
	 * be added multiple times to the filter chain (unlike a listener).
	 *
	 * @param filter Event filter to add to the filter chain.
	 */
	appendFilter( filter: EventFilter ): void
	{
		this._filters.push( filter );
	}

	/**
	 * Insert event filter at the start of the filter chain. This filter will
	 * be applied before all previously registered filters. Note that a filter
	 * may be added multiple times to the filter chain (unlike a listener).
	 *
	 * @param filter Event filter to add to the filter chain.
	 */
	insertFilter( filter: EventFilter ): void
	{
		this._filters.splice( 0, 0, filter );
	}

	/**
	 * Removes an event filter from the filter chain. If the filter is added
	 * multiple times, the first entry in the filter chain is removed.
	 *
	 * @param filter Event filter to remove from the filter chain.
	 */
	removeFilter( filter: EventFilter ): void
	{
		const index = this._filters.indexOf( filter );
		if ( index !== -1 )
		{
			this._filters.splice( index, 1 );
		}
	}

	/**
	 * Tests whether the specified filter currently has this dispatcher's focus.
	 *
	 * @param filter Filter to test.
	 *
	 * @return 'true' if the specified filter has focus; 'false' otherwise.
	 *
	 * @throws TypeError if {@code filter} is {@code null}.
	 */
	hasFocus( filter: EventFilter ): boolean
	{
		if ( !filter )
		{
			throw new TypeError();
		}
		return filter === this._focusFilter;
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
	 * @param filter Filter to be given focus.
	 */
	requestFocus( filter: EventFilter ): void
	{
		this._focusFilter = filter;
	}

	/**
	 * Requests that focus is released from any filter set as such. This will
	 * restore the original filter ordering.
	 */
	releaseFocus(): void
	{
		this._focusFilter = null;
	}

	/**
	 * Send event through the filter chain. The filter chain may cause the event
	 * to change or dissipate completely.
	 *
	 * @param event Event to filter.
	 *
	 * @return Filtered event (may be different or same as argument);
	 *          {@code null} if event dissipated (event was filtered out).
	 *
	 * @private
	 */
	filterEvent( event: EventObject ): EventObject | null
	{
		let filteredEvent = event;
		if ( filteredEvent )
		{
			const filters = this._filters;
			const focusFilter = this._focusFilter;

			if ( focusFilter )
			{
				filteredEvent = focusFilter.filterEvent( filteredEvent );
			}

			for ( let i = 0; filteredEvent && ( i < filters.length ); i++ )
			{
				const filter = filters[ i ];
				if ( filter !== focusFilter )
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
	 * @param listener Listener to register.
	 */
	addListener( listener: EventListener ): void
	{
		if ( listener && ( this._listeners.indexOf( listener ) === -1 ) )
		{
			this._listeners.push( listener );
		}
	}

	/**
	 * Unregisters a listener. Unregistering a listener twice or specifying
	 * {@code null} as listener has no effect.
	 *
	 * @param listener Listener to unregister.
	 */
	removeListener( listener: EventListener ): void
	{
		if ( listener )
		{
			const index = this._listeners.indexOf( listener );
			if ( index !== -1 )
			{
				this._listeners.splice( index, 1 );
			}
		}
	}

	/**
	 * Dispatches an event to all registered listeners after sending it through
	 * the filter chain. The filter chain may cause the event to change or
	 * dry out all together (no listener is invoked).
	 *
	 * @param event Event to be filtered and dispatched.
	 *
	 * @return Event that was passed to listeners (may be changed);
	 *          {@code null} if no event was sent (event was filtered out).
	 */
	dispatch( event: EventObject ): EventObject | null
	{
		const filteredEvent = this.filterEvent( event );
		if ( filteredEvent )
		{
			const listeners = this._listeners;
			if ( !listeners.length )
			{
				console.log( "LOST EVENT: " + event );
			}
			else
			{
				/*
				 * Dispatch event to all registered listeners.
				 */
				/* NOTE: use indexed loop here to prevent ConcurrentModificationException's when listeners change the listener list */
				//noinspection ForLoopReplaceableByForEach
				for ( let i = 0; i < listeners.length; i++ )
				{
					listeners[ i ]( filteredEvent );
				}
			}
		}
		return filteredEvent;
	}
}

/**
 * This is a special event dispatcher that ignores all events and does not
 * register any listeners or filers.
 */
export class NullEventDispatcher extends EventDispatcher
{
	/** @override */
	addListener(): void
	{
		throw new Error( "NULL dispatcher does not accept listeners" );
	}

	/** @override */
	appendFilter(): void
	{
		throw new Error( "NULL dispatcher does not accept filters" );
	}

	/** @override */
	insertFilter(): void
	{
		throw new Error( "NULL dispatcher does not accept filters" );
	}

	/** @override */
	dispatch(): EventObject | null
	{
		/* straight to /dev/null */
		return null;
	}

	/** @override */
	requestFocus(): void
	{
		/* don't accept focus */
	}
}

EventDispatcher.NULL = new NullEventDispatcher();
