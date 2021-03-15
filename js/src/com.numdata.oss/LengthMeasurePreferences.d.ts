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

import { BigDecimal } from 'bigdecimal';
import BigRational from 'big-rational';
import LengthMeasureFormat from "./LengthMeasureFormat";
import Locale from './Locale';

/**
 * This class provides length measure format preferences.
 *
 * @author Peter S. Heijnen
 * @author Gerrit Meinders (ES6 port)
 */
export default class LengthMeasurePreferences
{
	/**
	 * Name of the {@link #getBaseUnit} property.
	 */
	static BASE_UNIT: string;

	/**
	 * Name of the {@link #getDisplayUnit} property.
	 */
	static DISPLAY_UNIT: string;

	/**
	 * Name of the {@link #getMinScale} property.
	 */
	static MIN_SCALE: string;

	/**
	 * Name of the {@link #getMaxScale} property.
	 */
	static MAX_SCALE: string;

	/**
	 * Name of the {@link #isFraction} property.
	 */
	static FRACTION: string;

	/**
	 * Name of the {@link #getFractionSeparator} property.
	 */
	static FRACTION_SEPARATOR: string;

	/**
	 * Name of the {@link #isWhole} property.
	 */
	static WHOLE: string;

	/**
	 * Name of the {@link #isWholeZero} property.
	 */
	static WHOLE_ZERO: string;

	/**
	 * Name of the {@link #getWholeFractionSeparator} property.
	 */
	static WHOLE_FRACTION_SEPARATOR: string;

	/**
	 * Name of the {@link #isFeet} property.
	 */
	static FEET: string;

	/**
	 * Name of the {@link #isFeetZero} property.
	 */
	static FEET_ZERO: string;

	/**
	 * Name of the {@link #getFeetSymbol} property.
	 */
	static FEET_SYMBOL: string;

	/**
	 * Name of the {@link #getFeetInchesSeparator} property.
	 */
	static FEET_INCHES_SEPARATOR: string;

	/**
	 * Name of the {@link #isInchesZero} property.
	 */
	static INCHES_ZERO: string;

	/**
	 * Name of the {@link #getInchSymbol} property.
	 */
	static INCH_SYMBOL: string;

	/**
	 * Basic unit of length.
	 */
	static LengthUnit: typeof LengthUnit;

	/**
	 * Get preferences for basic decimal format with maximum of 4 fractional
	 * digits.
	 */
	static getMetricInstance(): LengthMeasurePreferences;

	/**
	 * Get preferences for engineering units based on decimal inches.
	 */
	static getEngineeringInstance(): LengthMeasurePreferences;

	/**
	 * Get preferences for architectural units based on fractional inches.
	 */
	static getArchitecturalInstance(): LengthMeasurePreferences;

	/**
	 * Get preferences for units based on fractions.
	 */
	static getFractionalInstance(): LengthMeasurePreferences;

	/**
	 * Creates preferences from a JSON representation.
	 *
	 * @param preferences Preferences as JSON.
	 */
	static fromJSON( preferences: any ): LengthMeasurePreferences;

	/**
	 * Construct new instance.
	 *
	 * @param [original] Original to clone.
	 */
	constructor( original?: any );

	/**
	 * Base unit of length.
	 */
	_baseUnit: any;

	/**
	 * Display unit of length.
	 */
	_displayUnit: any;

	/**
	 * Minimum scale for fractional part of number (default: 0).
	 */
	_minScale: any;

	/**
	 * Maximum scale of fractional part of number (default: 4).
	 */
	_maxScale: any;

	/**
	 * Whether to use fractional or decimal notation for numbers.
	 */
	_fraction: any;

	/**
	 * Separator to use in fraction to separate the numerator and denominator
	 * (default: slash).
	 */
	_fractionSeparator: any;

	/**
	 * Whether to include whole and part fractions instead of a single fraction
	 * (default: true). This only applies to fractional numbers.
	 */
	_whole: any;

	/**
	 * Whether to include the whole fraction, even if it is zero (default:
	 * false).
	 */
	_wholeZero: any;

	/**
	 * Separator to include between the whole and part fractions (default:
	 * space).
	 */
	_wholeFractionSeparator: any;

	/**
	 * Whether to include feet (default: false).
	 */
	_feet: any;

	/**
	 * Whether to include feet, even if the number of feet is zero (default:
	 * false).
	 */
	_feetZero: any;

	/**
	 * Symbol to indicate feet (default: single straight quote). This is usually
	 * a single quote (') or 'ft'.
	 */
	_feetSymbol: any;

