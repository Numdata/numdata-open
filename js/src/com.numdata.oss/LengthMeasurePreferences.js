/*
 * Copyright (c) 2017-2019, Numdata BV, The Netherlands.
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

import { BigDecimal, RoundingMode } from 'bigdecimal';
import BigRational from 'big-rational';
import LengthMeasureFormat from './LengthMeasureFormat';
import DecimalFormatSymbols from './DecimalFormatSymbols';

/**
 * Basic unit of length.
 *
 * @author Peter S. Heijnen
 * @author Gerrit Meinders (ported to Javascript)
 */
class LengthUnit
{
	/**
	 * Inch.
	 */
	static INCH = new LengthUnit( '0.0254' );

	/**
	 * Foot.
	 */
	static FOOT = new LengthUnit( '0.3048' );

	/**
	 * Metric.
	 */
	static METER = new LengthUnit( '1' );

	/**
	 * Centimeter.
	 */
	static CENTIMETER = new LengthUnit( '0.01' );

	/**
	 * Millimeter.
	 */
	static MILLIMETER = new LengthUnit( '0.001' );

	/**
	 * Multiplication factor to convert unit to meters.
	 */
	_decimalToMeters;

	/**
	 * Multiplication factor to convert unit to meters.
	 */
	_rationalToMeters;

	/**
	 * Construct unit.
	 *
	 * @param toMeters Multiplication factor to convert unit to meters.
	 */
	constructor( toMeters )
	{
		this._decimalToMeters = new BigDecimal( toMeters );
		this._rationalToMeters = new BigRational( toMeters );
	}

	/**
	 * Test whether this unit is an imperial unit (based on inches).
	 *
	 * @return {boolean} {@code true} if this unit is imperial.
	 */
	isImperial()
	{
		return ( this === LengthUnit.INCH ) || ( this === LengthUnit.FOOT );
	}

	/**
	 * Test whether this unit is a metric unit (based on meters).
	 *
	 * @return {boolean} {@code true} if this unit is metric.
	 */
	isMetric()
	{
		return !this.isImperial();
	}

	/**
	 * Converts a measure from one unit to another.
	 *
	 * @param {BigDecimal|BigRational} value Measure to convert.
	 * @param {LengthUnit} toUnit Unit to convert to.
	 * @param {number} [maxScale] Maximum scale of converted unit.
	 *
	 * @return {BigDecimal|BigRational} Converted measure.
	 */
	convert( value, toUnit, maxScale )
	{
		return ( value instanceof BigDecimal ) ? this.convertBD( value, toUnit, maxScale ) : this.convertBR( value, toUnit );
	}

	/**
	 * Converts a measure from one unit to another.
	 *
	 * @param {BigDecimal} value Measure to convert.
	 * @param {LengthUnit} toUnit Unit to convert to.
	 * @param {number} maxScale Maximum scale of converted unit.
	 *
	 * @return {BigDecimal} Converted measure.
	 */
	convertBD( value, toUnit, maxScale )
	{
		let result = value;

		if ( toUnit !== this )
		{
			if ( this !== LengthUnit.METER )
			{
				result = result.multiply( this._decimalToMeters );
			}

			if ( toUnit !== LengthUnit.METER )
			{
				result = result.divide( toUnit._decimalToMeters, maxScale, RoundingMode.HALF_UP() );
			}
		}

		return result;
	}

	/**
	 * Converts a measure from one unit to another.
	 *
	 * @param {BigRational} value  Measure to convert.
	 * @param {LengthUnit} toUnit Unit to convert to.
	 *
	 * @return {BigRational} Converted measure.
	 */
	convertBR( value, toUnit )
	{
		return ( toUnit === this ) ? value : value.multiply( this._rationalToMeters ).divide( toUnit._rationalToMeters );
	}

	/**
	 * Short name used to identify the unit, e.g. to choose a units.
	 *
	 * @returns {string} Short unit name.
	 */
	getShortName()
	{
		let result;
		switch ( this )
		{
			case LengthMeasurePreferences.LengthUnit.INCH:
				result = 'in';
				break;

			case LengthMeasurePreferences.LengthUnit.FOOT:
				result = 'ft';
				break;

			case LengthMeasurePreferences.LengthUnit.METER:
				result = 'm';
				break;

			case LengthMeasurePreferences.LengthUnit.CENTIMETER:
				result = 'cm';
				break;

			case LengthMeasurePreferences.LengthUnit.MILLIMETER:
				result = 'mm';
				break;

			default:
				throw new Error( "Unhandled display unit: " + this.getDisplayUnit() );
		}
		return result;
	}
}

