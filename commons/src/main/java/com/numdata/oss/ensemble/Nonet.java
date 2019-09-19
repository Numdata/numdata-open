/*
 * Copyright (c) 2008-2017, Numdata BV, The Netherlands.
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
package com.numdata.oss.ensemble;

/**
 * A nonet is a generic container of nine arbitrary objects.
 *
 * @author  Peter S. Heijnen
 */
public interface Nonet<T1,T2,T3,T4,T5,T6,T7,T8,T9>
	extends Iterable<Object>
{
	/**
	 * Get first value.
	 *
	 * @return  First value.
	 */
	T1 getValue1();

	/**
	 * Get second value.
	 *
	 * @return  Second value.
	 */
	T2 getValue2();

	/**
	 * Get third value.
	 *
	 * @return  Third value.
	 */
	T3 getValue3();

	/**
	 * Get fourth value.
	 *
	 * @return  Fourth value.
	 */
	T4 getValue4();

	/**
	 * Get fifth value.
	 *
	 * @return  Fifth value.
	 */
	T5 getValue5();

	/**
	 * Get sixth value.
	 *
	 * @return  Sixth value.
	 */
	T6 getValue6();

	/**
	 * Get seventh value.
	 *
	 * @return  Seventh value.
	 */
	T7 getValue7();

	/**
	 * Get eigth value.
	 *
	 * @return  Eigth value.
	 */
	T8 getValue8();

	/**
	 * Get nineth value.
	 *
	 * @return  Nineth value.
	 */
	T9 getValue9();
}