	/**
	 * Separator to include between the amount in feet and inches (default:
	 * hyphen).
	 */
	_feetInchesSeparator: any;

	/**
	 * Whether to include inches, even if the number of inches is zero (default:
	 * false).
	 */
	_inchesZero: any;

	/**
	 * Symbol to indicate inches (default: double straight quote).
	 */
	_inchSymbol: any;

	/**
	 * Get base unit of length.
	 *
	 * @return Base unit of length.
	 */
	getBaseUnit(): LengthUnit;

	/**
	 * Set base unit of length.
	 *
	 * @param unit Base unit of length.
	 */
	setBaseUnit( unit: LengthUnit ): void;

	/**
	 * Get display unit of length.
	 *
	 * @return Display unit of length.
	 */
	getDisplayUnit(): LengthUnit;

	/**
	 * Set display unit of length.
	 *
	 * @param unit Display unit of length.
	 */
	setDisplayUnit( unit: LengthUnit ): void;

	/**
	 * Get minimum scale for fractional part of number (default: 0).
	 *
	 * @return Minimum scale for fractional part of number (0 or higher).
	 */
	getMinScale(): number;

	/**
	 * Set minimum scale for fractional part of number (default: 0).
	 *
	 * @param scale Minimum scale for fractional part of number (0 or higher).
	 */
	setMinScale( scale: number ): void;

	/**
	 * Get maximum scale of fractional part of number (default: 4).
	 *
	 * @return Maximum scale for fractional part of number (0 or higher).
	 */
	getMaxScale(): number;

	/**
	 * Set maximum scale of fractional part of number (default: 4).
	 *
	 * @param scale Maximum scale for fractional part of number (0 or higher).
	 */
	setMaxScale( scale: number ): void;

	/**
	 * Get whether to use fractional or decimal notation for numbers.
	 *
	 * @return {@code true} if fractional notation is used; {@code false} if decimal notation is used.
	 */
	isFraction(): boolean;

	/**
	 * Set whether to use fractional or decimal notation for numbers.
	 *
	 * @param enabled {@code true} to use fractional notation; {@code false} to use decimal notation.
	 */
	setFraction( enabled: boolean ): void;

	/**
	 * Get separator used in fraction to separate the numerator and denominator
	 * (default: slash).
	 *
	 * @return Separator between numerator and denominator.
	 */
	getFractionSeparator(): string;

	/**
	 * Set separator to use in fraction to separate the numerator and
	 * denominator (default: slash).
	 *
	 * @param separator Separator between numerator and denominator.
	 */
	setFractionSeparator( separator: string ): void;

	/**
	 * Get whether to include whole and part fractions instead of a single
	 * fraction (default: true). This only applies to fractional numbers.
	 *
	 * @return {@code true} if whole and part fractions are included; {@code false} if a single fraction is used.
	 */
	isWhole(): boolean;

	/**
	 * Set whether to include whole and part fractions instead of a single
	 * fraction (default: true). This only applies to fractional numbers.
	 *
	 * @param enabled {@code true} to include whole and part fractions; {@code false} to use a single fraction.
	 */
	setWhole( enabled: boolean ): void;

	/**
	 * Get whether to include the whole fraction, even if it is zero (default:
	 * false).
	 *
	 * @return {@code true} to include zero whole fraction.
	 */
	isWholeZero(): boolean;

	/**
	 * Set whether to include the whole fraction, even if it is zero (default:
	 * false).
	 *
	 * @param enabled {@code true} to include zero whole fraction.
	 */
	setWholeZero( enabled: boolean ): void;

	/**
	 * Get separator to include between the whole and part fractions (default:
	 * space).
	 *
	 * @return Separator included between the whole and part fractions.
	 */
	getWholeFractionSeparator(): string;

	/**
	 * Set separator to include between the whole and part fractions (default:
	 * space).
	 *
	 * @param separator Separator to include between the whole and part fractions.
	 */
	setWholeFractionSeparator( separator: string ): void;

	/**
	 * Get whether to include feet (default: false).
	 *
	 * @return {@code true} if feet are included.
	 */
	isFeet(): boolean;

	/**
	 * Set whether to include feet (default: false).
	 *
	 * @param enabled {@code true} to include feet.
	 */
	setFeet( enabled: boolean ): void;

	/**
	 * Get whether to include feet, even if the number of feet is zero (default:
	 * false).
	 *
	 * @return {@code true} if feet are included, even if the number of feet is zero.
	 */
	isFeetZero(): boolean;

	/**
	 * Set whether to include feet, even if the number of feet is zero (default:
	 * false).
	 *
	 * @param enabled {@code true} to include feet, even if the number of feet is zero.
	 */
	setFeetZero( enabled: boolean ): void;

