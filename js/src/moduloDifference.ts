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

/**
 * Return the smallest non-negative value congruent to a modulo n.
 *
 * @param {number} a Value.
 * @param {number} n Modulus to use.
 *
 * @return {number} Value between 0 (inclusive) and n (exclusive).
 */
function mod( a: number, n: number ): number
{
	return ( a % n + n ) % n;
}

/**
 * Return the smallest (nearest to zero) value congruent to a modulo n.
 *
 * @param {number} a Value.
 * @param {number} n Modulus to use.
 *
 * @return {number} Value between -n/2 (inclusive) and n/2 (exclusive).
 */
function smallest( a: number, n: number ): number
{
	const h = n / 2;
	return mod( a + h, n ) - h;
}

/**
 * Returns the smallest signed distance from a to b, modulo n.
 *
 * @param {number} a Start value.
 * @param {number} b End value.
 * @param {number} n Modulus to use.
 *
 * @return {number} Signed distance, in the range [-n/2, n/2>.
 */
export default function moduloDifference( a: number, b: number, n: number ): number
{
	return smallest( b - a, n );
}