/**
 * This class provides length measure format preferences.
 *
 * @author Peter S. Heijnen
 * @author Gerrit Meinders (ES6 port)
 */
export default class LengthMeasurePreferences {
	/**
	 * Name of the {@link #getBaseUnit} property.
	 */
	static BASE_UNIT = "baseUnit";

	/**
	 * Name of the {@link #getDisplayUnit} property.
	 */
	static DISPLAY_UNIT = "displayUnit";

	/**
	 * Name of the {@link #getMinScale} property.
	 */
	static MIN_SCALE = "minScale";

	/**
	 * Name of the {@link #getMaxScale} property.
	 */
	static MAX_SCALE = "maxScale";

	/**
	 * Name of the {@link #isFraction} property.
	 */
	static FRACTION = "fraction";

	/**
	 * Name of the {@link #getFractionSeparator} property.
	 */
	static FRACTION_SEPARATOR = "fractionSeparator";

	/**
	 * Name of the {@link #isWhole} property.
	 */
	static WHOLE = "whole";

	/**
	 * Name of the {@link #isWholeZero} property.
	 */
	static WHOLE_ZERO = "wholeZero";

	/**
	 * Name of the {@link #getWholeFractionSeparator} property.
	 */
	static WHOLE_FRACTION_SEPARATOR = "wholeFractionSeparator";

	/**
	 * Name of the {@link #isFeet} property.
	 */
	static FEET = "feet";

	/**
	 * Name of the {@link #isFeetZero} property.
	 */
	static FEET_ZERO = "feetZero";

	/**
	 * Name of the {@link #getFeetSymbol} property.
	 */
	static FEET_SYMBOL = "feetSymbol";

	/**
	 * Name of the {@link #getFeetInchesSeparator} property.
	 */
	static FEET_INCHES_SEPARATOR = "feetInchesSeparator";

	/**
	 * Name of the {@link #isInchesZero} property.
	 */
	static INCHES_ZERO = "inchesZero";

	/**
	 * Name of the {@link #getInchSymbol} property.
	 */
	static INCH_SYMBOL = "inchSymbol";

	/**
	 * Basic unit of length.
	 */
	static LengthUnit = LengthUnit;

	/**
	 * Get preferences for basic decimal format with maximum of 4 fractional
	 * digits.
	 *
	 * @return {LengthMeasurePreferences} {@link LengthMeasurePreferences}.
	 */
	static getMetricInstance()
	{
		return new LengthMeasurePreferences();
	}

	/**
	 * Get preferences for engineering units based on decimal inches.
	 *
	 * @return {LengthMeasurePreferences} {@link LengthMeasurePreferences}.
	 */
	static getEngineeringInstance()
	{
		let result = new LengthMeasurePreferences();
		result.setBaseUnit( LengthMeasurePreferences.LengthUnit.INCH );
		result.setDisplayUnit( LengthMeasurePreferences.LengthUnit.INCH );
		result.setFeet( true );
		return result;
	}

	/**
	 * Get preferences for architectural units based on fractional inches.
	 *
	 * @return {LengthMeasurePreferences} {@link LengthMeasurePreferences}.
	 */
	static getArchitecturalInstance()
	{
		let result = new LengthMeasurePreferences();
		result.setBaseUnit( LengthMeasurePreferences.LengthUnit.INCH );
		result.setDisplayUnit( LengthMeasurePreferences.LengthUnit.INCH );
		result.setFeet( true );
		result.setFraction( true );
		return result;
	}

	/**
	 * Get preferences for units based on fractions.
	 *
	 * @return {LengthMeasurePreferences} {@link LengthMeasurePreferences}.
	 */
	static getFractionalInstance()
	{
		let result = new LengthMeasurePreferences();
		result.setFraction( true );
		return result;
	}