	/**
	 * Get symbol to indicate feet (default: single straight quote). This is
	 * usually a single quote (') or 'ft'.
	 *
	 * @return Symbol to indicate feet.
	 */
	getFeetSymbol(): string;

	/**
	 * Set symbol to indicate feet (default: single straight quote). This is
	 * usually a single quote (') or 'ft'.
	 *
	 * @param symbol Symbol to indicate feet.
	 */
	setFeetSymbol( symbol: string ): void;

	/**
	 * Get separator to include between the amount in feet and inches (default:
	 * hyphen).
	 *
	 * @return Separator included between the amount in feet and inches.
	 */
	getFeetInchesSeparator(): string;

	/**
	 * Set separator to include between the amount in feet and inches (default:
	 * hyphen).
	 *
	 * @param separator Separator to include between the amount in feet and inches.
	 */
	setFeetInchesSeparator( separator: string ): void;

	/**
	 * Get whether to include inches, even if the number of inches is zero.
	 *
	 * @return {@code true} if inches are included, even if the number of inches is zero.
	 */
	isInchesZero(): boolean;

	/**
	 * Set whether to include inches, even if the number of inches is zero.
	 *
	 * @param enabled {@code true} to include inches, even if the number of inches is zero.
	 */
	setInchesZero( enabled: boolean ): void;

	/**
	 * Get symbol to indicate inches (default: double straight quote).
	 *
	 * @return Symbol to indicate inches.
	 */
	getInchSymbol(): string;

	/**
	 * Set symbol to indicate inches (default: double straight quote).
	 *
	 * @param symbol Symbol to indicate inches.
	 */
	setInchSymbol( symbol: string ): void;

	/**
	 * Get number format based on these preferences for the given locale.
	 *
	 * @param locale Locale to use.
	 */
	getNumberFormat( locale: Locale ): LengthMeasureFormat;

	/**
	 * Returns a string with the display unit and any whitespace needed between
	 * a number and the unit suffix.
	 *
	 * @return Unit suffix.
	 */
	getUnitSuffix(): string;
}

/**
 * Basic unit of length.
 *
 * @author Peter S. Heijnen
 * @author Gerrit Meinders (ported to Javascript)
 */
declare class LengthUnit
{
	/**
	 * Inch.
	 */
	static INCH: LengthUnit;

	/**
	 * Foot.
	 */
	static FOOT: LengthUnit;

	/**
	 * Metric.
	 */
	static METER: LengthUnit;

	/**
	 * Centimeter.
	 */
	static CENTIMETER: LengthUnit;

	/**
	 * Millimeter.
	 */
	static MILLIMETER: LengthUnit;

	/**
	 * Construct unit.
	 *
	 * @param toMeters Multiplication factor to convert unit to meters.
	 */
	constructor( toMeters: any );

	/**
	 * Multiplication factor to convert unit to meters.
	 */
	_decimalToMeters: any;

	/**
	 * Multiplication factor to convert unit to meters.
	 */
	_rationalToMeters: any;

	/**
	 * Test whether this unit is an imperial unit (based on inches).
	 *
	 * @return {@code true} if this unit is imperial.
	 */
	isImperial(): boolean;

	/**
	 * Test whether this unit is a metric unit (based on meters).
	 *
	 * @return {@code true} if this unit is metric.
	 */
	isMetric(): boolean;

	/**
	 * Converts a measure from one unit to another.
	 *
	 * @param value Measure to convert.
	 * @param toUnit Unit to convert to.
	 * @param [maxScale] Maximum scale of converted unit.
	 *
	 * @return Converted measure.
	 */
	convert( value: BigDecimal | BigRational, toUnit: LengthUnit, maxScale?: number ): BigDecimal | BigRational;

	/**
	 * Converts a measure from one unit to another.
	 *
	 * @param value Measure to convert.
	 * @param toUnit Unit to convert to.
	 * @param maxScale Maximum scale of converted unit.
	 *
	 * @return Converted measure.
	 */
	convertBD( value: BigDecimal, toUnit: LengthUnit, maxScale: number ): BigDecimal;

	/**
	 * Converts a measure from one unit to another.
	 *
	 * @param value  Measure to convert.
	 * @param toUnit Unit to convert to.
	 *
	 * @return Converted measure.
	 */
	convertBR( value: BigRational, toUnit: LengthUnit ): BigRational;

	/**
	 * Short name used to identify the unit, e.g. to choose a units.
	 *
	 * @returns Short unit name.
	 */
	getShortName(): string;
}

export {};
