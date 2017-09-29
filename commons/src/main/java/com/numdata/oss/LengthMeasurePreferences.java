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
package com.numdata.oss;

import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;
import javax.xml.bind.annotation.*;

import org.jetbrains.annotations.*;

/**
 * This class provides length measure format preferences.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings ( "unused" )
@XmlRootElement(name = "LengthMeasurePreferences")
@XmlType( name = "LengthMeasurePreferences", propOrder = { } )
public class LengthMeasurePreferences
implements Serializable
{
	/**
	 * Name of the {@link #getBaseUnit} property.
	 */
	public static final String BASE_UNIT = "baseUnit";

	/**
	 * Name of the {@link #getDisplayUnit} property.
	 */
	public static final String DISPLAY_UNIT = "displayUnit";

	/**
	 * Name of the {@link #getMinScale} property.
	 */
	public static final String MIN_SCALE = "minScale";

	/**
	 * Name of the {@link #getMaxScale} property.
	 */
	public static final String MAX_SCALE = "maxScale";

	/**
	 * Name of the {@link #isFraction} property.
	 */
	public static final String FRACTION = "fraction";

	/**
	 * Name of the {@link #getFractionSeparator} property.
	 */
	public static final String FRACTION_SEPARATOR = "fractionSeparator";

	/**
	 * Name of the {@link #isWhole} property.
	 */
	public static final String WHOLE = "whole";

	/**
	 * Name of the {@link #isWholeZero} property.
	 */
	public static final String WHOLE_ZERO = "wholeZero";

	/**
	 * Name of the {@link #getWholeFractionSeparator} property.
	 */
	public static final String WHOLE_FRACTION_SEPARATOR = "wholeFractionSeparator";

	/**
	 * Name of the {@link #isFeet} property.
	 */
	public static final String FEET = "feet";

	/**
	 * Name of the {@link #isFeetZero} property.
	 */
	public static final String FEET_ZERO = "feetZero";

	/**
	 * Name of the {@link #getFeetSymbol} property.
	 */
	public static final String FEET_SYMBOL = "feetSymbol";

	/**
	 * Name of the {@link #getFeetInchesSeparator} property.
	 */
	public static final String FEET_INCHES_SEPARATOR = "feetInchesSeparator";

	/**
	 * Name of the {@link #isInchesZero} property.
	 */
	public static final String INCHES_ZERO = "inchesZero";

	/**
	 * Name of the {@link #getInchSymbol} property.
	 */
	public static final String INCH_SYMBOL = "inchSymbol";

	/**
	 * Basic unit of length.
	 */
	public enum LengthUnit
	{
		/**
		 * Inch.
		 */
		INCH( new BigDecimal( BigInteger.valueOf( 254 ), 4 ) ),

		/**
		 * Foot.
		 */
		FOOT( new BigDecimal( BigInteger.valueOf( 12 * 254 ), 4 ) ),

		/**
		 * Metric.
		 */
		METER( BigDecimal.ONE ),

		/**
		 * Centimeter.
		 */
		CENTIMETER( BigDecimalTools.ONE_HUNDREDTH ),

		/**
		 * Millimeter.
		 */
		MILLIMETER( BigDecimalTools.ONE_THOUSANDTH );

		/**
		 * Multiplication factor to convert unit to meters.
		 */
		private final BigDecimal _decimalToMeters;

		/**
		 * Multiplication factor to convert unit to meters.
		 */
		private final BigRational _rationalToMeters;

		/**
		 * Construct unit.
		 *
		 * @param toMeters Multiplication factor to convert unit to meters.
		 */
		LengthUnit( final BigDecimal toMeters )
		{
			_decimalToMeters = toMeters;
			_rationalToMeters = new BigRational( toMeters );
		}

		/**
		 * Test whether this unit is an imperial unit (based on inches).
		 *
		 * @return {@code true} if this unit is imperial.
		 */
		public boolean isImperial()
		{
			return ( this == INCH ) || ( this == FOOT );
		}

		/**
		 * Test whether this unit is a metric unit (based on meters).
		 *
		 * @return {@code true} if this unit is metric.
		 */
		public boolean isMetric()
		{
			return !isImperial();
		}

		/**
		 * Converts a measure from one unit to another.
		 *
		 * @param value    Measure to convert.
		 * @param toUnit   Unit to convert to.
		 * @param maxScale Maximum scale of converted unit.
		 *
		 * @return Converted measure.
		 */
		@NotNull
		public BigDecimal convert( @NotNull final BigDecimal value, @NotNull final LengthUnit toUnit, final int maxScale )
		{
			BigDecimal result = value;

			if ( toUnit != this )
			{
				if ( this != METER )
				{
					result = result.multiply( _decimalToMeters );
				}

				if ( toUnit != METER )
				{
					result = result.divide( toUnit._decimalToMeters, maxScale, RoundingMode.HALF_UP );
				}
			}

			return result;
		}


		/**
		 * Converts a measure from one unit to another.
		 *
		 * @param value  Measure to convert.
		 * @param toUnit Unit to convert to.
		 *
		 * @return Converted measure.
		 */
		@NotNull
		public BigRational convert( @NotNull final BigRational value, @NotNull final LengthUnit toUnit )
		{
			return ( toUnit == this ) ? value : value.multiply( _rationalToMeters ).divide( toUnit._rationalToMeters );
		}
	}

	/**
	 * Get preferences for basic decimal format with maximum of 4 fractional
	 * digits.
	 *
	 * @return {@link LengthMeasurePreferences}.
	 */
	public static LengthMeasurePreferences getMetricInstance()
	{
		return new LengthMeasurePreferences();
	}

	/**
	 * Get preferences for engineering units based on decimal inches.
	 *
	 * @return {@link LengthMeasurePreferences}.
	 */
	public static LengthMeasurePreferences getEngineeringInstance()
	{
		final LengthMeasurePreferences result = new LengthMeasurePreferences();
		result.setBaseUnit( LengthUnit.INCH );
		result.setDisplayUnit( LengthUnit.INCH );
		result.setFeet( true );
		return result;
	}

	/**
	 * Get preferences for architectural units based on fractional inches.
	 *
	 * @return {@link LengthMeasurePreferences}.
	 */
	public static LengthMeasurePreferences getArchitecturalInstance()
	{
		final LengthMeasurePreferences result = new LengthMeasurePreferences();
		result.setBaseUnit( LengthUnit.INCH );
		result.setDisplayUnit( LengthUnit.INCH );
		result.setFeet( true );
		result.setFraction( true );
		return result;
	}

	/**
	 * Get preferences for units based on fractions.
	 *
	 * @return {@link LengthMeasurePreferences}.
	 */
	public static LengthMeasurePreferences getFractionalInstance()
	{
		final LengthMeasurePreferences result = new LengthMeasurePreferences();
		result.setFraction( true );
		return result;

	}

	/**
	 * Base unit of length.
	 */
	@NotNull
	private LengthUnit _baseUnit;

	/**
	 * Display unit of length.
	 */
	@NotNull
	private LengthUnit _displayUnit;

	/**
	 * Minimum scale for fractional part of number (default: 0).
	 */
	private int _minScale;

	/**
	 * Maximum scale of fractional part of number (default: 4).
	 */
	private int _maxScale;

	/**
	 * Whether to use fractional or decimal notation for numbers.
	 */
	private boolean _fraction;

	/**
	 * Separator to use in fraction to separate the numerator and denominator
	 * (default: slash).
	 */
	private String _fractionSeparator;

	/**
	 * Whether to include whole and part fractions instead of a single fraction
	 * (default: true). This only applies to fractional numbers.
	 */
	private boolean _whole;

	/**
	 * Whether to include the whole fraction, even if it is zero (default:
	 * false).
	 */
	private boolean _wholeZero;

	/**
	 * Separator to include between the whole and part fractions (default:
	 * space).
	 */
	private String _wholeFractionSeparator;

	/**
	 * Whether to include feet (default: false).
	 */
	private boolean _feet;

	/**
	 * Whether to include feet, even if the number of feet is zero (default:
	 * false).
	 */
	private boolean _feetZero;

	/**
	 * Symbol to indicate feet (default: single straight quote). This is usually
	 * a single quote (') or 'ft'.
	 */
	private String _feetSymbol;

	/**
	 * Separator to include between the amount in feet and inches (default:
	 * hyphen).
	 */
	private String _feetInchesSeparator;

	/**
	 * Whether to include inches, even if the number of inches is zero (default:
	 * false).
	 */
	private boolean _inchesZero;

	/**
	 * Symbol to indicate inches (default: double straight quote).
	 */
	private String _inchSymbol;

	/**
	 * Construct new instance.
	 */
	public LengthMeasurePreferences()
	{
		_baseUnit = LengthUnit.METER;
		_displayUnit = LengthUnit.METER;
		_minScale = 0;
		_maxScale = 6;
		_fraction = false;
		_fractionSeparator = "/";
		_whole = true;
		_wholeZero = false;
		_wholeFractionSeparator = " ";
		_feet = false;
		_feetZero = false;
		_feetSymbol = "'";
		_feetInchesSeparator = "-";
		_inchesZero = false;
		_inchSymbol = "\"";
	}

	/**
	 * Clone constructor.
	 *
	 * @param original Original to clone.
	 */
	public LengthMeasurePreferences( @NotNull final LengthMeasurePreferences original )
	{
		_baseUnit = original.getBaseUnit();
		_displayUnit = original.getDisplayUnit();
		_minScale = original.getMinScale();
		_maxScale = original.getMaxScale();
		_fraction = original.isFraction();
		_fractionSeparator = original.getFractionSeparator();
		_whole = original.isWhole();
		_wholeZero = original.isWholeZero();
		_wholeFractionSeparator = original.getWholeFractionSeparator();
		_feet = original.isFeet();
		_feetZero = original.isFeetZero();
		_feetSymbol = original.getFeetSymbol();
		_feetInchesSeparator = original.getFeetInchesSeparator();
		_inchesZero = original.isInchesZero();
		_inchSymbol = original.getInchSymbol();
	}

	/**
	 * Get base unit of length.
	 *
	 * @return Base unit of length.
	 */
	@NotNull
	public LengthUnit getBaseUnit()
	{
		return _baseUnit;
	}

	/**
	 * Set base unit of length.
	 *
	 * @param unit Base unit of length.
	 */
	public void setBaseUnit( @NotNull final LengthUnit unit )
	{
		_baseUnit = unit;
	}

	/**
	 * Get display unit of length.
	 *
	 * @return Display unit of length.
	 */
	@NotNull
	public LengthUnit getDisplayUnit()
	{
		return _displayUnit;
	}

	/**
	 * Set display unit of length.
	 *
	 * @param unit Display unit of length.
	 */
	public void setDisplayUnit( @NotNull final LengthUnit unit )
	{
		_displayUnit = unit;
	}

	/**
	 * Get minimum scale for fractional part of number (default: 0).
	 *
	 * @return Minimum scale for fractional part of number (0 or higher).
	 */
	public int getMinScale()
	{
		return _minScale;
	}

	/**
	 * Set minimum scale for fractional part of number (default: 0).
	 *
	 * @param scale Minimum scale for fractional part of number (0 or higher).
	 */
	public void setMinScale( final int scale )
	{
		_minScale = scale;
	}

	/**
	 * Get maximum scale of fractional part of number (default: 4).
	 *
	 * @return Maximum scale for fractional part of number (0 or higher).
	 */
	public int getMaxScale()
	{
		return _maxScale;
	}

	/**
	 * Set maximum scale of fractional part of number (default: 4).
	 *
	 * @param scale Maximum scale for fractional part of number (0 or higher).
	 */
	public void setMaxScale( final int scale )
	{
		_maxScale = scale;
	}

	/**
	 * Get whether to use fractional or decimal notation for numbers.
	 *
	 * @return {@code true} of fractional notation is used; {@code false} if
	 * decimal notation is used.
	 */
	public boolean isFraction()
	{
		return _fraction;
	}

	/**
	 * Set whether to use fractional or decimal notation for numbers.
	 *
	 * @param enabled {@code true} to use fractional notation; {@code false} to
	 *                use decimal notation.
	 */
	public void setFraction( final boolean enabled )
	{
		_fraction = enabled;
	}

	/**
	 * Get separator used in fraction to separate the numerator and denominator
	 * (default: slash).
	 *
	 * @return Separator between numerator and denominator.
	 */
	public String getFractionSeparator()
	{
		return _fractionSeparator;
	}

	/**
	 * Set separator to use in fraction to separate the numerator and
	 * denominator (default: slash).
	 *
	 * @param separator Separator between numerator and denominator.
	 */
	public void setFractionSeparator( final String separator )
	{
		_fractionSeparator = separator;
	}

	/**
	 * Get whether to include whole and part fractions instead of a single
	 * fraction (default: true). This only applies to fractional numbers.
	 *
	 * @return {@code true} if whole and part fractions are included; {@code
	 * false} if a single fraction is used.
	 */
	public boolean isWhole()
	{
		return _whole;
	}

	/**
	 * Set whether to include whole and part fractions instead of a single
	 * fraction (default: true). This only applies to fractional numbers.
	 *
	 * @param enabled {@code true} to include whole and part fractions; {@code
	 *                false} to use a single fraction.
	 */
	public void setWhole( final boolean enabled )
	{
		_whole = enabled;
	}

	/**
	 * Get whether to include the whole fraction, even if it is zero (default:
	 * false).
	 *
	 * @return {@code true} to include zero whole fraction.
	 */
	public boolean isWholeZero()
	{
		return _wholeZero;
	}

	/**
	 * Set whether to include the whole fraction, even if it is zero (default:
	 * false).
	 *
	 * @param enabled {@code true} to include zero whole fraction.
	 */
	public void setWholeZero( final boolean enabled )
	{
		_wholeZero = enabled;
	}

	/**
	 * Get separator to include between the whole and part fractions (default:
	 * space).
	 *
	 * @return Separator included between the whole and part fractions.
	 */
	public String getWholeFractionSeparator()
	{
		return _wholeFractionSeparator;
	}

	/**
	 * Set separator to include between the whole and part fractions (default:
	 * space).
	 *
	 * @param separator Separator to include between the whole and part
	 *                  fractions.
	 */
	public void setWholeFractionSeparator( final String separator )
	{
		_wholeFractionSeparator = separator;
	}

	/**
	 * Get whether to include feet (default: false).
	 *
	 * @return {@code true} if feet are included.
	 */
	public boolean isFeet()
	{
		return _feet;
	}

	/**
	 * Set whether to include feet (default: false).
	 *
	 * @param enabled {@code true} to include feet.
	 */
	public void setFeet( final boolean enabled )
	{
		_feet = enabled;
	}

	/**
	 * Get whether to include feet, even if the number of feet is zero (default:
	 * false).
	 *
	 * @return {@code true} if feet are included, even if the number of feet is
	 * zero.
	 */
	public boolean isFeetZero()
	{
		return _feetZero;
	}

	/**
	 * Set whether to include feet, even if the number of feet is zero (default:
	 * false).
	 *
	 * @param enabled {@code true} to include feet, even if the number of feet
	 *                is zero.
	 */
	public void setFeetZero( final boolean enabled )
	{
		_feetZero = enabled;
	}

	/**
	 * Get symbol to indicate feet (default: single straight quote). This is
	 * usually a single quote (') or 'ft'.
	 *
	 * @return Symbol to indicate feet.
	 */
	public String getFeetSymbol()
	{
		return _feetSymbol;
	}

	/**
	 * Set symbol to indicate feet (default: single straight quote). This is
	 * usually a single quote (') or 'ft'.
	 *
	 * @param symbol Symbol to indicate feet.
	 */
	public void setFeetSymbol( final String symbol )
	{
		_feetSymbol = symbol;
	}

	/**
	 * Get separator to include between the amount in feet and inches (default:
	 * hyphen).
	 *
	 * @return Separator included between the amount in feet and inches.
	 */
	public String getFeetInchesSeparator()
	{
		return _feetInchesSeparator;
	}

	/**
	 * Set separator to include between the amount in feet and inches (default:
	 * hyphen).
	 *
	 * @param separator Separator to include between the amount in feet and
	 *                  inches.
	 */
	public void setFeetInchesSeparator( final String separator )
	{
		_feetInchesSeparator = separator;
	}

	/**
	 * Get whether to include inches, even if the number of inches is zero.
	 *
	 * @return {@code true} if inches are included, even if the number of inches
	 * is zero.
	 */
	public boolean isInchesZero()
	{
		return _inchesZero;
	}

	/**
	 * Set whether to include inches, even if the number of inches is zero.
	 *
	 * @param enabled {@code true} to include inches, even if the number of
	 *                inches is zero.
	 */
	public void setInchesZero( final boolean enabled )
	{
		_inchesZero = enabled;
	}

	/**
	 * Get symbol to indicate inches (default: double straight quote).
	 *
	 * @return Symbol to indicate inches.
	 */
	public String getInchSymbol()
	{
		return _inchSymbol;
	}

	/**
	 * Set symbol to indicate inches (default: double straight quote).
	 *
	 * @param symbol Symbol to indicate inches.
	 */
	public void setInchSymbol( final String symbol )
	{
		_inchSymbol = symbol;
	}

	/**
	 * Get number format based on these preferences for the given locale.
	 *
	 * @param locale Locale to use.
	 *
	 * @return {@link LengthMeasureFormat}.
	 */
	public LengthMeasureFormat getNumberFormat( @NotNull final Locale locale )
	{
		return new LengthMeasureFormat( this, DecimalFormatSymbols.getInstance( locale ) );
	}

	/**
	 * Returns a string with the display unit and any whitespace needed between
	 * a number and the unit suffix.
	 *
	 * @return Unit suffix.
	 */
	@NotNull
	public String getUnitSuffix()
	{
		final String result;
		switch ( getDisplayUnit() )
		{
			case INCH:
				result = TextTools.isEmpty( getInchSymbol() ) ? " in" : "";
				break;

			case FOOT:
				result = TextTools.isEmpty( getFeetSymbol() ) ? " ft" : "";
				break;

			case METER:
				result = " m";
				break;

			case CENTIMETER:
				result = " cm";
				break;

			case MILLIMETER:
				result = " mm";
				break;

			default:
				throw new AssertionError( "Unhandled display unit: " + getDisplayUnit() );
		}
		return result;
	}
}