	/**
	 * Creates preferences from a JSON representation.
	 *
	 * @param {*} preferences Preferences as JSON.
	 *
	 * @returns {LengthMeasurePreferences}
	 */
	static fromJSON( preferences )
	{
		let result = new LengthMeasurePreferences();
		result.setBaseUnit( LengthMeasurePreferences.LengthUnit[ preferences.baseUnit ] );
		result.setDisplayUnit( LengthMeasurePreferences.LengthUnit[ preferences.displayUnit ] );
		result.setMinScale( preferences.minScale );
		result.setMaxScale( preferences.maxScale );
		result.setFraction( preferences.fraction );
		result.setFractionSeparator( preferences.fractionSeparator );
		result.setWhole( preferences.whole );
		result.setWholeZero( preferences.wholeZero );
		result.setWholeFractionSeparator( preferences.wholeFractionSeparator );
		result.setFeet( preferences.feet );
		result.setFeetZero( preferences.feetZero );
		result.setFeetSymbol( preferences.feetSymbol );
		result.setFeetInchesSeparator( preferences.feetInchesSeparator );
		result.setInchesZero( preferences.inchesZero );
		result.setInchSymbol( preferences.inchSymbol );
		return result;
	}

	/**
	 * Base unit of length.
	 */
	_baseUnit;

	/**
	 * Display unit of length.
	 */
	_displayUnit;

	/**
	 * Minimum scale for fractional part of number (default: 0).
	 */
	_minScale;

	/**
	 * Maximum scale of fractional part of number (default: 4).
	 */
	_maxScale;

	/**
	 * Whether to use fractional or decimal notation for numbers.
	 */
	_fraction;

	/**
	 * Separator to use in fraction to separate the numerator and denominator
	 * (default: slash).
	 */
	_fractionSeparator;

	/**
	 * Whether to include whole and part fractions instead of a single fraction
	 * (default: true). This only applies to fractional numbers.
	 */
	_whole;

	/**
	 * Whether to include the whole fraction, even if it is zero (default:
	 * false).
	 */
	_wholeZero;

	/**
	 * Separator to include between the whole and part fractions (default:
	 * space).
	 */
	_wholeFractionSeparator;

	/**
	 * Whether to include feet (default: false).
	 */
	_feet;

	/**
	 * Whether to include feet, even if the number of feet is zero (default:
	 * false).
	 */
	_feetZero;

	/**
	 * Symbol to indicate feet (default: single straight quote). This is usually
	 * a single quote (') or 'ft'.
	 */
	_feetSymbol;

	/**
	 * Separator to include between the amount in feet and inches (default:
	 * hyphen).
	 */
	_feetInchesSeparator;

	/**
	 * Whether to include inches, even if the number of inches is zero (default:
	 * false).
	 */
	_inchesZero;

	/**
	 * Symbol to indicate inches (default: double straight quote).
	 */
	_inchSymbol;

	/**
	 * Construct new instance.
	 *
	 * @param [original] Original to clone.
	 */
	constructor( original )
	{
		if ( !original )
		{
			this._baseUnit = LengthMeasurePreferences.LengthUnit.METER;
			this._displayUnit = LengthMeasurePreferences.LengthUnit.METER;
			this._minScale = 0;
			this._maxScale = 6;
			this._fraction = false;
			this._fractionSeparator = "/";
			this._whole = true;
			this._wholeZero = false;
			this._wholeFractionSeparator = " ";
			this._feet = false;
			this._feetZero = false;
			this._feetSymbol = "'";
			this._feetInchesSeparator = "-";
			this._inchesZero = false;
			this._inchSymbol = "\"";
		}
		else
		{
			this._baseUnit = original.getBaseUnit();
			this._displayUnit = original.getDisplayUnit();
			this._minScale = original.getMinScale();
			this._maxScale = original.getMaxScale();
			this._fraction = original.isFraction();
			this._fractionSeparator = original.getFractionSeparator();
			this._whole = original.isWhole();
			this._wholeZero = original.isWholeZero();
			this._wholeFractionSeparator = original.getWholeFractionSeparator();
			this._feet = original.isFeet();
			this._feetZero = original.isFeetZero();
			this._feetSymbol = original.getFeetSymbol();
			this._feetInchesSeparator = original.getFeetInchesSeparator();
			this._inchesZero = original.isInchesZero();
			this._inchSymbol = original.getInchSymbol();
		}
	}

