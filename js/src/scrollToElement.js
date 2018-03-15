/*
 * Copyright (c) 2018, Numdata BV, The Netherlands.
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
 * Scrolls the given parent element vertically such that the given element is
 * visible. If the element is already fully visible, the scroll position is left
 * as-is. Horizontal scrolling is not supported.
 *
 * @author Gerrit Meinders
 *
 * @param element
 * @param parent
 */
export default function scrollToElement( element, parent )
{
	if ( element && element.getBoundingClientRect &&
		 parent && parent.getBoundingClientRect )
	{
		const itemBounds = element.getBoundingClientRect();
		const parentBounds = parent.getBoundingClientRect();

		if ( itemBounds.top < parentBounds.top ||
			 itemBounds.bottom > parentBounds.bottom )
		{
			// Not all browsers include width/height properties in DOMRect.
			const itemHeight = itemBounds.bottom - itemBounds.top;
			const parentHeight = parentBounds.bottom - parentBounds.top;

			parent.scrollTop += itemBounds.top - parentBounds.top - ( parentHeight - itemHeight ) / 2;
		}
	}
}
