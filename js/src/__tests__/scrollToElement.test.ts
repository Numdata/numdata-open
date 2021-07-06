/*
 * @jest-environment jsdom
 */
/*
 * Copyright (c) 2021, Unicon Creation BV, The Netherlands.
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

import scrollToElement from '../scrollToElement';

test( 'scroll to bottom', () => {
	document.body.innerHTML = `
<div>
	<div id="scroller" style="overflow-y: auto; height: 500px;">
		<div>
			<div id="top">top</div>
			<div style="height: 1000px;">spacer</div>
			<div id="middle">middle</div>
			<div style="height: 1000px;">spacer</div>
			<div id="bottom">bottom</div>
		</div>
	</div>
</div>
`;

	const scroller = document.getElementById( 'scroller' );
	/** @override */
	scroller.getBoundingClientRect = () => ( { height: 500, top: 0, bottom: 500 } as DOMRect );
	const scrollerCenter = 250;

	const top = document.getElementById( 'top' );
	const middle = document.getElementById( 'middle' );
	const bottom = document.getElementById( 'bottom' );
	/** @override */
	top.getBoundingClientRect = () => ( { height: 20, top: 0 - scroller.scrollTop, bottom: 20 - scroller.scrollTop } as DOMRect );
	const topCenter = 10;
	/** @override */
	middle.getBoundingClientRect = () => ( { height: 20, top: 1020 - scroller.scrollTop, bottom: 1040 - scroller.scrollTop } as DOMRect );
	const middleCenter = 1030;
	/** @override */
	bottom.getBoundingClientRect = () => ( { height: 20, top: 2040 - scroller.scrollTop, bottom: 2060 - scroller.scrollTop } as DOMRect );
	const bottomCenter = 2050;

	expect( scroller.scrollTop ).toBe( 0 );
	scrollToElement( top );
	expect( scroller.scrollTop ).toBe( 0 ); // Already in view, so no change.
	scrollToElement( middle );
	expect( scroller.scrollTop ).toBe( middleCenter - scrollerCenter );
	scrollToElement( bottom );
	expect( scroller.scrollTop ).toBe( bottomCenter - scrollerCenter );
	scrollToElement( middle );
	expect( scroller.scrollTop ).toBe( middleCenter - scrollerCenter );
	scrollToElement( top );
	expect( scroller.scrollTop ).toBe( topCenter - scrollerCenter );
} );