	/**
	 * Get base unit of length.
	 *
	 * @return {LengthUnit} Base unit of length.
	 */
	getBaseUnit()
	{
		return this._baseUnit;
	}

	/**
	 * Set base unit of length.
	 *
	 * @param {LengthUnit} unit Base unit of length.
	 */
	setBaseUnit( unit )
	{
		this._baseUnit = unit;
	}

	/**
	 * Get display unit of length.
	 *
	 * @return {LengthUnit} Display unit of length.
	 */
	getDisplayUnit()
	{
		return this._displayUnit;
	}

	/**
	 * Set display unit of length.
	 *
	 * @param {LengthUnit} unit Display unit of length.
	 */
	setDisplayUnit( unit )
	{
		this._displayUnit = unit;
	}

	/**
	 * Get minimum scale for fractional part of number (default: 0).
	 *
	 * @return {number} Minimum scale for fractional part of number (0 or higher).
	 */
	getMinScale()
	{
		return this._minScale;
	}

	/**
	 * Set minimum scale for fractional part of number (default: 0).
	 *
	 * @param {number} scale Minimum scale for fractional part of number (0 or higher).
	 */
	setMinScale( scale )
	{
		this._minScale = scale;
	}

	/**
	 * Get maximum scale of fractional part of number (default: 4).
	 *
	 * @return {number} Maximum scale for fractional part of number (0 or higher).
	 */
	getMaxScale()
	{
		return this._maxScale;
	}

	/**
	 * Set maximum scale of fractional part of number (default: 4).
	 *
	 * @param {number} scale Maximum scale for fractional part of number (0 or higher).
	 */
	setMaxScale( scale )
	{
		this._maxScale = scale;
	}

	/**
	 * Get whether to use fractional or decimal notation for numbers.
	 *
	 * @return {boolean} {@code true} if fractional notation is used; {@code false} if decimal notation is used.
	 */
	isFraction()
	{
		return this._fraction;
	}

	/**
	 * Set whether to use fractional or decimal notation for numbers.
	 *
	 * @param {boolean} enabled {@code true} to use fractional notation; {@code false} to use decimal notation.
	 */
	setFraction( enabled )
	{
		this._fraction = enabled;
	}

	/**
	 * Get separator used in fraction to separate the numerator and denominator
	 * (default: slash).
	 *
	 * @return {string} Separator between numerator and denominator.
	 */
	getFractionSeparator()
	{
		return this._fractionSeparator;
	}

	/**
	 * Set separator to use in fraction to separate the numerator and
	 * denominator (default: slash).
	 *
	 * @param {string} separator Separator between numerator and denominator.
	 */
	setFractionSeparator( separator )
	{
		this._fractionSeparator = separator;
	}

	/**
	 * Get whether to include whole and part fractions instead of a single
	 * fraction (default: true). This only applies to fractional numbers.
	 *
	 * @return {boolean} {@code true} if whole and part fractions are included; {@code false} if a single fraction is used.
	 */
	isWhole()
	{
		return this._whole;
	}

	/**
	 * Set whether to include whole and part fractions instead of a single
	 * fraction (default: true). This only applies to fractional numbers.
	 *
	 * @param {boolean} enabled {@code true} to include whole and part fractions; {@code false} to use a single fraction.
	 */
	setWhole( enabled )
	{
		this._whole = enabled;
	}

	/**
	 * Get whether to include the whole fraction, even if it is zero (default:
	 * false).
	 *
	 * @return {boolean} {@code true} to include zero whole fraction.
	 */
	isWholeZero()
	{
		return this._wholeZero;
	}

	/**
	 * Set whether to include the whole fraction, even if it is zero (default:
	 * false).
	 *
	 * @param {boolean} enabled {@code true} to include zero whole fraction.
	 */
	setWholeZero( enabled )
	{
		this._wholeZero = enabled;
	}

	/**
	 * Get separator to include between the whole and part fractions (default:
	 * space).
	 *
	 * @return {string} Separator included between the whole and part fractions.
	 */
	getWholeFractionSeparator()
	{
		return this._wholeFractionSeparator;
	}

	/**
	 * Set separator to include between the whole and part fractions (default:
	 * space).
	 *
	 * @param {string} separator Separator to include between the whole and part fractions.
	 */
	setWholeFractionSeparator( separator )
	{
		this._wholeFractionSeparator = separator;
	}

