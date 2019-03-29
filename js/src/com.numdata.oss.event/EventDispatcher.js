/*
 * Copyright (c) 2017-2019, Numdata BV, The Netherlands.
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
	 * Chain of filters an incoming event must pass through before being passed
	 * on to the registered listeners.
	 *
	 * NOTE: The entry with the lowest index is executed last!
	 * @type EventFilter[]
	 */
	_filters;

	/**
	 * Filter that is given first pick when filtering events. The rest of the
	 * filter ordering is retained. When the focus is released, the original
	 * filter ordering is restored.
	 * @type EventFilter
	 */
	_focusFilter = null;

	/**
	 * List of registered listeners.
	 * @type EventListener[]
	 */
	_listeners;

	constructor()
	{
		this._filters = [];
		this._listeners = [];
	}

	/**
	 * Append event filter to the end of the filter chain. This filter will be
	 * applied after all previously registered filters. Note that a filter may
	 * be added multiple times to the filter chain (unlike a listener).
	 *
	 * @param   {EventFilter} filter      Event filter to add to the filter chain.
	 */
	appendFilter( filter )
	{
		this._filters.push( filter );
	}

	/**
	 * Insert event filter at the start of the filter chain. This filter will
	 * be applied before all previously registered filters. Note that a filter
	 * may be added multiple times to the filter chain (unlike a listener).
	 *
	 * @param   {EventFilter} filter      Event filter to add to the filter chain.
	 */
	insertFilter( filter )
	{
		this._filters.splice( 0, 0, filter );
	}

	/**
	 * Removes an event filter from the filter chain. If the filter is added
	 * multiple times, the first entry in the filter chain is removed.
	 *
	 * @param   {EventFilter} filter      Event filter to remove from the filter chain.
	 */
	removeFilter( filter )
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
	 * @param   {!EventFilter} filter  Filter to test.
	 *
	 * @return  {boolean} {@code true} if the specified filter has focus; {@code false} otherwise.
	 *
	 * @throws  TypeError if {@code filter} is {@code null}.
	 */
	hasFocus( filter )
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
	 * @param   {!EventFilter} filter  Filter to be given focus.
	 */
	requestFocus( filter )
	{
		this._focusFilter = filter;
	}

	/**
	 * Requests that focus is released from any filter set as such. This will
	 * restore the original filter ordering.
	 */
	releaseFocus()
	{
		this._focusFilter = null;
	}

	/**
	 * Send event through the filter chain. The filter chain may cause the event
	 * to change or dissipate completely.
	 *
	 * @param   {EventObject} event       Event to filter.
	 *
	 * @return  {?EventObject} Filtered event (may be different or same as argument);
	 *          {@code null} if event dissipated (event was filtered out).
	 *
	 * @private
	 */
	filterEvent( event )
	{
		let filteredEvent = event;
		if ( filteredEvent )
		{
			let filters = this._filters;
			let focusFilter = this._focusFilter;

			if ( focusFilter )
			{
				filteredEvent = focusFilter.filterEvent( filteredEvent );
			}

			for ( let i = 0; filteredEvent && ( i < filters.length ); i++ )
			{
				let filter = filters[ i ];
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
	 * @param   {EventListener} listener    Listener to register.
	 */
	addListener( listener )
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
	 * @param   {EventListener} listener    Listener to unregister.
	 */
	removeListener( listener )
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
	 * @param   {EventObject} event       Event to be filtered and dispatched.
	 *
	 * @return  {?EventObject} Event that was passed to listeners (may be changed);
	 *          {@code null} if no event was sent (event was filtered out).
	 */
	dispatch( event )
	{
		let filteredEvent = this.filterEvent( event );
		if ( filteredEvent )
		{
			let listeners = this._listeners;
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
 * Provides information about an event.
 *
 * @typedef {object} EventObject
 */

/**
 * Event listener.
 *
 * @callback EventListener
 * @param {!EventObject} [event] Event.
 */

/**
 * Event filter that decides whether an event should be passed on by the dispatcher or not.
 *
 * @callback EventFilter
 * @param {!EventObject} event Event to filter.
 * @returns {?EventObject} Filtered event, or null.
 */

/**
 * This is a special event dispatcher that ignores all events and does not
 * register any listeners or filers.
 */
class NullEventDispatcher extends EventDispatcher
{
	addListener( listener ) // eslint-disable-line no-unused-vars
	{
		throw new Error( "NULL dispatcher does not accept listeners" );
	}

	appendFilter( filter ) // eslint-disable-line no-unused-vars
	{
		throw new Error( "NULL dispatcher does not accept filters" );
	}

	insertFilter( filter ) // eslint-disable-line no-unused-vars
	{
		throw new Error( "NULL dispatcher does not accept filters" );
	}

	dispatch( event ) // eslint-disable-line no-unused-vars
	{
		/* straight to /dev/null */
		return null;
	}

	requestFocus( filter ) // eslint-disable-line no-unused-vars
	{
		/* don't accept focus */
	}
}

/**
 * This is a special event dispatcher that ignores all events and does not
 * register any listeners or filers.
 */
EventDispatcher.NULL = new NullEventDispatcher();
