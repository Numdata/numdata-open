/*
 * (C) Copyright Unicon Creation BV 2021-2021 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Unicon Creation BV. Please contact
 * Unicon Creation BV for license information.
 */

import LengthMeasurePreferences from '../LengthMeasurePreferences';

/**
 * Unit tests for {@link LengthMeasurePreferences}. Most of it is already
 * covered by {@link LengthMeasureFormat.test.js} though.
 *
 * @author Gerrit Meinders
 */

describe( 'LengthUnit', () => {
	/**
	 * Tests {@link LengthMeasurePreferences.LengthUnit.valueOf}.
	 */
	test( 'valueOf', () => {
		expect( LengthMeasurePreferences.LengthUnit.valueOf( "INCH" ) ).toBe( LengthMeasurePreferences.LengthUnit.INCH );
		expect( LengthMeasurePreferences.LengthUnit.valueOf( "FOOT" ) ).toBe( LengthMeasurePreferences.LengthUnit.FOOT );
		expect( LengthMeasurePreferences.LengthUnit.valueOf( "METER" ) ).toBe( LengthMeasurePreferences.LengthUnit.METER );
		expect( LengthMeasurePreferences.LengthUnit.valueOf( "CENTIMETER" ) ).toBe( LengthMeasurePreferences.LengthUnit.CENTIMETER );
		expect( LengthMeasurePreferences.LengthUnit.valueOf( "MILLIMETER" ) ).toBe( LengthMeasurePreferences.LengthUnit.MILLIMETER );
	} );

	/**
	 * Tests {@link LengthMeasurePreferences.LengthUnit.isImperial}.
	 */
	test( 'isImperial', () => {
		expect( LengthMeasurePreferences.LengthUnit.INCH.isImperial() ).toBe( true );
		expect( LengthMeasurePreferences.LengthUnit.FOOT.isImperial() ).toBe( true );
		expect( LengthMeasurePreferences.LengthUnit.METER.isImperial() ).toBe( false );
		expect( LengthMeasurePreferences.LengthUnit.CENTIMETER.isImperial() ).toBe( false );
		expect( LengthMeasurePreferences.LengthUnit.MILLIMETER.isImperial() ).toBe( false );
	} );

	/**
	 * Tests {@link LengthMeasurePreferences.LengthUnit.isMetric}.
	 */
	test( 'isMetric', () => {
		expect( LengthMeasurePreferences.LengthUnit.INCH.isMetric() ).toBe( false );
		expect( LengthMeasurePreferences.LengthUnit.FOOT.isMetric() ).toBe( false );
		expect( LengthMeasurePreferences.LengthUnit.METER.isMetric() ).toBe( true );
		expect( LengthMeasurePreferences.LengthUnit.CENTIMETER.isMetric() ).toBe( true );
		expect( LengthMeasurePreferences.LengthUnit.MILLIMETER.isMetric() ).toBe( true );
	} );

	/**
	 * Tests {@link LengthMeasurePreferences.LengthUnit.getShortName}.
	 */
	test( 'getShortName', () => {
		expect( LengthMeasurePreferences.LengthUnit.INCH.getShortName() ).toBe( "in" );
		expect( LengthMeasurePreferences.LengthUnit.FOOT.getShortName() ).toBe( "ft" );
		expect( LengthMeasurePreferences.LengthUnit.METER.getShortName() ).toBe( "m" );
		expect( LengthMeasurePreferences.LengthUnit.CENTIMETER.getShortName() ).toBe( "cm" );
		expect( LengthMeasurePreferences.LengthUnit.MILLIMETER.getShortName() ).toBe( "mm" );
	} );
} );

/**
 * Tests {@link LengthMeasurePreferences.fromJSON}.
 */
test( 'fromJSON', () => {
	const parsed = LengthMeasurePreferences.fromJSON( {
		baseUnit: LengthMeasurePreferences.LengthUnit.METER.name,
		displayUnit: LengthMeasurePreferences.LengthUnit.MILLIMETER.name,
		minScale: 0,
		maxScale: 3,
		fraction: true,
		fractionSeparator: '/',
		whole: true,
		wholeZero: true,
		wholeFractionSeparator: ',',
		feet: true,
		feetZero: false,
		feetSymbol: 'ft',
		feetInchesSeparator: ' ',
		inchesZero: false,
		inchSymbol: 'in'
	} );

	expect( parsed._baseUnit ).toBe( LengthMeasurePreferences.LengthUnit.METER );
	expect( parsed._displayUnit ).toBe( LengthMeasurePreferences.LengthUnit.MILLIMETER );
	expect( parsed._minScale ).toEqual( 0 );
	expect( parsed._maxScale ).toEqual( 3 );
	expect( parsed._fraction ).toEqual( true );
	expect( parsed._fractionSeparator ).toEqual( '/' );
	expect( parsed._whole ).toEqual( true );
	expect( parsed._wholeZero ).toEqual( true );
	expect( parsed._wholeFractionSeparator ).toEqual( ',' );
	expect( parsed._feet ).toEqual( true );
	expect( parsed._feetZero ).toEqual( false );
	expect( parsed._feetSymbol ).toEqual( 'ft' );
	expect( parsed._feetInchesSeparator ).toEqual( ' ' );
	expect( parsed._inchesZero ).toEqual( false );
	expect( parsed._inchSymbol ).toEqual( 'in' );
} );

/**
 * Tests {@link LengthMeasurePreferences.getUnitSuffix}.
 */
test( 'getUnitSuffix', () => {
	expect( LengthMeasurePreferences.fromJSON( { displayUnit: LengthMeasurePreferences.LengthUnit.FOOT.name, feetSymbol: '' } ).getUnitSuffix() ).toBe( " ft" );
	expect( LengthMeasurePreferences.fromJSON( { displayUnit: LengthMeasurePreferences.LengthUnit.FOOT.name, feetSymbol: "'" } ).getUnitSuffix() ).toBe( "" );
	expect( LengthMeasurePreferences.fromJSON( { displayUnit: LengthMeasurePreferences.LengthUnit.INCH.name, inchSymbol: '' } ).getUnitSuffix() ).toBe( " in" );
	expect( LengthMeasurePreferences.fromJSON( { displayUnit: LengthMeasurePreferences.LengthUnit.INCH.name, inchSymbol: '"' } ).getUnitSuffix() ).toBe( "" );
	expect( LengthMeasurePreferences.fromJSON( { displayUnit: LengthMeasurePreferences.LengthUnit.METER.name } ).getUnitSuffix() ).toBe( " m" );
	expect( LengthMeasurePreferences.fromJSON( { displayUnit: LengthMeasurePreferences.LengthUnit.CENTIMETER.name } ).getUnitSuffix() ).toBe( " cm" );
	expect( LengthMeasurePreferences.fromJSON( { displayUnit: LengthMeasurePreferences.LengthUnit.MILLIMETER.name } ).getUnitSuffix() ).toBe( " mm" );
} );
