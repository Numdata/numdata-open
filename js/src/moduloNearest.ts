/*
 * Copyright (c) 2019-2021, Numdata BV, The Netherlands.
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

import moduloDifference from './moduloDifference';

/**
 * Returns the distance between a to b, modulo n.
 *
 * @param a Any value.
 * @param b Any value.
 * @param n Modulus to use.
 *
 * @return Distance between a and b, in the range [0, n/2].
 */
export function distance( a: number, b: number, n: number ): number
{
	return Math.abs( moduloDifference( a, b, n ) );
}

/**
 * Finds the element from candidates that is nearest the specified value
 * modulo n. If candidates is empty, the original value is returned.
 *
 * @param {number} value Value to compare to.
 * @param {number} modulus Modulus to use.
 * @param {number[]} candidates Values to choose from.
 *
 * @return {number} Nearest value.
 */
export default function moduloNearest( value: number, modulus: number, candidates: number[] ): number
{
	let nearestValue = value;
	let nearestDistance = NaN;

	for ( let i = 0; i < candidates.length; i++ )
	{
		const candidate = candidates[ i ];
		const candidateDistance = distance( value, candidate, modulus );
		if ( !( nearestDistance <= candidateDistance ) )
		{
			nearestValue = candidate;
			nearestDistance = candidateDistance;
		}
	}

	return nearestValue;
}
