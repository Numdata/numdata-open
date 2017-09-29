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
"use strict";
const expect = require('chai').expect;

var BigDecimal = require( 'bigdecimal' ).BigDecimal;

var parseBigRational = require( 'big-rational' );
var BigRational = parseBigRational().constructor;
BigRational.parse = parseBigRational;

var LengthMeasurePreferences = require( '../../lib/com.numdata.oss/LengthMeasurePreferences' ).default;
var Locale = require( '../../lib/com.numdata.oss/Locale' ).default;

/**
 * Unit test for {@link LengthMeasureFormat} class.
 *
 * @author Peter S. Heijnen
 * @author Gerrit Meinders (ported to Javascript)
 */

describe( 'LengthMeasureFormat', () =>
{
    function Test( text, preferences, locale )
    {
        this._text = text;
        this._preferences = preferences;
        this._locale = locale;
    }

    var decimal4 = LengthMeasurePreferences.getMetricInstance();
    decimal4.setMinScale( 4 );

    var decimalComma4 = LengthMeasurePreferences.getMetricInstance();
    decimalComma4.setMinScale( 4 );

    var engineering4 = LengthMeasurePreferences.getEngineeringInstance();
    engineering4.setFeetZero( true );
    engineering4.setInchesZero( true );
    engineering4.setMinScale( 4 );

    var engineeringComma4 = LengthMeasurePreferences.getEngineeringInstance();
    engineeringComma4.setFeetZero( true );
    engineeringComma4.setInchesZero( true );
    engineeringComma4.setMinScale( 4 );

    [
        new Test( "1.5000", decimal4, Locale.getDefault() ),
        new Test( "1.5000", decimal4, Locale.ENGLISH ),
        new Test( "1,5000", decimalComma4, Locale.GERMAN ),
        new Test( "1500", LengthMeasurePreferences.getMetricInstance(), Locale.GERMAN ),
        new Test( "1500.0000", decimal4, Locale.ENGLISH ),
        new Test( "1500,0000", decimalComma4, Locale.GERMAN ),
        new Test( "-1500.0000", decimal4, Locale.ENGLISH ),
        new Test( "-1500,0000", decimalComma4, Locale.GERMAN ),
        new Test( "0'-1.5000\"", engineering4, Locale.ENGLISH ),
        new Test( "0'-1,5000\"", engineeringComma4, Locale.GERMAN ),
        new Test( "125'-0.0000\"", engineering4, Locale.ENGLISH ),
        new Test( "125'-0,0000\"", engineeringComma4, Locale.GERMAN ),
        new Test( "-125'-0.0000\"", engineering4, Locale.ENGLISH ),
        new Test( "-125'-0,0000\"", engineeringComma4, Locale.GERMAN ),
        new Test( "0'-1 1/2\"", createArchitecturalPreferences( true, "-", " " ), Locale.US ),
        new Test( "-0'-1 1/2\"", createArchitecturalPreferences( true, "-", " " ), Locale.US ),
        new Test( "125'-0\"", createArchitecturalPreferences( true, "-", " " ), Locale.US ),
        new Test( "-125'-0\"", createArchitecturalPreferences( true, "-", " " ), Locale.US ),
        new Test( "1 1/2", LengthMeasurePreferences.getFractionalInstance(), Locale.US ),
        new Test( "-1 1/2", LengthMeasurePreferences.getFractionalInstance(), Locale.US ),
        new Test( "5'", LengthMeasurePreferences.getArchitecturalInstance(), Locale.US ),
        new Test( "60\"", createArchitecturalPreferences( false, "-", " " ), Locale.US ),
        new Test( "5'-9\"", createArchitecturalPreferences( true, "-", " " ), Locale.US ),
        new Test( "-5'-9\"", createArchitecturalPreferences( true, "-", " " ), Locale.US ),
        new Test( "5' 9\"", createArchitecturalPreferences( true, " ", " " ), Locale.US ),
        new Test( "5'9\"", createArchitecturalPreferences( true, "", " " ), Locale.US ),
        new Test( "5'-1/2\"", createArchitecturalPreferences( true, "-", " " ), Locale.US ),
        new Test( "5' 1/2\"", createArchitecturalPreferences( true, " ", " " ), Locale.US ),
        new Test( "5'1/2\"", createArchitecturalPreferences( true, "", " " ), Locale.US ),
        new Test( "5'-9-1/2\"", createArchitecturalPreferences( true, "-", "-" ), Locale.US ),
        new Test( "-5'-9-1/2\"", createArchitecturalPreferences( true, "-", "-" ), Locale.US ),
        new Test( "5' 9-1/2\"", createArchitecturalPreferences( true, " ", "-" ), Locale.US ),
        new Test( "5'9-1/2\"", createArchitecturalPreferences( true, "", "-" ), Locale.US ),
        new Test( "5'-9 1/2\"", createArchitecturalPreferences( true, "-", " " ), Locale.US ),
        new Test( "5' 9 1/2\"", createArchitecturalPreferences( true, " ", " " ), Locale.US ),
        new Test( "5'9 1/2\"", createArchitecturalPreferences( true, "", " " ), Locale.US )
    ].forEach( ( test ) =>
            {
                var text = test._text;
                var preferences = test._preferences;
                var locale = test._locale;

                var measureFormat = preferences.getNumberFormat( locale );
                measureFormat.setUsingBigValues( true );

                it( "should reproduce the original text for a big number (" + text + ")", () =>
                {
                    var parsed1 = measureFormat.parse( text );
                    expect( measureFormat.format( parsed1 ) ).to.equal( text );
                } );

                it( "should reproduce the original text for a native number (" + text + ")", () =>
                {
                    measureFormat.setUsingBigValues( false );
                    var parsed2 = measureFormat.parse( text );
                    expect( measureFormat.format( parsed2 ) ).to.equal( text );
                } );
            } );
} );

