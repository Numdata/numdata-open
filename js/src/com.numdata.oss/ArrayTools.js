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

/**
 * This class contains utility methods for working with arrays.
 *
 * @author  Peter S. Heijnen
 */
export default
{
	/**
	 * Test equality between arrays. This returns:
	 * <ul>
	 *   <li>{@code true}
	 *     if both arguments are {@code null}.
	 *   </li>
	 *   <li>{@code false}
	 *     if one of the arguments is {@code null}.
	 *   </li>
	 *   <li>{@code true}
	 *     if both arguments refer to the same object.
	 *   </li>
	 *   <li>{@code true}
	 *     if both arguments are arrays of equal type, length, and content
	 *     (tested recursively).
	 *   </li>
	 *   <li>{@code true}
	 *     if either argument is not an array or if the argument types are
	 *     unequal and <code>array1.equals( array2 )</code> returns
	 *     {@code true}.
	 *   </li>
	 * </ul>
	 *
	 * @param {*} array1  First array to compare for equality.
	 * @param {*} array2  Second array to compare for equality.
	 *
	 * @return {boolean} {@code true} if the two arrays are equal or both
	 *          {@code null} (see method comment for details);
	 *          {@code false} otherwise.
	 */
	equals: function( array1, array2 )
	{
		let result = array1 === array2;

		if ( !result && array1 && array2 && ( typeof array1 === 'object' ) && ( typeof array2 === 'object' ) )
		{
			if ( typeof array1.equals === 'function' )
			{
				result = array1.equals( array2 );
			}
			else if ( Array.isArray( array1 ) && Array.isArray( array2 ) )
			{
				const length = array1.length;
				if ( length === array2.length )
				{
					result = true;
					for ( let i = 0 ; i < length ; i++ )
					{
						const value1 = array1[ i ];
						const value2 = array2[ i ];

						if ( !this.equals( value1, value2 ) )
						{
							result = false;
							break;
						}
					}
				}
			}
		}

		return result;
	}
};
