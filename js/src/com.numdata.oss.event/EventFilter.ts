/*
 * Copyright (c) 2004-2021, Unicon Creation BV, The Netherlands.
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

import { EventObject } from './EventDispatcher';

/**
 * This interface should be implemented to create an event filter. The filter
 * decides whether the event should be passed on by the dispatcher or not.
 *
 * @author S. Bouwman
 * @see EventDispatcher
 * @see EventListener
 */
export default interface EventFilter
{
	/**
	 * Filters an event. The filter must decide whether the event should be:
	 * <dl>
	 *
	 * <dt>Dispatched as-is</dt><dd> The filter should return the event that was
	 * passed as argument.</dd>
	 *
	 * <dt>Modified</dt><dd> The filter may create a new event (e.g. to
	 * transform an event into some other event) and return it.</dd>
	 *
	 * <dt>Discarded</dt><dd> The event is discarded (no further filters are
	 * processed).</dd>
	 *
	 * </dl>
	 *
	 * @param event Event to be filtered.
	 *
	 * @return Filtered event (may be same or modified); {@code null} to discard
	 * event completely.
	 */
	filterEvent( event: EventObject ): EventObject | null;
}
