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

export default interface AugmentedList<T>
{
    [ Symbol.iterator ](): Iterator<T>;

    /**
     * Length of the list.
     * @returns {number}
     */
    readonly length: number;

    /**
     * Calls the given function for each element of the aggregation.
     *
     * @param {function} consumer Consumer function.
     */
    forEach( consumer: ( element: T, index?: number, array?: T[] ) => void ): void;

    /**
     * Creates a new array with the results of calling a provided function on
     * every element in this list.
     *
     * @param {function} callback Callback that produces the element of the new array.
     *
     * @return {Array} Created array.
     */
    map<R>( callback: ( element: T, index?: number, array?: T[] ) => R ): R[];

    /**
     * Returns a new array containing only the elements from this list that
     * match the given condition.
     *
     * @param {function} condition Condition function.
     *
     * @return {Array}
     */
    filter( condition: ( element: T, index?: number, array?: T[] ) => boolean ): T[];

    /**
     * Finds the first element in the list for which the given condition holds.
     *
     * @param {function} condition Condition function.
     *
     * @return {*}
     */
    find( condition: ( element: T, index?: number, array?: T[] ) => boolean ): T;

    /**
     * Tests whether all elements pass the given test.
     * @param condition Function implementing the test.
     */
    every( condition: ( element: T, index?: number, array?: T[] ) => boolean ): boolean;

    /**
     * Tests whether some element passes the given test.
     *
     * @param condition Function implementing the test.
     */
    some( condition: ( element: T, index?: number, array?: T[] ) => boolean ): boolean;

    isEmpty(): boolean;

    size(): number;

    clear(): void;

    add( element: T ): boolean;

    add( index: number, element: T ): void;

    addElement( element: T ): boolean;

    addIndex( index: number, element: T ): void;

    addAll( collection: Iterable<T> ): boolean;

    setLength( length: number ): void;

    contains( element: any ): boolean;

    containsAll( collection: Iterable<any> ): boolean;

    indexOf( element: any ): number;

    lastIndexOf( element: any ): number;

    get( index: number ): T;

    getFirst(): T;

    getLast(): T;

    setIndex( index: number, element: T ): T;

    setAll( list: Iterable<T> ): void;

    remove( index: number ): T;

    remove( element: any ): boolean;

    removeIndex( index: number ): T;

    removeRange( startIndex: number, endIndex: number ): void;

    removeElement( element: any ): boolean;

    removeFirst(): T;

    removeLast(): T;

    removeAll( collection: Iterable<any> ): boolean;

    equals( other: any ): boolean;
}