	/**
	 * Get whether to include feet (default: false).
	 *
	 * @return {boolean} {@code true} if feet are included.
	 */
	isFeet()
	{
		return this._feet;
	}

	/**
	 * Set whether to include feet (default: false).
	 *
	 * @param {boolean} enabled {@code true} to include feet.
	 */
	setFeet( enabled )
	{
		this._feet = enabled;
	}

	/**
	 * Get whether to include feet, even if the number of feet is zero (default:
	 * false).
	 *
	 * @return {boolean} {@code true} if feet are included, even if the number of feet is zero.
	 */
	isFeetZero()
	{
		return this._feetZero;
	}

	/**
	 * Set whether to include feet, even if the number of feet is zero (default:
	 * false).
	 *
	 * @param {boolean} enabled {@code true} to include feet, even if the number of feet is zero.
	 */
	setFeetZero( enabled )
	{
		this._feetZero = enabled;
	}

	/**
	 * Get symbol to indicate feet (default: single straight quote). This is
	 * usually a single quote (') or 'ft'.
	 *
	 * @return {string} Symbol to indicate feet.
	 */
	getFeetSymbol()
	{
		return this._feetSymbol;
	}

	/**
	 * Set symbol to indicate feet (default: single straight quote). This is
	 * usually a single quote (') or 'ft'.
	 *
	 * @param {string} symbol Symbol to indicate feet.
	 */
	setFeetSymbol( symbol )
	{
		this._feetSymbol = symbol;
	}

	/**
	 * Get separator to include between the amount in feet and inches (default:
	 * hyphen).
	 *
	 * @return {string} Separator included between the amount in feet and inches.
	 */
	getFeetInchesSeparator()
	{
		return this._feetInchesSeparator;
	}

	/**
	 * Set separator to include between the amount in feet and inches (default:
	 * hyphen).
	 *
	 * @param {string} separator Separator to include between the amount in feet and inches.
	 */
	setFeetInchesSeparator( separator )
	{
		this._feetInchesSeparator = separator;
	}

	/**
	 * Get whether to include inches, even if the number of inches is zero.
	 *
	 * @return {boolean} {@code true} if inches are included, even if the number of inches is zero.
	 */
	isInchesZero()
	{
		return this._inchesZero;
	}

	/**
	 * Set whether to include inches, even if the number of inches is zero.
	 *
	 * @param {boolean} enabled {@code true} to include inches, even if the number of inches is zero.
	 */
	setInchesZero( enabled )
	{
		this._inchesZero = enabled;
	}

	/**
	 * Get symbol to indicate inches (default: double straight quote).
	 *
	 * @return {string} Symbol to indicate inches.
	 */
	getInchSymbol()
	{
		return this._inchSymbol;
	}

	/**
	 * Set symbol to indicate inches (default: double straight quote).
	 *
	 * @param {string} symbol Symbol to indicate inches.
	 */
	setInchSymbol( symbol )
	{
		this._inchSymbol = symbol;
	}

	/**
	 * Get number format based on these preferences for the given locale.
	 *
	 * @param {Locale} locale Locale to use.
	 *
	 * @return {LengthMeasureFormat}
	 */
	getNumberFormat( locale )
	{
		return new LengthMeasureFormat( this, DecimalFormatSymbols.getInstance( locale ) );
	}

	/**
	 * Returns a string with the display unit and any whitespace needed between
	 * a number and the unit suffix.
	 *
	 * @return Unit suffix.
	 */
	getUnitSuffix()
	{
		let result;
		switch ( this.getDisplayUnit() )
		{
			case LengthMeasurePreferences.LengthUnit.INCH:
				result = !this.getInchSymbol() ? " in" : "";
				break;

			case LengthMeasurePreferences.LengthUnit.FOOT:
				result = !this.getFeetSymbol() ? " ft" : "";
				break;

			case LengthMeasurePreferences.LengthUnit.METER:
				result = " m";
				break;

			case LengthMeasurePreferences.LengthUnit.CENTIMETER:
				result = " cm";
				break;

			case LengthMeasurePreferences.LengthUnit.MILLIMETER:
				result = " mm";
				break;

			default:
				throw new Error( "Unhandled display unit: " + this.getDisplayUnit() );
		}
		return result;
	}
}