/**
 * Create {@link LengthMeasurePreferences} based on {@link
        * LengthMeasurePreferences#getArchitecturalInstance()}.
 *
 * @param {boolean} feet                   Whether to include feet in output.
 * @param {string} feetInchesSeparator    Separator to insert between feet and inches.
 * @param {string} wholeFractionSeparator Separator to insert between whole and fraction.
 *
 * @return {LengthMeasurePreferences} {@link LengthMeasurePreferences}.
 */
function createArchitecturalPreferences( feet, feetInchesSeparator, wholeFractionSeparator )
{
    var result = LengthMeasurePreferences.getArchitecturalInstance();
    result.setFeet( feet );
    result.setFeetZero( true );
    result.setFeetInchesSeparator( feetInchesSeparator );
    result.setInchesZero( true );
    result.setWholeFractionSeparator( wholeFractionSeparator );
    return result;
}

describe( "LengthMeasureFormat.format", () =>
{
    function Test( number, text, preferences )
    {
        this._number = number;
        this._text = text;
        this._preferences = preferences;
    }

    var locale = Locale.US;

    var decimal4 = LengthMeasurePreferences.getMetricInstance();
    decimal4.setMinScale( 4 );

    var engineering4 = LengthMeasurePreferences.getEngineeringInstance();
    engineering4.setFeetZero( true );
    engineering4.setInchesZero( true );
    engineering4.setMinScale( 4 );

    [
        new Test( new BigDecimal( "0.1" ), "0.1", LengthMeasurePreferences.getMetricInstance() ),
        new Test( new BigDecimal( "0.1" ), "0.1\"", LengthMeasurePreferences.getEngineeringInstance() ),
        new Test( new BigDecimal( "0.1" ), "3/32\"", LengthMeasurePreferences.getArchitecturalInstance() ),
        new Test( new BigDecimal( "0.1" ), "1/10", LengthMeasurePreferences.getFractionalInstance() ),
        new Test( BigRational.parse( 1, 3 ), "0.333333", LengthMeasurePreferences.getMetricInstance() ),
        new Test( BigRational.parse( 1, 3 ), "0.333333\"", LengthMeasurePreferences.getEngineeringInstance() ),
        new Test( BigRational.parse( 1, 3 ), "21/64\"", LengthMeasurePreferences.getArchitecturalInstance() ),
        new Test( BigRational.parse( 1, 3 ), "1/3", LengthMeasurePreferences.getFractionalInstance() ),
        new Test( BigRational.parse( 1, 4 ), "0.25", LengthMeasurePreferences.getMetricInstance() ),
        new Test( BigRational.parse( 1, 4 ), "0.25\"", LengthMeasurePreferences.getEngineeringInstance() ),
        new Test( BigRational.parse( 1, 4 ), "1/4\"", LengthMeasurePreferences.getArchitecturalInstance() ),
        new Test( BigRational.parse( 1, 4 ), "1/4", LengthMeasurePreferences.getFractionalInstance() ),
        new Test( BigRational.parse( 1, 5 ), "0.2", LengthMeasurePreferences.getMetricInstance() ),
        new Test( BigRational.parse( 1, 5 ), "0.2\"", LengthMeasurePreferences.getEngineeringInstance() ),
        new Test( BigRational.parse( 1, 5 ), "13/64\"", LengthMeasurePreferences.getArchitecturalInstance() ),
        new Test( BigRational.parse( 1, 5 ), "1/5", LengthMeasurePreferences.getFractionalInstance() ),
        new Test( BigRational.parse( 1, 6 ), "0.166667", LengthMeasurePreferences.getMetricInstance() ),
        new Test( BigRational.parse( 1, 6 ), "0.166667\"", LengthMeasurePreferences.getEngineeringInstance() ),
        new Test( BigRational.parse( 1, 6 ), "11/64\"", LengthMeasurePreferences.getArchitecturalInstance() ),
        new Test( BigRational.parse( 1, 6 ), "1/6", LengthMeasurePreferences.getFractionalInstance() )
    ].forEach( ( test ) =>
            {
                var number = test._number;
                var preferences = test._preferences;
                var text = test._text;

                var measureFormat = preferences.getNumberFormat( locale );
                it( 'should properly format ' + number, () => expect( measureFormat.format( number ) ).to.equal( text ) );
            } );
} );

describe( 'unit conversion', () =>
{
    function Test( number, text, preferences )
    {
        this._number = number;
        this._text = text;
        this._preferences = preferences;
    }

    var locale = Locale.US;

    var mmToInches = LengthMeasurePreferences.getArchitecturalInstance();
    mmToInches.setBaseUnit( LengthMeasurePreferences.LengthUnit.MILLIMETER );

    var mmToCm = LengthMeasurePreferences.getMetricInstance();
    mmToCm.setBaseUnit( LengthMeasurePreferences.LengthUnit.MILLIMETER );
    mmToCm.setDisplayUnit( LengthMeasurePreferences.LengthUnit.CENTIMETER );

    [
        new Test( 25.4, '1"', mmToInches ),
        new Test( 25.4, '2.54', mmToCm )
    ].forEach( ( test ) =>
            {
                var number = test._number;
                var preferences = test._preferences;
                var text = test._text;

                var measureFormat = preferences.getNumberFormat( locale );
                var formatted = measureFormat.format( number );

                it( 'should properly format number ' + number, () => expect( formatted ).to.equal( text ) );

                var parsed = measureFormat.parse( formatted );
                it( 'should properly format number ' + number, () => expect( number ).to.equal( parsed ) );
            } );
} );
